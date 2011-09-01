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

import icy.common.Version;
import icy.file.FileUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author stephane
 */
public class ElementDescriptor
{
    private static final String ID_NAME = "name";
    private static final String ID_VERSION = "version";
    private static final String ID_FILES = "files";
    private static final String ID_FILE = "file";
    private static final String ID_LINK = "link";
    private static final String ID_EXECUTE = "execute";
    private static final String ID_WRITE = "write";
    private static final String ID_DATEMODIF = "datemodif";
    private static final String ID_LOCALPATH = "localpath";
    private static final String ID_ONLINEPATH = "onlinepath";
    private static final String ID_CHANGESLOG = "changeslog";

    public class ElementFile
    {
        private String localPath;
        private String onlinePath;

        /**
         * symbolic link element, onlinePath define the target of the link file
         */
        private boolean link;

        /**
         * need execute permission
         */
        private boolean executable;

        /**
         * need write permission
         */
        private boolean writable;

        /**
         * date of modification
         */
        private long dateModif;

        /**
         * 
         */
        public ElementFile(Node node)
        {
            super();

            loadFromNode(node);
        }

        public void loadFromNode(Node node)
        {
            localPath = XMLUtil.getElementValue(node, ID_LOCALPATH, "");
            onlinePath = XMLUtil.getElementValue(node, ID_ONLINEPATH, "");
            dateModif = XMLUtil.getElementLongValue(node, ID_DATEMODIF, 0L);
            link = XMLUtil.getElementBooleanValue(node, ID_LINK, false);
            executable = XMLUtil.getElementBooleanValue(node, ID_EXECUTE, false);
            writable = XMLUtil.getElementBooleanValue(node, ID_WRITE, false);
        }

        public void saveToNode(Node node, boolean onlineSave)
        {
            XMLUtil.addElement(node, ID_LOCALPATH, localPath);

            if (onlineSave)
            {
                XMLUtil.addElement(node, ID_ONLINEPATH, onlinePath);
                XMLUtil.addElement(node, ID_DATEMODIF, Long.toString(dateModif));
                if (link)
                    XMLUtil.addElement(node, ID_LINK, Boolean.toString(link));
                if (executable)
                    XMLUtil.addElement(node, ID_EXECUTE, Boolean.toString(executable));
                if (writable)
                    XMLUtil.addElement(node, ID_WRITE, Boolean.toString(writable));
            }
        }

        public boolean isEmpty()
        {
            return StringUtil.isEmpty(localPath) && StringUtil.isEmpty(onlinePath);
        }

        public boolean exist()
        {
            return FileUtil.exist(localPath);
        }

        /**
         * @return the localPath
         */
        public String getLocalPath()
        {
            return localPath;
        }

        /**
         * @return the onlinePath
         */
        public String getOnlinePath()
        {
            return onlinePath;
        }

        /**
         * @return the dateModif
         */
        public long getDateModif()
        {
            return dateModif;
        }

        /**
         * @return the link
         */
        public boolean isLink()
        {
            return link;
        }

        public boolean isExecutable()
        {
            return executable;
        }

        public boolean isWritable()
        {
            return writable;
        }

        /**
         * @param dateModif
         *        the dateModif to set
         */
        public void setDateModif(long dateModif)
        {
            this.dateModif = dateModif;
        }
    }

    private String name;
    private Version version;
    private final ArrayList<ElementFile> files;
    private String changelog;

    /**
     * 
     */
    public ElementDescriptor(Node node)
    {
        super();

        files = new ArrayList<ElementFile>();

        loadFromNode(node);
    }

    public void loadFromNode(Node node)
    {
        name = XMLUtil.getElementValue(node, ID_NAME, "");
        version = new Version(XMLUtil.getElementValue(node, ID_VERSION, ""));
        changelog = XMLUtil.getElementValue(node, ID_CHANGESLOG, "");

        final ArrayList<Node> nodesFile = XMLUtil.getSubNodes(XMLUtil.getElement(node, ID_FILES), ID_FILE);
        if (nodesFile != null)
        {
            for (Node n : nodesFile)
            {
                final ElementFile elementFile = new ElementFile(n);

                if (!elementFile.isEmpty())
                    files.add(elementFile);
            }
        }
    }

    public void saveToNode(Node node, boolean onlineSave)
    {
        XMLUtil.addElement(node, ID_NAME, name);
        XMLUtil.addElement(node, ID_VERSION, version.toString());

        // some informations aren't needed for local version
        if (onlineSave)
            XMLUtil.addElement(node, ID_CHANGESLOG, changelog);

        final Element filesNode = XMLUtil.addElement(node, ID_FILES);
        for (ElementFile elementFile : files)
            elementFile.saveToNode(XMLUtil.addElement(filesNode, ID_FILE), onlineSave);
    }

    /**
     * return ElementFile containing specified local path
     */
    public ElementFile getElementFileFromLocalPath(String path)
    {
        for (ElementFile file : files)
            if (file.getLocalPath().compareToIgnoreCase(path) == 0)
                return file;

        return null;
    }

    /**
     * return true if element contains the specified local path
     */
    public boolean hasLocalPath(String path)
    {
        return getElementFileFromLocalPath(path) != null;
    }

    public boolean addElementFile(ElementFile file)
    {
        return files.add(file);
    }

    public boolean removeElementFile(ElementFile file)
    {
        return files.remove(file);
    }

    public void removeElementFileFromLocalPath(String path)
    {
        removeElementFile(getElementFileFromLocalPath(path));
    }

    public boolean isValid()
    {
        for (ElementFile file : files)
            if (!file.exist())
                return false;

        return true;
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
        return version;
    }

    /**
     * @return the files
     */
    public ArrayList<ElementFile> getFiles()
    {
        return files;
    }

    /**
     * @return the changelog
     */
    public String getChangelog()
    {
        return changelog;
    }

    /**
     * @param version
     *        the version to set
     */
    public void setVersion(Version version)
    {
        this.version = version;
    }

    @Override
    public String toString()
    {
        return name + " " + version;
    }
}
