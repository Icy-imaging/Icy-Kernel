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
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import plugins.kernel.roi.roi2d.plugin.ROI2DAreaPlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DEllipsePlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DLinePlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DPointPlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DPolyLinePlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DPolygonPlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DRectanglePlugin;

public class ToolRibbonTask extends RibbonTask implements PluginLoaderListener
{
    public static final String NAME = "File & ROI tools";

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
            saveButton.setEnabled(Icy.getMainInterface().getActiveSequence() != null);
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

            startGroup();

            pluginButtons = new ArrayList<IcyCommandToggleButton>();

            // we will add the action listener later
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

            // plugins ROI
            final List<PluginDescriptor> roiPlugins = PluginLoader.getPlugins(PluginROI.class);
            IcyCommandToggleButton button;

            // we want kernel ROI to be sorted
            Collections.sort(roiPlugins, new Comparator<PluginDescriptor>()
            {
                private Integer getOrder(PluginDescriptor plugin)
                {
                    final int result;

                    if (plugin.getClassName().equals(ROI2DPointPlugin.class.getName()))
                        result = -20;
                    else if (plugin.getClassName().equals(ROI2DLinePlugin.class.getName()))
                        result = -19;
                    else if (plugin.getClassName().equals(ROI2DPolyLinePlugin.class.getName()))
                        result = -18;
                    else if (plugin.getClassName().equals(ROI2DRectanglePlugin.class.getName()))
                        result = -17;
                    else if (plugin.getClassName().equals(ROI2DEllipsePlugin.class.getName()))
                        result = -16;
                    else if (plugin.getClassName().equals(ROI2DPolygonPlugin.class.getName()))
                        result = -15;
                    else if (plugin.getClassName().equals(ROI2DAreaPlugin.class.getName()))
                        result = -14;
                    else
                        result = 0;

                    return Integer.valueOf(result);
                }

                @Override
                public int compare(PluginDescriptor plugin1, PluginDescriptor plugin2)
                {
                    return getOrder(plugin1).compareTo(getOrder(plugin2));
                }
            });

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
    // final SelectRibbonBand selectBand;
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
        super(NAME, new FileRibbonBand(), new ROIRibbonBand());

        setResizeSequencingPolicy(new CoreRibbonResizeSequencingPolicies.CollapseFromLast(this));

        // get band
        fileBand = (FileRibbonBand) RibbonUtil.getBand(this, FileRibbonBand.NAME);
        // selectBand = (SelectRibbonBand) RibbonUtil.getBand(this, SelectRibbonBand.NAME);
        roiBand = (ROIRibbonBand) RibbonUtil.getBand(this, ROIRibbonBand.NAME);

        listeners = new EventListenerList();

        buttonActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setSelectedButton((IcyCommandToggleButton) e.getSource());
            }
        };

        // add action listener here
        for (AbstractCommandButton button : RibbonUtil.getButtons(roiBand))
            button.addActionListener(buttonActionListener);

        // create button group
        buttonGroup = new CommandToggleButtonGroup();
        buttonGroup.setAllowsClearingSelection(true);

        for (AbstractCommandButton button : RibbonUtil.getButtons(roiBand))
            buttonGroup.add((IcyCommandToggleButton) button);

        // no tool action by default
        currentTool = "";

        PluginLoader.addListener(this);
    }

    protected IcyCommandToggleButton getButtonFromName(String name)
    {
        if (StringUtil.isEmpty(name))
            return null;

        for (AbstractCommandButton button : RibbonUtil.getButtons(roiBand))
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
    protected void setSelectedButton(JCommandToggleButton button)
    {
        if (getSelectedButton() != button)
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
        setSelectedButton(getButtonFromName(value));
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
    public void onSequenceActivationChange()
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
