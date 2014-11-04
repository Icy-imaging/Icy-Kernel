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
import icy.image.lut.LUT;
import icy.imagej.ImageJWrapper;
import icy.painter.Overlay;
import icy.painter.Painter;
import icy.plugin.abstract_.Plugin;
import icy.preferences.XMLPreferences;
import icy.roi.ROI;
import icy.search.SearchEngine;
import icy.sequence.Sequence;
import icy.swimmingPool.SwimmingPool;
import icy.type.collection.CollectionUtil;
import icy.undo.IcyUndoManager;
import icy.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 * MainInterfaceBatch
 * Default implementation used when Icy is launched in batch mode, without any GUI
 * 
 * @see icy.gui.main.MainInterfaceGui
 * @author Nicolas HERVE & Stephane
 */
public class MainInterfaceBatch implements MainInterface
{
    /**
     * Swimming Pool can be useful even in batch mode
     */
    private final SwimmingPool swimmingPool;
    /**
     * We keep trace of active sequence.
     */
    private Sequence activeSequence;
    /**
     * We keep trace of active plugin.
     */
    private Plugin activePlugin;

    public MainInterfaceBatch()
    {
        swimmingPool = new SwimmingPool();
    }

    @Override
    public void init()
    {
        activeSequence = null;
        activePlugin = null;
    }

    @Override
    public boolean isHeadLess()
    {
        // always true with this interface
        return true;
    }

    @Override
    public ArrayList<JFrame> getExternalFrames()
    {
        return new ArrayList<JFrame>();
    }

    @Override
    public ArrayList<JInternalFrame> getInternalFrames()
    {
        return new ArrayList<JInternalFrame>();
    }

    @Override
    public XMLPreferences getPreferences()
    {
        return null;
    }

    @Override
    public InspectorPanel getInspector()
    {
        return null;
    }

    @Override
    public RoisPanel getRoisPanel()
    {
        return null;
    }

    @Override
    public LayersPanel getLayersPanel()
    {
        return null;
    }

    @Override
    public ArrayList<Plugin> getActivePlugins()
    {
        return CollectionUtil.createArrayList(activePlugin, false);
    }

    @Override
    public LUT getActiveLUT()
    {
        return null;
    }

    @Override
    public Viewer getActiveViewer()
    {
        return null;
    }

    @Override
    public Sequence getActiveSequence()
    {
        return activeSequence;
    }

    @Override
    public IcyBufferedImage getActiveImage()
    {
        if (activeSequence != null)
            return activeSequence.getFirstImage();

        return null;
    }

    @Override
    public IcyUndoManager getUndoManager()
    {
        if (activeSequence != null)
            return activeSequence.getUndoManager();

        return null;
    }

    @Override
    public boolean undo()
    {
        if (activeSequence != null)
            return activeSequence.undo();

        return false;
    }

    @Override
    public boolean redo()
    {
        if (activeSequence != null)
            return activeSequence.redo();

        return false;
    }

    @Override
    public Viewer getFocusedViewer()
    {
        return getActiveViewer();
    }

    @Override
    public Sequence getFocusedSequence()
    {
        return getActiveSequence();
    }

    @Override
    public IcyBufferedImage getFocusedImage()
    {
        return getActiveImage();
    }

    @Override
    public ArrayList<Viewer> getViewers()
    {
        return new ArrayList<Viewer>();
    }

    @Override
    public void setFocusedViewer(Viewer viewer)
    {
    }

    @Override
    public void setActiveViewer(Viewer viewer)
    {
    }

    @Override
    public void addToDesktopPane(JInternalFrame internalFrame)
    {
    }

    @Override
    public IcyDesktopPane getDesktopPane()
    {
        return null;
    }

    @Override
    public ApplicationMenu getApplicationMenu()
    {
        return null;
    }

    @Override
    public TaskFrameManager getTaskWindowManager()
    {
        return null;
    }

    @Override
    public void registerPlugin(Plugin plugin)
    {
        if (plugin != null)
            activePlugin = plugin;
    }

    @Override
    public void unRegisterPlugin(Plugin plugin)
    {
        if (plugin == activePlugin)
            activePlugin = null;
    }

    @Override
    public void registerViewer(Viewer viewer)
    {
    }

