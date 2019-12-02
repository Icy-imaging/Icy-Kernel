/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.kernel.roi.roi2d;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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
import icy.painter.Anchor2D;
import icy.painter.Anchor2D.Anchor2DPositionListener;
import icy.painter.OverlayEvent;
import icy.painter.OverlayEvent.OverlayEventType;
import icy.painter.OverlayListener;
import icy.painter.PainterEvent;
import icy.painter.PathAnchor2D;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROIEvent;
import icy.roi.edit.Point2DAddedROIEdit;
import icy.roi.edit.Point2DMovedROIEdit;
import icy.roi.edit.Point2DRemovedROIEdit;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.point.Point2DUtil;
import icy.type.point.Point5D;
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
 * @author Stephane
 */
public abstract class ROI2DShape extends ROI2D implements Shape
{
    public class ROI2DShapePainter extends ROI2DPainter implements Runnable
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
        protected Set<Anchor2D> actorsToAdd;
        protected Set<Anchor2D> actorsToRemove;

        public ROI2DShapePainter()
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

            actorsToAdd = new HashSet<Anchor2D>();
            actorsToRemove = new HashSet<Anchor2D>();

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

            // get bounds
            final double xs = scaling[0];
            final double ys = scaling[1];
            final double zs = scaling[2];
            double z0, z1;
            final double curZ = getZ();

            // all slices ?
            if (curZ == -1d)
            {
                // set object depth on whole volume
                z0 = 0;
                z1 = seq.getSizeZ() * zs;
            }
            // fixed Z position
            else
            {
                // set Z position
                z0 = curZ * zs;
                z1 = (curZ + 1d) * zs;
                // z0 = (curZ - 0.5) * scaling[2];
                // z1 = (curZ + 0.5) * scaling[2];
            }

            // update polydata object
            final List<double[]> point3DList = new ArrayList<double[]>();
            final List<int[]> polyList = new ArrayList<int[]>();
            final double[] coords = new double[6];

            // starting position
            double xm = 0d;
            double ym = 0d;
            double x0 = 0d;
            double y0 = 0d;
            double x1 = 0d;
            double y1 = 0d;
            int ind;

            // use flat path
            final PathIterator path = getPathIterator(null, 0.5d);

            // build point data
            while (!path.isDone())
            {
                switch (path.currentSegment(coords))
                {
                    case PathIterator.SEG_MOVETO:
                        x0 = xm = coords[0] * xs;
                        y0 = ym = coords[1] * ys;
                        break;

                    case PathIterator.SEG_LINETO:
                        x1 = coords[0] * xs;
                        y1 = coords[1] * ys;

                        ind = point3DList.size();

                        point3DList.add(new double[] {x0, y0, z0});
                        point3DList.add(new double[] {x1, y1, z0});
                        point3DList.add(new double[] {x0, y0, z1});
                        point3DList.add(new double[] {x1, y1, z1});
                        polyList.add(new int[] {1 + ind, 2 + ind, 0 + ind});
                        polyList.add(new int[] {3 + ind, 2 + ind, 1 + ind});

                        x0 = x1;
                        y0 = y1;
                        break;

                    case PathIterator.SEG_CLOSE:
                        x1 = xm;
                        y1 = ym;

                        ind = point3DList.size();

                        point3DList.add(new double[] {x0, y0, z0});
                        point3DList.add(new double[] {x1, y1, z0});
                        point3DList.add(new double[] {x0, y0, z1});
                        point3DList.add(new double[] {x1, y1, z1});
                        polyList.add(new int[] {1 + ind, 2 + ind, 0 + ind});
                        polyList.add(new int[] {3 + ind, 2 + ind, 1 + ind});

                        x0 = x1;
                        y0 = y1;
                        break;
                }

                path.next();
            }

            // convert to array
            final double[][] vertices = new double[point3DList.size()][3];
            final int[][] indexes = new int[polyList.size()][3];

            ind = 0;
            for (double[] pt3D : point3DList)
                vertices[ind++] = pt3D;

