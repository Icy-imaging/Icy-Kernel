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
package icy.imagej;

import icy.gui.main.MainFrame;
import icy.gui.util.SwingUtil;
import icy.main.Icy;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.util.ReflectionUtil;
import ij.ImageJ;
import ij.gui.ImageWindow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuBar;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * ImageJ Wrapper class.
 * 
 * @author Stephane
 */
public class ImageJWrapper extends ImageJ
{
    /**
     * 
     */
    private static final long serialVersionUID = -4361946959228494782L;

    public interface ImageJActiveImageListener
    {
        public void imageActived(ImageWindow iw);

    }

    public final static String PROPERTY_MENU = "menu";

    /**
     * swing GUI
     */
    final JPanel swingPanel;
    JMenuBar swingMenuBar;
    final JPanel swingStatusPanel;
    final JLabel swingStatusLabel;
    final JProgressBar swingProgressBar;
    ToolbarWrapper swingToolBar;

    /**
     * internal
     */
    final ArrayList<ImageJActiveImageListener> listeners;
    MenuBar menuBarSave;

    public ImageJWrapper()
    {
        // silent creation
        super(ImageJ.NO_SHOW);

        swingPanel = new JPanel();
        swingPanel.setLayout(new BorderLayout());

        // menubar
        swingMenuBar = null;
        updateMenu();

        // toolbar
        swingToolBar = new ToolbarWrapper(this);
        swingPanel.add(swingToolBar.getSwingComponent(), BorderLayout.CENTER);
        try
        {
            // patch imageJ toolbar to uses our wrapper
            ReflectionUtil.getField(this.getClass(), "toolbar", true).set(this, swingToolBar);
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, false);
            System.err.println("Cannot install ImageJ toolbar wrapper");
        }

        // status bar
        swingStatusPanel = new JPanel();
        swingStatusPanel.setLayout(new BorderLayout());

        swingStatusLabel = new JLabel();
        swingStatusLabel.setPreferredSize(new Dimension(420, 24));
        swingStatusLabel.setMaximumSize(new Dimension(420, 24));
        swingStatusLabel.setFocusable(true);
        swingStatusLabel.addKeyListener(this);
        swingStatusLabel.addMouseListener(this);
        swingStatusPanel.add(swingStatusLabel, BorderLayout.CENTER);
        swingProgressBar = new JProgressBar(0, 1000);
        swingProgressBar.setBorder(BorderFactory.createEmptyBorder());
        swingProgressBar.setFocusable(true);
        swingProgressBar.setPreferredSize(new Dimension(120, 20));
        swingProgressBar.setMaximumSize(new Dimension(120, 20));
        swingProgressBar.setValue(1000);
        swingProgressBar.addKeyListener(this);
        swingProgressBar.addMouseListener(this);
        swingStatusPanel.add(swingProgressBar, BorderLayout.EAST);

        swingPanel.add(swingStatusPanel, BorderLayout.SOUTH);

        swingPanel.validate();

        listeners = new ArrayList<ImageJWrapper.ImageJActiveImageListener>();
    }

    @Override
    public void removeNotify()
    {
        // TODO: remove this temporary hack when the "window dispose" bug will be fixed
        // on the OpenJDK / Sun JVM
        if (SystemUtil.isUnix())
            swingPanel.remove(swingToolBar);

        super.removeNotify();
    }

    void updateMenu()
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (swingPanel != null)
                {
                    if (swingMenuBar != null)
                        swingPanel.remove(swingMenuBar);

                    // update menu
                    swingMenuBar = SwingUtil.getJMenuBar(menuBarSave, true);

                    swingPanel.add(swingMenuBar, BorderLayout.NORTH);
                    swingPanel.validate();
                }
            }
        });
    }

    public void setActiveImage(ImageWindow iw)
    {
        // notify active image changed
        fireActiveImageChanged(iw);
    }

    public void showSwingProgress(final int currentIndex, final int finalIndex)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                final BoundedRangeModel model = swingProgressBar.getModel();

                model.setMaximum(finalIndex);
                model.setValue(currentIndex);
            }
        });
    }

    public void showSwingStatus(String s)
    {
        swingStatusLabel.setText(s);
    }

    /**
     * @return the Swing menuBar
     */
    public JMenuBar getSwingMenuBar()
    {
        return swingMenuBar;
    }

    /**
     * @return the swingPanel
     */
    public JPanel getSwingPanel()
    {
        return swingPanel;
    }

    /**
     * @return the Swing statusPanel
     */
    public JPanel getSwingStatusPanel()
    {
        return swingStatusPanel;
    }

    /**
     * @return the Swing statusLabel
     */
    public JLabel getSwingStatusLabel()
    {
        return swingStatusLabel;
    }

    /**
     * @return the Swing progressBar
     */
    public JProgressBar getSwingProgressBar()
    {
        return swingProgressBar;
    }

    /**
     * ICY integration
     */
    public void menuChanged()
    {
        // rebuild menu
        updateMenu();
    }

    /**
     * ICY integration
     */
    @Override
    public Point getLocationOnScreen()
    {
        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

        if (mainFrame != null)
            return mainFrame.getLocationOnScreen();

        return new Point(0, 0);
    }

    @Override
    public void setMenuBar(MenuBar mb)
    {
        menuBarSave = mb;
        menuChanged();
    }

    @Override
    public Image createImage(int width, int height)
    {
        return swingPanel.createImage(width, height);
    }

    public void addActiveImageListener(ImageJActiveImageListener listener)
    {
        listeners.add(listener);
    }

    public void removeActiveImageListener(ImageJActiveImageListener listener)
    {
        listeners.remove(listener);
    }

    public void fireActiveImageChanged(ImageWindow iw)
    {
        for (ImageJActiveImageListener listener : listeners)
            listener.imageActived(iw);
    }
}
