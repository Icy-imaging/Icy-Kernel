/*
 * Copyright 2010-2013 Institut Pasteur.
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

import icy.painter.Anchor2D;
import icy.painter.PathAnchor2D;
import icy.roi.ROI;
import icy.type.point.Point5D;
import icy.util.ShapeUtil;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * ROI Path.<br>
 * This ROI can display a Path2D shape.<br>
 * You can modify and remove points (adding new point isn't supported).
 * 
 * @author Stephane
 */
public class ROI2DPath extends ROI2DShape
{
    public static final String ID_POINTS = "points";
    public static final String ID_POINT = "point";
    public static final String ID_WINDING = "winding";

    protected Area closedArea;
    protected Path2D openPath;

    static Path2D initPath(Point2D position)
    {
        final Path2D result = new Path2D.Double();

        result.reset();
        if (position != null)
            result.moveTo(position.getX(), position.getY());

        return result;
    }

    /**
     * Build a new ROI2DPath from the specified path.
     */
    public ROI2DPath(Path2D path, Area closedArea, Path2D openPath)
    {
        super(path);

        rebuildControlPointsFromPath();

        if (closedArea == null)
            this.closedArea = new Area(ShapeUtil.getClosedPath(path));
        else
            this.closedArea = closedArea;
        if (openPath == null)
            this.openPath = ShapeUtil.getOpenPath(path);
        else
            this.openPath = openPath;

        // set name
        setName("Path2D");
    }

    /**
     * Build a new ROI2DPath from the specified path.
     */
    public ROI2DPath(Path2D path)
    {
        this(path, null, null);

    }

    /**
     * Build a new ROI2DPath from the specified path.
     */
    public ROI2DPath(Shape shape)
    {
        this(new Path2D.Double(shape), (shape instanceof Area) ? (Area) shape : null, null);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public ROI2DPath(Point2D pt, boolean cm)
    {
        this(pt);
    }

    public ROI2DPath(Point2D position)
    {
        this(initPath(position));
    }

    /**
     * Generic constructor for interactive mode
     */
    public ROI2DPath(Point5D pt)
    {
        this(pt.toPoint2D());
    }

    public ROI2DPath()
    {
        this(new Path2D.Double(Path2D.WIND_NON_ZERO));
    }

    @Override
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new PathAnchor2D(pos.getX(), pos.getY(), getColor(), getFocusedColor());
    }

    protected void rebuildControlPointsFromPath()
    {
        final Color color = getColor();
        final Color focusedColor = getFocusedColor();

        // remove listeners
        for (Anchor2D pt : controlPoints)
        {
            pt.removeOverlayListener(anchor2DOverlayListener);
            pt.removePositionListener(anchor2DPositionListener);
        }

        controlPoints.clear();
        // add path points to the control point list
        controlPoints.addAll(ShapeUtil.getAnchorsFromShape(getPath(), color, focusedColor));

        // add listeners
        for (Anchor2D pt : controlPoints)
        {
            pt.addOverlayListener(anchor2DOverlayListener);
            pt.addPositionListener(anchor2DPositionListener);
        }
    }

    protected Path2D getPath()
    {
        return (Path2D) shape;
    }

    /**
     * Returns the closed area part of the ROI2DPath in {@link Area} shape format
     */
    public Area getClosedArea()
    {
        return closedArea;
    }

    /**
     * Returns the open path part of the ROI2DPath in {@link Path2D} shape format
     */
    public Path2D getOpenPath()
    {
        return openPath;
    }

    @Override
    public boolean canAddPoint()
    {
        // this ROI doesn't support point add
        return false;
    }

    @Override
    public boolean contains(double x, double y)
    {
        // only consider closed path
        return ShapeUtil.getClosedPath(getPath()).contains(x, y);
    }

