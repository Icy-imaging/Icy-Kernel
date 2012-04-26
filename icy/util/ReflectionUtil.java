/**
 * 
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
    public static Method getMethod(Object object, String methodName, boolean forceAccess, Class<?>... parameterTypes)
            throws SecurityException, NoSuchMethodException
    {
        Class<?> clazz = object.getClass();
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
            throw new NoSuchMethodException("Method " + methodName + "(..) not found in class "
                    + object.getClass().getName());

        if (forceAccess)
            result.setAccessible(true);

        return result;
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
    public static Field getField(Object object, String fieldName, boolean forceAccess) throws SecurityException,
            NoSuchFieldException
    {
        Class<?> clazz = object.getClass();
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
            throw new NoSuchFieldException(" Field " + fieldName + " not found in class " + object.getClass().getName());

        if (forceAccess)
            result.setAccessible(true);

        return result;
    }

    /**
     * Return the object instance corresponding to the specified field name.
     */
    public static Object getFieldObject(Object object, String fieldName, boolean forceAccess)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        return getField(object, fieldName, forceAccess).get(object);
    }
}
