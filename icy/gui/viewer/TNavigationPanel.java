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
package icy.gui.viewer;

import icy.gui.component.IcySlider;
import icy.gui.component.button.IcyButton;
import icy.gui.component.button.IcyToggleButton;
import icy.gui.util.ComponentUtil;
import icy.gui.util.GuiUtil;
import icy.resource.icon.IcyIcon;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Stephane
 */
public class TNavigationPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 9123780562399386045L;

    private static final int DEFAULT_FRAME_RATE = 15;

    final JSlider slider;
    final JLabel leftLabel;
    final JLabel rightLabel;

    final IcyButton play, stop;
    final IcyToggleButton loop;
    final JSpinner frameRate;

    final Timer timer;

    public TNavigationPanel()
    {
        super(true);

        slider = new IcySlider(SwingConstants.HORIZONTAL);
        slider.setFocusable(false);
        slider.setMaximum(0);
        slider.setMinimum(0);
        slider.setToolTipText("Move cursor to navigate in T dimension");
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
                        rightLabel.setText(Integer.toString(slider.getMaximum()));
                        leftLabel.setText(Integer.toString(slider.getValue()));
                        validate();
                    }
                });
            }
        });
        ComponentUtil.setFixedHeight(slider, 22);

        timer = new Timer(1000 / DEFAULT_FRAME_RATE, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final int oldT = getTPosition();

                incTPosition();

                // end reached ?
                if (oldT == getTPosition())
                {
                    // loop mode --> reset
                    if (loop.isSelected())
                        resetTPosition();
                    else
                    {
                        // end play
                        timer.stop();
                        stop.setVisible(false);
                        play.setVisible(true);
                        // and reset position
                        resetTPosition();
                    }
                }
            }
        });

        play = new IcyButton(new IcyIcon("playback_play"));
        play.setFlat(true);
        play.setFocusable(false);
        play.setToolTipText("play");
        play.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                startPlay();
            }
        });

        stop = new IcyButton(new IcyIcon("playback_stop"));
        stop.setFlat(true);
        stop.setFocusable(false);
        stop.setToolTipText("stop");
        stop.setVisible(false);
        stop.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                stopPlay();
            }
        });

        loop = new IcyToggleButton(new IcyIcon("playback_reload", 14));
        loop.setFlat(true);
        loop.setFocusable(false);
        loop.setToolTipText("Enable loop playback");
        loop.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (loop.isSelected())
                    loop.setToolTipText("Disable loop playback");
                else
                    loop.setToolTipText("Enable loop playback");
            }
        });

        frameRate = new JSpinner(new SpinnerNumberModel(DEFAULT_FRAME_RATE, 1, 60, 1));
        frameRate.setFocusable(false);
        // no manual edition and edition focus
        final JTextField tf = ((JSpinner.DefaultEditor) frameRate.getEditor()).getTextField();
        tf.setEditable(false);
        tf.setFocusable(false);
        frameRate.setToolTipText("Change playback frame rate");
        frameRate.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                final int f = ((Integer) frameRate.getValue()).intValue();
                // adjust timer delay
                setTimerDelay(1000 / f);
            }
        });
        ComponentUtil.setFixedSize(frameRate, new Dimension(50, 22));

        leftLabel = new JLabel("0");
        leftLabel.setToolTipText("T position");
        rightLabel = new JLabel("0");
        rightLabel.setToolTipText("T sequence size");

        final JPanel leftPanel = GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(8), GuiUtil.createBoldLabel("T"),
                Box.createHorizontalStrut(10), leftLabel);

        rightLabel.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                // so left label is adjusted to right label size
                ComponentUtil.setFixedWidth(leftLabel, rightLabel.getWidth());
                leftPanel.revalidate();
            }
        });

        final JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.LINE_AXIS));

        final JLabel frameRateLabel = new JLabel("FPS");
        frameRateLabel.setToolTipText("Frames Per Second");

        rightPanel.add(rightLabel);
        rightPanel.add(Box.createHorizontalStrut(12));
        rightPanel.add(play);
        rightPanel.add(stop);
        rightPanel.add(loop);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(frameRate);
        rightPanel.add(Box.createHorizontalStrut(4));
        rightPanel.add(frameRateLabel);
        rightPanel.add(Box.createHorizontalStrut(4));

        // setBorder(BorderFactory.createLineBorder(BorderFactory.createTitledBorder("").getTitleColor()));
        setBorder(BorderFactory.createTitledBorder("").getBorder());
        setLayout(new BorderLayout());

        add(leftPanel, BorderLayout.WEST);
        add(slider, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        validate();
    }

    protected void resetTPosition()
    {
        setTPosition(0);
    }

    protected void incTPosition()
    {
        setTPosition(getTPosition() + 1);
    }

    protected void decTPosition()
    {
        setTPosition(Math.max(0, getTPosition() - 1));
    }

    protected void setTimerDelay(int delay)
    {
        timer.setDelay(delay);
    }

    protected int getTPosition()
    {
        return slider.getValue();
    }

    protected void setTPosition(int t)
    {
        slider.setValue(t);
    }

    /**
     * Returns the frame rate (given in frame per second) for play command.
     */
    public int getFrameRate()
    {
        return ((Integer) frameRate.getValue()).intValue();
    }

    /**
     * Sets the frame rate (given in frame per second) for play command.
     */
    public void setFrameRate(int fps)
    {
        frameRate.setValue(Integer.valueOf(fps));
    }

    /**
     * Returns true if <code>repeat</code> is enabled for play command.
     */
    public boolean isRepeat()
    {
        return loop.isSelected();
    }

    /**
     * Set <code>repeat</code> mode for play command.
     */
    public void setRepeat(boolean value)
    {
        loop.setSelected(value);
    }

    /**
     * Returns true if currently playing.
     */
    public boolean isPlaying()
    {
        return timer.isRunning();
    }

    /**
     * Start sequence play.
     * 
     * @see #stopPlay()
     * @see #setRepeat(boolean)
     */
    public void startPlay()
    {
        timer.start();
        play.setVisible(false);
        stop.setVisible(true);
    }

    /**
     * Stop sequence play.
     * 
     * @see #startPlay()
     */
    public void stopPlay()
    {
        timer.stop();
        stop.setVisible(false);
        play.setVisible(true);
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
