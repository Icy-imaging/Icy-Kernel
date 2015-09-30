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
package icy.gui.inspector;

import icy.gui.component.ExtTabbedPanel;
import icy.gui.component.ExternalizablePanel;
import icy.gui.main.ActiveSequenceListener;
import icy.gui.main.ActiveViewerListener;
import icy.gui.system.MemoryMonitorPanel;
import icy.gui.system.OutputConsolePanel;
import icy.gui.system.OutputConsolePanel.OutputConsoleChangeListener;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

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
public class InspectorPanel extends ExternalizablePanel implements ActiveViewerListener, ActiveSequenceListener
{
    private static final long serialVersionUID = 5538230736731006318L;

    /**
     * GUI
     */
    final ExtTabbedPanel mainPane;

    final SequencePanel sequencePanel;
    final RoisPanel roisPanel;
    final LayersPanel layersPanel;
    final UndoManagerPanel historyPanel;
    final OutputConsolePanel outputConsolePanel;
    final ChatPanel chatPanel;

    /**
     * The width of the inner component of the inspector should not exceed 300.
     */
    public InspectorPanel()
    {
        super("Inspector", "inspector", new Point(600, 140), new Dimension(300, 600));

        // tab panel
        mainPane = new ExtTabbedPanel();
        mainPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // main panels
        sequencePanel = new SequencePanel();
        // final JPanel pluginsPanel = new PluginsPanel();
        roisPanel = new RoisPanel();
        layersPanel = new LayersPanel();
        historyPanel = new UndoManagerPanel();
        outputConsolePanel = new OutputConsolePanel();
        chatPanel = new ChatPanel();

        // add main tab panels
        mainPane.addTab("Sequence", null, new JScrollPane(sequencePanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
                "Sequence informations");
        // mainPane.add("Active Plugin", pluginsPanel);
        mainPane.addTab("ROI", null, roisPanel, "Manage / edit your ROI");
        mainPane.addTab("Layer", null, layersPanel, "Show all layers details");
        mainPane.addTab("History", null, historyPanel, "Actions history");
        mainPane.addTab("Output", null, outputConsolePanel, "Console output");
        mainPane.addTab("Chat", null, chatPanel, "Chat room");

        // minimum required size for sequence infos panel
        final Dimension minDim = new Dimension(300, 480);
        getFrame().setMinimumSizeInternal(minDim);
        getFrame().setMinimumSizeExternal(minDim);
        setMinimumSize(minDim);
        setLayout(new BorderLayout());

        add(mainPane, BorderLayout.CENTER);
        add(new MemoryMonitorPanel(), BorderLayout.SOUTH);

        validate();
        setVisible(true);

        // get default color of tab background
        final Color defaultBgColor = mainPane.getBackgroundAt(0);

        mainPane.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                final int index = getIndexOfTab(outputConsolePanel);

                // set back default tab color
                if ((index != -1) && (mainPane.getSelectedIndex() == index))
                    mainPane.setBackgroundAt(index, defaultBgColor);
            }
        });

        outputConsolePanel.addOutputConsoleChangeListener(new OutputConsoleChangeListener()
        {
            @Override
            public void outputConsoleChanged(OutputConsolePanel source, boolean isError)
            {
                final int index = getIndexOfTab(outputConsolePanel);

                if ((index != -1) && (mainPane.getSelectedIndex() != index))
                {
                    final boolean fIsError = isError;

                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // change output console tab color when new data
                            if (fIsError)
                                mainPane.setBackgroundAt(index, Color.red);
                            else if (!mainPane.getBackgroundAt(index).equals(Color.red))
                                mainPane.setBackgroundAt(index, Color.blue);
                        }
                    });
                }
            }
        });

        // add focused sequence & viewer listener
        Icy.getMainInterface().addActiveViewerListener(this);
        Icy.getMainInterface().addActiveSequenceListener(this);
    }

    /**
     * @return the mainPane
     */
    public ExtTabbedPanel getMainPane()
    {
        return mainPane;
    }

    /**
     * @return the sequencePanel
     */
    public SequencePanel getSequencePanel()
    {
        return sequencePanel;
    }

    /**
     * @return the roisPanel
     */
    public RoisPanel getRoisPanel()
    {
        return roisPanel;
    }

    /**
     * @return the layersPanel
     */
    public LayersPanel getLayersPanel()
    {
        return layersPanel;
    }

    /**
     * @return the historyPanel
     */
    public UndoManagerPanel getHistoryPanel()
    {
        return historyPanel;
    }

    /**
     * @return the outputConsolePanel
     */
    public OutputConsolePanel getOutputConsolePanel()
    {
        return outputConsolePanel;
    }

    /**
     * @return the chatPanel
     */
    public ChatPanel getChatPanel()
    {
        return chatPanel;
    }

    /**
     * Return the index of specified tab component
     */
    protected int getIndexOfTab(Component component)
    {
        return mainPane.indexOfComponent(component);
    }

    @Override
    public void viewerActivated(Viewer viewer)
    {
        sequencePanel.viewerActivated(viewer);
        layersPanel.viewerActivated(viewer);
    }

    @Override
    public void viewerDeactivated(Viewer viewer)
    {
        // nothing to do here
    }

    /**
     * Called when focused viewer has changed
     */
    @Override
    public void activeViewerChanged(ViewerEvent event)
    {
        sequencePanel.activeViewerChanged(event);
        layersPanel.activeViewerChanged(event);
    }

    @Override
    public void sequenceActivated(Sequence sequence)
    {
        sequencePanel.sequenceActivated(sequence);
        roisPanel.sequenceActivated(sequence);
        historyPanel.sequenceActivated(sequence);
    }

    @Override
    public void sequenceDeactivated(Sequence sequence)
    {
        // nothing to do here
    }

    /**
     * Called by mainInterface when focused sequence has changed
     */
    @Override
    public void activeSequenceChanged(SequenceEvent event)
    {
        sequencePanel.activeSequenceChanged(event);
        roisPanel.activeSequenceChanged(event);
    }
}
