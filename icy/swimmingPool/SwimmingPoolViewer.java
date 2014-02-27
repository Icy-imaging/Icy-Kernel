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
package icy.swimmingPool;

import icy.gui.frame.IcyFrame;
import icy.gui.util.ComponentUtil;
import icy.main.Icy;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JPanel;

public class SwimmingPoolViewer implements SwimmingPoolListener, ActionListener
{

    IcyFrame mainFrame = new IcyFrame("Swimming Pool Viewer", true, true, true, true);

    SwimmingPoolViewerPanel spvp = new SwimmingPoolViewerPanel();

    public SwimmingPoolViewer()
    {

        mainFrame.getContentPane().setLayout(new BorderLayout());
        mainFrame.getContentPane().add(spvp, BorderLayout.CENTER);
        mainFrame.setVisible(true);
        mainFrame.setPreferredSize(new Dimension(400, 400));
        mainFrame.addToDesktopPane();
        mainFrame.center();
        mainFrame.pack();

        Icy.getMainInterface().getSwimmingPool().addListener(this);
        spvp.getDeleteAllButton().addActionListener(this);

        refreshGUI();

        mainFrame.requestFocus();

    }

    private void refreshGUI()
    {
        spvp.getScrollPanel().removeAll();

        for (SwimmingObject result : Icy.getMainInterface().getSwimmingPool().getObjects())
        {
            JPanel panel = new SwimmingPoolObjectPanel(result);
            ComponentUtil.setFixedHeight(panel, 40);
            spvp.getScrollPanel().add(panel);
        }

        spvp.getScrollPanel().add(Box.createVerticalGlue());

        String text = "No object in swimming pool.";

        int numberOfSwimmingObject = Icy.getMainInterface().getSwimmingPool().getObjects().size();
        if (numberOfSwimmingObject > 0)
        {
            text = "" + numberOfSwimmingObject + " objects in swimming pool.";
        }

        spvp.getNumberOfSwimmingObjectLabel().setText(text);

        spvp.getScrollPane().invalidate();
        spvp.getScrollPane().repaint();

    }

    @Override
    public void swimmingPoolChangeEvent(final SwimmingPoolEvent swimmingPoolEvent)
    {

        if (swimmingPoolEvent.getType() == SwimmingPoolEventType.ELEMENT_ADDED)
        {
            ThreadUtil.invokeLater(new Runnable()
            {

                @Override
                public void run()
                {

                    refreshGUI();
                }
            });
        }

        if (swimmingPoolEvent.getType() == SwimmingPoolEventType.ELEMENT_REMOVED)
        {
            ThreadUtil.invokeLater(new Runnable()
            {

                @Override
                public void run()
                {

                    refreshGUI();

                }
            });

        }

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {

        if (e.getSource() == spvp.getDeleteAllButton())
        {
            Icy.getMainInterface().getSwimmingPool().removeAll();
        }

    }

}
