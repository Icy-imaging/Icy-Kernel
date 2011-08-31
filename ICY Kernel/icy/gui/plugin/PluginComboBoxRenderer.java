/**
 * 
 */
package icy.gui.plugin;

import icy.gui.component.renderer.CustomComboBoxRenderer;
import icy.plugin.PluginDescriptor;
import icy.resource.ResourceUtil;

import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * @author Stephane
 */
public class PluginComboBoxRenderer extends CustomComboBoxRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = -8450810538242826922L;

    private final boolean showLabel;

    public PluginComboBoxRenderer(JComboBox combo, boolean showLabel)
    {
        super(combo);

        this.showLabel = showLabel;
    }

    @Override
    protected void updateItem(JList list, Object value)
    {
        if (value instanceof PluginDescriptor)
        {
            final PluginDescriptor plugin = (PluginDescriptor) value;

            setIcon(ResourceUtil.scaleIcon(plugin.getIcon(), 20));
            if (showLabel)
                setText(plugin.getName());
            else
                setText("");
            setToolTipText(plugin.getDescription());
            setEnabled(list.isEnabled());
            setFont(list.getFont());
        }
        else
            super.updateItem(list, value);
    }
}
