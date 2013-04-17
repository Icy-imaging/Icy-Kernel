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
package icy.preferences;

import icy.util.ClassUtil;
import icy.util.XMLUtil;

import java.io.File;
import java.util.ArrayList;

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
            // get document
            doc = XMLUtil.loadDocument(new File(filename));
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

    XMLPreferences(XMLPreferencesRoot root, Element element)
    {
        super();

        this.root = root;
        currentElement = element;
    }

    public String absolutePath()
    {
        String result = "/" + name();

        Element parent = XMLUtil.getParentElement(currentElement);
        while ((parent != null) && (parent != root.element))
        {
            result = "/" + XMLUtil.getGenericElementName(parent) + result;
            parent = XMLUtil.getParentElement(parent);
        }

        return result;
    }

    public String name()
    {
        return XMLUtil.getGenericElementName(currentElement);
    }

    public XMLPreferences getParent()
    {
        final Element parent = XMLUtil.getParentElement(currentElement);

        if (parent != null)
            return new XMLPreferences(root, parent);

        return null;
    }

    public ArrayList<XMLPreferences> getChildren()
    {
        final ArrayList<XMLPreferences> result = new ArrayList<XMLPreferences>();

        for (Element element : XMLUtil.getGenericElements(currentElement, TYPE_SECTION))
            result.add(new XMLPreferences(root, element));

        return result;
    }

    public ArrayList<String> childrenNames()
    {
        final ArrayList<String> result = new ArrayList<String>();

        for (Element element : XMLUtil.getGenericElements(currentElement, TYPE_SECTION))
            result.add(XMLUtil.getGenericElementName(element));

        return result;
    }

    private Element getSection(String name)
    {
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

        for (String subName : name.split("/"))
            if (!subName.isEmpty())
                element = XMLUtil.getGenericElement(element, TYPE_SECTION, subName);

        return element;
    }

    private Element setSection(String name)
    {
        if (name == null)
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

        for (String subName : name.split("/"))
            if (!subName.isEmpty())
                element = XMLUtil.setGenericElement(element, TYPE_SECTION, subName);

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

        // try to reach root from current element
        Element parent = XMLUtil.getParentElement(currentElement);
        while (parent != null)
        {
            // we reached root so the element still exist
            if (parent == root.element)
                return true;

            parent = XMLUtil.getParentElement(parent);
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

        for (Element element : XMLUtil.getGenericElements(currentElement, TYPE_KEY))
            result.add(XMLUtil.getGenericElementName(element));

        return result;
    }

    /**
     * Remove all non element nodes
     */
    public void clean()
    {
        final ArrayList<Node> nodes = XMLUtil.getChildren(currentElement);

        for (Node node : nodes)
        {
            final String nodeName = node.getNodeName();

            if (!(nodeName.equals(TYPE_KEY) || nodeName.equals(TYPE_SECTION)))
                XMLUtil.removeNode(currentElement, node);
        }
    }

    /**
     * Remove all direct children of this node
     */
    public void clear()
    {
        XMLUtil.removeChildren(currentElement, TYPE_KEY);
    }

    /**
     * Remove specified element
     */
    private void remove(Element element)
    {
        if (element != null)
        {
            final Element parent = XMLUtil.getParentElement(element);

            if (parent != null)
                XMLUtil.removeNode(parent, element);
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
        XMLUtil.removeChildren(currentElement, TYPE_SECTION);
    }

    public String get(String key, String def)
    {
        return XMLUtil.getGenericElementValue(currentElement, TYPE_KEY, key, def);
    }

    public boolean getBoolean(String key, boolean def)
    {
        return XMLUtil.getGenericElementBooleanValue(currentElement, TYPE_KEY, key, def);
    }

    public byte[] getBytes(String key, byte[] def)
    {
        return XMLUtil.getGenericElementBytesValue(currentElement, TYPE_KEY, key, def);
    }

    public double getDouble(String key, double def)
    {
        return XMLUtil.getGenericElementDoubleValue(currentElement, TYPE_KEY, key, def);
    }

    public float getFloat(String key, float def)
    {
        return XMLUtil.getGenericElementFloatValue(currentElement, TYPE_KEY, key, def);
    }

    public int getInt(String key, int def)
    {
        return XMLUtil.getGenericElementIntValue(currentElement, TYPE_KEY, key, def);
    }

    public long getLong(String key, long def)
    {
        return XMLUtil.getGenericElementLongValue(currentElement, TYPE_KEY, key, def);
    }

    public void put(String key, String value)
    {
        XMLUtil.setGenericElementValue(currentElement, TYPE_KEY, key, value);
    }

    public void putBoolean(String key, boolean value)
    {
        XMLUtil.setGenericElementBooleanValue(currentElement, TYPE_KEY, key, value);
    }

    public void putBytes(String key, byte[] value)
    {
        XMLUtil.setGenericElementBytesValue(currentElement, TYPE_KEY, key, value);
    }

    public void putDouble(String key, double value)
    {
        XMLUtil.setGenericElementDoubleValue(currentElement, TYPE_KEY, key, value);
    }

    public void putFloat(String key, float value)
    {
        XMLUtil.setGenericElementFloatValue(currentElement, TYPE_KEY, key, value);
    }

    public void putInt(String key, int value)
    {
        XMLUtil.setGenericElementIntValue(currentElement, TYPE_KEY, key, value);
    }

    public void putLong(String key, long value)
    {
        XMLUtil.setGenericElementLongValue(currentElement, TYPE_KEY, key, value);
    }

}
