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
package icy.gui.main;

import icy.common.listener.AcceptListener;
import icy.common.listener.weak.WeakListener;
import icy.gui.frame.IcyFrame;
import icy.gui.inspector.InspectorPanel;
import icy.gui.inspector.LayersPanel;
import icy.gui.inspector.RoisPanel;
import icy.gui.main.MainEvent.MainEventSourceType;
import icy.gui.main.MainEvent.MainEventType;
import icy.gui.menu.ApplicationMenu;
import icy.gui.menu.ToolRibbonTask;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerAdapter;
import icy.gui.viewer.ViewerEvent;
import icy.gui.viewer.ViewerListener;
import icy.image.IcyBufferedImage;
import icy.image.lut.LUT;
import icy.imagej.ImageJWrapper;
import icy.main.Icy;
import icy.painter.Overlay;
import icy.painter.OverlayWrapper;
import icy.painter.Painter;
import icy.plugin.abstract_.Plugin;
import icy.preferences.IcyPreferences;
import icy.preferences.XMLPreferences;
import icy.roi.ROI;
import icy.search.SearchEngine;
import icy.sequence.Sequence;
import icy.sequence.SequenceAdapter;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import icy.swimmingPool.SwimmingPool;
import icy.system.thread.ThreadUtil;
import icy.undo.IcyUndoManager;
import icy.util.StringUtil;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.event.EventListenerList;

