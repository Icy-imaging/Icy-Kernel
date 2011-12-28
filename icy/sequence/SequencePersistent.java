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
package icy.sequence;

import icy.file.FileUtil;
import icy.file.xml.XMLPersistent;
import icy.roi.ROI;
import icy.system.IcyExceptionHandler;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class SequencePersistent implements XMLPersistent
{
    private final static String ID_NAME = "name";

    private final static String ROOT_META = "meta";

    private final static String ROOT_ROIS = "rois";
    private final static String ID_ROI = "roi";

    private final Sequence sequence;

    private Document document;

    /**
     * 
     */
    public SequencePersistent(Sequence sequence)
    {
        super();

        this.sequence = sequence;

        document = XMLUtil.createDocument(true);
    }

    private String getXMLFileName()
    {
        final String seqFilename = sequence.getFilename();

        if (StringUtil.isEmpty(seqFilename))
            return null;
        
        final File file = new File(seqFilename);
        
        // filename reference a directory --> use "<directory>/meta.xml"
        if (file.isDirectory())
            return seqFilename + "/meta.xml";

        return FileUtil.setExtension(seqFilename, ".xml");
    }

    /**
     * Load XML persistent data.<br>
     * Return true if XML data has been correctly loaded.
     */
    public boolean loadXMLData()
    {
        final String xmlFilename = getXMLFileName();

        try
        {
            // load xml file into document
            if ((xmlFilename != null) && FileUtil.exists(xmlFilename))
            {
                document = XMLUtil.loadDocument(xmlFilename, true);

                if (document == null)
                {
                    // rename problematic file
                    FileUtil.copy(xmlFilename, xmlFilename + ".bak", true, false, false);

                    System.err.println("Error while loading Sequence XML persistent data.");
                    System.err.println("The faulty file '" + xmlFilename + "' has been saved as '" + xmlFilename
                            + ".bak'");

                    document = XMLUtil.createDocument(true);
                    return false;
                }

                // load data from XML document
                return loadFromXML(document.getDocumentElement());
            }
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, true);
            return false;
        }

        return true;
    }

    /**
     * Save XML persistent data.<br>
     * Return true if XML data has been correctly saved.
     */
    public boolean saveXMLData()
    {
        final String xmlFilename = getXMLFileName();

        if (xmlFilename == null)
            return false;

        try
        {
            // rebuild document
            refreshXMLData();

            // save xml file
            return XMLUtil.saveDocument(document, getXMLFileName());
        }
        catch (Exception e)
        {
            System.err.println("Error while saving Sequence XML persistent data :");
            IcyExceptionHandler.showErrorMessage(e, true);
            return false;
        }
    }

    public void refreshXMLData()
    {
        saveToXML(document.getDocumentElement());
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        final String name = XMLUtil.getElementValue(node, ID_NAME, "");

        // set name only if not empty
        if (!StringUtil.isEmpty(name))
            sequence.setName(name);

        loadMetaDataFromXML(node);
        loadROIsFromXML(node);

        return true;
    }

    private void loadMetaDataFromXML(Node node)
    {
        final Node nodeMeta = XMLUtil.getElement(node, ROOT_META);

        if (nodeMeta != null)
        {
            sequence.setPixelSizeX(XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXEL_SIZE_X, 1d));
            sequence.setPixelSizeY(XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXEL_SIZE_Y, 1d));
            sequence.setPixelSizeZ(XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXEL_SIZE_Z, 1d));
            sequence.setTimeInterval(XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_TIME_INTERVAL, 1d));
            for (int c = 0; c < sequence.getSizeC(); c++)
                sequence.setChannelName(
                        c,
                        XMLUtil.getElementValue(nodeMeta, Sequence.ID_CHANNEL_NAME + c, Sequence.DEFAULT_CHANNEL_NAME
                                + c));
        }
    }

    private void loadROIsFromXML(Node node)
    {
        final Node nodeROIs = XMLUtil.getElement(node, ROOT_ROIS);

        if (nodeROIs != null)
        {
            final ArrayList<Node> nodesROI = XMLUtil.getSubNodes(nodeROIs, ID_ROI);

            if (nodesROI != null)
            {
                for (Node n : nodesROI)
                {
                    final ROI roi = ROI.createFromXML(n);

                    // add to sequence
                    if (roi != null)
                        sequence.addROI(roi);
                }
            }
        }
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (node == null)
            return false;

        XMLUtil.setElementValue(node, ID_NAME, sequence.getName());

        saveMetaDataToXML(node);
        saveROIsToXML(node);

        return true;
    }

    private void saveMetaDataToXML(Node node)
    {
        final Node nodeMeta = XMLUtil.setElement(node, ROOT_META);

        if (nodeMeta != null)
        {
            XMLUtil.setElementDoubleValue(nodeMeta, Sequence.ID_PIXEL_SIZE_X, sequence.getPixelSizeX());
            XMLUtil.setElementDoubleValue(nodeMeta, Sequence.ID_PIXEL_SIZE_Y, sequence.getPixelSizeY());
            XMLUtil.setElementDoubleValue(nodeMeta, Sequence.ID_PIXEL_SIZE_Z, sequence.getPixelSizeZ());
            XMLUtil.setElementDoubleValue(nodeMeta, Sequence.ID_TIME_INTERVAL, sequence.getTimeInterval());
            for (int c = 0; c < sequence.getSizeC(); c++)
                XMLUtil.setElementValue(nodeMeta, Sequence.ID_CHANNEL_NAME + c, sequence.getChannelName(c));
        }
    }

    private void saveROIsToXML(Node node)
    {
        final Node nodeROIs = XMLUtil.setElement(node, ROOT_ROIS);

        if (nodeROIs != null)
        {
            XMLUtil.removeAllChilds(nodeROIs);

            for (ROI roi : sequence.getROIs())
            {
                final Node nodeROI = XMLUtil.addElement(nodeROIs, ID_ROI);

                if (nodeROI != null)
                {
                    if (!roi.saveToXML(nodeROI))
                        XMLUtil.removeNode(nodeROIs, nodeROI);
                }
            }
        }
    }

    /**
     * Get XML data node identified by specified name
     * 
     * @param name
     *        name of wanted node
     */
    public Node getNode(String name)
    {
        return XMLUtil.getSubNode(document.getDocumentElement(), name);
    }

    /**
     * Create a new node with specified name and return it<br>
     * If the node already exists the existing node is returned
     * 
     * @param name
     *        name of node to set in attached XML data
     */
    public Node setNode(String name)
    {
        // can't replace META and ROIS nodes
        if (!name.equals(ROOT_META) && !name.equals(ROOT_ROIS))
            return XMLUtil.setElement(document.getDocumentElement(), name);

        return null;
    }
}
