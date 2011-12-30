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
import icy.file.xml.XMLPersistent;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author stephane
 */
public class ElementDescriptor implements XMLPersistent
{
    private static final String ID_NAME = "name";
    private static final String ID_VERSION = "version";
    private static final String ID_FILES = "files";
    private static final String ID_FILE = "file";
    private static final String ID_LINK = "link";
    private static final String ID_EXECUTE = "execute";
    private static final String ID_WRITE = "write";
    private static final String ID_DIRECTORY = "directory";
    private static final String ID_FILENUMBER = "fileNumber";
    private static final String ID_DATEMODIF = "datemodif";
    private static final String ID_LOCALPATH = "localpath";
    private static final String ID_ONLINEPATH = "onlinepath";
    private static final String ID_CHANGESLOG = "changeslog";

    public class ElementFile implements XMLPersistent
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
         * directory file.
         */
        private boolean directory;

        /**
         * date of modification
         */
        private long dateModif;

        /**
         * number of file (for directory only)
         */
        private int fileNumber;

        /**
         * 
         */
        public ElementFile(Node node)
        {
            super();

            loadFromXML(node);
        }

        /**
         * Create a new element file using specified element informations
         */
        public ElementFile(ElementFile elementFile)
        {
            super();

            localPath = elementFile.localPath;
            onlinePath = elementFile.onlinePath;
            dateModif = elementFile.dateModif;
            link = elementFile.link;
            executable = elementFile.executable;
            writable = elementFile.writable;
            directory = elementFile.directory;
            fileNumber = elementFile.fileNumber;
        }

        @Override
        public boolean loadFromXML(Node node)
        {
            if (node == null)
                return false;

            localPath = XMLUtil.getElementValue(node, ID_LOCALPATH, "");
            onlinePath = XMLUtil.getElementValue(node, ID_ONLINEPATH, "");
            dateModif = XMLUtil.getElementLongValue(node, ID_DATEMODIF, 0L);
            link = XMLUtil.getElementBooleanValue(node, ID_LINK, false);
            executable = XMLUtil.getElementBooleanValue(node, ID_EXECUTE, false);
            writable = XMLUtil.getElementBooleanValue(node, ID_WRITE, false);
            directory = XMLUtil.getElementBooleanValue(node, ID_DIRECTORY, false);
            fileNumber = XMLUtil.getElementIntValue(node, ID_FILENUMBER, 1);

            return true;
        }

        @Override
        public boolean saveToXML(Node node)
        {
            return saveToNode(node, true);
        }

        boolean saveToNode(Node node, boolean onlineSave)
        {
            if (node == null)
                return false;

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
                if (directory)
                {
                    XMLUtil.addElement(node, ID_DIRECTORY, Boolean.toString(directory));
                    XMLUtil.addElement(node, ID_FILENUMBER, Integer.toString(fileNumber));
                }
            }