    @Override
    public boolean contains(Point2D p)
    {
        // only consider closed path
        return ShapeUtil.getClosedPath(getPath()).contains(p);
    }

    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        // only consider closed path
        return ShapeUtil.getClosedPath(getPath()).contains(x, y, w, h);
    }

    @Override
    public boolean contains(Rectangle2D r)
    {
        // only consider closed path
        return ShapeUtil.getClosedPath(getPath()).contains(r);
    }

    @Override
    public boolean contains(ROI roi)
    {
        // not closed --> do not contains anything
        if (!ShapeUtil.isClosed(shape))
            return false;

        return super.contains(roi);
    }

    @Override
    public ROI add(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                final Path2D path = getPath();

                if (roi instanceof ROI2DPath)
                {
                    final ROI2DPath roiPath = (ROI2DPath) roi;

                    // compute closed area and open path parts
                    closedArea.add(roiPath.closedArea);
                    openPath.append(roiPath.openPath, false);
                }
                else
                {
                    // compute closed area and open path parts
                    if (roiShape.getShape() instanceof Area)
                        closedArea.add((Area) roiShape.getShape());
                    else
                        closedArea.add(new Area(ShapeUtil.getClosedPath(roiShape)));
                    openPath.append(ShapeUtil.getOpenPath(roiShape), false);
                }

                // then rebuild path from closed and open parts
                path.reset();
                path.append(closedArea, false);
                path.append(openPath, false);

                rebuildControlPointsFromPath();
                roiChanged();

                return this;
            }
        }

        return super.add(roi, allowCreate);
    }

    @Override
    public ROI intersect(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                final Path2D path = getPath();

                if (roi instanceof ROI2DPath)
                {
                    final ROI2DPath roiPath = (ROI2DPath) roi;

                    // compute closed area intersection and clear open path
                    closedArea.intersect(roiPath.closedArea);
                    openPath.reset();
                }
                else
                {
                    // compute closed area intersection and clear open path
                    if (roiShape.getShape() instanceof Area)
                        closedArea.intersect((Area) roiShape.getShape());
                    else
                        closedArea.intersect(new Area(ShapeUtil.getClosedPath(roiShape)));
                    openPath.reset();
                }

                // then rebuild path from closed area (open part is empty)
                path.reset();
                path.append(closedArea, false);

                rebuildControlPointsFromPath();
                roiChanged();

                return this;
            }
        }

        return super.intersect(roi, allowCreate);
    }

    @Override
    public ROI exclusiveAdd(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                final Path2D path = getPath();

                if (roi instanceof ROI2DPath)
                {
                    final ROI2DPath roiPath = (ROI2DPath) roi;

                    // compute exclusive union on closed area and simple append for open path
                    closedArea.exclusiveOr(roiPath.closedArea);
                    openPath.append(roiPath.openPath, false);
                }
                else
                {
                    // compute exclusive union on closed area and simple append for open path
                    if (roiShape.getShape() instanceof Area)
                        closedArea.exclusiveOr((Area) roiShape.getShape());
                    else
                        closedArea.exclusiveOr(new Area(ShapeUtil.getClosedPath(roiShape)));
                    openPath.append(ShapeUtil.getOpenPath(roiShape), false);
                }

                // then rebuild path from closed and open parts
                path.reset();
                path.append(closedArea, false);
                path.append(openPath, false);

                rebuildControlPointsFromPath();
                roiChanged();

                return this;
            }
        }

        return super.exclusiveAdd(roi, allowCreate);
    }

    @Override
    public ROI subtract(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        if (roi instanceof ROI2DShape)
        {
            final ROI2DShape roiShape = (ROI2DShape) roi;

            // only if on same position
            if ((getZ() == roiShape.getZ()) && (getT() == roiShape.getT()) && (getC() == roiShape.getC()))
            {
                final Path2D path = getPath();

                if (roi instanceof ROI2DPath)
                {
                    final ROI2DPath roiPath = (ROI2DPath) roi;

                    // compute closed area intersection and clear open path parts
                    closedArea.exclusiveOr(roiPath.closedArea);
                    if (!roiPath.closedArea.isEmpty())
                        openPath.reset();
                }
                else
                {
                    final Area area;

                    // compute closed area and open path parts
                    if (roiShape.getShape() instanceof Area)
                        area = (Area) roiShape.getShape();
                    else
                        area = new Area(ShapeUtil.getClosedPath(roiShape));
                    if (!area.isEmpty())
                    {
                        closedArea.exclusiveOr(area);
                        openPath.reset();
                    }
                }

                // then rebuild path from closed and open parts
                path.reset();
                path.append(closedArea, false);
                path.append(openPath, false);

                rebuildControlPointsFromPath();
                roiChanged();

                return this;
            }
        }

        return super.subtract(roi, allowCreate);
    }

    /**
     * Return the list of control points for this ROI.
     */
    public List<PathAnchor2D> getPathAnchors()
    {
        final List<PathAnchor2D> result = new ArrayList<PathAnchor2D>();

        synchronized (controlPoints)
        {
            for (Anchor2D pt : controlPoints)
                result.add((PathAnchor2D) pt);
        }

        return result;
    }

    protected void updateCachedStructures()
    {
        closedArea = new Area(ShapeUtil.getClosedPath(getPath()));
        openPath = ShapeUtil.getOpenPath(getPath());
    }

    @Override
    protected void updateShape()
    {
        ShapeUtil.buildPathFromAnchors(getPath(), getPathAnchors(), false);
        // update internal closed area and open path
        updateCachedStructures();

        // call super method after shape has been updated
        super.updateShape();
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            removeAllPoint();

            final List<Node> nodesPoint = XMLUtil.getChildren(XMLUtil.getElement(node, ID_POINTS), ID_POINT);
            if (nodesPoint != null)
            {
                for (Node n : nodesPoint)
                {
                    final PathAnchor2D pt = new PathAnchor2D();
                    pt.loadPositionFromXML(n);
                    addPoint(pt);
                }
            }

            getPath().setWindingRule(XMLUtil.getElementIntValue(node, ID_WINDING, Path2D.WIND_NON_ZERO));
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (!super.saveToXML(node))
            return false;

        final Element points = XMLUtil.setElement(node, ID_POINTS);

        synchronized (controlPoints)
        {
            for (Anchor2D pt : controlPoints)
                pt.savePositionToXML(XMLUtil.addElement(points, ID_POINT));
        }

        XMLUtil.setElementIntValue(node, ID_WINDING, getPath().getWindingRule());

        return true;
    }
}
