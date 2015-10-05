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

import icy.file.FileUtil;
import icy.network.NetworkUtil;
import icy.plugin.classloader.exception.JclException;
import icy.plugin.classloader.exception.ResourceNotFoundException;
import icy.system.IcyExceptionHandler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that builds a local classpath by loading resources from different
 * files/paths
 * 
 * @author Kamran Zafar
 * @author Stephane Dallongeville
 */
public class ClasspathResources extends JarResources
{
    private static Logger logger = Logger.getLogger(ClasspathResources.class.getName());
    private boolean ignoreMissingResources;

    public ClasspathResources()
    {
        super();
        ignoreMissingResources = Configuration.suppressMissingResourceException();
    }

    /**
     * Attempts to load a remote resource (jars, properties files, etc)
     * 
     * @param url
     */
    protected void loadRemoteResource(URL url)
    {
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Attempting to load a remote resource.");

        if (url.toString().toLowerCase().endsWith(".jar"))
        {
            try
            {
                loadJar(url);
            }
            catch (IOException e)
            {
                System.err.println("JarResources.loadJar(" + url + ") error:");
                IcyExceptionHandler.showErrorMessage(e, false, true);
            }
            return;
        }

        if (entryUrls.containsKey(url.toString()))
        {
            if (!collisionAllowed)
                throw new JclException("Resource " + url.toString() + " already loaded");

            if (logger.isLoggable(Level.FINEST))
                logger.finest("Resource " + url.toString() + " already loaded; ignoring entry...");
            return;
        }

        if (logger.isLoggable(Level.FINEST))
            logger.finest("Loading remote resource.");

        entryUrls.put(url.toString(), url);
    }

    /**
     * Loads and returns content the remote resource (jars, properties files, etc)
     * 
     * @throws IOException
     */
    protected byte[] loadRemoteResourceContent(URL url) throws IOException
    {
        final byte[] result = NetworkUtil.download(url.openStream());

        if (result != null)
            loadedSize += result.length;

        return result;
    }

    /**
     * Reads local and remote resources
     * 
     * @param url
     */
    protected void loadResource(URL url)
    {
        try
        {
            final File file = new File(url.toURI());
            // Is Local
            loadResource(file, FileUtil.getGenericPath(file.getAbsolutePath()));
        }
        catch (IllegalArgumentException iae)
        {
            // Is Remote
            loadRemoteResource(url);
        }
        catch (URISyntaxException e)
        {
            throw new JclException("URISyntaxException", e);
        }
    }

    /**
     * Reads local resources from - Jar files - Class folders - Jar Library
     * folders
     * 
     * @param path
     */
    protected void loadResource(String path)
    {
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Resource: " + path);

        File fp = new File(path);

        if (!fp.exists() && !ignoreMissingResources)
            throw new JclException("File/Path does not exist");

        loadResource(fp, FileUtil.getGenericPath(path));
    }

    /**
     * Reads local resources from - Jar files - Class folders - Jar Library
     * folders
     * 
     * @param fol
     * @param packName
     */
    protected void loadResource(File fol, String packName)
    {
        // FILE
        if (fol.isFile())
        {
            if (fol.getName().toLowerCase().endsWith(".jar"))
            {
                try
                {
                    loadJar(fol.toURI().toURL());
                }
                catch (IOException e)
                {
                    System.err.println("JarResources.loadJar(" + fol.getAbsolutePath() + ") error:");
                    IcyExceptionHandler.showErrorMessage(e, false, true);
                }
            }
            else
                loadResourceInternal(fol, packName);
        }
        // DIRECTORY
        else
        {
            if (fol.list() != null)
            {
                for (String f : fol.list())
                {
                    File fl = new File(fol.getAbsolutePath() + "/" + f);

                    String pn = packName;

                    if (fl.isDirectory())
                    {

                        if (!pn.equals(""))
                            pn = pn + "/";

                        pn = pn + fl.getName();
                    }

                    loadResource(fl, pn);
                }
            }
        }
    }

    /**
     * Loads the local resource.
     */
    protected void loadResourceInternal(File file, String pack)
    {
        String entryName = "";

        if (pack.length() > 0)
            entryName = pack + "/";
        entryName += file.getName();

        if (entryUrls.containsKey(entryName))
        {
            if (!collisionAllowed)
                throw new JclException("Resource " + entryName + " already loaded");

            if (logger.isLoggable(Level.WARNING))
                logger.finest("Resource " + entryName + " already loaded; ignoring entry...");
            return;
        }

        if (logger.isLoggable(Level.FINEST))
            logger.finest("Loading resource: " + entryName);

        try
        {
            entryUrls.put(entryName, file.toURI().toURL());
        }
        catch (Exception e)
        {
            if (logger.isLoggable(Level.SEVERE))
                logger.finest("Error while loading: " + entryName);

            System.err.println("JarResources.loadResourceInternal(" + file.getAbsolutePath() + ") error:");
            IcyExceptionHandler.showErrorMessage(e, false, true);
        }
    }

    @Override
    protected void loadContent(String name, URL url) throws IOException
    {
        // JAR protocol
        if (url.getProtocol().equalsIgnoreCase(("jar")))
            super.loadContent(name, url);
        // FILE protocol
        else if (url.getProtocol().equalsIgnoreCase(("file")))
        {
            final byte[] content = loadResourceContent(url);
            setResourceContent(name, content);
        }
        // try remote loading
        else
        {
            final byte content[] = loadRemoteResourceContent(url);
            setResourceContent(name, content);
        }
    }

    /**
     * Loads and returns the local resource content.
     * 
     * @throws IOException
     */
    protected byte[] loadResourceContent(URL url) throws IOException
    {
        final byte[] result = NetworkUtil.download(url.openStream());

        if (result != null)
            loadedSize += result.length;

        return result;
    }

    /**
     * Removes the loaded resource
     * 
     * @param resource
     */
    public void unload(String resource)
    {
        if (entryContents.containsKey(resource))
        {
            if (logger.isLoggable(Level.FINEST))
                logger.finest("Removing resource " + resource);
            entryContents.remove(resource);
        }
        else
            throw new ResourceNotFoundException(resource, "Resource not found in local ClasspathResources");
    }

    public boolean isCollisionAllowed()
    {
        return collisionAllowed;
    }

    public void setCollisionAllowed(boolean collisionAllowed)
    {
        this.collisionAllowed = collisionAllowed;
    }

    public boolean isIgnoreMissingResources()
    {
        return ignoreMissingResources;
    }

    public void setIgnoreMissingResources(boolean ignoreMissingResources)
    {
        this.ignoreMissingResources = ignoreMissingResources;
    }
}
