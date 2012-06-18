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
package icy.type.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Stephane
 */
public class CollectionUtil
{
    public static <T> ArrayList<T> asArrayList(T... a)
    {
        return new ArrayList<T>(Arrays.asList(a));
    }

    public static <T> List<T> asList(T... a)
    {
        return Arrays.asList(a);
    }

    public static <T> ArrayList<T> createArrayList(T t)
    {
        final ArrayList<T> result = new ArrayList<T>();

        result.add(t);

        return result;
    }

    public static boolean addUniq(List<Object> list, Object object)
    {
        if (!list.contains(object))
            return list.add(object);

        return false;
    }
}
