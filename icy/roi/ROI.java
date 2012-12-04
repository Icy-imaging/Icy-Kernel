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
import icy.common.EventHierarchicalChecker;
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.file.xml.XMLPersistent;
import icy.main.Icy;
import icy.painter.AbstractPainter;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROIEvent.ROIEventType;
import icy.roi.ROIEvent.ROIPointEventType;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle5D;
import icy.util.ClassUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.event.EventListenerList;

import org.w3c.dom.Node;

public abstract class ROI implements ChangeListener, XMLPersistent
{
    public static class ROIIdComparator implements Comparator<ROI>
    {
        @Override
        public int compare(ROI roi1, ROI roi2)
        {
            if (roi1 == roi2)
                return 0;

            if (roi1 == null)
                return -1;
            if (roi2 == null)
                return 1;

            if (roi1.id < roi2.id)
                return -1;
            if (roi1.id > roi2.id)
                return 1;

            return 0;
        }
    }

    public static final String ID_CLASSNAME = "classname";
    public static final String ID_ID = "id";
    public static final String ID_NAME = "name";
    public static final String ID_COLOR = "color";
    public static final String ID_SELECTED_COLOR = "selected_color";
    public static final String ID_STROKE = "stroke";
    public static final String ID_SELECTED = "selected";

    public static final ROIIdComparator idComparator = new ROIIdComparator();

    protected static final int DEFAULT_STROKE = 2;
    protected static final Color DEFAULT_NORMAL_COLOR = Color.GREEN;
    protected static final Color DEFAULT_SELECTED_COLOR = Color.ORANGE;
    protected static final Color OVER_COLOR = Color.WHITE;

    public static final String PROPERTY_NAME = "name";

