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

package icy.plugin.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class loader that can load classes from different resources
 * 
 * @author Kamran Zafar
 * @author Stephane Dallongeville
 */
@SuppressWarnings("unchecked")
public abstract class AbstractClassLoader extends ClassLoader
{

    protected final List<ProxyClassLoader> loaders = new ArrayList<ProxyClassLoader>();

    private final ProxyClassLoader systemLoader = new SystemLoader();
    private final ProxyClassLoader parentLoader = new ParentLoader();
    private final ProxyClassLoader currentLoader = new CurrentLoader();
    private final ProxyClassLoader threadLoader = new ThreadContextLoader();

    /**
     * Build a new instance of AbstractClassLoader.java.
     * 
     * @param parent
     *        parent class loader
     */
    public AbstractClassLoader(ClassLoader parent)
    {
        super(parent);
        addDefaultLoader();
    }

    public void addLoader(ProxyClassLoader loader)
    {
        loaders.add(loader);

        Collections.sort(loaders);
    }

    protected void addDefaultLoader()
    {
        // always add this one
        loaders.add(systemLoader);
        loaders.add(parentLoader);
        loaders.add(currentLoader);
        loaders.add(threadLoader);

        Collections.sort(loaders);
    }

    @Override
    public Class loadClass(String className) throws ClassNotFoundException
    {
        return (loadClass(className, true));
    }

    /**
     * Overrides the loadClass method to load classes from other resources,
     * JarClassLoader is the only subclass in this project that loads classes
     * from jar files
     * 
     * @see java.lang.ClassLoader#loadClass(String, boolean)
     */
    @Override
    public Class loadClass(String className, boolean resolveIt) throws ClassNotFoundException
    {
        if (className == null || className.trim().equals(""))
            return null;

        final List<ProxyClassLoader> loadersDone = new ArrayList<ProxyClassLoader>();

        for (ProxyClassLoader l : loaders)
        {
            // don't search in same loader
            if (l.isEnabled() && !loadersDone.contains(l))
            {
                final Class clazz = l.loadClass(className, resolveIt);
                if (clazz != null)
                    return clazz;

                // loader done
                loadersDone.add(l);
            }
        }

        throw new ClassNotFoundException(className);
    }

    /**
     * Overrides the getResourceAsStream method to load non-class resources from
     * other sources, JarClassLoader is the only subclass in this project that
     * loads non-class resources from jar files
     * 
     * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream(String name)
    {
        if (name == null || name.trim().equals(""))
            return null;

        for (ProxyClassLoader l : loaders)
        {
            if (l.isEnabled())
            {
                final InputStream is = l.getResourceAsStream(name);
                if (is != null)
                    return is;
            }
        }

        return null;
    }

    @Override
    public URL getResource(String name)
    {
        if (name == null || name.trim().equals(""))
            return null;

        for (ProxyClassLoader l : loaders)
        {
            if (l.isEnabled())
            {
                final URL url = l.getResource(name);
                if (url != null)
                    return url;
            }
        }

        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException
    {
        if (name == null || name.trim().equals(""))
            return null;

        final List<URL> result = new ArrayList<URL>();

        for (ProxyClassLoader l : loaders)
        {
            if (l.isEnabled())
            {
                final Enumeration<URL> urls = l.getResources(name);

                if (urls != null)
                {
                    while (urls.hasMoreElements())
                    {
                        final URL url = urls.nextElement();

                        // avoid duplicate
                        if (!result.contains(url))
                            result.add(url);
                    }
                }
            }
        }

        return Collections.enumeration(result);
    }

    /**
     * System class loader
     */
    class SystemLoader extends ProxyClassLoader
    {
        private final Logger logger = Logger.getLogger(SystemLoader.class.getName());

        public SystemLoader()
        {
            super(10);

            enabled = Configuration.isSystemLoaderEnabled();
        }

        @Override
        public Object getLoader()
        {
            return getSystemClassLoader();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt)
        {
            Class result;

            try
            {
                result = findSystemClass(className);
            }
            catch (ClassNotFoundException e)
            {
                return null;
            }

            if (logger.isLoggable(Level.FINEST))
                logger.finest("Returning system class " + className);

            return result;
        }

        @Override
        public InputStream getResourceAsStream(String name)
        {
            InputStream is = getSystemResourceAsStream(name);

            if (is != null)
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning system resource " + name);

                return is;
            }

            return null;
        }

        @Override
        public URL getResource(String name)
        {
            URL url = getSystemResource(name);

            if (url != null)
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning system resource " + name);

                return url;
            }

