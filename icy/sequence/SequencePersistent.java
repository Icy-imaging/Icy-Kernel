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
import icy.util.StringUtil;
import icy.util.XMLUtil;

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

        return FileUtil.setExtension(seqFilename, ".xml");
    }

    public boolean loadXMLData()
    {
        final String xmlFilename = getXMLFileName();

        // load xml file into document
        if ((xmlFilename != null) && FileUtil.exist(xmlFilename))
            document = XMLUtil.loadDocument(xmlFilename, true);

        // load data from XML document
        return loadFromXML(document.getDocumentElement());
    }

    public boolean saveXMLData()
    {
        final String xmlFilename = getXMLFileName();

        if (xmlFilename == null)
            return false;

        // rebuild document
        refreshXMLData();

        // save xml file
        return XMLUtil.saveDocument(document, getXMLFileName());
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
            sequence.setPixelSizeX(XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXELS_SIZE_X, 1d));
            sequence.setPixelSizeY(XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXELS_SIZE_Y, 1d));
            sequence.setPixelSizeZ(XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXELS_SIZE_Z, 1d));
            sequence.setPixelSizeT(XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXELS_SIZE_T, 1d));
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
            XMLUtil.setElementDoubleValue(nodeMeta, Sequence.ID_PIXELS_SIZE_X, sequence.getPixelSizeX());
            XMLUtil.setElementDoubleValue(nodeMeta, Sequence.ID_PIXELS_SIZE_Y, sequence.getPixelSizeY());
            XMLUtil.setElementDoubleValue(nodeMeta, Sequence.ID_PIXELS_SIZE_Z, sequence.getPixelSizeZ());
            XMLUtil.setElementDoubleValue(nodeMeta, Sequence.ID_PIXELS_SIZE_T, sequence.getPixelSizeT());
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
