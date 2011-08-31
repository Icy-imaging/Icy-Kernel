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
    private final static String ID_PIXELSIZE = "pixelSize";
    private final static String ID_PIXELSPACING = "pixelSpacing";
    private final static String ID_X = "x";
    private final static String ID_Y = "y";
    private final static String ID_Z = "z";
    private final static String ID_T = "t";
    private final static String ID_C = "c";

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
            final Node nodePixelSize = XMLUtil.getElement(nodeMeta, ID_PIXELSIZE);

            if (nodePixelSize != null)
            {
                final double xs = XMLUtil.getElementDoubleValue(nodePixelSize, ID_X, 0d);
                final double ys = XMLUtil.getElementDoubleValue(nodePixelSize, ID_Y, 0d);
                final double zs = XMLUtil.getElementDoubleValue(nodePixelSize, ID_Z, 0d);
                final double ts = XMLUtil.getElementDoubleValue(nodePixelSize, ID_T, 0d);
                final double cs = XMLUtil.getElementDoubleValue(nodePixelSize, ID_C, 0d);

                if (xs != 0)
                    sequence.setPixelSizeX(xs);
                if (ys != 0)
                    sequence.setPixelSizeY(ys);
                if (zs != 0)
                    sequence.setPixelSizeZ(zs);
                if (ts != 0)
                    sequence.setPixelSizeT(ts);
                if (cs != 0)
                    sequence.setPixelSizeC(cs);
            }

            final Node nodePixelSpacing = XMLUtil.getElement(nodeMeta, ID_PIXELSPACING);

            if (nodePixelSpacing != null)
            {
                final double xs = XMLUtil.getElementDoubleValue(nodePixelSpacing, ID_X, 0d);
                final double ys = XMLUtil.getElementDoubleValue(nodePixelSpacing, ID_Y, 0d);
                final double zs = XMLUtil.getElementDoubleValue(nodePixelSpacing, ID_Z, 0d);
                final double ts = XMLUtil.getElementDoubleValue(nodePixelSpacing, ID_T, 0d);
                final double cs = XMLUtil.getElementDoubleValue(nodePixelSpacing, ID_C, 0d);

                if (xs != 0)
                    sequence.setPixelSpacingX(xs);
                if (ys != 0)
                    sequence.setPixelSpacingY(ys);
                if (zs != 0)
                    sequence.setPixelSpacingZ(zs);
                if (ts != 0)
                    sequence.setPixelSpacingT(ts);
                if (cs != 0)
                    sequence.setPixelSpacingC(cs);
            }
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
                        roi.attachTo(sequence);
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
            final Node nodePixelSize = XMLUtil.setElement(nodeMeta, ID_PIXELSIZE);

            if (node != null)
            {
                XMLUtil.setElementDoubleValue(nodePixelSize, ID_X, sequence.getPixelSizeX());
                XMLUtil.setElementDoubleValue(nodePixelSize, ID_Y, sequence.getPixelSizeY());
                XMLUtil.setElementDoubleValue(nodePixelSize, ID_Z, sequence.getPixelSizeZ());
                XMLUtil.setElementDoubleValue(nodePixelSize, ID_T, sequence.getPixelSizeT());
                XMLUtil.setElementDoubleValue(nodePixelSize, ID_C, sequence.getPixelSizeC());
            }

            final Node nodePixelSpacing = XMLUtil.setElement(nodeMeta, ID_PIXELSPACING);

            if (node != null)
            {
                XMLUtil.setElementDoubleValue(nodePixelSpacing, ID_X, sequence.getPixelSpacingX());
                XMLUtil.setElementDoubleValue(nodePixelSpacing, ID_Y, sequence.getPixelSpacingY());
                XMLUtil.setElementDoubleValue(nodePixelSpacing, ID_Z, sequence.getPixelSpacingZ());
                XMLUtil.setElementDoubleValue(nodePixelSpacing, ID_T, sequence.getPixelSpacingT());
                XMLUtil.setElementDoubleValue(nodePixelSpacing, ID_C, sequence.getPixelSpacingC());
            }
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
