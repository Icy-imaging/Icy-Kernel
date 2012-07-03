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
package icy.gui.lut;

import icy.gui.component.BorderedPanel;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.IcyColorMap.IcyColorMapType;
import icy.image.colormap.IcyColorMapBand;
import icy.image.colormap.IcyColorMapBand.ControlPoint;
import icy.image.lut.LUTBand;
import icy.image.lut.LUTBandEvent;
import icy.image.lut.LUTBandEvent.LUTBandEventType;
import icy.image.lut.LUTBandListener;
import icy.util.ColorUtil;
import icy.util.EventUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.EventListenerList;

/**
 * @author stephane
 */
public class ColormapViewer extends BorderedPanel implements MouseListener, MouseMotionListener, LUTBandListener
{
    private enum ActionType
    {
        NULL, MODIFY_CONTROLPOINT
    }

    public interface ColormapPositionListener extends EventListener
    {
        public void positionChanged(int index, int value);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -8338817004756013113L;

    private static final int POINT_SIZE = 10;
    private static final int LINE_SIZE = 3;
    private static final int BORDER_WIDTH = 4;
    private static final int BORDER_HEIGHT = 4;

    private static final int MIN_INDEX = 0;
    private static final int MAX_INDEX = IcyColorMap.MAX_INDEX;
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = IcyColorMap.MAX_LEVEL;

    /**
     * associated LUTBand
     */
    private final LUTBand lutBand;

    /**
     * alpha enabled
     */
    private boolean alphaEnabled;

    /**
     * gui
     */
    private final JPopupMenu menu;

    /**
     * listeners
     */
    private final EventListenerList colorMapPositionListeners;
    /**
     * cached
     */
    final IcyColorMap colormap;
    /**
     * internals
     */
    private float pixToIndexRatio;
    private float indexToPixRatio;
    private float pixToValueRatio;
    private float valueToPixRatio;
    private ActionType action;
    private IcyColorMapBand currentColormapBand;
    private ControlPoint currentControlPoint;

    public ColormapViewer(LUTBand lutBand)
    {
        super();

        // dimension (don't change or you will regret !)
        setMinimumSize(new Dimension(100, 100));
        setPreferredSize(new Dimension(240, 100));
        setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        // faster draw
        setOpaque(true);
        // set border
        setBorder(BorderFactory.createEmptyBorder(BORDER_HEIGHT, BORDER_WIDTH, BORDER_HEIGHT, BORDER_WIDTH));

        this.lutBand = lutBand;
        colormap = lutBand.getColorMap();

        colorMapPositionListeners = new EventListenerList();

        // gui
        menu = new JPopupMenu();

        alphaEnabled = true;

        action = ActionType.NULL;
        currentColormapBand = null;
        currentControlPoint = null;

        // calculate ratios
        updateRatios();

        // add listeners
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        // add listeners
        lutBand.addListener(this);
    }

    @Override
    public void removeNotify()
    {
        super.removeNotify();

        // remove listeners
        lutBand.removeListener(this);
    }

    /**
     * @return the colormap
     */
    public IcyColorMap getColormap()
    {
        return colormap;
    }

    /**
     * Translate index to pixel
     * 
     * @param index
     */
    public int indexToPix(int index)
    {
        final int clientX = getClientX();
        final int pix = (int) (index * indexToPixRatio) + clientX;
        return Math.max(Math.min(pix, getClientWidth() + clientX), clientX);
    }

    /**
     * Translate pixel to index
     * 
     * @param pixel
     */
    public int pixToIndex(int pixel)
    {
        final int ind = (int) ((pixel - getClientX()) * pixToIndexRatio);
        return Math.max(Math.min(ind, MAX_INDEX), MIN_INDEX);
    }

    /**
     * Translate value to pixel
     * 
     * @param value
     * @return pixel for specified value
     */
    public int valueToPix(int value)
    {
        final int clientY = getClientY();
        final int hl = (getClientHeight() - 1) + clientY;
        final int pix = hl - (int) (value * valueToPixRatio);
        return Math.max(Math.min(pix, hl), clientY);
    }

