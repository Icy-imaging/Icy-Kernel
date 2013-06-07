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
package icy.plugin;

import icy.common.Version;
import icy.file.FileUtil;
import icy.file.xml.XMLPersistent;
import icy.file.xml.XMLPersistentHelper;
import icy.image.ImageUtil;
import icy.network.NetworkUtil;
import icy.network.URLUtil;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginImageAnalysis;
import icy.preferences.RepositoryPreferences.RepositoryInfo;
import icy.resource.ResourceUtil;
import icy.util.ClassUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <br>
 * The plugin descriptor contains all the data needed to launch a plugin. <br>
 * 
 * @see PluginLauncher
 * @author Fabrice de Chaumont & Stephane
 */
public class PluginDescriptor implements XMLPersistent
{
    public static class PluginNameSorter implements Comparator<PluginDescriptor>
    {
        // static class
        public static PluginNameSorter instance = new PluginNameSorter();

        // static class
        private PluginNameSorter()
        {
            super();
        }

        @Override
        public int compare(PluginDescriptor o1, PluginDescriptor o2)
        {
            return o1.toString().compareToIgnoreCase(o2.toString());
        }
    }

    public static class PluginClassNameSorter implements Comparator<PluginDescriptor>
    {
        // static class
        public static PluginClassNameSorter instance = new PluginClassNameSorter();

        // static class
        private PluginClassNameSorter()
        {
            super();
        }

        @Override
        public int compare(PluginDescriptor o1, PluginDescriptor o2)
        {
            return o1.getClassName().compareToIgnoreCase(o2.getClassName());
        }
    }

    private static final int ICON_SIZE = 64;
    private static final int IMAGE_SIZE = 256;

    public static final ImageIcon DEFAULT_ICON = ResourceUtil.getImageIcon(ResourceUtil.IMAGE_PLUGIN_SMALL);
    public static final Image DEFAULT_IMAGE = ResourceUtil.IMAGE_PLUGIN;

    static final String ID_CLASSNAME = "classname";
    static final String ID_URL = "url";
    static final String ID_NAME = "name";
    static final String ID_REQUIRED_KERNEL_VERSION = "required_kernel_version";

    public static class PluginIdent implements XMLPersistent
    {
        /**
         * Returns the index for the specified plugin ident in the specified list.<br>
         * Returns -1 if not found.
         */
        public static int getIndex(List<PluginIdent> list, PluginIdent ident)
        {
            final int size = list.size();

            for (int i = 0; i < size; i++)
                if (list.get(i).equals(ident))
                    return i;

            return -1;
        }

        /**
         * Returns the index for the specified plugin in the specified list.<br>
         * Returns -1 if not found.
         */
        public static int getIndex(List<? extends PluginIdent> list, String className)
        {
            final int size = list.size();

            for (int i = 0; i < size; i++)
                if (list.get(i).getClassName().equals(className))
                    return i;

            return -1;
        }

        static final String ID_VERSION = "version";

        private String className;
        private Version version;
        private Version requiredKernelVersion;

        /**
         * 
         */
        public PluginIdent()
        {
            super();

            // default
            className = "";
            version = new Version();
            requiredKernelVersion = new Version();
        }

        /**
         * 
         */
        @Override
        public boolean loadFromXML(Node node)
        {
            if (node == null)
                return false;

            className = XMLUtil.getElementValue(node, ID_CLASSNAME, "");
            version = new Version(XMLUtil.getElementValue(node, ID_VERSION, ""));
            requiredKernelVersion = new Version(XMLUtil.getElementValue(node, ID_REQUIRED_KERNEL_VERSION, ""));

            return true;
        }

        /**
         * 
         */
        @Override
        public boolean saveToXML(Node node)
        {
            if (node == null)
                return false;

            XMLUtil.setElementValue(node, ID_CLASSNAME, className);
            XMLUtil.setElementValue(node, ID_VERSION, version.toString());
            XMLUtil.setElementValue(node, ID_REQUIRED_KERNEL_VERSION, requiredKernelVersion.toString());

            return true;
        }

