package plugins.kernel.roi.roi3d;

import icy.type.rectangle.Rectangle3D;

import java.awt.geom.Rectangle2D;

import plugins.kernel.roi.roi2d.ROI2DRectangle;

public class ROI3DRectangle extends ROI3DStack<ROI2DRectangle>
{
    public ROI3DRectangle()
    {
        super(ROI2DRectangle.class);

        setName("3D rectangle");
    }

    public ROI3DRectangle(Rectangle3D rect)
    {
        this();

        final Rectangle2D rect2d = rect.toRectangle2D();

        
        if (rect.isInfiniteZ())
            setSlice(-1, new ROI2DRectangle(rect2d));
        else
        {
            for (int z = (int) Math.floor(rect.getMinZ()); z < rect.getMaxZ(); z++)
                setSlice(z, new ROI2DRectangle(rect2d));
        }
    }
}
