package plugins.kernel.roi.roi4d;

import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle4D;
import plugins.kernel.roi.roi3d.ROI3DRectangle;

public class ROI4DRectangle extends ROI4DStack<ROI3DRectangle>
{
    public ROI4DRectangle()
    {
        super(ROI3DRectangle.class);

        setName("4D rectangle");
    }

    public ROI4DRectangle(Rectangle4D rect)
    {
        this();

        final Rectangle3D rect3d = rect.toRectangle3D();

        if (rect.isInfiniteT())
            setSlice(-1, new ROI3DRectangle(rect3d));
        else
        {
            for (int t = (int) Math.floor(rect.getMinT()); t < rect.getMaxT(); t++)
                setSlice(t, new ROI3DRectangle(rect3d));
        }
    }
}