            return true;
        }

        public boolean isEmpty()
        {
            return StringUtil.isEmpty(localPath) && StringUtil.isEmpty(onlinePath);
        }

        public boolean exists()
        {
            return FileUtil.exists(localPath);
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

        /**
         * @return the executable
         */
        public boolean isExecutable()
        {
            return executable;
        }

        /**
         * @return the writable
         */
        public boolean isWritable()
        {
            return writable;
        }

        /**
         * @return the directory
         */
        public boolean isDirectory()
        {
            return directory;
        }

        /**
         * @return the fileNumber
         */
        public int getFileNumber()
        {
            return fileNumber;
        }

        /**
         * @param dateModif
         *        the dateModif to set
         */
        public void setDateModif(long dateModif)
        {
            this.dateModif = dateModif;
        }

        /**
         * @param link
         *        the link to set
         */
        public void setLink(boolean link)
        {
            this.link = link;
        }

        /**
         * @param executable
         *        the executable to set
         */
        public void setExecutable(boolean executable)
        {
            this.executable = executable;
        }

        /**
         * @param writable
         *        the writable to set
         */
        public void setWritable(boolean writable)
        {
            this.writable = writable;
        }

        /**
         * @param directory
         *        the directory to set
         */
        public void setDirectory(boolean directory)
        {
            this.directory = directory;
        }

        /**
         * @param fileNumber
         *        the fileNumber to set
         */
        public void setFileNumber(int fileNumber)
        {
            this.fileNumber = fileNumber;
        }

        /**
         * Return true if the specified ElementFile is the same than current one.<br>
         * 
         * @param elementFile
         *        the element file to compare
         * @param compareOnlinePath
         *        specify if we compare online path information
         * @param compareValidDateOnly
         *        true if we do compare only valid date (!= 0)
         */
        public boolean isSame(ElementFile elementFile, boolean compareOnlinePath, boolean compareValidDateOnly)
        {
            if (elementFile == null)
                return false;

            if (!StringUtil.equals(elementFile.localPath, localPath))
                return false;
            if (compareOnlinePath && (!StringUtil.equals(elementFile.onlinePath, onlinePath)))
                return false;
            if (elementFile.fileNumber != fileNumber)
                return false;

            if ((elementFile.dateModif == 0) || (dateModif == 0))
            {
                // don't compare dates if one is invalid
                if (compareValidDateOnly)
                    return true;

                // one of the date is not valid --> can't compare
                return false;
            }

            return (elementFile.dateModif == dateModif);
        }

        @Override
        public String toString()
        {
            return FileUtil.getFileName(localPath);
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

        loadFromXML(node);
    }

    /**
     * Create a new element descriptor using specified element informations
     */
    public ElementDescriptor(ElementDescriptor element)
    {
        super();

        name = element.name;
        version = new Version(element.version.toString());
        changelog = element.changelog;

        files = new ArrayList<ElementFile>();

        for (ElementFile f : element.files)
            files.add(new ElementFile(f));
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

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

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        return saveToNode(node, true);
    }

    public boolean saveToNode(Node node, boolean onlineSave)
    {
        if (node == null)
            return false;

        XMLUtil.addElement(node, ID_NAME, name);
        XMLUtil.addElement(node, ID_VERSION, version.toString());

        // some informations aren't needed for local version
        if (onlineSave)
            XMLUtil.addElement(node, ID_CHANGESLOG, changelog);

        final Element filesNode = XMLUtil.addElement(node, ID_FILES);
        for (ElementFile elementFile : files)
            elementFile.saveToNode(XMLUtil.addElement(filesNode, ID_FILE), onlineSave);

        return true;
    }

    /**
     * return ElementFile containing specified local path
     */
    public ElementFile getElementFile(String localPath)
    {
        for (ElementFile file : files)
            if (file.getLocalPath().compareToIgnoreCase(localPath) == 0)
                return file;

        return null;
    }

    /**
     * return true if element contains the specified local path
     */
    public boolean hasLocalPath(String localPath)
    {
        return getElementFile(localPath) != null;
    }

    public boolean addElementFile(ElementFile file)
    {
        return files.add(file);
    }

    public boolean removeElementFile(ElementFile file)
    {
        return files.remove(file);
    }

    public void removeElementFile(String localPath)
    {
        removeElementFile(getElementFile(localPath));
    }

    /**
     * Validate the current element descriptor.<br>
     * It actually remove missing files from the element.<br>
     * Return true if all files are valid.
     */
    public boolean validate()
    {
        boolean result = true;

        for (int i = files.size() - 1; i >= 0; i--)
        {
            final ElementFile elementFile = files.get(i);
            final File file = new File(elementFile.getLocalPath());

            if (file.exists())
            {
                // update modification date
                elementFile.setDateModif(file.lastModified());
                // directory file ?
                if (file.isDirectory())
                {
                    // update directory informations
                    elementFile.setDirectory(true);
                    elementFile.setFileNumber(FileUtil.getFileList(file, true, false, false).size());
                }
            }
            else
            {
                // remove missing file
                files.remove(i);
                result = false;
            }
        }

        return result;
    }

    public boolean isValid()
    {
        for (ElementFile file : files)
            if (!file.exists())
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
     * @return the number of files
     */
    public int getFilesNumber()
    {
        return files.size();
    }

    /**
     * @return the files
     */
    public ArrayList<ElementFile> getFiles()
    {
        return files;
    }

    /**
     * @return the specified file
     */
    public ElementFile getFile(int index)
    {
        return files.get(index);
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

    /**
     * Return true if the specified ElementDescriptor is the same than current one.<br>
     * 
     * @param element
     *        the element descriptor to compare
     * @param compareFileOnlinePath
     *        specify if we compare file online path information
     */
    public boolean isSame(ElementDescriptor element, boolean compareFileOnlinePath)
    {
        if (element == null)
            return false;

        // different name
        if (!name.equals(element.name))
            return false;
        // different version
        if (!version.equals(element.version))
            return false;
        // different number of files
        if (files.size() != element.files.size())
            return false;

        // compare files
        for (ElementFile file : files)
        {
            final ElementFile elementFile = element.getElementFile(file.getLocalPath());

            // file missing --> different
            if (elementFile == null)
                return false;

            // file different (compare date only if they are valid) --> different
            if (!elementFile.isSame(file, compareFileOnlinePath, true))
                return false;
        }

        // same element
        return true;
    }

    /**
     * Process and return the update the element which contain differences<br>
     * from the specified local and online elements.<br>
     * If local element refers the same item, only missing or different files will remains.<br>
     * If local element refers a different element, online element is returned unchanged.
     * 
     * @return the update element (null if local and online elements are the same)
     */
    public static ElementDescriptor getUpdateElement(ElementDescriptor localElement, ElementDescriptor onlineElement)
    {
        if (onlineElement == null)
            return null;

        // use a copy
        final ElementDescriptor result = new ElementDescriptor(onlineElement);

        if (localElement == null)
            return result;
        // different name
        if (!StringUtil.equals(result.name, localElement.name))
            return result;

        // if same version, compare files on valid date only
        final boolean compareValidDateOnly = result.version.equals(localElement.version);

        // compare files
        for (int i = result.files.size() - 1; i >= 0; i--)
        {
            final ElementFile onlineFile = result.files.get(i);
            final ElementFile localFile = localElement.getElementFile(onlineFile.getLocalPath());

            // same file ? --> remove it (no need to be updated)
            if ((localFile != null) && onlineFile.isSame(localFile, false, compareValidDateOnly))
                result.files.remove(i);
        }

        // no files to update ? --> return null
        if (result.files.isEmpty())
            return null;

        return result;
    }

    /**
     * Update current element with informations from specified element
     */
    public void update(ElementDescriptor updateElement)
    {
        // update version info
        version = updateElement.version;

        // add or update files
        for (ElementFile updateFile : updateElement.files)
        {
            // get corresponding file
            final ElementFile localFile = getElementFile(updateFile.getLocalPath());

            // file missing ? --> add it
            if (localFile == null)
                files.add(updateFile);
            else
            {
                // update file (we don't care about online information)
                localFile.setDateModif(updateFile.getDateModif());
                localFile.setExecutable(updateFile.isExecutable());
                localFile.setLink(updateFile.isLink());
                localFile.setWritable(updateFile.isWritable());
                localFile.setDirectory(updateFile.isDirectory());
            }
        }
    }

    @Override
    public String toString()
    {
        return name + " " + version;
    }

}
