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
package icy.file.xml;

import icy.util.XMLUtil;

import java.io.File;
import java.net.URL;

import org.w3c.dom.Document;

/**
 * @author Stephane
 */
public class XMLPersistentHelper
{
    public static boolean loadFromXML(XMLPersistent persistent, String path)
    {
        return loadFromXML(persistent, XMLUtil.loadDocument(path, false));
    }

    public static boolean loadFromXML(XMLPersistent persistent, File file)
    {
        return loadFromXML(persistent, XMLUtil.loadDocument(file, false));
    }

    public static boolean loadFromXML(XMLPersistent persistent, URL xmlUrl)
    {
        return loadFromXML(persistent, XMLUtil.loadDocument(xmlUrl, false));
    }

    public static boolean loadFromXML(XMLPersistent persistent, Document document)
    {
        if (document == null)
            return false;

        // first we normalize the document
        document.normalizeDocument();

        // load from root node
        return persistent.loadFromXML(document.getDocumentElement());
    }

    public static boolean saveToXML(XMLPersistent persistent, String path)
    {
        final Document document = XMLUtil.createDocument(true);

        if (saveToXML(persistent, document))
            return XMLUtil.saveDocument(document, path);

        return false;
    }

    public static boolean saveToXML(XMLPersistent persistent, File file)
    {
        final Document document = XMLUtil.createDocument(true);

        if (saveToXML(persistent, document))
            return XMLUtil.saveDocument(document, file);

        return false;
    }

    public static boolean saveToXML(XMLPersistent persistent, Document document)
    {
        if (document == null)
            return false;

        // first we normalize the document
        document.normalizeDocument();

        // save to root node
        return persistent.saveToXML(document.getDocumentElement());
    }
}
