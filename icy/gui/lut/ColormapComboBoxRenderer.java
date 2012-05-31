/**
 * 
 */
package icy.gui.lut;

import icy.gui.component.renderer.CustomComboBoxRenderer;
import icy.image.colormap.IcyColorMap;

import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * @author Stephane
 */
public class ColormapComboBoxRenderer extends CustomComboBoxRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = 8439070623266035911L;
    
    private final int width;
    private final int height;

    public ColormapComboBoxRenderer(JComboBox combo, int w, int h)
    {
        super(combo);

        this.width = w;
        this.height = h;
    }

    public ColormapComboBoxRenderer(JComboBox combo)
    {
        this(combo, 64, 16);
    }

    @Override
    protected void updateItem(JList list, Object value)
    {
        if (value instanceof IcyColorMap)
        {
            final IcyColorMap colormap = (IcyColorMap) value;

            setIcon(new ColormapIcon(colormap, width, height));
            setText(null);
            setToolTipText("Set " + colormap.getName() + " colormap");
            setEnabled(list.isEnabled());
            setFont(list.getFont());
        }
        else
            super.updateItem(list, value);
    }
}
