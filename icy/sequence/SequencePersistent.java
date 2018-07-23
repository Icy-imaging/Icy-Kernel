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
package icy.sequence;

import icy.file.FileUtil;
import icy.file.xml.XMLPersistent;
import icy.image.lut.LUT;
import icy.painter.Overlay;
import icy.roi.ROI;
import icy.system.IcyExceptionHandler;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class SequencePersistent implements XMLPersistent
{
    private final static String ID_META = "meta";
    private final static String ID_ROIS = "rois";
    private final static String ID_OVERLAYS = "overlays";
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

    /**
     * Should return <code>null</code> if Sequence is not identified (no file name)
     */
    private String getXMLFileName()
    {
        final String baseName = sequence.getOutputFilename(false);

        if (StringUtil.isEmpty(baseName))
            return null;

        return baseName + XMLUtil.FILE_DOT_EXTENSION;
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
    public boolean saveXMLData() throws Exception
    {
        final String xmlFilename = getXMLFileName();

        if (xmlFilename == null)
            return false;

        // rebuild document
        refreshXMLData();

        // save xml file
        return XMLUtil.saveDocument(document, xmlFilename);
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
        // some overlays does not support persistence so we can ignore errors...
        loadOverlaysFromXML(node);
        if (!loadLUTFromXML(node))
            result = false;

        return result;
    }

    private boolean loadMetaDataFromXML(Node node)
    {
        final Node nodeMeta = XMLUtil.getElement(node, ID_META);

        // no node --> nothing to load...
        if (nodeMeta == null)
            return true;

        double d;
        long l;

        d = XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_POSITION_X, Double.NaN);
        if (!Double.isNaN(d))
            sequence.setPositionX(d);
        d = XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_POSITION_Y, Double.NaN);
        if (!Double.isNaN(d))
            sequence.setPositionY(d);
        d = XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_POSITION_Z, Double.NaN);
        if (!Double.isNaN(d))
            sequence.setPositionZ(d);
        l = XMLUtil.getElementLongValue(nodeMeta, Sequence.ID_POSITION_T, -1L);
        if (l != -1L)
            sequence.setPositionT(l);

        d = XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXEL_SIZE_X, Double.NaN);
        if (!Double.isNaN(d))
            sequence.setPixelSizeX(d);
        d = XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXEL_SIZE_Y, Double.NaN);
        if (!Double.isNaN(d))
            sequence.setPixelSizeY(d);
        d = XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_PIXEL_SIZE_Z, Double.NaN);
        if (!Double.isNaN(d))
            sequence.setPixelSizeZ(d);
        d = XMLUtil.getElementDoubleValue(nodeMeta, Sequence.ID_TIME_INTERVAL, Double.NaN);
        if (!Double.isNaN(d))
            sequence.setTimeInterval(d);

        for (int c = 0; c < sequence.getSizeC(); c++)
        {
            final String s = XMLUtil.getElementValue(nodeMeta, Sequence.ID_CHANNEL_NAME + c, "");

            if (!StringUtil.isEmpty(s))
                sequence.setChannelName(c, s);
        }

        return true;
    }

    private boolean loadROIsFromXML(Node node)
    {
        final Node roisNode = XMLUtil.getElement(node, ID_ROIS);

        // no node --> nothing to load...
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

    private boolean loadOverlaysFromXML(Node node)
    {
        final Node overlaysNode = XMLUtil.getElement(node, ID_OVERLAYS);

        // no node --> nothing to load...
        if (overlaysNode == null)
            return true;

        final int overlayCount = Overlay.getOverlayCount(overlaysNode);
        final List<Overlay> overlays = Overlay.loadOverlaysFromXML(overlaysNode);

        // add to sequence
        for (Overlay overlay : overlays)
            sequence.addOverlay(overlay);

        // return true if we got the expected number of ROI
        return (overlayCount == overlays.size());
    }

    private boolean loadLUTFromXML(Node node)
    {
        final Node nodeLut = XMLUtil.getElement(node, ID_LUT);

        // no node --> nothing to load...
        if (nodeLut == null)
            return true;

        // use the default LUT by default
        final LUT result = sequence.createCompatibleLUT();

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
        saveOverlaysToXML(node);
        saveLUTToXML(node);

        return true;
    }

    private void saveMetaDataToXML(Node node)
    {
        final Node nodeMeta = XMLUtil.setElement(node, ID_META);

        if (nodeMeta != null)
        {
            XMLUtil.setElementDoubleValue(nodeMeta, Sequence.ID_POSITION_X, sequence.getPositionX());
            XMLUtil.setElementDoubleValue(nodeMeta, Sequence.ID_POSITION_Y, sequence.getPositionY());
            XMLUtil.setElementDoubleValue(nodeMeta, Sequence.ID_POSITION_Z, sequence.getPositionZ());
            XMLUtil.setElementLongValue(nodeMeta, Sequence.ID_POSITION_T, sequence.getPositionT());
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

    private void saveOverlaysToXML(Node node)
    {
        final Node nodeOverlays = XMLUtil.setElement(node, ID_OVERLAYS);

        if (nodeOverlays != null)
        {
            XMLUtil.removeAllChildren(nodeOverlays);

            // get overlays in linked list for faster remove operation
            final List<Overlay> overlays = new LinkedList<Overlay>(sequence.getOverlays());
            // remove overlays from ROI as they are be automatically created from ROI
            for (ROI roi : sequence.getROIs(false))
                overlays.remove(roi.getOverlay());

            // set overlays in the XML node
            Overlay.saveOverlaysToXML(nodeOverlays, overlays);
        }
    }

    private void saveLUTToXML(Node node)
    {
        // save only if we have a custom LUT
        if (sequence.hasUserLUT())
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

    /**
     * Returns <code>true</code> if the specified Document represents a valid XML persistence document.
     */
    public static boolean isValidXMLPersitence(Document doc)
    {
        if (doc == null)
            return false;

        final Element rootNode = XMLUtil.getRootElement(doc);

        return (XMLUtil.getElement(rootNode, Sequence.ID_NAME) != null)
                && (XMLUtil.getElement(rootNode, ID_META) != null) && (XMLUtil.getElement(rootNode, ID_ROIS) != null)
                && (XMLUtil.getElement(rootNode, ID_OVERLAYS) != null);
    }

    /**
     * Returns <code>true</code> if the specified path represents a valid XML persistence file.
     */
    public static boolean isValidXMLPersitence(String path)
    {
        if ((path != null) && FileUtil.exists(path))
        {
            try
            {
                return isValidXMLPersitence(XMLUtil.loadDocument(path, true));
            }
            catch (Exception e)
            {
                // ignore
            }
        }

        return false;
    }
}
