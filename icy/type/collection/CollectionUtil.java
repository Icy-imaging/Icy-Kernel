/*
 * Copyright 2010-2013 Institut Pasteur.
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

    public static <T> ArrayList<T> createArrayList(T t, boolean addIfNull)
    {
        final ArrayList<T> result = new ArrayList<T>();

        if (addIfNull || (t != null))
            result.add(t);

        return result;
    }

    public static <T> ArrayList<T> createArrayList(T t)
    {
        return createArrayList(t, true);
    }

    public static <T> boolean addUniq(List<T> list, T t, boolean addIfNull)
    {
        if (addIfNull || (t != null))
        {
            if (!list.contains(t))
                return list.add(t);
        }

        return false;
    }

    public static <T> boolean addUniq(List<T> list, T t)
    {
        return addUniq(list, t, true);
    }

}
