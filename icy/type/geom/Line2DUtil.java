package icy.type.geom;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * @deprecated Use {@link GeomUtil} instead
 */
@Deprecated
public class Line2DUtil
{
    /**
     * @deprecated Use {@link GeomUtil#getIntersection(Line2D, Line2D)} instead
     */
    @Deprecated
    public static Point2D getIntersection(Line2D lineA, Line2D lineB)
    {
        return GeomUtil.getIntersection(lineA, lineB);
    }

    /**
     * @deprecated Use {@link GeomUtil#getIntersection(Line2D, Line2D)} instead
     */
    @Deprecated
    public static Point2D getIntersection(Line2D lineA, Line2D lineB, boolean limitToSegmentA, boolean limitToSegmentB)
    {
        return GeomUtil.getIntersection(lineA, lineB, limitToSegmentA, limitToSegmentB);
    }
}
