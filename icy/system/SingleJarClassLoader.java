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
package icy.system;

import icy.util.JarUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @deprecated
 * @author Stephane
 */
public class SingleJarClassLoader extends ClassLoader
{
    // The context to be used when loading classes and resources
    private AccessControlContext acc;
    // JAR file
    File f;

    public SingleJarClassLoader(File jarFile, ClassLoader parent)
    {
        super(parent);

        f = jarFile;

        // this is to make the stack depth consistent with 1.1
        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkCreateClassLoader();

        acc = AccessController.getContext();
    }

    public SingleJarClassLoader(String jarFileName, ClassLoader parent)
    {
        this(new File(jarFileName), parent);
    }

    public SingleJarClassLoader(File jarFile)
    {
        this(jarFile, null);
    }

    public SingleJarClassLoader(String jarFileName)
    {
        this(jarFileName, null);
    }

    Class<?> findSysClass(String name) throws ClassNotFoundException
    {
        return findSystemClass(name);
    }

    Class<?> defClass(String name, byte[] b, int off, int len) throws ClassFormatError
    {
        return defineClass(name, b, off, len, null);
    }

    /**
     * Finds and loads the class with the specified name from the URL search path. Any URLs
     * referring to JAR files are loaded and opened as needed until the class is found.
     * 
     * @param name
     *        the name of the class
     * @return the resulting class
     * @exception ClassNotFoundException
     *            if the class could not be found
     */
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException
    {
        try
        {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>()
            {
                public Class<?> run() throws ClassNotFoundException
                {
                    JarFile jarFile = null;
                    BufferedInputStream bis = null;
                    byte[] res = null;

                    try
                    {
                        jarFile = new JarFile(f);
                        JarEntry jarEntry = jarFile.getJarEntry(name.replace('.', '/') + ".class");
                        res = new byte[(int) jarEntry.getSize()];
                        bis = new BufferedInputStream(jarFile.getInputStream(jarEntry));
                        bis.read(res, 0, res.length);
                    }
                    catch (Exception ex)
                    {
                    }
                    finally
                    {
                        if (bis != null)
                        {
                            try
                            {
                                bis.close();
                            }
                            catch (IOException ioex)
                            {
                            }
                        }
                        if (jarFile != null)
                        {
                            try
                            {
                                jarFile.close();
                            }
                            catch (IOException ioex)
                            {
                            }
                        }
                    }

                    // search in SystemClass...
                    if (res == null)
                        return findSysClass(name);

                    Class<?> result;

                    result = defClass(name, res, 0, res.length);
                    if (result == null)
                        throw new ClassFormatError("Incorrect format for class " + name);

                    return result;
                }
            }, acc);
        }
        catch (java.security.PrivilegedActionException pae)
        {
            throw (ClassNotFoundException) pae.getException();
        }
    }

    /**
     * Finds the resource with the specified name on the URL search path.
     * 
     * @param name
     *        the name of the resource
     * @return a <code>URL</code> for the resource, or <code>null</code> if the resource could not
     *         be found.
     */
    @Override
    public URL findResource(final String name)
    {
        /*
         * The same restriction to finding classes applies to resources
         */
        final URL url = AccessController.doPrivileged(new PrivilegedAction<URL>()
        {
            public URL run()
            {
                JarFile jarFile = null;

                try
                {
                    jarFile = new JarFile(f);

                    final JarEntry jarEntry = JarUtil.getJarEntry(jarFile, name);
                    if (jarEntry != null)
                        return JarUtil.getJarURL(f.getAbsolutePath(), jarEntry);
                }
                catch (Exception e)
                {
                    return null;
                }
                finally
                {
                    if (jarFile != null)
                    {
                        try
                        {
                            jarFile.close();
                        }
                        catch (IOException e)
                        {
                            // ignore
                        }
                    }
                }

                return null;
            }
        }, acc);

        return url;
    }

    @Override
    public Enumeration<URL> findResources(final String name) throws IOException
    {
        final URL url = findResource(name);

        return new Enumeration<URL>()
        {
            private boolean read = false;

            public URL nextElement()
            {
                if (read)
                    throw new NoSuchElementException();

                read = true;
                return url;
            }

            public boolean hasMoreElements()
            {
                return !read;
            }
        };
    }

}
