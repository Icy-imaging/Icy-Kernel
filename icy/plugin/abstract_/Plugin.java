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
package icy.plugin.abstract_;

import icy.file.FileUtil;
import icy.gui.frame.IcyFrame;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.ImageUtil;
import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.plugin.interface_.PluginBundled;
import icy.plugin.interface_.PluginThreaded;
import icy.preferences.PluginsPreferences;
import icy.preferences.XMLPreferences;
import icy.resource.ResourceUtil;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.system.audit.Audit;
import icy.util.ClassUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ImageIcon;

/**
 * Base class for Plugin, provide some helper methods.<br>
 * By default the constructor of a Plugin class is called in the EDT (Event Dispatch Thread).<br>
 * If the plugin implements the {@link PluginThreaded} there is no more guarantee that is the case.
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public abstract class Plugin
{
    public static Plugin getPlugin(List<Plugin> list, String className)
    {
        for (Plugin plugin : list)
            if (plugin.getClass().getName().equals(className))
                return plugin;

        return null;
    }

    private PluginDescriptor descriptor;

    public Plugin(PluginDescriptor desc)
    {
        super();

        // get descriptor from loader
        if (desc == null)
            descriptor = PluginLoader.getPlugin(getClass().getName());
        else
            descriptor = desc;

        if (descriptor == null)
        {
            // descriptor not found (don't check for anonymous plugin class) ?
            if (!getClass().isAnonymousClass())
            {
                System.out.println("Warning : Plugin '" + getClass().getName()
                        + "' started but not found in PluginLoader !");
                System.out.println("Local XML plugin description file is probably incorrect.");
            }

            // create dummy descriptor
            descriptor = new PluginDescriptor(this.getClass());
            descriptor.setName(getClass().getSimpleName());
        }

        // audit
        Audit.pluginInstancied(this);
    }

    public Plugin()
    {
        this(null);
    }

    @Override
    protected void finalize() throws Throwable
    {
        // unregister plugin (weak reference so we can do it here)
        Icy.getMainInterface().unRegisterPlugin(this);

        super.finalize();
    }

    /**
     * @return the descriptor
     */
    public PluginDescriptor getDescriptor()
    {
        return descriptor;
    }

    /**
     * @return the plugin name (from its descriptor)
     */
    public String getName()
    {
        return descriptor.getName();
    }

    /**
     * @return <code>true</code> if this is a bundled plugin (see {@link PluginBundled}).
     */
    public boolean isBundled()
    {
        return this instanceof PluginBundled;
    }

    /**
     * @return the class name of the plugin owner.<br>
     *         If this Plugin is not bundled (see {@link PluginBundled}) then it just returns the
     *         current class name otherwise it will returns the plugin owner class name.
     */
    public String getOwnerClassName()
    {
        if (isBundled())
            return ((PluginBundled) this).getMainPluginClassName();

        return getClass().getName();
    }

    /**
     * @return the folder where the plugin is installed (or should be installed).
     */
    public String getInstallFolder()
    {
        return ClassUtil.getPathFromQualifiedName(ClassUtil.getPackageName(getClass().getName()));
    }

    public Viewer getActiveViewer()
    {
        return Icy.getMainInterface().getActiveViewer();
    }

    public Sequence getActiveSequence()
    {
        return Icy.getMainInterface().getActiveSequence();
    }

    public IcyBufferedImage getActiveImage()
    {
        return Icy.getMainInterface().getActiveImage();
    }

    /**
     * @deprecated Use {@link #getActiveViewer()} instead
     */
    @Deprecated
    public Viewer getFocusedViewer()
    {
        return getActiveViewer();
    }

    /**
     * @deprecated Use {@link #getActiveSequence()} instead
     */
    @Deprecated
    public Sequence getFocusedSequence()
    {
        return getActiveSequence();
    }

    /**
     * @deprecated Use {@link #getActiveImage()} instead
     */
    @Deprecated
    public IcyBufferedImage getFocusedImage()
    {
        return getActiveImage();
    }

    public void addIcyFrame(final IcyFrame frame)
    {
        frame.addToDesktopPane();
    }

    public void addSequence(final Sequence sequence)
    {
        Icy.getMainInterface().addSequence(sequence);
    }

    public void removeSequence(final Sequence sequence)
    {
        sequence.close();
    }

    public ArrayList<Sequence> getSequences()
    {
        return Icy.getMainInterface().getSequences();
    }

    /**
     * Return the resource URL from given resource name.<br>
     * Ex: <code>getResource("plugins/author/resources/def.xml");</code>
     * 
     * @param name
     *        resource name
     */
    public URL getResource(String name)
    {
        return getClass().getClassLoader().getResource(name);
    }

    /**
     * Return resources corresponding to given resource name.<br>
     * Ex: <code>getResources("plugins/author/resources/def.xml");</code>
     * 
     * @param name
     *        resource name
     * @throws IOException
     */
    public Enumeration<URL> getResources(String name) throws IOException
    {
        return getClass().getClassLoader().getResources(name);
    }

    /**
     * Return the resource as data stream from given resource name.<br>
     * Ex: <code>getResourceAsStream("plugins/author/resources/def.xml");</code>
     * 
     * @param name
     *        resource name
     */
    public InputStream getResourceAsStream(String name)
    {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    /**
     * Return the image resource from given resource name
     * Ex: <code>getResourceAsStream("plugins/author/resources/image.png");</code>
     * 
     * @param resourceName
     *        resource name
     */
    public BufferedImage getImageResource(String resourceName)
    {
        return ImageUtil.load(getResourceAsStream(resourceName));
    }

    /**
     * Return the icon resource from given resource name
     * Ex: <code>getResourceAsStream("plugins/author/resources/icon.png");</code>
     * 
     * @param resourceName
     *        resource name
     */
    public ImageIcon getIconResource(String resourceName)
    {
        return ResourceUtil.getImageIcon(getImageResource(resourceName));
    }

    /**
     * Retrieve the preferences root for this plugin.<br>
     */
    public XMLPreferences getPreferencesRoot()
    {
        return PluginsPreferences.root(this);
    }

    /**
     * Retrieve the plugin preferences node for specified name.<br>
     * i.e : getPreferences("window") will return node
     * "plugins.[authorPackage].[pluginClass].window"
     */
    public XMLPreferences getPreferences(String name)
    {
        return getPreferencesRoot().node(name);
    }

    /**
     * Returns the base resource path for plugin native libraries.<br/>
     * Depending the Operating System it can returns these values:
     * <ul>
     * <li>lib/unix32</li>
     * <li>lib/unix64</li>
     * <li>lib/mac32</li>
     * <li>lib/mac64</li>
     * <li>lib/win32</li>
     * <li>lib/win64</li>
     * </ul>
     */
    protected String getResourceLibraryPath()
    {
        return "lib" + ResourceUtil.separator + SystemUtil.getOSArchIdString();
    }

    /**
     * Load a packed native library from the JAR file.<br/>
     * Native libraries should be packaged with the following directory & file structure:
     * 
     * <pre>
     * /lib/unix32
     *   libxxx.so
     * /lib/unix64
     *   libxxx.so
     * /lib/mac32
     *   libxxx.dylib
     * /lib/mac64
     *   libxxx.dylib
     * /lib/win32
     *   xxx.dll
     * /lib/win64
     *   xxx.dll
     * /plugins/myname/mypackage    
     *   MyPlugin.class
     *   ....
     * </pre>
     * 
     * Here "xxx" is the name of the native library.<br/>
     * Current approach is to unpack the native library into a temporary file and load from there.
     * 
     * @param libName
     * @return true if the library was correctly loaded.
     * @see SystemUtil#loadLibrary(String)
     */
    public boolean loadLibrary(String libName)
    {
        try
        {
            // get mapped library name
            String mappedlibName = System.mapLibraryName(libName);
            // get base resource path for native library
            final String basePath = getResourceLibraryPath() + ResourceUtil.separator;

            // search for library in resource
            URL libUrl = getResource(basePath + mappedlibName);

            // not found ?
            if (libUrl == null)
            {
                // jnilib extension may not work, try with "dylib" extension instead
                if (mappedlibName.endsWith(".jnilib"))
                {
                    mappedlibName = mappedlibName.substring(0, mappedlibName.length() - 7) + ".dylib";
                    libUrl = getResource(basePath + mappedlibName);
                }
            }

            // resource not found --> error
            if (libUrl == null)
                throw new IOException("Couldn't find resource " + basePath + mappedlibName);

            // extract resource
            final File extractedFile = extractResource(SystemUtil.getTempLibraryDirectory() + FileUtil.separator
                    + mappedlibName, libUrl);
            // and load it
            System.load(extractedFile.getPath());

            return true;
        }
        catch (IOException e)
        {
            System.err.println("Error while loading packed library " + libName + ": " + e);
        }

        return false;
    }

    /**
     * Extract a resource to the specified path
     * 
     * @param outputPath
     *        the file to extract the resource to
     * @param resource
     *        the resource URL
     * @return the extracted file
     * @throws IOException
     */
    protected File extractResource(String outputPath, URL resource) throws IOException
    {
        // open resource stream
        final InputStream in = resource.openStream();
        // load resource
        final byte data[] = NetworkUtil.download(in);
        // create output file
        final File result = new File(outputPath);

        // file already exist ??
        if (result.exists())
        {
            // same size --> assume it's the same
            if (result.length() == data.length)
                return result;

            if (!FileUtil.delete(result, false))
                throw new IOException("Cannot overwrite " + result + " file !");
        }

        // save resource to file
        FileUtil.save(result, data, true);

        return result;
    }

    /**
     * Report an error log for this plugin (reported to Icy web site which report then to the
     * author of the plugin).
     * 
     * @see IcyExceptionHandler#report(PluginDescriptor, String)
     */
    public void report(String errorLog)
    {
        IcyExceptionHandler.report(descriptor, errorLog);
    }

    @Override
    public String toString()
    {
        return getDescriptor().getName();
    }
}
