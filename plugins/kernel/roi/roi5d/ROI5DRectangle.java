package plugins.kernel.roi.roi5d;

import icy.type.rectangle.Rectangle4D;
import icy.type.rectangle.Rectangle5D;
import plugins.kernel.roi.roi4d.ROI4DRectangle;

public class ROI5DRectangle extends ROI5DStack<ROI4DRectangle>
{
    public ROI5DRectangle()
    {
        super(ROI4DRectangle.class);

        setName("5D rectangle");
    }

    public ROI5DRectangle(Rectangle5D rect)
    {
        this();

        final Rectangle4D rect4d = rect.toRectangle4D();

        if (rect.isInfiniteC())
            setSlice(-1, new ROI4DRectangle(rect4d));
        else
        {
            for (int c = (int) Math.floor(rect.getMinC()); c < rect.getMaxC(); c++)
                setSlice(c, new ROI4DRectangle(rect4d));
        }
    }
}