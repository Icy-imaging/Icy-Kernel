/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.roi;

import icy.canvas.Canvas3D;
import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.canvas.IcyCanvas3D;
import icy.common.EventHierarchicalChecker;
import icy.gui.util.GuiUtil;
import icy.painter.Anchor2D;
import icy.painter.Anchor2D.Anchor2DListener;
import icy.painter.PainterEvent;
import icy.painter.PathAnchor2D;
import icy.painter.VtkPainter;
import icy.roi.ROIEvent.ROIPointEventType;
import icy.sequence.Sequence;
import icy.util.EventUtil;
import icy.util.ShapeUtil;
import icy.util.StringUtil;
import icy.vtk.VtkUtil;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Arrays;

import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

/**
 * @author Stephane
 */
public abstract class ROI2DShape extends ROI2D implements Shape, Anchor2DListener
{
    protected class ROI2DShapePainter extends ROI2DPainter implements VtkPainter
    {
        // used for 3D
        final vtkPolyData polyData;
        final vtkActor actor;
        boolean needRebuild;
        double scaling[];

        public ROI2DShapePainter()
        {
            super();

            // init 3D painters stuff
            polyData = new vtkPolyData();

            final vtkPolyDataMapper polyMapper = new vtkPolyDataMapper();
            polyMapper.SetInput(polyData);

            actor = new vtkActor();
            actor.SetMapper(polyMapper);

            scaling = new double[3];
            Arrays.fill(scaling, 1d);

            needRebuild = true;
        }

        // give access to the internal vtkActor
        public vtkActor getVtkActor()
        {
            return actor;
        }

        private double[] get3DScaling(IcyCanvas3D canvas)
        {
            final double[] result = new double[3];

            if (canvas instanceof Canvas3D)
            {
                final Canvas3D cv3d = (Canvas3D) canvas;

                // use canvas3D scaling info
                result[0] = cv3d.getXScaling();
                result[1] = cv3d.getYScaling();
                result[2] = cv3d.getZScaling();
            }
            else
            {
                final Sequence seq = canvas.getSequence();

                // use pixel size information
                result[0] = seq.getPixelSizeX();
                result[1] = seq.getPixelSizeY();
                result[2] = seq.getPixelSizeZ();
            }

            return result;
        }

        /**
         * update 3D painter for 3D canvas
         */
        protected void rebuild3DPainter(IcyCanvas3D canvas)
        {
            final Sequence seq = canvas.getSequence();

            // nothing to update
            if (seq == null)
                return;

            final ArrayList<Point3D> point3DList = new ArrayList<Point3D>();
            final ArrayList<Poly3D> polyList = new ArrayList<Poly3D>();
            final double[] coords = new double[6];

            final double nbSlice = seq.getSizeZ(canvas.getPositionT());
            // use flat path
            final PathIterator path = getPathIterator(null, 0.5d);

            // starting position
            double xm = 0d;
            double ym = 0d;
            double x0 = 0d;
            double y0 = 0d;
            double x1 = 0d;
            double y1 = 0d;
            int ind;

            // build point data
            while (!path.isDone())
            {
                int segType = path.currentSegment(coords);

                switch (segType)
                {
                    case PathIterator.SEG_MOVETO:
                        x0 = xm = coords[0];
                        y0 = ym = coords[1];
                        break;

                    case PathIterator.SEG_LINETO:
                        x1 = coords[0];
                        y1 = coords[1];

                        ind = point3DList.size();

                        point3DList.add(new Point3D(x0 * scaling[0], y0 * scaling[1], 0));
                        point3DList.add(new Point3D(x1 * scaling[0], y1 * scaling[1], 0));
                        point3DList.add(new Point3D(x0 * scaling[0], y0 * scaling[1], nbSlice * scaling[2]));
                        point3DList.add(new Point3D(x1 * scaling[0], y1 * scaling[1], nbSlice * scaling[2]));
                        polyList.add(new Poly3D(1 + ind, 2 + ind, 0 + ind));
                        polyList.add(new Poly3D(3 + ind, 2 + ind, 1 + ind));

                        x0 = x1;
                        y0 = y1;
                        break;

                    case PathIterator.SEG_CLOSE:
                        x1 = xm;
                        y1 = ym;

                        ind = point3DList.size();

                        point3DList.add(new Point3D(x0 * scaling[0], y0 * scaling[1], 0));
                        point3DList.add(new Point3D(x1 * scaling[0], y1 * scaling[1], 0));
                        point3DList.add(new Point3D(x0 * scaling[0], y0 * scaling[1], nbSlice * scaling[2]));
                        point3DList.add(new Point3D(x1 * scaling[0], y1 * scaling[1], nbSlice * scaling[2]));
                        polyList.add(new Poly3D(1 + ind, 2 + ind, 0 + ind));
                        polyList.add(new Poly3D(3 + ind, 2 + ind, 1 + ind));

                        x0 = x1;
                        y0 = y1;
                        break;
                }

                path.next();
            }

            // convert to array
            final double[][] vertices = new double[point3DList.size()][3];
            final int[][] indexes = new int[polyList.size()][3];

            int pointIndex = 0;
            for (Point3D p3D : point3DList)
            {
                vertices[pointIndex][0] = p3D.x;
                vertices[pointIndex][1] = p3D.y;
                vertices[pointIndex][2] = p3D.z;
                pointIndex++;
            }

            int polyIndex = 0;
            for (Poly3D poly : polyList)
            {
                indexes[polyIndex][0] = poly.p1;
                indexes[polyIndex][1] = poly.p2;
                indexes[polyIndex][2] = poly.p3;
                polyIndex++;
            }

            polyData.SetPolys(VtkUtil.getCells(polyList.size(), VtkUtil.prepareCells(indexes)));
            polyData.SetPoints(VtkUtil.getPoints(vertices));
        }

