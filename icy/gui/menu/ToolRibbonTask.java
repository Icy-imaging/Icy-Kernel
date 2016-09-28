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
package icy.gui.menu;

import icy.action.RoiActions;
import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandToggleButton;
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
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI3D;
import icy.sequence.Sequence;
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
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizeSequencingPolicies;

import plugins.kernel.roi.roi2d.ROI2DArea;
import plugins.kernel.roi.roi2d.ROI2DShape;
import plugins.kernel.roi.roi2d.plugin.ROI2DAreaPlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DEllipsePlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DLinePlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DPointPlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DPolyLinePlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DPolygonPlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DRectanglePlugin;
import plugins.kernel.roi.roi3d.ROI3DArea;
import plugins.kernel.roi.roi3d.plugin.ROI3DLinePlugin;
import plugins.kernel.roi.roi3d.plugin.ROI3DPointPlugin;
import plugins.kernel.roi.roi3d.plugin.ROI3DPolyLinePlugin;
import plugins.kernel.roi.roi4d.ROI4DArea;
import plugins.kernel.roi.roi5d.ROI5DArea;

/**
 * ROI dedicated task
 * 
 * @author Stephane
 */
public class ToolRibbonTask extends RibbonTask implements PluginLoaderListener
{
    public static final String NAME = "Region Of Interest";

    /**
     * @deprecated Use {@link #setSelected(String)} with <code>null</code> parameter instead.
     */
    @Deprecated
    public static final String SELECT = "Selection";
    /**
     * @deprecated Use {@link #setSelected(String)} with <code>null</code> parameter instead.
     */
    @Deprecated
    public static final String MOVE = "Move";

    /**
     * @deprecated Use {@link ToolRibbonTask#isROITool()} instead.
     */
    @Deprecated
    public static boolean isROITool(String command)
    {
        // assume it's a ROI command when it's not null
        return (command != null);
    }

    /**
     * Listener class
     */
    public interface ToolRibbonTaskListener extends EventListener
    {
        public void toolChanged(String command);
    }

    // public static class FileRibbonBand extends JRibbonBand
    // {
    // /**
    // *
    // */
    // private static final long serialVersionUID = -2677243480668715388L;
    //
    // public static final String NAME = "File";
    //
    // final IcyCommandButton openButton;
    // final IcyCommandButton saveButton;
    //
    // public FileRibbonBand()
    // {
    // super(NAME, new IcyIcon(ResourceUtil.ICON_DOC));
    //
    // openButton = new IcyCommandButton(FileActions.openSequenceAction);
    // addCommandButton(openButton, RibbonElementPriority.MEDIUM);
    // saveButton = new IcyCommandButton(FileActions.saveAsSequenceAction);
    // addCommandButton(saveButton, RibbonElementPriority.MEDIUM);
    //
    // RibbonUtil.setPermissiveResizePolicies(this);
    // updateButtonsState();
    // }
    //
    // void updateButtonsState()
    // {
    // saveButton.setEnabled(Icy.getMainInterface().getActiveSequence() != null);
    // }
    // }

    static class ROILengthBand extends JRibbonBand
    {
        public static final String BAND_NAME = "Point & Line";

        final List<IcyCommandToggleButton> pluginButtons;

