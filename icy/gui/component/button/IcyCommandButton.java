/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.component.button;

import icy.action.IcyAbstractAction;
import icy.resource.icon.IcyIcon;
import icy.util.StringUtil;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;

/**
 * @author Stephane
 */
public class IcyCommandButton extends JCommandButton
{
    /**
     * 
     */
    private static final long serialVersionUID = 6430339971361017326L;

    /**
     * internals
     */
    private boolean internalEnabled;
    private IcyAbstractAction action;
    private final PropertyChangeListener actionPropertyChangeListener;

    public IcyCommandButton(String title, IcyIcon icon)
    {
        super(title, icon);

        action = null;
        internalEnabled = isEnabled();

        actionPropertyChangeListener = new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (StringUtil.equals("enabled", evt.getPropertyName()))
                    refreshEnabled();
            }
        };
    }

    /**
     * @deprecated Use {@link #IcyCommandButton(String, IcyIcon)} instead.
     */
    @Deprecated
    public IcyCommandButton(String title, Image icon)
    {
        this(title, new IcyIcon(icon));
    }

    /**
     * @deprecated Use {@link #IcyCommandButton(String, IcyIcon)} instead.
     */
    @Deprecated
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

    public IcyCommandButton(IcyAbstractAction action)
    {
        this(null, (IcyIcon) null);

        setAction(action);
    }

    /**
     * @deprecated User {@link #IcyCommandButton(IcyAbstractAction)} instead.
     */
    @Deprecated
    public IcyCommandButton(icy.common.IcyAbstractAction action)
    {
        this(null, (IcyIcon) null);

        setAction(action);
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

    @Override
    public void setEnabled(boolean b)
    {
        internalEnabled = b;
        refreshEnabled();
    }

    protected void refreshEnabled()
    {
        super.setEnabled(internalEnabled && ((action == null) || action.isEnabled()));
    }

    /**
     * Returns the {@link IcyAbstractAction} attached to this button.
     */
    public IcyAbstractAction getAction()
    {
        return action;
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
            setCommandButtonKind(CommandButtonKind.ACTION_ONLY);

            if (value != null)
            {
                // set text
                setText(action.getName());

                // set icon
                final IcyIcon icon = action.getIcon();

                if (icon != null)
                    setIcon(new IcyIcon(icon));
                else
                    setIcon(null);

                // set tooltip
                setActionRichTooltip(action.getRichToolTip());

                // add listeners
                addActionListener(value);
                value.addPropertyChangeListener(actionPropertyChangeListener);
            }

            refreshEnabled();
        }
    }

    /**
     * Sets the button in Popup mode with specified Popup panel callback and tool tip
     */
    public void setPopup(PopupPanelCallback cb, RichTooltip toolTip)
    {
        // remove action
        setAction(null);

        // then set popup
        setCommandButtonKind(CommandButtonKind.POPUP_ONLY);

        if (cb != null)
            setPopupCallback(cb);
        if (toolTip != null)
            setPopupRichTooltip(toolTip);

        refreshEnabled();
    }
}
