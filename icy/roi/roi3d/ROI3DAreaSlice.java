/**
 * 
 */
package icy.roi.roi3d;

import icy.roi.BooleanMask2D;
import icy.roi.roi2d.ROI2DArea;

import java.awt.Color;

/**
 * @author Stephane
 */
class ROI3DAreaSlice extends ROI2DArea
{
    class ROI3DAreaSlicePainter extends ROI2DAreaPainter
    {
        @Override
        public OverlayPriority getPriority()
        {
            if (parent.getUseChildOverlayProperties())
                return super.getPriority();

            return parent.getOverlay().getPriority();
        }

        @Override
        public float getOpacity()
        {
            if (parent.getUseChildOverlayProperties())
                return super.getOpacity();

            return parent.getOpacity();
        }

        @Override
        public Color getColor()
        {
            if (parent.getUseChildOverlayProperties())
                return super.getColor();

            return parent.getColor();
        }

        @Override
        public double getStroke()
        {
            if (parent.getUseChildOverlayProperties())
                return super.getStroke();

            return parent.getStroke();
        }
    }

    final ROI3DArea parent;

    public ROI3DAreaSlice(ROI3DArea parent, ROI2DArea area)
    {
        super(area);

        this.parent = parent;
    }

    public ROI3DAreaSlice(ROI3DArea parent, BooleanMask2D mask)
    {
        super(mask);

        this.parent = parent;
    }

    @Override
    protected ROI3DAreaSlicePainter createPainter()
    {
        return new ROI3DAreaSlicePainter();
    }

    @Override
    public int getT()
    {
        // return parent T position
        return parent.getT();
    }

    @Override
    public int getC()
    {
        // return parent C position
        return parent.getC();
    }

    @Override
    public boolean isSelected()
    {
        // use parent state here
        return parent.isSelected();
    }

    @Override
    public boolean isFocused()
    {
        // use parent state here
        return parent.isFocused();
    }

    @Override
    public void setSelected(boolean value)
    {
        super.setSelected(value);

        // also modify the parent state
        parent.setSelected(value);
    }

    @Override
    public void setFocused(boolean value)
    {
        super.setFocused(value);

        // also modify the parent state
        parent.setFocused(value);
    }
}