        public ROILengthBand()
        {
            super(BAND_NAME, new IcyIcon(ResourceUtil.ICON_DOC));

            startGroup();

            pluginButtons = new ArrayList<IcyCommandToggleButton>();

            // we will add the action listener later
            setROIFromPlugins(null, null);

            RibbonUtil.setRestrictiveResizePolicies(this);
            setToolTipText("Point & Line type of ROI");
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

            // plugins ROI
            final List<PluginDescriptor> roiPlugins = PluginLoader.getPlugins(PluginROI.class);
            final List<PluginDescriptor> sortedRoiPlugins = new ArrayList<PluginDescriptor>();

            sortedRoiPlugins.add(PluginDescriptor.getPlugin(roiPlugins, ROI2DPointPlugin.class.getName()));
            sortedRoiPlugins.add(PluginDescriptor.getPlugin(roiPlugins, ROI2DLinePlugin.class.getName()));
            sortedRoiPlugins.add(PluginDescriptor.getPlugin(roiPlugins, ROI2DPolyLinePlugin.class.getName()));
            sortedRoiPlugins.add(PluginDescriptor.getPlugin(roiPlugins, ROI3DPointPlugin.class.getName()));
            sortedRoiPlugins.add(PluginDescriptor.getPlugin(roiPlugins, ROI3DLinePlugin.class.getName()));
            sortedRoiPlugins.add(PluginDescriptor.getPlugin(roiPlugins, ROI3DPolyLinePlugin.class.getName()));

            for (PluginDescriptor plugin : sortedRoiPlugins)
            {
                final IcyCommandToggleButton button = PluginCommandButton.createToggleButton(plugin, false);

                addCommandButton(button, RibbonElementPriority.MEDIUM);

                if (al != null)
                    button.addActionListener(al);
                if (buttonGroup != null)
                    buttonGroup.add(button);

                pluginButtons.add(button);
            }
        }
    }

    static class ROIAreaBand extends JRibbonBand
    {
        public static final String BAND_NAME = "Area";

        final List<IcyCommandToggleButton> pluginButtons;

        public ROIAreaBand()
        {
            super(BAND_NAME, new IcyIcon(ResourceUtil.ICON_DOC));

            startGroup();

            pluginButtons = new ArrayList<IcyCommandToggleButton>();

            // we will add the action listener later
            setROIFromPlugins(null, null);

            RibbonUtil.setRestrictiveResizePolicies(this);
            setToolTipText("Area type of ROI");
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

            // plugins ROI
            final List<PluginDescriptor> roiPlugins = PluginLoader.getPlugins(PluginROI.class);
            final List<PluginDescriptor> sortedRoiPlugins = new ArrayList<PluginDescriptor>();

            sortedRoiPlugins.add(PluginDescriptor.getPlugin(roiPlugins, ROI2DRectanglePlugin.class.getName()));
            sortedRoiPlugins.add(PluginDescriptor.getPlugin(roiPlugins, ROI2DEllipsePlugin.class.getName()));
            sortedRoiPlugins.add(PluginDescriptor.getPlugin(roiPlugins, ROI2DPolygonPlugin.class.getName()));
            sortedRoiPlugins.add(PluginDescriptor.getPlugin(roiPlugins, ROI2DAreaPlugin.class.getName()));

            for (PluginDescriptor plugin : sortedRoiPlugins)
            {
                final IcyCommandToggleButton button = PluginCommandButton.createToggleButton(plugin, false);

                if (plugin.getClassName().equals(ROI2DAreaPlugin.class.getName()))
                    addCommandButton(button, RibbonElementPriority.TOP);
                else
                    addCommandButton(button, RibbonElementPriority.MEDIUM);

                if (al != null)
                    button.addActionListener(al);
                if (buttonGroup != null)
                    buttonGroup.add(button);

                pluginButtons.add(button);
            }
        }
    }

    static class ROIConversionBand extends JRibbonBand
    {
        public static final String BAND_NAME = "Conversion";

        IcyCommandButton convertToStackButton;
        IcyCommandButton convertToMaskButton;
        IcyCommandButton convertToShapeButton;

        public ROIConversionBand()
        {
            super(BAND_NAME, new IcyIcon(ResourceUtil.ICON_LAYER_V2));

            // conversion
            convertToStackButton = new IcyCommandButton(RoiActions.convertToStackAction);
            convertToMaskButton = new IcyCommandButton(RoiActions.convertToMaskAction);
            convertToShapeButton = new IcyCommandButton(RoiActions.convertToShapeAction);
            addCommandButton(convertToStackButton, RibbonElementPriority.MEDIUM);
            addCommandButton(convertToMaskButton, RibbonElementPriority.MEDIUM);
            addCommandButton(convertToShapeButton, RibbonElementPriority.MEDIUM);

            setToolTipText("Conversion tools for ROI");
            RibbonUtil.setRestrictiveResizePolicies(this);
        }

