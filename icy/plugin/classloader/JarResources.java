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

package icy.plugin.classloader;

import icy.network.NetworkUtil;
import icy.plugin.classloader.exception.JclException;
import icy.system.IcyExceptionHandler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * JarResources reads jar files and loads the class content/bytes in a HashMap
 * 
 * @author Kamran Zafar
 * @author Stephane Dallongeville
 */
public class JarResources
{
    // <resourceName, content> map
    protected Map<String, byte[]> entryContents;
    // <resourceName, fileName> map
    protected Map<String, URL> entryUrls;

    protected boolean collisionAllowed;
    // keep trace of loaded resource size
    protected int loadedSize;

    private static Logger logger = Logger.getLogger(JarResources.class.getName());

    /**
     * Default constructor
     */
    public JarResources()
    {
        entryContents = new HashMap<String, byte[]>();
        entryUrls = new HashMap<String, URL>();
        collisionAllowed = Configuration.suppressCollisionException();
        loadedSize = 0;
    }

    public URL getResource(String name)
    {
        return entryUrls.get(name);
    }

    public byte[] getResourceContent(String name) throws IOException
    {
        byte content[] = entryContents.get(name);

        // we load the content
        if (content == null)
        {
            final URL url = entryUrls.get(name);

            if (url != null)
            {
                // load content and return it
                loadContent(name, url);
                content = entryContents.get(name);

//                try
//                {
//                    // load content and return it
//                    loadContent(name, url);
//                    content = entryContents.get(name);
//                }
//                catch (IOException e)
//                {
//                    // content cannot be loaded, remove the URL entry
//                    // better to not remove it after all, so we know why it failed next time
//                    // entryUrls.remove(name);
//                    
//                    // and throw exception
//                    throw e;
//                }
            }
        }

        return content;
    }

    protected void loadContent(String name, URL url) throws IOException
    {
        // only support JAR resource here
        final byte[] content = loadJarContent(url);
        setResourceContent(name, content);
    }

    /**
     * Returns an immutable Set of all resources names
     */
    public Set<String> getResourcesName()
    {
        return Collections.unmodifiableSet(entryUrls.keySet());
    }

    /**
     * Returns an immutable Map of all resources
     */
    public Map<String, URL> getResources()
    {
        return Collections.unmodifiableMap(entryUrls);
    }

    /**
     * Returns an immutable Map of all loaded jar resources
     */
    public Map<String, byte[]> getLoadedResources()
    {
        return Collections.unmodifiableMap(entryContents);
    }

    /**
     * Reads the jar file from a specified URL
     * 
     * @throws IOException
     */
    public void loadJar(URL url) throws IOException
    {
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Loading jar: " + url.toString());

        BufferedInputStream bis = null;
        JarInputStream jis = null;

        try
        {
            final InputStream in = url.openStream();

            if (in instanceof BufferedInputStream)
                bis = (BufferedInputStream) in;
            else
                bis = new BufferedInputStream(in);

            jis = new JarInputStream(bis);

            JarEntry jarEntry = null;
            while ((jarEntry = jis.getNextJarEntry()) != null)
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest(dump(jarEntry));

                if (jarEntry.isDirectory())
                    continue;

                if (entryUrls.containsKey(jarEntry.getName()))
                {
                    if (!collisionAllowed)
                        throw new JclException("Class/Resource " + jarEntry.getName() + " already loaded");

                    if (logger.isLoggable(Level.FINEST))
                        logger.finest("Class/Resource " + jarEntry.getName() + " already loaded; ignoring entry...");
                    continue;
                }

                // add to internal resource HashMap
                entryUrls.put(jarEntry.getName(), new URL("jar:" + url.toString() + "!/" + jarEntry.getName()));
            }
        }
        // catch (NullPointerException e)
        // {
        // if (logger.isLoggable(Level.FINEST))
        // logger.finest("Done loading.");
        // }
        finally
        {
            if (jis != null)
            {
                try
                {
                    jis.close();
                }
                catch (IOException e)
                {
                    // not important
                    System.err.println("JarResources.loadJar(" + url + ") error:");
                    IcyExceptionHandler.showErrorMessage(e, false, true);
                }
            }

            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                    // not important
                    System.err.println("JarResources.loadJar(" + url + ") error:");
                    IcyExceptionHandler.showErrorMessage(e, false, true);
                }
            }
        }
    }

    /**
     * Load the jar contents from InputStream
     * 
     * @throws IOException
     */
    protected byte[] loadJarContent(URL url) throws IOException
    {
        final JarURLConnection uc = (JarURLConnection) url.openConnection();
        final JarEntry jarEntry = uc.getJarEntry();

        if (jarEntry != null)
        {
            if (logger.isLoggable(Level.FINEST))
                logger.finest(dump(jarEntry));

            return NetworkUtil.download(uc.getInputStream(), jarEntry.getSize(), null);
        }

        throw new IOException("JarResources.loadJarContent(" + url.toString() + ") error:\nEntry not found !");
    }

    protected void setResourceContent(String name, byte content[])
    {
        if (entryContents.containsKey(name))
        {
            if (!collisionAllowed)
                throw new JclException("Class/Resource " + name + " already loaded");

            if (logger.isLoggable(Level.FINEST))
                logger.finest("Class/Resource " + name + " already loaded; ignoring entry...");
            return;
        }

        if (logger.isLoggable(Level.FINEST))
            logger.finest("Entry Name: " + name + ", " + "Entry Size: " + content.length);

        // add to internal resource HashMap
        entryContents.put(name, content);
    }

    /**
     * For debugging
     * 
     * @param je
     * @return String
     */
    private String dump(JarEntry je)
    {
        StringBuffer sb = new StringBuffer();
        if (je.isDirectory())
            sb.append("d ");
        else
            sb.append("f ");

        if (je.getMethod() == ZipEntry.STORED)
            sb.append("stored   ");
        else
            sb.append("deflated ");

        sb.append(je.getName());
        sb.append("\t");
        sb.append("" + je.getSize());
        if (je.getMethod() == ZipEntry.DEFLATED)
            sb.append("/" + je.getCompressedSize());

        return (sb.toString());
    }
}