    /**
     * Translate pixel to value
     * 
     * @param pixel
     * @return value for specified pixel
     */
    public int pixToValue(int pixel)
    {
        final int hl = (getClientHeight() - 1) + getClientY();
        final int value = (int) ((hl - pixel) * pixToValueRatio);
        return Math.max(Math.min(value, MAX_VALUE), MIN_VALUE);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        // we do it here as componentResized event occurs after paint (and it is not time consuming)
        updateRatios();

        final Graphics2D g2 = (Graphics2D) g.create();
        final int w = getWidth();
        final int h = getHeight();

        // draw colored background mesh
        for (int i = 0; i < w; i++)
        {
            // get current color from pixel position
            final Color curColor = getColorFromPixel(i);
            final Color grayMixed = ColorUtil.mixOver(Color.gray, curColor);
            final Color whiteMixed = ColorUtil.mixOver(Color.white, curColor);

            for (int j = 0; j < h; j += 16)
            {
                // set graphics color
                if (((i ^ j) & 16) != 0)
                    g2.setColor(grayMixed);
                else
                    g2.setColor(whiteMixed);

                g2.drawLine(i, j, i, j + 15);
            }
        }

        switch (colormap.getType())
        {
            case RGB:
                drawColormapBand(g2, colormap.blue);
                drawColormapBand(g2, colormap.green);
                drawColormapBand(g2, colormap.red);
                break;

            case GRAY:
                drawColormapBand(g2, colormap.gray);
                break;
        }

        if (alphaEnabled)
            drawColormapBand(g2, colormap.alpha);

        g2.setColor(Color.black);
        g2.drawRect(0, 0, w - 1, h - 1);

        g2.dispose();
    }

    private void drawColormapBand(Graphics2D g, IcyColorMapBand band)
    {
        drawColormap(g, band);
        drawControlPoints(g, band);
    }

    private void drawColormap(Graphics2D g, IcyColorMapBand cmb)
    {
        final Graphics2D g2 = (Graphics2D) g.create();

        // enable anti alias for better rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        GeneralPath polyline = null;
        
        if (cmb.isRawData()) // the LUT is defined directly, without control points
        {
            final int x = getClientX();
            final int w = getClientWidth();
            
        	polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, w);
        	
            int intensity = valueToPix(cmb.map[pixToIndex(0)]);
            polyline.moveTo(x, intensity);

            for (int i = x; i < (w + x); i++)
            {
                intensity = valueToPix(cmb.map[pixToIndex(i)]);
                polyline.lineTo(i, intensity);
            }
        }
        else // the LUT is defined through control points, use them.
        {
        	polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, cmb.getControlPointCount());
        	
        	ArrayList<ControlPoint> controlPoints = cmb.getControlPoints();
            int x = getPixelPosX(controlPoints.get(0));
            int y = getPixelPosY(controlPoints.get(0));
            polyline.moveTo(x, y);

            for (int i = 1; i < cmb.getControlPointCount(); i++)
            {
            	x = getPixelPosX(controlPoints.get(i));
                y = getPixelPosY(controlPoints.get(i));    
                polyline.lineTo(x, y);
            }
        }