        public boolean isEmpty()
        {
            return StringUtil.isEmpty(className) && version.isEmpty() && requiredKernelVersion.isEmpty();
        }

        /**
         * @return the className
         */
        public String getClassName()
        {
            return className;
        }

        /**
         * @param className
         *        the className to set
         */
        public void setClassName(String className)
        {
            this.className = className;
        }

        /**
         * return the simple className
         */
        public String getSimpleClassName()
        {
            return ClassUtil.getSimpleClassName(className);
        }

        /**
         * return the package name
         */
        public String getPackageName()
        {
            return ClassUtil.getPackageName(className);
        }

        /**
         * return the minimum package name (remove "icy" or/and "plugin" header)<br>
         */
        public String getSimplePackageName()
        {
            String result = getPackageName();

            if (result.startsWith("icy."))
                result = result.substring(4);
            if (result.startsWith(PluginLoader.PLUGIN_PACKAGE))
                result = result.substring(PluginLoader.PLUGIN_PACKAGE.length() + 1);

            return result;
        }

        /**
         * return the author package name (first part of simple package name)
         */
        public String getAuthorPackageName()
        {
            final String result = getSimplePackageName();
            final int index = result.indexOf('.');

            if (index != -1)
                return result.substring(0, index);

            return result;
        }

        /**
         * @param version
         *        the version to set
         */
        public void setVersion(Version version)
        {
            this.version = version;
        }

        /**
         * @return the version
         */
        public Version getVersion()
        {
            return version;
        }

        /**
         * @return the requiredKernelVersion
         */
        public Version getRequiredKernelVersion()
        {
            return requiredKernelVersion;
        }

        /**
         * @param requiredKernelVersion
         *        the requiredKernelVersion to set
         */
        public void setRequiredKernelVersion(Version requiredKernelVersion)
        {
            this.requiredKernelVersion = requiredKernelVersion;
        }

        public boolean isOlderOrEqual(PluginIdent ident)
        {
            return className.equals(ident.getClassName()) && version.isOlderOrEqual(ident.getVersion());
        }

        public boolean isOlder(PluginIdent ident)
        {
            return className.equals(ident.getClassName()) && version.isOlder(ident.getVersion());
        }

        public boolean isNewerOrEqual(PluginIdent ident)
        {
            return className.equals(ident.getClassName()) && version.isNewerOrEqual(ident.getVersion());
        }

        public boolean isNewer(PluginIdent ident)
        {
            return className.equals(ident.getClassName()) && version.isNewer(ident.getVersion());
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof PluginIdent)
            {
                PluginIdent ident = (PluginIdent) obj;

                return ident.getClassName().equals(className) && ident.getVersion().equals(getVersion());
            }

            return super.equals(obj);
        }

