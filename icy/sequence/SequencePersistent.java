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
package icy.sequence;

import icy.file.FileUtil;
import icy.file.xml.XMLPersistent;
import icy.image.lut.LUT;
import icy.roi.ROI;
import icy.system.IcyExceptionHandler;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class SequencePersistent implements XMLPersistent
{
    private final static String ID_META = "meta";
    private final static String ID_ROIS = "rois";
    private final static String ID_LUT = "lut";

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
        String seqFilename = sequence.getFilename();

        if (StringUtil.isEmpty(seqFilename))
            return null;

        // avoid '#' for XML file
        seqFilename = seqFilename.replaceAll("#", "_");

        // retrieve the serie index
        final int serieNum = sequence.getSerieIndex();

        // multi serie image ?
        if (serieNum != 0)
            // use a specific filename
            seqFilename = String.format("%s_S%d", seqFilename, Integer.valueOf(serieNum));

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
        boolean result;
        Exception exc = null;

        if ((xmlFilename != null) && FileUtil.exists(xmlFilename))
        {
            try
            {
                // load xml file into document
                document = XMLUtil.loadDocument(xmlFilename, true);

                // load data from XML document
                if (document != null)
                    result = loadFromXML(getRootNode());
                else
                {
                    document = XMLUtil.createDocument(true);
                    result = false;
                }
            }
            catch (Exception e)
            {
                exc = e;
                result = false;
            }

            // an error occurred
            if (!result)
            {
                // backup the problematic file
                String backupName = FileUtil.backup(xmlFilename);

                System.err.println("Error while loading Sequence XML persistent data.");
                System.err.println("The faulty file '" + xmlFilename + "' has been backuped as '" + backupName);

                if (exc != null)
                    IcyExceptionHandler.showErrorMessage(exc, true);

                return false;
            }
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
            return XMLUtil.saveDocument(document, xmlFilename);
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
        // force the new format when we save the XML
        saveToXML(getRootNode());
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        boolean result = true;
        final String name = XMLUtil.getElementValue(node, Sequence.ID_NAME, "");

        // set name only if not empty
        if (!StringUtil.isEmpty(name))
            sequence.setName(name);

        if (!loadMetaDataFromXML(node))
            result = false;
        if (!loadROIsFromXML(node))
            result = false;
        if (!loadLUTFromXML(node))
            result = false;

        return result;
    }

    private boolean loadMetaDataFromXML(Node node)
    {
        final Node nodeMeta = XMLUtil.getElement(node, ID_META);

        // new node --> nothing to load...
        if (nodeMeta == null)
            return true;

        sequence.setPixelSizeX(XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXEL_SIZE_X, 1d));
        sequence.setPixelSizeY(XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXEL_SIZE_Y, 1d));
        sequence.setPixelSizeZ(XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXEL_SIZE_Z, 1d));
        sequence.setTimeInterval(XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_TIME_INTERVAL, 1d));

        for (int c = 0; c < sequence.getSizeC(); c++)
            sequence.setChannelName(
                    c,
                    XMLUtil.getElementValue(nodeMeta, Sequence.ID_CHANNEL_NAME + c, MetaDataUtil.DEFAULT_CHANNEL_NAME
                            + c));

        return true;
    }

    private boolean loadROIsFromXML(Node node)
    {
        final Node roisNode = XMLUtil.getElement(node, ID_ROIS);

        // new node --> nothing to load...
        if (roisNode == null)
            return true;

        final int roiCount = ROI.getROICount(roisNode);
        final List<ROI> rois = ROI.loadROIsFromXML(roisNode);

        // add to sequence
        for (ROI roi : rois)
            sequence.addROI(roi);

        // return true if we got the expected number of ROI
        return (roiCount == rois.size());
    }

    private boolean loadLUTFromXML(Node node)
    {
        final Node nodeLut = XMLUtil.getElement(node, ID_LUT);

        // new node --> nothing to load...
        if (nodeLut == null)
            return true;

        // use the default LUT by default
        final LUT result = sequence.getDefaultLUT();

        if (result.loadFromXML(nodeLut))
            sequence.setUserLUT(result);

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        XMLUtil.setElementValue(node, Sequence.ID_NAME, sequence.getName());

        saveMetaDataToXML(node);
        saveROIsToXML(node);
        saveLUTToXML(node);

        return true;
    }

    private void saveMetaDataToXML(Node node)
    {
        final Node nodeMeta = XMLUtil.setElement(node, ID_META);

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
        final Node nodeROIs = XMLUtil.setElement(node, ID_ROIS);

        if (nodeROIs != null)
        {
            XMLUtil.removeAllChildren(nodeROIs);

            // get sorted ROIs
            final List<ROI> rois = sequence.getROIs(true);

            // set rois in the XML node
            ROI.saveROIsToXML(nodeROIs, rois);
        }
    }

    private void saveLUTToXML(Node node)
    {
        final LUT lut = sequence.getUserLUT();

        // something to save ?
        if (lut != null)
        {
            final Node nodeLut = XMLUtil.setElement(node, ID_LUT);

            if (nodeLut != null)
            {
                XMLUtil.removeAllChildren(nodeLut);
                lut.saveToXML(nodeLut);
            }
        }
    }

    /**
     * Get Sequence XML root node
     */
    public Node getRootNode()
    {
        return XMLUtil.getRootElement(document);
    }

    /**
     * Get XML data node identified by specified name
     * 
     * @param name
     *        name of wanted node
     */
    public Node getNode(String name)
    {
        return XMLUtil.getChild(getRootNode(), name);
    }

    /**
     * Create a new node with specified name and return it.<br>
     * If the node already exists the existing node is returned.
     * 
     * @param name
     *        name of node to set in attached XML data
     */
    public Node setNode(String name)
    {
        return XMLUtil.setElement(getRootNode(), name);
    }
}
