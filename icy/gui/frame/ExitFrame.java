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
package icy.gui.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;

import icy.main.Icy;

public class ExitFrame extends JFrame
{
    /**
     * 
     */
    private static final long serialVersionUID = 5980838429118602985L;

    private JPanel buttonPanel;

    boolean forced;
    Timer timer;
    private JPanel forceQuitPanel;

    /**
     * Create the frame.
     */
    public ExitFrame(int forceDelay)
    {
        super();

        forced = false;
        timer = new Timer(forceDelay, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                displayForcePanel();
            }
        });

        initialize();

        // default
        forceQuitPanel.setVisible(false);
        buttonPanel.setVisible(false);

        if (forceDelay > 0)
            timer.start();
        else if (forceDelay == 0)
            displayForcePanel();

        getRootPane().setWindowDecorationStyle(JRootPane.NONE);

        // pack, center and display
        pack();
        setLocationRelativeTo(Icy.getMainInterface().getMainFrame());
        setAlwaysOnTop(true);
        setVisible(true);
    }

    public ExitFrame()
    {
        this(-1);
    }

    public void displayForcePanel()
    {
        forceQuitPanel.setVisible(true);
        buttonPanel.setVisible(true);

        // pack and center
        pack();
        setLocationRelativeTo(Icy.getMainInterface().getMainFrame());
    }

    public boolean isForced()
    {
        return forced;
    }

    void initialize()
    {
        setTitle("Exit");
        setSize(new Dimension(400, 140));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        getContentPane().add(panel_1, BorderLayout.CENTER);
        panel_1.setLayout(new BorderLayout(0, 0));

        buttonPanel = new JPanel();
        panel_1.add(buttonPanel, BorderLayout.SOUTH);

        JButton button = new JButton("Force Quit");
        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                forced = true;
                // close frame
                dispose();
            }
        });
        button.setFont(new Font("Tahoma", Font.BOLD, 14));
        buttonPanel.add(button);

        JPanel panel = new JPanel();
        panel_1.add(panel, BorderLayout.CENTER);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel panel_2 = new JPanel();
        panel.add(panel_2);

        JLabel label = new JLabel("Please wait while exiting...");
        panel_2.add(label);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));

        forceQuitPanel = new JPanel();
        panel.add(forceQuitPanel);

        JLabel forceQuitLabel = new JLabel("Click on 'Force Quit' to kill remaining tasks and exit.");
        forceQuitPanel.add(forceQuitLabel);
        forceQuitLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        forceQuitLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
}
