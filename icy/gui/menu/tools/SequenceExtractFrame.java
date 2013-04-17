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
package icy.gui.menu.tools;

import icy.gui.frame.sequence.SequenceActionFrame;
import icy.gui.frame.sequence.SequenceActionFrame.SourceChangeListener;
import icy.gui.util.ComponentUtil;
import icy.image.lut.LUT;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * @author Stephane
 * @deprecated
 */
@Deprecated
public class SequenceExtractFrame extends SequenceActionFrame implements SourceChangeListener, ActionListener
{
    protected static final String SCALAR_CMD = "scalar";
    protected static final String COLOR_CMD = "color";
    protected static final String BAND_CMD = "band";
    protected static final String COLOR_ARGB_CMD = "color_argb";
    protected static final String COLOR_RGB_CMD = "color_rgb";
    protected static final String COLOR_GRAY_CMD = "color_gray";

    public enum ExtractType
    {
        SCALAR, COLOR
    };

    public enum ColorType
    {
        ARGB, RGB, GRAY
    };

    ExtractType extractType;
    final ArrayList<Integer> selectedBands;
    ColorType colorType;

    // GUI
    final JPanel bandSelectPanel;
    final JPanel colorOptionsPanel;

    /**
     * Constructor
     */
    public SequenceExtractFrame()
    {
        super("Extractor", true, false);

        // default
        extractType = ExtractType.SCALAR;
        selectedBands = new ArrayList<Integer>();
        selectedBands.add(Integer.valueOf(0));
        colorType = ColorType.ARGB;

        // GUI
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        final JPanel extractTypePanel = new JPanel();
        extractTypePanel.setBorder(BorderFactory.createTitledBorder("Select extraction type"));
        extractTypePanel.setLayout(new BoxLayout(extractTypePanel, BoxLayout.LINE_AXIS));
        // fix the height of extractType panel
        ComponentUtil.setFixedHeight(extractTypePanel, 56);

        // extraction type
        final JRadioButton scalarRadBtn = new JRadioButton("Scalar data", true);
        final JRadioButton colorRadBtn = new JRadioButton("Color data", false);

        // add button to same group
        final ButtonGroup extractTypeGroup = new ButtonGroup();
        extractTypeGroup.add(scalarRadBtn);
        extractTypeGroup.add(colorRadBtn);

        extractTypePanel.add(Box.createHorizontalStrut(10));
        extractTypePanel.add(scalarRadBtn);
        extractTypePanel.add(Box.createHorizontalStrut(10));
        extractTypePanel.add(colorRadBtn);
        extractTypePanel.add(Box.createHorizontalStrut(10));
        extractTypePanel.add(Box.createHorizontalGlue());

        // band selection
        bandSelectPanel = new JPanel();
        bandSelectPanel.setBorder(BorderFactory.createTitledBorder("Select band to extract"));
        bandSelectPanel.setLayout(new BoxLayout(bandSelectPanel, BoxLayout.LINE_AXIS));
        // fix the height of band select panel
        ComponentUtil.setFixedHeight(bandSelectPanel, 56);

        buildSelectBandPanel();

        // color extract options
        colorOptionsPanel = new JPanel();
        colorOptionsPanel.setBorder(BorderFactory.createTitledBorder("Color extraction type"));
        colorOptionsPanel.setLayout(new BoxLayout(colorOptionsPanel, BoxLayout.LINE_AXIS));
        // fix the height of band select panel
        ComponentUtil.setFixedHeight(colorOptionsPanel, 56);
        // default = hidden
        colorOptionsPanel.setVisible(false);

        // extraction type
        final JRadioButton argbRadBtn = new JRadioButton("ARGB", true);
        final JRadioButton rgbRadBtn = new JRadioButton("RGB", true);
        final JRadioButton grayRadBtn = new JRadioButton("Gray", false);

        // add to same group
        final ButtonGroup colorTypeGroup = new ButtonGroup();
        colorTypeGroup.add(argbRadBtn);
        colorTypeGroup.add(rgbRadBtn);
        colorTypeGroup.add(grayRadBtn);

        colorOptionsPanel.add(Box.createHorizontalStrut(10));
        colorOptionsPanel.add(argbRadBtn);
        colorOptionsPanel.add(Box.createHorizontalStrut(10));
        colorOptionsPanel.add(rgbRadBtn);
        colorOptionsPanel.add(Box.createHorizontalStrut(10));
        colorOptionsPanel.add(grayRadBtn);
        colorOptionsPanel.add(Box.createHorizontalStrut(10));
        colorOptionsPanel.add(Box.createHorizontalGlue());

        mainPanel.add(extractTypePanel);
        mainPanel.add(bandSelectPanel);
        mainPanel.add(colorOptionsPanel);
        mainPanel.add(Box.createVerticalGlue());

        setPreferredSize(new Dimension(400, 380));
        pack();
        setVisible(true);
        addToMainDesktopPane();
        setLocation(50, 50);
        requestFocus();

        // OTHERS
        scalarRadBtn.setActionCommand(SCALAR_CMD);
        colorRadBtn.setActionCommand(COLOR_CMD);
        argbRadBtn.setActionCommand(COLOR_ARGB_CMD);
        rgbRadBtn.setActionCommand(COLOR_RGB_CMD);
        grayRadBtn.setActionCommand(COLOR_GRAY_CMD);
        scalarRadBtn.addActionListener(this);
        colorRadBtn.addActionListener(this);
        argbRadBtn.addActionListener(this);
        rgbRadBtn.addActionListener(this);
        grayRadBtn.addActionListener(this);

        // listen source changes
        addSourceChangeListener(this);

        // define action on validation
        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Sequence seqIn = getSequence();

                // background processing as it can take up sometime
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final Sequence seqOut;

                        // scalar extraction
                        if (extractType == ExtractType.SCALAR)
                        {
                            // whole sequence
                            seqOut = seqIn.extractChannels(selectedBands);
                            // set sequence name
                            seqOut.setName("Band extraction of " + seqIn.getName());
                        }
                        else
                        // color extraction
                        {
                            // get lut of used viewer
                            final LUT lut = Icy.getMainInterface().getFirstViewer(seqIn).getLut();
                            final int imageType = colorTypeToImageType(colorType);

                            // create output sequence
                            seqOut = new Sequence();
                            // set sequence name
                            seqOut.setName("Color extraction of " + seqIn.getName());

                            seqOut.beginUpdate();
                            try
                            {
                                for (int t = 0; t < seqIn.getSizeT(); t++)
                                    for (int z = 0; z < seqIn.getSizeZ(); z++)
                                        seqOut.setImage(t, z,
                                                seqIn.getImage(t, z).convertToBufferedImage(lut, imageType));
                            }
                            finally
                            {
                                seqOut.endUpdate();
                            }
                        }

                        // add sequence
                        Icy.getMainInterface().addSequence(seqOut);
                    }
                });
            }
        });

        updateEnable();
    }

    private void buildSelectBandPanel()
    {
        bandSelectPanel.removeAll();

        final Sequence seq = getSequence();
        if (seq != null)
        {
            for (int i = 0; i < seq.getSizeC(); i++)
            {
                final JCheckBox checkBox = new JCheckBox(" " + i + "  ");
                checkBox.setActionCommand(BAND_CMD + i);
                checkBox.setSelected(selectedBands.contains(Integer.valueOf(i)));
                checkBox.addActionListener(this);
                bandSelectPanel.add(checkBox);
            }
        }
        else
            bandSelectPanel.add(new JLabel("no sequence selected"));

        bandSelectPanel.add(Box.createHorizontalGlue());

        bandSelectPanel.validate();
    }

    /**
     * @return the extractType
     */
    public ExtractType getExtractType()
    {
        return extractType;
    }

    /**
     * @param extractType
     *        the extractType to set
     */
    private void setExtractType(ExtractType value)
    {
        if (extractType != value)
        {
            extractType = value;

            // hide / show depending parameters panel
            bandSelectPanel.setVisible(value == ExtractType.SCALAR);
            colorOptionsPanel.setVisible(value == ExtractType.COLOR);

            mainPanel.validate();
            updateEnable();
        }
    }

    /**
     * @return the colorType
     */
    public ColorType getColorType()
    {
        return colorType;
    }

    /**
     * @param colorType
     *        the colorType to set
     */
    private void setColorType(ColorType value)
    {
        if (colorType != value)
        {
            colorType = value;
        }
    }

    int colorTypeToImageType(ColorType value)
    {
        switch (value)
        {
            default:
                return BufferedImage.TYPE_INT_ARGB;

            case RGB:
                return BufferedImage.TYPE_INT_RGB;

            case GRAY:
                return BufferedImage.TYPE_BYTE_GRAY;
        }
    }

    /**
     * @return the selectedBands
     */
    public ArrayList<Integer> getSelectedBands()
    {
        return selectedBands;
    }

    /**
     * @return the bandSelectPanel
     */
    public JPanel getBandSelectPanel()
    {
        return bandSelectPanel;
    }

    private void changeBandSelect(int band)
    {
        final Integer value = Integer.valueOf(band);

        if (selectedBands.contains(value))
            selectedBands.remove(value);
        else
            selectedBands.add(value);

        updateEnable();
    }

    private void updateEnable()
    {
        final boolean selectedBandsOk = (selectedBands.size() > 0);
        final boolean sourceOk = (getSequence() != null);
        final boolean ok;

        if (extractType == ExtractType.SCALAR)
            ok = selectedBandsOk;
        else
            ok = true;

        // disable action if source is not defined
        getOkBtn().setEnabled(ok && sourceOk);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final String cmd = e.getActionCommand();

        if (SCALAR_CMD.equals(cmd))
            setExtractType(ExtractType.SCALAR);
        else if (COLOR_CMD.equals(cmd))
            setExtractType(ExtractType.COLOR);
        else if (COLOR_ARGB_CMD.equals(cmd))
            setColorType(ColorType.ARGB);
        else if (COLOR_RGB_CMD.equals(cmd))
            setColorType(ColorType.RGB);
        else if (COLOR_GRAY_CMD.equals(cmd))
            setColorType(ColorType.GRAY);
        else if ((cmd != null) && cmd.startsWith(BAND_CMD))
            changeBandSelect(Integer.parseInt(cmd.substring(BAND_CMD.length())));

    }

    @Override
    public void sequenceChanged(Sequence sequence)
    {
        buildSelectBandPanel();
        updateEnable();
    }
}
