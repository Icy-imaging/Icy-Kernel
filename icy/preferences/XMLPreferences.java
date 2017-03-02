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
package icy.preferences;

import icy.util.ClassUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class XMLPreferences
{
    public static class XMLPreferencesRoot
    {
        private final String filename;
        private Document doc;

        // cached
        Element element;
        XMLPreferences preferences;

        public XMLPreferencesRoot(String filename)
        {
            this.filename = filename;

            load();
        }

        /**
         * Load preferences from file
         */
        public void load()
        {
            load(filename);
        }

        /**
         * Load preferences from file
         */
        public void load(String filename)
        {
            try
            {
                // get document
                doc = XMLUtil.loadDocument(new File(filename));
            }
            catch (Throwable t)
            {
                System.err.println("Error: " + filename + " preferences file is corrupted, cannot recover settings.");
                // corrupted XML file
                doc = null;
            }

            // create it if not existing
            if (doc == null)
                doc = XMLUtil.createDocument(false);

            // create root element
            element = XMLUtil.createRootElement(doc);
            // create our root XMLPreference object
            preferences = new XMLPreferences(this, element);
            preferences.clean();
        }

        /**
         * Save preferences to file
         */
        public void save()
        {
            save(filename);
        }

        /**
         * Save preferences to file
         */
        public void save(String filename)
        {
            if (doc != null)
                XMLUtil.saveDocument(doc, new File(filename));
        }

        /**
         * @return the element
         */
        public Element getElement()
        {
            return element;
        }

        /**
         * @return the preferences
         */
        public XMLPreferences getPreferences()
        {
            return preferences;
        }
    }

    private final static String TYPE_SECTION = "section";
    private final static String TYPE_KEY = "key";

    private final XMLPreferencesRoot root;
    private final Element currentElement;

    /**
     * Use:<br>
     * <code>new XMLPreferencesRoot(filename).getPreferences()</code><br>
     * to load preferences from file.
     */
    XMLPreferences(XMLPreferencesRoot root, Element element)
    {
        super();

        this.root = root;
        currentElement = element;
    }

    public String absolutePath()
    {
        String result = "/" + name();

        synchronized (root)
        {
            Element parent = XMLUtil.getParentElement(currentElement);
            while ((parent != null) && (parent != root.element))
            {
                result = "/" + XMLUtil.getGenericElementName(parent) + result;
                parent = XMLUtil.getParentElement(parent);
            }
        }

        return result;
    }

    public String name()
    {
        synchronized (root)
        {
            return XMLUtil.getGenericElementName(currentElement);
        }
    }

    public XMLPreferences getParent()
    {
        final Element parent;

        synchronized (root)
        {
            parent = XMLUtil.getParentElement(currentElement);
        }

        if (parent != null)
            return new XMLPreferences(root, parent);

        return null;
    }

    public ArrayList<XMLPreferences> getChildren()
    {
        final ArrayList<XMLPreferences> result = new ArrayList<XMLPreferences>();
        final List<Element> elements;

        synchronized (root)
        {
            elements = XMLUtil.getGenericElements(currentElement, TYPE_SECTION);
        }

        for (Element element : elements)
            result.add(new XMLPreferences(root, element));

        return result;
    }

    public ArrayList<String> childrenNames()
    {
        final ArrayList<String> result = new ArrayList<String>();

        synchronized (root)
        {
            for (Element element : XMLUtil.getGenericElements(currentElement, TYPE_SECTION))
                result.add(XMLUtil.getGenericElementName(element));
        }

        return result;
    }

    private Element getSection(String name)
    {
        if (StringUtil.isEmpty(name))
            return currentElement;

        Element element;

        // absolute path
        if (name.startsWith("/"))
            element = root.element;
        else
        {
            // we test first current node is still existing
            if (!exists())
                return null;

            element = currentElement;
        }

        synchronized (root)
        {
            for (String subName : name.split("/"))
                if (!subName.isEmpty())
                    element = XMLUtil.getGenericElement(element, TYPE_SECTION, subName);
        }

        return element;
    }

    private Element setSection(String name)
    {
        if (StringUtil.isEmpty(name))
            return currentElement;

        Element element;

        // absolute path
        if (name.startsWith("/"))
            element = root.element;
        else
        {
            // we test first current node is still existing
            if (!exists())
                return null;

            element = currentElement;
        }

        synchronized (root)
        {
            for (String subName : name.split("/"))
                if (!subName.isEmpty())
                    element = XMLUtil.setGenericElement(element, TYPE_SECTION, subName);
        }

        return element;
    }

    /**
     * Return XMLPreferences of specified node.<br>
     */
    public XMLPreferences node(String name)
    {
        final Element element = setSection(name);

        if (element != null)
            return new XMLPreferences(root, element);

        return null;
    }

    /**
     * Return XMLPreferences of specified node using class name of specified object.<br>
     * <code>nodeForClass(object) == node(object.getClass().getName())</code><br>
     * Ex : <code>nodeForClass("text") == node("java.lang.String")</code>
     */
    public XMLPreferences nodeForClass(Object object)
    {
        if (object != null)
            return node(ClassUtil.getPathFromQualifiedName(object.getClass().getName()));

        return null;
    }

    /**
     * Return the {@link XMLPreferences} node as an XML node.
     */
    public Element getXMLNode()
    {
        return currentElement;
    }

    /**
     * Return true if current node is existing
     */
    public boolean exists()
    {
        // root element, always exists
        if (currentElement == root.element)
            return true;

        synchronized (root)
        {
            // try to reach root from current element
            Element parent = XMLUtil.getParentElement(currentElement);
            while (parent != null)
            {
                // we reached root so the element still exist
                if (parent == root.element)
                    return true;

                parent = XMLUtil.getParentElement(parent);
            }
        }

        // can't reach root, element is no more existing
        return false;
    }

    /**
     * Return true if specified node exists
     */
    public boolean nodeExists(String name)
    {
        return getSection(name) != null;
    }

    /**
     * Return true if node for specified object exists.<br>
     * <code>nodeForClassExists(object) == nodeExists(object.getClass().getName())</code><br>
     * Ex : <code>nodeForClassExists("text") == nodeExists("java.lang.String")</code>
     */
    public boolean nodeForClassExists(Object object)
    {
        if (object != null)
            return nodeExists(ClassUtil.getPathFromQualifiedName(object.getClass().getName()));

        return false;
    }

    public ArrayList<String> keys()
    {
        final ArrayList<String> result = new ArrayList<String>();

        synchronized (root)
        {
            for (Element element : XMLUtil.getGenericElements(currentElement, TYPE_KEY))
                result.add(XMLUtil.getGenericElementName(element));
        }

        return result;
    }

    /**
     * Remove all non element nodes
     */
    public void clean()
    {
        synchronized (root)
        {
            final List<Node> nodes = XMLUtil.getChildren(currentElement);

            for (Node node : nodes)
            {
                final String nodeName = node.getNodeName();

                if (!(nodeName.equals(TYPE_KEY) || nodeName.equals(TYPE_SECTION)))
                    XMLUtil.removeNode(currentElement, node);
            }
        }
    }

    /**
     * Remove all direct children of this node
     */
    public void clear()
    {
        synchronized (root)
        {
            XMLUtil.removeChildren(currentElement, TYPE_KEY);
        }
    }

    /**
     * Remove specified element
     */
    private void remove(Element element)
    {
        if (element != null)
        {
            synchronized (root)
            {
                final Element parent = XMLUtil.getParentElement(element);

                if (parent != null)
                    XMLUtil.removeNode(parent, element);
            }
        }
    }

    /**
     * Remove current section
     */
    public void remove()
    {
        remove(currentElement);
    }

    /**
     * Remove specified section
     */
    public void remove(String name)
    {
        remove(getSection(name));
    }

    /**
     * Remove all sections
     */
    public void removeChildren()
    {
        synchronized (root)
        {
            XMLUtil.removeChildren(currentElement, TYPE_SECTION);
        }
    }

    public String get(String key, String def)
    {
        synchronized (root)
        {
            return XMLUtil.getGenericElementValue(currentElement, TYPE_KEY, key, def);
        }
    }

    public boolean getBoolean(String key, boolean def)
    {
        synchronized (root)
        {
            return XMLUtil.getGenericElementBooleanValue(currentElement, TYPE_KEY, key, def);
        }
    }

    public byte[] getBytes(String key, byte[] def)
    {
        synchronized (root)
        {
            return XMLUtil.getGenericElementBytesValue(currentElement, TYPE_KEY, key, def);
        }
    }

    public double getDouble(String key, double def)
    {
        synchronized (root)
        {
            return XMLUtil.getGenericElementDoubleValue(currentElement, TYPE_KEY, key, def);
        }
    }

    public float getFloat(String key, float def)
    {
        synchronized (root)
        {
            return XMLUtil.getGenericElementFloatValue(currentElement, TYPE_KEY, key, def);
        }
    }

    public int getInt(String key, int def)
    {
        synchronized (root)
        {
            return XMLUtil.getGenericElementIntValue(currentElement, TYPE_KEY, key, def);
        }
    }

    public long getLong(String key, long def)
    {
        synchronized (root)
        {
            return XMLUtil.getGenericElementLongValue(currentElement, TYPE_KEY, key, def);
        }
    }

    public void put(String key, String value)
    {
        synchronized (root)
        {
            XMLUtil.setGenericElementValue(currentElement, TYPE_KEY, key, value);
        }
    }

    public void putBoolean(String key, boolean value)
    {
        synchronized (root)
        {
            XMLUtil.setGenericElementBooleanValue(currentElement, TYPE_KEY, key, value);
        }
    }

    public void putBytes(String key, byte[] value)
    {
        synchronized (root)
        {
            XMLUtil.setGenericElementBytesValue(currentElement, TYPE_KEY, key, value.clone());
        }
    }

    public void putDouble(String key, double value)
    {
        synchronized (root)
        {
            XMLUtil.setGenericElementDoubleValue(currentElement, TYPE_KEY, key, value);
        }
    }

    public void putFloat(String key, float value)
    {
        synchronized (root)
        {
            XMLUtil.setGenericElementFloatValue(currentElement, TYPE_KEY, key, value);
        }
    }

    public void putInt(String key, int value)
    {
        synchronized (root)
        {
            XMLUtil.setGenericElementIntValue(currentElement, TYPE_KEY, key, value);
        }
    }

    public void putLong(String key, long value)
    {
        synchronized (root)
        {
            XMLUtil.setGenericElementLongValue(currentElement, TYPE_KEY, key, value);
        }
    }
}