            return null;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException
        {
            Enumeration<URL> urls = getSystemResources(name);

            if ((urls != null) && urls.hasMoreElements())
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning system resources " + name);

                return urls;
            }

            return null;
        }
    }

    /**
     * Parent class loader
     */
    class ParentLoader extends ProxyClassLoader
    {
        private final Logger logger = Logger.getLogger(ParentLoader.class.getName());

        public ParentLoader()
        {
            super(30);

            enabled = Configuration.isParentLoaderEnabled();
        }

        @Override
        public Object getLoader()
        {
            return getParent();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt)
        {
            Class result;

            try
            {
                result = getParent().loadClass(className);
            }
            catch (ClassNotFoundException e)
            {
                return null;
            }

            if (logger.isLoggable(Level.FINEST))
                logger.finest("Returning class " + className + " loaded with parent classloader");

            return result;
        }

        @Override
        public InputStream getResourceAsStream(String name)
        {
            InputStream is = getParent().getResourceAsStream(name);

            if (is != null)
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning resource " + name + " loaded with parent classloader");

                return is;
            }
            return null;
        }

        @Override
        public URL getResource(String name)
        {
            URL url = getParent().getResource(name);

            if (url != null)
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning resource " + name + " loaded with parent classloader");

                return url;
            }

            return null;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException
        {
            Enumeration<URL> urls = getParent().getResources(name);

            if ((urls != null) && urls.hasMoreElements())
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning resource " + name + " loaded with parent classloader");

                return urls;
            }

            return null;
        }
    }

    /**
     * Current class loader
     */
    class CurrentLoader extends ProxyClassLoader
    {
        private final Logger logger = Logger.getLogger(CurrentLoader.class.getName());

        public CurrentLoader()
        {
            super(20);

            enabled = Configuration.isCurrentLoaderEnabled();
        }

        @Override
        public Object getLoader()
        {
            return getClass().getClassLoader();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt)
        {
            Class result;

            try
            {
                result = getClass().getClassLoader().loadClass(className);
            }
            catch (ClassNotFoundException e)
            {
                return null;
            }

            if (logger.isLoggable(Level.FINEST))
                logger.finest("Returning class " + className + " loaded with current classloader");

            return result;
        }

        @Override
        public InputStream getResourceAsStream(String name)
        {
            InputStream is = getClass().getClassLoader().getResourceAsStream(name);

            if (is != null)
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning resource " + name + " loaded with current classloader");

                return is;
            }

            return null;
        }

        @Override
        public URL getResource(String name)
        {
            URL url = getClass().getClassLoader().getResource(name);

            if (url != null)
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning resource " + name + " loaded with current classloader");

                return url;
            }

            return null;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException
        {
            Enumeration<URL> urls = getClass().getClassLoader().getResources(name);

            if ((urls != null) && (urls.hasMoreElements()))
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning resources " + name + " loaded with current classloader");

                return urls;
            }

            return null;
        }

    }

    /**
     * Current class loader
     */
    class ThreadContextLoader extends ProxyClassLoader
    {
        private final Logger logger = Logger.getLogger(ThreadContextLoader.class.getName());

        public ThreadContextLoader()
        {
            super(40);

            enabled = Configuration.isThreadContextLoaderEnabled();
        }

        @Override
        public Object getLoader()
        {
            return Thread.currentThread().getContextClassLoader();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt)
        {
            Class result;
            try
            {
                result = Thread.currentThread().getContextClassLoader().loadClass(className);
            }
            catch (ClassNotFoundException e)
            {
                return null;
            }

            if (logger.isLoggable(Level.FINEST))
                logger.finest("Returning class " + className + " loaded with thread context classloader");

            return result;
        }

        @Override
        public InputStream getResourceAsStream(String name)
        {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);

            if (is != null)
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning resource " + name + " loaded with thread context classloader");

                return is;
            }

            return null;
        }

        @Override
        public URL getResource(String name)
        {
            URL url = Thread.currentThread().getContextClassLoader().getResource(name);

            if (url != null)
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning resource " + name + " loaded with thread context classloader");

                return url;
            }

            return null;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException
        {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(name);

            if ((urls != null) && (urls.hasMoreElements()))
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning resources " + name + " loaded with thread context classloader");

                return urls;
            }

            return null;
        }
    }

    public ProxyClassLoader getSystemLoader()
    {
        return systemLoader;
    }

    public ProxyClassLoader getParentLoader()
    {
        return parentLoader;
    }

    public ProxyClassLoader getCurrentLoader()
    {
        return currentLoader;
    }

    public ProxyClassLoader getThreadLoader()
    {
        return threadLoader;
    }
}
