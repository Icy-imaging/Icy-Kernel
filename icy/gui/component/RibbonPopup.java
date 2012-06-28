package icy.gui.component;

import icy.common.listener.weak.WeakWindowFocusListener;
import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.util.StringUtil;

import java.awt.BorderLayout;
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

/**
 * @author thomasprovoost
 */
public class RibbonPopup extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4951100060144899946L;
	
	/** Used to truncate the strings in the Ribbon popupmenu */
	private static final int MENU_MAX_CHAR = 40;
	
	final WindowFocusListener windowFListener = new WindowFocusListener() {
		
		@Override
		public void windowLostFocus(WindowEvent windowevent) {
			setVisible(false);
		}
		
		@Override
		public void windowGainedFocus(WindowEvent windowevent) {
			
		}
		
	};

	public RibbonPopup(PluginDescriptor plugin) {
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(generatePopupMenuTopPanel(plugin));

		// TODO Add rating button here.

		final Image plugImg = plugin.getImage();
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		southPanel.add(new JLabel(new ImageIcon(plugImg)));
		add(southPanel);		
		
		Icy.getMainInterface().getMainFrame().addWindowFocusListener(new WeakWindowFocusListener(windowFListener));
	}

	private JPanel generatePopupMenuTopPanel(PluginDescriptor plugin) {
		final String name = plugin.getName();
		String description = plugin.getDescription();
		final String website = plugin.getWeb();
		final String author = plugin.getAuthor();
		final ImageIcon plugIcon = plugin.getIcon();
		String jarUrl = plugin.getJarUrl();

		// Truncate the description (make it on one or more lines)
		description = "<html>" + description + "</html>";
		for (int i = 0; i < description.length(); ++i) {
			if (i % MENU_MAX_CHAR == 0 && i != 0) {
				int iSpace = i;
				while (description.charAt(iSpace) != ' ' && iSpace > 0)
					--iSpace;
				if (iSpace == 0)
					break;
				else
					description = description.substring(0, iSpace) + "<br/>" + description.substring(iSpace, description.length());
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
		itemPluginPage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				NetworkUtil.openURL("http://icy.bioimageanalysis.org/index.php?display=detailPlugin&pluginId=" + pluginId);
				setVisible(false);
			}
		});
		panelButtons.add(itemPluginPage);

		JButton itemPluginPageDoc = new JButton("Doc");
		itemPluginPageDoc.setIcon(new IcyIcon(ResourceUtil.ICON_DOC));
		itemPluginPageDoc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				NetworkUtil.openURL("http://icy.bioimageanalysis.org/index.php?display=detailPlugin&pluginId=" + pluginId
						+ "#documentation");
				setVisible(false);
			}
		});
		panelButtons.add(itemPluginPageDoc);

		if (StringUtil.isEmpty(pluginId)) {
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
		center.add(new JLabel(plugIcon), constraint);

		constraint.fill = GridBagConstraints.BOTH;
		constraint.gridx = 1;
		constraint.gridy = 0;
		constraint.gridheight = 1;
		center.add(panelRight, constraint);

		constraint.fill = GridBagConstraints.BOTH;
		constraint.gridx = 1;
		constraint.gridy = 1;
		center.add(panelButtons, constraint);

		JPanel toReturn = new JPanel();
		toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.Y_AXIS));

		// Name of the plugin
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
		northPanel.add(new JLabel("<html><b>" + name + "</b></html>"));

		// add everything to the panel
		toReturn.add(northPanel);
		toReturn.add(center);
		toReturn.add(new JSeparator());

		return toReturn;
	}
	
}
