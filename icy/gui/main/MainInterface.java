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

package icy.gui.main;

import icy.common.listener.AcceptListener;
import icy.gui.inspector.InspectorPanel;
import icy.gui.inspector.LayersPanel;
import icy.gui.inspector.RoisPanel;
import icy.gui.menu.ApplicationMenu;
import icy.gui.menu.ToolRibbonTask;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.imagej.ImageJWrapper;
import icy.painter.Overlay;
import icy.painter.Painter;
import icy.plugin.abstract_.Plugin;
import icy.preferences.XMLPreferences;
import icy.roi.ROI;
import icy.search.SearchEngine;
import icy.sequence.Sequence;
import icy.swimmingPool.SwimmingPool;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 * MainInterface
 * 
 * @see icy.gui.main.MainInterfaceGui
 * @author Fabrice de Chaumont & Stephane
 */
public interface MainInterface
{
    /**
     * Creates the windows in the Icy.getMainInterface()
     */
    public abstract void init();

    /**
     * Check if exit is allowed from registered listeners
     */
    public abstract boolean canExitExternal();

    /**
     * Open a viewer for the specified sequence.
     */
    public abstract void addSequence(Sequence sequence);

    /**
     * Returns all internal frames
     */
    public abstract ArrayList<JInternalFrame> getInternalFrames();

    /**
     * Returns all external frames
     */
    public abstract ArrayList<JFrame> getExternalFrames();

    public abstract XMLPreferences getPreferences();

    /**
     * Returns the inspector object (right informations panel)
     */
    public abstract InspectorPanel getInspector();

    /**
     * Returns the ROI manager panel
     */
    public abstract RoisPanel getRoisPanel();

    /**
     * Returns the Layer manager panel
     */
    public abstract LayersPanel getLayersPanel();

    /**
     * Returns the currently active plugins
     */
    public abstract ArrayList<Plugin> getActivePlugins();

    /**
     * Returns the active viewer window.
     * Returns <code>null</code> if there is no sequence opened.
     */
    public abstract Viewer getActiveViewer();

    /**
     * Returns the current active sequence.<br>
     * Returns <code>null</code> if there is no sequence opened.
     */
    public abstract Sequence getActiveSequence();

    /**
     * Returns the current active image.<br>
     * It can return <code>null</code> if the active viewer is <code>null</code> or
     * if it uses 3D display so prefer {@link #getActiveSequence()} instead.
     */
    public abstract IcyBufferedImage getActiveImage();

    /**
     * @deprecated Use {@link #getActiveViewer()} instead.
     */
    @Deprecated
    public abstract Viewer getFocusedViewer();

    /**
     * @deprecated Use {@link #getActiveSequence()} instead.
     */
    @Deprecated
    public abstract Sequence getFocusedSequence();

    /**
     * @deprecated Use {@link #getActiveImage()} instead.
     */
    @Deprecated
    public abstract IcyBufferedImage getFocusedImage();

    /**
     * Returns all active viewers
     */
    public abstract ArrayList<Viewer> getViewers();

    /**
     * Set the current active viewer.
     * 
     * @param viewer
     *        viewer which received activation
     */
    public abstract void setActiveViewer(Viewer viewer);

    /**
     * @deprecated Use {@link #setActiveViewer(Viewer)} instead.
     */
    @Deprecated
    public abstract void setFocusedViewer(Viewer viewer);

    /**
     * Set all active viewers to specified synchronization group id (0 means unsynchronized).
     */
    public abstract void setGlobalViewSyncId(int id);

    /**
     * Add the frame to the Desktop pane and change its layer value to make it over the other
     * internal frames.
     * 
     * @param internalFrame
     */
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

    /**
     * @deprecated Use {@link #getMainFrame()} instead
     */
    @Deprecated
    public abstract MainFrame getFrame();

    /**
     * Get Icy main frame
     */
    public abstract MainFrame getMainFrame();

    /**
     * Get Icy main searh engine.
     */
    public abstract SearchEngine getSearchEngine();

    /**
     * Close all viewers displaying the specified sequence.
     */
    public abstract void closeSequence(Sequence sequence);

    /**
     * @deprecated Use {@link #closeSequence(Sequence)} instead.
     */
    @Deprecated
    public abstract void closeViewersOfSequence(Sequence sequence);

