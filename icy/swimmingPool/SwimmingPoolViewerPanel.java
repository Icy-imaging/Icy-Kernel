package icy.swimmingPool;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

public class SwimmingPoolViewerPanel extends JPanel {
	
	private JButton deleteAllButton;
	private JPanel scrollPanel;
	private JScrollPane scrollPane;
	private JLabel numberOfSwimmingObjectLabel;
	private JPanel panel_1;
	private JLabel lblDate;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	public JPanel getScrollPanel() {
		return scrollPanel;
	}

	public JButton getDeleteAllButton() {
		return deleteAllButton;
	}
	
	public JLabel getNumberOfSwimmingObjectLabel() {
		return numberOfSwimmingObjectLabel;
	}
	
	/**
	 * Create the panel.
	 */
	public SwimmingPoolViewerPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{482, 0};
		gridBagLayout.rowHeights = new int[]{40, 40, 1, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{1, 100, 0};
		gbl_panel.rowHeights = new int[]{40, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		numberOfSwimmingObjectLabel = new JLabel("Swimming pool");
		numberOfSwimmingObjectLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_numberOfSwimmingObjectLabel = new GridBagConstraints();
		gbc_numberOfSwimmingObjectLabel.fill = GridBagConstraints.BOTH;
		gbc_numberOfSwimmingObjectLabel.insets = new Insets(0, 0, 0, 5);
		gbc_numberOfSwimmingObjectLabel.gridx = 0;
		gbc_numberOfSwimmingObjectLabel.gridy = 0;
		panel.add(numberOfSwimmingObjectLabel, gbc_numberOfSwimmingObjectLabel);
		
		deleteAllButton = new JButton("delete all");
		deleteAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		GridBagConstraints gbc_deleteAllButton = new GridBagConstraints();
		gbc_deleteAllButton.fill = GridBagConstraints.BOTH;
		gbc_deleteAllButton.gridx = 1;
		gbc_deleteAllButton.gridy = 0;
		panel.add(deleteAllButton, gbc_deleteAllButton);
		
		panel_1 = new JPanel();
		panel_1.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{200, 0, 100, 0};
		gbl_panel_1.rowHeights = new int[]{10, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		lblNewLabel_1 = new JLabel("Creation date");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		lblNewLabel = new JLabel("Name");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		lblDate = new JLabel("Action");
		lblDate.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblDate = new GridBagConstraints();
		gbc_lblDate.fill = GridBagConstraints.BOTH;
		gbc_lblDate.gridx = 2;
		gbc_lblDate.gridy = 0;
		panel_1.add(lblDate, gbc_lblDate);
		
		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		add(scrollPane, gbc_scrollPane);
		
		scrollPanel = new JPanel();
		scrollPane.setViewportView(scrollPanel);
		scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.PAGE_AXIS));

	}

}
