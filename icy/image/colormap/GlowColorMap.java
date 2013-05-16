package icy.image.colormap;

public class GlowColorMap extends IcyColorMap
{
    public GlowColorMap(boolean overUnderMark)
    {
        super(overUnderMark ? "Glow Under Over" : "Glow");

        beginUpdate();
        try
        {
            if (overUnderMark)
            {
                red.setControlPoint(0, 0);
                red.setControlPoint(64, 255);
                red.setControlPoint(192, 255);
                red.setControlPoint(254, 255);
                red.setControlPoint(255, 0);
            }
            else
            {
                red.setControlPoint(0, 0);
                red.setControlPoint(64, 255);
                red.setControlPoint(192, 255);
                red.setControlPoint(255, 255);
            }

            if (overUnderMark)
            {
                green.setControlPoint(0, 255);
                green.setControlPoint(1, 0);
                green.setControlPoint(64, 0);
                green.setControlPoint(192, 255);
                green.setControlPoint(254, 255);
                green.setControlPoint(255, 0);
            }
            else
            {
                green.setControlPoint(0, 0);
                green.setControlPoint(64, 0);
                green.setControlPoint(192, 255);
                green.setControlPoint(255, 255);
            }

            blue.setControlPoint(0, 0);
            blue.setControlPoint(192, 0);
            blue.setControlPoint(255, 255);
        }
        finally
        {
            endUpdate();
        }
    }
}
