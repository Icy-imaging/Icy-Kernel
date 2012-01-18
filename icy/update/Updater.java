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
package icy.update;

import icy.file.FileUtil;
import icy.update.ElementDescriptor.ElementFile;
import icy.util.StringUtil;
import icy.util.XMLUtil;
import icy.util.ZipUtil;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Updater
{
    public static final String ICYUPDATER_NAME = "ICY Updater";

    public static final String UPDATE_DIRECTORY = "update";
    public static final String BACKUP_DIRECTORY = "backup";
    public static final String UPDATE_BASE_NAME = "update";
    public static final String UPDATE_EXT_NAME = ".xml";
    public static final String UPDATE_NAME = UPDATE_BASE_NAME + UPDATE_EXT_NAME;
    public static final String VERSION_NAME = "version.xml";
    // public static final String OBSOLETE_NAME = "obsolete.xml";
    public static final String UPDATER_NAME = "updater.jar";

    public static final String ARG_NOSTART = "-nostart";
    public static final String ARG_UPDATE = "-update";

    private static final String ID_ELEMENTS = "elements";
    private static final String ID_ELEMENT = "element";
    private static final String ID_OBSOLETES = "obsoletes";
    private static final String ID_LOCALPATH = "localpath";

    // /**
    // * Get update elements.<br>
    // * Compare local elements with online element and return a list of element<br>
    // * which need to be updated.
    // */
    // public static ArrayList<ElementDescriptor> getUpdateElements()
    // {
    // return getUpdateElements(getLocalElements());
    // }

    // /**
    // * Update the local version.xml file so it contains only present elements with correct
    // * modification date.
    // */
    // public static boolean validateLocalElementsXML()
    // {
    // // get local elements
    // final ArrayList<ElementDescriptor> localElements = getLocalElements();
    // // validate them
    // validateLocalElements(localElements);
    // // and save to local XML file
    // return saveElementsToXML(localElements, VERSION_NAME, false);
    // }

    // public static boolean updateXML()
    // {
    // final ArrayList<ElementDescriptor> localElements = getLocalElements();
    // final ArrayList<ElementDescriptor> onlineElements = getOnlineElements();
    //
    // // update local list
    // for (ElementDescriptor onlineElement : onlineElements)
    // {
    // final ElementDescriptor localElement = findElement(onlineElement.getName(), localElements);
    // // local element absent or outdated ?
    // if (localElement == null)
    // localElements.add(onlineElement);
    // else if (onlineElement.getVersion().isGreater(localElement.getVersion()))
    // // set new version
    // localElement.setVersion(onlineElement.getVersion());
    // }
    //
    // // save new version XML file return
    // return saveElementsToXML(localElements, VERSION_NAME, false);
    // }

    /**
     * Validate the specified list of elements against local files.<br>
     * This actually remove missing files and update the file modification date.
     */
    public static void validateElements(ArrayList<ElementDescriptor> elements)
    {
        // validate elements against local files
        for (int i = elements.size() - 1; i >= 0; i--)
        {
            final ElementDescriptor element = elements.get(i);

            // validate element
            element.validate();

            // no more valid file ? --> remove element
            if (element.getFilesNumber() == 0)
                elements.remove(i);
        }
    }

    /**
     * Get the list of local elements.<br>
     * Elements are fetched from local version.xml file then validated with local files.
     */
    public static ArrayList<ElementDescriptor> getLocalElements()
    {
        // get local elements from XML file
        final ArrayList<ElementDescriptor> result = loadElementsFromXML(VERSION_NAME);

        // validate elements
        validateElements(result);

        return result;
    }

    /**
     * Get the list of online elements (online update.xml file)
     */
    public static ArrayList<ElementDescriptor> getOnlineElements()
    {
        return loadElementsFromXML(UPDATE_DIRECTORY + FileUtil.separator + UPDATE_NAME);
    }

    /**
     * Get update elements.<br>
     * Compare specified local elements with online element and return a list of element<br>
     * which need to be updated.
     */
    public static ArrayList<ElementDescriptor> getUpdateElements(ArrayList<ElementDescriptor> localElements)
    {
        final ArrayList<ElementDescriptor> result = new ArrayList<ElementDescriptor>();
        final ArrayList<ElementDescriptor> onlineElements = getOnlineElements();

        // build update list
        for (ElementDescriptor onlineElement : onlineElements)
        {
            final ElementDescriptor localElement = findElement(onlineElement.getName(), localElements);
            // get update element (differences between online and local element)
            final ElementDescriptor updateElement = ElementDescriptor.getUpdateElement(localElement, onlineElement);

            if (updateElement != null)
                // add the element to update list
                result.add(updateElement);
        }

        return result;
    }

    /**
     * Get the list of obsoletes files
     */
    public static ArrayList<String> getObsoletes()
    {
        final ArrayList<String> result = new ArrayList<String>();

        final Document document = XMLUtil.loadDocument(UPDATE_DIRECTORY + FileUtil.separator + UPDATE_NAME, false);

        if (document != null)
        {
            // first we normalize the document
            document.normalizeDocument();

            // get obsoletes node
            final Node obsoletes = XMLUtil.getElement(document.getDocumentElement(), ID_OBSOLETES);

            // get all local path
            final ArrayList<Node> nodesLocalpath = XMLUtil.getSubNodes(obsoletes, ID_LOCALPATH);
            if (nodesLocalpath != null)
            {
                for (Node n : nodesLocalpath)
                {
                    final String value = XMLUtil.getValue((Element) n, "");
                    if (!StringUtil.isEmpty(value, true))
                        result.add(value);
                }
            }
        }

        return result;
    }

    public static ArrayList<ElementDescriptor> loadElementsFromXML(String path)
    {
        final ArrayList<ElementDescriptor> result = new ArrayList<ElementDescriptor>();

        final Document document = XMLUtil.loadDocument(path, true);

        if (document != null)
        {
            // first we normalize the document
            document.normalizeDocument();

            // get elements node
            final Node elements = XMLUtil.getElement(document.getDocumentElement(), ID_ELEMENTS);

            // get elements
            final ArrayList<Node> nodesElement = XMLUtil.getSubNodes(elements, ID_ELEMENT);
            if (nodesElement != null)
            {
                for (Node n : nodesElement)
                    result.add(new ElementDescriptor(n));
            }
        }

        return result;
    }

    /**
     * Save the specified elements to the specified filename
     */
    public static boolean saveElementsToXML(ArrayList<ElementDescriptor> elements, String path, boolean onlineSave)
    {
        final Document document = XMLUtil.createDocument(true);

        final Element elementsNode = XMLUtil.addElement(document.getDocumentElement(), ID_ELEMENTS);

        // set elements
        for (ElementDescriptor element : elements)
            element.saveToNode(XMLUtil.addElement(elementsNode, ID_ELEMENT), onlineSave);

        return XMLUtil.saveDocument(document, path);
    }

    /**
     * Find an element in the specified list
     */
    public static ElementDescriptor findElement(String name, ArrayList<ElementDescriptor> list)
    {
        for (ElementDescriptor element : list)
            if (name.equals(element.getName()))
                return element;

        return null;
    }

    /**
     * Find an element from his local path in the specified list
     */
    // private static ElementDescriptor findElementFromLocalPath(String path,
    // ArrayList<ElementDescriptor> list)
    // {
    // for (ElementDescriptor element : list)
    // if (element.hasLocalPath(path))
    // return element;
    //
    // return null;
    // }

    /**
     * Return true if some update files are present in the update directory
     */
    public static boolean hasUpdateFiles()
    {
        final ArrayList<String> paths = FileUtil.getFileListAsString(UPDATE_DIRECTORY, true, true);

        for (String path : paths)
        {
            final String filename = FileUtil.getFileName(path);

            // check if we have others files other than updater and XML definitions
            if ((!filename.equals(UPDATER_NAME)) && (!filename.equals(UPDATE_NAME)))
                return true;
        }

        return false;
    }

    /**
     * Update the specified "update" element (move files from update to application directory)<br>
     * then modify local elements list according to changes made.
     * 
     * @return true if update succeed, false otherwise
     */
    public static boolean udpateElement(ElementDescriptor updateElement, ArrayList<ElementDescriptor> localElements)
    {
        // update all element files
        if (Updater.updateFiles(updateElement.getFiles()))
        {
            // then modify local elements list
            updateElementInfos(updateElement, localElements);
            return true;
        }

        return false;
    }

    /**
     * Update local elements according to changes presents in updateElement
     */
    public static void updateElementInfos(ElementDescriptor updateElement, ArrayList<ElementDescriptor> localElements)
    {
        // find corresponding current local element
        final ElementDescriptor localElement = Updater.findElement(updateElement.getName(), localElements);

        // local element doesn't exist
        if (localElement == null)
            // add it
            localElements.add(updateElement);
        else
            // just update local element with update element info
            localElement.update(updateElement);
    }

    /**
     * Update the specified files
     */
    public static boolean updateFiles(ArrayList<ElementFile> files)
    {
        boolean result = true;

        for (ElementFile file : files)
            result = result & updateFile(file);

        return result;
    }

    /**
     * Update the specified local file
     */
    public static boolean updateFile(ElementFile file)
    {
        final String localPath = file.getLocalPath();

        // directory type file --> extract it
        if (file.isDirectory())
        {
            final String dirName = UPDATE_DIRECTORY + FileUtil.separator + localPath;
            final String zipName = dirName + ".zip";

            // rename directory type file (no extension) to zip file
            FileUtil.rename(dirName, zipName, true, true);
            ZipUtil.extract(zipName);
        }

        if (updateFile(localPath, file.getDateModif()))
        {
            final File dest = new File(localPath);

            // there is no reason the file doesn't exists but anyway...
            if (dest.exists())
            {
                if (file.isExecutable())
                    dest.setExecutable(true, false);
                if (file.isWritable())
                    dest.setWritable(true, false);

                return true;
            }
        }

        return false;
    }

    /**
     * Backup the specified local file
     */
    public static boolean backup(String localPath)
    {
        // file exist ? backup it
        if (FileUtil.exists(localPath))
        {
            if (!FileUtil.copy(localPath, BACKUP_DIRECTORY + FileUtil.separator + localPath, true, false, true))
                return false;

            // verify that backup file exist
            return FileUtil.exists(BACKUP_DIRECTORY + FileUtil.separator + localPath);
        }

        return true;
    }

    /**
     * Update the specified local file
     */
    public static boolean updateFile(String localPath, long dateModif)
    {
        // no update needed
        if (!needUpdate(localPath, dateModif))
            return true;

        // backup file
        if (!backup(localPath))
        {
            // backup failed
            System.err.println("Updater.udpateFile(" + localPath + ") failed :");
            System.err.println("Can't backup file to '" + BACKUP_DIRECTORY + FileUtil.separator + localPath);
            return false;
        }

        // move file
        if (!FileUtil.move(UPDATE_DIRECTORY + FileUtil.separator + localPath, localPath, true, false))
        {
            // temporary hack to bypass the javacl version problem
            if (!localPath.equals("lib/javacl.jar"))
            {
                // move failed
                System.err.println("Updater.udpateFile('" + localPath + "') failed !");
                System.err.println("Can't move file from '" + UPDATE_DIRECTORY + FileUtil.separator + localPath
                        + "' to '" + localPath + "'");
                return false;
            }
        }

        return true;
    }

    /**
     * Return true if specified file is different from the update file (in Update directory)
     */
    public static boolean needUpdate(String localPath, long dateModif)
    {
        final File localFile = new File(localPath);

        return (!localFile.exists()) || (dateModif == 0L) || (localFile.lastModified() != dateModif);
    }

    /**
     * Process to restoration (in case the update failed)
     */
    public static boolean restore()
    {
        final int len = BACKUP_DIRECTORY.length();
        // get files only (no directory)
        final ArrayList<String> paths = FileUtil.getFileListAsString(BACKUP_DIRECTORY, true, false, false);
        boolean result = true;

        for (String backupPath : paths)
        {
            final String finalPath = backupPath.substring(len + 1);

            // don't restore updater
            if (finalPath.equals(UPDATER_NAME))
                continue;

            if (!FileUtil.move(backupPath, finalPath, true, false))
            {
                // move failed (FileUtil.move is already displaying error messages if needed)
                System.err.println("Updater.restore() cannot restore '" + finalPath + "', you should do it manually.");
                result = false;
            }
        }

        return result;
    }

    /**
     * Delete obsoletes files
     */
    public static void deleteObsoletes()
    {
        // delete obsolete files
        for (String obsolete : getObsoletes())
            FileUtil.delete(obsolete, false);
    }
}