            ind = 0;
            for (int[] poly : polyList)
                indexes[ind++] = poly;

            final vtkCellArray previousCells = vCells;
            final vtkPoints previousPoints = vPoints;
            vCells = VtkUtil.getCells(polyList.size(), VtkUtil.prepareCells(indexes));
            vPoints = VtkUtil.getPoints(vertices);

            final Rectangle2D bounds = getBounds2D();

            // actor can be accessed in canvas3d for rendering so we need to synchronize access
            vtkPanel.lock();
            try
            {
                // update outline data
                VtkUtil.setOutlineBounds(outline, bounds.getMinX() * xs, bounds.getMaxX() * xs, bounds.getMinY() * ys,
                        bounds.getMaxY() * ys, z0, z1, canvas);
                outlineMapper.Update();
                // update polygon data from cell and points
                polyData.SetPolys(vCells);
                polyData.SetPoints(vPoints);
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
                    ROI2DShape.this.beginUpdate();
                    try
                    {
                        // get control points list
                        final List<Anchor2D> controlPoints = getControlPoints();

                        // send event to controls points first
                        for (Anchor2D pt : controlPoints)
                            pt.keyPressed(e, imagePoint, canvas);

                        // specific action for ROI2DShape
                        if (!e.isConsumed())
                        {
                            final Sequence sequence = canvas.getSequence();

                            switch (e.getKeyCode())
                            {
                                case KeyEvent.VK_DELETE:
                                case KeyEvent.VK_BACK_SPACE:
                                    final Anchor2D selectedPoint = getSelectedPoint();

                                    // try to remove selected point
                                    if (removeSelectedPoint(canvas))
                                    {
                                        // consume event
                                        e.consume();

                                        // add undo operation
                                        if (sequence != null)
                                            sequence.addUndoableEdit(new Point2DRemovedROIEdit(ROI2DShape.this,
                                                    controlPoints, selectedPoint));
                                    }
                                    break;
                            }
                        }
                    }
                    finally
                    {
                        ROI2DShape.this.endUpdate();
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
                    ROI2DShape.this.beginUpdate();
                    try
                    {
                        // send event to controls points first
                        synchronized (controlPoints)
                        {
                            for (Anchor2D pt : controlPoints)
                                pt.keyReleased(e, imagePoint, canvas);
                        }
                    }
                    finally
                    {
                        ROI2DShape.this.endUpdate();
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
                    ROI2DShape.this.beginUpdate();
                    try
                    {
                        // send event to controls points first
                        synchronized (controlPoints)
                        {
                            for (Anchor2D pt : controlPoints)
                                pt.mousePressed(e, imagePoint, canvas);
                        }

                        // specific action for this ROI
                        if (!e.isConsumed())
                        {
                            // add point operation not supported on VtkCanvas (it could be but we don't want it)
                            if (canvas instanceof VtkCanvas)
                                return;
                            // we need it
                            if (imagePoint == null)
                                return;

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
                                        final Anchor2D point = addNewPoint(imagePoint.toPoint2D(), insertMode);

                                        // point added ?
                                        if (point != null)
                                        {
                                            // consume event
                                            e.consume();

                                            final Sequence sequence = canvas.getSequence();

                                            // add undo operation
                                            if (sequence != null)
                                                sequence.addUndoableEdit(
                                                        new Point2DAddedROIEdit(ROI2DShape.this, point));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    finally
                    {
                        ROI2DShape.this.endUpdate();
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

                    ROI2DShape.this.beginUpdate();
                    try
                    {
                        // default anchor action on mouse release
                        synchronized (controlPoints)
                        {
                            for (Anchor2D pt : controlPoints)
                                pt.mouseReleased(e, imagePoint, canvas);
                        }
                    }
                    finally
                    {
                        ROI2DShape.this.endUpdate();
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
                    ROI2DShape.this.beginUpdate();
                    try
                    {
                        // default anchor action on mouse click
                        synchronized (controlPoints)
                        {
                            for (Anchor2D pt : controlPoints)
                                pt.mouseClick(e, imagePoint, canvas);
                        }
                    }
                    finally
                    {
                        ROI2DShape.this.endUpdate();
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
                    ROI2DShape.this.beginUpdate();
                    try
                    {
                        // default anchor action on mouse drag
                        synchronized (controlPoints)
                        {
                            for (Anchor2D pt : controlPoints)
                            {
                                final Point2D savedPosition;

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
                                            new Point2DMovedROIEdit(ROI2DShape.this, pt, savedPosition));
                            }
                        }
                    }
                    finally
                    {
                        ROI2DShape.this.endUpdate();
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
                    ROI2DShape.this.beginUpdate();
                    try
                    {
                        // refresh control point state
                        synchronized (controlPoints)
                        {
                            for (Anchor2D pt : controlPoints)
                                pt.mouseMove(e, imagePoint, canvas);
                        }
                    }
                    finally
                    {
                        ROI2DShape.this.endUpdate();
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

                final Rectangle2D bounds = shape.getBounds2D();

                // enlarge bounds with stroke
                final double over = getAdjustedStroke(canvas) * 2;
                ShapeUtil.enlarge(bounds, over, over, true);

                // define LOD level
                final boolean shapeVisible = isVisible(bounds, g, canvas);

                if (shapeVisible)
                {
                    final boolean small = isSmall(bounds, g, canvas);
                    final boolean tiny = isTiny(bounds, g, canvas);

                    // draw shape
                    drawShape(g, sequence, canvas, small);

                    // draw control points (only if not tiny)
                    if (!tiny && isSelected() && !isReadOnly())
                    {
                        // draw control point if selected
                        synchronized (controlPoints)
                        {
                            for (Anchor2D pt : controlPoints)
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
                    for (Anchor2D anchor : actorsToRemove)
                        for (vtkProp prop : anchor.getProps())
                            VtkUtil.removeProp(renderer, prop);

                    // done
                    actorsToRemove.clear();
                }
                // need to add control points actor ?
                synchronized (actorsToAdd)
                {
                    for (Anchor2D anchor : actorsToAdd)
                        for (vtkProp prop : anchor.getProps())
                            VtkUtil.addProp(renderer, prop);

                    // done
                    actorsToAdd.clear();
                }

                // needed to forward paint event to control point
                synchronized (controlPoints)
                {
                    for (Anchor2D pt : controlPoints)
                        pt.paint(null, sequence, canvas);
                }
            }
        }

        /**
         * Draw the shape
         */
        protected void drawShape(Graphics2D g, Sequence sequence, IcyCanvas canvas, boolean simplified)
        {
            drawShape(g, sequence, canvas, shape, simplified);
        }

        /**
         * Draw the shape
         */
        protected void drawShape(Graphics2D g, Sequence sequence, IcyCanvas canvas, Shape shape, boolean simplified)
        {
            final Graphics2D g2 = (Graphics2D) g.create();

            // simplified draw
            if (simplified)
            {
                g2.setColor(getDisplayColor());

                // fill content if selected
                if (isSelected())
                    g2.fill(shape);

                // then draw shape
                g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke)));
                g2.draw(shape);
            }
            // normal draw
            else
            {
                // ROI selected and has content to draw ?
                if (isSelected())
                {
                    final AlphaComposite prevAlpha = (AlphaComposite) g2.getComposite();

                    float newAlpha = prevAlpha.getAlpha() * getOpacity();
                    newAlpha = Math.min(1f, newAlpha);
                    newAlpha = Math.max(0f, newAlpha);

                    // show content with an alpha factor
                    g2.setComposite(prevAlpha.derive(newAlpha));
                    g2.setColor(getDisplayColor());

                    // only fill closed shape
                    g2.fill(ShapeUtil.getClosedPath(shape));

                    // restore composite and set stroke
                    g2.setComposite(prevAlpha);
                    g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke + 1d)));

                    // then draw object shape without border
                    g2.draw(shape);
                }
                else
                {
                    // draw border
                    g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke + 1d)));
                    g2.setColor(Color.black);
                    g2.draw(shape);
                    // draw shape
                    g2.setStroke(new BasicStroke((float) ROI.getAdjustedStroke(canvas, stroke)));
                    g2.setColor(getDisplayColor());
                    g2.draw(shape);
                }
            }

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
                    for (Anchor2D anchor : controlPoints)
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
                for (Anchor2D pt : controlPoints)
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
     * ROI shape (in image coordinates)
     */
    protected final Shape shape;
    /**
     * control points
     */
    protected final List<Anchor2D> controlPoints;

    /**
     * internals
     */
    protected final Anchor2DPositionListener anchor2DPositionListener;
    protected final OverlayListener anchor2DOverlayListener;
    protected boolean firstMove;

    public ROI2DShape(Shape shape)
    {
        super();

        this.shape = shape;
        controlPoints = new ArrayList<Anchor2D>();
        firstMove = true;

        anchor2DPositionListener = new Anchor2DPositionListener()
        {
            @Override
            public void positionChanged(Anchor2D source)
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
        return "Shape2D";
    }

    @Override
    protected ROI2DShapePainter createPainter()
    {
        return new ROI2DShapePainter();
    }

    /**
     * build a new anchor with specified position
     */
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new Anchor2D(pos.getX(), pos.getY(), getColor(), getFocusedColor());
    }

    /**
     * build a new anchor with specified position
     */
    protected Anchor2D createAnchor(double x, double y)
    {
        return createAnchor(new Point2D.Double(x, y));
    }

    /**
     * @return the shape
     */
    public Shape getShape()
    {
        return shape;
    }

    /**
     * Rebuild shape.<br>
     * This method should be overridden by derived classes which<br>
     * have to call the super.updateShape() method at end.
     */
    protected void updateShape()
    {
        final int z = getZ();

        beginUpdate();
        try
        {
            // fix control points Z position if needed
            synchronized (controlPoints)
            {
                for (Anchor2D points : controlPoints)
                    points.setZ(z);
            }
        }
        finally
        {
            endUpdate();
        }

        // the shape should have been rebuilt here
        ((ROI2DShapePainter) painter).needRebuild = true;
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
    protected void addPoint(Anchor2D pt)
    {
        addPoint(pt, -1);
    }

    /**
     * Internal use only, use {@link #addNewPoint(Point2D, boolean)} instead.
     */
    public void addPoint(Anchor2D pt, int index)
    {
        // set Z position
        pt.setZ(getZ());
        // set visible state
        pt.setVisible(isSelected());
        // add listeners
        pt.addPositionListener(anchor2DPositionListener);
        pt.addOverlayListener(anchor2DOverlayListener);

        synchronized (controlPoints)
        {
            if (index == -1)
                controlPoints.add(pt);
            else
                controlPoints.add(index, pt);
        }

        synchronized (((ROI2DShapePainter) getOverlay()).actorsToAdd)
        {
            // store it in the "actor to add" list
            ((ROI2DShapePainter) getOverlay()).actorsToAdd.add(pt);
        }
        synchronized (((ROI2DShapePainter) getOverlay()).actorsToRemove)
        {
            // and remove it from the "actor to remove" list
            ((ROI2DShapePainter) getOverlay()).actorsToRemove.remove(pt);
        }

        roiChanged(true);
    }

    /**
     * @deprecated Use {@link #addNewPoint(Point2D, boolean)} instead.
     */
    @Deprecated
    public boolean addPoint(Point2D pos, boolean insert)
    {
        return (addNewPoint(pos, insert) != null);
    }

    /**
     * @deprecated Use {@link #addNewPoint(Point2D, boolean)} instead.
     */
    @Deprecated
    public boolean addPointAt(Point2D pos, boolean insert)
    {
        return (addNewPoint(pos, insert) != null);
    }

    /**
     * Add a new point to this shape ROI.
     * 
     * @param pos
     *        position of the new point
     * @param insert
     *        if set to <code>true</code> the new point will be inserted between the 2 closest points (in pixels
     *        distance) else the new point is inserted at the end of the point list
     * @param select
     *        select the new created point
     * @return the new created Anchor2D point if the operation succeed or <code>null</code> otherwise (if the ROI does
     *         not support this operation for instance)
     */
    public Anchor2D addNewPoint(Point2D pos, boolean insert, boolean select)
    {
        if (!canAddPoint())
            return null;

        final Anchor2D pt = createAnchor(pos);

        if (insert)
            // insert mode ? --> place the new point with closest points
            addPoint(pt, getInsertPointPosition(pos));
        else
            // just add the new point at last position
            addPoint(pt);

        // always select
        if (select)
            pt.setSelected(true);

        return pt;
    }

    /**
     * Add a new point to this shape ROI.
     * 
     * @param pos
     *        position of the new point
     * @param insert
     *        if set to <code>true</code> the new point will be inserted between the 2 closest
     *        points (in pixels distance) else the new point is inserted at the end of the point
     *        list
     * @return the new created Anchor2D point if the operation succeed or <code>null</code> otherwise (if the ROI does
     *         not support this operation for instance)
     */
    public Anchor2D addNewPoint(Point2D pos, boolean insert)
    {
        return addNewPoint(pos, insert, true);
    }

    /**
     * internal use only
     */
    protected boolean removePoint(IcyCanvas canvas, Anchor2D pt)
    {
        boolean empty;

        pt.removeOverlayListener(anchor2DOverlayListener);
        pt.removePositionListener(anchor2DPositionListener);

        synchronized (controlPoints)
        {
            controlPoints.remove(pt);
            empty = controlPoints.isEmpty();
        }

        synchronized (((ROI2DShapePainter) getOverlay()).actorsToRemove)
        {
            // store it in the "actor to remove" list
            ((ROI2DShapePainter) getOverlay()).actorsToRemove.add(pt);
        }
        synchronized (((ROI2DShapePainter) getOverlay()).actorsToAdd)
        {
            // and remove it from the "actor to add" list
            ((ROI2DShapePainter) getOverlay()).actorsToAdd.remove(pt);
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
    public boolean removePoint(Anchor2D pt)
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
            synchronized (((ROI2DShapePainter) getOverlay()).actorsToRemove)
            {
                // store all points in the "actor to remove" list
                ((ROI2DShapePainter) getOverlay()).actorsToRemove.addAll(controlPoints);
            }
            synchronized (((ROI2DShapePainter) getOverlay()).actorsToAdd)
            {
                // and remove them from the "actor to add" list
                ((ROI2DShapePainter) getOverlay()).actorsToAdd.removeAll(controlPoints);
            }

            for (Anchor2D pt : controlPoints)
            {
                pt.removeOverlayListener(anchor2DOverlayListener);
                pt.removePositionListener(anchor2DPositionListener);
            }

            controlPoints.clear();
        }
    }

    /**
     * @deprecated Use {@link #removeSelectedPoint(IcyCanvas)} instead.
     */
    @Deprecated
    public boolean removePointAt(IcyCanvas canvas, Point2D imagePoint)
    {
        if (!canRemovePoint())
            return false;

        // first we try to remove selected point
        if (!removeSelectedPoint(canvas))
        {
            // if no point selected, try to select and remove a point at specified position
            if (selectPointAt(canvas, imagePoint))
                return removeSelectedPoint(canvas);

            return false;
        }

        return true;
    }

    /**
     * @deprecated Use {@link #removeSelectedPoint(IcyCanvas)} instead.
     */
    @Deprecated
    protected boolean removeSelectedPoint(IcyCanvas canvas, Point2D imagePoint)
    {
        return removeSelectedPoint(canvas);
    }

    /**
     * Remove the current selected point.
     */
    public boolean removeSelectedPoint(IcyCanvas canvas)
    {
        if (!canRemovePoint())
            return false;

        final Anchor2D selectedPoint = getSelectedPoint();

        if (selectedPoint == null)
            return false;

        synchronized (controlPoints)
        {
            final int index = controlPoints.indexOf(selectedPoint);

            // try to remove point
            if (!removePoint(canvas, selectedPoint))
                return false;

            // still have control points
            if (!controlPoints.isEmpty())
            {
                // save the point position
                final Point2D imagePoint = selectedPoint.getPosition();

                // we are using PathAnchor2D ?
                if (selectedPoint instanceof PathAnchor2D)
                {
                    final PathAnchor2D selectedPathPoint = (PathAnchor2D) selectedPoint;

                    switch (selectedPathPoint.getType())
                    {
                        // we removed a MOVETO point ?
                        case PathIterator.SEG_MOVETO:
                            // try to set next point to MOVETO state
                            if (index < controlPoints.size())
                            {
                                final PathAnchor2D nextPoint = (PathAnchor2D) controlPoints.get(index);

                                // next point is a CLOSE one ?
                                if (nextPoint.getType() == PathIterator.SEG_CLOSE)
                                {
                                    // delete it
                                    if (removePoint(canvas, nextPoint))
                                    {
                                        // it was the last control point --> delete ROI
                                        if (controlPoints.size() == 0)
                                            remove();
                                    }
                                }
                                else
                                    // whatever is next point, set it to MOVETO
                                    nextPoint.setType(PathIterator.SEG_MOVETO);
                            }
                            break;

                        // we removed a CLOSE point ?
                        case PathIterator.SEG_CLOSE:
                            // try to set previous point to CLOSE state
                            if (index > 0)
                            {
                                final PathAnchor2D prevPoint = (PathAnchor2D) controlPoints.get(index - 1);

                                // next point is a MOVETO one ?
                                if (prevPoint.getType() == PathIterator.SEG_MOVETO)
                                {
                                    // delete it
                                    if (removePoint(canvas, prevPoint))
                                    {
                                        // it was the last control point --> delete ROI
                                        if (controlPoints.size() == 0)
                                            remove();
                                    }
                                }
                                else
                                    // whatever is previous point, set it to CLOSE
                                    prevPoint.setType(PathIterator.SEG_CLOSE);
                            }
                            break;
                    }
                }

                // select a new point if possible
                if (controlPoints.size() > 0)
                    selectPointAt(canvas, imagePoint);
            }
        }

        return true;
    }

    protected Anchor2D getSelectedPoint()
    {
        synchronized (controlPoints)
        {
            for (Anchor2D pt : controlPoints)
                if (pt.isSelected())
                    return pt;
        }

        return null;
    }

    /**
     * @deprecated Use {@link #getSelectedPoint()} instead.
     */
    @Deprecated
    protected Anchor2D getSelectedControlPoint()
    {
        return getSelectedPoint();
    }

    @Override
    public boolean hasSelectedPoint()
    {
        return (getSelectedPoint() != null);
    }

    protected boolean selectPointAt(IcyCanvas canvas, Point2D imagePoint)
    {
        synchronized (controlPoints)
        {
            // find the new selected control point
            for (Anchor2D pt : controlPoints)
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
                for (Anchor2D pt : controlPoints)
                    pt.setSelected(false);
            }
        }
        finally
        {
            endUpdate();
        }
    };

    @SuppressWarnings("static-method")
    protected double getTotalDistance(List<Point2D> points, double factorX, double factorY)
    {
        // default implementation of total length connect the last point
        return Point2DUtil.getTotalDistance(points, factorX, factorY, true);
    }

    // default implementation for ROI2DShape
    @Override
    public double computeNumberOfContourPoints()
    {
        return getTotalDistance(getPointsInternal(), 1d, 1d);
    }

    @Override
    public double getLength(Sequence sequence) throws UnsupportedOperationException
    {
        // cannot be cached because dependent from Sequence metadata
        return getTotalDistance(getPointsInternal(), sequence.getPixelSizeX(), sequence.getPixelSizeY());
    }

    /**
     * Find best insert position for specified point
     */
    protected int getInsertPointPosition(Point2D pos)
    {
        final List<Point2D> points = getPointsInternal();

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
            final double d = getTotalDistance(points, 1d, 1d);
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

    /**
     * Returns true if specified point coordinates overlap the ROI edge.
     */
    @Override
    public boolean isOverEdge(IcyCanvas canvas, double x, double y)
    {
        // use bigger stroke for isOver test for easier intersection
        final double strk = painter.getAdjustedStroke(canvas) * 3;
        final Rectangle2D rect = new Rectangle2D.Double(x - (strk * 0.5), y - (strk * 0.5), strk, strk);
        final Rectangle2D roiBounds = getBounds2D();

        // special test for empty object (point or orthogonal line)
        if (roiBounds.isEmpty())
            return rect.intersectsLine(roiBounds.getMinX(), roiBounds.getMinY(), roiBounds.getMaxX(),
                    roiBounds.getMaxY());

        // fast intersect test to start with
        if (roiBounds.intersects(rect))
            // use flatten path, intersects on curved shape return incorrect result
            return ShapeUtil.pathIntersects(getPathIterator(null, 0.1), rect);

        return false;
    }

    // @Override
    // public boolean isOverPoint(IcyCanvas canvas, double x, double y)
    // {
    // if (isSelected())
    // {
    // for (Anchor2D pt : controlPoints)
    // if (pt.isOver(canvas, x, y))
    // return true;
    // }
    //
    // return false;
    // }

    /**
     * Return the list of control points for this ROI.
     */
    public List<Anchor2D> getControlPoints()
    {
        synchronized (controlPoints)
        {
            return new ArrayList<Anchor2D>(controlPoints);
        }
    }

    /**
     * Return the list of positions of control points for this ROI.
     */
    public ArrayList<Point2D> getPoints()
    {
        final ArrayList<Point2D> result = new ArrayList<Point2D>();

        synchronized (controlPoints)
        {
            for (Anchor2D pt : controlPoints)
                result.add(pt.getPosition());
        }

        return result;
    }

    /**
     * Return the list of positions of control points for this ROI.<br>
     * This is the direct internal position reference, don't modify them !
     */
    protected ArrayList<Point2D> getPointsInternal()
    {
        final ArrayList<Point2D> result = new ArrayList<Point2D>();

        synchronized (controlPoints)
        {
            for (Anchor2D pt : controlPoints)
                result.add(pt.getPositionInternal());
        }

        return result;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at)
    {
        return shape.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness)
    {
        return shape.getPathIterator(at, flatness);
    }

    @Override
    public boolean[] getBooleanMask(int x, int y, int width, int height, boolean inclusive)
    {
        if ((width <= 0) || (height <= 0))
            return new boolean[0];

        if (inclusive)
        {
            final Rectangle bounds = getBounds();
            final int wr = bounds.width;
            final int hr = bounds.height;

            // special case of very small ROI with inclusive mask to fix Graphics draw inaccuracy issue
            if ((wr == 1) || (hr == 1))
            {
                final boolean[] result = new boolean[width * height];
                final Rectangle r = new Rectangle(x, y, width, height);
                final int xr = bounds.x;
                final int yr = bounds.y;

                // mask contains something ?
                if (r.intersects(xr, yr, wr, hr))
                {
                    int ind = 0;

                    for (int j = 0; j < height; j++)
                        for (int i = 0; i < width; i++)
                            result[ind++] = bounds.contains(x + i, y + j);
                }

                return result;
            }
        }

        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        final byte[] buffer = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        final Graphics2D g = img.createGraphics();

        // we want accurate rendering as we use the image for the mask
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // translate back to origin and pixel center
        g.translate(-(x - 0.5d), -(y - 0.5d));

        // fill content
        g.setColor(Color.white);
        // only fill closed shapes
        g.fill(ShapeUtil.getClosedPath(shape));
        // we want edge as well
        if (inclusive)
            g.draw(shape);
        // TODO: do we really need that ??
        else
        {
            // remove edge from content as fill operation may be a bit off
            g.setColor(Color.black);
            g.draw(shape);
        }

        g.dispose();

        final boolean[] result = new boolean[width * height];

        // compute mask from image
        for (int i = 0; i < result.length; i++)
            result[i] = (buffer[i] != 0);

        return result;
    }

    @Override
    public boolean contains(Point2D p)
    {
        return shape.contains(p);
    }

    @Override
    public boolean contains(Rectangle2D r)
    {
        return shape.contains(r);
    }

    @Override
    public boolean contains(double x, double y)
    {
        return shape.contains(x, y);
    }

    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        return shape.contains(x, y, w, h);
    }

    @Override
    public boolean intersects(Rectangle2D r)
    {
        return shape.intersects(r);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h)
    {
        return shape.intersects(x, y, w, h);
    }

    @Override
    public Rectangle2D computeBounds2D()
    {
        final Rectangle2D result = shape.getBounds2D();

        // shape shouldn't be empty (even for single Point) --> always use a minimal bounds
        if (result.isEmpty())
        {
            result.setFrame(result.getX(), result.getY(), Math.max(result.getWidth(), 0.001d),
                    Math.max(result.getHeight(), 0.001d));
        }

        return result;
    }

    @Override
    public ROI getUnion(ROI roi) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                final ROI2DPath result = new ROI2DPath(ShapeUtil.union(this, roiShape));

                // don't forget to restore 5D position
                result.setZ(getZ());
                result.setT(getT());
                result.setC(getC());

                return result;
            }
        }