    /**
     * Create a ROI from its tool command name
     * 
     * @param className
     *        roi class name
     * @param seq
     *        sequence object
     * @param imagePoint
     *        position in image coordinates
     * @param creation
     *        specify if roi is created in "creation mode"
     * @return ROI (null if command is an incorrect ROI class name)
     */
    public static ROI create(String className, Sequence seq, Point2D imagePoint, boolean creation)
    {
        ROI result = null;

        try
        {
            // search for the specified className
            final Class<?> clazz = ClassUtil.findClass(className);

            // class found
            if (clazz != null)
            {
                try
                {
                    // we first check if we have a PluginROI class here
                    final Class<? extends PluginROI> roiClazz = clazz.asSubclass(PluginROI.class);
                    // create ROI
                    result = roiClazz.newInstance().createROI(imagePoint, creation);
                }
                catch (ClassCastException e0)
                {
                    // check if this is a ROI class
                    final Class<? extends ROI> roiClazz = clazz.asSubclass(ROI.class);

                    try
                    {
                        // get constructor (Point2D, boolean)
                        final Constructor<? extends ROI> constructor = roiClazz.getConstructor(new Class[] {
                                Point2D.class, boolean.class});
                        // build ROI
                        result = constructor.newInstance(new Object[] {imagePoint, Boolean.valueOf(creation)});
                    }
                    catch (NoSuchMethodException e1)
                    {
                        // get constructor (Point2D)
                        final Constructor<? extends ROI> constructor = roiClazz
                                .getConstructor(new Class[] {Point2D.class});
                        // build ROI
                        result = constructor.newInstance(new Object[] {imagePoint});
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("ROI.create('" + className + "', ...) error :");
            IcyExceptionHandler.showErrorMessage(e, false);
        }

        // attach to sequence once ROI is initialized
        if ((seq != null) && (result != null))
            seq.addROI(result, true);

        return result;
    }

    /**
     * Create a ROI from a xml definition
     * 
     * @param node
     *        xml node defining the roi
     * @return ROI (null if node is an incorrect ROI definition)
     */
    public static ROI createFromXML(Node node)
    {
        if (node == null)
            return null;

        final String className = XMLUtil.getElementValue(node, ID_CLASSNAME, "");

        final ROI roi = create(className, null, new Point2D.Double(0, 0), false);
        // load properties from XML
        if (roi != null)
        {
            roi.loadFromXML(node);
            roi.selected = false;
        }

        return roi;
    }

    /**
     * Return ROI of specified type from the ROI list
     */
    public static ArrayList<ROI> getROIList(ArrayList<? extends ROI> rois, Class<? extends ROI> clazz)
    {
        return getROIList(rois.toArray(new ROI[rois.size()]), clazz);
    }

    /**
     * Return ROI of specified type from the ROI list
     */
    public static ArrayList<ROI> getROIList(ROI rois[], Class<? extends ROI> clazz)
    {
        final ArrayList<ROI> result = new ArrayList<ROI>();

        for (ROI roi : rois)
            if (clazz.isInstance(roi))
                result.add(roi);

        return result;
    }

    /**
     * @deprecated uses {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageDeltaX(IcyCanvas canvas, int value)
    {
        return canvas.canvasToImageDeltaX(value);
    }

    /**
     * @deprecated uses {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaX(IcyCanvas canvas, double value, double logFactor)
    {
        return canvas.canvasToImageLogDeltaX((int) value, logFactor);
    }

    /**
     * @deprecated uses {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaX(IcyCanvas canvas, double value)
    {
        return canvas.canvasToImageLogDeltaX((int) value);
    }

    /**
     * @deprecated uses {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaX(IcyCanvas canvas, int value, double logFactor)
    {
        return canvas.canvasToImageLogDeltaX(value, logFactor);
    }

    /**
     * @deprecated uses {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaX(IcyCanvas canvas, int value)
    {
        return canvas.canvasToImageLogDeltaX(value);
    }

    /**
     * @deprecated uses {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageDeltaY(IcyCanvas canvas, int value)
    {
        return canvas.canvasToImageDeltaY(value);
    }

    /**
     * @deprecated uses {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaY(IcyCanvas canvas, double value, double logFactor)
    {
        return canvas.canvasToImageLogDeltaY((int) value, logFactor);
    }

    /**
     * @deprecated uses {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaY(IcyCanvas canvas, double value)
    {
        return canvas.canvasToImageLogDeltaY((int) value);
    }

    /**
     * @deprecated uses {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaY(IcyCanvas canvas, int value, double logFactor)
    {
        return canvas.canvasToImageLogDeltaY(value, logFactor);
    }

    /**
     * @deprecated uses {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaY(IcyCanvas canvas, int value)
    {
        return canvas.canvasToImageLogDeltaY(value);
    }

    /**
     * Abstract basic class for ROI painter
     */
    protected abstract class ROIPainter extends AbstractPainter
    {

    }

    /**
     * unique ROI id
     */
    private static int id_generator = 1;

    /**
     * ROI painter
     */
    protected final AbstractPainter painter;
    /**
     * ROI stroke (canvas coordinates)
     */
    protected double stroke;

    protected int id;
    protected Color color;
    protected Color selectedColor;
    protected String name;
    protected boolean creating;
    protected boolean focused;
    protected boolean selected;
    protected boolean editable;

    /**
     * last mouse position (image coordinates)
     */
    protected final Point2D.Double mousePos;

    /**
     * listeners
     */
    protected final EventListenerList listeners;
    /**
     * internal updater
     */
    protected final UpdateEventHandler updater;

    public ROI()
    {
        super();

        // ensure unique id
        id = generateId();
        painter = createPainter();
        stroke = DEFAULT_STROKE;
        color = DEFAULT_NORMAL_COLOR;
        selectedColor = DEFAULT_SELECTED_COLOR;
        name = "";
        editable = true;
        creating = true;
        focused = false;
        selected = false;

        mousePos = new Point2D.Double();
        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);
    }

    protected abstract ROIPainter createPainter();

    /**
     * Return number of dimension.
     */
    public abstract int getDimension();

    /**
     * generate unique id
     */
    private static int generateId()
    {
        synchronized (ROI.class)
        {
            return id_generator++;
        }
    }

    /**
     * @deprecated use {@link Sequence#addROI(ROI)} instead
     */
    @Deprecated
    public void attachTo(Sequence sequence)
    {
        if (sequence != null)
            sequence.addROI(this);
    }

    /**
     * @deprecated use {@link Sequence#removeROI(ROI)} instead
     */
    @Deprecated
    public void detachFrom(Sequence sequence)
    {
        if (sequence != null)
            sequence.removeROI(this);
    }

    /**
     * Remove this ROI from all attached sequence
     */
    public void detachFromAll(boolean canUndo)
    {
        final ArrayList<Sequence> sequences = Icy.getMainInterface().getSequencesContaining(this);

        for (Sequence sequence : sequences)
            sequence.removeROI(this, canUndo);
    }

    /**
     * Remove this ROI from all attached sequence
     */
    public void detachFromAll()
    {
        detachFromAll(false);
    }

    /**
     * Return true is this ROI is attached to at least one sequence
     */
    public boolean isAttached(Sequence sequence)
    {
        if (sequence != null)
            return sequence.contains(this);

        return false;
    }

    /**
     * Return first sequence where ROI is attached
     */
    public Sequence getFirstSequence()
    {
        return Icy.getMainInterface().getFirstSequenceContaining(this);
    }

    /**
     * Return sequences where ROI is attached
     */
    public ArrayList<Sequence> getSequences()
    {
        return Icy.getMainInterface().getSequencesContaining(this);
    }

    /**
     * Delete this ROI (detach from all sequence)
     */
    public void delete(boolean canUndo)
    {
        detachFromAll(canUndo);
    }

    /**
     * Delete this ROI (detach from all sequence)
     */
    public void delete()
    {
        delete(true);
    }

    public String getClassName()
    {
        return getClass().getName();
    }

    public String getSimpleClassName()
    {
        return ClassUtil.getSimpleClassName(getClassName());
    }

    public AbstractPainter getPainter()
    {
        return painter;
    }

    public double getStroke()
    {
        return stroke;
    }

    public double getAdjustedStroke(IcyCanvas canvas, double strk)
    {
        final double adjStrkX = canvas.canvasToImageLogDeltaX((int) strk);
        final double adjStrkY = canvas.canvasToImageLogDeltaY((int) strk);

        return Math.max(adjStrkX, adjStrkY);
    }

    /**
     * Get adjusted stroke for the current canvas transformation
     */
    public double getAdjustedStroke(IcyCanvas canvas)
    {
        return getAdjustedStroke(canvas, stroke);
    }

    public void setStroke(double value)
    {
        if (stroke != value)
        {
            stroke = value;
            painter.changed();
        }
    }

    /**
     * @return the display color
     */
    public Color getDisplayColor()
    {
        Color result;

        if (selected)
            result = selectedColor;
        else
            result = color;

        if (focused)
            result = OVER_COLOR;
        // / result = ColorUtil.mix(ColorUtil.mix(result, Color.white), Color.white);

        return result;
    }

    /**
     * @return the id
     *         ROI IDs are in the range [1...n]
     */
    public int getId()
    {
        return id;
    }

    /**
     * @return the Color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * @param value
     *        the Color to set
     */
    public void setColor(Color value)
    {
        if (color != value)
        {
            color = value;
            painter.changed();
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
            painter.changed();
        }
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param value
     *        the name to set
     */
    public void setName(String value)
    {
        if (name != value)
        {
            name = value;
            propertyChanged(PROPERTY_NAME);
        }
    }

    /**
     * @return the creating
     */
    public boolean isCreating()
    {
        return creating;
    }

    /**
     * @return the focused
     */
    public boolean isFocused()
    {
        return focused;
    }

    /**
     * @param value
     *        the focused to set
     */
    public void setFocused(boolean value)
    {
        boolean done = false;

        if (value)
        {
            // only one ROI focused per sequence
            final ArrayList<Sequence> attachedSeqs = Icy.getMainInterface().getSequencesContaining(this);

            for (Sequence seq : attachedSeqs)
                done |= seq.setFocusedROI(this);
        }

        if (!done)
        {
            if (value)
                internalFocus();
            else
                internalUnfocus();
        }
    }

    public void internalFocus()
    {
        if (focused != true)
        {
            focused = true;
            focusChanged();
        }
    }

    public void internalUnfocus()
    {
        if (focused != false)
        {
            focused = false;
            focusChanged();
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
     * @param exclusive
     *        exclusive selection (only one selected ROI per sequence)
     */
    public void setSelected(boolean value, boolean exclusive)
    {
        boolean done = false;

        // always perform the process to perform exclusive select after no exclusive one
        if (exclusive)
        {
            // use the sequence for ROI selection with exclusive parameter
            final ArrayList<Sequence> attachedSeqs = Icy.getMainInterface().getSequencesContaining(this);

            for (Sequence seq : attachedSeqs)
                done |= seq.setSelectedROI(value ? this : null, exclusive);
        }

        if (!done)
        {
            if (value)
                internalSelect();
            else
                internalUnselect();
        }
    }

    public void internalUnselect()
    {
        if (selected != false)
        {
            selected = false;
            // as soon ROI has been unselected, we're not in create mode anymore
            creating = false;
            selectionChanged();
        }
    }

    public void internalSelect()
    {
        if (selected != true)
        {
            selected = true;
            selectionChanged();
        }
    }

    /**
     * Return true if ROI is editable.
     */
    public boolean isEditable()
    {
        return editable;
    }

    /**
     * Set the editable state of ROI.
     */
    public void setEditable(boolean value)
    {
        if (editable != value)
        {
            editable = value;

            if (!value)
                setSelected(false, false);
        }
    }

    /**
     * Tests if a specified 5D point is inside the ROI.
     * 
     * @return <code>true</code> if the specified <code>Point5D</code> is inside the boundary of the
     *         <code>ROI</code>; <code>false</code> otherwise.
     */
    public abstract boolean contains(double x, double y, double z, double t, double c);

    /**
     * Tests if a specified {@link Point5D} is inside the ROI.
     * 
     * @param p
     *        the specified <code>Point5D</code> to be tested
     * @return <code>true</code> if the specified <code>Point2D</code> is inside the boundary of the
     *         <code>ROI</code>; <code>false</code> otherwise.
     */
    public boolean contains(Point5D p)
    {
        return contains(p.getX(), p.getY(), p.getZ(), p.getT(), p.getC());
    }

    /**
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified
     * rectangular area. The rectangular area is considered to intersect the <code>ROI</code> if any
     * point is contained in both the interior of the <code>ROI</code> and the specified rectangular
     * area.
     * <p>
     * The {@code ROI.intersects()} method allows a {@code ROI} implementation to conservatively
     * return {@code true} when:
     * <ul>
     * <li>there is a high probability that the rectangular area and the <code>ROI</code> intersect,
     * but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the
     * rectangular area does not intersect the {@code ROI}.
     * 
     * @return <code>true</code> if the interior of the <code>ROI</code> and the interior of the
     *         rectangular area intersect, or are both highly likely to intersect and intersection
     *         calculations would be too expensive to perform; <code>false</code> otherwise.
     */
    public abstract boolean intersects(double x, double y, double z, double t, double c, double sizeX, double sizeY,
            double sizeZ, double sizeT, double sizeC);

    /**
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified
     * rectangular area. The rectangular area is considered to intersect the <code>ROI</code> if any
     * point is contained in both the interior of the <code>ROI</code> and the specified rectangular
     * area.
     * <p>
     * The {@code ROI.intersects()} method allows a {@code ROI} implementation to conservatively
     * return {@code true} when:
     * <ul>
     * <li>there is a high probability that the rectangular area and the <code>ROI</code> intersect,
     * but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the
     * rectangular area does not intersect the {@code ROI}.
     * 
     * @return <code>true</code> if the interior of the <code>ROI</code> and the interior of the
     *         rectangular area intersect, or are both highly likely to intersect and intersection
     *         calculations would be too expensive to perform; <code>false</code> otherwise.
     */
    public boolean intersects(Rectangle5D r)
    {
        return intersects(r.getX(), r.getY(), r.getZ(), r.getT(), r.getC(), r.getSizeX(), r.getSizeY(), r.getSizeZ(),
                r.getSizeT(), r.getSizeC());
    }

    /**
     * Return perimeter of ROI in pixels.<br>
     * This is basically the number of pixel representing ROI edges.<br>
     */
    /*
     * Override this method to adapt and optimize for a specific ROI.
     */
    public abstract double getPerimeter();

    /**
     * Return volume of ROI in pixels.<br>
     * For a 2D ROI, volume is equivalent to the area.<br>
     */
    /*
     * Override this method to adapt and optimize for a specific ROI.
     */
    public abstract double getVolume();

    public void setMousePos(Point2D pos)
    {
        if ((pos != null) && !mousePos.equals(pos))
            mousePos.setLocation(pos);
    }

    public ROI getCopy()
    {
        // use XML persistence for cloning
        final Node node = XMLUtil.createDocument(true).getDocumentElement();

        saveToXML(node);
        final ROI result = createFromXML(node);

        // then generate id and modify name
        result.id = generateId();

        return result;
    }

    // protected void copyFrom(ROI roi)
    // {
    // color = new Color(roi.getColor().getRGB());
    // name = roi.getName();
    // selectedColor = new Color(roi.getSelectedColor().getRGB());
    // stroke = roi.getStroke();
    //
    // nameChanged();
    // painterChanged();
    // }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        beginUpdate();
        try
        {
            // FIXME : this can make duplicate id but it is also important to preserve id
            id = XMLUtil.getElementIntValue(node, ID_ID, 0);
            setName(XMLUtil.getElementValue(node, ID_NAME, ""));
            setColor(new Color(XMLUtil.getElementIntValue(node, ID_COLOR, DEFAULT_NORMAL_COLOR.getRGB())));
            setSelectedColor(new Color(XMLUtil.getElementIntValue(node, ID_SELECTED_COLOR,
                    DEFAULT_SELECTED_COLOR.getRGB())));
            setStroke(XMLUtil.getElementDoubleValue(node, ID_STROKE, DEFAULT_STROKE));
            setSelected(XMLUtil.getElementBooleanValue(node, ID_SELECTED, false), false);
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

        XMLUtil.setElementValue(node, ID_CLASSNAME, getClassName());
        XMLUtil.setElementIntValue(node, ID_ID, id);
        XMLUtil.setElementValue(node, ID_NAME, name);
        XMLUtil.setElementIntValue(node, ID_COLOR, color.getRGB());
        XMLUtil.setElementIntValue(node, ID_SELECTED_COLOR, selectedColor.getRGB());
        XMLUtil.setElementDoubleValue(node, ID_STROKE, stroke);
        XMLUtil.setElementBooleanValue(node, ID_SELECTED, selected);

        return true;
    }

    /**
     * @deprecated Uses {@link #roiChanged()} instead
     */
    @Deprecated
    public void roiChanged(ROIPointEventType pointEventType, Object point)
    {
        // handle with updater
        updater.changed(new ROIEvent(this, ROIEventType.ROI_CHANGED, pointEventType, point));
    }

    /**
     * Called when ROI has changed its bounds.
     */
    public void roiChanged()
    {
        // handle with updater
        updater.changed(new ROIEvent(this, ROIEventType.ROI_CHANGED));
    }

    /**
     * Called when ROI selected state changed.
     */
    public void selectionChanged()
    {
        // handle with updater
        updater.changed(new ROIEvent(this, ROIEventType.SELECTION_CHANGED));
    }

    /**
     * Called when ROI focus state changed.
     */
    public void focusChanged()
    {
        // handle with updater
        updater.changed(new ROIEvent(this, ROIEventType.FOCUS_CHANGED));
    }

    /**
     * Called when ROI painter changed.
     * 
     * @deprecated
     */
    @Deprecated
    public void painterChanged()
    {
        // handle with updater
        updater.changed(new ROIEvent(this, ROIEventType.PAINTER_CHANGED));
    }

    /**
     * Called when ROI name has changed.
     * 
     * @deprecated Uses {@link #propertyChanged(String)} instead.
     */
    @Deprecated
    public void nameChanged()
    {
        // handle with updater
        updater.changed(new ROIEvent(this, ROIEventType.NAME_CHANGED));
    }

    /**
     * Called when ROI property has changed
     */
    public void propertyChanged(String propertyName)
    {
        // handle with updater
        updater.changed(new ROIEvent(this, propertyName));

        // backward compatibility
        if (StringUtil.equals(propertyName, PROPERTY_NAME))
            updater.changed(new ROIEvent(this, ROIEventType.NAME_CHANGED));
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(ROIListener listener)
    {
        listeners.add(ROIListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(ROIListener listener)
    {
        listeners.remove(ROIListener.class, listener);
    }

    private void fireChangedEvent(ROIEvent event)
    {
        for (ROIListener listener : listeners.getListeners(ROIListener.class))
            listener.roiChanged(event);
    }

    public void beginUpdate()
    {
        updater.beginUpdate();
        painter.beginUpdate();
    }

    public void endUpdate()
    {
        painter.endUpdate();
        updater.endUpdate();
    }

    public boolean isUpdating()
    {
        return updater.isUpdating();
    }

    @Override
    public void onChanged(EventHierarchicalChecker object)
    {
        final ROIEvent event = (ROIEvent) object;

        switch (event.getType())
        {
        // do here global process on ROI change
            case ROI_CHANGED:
            case SELECTION_CHANGED:
            case FOCUS_CHANGED:
                // case PAINTER_CHANGED:
                // painter of ROI changed
                painter.changed();
                break;
        }

        // notify listener we have changed
        fireChangedEvent(event);
    }

}
