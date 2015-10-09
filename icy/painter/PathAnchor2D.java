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
package icy.painter;

import icy.sequence.Sequence;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import org.w3c.dom.Node;

/**
 * Anchor for path type shape.<br>
 * Support extra coordinate to store curve informations.
 * 
 * @author Stephane
 */
public class PathAnchor2D extends Anchor2D
{
    private static final String ID_POS_CEXT_X = "pos_ext1_x";
    private static final String ID_POS_CEXT_Y = "pos_ext1_y";
    private static final String ID_POS_QEXT_X = "pos_ext2_x";
    private static final String ID_POS_QEXT_Y = "pos_ext2_y";
    private static final String ID_TYPE = "type";

    /**
     * Curve extra coordinates
     */
    private final Point2D.Double posCExt;
    /**
     * Quad extra coordinates
     */
    private final Point2D.Double posQExt;
    /**
     * anchor type (used as PathIterator type)
     */
    private int type;

    public PathAnchor2D(double x1, double y1, double x2, double y2, double x3, double y3, int ray, Color color,
            Color selectedColor, int type)
    {
        super(x3, y3, ray, color, selectedColor);

        posCExt = new Point2D.Double(x1, y1);
        posQExt = new Point2D.Double(x2, y2);
        this.type = type;
    }

    public PathAnchor2D(double x1, double y1, double x2, double y2, double x3, double y3, int ray, Color color,
            Color selectedColor)
    {
        this(x1, y1, x2, y2, x3, y3, ray, color, selectedColor, -1);
    }

    /**
     * @deprecated Uses {@link #PathAnchor2D(double, double, double, double, double, double, int, Color, Color)}
     *             instead.
     */
    @Deprecated
    public PathAnchor2D(Sequence sequence, double x1, double y1, double x2, double y2, double x3, double y3, int ray,
            Color color, Color selectedColor)
    {
        this(x1, y1, x2, y2, x3, y3, ray, color, selectedColor, -1);

        if (sequence != null)
            sequence.addOverlay(this);
    }

    public PathAnchor2D(double x1, double y1, double x2, double y2, double x3, double y3, Color color,
            Color selectedColor)
    {
        this(x1, y1, x2, y2, x3, y3, DEFAULT_RAY, color, selectedColor, PathIterator.SEG_CUBICTO);
    }

    public PathAnchor2D(double x1, double y1, double x2, double y2, double x3, double y3)
    {
        this(x1, y1, x2, y2, x3, y3, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR,
                PathIterator.SEG_CUBICTO);
    }

    public PathAnchor2D(double x1, double y1, double x2, double y2, Color color, Color selectedColor)
    {
        this(0d, 0d, x1, y1, x2, y2, DEFAULT_RAY, color, selectedColor, PathIterator.SEG_QUADTO);
    }

    public PathAnchor2D(double x1, double y1, double x2, double y2)
    {
        this(0d, 0d, x1, y1, x2, y2, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR, PathIterator.SEG_QUADTO);
    }

    public PathAnchor2D(double x1, double y1, Color color, Color selectedColor, int type)
    {
        this(0d, 0d, 0d, 0d, x1, y1, DEFAULT_RAY, color, selectedColor, type);
    }

    public PathAnchor2D(double x1, double y1, Color color, Color selectedColor)
    {
        this(0d, 0d, 0d, 0d, x1, y1, DEFAULT_RAY, color, selectedColor, PathIterator.SEG_LINETO);
    }

    public PathAnchor2D(double x1, double y1, int type)
    {
        this(0d, 0d, 0d, 0d, x1, y1, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR, type);
    }

    public PathAnchor2D(double x1, double y1)
    {
        this(0d, 0d, 0d, 0d, x1, y1, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR, PathIterator.SEG_MOVETO);
    }

    public PathAnchor2D()
    {
        this(0d, 0d, 0d, 0d, 0, 0, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR, PathIterator.SEG_MOVETO);
    }

    /**
     * @return the type
     */
    public int getType()
    {
        return type;
    }

    /**
     * Set the type of this segment
     * 
     * @see PathIterator
     * @param value
     *        the type to set
     */
    public void setType(int value)
    {
        if (type != value)
        {
            type = value;
            painterChanged();
        }
    }

    /**
     * @return the posQExt
     */
    public Point2D.Double getPosQExt()
    {
        return posQExt;
    }

    /**
     * @return the posCExt
     */
    public Point2D.Double getPosCExt()
    {
        return posCExt;
    }

    public void setPosQExt(Point2D p)
    {
        setPosQExt(p.getX(), p.getY());
    }

