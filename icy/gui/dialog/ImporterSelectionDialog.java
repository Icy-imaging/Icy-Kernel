/**
 * 
 */
package icy.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import icy.file.FileUtil;
import icy.main.Icy;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;

/**
 * Dialog to let the user select the appropriate importer to open a file when several importers are
 * available.
 * 
 * @author Stephane
 */
public class ImporterSelectionDialog extends ActionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = -7233417975763216494L;

    JList importerList;
    JLabel pathLabel;

    public ImporterSelectionDialog(List<? extends Object> importers, String path)
    {
        super("Select importer");

        initializeGui();

        pathLabel.setText("  " + FileUtil.getFileName(path));

        importerList.setListData(getItems(importers).toArray());
        if (importers.size() > 0)
            importerList.setSelectedIndex(0);

        importerList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                // double click ?
                if ((e.getClickCount() == 2) && (e.getButton() == MouseEvent.BUTTON1))
                {
                    // have an item selected ? select it !
                    if (importerList.getSelectedIndex() != -1)
                        getOkBtn().doClick();
                }
            }
        });

        setPreferredSize(new Dimension(360, 240));
        pack();
        setLocationRelativeTo(Icy.getMainInterface().getMainFrame());
        setVisible(true);
    }

    private void initializeGui()
    {
        setTitle("Importer selection");

        importerList = new JList();
        importerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(importerList);
        scrollPane.setPreferredSize(new Dimension(320, 80));
        scrollPane.setMinimumSize(new Dimension(320, 80));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        getMainPanel().add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(4, 0, 4, 0));
        getMainPanel().add(panel, BorderLayout.NORTH);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] {0, 0};
        gbl_panel.rowHeights = new int[] {0, 0, 0};
        gbl_panel.columnWeights = new double[] {1.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);

        JLabel newLabel = new JLabel(" Select the importer to open the following file:");
        newLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        GridBagConstraints gbc_newLabel = new GridBagConstraints();
        gbc_newLabel.anchor = GridBagConstraints.WEST;
        gbc_newLabel.insets = new Insets(0, 0, 5, 0);
        gbc_newLabel.gridx = 0;
        gbc_newLabel.gridy = 0;
        panel.add(newLabel, gbc_newLabel);

        pathLabel = new JLabel("  ");
        pathLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
        GridBagConstraints gbc_pathLabel = new GridBagConstraints();
        gbc_pathLabel.anchor = GridBagConstraints.WEST;
        gbc_pathLabel.gridx = 0;
        gbc_pathLabel.gridy = 1;
        panel.add(pathLabel, gbc_pathLabel);
    }

    private List<ImporterPluginItem> getItems(List<? extends Object> importers)
    {
        final List<ImporterPluginItem> result = new ArrayList<ImporterPluginItem>();

        for (Object importer : importers)
        {
            final PluginDescriptor plugin = PluginLoader.getPlugin(importer.getClass().getName());
            if (plugin != null)
                result.add(new ImporterPluginItem(plugin, importer));
        }

        return result;
    }

    public Object getSelectedImporter()
    {
        return ((ImporterPluginItem) importerList.getSelectedValue()).getImporter();
    }

    private class ImporterPluginItem
    {
        final PluginDescriptor plugin;
        final Object importer;

        ImporterPluginItem(PluginDescriptor plugin, Object importer)
        {
            super();

            this.plugin = plugin;
            this.importer = importer;
        }

        public Object getImporter()
        {
            return importer;
        }

        @Override
        public String toString()
        {
            return plugin.toString();
        }
    }
}