        @Override
        public String toString()
        {
            return className + " " + version.toString();
        }
    }

    public static class PluginOnlineIdent extends PluginIdent
    {
        private String name;
        private String url;

        public PluginOnlineIdent()
        {
            super();

            name = "";
            url = "";
        }

        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return the url
         */
        public String getUrl()
        {
            return url;
        }

        @Override
        public boolean loadFromXML(Node node)
        {
            if (super.loadFromXML(node))
            {
                name = XMLUtil.getElementValue(node, PluginDescriptor.ID_NAME, "");
                url = XMLUtil.getElementValue(node, PluginDescriptor.ID_URL, "");
                return true;
            }

            return false;
        }

        @Override
        public boolean saveToXML(Node node)
        {
            if (super.saveToXML(node))
            {
                XMLUtil.setElementValue(node, PluginDescriptor.ID_NAME, name);
                XMLUtil.setElementValue(node, PluginDescriptor.ID_URL, name);
                return true;
            }

            return false;
        }
    }

    private static final String ID_JAR_URL = "jar_url";
    private static final String ID_IMAGE_URL = "image_url";
    private static final String ID_ICON_URL = "icon_url";
    private static final String ID_AUTHOR = "author";
    private static final String ID_CHANGELOG = "changelog";
    private static final String ID_WEB = "web";
    private static final String ID_EMAIL = "email";
    private static final String ID_DESCRIPTION = "description";
    private static final String ID_DEPENDENCIES = "dependencies";
    private static final String ID_DEPENDENCY = "dependency";

    private Class<? extends Plugin> pluginClass;

    private ImageIcon icon;
    private Image image;

    private String name;
    private PluginIdent ident;
    private String xmlUrl;
    private String jarUrl;
    String imageUrl;
    String iconUrl;
    private String author;
    private String web;
    private String email;
    private String desc;
    private String changesLog;

    private boolean enabled;
    private boolean descriptorLoaded;
    private boolean imagesLoaded;
    // boolean checkingForUpdate;
    // boolean updateChecked;
    // PluginDescriptor onlineDescriptor;

    // private final List<String> publicClasseNames;
    private final List<PluginIdent> required;

    // only for online descriptor
    private RepositoryInfo repository;

    // private static final DateFormat dateFormatter = DateFormat.getDateInstance();
    // private static final GregorianCalendar calendar = (GregorianCalendar)
    // GregorianCalendar.getInstance();

    // /**
    // * Get online plugin of specified PluginIdent<br>
    // * Take care of "allow beta" global flag<br>
    // * This method can take sometime !<br>
    // */
    // public static PluginDescriptor getOnlinePlugin(PluginIdent ident, boolean loadImage)
    // {
    // PluginDescriptor betaDescriptor = null;
    // PluginDescriptor stableDescriptor = null;
    //
    // try
    // {
    // // get beta online plugin descriptor if allowed
    // if (PluginPreferences.getAllowBeta())
    // betaDescriptor = new PluginDescriptor(ident.getUrlBeta(), loadImage);
    // }
    // catch (Exception e)
    // {
    // betaDescriptor = null;
    // }
    //
    // try
    // {
    // // get stable online plugin descriptor
    // stableDescriptor = new PluginDescriptor(ident.getUrlStable(), loadImage);
    // }
    // catch (Exception e)
    // {
    // stableDescriptor = null;
    // }
    //
    // if ((betaDescriptor != null) && ((stableDescriptor == null) ||
    // betaDescriptor.isNewerOrEqual(stableDescriptor)))
    // return betaDescriptor;
    //
    // return stableDescriptor;
    // }

    /**
     * Returns the index for the specified plugin in the specified list.<br>
     * Returns -1 if not found.
     */
    public static int getIndex(List<PluginDescriptor> list, PluginDescriptor plugin)
    {
        return getIndex(list, plugin.getIdent());
    }

    /**
     * Returns the index for the specified plugin in the specified list.<br>
     * Returns -1 if not found.
     */
    public static int getIndex(List<PluginDescriptor> list, PluginIdent ident)
    {
        final int size = list.size();

        for (int i = 0; i < size; i++)
            if (list.get(i).getIdent().equals(ident))
                return i;

        return -1;
    }

    /**
     * Returns the index for the specified plugin in the specified list.<br>
     * Returns -1 if not found.
     */
    public static int getIndex(List<PluginDescriptor> list, String className)
    {
        final int size = list.size();

        for (int i = 0; i < size; i++)
            if (list.get(i).getClassName().equals(className))
                return i;

        return -1;
    }

    /**
     * Returns true if the specified plugin is present in the specified list.
     */
    public static boolean existInList(List<PluginDescriptor> list, PluginDescriptor plugin)
    {
        return existInList(list, plugin.getIdent());
    }

    /**
     * Returns true if the specified plugin is present in the specified list.
     */
    public static boolean existInList(List<PluginDescriptor> list, PluginIdent ident)
    {
        return getIndex(list, ident) != -1;
    }

    /**
     * Returns true if the specified plugin is present in the specified list.
     */
    public static boolean existInList(List<PluginDescriptor> list, String className)
    {
        return getIndex(list, className) != -1;
    }

    public static void addToList(List<PluginDescriptor> list, PluginDescriptor plugin, int position)
    {
        if ((plugin != null) && !existInList(list, plugin))
            list.add(position, plugin);
    }

    public static void addToList(List<PluginDescriptor> list, PluginDescriptor plugin)
    {
        if ((plugin != null) && !existInList(list, plugin))
            list.add(plugin);
    }

    public static boolean removeFromList(List<PluginDescriptor> list, String className)
    {
        for (int i = list.size() - 1; i >= 0; i--)
        {
            final PluginDescriptor p = list.get(i);

            if (p.getClassName().equals(className))
            {
                list.remove(i);
                return true;
            }
        }

        return false;
    }

    // public static String getPluginTypeString(int type)
    // {
    // if ((type >= 0) && (type < pluginTypeString.length))
    // return pluginTypeString[type];
    //
    // return "";
    // }

    public static List<PluginDescriptor> getPlugins(List<PluginDescriptor> list, String className)
    {
        final List<PluginDescriptor> result = new ArrayList<PluginDescriptor>();

        for (PluginDescriptor plugin : list)
            if (plugin.getClassName().equals(className))
                result.add(plugin);

        return result;
    }

    public static PluginDescriptor getPlugin(List<PluginDescriptor> list, String className)
    {
        for (PluginDescriptor plugin : list)
            if (plugin.getClassName().equals(className))
                return plugin;

        return null;
    }

    public static PluginDescriptor getPlugin(List<PluginDescriptor> list, PluginIdent ident, boolean acceptNewer)
    {
        if (acceptNewer)
        {
            for (PluginDescriptor plugin : list)
                if (plugin.getIdent().isNewerOrEqual(ident))
                    return plugin;
        }
        else
        {
            for (PluginDescriptor plugin : list)
                if (plugin.getIdent().equals(ident))
                    return plugin;
        }

        return null;
    }

    public PluginDescriptor()
    {
        super();

        pluginClass = null;

        icon = DEFAULT_ICON;
        image = DEFAULT_IMAGE;

        xmlUrl = "";
        name = "";
        ident = new PluginIdent();
        jarUrl = "";
        imageUrl = "";
        iconUrl = "";
        author = "";
        web = "";
        email = "";
        desc = "";
        changesLog = "";

        required = new ArrayList<PluginIdent>();
        repository = null;

        // default
        enabled = true;
        descriptorLoaded = true;
        imagesLoaded = true;
    }

    /**
     * Create from class, used for local plugin.
     */
    public PluginDescriptor(Class<? extends Plugin> clazz)
    {
        this();

        this.pluginClass = clazz;

        final String baseResourceName = clazz.getSimpleName();
        final String baseLocalName = ClassUtil.getPathFromQualifiedName(clazz.getName());
        URL url;

        // load icon
        url = clazz.getResource(baseResourceName + getIconExtension());
        if (url == null)
            url = URLUtil.getURL(baseLocalName + getIconExtension());
        loadIcon(url);

        // load image
        url = clazz.getResource(baseResourceName + getImageExtension());
        if (url == null)
            url = URLUtil.getURL(baseLocalName + getImageExtension());
        loadImage(url);

        // load xml
        url = clazz.getResource(baseResourceName + getXMLExtension());
        if (url == null)
            url = URLUtil.getURL(baseLocalName + getXMLExtension());

        // can't load XML from specified URL ?
        if (!loadFromXML(url))
        {
            // xml is absent or incorrect, we set default informations
            ident.setClassName(pluginClass.getName());
            name = pluginClass.getSimpleName();
            desc = name + " plugin";
        }

        // mark descriptor and images as loaded
        descriptorLoaded = true;
        imagesLoaded = true;
    }

    /**
     * Create from plugin online identifier, used for online plugin only.
     * 
     * @throws IllegalArgumentException
     */
    public PluginDescriptor(PluginOnlineIdent ident, RepositoryInfo repos) throws IllegalArgumentException
    {
        this();

        this.ident.setClassName(ident.getClassName());
        this.ident.setVersion(ident.getVersion());
        this.ident.setRequiredKernelVersion(ident.getRequiredKernelVersion());
        this.xmlUrl = ident.getUrl();
        this.name = ident.getName();
        this.repository = repos;

        // mark descriptor and images as not yet loaded
        descriptorLoaded = false;
        imagesLoaded = false;
    }

    /**
     * @deprecated Use {@link #loadDescriptor()} or {@link #loadAll()} instead
     */
    @Deprecated
    public boolean load(boolean loadImages)
    {
        if (loadDescriptor())
            if (loadImages)
                return loadImages();

        return false;
    }

    /**
     * Load descriptor informations (xmlUrl field should be correctly filled)
     */
    public boolean loadDescriptor()
    {
        return loadDescriptor(false);
    }

    /**
     * Load descriptor informations (xmlUrl field should be correctly filled)
     */
    public boolean loadDescriptor(boolean reload)
    {
        // already loaded ?
        if (descriptorLoaded && !reload)
            return true;

        // retrieve document
        final Document document = XMLUtil.loadDocument(xmlUrl, repository.getAuthenticationInfo(), true);

        if (document != null)
        {
            // load xml
            if (!loadFromXML(document.getDocumentElement()))
            {
                System.err.println("Can't find valid XML file from '" + xmlUrl + "' for plugin class '"
                        + ident.getClassName() + "'");
                return false;
            }

            descriptorLoaded = true;

            return true;
        }

        // display error only for first load
        if (!reload)
            System.err.println("Can't load XML file from '" + xmlUrl + "' for plugin class '" + ident.getClassName()
                    + "'");

        return false;
    }

    /**
     * Load icon and image (both icon and image url fields should be correctly filled)
     */
    public boolean loadImages()
    {
        // can't load images if descriptor is not yet loaded
        if (!descriptorLoaded)
            return false;
        // already loaded ?
        if (imagesLoaded)
            return true;

        // load icon
        loadIcon(URLUtil.getURL(iconUrl));
        // load image
        loadImage(URLUtil.getURL(imageUrl));

        imagesLoaded = true;

        return true;
    }

    /**
     * Load descriptor and images if not already done
     */
    public boolean loadAll()
    {
        if (loadDescriptor())
            return loadImages();

        return false;
    }

    /**
     * Check if the given class is a subclass of the class contained in this descriptor.
     * 
     * @param containedClass
     */
    public boolean isInstanceOf(Class<?> containedClass)
    {
        return ClassUtil.isSubClass(pluginClass, containedClass);
    }

    /**
     * Return true if the plugin class is abstract
     */
    public boolean isAbstract()
    {
        return ClassUtil.isAbstract(pluginClass);
    }

    /**
     * Return true if the plugin class is private
     */
    public boolean isPrivate()
    {
        return ClassUtil.isPrivate(pluginClass);
    }

    /**
     * Return true if the plugin class is an interface
     */
    public boolean isInterface()
    {
        return pluginClass.isInterface();
    }

    /**
     * return true if the plugin has an action which can be started from menu
     */
    public boolean isActionable()
    {
        return isClassLoaded() && !isPrivate() && !isAbstract() && !isInterface()
                && isInstanceOf(PluginImageAnalysis.class);
    }

    /**
     * Return true if the plugin is in beta state
     */
    public boolean isBeta()
    {
        return getVersion().isBeta();
    }

    void loadIcon(URL url)
    {
        // load icon
        if (url != null)
            icon = ResourceUtil.getImageIcon(
                    ImageUtil.load(NetworkUtil.getInputStream(url,
                            (repository != null) ? repository.getAuthenticationInfo() : null, true, false), false),
                    ICON_SIZE);
        // get default icon
        if (icon == null)
            icon = DEFAULT_ICON;
    }

    void loadImage(URL url)
    {
        // load image
        if (url != null)
            image = ImageUtil.scale(
                    ImageUtil.load(NetworkUtil.getInputStream(url,
                            (repository != null) ? repository.getAuthenticationInfo() : null, true, false), false),
                    IMAGE_SIZE, IMAGE_SIZE);
        // get default image
        if (image == null)
            image = DEFAULT_IMAGE;
    }

    // public void save()
    // {
    // // save icon
    // if (icon != null)
    // ImageUtil.saveImage(ImageUtil.toRenderedImage(icon.getImage()), "png", getIconFilename());
    // // save image
    // if (image != null)
    // ImageUtil.saveImage(ImageUtil.toRenderedImage(image), "png", getImageFilename());
    // // save xml
    // saveToXML();
    // }

    public boolean loadFromXML(String path)
    {
        return XMLPersistentHelper.loadFromXML(this, path);
    }

    public boolean loadFromXML(URL xmlUrl)
    {
        return XMLPersistentHelper.loadFromXML(this, xmlUrl);
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        // get the plugin ident
        ident.loadFromXML(node);

        name = XMLUtil.getElementValue(node, ID_NAME, "");
        xmlUrl = XMLUtil.getElementValue(node, ID_URL, "");
        jarUrl = XMLUtil.getElementValue(node, ID_JAR_URL, "");
        imageUrl = XMLUtil.getElementValue(node, ID_IMAGE_URL, "");
        iconUrl = XMLUtil.getElementValue(node, ID_ICON_URL, "");
        author = XMLUtil.getElementValue(node, ID_AUTHOR, "");
        web = XMLUtil.getElementValue(node, ID_WEB, "");
        email = XMLUtil.getElementValue(node, ID_EMAIL, "");
        desc = XMLUtil.getElementValue(node, ID_DESCRIPTION, "");
        changesLog = XMLUtil.getElementValue(node, ID_CHANGELOG, "");

        final Node nodeDependances = XMLUtil.getElement(node, ID_DEPENDENCIES);
        if (nodeDependances != null)
        {
            final ArrayList<Node> nodesDependances = XMLUtil.getChildren(nodeDependances, ID_DEPENDENCY);

            for (Node n : nodesDependances)
            {
                final PluginIdent ident = new PluginIdent();
                // required don't need URL information as we now search from classname
                ident.loadFromXML(n);
                if (!ident.isEmpty())
                    required.add(ident);
            }
        }

        return true;
    }

    public boolean saveToXML()
    {
        return XMLPersistentHelper.saveToXML(this, getXMLFilename());
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (node == null)
            return false;

        ident.saveToXML(node);

        XMLUtil.setElementValue(node, ID_NAME, name);
        XMLUtil.setElementValue(node, ID_URL, xmlUrl);
        XMLUtil.setElementValue(node, ID_JAR_URL, jarUrl);
        XMLUtil.setElementValue(node, ID_IMAGE_URL, imageUrl);
        XMLUtil.setElementValue(node, ID_ICON_URL, iconUrl);
        XMLUtil.setElementValue(node, ID_AUTHOR, author);
        XMLUtil.setElementValue(node, ID_WEB, web);
        XMLUtil.setElementValue(node, ID_EMAIL, email);
        XMLUtil.setElementValue(node, ID_DESCRIPTION, desc);
        XMLUtil.setElementValue(node, ID_CHANGELOG, changesLog);

        // synchronized (dateFormatter)
        // {
        // XMLUtil.addChildElement(root, ID_INSTALL_DATE, dateFormatter.format(installed));
        // XMLUtil.addChildElement(root, ID_LASTUSE_DATE, dateFormatter.format(lastUse));
        // }

        // final Element publicClasses = XMLUtil.setElement(node, ID_PUBLIC_CLASSES);
        // if (publicClasses != null)
        // {
        // XMLUtil.removeAllChilds(publicClasses);
        // for (String className : publicClasseNames)
        // XMLUtil.addValue(XMLUtil.addElement(publicClasses, ID_CLASSNAME), className);
        // }

        final Element dependances = XMLUtil.setElement(node, ID_DEPENDENCIES);
        if (dependances != null)
        {
            XMLUtil.removeAllChildren(dependances);
            for (PluginIdent dep : required)
                dep.saveToXML(XMLUtil.addElement(dependances, ID_DEPENDENCY));
        }

        return true;
    }

    public boolean isClassLoaded()
    {
        return pluginClass != null;
    }

    /**
     * Returns the plugin class name.<br>
     * Ex: "plugins.tutorial.Example1"
     */
    public String getClassName()
    {
        return ident.getClassName();
    }

    public String getSimpleClassName()
    {
        return ident.getSimpleClassName();
    }

    public String getPackageName()
    {
        return ident.getPackageName();
    }

    /**
     * return the minimum package name (remove "icy" or/and "plugin" header)<br>
     */
    public String getSimplePackageName()
    {
        return ident.getSimplePackageName();
    }

    /**
     * return the author package name (first part of simple package name)
     */
    public String getAuthorPackageName()
    {
        return ident.getAuthorPackageName();
    }

    /**
     * @return the pluginClass
     */
    public String getClassAsString()
    {
        if (pluginClass != null)
            return pluginClass.toString();

        return "";
    }

    /**
     * @return the pluginClass
     */
    public Class<? extends Plugin> getPluginClass()
    {
        return pluginClass;
    }

    /**
     * return associated filename
     */
    public String getFilename()
    {
        return ClassUtil.getPathFromQualifiedName(getClassName());
    }

    /**
     * return xml extension
     */
    public String getXMLExtension()
    {
        return ".xml";
    }

    /**
     * return xml filename
     */
    public String getXMLFilename()
    {
        return getFilename() + getXMLExtension();
    }

    /**
     * return icon extension
     */
    public String getIconExtension()
    {
        return "_icon.png";
    }

    /**
     * return icon filename
     */
    public String getIconFilename()
    {
        return getFilename() + getIconExtension();
    }

    /**
     * return image extension
     */
    public String getImageExtension()
    {
        return ".png";
    }

    /**
     * return image filename
     */
    public String getImageFilename()
    {
        return getFilename() + getImageExtension();
    }

    /**
     * return jar extension
     */
    public String getJarExtension()
    {
        return ".jar";
    }

    /**
     * return jar filename
     */
    public String getJarFilename()
    {
        return getFilename() + getJarExtension();
    }

    /**
     * @return the icon
     */
    public ImageIcon getIcon()
    {
        return icon;
    }

    /**
     * @return the icon as image
     */
    public Image getIconAsImage()
    {
        if (icon != null)
            return icon.getImage();

        return null;
    }

    /**
     * @return the image
     */
    public Image getImage()
    {
        return image;
    }

    // /**
    // * @return the lastUse
    // */
    // public Date getLastUse()
    // {
    // return lastUse;
    // }
    //
    // /**
    // * @param lastUse
    // * the lastUse to set
    // */
    // public void setLastUse(Date lastUse)
    // {
    // this.lastUse = lastUse;
    // }

    /**
     * @return the ident
     */
    public PluginIdent getIdent()
    {
        return ident;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the version
     */
    public Version getVersion()
    {
        if (ident != null)
            return ident.getVersion();

        return new Version();
    }

    // /**
    // * @return the url for current version
    // */
    // public String getUrlCurrent()
    // {
    // if (ident != null)
    // {
    // final Version ver = ident.getVersion();
    //
    // if (ver.isBeta())
    // return ident.getUrlBeta();
    //
    // return ident.getUrlStable();
    // }
    //
    // return "";
    // }

    /**
     * @return the url
     */
    public String getUrl()
    {
        // url is default XML url
        return getXmlUrl();
    }

    /**
     * @return the url for xml file
     */
    public String getXmlUrl()
    {
        return xmlUrl;
    }

    /**
     * @return the desc
     * @deprecated use {@link #getDescription()} instead
     */
    @Deprecated
    public String getDesc()
    {
        return getDescription();
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return desc;
    }

    /**
     * @return the jarUrl
     */
    public String getJarUrl()
    {
        return jarUrl;
    }

    /**
     * @param jarUrl
     *        the jarUrl to set
     */
    public void setJarUrl(String jarUrl)
    {
        this.jarUrl = jarUrl;
    }

    /**
     * @return the imageUrl
     */
    public String getImageUrl()
    {
        return imageUrl;
    }

    /**
     * @param imageUrl
     *        the imageUrl to set
     */
    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    /**
     * @return the iconUrl
     */
    public String getIconUrl()
    {
        return iconUrl;
    }

    /**
     * @param iconUrl
     *        the iconUrl to set
     */
    public void setIconUrl(String iconUrl)
    {
        this.iconUrl = iconUrl;
    }

    /**
     * @return the author
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * @return the web
     */
    public String getWeb()
    {
        return web;
    }

    /**
     * @return the email
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * @return the changesLog
     */
    public String getChangesLog()
    {
        return changesLog;
    }

    /**
     * @return the requiredKernelVersion
     */
    public Version getRequiredKernelVersion()
    {
        return ident.getRequiredKernelVersion();
    }

    /**
     * Returns true if descriptor is loaded.
     */
    public boolean isDescriptorLoaded()
    {
        return descriptorLoaded;
    }

    /**
     * @deprecated Use {@link #isDescriptorLoaded()} instead
     */
    @Deprecated
    public boolean isLoaded()
    {
        return descriptorLoaded;
    }

    /**
     * Returns true if image and icon are loaded.
     */
    public boolean isImagesLoaded()
    {
        return descriptorLoaded;
    }

    /**
     * Returns true if both descriptor and images are loaded.
     */
    public boolean isAllLoaded()
    {
        return descriptorLoaded && imagesLoaded;
    }

    /**
     * @return the required
     */
    public List<PluginIdent> getRequired()
    {
        return new ArrayList<PluginIdent>(required);
    }

    public RepositoryInfo getRepository()
    {
        return repository;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * @param enabled
     *        the enabled to set
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Return true if plugin is installed (corresponding JAR file exits)
     */
    public boolean isInstalled()
    {
        return FileUtil.exists(getJarFilename());
    }

    // /**
    // * @return the hasUpdate
    // */
    // public boolean getHasUpdate()
    // {
    // // true if online version > local version
    // return (onlineDescriptor != null) && onlineDescriptor.getVersion().isGreater(getVersion());
    // }
    //
    // /**
    // * @return the checkingForUpdate
    // */
    // public boolean isCheckingForUpdate()
    // {
    // return checkingForUpdate;
    // }
    //
    // /**
    // * @return the onlineDescriptor
    // */
    // public PluginDescriptor getOnlineDescriptor()
    // {
    // return onlineDescriptor;
    // }

    // /**
    // * @return the updateChecked
    // */
    // public boolean isUpdateChecked()
    // {
    // return updateChecked;
    // }
    //
    // /**
    // * check for update (asynchronous as it can take sometime)
    // */
    // public void checkForUpdate()
    // {
    // if (updateChecked)
    // return;
    //
    // checkingForUpdate = true;
    //
    // ThreadUtil.bgRunWait(new Runnable()
    // {
    // @Override
    // public void run()
    // {
    // try
    // {
    // onlineDescriptor = getOnlinePlugin(getIdent(), false);
    // }
    // catch (Exception E)
    // {
    // onlineDescriptor = null;
    // }
    // finally
    // {
    // checkingForUpdate = false;
    // updateChecked = true;
    // }
    // }
    // });
    // }

    /**
     * @param name
     *        the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Return true if specified plugin is required by current plugin
     */
    public boolean requires(PluginDescriptor plugin)
    {
        final PluginIdent curIdent = plugin.getIdent();

        for (PluginIdent ident : required)
            if (ident.isOlderOrEqual(curIdent))
                return true;

        return false;
    }

    public boolean isOlderOrEqual(PluginDescriptor plugin)
    {
        return ident.isOlderOrEqual(plugin.getIdent());
    }

    public boolean isOlder(PluginDescriptor plugin)
    {
        return ident.isOlder(plugin.getIdent());
    }

    public boolean isNewerOrEqual(PluginDescriptor plugin)
    {
        return ident.isNewerOrEqual(plugin.getIdent());
    }

    public boolean isNewer(PluginDescriptor plugin)
    {
        return ident.isNewer(plugin.getIdent());
    }

    @Override
    public String toString()
    {
        return getName() + " " + getVersion().toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof PluginDescriptor)
        {
            final PluginDescriptor plug = (PluginDescriptor) obj;

            return getClassName().equals(plug.getClassName()) && getVersion().equals(plug.getVersion());
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return getClassName().hashCode() ^ getVersion().hashCode();
    }
}
