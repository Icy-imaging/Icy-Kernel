/**
 * 
 */
package plugins.kernel.roi.roi3d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Node;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.common.CollapsibleEvent;
import icy.math.Line3DIterator;
import icy.painter.Anchor3D;
import icy.painter.Anchor3D.Anchor3DPositionListener;
import icy.painter.OverlayEvent;
import icy.painter.OverlayEvent.OverlayEventType;
import icy.painter.OverlayListener;
import icy.painter.VtkPainter;
import icy.roi.ROI;
import icy.roi.ROI3D;
import icy.roi.ROIEvent;
import icy.roi.edit.Point3DAddedROIEdit;
import icy.roi.edit.Point3DMovedROIEdit;
import icy.roi.edit.Point3DRemovedROIEdit;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.geom.Line3D;
import icy.type.geom.Shape3D;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.util.EventUtil;
import icy.util.GraphicsUtil;
import icy.util.ShapeUtil;
import icy.util.StringUtil;
import icy.vtk.IcyVtkPanel;
import icy.vtk.VtkUtil;
import plugins.kernel.canvas.VtkCanvas;
import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkInformation;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkRenderer;

/**
 * Base class for 3D shape ROI (working from 3D control points).
 * 
 * @author Stephane Dallongeville
 */
public class ROI3DShape extends ROI3D implements Shape3D
{
    public class ROI3DShapePainter extends ROI3DPainter implements VtkPainter, Runnable
    {
        // VTK 3D objects
        protected vtkPolyData outline;
        protected vtkPolyDataMapper outlineMapper;
        protected vtkActor outlineActor;
        protected vtkInformation vtkInfo;
        protected vtkCellArray vCells;
        protected vtkPoints vPoints;
        protected vtkPolyData polyData;
        protected vtkPolyDataMapper polyMapper;
        protected vtkActor actor;
        // 3D internal
        protected boolean needRebuild;
        protected double scaling[];
        protected WeakReference<VtkCanvas> canvas3d;
        protected Set<Anchor3D> actorsToAdd;
        protected Set<Anchor3D> actorsToRemove;

        public ROI3DShapePainter()
        {
            super();

            // don't create VTK object on constructor
            outline = null;
            outlineMapper = null;
            outlineActor = null;
            vtkInfo = null;
            vCells = null;
            vPoints = null;
            polyData = null;
            polyMapper = null;
            actor = null;

            scaling = new double[3];
            Arrays.fill(scaling, 1d);

            actorsToAdd = new HashSet<Anchor3D>();
            actorsToRemove = new HashSet<Anchor3D>();

            needRebuild = true;
            canvas3d = new WeakReference<VtkCanvas>(null);
        }

        @Override
        protected void finalize() throws Throwable
        {
            super.finalize();

            // release allocated VTK resources
            if (actor != null)
                actor.Delete();
            if (polyMapper != null)
                polyMapper.Delete();
            if (polyData != null)
                polyData.Delete();
            if (vPoints != null)
                vPoints.Delete();
            if (vCells != null)
                vCells.Delete();
            if (outlineActor != null)
            {
                outlineActor.SetPropertyKeys(null);
                outlineActor.Delete();
            }
            if (vtkInfo != null)
            {
                vtkInfo.Remove(VtkCanvas.visibilityKey);
                vtkInfo.Delete();
            }
            if (outlineMapper != null)
                outlineMapper.Delete();
            if (outline != null)
            {
                outline.GetPointData().GetScalars().Delete();
                outline.GetPointData().Delete();
                outline.Delete();
            }
        };

        protected void initVtkObjects()
        {
            outline = VtkUtil.getOutline(0d, 1d, 0d, 1d, 0d, 1d);
            outlineMapper = new vtkPolyDataMapper();
            outlineMapper.SetInputData(outline);
            outlineActor = new vtkActor();
            outlineActor.SetMapper(outlineMapper);
            // disable picking on the outline
            outlineActor.SetPickable(0);
            // and set it to wireframe representation
            outlineActor.GetProperty().SetRepresentationToWireframe();
            // use vtkInformations to store outline visibility state (hacky)
            vtkInfo = new vtkInformation();
            vtkInfo.Set(VtkCanvas.visibilityKey, 0);
            // VtkCanvas use this to restore correctly outline visibility flag
            outlineActor.SetPropertyKeys(vtkInfo);

            // init poly data object
            polyData = new vtkPolyData();
            polyMapper = new vtkPolyDataMapper();
            polyMapper.SetInputData(polyData);
            actor = new vtkActor();
            actor.SetMapper(polyMapper);

            // initialize color and stroke
            final Color col = getColor();
            final double r = col.getRed() / 255d;
            final double g = col.getGreen() / 255d;
            final double b = col.getBlue() / 255d;

            outlineActor.GetProperty().SetColor(r, g, b);
            final vtkProperty property = actor.GetProperty();
            property.SetPointSize(getStroke());
            property.SetColor(r, g, b);
        }

