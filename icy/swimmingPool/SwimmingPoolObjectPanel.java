package icy.swimmingPool;

import icy.main.Icy;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

public class SwimmingPoolObjectPanel extends JPanel {

	
	private SwimmingObject result;
	private JButton deleteButton;
	
	/**
	 * Create the panel.
	 * @param result 
	 */
	public SwimmingPoolObjectPanel(SwimmingObject result) {
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		
		this.result = result;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{200, 0, 100, 0};
		gridBagLayout.rowHeights = new int[]{23, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel dateLabel = new JLabel("New label");
		dateLabel.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_dateLabel = new GridBagConstraints();
		gbc_dateLabel.fill = GridBagConstraints.BOTH;
		gbc_dateLabel.insets = new Insets(0, 0, 0, 5);
		gbc_dateLabel.gridx = 0;
		gbc_dateLabel.gridy = 0;
		add(dateLabel, gbc_dateLabel);
		
		JLabel nameLabel = new JLabel("New label");
		nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
		gbc_descriptionLabel.insets = new Insets(0, 0, 0, 5);
		gbc_descriptionLabel.fill = GridBagConstraints.BOTH;
		gbc_descriptionLabel.gridx = 1;
		gbc_descriptionLabel.gridy = 0;
		add(nameLabel, gbc_descriptionLabel);
		
		deleteButton = new JButton("Delete");
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.BOTH;
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 0;
		add(deleteButton, gbc_btnNewButton);
		
		dateLabel.setText( " " + result.getCreationDate().toString() );
		nameLabel.setText( result.getName() );		
		
		deleteButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				Icy.getMainInterface().getSwimmingPool().remove( SwimmingPoolObjectPanel.this.result );

			}
		});

	}

}
