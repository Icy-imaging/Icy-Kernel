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

import icy.common.EventHierarchicalChecker;
import icy.common.listener.AcceptListener;
import icy.gui.inspector.InspectorPanel;
import icy.gui.inspector.RoisPanel;
import icy.gui.menu.ApplicationMenu;
import icy.gui.menu.ToolRibbonTask;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.imagej.ImageJWrapper;
import icy.painter.Painter;
import icy.plugin.abstract_.Plugin;
import icy.preferences.XMLPreferences;
import icy.roi.ROI;
import icy.search.SearchEngine;
import icy.sequence.Sequence;
import icy.swimmingPool.SwimmingPool;

import java.util.ArrayList;

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

    public MainInterfaceBatch()
    {
        swimmingPool = new SwimmingPool();
    }

    @Override
    public void init()
    {

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
    public ArrayList<Plugin> getActivePlugins()
    {
        return null;
    }

    @Override
    public Viewer getFocusedViewer()
    {
        return null;
    }

    @Override
    public Sequence getFocusedSequence()
    {
        return null;
    }

    @Override
    public IcyBufferedImage getFocusedImage()
    {
        return null;
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
    }

    @Override
    public void unRegisterPlugin(Plugin plugin)
    {
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
    public void closeViewersOfSequence(Sequence sequence)
    {
    }

    @Override
    public void closeAllViewers()
    {
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
    public Viewer getActiveViewer()
    {
        return null;
    }

    @Override
    public ArrayList<Sequence> getSequences()
    {
        return new ArrayList<Sequence>();
    }

    @Override
    public ArrayList<Sequence> getSequences(String name)
    {
        return new ArrayList<Sequence>();
    }

    @Override
    public Sequence getFirstSequencesContaining(ROI roi)
    {
        return null;
    }

    @Override
    public Sequence getFirstSequencesContaining(Painter painter)
    {
        return null;
    }

    @Override
    public ArrayList<Sequence> getSequencesContaining(ROI roi)
    {
        return new ArrayList<Sequence>();
    }

    @Override
    public ArrayList<Sequence> getSequencesContaining(Painter painter)
    {
        return new ArrayList<Sequence>();
    }

    @Override
    public ArrayList<ROI> getROIs()
    {
        return new ArrayList<ROI>();
    }

    @Override
    public ROI getROI(Painter painter)
    {
        return null;
    }

    @Override
    public ArrayList<Painter> getPainters()
    {
        return new ArrayList<Painter>();
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

    @Override
    public void addListener(MainListener listener)
    {
    }

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

    @Override
    public void beginUpdate()
    {
    }

    @Override
    public void endUpdate()
    {
    }

    @Override
    public boolean isUpdating()
    {
        return false;
    }

    @Override
    public void onChanged(EventHierarchicalChecker object)
    {
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
        return false;
    }

    @Override
    public Sequence getFirstSequenceContaining(ROI roi)
    {
        return null;
    }

    @Override
    public Sequence getFirstSequenceContaining(Painter painter)
    {
        return null;
    }

    @Override
    public Viewer getFirstViewerContaining(ROI roi)
    {
        return null;
    }

    @Override
    public Viewer getFirstViewerContaining(Painter painter)
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

    @Override
    public void addFocusedViewerListener(FocusedViewerListener listener)
    {

    }

    @Override
    public void removeFocusedViewerListener(FocusedViewerListener listener)
    {

    }

    @Override
    public void addFocusedSequenceListener(FocusedSequenceListener listener)
    {

    }

    @Override
    public void removeFocusedSequenceListener(FocusedSequenceListener listener)
    {

    }

    @Override
    public void addSequence(Sequence sequence)
    {

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
