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
package icy.util;

import icy.file.FileUtil;
import icy.plugin.PluginLoader;
import icy.system.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author stephane
 */
public class ClassUtil
{
    /**
     * Return the current thread context class loader
     */
    public static ClassLoader getContextClassLoader()
    {
        return SystemUtil.getContextClassLoader();
    }

    /**
     * Return the system class loader
     */
    public static ClassLoader getSystemClassLoader()
    {
        return SystemUtil.getSystemClassLoader();
    }

    /**
     * Get Class object of specified class from the system class loader
     * 
     * @throws ClassNotFoundException
     */
    public static Class<?> findClass(String className) throws ClassNotFoundException
    {
        try
        {
            // first try to load from Plugin class loader
            return PluginLoader.loadClass(className);
        }
        catch (ClassNotFoundException e1)
        {
            try
            {
                // then try to load from System class loader
                return ClassLoader.getSystemClassLoader().loadClass(className);
            }
            catch (ClassNotFoundException e2)
            {
                try
                {
                    // try forName style from Plugin class loader with initialization
                    return Class.forName(className, true, PluginLoader.getLoader());
                }
                catch (ClassNotFoundException e3)
                {
                    try
                    {
                        // try forName style from Plugin class loader without initialization
                        return Class.forName(className, false, PluginLoader.getLoader());
                    }
                    catch (ClassNotFoundException e4)
                    {
                        // last luck...
                        return Class.forName(className);
                    }
                }
            }
        }
    }

    /**
     * Transform the specified path in qualified name.<br>
     * ex : "document/class/loader.class" --> "document.class.loader.class" (unix)
     * "document\class\loader.class" --> "document.class.loader.class" (win)
     * 
     * @param path
     */
    public static String getQualifiedNameFromPath(String path)
    {
        return FileUtil.getGenericPath(path).replace(FileUtil.separatorChar, '.');
    }

    /**
     * Transform the specified qualified name in path.<br>
     * ex : "plugins.class.loader.class" --> "plugins/class/loader.class" (unix)
     * "plugins.class.loader.class" --> "plugins\class\loader.class" (win)
     * 
     * @param qualifiedName
     */
    public static String getPathFromQualifiedName(String qualifiedName)
    {
        return qualifiedName.replace('.', FileUtil.separatorChar);
    }

    /**
     * Get package name<br>
     * ex : "plugin.test.myClass" --> "plugin.test"
     */
    public static String getPackageName(String className)
    {
        final int index = className.lastIndexOf('.');

        if (index != -1)
            return className.substring(0, index);

        return "";
    }

    /**
     * Get first package name<br>
     * ex : "plugin.test.myClass" --> "plugin"
     */
    public static String getFirstPackageName(String className)
    {
        final String packageName = getPackageName(className);
        final int index = packageName.lastIndexOf('.');

        if (index != -1)
            return packageName.substring(0, index);

        return packageName;
    }

    /**
     * Get the base class name<br>
     * ex : "plugin.myClass$InternClass$1" --> "plugin.myClass"
     */
    public static String getBaseClassName(String className)
    {
        // handle inner classes...
        final int lastDollar = className.indexOf('$');
        if (lastDollar > 0)
            return className.substring(0, lastDollar);

        return className;
    }

    /**
     * Get simple class name<br>
     * ex : "plugin.test.myClass$InternClass$1" --> "myClass$InternClass$1"
     */
    public static String getSimpleClassName(String className)
    {
        final int index = className.lastIndexOf('.');

        if (index != -1)
            return className.substring(index + 1);

        return className;
    }

    /**
     * Return true if clazz implements the specified interface
     */
    public static Class<?>[] getInterfaces(Class<?> c)
    {
        if (c == null)
            return new Class[0];

        return c.getInterfaces();
    }

    /**
     * Return true if class is abstract
     */
    public static boolean isAbstract(Class<?> c)
    {
        if (c == null)
            return false;

        return Modifier.isAbstract(c.getModifiers());
    }