        /**
         * update 3D painter for 3D canvas (called only when VTK is loaded).
         */
        protected void rebuildVtkObjects()
        {
            final VtkCanvas canvas = canvas3d.get();
            // canvas was closed
            if (canvas == null)
                return;

            final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
            // canvas was closed
            if (vtkPanel == null)
                return;

            final Sequence seq = canvas.getSequence();
            // nothing to update
            if (seq == null)
                return;

            // get scaling
            final double xs = scaling[0];
            final double ys = scaling[1];
            final double zs = scaling[2];

            // update polydata
            final int numPts = controlPoints.size();
            final double[][] vertices = new double[numPts][3];
            final int[] indexes = new int[numPts + 1];
            indexes[0] = numPts;

            if (!controlPoints.isEmpty())
            {
                // add all controls point position
                for (int i = 0; i < numPts; i++)
                {
                    final Point3D point = controlPoints.get(i).getPosition();
                    final double[] vertex = vertices[i];

                    vertex[0] = point.getX() * xs;
                    vertex[1] = point.getY() * ys;
                    vertex[2] = point.getZ() * zs;

                    indexes[i + 1] = i;
                }
            }

            final vtkCellArray previousCells = vCells;
            final vtkPoints previousPoints = vPoints;
            vCells = VtkUtil.getCells(1, indexes);
            vPoints = VtkUtil.getPoints(vertices);

            // get bounds
            final Rectangle3D bounds = getBounds3D();

            // actor can be accessed in canvas3d for rendering so we need to synchronize access
            vtkPanel.lock();
            try
            {
                // update outline data
                VtkUtil.setOutlineBounds(outline, bounds.getMinX() * xs, bounds.getMaxX() * xs, bounds.getMinY() * ys,
                        bounds.getMaxY() * ys, bounds.getMinZ() * zs, bounds.getMaxZ() * zs, canvas);
                outlineMapper.Update();
                // update polygon data from cell and points
                polyData.SetPoints(vPoints);
                polyData.SetLines(vCells);
                polyMapper.Update();

                // release previous allocated VTK objects
                if (previousCells != null)
                    previousCells.Delete();
                if (previousPoints != null)
                    previousPoints.Delete();
            }
            finally
            {
                vtkPanel.unlock();
            }

            // update color and others properties
            updateVtkDisplayProperties();
        }

        protected void updateVtkDisplayProperties()
        {
            if (actor == null)
                return;

            final VtkCanvas cnv = canvas3d.get();
            final vtkProperty vtkProperty = actor.GetProperty();
            final Color col = getDisplayColor();
            final double r = col.getRed() / 255d;
            final double g = col.getGreen() / 255d;
            final double b = col.getBlue() / 255d;
            final double strk = getStroke();
            // final float opacity = getOpacity();

            final IcyVtkPanel vtkPanel = (cnv != null) ? cnv.getVtkPanel() : null;

            // we need to lock canvas as actor can be accessed during rendering
            if (vtkPanel != null)
                vtkPanel.lock();
            try
            {
                // set actors color
                outlineActor.GetProperty().SetColor(r, g, b);
                if (isSelected())
                {
                    outlineActor.GetProperty().SetRepresentationToWireframe();
                    outlineActor.SetVisibility(1);
                    vtkInfo.Set(VtkCanvas.visibilityKey, 1);
                }
                else
                {
                    outlineActor.GetProperty().SetRepresentationToPoints();
                    outlineActor.SetVisibility(0);
                    vtkInfo.Set(VtkCanvas.visibilityKey, 0);
                }
                vtkProperty.SetColor(r, g, b);
                vtkProperty.SetPointSize(strk);
                vtkProperty.SetLineWidth(strk);
                // opacity here is about ROI content, global opacity is handled by Layer
                // vtkProperty.SetOpacity(opacity);
                setVtkObjectsColor(col);
            }
            finally
            {
                if (vtkPanel != null)
                    vtkPanel.unlock();
            }

            // need to repaint
            painterChanged();
        }

        protected void setVtkObjectsColor(Color color)
        {
            if (outline != null)
                VtkUtil.setPolyDataColor(outline, color, canvas3d.get());
            if (polyData != null)
                VtkUtil.setPolyDataColor(polyData, color, canvas3d.get());
        }

        @Override
        protected boolean updateFocus(InputEvent e, Point5D imagePoint, IcyCanvas canvas)
        {
            // specific VTK canvas processing
            if (canvas instanceof VtkCanvas)
            {
                // mouse is over the ROI actor ? --> focus the ROI
                final boolean focused = (actor != null) && (actor == ((VtkCanvas) canvas).getPickedObject());

                setFocused(focused);

                return focused;
            }

            return super.updateFocus(e, imagePoint, canvas);
        }