    /**
     * Close all viewers
     */
    public abstract void closeAllViewers();

    /**
     * Returns first viewer for the sequence containing specified ROI
     */
    public abstract Viewer getFirstViewerContaining(ROI roi);

    /**
     * Returns first viewer for the sequence containing specified Painter.
     * 
     * @deprecated use {@link #getFirstViewerContaining(Overlay)} instead.
     */
    @Deprecated
    public abstract Viewer getFirstViewerContaining(Painter painter);

    /**
     * Returns first viewer for the sequence containing specified Overlay
     */
    public abstract Viewer getFirstViewerContaining(Overlay overlay);

    /**
     * Returns first viewer attached to specified sequence
     */
    public abstract Viewer getFirstViewer(Sequence sequence);

    /**
     * Returns viewers attached to specified sequence
     */
    public abstract ArrayList<Viewer> getViewers(Sequence sequence);

    /**
     * Returns true if specified viewer is the unique viewer for its attached sequence
     */
    public abstract boolean isUniqueViewer(Viewer viewer);

    /**
     * Returns list of active sequence (displayed in a viewer)
     */
    public abstract ArrayList<Sequence> getSequences();

    /**
     * Returns list of active sequence (displayed in a viewer) matching the specified name.
     */
    public abstract ArrayList<Sequence> getSequences(String name);

    /**
     * Returns true if specified sequence is currently opened (displayed in a viewer)
     */
    public abstract boolean isOpened(Sequence sequence);

    /**
     * Use {@link #getFirstSequenceContaining(ROI)} instead
     * 
     * @deprecated
     */
    @Deprecated
    public abstract Sequence getFirstSequencesContaining(ROI roi);

    /**
     * Use {@link #getFirstSequenceContaining(Overlay)} instead
     * 
     * @deprecated
     */
    @Deprecated
    public abstract Sequence getFirstSequencesContaining(Painter painter);

    /**
     * Returns the first active sequence containing the specified ROI
     */
    public abstract Sequence getFirstSequenceContaining(ROI roi);

    /**
     * Returns the first active sequence containing the specified Painter
     * 
     * @deprecated Use {@link #getFirstSequenceContaining(Overlay)} instead.
     */
    @Deprecated
    public abstract Sequence getFirstSequenceContaining(Painter painter);

    /**
     * Returns the first active sequence containing the specified Overlay
     */
    public abstract Sequence getFirstSequenceContaining(Overlay overlay);

    /**
     * Returns all active sequence containing the specified ROI
     */
    public abstract ArrayList<Sequence> getSequencesContaining(ROI roi);

    /**
     * Returns all active sequence containing the specified Painter
     * 
     * @deprecated Use {@link #getSequencesContaining(Overlay)} instead.
     */
    @Deprecated
    public abstract ArrayList<Sequence> getSequencesContaining(Painter painter);

    /**
     * Returns all active sequence containing the specified Overlay
     */
    public abstract List<Sequence> getSequencesContaining(Overlay overlay);

    /**
     * Returns all active ROI
     */
    public abstract ArrayList<ROI> getROIs();

    /**
     * Returns the ROI containing the specified painter (if any).
     * 
     * @deprecated Use {@link #getROI(Overlay)} instead.
     */
    @Deprecated
    public abstract ROI getROI(Painter painter);

    /**
     * Returns the ROI containing the specified overlay (if any)
     */
    public abstract ROI getROI(Overlay overlay);

    /**
     * Returns all active Painter.
     * 
     * @deprecated Use {@link #getOverlays()} instead.
     */
    @Deprecated
    public abstract ArrayList<Painter> getPainters();

    /**
     * Returns all active Overlay.
     */
    public abstract List<Overlay> getOverlays();

    /**
     * Returns the SwimmingPool object
     */
    public abstract SwimmingPool getSwimmingPool();

    /**
     * Returns the ImageJ object instance
     */
    public abstract ImageJWrapper getImageJ();

    /**
     * Returns current selected tool (ROI / Selection)
     */
    public abstract String getSelectedTool();

    /**
     * Set current selected tool (ROI / Selection)
     */
    public abstract void setSelectedTool(String command);

    /**
     * Returns the tool task of the Ribbon menu.
     */
    public abstract ToolRibbonTask getToolRibbon();

