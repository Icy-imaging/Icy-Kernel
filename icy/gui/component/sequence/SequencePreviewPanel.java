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
package icy.gui.component.sequence;

import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.sequence.SequenceModel;
import icy.sequence.SequenceModel.SequenceModelListener;
import icy.system.thread.ThreadUtil;
import icy.util.GraphicsUtil;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SequencePreviewPanel extends JPanel implements ChangeListener, SequenceModelListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 4985194381532600393L;

    private class CustomPanel extends JPanel implements Runnable
    {
        /**
         * 
         */
        private static final long serialVersionUID = 6307431557815572470L;

        private BufferedImage cache;

        public CustomPanel()
        {
            super();

            cache = null;
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            final int w = getWidth();
            final int h = getHeight();
            final Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22));

            if (cache == null)
                GraphicsUtil.drawCenteredString(g2, "No image", w / 2, h / 2, false);
            else if ((w > 0) && (h > 0))
            {
                final int sw = getSizeX();
                final int sh = getSizeY();
                final int iw = cache.getWidth();
                final int ih = cache.getHeight();
                final int fiw;
                final int fih;

                if (fitToView)
                {
                    final double ratio1 = Math.max((double) iw / (double) sw, (double) ih / (double) sh);
                    final double ratio2 = Math.max((double) sw / (double) w, (double) sh / (double) h);

                    fiw = (int) (iw / (ratio1 * ratio2));
                    fih = (int) (ih / (ratio1 * ratio2));
                }
                else
                {
                    final double ratio = Math.max((double) sw / (double) w, (double) sh / (double) h);

                    fiw = (int) (iw / ratio);
                    fih = (int) (ih / ratio);
                }

                g2.drawImage(cache, (w - fiw) / 2, (h - fih) / 2, fiw, fih, null);
            }

            g2.dispose();
        }

        public void imageChanged()
        {
            // request rebuild cache
            ThreadUtil.runSingle(this);
        }

        int getSizeX()
        {
            if (model != null)
                return model.getSizeX();
            if (cache != null)
                return cache.getWidth();
            return 0;
        }

        int getSizeY()
        {
            if (model != null)
                return model.getSizeY();
            if (cache != null)
                return cache.getHeight();
            return 0;
        }

        @Override
        public void run()
        {
            // rebuild cache and repaint
            final BufferedImage img = getImage();

            if (img instanceof IcyBufferedImage)
                cache = IcyBufferedImageUtil.toBufferedImage((IcyBufferedImage) img, BufferedImage.TYPE_INT_ARGB);
            else
                cache = img;

            repaint();
        }
    }

    protected boolean autoHideSliders;
    protected boolean fitToView;

    protected JSlider tSlider;
    protected JSlider zSlider;
    protected CustomPanel imagePanel;
    protected SequenceModel model;
    protected JLabel titleLabel;
    protected JPanel mainPanel;
    protected JPanel zPanel;
    protected JPanel tPanel;
    protected JLabel lblZ;
    protected JLabel lblT;
    protected JLabel lblZValue;
    protected JLabel lblTValue;

    /**
     * Create the panel.
     */
    public SequencePreviewPanel(String title, boolean autoHideSliders)
    {
        super();

        this.autoHideSliders = autoHideSliders;
        fitToView = true;

        model = null;

        initializeGui();

        if (autoHideSliders)
        {
            zPanel.setVisible(false);
            tPanel.setVisible(false);
        }

        setMaxZ(0);
        setMaxT(0);

        zSlider.addChangeListener(this);
        tSlider.addChangeListener(this);

        setTitle(title);

        // setMinimumSize(new Dimension(320, 200));
        setPreferredSize(new Dimension(280, 200));

        validate();
    }

    /**
     * Create the panel.
     */
    public SequencePreviewPanel(boolean autoHideSliders)
    {
        this(null, autoHideSliders);
    }

    /**
     * Create the panel.
     */
    public SequencePreviewPanel(String title)
    {
        this(title, true);
    }

    /**
     * Create the panel.
     */
    public SequencePreviewPanel()
    {
        this(null, true);
    }

    private void initializeGui()
    {
        setLayout(new BorderLayout(0, 0));

        titleLabel = new JLabel("Title");
        titleLabel.setBorder(new EmptyBorder(2, 0, 4, 0));
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        mainPanel = new JPanel();
        add(mainPanel, BorderLayout.CENTER);
        GridBagLayout gbl_mainPanel = new GridBagLayout();
        gbl_mainPanel.columnWidths = new int[] {0, 0, 0};
        gbl_mainPanel.rowHeights = new int[] {0, 0, 0};
        gbl_mainPanel.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
        gbl_mainPanel.rowWeights = new double[] {1.0, 0.0, Double.MIN_VALUE};
        mainPanel.setLayout(gbl_mainPanel);

        imagePanel = new CustomPanel();
        GridBagConstraints gbc_imagePanel = new GridBagConstraints();
        gbc_imagePanel.insets = new Insets(0, 0, 5, 5);
        gbc_imagePanel.fill = GridBagConstraints.BOTH;
        gbc_imagePanel.gridx = 1;
        gbc_imagePanel.gridy = 0;
        mainPanel.add(imagePanel, gbc_imagePanel);

        zPanel = new JPanel();
        GridBagConstraints gbc_zPanel = new GridBagConstraints();
        gbc_zPanel.insets = new Insets(0, 0, 5, 5);
        gbc_zPanel.fill = GridBagConstraints.BOTH;
        gbc_zPanel.gridx = 0;
        gbc_zPanel.gridy = 0;
        mainPanel.add(zPanel, gbc_zPanel);
        GridBagLayout gbl_zPanel = new GridBagLayout();
        gbl_zPanel.columnWidths = new int[] {0, 0};
        gbl_zPanel.rowHeights = new int[] {0, 0, 0, 0};
        gbl_zPanel.columnWeights = new double[] {0.0, Double.MIN_VALUE};
        gbl_zPanel.rowWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
        zPanel.setLayout(gbl_zPanel);

        lblZ = new JLabel("Z");
        GridBagConstraints gbc_lblZ = new GridBagConstraints();
        gbc_lblZ.fill = GridBagConstraints.BOTH;
        gbc_lblZ.insets = new Insets(0, 0, 5, 0);
        gbc_lblZ.gridx = 0;
        gbc_lblZ.gridy = 0;
        zPanel.add(lblZ, gbc_lblZ);
        lblZ.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblZ.setHorizontalAlignment(SwingConstants.CENTER);

        zSlider = new JSlider(SwingConstants.VERTICAL);
        GridBagConstraints gbc_zSlider = new GridBagConstraints();
        gbc_zSlider.fill = GridBagConstraints.BOTH;
        gbc_zSlider.insets = new Insets(0, 0, 5, 0);
        gbc_zSlider.gridx = 0;
        gbc_zSlider.gridy = 1;
        zPanel.add(zSlider, gbc_zSlider);
        // zSlider.setFocusable(false);

        lblZValue = new JLabel("0");
        GridBagConstraints gbc_lblZValue = new GridBagConstraints();
        gbc_lblZValue.fill = GridBagConstraints.BOTH;
        gbc_lblZValue.gridx = 0;
        gbc_lblZValue.gridy = 2;
        zPanel.add(lblZValue, gbc_lblZValue);
        lblZValue.setHorizontalAlignment(SwingConstants.CENTER);
        lblZValue.setFont(new Font("Tahoma", Font.BOLD, 11));

        tPanel = new JPanel();
        GridBagConstraints gbc_tPanel = new GridBagConstraints();
        gbc_tPanel.fill = GridBagConstraints.BOTH;
        gbc_tPanel.gridx = 1;
        gbc_tPanel.gridy = 1;
        mainPanel.add(tPanel, gbc_tPanel);
        GridBagLayout gbl_tPanel = new GridBagLayout();
        gbl_tPanel.columnWidths = new int[] {0, 0, 0, 0};
        gbl_tPanel.rowHeights = new int[] {0, 0};
        gbl_tPanel.columnWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_tPanel.rowWeights = new double[] {1.0, Double.MIN_VALUE};
        tPanel.setLayout(gbl_tPanel);

        lblTValue = new JLabel("0");
        lblTValue.setMaximumSize(new Dimension(1000, 14));
        lblTValue.setPreferredSize(new Dimension(20, 14));
        lblTValue.setMinimumSize(new Dimension(20, 14));
        GridBagConstraints gbc_lblTValue = new GridBagConstraints();
        gbc_lblTValue.fill = GridBagConstraints.BOTH;
        gbc_lblTValue.insets = new Insets(0, 0, 0, 5);
        gbc_lblTValue.gridx = 0;
        gbc_lblTValue.gridy = 0;
        tPanel.add(lblTValue, gbc_lblTValue);
        lblTValue.setHorizontalAlignment(SwingConstants.CENTER);
        lblTValue.setFont(new Font("Tahoma", Font.BOLD, 11));

        tSlider = new JSlider(SwingConstants.HORIZONTAL);
        GridBagConstraints gbc_tSlider = new GridBagConstraints();
        gbc_tSlider.fill = GridBagConstraints.BOTH;
        gbc_tSlider.insets = new Insets(0, 0, 0, 5);
        gbc_tSlider.gridx = 1;
        gbc_tSlider.gridy = 0;
        tPanel.add(tSlider, gbc_tSlider);
        // tSlider.setFocusable(false);

        lblT = new JLabel("T");
        lblT.setPreferredSize(new Dimension(20, 14));
        lblT.setMaximumSize(new Dimension(1000, 14));
        lblT.setMinimumSize(new Dimension(20, 14));
        GridBagConstraints gbc_lblT = new GridBagConstraints();
        gbc_lblT.fill = GridBagConstraints.BOTH;
        gbc_lblT.gridx = 2;
        gbc_lblT.gridy = 0;
        tPanel.add(lblT, gbc_lblT);
        lblT.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblT.setHorizontalAlignment(SwingConstants.CENTER);

        validate();
    }

    /**
     * @return the main panel
     */
    public JPanel getMainPanel()
    {
        return mainPanel;
    }

    /**
     * @return the zPanel
     */
    public JPanel getZPanel()
    {
        return zPanel;
    }

    /**
     * @return the tPanel
     */
    public JPanel getTPanel()
    {
        return tPanel;
    }

    public boolean getAutoHideSliders()
    {
        return autoHideSliders;
    }

    public void setAutoHideSliders(boolean value)
    {
        if (autoHideSliders != value)
        {
            autoHideSliders = value;
            zPanel.setVisible((zSlider.getMaximum() > 0) && value);
            tPanel.setVisible((tSlider.getMaximum() > 0) && value);
        }
    }

    public void setFitToView(boolean value)
    {
        if (fitToView != value)
        {
            fitToView = value;
            imagePanel.imageChanged();
        }
    }

    public void setPositionZ(int z)
    {
        zSlider.setValue(z);
        imageChanged();
    }

    public void setPositionT(int t)
    {
        tSlider.setValue(t);
        imageChanged();
    }

    private void setMaxZ(int value)
    {
        zSlider.setMaximum(Math.max(0, value));
        if (autoHideSliders)
            zPanel.setVisible(value > 0);
    }

    private void setMaxT(int value)
    {
        tSlider.setMaximum(Math.max(0, value));
        if (autoHideSliders)
            tPanel.setVisible(value > 0);
    }

    /**
     * @return the image provider
     */
    public SequenceModel getModel()
    {
        return model;
    }

    public void setModel(SequenceModel model)
    {
        if (this.model != model)
        {
            if (this.model != null)
                this.model.removeSequenceModelListener(this);

            this.model = model;

            if (model != null)
                model.addSequenceModelListener(this);

            dimensionChanged();
        }
    }

    public void setTitle(String value)
    {
        if (!titleLabel.getText().equals(value))
        {
            titleLabel.setText(value);
            titleLabel.setVisible(!StringUtil.isEmpty(value));
        }
    }

    @Override
    public void dimensionChanged()
    {
        if (model != null)
        {
            setMaxZ(model.getSizeZ() - 1);
            setMaxT(model.getSizeT() - 1);
        }
        else
        {
            setMaxZ(0);
            setMaxT(0);
        }

        imagePanel.imageChanged();
    }

    @Override
    public void imageChanged()
    {
        imagePanel.imageChanged();
    }

    BufferedImage getImage()
    {
        if (model == null)
            return null;

        return model.getImage(tSlider.getValue(), zSlider.getValue());
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        imagePanel.imageChanged();
        lblZValue.setText(Integer.toString(zSlider.getValue()));
        lblTValue.setText(Integer.toString(tSlider.getValue()));
    }
}
