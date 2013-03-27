/**
 * 
 */
package icy.gui.component.button;

import icy.common.IcyAbstractAction;
import icy.resource.icon.IcyIcon;
import icy.util.StringUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;

import org.pushingpixels.flamingo.api.common.JCommandMenuButton;

/**
 * @author Stephane
 */
public class IcyCommandMenuButton extends JCommandMenuButton
{
    /**
     * 
     */
    private static final long serialVersionUID = -8129025104172266942L;

    /**
     * internals
     */
    private IcyAbstractAction action;
    private final PropertyChangeListener actionPropertyChangeListener;

    public IcyCommandMenuButton(String title, IcyIcon icon)
    {
        super(title, icon);

        action = null;

        actionPropertyChangeListener = new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (StringUtil.equals("enabled", evt.getPropertyName()))
                    repaint();
            }
        };
    }

    /**
     * @deprecated Use {@link #IcyCommandMenuButton(String, IcyIcon)} instead.
     */
    @Deprecated
    public IcyCommandMenuButton(String title, String iconName)
    {
        this(title, new IcyIcon(iconName));
    }

    public IcyCommandMenuButton(IcyIcon icon)
    {
        this(null, icon);
    }

    public IcyCommandMenuButton(String title)
    {
        this(title, (IcyIcon) null);
    }

    public IcyCommandMenuButton(IcyAbstractAction action)
    {
        this(null, (IcyIcon) null);

        setAction(action);
    }

    public IcyCommandMenuButton()
    {
        this(null, (IcyIcon) null);
    }

    /**
     * Return the icon as IcyIcon
     */
    public IcyIcon getIcyIcon()
    {
        final Icon icon = getIcon();

        if (icon instanceof IcyIcon)
            return (IcyIcon) icon;

        return null;
    }

    /**
     * @return the icon name
     */
    public String getIconName()
    {
        final IcyIcon icon = getIcyIcon();

        if (icon != null)
            return icon.getName();

        return null;
    }

    /**
     * @param iconName
     *        the icon name to set
     */
    public void setIconName(String iconName)
    {
        final IcyIcon icon = getIcyIcon();

        if (icon != null)
            icon.setName(iconName);
    }

    @Override
    public boolean isEnabled()
    {
        return super.isEnabled() && ((action == null) || action.isEnabled());
    }

    @Override
    public void setEnabled(boolean b)
    {
        final boolean oldValue = isEnabled();

        super.setEnabled(b);

        if ((oldValue != b) && (action != null))
            action.setEnabled(b);
    }

    /**
     * Sets the {@link IcyAbstractAction} attached to this button.
     */
    public void setAction(IcyAbstractAction value)
    {
        if (action != value)
        {
            // remove listener from previous action
            if (action != null)
            {
                removeActionListener(action);
                action.removePropertyChangeListener(actionPropertyChangeListener);
            }

            action = value;

            setText(action.getName());

            final IcyIcon icon = action.getIcon();

            if (icon != null)
                setIcon(new IcyIcon(icon));
            else
                setIcon(null);

            setCommandButtonKind(CommandButtonKind.ACTION_ONLY);

            if (value != null)
            {
                // set tooltip
                setActionRichTooltip(action.getRichToolTip());

                // add listeners
                addActionListener(value);
                value.addPropertyChangeListener(actionPropertyChangeListener);
            }
        }
    }

    /**
     * Returns the {@link IcyAbstractAction} attached to this button.
     */
    public IcyAbstractAction getAction()
    {
        return action;
    }
}