        if (isFocused(cmb))
            g2.setColor(Color.lightGray);
        else
            g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(LINE_SIZE + 1));
        g2.draw(polyline);

        g2.setColor(getColor(cmb));
        g2.setStroke(new BasicStroke(LINE_SIZE));
        g2.draw(polyline);

        g2.dispose();
    }

    private void drawControlPoints(Graphics2D g, IcyColorMapBand cmb)
    {
        final Graphics2D g2 = (Graphics2D) g.create();

        // enable anti alias for better rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int offset_oval = POINT_SIZE / 2;

        // define color
        final Color color = getColor(cmb);

        for (ControlPoint controlPoint : cmb.getControlPoints())
        {
            final int x = getPixelPosX(controlPoint);
            final int y = getPixelPosY(controlPoint);

            if (controlPoint.isFixed())
            {
                // draw square control point
                g2.setColor(color);
                g2.fillRect(x - (offset_oval - 1), y - (offset_oval - 1), POINT_SIZE - 2, POINT_SIZE - 2);
                g2.setColor(Color.darkGray);
                g2.drawRect(x - (offset_oval - 1), y - (offset_oval - 1), POINT_SIZE - 2, POINT_SIZE - 2);
                if (isFocused(controlPoint))
                    g2.setColor(Color.white);
                else
                    g2.setColor(Color.black);
                g2.drawRect(x - (offset_oval - 0), y - (offset_oval - 0), POINT_SIZE - 0, POINT_SIZE - 0);
            }
            else
            {
                // draw round control point
                g2.setColor(color);
                g2.fillOval(x - (offset_oval - 1), y - (offset_oval - 1), POINT_SIZE - 2, POINT_SIZE - 2);
                g2.setColor(Color.darkGray);
                g2.drawOval(x - (offset_oval - 1), y - (offset_oval - 1), POINT_SIZE - 2, POINT_SIZE - 2);
                if (isFocused(controlPoint))
                    g2.setColor(Color.white);
                else
                    g2.setColor(Color.black);
                g2.drawOval(x - (offset_oval - 0), y - (offset_oval - 0), POINT_SIZE - 0, POINT_SIZE - 0);
            }
        }

        g2.dispose();
    }

    public boolean isAlphaEnabled()
    {
        return alphaEnabled;
    }

    public void setAlphaEnabled(boolean value)
    {
        if (alphaEnabled != value)
        {
            alphaEnabled = value;

            if (!value)
                colormap.alpha.removeAllControlPoint();

            repaint();
        }
    }

    /**
     * set current controller or control point
     */
    public void setCurrentElements(IcyColorMapBand cmb, ControlPoint cp)
    {
        if (currentControlPoint != cp)
        {
            currentControlPoint = cp;
            repaint();
        }

        if (currentColormapBand != cmb)
        {
            currentColormapBand = cmb;
            repaint();
        }

        final int cursor;

        if ((cmb != null) || (cp != null))
            cursor = Cursor.HAND_CURSOR;
        else
            cursor = Cursor.DEFAULT_CURSOR;

        // set cursor only only if different
        if (getCursor().getType() != cursor)
            setCursor(new Cursor(cursor));
    }

    private boolean isFocused(IcyColorMapBand cmb)
    {
        return (cmb != null) && (currentColormapBand == cmb);
    }

    private boolean isFocused(ControlPoint cp)
    {
        return (cp != null) && (currentControlPoint == cp);
    }

    /**
     * return the final color for specified index
     */
    public Color getColor(int index)
    {
        return colormap.getColor(index);
    }

    /**
     * get color of specified band
     */
    public Color getColor(IcyColorMapBand cmb)
    {
        if (cmb == colormap.red)
            return Color.red;
        if (cmb == colormap.green)
            return Color.green;
        if (cmb == colormap.blue)
            return Color.blue;
        if (cmb == colormap.gray)
            return Color.gray;
        if (cmb == colormap.alpha)
            return Color.white;

        return Color.black;
    }

    /**
     * return the final color for specified pixel position
     */
    public Color getColorFromPixel(int pixel)
    {
        return getColor(pixToIndex(pixel));
    }

    /**
     * update ratios for data <--> pix conversion
     */
    private void updateRatios()
    {
        final int w = getClientWidth();
        final int h = getClientHeight();

        if (w <= 0)
        {
            indexToPixRatio = 0f;
            pixToIndexRatio = 0f;
        }
        else
        {
            indexToPixRatio = (float) (w - 1) / (float) (IcyColorMap.SIZE - 1);
            if (indexToPixRatio != 0f)
                pixToIndexRatio = 1f / indexToPixRatio;
            else
                pixToIndexRatio = 0f;
        }

        if (h <= 0)
        {
            valueToPixRatio = 0f;
            pixToValueRatio = 0f;
        }
        else
        {
            valueToPixRatio = (float) (h - 1) / (float) (IcyColorMap.MAX_LEVEL);
            if (valueToPixRatio != 0f)
                pixToValueRatio = 1f / valueToPixRatio;
            else
                pixToValueRatio = 0f;
        }
    }

    // /**
    // * Check if point is over any control point
    // *
    // * @param pos
    // * point
    // * @return boolean
    // */
    // private boolean isOverControlPoint(Point pos)
    // {
    // boolean result = false;
    // final IcyColorMapType type = colormap.getType();
    //
    // // check only if alpha enabled
    // if (alphaEnabled)
    // result = result || isOverControlPoint(colormap.alpha, pos);
    //
    // // test according to display order (ARGB)
    // if (type == IcyColorMapType.RGB)
    // result = result || isOverControlPoint(colormap.red, pos) ||
    // isOverControlPoint(colormap.green, pos)
    // || isOverControlPoint(colormap.blue, pos);
    // if (type == IcyColorMapType.GRAY)
    // result = result || isOverControlPoint(colormap.gray, pos);
    //
    // return result;
    // }

    // /**
    // * Check if point is over any control point from this band
    // *
    // * @param pos
    // * point
    // * @return boolean
    // */
    // private boolean isOverControlPoint(IcyColorMapBand cmb, Point pos)
    // {
    // for (ControlPoint cp : cmb.getControlPoints())
    // if (isOverlapped(cp, pos))
    // return true;
    //
    // return false;
    // }

    /**
     * Check if point is over any point in colormap
     * 
     * @param pos
     *        point
     * @return boolean
     */
    public boolean isOverlapped(IcyColorMapBand cmb, Point pos)
    {
        final int index_min = Math.max(0, pixToIndex(pos.x - LINE_SIZE));
        final int index_max = Math.min(IcyColorMap.MAX_INDEX, pixToIndex(pos.x + LINE_SIZE));

        for (int ind = index_min; ind < index_max; ind++)
            if (Point2D.distance(pos.x, pos.y, indexToPix(ind), valueToPix(cmb.map[ind])) <= (LINE_SIZE + 1))
                return true;

        return false;
    }

    /**
     * Return true if pixel (x, y) is over the control point
     * 
     * @param p
     *        point
     * @return boolean
     */
    public boolean isOverlapped(ControlPoint cp, Point p)
    {
        return getDistance(cp, p) <= (POINT_SIZE / 2);
    }

    /**
     * Return distance between control point and the specified point
     * 
     * @param p
     *        point
     * @return boolean
     */
    public double getDistance(ControlPoint cp, Point p)
    {
        return Point2D.distance(p.x, p.y, indexToPix(cp.getIndex()), valueToPix(cp.getValue()));
    }

    /**
     * Set position from a pixel position
     * 
     * @param x
     * @param y
     */
    public void setPixelPosition(ControlPoint cp, int x, int y)
    {
        cp.setPosition(pixToIndex(x), pixToValue(y));
    }

    /**
     * Get X pixel position
     * 
     * @return X pixel position
     */
    public int getPixelPosX(ControlPoint cp)
    {
        return indexToPix(cp.getIndex());
    }

    /**
     * Get Y pixel position
     * 
     * @return Y pixel position
     */
    public int getPixelPosY(ControlPoint cp)
    {
        return valueToPix(cp.getValue());
    }

    /**
     * Find the overlapped colormap band by specified point
     * 
     * @param pos
     *        point
     * @return ColormapController
     */
    private IcyColorMapBand getOverlappedColormapController(Point pos)
    {
        final IcyColorMapType type = colormap.getType();

        // check only if alpha enabled
        if (alphaEnabled)
            if (isOverlapped(colormap.alpha, pos))
                return colormap.alpha;

        // test according to display order (ARGB)
        if (type == IcyColorMapType.RGB)
        {
            if (isOverlapped(colormap.red, pos))
                return colormap.red;
            if (isOverlapped(colormap.green, pos))
                return colormap.green;
            if (isOverlapped(colormap.blue, pos))
                return colormap.blue;
        }
        if (type == IcyColorMapType.GRAY)
            if (isOverlapped(colormap.gray, pos))
                return colormap.gray;

        return null;
    }

    /**
     * Find the closest overlapped control point by specified point
     * 
     * @param pos
     *        point
     * @return ControlPoint
     */
    private ControlPoint getClosestOverlappedControlPoint(Point pos)
    {
        ControlPoint point;
        final IcyColorMapType type = colormap.getType();

        // check only if alpha enabled
        if (alphaEnabled)
        {
            point = getClosestOverlappedControlPoint(colormap.alpha, pos);
            if (point != null)
                return point;
        }

        // test according to display order (RGBA)
        if (type == IcyColorMapType.RGB)
        {
            point = getClosestOverlappedControlPoint(colormap.red, pos);
            if (point != null)
                return point;
            point = getClosestOverlappedControlPoint(colormap.green, pos);
            if (point != null)
                return point;
            point = getClosestOverlappedControlPoint(colormap.blue, pos);
            if (point != null)
                return point;
        }
        if (type == IcyColorMapType.GRAY)
        {
            point = getClosestOverlappedControlPoint(colormap.gray, pos);
            if (point != null)
                return point;
        }

        return null;
    }

    /**
     * Find the closest overlapped control point by specified point
     * 
     * @param pos
     *        point
     * @return ControlPoint
     */
    private ControlPoint getClosestOverlappedControlPoint(IcyColorMapBand cmb, Point pos)
    {
        final List<ControlPoint> overlapped = new ArrayList<ControlPoint>();

        // add all overlapped control points to the list
        for (ControlPoint point : cmb.getControlPoints())
            if (isOverlapped(point, pos))
                overlapped.add(point);

        final int size = overlapped.size();

        // we have at least one overlapped control point ?
        if (size > 0)
        {
            // find the closest from the specified position
            ControlPoint closestPoint = overlapped.get(0);
            double minDist = getDistance(closestPoint, pos);

            for (int i = 1; i < size; i++)
            {
                final ControlPoint currentPoint = overlapped.get(i);
                final double curDist = getDistance(currentPoint, pos);

                if (curDist < minDist)
                {
                    closestPoint = currentPoint;
                    minDist = curDist;
                }
            }

            return closestPoint;
        }

        return null;
    }

    /**
     * Set a control point to specified index and value
     * 
     * @param pos
     *        position
     */
    ControlPoint setControlPoint(IcyColorMapBand cmb, Point pos)
    {
        return cmb.setControlPoint(pixToIndex(pos.x), pixToValue(pos.y));
    }

    /**
     * show popup menu
     */
    private void showPopupMenu(final Point pos)
    {
        // rebuild menu
        menu.removeAll();

        // keep a copy of current control point
        final ControlPoint cp = currentControlPoint;

        if (cp != null)
        {
            // fixed control point --> no popup menu
            if (cp.isFixed())
                return;

            final JMenuItem removeItem = new JMenuItem("remove (Shift + Click)");

            removeItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // remove the control point
                    cp.remove();
                }
            });

            menu.add(removeItem);
        }
        else
        {
            final IcyColorMapType type = colormap.getType();

            if (type == IcyColorMapType.GRAY)
            {
                final JMenuItem addCPItem = new JMenuItem("add Gray point");

                addCPItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        // add gray control point
                        setControlPoint(colormap.gray, pos);
                    }
                });

                menu.add(addCPItem);
            }
            if (type == IcyColorMapType.RGB)
            {
                final JMenuItem addCRPItem = new JMenuItem("add Red point");
                final JMenuItem addCGPItem = new JMenuItem("add Green point");
                final JMenuItem addCBPItem = new JMenuItem("add Blue point");

                addCRPItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        // add red control point
                        setControlPoint(colormap.red, pos);
                    }
                });
                addCGPItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        // add green control point
                        setControlPoint(colormap.green, pos);
                    }
                });
                addCBPItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        // add blue control point
                        setControlPoint(colormap.blue, pos);
                    }
                });

                menu.add(addCRPItem);
                menu.add(addCGPItem);
                menu.add(addCBPItem);
            }

            if (alphaEnabled)
            {
                final JMenuItem addAlphaCPItem = new JMenuItem("add Alpha point");

                addAlphaCPItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        // add alpha control point
                        setControlPoint(colormap.alpha, pos);
                    }
                });

                menu.add(addAlphaCPItem);
            }
        }

        menu.pack();
        menu.validate();

        // display menu
        menu.show(this, pos.x, pos.y);
    }

    /**
     * update current controller and control point from mouse position
     */
    private void updateCurrentElements(Point pos)
    {
        final IcyColorMapBand cmb;
        // by default we search for an overlapped control point
        final ControlPoint cp = getClosestOverlappedControlPoint(pos);

        // if no overlapped control point we search for overlapped controller
        if (cp == null)
            cmb = getOverlappedColormapController(pos);
        else
            cmb = null;

        // define current elements
        setCurrentElements(cmb, cp);
    }

    /**
     * update colormap position info
     */
    private void updateColormapPositionInfo(Point pos)
    {
        final ControlPoint cp;
        final int index;
        final int value;

        if (action != ActionType.NULL)
            cp = currentControlPoint;
        else
            cp = getClosestOverlappedControlPoint(pos);

        if (cp != null)
        {
            index = cp.getIndex();
            value = cp.getValue();
        }
        else
        {
            index = pixToIndex(pos.x);
            value = pixToValue(pos.y);
        }

        colormapPositionChanged(index, value);
    }

    /**
     * process on colormap change
     */
    public void onColormapChanged()
    {
        // repaint the colormap
        repaint();
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addColormapPositionListener(ColormapPositionListener listener)
    {
        colorMapPositionListeners.add(ColormapPositionListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeColormapPositionListener(ColormapPositionListener listener)
    {
        colorMapPositionListeners.remove(ColormapPositionListener.class, listener);
    }

    /**
     * mouse position on colormap info changed
     */
    public void colormapPositionChanged(int index, int value)
    {
        for (ColormapPositionListener listener : colorMapPositionListeners.getListeners(ColormapPositionListener.class))
            listener.positionChanged(index, value);
    }

    @Override
    public void lutBandChanged(LUTBandEvent e)
    {
        if (e.getType() == LUTBandEventType.COLORMAP_CHANGED)
            onColormapChanged();
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        // nothing to do here
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        // nothing to do here
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        // clear position info
        colormapPositionChanged(-1, -1);
        // unfocus if no action
        if (action == ActionType.NULL)
            setCurrentElements(null, null);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        final Point pos = e.getPoint();

        if (EventUtil.isLeftMouseButton(e))
        {
            // we have a selected control point ?
            if (currentControlPoint != null)
            {
                // Shift pressed --> remove control point
                if (EventUtil.isShiftDown(e))
                    currentControlPoint.remove();
                // else we start modification
                else
                    action = ActionType.MODIFY_CONTROLPOINT;
            }
            // we have a selected controller ?
            else if (currentColormapBand != null)
            {
                action = ActionType.MODIFY_CONTROLPOINT;
                // add a new control point to the controller which become the active control point
                setCurrentElements(null, setControlPoint(currentColormapBand, pos));
            }
        }
        else if (EventUtil.isRightMouseButton(e))
        {
            showPopupMenu(pos);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (EventUtil.isLeftMouseButton(e))
        {
            action = ActionType.NULL;
            updateCurrentElements(e.getPoint());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        final Point pos = e.getPoint();

        switch (action)
        {
            case MODIFY_CONTROLPOINT:
                setPixelPosition(currentControlPoint, pos.x, pos.y);
                break;
        }

        updateColormapPositionInfo(pos);
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        final Point pos = e.getPoint();

        updateCurrentElements(pos);
        updateColormapPositionInfo(pos);
    }

}