    /**
     * Return true if class is public
     */
    public static boolean isPublic(Class<?> c)
    {
        if (c == null)
            return false;

        return Modifier.isPublic(c.getModifiers());
    }

    /**
     * Return true if class is private
     */
    public static boolean isPrivate(Class<?> c)
    {
        if (c == null)
            return false;

        return Modifier.isPrivate(c.getModifiers());
    }

    /**
     * Return true if clazz extends baseClass
     */
    public static boolean isSubClass(Class<?> c, Class<?> baseClass)
    {
        if ((c == null) || (baseClass == null))
            return false;

        return baseClass.isAssignableFrom(c);
    }

    /**
     * This method finds all classes that are located in the package identified by the given
     * <code>packageName</code>.<br>
     * <b>ATTENTION:</b><br>
     * This is a relative expensive operation. Depending on your classpath multiple
     * directories,JAR-, and WAR-files may need to be scanned. <br>
     * 
     * @param packageName
     *        is the name of the {@link Package} to scan.
     * @param includeSubPackages
     *        - if <code>true</code> all sub-packages of the specified {@link Package} will be
     *        included in the search.
     * @return found classes set
     * @throws IOException
     *         if the operation failed with an I/O error.
     */
    public static Set<String> findClassNamesInPackage(String packageName, boolean includeSubPackages)
            throws IOException
    {
        final HashSet<String> classes = new HashSet<String>();

        findClassNamesInPackage(packageName, includeSubPackages, classes);

        return classes;
    }

    /**
     * This method finds all classes that are located in the package identified by the given
     * <code>packageName</code>.<br>
     * <b>ATTENTION:</b><br>
     * This is a relative expensive operation. Depending on your classpath multiple
     * directories,JAR-, and WAR-files may need to be scanned. <br>
     * Original code written by Jorg Hohwiller for the m-m-m project (http://m-m-m.sf.net)
     * 
     * @param packageName
     *        is the name of the {@link Package} to scan.
     * @param includeSubPackages
     *        - if <code>true</code> all sub-packages of the specified {@link Package} will be
     *        included in the search.
     * @param classes
     *        save found classes here
     */
    public static void findClassNamesInPackage(String packageName, boolean includeSubPackages, Set<String> classes)
            throws IOException
    {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final String path = ClassUtil.getPathFromQualifiedName(packageName);
        final Enumeration<URL> urls = classLoader.getResources(path);

        while (urls.hasMoreElements())
        {
            final URL packageUrl = urls.nextElement();
            final String urlPath = URLDecoder.decode(packageUrl.getFile(), "UTF-8");
            final String protocol = packageUrl.getProtocol().toLowerCase();

            if ("file".equals(protocol))
                findClassNamesInPath(urlPath, packageName, includeSubPackages, classes);
            else if ("jar".equals(protocol))
            {
                final JarURLConnection connection = (JarURLConnection) packageUrl.openConnection();
                final JarFile jarFile = connection.getJarFile();
                final Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                final String pathWithPrefix = path + '/';
                final int prefixLength = path.length() + 1;

                while (jarEntryEnumeration.hasMoreElements())
                {
                    final JarEntry jarEntry = jarEntryEnumeration.nextElement();
                    String absoluteFileName = jarEntry.getName();

                    if (absoluteFileName.endsWith(".class"))
                    {
                        if (absoluteFileName.startsWith("/"))
                            absoluteFileName = absoluteFileName.substring(1);

                        boolean accept = true;
                        if (absoluteFileName.startsWith(pathWithPrefix))
                        {
                            String qualifiedName = absoluteFileName.replace('/', '.');

                            if (!includeSubPackages)
                            {
                                int index = absoluteFileName.indexOf('/', prefixLength);
                                if (index != -1)
                                    accept = false;
                            }

                            if (accept)
                            {
                                final String className = filenameToClassname(qualifiedName);
                                if (className != null)
                                    classes.add(className);
                            }
                        }
                    }
                }

                jarFile.close();
            }
        }
    }

