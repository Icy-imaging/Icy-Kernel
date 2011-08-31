/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.swimmingPool;

import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.main.Icy;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

public class SwimmingPoolViewer implements SwimmingPoolListener
{

    IcyFrame mainFrame = new IcyFrame("Swimming Pool Viewer", true, true, true, true);
    JPanel mainPanel = new JPanel();
    JScrollPane mainScrollPane = new JScrollPane(mainPanel);

    public SwimmingPoolViewer()
    {

        mainFrame.getContentPane().setLayout(new BorderLayout());
        mainFrame.getContentPane().add(mainScrollPane, BorderLayout.CENTER);
        mainFrame.setVisible(true);
        mainFrame.setPreferredSize(new Dimension(300, 300));
        mainFrame.addToMainDesktopPane();
        mainFrame.center();
        mainFrame.pack();

        Icy.getMainInterface().getSwimmingPool().addListener(this);

        mainPanel.setPreferredSize(new Dimension(200, 200));
        mainPanel.setLayout(new FlowLayout());

        for (SwimmingObject result : Icy.getMainInterface().getSwimmingPool().getObjects())
        {
            mainPanel.add(new SwimmingPoolElementPanel(result));
            // mainPanel.updateUI();
            mainPanel.revalidate();
        }

        mainFrame.requestFocus();

    }

    @Override
    public void swimmingPoolChangeEvent(final SwimmingPoolEvent swimmingPoolEvent)
    {

    	if (swimmingPoolEvent.getType() == SwimmingPoolEventType.ELEMENT_ADDED)
    	{
    		ThreadUtil.invokeLater( new Runnable() {

    			@Override
    			public void run() {					
    				mainPanel.add(new SwimmingPoolElementPanel(swimmingPoolEvent.getResult()));
    				mainPanel.revalidate();
    			}
    		} );
    	}
    	if (swimmingPoolEvent.getType() == SwimmingPoolEventType.ELEMENT_REMOVED)
    	{
    		ThreadUtil.invokeLater( new Runnable() {

    			@Override
    			public void run() {					
    				for (int i = 0; i < mainPanel.getComponents().length; i++)
    				{
    					SwimmingPoolElementPanel spep = (SwimmingPoolElementPanel) mainPanel.getComponent(i);
    					if (spep.result == swimmingPoolEvent.getResult())
    					{
    						mainPanel.remove(spep);
    						mainFrame.revalidate();
    						// FIXME : why this is needed ?
    						mainFrame.repaint();
    						break;
    					}
    				}
    			}
    		} );

    	}

    }

    class SwimmingPoolElementPanel extends JPanel implements ActionListener
    {
        private static final long serialVersionUID = 8714333090862303222L;
        JButton destroyButton = new JButton("Destroy");
        SwimmingObject result;

        public SwimmingPoolElementPanel(SwimmingObject result)
        {
            this.result = result;
            setBorder(new BevelBorder(BevelBorder.RAISED));
            setPreferredSize(new Dimension(200, 80));
            //add(new JLabel("Sequence: test"));
            try
            {
            	add(new JLabel(result.getObject().toString()));
            }
            catch ( NullPointerException e) {
            	add(new JLabel("Unable to retreive object name."));
            }
            add(GuiUtil.besidesPanel(destroyButton));
            destroyButton.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            if (arg0.getSource() == destroyButton)
            {
                Icy.getMainInterface().getSwimmingPool().remove(result);
            }
        }

    }

}
