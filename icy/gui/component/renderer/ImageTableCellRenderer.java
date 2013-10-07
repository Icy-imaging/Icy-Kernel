/**
 * 
 */
package icy.gui.component.renderer;

import icy.resource.ResourceUtil;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.SwingConstants;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

/**
 * @author Stephane
 */
public class ImageTableCellRenderer extends SubstanceDefaultTableCellRenderer
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
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    public ImageTableCellRenderer()
    {
        this(-1);
    }

    @Override
    protected void setValue(Object value)
    {
        if (value instanceof Image)
            setIcon(ResourceUtil.getImageIcon((Image) value, size));
        else if (value instanceof Icon)
            setIcon((Icon) value);
        else
            super.setValue(value);
    }
}
