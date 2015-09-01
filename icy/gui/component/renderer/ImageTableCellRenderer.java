/**
 * 
 */
package icy.gui.component.renderer;

import icy.resource.ResourceUtil;

import java.awt.Image;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer.IconRenderer;

/**
 * @author Stephane
 */
public class ImageTableCellRenderer extends IconRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = -6330780301500309146L;

    final int size;

    public ImageTableCellRenderer(int size)
    {
        super();

        this.size = size;
        setIconTextGap(0);
    }

    public ImageTableCellRenderer()
    {
        this(-1);
    }

    @Override
    public void setValue(Object value)
    {
        if (value instanceof Image)
            setIcon(ResourceUtil.getImageIcon((Image) value, size));
        else
            super.setValue(value);
    }
}
