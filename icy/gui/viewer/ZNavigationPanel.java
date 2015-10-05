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
package icy.gui.viewer;

import icy.gui.component.IcySlider;
import icy.gui.util.ComponentUtil;
import icy.gui.util.GuiUtil;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Stephane
 */
public class ZNavigationPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -1839748578015800706L;

    final IcySlider slider;
    final JLabel topLabel;
    final JLabel bottomLabel;

    public ZNavigationPanel()
    {
        super();

        slider = new IcySlider(SwingConstants.VERTICAL);
        slider.setFocusable(false);
        slider.setMaximum(0);
        slider.setMinimum(0);
        slider.setToolTipText("Move cursor to navigate in Z dimension");
        slider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        topLabel.setText(Integer.toString(slider.getMaximum()));
                        bottomLabel.setText(Integer.toString(slider.getValue()));

                        validate();
                    }
                });
            }
        });
        ComponentUtil.setFixedWidth(slider, 22);

        topLabel = new JLabel("000");
        topLabel.setToolTipText("Z sequence size");
        bottomLabel = new JLabel("000");
        bottomLabel.setToolTipText("Z position");

        final JLabel zLabel = GuiUtil.createBoldLabel("Z");

        setBorder(BorderFactory.createTitledBorder("").getBorder());
        setLayout(new BorderLayout());

        add(GuiUtil.createCenteredLabel(topLabel), BorderLayout.NORTH);
        add(slider, BorderLayout.CENTER);
        add(GuiUtil.createPageBoxPanel(GuiUtil.createCenteredLabel(bottomLabel), Box.createVerticalStrut(8),
                GuiUtil.createCenteredLabel(zLabel)), BorderLayout.SOUTH);

        validate();
    }

    /**
     * @see icy.gui.component.IcySlider#setPaintLabels(boolean)
     */
    public void setPaintLabels(boolean b)
    {
        slider.setPaintLabels(b);
    }

    /**
     * @see icy.gui.component.IcySlider#setPaintTicks(boolean)
     */
    public void setPaintTicks(boolean b)
    {
        slider.setPaintTicks(b);
    }

    /**
     * @see javax.swing.JSlider#addChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener(ChangeListener l)
    {
        slider.addChangeListener(l);
    }

    /**
     * @see javax.swing.JSlider#removeChangeListener(javax.swing.event.ChangeListener)
     */
    public void removeChangeListener(ChangeListener l)
    {
        slider.removeChangeListener(l);
    }

    /**
     * Remove all change listener
     */
    public void removeAllChangeListener()
    {
        for (ChangeListener l : slider.getListeners(ChangeListener.class))
            slider.removeChangeListener(l);
    }

    /**
     * @see javax.swing.JSlider#getValue()
     */
    public int getValue()
    {
        return slider.getValue();
    }

    /**
     * @param n
     * @see javax.swing.JSlider#setValue(int)
     */
    public void setValue(int n)
    {
        slider.setValue(n);
    }

    /**
     * @see javax.swing.JSlider#getMinimum()
     */
    public int getMinimum()
    {
        return slider.getMinimum();
    }

    /**
     * @see javax.swing.JSlider#setMinimum(int)
     */
    public void setMinimum(int minimum)
    {
        slider.setMinimum(minimum);
    }

    /**
     * @see javax.swing.JSlider#getMaximum()
     */
    public int getMaximum()
    {
        return slider.getMaximum();
    }

    /**
     * @see javax.swing.JSlider#setMaximum(int)
     */
    public void setMaximum(int maximum)
    {
        slider.setMaximum(maximum);
    }

    /**
     * @see javax.swing.JSlider#getPaintTicks()
     */
    public boolean getPaintTicks()
    {
        return slider.getPaintTicks();
    }

    /**
     * @see javax.swing.JSlider#getPaintTrack()
     */
    public boolean getPaintTrack()
    {
        return slider.getPaintTrack();
    }

    /**
     * @see javax.swing.JSlider#setPaintTrack(boolean)
     */
    public void setPaintTrack(boolean b)
    {
        slider.setPaintTrack(b);
    }

    /**
     * @see javax.swing.JSlider#getPaintLabels()
     */
    public boolean getPaintLabels()
    {
        return slider.getPaintLabels();
    }

}
