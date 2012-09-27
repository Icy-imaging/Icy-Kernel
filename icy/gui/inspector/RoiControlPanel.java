/**
 * 
 */
package icy.gui.inspector;

import icy.gui.component.button.ColorChooserButton;
import icy.gui.component.button.IcyButton;
import icy.resource.icon.IcyIcon;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Stephane
 */
public class RoiControlPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 7403770406075917063L;
    private JTextField textField;
    private JTextField textField_1;
    private JTextField textField_2;
    private JTextField textField_3;
    private JTextField textField_4;
    private JTextField textField_5;
    private JTextField textField_6;
    private JTextField textField_7;
    private JTextField textField_8;
    private JTextField textField_9;
    private JTextField textField_10;

    public RoiControlPanel()
    {
        super();

        initialize();
    }

    private void initialize()
    {
        JPanel panel = new JPanel();

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 20, 0, 20, 0, 20, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gridBagLayout);

        final JLabel lblNewLabel = new JLabel("Name");
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        panel.add(lblNewLabel, gbc_lblNewLabel);

        textField = new JTextField();
        GridBagConstraints gbc_textField = new GridBagConstraints();
        gbc_textField.gridwidth = 3;
        gbc_textField.insets = new Insets(0, 0, 5, 5);
        gbc_textField.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField.gridx = 2;
        gbc_textField.gridy = 0;
        panel.add(textField, gbc_textField);
        textField.setColumns(10);

        final ColorChooserButton colorChooserButton = new ColorChooserButton();
        GridBagConstraints gbc_colorChooserButton = new GridBagConstraints();
        gbc_colorChooserButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_colorChooserButton.insets = new Insets(0, 0, 5, 5);
        gbc_colorChooserButton.gridx = 6;
        gbc_colorChooserButton.gridy = 0;
        panel.add(colorChooserButton, gbc_colorChooserButton);

        final IcyButton icyButton = new IcyButton((IcyIcon) null);
        GridBagConstraints gbc_icyButton = new GridBagConstraints();
        gbc_icyButton.insets = new Insets(0, 0, 5, 0);
        gbc_icyButton.gridx = 8;
        gbc_icyButton.gridy = 0;
        panel.add(icyButton, gbc_icyButton);

        final JLabel lblPosition = new JLabel("Position");
        GridBagConstraints gbc_lblPosition = new GridBagConstraints();
        gbc_lblPosition.anchor = GridBagConstraints.EAST;
        gbc_lblPosition.insets = new Insets(0, 0, 5, 5);
        gbc_lblPosition.gridx = 0;
        gbc_lblPosition.gridy = 1;
        panel.add(lblPosition, gbc_lblPosition);

        final JLabel lblX = new JLabel("X");
        GridBagConstraints gbc_lblX = new GridBagConstraints();
        gbc_lblX.insets = new Insets(0, 0, 5, 5);
        gbc_lblX.anchor = GridBagConstraints.EAST;
        gbc_lblX.gridx = 1;
        gbc_lblX.gridy = 1;
        panel.add(lblX, gbc_lblX);

        textField_1 = new JTextField();
        GridBagConstraints gbc_textField_1 = new GridBagConstraints();
        gbc_textField_1.insets = new Insets(0, 0, 5, 5);
        gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_1.gridx = 2;
        gbc_textField_1.gridy = 1;
        panel.add(textField_1, gbc_textField_1);
        textField_1.setColumns(10);

        final JLabel lblY = new JLabel("Y");
        GridBagConstraints gbc_lblY = new GridBagConstraints();
        gbc_lblY.anchor = GridBagConstraints.EAST;
        gbc_lblY.insets = new Insets(0, 0, 5, 5);
        gbc_lblY.gridx = 3;
        gbc_lblY.gridy = 1;
        panel.add(lblY, gbc_lblY);

        textField_2 = new JTextField();
        GridBagConstraints gbc_textField_2 = new GridBagConstraints();
        gbc_textField_2.insets = new Insets(0, 0, 5, 5);
        gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_2.gridx = 4;
        gbc_textField_2.gridy = 1;
        panel.add(textField_2, gbc_textField_2);
        textField_2.setColumns(10);
        
        final JLabel lblZ = new JLabel("Z");
        GridBagConstraints gbc_lblZ = new GridBagConstraints();
        gbc_lblZ.anchor = GridBagConstraints.EAST;
        gbc_lblZ.insets = new Insets(0, 0, 5, 5);
        gbc_lblZ.gridx = 1;
        gbc_lblZ.gridy = 2;
        panel.add(lblZ, gbc_lblZ);
        
        textField_5 = new JTextField();
        GridBagConstraints gbc_textField_5 = new GridBagConstraints();
        gbc_textField_5.insets = new Insets(0, 0, 5, 5);
        gbc_textField_5.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_5.gridx = 2;
        gbc_textField_5.gridy = 2;
        panel.add(textField_5, gbc_textField_5);
        textField_5.setColumns(10);
        
        final JLabel lblT = new JLabel("T");
        GridBagConstraints gbc_lblT = new GridBagConstraints();
        gbc_lblT.anchor = GridBagConstraints.EAST;
        gbc_lblT.insets = new Insets(0, 0, 5, 5);
        gbc_lblT.gridx = 3;
        gbc_lblT.gridy = 2;
        panel.add(lblT, gbc_lblT);
        
        textField_6 = new JTextField();
        GridBagConstraints gbc_textField_6 = new GridBagConstraints();
        gbc_textField_6.insets = new Insets(0, 0, 5, 5);
        gbc_textField_6.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_6.gridx = 4;
        gbc_textField_6.gridy = 2;
        panel.add(textField_6, gbc_textField_6);
        textField_6.setColumns(10);
        
        final JLabel lblC = new JLabel("C");
        GridBagConstraints gbc_lblC = new GridBagConstraints();
        gbc_lblC.anchor = GridBagConstraints.EAST;
        gbc_lblC.insets = new Insets(0, 0, 5, 5);
        gbc_lblC.gridx = 5;
        gbc_lblC.gridy = 2;
        panel.add(lblC, gbc_lblC);
        
        textField_9 = new JTextField();
        GridBagConstraints gbc_textField_9 = new GridBagConstraints();
        gbc_textField_9.gridwidth = 2;
        gbc_textField_9.insets = new Insets(0, 0, 5, 5);
        gbc_textField_9.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_9.gridx = 6;
        gbc_textField_9.gridy = 2;
        panel.add(textField_9, gbc_textField_9);
        textField_9.setColumns(10);

        final JLabel lblSize = new JLabel("Size");
        GridBagConstraints gbc_lblSize = new GridBagConstraints();
        gbc_lblSize.anchor = GridBagConstraints.EAST;
        gbc_lblSize.insets = new Insets(0, 0, 5, 5);
        gbc_lblSize.gridx = 0;
        gbc_lblSize.gridy = 3;
        panel.add(lblSize, gbc_lblSize);

        final JLabel lblX_1 = new JLabel("X");
        GridBagConstraints gbc_lblX_1 = new GridBagConstraints();
        gbc_lblX_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblX_1.anchor = GridBagConstraints.EAST;
        gbc_lblX_1.gridx = 1;
        gbc_lblX_1.gridy = 3;
        panel.add(lblX_1, gbc_lblX_1);

        textField_3 = new JTextField();
        GridBagConstraints gbc_textField_3 = new GridBagConstraints();
        gbc_textField_3.insets = new Insets(0, 0, 5, 5);
        gbc_textField_3.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_3.gridx = 2;
        gbc_textField_3.gridy = 3;
        panel.add(textField_3, gbc_textField_3);
        textField_3.setColumns(10);

        final JLabel lblY_1 = new JLabel("Y");
        GridBagConstraints gbc_lblY_1 = new GridBagConstraints();
        gbc_lblY_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblY_1.anchor = GridBagConstraints.EAST;
        gbc_lblY_1.gridx = 3;
        gbc_lblY_1.gridy = 3;
        panel.add(lblY_1, gbc_lblY_1);

        textField_4 = new JTextField();
        GridBagConstraints gbc_textField_4 = new GridBagConstraints();
        gbc_textField_4.insets = new Insets(0, 0, 5, 5);
        gbc_textField_4.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_4.gridx = 4;
        gbc_textField_4.gridy = 3;
        panel.add(textField_4, gbc_textField_4);
        textField_4.setColumns(10);

        setLayout(new BorderLayout());

        add(panel, BorderLayout.CENTER);
        
        final JLabel lblZ_1 = new JLabel("Z");
        GridBagConstraints gbc_lblZ_1 = new GridBagConstraints();
        gbc_lblZ_1.anchor = GridBagConstraints.EAST;
        gbc_lblZ_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblZ_1.gridx = 1;
        gbc_lblZ_1.gridy = 4;
        panel.add(lblZ_1, gbc_lblZ_1);
        
        textField_7 = new JTextField();
        GridBagConstraints gbc_textField_7 = new GridBagConstraints();
        gbc_textField_7.insets = new Insets(0, 0, 0, 5);
        gbc_textField_7.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_7.gridx = 2;
        gbc_textField_7.gridy = 4;
        panel.add(textField_7, gbc_textField_7);
        textField_7.setColumns(10);
        
        final JLabel lblT_1 = new JLabel("T");
        GridBagConstraints gbc_lblT_1 = new GridBagConstraints();
        gbc_lblT_1.anchor = GridBagConstraints.EAST;
        gbc_lblT_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblT_1.gridx = 3;
        gbc_lblT_1.gridy = 4;
        panel.add(lblT_1, gbc_lblT_1);
        
        textField_8 = new JTextField();
        GridBagConstraints gbc_textField_8 = new GridBagConstraints();
        gbc_textField_8.insets = new Insets(0, 0, 0, 5);
        gbc_textField_8.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_8.gridx = 4;
        gbc_textField_8.gridy = 4;
        panel.add(textField_8, gbc_textField_8);
        textField_8.setColumns(10);
        
        final JLabel lblC_1 = new JLabel("C");
        GridBagConstraints gbc_lblC_1 = new GridBagConstraints();
        gbc_lblC_1.anchor = GridBagConstraints.EAST;
        gbc_lblC_1.insets = new Insets(0, 0, 0, 5);
        gbc_lblC_1.gridx = 5;
        gbc_lblC_1.gridy = 4;
        panel.add(lblC_1, gbc_lblC_1);
        
        textField_10 = new JTextField();
        GridBagConstraints gbc_textField_10 = new GridBagConstraints();
        gbc_textField_10.gridwidth = 2;
        gbc_textField_10.insets = new Insets(0, 0, 0, 5);
        gbc_textField_10.fill = GridBagConstraints.HORIZONTAL;
        gbc_textField_10.gridx = 6;
        gbc_textField_10.gridy = 4;
        panel.add(textField_10, gbc_textField_10);
        textField_10.setColumns(10);

    }

}
