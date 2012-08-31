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
package icy.image.colormap;

import icy.file.xml.XMLPersistent;
import icy.math.Interpolator;
import icy.util.XMLUtil;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class IcyColorMapBand implements XMLPersistent
{
    static final String ID_INDEX = "index";
    static final String ID_VALUE = "value";

    public class ControlPoint implements Comparable<ControlPoint>, XMLPersistent
    {

        private int index;
        int value;
        private final boolean fixed;

        /**
         * @param index
         * @param value
         */
        public ControlPoint(int index, int value, boolean fixed)
        {
            super();

            this.index = index;
            this.value = value;
            this.fixed = fixed;
        }

        /**
         * @param index
         * @param value
         */
        public ControlPoint(int index, int value)
        {
            this(index, value, false);
        }

        /**
         * @return the fixed flag
         */
        public boolean isFixed()
        {
            return fixed;
        }

        /**
         * @return the index
         */
        public int getIndex()
        {
            return index;
        }

        /**
         * @param index
         *        the index to set
         */
        public void setIndex(int index)
        {
            if ((!fixed) && (this.index != index))
            {
                this.index = index;

                changed();
            }
        }

        /**
         * @return the value
         */
        public int getValue()
        {
            return value;
        }

        /**
         * @param value
         *        the value to set
         */
        public void setValue(int value)
        {
            if (this.value != value)
            {
                this.value = value;

                changed();
            }
        }

        /**
         * Set control point position
         * 
         * @param p
         *        point
         */
        public void setPosition(Point p)
        {
            setPosition(p.x, p.y);
        }

        /**
         * Get control point position
         * 
         * @return point position
         */
        public Point getPosition()
        {
            return new Point(index, value);
        }

        /**
         * Set control point position
         * 
         * @param index
         * @param value
         */
        public void setPosition(int index, int value)
        {
            if (((!fixed) && (this.index != index)) || (this.value != value))
            {
                if (!fixed)
                    this.index = index;
                this.value = value;

                changed();
            }
        }

        /**
         * remove the control point
         */
        public void remove()
        {
            if (!fixed)
                removeControlPoint(this);
        }

        /**
         * put here process on changed event
         */
        protected void onChanged()
        {
            // nothing for now

        }

        /**
         * changed event
         */
        protected void changed()
        {
            // common process on change
            onChanged();
            // inform colormap that control point has changed
            controlPointChanged(this);
        }

        @Override
        public int compareTo(ControlPoint o)
        {
            if (index < o.getIndex())
                return -1;
            else if (index > o.getIndex())
                return 1;
            else
                return 0;
        }

        @Override
        public boolean loadFromXML(Node node)
        {
            if (node == null)
                return false;

            final int ind = XMLUtil.getElementIntValue(node, ID_INDEX, 0);
            final int val = XMLUtil.getElementIntValue(node, ID_VALUE, 0);

            setPosition(ind, val);

            return true;
        }

        @Override
        public boolean saveToXML(Node node)
        {
            if (node == null)
                return false;

            XMLUtil.setElementIntValue(node, ID_INDEX, getIndex());
            XMLUtil.setElementIntValue(node, ID_VALUE, getValue());

            return true;
        }
    }

    private static final String ID_RAWDATA = "rawdata";
    private static final String ID_POINT = "point";

    /**
     * parent colormap
     */
    private final IcyColorMap colormap;
    /**
     * list of control point
     */
    protected final ArrayList<ControlPoint> controlPoints;
    /**
     * we use short to store byte to avoid "sign problem"
     */
    public final short[] map;
    /**
     * normalized maps
     */
    public final float[] mapf;

    /**
     * internals
     */
    private int updateCnt;
    private boolean controlPointsChangedPending;
    private boolean mapDataChangedPending;
    private boolean mapFDataChangedPending;
    private boolean rawData;

    public IcyColorMapBand(IcyColorMap colorMap, short initValue)
    {
        super();

        controlPoints = new ArrayList<ControlPoint>();

        this.colormap = colorMap;

        // allocate map
        map = new short[IcyColorMap.SIZE];
        mapf = new float[IcyColorMap.SIZE];

        // default
        Arrays.fill(map, initValue);
        updateFloatMapFromIntMap();

        // add fixed control point to index 0
        controlPoints.add(new ControlPoint(0, map[0], true));
        // add fixed control point to index IcyColorMap.MAX_INDEX
        controlPoints.add(new ControlPoint(IcyColorMap.MAX_INDEX, map[IcyColorMap.MAX_INDEX], true));

        updateCnt = 0;
        controlPointsChangedPending = false;
        mapDataChangedPending = false;
        mapFDataChangedPending = false;
        rawData = false;
    }

    public IcyColorMapBand(IcyColorMap colorMap)
    {
        this(colorMap, (short) 0);
    }

    public int getControlPointCount()
    {
        return controlPoints.size();
    }

    public ArrayList<ControlPoint> getControlPoints()
    {
        return controlPoints;
    }

    /**
     * get the control point
     */
    public ControlPoint getControlPoint(int index)
    {
        return controlPoints.get(index);
    }

    /**
     * get the control point at specified index (return null if not found)
     */
    public ControlPoint getControlPointWithIndex(int index, boolean create)
    {
        // TODO: search can be optimized as the list is sorted on index value
        for (ControlPoint cp : controlPoints)
            if (cp.getIndex() == index)
                return cp;

        if (create)
        {
            final ControlPoint result = new ControlPoint(index, 0, (index == 0) || (index == IcyColorMap.MAX_INDEX));
            // add to list
            controlPoints.add(result);
            // and return
            return result;
        }

        return null;
    }

    /**
     * Return true if there is a control point at specified index
     */
    public boolean hasControlPointWithIndex(int index)
    {
        return getControlPointWithIndex(index, false) != null;
    }

    /**
     * Set a control point to specified index and value (normalized)
     */
    public ControlPoint setControlPoint(int index, float value)
    {
        return setControlPoint(index, (int) (value * IcyColorMap.MAX_LEVEL));
    }

    /**
     * Set a control point to specified index and value
     */
    public ControlPoint setControlPoint(int index, int value)
    {
        // flag to indicate we don't have raw data
        rawData = false;

        // search for an existing control point at this index
        ControlPoint controlPoint = getControlPointWithIndex(index, false);

        // not found ?
        if (controlPoint == null)
        {
            // create a new control point
            controlPoint = new ControlPoint(index, value);
            // and add it to the list
            controlPoints.add(controlPoint);
            // notify point added
            controlPointAdded(controlPoint);
        }
        else
        {
            // modify intensity of control point
            controlPoint.setValue(value);
        }

        return controlPoint;
    }

    /**
     * Remove the specified control point
     * 
     * @param controlPoint
     */
    public void removeControlPoint(ControlPoint controlPoint)
    {
        if (controlPoints.remove(controlPoint))
            controlPointRemoved(controlPoint);
    }

    /**
     * Remove all control point
     */
    public void removeAllControlPoint()
    {
        if (controlPoints.size() <= 2)
            return;

        beginUpdate();
        try
        {
            // more than the 2 fixed controls point ?
            while (controlPoints.size() > 2)
                removeControlPoint(controlPoints.get(1));
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * Copy data from specified source colormap band
     */
    public void copyFrom(IcyColorMapBand source)
    {
        // copy the rawData property
        rawData = source.rawData;

        // we remove all controls points (even fixed ones)
        controlPoints.clear();

        for (ControlPoint cp : source.controlPoints)
            controlPoints.add(new ControlPoint(cp.getIndex(), cp.getValue(), cp.isFixed()));

        // only the 2 fixed controls point ?
        if (controlPoints.size() <= 2)
        {
            // directly copy table data
            System.arraycopy(source.map, 0, map, 0, IcyColorMap.SIZE);
            // notify we changed table data
            mapDataChanged();
        }
        else
            // notify we modified control point
            controlPointsChanged();
    }

    /**
     * Copy data from specified byte array
     */
    public void copyFrom(byte[] src)
    {
        // we remove all controls points (even fixed ones)
        controlPoints.clear();

        final double srcOffsetStep = src.length / IcyColorMap.SIZE;
        double srcOffset = 0;

        // directly copy table data
        for (int dstOffset = 0; dstOffset < IcyColorMap.SIZE; dstOffset++)
        {
            map[dstOffset] = (short) (src[(int) srcOffset] & 0xFF);
            srcOffset += srcOffsetStep;
        }

        // take it as this is a raw map
        rawData = true;
        // rebuild fixed control points
        updateFixedCP();

        // notify we changed table data
        mapDataChanged();
    }

    /**
     * Copy data from specified short array.<br>
     * 
     * @param src
     *        data short array
     * @param shift
     *        shift factor if value need to be shifted (8 if data are short formatted)
     */
    public void copyFrom(short[] src, int shift)
    {
        final byte[] byteMap = new byte[src.length];

        // transform short map to byte map
        for (int i = 0; i < src.length; i++)
            byteMap[i] = (byte) (src[i] >> shift);

        // copy
        copyFrom(byteMap);
    }

    /**
     * Set direct intensity value to specified index
     */
    public void setValue(int index, int value)
    {
        // flag to indicate we have raw data
        rawData = true;

        if (map[index] != value)
        {
            // clear control point as we are manually setting map value
            removeAllControlPoint();

            // set value
            map[index] = (short) value;

            // notify change
            mapDataChanged();
        }
    }

    /**
     * Set direct intensity (normalized) value to specified index
     */
    public void setNormalizedValue(int index, float value)
    {
        // flag to indicate we have raw data
        rawData = true;

        if (mapf[index] != value)
        {
            // clear control point as we are manually setting map value
            removeAllControlPoint();

            // set value
            mapf[index] = value;

            // notify change
            mapFDataChanged();
        }
    }

    /**
     * return intensity for specified index
     */
    public short getIntensity(int index)
    {
        return map[index];
    }

    /**
     * Return true is the color map band is all set to a fixed value.
     */
    public boolean isAllSame()
    {
        final short value = map[0];

        for (int i = 1; i < IcyColorMap.SIZE; i++)
            if (map[i] != value)
                return false;

        return true;
    }

    /**
     * Return true is the color map band is all set to zero.
     */
    public boolean isAllZero()
    {
        for (short value : map)
            if (value != 0)
                return false;

        return true;
    }

    /**
     * Return true is the color map band is all set to one.
     */
    public boolean isAllOne()
    {
        for (short value : map)
            if (value != IcyColorMap.MAX_LEVEL)
                return false;

        return true;
    }

    /**
     * Return true is the color map band is a linear one.<br>
     * Linear map are used to display plain gray or plain color image.<br>
     * Non linear map means you may have an indexed color image or
     * you want to enhance contrast/color in display.
     */
    public boolean isLinear()
    {
        float lastdiff = mapf[1] - mapf[0];

        for (int i = 2; i < IcyColorMap.SIZE; i++)
        {
            final float diff = mapf[i] - mapf[i - 1];

            // important difference in difference
            if ((diff != lastdiff) && (Math.abs(diff / (diff - lastdiff)) < 1000f))
                return false;

            lastdiff = diff;
        }

        return true;
    }

    /**
     * update float map from int map
     */
    private void updateFloatMapFromIntMap()
    {
        for (int i = 0; i < IcyColorMap.SIZE; i++)
            mapf[i] = (float) map[i] / IcyColorMap.MAX_LEVEL;
    }

    /**
     * update float map from int map
     */
    private void updateIntMapFromFloatMap()
    {
        for (int i = 0; i < IcyColorMap.SIZE; i++)
            map[i] = (short) (mapf[i] * IcyColorMap.MAX_LEVEL);
    }

    /**
     * update fixed controls points with map data
     */
    private void updateFixedCP()
    {
        // internal update (no event wanted)
        getControlPointWithIndex(0, true).value = map[0];
        getControlPointWithIndex(IcyColorMap.MAX_INDEX, true).value = map[IcyColorMap.MAX_INDEX];
    }

    /**
     * Called when a control point has been modified
     * 
     * @param controlPoint
     *        modified control point
     */
    public void controlPointChanged(ControlPoint controlPoint)
    {
        controlPointsChanged();
    }

    /**
     * Called when a control point has been added
     * 
     * @param controlPoint
     *        added control point
     */
    public void controlPointAdded(ControlPoint controlPoint)
    {
        controlPointsChanged();
    }

    /**
     * Called when a control point has been removed
     * 
     * @param controlPoint
     *        removed control point
     */
    public void controlPointRemoved(ControlPoint controlPoint)
    {
        controlPointsChanged();
    }

    /**
     * common process on Control Point list change
     */
    public void onControlPointsChanged()
    {
        // sort the list
        Collections.sort(controlPoints);

        final ArrayList<Point> points = new ArrayList<Point>();

        // get position only
        for (ControlPoint point : controlPoints)
            points.add(point.getPosition());

        // get linear interpolation values
        final double[] values = Interpolator.doYLinearInterpolation(points, 1);

        // directly modify the colormap table data
        for (int i = 0; i < IcyColorMap.SIZE; i++)
            map[i] = (short) Math.round(values[i]);

        mapDataChanged();
    }

    /**
     * common process on map (int) data change
     */
    public void onMapDataChanged()
    {
        // update float map from the modified int map
        updateFloatMapFromIntMap();
        // udpate fixed controls points
        updateFixedCP();
        // manually set a changed event as we directly modified the colormap
        colormap.changed();
    }

    /**
     * common process on map (float) data change
     */
    public void onMapFDataChanged()
    {
        // update int map from the modified float map
        updateIntMapFromFloatMap();
        // udpate fixed controls points
        updateFixedCP();
        // manually set a changed event as we directly modified the colormap
        colormap.changed();
    }

    /**
     * called when the controller modified Control Point list
     */
    public void controlPointsChanged()
    {
        if (isUpdating())
        {
            controlPointsChangedPending = true;
            // map will be modified anyway
            mapDataChangedPending = false;
            mapFDataChangedPending = false;
        }
        else
            onControlPointsChanged();
    }

    /**
     * called when the controller directly modified the map (int) data
     */
    public void mapDataChanged()
    {
        if (isUpdating())
        {
            mapDataChangedPending = true;
            // to keep the changed made to map (int)
            mapFDataChangedPending = false;
            controlPointsChangedPending = false;
        }
        else
            onMapDataChanged();
    }

    /**
     * called when the controller directly modified the map (float) data
     */
    public void mapFDataChanged()
    {
        if (isUpdating())
        {
            mapFDataChangedPending = true;
            // to keep the changed made to map (float)
            mapDataChangedPending = false;
            controlPointsChangedPending = false;
        }
        else
            onMapFDataChanged();
    }

    public void beginUpdate()
    {
        updateCnt++;
    }

    public void endUpdate()
    {
        updateCnt--;
        if (updateCnt <= 0)
        {
            // process pending tasks
            if (controlPointsChangedPending)
            {
                onControlPointsChanged();
                controlPointsChangedPending = false;
            }
            else if (mapDataChangedPending)
            {
                onMapDataChanged();
                mapDataChangedPending = false;
            }
            else if (mapFDataChangedPending)
            {
                onMapFDataChanged();
                mapFDataChangedPending = false;
            }
        }
    }

    public boolean isUpdating()
    {
        return updateCnt > 0;
    }

    /**
     * returns true when the LUT is specified by raw data (for example GIF files),
     * false when the LUT is specified by control points.
     */
    public boolean isRawData()
    {
        return rawData;
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        rawData = XMLUtil.getAttributeBooleanValue((Element) node, ID_RAWDATA, false);

        final ArrayList<Node> nodesPoint = XMLUtil.getChildren(node, ID_POINT);

        beginUpdate();
        try
        {
            if (rawData)
            {
                int ind = 0;
                for (Node nodePoint : nodesPoint)
                {
                    final int val = XMLUtil.getElementIntValue(nodePoint, ID_VALUE, 0);

                    setValue(ind, val);
                    ind++;
                }
            }
            else
            {
                removeAllControlPoint();
                for (Node nodePoint : nodesPoint)
                {
                    final int ind = XMLUtil.getElementIntValue(nodePoint, ID_INDEX, 0);
                    final int val = XMLUtil.getElementIntValue(nodePoint, ID_VALUE, 0);

                    setControlPoint(ind, val);
                }
            }
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
        if (node == null)
            return false;

        XMLUtil.setAttributeBooleanValue((Element) node, ID_RAWDATA, rawData);
        XMLUtil.removeChildren(node, ID_POINT);

        boolean result = true;

        if (rawData)
        {
            for (int ind = 0; ind < map.length; ind++)
            {
                final Node nodePoint = XMLUtil.addElement(node, ID_POINT);
                XMLUtil.setElementIntValue(nodePoint, ID_VALUE, map[ind]);
            }
        }
        else
        {
            for (int ind = 0; ind < controlPoints.size(); ind++)
            {
                final ControlPoint cp = controlPoints.get(ind);
                final Node nodePoint = XMLUtil.addElement(node, ID_POINT);

                result = result && cp.saveToXML(nodePoint);
            }
        }

        return result;
    }

}
