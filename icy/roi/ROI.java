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
import icy.common.EventHierarchicalChecker;
import icy.common.UpdateEventHandler;
import icy.common.listener.ChangeListener;
import icy.file.xml.XMLPersistent;
import icy.main.Icy;
import icy.painter.Overlay;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROIEvent.ROIEventType;
import icy.roi.ROIEvent.ROIPointEventType;
import icy.sequence.Sequence;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle5D;
import icy.util.ClassUtil;
import icy.util.ColorUtil;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    public static final String ID_ROI = "roi";

    public static final String ID_CLASSNAME = "classname";
    public static final String ID_ID = "id";
    public static final String ID_NAME = "name";
    public static final String ID_COLOR = "color";
    // public static final String ID_SELECTED_COLOR = "selected_color";
    public static final String ID_STROKE = "stroke";
    public static final String ID_OPACITY = "opacity";
    public static final String ID_SELECTED = "selected";

    public static final ROIIdComparator idComparator = new ROIIdComparator();

    protected static final int DEFAULT_STROKE = 2;
    protected static final Color DEFAULT_COLOR = Color.GREEN;
    /**
     * @deprecated Use {@link #DEFAULT_COLOR} instead.
     */
    protected static final Color DEFAULT_NORMAL_COLOR = DEFAULT_COLOR;
    // protected static final Color DEFAULT_SELECTED_COLOR = Color.ORANGE;
    // protected static final Color OVER_COLOR = Color.WHITE;
    protected static final float DEFAULT_OPACITY = 0.3f;

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_EDITABLE = "editable";

    /**
     * Create a ROI from its class name or {@link PluginROI} class name.
     * 
     * @param className
     *        roi class name or {@link PluginROI} class name.
     * @return ROI (null if command is an incorrect ROI class name)
     */
    public static ROI create(String className)
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
                    result = roiClazz.newInstance().createROI();
                }
                catch (ClassCastException e0)
                {
                    // check if this is a ROI class
                    final Class<? extends ROI> roiClazz = clazz.asSubclass(ROI.class);

                    // default constructor
                    final Constructor<? extends ROI> constructor = roiClazz.getConstructor(new Class[] {});
                    // build ROI
                    result = constructor.newInstance();
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Cannot create ROI: " + className);
            System.err.println("Default constructor not found, ROI have to implement the default constructor !");
        }

        return result;
    }

    /**
     * Create a ROI from its class name or {@link PluginROI} class name (interactive mode).
     * 
     * @param className
     *        roi class name or {@link PluginROI} class name.
     * @param imagePoint
     *        initial point position in image coordinates (interactive mode).
     * @return ROI (null if command is an incorrect ROI class name)
     */
    public static ROI create(String className, Point2D imagePoint)
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
                    result = roiClazz.newInstance().createROI(imagePoint);
                }
                catch (ClassCastException e0)
                {
                    // check if this is a ROI class
                    final Class<? extends ROI> roiClazz = clazz.asSubclass(ROI.class);

                    try
                    {
                        // get constructor (Point2D)
                        final Constructor<? extends ROI> constructor = roiClazz
                                .getConstructor(new Class[] {Point2D.class});
                        // build ROI
                        result = constructor.newInstance(new Object[] {imagePoint});
                    }
                    catch (NoSuchMethodException e1)
                    {
                        // try default constructor as last chance...
                        final Constructor<? extends ROI> constructor = roiClazz.getConstructor(new Class[] {});
                        // build ROI
                        result = constructor.newInstance();
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Cannot create ROI: " + className);
            System.err.println("Default constructor not found, ROI have to implement the default constructor !");
        }

        return result;
    }

    /**
     * @deprecated Use {@link ROI#create(String, Point2D)} instead.
     */
    @Deprecated
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
                    result = roiClazz.newInstance().createROI(imagePoint);
                }
                catch (ClassCastException e0)
                {
                    // check if this is a ROI class
                    final Class<? extends ROI> roiClazz = clazz.asSubclass(ROI.class);

                    try
                    {
                        // get constructor (Point2D)
                        final Constructor<? extends ROI> constructor = roiClazz
                                .getConstructor(new Class[] {Point2D.class});
                        // build ROI
                        result = constructor.newInstance(new Object[] {imagePoint});
                    }
                    catch (NoSuchMethodException e2)
                    {
                        // try default constructor as last chance...
                        final Constructor<? extends ROI> constructor = roiClazz.getConstructor(new Class[] {});
                        // build ROI
                        result = constructor.newInstance();
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Cannot create ROI: " + className);
            System.err.println("Default constructor not found, ROI have to implement the default constructor !");
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

        final ROI roi = create(className);
        // load properties from XML
        if (roi != null)
        {
            roi.loadFromXML(node);
            roi.setSelected(false, false);
        }

        return roi;
    }

    public static double getAdjustedStroke(IcyCanvas canvas, double stroke)
    {
        final double adjStrkX = canvas.canvasToImageLogDeltaX((int) stroke);
        final double adjStrkY = canvas.canvasToImageLogDeltaY((int) stroke);

        return Math.max(adjStrkX, adjStrkY);
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
     * Return a list of ROI from a XML node.
     * 
     * @param node
     *        XML node defining the ROI list
     * @return a list of ROI
     */
    public static List<ROI> loadROIsFromXML(Node node)
    {
        final ArrayList<ROI> result = new ArrayList<ROI>();

        if (node != null)
        {
            final ArrayList<Node> nodesROI = XMLUtil.getChildren(node, ID_ROI);

            if (nodesROI != null)
            {
                for (Node n : nodesROI)
                {
                    final ROI roi = createFromXML(n);

                    // add to sequence
                    if (roi != null)
                        result.add(roi);
                }
            }
        }

        return result;
    }

    /**
     * @deprecated Use {@link #loadROIsFromXML(Node)} instead.
     */
    @Deprecated
    public static List<ROI> getROIsFromXML(Node node)
    {
        return loadROIsFromXML(node);
    }

    /**
     * Set a list of ROI to a XML node.
     * 
     * @param node
     *        XML node which is used to store the list of ROI
     * @param rois
     *        the list of ROI to store in the XML node
     */
    public static void saveROIsToXML(Node node, List<ROI> rois)
    {
        if (node != null)
        {
            for (ROI roi : rois)
            {
                final Node nodeROI = XMLUtil.addElement(node, ID_ROI);

                if (nodeROI != null)
                {
                    if (!roi.saveToXML(nodeROI))
                        XMLUtil.removeNode(node, nodeROI);
                }
            }
        }
    }

    /**
     * @deprecated Use {@link #saveROIsToXML(Node, List)} instead
     */
    @Deprecated
    public static void setROIsFromXML(Node node, List<ROI> rois)
    {
        saveROIsToXML(node, rois);
    }

    /**
     * @deprecated Use {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageDeltaX(IcyCanvas canvas, int value)
    {
        return canvas.canvasToImageDeltaX(value);
    }

    /**
     * @deprecated Use {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaX(IcyCanvas canvas, double value, double logFactor)
    {
        return canvas.canvasToImageLogDeltaX((int) value, logFactor);
    }

    /**
     * @deprecated Use {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaX(IcyCanvas canvas, double value)
    {
        return canvas.canvasToImageLogDeltaX((int) value);
    }

    /**
     * @deprecated Use {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaX(IcyCanvas canvas, int value, double logFactor)
    {
        return canvas.canvasToImageLogDeltaX(value, logFactor);
    }

    /**
     * @deprecated Use {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaX(IcyCanvas canvas, int value)
    {
        return canvas.canvasToImageLogDeltaX(value);
    }

    /**
     * @deprecated Use {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageDeltaY(IcyCanvas canvas, int value)
    {
        return canvas.canvasToImageDeltaY(value);
    }

    /**
     * @deprecated Use {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaY(IcyCanvas canvas, double value, double logFactor)
    {
        return canvas.canvasToImageLogDeltaY((int) value, logFactor);
    }

    /**
     * @deprecated Use {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaY(IcyCanvas canvas, double value)
    {
        return canvas.canvasToImageLogDeltaY((int) value);
    }

    /**
     * @deprecated Use {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaY(IcyCanvas canvas, int value, double logFactor)
    {
        return canvas.canvasToImageLogDeltaY(value, logFactor);
    }

    /**
     * @deprecated Use {@link IcyCanvas} methods instead
     */
    @Deprecated
    public static double canvasToImageLogDeltaY(IcyCanvas canvas, int value)
    {
        return canvas.canvasToImageLogDeltaY(value);
    }

    /**
     * Abstract basic class for ROI painter
     */
    public abstract class ROIPainter extends Overlay implements XMLPersistent
    {
        protected double stroke;
        protected Color color;
        // protected Color selectedColor;
        protected float opacity;

        public ROIPainter()
        {
            super("ROI painter", OverlayPriority.SHAPE_NORMAL);

            stroke = DEFAULT_STROKE;
            color = DEFAULT_NORMAL_COLOR;
            // selectedColor = DEFAULT_SELECTED_COLOR;
            opacity = DEFAULT_OPACITY;

            // we fix the ROI overlay
            fixed = true;
            readOnly = true;
        }

        /**
         * Return the ROI painter stroke.
         */
        public double getStroke()
        {
            return painter.stroke;
        }

        /**
         * Get adjusted stroke for the current canvas transformation
         */
        public double getAdjustedStroke(IcyCanvas canvas)
        {
            return ROI.getAdjustedStroke(canvas, stroke);
        }

        /**
         * Set ROI painter stroke.
         */
        public void setStroke(double value)
        {
            if (stroke != value)
            {
                stroke = value;
                painterChanged();
            }
        }

        /**
         * Returns the content opacity factor (0 = transparent while 1 means opaque).
         */
        public float getOpacity()
        {
            return opacity;
        }

        /**
         * Sets the content opacity factor (0 = transparent while 1 means opaque).
         */
        public void setOpacity(float value)
        {
            if (opacity != value)
            {
                opacity = value;
                painterChanged();
            }
        }

        /**
         * Returns the color for focused state
         */
        public Color getFocusedColor()
        {
            final int lum = ColorUtil.getLuminance(color);

            if (lum < (256 - 32))
                return Color.white;

            return Color.gray;
        }

        /**
         * @deprecated
         */
        public Color getSelectedColor()
        {
            return color;

            // Color result = color;
            //
            // if (ColorUtil.getLuminance(result) < (256 - 64))
            // result = ColorUtil.add(result, Color.darkGray);
            // else
            // result = ColorUtil.sub(result, Color.darkGray);
            //
            // return result;
        }

        /**
         * Returns the color used to display the ROI depending its current state.
         */
        public Color getDisplayColor()
        {
            if (focused)
                return getFocusedColor();
            // if (selected)
            // return getSelectedColor();

            return color;
        }

        /**
         * Return the ROI painter base color.
         */
        public Color getColor()
        {
            return color;
        }

        /**
         * Set the ROI painter base color.
         */
        public void setColor(Color value)
        {
            if (color != value)
            {
                color = value;
                painterChanged();
            }
        }

        // /**
        // * Return the ROI painter selected color.
        // */
        // public Color getSelectedColor()
        // {
        // return selectedColor;
        // }
        //
        /**
         * @deprecated Selected color is now automatically calculated
         */
        public void setSelectedColor(Color value)
        {

        }

        public void computePriority()
        {
            if (ROI.this.focused)
                painter.setPriority(OverlayPriority.SHAPE_TOP);
            else if (ROI.this.selected)
                painter.setPriority(OverlayPriority.SHAPE_HIGH);
            else
                painter.setPriority(OverlayPriority.SHAPE_LOW);
        }

        @Override
        public String getName()
        {
            // use ROI name
            return ROI.this.getName();
        }

        @Override
        public boolean loadFromXML(Node node)
        {
            if (node == null)
                return false;

            beginUpdate();
            try
            {
                setColor(new Color(XMLUtil.getElementIntValue(node, ID_COLOR, DEFAULT_COLOR.getRGB())));
                // setSelectedColor(new Color(XMLUtil.getElementIntValue(node, ID_SELECTED_COLOR,
                // getSelectedColor()
                // .getRGB())));
                setStroke(XMLUtil.getElementDoubleValue(node, ID_STROKE, DEFAULT_STROKE));
                setOpacity(XMLUtil.getElementFloatValue(node, ID_OPACITY, DEFAULT_OPACITY));
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

            XMLUtil.setElementIntValue(node, ID_COLOR, color.getRGB());
            // XMLUtil.setElementIntValue(node, ID_SELECTED_COLOR, getSelectedColor().getRGB());
            XMLUtil.setElementDoubleValue(node, ID_STROKE, stroke);
            XMLUtil.setElementFloatValue(node, ID_OPACITY, opacity);

            return true;
        }
    }

    /**
     * id generator
     */
    private static int id_generator = 1;

    /**
     * associated ROI painter
     */
    protected final ROIPainter painter;

    protected int id;
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
     * @deprecated Use {@link #remove(boolean)} instead.
     */
    @Deprecated
    public void detachFromAll(boolean canUndo)
    {
        remove(canUndo, 0);
    }

    /**
     * @deprecated Use {@link #remove()} instead.
     */
    @Deprecated
    public void detachFromAll()
    {
        remove(false, 0);
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
     * Remove this ROI (detach from all sequence)
     */
    public void remove(boolean canUndo, int i)
    {
        final ArrayList<Sequence> sequences = Icy.getMainInterface().getSequencesContaining(this);

        for (Sequence sequence : sequences)
            sequence.removeROI(this, canUndo);
    }

    /**
     * Remove this ROI (detach from all sequence)
     */
    public void remove()
    {
        remove(true, 0);
    }

    /**
     * @deprecated Use {@link #remove(boolean)} instead.
     */
    @Deprecated
    public void delete(boolean canUndo)
    {
        remove(canUndo, 0);
    }

    /**
     * @deprecated Use {@link #remove()} instead.
     */
    @Deprecated
    public void delete()
    {
        remove(true, 0);
    }

    public String getClassName()
    {
        return getClass().getName();
    }

    public String getSimpleClassName()
    {
        return ClassUtil.getSimpleClassName(getClassName());
    }

    /**
     * ROI unique id
     */
    public int getId()
    {
        return id;
    }

    /**
     * Returns the ROI painter (used to draw and interact with {@link ROI} on {@link IcyCanvas})
     */
    public ROIPainter getPainter()
    {
        return painter;
    }

    /**
     * Return the ROI painter stroke.
     */
    public double getStroke()
    {
        return painter.getStroke();
    }

    /**
     * Get adjusted stroke for the current canvas transformation
     */
    public double getAdjustedStroke(IcyCanvas canvas)
    {
        return painter.getAdjustedStroke(canvas);
    }

    /**
     * Set ROI painter stroke.
     */
    public void setStroke(double value)
    {
        painter.setStroke(value);
    }

    /**
     * Returns the ROI painter opacity factor (0 = transparent while 1 means opaque).
     */
    public float getOpacity()
    {
        return painter.getOpacity();
    }

    /**
     * Sets the ROI painter content opacity factor (0 = transparent while 1 means opaque).
     */
    public void setOpacity(float value)
    {
        painter.setOpacity(value);
    }

    /**
     * Return the ROI painter focused color.
     */
    public Color getFocusedColor()
    {
        return painter.getFocusedColor();
    }

    /**
     * @deprecated
     */
    public Color getSelectedColor()
    {
        return painter.getSelectedColor();
    }

    /**
     * Returns the color used to display the ROI depending its current state.
     */
    public Color getDisplayColor()
    {
        return painter.getDisplayColor();
    }

    /**
     * Return the ROI painter base color.
     */
    public Color getColor()
    {
        return painter.getColor();
    }

    /**
     * Set the ROI painter base color.
     */
    public void setColor(Color value)
    {
        painter.setColor(value);
    }

    /**
     * @deprecated selected color is automatically calculated.
     */
    @Deprecated
    public void setSelectedColor(Color value)
    {
        // painter.setSelectedColor(value);
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
            // painter name is ROI name so we notify it
            painter.propertyChanged(Overlay.PROPERTY_NAME);
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

            propertyChanged(PROPERTY_EDITABLE);
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
        if (result != null)
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
            setSelected(XMLUtil.getElementBooleanValue(node, ID_SELECTED, false), false);
            painter.loadFromXML(node);
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
        XMLUtil.setElementBooleanValue(node, ID_SELECTED, selected);
        painter.saveToXML(node);

        return true;
    }

    /**
     * @deprecated Use {@link #roiChanged()} instead
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
     * @deprecated Use {@link #propertyChanged(String)} instead.
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

        // do here global process on ROI change
        switch (event.getType())
        {
            case ROI_CHANGED:
                painter.painterChanged();
                break;

            case SELECTION_CHANGED:
                // compute painter priority
                painter.computePriority();
                painter.painterChanged();
                break;

            case FOCUS_CHANGED:
                // compute painter priority
                painter.computePriority();
                painter.painterChanged();
                break;

            case PROPERTY_CHANGED:
                break;
            default:
                break;
        }

        // notify listener we have changed
        fireChangedEvent(event);
    }

}
