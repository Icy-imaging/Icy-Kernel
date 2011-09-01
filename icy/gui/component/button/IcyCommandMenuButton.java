/**
 * 
 */
package icy.gui.component.button;

import icy.resource.icon.IcyIcon;

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

    public IcyCommandMenuButton(String title, IcyIcon icon)
    {
        super(title, icon);
    }

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

}
