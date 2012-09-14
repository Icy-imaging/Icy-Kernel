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
import icy.util.ReflectionUtil;

import java.awt.Component;
import java.lang.reflect.Method;

import javax.swing.Popup;
import javax.swing.PopupFactory;

public class CustomPopupFactory extends PopupFactory
{
//    private static final Float OPAQUE = new Float(1.0F);

    private final boolean macos;

    private Method getPopupMethod;
    private int heavy;

    public CustomPopupFactory()
    {
        super();

        getPopupMethod = null;
        heavy = 0;

        macos = SystemUtil.isMac();

        if (macos)
        {
            try
            {
                getPopupMethod = ReflectionUtil.getMethod(PopupFactory.class, "getPopup", true, Component.class,
                        Component.class, int.class, int.class, int.class);
                heavy = ReflectionUtil.getField(PopupFactory.class, "HEAVY_WEIGHT_POPUP", true).getInt(null);
            }
            catch (Exception e)
            {
                getPopupMethod = null;
                heavy = 0;
            }
        }
    }

    // private static Window getWindow(Component component)
    // {
    // Object obj;
    // for (obj = component; !(obj instanceof Window) && obj != null; obj = ((Component)
    // (obj)).getParent())
    // ;
    // return (Window) obj;
    // }

    @Override
    public Popup getPopup(Component owner, Component contents, int x, int y)
    {
        if (contents == null)
        {
            throw new IllegalArgumentException("Popup.getPopup must be passed non-null contents");
        }

        if (macos && (getPopupMethod != null))
        {
            try
            {
                return (Popup) getPopupMethod.invoke(this, owner, contents, Integer.valueOf(x), Integer.valueOf(y),
                        Integer.valueOf(heavy));
            }
            catch (Exception e)
            {
                // ignore
            }

            // popup = getPopup(owner, contents, x, y, HEAVY_WEIGHT_POPUP);
            //
            // // this is intended to force Heavy Weight popup component
            // final Popup popup = super.getPopup(null, component1, x, y);
            //
            // final Window window = getWindow(component1);
            //
            // if (window == null)
            // return popup;
            // if (!(window instanceof RootPaneContainer))
            // return popup;
            //
            // final JRootPane popupRootPane = ((RootPaneContainer) window).getRootPane();
            // popupRootPane.putClientProperty("Window.alpha", OPAQUE);
            // popupRootPane.putClientProperty("Window.shadow", Boolean.FALSE);
            //
            // return popup;
        }

        return super.getPopup(owner, contents, x, y);
    }
}
