/**
 * 
 */
package icy.gui.component.renderer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * @author Stephane
 */
public class LabelComboBoxRenderer extends CustomComboBoxRenderer
{
    /**
     * 
     */
    private static final long serialVersionUID = -4914566166205633920L;

    public LabelComboBoxRenderer(JComboBox combo)
    {
        super(combo);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void updateItem(JList list, Object value)
    {
        if (value instanceof JLabel)
        {
            final JLabel label = (JLabel) value;

            setIcon(label.getIcon());
            setText(label.getText());
            setToolTipText(label.getToolTipText());
            setEnabled(label.isEnabled());
            setFont(label.getFont());
        }
        else
            super.updateItem(list, value);
    }
}
