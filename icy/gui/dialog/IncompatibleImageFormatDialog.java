/*
 * Copyright 2010-2013 Institut Pasteur.
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
package icy.gui.dialog;

import icy.gui.frame.ActionFrame;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * @author Stephane
 */
public class IncompatibleImageFormatDialog extends ActionFrame
{
    public IncompatibleImageFormatDialog()
    {
        super("Information", true);

        initialize();

        // hide title and cancel button
        setTitleVisible(false);
        getCancelBtn().setVisible(false);

        setSize(600, 240);
        addToMainDesktopPane();
        center();
        setVisible(true);
    }

    private void initialize()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {434, 0};
        gridBagLayout.rowHeights = new int[] {48, 0, 0};
        gridBagLayout.columnWeights = new double[] {1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
        mainPanel.setLayout(gridBagLayout);

        final JLabel lblNewLabel = new JLabel("The selected format is not compatible with your sequence format.");
        lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
        gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        mainPanel.add(lblNewLabel, gbc_lblNewLabel);

        final JLabel lblNewLabel_1 = new JLabel(
                "<html>Convert your sequence to 8 bits RGB or Grayscale.<br/>You can do it in the <i><b>Sequence operation</b></i> tab, <i><b>rendering</b></i> group :<br/><br/>You can also choose a compatible image format as TIFF.");
        lblNewLabel_1.setHorizontalTextPosition(SwingConstants.LEADING);
        lblNewLabel_1.setIconTextGap(22);
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_1.setIcon(new ImageIcon(IncompatibleImageFormatDialog.class
                .getResource("/res/image/app/convertrg.png")));
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.gridx = 0;
        gbc_lblNewLabel_1.gridy = 1;
        mainPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
    }
}
