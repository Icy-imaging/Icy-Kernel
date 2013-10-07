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
package icy.painter;

import icy.canvas.Canvas3D;
import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.common.EventHierarchicalChecker;
import icy.file.xml.XMLPersistent;
import icy.painter.OverlayEvent.OverlayEventType;
import icy.painter.PainterEvent.PainterEventType;
import icy.sequence.Sequence;
import icy.type.point.Point5D;
import icy.util.EventUtil;
import icy.util.ShapeUtil;
import icy.util.XMLUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public class Anchor2D extends Overlay implements XMLPersistent
{
    @SuppressWarnings("deprecation")
    public static interface Anchor2DListener extends PainterListener
    {
        public void positionChanged(Anchor2D source);
    }

    public static class Anchor2DEvent implements EventHierarchicalChecker
    {
        private final Anchor2D source;

        public Anchor2DEvent(Anchor2D source)
        {
            super();

            this.source = source;
        }

        /**
         * @return the source
         */
        public Anchor2D getSource()
        {
            return source;
        }

        @Override
        public boolean isEventRedundantWith(EventHierarchicalChecker event)
        {
            if (event instanceof Anchor2DEvent)
                return ((Anchor2DEvent) event).getSource() == source;

            return false;
        }
    }

    protected static final String ID_COLOR = "color";
    protected static final String ID_SELECTEDCOLOR = "selected_color";
    protected static final String ID_SELECTED = "selected";
    protected static final String ID_POS_X = "pos_x";
    protected static final String ID_POS_Y = "pos_y";
    protected static final String ID_RAY = "ray";
    protected static final String ID_VISIBLE = "visible";

    protected static final int DEFAULT_RAY = 6;
    protected static final Color DEFAULT_NORMAL_COLOR = Color.YELLOW;
    protected static final Color DEFAULT_SELECTED_COLOR = Color.WHITE;

    /**
     * position (canvas)
     */
    protected final Point2D.Double position;
    /**
     * radius
     */
    protected int ray;

    /**
     * color
     */
    protected Color color;
    /**
     * selection color
     */
    protected Color selectedColor;
    /**
     * selection flag
     */
    protected boolean selected;
    /**
     * flag that indicate if anchor is visible
     */
    protected boolean visible;

    /**
     * internals
     */
    protected final Ellipse2D ellipse;
    protected Point2D startDragMousePosition;
    protected Point2D startDragPainterPosition;

    public Anchor2D(double x, double y, int ray, Color color, Color selectedColor)
    {
        super("Anchor", OverlayPriority.SHAPE_NORMAL);

        position = new Point2D.Double(x, y);
        this.ray = ray;
        this.color = color;
        this.selectedColor = selectedColor;
        selected = false;
        visible = true;

        ellipse = new Ellipse2D.Double();
        startDragMousePosition = null;
        startDragPainterPosition = null;
    }

    public Anchor2D(double x, double y, int ray, Color color)
    {
        this(x, y, ray, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param x
     * @param y
     * @param ray
     */
    public Anchor2D(double x, double y, int ray)
    {
        this(x, y, ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param x
     * @param y
     * @param color
     */
    public Anchor2D(double x, double y, Color color, Color selectedColor)
    {
        this(x, y, DEFAULT_RAY, color, selectedColor);
    }

    /**
     * @param x
     * @param y
     * @param color
     */
    public Anchor2D(double x, double y, Color color)
    {
        this(x, y, DEFAULT_RAY, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @param x
     * @param y
     */
    public Anchor2D(double x, double y)
    {
        this(x, y, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     */
    public Anchor2D()
    {
        this(0d, 0d, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @deprecated Use {@link #Anchor2D(double, double, int, Color, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Point2D position, int ray, Color color, Color selectedColor)
    {
        this(position.getX(), position.getY(), ray, color, selectedColor);
    }

    /**
     * @deprecated Use {@link #Anchor2D(double, double, int, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Point2D position, int ray, Color color)
    {
        this(position.getX(), position.getY(), ray, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @deprecated Use {@link #Anchor2D(double, double, int)} instead.
     */
    @Deprecated
    public Anchor2D(Point2D position, int ray)
    {
        this(position.getX(), position.getY(), ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @deprecated Use {@link #Anchor2D(double, double, Color, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Point2D position, Color color, Color selectedColor)
    {
        this(position.getX(), position.getY(), DEFAULT_RAY, color, selectedColor);
    }

    /**
     * @deprecated Use {@link #Anchor2D(double, double, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Point2D position, Color color)
    {
        this(position.getX(), position.getY(), DEFAULT_RAY, color, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @deprecated Use {@link #Anchor2D(double, double)} instead.
     */
    @Deprecated
    public Anchor2D(Point2D position)
    {
        this(position.getX(), position.getY(), DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, int, Color, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, double x, double y, int ray, Color color, Color selectedColor)
    {
        this(x, y, ray, color, selectedColor);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, int, Color, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, Point2D position, int ray, Color color, Color selectedColor)
    {
        this(position.getX(), position.getY(), ray, color, selectedColor);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(Point2D, int, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, Point2D position, int ray, Color color)
    {
        this(position.getX(), position.getY(), ray, color, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, int)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, double x, double y, int ray)
    {
        this(x, y, ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, Point2D position, Color color)
    {
        this(position.getX(), position.getY(), DEFAULT_RAY, color, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, int)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, Point2D position, int ray)
    {
        this(position.getX(), position.getY(), ray, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, int)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, double x, double y, Color color, Color selectedColor)
    {
        this(x, y, DEFAULT_RAY, color, selectedColor);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, Color, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, Point2D position, Color color, Color selectedColor)
    {
        this(position.getX(), position.getY(), DEFAULT_RAY, color, selectedColor);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double, Color, Color)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, double x, double y, Color color)
    {
        this(x, y, DEFAULT_RAY, color, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, double x, double y)
    {
        this(sequence, x, y, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D(double, double)} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence, Point2D position)
    {
        this(position.getX(), position.getY(), DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @deprecated Use {@link Anchor2D#Anchor2D()} instead.
     */
    @Deprecated
    public Anchor2D(Sequence sequence)
    {
        this(0d, 0d, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR);
        sequence.addOverlay(this);
    }

    /**
     * @return the x
     */
    public double getX()
    {
        return position.x;
    }

    /**
     * @param x
     *        the x to set
     */
    public void setX(double x)
    {
        setPosition(x, position.y);
    }

    /**
     * @return the y
     */
    public double getY()
    {
        return position.y;
    }

    /**
     * @param y
     *        the y to set
     */
    public void setY(double y)
    {
        setPosition(position.x, y);
    }

    public Point2D getPosition()
    {
        return new Point2D.Double(position.x, position.y);
    }

    public double getPositionX()
    {
        return position.x;
    }

    public double getPositionY()
    {
        return position.y;
    }

    public void moveTo(Point2D p)
    {
        setPosition(p.getX(), p.getY());
    }

    public void moveTo(double x, double y)
    {
        setPosition(x, y);
    }

    public void setPosition(Point2D p)
    {
        setPosition(p.getX(), p.getY());
    }

    public void setPosition(double x, double y)
    {
        if ((position.x != x) || (position.y != y))
        {
            position.x = x;
            position.y = y;

            positionChanged();
            painterChanged();
        }
    }

    public void translate(double dx, double dy)
    {
        setPosition(position.x + dx, position.y + dy);
    }

    /**
     * @return the ray
     */
    public int getRay()
    {
        return ray;
    }

    /**
     * @param value
     *        the ray to set
     */
    public void setRay(int value)
    {
        if (ray != value)
        {
            ray = value;
            painterChanged();
        }
    }

    /**
     * @return the color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * @param value
     *        the color to set
     */
    public void setColor(Color value)
    {
        if (color != value)
        {
            color = value;
            painterChanged();
        }
    }

    /**
     * @return the selectedColor
     */
    public Color getSelectedColor()
    {
        return selectedColor;
    }

    /**
     * @param value
     *        the selectedColor to set
     */
    public void setSelectedColor(Color value)
    {
        if (selectedColor != value)
        {
            selectedColor = value;
            painterChanged();
        }
    }

    /**
     * @return the selected
     */
    public boolean isSelected()
    {
        return selected;
    }

    /**
     * @param value
     *        the selected to set
     */
    public void setSelected(boolean value)
    {
        if (selected != value)
        {
            selected = value;

            // end drag
            if (!value)
                startDragMousePosition = null;

            painterChanged();
        }
    }

    // /**
    // * @return the canRemove
    // */
    // public boolean isCanRemove()
    // {
    // return canRemove;
    // }
    //
    // /**
    // * @param value
    // * the canRemove to set
    // */
    // public void setCanRemove(boolean value)
    // {
    // canRemove = value;
    // }

    /**
     * @return the visible
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * @param value
     *        the visible to set
     */
    public void setVisible(boolean value)
    {
        if (visible != value)
        {
            visible = value;
            painterChanged();
        }
    }

    public boolean isOver(IcyCanvas canvas, Point2D p)
    {
        if (p == null)
            return false;
        
        return isOver(canvas, p.getX(), p.getY());
    }

    public boolean isOver(IcyCanvas canvas, double x, double y)
    {
        updateEllipse(canvas);
        return ellipse.contains(x, y);
    }

    protected double getAdjRay(IcyCanvas canvas)
    {
        return Math.max(canvas.canvasToImageLogDeltaX(ray), canvas.canvasToImageLogDeltaY(ray));
    }

    private void updateEllipse(IcyCanvas canvas)
    {
        final double adjRayX = canvas.canvasToImageLogDeltaX(ray);
        final double adjRayY = canvas.canvasToImageLogDeltaY(ray);

        ellipse.setFrame(position.x - adjRayX, position.y - adjRayY, adjRayX * 2, adjRayY * 2);
    }

    protected boolean updateDrag(InputEvent e, double x, double y)
    {
        // not dragging --> exit
        if (startDragMousePosition == null)
            return false;

        double dx = x - startDragMousePosition.getX();
        double dy = y - startDragMousePosition.getY();

        // shift action --> limit to one direction
        if (EventUtil.isShiftDown(e))
        {
            // X drag
            if (Math.abs(dx) > Math.abs(dy))
                dy = 0;
            // Y drag
            else
                dx = 0;
        }

        // set new position
        setPosition(new Point2D.Double(startDragPainterPosition.getX() + dx, startDragPainterPosition.getY() + dy));

        return true;
    }

    protected boolean updateDrag(InputEvent e, Point2D pt)
    {
        return updateDrag(e, pt.getX(), pt.getY());
    }

    /**
     * called when anchor position has changed
     */
    protected void positionChanged()
    {
        updater.changed(new Anchor2DEvent(this));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onChanged(EventHierarchicalChecker object)
    {
        if (object instanceof Anchor2DEvent)
        {
            firePositionChangedEvent(((Anchor2DEvent) object).getSource());
            return;
        }

        // provide event backward compatibility
        if (object instanceof OverlayEvent)
        {
            final OverlayEvent event = (OverlayEvent) object;

            if (event.getType() == OverlayEventType.PAINTER_CHANGED)
            {
                final PainterEvent pe = new PainterEvent(this, PainterEventType.PAINTER_CHANGED);

                for (Anchor2DListener listener : listeners.getListeners(Anchor2DListener.class))
                    listener.painterChanged(pe);
            }
        }

        super.onChanged(object);
    }

    protected void firePositionChangedEvent(Anchor2D source)
    {
        for (Anchor2DListener listener : listeners.getListeners(Anchor2DListener.class))
            listener.positionChanged(source);
    }

    public void addAnchorListener(Anchor2DListener listener)
    {
        listeners.add(Anchor2DListener.class, listener);
    }

    public void removeAnchorListener(Anchor2DListener listener)
    {
        listeners.remove(Anchor2DListener.class, listener);
    }

    /**
     * @deprecated Use {@link #addAnchorListener(Anchor2DListener)} instead.
     */
    @Deprecated
    public void addListener(Anchor2DListener listener)
    {
        addAnchorListener(listener);
    }

    /**
     * @deprecated Use {@link #removeAnchorListener(Anchor2DListener)} instead.
     */
    @Deprecated
    public void removeListener(Anchor2DListener listener)
    {
        removeAnchorListener(listener);
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        // canvas3D not handled here
        if (canvas instanceof IcyCanvas2D)
        {
            updateEllipse(canvas);

            // trivial paint optimization
            final boolean shapeVisible = ShapeUtil.isVisible(g, ellipse);

            if (shapeVisible)
            {
                final Graphics2D g2 = (Graphics2D) g.create();

                // draw content
                if (isSelected())
                    g2.setColor(getSelectedColor());
                else
                    g2.setColor(getColor());
                g2.fill(ellipse);

                // draw black border
                g2.setStroke(new BasicStroke((float) (getAdjRay(canvas) / 8f)));
                g2.setColor(Color.black);
                g2.draw(ellipse);

                g2.dispose();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        // canvas3D not handled here
        if (canvas instanceof Canvas3D)
            return;
        // no image position --> exit
        if (imagePoint == null)
            return;

        // just for the shift key state change
        updateDrag(e, imagePoint.x, imagePoint.y);
    }

    @Override
    public void keyReleased(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        // canvas3D not handled here
        if (canvas instanceof Canvas3D)
            return;
        // no image position --> exit
        if (imagePoint == null)
            return;

        // just for the shift key state change
        updateDrag(e, imagePoint.x, imagePoint.y);
    }

    @Override
    public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        if (e.isConsumed())
            return;

        // canvas3D not handled here
        if (canvas instanceof Canvas3D)
            return;
        // no image position --> exit
        if (imagePoint == null)
            return;

        if (EventUtil.isLeftMouseButton(e))
        {
            // consume event to activate drag
            if (isSelected())
            {
                startDragMousePosition = imagePoint.toPoint2D();
                startDragPainterPosition = getPosition();
                e.consume();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        startDragMousePosition = null;
    }

    @Override
    public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        if (e.isConsumed())
            return;

        // canvas3D not handled here
        if (canvas instanceof Canvas3D)
            return;
        // no image position --> exit
        if (imagePoint == null)
            return;

        if (EventUtil.isLeftMouseButton(e))
        {
            // if selected then move according to mouse position
            if (isSelected())
            {
                // force start drag if not already the case
                if (startDragMousePosition == null)
                {
                    startDragMousePosition = getPosition();
                    startDragPainterPosition = getPosition();
                }

                updateDrag(e, imagePoint.x, imagePoint.y);

                e.consume();
            }
        }
    }

    @Override
    public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
    {
        if (!isVisible())
            return;

        // canvas3D not handled here
        if (canvas instanceof Canvas3D)
            return;
        // no image position --> exit
        if (imagePoint == null)
            return;

        // already consumed, no selection possible
        if (e.isConsumed())
            setSelected(false);
        else
        {
            final boolean overlapped = isOver(canvas, imagePoint.x, imagePoint.y);

            setSelected(overlapped);

            // so we can only have one selected at once
            if (overlapped)
                e.consume();
        }
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        beginUpdate();
        try
        {
            setColor(new Color(XMLUtil.getElementIntValue(node, ID_COLOR, DEFAULT_NORMAL_COLOR.getRGB())));
            setSelectedColor(new Color(XMLUtil.getElementIntValue(node, ID_SELECTEDCOLOR,
                    DEFAULT_SELECTED_COLOR.getRGB())));
            setX(XMLUtil.getElementDoubleValue(node, ID_POS_X, 0d));
            setY(XMLUtil.getElementDoubleValue(node, ID_POS_Y, 0d));
            setRay(XMLUtil.getElementIntValue(node, ID_RAY, DEFAULT_RAY));
            setVisible(XMLUtil.getElementBooleanValue(node, ID_VISIBLE, true));
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

        XMLUtil.setElementIntValue(node, ID_COLOR, getColor().getRGB());
        XMLUtil.setElementIntValue(node, ID_SELECTEDCOLOR, getSelectedColor().getRGB());
        XMLUtil.setElementDoubleValue(node, ID_POS_X, getX());
        XMLUtil.setElementDoubleValue(node, ID_POS_Y, getY());
        XMLUtil.setElementIntValue(node, ID_RAY, getRay());
        XMLUtil.setElementBooleanValue(node, ID_VISIBLE, isVisible());

        return true;
    }
}
