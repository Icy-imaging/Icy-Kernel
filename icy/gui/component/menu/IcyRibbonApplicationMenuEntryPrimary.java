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
package icy.gui.component.menu;

import icy.action.IcyAbstractAction;
import icy.resource.icon.IcyIcon;

import java.awt.event.ActionListener;

import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;

/**
 * @author Stephane
 */
public class IcyRibbonApplicationMenuEntryPrimary extends RibbonApplicationMenuEntryPrimary
{
    private final IcyAbstractAction action;

    public IcyRibbonApplicationMenuEntryPrimary(ResizableIcon icon, String text, ActionListener mainActionListener,
            CommandButtonKind entryKind)
    {
        super(icon, text, mainActionListener, entryKind);

        action = null;
    }

    public IcyRibbonApplicationMenuEntryPrimary(IcyAbstractAction action)
    {
        super((action.getIcon() != null) ? new IcyIcon(action.getIcon()) : null, action.getName(), action,
                CommandButtonKind.ACTION_ONLY);

        this.action = action;

        // set tooltip
        setActionRichTooltip(action.getRichToolTip());
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
}
