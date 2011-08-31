/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.component.button;

import icy.common.IcyAbstractAction;
import icy.gui.component.ComponentUtil;
import icy.resource.icon.IcyIcon;
import icy.util.StringUtil;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * @author Stephane
 */
public class IcyButton extends JButton
{
    private static final long serialVersionUID = -2259114067015863508L;

    private boolean flat;

    /**
     * Create a button with specified text and icon
     */
    public IcyButton(String text, IcyIcon icon)
    {
        super(text, icon);

        flat = false;
        init();

    }

    /**
     * Create a button with specified action
     */
    public IcyButton(IcyAbstractAction action)
    {
        super(action);

        flat = false;
        init();
    }

    public IcyButton(String text, Image iconImage)
    {
        this(text, new IcyIcon(iconImage));
    }

    public IcyButton(IcyIcon icon)
    {
        this(null, icon);
    }

    public IcyButton(Image iconImage)
    {
        this(null, new IcyIcon(iconImage));
    }

    public IcyButton(Image iconImage, int size)
    {
        this(null, new IcyIcon(iconImage, size));
    }

    /**
     * Create a button with specified text and icon (from resource name)<br>
     * 
     * @param text
     *        button text
     * @param iconName
     *        icon name
     * @param iconSize
     *        icon size
     */
    public IcyButton(String text, String iconName, int iconSize)
    {
        this(text, new IcyIcon(iconName, iconSize));
    }

    public IcyButton(String text, String iconName)
    {
        this(text, iconName, IcyIcon.DEFAULT_SIZE);
    }

    private void init()
    {
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);

        // manual change notify
        updateSize();
    }

    /**
     * @return the flat
     */
    public boolean isFlat()
    {
        return flat;
    }

    /**
     * @param flat
     *        the flat to set
     */
    public void setFlat(boolean flat)
    {
        if (this.flat != flat)
        {
            this.flat = flat;

            setBorderPainted(!flat);
            setFocusPainted(!flat);
            setFocusable(!flat);

            updateSize();
        }
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
     *        the iconName to set
     */
    public void setIconName(String iconName)
    {
        final IcyIcon icon = getIcyIcon();

        if (icon != null)
        {
            icon.setName(iconName);
            updateSize();
        }
    }

    /**
     * @return the icon size
     */
    public int getIconSize()
    {
        final IcyIcon icon = getIcyIcon();

        if (icon != null)
            return icon.getSize();

        return -1;
    }

    /**
     * @param iconSize
     *        the iconSize to set
     */
    public void setIconSize(int iconSize)
    {
        final IcyIcon icon = getIcyIcon();

        if (icon != null)
        {
            icon.setSize(iconSize);
            updateSize();
        }
    }

    @Override
    public void setText(String text)
    {
        super.setText(text);

        updateSize();
    }

    public void updateSize()
    {
        final IcyIcon icon = getIcyIcon();
        boolean noText = StringUtil.isEmpty(getText());
        noText |= (getAction() != null) && getHideActionText();

        // adjust size to icon size if no text
        if (flat && (icon != null) && noText)
            ComponentUtil.setFixedSize(this, icon.getDimension());
    }
}