    @Override
    public void unRegisterViewer(Viewer viewer)
    {
    }

    // @Override
    // public void registerStreamPlugin(PluginStreamGenerator pluginStreamGenerator)
    // {
    // }

    @Override
    @Deprecated
    public MainFrame getFrame()
    {
        return getMainFrame();
    }

    @Override
    public MainFrame getMainFrame()
    {
        return null;
    }

    @Override
    public void closeSequence(Sequence sequence)
    {
        if (sequence == activeSequence)
            activeSequence = null;
    }

    @Override
    public void closeViewersOfSequence(Sequence sequence)
    {
        closeSequence(sequence);
    }

    @Override
    public void closeAllViewers()
    {
        activeSequence = null;
    }

    @Override
    public Viewer getFirstViewer(Sequence sequence)
    {
        return null;
    }

    @Override
    public ArrayList<Viewer> getViewers(Sequence sequence)
    {
        return new ArrayList<Viewer>();
    }

    @Override
    public boolean isUniqueViewer(Viewer viewer)
    {
        return false;
    }

    @Override
    public ArrayList<Sequence> getSequences()
    {
        if (activeSequence != null)
            return CollectionUtil.createArrayList(activeSequence);

        return new ArrayList<Sequence>();
    }

    @Override
    public ArrayList<Sequence> getSequences(String name)
    {
        if ((activeSequence != null) && StringUtil.equals(name, activeSequence.getName()))
            return CollectionUtil.createArrayList(activeSequence);

        return new ArrayList<Sequence>();
    }

    @Override
    public Sequence getFirstSequencesContaining(ROI roi)
    {
        return getFirstSequenceContaining(roi);
    }

    @Deprecated
    @Override
    public Sequence getFirstSequencesContaining(Painter painter)
    {
        return getFirstSequenceContaining(painter);
    }

    @Override
    public ArrayList<Sequence> getSequencesContaining(ROI roi)
    {
        if ((activeSequence != null) && activeSequence.contains(roi))
            return CollectionUtil.createArrayList(activeSequence);

        return new ArrayList<Sequence>();
    }

    @Deprecated
    @Override
    public ArrayList<Sequence> getSequencesContaining(Painter painter)
    {
        if ((activeSequence != null) && activeSequence.contains(painter))
            return CollectionUtil.createArrayList(activeSequence);

        return new ArrayList<Sequence>();
    }

    @Override
    public ArrayList<Sequence> getSequencesContaining(Overlay overlay)
    {
        if ((activeSequence != null) && activeSequence.contains(overlay))
            return CollectionUtil.createArrayList(activeSequence);

        return new ArrayList<Sequence>();
    }

    @Override
    public ArrayList<ROI> getROIs()
    {
        if (activeSequence != null)
            return activeSequence.getROIs();

        // TODO: add ROI from swimming pool ?

        return new ArrayList<ROI>();
    }

    @Override
    @Deprecated
    public ROI getROI(Painter painter)
    {
        if (painter instanceof Overlay)
            return getROI((Overlay) painter);

        return null;
    }

    @Override
    public ROI getROI(Overlay overlay)
    {
        final List<ROI> rois = getROIs();

        for (ROI roi : rois)
            if (roi.getOverlay() == overlay)
                return roi;

        return null;
    }

    @Deprecated
    @Override
    public ArrayList<Painter> getPainters()
    {
        if (activeSequence != null)
            return activeSequence.getPainters();

        // TODO: add Painter from swimming pool ?

        return new ArrayList<Painter>();
    }

    @Override
    public List<Overlay> getOverlays()
    {
        if (activeSequence != null)
            return activeSequence.getOverlays();

        // TODO: add Overlay from swimming pool ?

        return new ArrayList<Overlay>();
    }

    @Override
    public SwimmingPool getSwimmingPool()
    {
        return swimmingPool;
    }

    @Override
    public String getSelectedTool()
    {
        return null;
    }

    @Override
    public void setSelectedTool(String command)
    {
    }

    @Override
    public ToolRibbonTask getToolRibbon()
    {
        return null;
    }

    @Override
    public boolean isAlwaysOnTop()
    {
        return false;
    }

    @Override
    public boolean isDetachedMode()
    {
        return false;
    }