        @Override
        public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isSelected() && !isReadOnly())
            {
                if (isActiveFor(canvas))
                {
                    ROI3DShape.this.beginUpdate();
                    try
                    {
                        // get control points list
                        final List<Anchor3D> controlPoints = getControlPoints();

                        // send event to controls points first
                        for (Anchor3D pt : controlPoints)
                            pt.keyPressed(e, imagePoint, canvas);

                        // specific action for ROI3DPolyLine
                        if (!e.isConsumed())
                        {
                            final Sequence sequence = canvas.getSequence();

                            switch (e.getKeyCode())
                            {
                                case KeyEvent.VK_DELETE:
                                case KeyEvent.VK_BACK_SPACE:
                                    final Anchor3D selectedPoint = getSelectedPoint();

                                    // try to remove selected point
                                    if (removeSelectedPoint(canvas))
                                    {
                                        // consume event
                                        e.consume();

                                        // add undo operation
                                        if (sequence != null)
                                            sequence.addUndoableEdit(new Point3DRemovedROIEdit(ROI3DShape.this,
                                                    controlPoints, selectedPoint));
                                    }
                                    break;
                            }
                        }
                    }
                    finally
                    {
                        ROI3DShape.this.endUpdate();
                    }
                }
            }

            // then send event to parent
            super.keyPressed(e, imagePoint, canvas);
        }

        @Override
        public void keyReleased(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isSelected() && !isReadOnly())
            {
                if (isActiveFor(canvas))
                {
                    // check we can do the action
                    if (imagePoint != null)
                    {
                        ROI3DShape.this.beginUpdate();
                        try
                        {
                            // send event to controls points first
                            synchronized (controlPoints)
                            {
                                for (Anchor3D pt : controlPoints)
                                    pt.keyReleased(e, imagePoint, canvas);
                            }
                        }
                        finally
                        {
                            ROI3DShape.this.endUpdate();
                        }
                    }
                }
            }

            // then send event to parent
            super.keyReleased(e, imagePoint, canvas);
        }

        @Override
        public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isActiveFor(canvas))
            {
                // check we can do the action
                if (isSelected() && !isReadOnly())
                {
                    ROI3DShape.this.beginUpdate();
                    try
                    {
                        // send event to controls points first
                        synchronized (controlPoints)
                        {
                            for (Anchor3D pt : controlPoints)
                                pt.mousePressed(e, imagePoint, canvas);
                        }

                        // specific action for this ROI
                        if (!e.isConsumed())
                        {
                            if (imagePoint != null)
                            {
                                // left button action
                                if (EventUtil.isLeftMouseButton(e))
                                {
                                    // ROI should not be focused to add point (for multi selection)
                                    if (!isFocused())
                                    {
                                        final boolean insertMode = EventUtil.isControlDown(e);

                                        // insertion mode or creating the ROI ? --> add a new point
                                        if (insertMode || isCreating())
                                        {
                                            // try to add point
                                            final Anchor3D point = addNewPoint(imagePoint.toPoint3D(), insertMode);

                                            // point added ?
                                            if (point != null)
                                            {
                                                // consume event
                                                e.consume();

                                                final Sequence sequence = canvas.getSequence();

                                                // add undo operation
                                                if (sequence != null)
                                                    sequence.addUndoableEdit(
                                                            new Point3DAddedROIEdit(ROI3DShape.this, point));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    finally
                    {
                        ROI3DShape.this.endUpdate();
                    }
                }
            }

            // then send event to parent
            super.mousePressed(e, imagePoint, canvas);
        }

        @Override
        public void mouseReleased(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // not anymore the first move
            firstMove = false;

            if (isSelected() && !isReadOnly())
            {
                // send event to controls points first
                if (isActiveFor(canvas))
                {
                    final Sequence sequence = canvas.getSequence();

                    ROI3DShape.this.beginUpdate();
                    try
                    {
                        // default anchor action on mouse release
                        synchronized (controlPoints)
                        {
                            for (Anchor3D pt : controlPoints)
                                pt.mouseReleased(e, imagePoint, canvas);
                        }
                    }
                    finally
                    {
                        ROI3DShape.this.endUpdate();
                    }

                    // prevent undo operation merging
                    if (sequence != null)
                        sequence.getUndoManager().noMergeForNextEdit();
                }
            }

            // then send event to parent
            super.mouseReleased(e, imagePoint, canvas);
        }

        @Override
        public void mouseClick(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isSelected() && !isReadOnly())
            {
                // send event to controls points first
                if (isActiveFor(canvas))
                {
                    ROI3DShape.this.beginUpdate();
                    try
                    {
                        // default anchor action on mouse click
                        synchronized (controlPoints)
                        {
                            for (Anchor3D pt : controlPoints)
                                pt.mouseClick(e, imagePoint, canvas);
                        }
                    }
                    finally
                    {
                        ROI3DShape.this.endUpdate();
                    }
                }
            }

            // then send event to parent
            super.mouseClick(e, imagePoint, canvas);
        }

        @Override
        public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isActiveFor(canvas))
            {
                // check we can do the action
                if (isSelected() && !isReadOnly())
                {
                    final Sequence sequence = canvas.getSequence();

                    // send event to controls points first
                    ROI3DShape.this.beginUpdate();
                    try
                    {
                        // default anchor action on mouse drag
                        synchronized (controlPoints)
                        {
                            for (Anchor3D pt : controlPoints)
                            {
                                final Point3D savedPosition;

                                // don't want to undo position change on first creation movement
                                if ((sequence != null) && (!isCreating() || !firstMove))
                                    savedPosition = pt.getPosition();
                                else
                                    savedPosition = null;

                                pt.mouseDrag(e, imagePoint, canvas);

                                // position changed and undo supported --> add undo operation
                                if ((sequence != null) && (savedPosition != null)
                                        && !savedPosition.equals(pt.getPosition()))
                                    sequence.addUndoableEdit(
                                            new Point3DMovedROIEdit(ROI3DShape.this, pt, savedPosition));
                            }
                        }
                    }
                    finally
                    {
                        ROI3DShape.this.endUpdate();
                    }
                }
            }

            // then send event to parent
            super.mouseDrag(e, imagePoint, canvas);
        }

        @Override
        public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isActiveFor(canvas))
            {
                // check we can do the action
                if (isSelected() && !isReadOnly())
                {
                    // send event to controls points first
                    ROI3DShape.this.beginUpdate();
                    try
                    {
                        // refresh control point state
                        synchronized (controlPoints)
                        {
                            for (Anchor3D pt : controlPoints)
                                pt.mouseMove(e, imagePoint, canvas);
                        }
                    }
                    finally
                    {
                        ROI3DShape.this.endUpdate();
                    }
                }
            }

            // then send event to parent
            super.mouseMove(e, imagePoint, canvas);
        }

        /**
         * Draw the ROI
         */
        @Override
        public void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (canvas instanceof IcyCanvas2D)
            {
                // not supported
                if (g == null)
                    return;

                final Rectangle2D bounds = getBounds3D().toRectangle2D();

                // enlarge bounds with stroke
                final double over = getAdjustedStroke(canvas) * 2;
                ShapeUtil.enlarge(bounds, over, over, true);

                // define LOD level
                final boolean shapeVisible = isVisible(bounds, g, canvas);

                if (shapeVisible)
                {
                    final boolean small = isSmall(bounds, g, canvas);

                    // draw shape
                    drawShape(g, sequence, canvas, small);

                    // draw control points (only if not tiny)
                    if (!isTiny(bounds, g, canvas) && isSelected() && !isReadOnly())
                    {
                        // draw control point if selected
                        synchronized (controlPoints)
                        {
                            for (Anchor3D pt : controlPoints)
                                pt.paint(g, sequence, canvas, small);
                        }
                    }
                }
            }

            if (canvas instanceof VtkCanvas)
            {
                // 3D canvas
                final VtkCanvas cnv = (VtkCanvas) canvas;
                // update reference if needed
                if (canvas3d.get() != cnv)
                    canvas3d = new WeakReference<VtkCanvas>(cnv);

                // initialize VTK objects if not yet done
                if (actor == null)
                    initVtkObjects();

                // FIXME : need a better implementation
                final double[] s = cnv.getVolumeScale();

                // scaling changed ?
                if (!Arrays.equals(scaling, s))
                {
                    // update scaling
                    scaling = s;
                    // need rebuild
                    needRebuild = true;
                }

                // need to rebuild 3D data structures ?
                if (needRebuild)
                {
                    // request rebuild 3D objects
                    ThreadUtil.runSingle(this);
                    needRebuild = false;
                }

                final vtkRenderer renderer = cnv.getRenderer();

                // need to remove control points actor ?
                synchronized (actorsToRemove)
                {
                    for (Anchor3D anchor : actorsToRemove)
                        for (vtkProp prop : anchor.getProps())
                            VtkUtil.removeProp(renderer, prop);

                    // done
                    actorsToRemove.clear();
                }
                // need to add control points actor ?
                synchronized (actorsToAdd)
                {
                    for (Anchor3D anchor : actorsToAdd)
                        for (vtkProp prop : anchor.getProps())
                            VtkUtil.addProp(renderer, prop);

                    // done
                    actorsToAdd.clear();
                }

                // needed to forward paint event to control point
                synchronized (controlPoints)
                {
                    for (Anchor3D pt : controlPoints)
                        pt.paint(null, sequence, canvas);
                }
            }
        }

        /**
         * Draw the shape in specified Graphics2D context.<br>
         * Override {@link #drawShape(Graphics2D, Sequence, IcyCanvas, boolean, boolean)} instead if possible.
         */
        protected void drawShape(Graphics2D g, Sequence sequence, IcyCanvas canvas, boolean simplified)
        {
            drawShape(g, sequence, canvas, simplified, true);
        }

        /**
         * Draw the shape in specified Graphics2D context.<br>
         * Default implementation just draw '3D' lines between all controls points
         */
        protected void drawShape(Graphics2D g, Sequence sequence, IcyCanvas canvas, boolean simplified,
                boolean connectLastPoint)
        {
            final List<Point3D> points = getPointsInternal();
            final Graphics2D g2 = (Graphics2D) g.create();

            // normal rendering without selection --> draw border first
            if (!simplified && !isSelected())
            {
                // draw border
                g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke + 1d), BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER));
                g2.setColor(Color.black);

                for (int i = 1; i < points.size(); i++)
                    drawLine3D(g2, sequence, canvas, points.get(i - 1), points.get(i));
                // connect last point
                if (connectLastPoint && (points.size() > 2))
                    drawLine3D(g2, sequence, canvas, points.get(points.size() - 1), points.get(0));
            }

            // then draw shape
            g2.setStroke(new BasicStroke(
                    (float) ROI.getAdjustedStroke(canvas, (!simplified && isSelected()) ? stroke + 1 : stroke),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            g2.setColor(getDisplayColor());

            for (int i = 1; i < points.size(); i++)
                drawLine3D(g2, sequence, canvas, points.get(i - 1), points.get(i));
            // connect last point
            if (connectLastPoint && (points.size() > 2))
                drawLine3D(g2, sequence, canvas, points.get(points.size() - 1), points.get(0));

            g2.dispose();
        }

        /**
         * Returns <code>true</code> if the specified bounds should be considered as "tiny" in the
         * specified canvas / graphics context.
         */
        protected boolean isVisible(Rectangle2D bounds, Graphics2D g, IcyCanvas canvas)
        {
            return GraphicsUtil.isVisible(g, bounds);
        }

        /**
         * Returns <code>true</code> if the specified bounds should be considered as "tiny" in the
         * specified canvas / graphics context.
         */
        protected boolean isSmall(Rectangle2D bounds, Graphics2D g, IcyCanvas canvas)
        {
            if (isCreating())
                return false;

            final double scale = Math.max(Math.abs(canvas.getScaleX()), Math.abs(canvas.getScaleY()));
            final double size = Math.max(scale * bounds.getWidth(), scale * bounds.getHeight());

            return size < LOD_SMALL;
        }

        /**
         * Returns <code>true</code> if the specified bounds should be considered as "tiny" in the
         * specified canvas / graphics context.
         */
        protected boolean isTiny(Rectangle2D bounds, Graphics2D g, IcyCanvas canvas)
        {
            if (isCreating())
                return false;

            final double scale = Math.max(Math.abs(canvas.getScaleX()), Math.abs(canvas.getScaleY()));
            final double size = Math.max(scale * bounds.getWidth(), scale * bounds.getHeight());

            return size < LOD_TINY;
        }

        @Override
        public void setColor(Color value)
        {
            beginUpdate();
            try
            {
                super.setColor(value);

                // also change colors of controls points
                final Color focusedColor = getFocusedColor();

                synchronized (controlPoints)
                {
                    for (Anchor3D anchor : controlPoints)
                    {
                        anchor.setColor(value);
                        anchor.setSelectedColor(focusedColor);
                    }
                }
            }
            finally
            {
                endUpdate();
            }
        }

        @Override
        public vtkProp[] getProps()
        {
            // initialize VTK objects if not yet done
            if (actor == null)
                initVtkObjects();

            final List<vtkProp> result = new ArrayList<vtkProp>();

            // add VTK objects from ROI shape
            result.add(actor);
            result.add(outlineActor);

            // then add VTK objects from controls points
            synchronized (controlPoints)
            {
                for (Anchor3D pt : controlPoints)
                    for (vtkProp prop : pt.getProps())
                        result.add(prop);
            }

            return result.toArray(new vtkProp[result.size()]);
        }

        @Override
        public void run()
        {
            rebuildVtkObjects();
        }
    }

    /**
     * Draw a 3D line in specified graphics object
     */
    protected static void drawLine3D(Graphics2D g, Sequence sequence, IcyCanvas canvas, Point3D p1, Point3D p2)
    {
        final Line2D line2d = new Line2D.Double();

        // get canvas Z position
        final int cnvZ = canvas.getPositionZ();
        // calculate z fade range
        final double zRange = Math.min(10d, Math.max(3d, sequence.getSizeZ() / 8d));

        // same Z, don't need to split lines
        if (p1.getZ() == p2.getZ())
            drawSegment3D(g, p1, p2, zRange, cnvZ, line2d);
        else
        {
            final Line3DIterator it = new Line3DIterator(new Line3D(p1, p2), 4d / canvas.getScaleX());
            // start position
            Point3D pos = it.next();

            do
            {
                // get next position
                final Point3D nextPos = it.next();
                // draw line
                drawSegment3D(g, pos, nextPos, zRange, cnvZ, line2d);
                // update current pos
                pos = nextPos;
            }
            while (it.hasNext());
        }
    }

    /**
     * Draw a 3D line in specified graphics object
     */
    protected static void drawSegment3D(Graphics2D g, Point3D p1, Point3D p2, double zRange, int canvasZ, Line2D line2d)
    {
        // get Line Z pos
        final double meanZ = (p1.getZ() + p2.getZ()) / 2d;
        // get delta Z (difference between canvas Z position and line Z pos)
        final double dz = Math.abs(meanZ - canvasZ);

        // not visible on this Z position
        if (dz > zRange)
            return;

        // ratio for size / opacity
        final float ratio = 1f - (float) (dz / zRange);
        final Composite prevComposite = g.getComposite();

        if (ratio != 1f)
            GraphicsUtil.mixAlpha(g, ratio);

        // draw line
        line2d.setLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        g.draw(line2d);

        // restore composite
        g.setComposite(prevComposite);
    }

    public static final String ID_POINTS = "points";
    public static final String ID_POINT = "point";

    /**
     * Polyline3D shape (in image coordinates)
     */
    protected final Shape3D shape;
    /**
     * control points
     */
    protected final List<Anchor3D> controlPoints;

    /**
     * internals
     */
    protected final Anchor3DPositionListener anchor2DPositionListener;
    protected final OverlayListener anchor2DOverlayListener;
    protected boolean firstMove;

    /**
     * 
     */
    public ROI3DShape(Shape3D shape)
    {
        super();

        this.shape = shape;
        controlPoints = new ArrayList<Anchor3D>();
        firstMove = true;

        anchor2DPositionListener = new Anchor3DPositionListener()
        {
            @Override
            public void positionChanged(Anchor3D source)
            {
                controlPointPositionChanged(source);
            }
        };
        anchor2DOverlayListener = new OverlayListener()
        {
            @Override
            public void overlayChanged(OverlayEvent event)
            {
                controlPointOverlayChanged(event);
            }
        };
    }

    @Override
    public String getDefaultName()
    {
        return "Shape3D";
    }

    @Override
    protected ROI3DShapePainter createPainter()
    {
        return new ROI3DShapePainter();
    }

    /**
     * build a new anchor with specified position
     */
    protected Anchor3D createAnchor(Point3D pos)
    {
        return new Anchor3D(pos.getX(), pos.getY(), pos.getZ(), getColor(), getFocusedColor());
    }

    /**
     * @return the shape
     */
    public Shape3D getShape()
    {
        return shape;
    }

    /**
     * Return true if this ROI support adding new point
     */
    public boolean canAddPoint()
    {
        return true;
    }

    /**
     * Return true if this ROI support removing point
     */
    public boolean canRemovePoint()
    {
        return true;
    }

    /**
     * Internal use only
     */
    protected void addPoint(Anchor3D pt)
    {
        addPoint(pt, -1);
    }

    /**
     * Internal use only, use {@link #addNewPoint(Point3D, boolean)} instead.
     */
    public void addPoint(Anchor3D pt, int index)
    {
        // set visible state
        pt.setVisible(isSelected());

        pt.addPositionListener(anchor2DPositionListener);
        pt.addOverlayListener(anchor2DOverlayListener);

        if (index == -1)
            controlPoints.add(pt);
        else
            controlPoints.add(index, pt);

        synchronized (((ROI3DShapePainter) getOverlay()).actorsToAdd)
        {
            // store it in the "actor to add" list
            ((ROI3DShapePainter) getOverlay()).actorsToAdd.add(pt);
        }
        synchronized (((ROI3DShapePainter) getOverlay()).actorsToRemove)
        {
            // and remove it from the "actor to remove" list
            ((ROI3DShapePainter) getOverlay()).actorsToRemove.remove(pt);
        }

        roiChanged(true);
    }

    /**
     * Add a new point to the Polyline 3D ROI.
     * 
     * @param pos
     *        position of the new point
     * @param insert
     *        if set to <code>true</code> the new point will be inserted between the 2 closest
     *        points (in pixels distance) else the new point is inserted at the end of the point
     *        list
     * @return the new created Anchor3D point
     */
    public Anchor3D addNewPoint(Point3D pos, boolean insert)
    {
        if (!canAddPoint())
            return null;

        final Anchor3D pt = createAnchor(pos);

        if (insert)
            // insert mode ? --> place the new point with closest points
            addPoint(pt, getInsertPointPosition(pos));
        else
            // just add the new point at last position
            addPoint(pt);

        // always select
        pt.setSelected(true);

        return pt;
    }

    /**
     * internal use only
     */
    protected boolean removePoint(IcyCanvas canvas, Anchor3D pt)
    {
        boolean empty;

        pt.removeOverlayListener(anchor2DOverlayListener);
        pt.removePositionListener(anchor2DPositionListener);

        synchronized (controlPoints)
        {
            controlPoints.remove(pt);
            empty = controlPoints.isEmpty();
        }

        synchronized (((ROI3DShapePainter) getOverlay()).actorsToRemove)
        {
            // store it in the "actor to remove" list
            ((ROI3DShapePainter) getOverlay()).actorsToRemove.add(pt);
        }
        synchronized (((ROI3DShapePainter) getOverlay()).actorsToAdd)
        {
            // and remove it from the "actor to add" list
            ((ROI3DShapePainter) getOverlay()).actorsToAdd.remove(pt);
        }

        // empty ROI ? --> remove from all sequence
        if (empty)
            remove();
        else
            roiChanged(true);

        return true;
    }

    /**
     * This method give you lower level access on point remove operation but can be unsafe.<br/>
     * Use {@link #removeSelectedPoint(IcyCanvas)} when possible.
     */
    public boolean removePoint(Anchor3D pt)
    {
        return removePoint(null, pt);
    }

    /**
     * internal use only (used for fast clear)
     */
    protected void removeAllPoint()
    {
        synchronized (controlPoints)
        {
            synchronized (((ROI3DShapePainter) getOverlay()).actorsToRemove)
            {
                // store all points in the "actor to remove" list
                ((ROI3DShapePainter) getOverlay()).actorsToRemove.addAll(controlPoints);
            }
            synchronized (((ROI3DShapePainter) getOverlay()).actorsToAdd)
            {
                // and remove them from the "actor to add" list
                ((ROI3DShapePainter) getOverlay()).actorsToAdd.removeAll(controlPoints);
            }

            for (Anchor3D pt : controlPoints)
            {
                pt.removeOverlayListener(anchor2DOverlayListener);
                pt.removePositionListener(anchor2DPositionListener);
            }

            controlPoints.clear();
        }
    }

    /**
     * Remove the current selected point.
     */
    public boolean removeSelectedPoint(IcyCanvas canvas)
    {
        if (!canRemovePoint())
            return false;

        final Anchor3D selectedPoint = getSelectedPoint();

        if (selectedPoint == null)
            return false;

        synchronized (controlPoints)
        {
            // try to remove point
            if (!removePoint(canvas, selectedPoint))
                return false;

            // still have control points
            if (controlPoints.size() > 0)
            {
                // save the point position
                final Point3D imagePoint = selectedPoint.getPosition();

                // select a new point if possible
                if (controlPoints.size() > 0)
                    selectPointAt(canvas, imagePoint);
            }
        }

        return true;
    }

    protected Anchor3D getSelectedPoint()
    {
        synchronized (controlPoints)
        {
            for (Anchor3D pt : controlPoints)
                if (pt.isSelected())
                    return pt;
        }

        return null;
    }

    @Override
    public boolean hasSelectedPoint()
    {
        return (getSelectedPoint() != null);
    }

    protected boolean selectPointAt(IcyCanvas canvas, Point3D imagePoint)
    {
        synchronized (controlPoints)
        {
            // find the new selected control point
            for (Anchor3D pt : controlPoints)
            {
                // control point is overlapped ?
                if (pt.isOver(canvas, imagePoint))
                {
                    // select it
                    pt.setSelected(true);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void unselectAllPoints()
    {
        beginUpdate();
        try
        {
            synchronized (controlPoints)
            {
                // unselect all point
                for (Anchor3D pt : controlPoints)
                    pt.setSelected(false);
            }
        }
        finally
        {
            endUpdate();
        }
    };

    @SuppressWarnings("static-method")
    protected double getTotalDistance(List<Point3D> points, double factorX, double factorY, double factorZ)
    {
        // default implementation
        return Point3D.getTotalDistance(points, factorX, factorY, factorZ, true);
    }

    @Override
    public double getLength(Sequence sequence)
    {
        return getTotalDistance(getPointsInternal(), sequence.getPixelSizeX(), sequence.getPixelSizeY(),
                sequence.getPixelSizeZ());
    }

    @Override
    public double computeSurfaceArea(Sequence sequence)
    {
        return 0d;
    }

    @Override
    public double computeNumberOfContourPoints()
    {
        return getTotalDistance(getPointsInternal(), 1d, 1d, 1d);
    }

    /**
     * Find best insert position for specified point
     */
    protected int getInsertPointPosition(Point3D pos)
    {
        final List<Point3D> points = getPointsInternal();

        final int size = points.size();
        // by default we use last position
        int result = size;
        double minDistance = Double.MAX_VALUE;

        // we try all cases
        for (int i = size; i >= 0; i--)
        {
            // add point at current position
            points.add(i, pos);

            // calculate total distance
            final double d = getTotalDistance(points, 1d, 1d, 1d);
            // minimum distance ?
            if (d < minDistance)
            {
                // save index
                minDistance = d;
                result = i;
            }

            // remove point from current position
            points.remove(i);
        }

        return result;
    }

    // @Override
    // public boolean isOverPoint(IcyCanvas canvas, double x, double y)
    // {
    // if (isSelected())
    // {
    // for (Anchor3D pt : controlPoints)
    // if (pt.isOver(canvas, x, y))
    // return true;
    // }
    //
    // return false;
    // }

    /**
     * Return the list of control points for this ROI.
     */
    public List<Anchor3D> getControlPoints()
    {
        synchronized (controlPoints)
        {
            return new ArrayList<Anchor3D>(controlPoints);
        }
    }

    /**
     * Return the list of position for all control points of the ROI.
     */
    public List<Point3D> getPoints()
    {
        final List<Point3D> result = new ArrayList<Point3D>();

        synchronized (controlPoints)
        {
            for (Anchor3D pt : controlPoints)
                result.add(pt.getPosition());
        }

        return result;
    }

    /**
     * Return the list of positions of control points for this ROI.<br>
     * This is the direct internal position reference, don't modify them !
     */
    protected List<Point3D> getPointsInternal()
    {
        final List<Point3D> result = new ArrayList<Point3D>();

        synchronized (controlPoints)
        {
            for (Anchor3D pt : controlPoints)
                result.add(pt.getPositionInternal());
        }

        return result;
    }

    /**
     * Returns true if specified point coordinates overlap the ROI edge.
     */
    @Override
    public boolean isOverEdge(IcyCanvas canvas, double x, double y, double z)
    {
        // use bigger stroke for isOver test for easier intersection
        final double strk = painter.getAdjustedStroke(canvas) * 3;
        final Rectangle3D rect = new Rectangle3D.Double(x - (strk * 0.5), y - (strk * 0.5), z - (strk * 0.5), strk,
                strk, strk);

        return intersects(rect);
    }

    @Override
    public boolean contains(Point3D p)
    {
        return shape.contains(p);
    }

    @Override
    public boolean contains(Rectangle3D r)
    {
        return shape.contains(r);
    }

    @Override
    public boolean contains(double x, double y, double z)
    {
        return shape.contains(x, y, z);
    }

    @Override
    public boolean contains(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        return shape.contains(x, y, z, sizeX, sizeY, sizeZ);
    }

    @Override
    public boolean intersects(Rectangle3D r)
    {
        return shape.intersects(r);
    }

    @Override
    public boolean intersects(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        return shape.intersects(x, y, z, sizeX, sizeY, sizeZ);
    }

    @Override
    public Rectangle3D computeBounds3D()
    {
        final Rectangle3D result = shape.getBounds();
        
        // shape shouldn't be empty (even for single Point) --> always use a minimal bounds
        if (result.isEmpty())
        {
            result.setSizeX(0.001d);
            result.setSizeY(0.001d);
            result.setSizeZ(0.001d);
        }

        return result;
    }

    @Override
    public boolean canTranslate()
    {
        return true;
    }

    @Override
    public void translate(double dx, double dy, double dz)
    {
        beginUpdate();
        try
        {
            synchronized (controlPoints)
            {
                for (Anchor3D pt : controlPoints)
                    pt.translate(dx, dy, dz);
            }
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Called when anchor position changed
     */
    public void controlPointPositionChanged(Anchor3D source)
    {
        // anchor(s) position changed --> ROI changed
        roiChanged(true);
    }

    /**
     * Called when anchor overlay changed
     */
    public void controlPointOverlayChanged(OverlayEvent event)
    {
        // we only mind about painter change from anchor...
        if (event.getType() == OverlayEventType.PAINTER_CHANGED)
        {
            // we have a control point selected --> remove focus on ROI
            if (hasSelectedPoint())
                setFocused(false);

            // anchor changed --> ROI painter changed
            getOverlay().painterChanged();
        }
    }

    /**
     * roi changed
     */
    @Override
    public void onChanged(CollapsibleEvent object)
    {
        final ROIEvent event = (ROIEvent) object;

        // do here global process on ROI change
        switch (event.getType())
        {
            case ROI_CHANGED:
                // refresh shape
                updateShape();
                break;

            case FOCUS_CHANGED:
                ((ROI3DShapePainter) getOverlay()).updateVtkDisplayProperties();
                break;

            case SELECTION_CHANGED:
                final boolean s = isSelected();

                beginUpdate();
                try
                {
                    // set control points visible or not
                    synchronized (controlPoints)
                    {
                        for (Anchor3D pt : controlPoints)
                            pt.setVisible(s);
                    }

                    // unselect if not visible
                    if (!s)
                        unselectAllPoints();
                }
                finally
                {
                    endUpdate();
                }

                ((ROI3DShapePainter) getOverlay()).updateVtkDisplayProperties();
                break;

            case PROPERTY_CHANGED:
                final String property = event.getPropertyName();

                if (StringUtil.equals(property, PROPERTY_STROKE) || StringUtil.equals(property, PROPERTY_COLOR)
                        || StringUtil.equals(property, PROPERTY_OPACITY))
                    ((ROI3DShapePainter) getOverlay()).updateVtkDisplayProperties();
                break;

            default:
                break;
        }

        super.onChanged(object);
    }

    @Override
    public double computeNumberOfPoints()
    {
        return 0d;
    }

    /**
     * Rebuild shape.<br>
     * This method should be overridden by derived classes which<br>
     * have to call the super.updateShape() method at end.
     */
    protected void updateShape()
    {
        // the shape should have been rebuilt here
        ((ROI3DShapePainter) painter).needRebuild = true;
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            firstMove = false;
            // unselect all control points
            unselectAllPoints();
        }
        finally
        {
            endUpdate();
        }

        return true;
    }
}
