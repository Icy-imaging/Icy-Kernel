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

package icy.gui.main;

import icy.common.AcceptListener;
import icy.common.EventHierarchicalChecker;
import icy.gui.inspector.InspectorPanel;
import icy.gui.menu.ApplicationMenu;
import icy.gui.menu.ToolRibbonTask;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.painter.Painter;
import icy.plugin.abstract_.Plugin;
import icy.preferences.XMLPreferences;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.swimmingPool.SwimmingPool;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 * MainInterface
 * 
 * @see icy.gui.main.MainInterfaceGui
 * @author Fabrice de Chaumont
 */

public interface MainInterface
{
    public abstract void init();

    /**
     * check if exit is allowed from registered listeners
     */
    public abstract boolean canExitExternal();

    public abstract ArrayList<JFrame> getExternalFrames();

    public abstract XMLPreferences getPreferences();

    public abstract InspectorPanel getInspector();

    public abstract ArrayList<Plugin> getActivePlugins();

    public abstract Viewer getFocusedViewer();

    public abstract Sequence getFocusedSequence();

    public abstract IcyBufferedImage getFocusedImage();

    public abstract ArrayList<Viewer> getViewers();

    public abstract void setFocusedViewer(Viewer viewer);

    public abstract void addToDesktopPane(JInternalFrame internalFrame);

    public abstract IcyDesktopPane getDesktopPane();

    public abstract ApplicationMenu getApplicationMenu();

    public abstract TaskFrameManager getTaskWindowManager();

    @Deprecated
    public abstract void registerExternalFrame(JFrame frame);

    @Deprecated
    public abstract void unRegisterExternalFrame(JFrame frame);

    public abstract void registerPlugin(Plugin plugin);

    public abstract void unRegisterPlugin(Plugin plugin);

    public abstract void registerViewer(Viewer viewer);

    public abstract void unRegisterViewer(Viewer viewer);

    // public abstract void registerStreamPlugin(PluginStreamGenerator pluginStreamGenerator);

    public abstract MainFrame getFrame();

    public abstract void closeViewersOfSequence(Sequence sequence);

    public abstract void closeAllViewers();

    /**
     * Return first viewer for the sequence containing specified ROI
     */
    public abstract Viewer getFirstViewerContaining(ROI roi);

    /**
     * Return first viewer for the sequence containing specified Painter
     */
    public abstract Viewer getFirstViewerContaining(Painter painter);

    public abstract Viewer getFirstViewer(Sequence sequence);

    public abstract ArrayList<Viewer> getViewers(Sequence sequence);

    public abstract boolean isUniqueViewer(Viewer viewer);

    public abstract Viewer getActiveViewer();

    public abstract ArrayList<Sequence> getSequences();

    public abstract boolean isOpened(Sequence sequence);

    /**
     * Use {@link #getFirstSequenceContaining(ROI)} instead
     * 
     * @deprecated
     */
    @Deprecated
    public abstract Sequence getFirstSequencesContaining(ROI roi);

    /**
     * Use {@link #getFirstSequenceContaining(Painter)} instead
     * 
     * @deprecated
     */
    @Deprecated
    public abstract Sequence getFirstSequencesContaining(Painter painter);

    /**
     * Return the first active sequence containing the specified ROI
     */
    public abstract Sequence getFirstSequenceContaining(ROI roi);

    /**
     * Return the first active sequence containing the specified Painter
     */
    public abstract Sequence getFirstSequenceContaining(Painter painter);

    public abstract ArrayList<Sequence> getSequencesContaining(ROI roi);

    public abstract ArrayList<Sequence> getSequencesContaining(Painter painter);

    public abstract ArrayList<ROI> getROIs();

    public abstract ROI getROI(Painter painter);

    public abstract ArrayList<Painter> getPainters();

    public abstract SwimmingPool getSwimmingPool();

    public abstract String getSelectedTool();

    public abstract void setSelectedTool(String command);

    public abstract ToolRibbonTask getToolRibbon();

    public abstract boolean isAlwaysOnTop();

    public abstract void setAlwaysOnTop(boolean value);

    public abstract void addListener(MainListener listener);

    public abstract void removeListener(MainListener listener);

    public abstract void addCanExitListener(AcceptListener listener);

    public abstract void removeCanExitListener(AcceptListener listener);

    public abstract void beginUpdate();

    public abstract void endUpdate();

    public abstract boolean isUpdating();

    public abstract void onChanged(EventHierarchicalChecker object);

}