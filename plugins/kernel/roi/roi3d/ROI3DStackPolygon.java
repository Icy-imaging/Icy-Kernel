/**
 * 
 */
package plugins.kernel.roi.roi3d;

import icy.type.geom.Polygon2D;

import plugins.kernel.roi.roi2d.ROI2DPolygon;

/**
 * Class defining a 3D Stack Polygon ROI as a stack of individual 2D Polygon ROI.
 * 
 * @author Stephane
 */
public class ROI3DStackPolygon extends ROI3DStackShape
{
    public ROI3DStackPolygon()
    {
        super(ROI2DPolygon.class);
    }

    public ROI3DStackPolygon(Polygon2D polygon, int zMin, int zMax)
    {
        this();

        if (zMax < zMin)
            throw new IllegalArgumentException("ROI3DStackPolygon: cannot create the ROI (zMax < zMin).");

        beginUpdate();
        try
        {
            for (int z = zMin; z <= zMax; z++)
                setSlice(z, new ROI2DPolygon(polygon));
        }
        finally
        {
            endUpdate();
        }
    }
    
    @Override
    public String getDefaultName()
    {
        return "Polygon2D stack";
    }
}