    /**
     * This method finds all classes that are located in the specified directory.<br>
     * 
     * @param path
     *        path to scan.
     * @param includeSubDir
     *        if <code>true</code> all sub-directory are also scanned.
     * @return set of found class.
     */
    public static HashSet<String> findClassNamesInPath(String path, boolean includeSubDir)
    {
        return findClassNamesInPath(path, ClassUtil.getQualifiedNameFromPath(path), includeSubDir, true);
    }

    /**
     * This method finds all classes that are located in the specified directory.<br>
     * 
     * @param path
     *        path to scan.
     * @param includeSubDir
     *        if <code>true</code> all sub-directory are also scanned.
     * @param includeJar
     *        if <code>true</code> all JAR files are also scanned
     * @return set of found class.
     */
    public static HashSet<String> findClassNamesInPath(String path, boolean includeSubDir, boolean includeJar)
    {
        return findClassNamesInPath(path, ClassUtil.getQualifiedNameFromPath(path), includeSubDir, includeJar);
    }

    /**
     * This method finds all classes that are located in the specified directory.<br>
     * 
     * @param path
     *        path to scan.
     * @param packageName
     *        package name prefix
     * @param includeSubDir
     *        if <code>true</code> all sub-directory are also scanned.
     * @return set of found class.
     */
    public static HashSet<String> findClassNamesInPath(String path, String packageName, boolean includeSubDir)
    {
        final HashSet<String> classes = new HashSet<String>();

        findClassNamesInPath(path, packageName, includeSubDir, true, classes);

        return classes;
    }

    /**
     * This method finds all classes that are located in the specified directory.<br>
     * 
     * @param path
     *        path to scan.
     * @param packageName
     *        package name prefix
     * @param includeSubDir
     *        if <code>true</code> all sub-directory are also scanned.
     * @param includeJar
     *        if <code>true</code> all JAR files are also scanned
     * @return set of found class.
     */
    public static HashSet<String> findClassNamesInPath(String path, String packageName, boolean includeSubDir,
            boolean includeJar)
    {
        final HashSet<String> classes = new HashSet<String>();

        findClassNamesInPath(path, packageName, includeSubDir, includeJar, classes);

        return classes;
    }

    /**
     * This method finds all classes that are located in the specified directory.<br>
     * 
     * @param path
     *        path to scan.
     * @param packageName
     *        package name prefix
     * @param includeSubDir
     *        if <code>true</code> all sub-directory are also scanned.
     * @param classes
     *        save found classes here
     */
    public static void findClassNamesInPath(String path, String packageName, boolean includeSubDir, Set<String> classes)
    {
        findClassNamesInPath(path, packageName, includeSubDir, true, classes);
    }

    /**
     * This method finds all classes that are located in the specified directory.<br>
     * 
     * @param path
     *        path to scan.
     * @param packageName
     *        package name prefix
     * @param includeSubDir
     *        if <code>true</code> all sub-directory are also scanned.
     * @param includeJar
     *        if <code>true</code> all JAR files are also scanned
     * @param classes
     *        save found classes here
     */
    public static void findClassNamesInPath(String path, String packageName, boolean includeSubDir, boolean includeJar,
            Set<String> classes)
    {
        final File dir = new File(path);
        final String qualifiedName;

        if (StringUtil.isEmpty(packageName))
            qualifiedName = "";
        else
            qualifiedName = packageName + '.';

        if (dir.isDirectory())
        {
            if (includeSubDir)
                findClassNamesRecursive(dir, includeJar, classes, qualifiedName);
            else
                for (File file : dir.listFiles())
                    findClassNameInFile(file, includeJar, classes, qualifiedName);
        }
        else
            findClassNameInFile(dir, classes, qualifiedName);
    }

