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
package icy.gui.menu;

import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandToggleButton;
import icy.gui.frame.progress.ToolTipFrame;
import icy.gui.menu.action.FileActions;
import icy.gui.plugin.PluginCommandButton;
import icy.gui.util.RibbonUtil;
import icy.main.Icy;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.plugin.interface_.PluginROI;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.roi.ROI2DArea;
import icy.roi.ROI2DEllipse;
import icy.roi.ROI2DLine;
import icy.roi.ROI2DPoint;
import icy.roi.ROI2DPolyLine;
import icy.roi.ROI2DPolygon;
import icy.roi.ROI2DRectangle;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.CommandToggleButtonGroup;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizeSequencingPolicies;

public class ToolRibbonTask extends RibbonTask implements PluginLoaderListener
{
    public static final String NAME = "Selection & ROI tools";

    public static final String SELECT = "Selection";
    public static final String MOVE = "Move";

    private static final String TOOLTIP_ROI2D_POINT = "<b>ROI Point : single point type ROI</b><br><br>"
            + "Click on the image where you want to set your point.<br>"
            + "Unselect the ROI with ESC key, left click or double click.<br>"
            + "Remove the ROI with DELETE key when the ROI is selected or focused.";
    private static final String TOOLTIP_ROI2D_LINE = "<b>ROI Line : single line type ROI</b><br><br>"
            + "Drag from start point to destination point.<br>"
            + "Unselect the ROI with ESC key, left click or double click.<br>"
            + "Remove the ROI with DELETE key when the ROI is selected or focused.";
    private static final String TOOLTIP_ROI2D_POLYLINE = "<b>ROI Polyline : multi line type ROI</b><br><br>"
            + "Add a new point with left click.<br>"
            + "Add a new point between two points with left click + CONTROL key.<br>"
            + "Remove a point with DELETE key when the point is focused.<br>"
            + "Unselect / end modification with ESC key or double click.<br>"
            + "Remove the ROI with DELETE key when the ROI is selected or focused.";
    private static final String TOOLTIP_ROI2D_RECTANGLE = "<b>ROI Rectangle : rectangle type ROI</b><br><br>"
            + "Drag from start point to destination point.<br>"
            + "Unselect the ROI with ESC key, left click or double click.<br>"
            + "Remove the ROI with DELETE key when the ROI is selected or focused.";
    private static final String TOOLTIP_ROI2D_ELLIPSE = "<b>ROI Ellipse : ellipse type ROI</b><br><br>"
            + "Drag from start point to destination point.<br>"
            + "Unselect the ROI with ESC key, left click or double click.<br>"
            + "Remove the ROI with DELETE key when the ROI is selected or focused.";
    private static final String TOOLTIP_ROI2D_POLYGON = "<b>ROI Polygon : polygon type ROI</b><br><br>"
            + "Add a new point with left click.<br>"
            + "Add a new point between two points with left click + CONTROL key.<br>"
            + "Remove a point with DELETE key when the point is focused.<br>"
            + "Unselect / end modification with ESC key or double click.<br>"
            + "Remove the ROI with DELETE key when the ROI is selected or focused.";
    private static final String TOOLTIP_ROI2D_AREA = "<b>ROI Area : bitmap mask area type ROI</b><br><br>"
            + "Draw in with left mouse button and erase with right button.<br>"
            + "Unselect / end modification with ESC key or double click.<br>"
            + "Increase or decrease the pencil size with '+' / '-' keys<br>"
            + "Remove the ROI with DELETE key when the ROI is selected or focused.";

    private static final int TOOLTIP_LIVETIME = 60; // 60 seconds

    public static boolean isROITool(String command)
    {
        // assume it's a ROI command when it's not standard MOVE or SELECT command
        return (command != null) && (!command.equals(SELECT)) && (!command.equals(MOVE));
    }

    /**
     * Listener class
     */
    public interface ToolRibbonTaskListener extends EventListener
    {
        public void toolChanged(String command);
    }

    public static class FileRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2677243480668715388L;

        public static final String NAME = "File";

        final IcyCommandButton openButton;
        final IcyCommandButton saveButton;

