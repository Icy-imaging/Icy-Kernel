package plugins.kernel.searchbar.gui;

import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginRepositoryLoader;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXBusyLabel;

import plugins.kernel.searchbar.common.WeakWindowFocusListener;

/**
 * A {@link JPopupMenu} that displays all necessary basic information on a
 * plugin: name, author, icon and image.
 * 
 * @author thomasprovoost
 */
public class PluginPopup extends JWindow
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 4951100060144899946L;

    /** Used to truncate the strings in the Ribbon popupmenu */
    private static final int MENU_MAX_CHAR = 40;

    private static final boolean DEBUG = false;

    final WindowFocusListener windowFListener = new WindowFocusListener()
    {

        @Override
        public void windowLostFocus(WindowEvent windowevent)
        {
            setVisible(false);
        }

        @Override
        public void windowGainedFocus(WindowEvent windowevent)
        {
        }

    };

    private PluginDescriptor plugin;

    public PluginPopup(final PluginDescriptor plugin)
    {
        this.plugin = plugin;
        buildGUI();
        pack();
        setFocusable(false);
        setFocusableWindowState(false);
        Icy.getMainInterface().getMainFrame().addWindowFocusListener(new WeakWindowFocusListener(windowFListener));
    }

    private void buildGUI()
    {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEtchedBorder());
        add(mainPanel);

        JPanel generatedPanel = generatePopupMenuTopPanel(plugin);
        mainPanel.add(generatedPanel);

        if (!StringUtil.isEmpty(plugin.getImageUrl()))
        {
            generatedPanel.add(new JSeparator());
            final Image plugImg = plugin.getImage();
            JPanel southPanel = new JPanel(new BorderLayout());
            southPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            if (plugin.isAllLoaded())
            {
                southPanel.add(new JLabel(new ImageIcon(plugImg)));
            }
            else
            {
                JXBusyLabel busyLabel = new JXBusyLabel(new Dimension(40, 40));
                busyLabel.setBusy(true);
                busyLabel.setHorizontalAlignment(SwingConstants.CENTER);
                southPanel.add(busyLabel);
                ThreadUtil.bgRun(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        while (!plugin.isAllLoaded())
                            ThreadUtil.sleep(200);
                        ThreadUtil.invokeLater(new Runnable()
                        {

                            @Override
                            public void run()
                            {
                                removeAll();
                                buildGUI();
                                pack();
                                repaint();
                            }
                        });
                    }
                });
            }
            mainPanel.add(southPanel);
        }
    }

    private JPanel generatePopupMenuTopPanel(final PluginDescriptor plugin)
    {
        final String name = plugin.getName();
        String description = plugin.getDescription();
        final String website = plugin.getWeb();
        final String author = plugin.getAuthor();
        final ImageIcon plugIcon = plugin.getIcon();
        String jarUrl = plugin.getJarUrl();
        boolean onlineFailed = PluginRepositoryLoader.failed();

        // Truncate the description (make it on one or more lines)
        description = "<html>" + description + "</html>";
        for (int i = 0; i < description.length(); ++i)
        {
            if (i % MENU_MAX_CHAR == 0 && i != 0)
            {
                int iSpace = i;
                while (description.charAt(iSpace) != ' ' && iSpace > 0)
                    --iSpace;
                if (iSpace == 0)
                    break;
                else
                    description = description.substring(0, iSpace) + "<br/>"
                            + description.substring(iSpace, description.length());
            }
        }

        JPanel panelRight = new JPanel();
        panelRight.setLayout(new BoxLayout(panelRight, BoxLayout.Y_AXIS));
        panelRight.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 8));
        panelRight.add(new JLabel(description));
        panelRight.add(StringUtil.isEmpty(website) ? new JLabel(" ") : new JLabel(website));
        panelRight.add(new JLabel(author));

        JPanel panelButtons = new JPanel();
        panelButtons.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        GridLayout layout = new GridLayout(1, 2);
        layout.setHgap(4);
        panelButtons.setLayout(layout);

        final String const_pluginId = "pluginId=";
        final String const_urlBeta = "&beta=";
        int beginIdx = jarUrl.indexOf(const_pluginId) + const_pluginId.length();
        int endIdx = jarUrl.indexOf(const_urlBeta);
        final String pluginId;

        if (jarUrl == null || StringUtil.isEmpty(jarUrl))
            pluginId = "";
        else
            pluginId = jarUrl.substring(beginIdx, endIdx);

        JButton itemPluginPage = new JButton("Webpage");
        itemPluginPage.setIcon(new IcyIcon(ResourceUtil.ICON_WINDOW));
        itemPluginPage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        itemPluginPage.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                NetworkUtil.openURL("http://icy.bioimageanalysis.org/index.php?display=detailPlugin&pluginId="
                        + pluginId);
                setVisible(false);
            }
        });
        panelButtons.add(itemPluginPage);

        JButton itemPluginPageDoc = new JButton("Doc");
        itemPluginPageDoc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        itemPluginPageDoc.setIcon(new IcyIcon(ResourceUtil.ICON_DOC));
        itemPluginPageDoc.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                NetworkUtil.openURL("http://icy.bioimageanalysis.org/index.php?display=detailPlugin&pluginId="
                        + pluginId + "#documentation");
                setVisible(false);
            }
        });
        panelButtons.add(itemPluginPageDoc);

        if (StringUtil.isEmpty(pluginId) || onlineFailed)
        {
            if (DEBUG)
                System.out.println("No plugin ID or no connection.");
            itemPluginPage.setEnabled(false);
            itemPluginPageDoc.setEnabled(false);
        }

        // Draw center panel : image on the left, description + others on the
        // right
        JPanel center = new JPanel();
        center.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        center.setLayout(new GridBagLayout());

        GridBagConstraints constraint = new GridBagConstraints();
        constraint.fill = GridBagConstraints.BOTH;
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.gridheight = 2;

        if (StringUtil.isEmpty(plugin.getIconUrl()))
            center.add(new JLabel());
        else if (!plugin.isAllLoaded())
        {
            JXBusyLabel busyLabel = new JXBusyLabel();
            busyLabel.setBusy(true);
            center.add(busyLabel, constraint);
            ThreadUtil.bgRun(new Runnable()
            {

                @Override
                public void run()
                {
                    while (!plugin.isAllLoaded())
                        ThreadUtil.sleep(200);
                    ThreadUtil.invokeLater(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            removeAll();
                            buildGUI();
                            pack();
                            repaint();
                        }
                    });
                }
            });
        }
        else
            center.add(new JLabel(plugIcon), constraint);

        constraint.fill = GridBagConstraints.BOTH;
        constraint.gridx = 1;
        constraint.gridy = 0;
        constraint.gridheight = 1;
        center.add(panelRight, constraint);

        constraint.fill = GridBagConstraints.BOTH;
        constraint.gridx = 1;
        constraint.gridy = 1;
        // center.add(panelButtons, constraint);

        JPanel toReturn = new JPanel();
        toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.Y_AXIS));

        // Name of the plugin
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
        northPanel.add(new JLabel("<html><b>" + name + "</b></html>"));

        // add everything to the panel
        toReturn.add(northPanel);
        toReturn.add(center);

        return toReturn;
    }

}
