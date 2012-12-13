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
package icy.swimmingPool;

import icy.util.ClassUtil;
import icy.util.StringUtil;

import java.util.ArrayList;

import javax.swing.ImageIcon;

public class SwimmingObject
{
    public static ArrayList<String> getObjectTypes(ArrayList<SwimmingObject> objects)
    {
        final ArrayList<String> result = new ArrayList<String>();

        for (SwimmingObject obj : objects)
        {
            final String type = obj.getObjectClassName();

            if (!result.contains(type))
                result.add(type);
        }

        return result;
    }

    private static final String DEFAULT_NAME = "object";
    private static final ImageIcon DEFAULT_ICON = null;

    private static int id_gen = 1;

    private final Object object;

    private final String name;
    /** 32x32 icon */
    private final ImageIcon icon;
    private final int id;

    public SwimmingObject(Object object, String name, ImageIcon icon)
    {
        super();

        synchronized (SwimmingObject.class)
        {
            id = id_gen;
            id_gen++;
        }

        this.object = object;
        if (StringUtil.isEmpty(name))
            this.name = DEFAULT_NAME + " " + id;
        else
            this.name = name;
        if (icon == null)
            this.icon = DEFAULT_ICON;
        else
            this.icon = icon;
    }

    public SwimmingObject(Object object, String name)
    {
        this(object, name, null);
    }

    public SwimmingObject(Object object, ImageIcon icon)
    {
        this(object, null, icon);
    }

    public SwimmingObject(Object object)
    {
        this(object, null, null);
    }

    /**
     * @return the object
     */
    public Object getObject()
    {
        return object;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    public String getObjectClassName()
    {
        if (object != null)
            return object.getClass().getName();

        return "";
    }

    public String getObjectSimpleClassName()
    {
        return ClassUtil.getSimpleClassName(getObjectClassName());
    }

    /**
     * @return the icon
     */
    public ImageIcon getIcon()
    {
        return icon;
    }

    /**
     * @return the id
     */
    public int getId()
    {
        return id;
    }

}
