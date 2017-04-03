package plugins.kernel.roi.roi3d;

import icy.type.rectangle.Rectangle3D;

import java.awt.geom.Rectangle2D;

import plugins.kernel.roi.roi2d.ROI2DRectangle;

/**
 * Class defining a 3D Rectangle ROI as a stack of individual 2D Rectangle ROI.
 * 
 * @author Stephane
 */
public class ROI3DStackRectangle extends ROI3DStackShape
{
    public ROI3DStackRectangle()
    {
        super(ROI2DRectangle.class);

        setName("3D rectangle");
    }

    public ROI3DStackRectangle(Rectangle3D rect)
    {
        this();

        if (rect.isInfiniteZ())
            throw new IllegalArgumentException("Cannot set infinite Z dimension on the 3D Stack Rectangle ROI.");

        final Rectangle2D rect2d = rect.toRectangle2D();

        beginUpdate();
        try
        {
            for (int z = (int) Math.floor(rect.getMinZ()); z <= (int) rect.getMaxZ(); z++)
                setSlice(z, new ROI2DRectangle(rect2d));
        }
        finally
        {
            endUpdate();
        }
    }

    public ROI3DStackRectangle(Rectangle2D rect, int zMin, int zMax)
    {
        this();

        if (zMax < zMin)
            throw new IllegalArgumentException("ROI3DStackRectangle: cannot create the ROI (zMax < zMin).");

        beginUpdate();
        try
        {
            for (int z = zMin; z <= zMax; z++)
                setSlice(z, new ROI2DRectangle(rect));
        }
        finally
        {
            endUpdate();
        }
    }
    @Override
    public String getDefaultName()
    {
        return "Rectangle2D stack";
    }
}