        return super.getUnion(roi);
    }

    @Override
    public ROI getIntersection(ROI roi) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                final ROI2DPath result = new ROI2DPath(ShapeUtil.intersect(this, roiShape));

                // don't forget to restore 5D position
                result.setZ(getZ());
                result.setT(getT());
                result.setC(getC());

                return result;
            }
        }

        return super.getIntersection(roi);
    }

    @Override
    public ROI getExclusiveUnion(ROI roi) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                final ROI2DPath result = new ROI2DPath(ShapeUtil.exclusiveUnion(this, roiShape));

                // don't forget to restore 5D position
                result.setZ(getZ());
                result.setT(getT());
                result.setC(getC());

                return result;
            }
        }

        return super.getExclusiveUnion(roi);
    }

    @Override
    public ROI getSubtraction(ROI roi) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                final ROI2DPath result = new ROI2DPath(ShapeUtil.subtract(this, roiShape));

                // don't forget to restore 5D position
                result.setZ(getZ());
                result.setT(getT());
                result.setC(getC());

                return result;
            }
        }

        return super.getSubtraction(roi);
    }

    @Override
    public boolean canTranslate()
    {
        return true;
    }

    @Override
    public void translate(double dx, double dy)
    {
        beginUpdate();
        try
        {
            synchronized (controlPoints)
            {
                for (Anchor2D pt : controlPoints)
                    pt.translate(dx, dy);
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
    public void controlPointPositionChanged(Anchor2D source)
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
     * Called when anchor painter changed, provided only for backward compatibility.<br>
     * Don't use it.
     */
    @SuppressWarnings({"deprecation"})
    public void painterChanged(PainterEvent event)
    {
        // ignore it now
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
                ((ROI2DShapePainter) getOverlay()).updateVtkDisplayProperties();
                break;

            case SELECTION_CHANGED:
                final boolean s = isSelected();

                beginUpdate();
                try
                {
                    // set control points visible or not
                    synchronized (controlPoints)
                    {
                        for (Anchor2D pt : controlPoints)
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

                ((ROI2DShapePainter) getOverlay()).updateVtkDisplayProperties();
                break;

            case PROPERTY_CHANGED:
                final String property = event.getPropertyName();

                if (StringUtil.equals(property, PROPERTY_STROKE) || StringUtil.equals(property, PROPERTY_COLOR)
                        || StringUtil.equals(property, PROPERTY_OPACITY))
                    ((ROI2DShapePainter) getOverlay()).updateVtkDisplayProperties();
                break;

            default:
                break;
        }

        super.onChanged(object);
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