    /**
     * Returns true if the main frame is set as "always on top"
     */
    public abstract boolean isAlwaysOnTop();

    /**
     * Set the main frame as "always on top"
     */
    public abstract void setAlwaysOnTop(boolean value);

    /**
     * Returns true if the application is in "detached" mode
     */
    public abstract boolean isDetachedMode();

    /**
     * Set the the application is in "detached" mode
     */
    public abstract void setDetachedMode(boolean value);

    /**
     * @deprecated Use addGlobalXXXListener instead.
     */
    @Deprecated
    public abstract void addListener(MainListener listener);

    /**
     * @deprecated Use removeGlobalXXXListener instead.
     */
    @Deprecated
    public abstract void removeListener(MainListener listener);

    /**
     * Add global Viewer listener
     */
    public abstract void addGlobalViewerListener(GlobalViewerListener listener);

    /**
     * Remove global Viewer listener
     */
    public abstract void removeGlobalViewerListener(GlobalViewerListener listener);

    /**
     * Add global Sequence listener
     */
    public abstract void addGlobalSequenceListener(GlobalSequenceListener listener);

    /**
     * Remove global Sequence listener
     */
    public abstract void removeGlobalSequenceListener(GlobalSequenceListener listener);

    /**
     * Add global ROI listener
     */
    public abstract void addGlobalROIListener(GlobalROIListener listener);

    /**
     * Remove global ROI listener
     */
    public abstract void removeGlobalROIListener(GlobalROIListener listener);

    /**
     * Add global Overlay listener
     */
    public abstract void addGlobalOverlayListener(GlobalOverlayListener listener);

    /**
     * Remove global Overlay listener
     */
    public abstract void removeGlobalOverlayListener(GlobalOverlayListener listener);

    /**
     * Add global Plugin listener
     */
    public abstract void addGlobalPluginListener(GlobalPluginListener listener);

    /**
     * Remove global Plugin listener
     */
    public abstract void removeGlobalPluginListener(GlobalPluginListener listener);

    /**
     * @deprecated Use {@link #addActiveViewerListener(ActiveViewerListener)} instead.
     */
    @Deprecated
    public abstract void addFocusedViewerListener(FocusedViewerListener listener);

    /**
     * @deprecated Use {@link #removeActiveViewerListener(ActiveViewerListener)} instead.
     */
    @Deprecated
    public abstract void removeFocusedViewerListener(FocusedViewerListener listener);

    /**
     * Add active viewer listener.<br>
     * This permit to receive events of activated viewer only.<br>
     * It can also be used to detect viewer activation change.
     */
    public abstract void addActiveViewerListener(ActiveViewerListener listener);

    /**
     * Remove active viewer listener.
     */
    public abstract void removeActiveViewerListener(ActiveViewerListener listener);

    /**
     * @deprecated Use {@link #addActiveSequenceListener(ActiveSequenceListener)} instead.
     */
    @Deprecated
    public abstract void addFocusedSequenceListener(FocusedSequenceListener listener);

    /**
     * @deprecated Use {@link #removeActiveSequenceListener(ActiveSequenceListener)} instead.
     */
    @Deprecated
    public abstract void removeFocusedSequenceListener(FocusedSequenceListener listener);

    /**
     * Add active sequence listener.<br>
     * This permit to receive events of activated sequence only.<br>
     * It can also be used to detect sequence activation change.
     */
    public abstract void addActiveSequenceListener(ActiveSequenceListener listener);

    /**
     * Remove focused sequence listener.
     */
    public abstract void removeActiveSequenceListener(ActiveSequenceListener listener);

    /**
     * Add "can exit" listener.<br>
     * <br>
     * CAUTION : A weak reference is used to reference the listener for easier release<br>
     * so you should have a hard reference to your listener to keep it alive.
     */
    public abstract void addCanExitListener(AcceptListener listener);

    /**
     * Remove "can exit" listener
     */
    public abstract void removeCanExitListener(AcceptListener listener);

    /**
     * @deprecated
     */
    @Deprecated
    public abstract void beginUpdate();

    /**
     * @deprecated
     */
    @Deprecated
    public abstract void endUpdate();

    /**
     * @deprecated
     */
    @Deprecated
    public abstract boolean isUpdating();
}