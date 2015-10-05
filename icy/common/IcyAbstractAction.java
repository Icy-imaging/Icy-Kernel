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
package icy.common;

import icy.resource.icon.IcyIcon;

/**
 * @deprecated Use {@link icy.action.IcyAbstractAction} instead.
 */
@Deprecated
public abstract class IcyAbstractAction extends icy.action.IcyAbstractAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 5616342327585478868L;

    private static final int DEFAULT_ICON_SIZE = 20;

    public IcyAbstractAction(String name, IcyIcon icon, String description, String longDescription, int keyCode,
            int modifiers, boolean bgProcess, String processMessage)
    {
        super(name, icon, description, longDescription, keyCode, modifiers, bgProcess, processMessage);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, String longDescription, int keyCode,
            int modifiers)
    {
        this(name, icon, description, longDescription, keyCode, modifiers, false, null);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, String longDescription, boolean bgProcess,
            String processMessage)
    {
        this(name, icon, description, longDescription, 0, 0, bgProcess, processMessage);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, boolean bgProcess, String processMessage)
    {
        this(name, icon, description, null, 0, 0, bgProcess, processMessage);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, int keyCode, int modifiers)
    {
        this(name, icon, description, null, keyCode, modifiers, false, null);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, int keyCode)
    {
        this(name, icon, description, null, keyCode, 0, false, null);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, String longDescription)
    {
        this(name, icon, description, longDescription, 0, 0, false, null);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description)
    {
        this(name, icon, description, null, 0, 0, false, null);
    }

    public IcyAbstractAction(String name, IcyIcon icon)
    {
        this(name, icon, null, null, 0, 0, false, null);
    }

    public IcyAbstractAction(String name)
    {
        this(name, null, null, null, 0, 0, false, null);
    }

    /**
     * @deprecated Use {@link #IcyAbstractAction(String, IcyIcon, String, int, int)} instead.
     */
    @Deprecated
    public IcyAbstractAction(String name, String iconName, String description, int keyCode, int modifiers)
    {
        this(name, new IcyIcon(iconName, DEFAULT_ICON_SIZE), description, null, keyCode, modifiers, false, null);
    }

    /**
     * @deprecated Use {@link #IcyAbstractAction(String, IcyIcon, String, int)} instead.
     */
    @Deprecated
    public IcyAbstractAction(String name, String iconName, String description, int keyCode)
    {
        this(name, new IcyIcon(iconName, DEFAULT_ICON_SIZE), description, null, keyCode, 0, false, null);
    }

    /**
     * @deprecated Use {@link #IcyAbstractAction(String, IcyIcon, String)} instead.
     */
    @Deprecated
    public IcyAbstractAction(String name, String iconName, String description)
    {
        this(name, new IcyIcon(iconName, DEFAULT_ICON_SIZE), description, null, 0, 0, false, null);
    }

    /**
     * @deprecated Use {@link #IcyAbstractAction(String, IcyIcon)} instead.
     */
    @Deprecated
    public IcyAbstractAction(String name, String iconName)
    {
        this(name, new IcyIcon(iconName, DEFAULT_ICON_SIZE), null, null, 0, 0, false, null);
    }
}
