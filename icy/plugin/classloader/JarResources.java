/**
 * JCL (Jar Class Loader)
 * Copyright (C) 2011 Kamran Zafar
 * This file is part of Jar Class Loader (JCL).
 * Jar Class Loader (JCL) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * JarClassLoader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with JCL. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Kamran Zafar
 *         Contact Info:
 *         Email: xeus.man@gmail.com
 *         Web: http://xeustech.blogspot.com
 */

package icy.plugin.classloader;

import icy.plugin.classloader.exception.JclException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
    protected Map<String, byte[]> jarEntryContents;
    // <resourceName, fileName> map
    protected Map<String, URL> jarEntryUrls;

    protected boolean collisionAllowed;
    // keep trace of loaded resource size
    protected int loadedSize;

    private static Logger logger = Logger.getLogger(JarResources.class.getName());

    /**
     * Default constructor
     */
    public JarResources()
    {
        jarEntryContents = new HashMap<String, byte[]>();
        jarEntryUrls = new HashMap<String, URL>();
        collisionAllowed = Configuration.suppressCollisionException();
        loadedSize = 0;
    }

    /**
     * @param name
     * @return byte[]
     */
    public URL getResource(String name)
    {
        return jarEntryUrls.get(name);
    }

    /**
     * @param name
     * @return byte[]
     */
    public byte[] getResourceContent(String name)
    {
        byte content[] = jarEntryContents.get(name);

        // we load the content
        if (content == null)
        {
            final URL url = jarEntryUrls.get(name);

            if (url != null)
            {
                // load content and return it
                if (loadContent(name, url))
                    return jarEntryContents.get(name);
            }
        }

        return content;
    }

    protected boolean loadContent(String name, URL url)
    {
        // only support JAR resource here
        if (url.getProtocol().equalsIgnoreCase(("jar")))
        {
            final byte content[] = loadJarContent(url);

            if (content != null)
            {
                setResourceContent(name, content);
                return true;
            }
        }

        return false;
    }

    /**
     * Returns an immutable Set of all resources names
     */
    public Set<String> getResourcesName()
    {
        return Collections.unmodifiableSet(jarEntryUrls.keySet());
    }

    /**
     * Returns an immutable Map of all resources
     */
    public Map<String, URL> getResources()
    {
        return Collections.unmodifiableMap(jarEntryUrls);
    }

    /**
     * Returns an immutable Map of all loaded jar resources
     */
    public Map<String, byte[]> getLoadedResources()
    {
        return Collections.unmodifiableMap(jarEntryContents);
    }

    /**
     * Reads the specified jar file
     */
    public void loadJar(String jarFile)
    {
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Loading jar: " + jarFile);

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(jarFile);
            loadJar(new File(jarFile).toURI().toURL(), fis);
        }
        catch (IOException e)
        {
            throw new JclException(e);
        }
        finally
        {
            if (fis != null)
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                    throw new JclException(e);
                }
        }
    }

    /**
     * Reads the jar file from a specified URL
     */
    public void loadJar(URL url)
    {
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Loading jar: " + url.toString());

        InputStream in = null;
        try
        {
            in = url.openStream();
            loadJar(url, in);
        }
        catch (IOException e)
        {
            throw new JclException(e);
        }
        finally
        {
            if (in != null)
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    throw new JclException(e);
                }
        }
    }

    /**
     * Load the jar from InputStream
     */
    public void loadJar(URL baseUrl, InputStream jarStream)
    {
        BufferedInputStream bis = null;
        JarInputStream jis = null;

        try
        {
            bis = new BufferedInputStream(jarStream);
            jis = new JarInputStream(bis);

            JarEntry jarEntry = null;
            while ((jarEntry = jis.getNextJarEntry()) != null)
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest(dump(jarEntry));

                if (jarEntry.isDirectory())
                {
                    continue;
                }

                if (jarEntryUrls.containsKey(jarEntry.getName()))
                {
                    if (!collisionAllowed)
                        throw new JclException("Class/Resource " + jarEntry.getName() + " already loaded");

                    if (logger.isLoggable(Level.FINEST))
                        logger.finest("Class/Resource " + jarEntry.getName() + " already loaded; ignoring entry...");
                    continue;
                }

                // add to internal resource HashMap
                jarEntryUrls.put(jarEntry.getName(), new URL("jar:" + baseUrl.toString() + "!/" + jarEntry.getName()));
            }
        }
        catch (IOException e)
        {
            throw new JclException(e);
        }
        catch (NullPointerException e)
        {
            if (logger.isLoggable(Level.FINEST))
                logger.finest("Done loading.");
        }
        finally
        {
            if (jis != null)
                try
                {
                    jis.close();
                }
                catch (IOException e)
                {
                    throw new JclException(e);
                }

            if (bis != null)
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                    throw new JclException(e);
                }
        }
    }

    /**
     * Load the jar contents from InputStream
     */
    protected byte[] loadJarContent(URL url)
    {
        InputStream in = null;
        BufferedInputStream bis = null;
        JarFile jf = null;

        String path;
        int ind;
        String filename;
        String resname;

        try
        {
            path = url.getFile();
            ind = path.indexOf('!');
            filename = path.substring(5, ind);
            resname = path.substring(ind + 2);

            try
            {
                jf = new JarFile(filename);
            }
            catch (IOException e)
            {
                // try to decode the URL then
                path = URLDecoder.decode(url.getFile(), "UTF-8");
                ind = path.indexOf('!');
                filename = path.substring(5, ind);
                resname = path.substring(ind + 2);

                jf = new JarFile(filename);
            }

            final JarEntry jarEntry = jf.getJarEntry(resname);

            if (jarEntry != null)
            {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest(dump(jarEntry));

                in = jf.getInputStream(jarEntry);
                bis = new BufferedInputStream(in);

                byte[] b = new byte[2048];
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                int len = 0;
                while ((len = bis.read(b)) > 0)
                    out.write(b, 0, len);

                out.close();

                loadedSize += out.size();

                // System.out.println("Entry Name: " + jarEntry.getName() + ", Size: " + out.size()
                // + " ("
                // + (loadedSize / 1024) + " KB)");

                return out.toByteArray();
            }

            return null;
        }
        catch (IOException e)
        {
            throw new JclException(e);
        }
        finally
        {
            if (bis != null)
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                    throw new JclException(e);
                }

            if (in != null)
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    throw new JclException(e);
                }

            if (jf != null)
                try
                {
                    jf.close();
                }
                catch (IOException e)
                {
                    throw new JclException(e);
                }
        }
    }

    protected void setResourceContent(String name, byte content[])
    {
        if (jarEntryContents.containsKey(name))
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
        jarEntryContents.put(name, content);
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
        {
            sb.append("d ");
        }
        else
        {
            sb.append("f ");
        }

        if (je.getMethod() == ZipEntry.STORED)
        {
            sb.append("stored   ");
        }
        else
        {
            sb.append("defalted ");
        }

        sb.append(je.getName());
        sb.append("\t");
        sb.append("" + je.getSize());
        if (je.getMethod() == ZipEntry.DEFLATED)
        {
            sb.append("/" + je.getCompressedSize());
        }

        return (sb.toString());
    }
}
