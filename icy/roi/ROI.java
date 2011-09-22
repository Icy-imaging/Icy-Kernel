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
import icy.common.IcyChangedListener;
import icy.common.UpdateEventHandler;
import icy.file.xml.XMLPersistent;
import icy.main.Icy;
import icy.painter.AbstractPainter;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROIEvent.ROIEventType;
import icy.roi.ROIEvent.ROIPointEventType;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.util.ClassUtil;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import javax.swing.event.EventListenerList;

import org.w3c.dom.Node;

public abstract class ROI implements IcyChangedListener, XMLPersistent
{
    public static final String ID_CLASSNAME = "classname";
    public static final String ID_ID = "id";
    public static final String ID_NAME = "name";
    public static final String ID_COLOR = "color";
    public static final String ID_SELECTED_COLOR = "selected_color";
    public static final String ID_STROKE = "stroke";
    public static final String ID_SELECTED = "selected";

    protected static final int DEFAULT_STROKE = 2;
    protected static final Color DEFAULT_NORMAL_COLOR = Color.GREEN;
    protected static final Color DEFAULT_SELECTED_COLOR = Color.ORANGE;
    protected static final Color OVER_COLOR = Color.WHITE;

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
     * @return ROI (null if command is an incorrect ROI command name)
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
            seq.addROI(result);

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
            roi.loadFromXML(node);

        return roi;
    }

    /**
     * Return ROI of specified type from the ROI list
     */
    public static ArrayList<ROI> getROIList(ArrayList<? extends ROI> rois, Class<? extends ROI> clazz)
    {
        final ArrayList<ROI> result = new ArrayList<ROI>();

        for (ROI roi : rois)
            if (clazz.isInstance(roi))
                result.add(roi);

        return result;
    }

    /**
     * Convert a canvas X coefficient into image X coefficient.<br>
     * We don't take about rotation here as we just want scale ratio here.
     */
    public static double canvasToImageDeltaX(IcyCanvas canvas, int value)
    {
        return value / canvas.getScaleX();
    }

    /**
     * Convert a canvas X coefficient into image X log coefficient <br>
     * The conversion is still affected by zoom ratio but in a logarithm form
     */
    public static double canvasToImageLogDeltaX(IcyCanvas canvas, double value, double logFactor)
    {
        final double scaleFactor = canvas.getScaleX();
        // keep the zoom ratio but in a log perspective
        return value / (scaleFactor / Math.pow(10, Math.log10(scaleFactor) / logFactor));
    }

    /**
     * Convert a canvas X coefficient into image X log coefficient <br>
     * The conversion is still affected by zoom ratio but in a logarithm form
     */
    public static double canvasToImageLogDeltaX(IcyCanvas canvas, double value)
    {
        return canvasToImageLogDeltaX(canvas, value, 5d);
    }

    /**
     * Convert a canvas X coefficient into image X log coefficient <br>
     * The conversion is still affected by zoom ratio but in a logarithm form
     */
    public static double canvasToImageLogDeltaX(IcyCanvas canvas, int value, double logFactor)
    {
        return canvasToImageLogDeltaX(canvas, (double) value, logFactor);
    }

    /**
     * Convert a canvas X coefficient into image X log coefficient <br>
     * The conversion is still affected by zoom ratio but in a logarithm form
     */
    public static double canvasToImageLogDeltaX(IcyCanvas canvas, int value)
    {
        return canvasToImageLogDeltaX(canvas, (double) value);
    }

    /**
     * Convert a canvas Y coefficient into image Y coefficient
     * We don't take about rotation here as we just want scale ratio here.
     */
    public static double canvasToImageDeltaY(IcyCanvas canvas, int value)
    {
        return value / canvas.getScaleY();
    }

    /**
     * Convert a canvas Y coefficient into image Y log coefficient <br>
     * The conversion is still affected by zoom ratio but in a logarithm form
     */
    public static double canvasToImageLogDeltaY(IcyCanvas canvas, double value, double logFactor)
    {
        final double scaleFactor = canvas.getScaleY();
        // keep the zoom ratio but in a log perspective
        return value / (scaleFactor / Math.pow(10, Math.log10(scaleFactor) / logFactor));
    }

    /**
     * Convert a canvas Y coefficient into image Y log coefficient <br>
     * The conversion is still affected by zoom ratio but in a logarithm form
     */
    public static double canvasToImageLogDeltaY(IcyCanvas canvas, double value)
    {
        return canvasToImageLogDeltaY(canvas, value, 5d);
    }

    /**
     * Convert a canvas Y coefficient into image Y log coefficient <br>
     * The conversion is still affected by zoom ratio but in a logarithm form
     */
    public static double canvasToImageLogDeltaY(IcyCanvas canvas, int value, double logFactor)
    {
        return canvasToImageLogDeltaY(canvas, (double) value, logFactor);
    }

    /**
     * Convert a canvas Y coefficient into image Y log coefficient <br>
     * The conversion is still affected by zoom ratio but in a logarithm form
     */
    public static double canvasToImageLogDeltaY(IcyCanvas canvas, int value)
    {
        return canvasToImageLogDeltaY(canvas, (double) value);
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

    /**
     * last mouse position (image coordinates)
     */
    protected final Point2D mousePos;

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
        creating = true;
        focused = false;
        selected = false;

        mousePos = new Point2D.Double();
        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);
    }

    protected abstract ROIPainter createPainter();

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
    public void detachFromAll()
    {
        final ArrayList<Sequence> sequences = Icy.getMainInterface().getSequencesContaining(this);

        for (Sequence sequence : sequences)
            sequence.removeROI(this);
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
    public void delete()
    {
        detachFromAll();
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
        return Math.max(canvasToImageLogDeltaX(canvas, strk), canvasToImageLogDeltaY(canvas, strk));
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
            nameChanged();
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
            painterChanged();
        }
    }

    public void internalUnfocus()
    {
        if (focused != false)
        {
            focused = false;
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
     * @param exclusive
     *        exclusive selection (only one selected ROI per sequence)
     */
    public void setSelected(boolean value, boolean exclusive)
    {
        boolean done = false;

        // always perform the process to perform exclusive select after no exclusive one
        if (value && exclusive)
        {
            // use the sequence for ROI selection with exclusive parameter
            final ArrayList<Sequence> attachedSeqs = Icy.getMainInterface().getSequencesContaining(this);

            for (Sequence seq : attachedSeqs)
                done |= seq.setSelectedROI(this, exclusive);
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
            painterChanged();
        }
    }

    public void internalSelect()
    {
        if (selected != true)
        {
            selected = true;
            painterChanged();
        }
    }

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
        id = generateId();
        // change name too
        setName(name + " copy");

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
     * called when ROI has changed its bounds
     */
    public void roiChanged(ROIPointEventType pointEventType, Object point)
    {
        // handle with updater
        updater.changed(new ROIEvent(this, ROIEventType.ROI_CHANGED, pointEventType, point));
    }

    /**
     * called when ROI has changed its bounds
     */
    public void roiChanged()
    {
        // handle with updater
        roiChanged(ROIPointEventType.NULL, null);
    }

    /**
     * called when ROI need to be repainted
     */
    public void painterChanged()
    {
        // handle with updater
        updater.changed(new ROIEvent(this, ROIEventType.PAINTER_CHANGED));
    }

    /**
     * called when ROI name has changed
     */
    public void nameChanged()
    {
        // handle with updater
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
            case PAINTER_CHANGED:
                // painter of ROI changed
                painter.changed();
                break;
        }

        // notify listener we have changed
        fireChangedEvent(event);
    }

}
