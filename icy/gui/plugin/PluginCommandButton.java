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
package icy.gui.plugin;

import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandToggleButton;
import icy.gui.frame.IcyFrame;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.abstract_.Plugin;
import icy.resource.icon.BasicResizableIcon;
import icy.system.thread.ThreadUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;

/**
 * Class helper to create plugin command button
 * 
 * @author Stephane
 */
public class PluginCommandButton
{
    /**
     * Set a plugin button with specified action
     */
    public static void setButton(AbstractCommandButton button, final PluginDescriptor plugin, boolean doAction)
    {
        final String name = plugin.getName();
        final String className = plugin.getClassName();
        final ImageIcon plugIcon = plugin.getIcon();
        
        
        // update text & icon
        button.setText(name);
        button.setIcon(new BasicResizableIcon(plugIcon));
        // save class name here
        button.setName(className);

        button.setActionRichTooltip(new PluginRichToolTip(plugin));

        // remove previous listener on button
        final ActionListener[] listeners = button.getListeners(ActionListener.class);
        for (ActionListener listener : listeners)
            button.removeActionListener(listener);

        if (doAction)
        {
            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final AbstractCommandButton button = (AbstractCommandButton) e.getSource();
                    final PluginDescriptor plugin = PluginLoader.getPlugin(button.getName());

                    if (plugin != null)
                        PluginLauncher.start(plugin);
                }
            });
           
            if(button.getClass().equals(IcyCommandButton.class))
            {
	            final IcyCommandButton btn =(IcyCommandButton) button;
	            btn.addMouseListener(new MouseListener(){
					@Override
					public void mouseClicked(MouseEvent arg0) {
						updateSubmenu(btn,plugin);
					}
	
					@Override
					public void mouseEntered(MouseEvent arg0) {
						updateSubmenu(btn,plugin);
					}
					@Override
					public void mouseExited(MouseEvent arg0) {
						btn.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
						
					}
	
					@Override
					public void mousePressed(MouseEvent arg0) {
						
					}
	
					@Override
					public void mouseReleased(MouseEvent arg0) {
						
					}
	            	
	            });
            }
        }
    }
    /**
     * Update the popup menu of current command button
     */
    public static void updateSubmenu(final IcyCommandButton btn, final PluginDescriptor plugin)
    {
    	if(Plugin.openedFramesMap.containsKey(plugin.getClassName()))
		{
	        btn.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
	        btn.setPopupRichTooltip(new PluginRichToolTip(plugin));
	        btn.setPopupCallback(new PopupPanelCallback()
	        {
	            @Override
	            public JPopupPanel getPopupPanel(JCommandButton commandButton)
	            {
	            	final JMenu framesOfPluginMenu = new JMenu("Opened frames");
	            	final JMenuItem newInstanceItem = new JMenuItem("New Instance");
	            	newInstanceItem.addActionListener(new ActionListener(){
            			 @Override
                         public void actionPerformed(ActionEvent e)
                         {
            				 btn.doActionClick();
                         }
            		});
	            	framesOfPluginMenu.add(newInstanceItem);
	            	framesOfPluginMenu.addSeparator();
	            	ArrayList<IcyFrame> fl = Plugin.openedFramesMap.get(plugin.getClassName());
	            	synchronized (fl)
            		{
	            		for(final IcyFrame f : fl){
		            		final JMenuItem frameItem = new JMenuItem(f.getTitle());
		            		frameItem.addActionListener(new ActionListener()
		                    {
		            			 @Override
	                             public void actionPerformed(ActionEvent e)
	                             {
	                                 ThreadUtil.invokeLater(new Runnable()
	                                 {
	                                     @Override
	                                     public void run()
	                                     {
	                                         // remove minimized state
	                                         if (f.isMinimized())
	                                             f.setMinimized(false);
	                                         // then grab focus
	                                         f.requestFocus();
	                                         f.toFront();
	                                     }
	                                 });
	                             }
		                    });
		                    framesOfPluginMenu.add(frameItem);
	            		}
            		}
	            	
	            	framesOfPluginMenu.addSeparator();
	            	// close all menu item
	            	final JMenuItem closeAllItem = new JMenuItem("Close All");
	            	closeAllItem.addActionListener(new ActionListener(){
            			 @Override
                         public void actionPerformed(ActionEvent e)
                         {
            				 if(Plugin.openedFramesMap.containsKey(plugin.getClassName()))
            				 {
            					 synchronized (Plugin.openedFramesMap.get(plugin.getClassName()))
            					 {
            						 for(int i=0;i<Plugin.openedFramesMap.get(plugin.getClassName()).size();i++)
            						 {
	            					 
	            						Plugin.openedFramesMap.get(plugin.getClassName()).get(i).close();
	            					 }
	            				 }
            				 }
                         }
            		});
	            	framesOfPluginMenu.add(closeAllItem);
	            	
	                final JPopupMenu popupMenu = framesOfPluginMenu.getPopupMenu();
	                
	                // FIXME : set as heavy weight component for VTK (doesn't work)
	                // popupMenu.setLightWeightPopupEnabled(false);
	                popupMenu.show(btn, 0, btn.getHeight());
	
	                return null;
	            }
	        });
	       
		}
		else
			btn.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
    }

    /**
     * Set a plugin button with default action
     */
    public static void setButton(AbstractCommandButton button, PluginDescriptor plugin)
    {
        setButton(button, plugin, true);
    }

    /**
     * Build a plugin button
     */
    public static AbstractCommandButton createButton(final PluginDescriptor plugin, boolean toggle, boolean doAction)
    {
        final AbstractCommandButton result;

        // build command button
        if (toggle)
            result = new IcyCommandToggleButton();
        else
        {
	        result = new IcyCommandButton();
        }
        
        setButton(result, plugin, doAction);

        return result;
    }

    /**
     * Build a plugin button with default action (execute plugin)
     */
    public static IcyCommandButton createButton(PluginDescriptor plugin)
    {
        // build with default action listener
        return (IcyCommandButton) createButton(plugin, false, true);
    }

    /**
     * Build a plugin toggle button with default action (execute plugin) if enable.
     */
    public static IcyCommandToggleButton createToggleButton(PluginDescriptor plugin, boolean doAction)
    {
        return (IcyCommandToggleButton) createButton(plugin, true, doAction);
    }
}
