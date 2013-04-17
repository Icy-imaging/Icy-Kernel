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
package icy.gui.frame;

import icy.gui.component.IcyPanel;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * @author stephane
 */
public class LabelFrame extends IcyFrame
{
    public LabelFrame(String text)
    {
        this("Text", text);
    }

    public LabelFrame(String title, String text)
    {
        super(title, true, true, false, false);

        final JLabel label = new JLabel(text);
        label.setForeground(Color.white);

        final IcyPanel panel = new IcyPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        panel.add(label);

        add(new JScrollPane(panel));

        setMinimumSize(new Dimension(400, 300));
        setPreferredSize(new Dimension(400, 300));

        pack();
        validate();

        setVisible(true);
    }
}
