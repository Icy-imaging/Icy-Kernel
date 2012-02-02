/**
 * 
 */
package icy.gui.component.button;

import icy.resource.icon.IcyIcon;

import java.awt.Image;

import javax.swing.Icon;

import org.pushingpixels.flamingo.api.common.JCommandButton;

/**
 * @author Stephane
 */
public class IcyCommandButton extends JCommandButton
{
    /**
     * 
     */
    private static final long serialVersionUID = 6430339971361017326L;

    public IcyCommandButton(String title, IcyIcon icon)
    {
        super(title, icon);
    }

    public IcyCommandButton(String title, Image icon)
    {
        this(title, new IcyIcon(icon));
    }

    public IcyCommandButton(String title, String iconName)
    {
        this(title, new IcyIcon(iconName));
    }

    public IcyCommandButton(IcyIcon icon)
    {
        this(null, icon);
    }

    public IcyCommandButton(String title)
    {
        this(title, (IcyIcon) null);
    }

    public IcyCommandButton()
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
