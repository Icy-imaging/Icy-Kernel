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
import icy.common.EventHierarchicalChecker;
import icy.gui.util.GuiUtil;
import icy.painter.Anchor2D;
import icy.painter.Anchor2D.Anchor2DListener;
import icy.painter.PainterEvent;
import icy.painter.PathAnchor2D;
import icy.roi.ROIEvent.ROIPointEventType;
import icy.sequence.Sequence;
import icy.util.EventUtil;
import icy.util.ShapeUtil;
import icy.util.StringUtil;
import icy.vtk.VtkUtil;

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

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkRenderer;

/**
 * @author Stephane
 */
public abstract class ROI2DShape extends ROI2D implements Shape, Anchor2DListener
{
    protected class ROI2DShapePainter extends ROI2DPainter
    {
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

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (!isActiveFor(canvas))
                return;

            if (canvas instanceof IcyCanvas2D)
            {
                final Graphics2D graphics = (Graphics2D) g.create();

                // default paint for ROI2D
                graphics.setColor(getDisplayColor());
                if (ROI2DShape.this.selected)
                    graphics.setStroke(new BasicStroke((float) getAdjustedStroke(canvas, ROI2DShape.this.stroke + 1d)));
                else
                    graphics.setStroke(new BasicStroke((float) getAdjustedStroke(canvas, ROI2DShape.this.stroke)));

                graphics.draw(shape);
                // draw from flatten shape as we use it for collision detection
                // ShapeUtil.drawFromPath(getPathIterator(null, 0.1), g);

                // draw control point if selected
                if (ROI2DShape.this.selected)
                {
                    for (Anchor2D pt : controlPoints)
                        pt.paint(graphics, sequence, canvas);

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

                    graphics.setColor(getDisplayColor());
                    graphics.setFont(font);
                    graphics.drawString(roiInfoString, xPos, yPos);
                }

                graphics.dispose();
            }

            if (canvas instanceof Canvas3D)
            {
                // 3D canvas
                final Canvas3D canvas3d = (Canvas3D) canvas;

                // FIXME : this is a hack, need to add correct 3D display

                // sphere.SetRadius( sphere.GetRadius()+1 );
                if (!initialized)
                {
                    System.out
                            .println("TODO: ne marche pas si plusieurs viewers 3D sont instancié car le painter n'en n'itialise qu'un");
                    init(canvas3d.getRenderer());
                    initialized = true;
                }

                PathIterator path = shape.getPathIterator(null);
                double[] coords = new double[6];
                double h0 = 0.;
                double v0 = 0.;
                double h1 = 0.;
                double v1 = 0.;
                ArrayList<Point3D> point3DList = new ArrayList<Point3D>();
                ArrayList<Poly3D> polyList = new ArrayList<Poly3D>();

                // Canvas3D canvas3d = (Canvas3D) canvas;
                double zScale = canvas3d.getZScaling();
                double xScale = canvas3d.getXScaling();
                double yScale = canvas3d.getYScaling();
                double nbSlice = canvas3d.getSequence().getSizeZ(canvas3d.getViewer().getT());

                while (!path.isDone())
                {
                    int segType = path.currentSegment(coords);

                    switch (segType)
                    {
                        case PathIterator.SEG_MOVETO:
                            h0 = coords[0];
                            v0 = coords[1];
                            // System.out.println("moveto " + h0 + " " + v0 );
                            break;

                        case PathIterator.SEG_LINETO:
                            // System.out.println("lineto");
                        case PathIterator.SEG_CUBICTO:
                            // System.out.println("seg cubicto");
                            h1 = coords[0];
                            v1 = coords[1];
                            // drawLine(h0, v0, h1, v1);
                            // System.out.println("add 3D vert " + h0 + " " + h1 + " " + v0 + " " +
                            // v1 );
                            int currentPointIndex = point3DList.size();
                            // System.out.println("current point index : " + currentPointIndex );
                            point3DList.add(new Point3D(h0 * xScale, v0 * yScale, 0));
                            point3DList.add(new Point3D(h1 * xScale, v1 * yScale, 0));
                            point3DList.add(new Point3D(h0 * xScale, v0 * yScale, nbSlice * zScale));
                            point3DList.add(new Point3D(h1 * xScale, v1 * yScale, nbSlice * zScale));
                            polyList.add(new Poly3D(1 + currentPointIndex, 2 + currentPointIndex, 0 + currentPointIndex));
                            polyList.add(new Poly3D(3 + currentPointIndex, 2 + currentPointIndex, 1 + currentPointIndex));

                            h0 = h1;
                            v0 = v1;
                            break;

                        case PathIterator.SEG_CLOSE:
                            // System.out.println("segclose");
                            break;

                        default:
                            // AOutput.logln(" Error unknown segment type");
                            break;
                    }
                    path.next();
                }
                double[][] vertex = new double[point3DList.size()][3];
                int[][] index = new int[polyList.size()][3];

                int pointIndex = 0;
                for (Point3D p3D : point3DList)
                {
                    // System.out.println( p3D.x + " " + p3D.y + " " + p3D.z );
                    vertex[pointIndex][0] = p3D.x;
                    vertex[pointIndex][1] = p3D.y;
                    vertex[pointIndex][2] = p3D.z;

                    pointIndex++;
                }

                int polyIndex = 0;
                for (Poly3D poly : polyList)
                {
                    // System.out.println( poly.p1 + " " + poly.p2 + " " + poly.p3 );
                    index[polyIndex][0] = poly.p1;
                    index[polyIndex][1] = poly.p2;
                    index[polyIndex][2] = poly.p3;
                    polyIndex++;
                }

                // double[][] cube_vertex = new double[][] { {-10+getBounds().getCenterX(),
                // -10+getBounds().getCenterY(), -10 },
                // {-10, 10, -10}, {10, 10, -10},
                // {10, -10, -10}, {-10, -10, 10}, {-10, 10, 10}, {10, 10, 10}, {10, -10, 10}};
                // int[][] cube_poly = new int[][] { {0, 1, 2}, {0, 2, 3}, {4, 5, 1}, {4, 1, 0}, {3,
                // 2, 6}, {3, 6, 7},
                // {1, 5, 6}, {1, 6, 2}, {4, 0, 3}, {4, 3, 7}, {7, 6, 5}, {7, 5, 4}};

                final vtkPoints points;
                // polygon data
                final vtkCellArray cells;
                points = VtkUtil.getPoints(vertex);
                cells = VtkUtil.getCells(12, VtkUtil.prepareCells(index));

                polyData.SetPolys(cells);
                polyData.SetPoints(points);
            }
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

