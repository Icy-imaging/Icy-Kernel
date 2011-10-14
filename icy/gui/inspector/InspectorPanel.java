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
package icy.gui.inspector;

import icy.gui.component.ExternalizablePanel;
import icy.gui.main.MainAdapter;
import icy.gui.main.MainEvent;
import icy.gui.main.MainListener;
import icy.gui.main.WeakMainListener;
import icy.gui.sequence.SequenceInfosPanel;
import icy.gui.system.MemoryMonitorPanel;
import icy.gui.system.OutputConsolePanel;
import icy.gui.system.OutputConsolePanel.OutputConsoleChangeListener;
import icy.gui.util.WindowPositionSaver;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerAdapter;
import icy.gui.viewer.ViewerEvent;
import icy.gui.viewer.ViewerListener;
import icy.gui.viewer.WeakViewerListener;
import icy.main.Icy;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.lang.ref.WeakReference;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This window shows all details about the current sequence.
 * 
 * @author Fabrice de Chaumont & Stephane
 */
public class InspectorPanel extends ExternalizablePanel
{
    private static final long serialVersionUID = 5538230736731006318L;

    /**
     * GUI
     */
    final SequencePanel sequencePanel;
    // final ViewerPanel viewerPanel;
    final SequenceInfosPanel sequenceInfosPanel;

    /**
     * internals
     */
    private WeakReference<Viewer> internalViewer;
    private final MainListener mainListener;
    private final ViewerListener viewerListener;
    private final WeakViewerListener weakViewerListener;

    /**
     * The width of the inner component of the inspector should not exceed 300.
     */
    public InspectorPanel()
    {
        super("Inspector");

        new WindowPositionSaver(this, "frame/inspector", new Point(600, 140), new Dimension(260, 600));

        internalViewer = new WeakReference<Viewer>(null);

        mainListener = new MainAdapter()
        {
            @Override
            public void viewerFocused(MainEvent event)
            {
                final Viewer viewer = (Viewer) event.getSource();

                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // set viewer
                        setViewer(viewer);
                    }
                });
            }
        };

        viewerListener = new ViewerAdapter()
        {
            @Override
            public void viewerChanged(ViewerEvent event)
            {
                // we receive from current focused viewer only
                switch (event.getType())
                {
                    case CANVAS_CHANGED:
                        // refresh canvas panel
                        updateCanvasPanel(event.getSource());
                        break;

                    case LUT_CHANGED:
                        // refresh lut panel
                        updateLutPanel(event.getSource());
                        break;

                    case POSITION_CHANGED:
                        // nothing to do
                        break;
                }
            }
        };

        // weak reference --> released when InspectorPanel is released
        weakViewerListener = new WeakViewerListener(viewerListener);

        // main TAB panels
        // viewerPanel = new ViewerPanel();
        sequencePanel = new SequencePanel();
        // final JPanel pluginsPanel = new PluginsPanel();
        final JPanel roisPanel = new RoisPanel(true, true);
        final OutputConsolePanel outputPanel = new OutputConsolePanel();
        final LayersPanel layersPanel = new LayersPanel();

        sequenceInfosPanel = new SequenceInfosPanel();
        sequencePanel.setInfosPanel(sequenceInfosPanel);

        updateSequenceInfosPanel(null);
        updateLutPanel(null);
        updateCanvasPanel(null);
        updateSequenceInfosPanel(null);

        final JScrollPane scSequence = new JScrollPane(sequencePanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // TAB panel
        final JTabbedPane mainPane = new JTabbedPane();

        // add main tab panels
        mainPane.addTab("Sequence", null, scSequence, "Sequence informations");
        // mainPane.add("Active Plugin", pluginsPanel);
        mainPane.addTab("Layer", null, layersPanel, "Show all layers details");
        mainPane.addTab("ROI", null, roisPanel, "Manage / edit your ROI");
        mainPane.addTab("Undo", null, new UndoManagerPanel(), "");
        mainPane.addTab("Output", null, outputPanel);

        setMinimumSize(new Dimension(260, 480));
        setLayout(new BorderLayout());

        // add(viewerPanel, BorderLayout.NORTH);
        add(mainPane, BorderLayout.CENTER);
        add(new MemoryMonitorPanel(), BorderLayout.SOUTH);

        validate();
        setVisible(true);

        // get index of output console
        final int outputConsoleTabIndex = mainPane.indexOfComponent(outputPanel);
        final Color defaultBgColor = mainPane.getBackgroundAt(0);

        mainPane.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                // set back default tab color
                if (mainPane.getSelectedIndex() == outputConsoleTabIndex)
                    mainPane.setBackgroundAt(outputConsoleTabIndex, defaultBgColor);
            }
        });

        outputPanel.addOutputConsoleChangeListener(new OutputConsoleChangeListener()
        {
            @Override
            public void outputConsoleChanged(OutputConsolePanel source, boolean isError)
            {
                if (mainPane.getSelectedIndex() != outputConsoleTabIndex)
                {
                    final boolean fIsError = isError;

                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // change output console tab color when new data
                            if (fIsError)
                                mainPane.setBackgroundAt(outputConsoleTabIndex, Color.red);
                            else
                                mainPane.setBackgroundAt(outputConsoleTabIndex, Color.blue);
                        }
                    });
                }
            }
        });

        // add main listener (weak reference --> released when InspectorPanel is released)
        Icy.getMainInterface().addListener(new WeakMainListener(mainListener));
    }

    void updateAll(Viewer value)
    {
        // viewerPanel.setViewer(viewer);
        // refresh lut and canvas panel
        updateLutPanel(value);
        updateCanvasPanel(value);
        // refresh sequence description
        updateSequenceInfosPanel(value);

        revalidate();
        repaint();
    }

    void updateLutPanel(Viewer viewer)
    {
        if (viewer != null)
            sequencePanel.setLutPanel(viewer.getLutPanel());
        else
            sequencePanel.setLutPanel(null);
    }

    void updateCanvasPanel(Viewer viewer)
    {
        if (viewer != null)
            sequencePanel.setCanvasPanel(viewer.getCanvasPanel());
        else
            sequencePanel.setCanvasPanel(null);
    }

    void updateSequenceInfosPanel(Viewer viewer)
    {
        if (viewer != null)
            sequenceInfosPanel.setSequence(viewer.getSequence());
        else
            sequenceInfosPanel.setSequence(null);
    }

    /**
     * @param viewer
     *        the viewer to set
     */
    void setViewer(Viewer value)
    {
        if (internalViewer.get() != value)
        {
            final Viewer previousViewer = internalViewer.get();

            // unregister previous viewer listener if any
            if (previousViewer != null)
                previousViewer.removeListener(weakViewerListener);

            internalViewer = new WeakReference<Viewer>(value);

            updateAll(value);

            // register new listener
            if (value != null)
                value.addListener(weakViewerListener);
        }
    }
}
