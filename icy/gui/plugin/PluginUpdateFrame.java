/**
 * 
 */
package icy.gui.plugin;

import icy.gui.frame.ActionFrame;
import icy.gui.util.GuiUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginUpdater;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author Stephane
 */
public class PluginUpdateFrame extends ActionFrame
{
    JList pluginList;
    private DefaultListModel listModel;

    public PluginUpdateFrame(final ArrayList<PluginDescriptor> toInstallPlugins)
    {
        super("Plugin Update", true);

        setPreferredSize(new Dimension(640, 500));

        final JPanel titlePanel = GuiUtil.createCenteredBoldLabel("Select the plugin(s) to update in the list");
        titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final JTextArea changeLogArea = new JTextArea();
        changeLogArea.setEditable(false);
        final JLabel changeLogTitleLabel = GuiUtil.createBoldLabel("Change log :");

        listModel = new DefaultListModel();
        pluginList = new JList(listModel);
        for (PluginDescriptor plugin : toInstallPlugins)
            listModel.addElement(plugin);

        pluginList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        pluginList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (pluginList.getSelectedValue() != null)
                {
                    final PluginDescriptor plugin = (PluginDescriptor) pluginList.getSelectedValue();

                    final String changeLog = plugin.getChangesLog();

                    if (StringUtil.isEmpty(changeLog))
                        changeLogArea.setText("no change log");
                    else
                        changeLogArea.setText(changeLog);
                    changeLogArea.setCaretPosition(0);
                    changeLogTitleLabel.setText(plugin.getName() + " change log");
                }
            }
        });
        pluginList.setSelectionInterval(0, toInstallPlugins.size() - 1);

        getOkBtn().setText("Update");
        getCancelBtn().setText("Close");
        setCloseAfterAction(false);
        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // launch update
                doUpdate();
            }
        });

        final JScrollPane medScrollPane = new JScrollPane(pluginList);
        final JScrollPane changeLogScrollPane = new JScrollPane(GuiUtil.createTabArea(changeLogArea, 4));
        final JPanel bottomPanel = GuiUtil.createPageBoxPanel(Box.createVerticalStrut(4),
                GuiUtil.createCenteredLabel(changeLogTitleLabel), Box.createVerticalStrut(4), changeLogScrollPane);

        final JPanel mainPanel = getMainPanel();

        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, medScrollPane, bottomPanel);

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        pack();
        addToMainDesktopPane();
        setVisible(true);
        center();
        requestFocus();

        // set splitter to middle
        splitPane.setDividerLocation(0.5d);
    }

    /**
     * update selected plugins
     */
    protected void doUpdate()
    {
        final ArrayList<PluginDescriptor> plugins = new ArrayList<PluginDescriptor>();

        for (Object value : pluginList.getSelectedValues())
            plugins.add((PluginDescriptor) value);

        for (PluginDescriptor plugin : plugins)
            listModel.removeElement(plugin);

        // no more plugin to update ? close frame
        if (listModel.isEmpty())
            close();

        // process plugins update in background
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                if (!plugins.isEmpty())
                    PluginUpdater.updatePlugins(plugins, true);
            }
        });
    }
}
