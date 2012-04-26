/**
 * 
 */
package icy.gui.component.button;

import icy.resource.icon.IcyIcon;

import javax.swing.Icon;

import org.pushingpixels.flamingo.api.common.JCommandToggleButton;

/**
 * @author Stephane
 */
public class IcyCommandToggleButton extends JCommandToggleButton
{
    /**
     * 
     */
    private static final long serialVersionUID = 6540972110297834178L;

    public IcyCommandToggleButton(String title, IcyIcon icon)
    {
        super(title, icon);
    }

    /**
     * @deprecated Uses {@link #IcyCommandToggleButton(String, IcyIcon)} instead.
     */
    @Deprecated
    public IcyCommandToggleButton(String title, String iconName)
    {
        this(title, new IcyIcon(iconName));
    }

    public IcyCommandToggleButton(IcyIcon icon)
    {
        this(null, icon);
    }

    public IcyCommandToggleButton(String title)
    {
        this(title, (IcyIcon) null);
    }

    public IcyCommandToggleButton()
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

}