        public void updateButtonsState()
        {
            boolean convertStackEnable = false;
            boolean convertMaskEnable = false;
            boolean convertShapeEnable = false;
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            if (seq != null)
            {
                final List<ROI> selectedRois = seq.getSelectedROIs();
                final int selectedSize = selectedRois.size();

                for (ROI roi : selectedRois)
                {
                    if (roi instanceof ROI2D)
                    {
                        convertStackEnable = true;
                        if (!(roi instanceof ROI2DShape))
                            convertShapeEnable = true;
                    }
                    if (!((roi instanceof ROI2DArea) || (roi instanceof ROI3DArea) || (roi instanceof ROI4DArea) || (roi instanceof ROI5DArea)))
                        convertMaskEnable = true;
                    if (roi instanceof ROI3D)
                    {
                        if (roi instanceof ROI3DArea)
                            convertShapeEnable = true;
                    }
                }
            }

            convertToStackButton.setEnabled(convertStackEnable);
            convertToMaskButton.setEnabled(convertMaskEnable);
            convertToShapeButton.setEnabled(convertShapeEnable);
        }
    }

    static class ROISeparationBand extends JRibbonBand
    {
        public static final String BAND_NAME = "Separation";

        IcyCommandButton separateObjectsButton;
        IcyCommandButton cutButton;
        IcyCommandButton splitButton;

        public ROISeparationBand()
        {
            super(BAND_NAME, new IcyIcon(ResourceUtil.ICON_LAYER_V2));

            // conversion
            separateObjectsButton = new IcyCommandButton(RoiActions.separateObjectsAction);
            cutButton = new IcyCommandButton(RoiActions.manualCutAction);
            splitButton = new IcyCommandButton(RoiActions.autoSplitAction);
            addCommandButton(separateObjectsButton, RibbonElementPriority.MEDIUM);
            addCommandButton(cutButton, RibbonElementPriority.MEDIUM);
            addCommandButton(splitButton, RibbonElementPriority.MEDIUM);

            setToolTipText("Conversion tools for ROI");
            RibbonUtil.setRestrictiveResizePolicies(this);
        }

        public void updateButtonsState()
        {
            boolean separateObjEnable = false;
            boolean cutEnable = false;
            boolean splitEnable = false;
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            if (seq != null)
            {
                final List<ROI> selectedRois = seq.getSelectedROIs();

                cutEnable = seq.hasROI();
                separateObjEnable = !selectedRois.isEmpty();
                splitEnable = !selectedRois.isEmpty();
            }

            separateObjectsButton.setEnabled(separateObjEnable);
            cutButton.setEnabled(cutEnable);
            splitButton.setEnabled(splitEnable);
        }
    }

    // final FileRibbonBand fileBand;
    // final SelectRibbonBand selectBand;
    final ROILengthBand roiLengthBand;
    final ROIAreaBand roiAreaBand;
    final ROIConversionBand roiConversionBand;
    final ROISeparationBand roiSeparationBand;

    final CommandToggleButtonGroup buttonGroup;
    final ActionListener buttonActionListener;

    String currentTool;

    /**
     * List of listeners
     */
    private final EventListenerList listeners;

