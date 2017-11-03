package plugins.kernel.roi.roi4d;

import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle4D;

import plugins.kernel.roi.roi3d.ROI3DStackRectangle;

public class ROI4DStackRectangle extends ROI4DStack<ROI3DStackRectangle>
{
    public ROI4DStackRectangle()
    {
        super(ROI3DStackRectangle.class);

        setName("4D rectangle");
    }

    public ROI4DStackRectangle(Rectangle4D rect)
    {
        this();

        final Rectangle3D rect3d = rect.toRectangle3D();

        if (rect.isInfiniteT())
            setSlice(-1, new ROI3DStackRectangle(rect3d));
        else
        {
            for (int t = (int) Math.floor(rect.getMinT()); t < rect.getMaxT(); t++)
                setSlice(t, new ROI3DStackRectangle(rect3d));
        }
    }
}