    @Override
    public void setAlwaysOnTop(boolean value)
    {
    }

    @Override
    public void setDetachedMode(boolean value)
    {
    }

    @Deprecated
    @Override
    public void addListener(MainListener listener)
    {
    }

    @Deprecated
    @Override
    public void removeListener(MainListener listener)
    {
    }

    @Override
    public void addCanExitListener(AcceptListener listener)
    {
    }

    @Override
    public void removeCanExitListener(AcceptListener listener)
    {
    }

    @Deprecated
    @Override
    public void beginUpdate()
    {
    }

    @Deprecated
    @Override
    public void endUpdate()
    {
    }

    @Deprecated
    @Override
    public boolean isUpdating()
    {
        return false;
    }

    @Deprecated
    @Override
    public void registerExternalFrame(JFrame frame)
    {
    }

    @Deprecated
    @Override
    public void unRegisterExternalFrame(JFrame frame)
    {
    }

    @Override
    public boolean isOpened(Sequence sequence)
    {
        return (sequence == activeSequence);
    }

    @Override
    public Sequence getFirstSequenceContaining(ROI roi)
    {
        if ((activeSequence != null) && activeSequence.contains(roi))
            return activeSequence;

        return null;
    }

    @Override
    @Deprecated
    public Sequence getFirstSequenceContaining(Painter painter)
    {
        if ((activeSequence != null) && activeSequence.contains(painter))
            return activeSequence;

        return null;
    }

    @Override
    public Sequence getFirstSequenceContaining(Overlay overlay)
    {
        if ((activeSequence != null) && activeSequence.contains(overlay))
            return activeSequence;

        return null;
    }

    @Override
    public Viewer getFirstViewerContaining(ROI roi)
    {
        return null;
    }

    @Deprecated
    @Override
    public Viewer getFirstViewerContaining(Painter painter)
    {
        return null;
    }

    @Override
    public Viewer getFirstViewerContaining(Overlay overlay)
    {
        return null;
    }

    @Override
    public boolean canExitExternal()
    {
        return true;
    }

    @Override
    public ImageJWrapper getImageJ()
    {
        return null;
    }

    @Deprecated
    @Override
    public void addFocusedViewerListener(FocusedViewerListener listener)
    {
    }

    @Deprecated
    @Override
    public void removeFocusedViewerListener(FocusedViewerListener listener)
    {
    }

    @Deprecated
    @Override
    public void addFocusedSequenceListener(FocusedSequenceListener listener)
    {
    }

    @Deprecated
    @Override
    public void removeFocusedSequenceListener(FocusedSequenceListener listener)
    {
    }

    @Override
    public void addGlobalViewerListener(GlobalViewerListener listener)
    {
    }

    @Override
    public void removeGlobalViewerListener(GlobalViewerListener listener)
    {
    }

    @Override
    public void addGlobalSequenceListener(GlobalSequenceListener listener)
    {
    }

    @Override
    public void removeGlobalSequenceListener(GlobalSequenceListener listener)
    {
    }

    @Override
    public void addGlobalROIListener(GlobalROIListener listener)
    {
    }

    @Override
    public void removeGlobalROIListener(GlobalROIListener listener)
    {
    }

    @Override
    public void addGlobalOverlayListener(GlobalOverlayListener listener)
    {
    }

    @Override
    public void removeGlobalOverlayListener(GlobalOverlayListener listener)
    {
    }

    @Override
    public void addGlobalPluginListener(GlobalPluginListener listener)
    {
    }

    @Override
    public void removeGlobalPluginListener(GlobalPluginListener listener)
    {
    }

    @Override
    public void addActiveViewerListener(ActiveViewerListener listener)
    {
    }

    @Override
    public void removeActiveViewerListener(ActiveViewerListener listener)
    {
    }

    @Override
    public void addActiveSequenceListener(ActiveSequenceListener listener)
    {
    }

    @Override
    public void removeActiveSequenceListener(ActiveSequenceListener listener)
    {
    }

    @Override
    public void addSequence(Sequence sequence)
    {
        if (sequence != null)
            activeSequence = sequence;
    }

    @Override
    public void setGlobalViewSyncId(int id)
    {

    }

    @Override
    public SearchEngine getSearchEngine()
    {
        return null;
    }
}
