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
package icy.gui.util;

import icy.system.SystemUtil;

import java.awt.Component;
import java.awt.Window;

import javax.swing.JRootPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.RootPaneContainer;

/**
 * @author stephane
 */
public class CustomPopupFactory extends PopupFactory
{
    private static final Float OPAQUE = new Float(1.0F);

    private final boolean macos;

    /**
     * this is to mimic osx style
     */
    boolean windowShadowEnabled;

    public CustomPopupFactory()
    {
        super();

        macos = SystemUtil.isMac();
        // this is specific to apple os
        windowShadowEnabled = macos;
    }

    public boolean isWindowShadowEnabled()
    {
        return windowShadowEnabled;
    }

    public void setWindowShadowEnabled(boolean windowShadowEnabled)
    {
        // only for mac os
        this.windowShadowEnabled = windowShadowEnabled & macos;
    }

    private static Window getWindow(Component component)
    {
        Object obj;
        for (obj = component; !(obj instanceof Window) && obj != null; obj = ((Component) (obj)).getParent())
            ;
        return (Window) obj;
    }

    @Override
    public Popup getPopup(Component component, Component component1, int x, int y)
    {
        if (macos)
        {
            // this is intended to force Heavy Weight popup component
            final Popup popup = super.getPopup(null, component1, x, y);

            final Window window = getWindow(component1);

            if (window == null)
                return popup;
            if (!(window instanceof RootPaneContainer))
                return popup;

            final JRootPane popupRootPane = ((RootPaneContainer) window).getRootPane();
            popupRootPane.putClientProperty("Window.alpha", OPAQUE);
            popupRootPane.putClientProperty("Window.shadow", Boolean.FALSE);

            return popup;
        }

        return super.getPopup(component, component1, x, y);
    }
}