/**
 * MainInterfaceGui
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public class MainInterfaceGui implements MainInterface
{
    private class WeakAcceptListener extends WeakListener<AcceptListener> implements AcceptListener
    {
        public WeakAcceptListener(AcceptListener listener)
        {
            super(listener);
        }

        @Override
        public void removeListener(Object source)
        {
            internalRemoveCanExitListener(this);
        }

        @Override
        public boolean accept(Object source)
        {
            final AcceptListener listener = getListener();

            if (listener != null)
                return listener.accept(source);

            return true;
        }
    }

    private final EventListenerList listeners;
    // private final UpdateEventHandler updater;

    /**
     * used to generate focused sequence & viewer events
     */
    private final ViewerListener activeViewerListener;
    private final SequenceListener sequenceListener;

    private final List<Viewer> viewers;
    private final List<Sequence> sequences;
    private final List<WeakReference<Plugin>> activePlugins;

    private final SwimmingPool swimmingPool;
    private final TaskFrameManager taskFrameManager;

    MainFrame mainFrame;

    Viewer previousActiveViewer;
    Viewer activeViewer;
    Sequence activeSequence;

    /**
     * Take care that MainInterface constructor do not call the {@link Icy#getMainInterface()}
     * method.<br>
     * We use a separate {@link #init()} for that purpose.
     */
    public MainInterfaceGui()
    {
        listeners = new EventListenerList();
        viewers = new ArrayList<Viewer>();
        sequences = new ArrayList<Sequence>();
        activePlugins = new ArrayList<WeakReference<Plugin>>();
        swimmingPool = new SwimmingPool();
        taskFrameManager = new TaskFrameManager();

        // active viewer listener
        activeViewerListener = new ViewerAdapter()
        {
            @Override
            public void viewerChanged(ViewerEvent event)
            {
                activeViewerChanged(event);
            }
        };

        // global sequence listener
        sequenceListener = new SequenceAdapter()
        {
            @Override
            public void sequenceChanged(SequenceEvent event)
            {
                MainInterfaceGui.this.sequenceChanged(event);
            }
        };

        mainFrame = null;

        previousActiveViewer = null;
        activeViewer = null;
        activeSequence = null;
    }

    @Override
    public void init()
    {
        // build main frame
        mainFrame = new MainFrame();
        mainFrame.init();
        mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                // exit application
                Icy.exit(false);
            }
        });

        taskFrameManager.init();
    }

    @Override
    public boolean isHeadLess()
    {
        // we are not head less with this interface
        return false;
    }

    @Override
    public void addSequence(Sequence sequence)
    {
        if (sequence != null)
        {
            final Sequence seq = sequence;

            // thread safe
            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    new Viewer(seq);
                }
            });
        }
    }

    @Override
    public ArrayList<JFrame> getExternalFrames()
    {
        final ArrayList<JFrame> result = new ArrayList<JFrame>();
        final Window[] windows = Window.getWindows();

        for (Window w : windows)
            if (w instanceof JFrame)
                result.add((JFrame) w);

        return result;
    }

    @Override
    public ArrayList<JInternalFrame> getInternalFrames()
    {
        if (mainFrame == null)
            return new ArrayList<JInternalFrame>();

        return mainFrame.getInternalFrames();

    }

    /**
     * @return the preferences
     */
    @Override
    public XMLPreferences getPreferences()
    {
        return IcyPreferences.applicationRoot();
    }

    @Override
    public InspectorPanel getInspector()
    {
        if (mainFrame == null)
            return null;

        return mainFrame.getInspector();
    }

    @Override
    public RoisPanel getRoisPanel()
    {
        if (mainFrame == null)
            return null;

        final InspectorPanel inspector = mainFrame.getInspector();
        if (inspector == null)
            return null;

        return inspector.getRoisPanel();
    }

    @Override
    public LayersPanel getLayersPanel()
    {
        if (mainFrame == null)
            return null;

        final InspectorPanel inspector = mainFrame.getInspector();
        if (inspector == null)
            return null;

        return inspector.getLayersPanel();
    }

    @Override
    public ArrayList<Plugin> getActivePlugins()
    {
        final ArrayList<Plugin> result = new ArrayList<Plugin>();

        synchronized (activePlugins)
        {
            for (WeakReference<Plugin> ref : activePlugins)
            {
                final Plugin plugin = ref.get();

                if (plugin != null)
                    result.add(plugin);
            }
        }

        return result;
    }

    @Override
    public LUT getActiveLUT()
    {
        if (activeViewer != null)
            return activeViewer.getLut();

        return null;
    }

    @Override
    public Viewer getActiveViewer()
    {
        return activeViewer;
    }

    @Override
    public Sequence getActiveSequence()
    {
        return activeSequence;
    }

    @Override
    public IcyBufferedImage getActiveImage()
    {
        if (activeViewer != null)
            return activeViewer.getCurrentImage();

        return null;
    }

    @Override
    @Deprecated
    public Viewer getFocusedViewer()
    {
        return getActiveViewer();
    }

    @Override
    @Deprecated
    public Sequence getFocusedSequence()
    {
        return getActiveSequence();
    }

    @Override
    @Deprecated
    public IcyBufferedImage getFocusedImage()
    {
        return getActiveImage();
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
    public ArrayList<Viewer> getViewers()
    {
        synchronized (viewers)
        {
            return new ArrayList<Viewer>(viewers);
        }
    }

    @Override
    public synchronized void setActiveViewer(Viewer viewer)
    {
        if (activeViewer == viewer)
            return;

        // got a previously active viewer ?
        if (activeViewer != null)
        {
            // remove active viewer listener
            activeViewer.removeListener(activeViewerListener);

            // force previous viewer internal frame to release focus
            try
            {
                activeViewer.getInternalFrame().setSelected(false);
            }
            catch (PropertyVetoException e)
            {
                // ignore
            }
        }

        previousActiveViewer = activeViewer;
        activeViewer = viewer;

        // add active viewer listener
        if (activeViewer != null)
            activeViewer.addListener(activeViewerListener);

        // activation changed
        viewerActivationChanged(previousActiveViewer, activeViewer);
    }

    @Override
    @Deprecated
    public synchronized void setFocusedViewer(Viewer viewer)
    {
        setActiveViewer(viewer);
    }

    @Override
    public synchronized void addToDesktopPane(JInternalFrame internalFrame)
    {
        getDesktopPane().add(internalFrame, JLayeredPane.DEFAULT_LAYER);
    }

    @Override
    public IcyDesktopPane getDesktopPane()
    {
        if (mainFrame == null)
            return null;

        return mainFrame.getDesktopPane();
    }

    @Override
    public ApplicationMenu getApplicationMenu()
    {
        if (mainFrame == null)
            return null;

        return mainFrame.getApplicationMenu();
    }

    @Override
    public TaskFrameManager getTaskWindowManager()
    {
        return taskFrameManager;
    }

    private WeakReference<Plugin> getPluginReference(Plugin plugin)
    {
        synchronized (activePlugins)
        {
            for (WeakReference<Plugin> ref : activePlugins)
                if (ref.get() == plugin)
                    return ref;
        }

        return null;
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
    public synchronized void registerPlugin(Plugin plugin)
    {
        synchronized (activePlugins)
        {
            activePlugins.add(new WeakReference<Plugin>(plugin));
        }

        // plugin opened
        pluginStarted(plugin);
    }

    @Override
    public synchronized void unRegisterPlugin(Plugin plugin)
    {
        final WeakReference<Plugin> ref = getPluginReference(plugin);

        synchronized (activePlugins)
        {
            // reference found
            if (ref != null)
                activePlugins.remove(ref);
        }

        // plugin closed
        pluginEnded(plugin);
    }

    @Override
    public synchronized void registerViewer(Viewer viewer)
    {
        if (viewer == null)
            return;

        // viewer opened
        viewerOpened(viewer);
    }

    @Override
    public synchronized void unRegisterViewer(Viewer viewer)
    {
        if (viewer == null)
            return;

        // viewer closed
        viewerClosed(viewer);

        // no more opened viewer ?
        if (viewers.isEmpty())
            // set focus to null
            setActiveViewer(null);
        else
        {
            final IcyFrame frame = IcyFrame.findIcyFrame(getDesktopPane().getSelectedFrame());

            if (frame instanceof Viewer)
                ((Viewer) frame).requestFocus();
            else
            {
                // it was the active viewer ?
                if (activeViewer == viewer)
                {
                    // restore focus to previous active
                    if (previousActiveViewer != null)
                    {
                        setActiveViewer(previousActiveViewer);
                        // no more previous active now
                        previousActiveViewer = null;
                    }
                    else
                        // or just focus another one
                        setActiveViewer(viewers.get(viewers.size() - 1));
                }
            }
        }
    }

    @Override
    @Deprecated
    public MainFrame getFrame()
    {
        return getMainFrame();
    }

    @Override
    public MainFrame getMainFrame()
    {
        return mainFrame;
    }

    @Override
    public SearchEngine getSearchEngine()
    {
        return mainFrame.getSearchBar().getSearchEngine();
    }

    @Override
    public void closeSequence(Sequence sequence)
    {
        // use copy as this actually modify viewers list
        for (Viewer v : getViewers())
            if (v.getSequence() == sequence)
                v.close();
    }

    @Override
    public void closeViewersOfSequence(Sequence sequence)
    {
        closeSequence(sequence);
    }

    @Override
    public synchronized void closeAllViewers()
    {
        // use copy as this actually modify viewers list
        for (Viewer viewer : getViewers())
            viewer.close();
    }

    @Override
    public Viewer getFirstViewerContaining(ROI roi)
    {
        return getFirstViewer(getFirstSequenceContaining(roi));
    }

    @Deprecated
    @Override
    public Viewer getFirstViewerContaining(Painter painter)
    {
        return getFirstViewer(getFirstSequenceContaining(painter));
    }

    @Override
    public Viewer getFirstViewerContaining(Overlay overlay)
    {
        return getFirstViewer(getFirstSequenceContaining(overlay));
    }

    @Override
    public Viewer getFirstViewer(Sequence sequence)
    {
        if (sequence != null)
        {
            for (Viewer viewer : getViewers())
                if (viewer.getSequence() == sequence)
                    return viewer;
        }

        return null;
    }

    @Override
    public ArrayList<Viewer> getViewers(Sequence sequence)
    {
        final ArrayList<Viewer> result = new ArrayList<Viewer>();

        for (Viewer v : getViewers())
            if (v.getSequence() == sequence)
                result.add(v);

        return result;
    }

    @Override
    public boolean isUniqueViewer(Viewer viewer)
    {
        final List<Viewer> viewers = getViewers(viewer.getSequence());

        return (viewers.size() == 1) && (viewers.get(0) == viewer);
    }

    @Override
    public ArrayList<Sequence> getSequences()
    {
        synchronized (sequences)
        {
            return new ArrayList<Sequence>(sequences);
        }
    }

    @Override
    public ArrayList<Sequence> getSequences(String name)
    {
        final ArrayList<Sequence> result = new ArrayList<Sequence>();

        synchronized (viewers)
        {
            for (Viewer viewer : viewers)
            {
                final Sequence sequence = viewer.getSequence();

                // matching name and no duplicate
                if (!result.contains(sequence) && StringUtil.equals(name, sequence.getName()))
                    result.add(sequence);
            }
        }

        return result;
    }

    @Override
    public boolean isOpened(Sequence sequence)
    {
        return getSequences().contains(sequence);
    }

    @Deprecated
    @Override
    public Sequence getFirstSequencesContaining(ROI roi)
    {
        return getFirstSequenceContaining(roi);
    }

    @Override
    public Sequence getFirstSequenceContaining(ROI roi)
    {
        for (Sequence seq : getSequences())
            if (seq.contains(roi))
                return seq;

        return null;
    }

    @Deprecated
    @Override
    public Sequence getFirstSequencesContaining(Painter painter)
    {
        return getFirstSequenceContaining(painter);
    }

    @Deprecated
    @Override
    public Sequence getFirstSequenceContaining(Painter painter)
    {
        for (Sequence seq : getSequences())
            if (seq.contains(painter))
                return seq;

        return null;
    }

    @Override
    public Sequence getFirstSequenceContaining(Overlay overlay)
    {
        for (Sequence seq : getSequences())
            if (seq.contains(overlay))
                return seq;

        return null;
    }

    @Override
    public ArrayList<Sequence> getSequencesContaining(ROI roi)
    {
        final ArrayList<Sequence> result = getSequences();

        for (int i = result.size() - 1; i >= 0; i--)
            if (!result.get(i).contains(roi))
                result.remove(i);

        return result;
    }

    @Override
    @Deprecated
    public ArrayList<Sequence> getSequencesContaining(Painter painter)
    {
        final ArrayList<Sequence> result = getSequences();

        for (int i = result.size() - 1; i >= 0; i--)
            if (!result.get(i).contains(painter))
                result.remove(i);

        return result;
    }

    @Override
    public List<Sequence> getSequencesContaining(Overlay overlay)
    {
        final ArrayList<Sequence> result = getSequences();

        for (int i = result.size() - 1; i >= 0; i--)
            if (!result.get(i).contains(overlay))
                result.remove(i);

        return result;
    }

    @Override
    public ArrayList<ROI> getROIs()
    {
        // HashSet is better suited for add elements
        final HashSet<ROI> result = new HashSet<ROI>();

        for (Sequence seq : getSequences())
            for (ROI roi : seq.getROISet())
                result.add(roi);

        // TODO: add ROI from swimming pool ?

        return new ArrayList<ROI>(result);
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

    @Override
    @Deprecated
    public ArrayList<Painter> getPainters()
    {
        // HashSet better suited for add element
        final HashSet<Painter> result = new HashSet<Painter>();

        for (Sequence seq : getSequences())
            result.addAll(seq.getPainterSet());

        // TODO: add Painter from swimming pool ?

        return new ArrayList<Painter>();
    }

    @Override
    public List<Overlay> getOverlays()
    {
        // HashSet better suited for add element
        final HashSet<Overlay> result = new HashSet<Overlay>();

        for (Sequence seq : getSequences())
            result.addAll(seq.getOverlaySet());

        // TODO: add Overlay from swimming pool ?

        return new ArrayList<Overlay>();
    }

    @Override
    public SwimmingPool getSwimmingPool()
    {
        return swimmingPool;
    }

    @Override
    public ImageJWrapper getImageJ()
    {
        if (mainFrame == null)
            return null;

        return mainFrame.getMainRibbon().getImageJ();
    }

    @Override
    public String getSelectedTool()
    {
        if (mainFrame == null)
            return null;

        return mainFrame.getMainRibbon().getToolRibbon().getSelected();
    }

    @Override
    public void setSelectedTool(String command)
    {
        if (mainFrame != null)
            mainFrame.getMainRibbon().getToolRibbon().setSelected(command);
    }

    @Override
    public ToolRibbonTask getToolRibbon()
    {
        if (mainFrame == null)
            return null;

        return mainFrame.getMainRibbon().getToolRibbon();
    }

    @Override
    public boolean isAlwaysOnTop()
    {
        if (mainFrame == null)
            return false;

        return mainFrame.isAlwaysOnTop();
    }

    @Override
    public void setAlwaysOnTop(boolean value)
    {
        if (mainFrame != null)
            mainFrame.setAlwaysOnTop(value);
    }

    @Override
    public boolean isDetachedMode()
    {
        if (mainFrame == null)
            return false;

        return mainFrame.isDetachedMode();
    }

    @Override
    public void setDetachedMode(boolean value)
    {
        if (mainFrame != null)
            mainFrame.setDetachedMode(value);
    }

    @Override
    @Deprecated
    public synchronized void addListener(MainListener listener)
    {
        listeners.add(MainListener.class, listener);
    }

    @Override
    @Deprecated
    public synchronized void removeListener(MainListener listener)
    {
        listeners.remove(MainListener.class, listener);
    }

    @Override
    public synchronized void addGlobalViewerListener(GlobalViewerListener listener)
    {
        listeners.add(GlobalViewerListener.class, listener);
    }

    @Override
    public synchronized void removeGlobalViewerListener(GlobalViewerListener listener)
    {
        listeners.remove(GlobalViewerListener.class, listener);
    }

    @Override
    public synchronized void addGlobalSequenceListener(GlobalSequenceListener listener)
    {
        listeners.add(GlobalSequenceListener.class, listener);
    }

    @Override
    public synchronized void removeGlobalSequenceListener(GlobalSequenceListener listener)
    {
        listeners.remove(GlobalSequenceListener.class, listener);
    }

    @Override
    public synchronized void addGlobalROIListener(GlobalROIListener listener)
    {
        listeners.add(GlobalROIListener.class, listener);
    }

    @Override
    public synchronized void removeGlobalROIListener(GlobalROIListener listener)
    {
        listeners.remove(GlobalROIListener.class, listener);
    }

    @Override
    public synchronized void addGlobalOverlayListener(GlobalOverlayListener listener)
    {
        listeners.add(GlobalOverlayListener.class, listener);
    }

    @Override
    public synchronized void removeGlobalOverlayListener(GlobalOverlayListener listener)
    {
        listeners.remove(GlobalOverlayListener.class, listener);
    }

    @Override
    public synchronized void addGlobalPluginListener(GlobalPluginListener listener)
    {
        listeners.add(GlobalPluginListener.class, listener);
    }

    @Override
    public synchronized void removeGlobalPluginListener(GlobalPluginListener listener)
    {
        listeners.remove(GlobalPluginListener.class, listener);
    }

    @Override
    public synchronized void addCanExitListener(AcceptListener listener)
    {
        listeners.add(WeakAcceptListener.class, new WeakAcceptListener(listener));
    }

    @Override
    public synchronized void removeCanExitListener(AcceptListener listener)
    {
        // we use weak reference so we have to find base listener...
        for (WeakAcceptListener l : listeners.getListeners(WeakAcceptListener.class))
            if (listener == l.getListener())
                internalRemoveCanExitListener(l);
    }

    public synchronized void internalRemoveCanExitListener(WeakAcceptListener listener)
    {
        listeners.remove(WeakAcceptListener.class, listener);
    }

    @Deprecated
    @Override
    public synchronized void addFocusedViewerListener(FocusedViewerListener listener)
    {
        listeners.add(FocusedViewerListener.class, listener);
    }

    @Deprecated
    @Override
    public synchronized void removeFocusedViewerListener(FocusedViewerListener listener)
    {
        listeners.remove(FocusedViewerListener.class, listener);
    }

    @Deprecated
    @Override
    public synchronized void addFocusedSequenceListener(FocusedSequenceListener listener)
    {
        listeners.add(FocusedSequenceListener.class, listener);
    }

    @Deprecated
    @Override
    public synchronized void removeFocusedSequenceListener(FocusedSequenceListener listener)
    {
        listeners.remove(FocusedSequenceListener.class, listener);
    }

    @Override
    public synchronized void addActiveViewerListener(ActiveViewerListener listener)
    {
        listeners.add(ActiveViewerListener.class, listener);
    }

    @Override
    public synchronized void removeActiveViewerListener(ActiveViewerListener listener)
    {
        listeners.remove(ActiveViewerListener.class, listener);
    }

    @Override
    public synchronized void addActiveSequenceListener(ActiveSequenceListener listener)
    {
        listeners.add(ActiveSequenceListener.class, listener);
    }

    @Override
    public synchronized void removeActiveSequenceListener(ActiveSequenceListener listener)
    {
        listeners.remove(ActiveSequenceListener.class, listener);
    }

    /**
     * fire plugin opened event
     */
    @SuppressWarnings("deprecation")
    private void firePluginStartedEvent(Plugin plugin)
    {
        for (GlobalPluginListener listener : listeners.getListeners(GlobalPluginListener.class))
            listener.pluginStarted(plugin);

        // backward compatibility
        final MainEvent event = new MainEvent(MainEventSourceType.PLUGIN, MainEventType.OPENED, plugin);
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.pluginOpened(event);
    }

    /**
     * fire plugin closed event
     */
    @SuppressWarnings("deprecation")
    private void firePluginEndedEvent(Plugin plugin)
    {
        for (GlobalPluginListener listener : listeners.getListeners(GlobalPluginListener.class))
            listener.pluginEnded(plugin);

        // backward compatibility
        final MainEvent event = new MainEvent(MainEventSourceType.PLUGIN, MainEventType.CLOSED, plugin);
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.pluginClosed(event);
    }

    /**
     * fire viewer opened event
     */
    @SuppressWarnings("deprecation")
    private void fireViewerOpenedEvent(Viewer viewer)
    {
        for (GlobalViewerListener listener : listeners.getListeners(GlobalViewerListener.class))
            listener.viewerOpened(viewer);

        // backward compatibility
        final MainEvent event = new MainEvent(MainEventSourceType.VIEWER, MainEventType.OPENED, viewer);
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.viewerOpened(event);
    }

    /**
     * fire viewer close event
     */
    @SuppressWarnings("deprecation")
    private void fireViewerClosedEvent(Viewer viewer)
    {
        for (GlobalViewerListener listener : listeners.getListeners(GlobalViewerListener.class))
            listener.viewerClosed(viewer);

        // backward compatibility
        final MainEvent event = new MainEvent(MainEventSourceType.VIEWER, MainEventType.CLOSED, viewer);
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.viewerClosed(event);
    }

    /**
     * fire viewer deactive event
     */
    private void fireViewerDeactivatedEvent(Viewer viewer)
    {
        for (ActiveViewerListener listener : listeners.getListeners(ActiveViewerListener.class))
            listener.viewerDeactivated(viewer);
    }

    /**
     * fire viewer active event
     */
    @SuppressWarnings("deprecation")
    private void fireViewerActivatedEvent(Viewer viewer)
    {
        for (ActiveViewerListener listener : listeners.getListeners(ActiveViewerListener.class))
            listener.viewerActivated(viewer);

        // backward compatibility
        final MainEvent event = new MainEvent(MainEventSourceType.VIEWER, MainEventType.FOCUSED, viewer);
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.viewerFocused(event);
        for (FocusedViewerListener listener : listeners.getListeners(FocusedViewerListener.class))
            listener.focusChanged(viewer);
    }

    /**
     * fire sequence opened event
     */
    @SuppressWarnings("deprecation")
    private void fireSequenceOpenedEvent(Sequence sequence)
    {
        for (GlobalSequenceListener listener : listeners.getListeners(GlobalSequenceListener.class))
            listener.sequenceOpened(sequence);

        // backward compatibility
        final MainEvent event = new MainEvent(MainEventSourceType.SEQUENCE, MainEventType.OPENED, sequence);
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.sequenceOpened(event);
    }

    /**
     * fire sequence active event
     */
    @SuppressWarnings("deprecation")
    private void fireSequenceClosedEvent(Sequence sequence)
    {
        for (GlobalSequenceListener listener : listeners.getListeners(GlobalSequenceListener.class))
            listener.sequenceClosed(sequence);

        // backward compatibility
        final MainEvent event = new MainEvent(MainEventSourceType.SEQUENCE, MainEventType.CLOSED, sequence);
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.sequenceClosed(event);
    }

    /**
     * fire sequence deactive event
     */
    private void fireSequenceDeactivatedEvent(Sequence sequence)
    {
        for (ActiveSequenceListener listener : listeners.getListeners(ActiveSequenceListener.class))
            listener.sequenceDeactivated(sequence);
    }

    /**
     * fire sequence active event
     */
    @SuppressWarnings("deprecation")
    private void fireSequenceActivatedEvent(Sequence sequence)
    {
        for (ActiveSequenceListener listener : listeners.getListeners(ActiveSequenceListener.class))
            listener.sequenceActivated(sequence);

        // backward compatibility
        final MainEvent event = new MainEvent(MainEventSourceType.SEQUENCE, MainEventType.FOCUSED, sequence);
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.sequenceFocused(event);
        for (FocusedSequenceListener listener : listeners.getListeners(FocusedSequenceListener.class))
            listener.focusChanged(sequence);
    }

    /**
     * fire ROI added event
     */
    @SuppressWarnings("deprecation")
    private void fireRoiAddedEvent(ROI roi)
    {
        for (GlobalROIListener listener : listeners.getListeners(GlobalROIListener.class))
            listener.roiAdded(roi);

        // backward compatibility
        final MainEvent event = new MainEvent(MainEventSourceType.ROI, MainEventType.ADDED, roi);
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.roiAdded(event);
    }

    /**
     * fire ROI removed event
     */
    @SuppressWarnings("deprecation")
    private void fireRoiRemovedEvent(ROI roi)
    {
        for (GlobalROIListener listener : listeners.getListeners(GlobalROIListener.class))
            listener.roiRemoved(roi);

        // backward compatibility
        final MainEvent event = new MainEvent(MainEventSourceType.ROI, MainEventType.REMOVED, roi);
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.roiRemoved(event);
    }

    /**
     * fire painter added event
     */
    @SuppressWarnings("deprecation")
    private void fireOverlayAddedEvent(Overlay overlay)
    {
        for (GlobalOverlayListener listener : listeners.getListeners(GlobalOverlayListener.class))
            listener.overlayAdded(overlay);

        // backward compatibility
        final Painter painter;

        if (overlay instanceof OverlayWrapper)
            painter = ((OverlayWrapper) overlay).getPainter();
        else
            painter = overlay;

        final MainEvent event = new MainEvent(MainEventSourceType.PAINTER, MainEventType.ADDED, painter);
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.painterAdded(event);
    }

    /**
     * fire painter removed event
     */
    @SuppressWarnings("deprecation")
    private void fireOverlayRemovedEvent(Overlay overlay)
    {
        for (GlobalOverlayListener listener : listeners.getListeners(GlobalOverlayListener.class))
            listener.overlayRemoved(overlay);

        // backward compatibility
        final Painter painter;

        if (overlay instanceof OverlayWrapper)
            painter = ((OverlayWrapper) overlay).getPainter();
        else
            painter = overlay;

        final MainEvent event = new MainEvent(MainEventSourceType.PAINTER, MainEventType.REMOVED, painter);
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.painterRemoved(event);
    }

    /**
     * fire active viewer changed event
     */
    @SuppressWarnings("deprecation")
    private void fireActiveViewerChangedEvent(ViewerEvent event)
    {
        for (ActiveViewerListener listener : listeners.getListeners(ActiveViewerListener.class))
            listener.activeViewerChanged(event);

        // backward compatibility
        for (FocusedViewerListener listener : listeners.getListeners(FocusedViewerListener.class))
            listener.focusedViewerChanged(event);
    }

    /**
     * fire active sequence changed event
     */
    @SuppressWarnings("deprecation")
    private void fireActiveSequenceChangedEvent(SequenceEvent event)
    {
        for (ActiveSequenceListener listener : listeners.getListeners(ActiveSequenceListener.class))
            listener.activeSequenceChanged(event);

        // backward compatibility
        for (FocusedSequenceListener listener : listeners.getListeners(FocusedSequenceListener.class))
            listener.focusedSequenceChanged(event);
    }

    @Override
    public boolean canExitExternal()
    {
        for (AcceptListener listener : listeners.getListeners(WeakAcceptListener.class))
            if (!listener.accept(mainFrame))
                return false;

        return true;
    }

    @Deprecated
    @Override
    public void beginUpdate()
    {
        // updater.beginUpdate();
    }

    @Deprecated
    @Override
    public void endUpdate()
    {
        // updater.endUpdate();
    }

    @Deprecated
    @Override
    public boolean isUpdating()
    {
        return false;
        // return updater.isUpdating();
    }

    /**
     * called when a plugin is opened
     */
    private void pluginStarted(Plugin plugin)
    {
        firePluginStartedEvent(plugin);
    }

    /**
     * called when a plugin is closed
     */
    private void pluginEnded(Plugin plugin)
    {
        firePluginEndedEvent(plugin);
    }

    /**
     * called when a viewer is opened
     */
    private void viewerOpened(Viewer viewer)
    {
        final Sequence sequence = viewer.getSequence();
        boolean opened = true;

        synchronized (viewers)
        {
            // check if the sequence has just been opened
            if (sequence != null)
            {
                for (Viewer v : viewers)
                {
                    if (v.getSequence() == sequence)
                    {
                        opened = false;
                        break;
                    }
                }
            }

            // add viewer to the viewer list
            viewers.add(viewer);
        }

        // single viewer for this sequence ?
        if ((sequence != null) && opened)
            // send opened event
            sequenceOpened(sequence);

        // fire viewer open event (after sequence open)
        fireViewerOpenedEvent(viewer);
    }

    /**
     * called when viewer activation changed
     */
    private void viewerActivationChanged(Viewer oldActive, Viewer newActive)
    {
        final Sequence sequence;

        // new active viewer is not null ?
        if (newActive != null)
        {
            // remove focus on ImageJ image
            final ImageJWrapper ij = Icy.getMainInterface().getImageJ();
            if (ij != null)
                ij.setActiveImage(null);

            // get active sequence
            sequence = newActive.getSequence();
        }
        else
            sequence = null;

        final Sequence oldActiveSequence = activeSequence;

        // sequence active changed ?
        if (oldActiveSequence != sequence)
        {
            activeSequence = sequence;
            sequenceActivationChanged(oldActiveSequence, sequence);
        }

        // fire deactivated / activated events
        fireViewerDeactivatedEvent(oldActive);
        fireViewerActivatedEvent(newActive);
    }

    /**
     * called when the active viewer changed
     */
    void activeViewerChanged(ViewerEvent event)
    {
        // propagate event if it comes from the active viewer
        // FIXME: why we need to test that ? it should always be the active viewer ?
        if (event.getSource() == activeViewer)
            fireActiveViewerChangedEvent(event);
    }

    /**
     * called when a viewer is closed
     */
    private void viewerClosed(Viewer viewer)
    {
        // retrieve the viewer LUT before we release it
        final LUT lut = viewer.getLut();

        // remove active viewer listener
        if (viewer == activeViewer)
            viewer.removeListener(activeViewerListener);
        // remove viewer from the viewer list
        synchronized (viewers)
        {
            viewers.remove(viewer);
        }
        // fire viewer closed event (before sequence close)
        fireViewerClosedEvent(viewer);

        final Sequence sequence = viewer.getSequence();

        // check if a sequence has been closed
        if (sequence != null)
        {
            // if no viewer for this sequence
            if (getViewers(sequence).isEmpty())
                // sequence close
                sequenceClosed(sequence, lut);
        }
    }

    /**
     * called when a sequence is opened
     */
    private void sequenceOpened(Sequence sequence)
    {
        // add to sequence list
        synchronized (sequences)
        {
            sequences.add(sequence);
        }
        // listen the sequence
        sequence.addListener(sequenceListener);
        // fire sequence opened event (before roi / overlay events)
        fireSequenceOpenedEvent(sequence);

        // check about ROI add event
        for (ROI roi : sequence.getROIs())
            checkRoiAdded(roi, false);
        // check about Overlay add event
        for (Overlay overlay : sequence.getOverlays())
            checkOverlayAdded(overlay, false);
    }

    /**
     * called when sequence activation changed
     */
    private void sequenceActivationChanged(Sequence oldActive, Sequence newActive)
    {
        // fire events
        fireSequenceDeactivatedEvent(oldActive);
        fireSequenceActivatedEvent(newActive);
    }

    /**
     * called when a sequence changed
     */
    void sequenceChanged(SequenceEvent event)
    {
        final Sequence sequence = event.getSequence();

        // handle event for active sequence only
        if (isOpened(sequence))
        {
            switch (event.getSourceType())
            {
                case SEQUENCE_ROI:
                    switch (event.getType())
                    {
                        case ADDED:
                            checkRoiAdded((ROI) event.getSource(), false);
                            break;

                        case REMOVED:
                            checkRoiRemoved((ROI) event.getSource(), false);
                            break;
                    }
                    break;

                case SEQUENCE_OVERLAY:
                    switch (event.getType())
                    {
                        case ADDED:
                            checkOverlayAdded((Overlay) event.getSource(), false);
                            break;

                        case REMOVED:
                            checkOverlayRemoved((Overlay) event.getSource(), false);
                            break;
                    }
                    break;
            }
        }

        // propagate event if it comes from the active sequence
        if (sequence == activeSequence)
            fireActiveSequenceChangedEvent(event);
    }

    /**
     * Called when a sequence is closed.
     * 
     * @param sequence
     *        the sequence which has been closed.
     * @param viewer
     *        the viewer which has been closed.
     */
    private void sequenceClosed(Sequence sequence, LUT userLut)
    {
        // check about Overlay remove event
        for (Overlay overlay : sequence.getOverlays())
            checkOverlayRemoved(overlay, true);
        // check about ROI remove event
        for (ROI roi : sequence.getROIs())
            checkRoiRemoved(roi, true);

        // remove from sequence listener
        sequence.removeListener(sequenceListener);
        // remove from the sequence list
        synchronized (sequences)
        {
            sequences.remove(sequence);
        }

        // set the user LUT in the sequence
        if (userLut != null)
            sequence.setUserLUT(userLut);
        // inform sequence is now closed
        sequence.closed();
        
        // fire sequence closed event (after roi / overlay events)
        fireSequenceClosedEvent(sequence);
    }

    private void checkRoiAdded(ROI roi, boolean checkBeforeAdd)
    {
        final List<Sequence> sequencesContainingRoi = getSequencesContaining(roi);

        // only 1 sequence contains (or will contain) this roi ?
        if (sequencesContainingRoi.size() == (checkBeforeAdd ? 0 : 1))
            // roi added
            roiAdded(roi);
    }

    private void checkRoiRemoved(ROI roi, boolean checkBeforeRemove)
    {
        final List<Sequence> sequencesContainingRoi = getSequencesContaining(roi);

        // no more sequence contains (or will contain) this roi ?
        if (sequencesContainingRoi.size() == (checkBeforeRemove ? 1 : 0))
            // roi removed
            roiRemoved(roi);
    }

    private void checkOverlayAdded(Overlay overlay, boolean checkBeforeAdd)
    {
        final List<Sequence> sequencesContainingOverlay = getSequencesContaining(overlay);

        // only 1 sequence contains (or will contain) this overlay ?
        if (sequencesContainingOverlay.size() == (checkBeforeAdd ? 0 : 1))
            // overlay added
            overlayAdded(overlay);
    }

    private void checkOverlayRemoved(Overlay overlay, boolean checkBeforeRemove)
    {
        final List<Sequence> sequencesContainingOverlay = getSequencesContaining(overlay);

        // no more sequence contains (or will contain) this overlay ?
        if (sequencesContainingOverlay.size() == (checkBeforeRemove ? 1 : 0))
            // overlay removed
            overlayRemoved(overlay);
    }

    /**
     * called when a roi is added for the first time in a sequence
     */
    private void roiAdded(ROI roi)
    {
        fireRoiAddedEvent(roi);
    }

    /**
     * called when a roi is removed from all sequence
     */
    private void roiRemoved(ROI roi)
    {
        fireRoiRemovedEvent(roi);
    }

    /**
     * called when an overlay is added for the first time in a sequence
     */
    private void overlayAdded(Overlay overlay)
    {
        fireOverlayAddedEvent(overlay);
    }

    /**
     * called when an overlay is removed from all sequence
     */
    private void overlayRemoved(Overlay overlay)
    {
        fireOverlayRemovedEvent(overlay);
    }

    @Override
    public void setGlobalViewSyncId(int id)
    {
        for (Viewer viewer : getViewers())
            viewer.setViewSyncId(id);
    }
}
