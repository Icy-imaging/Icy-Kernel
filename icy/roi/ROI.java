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
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginROI;
import icy.resource.ResourceUtil;
import icy.roi.ROIEvent.ROIEventType;
import icy.roi.ROIEvent.ROIPointEventType;
import icy.sequence.Sequence;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle4D;
import icy.type.rectangle.Rectangle5D;
import icy.util.ClassUtil;
import icy.util.ColorUtil;
import icy.util.ShapeUtil.BooleanOperator;
import icy.util.StringUtil;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.w3c.dom.Node;

import plugins.kernel.roi.roi3d.ROI3DArea;
import plugins.kernel.roi.roi4d.ROI4DArea;
import plugins.kernel.roi.roi5d.ROI5DArea;

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

    public static final int DEFAULT_STROKE = 2;
    public static final Color DEFAULT_COLOR = Color.GREEN;
    /**
     * @deprecated Use {@link #DEFAULT_COLOR} instead.
     */
    @Deprecated
    public static final Color DEFAULT_NORMAL_COLOR = DEFAULT_COLOR;
    // protected static final Color DEFAULT_SELECTED_COLOR = Color.ORANGE;
    // protected static final Color OVER_COLOR = Color.WHITE;
    public static final float DEFAULT_OPACITY = 0.3f;

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_ICON = "icon";
    public static final String PROPERTY_CREATING = "creating";
    public static final String PROPERTY_READONLY = "readonly";

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
                    // create the plugin
                    final PluginROI plugin = roiClazz.newInstance();
                    // create ROI
                    result = plugin.createROI();
                    // set ROI icon from plugin icon
                    final Image icon = ((Plugin) plugin).getDescriptor().getIconAsImage();
                    if (icon != null)
                        result.setIcon(icon);
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
     * @return ROI (null if the specified class name is an incorrect ROI class name)
     */
    public static ROI create(String className, Point5D imagePoint)
    {
        if (imagePoint == null)
            return create(className);

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
                    // create the plugin
                    final PluginROI plugin = roiClazz.newInstance();

                    // then create ROI with the Point5D constructor
                    result = plugin.createROI(imagePoint);
                    // not supported --> use default constructor
                    if (result == null)
                        result = plugin.createROI();

                    // set ROI icon from plugin icon
                    final Image icon = ((Plugin) plugin).getDescriptor().getIconAsImage();
                    if (icon != null)
                        result.setIcon(icon);
                }
                catch (ClassCastException e0)
                {
                    // check if this is a ROI class
                    final Class<? extends ROI> roiClazz = clazz.asSubclass(ROI.class);

                    try
                    {
                        // get constructor (Point5D)
                        final Constructor<? extends ROI> constructor = roiClazz
                                .getConstructor(new Class[] {Point5D.class});
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
     * @deprecated Use {@link #create(String, Point5D)} instead
     */
    @Deprecated
    public static ROI create(String className, Point2D imagePoint)
    {
        return create(className, new Point5D.Double(imagePoint.getX(), imagePoint.getY(), -1d, -1d, -1d));
    }

    /**
     * @deprecated Use {@link ROI#create(String, Point5D)} instead.
     */
    @Deprecated
    public static ROI create(String className, Sequence seq, Point2D imagePoint, boolean creation)
    {
        final ROI result = create(className, imagePoint);

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
            roi.setSelected(false);
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
    public static List<ROI> getROIList(List<? extends ROI> rois, Class<? extends ROI> clazz)
    {
        final List<ROI> result = new ArrayList<ROI>();

        for (ROI roi : rois)
            if (clazz.isInstance(roi))
                result.add(roi);

        return result;
    }

    /**
     * @deprecated Use {@link #getROIList(List, Class)} instead.
     */
    @Deprecated
    public static ArrayList<ROI> getROIList(ArrayList<? extends ROI> rois, Class<? extends ROI> clazz)
    {
        final ArrayList<ROI> result = new ArrayList<ROI>();

        for (ROI roi : rois)
            if (clazz.isInstance(roi))
                result.add(roi);

        return result;
    }

    /**
     * @deprecated Use {@link #getROIList(List, Class)} instead.
     */
    @Deprecated
    public static List<ROI> getROIList(ROI rois[], Class<? extends ROI> clazz)
    {
        final List<ROI> result = new ArrayList<ROI>();

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
        final List<ROI> result = new ArrayList<ROI>();

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
     * Abstract basic class for ROI overlay
     */
    public abstract class ROIPainter extends Overlay implements XMLPersistent
    {
        /**
         * Overlay properties
         */
        protected double stroke;
        protected Color color;
        protected float opacity;

        /**
         * Last mouse position (image coordinates).
         * Needed for some internals operation
         */
        protected final Point5D.Double mousePos;

        public ROIPainter()
        {
            super("ROI painter", OverlayPriority.SHAPE_NORMAL);

            stroke = DEFAULT_STROKE;
            color = DEFAULT_COLOR;
            opacity = DEFAULT_OPACITY;

            mousePos = new Point5D.Double();

            // we fix the ROI overlay
            canBeRemoved = false;
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
            return ROI.getAdjustedStroke(canvas, getStroke());
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
            final int lum = ColorUtil.getLuminance(getColor());

            if (lum < (256 - 32))
                return Color.white;

            return Color.gray;
        }

        /**
         * @deprecated
         */
        @Deprecated
        public Color getSelectedColor()
        {
            return getColor();
        }

        /**
         * Returns the color used to display the ROI depending its current state.
         */
        public Color getDisplayColor()
        {
            if (isFocused())
                return getFocusedColor();

            return getColor();
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

        /**
         * @deprecated Selected color is now automatically calculated
         */
        @Deprecated
        public void setSelectedColor(Color value)
        {

        }

        /**
         * Returns the overlay internals mouse position (image coordinates)
         */
        public Point5D.Double getMousePos()
        {
            return mousePos;
        }

        /**
         * Set the overlay internals mouse position (image coordinates)
         */
        public void setMousePos(Point5D pos)
        {
            if ((pos != null) && !mousePos.equals(pos))
                mousePos.setLocation(pos);
        }

        public void computePriority()
        {
            if (isFocused())
                setPriority(OverlayPriority.SHAPE_TOP);
            else if (isSelected())
                setPriority(OverlayPriority.SHAPE_HIGH);
            else
                setPriority(OverlayPriority.SHAPE_LOW);
        }

        @Override
        public boolean isReadOnly()
        {
            // use ROI read only property
            return ROI.this.isReadOnly();
        }

        @Override
        public String getName()
        {
            // use ROI name property
            return ROI.this.getName();
        }

        @Override
        public void setName(String name)
        {
            // modifying layer name modify ROI name
            ROI.this.setName(name);
        }

        @Override
        public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isActiveFor(canvas))
            {
                if (!e.isConsumed())
                {
                    switch (e.getKeyCode())
                    {
                        case KeyEvent.VK_ESCAPE:
                            // shape selected ? --> global unselect ROI
                            if (isSelected())
                            {
                                canvas.getSequence().setSelectedROI(null);
                                e.consume();
                            }
                            break;

                        case KeyEvent.VK_DELETE:
                        case KeyEvent.VK_BACK_SPACE:
                            if (!isReadOnly())
                            {
                                // roi selected ?
                                if (isSelected())
                                {
                                    final boolean result;

                                    if (isFocused())
                                        // remove ROI from sequence
                                        result = canvas.getSequence().removeROI(ROI.this);
                                    else
                                        // remove all selected ROI from the sequence
                                        result = canvas.getSequence().removeSelectedROIs(false);

                                    if (result)
                                        e.consume();
                                }
                                // roi focused ? --> delete ROI
                                else if (isFocused())
                                {
                                    // remove ROI from sequence
                                    if (canvas.getSequence().removeROI(ROI.this))
                                        e.consume();
                                }
                            }
                            break;
                    }
                }
            }
        }

        @Override
        public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isActiveFor(canvas))
            {
                // update mouse position
                if (imagePoint != null)
                    setMousePos(imagePoint);
            }
        }

        @Override
        public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            if (isActiveFor(canvas))
            {
                // update mouse position
                if (imagePoint != null)
                    setMousePos(imagePoint);
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
                setColor(new Color(XMLUtil.getElementIntValue(node, ID_COLOR, DEFAULT_COLOR.getRGB())));
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
    protected boolean readOnly;

    // attached ROI icon
    protected Image icon;

    /**
     * cached calculated properties
     */
    protected Rectangle5D cachedBounds;
    protected double cachedNumberOfPoints;
    protected double cachedNumberOfEdgePoints;
    protected boolean boundsInvalid;
    protected boolean numberOfEdgePointsInvalid;
    protected boolean numberOfPointsInvalid;

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
        readOnly = false;
        creating = false;
        focused = false;
        selected = false;

        cachedBounds = new Rectangle5D.Double();
        cachedNumberOfPoints = 0d;
        cachedNumberOfEdgePoints = 0d;
        boundsInvalid = true;
        numberOfPointsInvalid = true;
        numberOfEdgePointsInvalid = true;

        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);

        // default icon
        icon = ResourceUtil.ICON_ROI;
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
        remove(canUndo);
    }

    /**
     * @deprecated Use {@link #remove()} instead.
     */
    @Deprecated
    public void detachFromAll()
    {
        remove(false);
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
    public void remove(boolean canUndo)
    {
        final List<Sequence> sequences = Icy.getMainInterface().getSequencesContaining(this);

        for (Sequence sequence : sequences)
            sequence.removeROI(this, canUndo);
    }

    /**
     * Remove this ROI (detach from all sequence)
     */
    public void remove()
    {
        remove(true);
    }

    /**
     * @deprecated Use {@link #remove(boolean)} instead.
     */
    @Deprecated
    public void delete(boolean canUndo)
    {
        remove(canUndo);
    }

    /**
     * @deprecated Use {@link #remove()} instead.
     */
    @Deprecated
    public void delete()
    {
        remove(true);
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
     * @deprecated Use {@link #getOverlay()} instead.
     */
    @Deprecated
    public ROIPainter getPainter()
    {
        return painter;
    }

    /**
     * Returns the ROI overlay (used to draw and interact with {@link ROI} on {@link IcyCanvas})
     */
    public ROIPainter getOverlay()
    {
        return painter;
    }

    /**
     * Return the ROI painter stroke.
     */
    public double getStroke()
    {
        return getOverlay().getStroke();
    }

    /**
     * Get adjusted stroke for the current canvas transformation
     */
    public double getAdjustedStroke(IcyCanvas canvas)
    {
        return getOverlay().getAdjustedStroke(canvas);
    }

    /**
     * Set ROI painter stroke.
     */
    public void setStroke(double value)
    {
        getOverlay().setStroke(value);
    }

    /**
     * Returns the ROI painter opacity factor (0 = transparent while 1 means opaque).
     */
    public float getOpacity()
    {
        return getOverlay().getOpacity();
    }

    /**
     * Sets the ROI painter content opacity factor (0 = transparent while 1 means opaque).
     */
    public void setOpacity(float value)
    {
        getOverlay().setOpacity(value);
    }

    /**
     * Return the ROI painter focused color.
     */
    public Color getFocusedColor()
    {
        return getOverlay().getFocusedColor();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public Color getSelectedColor()
    {
        return getOverlay().getSelectedColor();
    }

    /**
     * Returns the color used to display the ROI depending its current state.
     */
    public Color getDisplayColor()
    {
        return getOverlay().getDisplayColor();
    }

    /**
     * Return the ROI painter base color.
     */
    public Color getColor()
    {
        return getOverlay().getColor();
    }

    /**
     * Set the ROI painter base color.
     */
    public void setColor(Color value)
    {
        getOverlay().setColor(value);
    }

    /**
     * @deprecated selected color is automatically calculated.
     */
    @Deprecated
    public void setSelectedColor(Color value)
    {
    }

    /**
     * @return the icon
     */
    public Image getIcon()
    {
        return icon;
    }

    /**
     * @param value
     *        the icon to set
     */
    public void setIcon(Image value)
    {
        if (icon != value)
        {
            icon = value;
            propertyChanged(PROPERTY_ICON);
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
     * Set the internal <i>creation mode</i> state.<br>
     * The ROI interaction behave differently when in <i>creation mode</i>.<br>
     * You should not set this state when you create an ROI from the code.
     */
    public void setCreating(boolean value)
    {
        if (creating != value)
        {
            creating = value;
            propertyChanged(PROPERTY_CREATING);
        }
    }

    /**
     * Returns true if the ROI has a (control) point which is currently focused/selected
     */
    public abstract boolean hasSelectedPoint();

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
     * Set the selected state of this ROI.<br>
     * Use {@link Sequence#setSelectedROI(ROI)} for exclusive ROI selection.
     * 
     * @param value
     *        the selected to set
     */
    public void setSelected(boolean value)
    {
        if (selected != value)
        {
            selected = value;
            // as soon ROI has been unselected, we're not in create mode anymore
            if (!value)
                setCreating(false);

            selectionChanged();
        }
    }

    /**
     * @deprecated Use {@link #setSelected(boolean)} or {@link Sequence#setSelectedROI(ROI)}
     *             depending you want exclusive selection or not.
     */
    @Deprecated
    public void setSelected(boolean value, boolean exclusive)
    {
        if (exclusive)
        {
            // use the sequence for ROI selection with exclusive parameter
            final List<Sequence> attachedSeqs = Icy.getMainInterface().getSequencesContaining(this);

            for (Sequence seq : attachedSeqs)
                seq.setSelectedROI(value ? this : null);
        }
        else
            setSelected(value);
    }

    /**
     * @deprecated Use {@link #setSelected(boolean)} instead.
     */
    @Deprecated
    public void internalUnselect()
    {
        if (selected != false)
        {
            selected = false;
            // as soon ROI has been unselected, we're not in create mode anymore
            setCreating(false);
            selectionChanged();
        }
    }

    /**
     * @deprecated Use {@link #setSelected(boolean)} instead.
     */
    @Deprecated
    public void internalSelect()
    {
        if (selected != true)
        {
            selected = true;
            selectionChanged();
        }
    }

    /**
     * @deprecated Use {@link #isReadOnly()} instead.
     */
    @Deprecated
    public boolean isEditable()
    {
        return !isReadOnly();
    }

    /**
     * @deprecated Use {@link #setReadOnly(boolean)} instead.
     */
    @Deprecated
    public void setEditable(boolean value)
    {
        setReadOnly(!value);
    }

    /**
     * Return true if ROI is in <i>read only</i> state (cannot be modified from GUI).
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }

    /**
     * Set the <i>read only</i> state of ROI.
     */
    public void setReadOnly(boolean value)
    {
        if (readOnly != value)
        {
            readOnly = value;

            propertyChanged(PROPERTY_READONLY);
            if (value)
                setSelected(false);
        }
    }

    /**
     * Return true if the ROI is active for the specified canvas.
     */
    public abstract boolean isActiveFor(IcyCanvas canvas);

    /**
     * Calculate and returns the bounding box of the <code>ROI</code>.<br>
     * This method is used by {@link #getBounds5D()} which should try to cache the result as the
     * bounding box calculation can take some computation time for complex ROI.
     */
    public abstract Rectangle5D computeBounds5D();

    /**
     * Returns the bounding box of the <code>ROI</code>. Note that there is no guarantee that the
     * returned {@link Rectangle5D} is the smallest bounding box that encloses the <code>ROI</code>,
     * only that the <code>ROI</code> lies entirely within the indicated <code>Rectangle5D</code>.
     * 
     * @return an instance of <code>Rectangle5D</code> that is a bounding box of the
     *         <code>ROI</code>.
     * @see #computeBounds5D()
     */
    public final Rectangle5D getBounds5D()
    {
        // we need to recompute bounds
        if (boundsInvalid)
        {
            cachedBounds = computeBounds5D();
            boundsInvalid = false;
        }

        return (Rectangle5D) cachedBounds.clone();
    }

    /**
     * Returns the ROI position which normally correspond to the <i>minimum</i> point of the ROI
     * bounds.<br>
     * 
     * @see #getBounds5D()
     */
    public Point5D getPosition5D()
    {
        return getBounds5D().getPosition();
    }

    /**
     * Returns <code>true</code> if this ROI accepts bounds change through the
     * {@link #setBounds5D(Rectangle5D)} method.
     */
    public abstract boolean canSetBounds();

    /**
     * Returns <code>true</code> if this ROI accepts position change through the
     * {@link #setPosition5D(Point5D)} method.
     */
    public abstract boolean canSetPosition();

    /**
     * Set the <code>ROI</code> bounds.<br>
     * Note that not all ROI supports bounds modification and you should call
     * {@link #canSetBounds()} first to test if the operation is supported.<br>
     * 
     * @param bounds
     *        new ROI bounds
     */
    public abstract void setBounds5D(Rectangle5D bounds);

    /**
     * Set the <code>ROI</code> position.<br>
     * Note that not all ROI supports position modification and you should call
     * {@link #canSetPosition()} first to test if the operation is supported.<br>
     * 
     * @param position
     *        new ROI position
     */
    public abstract void setPosition5D(Point5D position);

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
     * Tests if the <code>ROI</code> entirely contains the specified 5D rectangular area. All
     * coordinates that lie inside the rectangular area must lie within the <code>ROI</code> for the
     * entire rectangular area to be considered contained within the <code>ROI</code>.
     * <p>
     * The {@code ROI.contains()} method allows a {@code ROI} implementation to conservatively
     * return {@code false} when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the
     * rectangular area are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code false} even though the
     * {@code ROI} contains the rectangular area.
     * 
     * @param x
     *        the X coordinate of the start corner of the specified rectangular area
     * @param y
     *        the Y coordinate of the start corner of the specified rectangular area
     * @param z
     *        the Z coordinate of the start corner of the specified rectangular area
     * @param t
     *        the T coordinate of the start corner of the specified rectangular area
     * @param c
     *        the C coordinate of the start corner of the specified rectangular area
     * @param sizeX
     *        the X size of the specified rectangular area
     * @param sizeY
     *        the Y size of the specified rectangular area
     * @param sizeZ
     *        the Z size of the specified rectangular area
     * @param sizeT
     *        the T size of the specified rectangular area
     * @param sizeC
     *        the C size of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>ROI</code> entirely contains the
     *         specified rectangular area; <code>false</code> otherwise or, if the <code>ROI</code>
     *         contains the rectangular area and the <code>intersects</code> method returns
     *         <code>true</code> and the containment calculations would be too expensive to perform.
     */
    public abstract boolean contains(double x, double y, double z, double t, double c, double sizeX, double sizeY,
            double sizeZ, double sizeT, double sizeC);

    /**
     * Tests if the <code>ROI</code> entirely contains the specified <code>Rectangle5D</code>. The
     * {@code ROI.contains()} method allows a implementation to conservatively return {@code false}
     * when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the
     * <code>Rectangle2D</code> are prohibitively expensive.
     * </ul>
     * This means that for some ROIs this method might return {@code false} even though the
     * {@code ROI} contains the {@code Rectangle5D}.
     * 
     * @param r
     *        The specified <code>Rectangle5D</code>
     * @return <code>true</code> if the interior of the <code>ROI</code> entirely contains the
     *         <code>Rectangle5D</code>; <code>false</code> otherwise or, if the <code>ROI</code>
     *         contains the <code>Rectangle5D</code> and the <code>intersects</code> method returns
     *         <code>true</code> and the containment calculations would be too expensive to perform.
     * @see #contains(double, double, double, double, double, double, double, double, double,
     *      double)
     */
    public boolean contains(Rectangle5D r)
    {
        return contains(r.getX(), r.getY(), r.getZ(), r.getT(), r.getC(), r.getSizeX(), r.getSizeY(), r.getSizeZ(),
                r.getSizeT(), r.getSizeC());
    }

    /**
     * Tests if the <code>ROI</code> entirely contains the specified <code>ROI</code>.
     * 
     * @return <code>true</code> if the interior of the <code>ROI</code> entirely contains the
     *         specified <code>ROI</code>; <code>false</code> otherwise.
     */
    public abstract boolean contains(ROI roi);

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
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified
     * <code>ROI</code>
     * 
     * @return <code>true</code> if the interior of both <code>ROI</code> intersect;
     *         <code>false</code> otherwise.
     */
    public abstract boolean intersects(ROI roi);

    /**
     * Returns the boolean array mask for the specified rectangular region at specified C, Z, T
     * position.<br>
     * <br>
     * If pixel (x1, y1, c, z, t) is contained in the roi:<br>
     * <code>&nbsp result[((y1 - y) * width) + (x1 - x)] = true</code><br>
     * If pixel (x1, y1, c, z, t) is not contained in the roi:<br>
     * <code>&nbsp result[((y1 - y) * width) + (x1 - x)] = false</code><br>
     * 
     * @param x
     *        the X coordinate of the upper-left corner of the specified rectangular region
     * @param y
     *        the Y coordinate of the upper-left corner of the specified rectangular region
     * @param width
     *        the width of the specified rectangular region
     * @param height
     *        the height of the specified rectangular region
     * @param z
     *        Z position we want to retrieve the boolean mask
     * @param t
     *        T position we want to retrieve the boolean mask
     * @param c
     *        C position we want to retrieve the boolean mask
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     * @return the boolean bitmap mask
     */
    public boolean[] getBooleanMask2D(int x, int y, int width, int height, int z, int t, int c, boolean inclusive)
    {
        final boolean[] result = new boolean[width * height];

        // simple and basic implementation, override it to have better performance
        int offset = 0;
        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                if (inclusive)
                    result[offset] = intersects(x + i, y + j, z, t, c, 1d, 1d, 1d, 1d, 1d);
                else
                    result[offset] = contains(x + i, y + j, z, t, c, 1d, 1d, 1d, 1d, 1d);
                offset++;
            }
        }

        return result;
    }

    /**
     * Get the boolean bitmap mask for the specified rectangular area of the roi and for the
     * specified Z,T position.<br>
     * if the pixel (x,y) is contained in the roi Z,T position then result[(y * width) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi Z,T position then result[(y * width) + x] =
     * false
     * 
     * @param rect
     *        2D rectangular area we want to retrieve the boolean mask
     * @param z
     *        Z position we want to retrieve the boolean mask
     * @param t
     *        T position we want to retrieve the boolean mask
     * @param c
     *        C position we want to retrieve the boolean mask
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public boolean[] getBooleanMask2D(Rectangle rect, int z, int t, int c, boolean inclusive)
    {
        return getBooleanMask2D(rect.x, rect.y, rect.width, rect.height, z, t, c, inclusive);
    }

    /**
     * Returns the {@link BooleanMask2D} object representing the XY plan content at specified Z, T,
     * C position.<br>
     * <br>
     * If pixel (x, y, c, z, t) is contained in the roi:<br>
     * <code>&nbsp mask[(y - bounds.y) * bounds.width) + (x - bounds.x)] = true</code> <br>
     * If pixel (x, y, c, z, t) is not contained in the roi:<br>
     * <code>&nbsp mask[(y - bounds.y) * bounds.width) + (x - bounds.x)] = false</code>
     * 
     * @param z
     *        Z position we want to retrieve the boolean mask
     * @param t
     *        T position we want to retrieve the boolean mask
     * @param c
     *        C position we want to retrieve the boolean mask
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask2D getBooleanMask2D(int z, int t, int c, boolean inclusive)
    {
        final Rectangle bounds2D = getBounds5D().toRectangle2D().getBounds();

        // no mask
        if (bounds2D.isEmpty())
            return null;

        return new BooleanMask2D(bounds2D, getBooleanMask2D(bounds2D.x, bounds2D.y, bounds2D.width, bounds2D.height, z,
                t, c, inclusive));
    }

    /**
     * Compute the resulting bounds for <i>union</i> operation with the specified ROI.<br>
     * It returns <code>null</code> or throw an exception if the <i>union</i> operation cannot be
     * done (incompatible dimension).
     */
    protected Rectangle5D getUnionBounds(ROI roi, boolean throwException) throws UnsupportedOperationException
    {
        final Rectangle5D bounds1 = getBounds5D();

        if (roi == null)
            return bounds1;

        final Rectangle5D bounds2 = roi.getBounds5D();

        // init infinite dim infos
        final boolean ic1 = bounds1.getSizeC() == Double.POSITIVE_INFINITY;
        final boolean ic2 = bounds2.getSizeC() == Double.POSITIVE_INFINITY;
        final boolean it1 = bounds1.getSizeT() == Double.POSITIVE_INFINITY;
        final boolean it2 = bounds2.getSizeT() == Double.POSITIVE_INFINITY;
        final boolean iz1 = bounds1.getSizeZ() == Double.POSITIVE_INFINITY;
        final boolean iz2 = bounds2.getSizeZ() == Double.POSITIVE_INFINITY;

        // cannot process union when we have an infinite dimension with a finite one
        if ((ic1 ^ ic2) || (it1 ^ it2) || (iz1 ^ iz2))
        {
            if (throwException)
                throw new UnsupportedOperationException("Can't process union on ROI with different finite dimension");
            return null;
        }
        // cannot process union if C dimension is finite but T or Z is infinite
        if (!ic1 && (it1 || iz1))
        {
            if (throwException)
                throw new UnsupportedOperationException(
                        "Can't process union on ROI with a finite C dimension and infinite T or Z dimension");
            return null;
        }
        // cannot process union if T dimension is finite but Z is infinite
        if (!it1 && iz1)
        {
            if (throwException)
                throw new UnsupportedOperationException(
                        "Can't process union on ROI with a finite T dimension and infinite Z dimension");
            return null;
        }

        // do union
        Rectangle5D.union(bounds1, bounds2, bounds1);

        return bounds1;
    }

    /**
     * Compute the resulting bounds for <i>intersection</i> operation with the specified ROI.<br>
     * It returns <code>null</code> or throw an exception if the <i>intersection</i> operation
     * cannot be
     * done (incompatible dimension).
     */
    protected Rectangle5D getIntersectionBounds(ROI roi, boolean throwException) throws UnsupportedOperationException
    {
        final Rectangle5D bounds1 = getBounds5D();

        if (roi == null)
            return bounds1;

        final Rectangle5D bounds2 = roi.getBounds5D();

        // do intersection
        Rectangle5D.intersect(bounds1, bounds2, bounds1);

        // init infinite dim infos
        final boolean ic = bounds1.getSizeC() == Double.POSITIVE_INFINITY;
        final boolean it = bounds1.getSizeT() == Double.POSITIVE_INFINITY;
        final boolean iz = bounds1.getSizeZ() == Double.POSITIVE_INFINITY;

        // cannot process intersection if C dimension is finite but T or Z is infinite
        if (!ic && (it || iz))
        {
            if (throwException)
                throw new UnsupportedOperationException(
                        "Can't process intersection on ROI with a finite C dimension and infinite T or Z dimension");
            return null;
        }
        // cannot process intersection if T dimension is finite but Z is infinite
        if (!it && iz)
        {
            if (throwException)
                throw new UnsupportedOperationException(
                        "Can't process intersection on ROI with a finite T dimension and infinite Z dimension");
            return null;
        }

        return bounds1;
    }

    /**
     * Returns the effective number of dimension needed for the specified bounds.
     */
    protected int getEffectiveDimension(Rectangle5D bounds)
    {
        int result = 5;
        double size;

        size = bounds.getSizeC();
        if ((size == Double.POSITIVE_INFINITY) || (size <= 1d))
        {
            result--;
            size = bounds.getSizeT();
            if ((size == Double.POSITIVE_INFINITY) || (size <= 1d))
            {
                result--;
                size = bounds.getSizeZ();
                if ((size == Double.POSITIVE_INFINITY) || (size <= 1d))
                    result--;
            }
        }

        return result;
    }

    /**
     * Compute the specified boolean operation with the specified <code>ROI</code> and return result
     * in a new <code>ROI</code>.<br>
     * If <code>op</code> is <code>null</code> then we process subtraction operation.
     */
    /*
     * Generic implementation for ROI using the BooleanMask object so the result is just an
     * approximation. Override to optimize for specific ROI.
     */
    protected ROI computeOperation(ROI roi, BooleanOperator op) throws UnsupportedOperationException
    {
        if (roi == null)
            return this;

        final Rectangle5D bounds5D;

        if (op == null)
            bounds5D = getBounds5D();
        else if (op == BooleanOperator.AND)
            bounds5D = getIntersectionBounds(roi, true);
        else
            bounds5D = getUnionBounds(roi, true);

        final int dim = getEffectiveDimension(bounds5D);

        // we want integer bounds now
        final Rectangle5D.Integer bounds = bounds5D.toInteger();

        final int sizeZ;
        final int sizeT;
        final int sizeC;

        switch (dim)
        {
            case 2: // XY ROI with fixed ZTC
                sizeZ = 1;
                sizeT = 1;
                sizeC = 1;
                break;

            case 3: // XYZ ROI with fixed TC
                sizeZ = bounds.z;
                sizeT = 1;
                sizeC = 1;
                break;

            case 4: // XYZT ROI with fixed C
                sizeZ = bounds.z;
                sizeT = bounds.t;
                sizeC = 1;
                break;

            default: // XYZTC ROI
                sizeZ = bounds.z;
                sizeT = bounds.t;
                sizeC = bounds.c;
                break;
        }

        // get 3D and 4D bounds
        Rectangle3D.Integer bounds3D = (Rectangle3D.Integer) bounds.toRectangle3D();
        Rectangle4D.Integer bounds4D = (Rectangle4D.Integer) bounds.toRectangle4D();

        final BooleanMask4D mask5D[] = new BooleanMask4D[sizeC];

        for (int c = 0; c < sizeC; c++)
        {
            final BooleanMask3D mask4D[] = new BooleanMask3D[sizeT];

            for (int t = 0; t < sizeT; t++)
            {
                final BooleanMask2D mask3D[] = new BooleanMask2D[sizeZ];

                // process union
                for (int z = 0; z < sizeZ; z++)
                {
                    // special case for subtraction
                    if (op == null)
                    {
                        mask3D[z] = BooleanMask2D.getSubtraction(
                                getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
                                roi.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true));
                    }
                    else if (op == BooleanOperator.AND)
                    {
                        mask3D[z] = BooleanMask2D.getIntersection(
                                roi.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
                                getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true));
                    }
                    else if (op == BooleanOperator.OR)
                    {
                        mask3D[z] = BooleanMask2D.getUnion(
                                roi.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
                                getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true));
                    }
                    else if (op == BooleanOperator.XOR)
                    {
                        mask3D[z] = BooleanMask2D.getExclusiveUnion(
                                roi.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
                                getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true));
                    }
                }

                mask4D[t] = new BooleanMask3D(bounds3D, mask3D);
            }

            mask5D[c] = new BooleanMask4D(bounds4D, mask4D);
        }

        // build the 5D result ROI
        final BooleanMask5D mask = new BooleanMask5D(bounds, mask5D);

        // optimize bounds of the new created mask
        mask.optimizeBounds();

        final ROI result;

        switch (dim)
        {
            case 2: // XY ROI with fixed ZTC
                result = new plugins.kernel.roi.roi2d.ROI2DArea(mask.getMask2D(bounds.z, bounds.t, bounds.c));
                break;
            case 3: // XYZ ROI with fixed TC
                result = new ROI3DArea(mask.getMask3D(bounds.t, bounds.c));
                break;
            case 4: // XYZT ROI with fixed C
                result = new ROI4DArea(mask.getMask4D(bounds.c));
                break;
            case 5: // XYZTC ROI
                result = new ROI5DArea(mask);
                break;
            default:
                result = null;
                break;
        }

        if (op == null)
            result.setName("Substraction");
        else if (op == BooleanOperator.AND)
            result.setName("Intersection");
        else if (op == BooleanOperator.OR)
            result.setName("Union");
        else if (op == BooleanOperator.XOR)
            result.setName("Exclusive union");

        return result;
    }

    /**
     * Compute the boolean operation with specified <code>ROI</code> and return result in a new
     * <code>ROI</code>.
     */
    public ROI merge(ROI roi, BooleanOperator op) throws UnsupportedOperationException
    {
        if (op == null)
            return null;

        return computeOperation(roi, op);
    }

    /**
     * Compute union with specified <code>ROI</code> and return result in a new <code>ROI</code>.
     */
    /*
     * Generic implementation for ROI using the BooleanMask object so the result is just an
     * approximation. Override to optimize for specific ROI.
     */
    public ROI getUnion(ROI roi) throws UnsupportedOperationException
    {
        return computeOperation(roi, BooleanOperator.OR);
    }

    /**
     * Compute intersection with specified <code>ROI</code> and return result in a new
     * <code>ROI</code>.
     */
    /*
     * Generic implementation for ROI using the BooleanMask object so the result is just an
     * approximation. Override to optimize for specific ROI.
     */
    public ROI getIntersection(ROI roi) throws UnsupportedOperationException
    {
        return computeOperation(roi, BooleanOperator.AND);
    }

    /**
     * Compute exclusive union with specified <code>ROI</code> and return result in a new
     * <code>ROI</code>.
     */
    /*
     * Generic implementation for ROI using the BooleanMask object so the result is just an
     * approximation. Override to optimize for specific ROI.
     */
    public ROI getExclusiveUnion(ROI roi) throws UnsupportedOperationException
    {
        return computeOperation(roi, BooleanOperator.XOR);
    }

    /**
     * Subtract the specified <code>ROI</code> and return result in a new <code>ROI</code>.
     */
    /*
     * Generic implementation for ROI using the BooleanMask object so the result is just an
     * approximation. Override to optimize for specific ROI.
     */
    public ROI getSubtraction(ROI roi) throws UnsupportedOperationException
    {
        return computeOperation(roi, null);
    }

    /**
     * Compute and returns the number of point (pixel) composing the ROI edges.
     */
    /*
     * Override this method to adapt and optimize for a specific ROI.
     */
    public abstract double computeNumberOfEdgePoints();

    /**
     * Returns the number of point (pixel) composing the ROI edges.<br>
     * It is used to calculate the perimeter (2D) or surface area (3D) of the ROI.
     * 
     * @see #computeNumberOfEdgePoints()
     */
    public final double getNumberOfEdgePoints()
    {
        // we need to recompute the number of edge point
        if (numberOfEdgePointsInvalid)
        {
            cachedNumberOfEdgePoints = computeNumberOfEdgePoints();
            numberOfEdgePointsInvalid = false;
        }

        return cachedNumberOfEdgePoints;
    }

    /**
     * Compute and returns the number of point (pixel) contained in the ROI.
     */
    /*
     * Override this method to adapt and optimize for a specific ROI.
     */
    public abstract double computeNumberOfPoints();

    /**
     * Returns the number of point (pixel) contained in the ROI.<br>
     * It is used to calculate the area (2D) or volume (3D) of the ROI.
     */
    public final double getNumberOfPoints()
    {
        // we need to recompute the number of point
        if (numberOfPointsInvalid)
        {
            cachedNumberOfPoints = computeNumberOfPoints();
            numberOfPointsInvalid = false;
        }

        return cachedNumberOfPoints;
    }

    /**
     * @deprecated Only for ROI2D object, Use {@link #getNumberOfEdgePoints()} instead.
     */
    @Deprecated
    public double getPerimeter()
    {
        return getNumberOfEdgePoints();
    }

    /**
     * @deprecated Only for ROI3D object, use {@link #getNumberOfPoints()} instead for other type of
     *             ROI.
     */
    @Deprecated
    public double getVolume()
    {
        return getNumberOfPoints();
    }

    /**
     * @deprecated Use <code>getOverlay().setMousePos(..)</code> instead.
     */
    @Deprecated
    public void setMousePos(Point2D pos)
    {
        if (pos != null)
            getOverlay().setMousePos(new Point5D.Double(pos.getX(), pos.getY(), -1, -1, -1));
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
            setSelected(XMLUtil.getElementBooleanValue(node, ID_SELECTED, false));
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
        XMLUtil.setElementValue(node, ID_NAME, getName());
        XMLUtil.setElementBooleanValue(node, ID_SELECTED, isSelected());
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
                // cached properties need to be recomputed
                boundsInvalid = true;
                numberOfEdgePointsInvalid = true;
                numberOfPointsInvalid = true;
                painter.painterChanged();
                break;

            case SELECTION_CHANGED:
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
