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
package icy.roi;

import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.type.point.Point4D;
import icy.type.rectangle.Rectangle4D;
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

    @Override
    final public int getDimension()
    {
        return 4;
    }

    /**
     * Returns the bounding box of the <code>ROI</code>. Note that there is no guarantee that the
     * returned {@link Rectangle4D} is the smallest bounding box that encloses the <code>ROI</code>,
     * only that the <code>ROI</code> lies entirely within the indicated <code>Rectangle4D</code>.
     * 
     * @return an instance of <code>Rectangle4D</code> that is a bounding box of the
     *         <code>ROI</code>.
     */
    public abstract Rectangle4D getBounds4D();

    /**
     * Returns the top left corner of the ROI bounds.<br>
     * 
     * @see #getBounds4D()
     */
    public Point4D getPosition4D()
    {
        return getBounds4D().getPosition();
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
     * It internally uses the current canvas C coordinate and the visible state of the
     * attached layer.
     */
    public boolean isActiveFor(IcyCanvas canvas)
    {
        if (!canvas.isLayersVisible())
            return false;

        final Layer layer = canvas.getLayer(painter);

        if ((layer != null) && layer.isVisible())
            return isActiveFor(canvas.getPositionC());

        return false;
    }

    /**
     * Return true if the ROI is active for the specified C coordinate
     */
    public boolean isActiveFor(int c)
    {
        return (this.c == -1) || (c == -1) || (this.c == c);
    }

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

    @Override
    public boolean saveToXML(Node node)
    {
        if (!super.saveToXML(node))
            return false;

        XMLUtil.setElementIntValue(node, ID_C, getC());

        return true;
    }

}
