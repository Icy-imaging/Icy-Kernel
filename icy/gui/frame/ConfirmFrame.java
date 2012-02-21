/**
 * 
 */
package icy.gui.frame;

import icy.gui.dialog.ConfirmDialog;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * ConfirmFrame class.<br>
 * Almost same as ConfirmDialog except it is not modal so you have to check<br>
 * for both <code>ready()</code> and <code>confirmed()</code> methods.
 * 
 * @author Stephane
 */
public class ConfirmFrame extends JOptionPane
{
    /**
     * 
     */
    private static final long serialVersionUID = 2833505262575458420L;

    public ConfirmFrame(final String title, final String message, final int optionType)
    {
        super(message, JOptionPane.QUESTION_MESSAGE, optionType);

        setInitialValue(initialValue);
        setComponentOrientation(getRootFrame().getComponentOrientation());
        JDialog dialog = createDialog(null, title);
        dialog.setModal(false);
        dialog.setAlwaysOnTop(true);

        selectInitialValue();
        dialog.show();
    }

    public ConfirmFrame(final String title, final String message)
    {
        this(title, message, YES_NO_OPTION);
    }

    public ConfirmFrame(final String message)
    {
        this("Confirmation", message, YES_NO_OPTION);
    }

    /**
     * Return true if user confirmed
     */
    public boolean confirmed()
    {
        final Object v = getValue();

        if (v instanceof Integer)
            return ConfirmDialog.getBooleanReturnValue(((Integer) v).intValue());

        return false;
    }

    /**
     * Return true if user made choice.
     */
    public boolean ready()
    {
        return (getValue() != JOptionPane.UNINITIALIZED_VALUE);
    }
}
