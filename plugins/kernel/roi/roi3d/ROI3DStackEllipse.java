/**
 * 
 */
package plugins.kernel.roi.roi3d;

import icy.type.rectangle.Rectangle3D;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import plugins.kernel.roi.roi2d.ROI2DEllipse;

/**
 * Class defining a 3D Ellipse ROI as a stack of individual 2D Ellipse ROI.
 * 
 * @author Stephane
 */
public class ROI3DStackEllipse extends ROI3DStackShape
{
    public ROI3DStackEllipse()
    {
        super(ROI2DEllipse.class);
    }

    public ROI3DStackEllipse(Rectangle3D rect)
    {
        this();

        if (rect.isInfiniteZ())
            throw new IllegalArgumentException("Cannot set infinite Z dimension on the 3D Stack Ellipse ROI.");

        final Rectangle2D rect2d = rect.toRectangle2D();

        beginUpdate();
        try
        {
            for (int z = (int) Math.floor(rect.getMinZ()); z <= (int) rect.getMaxZ(); z++)
                setSlice(z, new ROI2DEllipse(rect2d));
        }
        finally
        {
            endUpdate();
        }
    }

    public ROI3DStackEllipse(Rectangle2D rect, int zMin, int zMax)
    {
        this();

        if (zMax < zMin)
            throw new IllegalArgumentException("ROI3DStackEllipse: cannot create the ROI (zMax < zMin).");

        beginUpdate();
        try
        {
            for (int z = zMin; z <= zMax; z++)
                setSlice(z, new ROI2DEllipse(rect));
        }
        finally
        {
            endUpdate();
        }
    }

    public ROI3DStackEllipse(Ellipse2D ellipse, int zMin, int zMax)
    {
        this(ellipse.getBounds2D(), zMin, zMax);
    }

    @Override
    public String getDefaultName()
    {
        return "Ellipse2D stack";
    }
}
