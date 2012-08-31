/**
 * 
 */
package icy.gui.component;

import icy.gui.util.ComponentUtil;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;

/**
 * @author Stephane
 */
public class IcySlider extends JSlider
{
    /**
     * 
     */
    private static final long serialVersionUID = 3416400365856996824L;

    private boolean smartTickMarkers;

    /**
     * 
     */
    public IcySlider()
    {
        super();

        smartTickMarkers = true;
    }

    /**
     * @param brm
     */
    public IcySlider(BoundedRangeModel brm)
    {
        super(brm);

        smartTickMarkers = true;
    }

    /**
     * @param orientation
     * @param min
     * @param max
     * @param value
     */
    public IcySlider(int orientation, int min, int max, int value)
    {
        super(orientation, min, max, value);

        smartTickMarkers = true;
    }

    /**
     * @param min
     * @param max
     * @param value
     */
    public IcySlider(int min, int max, int value)
    {
        super(min, max, value);

        smartTickMarkers = true;
    }

    /**
     * @param min
     * @param max
     */
    public IcySlider(int min, int max)
    {
        super(min, max);

        smartTickMarkers = true;
    }

    /**
     * @param orientation
     */
    public IcySlider(int orientation)
    {
        super(orientation);

        smartTickMarkers = true;
    }

    /**
     * @return the smartTickMarkers
     */
    public boolean isSmartTickMarkers()
    {
        return smartTickMarkers;
    }

    /**
     * @param value
     *        the smartTickMarkers to set
     */
    public void setSmartTickMarkers(boolean value)
    {
        if (smartTickMarkers != value)
        {
            smartTickMarkers = value;

            updateTicksAndLabels();
        }
    }

    private void updateTicksAndLabels()
    {
        if (smartTickMarkers && (getPaintTicks() || getPaintLabels()))
            ComponentUtil.setTickMarkers(this);
    }

    @Override
    public void setPaintLabels(boolean b)
    {
        super.setPaintLabels(b);

        updateTicksAndLabels();
    }

    @Override
    public void setPaintTicks(boolean b)
    {
        super.setPaintTicks(b);

        updateTicksAndLabels();
    }

    @Override
    protected void fireStateChanged()
    {
        super.fireStateChanged();

        updateTicksAndLabels();
    }
}
