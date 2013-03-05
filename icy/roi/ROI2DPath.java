/**
 * 
 */
package icy.roi;

import icy.painter.Anchor2D;
import icy.painter.PathAnchor2D;
import icy.util.ShapeUtil;
import icy.util.XMLUtil;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

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

    static Path2D initPath(Point2D position)
    {
        final Path2D result = new Path2D.Double();

        result.reset();
        if (position != null)
            result.moveTo(position.getX(), position.getY());

        return result;
    }

    public ROI2DPath(Path2D path)
    {
        super(path);

        // add path points to the control point list
        controlPoints.addAll(ShapeUtil.getAnchorsFromShape(path));

        // add listeners
        for (Anchor2D pt : controlPoints)
        {
            pt.addOverlayListener(this);
            pt.addAnchorListener(this);
        }

        setName("Path2D");
    }

    public ROI2DPath(Shape shape)
    {
        this(new Path2D.Double(shape));
    }

    /**
     * constructor for XML creation
     */
    public ROI2DPath(Point2D position, boolean cm)
    {
        // ignore creation mode flag
        this(initPath(position));
    }

    @Override
    protected Anchor2D createAnchor(Point2D pos)
    {
        return new PathAnchor2D(pos.getX(), pos.getY(), DEFAULT_SELECTED_COLOR, OVER_COLOR);
    }

    private ArrayList<PathAnchor2D> getControlPoints()
    {
        final ArrayList<PathAnchor2D> result = new ArrayList<PathAnchor2D>();

        synchronized (controlPoints)
        {
            for (Anchor2D pt : controlPoints)
                if (pt instanceof PathAnchor2D)
                    result.add((PathAnchor2D) pt);
        }

        return result;
    }

    protected Path2D getPath()
    {
        return (Path2D) shape;
    }

    @Override
    public boolean canAddPoint()
    {
        // this ROI doesn't support point add
        return false;
    }

    @Override
    protected void updateShape()
    {
        ShapeUtil.buildPathFromAnchors(getPath(), getControlPoints());

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

            final ArrayList<Node> nodesPoint = XMLUtil.getSubNodes(XMLUtil.getElement(node, ID_POINTS), ID_POINT);
            if (nodesPoint != null)
            {
                for (Node n : nodesPoint)
                {
                    final PathAnchor2D pt = new PathAnchor2D();
                    pt.loadFromXML(n);
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
        for (Anchor2D pt : controlPoints)
            pt.saveToXML(XMLUtil.addElement(points, ID_POINT));

        XMLUtil.setElementIntValue(node, ID_WINDING, getPath().getWindingRule());

        return true;
    }
}
