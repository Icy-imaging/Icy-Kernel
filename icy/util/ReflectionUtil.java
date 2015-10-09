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
package icy.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection tools class.
 * 
 * @author Stephane
 */
public class ReflectionUtil
{
    /**
     * Return the Method object corresponding to the specified method name and parameters.
     */
    public static Method getMethod(Class<?> objectClass, String methodName, boolean forceAccess,
            Class<?>... parameterTypes) throws SecurityException, NoSuchMethodException
    {
        Class<?> clazz = objectClass;
        Method result = null;

        while ((clazz != null) && (result == null))
        {
            try
            {
                result = clazz.getDeclaredMethod(methodName, parameterTypes);
            }
            catch (NoSuchMethodException e)
            {
                // ignore
            }

            clazz = clazz.getSuperclass();
        }

        if (result == null)
            throw new NoSuchMethodException("Method " + methodName + "(..) not found in class " + objectClass.getName());

        if (forceAccess)
            result.setAccessible(true);

        return result;
    }

    /**
     * Return the Method object corresponding to the specified method name and parameters.
     * 
     * @deprecated Use {@link #getMethod(Class, String, boolean, Class...)} instead.
     */
    @Deprecated
    public static Method getMethod(Object object, String methodName, boolean forceAccess, Class<?>... parameterTypes)
            throws SecurityException, NoSuchMethodException
    {
        return getMethod(object.getClass(), methodName, forceAccess, parameterTypes);
    }

    /**
     * Invoke the method of <code>object</code> corresponding to the specified name and with
     * specified parameters values.
     */
    public static Object invokeMethod(Object object, String methodName, boolean forceAccess, Object... args)
            throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        final Class<?>[] parameterTypes = new Class<?>[args.length];

        // build parameter types
        for (int i = 0; i < args.length; i++)
            parameterTypes[i] = args[i].getClass();

        // get method
        final Method method = getMethod(object, methodName, forceAccess, parameterTypes);
        // invoke method
        return method.invoke(object, args);
    }

    /**
     * Return the Field object corresponding to the specified field name.
     */
    public static Field getField(Class<?> objectClass, String fieldName, boolean forceAccess) throws SecurityException,
            NoSuchFieldException
    {
        Class<?> clazz = objectClass;
        Field result = null;

        while ((clazz != null) && (result == null))
        {
            try
            {
                result = clazz.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e)
            {
                // ignore
            }

            clazz = clazz.getSuperclass();
        }

        if (result == null)
            throw new NoSuchFieldException(" Field " + fieldName + " not found in class " + objectClass.getName());

        if (forceAccess)
            result.setAccessible(true);

        return result;
    }

    /**
     * Return the Field object corresponding to the specified field name.
     * 
     * @deprecated Use {@link #getField(Class, String, boolean)} instead.
     */
    @Deprecated
    public static Field getField(Object object, String fieldName, boolean forceAccess) throws SecurityException,
            NoSuchFieldException
    {
        return getField(object.getClass(), fieldName, forceAccess);
    }

    /**
     * Return the object instance corresponding to the specified field name.
     */
    public static Object getFieldObject(Object object, String fieldName, boolean forceAccess)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        return getField(object.getClass(), fieldName, forceAccess).get(object);
    }
}
