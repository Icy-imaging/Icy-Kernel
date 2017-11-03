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

import icy.gui.util.LookAndFeelUtil;
import icy.gui.util.LookAndFeelUtil.WeakSkinChangeListener;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.util.ColorUtil;
import icy.util.ReflectionUtil;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.MenuComponent;
import java.awt.PopupMenu;
import java.lang.reflect.Field;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.pushingpixels.substance.api.skin.SkinChangeListener;

import ij.IJ;
import ij.gui.Toolbar;
import ij.plugin.MacroInstaller;

/**
 * ImageJ ToolBar Wrapper class.
 * 
 * @author Stephane
 */
public class ToolbarWrapper extends Toolbar
{
    private class CustomToolBar extends JToolBar
    {
        /**
         * 
         */
        private static final long serialVersionUID = -3278693015639517146L;

        /**
         * @param orientation
         */
        public CustomToolBar(int orientation)
        {
            super(orientation);

            setBorder(BorderFactory.createEmptyBorder());
            setFloatable(false);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            ToolbarWrapper.this.paint(g);
            
            // try
            // {
            // if (drawButtonsMethod != null)
            // drawButtonsMethod.invoke(ToolbarWrapper.this, g);
            // }
            // catch (Exception e)
            // {
            // IcyExceptionHandler.showErrorMessage(e, false);
            // System.err.println("Cannot redraw toolbar buttons from ImageJ.");
            // }
        }

        @Override
        public void removeNotify()
        {
            try
            {
                // TODO: remove this temporary hack when the "window dispose" bug will be fixed
                // on the OpenJDK / Sun JVM
                if (SystemUtil.isUnix())
                {
                    @SuppressWarnings("rawtypes")
                    Vector pp = (Vector) ReflectionUtil.getFieldObject(this, "popups", true);
                    pp.clear();
                }
            }
            catch (Exception e)
            {
                IcyExceptionHandler.showErrorMessage(e, false);
                System.err.println("Cannot remove toolbar buttons from ImageJ.");
            }

            super.removeNotify();
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 6546185720732862969L;

    final CustomToolBar swingComponent;

    private final SkinChangeListener skinChangeListener;

    /**
     * leaked methods and fields
     */
    // Method drawButtonsMethod;
    Field grayField;
    Field brighterField;
    Field darkerField;
    Field evenDarkerField;
    Field toolColorField;

    // private Color gray = ImageJ.backgroundColor;
    // private Color brighter = gray.brighter();
    // private Color darker = new Color(175, 175, 175);
    // private Color evenDarker = new Color(110, 110, 110);

    public ToolbarWrapper(ImageJWrapper ijw)
    {
        super();

        swingComponent = new CustomToolBar(SwingConstants.HORIZONTAL);

        swingComponent.setMinimumSize(getMinimumSize());
        swingComponent.setPreferredSize(getPreferredSize());
        swingComponent.addKeyListener(ijw);
        swingComponent.addMouseListener(this);
        swingComponent.addMouseMotionListener(this);

        try
        {
            @SuppressWarnings("rawtypes")
            final Vector popups = (Vector) ((Vector) ReflectionUtil.getFieldObject(this, "popups", true)).clone();

            // transfer all popup from original toolbar to the swing toolbar
            for (Object obj : popups)
            {
                final PopupMenu popup = (PopupMenu) obj;

                super.remove(popup);
                swingComponent.add(popup);
            }
            
            // get access to private methods
            // drawButtonsMethod = ReflectionUtil.getMethod(this.getClass(), "drawButtons", true,
            // Graphics.class);
            
            // get access to private fields
            grayField = ReflectionUtil.getField(this.getClass(), "gray", true);
            brighterField = ReflectionUtil.getField(this.getClass(), "brighter", true);
            darkerField = ReflectionUtil.getField(this.getClass(), "darker", true);
            evenDarkerField = ReflectionUtil.getField(this.getClass(), "evenDarker", true);
            toolColorField = ReflectionUtil.getField(this.getClass(), "toolColor", true);
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, false);
            System.err.println("Cannot restore toolbar buttons from ImageJ.");
        }

        skinChangeListener = new SkinChangeListener()
        {
            @Override
            public void skinChanged()
            {
                Color bgCol = LookAndFeelUtil.getBackground(swingComponent);
                Color brighterCol;
                Color darkerCol;
                Color evenDarkerCol;

                if (ColorUtil.getLuminance(bgCol) < 64)
                    bgCol = ColorUtil.mix(bgCol, Color.gray);

                brighterCol = ColorUtil.mix(bgCol, Color.white);
                darkerCol = ColorUtil.mix(bgCol, Color.black);
                evenDarkerCol = ColorUtil.mix(darkerCol, Color.black);

                if (ColorUtil.getLuminance(bgCol) < 128)
                    brighterCol = ColorUtil.mix(bgCol, brighterCol);
                else
                {
                    darkerCol = ColorUtil.mix(bgCol, darkerCol);
                    evenDarkerCol = ColorUtil.mix(darkerCol, evenDarkerCol);
                }

                try
                {
                    if (grayField != null)
                        grayField.set(ToolbarWrapper.this, bgCol);
                    if (brighterField != null)
                        brighterField.set(ToolbarWrapper.this, brighterCol);
                    if (darkerField != null)
                        darkerField.set(ToolbarWrapper.this, darkerCol);
                    if (evenDarkerField != null)
                        evenDarkerField.set(ToolbarWrapper.this, evenDarkerCol);
                    if (toolColorField != null)
                        toolColorField.set(ToolbarWrapper.this, LookAndFeelUtil.getForeground(swingComponent));

                    swingComponent.repaint();
                }
                catch (Exception e)
                {
                    IcyExceptionHandler.showErrorMessage(e, false);
                    System.err.println("Cannot hack background color of ImageJ toolbar.");
                }
            }
        };

        // install default tools and macros
        new MacroInstaller().run(IJ.getDirectory("macros") + "StartupMacros.txt");

        LookAndFeelUtil.addSkinChangeListener(new WeakSkinChangeListener(skinChangeListener));
    }
    
    @Override
    public Container getParent()
    {
        return swingComponent.getParent();
    }

    /**
     * @return the swingComponent
     */
    public JToolBar getSwingComponent()
    {
        return swingComponent;
    }

    @Override
    public synchronized void add(PopupMenu popup)
    {
        if (swingComponent == null)
            super.add(popup);
        else
        {
            swingComponent.add(popup);
            swingComponent.repaint();
        }
    }

    @Override
    public synchronized void remove(MenuComponent popup)
    {
        if (swingComponent == null)
            super.remove(popup);
        else
        {
            swingComponent.remove(popup);
            swingComponent.repaint();
        }
    }

    // @Override
    // public void itemStateChanged(ItemEvent e)
    // {
    // super.itemStateChanged(e);
    //
    // swingComponent.repaint();
    // }
    //
    // @Override
    // public void paint(Graphics g)
    // {
    // swingComponent.repaint();
    // }

    @Override
    public Graphics getGraphics()
    {
        return swingComponent.getGraphics();
    }

    // /**
    // * Rebuild swing toolbar component
    // */
    // protected void rebuild()
    // {
    // swingComponent.removeAll();
    // swingComponent.validate();
    // }
}
