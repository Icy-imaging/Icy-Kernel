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
package icy.roi;

import icy.canvas.IcyCanvas;
import icy.util.XMLUtil;

import java.util.ArrayList;

import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public abstract class ROI4D extends ROI
{
    /**
     * Return ROI4D of ROI list
     */
    public static ArrayList<ROI4D> getROI4DList(ArrayList<ROI> rois)
    {
        final ArrayList<ROI4D> result = new ArrayList<ROI4D>();

        for (ROI roi : rois)
            if (roi instanceof ROI4D)
                result.add((ROI4D) roi);

        return result;
    }

    public static final String ID_C = "c";

    /**
     * c coordinate attachment
     */
    protected int c;

    public ROI4D()
    {
        super();

        // by default we consider no specific C attachment
        c = -1;
    }

    /**
     * @return the c
     */
    public int getC()
    {
        return c;
    }

    /**
     * @param value
     *        the c to set
     */
    public void setC(int value)
    {
        if (c != value)
        {
            c = value;
            roiChanged();
        }
    }

    /**
     * Return true if the ROI is active for the specified canvas.<br>
     * It internally uses the current canvas C coordinate
     */
    public boolean isActiveFor(IcyCanvas canvas)
    {
        return isActiveFor(canvas.getC());
    }

    /**
     * Return true if the ROI is active for the specified coordinate
     */
    public boolean isActiveFor(int c)
    {
        return isActiveForC(c);
    }

    /**
     * Return true if the ROI is active for the specified C coordinate
     */
    public boolean isActiveForC(int c)
    {
        return (this.c == -1) || (this.c == c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI#loadFromXML(org.w3c.dom.Node)
     */
    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            setC(XMLUtil.getElementIntValue(node, ID_C, -1));
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see icy.roi.ROI#saveToXML(org.w3c.dom.Node)
     */
    @Override
    public boolean saveToXML(Node node)
    {
        if (!super.saveToXML(node))
            return false;

        XMLUtil.setElementIntValue(node, ID_C, getC());

        return true;
    }

}