    private static void findClassNamesRecursive(File directory, boolean includeJar, Set<String> classSet,
            String qualifiedName)
    {
        for (File childFile : directory.listFiles())
        {
            final String childFilename = childFile.getName();

            // files or directories starting with "." aren't allowed
            if (!childFilename.startsWith("."))
            {
                if (childFile.isDirectory())
                    findClassNamesRecursive(childFile, includeJar, classSet, qualifiedName + childFilename + '.');
                else
                    findClassNameInFile(childFile, includeJar, classSet, qualifiedName);
            }
        }
    }

    /**
     * Search for all classes in specified file
     */
    public static void findClassNameInFile(File file, boolean includeJar, Set<String> classSet,
            String qualifiedNamePrefix)
    {
        final String fileName = file.getPath();
        if (FileUtil.getFileExtension(fileName, false).toLowerCase().equals("jar"))
        {
            if (includeJar)
                findClassNamesInJAR(fileName, classSet);
        }
        else
            addClassFileName(file.getName(), classSet, qualifiedNamePrefix);
    }

    /**
     * Search for all classes in specified file
     */
    public static void findClassNameInFile(File file, Set<String> classSet, String qualifiedNamePrefix)
    {
        findClassNameInFile(file, true, classSet, qualifiedNamePrefix);
    }

    /**
     * Search for all classes in JAR file
     */
    public static void findClassNamesInJAR(String fileName, Set<String> classSet)
    {
        final JarFile jarFile;

        try
        {
            jarFile = new JarFile(fileName);
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
            return;
        }

        final Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements())
        {
            final JarEntry jarEntry = entries.nextElement();

            if (!jarEntry.isDirectory())
                addClassFileName(jarEntry.getName(), classSet, "");
        }

        try
        {
            jarFile.close();
        }
        catch (IOException e)
        {
            // ignore
        }
    }

    /**
     * Search for all classes in JAR file
     * 
     * @param fileName
     */
    public static Set<String> findClassNamesInJAR(String fileName)
    {
        final HashSet<String> result = new HashSet<String>();

        findClassNamesInJAR(fileName, result);

        return result;
    }

    private static void addClassFileName(String fileName, Set<String> classSet, String prefix)
    {
        final String simpleClassName = filenameToClassname(fileName);

        if (simpleClassName != null)
            classSet.add(prefix + simpleClassName);
    }

    /**
     * This method checks and transforms the filename of a potential {@link Class} given by
     * <code>fileName</code>.
     * 
     * @param fileName
     *        is the filename.
     * @return the according Java {@link Class#getName() class-name} for the given
     *         <code>fileName</code> if it is a class-file that is no anonymous {@link Class}, else
     *         <code>null</code>.
     */
    public static String filenameToClassname(String fileName)
    {
        // class file ?
        if (fileName.toLowerCase().endsWith(".class"))
            // remove ".class" extension and fix classname
            return fixClassName(fileName.substring(0, fileName.length() - 6));

        return null;
    }

    /**
     * This method checks and transforms the filename of a potential {@link Class} given by
     * <code>fileName</code>.<br>
     * Code written by Jorg Hohwiller for the m-m-m project (http://m-m-m.sf.net)
     * 
     * @param fileName
     *        is the filename.
     * @return the according Java {@link Class#getName() class-name} for the given
     *         <code>fileName</code> if it is a class-file that is no anonymous {@link Class}, else
     *         <code>null</code>.
     */
    public static String fixClassName(String fileName)
    {
        // replace path separator by package separator
        String result = fileName.replace('/', '.');

        // handle inner classes...
        final int lastDollar = result.lastIndexOf('$');
        if (lastDollar > 0)
        {
            char innerChar = result.charAt(lastDollar + 1);
            // ignore anonymous inner class
            if ((innerChar >= '0') && (innerChar <= '9'))
                return null;

            // TODO: check we really don't need to replace '$' by '.'
            // return result.replace('$', '.');
        }

        return result;
    }

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

    public static Object getFieldObject(Object object, String fieldName, boolean forceAccess)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        final Field field = getField(object, fieldName, forceAccess);

        if (field != null)
            return field.get(object);

        return null;
    }
}