    boolean initialized = false;

    vtkPolyData polyData = null;
    private final double[][] cube_vertex = new double[][] { {-10, -10, -10}, {-10, 10, -10}, {10, 10, -10},
            {10, -10, -10}, {-10, -10, 10}, {-10, 10, 10}, {10, 10, 10}, {10, -10, 10}};
    private final int[][] cube_poly = new int[][] { {0, 1, 2}, {0, 2, 3}, {4, 5, 1}, {4, 1, 0}, {3, 2, 6}, {3, 6, 7},
            {1, 5, 6}, {1, 6, 2}, {4, 0, 3}, {4, 3, 7}, {7, 6, 5}, {7, 5, 4}};

    /**
     * init 3D
     * 
     * @param renderer
     */
    void init(vtkRenderer renderer)
    {
        // vertex data
        final vtkPoints points;
        // polygon data
        final vtkCellArray cells;

        // if (true)
        {
            // fast java data conversion for vertexes
            points = VtkUtil.getPoints(cube_vertex);
            // fast java data conversion for cells (polygons)
            cells = VtkUtil.getCells(12, VtkUtil.prepareCells(cube_poly));
        }
        // else
        // {
        // // define vertexes
        // points = new vtkPoints();
        //
        // for (int i = 0; i < cube_vertex.length; i++)
        // points.InsertNextPoint(cube_vertex[i]);
        //
        // // define cells
        // cells = new vtkCellArray();
        //
        // final vtkIdList idList = new vtkIdList();
        // for (int i = 0; i < cube_poly.length; i++)
        // {
        // final int[] poly = cube_poly[i];
        //
        // // set index in idList
        // idList.Reset();
        // for (int j = 0; j < poly.length; j++)
        // idList.InsertNextId(poly[j]);
        //
        // // insert cell
        // cells.InsertNextCell(idList);
        //
        // // vtk.CellType.POLYGON.GetId()
        // }
        // }

        polyData = new vtkPolyData();

        // set polygon
        polyData.SetPolys(cells);
        // set vertex
        polyData.SetPoints(points);

        final vtkPolyDataMapper polyMapper = new vtkPolyDataMapper();
        polyMapper.SetInput(polyData);

        final vtkActor actor = new vtkActor();
        actor.SetMapper(polyMapper);

        renderer.AddActor(actor);
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

    protected abstract void updateShape();

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

    @Override
    public boolean addPointAt(Point2D pos, boolean ctrl)
    {
        if (!canAddPoint())
            return false;

        final Anchor2D pt = createAnchor(pos);
        addPoint(pt);
        // select point is control is not pressed
        pt.setSelected(!ctrl);
        return true;
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
     * return true if specified point coordinates overlap the ROI (without control point)
     */
    @Override
    public boolean isOver(IcyCanvas canvas, double x, double y)
    {
        final double strk = getAdjustedStroke(canvas) * 2;
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
    public boolean[] getAsBooleanMask(int x, int y, int w, int h)
    {
        if ((w <= 0) || (h <= 0))
            return new boolean[0];

        final BufferedImage maskImg = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        final Graphics2D g = maskImg.createGraphics();

        // draw shape in image
        g.setColor(Color.white);
        g.translate(-x, -y);
        g.fill(shape);
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