        public FileRibbonBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_DOC));

            openButton = new IcyCommandButton(FileActions.openSequenceAction);
            addCommandButton(openButton, RibbonElementPriority.TOP);
            saveButton = new IcyCommandButton(FileActions.saveSequenceAction);
            addCommandButton(saveButton, RibbonElementPriority.TOP);

            RibbonUtil.setPermissiveResizePolicies(this);
            updateButtonsState();
        }

        void updateButtonsState()
        {
            saveButton.setEnabled(Icy.getMainInterface().getFocusedSequence() != null);
        }
    }

    public static class SelectRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2677243480668715388L;

        public static final String NAME = "Selection";

        public SelectRibbonBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_DOC));
            {
                IcyCommandToggleButton button;

                startGroup();

                button = new IcyCommandToggleButton("Select", new IcyIcon("cursor_arrow"));
                button.setName(SELECT);
                button.setActionRichTooltip(new RichTooltip("Select mode", "Select and move objects (as ROI)"));
                addCommandButton(button, RibbonElementPriority.TOP);

                button = new IcyCommandToggleButton("Move", new IcyIcon("cursor_hand"));
                button.setName(MOVE);
                button.setActionRichTooltip(new RichTooltip("Move mode",
                        "Drag image while pressing the left mouse button"));
                addCommandButton(button, RibbonElementPriority.TOP);

                RibbonUtil.setPermissiveResizePolicies(this);
            }
        }
    }

    public static class ROIRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -4005641508453876533L;

        public static final String NAME = "ROI";

        final List<IcyCommandToggleButton> pluginButtons;

        public ROIRibbonBand()
        {
            super(NAME, new IcyIcon(ResourceUtil.ICON_DOC));

            IcyCommandToggleButton button;

            startGroup();

            // basics ROI
            button = new IcyCommandToggleButton("Point", new IcyIcon("roi_point"));
            button.setName(ROI2DPoint.class.getName());
            button.setActionRichTooltip(new RichTooltip("ROI Point", "Create a point type (single pixel) ROI"));
            addCommandButton(button, RibbonElementPriority.MEDIUM);

            button = new IcyCommandToggleButton("Line", new IcyIcon("roi_line"));
            button.setName(ROI2DLine.class.getName());
            button.setActionRichTooltip(new RichTooltip("ROI Line",
                    "Create a single line type ROI. Drag from start point to destination point."));
            addCommandButton(button, RibbonElementPriority.MEDIUM);

            button = new IcyCommandToggleButton("Polyline", new IcyIcon("roi_polyline"));
            button.setName(ROI2DPolyLine.class.getName());
            button.setActionRichTooltip(new RichTooltip("ROI Polyline",
                    "Create a multi line type ROI. Add a new point with left click, end draw with right click or ESC key."));
            addCommandButton(button, RibbonElementPriority.MEDIUM);

            button = new IcyCommandToggleButton("Rectangle", new IcyIcon("roi_rectangle"));
            button.setName(ROI2DRectangle.class.getName());
            button.setActionRichTooltip(new RichTooltip("ROI Rectangle",
                    "Create a rectangle type ROI.  Drag from start point to destination point."));
            addCommandButton(button, RibbonElementPriority.MEDIUM);

            button = new IcyCommandToggleButton("Ellipse", new IcyIcon("roi_oval"));
            button.setName(ROI2DEllipse.class.getName());
            button.setActionRichTooltip(new RichTooltip("ROI Ellipse",
                    "Create a ellipse type ROI. Drag from start point to destination point."));
            addCommandButton(button, RibbonElementPriority.MEDIUM);

            button = new IcyCommandToggleButton("Polygon", new IcyIcon("roi_polygon"));
            button.setName(ROI2DPolygon.class.getName());
            button.setActionRichTooltip(new RichTooltip("ROI Polygon",
                    "Create a polygon type ROI. Add a new point with left click, end draw with right click or ESC key."));
            addCommandButton(button, RibbonElementPriority.MEDIUM);

            button = new IcyCommandToggleButton("Area", new IcyIcon("roi_area"));
            button.setName(ROI2DArea.class.getName());
            button.setActionRichTooltip(new RichTooltip(
                    "ROI Area",
                    "Create a area type ROI. Add points with left mouse button, remove points with right mouse button. Press ESC to end draw or right click outside ROI bounds."));
            addCommandButton(button, RibbonElementPriority.TOP);

            pluginButtons = new ArrayList<IcyCommandToggleButton>();

            setROIFromPlugins(null, null);

            RibbonUtil.setPermissiveResizePolicies(this);
        }

        void setROIFromPlugins(CommandToggleButtonGroup buttonGroup, ActionListener al)
        {
            // remove previous plugin buttons
            for (IcyCommandToggleButton button : pluginButtons)
            {
                if (al != null)
                    button.removeActionListener(al);
                removeCommandButton(button);
                if (buttonGroup != null)
                    buttonGroup.remove(button);
            }
            pluginButtons.clear();

            IcyCommandToggleButton button;

            // plugins ROI
            final ArrayList<PluginDescriptor> roiPlugins = PluginLoader.getPlugins(PluginROI.class);

            for (PluginDescriptor plugin : roiPlugins)
            {
                button = PluginCommandButton.createToggleButton(plugin, false);
                addCommandButton(button, RibbonElementPriority.MEDIUM);
                if (al != null)
                    button.addActionListener(al);
                if (buttonGroup != null)
                    buttonGroup.add(button);
                pluginButtons.add(button);
            }
        }
    }

    final FileRibbonBand fileBand;
    final SelectRibbonBand selectBand;
    final ROIRibbonBand roiBand;

    final CommandToggleButtonGroup buttonGroup;
    final ActionListener buttonActionListener;

    String currentTool;

    /**
     * List of listeners
     */
    private final EventListenerList listeners;

    public ToolRibbonTask()
    {
        super(NAME, new FileRibbonBand(), new SelectRibbonBand(), new ROIRibbonBand());

        setResizeSequencingPolicy(new CoreRibbonResizeSequencingPolicies.CollapseFromLast(this));

        // get band
        fileBand = (FileRibbonBand) RibbonUtil.getBand(this, FileRibbonBand.NAME);
        selectBand = (SelectRibbonBand) RibbonUtil.getBand(this, SelectRibbonBand.NAME);
        roiBand = (ROIRibbonBand) RibbonUtil.getBand(this, ROIRibbonBand.NAME);

        listeners = new EventListenerList();

        buttonActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                internalSetSelected(((IcyCommandToggleButton) e.getSource()).getName());
            }
        };

        // add action listener here
        for (AbstractCommandButton button : RibbonUtil.getButtons(selectBand))
            button.addActionListener(buttonActionListener);
        for (AbstractCommandButton button : RibbonUtil.getButtons(roiBand))
            button.addActionListener(buttonActionListener);

        // create button group
        buttonGroup = new CommandToggleButtonGroup();

        for (AbstractCommandButton button : RibbonUtil.getButtons(selectBand))
            buttonGroup.add((IcyCommandToggleButton) button);
        for (AbstractCommandButton button : RibbonUtil.getButtons(roiBand))
            buttonGroup.add((IcyCommandToggleButton) button);

        buttonGroup.setAllowsClearingSelection(false);

        // SELECT action by default
        currentTool = "";
        setSelected(SELECT);

        PluginLoader.addListener(this);
    }

    private IcyCommandToggleButton getButtonFromToolName(String toolName)
    {
        for (AbstractCommandButton button : RibbonUtil.getButtons(selectBand))
            if (toolName.equals(button.getName()))
                return (IcyCommandToggleButton) button;

        for (AbstractCommandButton button : RibbonUtil.getButtons(roiBand))
            if (toolName.equals(button.getName()))
                return (IcyCommandToggleButton) button;

        return null;
    }

    public String getSelected()
    {
        return buttonGroup.getSelected().getName();
    }

    public void setSelected(String toolName)
    {
        if (!currentTool.equals(toolName))
        {
            final IcyCommandToggleButton button = getButtonFromToolName(toolName);

            if (button != null)
            {
                buttonGroup.setSelected(button, true);

                currentTool = toolName;
                toolChanged(toolName);
            }
        }
    }

    /**
     * Called when user click on one of the tool button
     */
    void internalSetSelected(String toolName)
    {
        if (!currentTool.equals(toolName))
        {
            currentTool = toolName;

            displayToolTip(toolName);
            toolChanged(toolName);
        }
    }

    /**
     * Display tips for specified tool
     */
    private void displayToolTip(String toolName)
    {
        if (StringUtil.isEmpty(toolName) || (Icy.getMainInterface().getFocusedViewer() == null))
            return;

        final String tips;

        if (toolName.equals(ROI2DPoint.class.getName()))
            tips = TOOLTIP_ROI2D_POINT;
        else if (toolName.equals(ROI2DLine.class.getName()))
            tips = TOOLTIP_ROI2D_LINE;
        else if (toolName.equals(ROI2DPolyLine.class.getName()))
            tips = TOOLTIP_ROI2D_POLYLINE;
        else if (toolName.equals(ROI2DRectangle.class.getName()))
            tips = TOOLTIP_ROI2D_RECTANGLE;
        else if (toolName.equals(ROI2DEllipse.class.getName()))
            tips = TOOLTIP_ROI2D_ELLIPSE;
        else if (toolName.equals(ROI2DPolygon.class.getName()))
            tips = TOOLTIP_ROI2D_POLYGON;
        else if (toolName.equals(ROI2DArea.class.getName()))
            tips = TOOLTIP_ROI2D_AREA;
        else
            tips = null;

        if (tips != null)
            new ToolTipFrame(tips, TOOLTIP_LIVETIME, toolName);
    }

    public void toolChanged(String toolName)
    {
        fireChangedEvent(toolName);
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(ToolRibbonTaskListener listener)
    {
        listeners.add(ToolRibbonTaskListener.class, listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(ToolRibbonTaskListener listener)
    {
        listeners.remove(ToolRibbonTaskListener.class, listener);
    }

    /**
     * @param toolName
     */
    void fireChangedEvent(String toolName)
    {
        for (ToolRibbonTaskListener listener : listeners.getListeners(ToolRibbonTaskListener.class))
            listener.toolChanged(toolName);
    }

    /**
     * call this method on sequence change
     */
    public void onSequenceChange()
    {
        fileBand.updateButtonsState();
    }

    @Override
    public void pluginLoaderChanged(PluginLoaderEvent e)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // refresh ROI button which come from plugins
                roiBand.setROIFromPlugins(buttonGroup, buttonActionListener);
            }
        });
    }

}
