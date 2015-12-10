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
import icy.preferences.GeneralPreferences;
import icy.resource.ResourceUtil;
import icy.roi.ROIEvent.ROIEventType;
import icy.roi.ROIEvent.ROIPointEventType;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.type.dimension.Dimension5D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle4D;
import icy.type.rectangle.Rectangle5D;
import icy.util.ClassUtil;
import icy.util.ColorUtil;
import icy.util.EventUtil;
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

import plugins.kernel.roi.roi2d.ROI2DArea;
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

    public static class ROINameComparator implements Comparator<ROI>
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

            return roi1.getName().compareTo(roi2.getName());
        }
    }

    public static final String ID_ROI = "roi";

    public static final String ID_CLASSNAME = "classname";
    public static final String ID_ID = "id";
    public static final String ID_NAME = "name";
    public static final String ID_COLOR = "color";
    public static final String ID_STROKE = "stroke";
    public static final String ID_OPACITY = "opacity";
    public static final String ID_SELECTED = "selected";
    public static final String ID_READONLY = "readOnly";
    public static final String ID_SHOWNAME = "showName";

    public static final ROIIdComparator idComparator = new ROIIdComparator();
    public static final ROINameComparator nameComparator = new ROINameComparator();

    public static final double DEFAULT_STROKE = 2;
    public static final Color DEFAULT_COLOR = Color.GREEN;
    public static final float DEFAULT_OPACITY = 0.3f;

    /**
     * @deprecated Use {@link #DEFAULT_COLOR} instead.
     */
    @Deprecated
    public static final Color DEFAULT_NORMAL_COLOR = DEFAULT_COLOR;

    public static final String PROPERTY_NAME = ID_NAME;
    public static final String PROPERTY_ICON = "icon";
    public static final String PROPERTY_CREATING = "creating";
    public static final String PROPERTY_READONLY = ID_READONLY;
    public static final String PROPERTY_SHOWNAME = ID_SHOWNAME;
    public static final String PROPERTY_COLOR = ID_COLOR;
    public static final String PROPERTY_STROKE = ID_STROKE;
    public static final String PROPERTY_OPACITY = ID_OPACITY;

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
        catch (NoSuchMethodException e)
        {
            IcyExceptionHandler.handleException(new NoSuchMethodException("Default constructor not found in class '"
                    + className + "', cannot create the ROI."), true);
        }
        catch (ClassNotFoundException e)
        {
            IcyExceptionHandler.handleException(new ClassNotFoundException("Cannot find '" + className
                    + "' class, cannot create the ROI."), true);
        }
        catch (Exception e)
        {
            IcyExceptionHandler.handleException(e, true);
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
            IcyExceptionHandler.handleException(new NoSuchMethodException("Default constructor not found in class '"
                    + className + "', cannot create the ROI."), true);
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
            // error while loading infos --> return null
            if (!roi.loadFromXML(node))
                return null;

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
     * Return the number of ROI defined in the specified XML node.
     * 
     * @param node
     *        XML node defining the ROI list
     * @return the number of ROI defined in the XML node.
     */
    public static int getROICount(Node node)
    {
        if (node != null)
        {
            final List<Node> nodesROI = XMLUtil.getChildren(node, ID_ROI);
            if (nodesROI != null)
                return nodesROI.size();
        }

        return 0;
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
            final List<Node> nodesROI = XMLUtil.getChildren(node, ID_ROI);

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

                if (!roi.saveToXML(nodeROI))
                {
                    XMLUtil.removeNode(node, nodeROI);
                    System.err.println("Error: the roi " + roi.getName() + "s was not correctly saved to XML !");
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

    public static Color getDefaultColor()
    {
        return new Color(GeneralPreferences.getPreferencesRoiOverlay().getInt(ID_COLOR, DEFAULT_COLOR.getRGB()));
    }

    public static float getDefaultOpacity()
    {
        return GeneralPreferences.getPreferencesRoiOverlay().getFloat(ID_OPACITY, DEFAULT_OPACITY);
    }

    public static double getDefaultStroke()
    {
        return GeneralPreferences.getPreferencesRoiOverlay().getDouble(ID_STROKE, DEFAULT_STROKE);
    }

    public static boolean getDefaultShowName()
    {
        return GeneralPreferences.getPreferencesRoiOverlay().getBoolean(ID_SHOWNAME, false);
    }

    public static void setDefaultColor(Color value)
    {
        GeneralPreferences.getPreferencesRoiOverlay().putInt(ID_COLOR, value.getRGB());
    }

    public static void setDefaultOpacity(float value)
    {
        GeneralPreferences.getPreferencesRoiOverlay().putFloat(ID_OPACITY, value);
    }

    public static void setDefaultStroke(double value)
    {
        GeneralPreferences.getPreferencesRoiOverlay().putDouble(ID_STROKE, value);
    }

    public static void setDefaultShowName(boolean value)
    {
        GeneralPreferences.getPreferencesRoiOverlay().putBoolean(ID_SHOWNAME, value);
    }

    /**
     * Returns the effective number of dimension needed for the specified bounds.
     */
    protected static int getEffectiveDimension(Rectangle5D bounds)
    {
        int result = 5;

        if (bounds.isInfiniteC() || (bounds.getSizeC() <= 1d))
        {
            result--;
            if (bounds.isInfiniteT() || (bounds.getSizeT() <= 1d))
            {
                result--;
                if (bounds.isInfiniteZ() || (bounds.getSizeZ() <= 1d))
                    result--;
            }
        }

        return result;
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
        protected boolean showName;

        /**
         * Last mouse position (image coordinates).
         * Needed for some internals operation
         */
        protected final Point5D.Double mousePos;

        public ROIPainter()
        {
            super("ROI painter", OverlayPriority.SHAPE_NORMAL);

            stroke = getDefaultStroke();
            color = getDefaultColor();
            opacity = getDefaultOpacity();
            showName = getDefaultShowName();

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
                // painter changed event is done on property changed
                ROI.this.propertyChanged(PROPERTY_STROKE);
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
                // painter changed event is done on property changed
                ROI.this.propertyChanged(PROPERTY_OPACITY);
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
            if ((color != null) && (color != value))
            {
                color = value;
                // painter changed event is done on property changed
                ROI.this.propertyChanged(PROPERTY_COLOR);
            }
        }

        /**
         * Return <code>true</code> if ROI painter should display the ROI name at draw time.<br>
         */
        public boolean getShowName()
        {
            return showName;
        }

        /**
         * When set to <code>true</code> the ROI painter display the ROI name at draw time.
         */
        public void setShowName(boolean value)
        {
            if (showName != value)
            {
                showName = value;
                ROI.this.propertyChanged(PROPERTY_SHOWNAME);
            }
        }

        /**
         * @deprecated Selected color is now automatically calculated
         */
        @Deprecated
        public void setSelectedColor(Color value)
        {
            //
        }

        /**
         * @deprecated Better to retrieve mouse position from the {@link IcyCanvas} object.
         */
        @Deprecated
        public Point5D.Double getMousePos()
        {
            return mousePos;
        }

        /**
         * @deprecated Better to retrieve mouse position from the {@link IcyCanvas} object.
         */
        @Deprecated
        public void setMousePos(Point5D pos)
        {
            if (!mousePos.equals(pos))
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
            if (!e.isConsumed())
            {
                if (isActiveFor(canvas))
                {
                    switch (e.getKeyCode())
                    {
                        case KeyEvent.VK_ESCAPE:
                            // roi selected ? --> global unselect ROI
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

                                    // if (isFocused())
                                    // // remove ROI from sequence
                                    // result = canvas.getSequence().removeROI(ROI.this);
                                    // else
                                    // remove all selected ROI from the sequence
                                    result = canvas.getSequence().removeSelectedROIs(false, true);

                                    if (result)
                                        e.consume();
                                }
                                // roi focused ? --> delete ROI
                                else if (isFocused())
                                {
                                    // remove ROI from sequence
                                    if (canvas.getSequence().removeROI(ROI.this, true))
                                        e.consume();
                                }
                            }
                            break;
                    }

                    // control modifier is used for ROI modification from keyboard
                    if (EventUtil.isMenuControlDown(e) && isSelected() && !isReadOnly())
                    {
                        switch (e.getKeyCode())
                        {
                            case KeyEvent.VK_LEFT:
                                if (EventUtil.isAltDown(e))
                                {
                                    // resize
                                    if (canSetBounds())
                                    {
                                        final Rectangle5D bnd = getBounds5D();
                                        if (EventUtil.isShiftDown(e))
                                            bnd.setSizeX(Math.max(1, bnd.getSizeX() - 10));
                                        else
                                            bnd.setSizeX(Math.max(1, bnd.getSizeX() - 1));
                                        setBounds5D(bnd);
                                        e.consume();
                                    }
                                }
                                else
                                {
                                    // move
                                    if (canSetPosition())
                                    {
                                        final Point5D pos = getPosition5D();
                                        if (EventUtil.isShiftDown(e))
                                            pos.setX(pos.getX() - 10);
                                        else
                                            pos.setX(pos.getX() - 1);
                                        setPosition5D(pos);
                                        e.consume();
                                    }
                                }
                                break;

                            case KeyEvent.VK_RIGHT:
                                if (EventUtil.isAltDown(e))
                                {
                                    // resize
                                    if (canSetBounds())
                                    {
                                        final Rectangle5D bnd = getBounds5D();
                                        if (EventUtil.isShiftDown(e))
                                            bnd.setSizeX(Math.max(1, bnd.getSizeX() + 10));
                                        else
                                            bnd.setSizeX(Math.max(1, bnd.getSizeX() + 1));
                                        setBounds5D(bnd);
                                        e.consume();
                                    }
                                }
                                else
                                {
                                    // move
                                    if (canSetPosition())
                                    {
                                        final Point5D pos = getPosition5D();
                                        if (EventUtil.isShiftDown(e))
                                            pos.setX(pos.getX() + 10);
                                        else
                                            pos.setX(pos.getX() + 1);
                                        setPosition5D(pos);
                                        e.consume();
                                    }
                                }
                                break;

                            case KeyEvent.VK_UP:
                                if (EventUtil.isAltDown(e))
                                {
                                    // resize
                                    if (canSetBounds())
                                    {
                                        final Rectangle5D bnd = getBounds5D();
                                        if (EventUtil.isShiftDown(e))
                                            bnd.setSizeY(Math.max(1, bnd.getSizeY() - 10));
                                        else
                                            bnd.setSizeY(Math.max(1, bnd.getSizeY() - 1));
                                        setBounds5D(bnd);
                                        e.consume();
                                    }
                                }
                                else
                                {
                                    // move
                                    if (canSetPosition())
                                    {
                                        final Point5D pos = getPosition5D();
                                        if (EventUtil.isShiftDown(e))
                                            pos.setY(pos.getY() - 10);
                                        else
                                            pos.setY(pos.getY() - 1);
                                        setPosition5D(pos);
                                        e.consume();
                                    }
                                }
                                break;

                            case KeyEvent.VK_DOWN:
                                if (EventUtil.isAltDown(e))
                                {
                                    // resize
                                    if (canSetBounds())
                                    {
                                        final Rectangle5D bnd = getBounds5D();
                                        if (EventUtil.isShiftDown(e))
                                            bnd.setSizeY(Math.max(1, bnd.getSizeY() + 10));
                                        else
                                            bnd.setSizeY(Math.max(1, bnd.getSizeY() + 1));
                                        setBounds5D(bnd);
                                        e.consume();
                                    }
                                }
                                else
                                {
                                    // move
                                    if (canSetPosition())
                                    {
                                        final Point5D pos = getPosition5D();
                                        if (EventUtil.isShiftDown(e))
                                            pos.setY(pos.getY() + 10);
                                        else
                                            pos.setY(pos.getY() + 1);
                                        setPosition5D(pos);
                                        e.consume();
                                    }
                                }
                                break;
                        }
                    }
                }
            }

            // this allow to keep the backward compatibility
            super.keyPressed(e, imagePoint, canvas);
        }

        @Override
        public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // this allow to keep the backward compatibility
            super.mouseDrag(e, imagePoint, canvas);
        }

        @Override
        public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // this allow to keep the backward compatibility
            super.mouseMove(e, imagePoint, canvas);
        }

        @Override
        public boolean loadFromXML(Node node)
        {
            if (node == null)
                return false;

            beginUpdate();
            try
            {
                setColor(new Color(XMLUtil.getElementIntValue(node, ID_COLOR, getDefaultColor().getRGB())));
                setStroke(XMLUtil.getElementDoubleValue(node, ID_STROKE, getDefaultStroke()));
                setOpacity(XMLUtil.getElementFloatValue(node, ID_OPACITY, getDefaultOpacity()));
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
    protected double cachedNumberOfContourPoints;
    protected boolean boundsInvalid;
    protected boolean numberOfContourPointsInvalid;
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
        cachedNumberOfContourPoints = 0d;
        boundsInvalid = true;
        numberOfPointsInvalid = true;
        numberOfContourPointsInvalid = true;

        listeners = new EventListenerList();
        updater = new UpdateEventHandler(this, false);

        // default icon
        icon = ResourceUtil.ICON_ROI;
    }

    protected abstract ROIPainter createPainter();

    /**
     * Returns the number of dimension of the ROI:<br>
     * 2 for ROI2D<br>
     * 3 for ROI3D<br>
     * 4 for ROI4D<br>
     * 5 for ROI5D<br>
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
        //
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
     * Generic way to retrieve a ROI property value.<br>
     * Returns <code>null</code> if property name is invalid.
     * 
     * @param propertyName
     *        property name (for instance {@link #PROPERTY_COLOR})
     */
    public Object getPropertyValue(String propertyName)
    {
        if (StringUtil.equals(propertyName, PROPERTY_COLOR))
            return getColor();
        if (StringUtil.equals(propertyName, PROPERTY_CREATING))
            return Boolean.valueOf(isCreating());
        if (StringUtil.equals(propertyName, PROPERTY_ICON))
            return getIcon();
        if (StringUtil.equals(propertyName, PROPERTY_NAME))
            return getName();
        if (StringUtil.equals(propertyName, PROPERTY_OPACITY))
            return Float.valueOf(getOpacity());
        if (StringUtil.equals(propertyName, PROPERTY_READONLY))
            return Boolean.valueOf(isReadOnly());
        if (StringUtil.equals(propertyName, PROPERTY_SHOWNAME))
            return Boolean.valueOf(getShowName());
        if (StringUtil.equals(propertyName, PROPERTY_STROKE))
            return Double.valueOf(getStroke());

        return null;
    }

    /**
     * Generic way to set ROI property value.
     * 
     * @param propertyName
     *        property name (for instance {@value #PROPERTY_COLOR})
     * @param value
     *        the value to set in the property (for instance Color.red for {@link #PROPERTY_COLOR})
     */
    public void setPropertyValue(String propertyName, Object value)
    {
        if (StringUtil.equals(propertyName, PROPERTY_COLOR))
            setColor((Color) value);
        if (StringUtil.equals(propertyName, PROPERTY_CREATING))
            setCreating(((Boolean) value).booleanValue());
        if (StringUtil.equals(propertyName, PROPERTY_ICON))
            setIcon((Image) value);
        if (StringUtil.equals(propertyName, PROPERTY_NAME))
            setName((String) value);
        if (StringUtil.equals(propertyName, PROPERTY_OPACITY))
            setOpacity(((Float) value).floatValue());
        if (StringUtil.equals(propertyName, PROPERTY_READONLY))
            setReadOnly(((Boolean) value).booleanValue());
        if (StringUtil.equals(propertyName, PROPERTY_SHOWNAME))
            setShowName(((Boolean) value).booleanValue());
        if (StringUtil.equals(propertyName, PROPERTY_STROKE))
            setStroke(((Double) value).doubleValue());
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
            final List<Sequence> attachedSeqs = Icy.getMainInterface().getSequencesContaining(this);

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
     * @deprecated Use {@link #setSelected(boolean)} or {@link Sequence#setSelectedROI(ROI)} depending you want
     *             exclusive selection or not.
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
     * Return <code>true</code> if ROI should display its name at draw time.<br>
     */
    public boolean getShowName()
    {
        return getOverlay().getShowName();
    }

    /**
     * Set the <i>show name</i> property of ROI.<br>
     * When set to <code>true</code> the ROI shows its name at draw time.
     */
    public void setShowName(boolean value)
    {
        getOverlay().setShowName(value);
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
     * @return an instance of <code>Rectangle5D</code> that is a bounding box of the <code>ROI</code>.
     * @see #computeBounds5D()
     */
    public Rectangle5D getBounds5D()
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
     * Returns <code>true</code> if this ROI accepts bounds change through the {@link #setBounds5D(Rectangle5D)} method.
     */
    public abstract boolean canSetBounds();

    /**
     * Returns <code>true</code> if this ROI accepts position change through the {@link #setPosition5D(Point5D)} method.
     */
    public abstract boolean canSetPosition();

    /**
     * Set the <code>ROI</code> bounds.<br>
     * Note that not all ROI supports bounds modification and you should call {@link #canSetBounds()} first to test if
     * the operation is supported.<br>
     * 
     * @param bounds
     *        new ROI bounds
     */
    public abstract void setBounds5D(Rectangle5D bounds);

    /**
     * Set the <code>ROI</code> position.<br>
     * Note that not all ROI supports position modification and you should call {@link #canSetPosition()} first to test
     * if the operation is supported.<br>
     * 
     * @param position
     *        new ROI position
     */
    public abstract void setPosition5D(Point5D position);

    /**
     * Tests if a specified 5D point is inside the ROI.
     * 
     * @return <code>true</code> if the specified <code>Point5D</code> is inside the boundary of the <code>ROI</code>;
     *         <code>false</code> otherwise.
     */
    public abstract boolean contains(double x, double y, double z, double t, double c);

    /**
     * Tests if a specified {@link Point5D} is inside the ROI.
     * 
     * @param p
     *        the specified <code>Point5D</code> to be tested
     * @return <code>true</code> if the specified <code>Point2D</code> is inside the boundary of the <code>ROI</code>;
     *         <code>false</code> otherwise.
     */
    public boolean contains(Point5D p)
    {
        if (p == null)
            return false;

        return contains(p.getX(), p.getY(), p.getZ(), p.getT(), p.getC());
    }

    /**
     * Tests if the <code>ROI</code> entirely contains the specified 5D rectangular area. All
     * coordinates that lie inside the rectangular area must lie within the <code>ROI</code> for the
     * entire rectangular area to be considered contained within the <code>ROI</code>.
     * <p>
     * The {@code ROI.contains()} method allows a {@code ROI} implementation to conservatively return {@code false}
     * when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the rectangular area are
     * prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code false} even though the {@code ROI} contains
     * the rectangular area.
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
     *         specified rectangular area; <code>false</code> otherwise or, if the <code>ROI</code> contains the
     *         rectangular area and the <code>intersects</code> method returns <code>true</code> and the containment
     *         calculations would be too expensive to perform.
     */
    public abstract boolean contains(double x, double y, double z, double t, double c, double sizeX, double sizeY,
            double sizeZ, double sizeT, double sizeC);

    /**
     * Tests if the <code>ROI</code> entirely contains the specified <code>Rectangle5D</code>. The
     * {@code ROI.contains()} method allows a implementation to conservatively return {@code false} when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the
     * <code>Rectangle2D</code> are prohibitively expensive.
     * </ul>
     * This means that for some ROIs this method might return {@code false} even though the {@code ROI} contains the
     * {@code Rectangle5D}.
     * 
     * @param r
     *        The specified <code>Rectangle5D</code>
     * @return <code>true</code> if the interior of the <code>ROI</code> entirely contains the <code>Rectangle5D</code>;
     *         <code>false</code> otherwise or, if the <code>ROI</code> contains the <code>Rectangle5D</code> and the
     *         <code>intersects</code> method returns <code>true</code> and the containment calculations would be too
     *         expensive to perform.
     * @see #contains(double, double, double, double, double, double, double, double, double, double)
     */
    public boolean contains(Rectangle5D r)
    {
        if (r == null)
            return false;

        return contains(r.getX(), r.getY(), r.getZ(), r.getT(), r.getC(), r.getSizeX(), r.getSizeY(), r.getSizeZ(),
                r.getSizeT(), r.getSizeC());
    }

    /**
     * Tests if the <code>ROI</code> entirely contains the specified <code>ROI</code>.
     * WARNING: this method may be "pixel accurate" only depending the internal implementation.
     * 
     * @return <code>true</code> if the current <code>ROI</code> entirely contains the
     *         specified <code>ROI</code>; <code>false</code> otherwise.
     */
    public boolean contains(ROI roi)
    {
        // default implementation using BooleanMask
        final Rectangle5D.Integer bounds = getBounds5D().toInteger();
        final Rectangle5D.Integer roiBounds = roi.getBounds5D().toInteger();

        // simple bounds contains test
        if (bounds.contains(roiBounds))
        {
            final Rectangle5D.Integer intersection = bounds.createIntersection(roiBounds).toInteger();
            int minZ;
            int maxZ;
            int minT;
            int maxT;
            int minC;
            int maxC;

            // special infinite case
            if (intersection.isInfiniteZ())
            {
                minZ = -1;
                maxZ = -1;
            }
            else
            {
                minZ = (int) intersection.getMinZ();
                maxZ = (int) intersection.getMaxZ();
            }
            if (intersection.isInfiniteT())
            {
                minT = -1;
                maxT = -1;
            }
            else
            {
                minT = (int) intersection.getMinT();
                maxT = (int) intersection.getMaxT();
            }
            if (intersection.isInfiniteC())
            {
                minC = -1;
                maxC = -1;
            }
            else
            {
                minC = (int) intersection.getMinC();
                maxC = (int) intersection.getMaxC();
            }

            // slow method using the boolean mask
            for (int c = minC; c <= maxC; c++)
            {
                for (int t = minT; t <= maxT; t++)
                {
                    for (int z = minZ; z <= maxZ; z++)
                    {
                        BooleanMask2D mask;

                        // test first only on content
                        mask = roi.getBooleanMask2D(z, t, c, false);
                        if (!mask.isEmpty() && !getBooleanMask2D(z, t, c, false).contains(mask))
                            return false;

                        // then test on content and edge
                        mask = roi.getBooleanMask2D(z, t, c, true);
                        if (!mask.isEmpty() && !getBooleanMask2D(z, t, c, true).contains(mask))
                            return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified
     * rectangular area. The rectangular area is considered to intersect the <code>ROI</code> if any
     * point is contained in both the interior of the <code>ROI</code> and the specified rectangular
     * area.
     * <p>
     * The {@code ROI.intersects()} method allows a {@code ROI} implementation to conservatively return {@code true}
     * when:
     * <ul>
     * <li>there is a high probability that the rectangular area and the <code>ROI</code> intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the rectangular area does
     * not intersect the {@code ROI}.
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
     * The {@code ROI.intersects()} method allows a {@code ROI} implementation to conservatively return {@code true}
     * when:
     * <ul>
     * <li>there is a high probability that the rectangular area and the <code>ROI</code> intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the rectangular area does
     * not intersect the {@code ROI}.
     * 
     * @return <code>true</code> if the interior of the <code>ROI</code> and the interior of the
     *         rectangular area intersect, or are both highly likely to intersect and intersection
     *         calculations would be too expensive to perform; <code>false</code> otherwise.
     */
    public boolean intersects(Rectangle5D r)
    {
        if (r == null)
            return false;

        return intersects(r.getX(), r.getY(), r.getZ(), r.getT(), r.getC(), r.getSizeX(), r.getSizeY(), r.getSizeZ(),
                r.getSizeT(), r.getSizeC());
    }

    /**
     * Tests if the current <code>ROI</code> intersects the specified <code>ROI</code>.<br>
     * Note that this method may be "pixel accurate" only depending the internal implementation.
     * 
     * @return <code>true</code> if <code>ROI</code> intersect, <code>false</code> otherwise.
     */
    public boolean intersects(ROI roi)
    {
        // default implementation using BooleanMask
        final Rectangle5D.Integer bounds = getBounds5D().toInteger();
        final Rectangle5D.Integer roiBounds = roi.getBounds5D().toInteger();
        final Rectangle5D.Integer intersection = bounds.createIntersection(roiBounds).toInteger();

        // intersection not empty
        if (!intersection.isEmpty())
        {
            int minZ;
            int maxZ;
            int minT;
            int maxT;
            int minC;
            int maxC;

            // special infinite case
            if (intersection.isInfiniteZ())
            {
                minZ = -1;
                maxZ = -1;
            }
            else
            {
                minZ = (int) intersection.getMinZ();
                maxZ = (int) intersection.getMaxZ();
            }
            if (intersection.isInfiniteT())
            {
                minT = -1;
                maxT = -1;
            }
            else
            {
                minT = (int) intersection.getMinT();
                maxT = (int) intersection.getMaxT();
            }
            if (intersection.isInfiniteC())
            {
                minC = -1;
                maxC = -1;
            }
            else
            {
                minC = (int) intersection.getMinC();
                maxC = (int) intersection.getMaxC();
            }

            // slow method using the boolean mask
            for (int c = minC; c <= maxC; c++)
            {
                for (int t = minT; t <= maxT; t++)
                {
                    for (int z = minZ; z <= maxZ; z++)
                    {
                        if (getBooleanMask2D(z, t, c, true).intersects(roi.getBooleanMask2D(z, t, c, true)))
                            return true;
                    }
                }
            }
        }

        return false;
    }

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
     *        Z position we want to retrieve the boolean mask.<br>
     *        Set it to -1 to retrieve the mask whatever is the Z position of ROI2D.
     * @param t
     *        T position we want to retrieve the boolean mask.<br>
     *        Set it to -1 to retrieve the mask whatever is the T position of ROI2D/ROI3D.
     * @param c
     *        C position we want to retrieve the boolean mask.<br>
     *        Set it to -1 to retrieve the mask whatever is the C position of ROI2D/ROI3D/ROI4D.
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask2D getBooleanMask2D(int z, int t, int c, boolean inclusive)
    {
        final Rectangle bounds2D = getBounds5D().toRectangle2D().getBounds();

        // empty ROI --> return empty mask
        if (bounds2D.isEmpty())
            return new BooleanMask2D(new Rectangle(), new boolean[0]);

        return new BooleanMask2D(bounds2D, getBooleanMask2D(bounds2D.x, bounds2D.y, bounds2D.width, bounds2D.height, z,
                t, c, inclusive));
    }

    /**
     * Compute the resulting bounds for <i>subtraction</i> operation with the specified ROI.<br>
     * It returns <code>null</code> or throw an exception if the <i>subtraction</i> operation cannot
     * be
     * done (incompatible dimension).
     */
    protected Rectangle5D getSubtractionBounds(ROI roi, boolean throwException) throws UnsupportedOperationException
    {
        final Rectangle5D bounds1 = getBounds5D();

        if (roi == null)
            return bounds1;

        final Rectangle5D bounds2 = roi.getBounds5D();

        // init infinite dim infos
        final boolean ic1 = bounds1.isInfiniteC();
        final boolean ic2 = bounds2.isInfiniteC();
        final boolean it1 = bounds1.isInfiniteT();
        final boolean it2 = bounds2.isInfiniteT();
        final boolean iz1 = bounds1.isInfiniteZ();
        final boolean iz2 = bounds2.isInfiniteZ();

        // cannot process subtraction when we have an finite dimension on second ROI
        // while having a infinite one on the first ROI
        if (ic1 && !ic2)
        {
            if (throwException)
                throw new UnsupportedOperationException(
                        "Can't process subtraction: ROI 1 has infinite C dimension while ROI 2 has a finite one");
            return null;
        }
        if (it1 && !it2)
        {
            if (throwException)
                throw new UnsupportedOperationException(
                        "Can't process subtraction: ROI 1 has infinite T dimension while ROI 2 has a finite one");
            return null;
        }
        if (iz1 && !iz2)
        {
            if (throwException)
                throw new UnsupportedOperationException(
                        "Can't process subtraction: ROI 1 has infinite Z dimension while ROI 2 has a finite one");
            return null;
        }

        return bounds1;
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
        final boolean ic1 = bounds1.isInfiniteC();
        final boolean ic2 = bounds2.isInfiniteC();
        final boolean it1 = bounds1.isInfiniteT();
        final boolean it2 = bounds2.isInfiniteT();
        final boolean iz1 = bounds1.isInfiniteZ();
        final boolean iz2 = bounds2.isInfiniteZ();

        // cannot process union when we have an infinite dimension with a finite one
        if ((ic1 ^ ic2) || (it1 ^ it2) || (iz1 ^ iz2))
        {
            if (throwException)
                throw new UnsupportedOperationException("Can't process union on ROI with different infinite dimension");
            return null;
        }

        // do union
        Rectangle5D.union(bounds1, bounds2, bounds1);

        // init infinite dim infos on result
        final boolean ic = bounds1.isInfiniteC() || (bounds1.getSizeC() <= 1d);
        final boolean it = bounds1.isInfiniteT() || (bounds1.getSizeT() <= 1d);
        final boolean iz = bounds1.isInfiniteZ() || (bounds1.getSizeZ() <= 1d);

        // cannot process union if C dimension is finite but T or Z is infinite
        if (!ic && (it || iz))
        {
            if (throwException)
                throw new UnsupportedOperationException(
                        "Can't process union on ROI with a finite C dimension and infinite T or Z dimension");
            return null;
        }
        // cannot process union if T dimension is finite but Z is infinite
        if (!it && iz)
        {
            if (throwException)
                throw new UnsupportedOperationException(
                        "Can't process union on ROI with a finite T dimension and infinite Z dimension");
            return null;
        }

        return bounds1;
    }

    /**
     * Compute the resulting bounds for <i>intersection</i> operation with the specified ROI.<br>
     * It returns <code>null</code> or throw an exception if the <i>intersection</i> operation
     * cannot be done (incompatible dimension).
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
        final boolean ic = bounds1.isInfiniteC() || (bounds1.getSizeC() <= 1d);
        final boolean it = bounds1.isInfiniteT() || (bounds1.getSizeT() <= 1d);
        final boolean iz = bounds1.isInfiniteZ() || (bounds1.getSizeZ() <= 1d);

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
     * @deprecated Override directly these methods:<br>
     *             {@link #getUnion(ROI)}<br>
     *             {@link #getIntersection(ROI)}<br>
     *             {@link #getExclusiveUnion(ROI)}<br>
     *             {@link #getSubtraction(ROI)}<br>
     *             or use {@link #merge(ROI, BooleanOperator)} method instead.
     */
    /*
     * Generic implementation for ROI using the BooleanMask object so the result is just an
     * approximation. Override to optimize for specific ROI.
     */
    @Deprecated
    protected ROI computeOperation(ROI roi, BooleanOperator op) throws UnsupportedOperationException
    {
        System.out.println("Deprecated method " + getClassName() + ".computeOperation(ROI, BooleanOperator) called !");
        return null;
    }

    protected static Dimension5D.Integer getOpDim(int dim, Rectangle5D.Integer bounds)
    {
        final Dimension5D.Integer result = new Dimension5D.Integer();

        switch (dim)
        {
            case 2: // XY ROI with fixed ZTC
                result.sizeZ = 1;
                result.sizeT = 1;
                result.sizeC = 1;
                break;

            case 3: // XYZ ROI with fixed TC
                result.sizeZ = bounds.sizeZ;
                result.sizeT = 1;
                result.sizeC = 1;
                break;

            case 4: // XYZT ROI with fixed C
                result.sizeZ = bounds.sizeZ;
                result.sizeT = bounds.sizeT;
                result.sizeC = 1;
                break;

            default: // XYZTC ROI
                result.sizeZ = bounds.sizeZ;
                result.sizeT = bounds.sizeT;
                result.sizeC = bounds.sizeC;
                break;
        }

        return result;
    }

    /**
     * Same as {@link #merge(ROI, BooleanOperator)} except it modifies the current <code>ROI</code> to reflect the
     * result of the boolean operation with specified <code>ROI</code>.<br>
     * Note that this operation work only if the 2 ROIs are compatible for that type of operation. If that is not
     * the case a {@link UnsupportedOperationException} is thrown if <code>allowCreate</code> parameter is set to
     * <code>false</code>, if the parameter is set to <code>true</code> the result may be returned in a new created ROI.
     * 
     * @param roi
     *        the <code>ROI</code> to merge with current <code>ROI</code>
     * @param op
     *        the boolean operation to process
     * @param allowCreate
     *        if set to <code>true</code> the method will create a new ROI to return the result of the operation if it
     *        cannot be directly processed on the current <code>ROI</code>
     * @return the modified ROI or a new created ROI if the operation cannot be directly processed on the current ROI
     *         and <code>allowCreate</code> parameter was set to <code>true</code>
     * @throws UnsupportedOperationException
     *         if the two ROI cannot be merged together.
     * @see #merge(ROI, BooleanOperator)
     */
    public ROI mergeWith(ROI roi, BooleanOperator op, boolean allowCreate) throws UnsupportedOperationException
    {
        switch (op)
        {
            case AND:
                return intersect(roi, allowCreate);

            case OR:
                return add(roi, allowCreate);

            case XOR:
                return exclusiveAdd(roi, allowCreate);
        }

        return this;
    }

    /**
     * Adds content of specified <code>ROI</code> into this <code>ROI</code>.
     * The resulting content of this <code>ROI</code> will include
     * the union of both ROI's contents.<br>
     * Note that this operation work only if the 2 ROIs are compatible for that type of operation. If that is not
     * the case a {@link UnsupportedOperationException} is thrown if <code>allowCreate</code> parameter is set to
     * <code>false</code>, if the parameter is set to <code>true</code> the result may be returned in a new created ROI.
     * 
     * <pre>
     *     // Example:
     *      roi1 (before)     +         roi2       =    roi1 (after)
     * 
     *     ################     ################     ################
     *     ##############         ##############     ################
     *     ############             ############     ################
     *     ##########                 ##########     ################
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     * 
     * @param roi
     *        the <code>ROI</code> to be added to the current <code>ROI</code>
     * @param allowCreate
     *        if set to <code>true</code> the method will create a new ROI to return the result of the operation if it
     *        cannot be directly processed on the current <code>ROI</code>
     * @return the modified ROI or a new created ROI if the operation cannot be directly processed on the current ROI
     *         and <code>allowCreate</code> parameter was set to <code>true</code>
     * @throws UnsupportedOperationException
     *         if the two ROI cannot be added together.
     * @see #getUnion(ROI)
     */
    public ROI add(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        // nothing to do
        if (roi == null)
            return this;

        if (allowCreate)
            return getUnion(roi);

        throw new UnsupportedOperationException(getClassName() + " does not support add(ROI) operation !");
    }

    /**
     * Sets the content of this <code>ROI</code> to the intersection of
     * its current content and the content of the specified <code>ROI</code>.
     * The resulting ROI will include only contents that were contained in both ROI.<br>
     * Note that this operation work only if the 2 ROIs are compatible for that type of operation. If that is not
     * the case a {@link UnsupportedOperationException} is thrown if <code>allowCreate</code> parameter is set to
     * <code>false</code>, if the parameter is set to <code>true</code> the result may be returned in a new created ROI.
     * 
     * <pre>
     *     // Example:
     *     roi1 (before) intersect    roi2        =   roi1 (after)
     * 
     *     ################     ################     ################
     *     ##############         ##############       ############
     *     ############             ############         ########
     *     ##########                 ##########           ####
     *     ########                     ########
     *     ######                         ######
     *     ####                             ####
     *     ##                                 ##
     * </pre>
     * 
     * @param roi
     *        the <code>ROI</code> to be intersected to the current <code>ROI</code>
     * @param allowCreate
     *        if set to <code>true</code> the method will create a new ROI to return the result of the operation if it
     *        cannot be directly processed on the current <code>ROI</code>
     * @return the modified ROI or a new created ROI if the operation cannot be directly processed on the current ROI
     *         and <code>allowCreate</code> parameter was set to <code>true</code>
     * @throws UnsupportedOperationException
     *         if the two ROI cannot be intersected together.
     * @see #getIntersection(ROI)
     */
    public ROI intersect(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        // nothing to do
        if (roi == null)
            return this;

        if (allowCreate)
            return getIntersection(roi);

        throw new UnsupportedOperationException(getClassName() + " does not support intersect(ROI) operation !");
    }

    /**
     * Sets the content of this <code>ROI</code> to be the union of its current content and the content of the specified
     * <code>ROI</code>, minus their intersection.
     * The resulting <code>ROI</code> will include only content that were contained in either this <code>ROI</code> or
     * in the specified <code>ROI</code>, but not in both.<br>
     * Note that this operation work only if the 2 ROIs are compatible for that type of operation. If that is not
     * the case a {@link UnsupportedOperationException} is thrown if <code>allowCreate</code> parameter is set to
     * <code>false</code>, if the parameter is set to <code>true</code> the result may be returned in a new created ROI.
     * 
     * <pre>
     *     // Example:
     *      roi1 (before)   xor      roi2         =    roi1 (after)
     * 
     *     ################     ################
     *     ##############         ##############     ##            ##
     *     ############             ############     ####        ####
     *     ##########                 ##########     ######    ######
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     * 
     * @param roi
     *        the <code>ROI</code> to be exclusively added to the current <code>ROI</code>
     * @param allowCreate
     *        if set to <code>true</code> the method will create a new ROI to return the result of the operation if it
     *        cannot be directly processed on the current <code>ROI</code>
     * @return the modified ROI or a new created ROI if the operation cannot be directly processed on the current ROI
     *         and <code>allowCreate</code> parameter was set to <code>true</code>
     * @throws UnsupportedOperationException
     *         if the two ROI cannot be exclusively added together.
     * @see #getExclusiveUnion(ROI)
     */
    public ROI exclusiveAdd(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        // nothing to do
        if (roi == null)
            return this;

        if (allowCreate)
            return getExclusiveUnion(roi);

        throw new UnsupportedOperationException(getClassName() + " does not support exclusiveAdd(ROI) operation !");
    }

    /**
     * Subtract the specified <code>ROI</code> content from current <code>ROI</code>.<br>
     * Note that this operation work only if the 2 ROIs are compatible for that type of operation. If that is not
     * the case a {@link UnsupportedOperationException} is thrown if <code>allowCreate</code> parameter is set to
     * <code>false</code>, if the parameter is set to <code>true</code> the result may be returned in a new created ROI.
     * 
     * @param roi
     *        the <code>ROI</code> to subtract from the current <code>ROI</code>
     * @param allowCreate
     *        if set to <code>true</code> the method will create a new ROI to return the result of the operation if it
     *        cannot be directly processed on the current <code>ROI</code>
     * @return the modified ROI or a new created ROI if the operation cannot be directly processed on the current ROI
     *         and <code>allowCreate</code> parameter was set to <code>true</code>
     * @throws UnsupportedOperationException
     *         if we can't subtract the specified <code>ROI</code> from this <code>ROI</code>
     * @see #getSubtraction(ROI)
     */
    public ROI subtract(ROI roi, boolean allowCreate) throws UnsupportedOperationException
    {
        // nothing to do
        if (roi == null)
            return this;

        if (allowCreate)
            return getSubtraction(roi);

        throw new UnsupportedOperationException(getClassName() + " does not support subtract(ROI) operation !");
    }

    protected static ROI getOpResult(int dim, BooleanMask5D mask, Rectangle5D.Integer bounds)
    {
        final ROI result;

        switch (dim)
        {
            case 2: // XY ROI with fixed ZTC
                result = new ROI2DArea(mask.getMask2D(bounds.z, bounds.t, bounds.c));

                // set ZTC position
                result.beginUpdate();
                try
                {
                    ((ROI2D) result).setZ(bounds.z);
                    ((ROI2D) result).setT(bounds.t);
                    ((ROI2D) result).setC(bounds.c);
                }
                finally
                {
                    result.endUpdate();
                }
                break;

            case 3: // XYZ ROI with fixed TC
                result = new ROI3DArea(mask.getMask3D(bounds.t, bounds.c));

                // set TC position
                result.beginUpdate();
                try
                {
                    ((ROI3D) result).setT(bounds.t);
                    ((ROI3D) result).setC(bounds.c);
                }
                finally
                {
                    result.endUpdate();
                }
                break;

            case 4: // XYZT ROI with fixed C
                result = new ROI4DArea(mask.getMask4D(bounds.c));
                // set C position
                ((ROI4D) result).setC(bounds.c);
                break;

            case 5: // XYZTC ROI
                result = new ROI5DArea(mask);
                break;

            default:
                throw new UnsupportedOperationException(
                        "Can't process boolean operation on a ROI with unknown dimension.");
        }

        return result;
    }

    /**
     * Compute the boolean operation with specified <code>ROI</code> and return result in a new <code>ROI</code>.
     */
    public ROI merge(ROI roi, BooleanOperator op) throws UnsupportedOperationException
    {
        switch (op)
        {
            case AND:
                return getIntersection(roi);

            case OR:
                return getUnion(roi);

            case XOR:
                return getExclusiveUnion(roi);
        }

        return null;
    }

    /**
     * Compute union with specified <code>ROI</code> and return result in a new <code>ROI</code>.
     */
    public ROI getUnion(ROI roi) throws UnsupportedOperationException
    {
        // return copy of itself
        if (roi == null)
            return getCopy();

        final Rectangle5D bounds5D = getUnionBounds(roi, true);
        final int dim = getEffectiveDimension(bounds5D);
        // we want integer bounds now
        final Rectangle5D.Integer bounds = bounds5D.toInteger();
        final Dimension5D.Integer roiSize = getOpDim(dim, bounds);
        // get 3D and 4D bounds
        final Rectangle3D.Integer bounds3D = (Rectangle3D.Integer) bounds.toRectangle3D();
        final Rectangle4D.Integer bounds4D = (Rectangle4D.Integer) bounds.toRectangle4D();

        final BooleanMask4D mask5D[] = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++)
        {
            final BooleanMask3D mask4D[] = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++)
            {
                final BooleanMask2D mask3D[] = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++)
                {
                    mask3D[z] = BooleanMask2D.getUnion(
                            roi.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
                            getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true));
                }

                mask4D[t] = new BooleanMask3D(bounds3D, mask3D);
            }

            mask5D[c] = new BooleanMask4D(bounds4D, mask4D);
        }

        // build the 5D result ROI
        final BooleanMask5D mask = new BooleanMask5D(bounds, mask5D);
        // optimize bounds of the new created mask
        mask.optimizeBounds();

        // get result
        final ROI result = getOpResult(dim, mask, bounds);
        // set name
        result.setName("Union");

        return result;
    }

    /**
     * Compute intersection with specified <code>ROI</code> and return result in a new <code>ROI</code>.
     */
    public ROI getIntersection(ROI roi) throws UnsupportedOperationException
    {
        // empty ROI
        if (roi == null)
            return new ROI2DArea();

        final Rectangle5D bounds5D = getIntersectionBounds(roi, true);
        final int dim = getEffectiveDimension(bounds5D);
        // we want integer bounds now
        final Rectangle5D.Integer bounds = bounds5D.toInteger();
        final Dimension5D.Integer roiSize = getOpDim(dim, bounds);
        // get 3D and 4D bounds
        final Rectangle3D.Integer bounds3D = (Rectangle3D.Integer) bounds.toRectangle3D();
        final Rectangle4D.Integer bounds4D = (Rectangle4D.Integer) bounds.toRectangle4D();

        final BooleanMask4D mask5D[] = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++)
        {
            final BooleanMask3D mask4D[] = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++)
            {
                final BooleanMask2D mask3D[] = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++)
                {
                    mask3D[z] = BooleanMask2D.getIntersection(
                            roi.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
                            getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true));
                }

                mask4D[t] = new BooleanMask3D(bounds3D, mask3D);
            }

            mask5D[c] = new BooleanMask4D(bounds4D, mask4D);
        }

        // build the 5D result ROI
        final BooleanMask5D mask = new BooleanMask5D(bounds, mask5D);
        // optimize bounds of the new created mask
        mask.optimizeBounds();

        // get result
        final ROI result = getOpResult(dim, mask, bounds);
        // set name
        result.setName("Intersection");

        return result;
    }

    /**
     * Compute exclusive union with specified <code>ROI</code> and return result in a new <code>ROI</code>.
     */
    public ROI getExclusiveUnion(ROI roi) throws UnsupportedOperationException
    {
        // return copy of itself
        if (roi == null)
            return getCopy();

        final Rectangle5D bounds5D = getUnionBounds(roi, true);
        final int dim = getEffectiveDimension(bounds5D);
        // we want integer bounds now
        final Rectangle5D.Integer bounds = bounds5D.toInteger();
        final Dimension5D.Integer roiSize = getOpDim(dim, bounds);
        // get 3D and 4D bounds
        final Rectangle3D.Integer bounds3D = (Rectangle3D.Integer) bounds.toRectangle3D();
        final Rectangle4D.Integer bounds4D = (Rectangle4D.Integer) bounds.toRectangle4D();

        final BooleanMask4D mask5D[] = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++)
        {
            final BooleanMask3D mask4D[] = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++)
            {
                final BooleanMask2D mask3D[] = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++)
                {
                    mask3D[z] = BooleanMask2D.getExclusiveUnion(
                            roi.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
                            getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true));
                }

                mask4D[t] = new BooleanMask3D(bounds3D, mask3D);
            }

            mask5D[c] = new BooleanMask4D(bounds4D, mask4D);
        }

        // build the 5D result ROI
        final BooleanMask5D mask = new BooleanMask5D(bounds, mask5D);
        // optimize bounds of the new created mask
        mask.optimizeBounds();

        // get result
        final ROI result = getOpResult(dim, mask, bounds);
        // set name
        result.setName("Exclusive union");

        return result;
    }

    /**
     * Subtract the specified <code>ROI</code> and return result in a new <code>ROI</code>.
     */
    public ROI getSubtraction(ROI roi) throws UnsupportedOperationException
    {
        // return copy of itself
        if (roi == null)
            return getCopy();

        final Rectangle5D bounds5D = getSubtractionBounds(roi, true);
        final int dim = getEffectiveDimension(bounds5D);
        // we want integer bounds now
        final Rectangle5D.Integer bounds = bounds5D.toInteger();
        final Dimension5D.Integer roiSize = getOpDim(dim, bounds);
        // get 3D and 4D bounds
        final Rectangle3D.Integer bounds3D = (Rectangle3D.Integer) bounds.toRectangle3D();
        final Rectangle4D.Integer bounds4D = (Rectangle4D.Integer) bounds.toRectangle4D();

        final BooleanMask4D mask5D[] = new BooleanMask4D[roiSize.sizeC];

        for (int c = 0; c < roiSize.sizeC; c++)
        {
            final BooleanMask3D mask4D[] = new BooleanMask3D[roiSize.sizeT];

            for (int t = 0; t < roiSize.sizeT; t++)
            {
                final BooleanMask2D mask3D[] = new BooleanMask2D[roiSize.sizeZ];

                for (int z = 0; z < roiSize.sizeZ; z++)
                {
                    mask3D[z] = BooleanMask2D.getSubtraction(
                            getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true),
                            roi.getBooleanMask2D(bounds.z + z, bounds.t + t, bounds.c + c, true));
                }

                mask4D[t] = new BooleanMask3D(bounds3D, mask3D);
            }

            mask5D[c] = new BooleanMask4D(bounds4D, mask4D);
        }

        // build the 5D result ROI
        final BooleanMask5D mask = new BooleanMask5D(bounds, mask5D);
        // optimize bounds of the new created mask
        mask.optimizeBounds();

        // get result
        final ROI result = getOpResult(dim, mask, bounds);
        // set name
        result.setName("Substraction");

        return result;
    }

    /**
     * Compute and returns the number of point (pixel) composing the ROI contour.
     */
    /*
     * Override this method to adapt and optimize for a specific ROI.
     */
    public abstract double computeNumberOfContourPoints();

    /**
     * Returns the number of point (pixel) composing the ROI contour.<br>
     * It is used to calculate the perimeter (2D) or surface area (3D) of the ROI.
     * 
     * @see #computeNumberOfContourPoints()
     */
    public double getNumberOfContourPoints()
    {
        // we need to recompute the number of edge point
        if (numberOfContourPointsInvalid)
        {
            cachedNumberOfContourPoints = computeNumberOfContourPoints();
            numberOfContourPointsInvalid = false;
        }

        return cachedNumberOfContourPoints;
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
    public double getNumberOfPoints()
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
     * @deprecated Only for ROI2D object, Use {@link #getNumberOfContourPoints()} instead.
     */
    @Deprecated
    public double getPerimeter()
    {
        return getNumberOfContourPoints();
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

    /**
     * Returns a copy of the ROI or <code>null</code> if the operation failed.
     */
    public ROI getCopy()
    {
        // use XML persistence for cloning
        final Node node = XMLUtil.createDocument(true).getDocumentElement();

        // save
        if (!saveToXML(node))
        {
            System.err.println("Cannot get a copy of roi " + getName() + ": XML save operation failed.");
            // throw new RuntimeException("Cannot get a copy of roi " + getName() + ": XML save operation failed !");
            return null;
        }

        final ROI result = createFromXML(node);
        if (result == null)
        {
            System.err.println("Cannot get a copy of roi " + getName() + ": creation from XML failed.");
            // throw new RuntimeException("Cannot get a copy of roi " + getName() + ": creation from XML failed !");
            return null;
        }

        // then generate id and modify name
        result.id = generateId();

        return result;
    }

    /**
     * Returns the name suffix when we want to obtain only a sub part of the ROI (always in Z,T,C order).<br/>
     * For instance if we use for z=1, t=5 and c=-1 this method will return <code>[Z=1, T=5]</code>
     * 
     * @param z
     *        the specific Z position (slice) we want to retrieve (<code>-1</code> to retrieve the
     *        whole ROI Z dimension)
     * @param t
     *        the specific T position (frame) we want to retrieve (<code>-1</code> to retrieve the
     *        whole ROI T dimension)
     * @param c
     *        the specific C position (channel) we want to retrieve (<code>-1</code> to retrieve the
     *        whole ROI C dimension)
     */
    static public String getNameSuffix(int z, int t, int c)
    {
        String result = "";

        if (z != -1)
        {
            if (StringUtil.isEmpty(result))
                result = " [";
            else
                result += ", ";
            result += "Z=" + z;
        }
        if (t != -1)
        {
            if (StringUtil.isEmpty(result))
                result = " [";
            else
                result += ", ";
            result += "T=" + t;
        }
        if (c != -1)
        {
            if (StringUtil.isEmpty(result))
                result = " [";
            else
                result += ", ";
            result += "C=" + c;
        }

        if (!StringUtil.isEmpty(result))
            result += "]";

        return result;
    }

    /**
     * Returns a sub part of the ROI.<br/>
     * The default implementation returns result in "area" format: ({@link ROI2DArea}, {@link ROI3DArea},
     * {@link ROI4DArea} or {@link ROI5DArea}) where only internals pixels are preserved.</br>
     * Note that this function can eventually return <code>null</code> when the result ROI is empty.
     * 
     * @param z
     *        the specific Z position (slice) we want to retrieve (<code>-1</code> to retrieve the
     *        whole ROI Z dimension)
     * @param t
     *        the specific T position (frame) we want to retrieve (<code>-1</code> to retrieve the
     *        whole ROI T dimension)
     * @param c
     *        the specific C position (channel) we want to retrieve (<code>-1</code> to retrieve the
     *        whole ROI C dimension)
     */
    public ROI getSubROI(int z, int t, int c)
    {
        final ROI result;

        switch (getDimension())
        {
            default:
                result = new ROI2DArea(getBooleanMask2D(z, t, c, false));
                break;

            case 3:
                if (z == -1)
                    result = new ROI3DArea(((ROI3D) this).getBooleanMask3D(z, t, c, false));
                else
                    result = new ROI2DArea(((ROI3D) this).getBooleanMask2D(z, t, c, false));
                break;

            case 4:
                if (z == -1)
                {
                    if (t == -1)
                        result = new ROI4DArea(((ROI4D) this).getBooleanMask4D(z, t, c, false));
                    else
                        result = new ROI3DArea(((ROI4D) this).getBooleanMask3D(z, t, c, false));
                }
                else
                {
                    if (t == -1)
                        result = new ROI4DArea(((ROI4D) this).getBooleanMask4D(z, t, c, false));
                    else
                        result = new ROI2DArea(((ROI4D) this).getBooleanMask2D(z, t, c, false));
                }
                break;

            case 5:
                if (z == -1)
                {
                    if (t == -1)
                    {
                        if (c == -1)
                            result = new ROI5DArea(((ROI5D) this).getBooleanMask5D(z, t, c, false));
                        else
                            result = new ROI4DArea(((ROI5D) this).getBooleanMask4D(z, t, c, false));
                    }
                    else
                    {
                        if (c == -1)
                            result = new ROI5DArea(((ROI5D) this).getBooleanMask5D(z, t, c, false));
                        else
                            result = new ROI3DArea(((ROI5D) this).getBooleanMask3D(z, t, c, false));
                    }
                }
                else
                {
                    if (t == -1)
                    {
                        if (c == -1)
                            result = new ROI5DArea(((ROI5D) this).getBooleanMask5D(z, t, c, false));
                        else
                            result = new ROI4DArea(((ROI5D) this).getBooleanMask4D(z, t, c, false));
                    }
                    else
                    {
                        if (c == -1)
                            result = new ROI5DArea(((ROI5D) this).getBooleanMask5D(z, t, c, false));
                        else
                            result = new ROI2DArea(((ROI5D) this).getBooleanMask2D(z, t, c, false));
                    }
                }
                break;
        }

        result.beginUpdate();
        try
        {
            final Point5D pos = result.getPosition5D();

            // set Z, T, C position
            if (z != -1)
                pos.setZ(z);
            if (t != -1)
                pos.setT(t);
            if (c != -1)
                pos.setC(c);

            result.setPosition5D(pos);

            // copy other properties
            result.setColor(getColor());
            result.setName(getName() + getNameSuffix(z, t, c));
            result.setOpacity(getOpacity());
            result.setStroke(getStroke());
            result.setShowName(getShowName());
        }
        finally
        {
            result.endUpdate();
        }

        return result;
    }

    /**
     * Copy all properties from the given ROI.<br>
     * All compatible properties from the source ROI are copied into current ROI except the internal id.<br>
     * Return <code>false</code> if the operation failed
     */
    public boolean copyFrom(ROI roi)
    {
        // use XML persistence for cloning
        final Node node = XMLUtil.createDocument(true).getDocumentElement();

        // save operation can fails sometime...
        if (roi.saveToXML(node))
            if (loadFromXML(node, true))
                return true;

        return false;
        // if (tries == 0)
        // throw new RuntimeException("Cannot copy roi from " + roi.getName() + ": XML load operation failed !");
    }

    public boolean loadFromXML(Node node, boolean preserveId)
    {
        if (node == null)
            return false;

        beginUpdate();
        try
        {
            // FIXME : this can make duplicate id but it is also important to preserve id
            if (!preserveId)
                id = XMLUtil.getElementIntValue(node, ID_ID, 0);
            setName(XMLUtil.getElementValue(node, ID_NAME, ""));
            setSelected(XMLUtil.getElementBooleanValue(node, ID_SELECTED, false));
            setReadOnly(XMLUtil.getElementBooleanValue(node, ID_READONLY, false));
            setShowName(XMLUtil.getElementBooleanValue(node, ID_SHOWNAME, false));
            painter.loadFromXML(node);
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
        return loadFromXML(node, false);
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
        XMLUtil.setElementBooleanValue(node, ID_READONLY, isReadOnly());
        XMLUtil.setElementBooleanValue(node, ID_SHOWNAME, getShowName());

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
     * Called when ROI has changed its content.
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
                numberOfContourPointsInvalid = true;
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
                final String property = event.getPropertyName();

                // painter affecting display
                if (StringUtil.isEmpty(property) || StringUtil.equals(property, PROPERTY_NAME)
                        || StringUtil.equals(property, PROPERTY_SHOWNAME)
                        || StringUtil.equals(property, PROPERTY_COLOR) || StringUtil.equals(property, PROPERTY_OPACITY)
                        || StringUtil.equals(property, PROPERTY_SHOWNAME)
                        || StringUtil.equals(property, PROPERTY_STROKE))
                    painter.painterChanged();
                break;

            default:
                break;
        }

        // notify listener we have changed
        fireChangedEvent(event);
    }
}
