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

import icy.common.EventHierarchicalChecker;
import icy.common.IcyChangedListener;
import icy.common.UpdateEventHandler;
import icy.gui.inspector.InspectorPanel;
import icy.gui.main.MainEvent.MainEventSourceType;
import icy.gui.main.MainEvent.MainEventType;
import icy.gui.menu.ApplicationMenu;
import icy.gui.menu.ToolRibbonTask;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.painter.Painter;
import icy.plugin.abstract_.Plugin;
import icy.preferences.IcyPreferences;
import icy.preferences.XMLPreferences;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;
import icy.swimmingPool.SwimmingPool;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.event.EventListenerList;

/**
 * MainInterfaceGui
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public class MainInterfaceGui implements IcyChangedListener, MainInterface
{
    private final EventListenerList listeners;
    private final UpdateEventHandler updater;
    private final SequenceListener sequenceListener;

    private final ArrayList<Viewer> viewers;
    private final ArrayList<WeakReference<Plugin>> activePlugins;

    private final SwimmingPool swimmingPool;
    private final TaskFrameManager taskFrameManager;

    MainFrame mainFrame;

    Viewer focusedViewer;
    private Sequence focusedSequence;

    /**
     * init() should be called next to the constructor to add internal frames and windows.
     */
    public MainInterfaceGui()
    {
        listeners = new EventListenerList();
        // try to not dispatch on AWT when possible !
        updater = new UpdateEventHandler(this, false);
        viewers = new ArrayList<Viewer>();
        activePlugins = new ArrayList<WeakReference<Plugin>>();
        swimmingPool = new SwimmingPool();
        taskFrameManager = new TaskFrameManager();

        // we have to use a separate sequenceListener object
        // as we already have the sequenceClosed(Sequence seq) method
        sequenceListener = new SequenceListener()
        {
            @Override
            public void sequenceChanged(SequenceEvent sequenceEvent)
            {
                // handle event for active sequence only
                if (isOpened(sequenceEvent.getSequence()))
                {
                    switch (sequenceEvent.getSourceType())
                    {
                        case SEQUENCE_ROI:
                            switch (sequenceEvent.getType())
                            {
                                case ADDED:
                                    checkRoiAdded((ROI) sequenceEvent.getSource());
                                    break;

                                case REMOVED:
                                    checkRoiRemoved((ROI) sequenceEvent.getSource());
                                    break;
                            }
                            break;

                        case SEQUENCE_PAINTER:
                            switch (sequenceEvent.getType())
                            {
                                case ADDED:
                                    checkPainterAdded((Painter) sequenceEvent.getSource());
                                    break;

                                case REMOVED:
                                    checkPainterRemoved((Painter) sequenceEvent.getSource());
                                    break;
                            }
                            break;
                    }
                }
            }

            @Override
            public void sequenceClosed(Sequence sequence)
            {
                // nothing to do here
            }
        };

        mainFrame = null;

        focusedViewer = null;
        focusedSequence = null;
    }

    /**
     * Creates the windows in the Icy.getMainInterface().
     */
    @Override
    public void init()
    {
        // build main frame
        mainFrame = new MainFrame();
        mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                // exit application
                Icy.exit(false, false);
            }
        });
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
        return mainFrame.getInspector();
    }

    @Override
    public ArrayList<Plugin> getActivePlugins()
    {
        final ArrayList<Plugin> result = new ArrayList<Plugin>();

        for (WeakReference<Plugin> ref : activePlugins)
        {
            final Plugin plugin = ref.get();

            if (plugin != null)
                result.add(plugin);
        }

        return result;
    }

    /**
     * @return the current focused viewer
     */
    @Override
    public Viewer getFocusedViewer()
    {
        return focusedViewer;
    }

    /**
     * @return the current focused sequence
     */
    @Override
    public Sequence getFocusedSequence()
    {
        return focusedSequence;
    }

    /**
     * @return the current focused image
     */
    @Override
    public IcyBufferedImage getFocusedImage()
    {
        if (focusedViewer != null)
            return focusedViewer.getCurrentImage();

        return null;
    }

    @Override
    public ArrayList<Viewer> getViewers()
    {
        return new ArrayList<Viewer>(viewers);
    }

    /**
     * @param viewer
     *        viewer which received focus
     */
    @Override
    public void setFocusedViewer(Viewer viewer)
    {
        if (focusedViewer == viewer)
            return;

        if (focusedViewer != null)
        {
            // force previous viewer internal frame to release focus
            try
            {
                focusedViewer.getInternalFrame().setSelected(false);
            }
            catch (PropertyVetoException e)
            {
                // ignore
            }
        }

        focusedViewer = viewer;

        // focus changed
        viewerFocusChanged(viewer);
    }

    /**
     * Add the frame to the Desktop pane and change its layer value to make it over the other
     * internal frames.
     * 
     * @param internalFrame
     */
    @Override
    public void addToDesktopPane(JInternalFrame internalFrame)
    {
        getDesktopPane().add(internalFrame, JLayeredPane.DEFAULT_LAYER);
    }

    @Override
    public IcyDesktopPane getDesktopPane()
    {
        return mainFrame.getDesktopPane();
    }

    @Override
    public ApplicationMenu getApplicationMenu()
    {
        return mainFrame.getApplicationMenu();
    }

    @Override
    public TaskFrameManager getTaskWindowManager()
    {
        return taskFrameManager;
    }

    private WeakReference<Plugin> getPluginReference(Plugin plugin)
    {
        for (WeakReference<Plugin> ref : activePlugins)
            if (ref.get() == plugin)
                return ref;

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
    public void registerPlugin(Plugin plugin)
    {
        activePlugins.add(new WeakReference<Plugin>(plugin));

        // plugin opened
        pluginOpened(plugin);
    }

    @Override
    public void unRegisterPlugin(Plugin plugin)
    {
        final WeakReference<Plugin> ref = getPluginReference(plugin);
        // reference found
        if (ref != null)
            activePlugins.remove(ref);

        // plugin closed
        pluginClosed(plugin);
    }

    @Override
    public void registerViewer(Viewer viewer)
    {
        viewers.add(viewer);

        // viewer opened
        viewerOpened(viewer);
    }

    @Override
    public void unRegisterViewer(Viewer viewer)
    {
        viewers.remove(viewer);

        // viewer closed
        viewerClosed(viewer);

        // no more opened viewer ?
        if (viewers.isEmpty())
        {
            // set focus to null
            setFocusedViewer(null);
        }
        else
            viewers.get(viewers.size() - 1).requestFocus();
    }

    @Override
    public MainFrame getFrame()
    {
        return mainFrame;
    }

    /**
     * Close viewers attached to specified sequence
     */
    @Override
    public void closeViewersOfSequence(Sequence sequence)
    {
        for (Viewer v : getViewers())
            if (v.getSequence() == sequence)
                v.close();
    }

    /**
     * Close all viewers
     */
    @Override
    public void closeAllViewers()
    {
        for (Viewer viewer : getViewers())
            viewer.close();
    }

    /**
     * Return first viewer for the sequence containing specified ROI
     */
    @Override
    public Viewer getFirstViewerContaining(ROI roi)
    {
        return getFirstViewer(getFirstSequenceContaining(roi));
    }

    /**
     * Return first viewer for the sequence containing specified Painter
     */
    @Override
    public Viewer getFirstViewerContaining(Painter painter)
    {
        return getFirstViewer(getFirstSequenceContaining(painter));
    }

    /**
     * Return first viewer attached to specified sequence
     */
    @Override
    public Viewer getFirstViewer(Sequence sequence)
    {
        if (sequence != null)
        {
            for (Viewer viewer : viewers)
                if (viewer.getSequence() == sequence)
                    return viewer;
        }

        return null;
    }

    /**
     * Return viewers attached to specified sequence
     */
    @Override
    public ArrayList<Viewer> getViewers(Sequence sequence)
    {
        final ArrayList<Viewer> result = new ArrayList<Viewer>();

        for (Viewer v : viewers)
            if (v.getSequence() == sequence)
                result.add(v);

        return result;
    }

    /**
     * Return true if specified viewer is the unique viewer for its attached sequence
     */
    @Override
    public boolean isUniqueViewer(Viewer viewer)
    {
        final ArrayList<Viewer> viewers = getViewers(viewer.getSequence());

        return (viewers.size() == 1) && (viewers.get(0) == viewer);
    }

    /**
     * Return the active / focused viewer
     */
    @Override
    public Viewer getActiveViewer()
    {
        for (Viewer v : viewers)
            if (v.isActive())
                return v;

        return null;
    }

    /**
     * Return list of active / opened sequence (displayed in a viewer)
     */
    @Override
    public ArrayList<Sequence> getSequences()
    {
        final ArrayList<Sequence> sequences = new ArrayList<Sequence>();

        for (Viewer viewer : viewers)
        {
            final Sequence seq = viewer.getSequence();

            if (!sequences.contains(seq))
                sequences.add(seq);
        }

        // TODO: add sequences from swimming pool ?

        return sequences;
    }

    /**
     * Return true if specified sequence is currently opened (displayed in a viewer)
     */
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
        final ArrayList<Sequence> sequences = getSequences();

        for (Sequence seq : sequences)
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

    @Override
    public Sequence getFirstSequenceContaining(Painter painter)
    {
        final ArrayList<Sequence> sequences = getSequences();

        for (Sequence seq : sequences)
            if (seq.contains(painter))
                return seq;

        return null;
    }

    /**
     * Return all active sequence containing the specified ROI
     */
    @Override
    public ArrayList<Sequence> getSequencesContaining(ROI roi)
    {
        final ArrayList<Sequence> sequences = getSequences();
        final ArrayList<Sequence> result = new ArrayList<Sequence>();

        for (Sequence seq : sequences)
            if (seq.contains(roi))
                result.add(seq);

        return result;
    }

    /**
     * Return all active sequence containing the specified Painter
     */
    @Override
    public ArrayList<Sequence> getSequencesContaining(Painter painter)
    {
        final ArrayList<Sequence> sequences = getSequences();
        final ArrayList<Sequence> result = new ArrayList<Sequence>();

        for (Sequence seq : sequences)
            if (seq.contains(painter))
                result.add(seq);

        return result;
    }

    /**
     * Return all active ROI
     */
    @Override
    public ArrayList<ROI> getROIs()
    {
        final ArrayList<Sequence> sequences = getSequences();
        final ArrayList<ROI> result = new ArrayList<ROI>();

        for (Sequence seq : sequences)
        {
            final ArrayList<ROI> rois = seq.getROIs();

            for (ROI roi : rois)
                if (!result.contains(roi))
                    result.add(roi);
        }

        // TODO: add ROI from swimming pool ?

        return result;
    }

    /**
     * Return the ROI containing the specified painter (if any)
     */
    @Override
    public ROI getROI(Painter painter)
    {
        final ArrayList<ROI> rois = getROIs();

        for (ROI roi : rois)
            if (roi.getPainter() == painter)
                return roi;

        return null;
    }

    /**
     * Return all active Painter
     */
    @Override
    public ArrayList<Painter> getPainters()
    {
        final ArrayList<Sequence> sequences = getSequences();
        final ArrayList<Painter> result = new ArrayList<Painter>();

        for (Sequence seq : sequences)
        {
            final ArrayList<Painter> painters = seq.getPainters();

            for (Painter painter : painters)
                if (!result.contains(painter))
                    result.add(painter);
        }

        // TODO: add Painter from swimming pool ?

        return result;
    }

    @Override
    public SwimmingPool getSwimmingPool()
    {
        return swimmingPool;
    }

    @Override
    public String getSelectedTool()
    {
        return mainFrame.getMainRibbon().getToolRibbon().getSelected();
    }

    @Override
    public void setSelectedTool(String command)
    {
        mainFrame.getMainRibbon().getToolRibbon().setSelected(command);
    }

    @Override
    public ToolRibbonTask getToolRibbon()
    {
        return mainFrame.getMainRibbon().getToolRibbon();
    }

    @Override
    public boolean isAlwaysOnTop()
    {
        return mainFrame.isAlwaysOnTop();
    }

    @Override
    public void setAlwaysOnTop(boolean value)
    {
        mainFrame.setAlwaysOnTop(value);
    }

    /**
     * Add main listener
     * 
     * @param listener
     */
    @Override
    public void addListener(MainListener listener)
    {
        listeners.add(MainListener.class, listener);
    }

    /**
     * Remove main listener
     * 
     * @param listener
     */
    @Override
    public void removeListener(MainListener listener)
    {
        listeners.remove(MainListener.class, listener);
    }

    /**
     * fire plugin opened event
     */
    private void firePluginOpenedEvent(MainEvent event)
    {
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.pluginOpened(event);
    }

    /**
     * fire plugin closed event
     */
    private void firePluginClosedEvent(MainEvent event)
    {
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.pluginClosed(event);
    }

    /**
     * fire viewer opened event
     */
    private void fireViewerOpenedEvent(MainEvent event)
    {
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.viewerOpened(event);
    }

    /**
     * fire viewer focused event
     */
    private void fireViewerFocusedEvent(MainEvent event)
    {
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.viewerFocused(event);
    }

    /**
     * fire viewer focused event
     */
    private void fireViewerClosedEvent(MainEvent event)
    {
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.viewerClosed(event);
    }

    /**
     * fire sequence opened event
     */
    private void fireSequenceOpenedEvent(MainEvent event)
    {
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.sequenceOpened(event);
    }

    /**
     * fire sequence focused event
     */
    private void fireSequenceFocusedEvent(MainEvent event)
    {
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.sequenceFocused(event);
    }

    /**
     * fire sequence focused event
     */
    private void fireSequenceClosedEvent(MainEvent event)
    {
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.sequenceClosed(event);
    }

    /**
     * fire ROI added event
     */
    private void fireRoiAddedEvent(MainEvent event)
    {
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.roiAdded(event);
    }

    /**
     * fire ROI removed event
     */
    private void fireRoiRemovedEvent(MainEvent event)
    {
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.roiRemoved(event);
    }

    /**
     * fire painter added event
     */
    private void firePainterAddedEvent(MainEvent event)
    {
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.painterAdded(event);
    }

    /**
     * fire painter removed event
     */
    private void firePainterRemovedEvent(MainEvent event)
    {
        for (MainListener listener : listeners.getListeners(MainListener.class))
            listener.painterRemoved(event);
    }

    /**
     * @see icy.common.UpdateEventHandler#beginUpdate()
     */
    @Override
    public void beginUpdate()
    {
        updater.beginUpdate();
    }

    /**
     * @see icy.common.UpdateEventHandler#endUpdate()
     */
    @Override
    public void endUpdate()
    {
        updater.endUpdate();
    }

    /**
     * @see icy.common.UpdateEventHandler#isUpdating()
     */
    @Override
    public boolean isUpdating()
    {
        return updater.isUpdating();
    }

    /**
     * called when a plugin is opened
     */
    private void pluginOpened(Plugin plugin)
    {
        updater.changed(new MainEvent(MainEventSourceType.PLUGIN, MainEventType.OPENED, plugin));
    }

    /**
     * called when a plugin is closed
     */
    private void pluginClosed(Plugin plugin)
    {
        updater.changed(new MainEvent(MainEventSourceType.PLUGIN, MainEventType.CLOSED, plugin));
    }

    /**
     * called when a viewer is opened
     */
    private void viewerOpened(Viewer viewer)
    {
        // check if a sequence has been opened
        final Sequence sequence;

        if (viewer != null)
            sequence = viewer.getSequence();
        else
            sequence = null;

        if (sequence != null)
        {
            // if only 1 viewer for this sequence
            if (getViewers(viewer.getSequence()).size() == 1)
                // sequence opened
                sequenceOpened(sequence);
        }

        updater.changed(new MainEvent(MainEventSourceType.VIEWER, MainEventType.OPENED, viewer));
    }

    /**
     * called when viewer focus changed
     */
    private void viewerFocusChanged(Viewer viewer)
    {
        // check if focused sequence has changed
        final Sequence sequence;

        if (viewer != null)
            sequence = viewer.getSequence();
        else
            sequence = null;

        // sequence focused changed ?
        if (focusedSequence != sequence)
        {
            focusedSequence = sequence;

            // focus changed
            sequenceFocusChanged(sequence);
        }

        updater.changed(new MainEvent(MainEventSourceType.VIEWER, MainEventType.FOCUSED, viewer));
    }

    /**
     * called when a viewer is closed
     */
    private void viewerClosed(Viewer viewer)
    {
        updater.changed(new MainEvent(MainEventSourceType.VIEWER, MainEventType.CLOSED, viewer));

        // check if a sequence has been closed
        final Sequence sequence;

        if (viewer != null)
            sequence = viewer.getSequence();
        else
            sequence = null;

        if (sequence != null)
        {
            // if no viewer for this sequence
            if (getViewers(viewer.getSequence()).size() == 0)
                // sequence close
                sequenceClosed(sequence);
        }
    }

    /**
     * called when a sequence is opened
     */
    private void sequenceOpened(Sequence sequence)
    {
        // listen the sequence
        sequence.addListener(sequenceListener);

        beginUpdate();
        try
        {
            // check if it contains new ROI
            for (ROI roi : sequence.getROIs())
                checkRoiAdded(roi);
            // check if it contains new Painter
            for (Painter painter : sequence.getPainters())
                checkPainterAdded(painter);
        }
        finally
        {
            endUpdate();
        }

        updater.changed(new MainEvent(MainEventSourceType.SEQUENCE, MainEventType.OPENED, sequence));
    }

    /**
     * called when sequence focus changed
     */
    private void sequenceFocusChanged(Sequence sequence)
    {
        updater.changed(new MainEvent(MainEventSourceType.SEQUENCE, MainEventType.FOCUSED, sequence));
    }

    /**
     * called when a sequence is closed
     */
    private void sequenceClosed(Sequence sequence)
    {
        beginUpdate();
        try
        {
            // check if it still contains Painter
            for (Painter painter : sequence.getPainters())
                // the sequence is already removed so the method is ok
                checkPainterRemoved(painter);
            // check if it still contains ROI
            for (ROI roi : sequence.getROIs())
                // the sequence is already removed so the method is ok
                checkRoiRemoved(roi);
        }
        finally
        {
            endUpdate();
        }

        // inform sequence is now closed
        sequence.closed();

        // remove from sequence listener
        sequence.removeListener(sequenceListener);

        updater.changed(new MainEvent(MainEventSourceType.SEQUENCE, MainEventType.CLOSED, sequence));
    }

    void checkRoiAdded(ROI roi)
    {
        // special case of multiple ROI add --> we assume ROI has been added...
        if (roi == null)
            roiAdded(null);
        // if only 1 sequence contains this roi
        else if (getSequencesContaining(roi).size() == 1)
            // roi added
            roiAdded(roi);
    }

    void checkRoiRemoved(ROI roi)
    {
        // special case of multiple ROI remove --> we assume ROI has been removed...
        if (roi == null)
            roiRemoved(null);
        // if no sequence contains this roi
        else if (getSequencesContaining(roi).size() == 0)
            // roi removed
            roiRemoved(roi);
    }

    void checkPainterAdded(Painter painter)
    {
        // special case of multiple Painter add --> we assume Painter has been added...
        if (painter == null)
            painterAdded(null);
        // if only 1 sequence contains this painter
        else if (getSequencesContaining(painter).size() == 1)
            // painter added
            painterAdded(painter);
    }

    void checkPainterRemoved(Painter painter)
    {
        // special case of multiple Painter remove --> we assume Painter has been removed...
        if (painter == null)
            painterRemoved(null);
        // if no sequence contains this painter
        else if (getSequencesContaining(painter).size() == 0)
            // painter removed
            painterRemoved(painter);
    }

    /**
     * called when a roi is added for the first time in a sequence
     */
    private void roiAdded(ROI roi)
    {
        updater.changed(new MainEvent(MainEventSourceType.ROI, MainEventType.ADDED, roi));
    }

    /**
     * called when a roi is removed from all sequence
     */
    private void roiRemoved(ROI roi)
    {
        updater.changed(new MainEvent(MainEventSourceType.ROI, MainEventType.REMOVED, roi));
    }

    /**
     * called when a painter is added for the first time in a sequence
     */
    private void painterAdded(Painter painter)
    {
        updater.changed(new MainEvent(MainEventSourceType.PAINTER, MainEventType.ADDED, painter));
    }

    /**
     * called when a painter is removed from all sequence
     */
    private void painterRemoved(Painter painter)
    {
        updater.changed(new MainEvent(MainEventSourceType.PAINTER, MainEventType.REMOVED, painter));
    }

    @Override
    public void onChanged(EventHierarchicalChecker object)
    {
        final MainEvent event = (MainEvent) object;

        switch (event.getSourceType())
        {
            case PLUGIN:
                switch (event.getType())
                {
                    case OPENED:
                        firePluginOpenedEvent(event);
                        break;

                    case CLOSED:
                        firePluginClosedEvent(event);
                        break;
                }
                break;

            case VIEWER:
                switch (event.getType())
                {
                    case OPENED:
                        fireViewerOpenedEvent(event);
                        break;

                    case FOCUSED:
                        fireViewerFocusedEvent(event);
                        break;

                    case CLOSED:
                        fireViewerClosedEvent(event);
                        break;
                }
                break;

            case SEQUENCE:
                switch (event.getType())
                {
                    case OPENED:
                        fireSequenceOpenedEvent(event);
                        break;

                    case FOCUSED:
                        fireSequenceFocusedEvent(event);
                        break;

                    case CLOSED:
                        fireSequenceClosedEvent(event);
                        break;
                }
                break;

            case ROI:
                switch (event.getType())
                {
                    case ADDED:
                        fireRoiAddedEvent(event);
                        break;

                    case REMOVED:
                        fireRoiRemovedEvent(event);
                        break;
                }
                break;

            case PAINTER:
                switch (event.getType())
                {
                    case ADDED:
                        firePainterAddedEvent(event);
                        break;

                    case REMOVED:
                        firePainterRemovedEvent(event);
                        break;
                }
                break;
        }
    }

}