        @Override
        public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            ROI2DShape.this.beginUpdate();
            try
            {
                // give first to controls points
                if (ROI2DShape.this.selected)
                {
                    // default anchor action on mouse pressed
                    for (Anchor2D pt : controlPoints)
                        pt.mousePressed(e, imagePoint, canvas);
                }

                // then to ROI
                super.mousePressed(e, imagePoint, canvas);

                // not yet consumed...
                if (!e.isConsumed())
                {
                    // right button action
                    if (EventUtil.isRightMouseButton(e))
                        // unselect
                        ROI2DShape.this.setSelected(false, false);
                }
            }
            finally
            {
                ROI2DShape.this.endUpdate();
            }
        }

        @Override
        public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            ROI2DShape.this.beginUpdate();
            try
            {
                // give first to controls points
                if (ROI2DShape.this.selected)
                {
                    // default anchor action on mouse drag
                    for (Anchor2D pt : controlPoints)
                        pt.mouseDrag(e, imagePoint, canvas);
                }

                // then to ROI
                super.mouseDrag(e, imagePoint, canvas);
            }
            finally
            {
                ROI2DShape.this.endUpdate();
            }
        }

        @Override
        public void mouseMove(MouseEvent e, Point2D imagePoint, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            ROI2DShape.this.beginUpdate();
            try
            {
                // give first to control points
                if (ROI2DShape.this.selected)
                {
                    // refresh control point state
                    for (Anchor2D pt : controlPoints)
                        pt.mouseMove(e, imagePoint, canvas);
                }

                // then to ROI
                super.mouseMove(e, imagePoint, canvas);
            }
            finally
            {
                ROI2DShape.this.endUpdate();
            }
        }

        /**
         * Draw the ROI itself
         */
        protected void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (canvas instanceof IcyCanvas2D)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                g2.setColor(getDisplayColor());

                // ROI selected ?
                if (selected)
                {
                    final Graphics2D g3 = (Graphics2D) g2.create();

                    final AlphaComposite prevAlpha = (AlphaComposite) g3.getComposite();
                    // show content with an alpha factor
                    g3.setComposite(prevAlpha.derive(prevAlpha.getAlpha() * 0.3f));
                    g3.fill(shape);

                    g3.dispose();
                }

                if (selected)
                    g2.setStroke(new BasicStroke((float) getAdjustedStroke(canvas, stroke + 1d)));
                else
                    g2.setStroke(new BasicStroke((float) getAdjustedStroke(canvas, stroke)));

                g2.draw(shape);
                // draw from flatten shape as we use it for collision detection
                // ShapeUtil.drawFromPath(getPathIterator(null, 0.1), g);

                if (selected)
                {
                    // draw control point if selected
                    for (Anchor2D pt : controlPoints)
                        pt.paint(g2, sequence, canvas);
                }

                g2.dispose();
            }

            if (canvas instanceof Canvas3D)
            {
                // 3D canvas
                final Canvas3D canvas3d = (Canvas3D) canvas;

                // FIXME : probably need a better implementation

                // get 3D scaling
                final double[] s = get3DScaling(canvas3d);

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
                    rebuild3DPainter(canvas3d);
                    needRebuild = false;
                }

                // add actor to the renderer if not already exist
                VtkUtil.addActor(canvas3d.getRenderer(), actor);
            }
        }

        /**
         * Draw extras informations as name, size and position
         */
        protected void drawInfos(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (canvas instanceof IcyCanvas2D)
            {
                if (selected)
                {
                    final Graphics2D g2 = (Graphics2D) g.create();

                    g2.setColor(getDisplayColor());

                    // draw position and size
                    final Rectangle2D bounds = getBounds2D();

                    String roiPositionString = "(" + StringUtil.toString(bounds.getX(), 1) + ";"
                            + StringUtil.toString(bounds.getY(), 1) + ")";
                    String roiBoundingSizeString = "[" + StringUtil.toString(bounds.getWidth(), 1) + ";"
                            + StringUtil.toString(bounds.getHeight(), 1) + "]";
                    String roiInfoString = roiPositionString + " " + roiBoundingSizeString;

                    Font font;
                    Rectangle2D stringBounds;
                    int xPos, yPos;

                    font = new Font("Arial", Font.BOLD, (int) ROI.canvasToImageLogDeltaX(canvas, 15));
                    stringBounds = GuiUtil.getStringBounds(g, font, roiInfoString);
                    xPos = (int) (bounds.getX() + bounds.getWidth() / 2 - stringBounds.getWidth() / 2);
                    yPos = (int) (bounds.getY() - stringBounds.getHeight() / 2);

                    g2.setColor(getDisplayColor());
                    g2.setFont(font);
                    g2.drawString(roiInfoString, xPos, yPos);

                    g2.dispose();
                }
            }
        }

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            drawROI(g, sequence, canvas);
            drawInfos(g, sequence, canvas);
        }

        @Override
        public vtkActor[] getActors()
        {
            return new vtkActor[] {actor};
        }

        @Override
        public vtkActor2D[] getActors2D()
        {
            return new vtkActor2D[] {};
        }

    }

    class Poly3D
    {
        public Poly3D(int p1, int p2, int p3)
        {
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
        }

        int p1;
        int p2;
        int p3;
    }

    class Point3D
    {
        public Point3D(double x, double y, double z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        double x;
        double y;
        double z;
    }

    /**
     * ROI shape (in image coordinates)
     */
    protected final Shape shape;
    /**
     * control points
     */
    protected final ArrayList<Anchor2D> controlPoints;

    public ROI2DShape(Shape shape)
    {
        super();

        this.shape = shape;
        controlPoints = new ArrayList<Anchor2D>();
    }

    @Override
    protected ROI2DShapePainter createPainter()
    {
        return new ROI2DShapePainter();
    }

    /**
     * @return the shape
     */
    public Shape getShape()
    {
        return shape;
    }

    /**
     * build a new anchor with specified position
     */
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new Anchor2D(pos, DEFAULT_SELECTED_COLOR, OVER_COLOR);
    }

    /**
     * build a new anchor with specified position
     */
    final protected Anchor2D createAnchor(double x, double y)
    {
        return createAnchor(new Point2D.Double(x, y));
    }

    @Override
    public void setSelected(boolean value, boolean exclusive)
    {
        // unselected ? --> unselected all control points
        if (!value)
        {
            for (Anchor2D pt : controlPoints)
                pt.setSelected(false);
        }

        super.setSelected(value, exclusive);
    }

    /**
     * Rebuild shape.<br>
     * This method should be overridden by derived classes which<br>
     * have to call the super.update() method at end.
     */
    protected void updateShape()
    {
        // the shape should have been rebuilt here
        ((ROI2DShapePainter) painter).needRebuild = true;
    }

    protected Anchor2D getSelectedControlPoint()
    {
        for (Anchor2D pt : controlPoints)
            if (pt.isSelected())
                return pt;

        return null;
    }

    @Override
    public boolean hasSelectedPoint()
    {
        return (getSelectedControlPoint() != null);
    }

    @Override
    public boolean canAddPoint()
    {
        return true;
    }

    @Override
    public boolean canRemovePoint()
    {
        return true;
    }

    /**
     * internal use only
     */
    protected void addPoint(Anchor2D pt)
    {
        pt.addListener(this);
        controlPoints.add(pt);
        roiChanged(ROIPointEventType.POINT_ADDED, pt);
    }

    /**
     * internal use only
     */
    protected void addPoint(Anchor2D pt, int index)
    {
        pt.addListener(this);
        controlPoints.add(index, pt);
        roiChanged(ROIPointEventType.POINT_ADDED, pt);
    }

    @Override
    public boolean addPointAt(Point2D pos, boolean ctrl)
    {
        if (!canAddPoint())
            return false;

        // creation mode
        if (creating)
        {
            final Anchor2D pt = createAnchor(pos);

            // just add the new point at last position
            addPoint(pt);
            // and select point is control is not pressed
            pt.setSelected(!ctrl);

            return true;
        }

        // modification mode, add point only if control is pressed
        if (ctrl)
        {
            final Anchor2D pt = createAnchor(pos);

            // place the new point with closest points
            addPoint(pt, getInsertPointPosition(pos));
            // always select
            pt.setSelected(true);

            return true;
        }

        return false;
    }

    /**
     * internal use only
     */
    protected boolean removePoint(Anchor2D pt)
    {
        controlPoints.remove(pt);
        pt.removeListener(this);
        roiChanged(ROIPointEventType.POINT_REMOVED, pt);
        return true;
    }

    /**
     * internal use only
     */
    protected void removeAllPoint()
    {
        beginUpdate();
        try
        {
            while (controlPoints.size() > 0)
                removePoint(controlPoints.get(0));
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public boolean removePointAt(IcyCanvas canvas, Point2D imagePoint)
    {
        if (!canRemovePoint())
            return false;

        // first we try to remove selected point
        if (!removeSelectedPoint(canvas, imagePoint))
        {
            // if no point selected, try to select and remove a point at specified position
            if (selectPointAt(canvas, imagePoint))
                return removeSelectedPoint(canvas, imagePoint);

            return false;
        }

        return true;
    }

    @Override
    protected boolean removeSelectedPoint(IcyCanvas canvas, Point2D imagePoint)
    {
        if (!canRemovePoint())
            return false;

        final Anchor2D selectedPoint = getSelectedControlPoint();

        if (selectedPoint != null)
        {
            final int index = controlPoints.indexOf(selectedPoint);

            // try to remove point
            if (removePoint(selectedPoint))
            {
                // last control point removed --> delete ROI
                if (controlPoints.size() == 0)
                    delete();
                else
                {
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
                                        if (removePoint(nextPoint))
                                        {
                                            // it was the last control point --> delete ROI
                                            if (controlPoints.size() == 0)
                                                delete();
                                        }
                                    }
                                    else
                                    {
                                        // whatever is next point, set it to MOVETO
                                        nextPoint.setType(PathIterator.SEG_MOVETO);
                                        nextPoint.setVisible(true);
                                    }
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
                                        if (removePoint(prevPoint))
                                        {
                                            // it was the last control point --> delete ROI
                                            if (controlPoints.size() == 0)
                                                delete();
                                        }
                                    }
                                    else
                                    {
                                        // whatever is previous point, set it to CLOSE
                                        prevPoint.setType(PathIterator.SEG_CLOSE);
                                        prevPoint.setVisible(false);
                                    }
                                }
                                break;
                        }
                    }

                    // select a new point if possible
                    if (controlPoints.size() > 0)
                        selectPointAt(canvas, imagePoint);
                }

                return true;
            }
        }

        return false;
    }

    protected boolean selectPointAt(IcyCanvas canvas, Point2D imagePoint)
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

        return false;
    }

    /**
     * Return total distance of the specified list of points.
     */
    protected double getTotalDistance(ArrayList<Point2D> points, boolean connectLastPoint)
    {
        final int size = points.size();
        double result = 0;

        if (size > 1)
        {
            for (int i = 0; i < size - 1; i++)
                result += points.get(i).distance(points.get(i + 1));

            // add last to first point distance
            if (connectLastPoint)
                result += points.get(size - 1).distance(points.get(0));
        }

        return result;
    }

    /**
     * Return total distance of the specified list of points.
     */
    protected double getTotalDistance(ArrayList<Point2D> points)
    {
        return getTotalDistance(points, true);
    }

    /**
     * Find best insert position for specified point
     */
    protected int getInsertPointPosition(Point2D pos)
    {
        final ArrayList<Point2D> points = new ArrayList<Point2D>();

        for (Anchor2D pt : controlPoints)
            points.add(pt.getPosition());

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
            final double d = getTotalDistance(points);
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
     * return true if specified point coordinates overlap the ROI (without control point)
     */
    @Override
    public boolean isOver(IcyCanvas canvas, double x, double y)
    {
        // use bigger stroke for isOver test for easier intersection
        final double strk = getAdjustedStroke(canvas) * 3;
        final Rectangle2D rect = new Rectangle2D.Double(x - (strk * 0.5), y - (strk * 0.5), strk, strk);
        // use flatten path, intersects on curved shape return incorrect result
        return ShapeUtil.pathIntersects(getPathIterator(null, 0.1), rect);
    }

    @Override
    public boolean isOverPoint(IcyCanvas canvas, double x, double y)
    {
        if (selected)
        {
            for (Anchor2D pt : controlPoints)
                if (pt.isOver(canvas, x, y))
                    return true;
        }

        return false;
    }

    ArrayList<Point2D> getPositions()
    {
        final ArrayList<Point2D> result = new ArrayList<Point2D>();

        for (Anchor2D pt : controlPoints)
            result.add(pt.getPosition());

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
    public Rectangle getBounds()
    {
        return shape.getBounds();
    }

    @Override
    public Rectangle2D getBounds2D()
    {
        return shape.getBounds2D();
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
    public boolean[] getAsBooleanMask(int x, int y, int w, int h, boolean inclusive)
    {
        if ((w <= 0) || (h <= 0))
            return new boolean[0];

        final BufferedImage maskImg = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        final Graphics2D g = maskImg.createGraphics();

        // draw shape in image
        g.setColor(Color.white);
        g.translate(-x, -y);
        g.fill(shape);
        if (inclusive)
            g.draw(shape);
        g.dispose();

        // use the image to define the mask
        final byte[] maskData = ((DataBufferByte) maskImg.getRaster().getDataBuffer()).getData();
        final boolean[] result = new boolean[w * h];

        for (int i = 0; i < result.length; i++)
            result[i] = (maskData[i] != 0);

        return result;
    }

    @Override
    public void translate(double dx, double dy)
    {
        beginUpdate();
        try
        {
            for (Anchor2D pt : controlPoints)
                pt.translate(dx, dy);
        }
        finally
        {
            endUpdate();
        }
    }

    // called when an anchor position changed
    @Override
    public void positionChanged(Anchor2D source)
    {
        // anchor(s) position changed --> ROI changed
        roiChanged(ROIPointEventType.POINT_CHANGED, source);
    }

    // called when anchor changed
    @Override
    public void painterChanged(PainterEvent event)
    {
        // we have a control point selected --> remove focus on ROI
        if (hasSelectedPoint())
            setFocused(false);

        // anchor changed --> ROI painter changed
        painter.changed();
    }

    // called when roi changed
    @Override
    public void onChanged(EventHierarchicalChecker object)
    {
        final ROIEvent event = (ROIEvent) object;

        switch (event.getType())
        {
        // do here global process on ROI change
            case ROI_CHANGED:
                // refresh shape
                updateShape();
                break;
        }

        super.onChanged(object);
    }
}
