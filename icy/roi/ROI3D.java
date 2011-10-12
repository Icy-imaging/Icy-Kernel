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
public abstract class ROI3D extends ROI
{
    /**
     * Return ROI3D of ROI list
     */
    public static ArrayList<ROI3D> getROI3DList(ArrayList<ROI> rois)
    {
        final ArrayList<ROI3D> result = new ArrayList<ROI3D>();

        for (ROI roi : rois)
            if (roi instanceof ROI3D)
                result.add((ROI3D) roi);

        return result;
    }

    public static final String ID_T = "t";
    public static final String ID_C = "c";

    /**
     * t coordinate attachment
     */
    protected int t;
    /**
     * c coordinate attachment
     */
    protected int c;

    public ROI3D()
    {
        super();

        // by default we consider no specific T and C attachment
        t = -1;
        c = -1;
    }

    /**
     * @return the t
     */
    public int getT()
    {
        return t;
    }

    /**
     * @param value
     *        the t to set
     */
    public void setT(int value)
    {
        if (t != value)
        {
            t = value;
            roiChanged();
        }
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
     * It internally uses the current canvas T, C coordinates
     */
    public boolean isActiveFor(IcyCanvas canvas)
    {
        return isActiveFor(canvas.getPositionT(), canvas.getPositionC());
    }

    /**
     * Return true if the ROI is active for the specified T, C coordinates
     */
    public boolean isActiveFor(int t, int c)
    {
        return isActiveForT(t) && isActiveForC(c);
    }

    /**
     * Return true if the ROI is active for the specified T coordinate
     */
    public boolean isActiveForT(int t)
    {
        return (this.t == -1) || (this.t == t);
    }

    /**
     * Return true if the ROI is active for the specified C coordinate
     */
    public boolean isActiveForC(int c)
    {
        return (this.c == -1) || (this.c == c);
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            setT(XMLUtil.getElementIntValue(node, ID_T, -1));
            setC(XMLUtil.getElementIntValue(node, ID_C, -1));
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (!super.saveToXML(node))
            return false;

        XMLUtil.setElementIntValue(node, ID_T, getT());
        XMLUtil.setElementIntValue(node, ID_C, getC());

        return true;
    }

}
