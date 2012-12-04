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

import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;

/**
 * @author Stephane
 */
public class IcyCommandToggleMenuButton extends JCommandToggleMenuButton
{
    /**
     * 
     */
    private static final long serialVersionUID = -7391297214095914082L;

    /**
     * internals
     */
    private IcyAbstractAction action;
    private final PropertyChangeListener actionPropertyChangeListener;

    public IcyCommandToggleMenuButton(String title, IcyIcon icon)
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
     * @deprecated Uses {@link #IcyCommandToggleMenuButton(String, IcyIcon)} instead.
     */
    @Deprecated
    public IcyCommandToggleMenuButton(String title, String iconName)
    {
        this(title, new IcyIcon(iconName));
    }

    public IcyCommandToggleMenuButton(IcyIcon icon)
    {
        this(null, icon);
    }

    public IcyCommandToggleMenuButton(String title)
    {
        this(title, (IcyIcon) null);
    }

    public IcyCommandToggleMenuButton(IcyAbstractAction action)
    {
        this(null, (IcyIcon) null);

        setAction(action);
    }

    public IcyCommandToggleMenuButton()
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
                action.removePropertyChangeListener(actionPropertyChangeListener);

            action = value;

            setText(action.getName());

            final IcyIcon icon = action.getIcon();

            if (icon != null)
                setIcon(new IcyIcon(icon));
            else
                setIcon(null);

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