    public ToolRibbonTask()
    {
        super(NAME, new ROILengthBand(), new ROIAreaBand(), new ROIConversionBand(), new ROISeparationBand());
        // super(NAME, new FileRibbonBand(), new ROIRibbonBand());

        setResizeSequencingPolicy(new CoreRibbonResizeSequencingPolicies.CollapseFromLast(this));

        // get band
        // fileBand = (FileRibbonBand) RibbonUtil.getBand(this, FileRibbonBand.NAME);
        // selectBand = (SelectRibbonBand) RibbonUtil.getBand(this, SelectRibbonBand.NAME);
        roiLengthBand = (ROILengthBand) RibbonUtil.getBand(this, ROILengthBand.BAND_NAME);
        roiAreaBand = (ROIAreaBand) RibbonUtil.getBand(this, ROIAreaBand.BAND_NAME);
        roiConversionBand = (ROIConversionBand) RibbonUtil.getBand(this, ROIConversionBand.BAND_NAME);
        roiSeparationBand = (ROISeparationBand) RibbonUtil.getBand(this, ROISeparationBand.BAND_NAME);

        listeners = new EventListenerList();

        buttonActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setSelectedButton((IcyCommandToggleButton) e.getSource(), true);
            }
        };

        // add action listener here
        for (AbstractCommandButton button : RibbonUtil.getButtons(roiLengthBand))
            button.addActionListener(buttonActionListener);
        for (AbstractCommandButton button : RibbonUtil.getButtons(roiAreaBand))
            button.addActionListener(buttonActionListener);

        // create button group
        buttonGroup = new CommandToggleButtonGroup();
        buttonGroup.setAllowsClearingSelection(true);

        for (AbstractCommandButton button : RibbonUtil.getButtons(roiLengthBand))
            buttonGroup.add((IcyCommandToggleButton) button);
        for (AbstractCommandButton button : RibbonUtil.getButtons(roiAreaBand))
            buttonGroup.add((IcyCommandToggleButton) button);

        // no tool action by default
        currentTool = "";

        PluginLoader.addListener(this);
    }

    protected IcyCommandToggleButton getButtonFromName(String name)
    {
        if (StringUtil.isEmpty(name))
            return null;

        for (AbstractCommandButton button : RibbonUtil.getButtons(roiLengthBand))
            if (name.equals(button.getName()))
                return (IcyCommandToggleButton) button;
        for (AbstractCommandButton button : RibbonUtil.getButtons(roiAreaBand))
            if (name.equals(button.getName()))
                return (IcyCommandToggleButton) button;

        return null;
    }

    /**
     * Returns true if current selected tool is ROI type tool.
     */
    public boolean isROITool()
    {
        // currently we only have ROI tool
        // so as soon selected is not null it is ROI tool
        return getSelected() != null;
    }

    /**
     * Returns the current selected button (can be <code>null</code>).
     */
    protected JCommandToggleButton getSelectedButton()
    {
        return buttonGroup.getSelected();
    }

    /**
     * Sets the current selected button (can be <code>null</code>).
     */
    protected void setSelectedButton(JCommandToggleButton button, boolean force)
    {
        if (force || (getSelectedButton() != button))
        {
            // select the button
            if (button != null)
                buttonGroup.setSelected(button, true);
            else
                buttonGroup.clearSelection();

            // notify tool change
            toolChanged(getSelected());
        }
    }

    /**
     * Returns the current selected tool.<br>
     * It can be null if no tool is currently selected.
     */
    public String getSelected()
    {
        final JCommandToggleButton button = getSelectedButton();

        if (button != null)
            return button.getName();

        return null;
    }

    /**
     * Sets the current selected tool.<br>
     * If <i>toolName</i> is a invalid tool name or <code>null</code> then no tool is selected.
     */
    public void setSelected(String value)
    {
        setSelectedButton(getButtonFromName(value), false);
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
     * call this method on sequence activation change
     */
    public void onSequenceActivationChange()
    {
        // fileBand.updateButtonsState();
        roiConversionBand.updateButtonsState();
        roiSeparationBand.updateButtonsState();
    }

    /**
     * call this method on sequence ROI change
     */
    public void onSequenceChange()
    {
        roiConversionBand.updateButtonsState();
        roiSeparationBand.updateButtonsState();
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
                roiLengthBand.setROIFromPlugins(buttonGroup, buttonActionListener);
                roiAreaBand.setROIFromPlugins(buttonGroup, buttonActionListener);
            }
        });
    }

}