    public void setPosQExt(double x, double y)
    {
        if ((posQExt.x != x) || (posQExt.y != y))
        {
            posQExt.x = x;
            posQExt.y = y;

            positionChanged();
            painterChanged();
        }
    }

    public void setPosCExt(Point2D p)
    {
        setPosCExt(p.getX(), p.getY());
    }

    public void setPosCExt(double x, double y)
    {
        if ((posCExt.x != x) || (posCExt.y != y))
        {
            posCExt.x = x;
            posCExt.y = y;

            positionChanged();
            painterChanged();
        }
    }

    /**
     * @return the PosQExt.x
     */
    public double getPosQExtX()
    {
        return posQExt.x;
    }

    /**
     * @param x
     *        the PosQExt.x to set
     */
    public void setPosQExtX(double x)
    {
        setPosQExt(x, posQExt.y);
    }

    /**
     * @return the PosQExt.y
     */
    public double getPosQExtY()
    {
        return posQExt.y;
    }

    /**
     * @param y
     *        the PosQExt.y to set
     */
    public void setPosQExtY(double y)
    {
        setPosQExt(posQExt.x, y);
    }

    /**
     * @return the PosCExt.x
     */
    public double getPosCExtX()
    {
        return posCExt.x;
    }

    /**
     * @param x
     *        the PosCExt.x to set
     */
    public void setPosCExtX(double x)
    {
        setPosCExt(x, posCExt.y);
    }

    /**
     * @return the PosCExt.y
     */
    public double getPosCExtY()
    {
        return posCExt.y;
    }

    /**
     * @param y
     *        the PosCExt.y to set
     */
    public void setPosCExtY(double y)
    {
        setPosCExt(posCExt.x, y);
    }

    @Override
    public void translate(double dx, double dy)
    {
        beginUpdate();
        try
        {
            super.translate(dx, dy);

            setPosCExt(posCExt.x + dx, posCExt.y + dy);
            setPosQExt(posQExt.x + dx, posQExt.y + dy);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public boolean loadPositionFromXML(Node node)
    {
        if (node == null)
            return false;

        beginUpdate();
        try
        {
            super.loadPositionFromXML(node);

            setPosCExtX(XMLUtil.getElementDoubleValue(node, ID_POS_CEXT_X, 0d));
            setPosCExtY(XMLUtil.getElementDoubleValue(node, ID_POS_CEXT_Y, 0d));
            setPosQExtX(XMLUtil.getElementDoubleValue(node, ID_POS_QEXT_X, 0d));
            setPosQExtY(XMLUtil.getElementDoubleValue(node, ID_POS_QEXT_Y, 0d));
            setType(XMLUtil.getElementIntValue(node, ID_TYPE, -1));
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        beginUpdate();
        try
        {
            super.loadFromXML(node);

            setPosCExtX(XMLUtil.getElementDoubleValue(node, ID_POS_CEXT_X, 0d));
            setPosCExtY(XMLUtil.getElementDoubleValue(node, ID_POS_CEXT_Y, 0d));
            setPosQExtX(XMLUtil.getElementDoubleValue(node, ID_POS_QEXT_X, 0d));
            setPosQExtY(XMLUtil.getElementDoubleValue(node, ID_POS_QEXT_Y, 0d));
            setType(XMLUtil.getElementIntValue(node, ID_TYPE, -1));
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean savePositionToXML(Node node)
    {
        if (node == null)
            return false;

        super.savePositionToXML(node);

        XMLUtil.setElementDoubleValue(node, ID_POS_CEXT_X, getPosCExtX());
        XMLUtil.setElementDoubleValue(node, ID_POS_CEXT_Y, getPosCExtY());
        XMLUtil.setElementDoubleValue(node, ID_POS_QEXT_X, getPosQExtX());
        XMLUtil.setElementDoubleValue(node, ID_POS_QEXT_Y, getPosQExtY());
        XMLUtil.setElementIntValue(node, ID_TYPE, getType());

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (node == null)
            return false;

        super.saveToXML(node);

        XMLUtil.setElementDoubleValue(node, ID_POS_CEXT_X, getPosCExtX());
        XMLUtil.setElementDoubleValue(node, ID_POS_CEXT_Y, getPosCExtY());
        XMLUtil.setElementDoubleValue(node, ID_POS_QEXT_X, getPosQExtX());
        XMLUtil.setElementDoubleValue(node, ID_POS_QEXT_Y, getPosQExtY());
        XMLUtil.setElementIntValue(node, ID_TYPE, getType());

        return true;
    }
}
