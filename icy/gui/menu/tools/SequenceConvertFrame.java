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
package icy.gui.menu.tools;

import icy.gui.frame.sequence.SequenceActionFrame;
import icy.gui.frame.sequence.SequenceActionFrame.SourceChangeListener;
import icy.gui.util.ComponentUtil;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.TypeUtil;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Stephane
 * @deprecated
 */
@Deprecated
public class SequenceConvertFrame extends SequenceActionFrame implements SourceChangeListener, ActionListener
{
    protected static final String DATATYPE_CMD = "datatype";
    protected static final String SIGNED_CMD = "signed";
    protected static final String SCALE_CMD = "scale";

    int dataType;
    boolean signed;
    boolean scaled;

    private final JLabel seqDataTypeLabel;
    private final JCheckBox signedCheck;

    private boolean saveSigned;

    /**
     * Constructor
     */
    public SequenceConvertFrame()
    {
        super("Converter", true, false);

        // default
        dataType = TypeUtil.TYPE_BYTE;
        signed = false;
        scaled = true;

        // GUI
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        final JPanel dataTypePanel = new JPanel();
        dataTypePanel.setBorder(BorderFactory.createTitledBorder("Select data type"));
        dataTypePanel.setLayout(new BoxLayout(dataTypePanel, BoxLayout.PAGE_AXIS));
        // fix the height of dataType panel
        ComponentUtil.setFixedHeight(dataTypePanel, 84);

        // current type
        final JPanel currentTypePanel = new JPanel();
        currentTypePanel.setLayout(new BoxLayout(currentTypePanel, BoxLayout.LINE_AXIS));

        seqDataTypeLabel = new JLabel();
        updateSequenceDataTypeLabel();

        currentTypePanel.add(Box.createHorizontalStrut(10));
        currentTypePanel.add(seqDataTypeLabel);
        currentTypePanel.add(Box.createHorizontalGlue());

        // new type
        final JPanel newTypePanel = new JPanel();
        newTypePanel.setLayout(new BoxLayout(newTypePanel, BoxLayout.LINE_AXIS));

        final JLabel dataTypeLabel = new JLabel("New data type  ");
        final JComboBox dataTypeCombo = new JComboBox(TypeUtil.getItems(true, false));
        signedCheck = new JCheckBox("Signed", signed);

        newTypePanel.add(Box.createHorizontalStrut(10));
        newTypePanel.add(dataTypeLabel);
        newTypePanel.add(dataTypeCombo);
        newTypePanel.add(Box.createHorizontalStrut(10));
        newTypePanel.add(signedCheck);
        newTypePanel.add(Box.createHorizontalStrut(10));

        dataTypePanel.add(currentTypePanel);
        dataTypePanel.add(Box.createVerticalStrut(6));
        dataTypePanel.add(newTypePanel);

        // misc options
        final JPanel optionsPanel = new JPanel();
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Select options"));
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.LINE_AXIS));
        // fix the height of options panel
        ComponentUtil.setFixedHeight(optionsPanel, 54);

        final JCheckBox scaleCheck = new JCheckBox("Scale data value to the new type", scaled);

        optionsPanel.add(Box.createHorizontalStrut(10));
        optionsPanel.add(scaleCheck);
        optionsPanel.add(Box.createHorizontalGlue());

        mainPanel.add(dataTypePanel);
        mainPanel.add(optionsPanel);
        mainPanel.add(Box.createVerticalGlue());

        setPreferredSize(new Dimension(400, 400));
        pack();
        setVisible(true);
        addToMainDesktopPane();
        setLocation(50, 50);
        requestFocus();

        // OTHERS
        dataTypeCombo.setActionCommand(DATATYPE_CMD);
        signedCheck.setActionCommand(SIGNED_CMD);
        scaleCheck.setActionCommand(SCALE_CMD);
        dataTypeCombo.addActionListener(this);
        signedCheck.addActionListener(this);
        scaleCheck.addActionListener(this);

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
                        final Sequence outSeq;

                        // convert whole sequence
                        outSeq = seqIn.convertToType(dataType, signed, scaled);
                        // set sequence name
                        outSeq.setName(seqIn.getName() + " [" + TypeUtil.toLongString(dataType, signed) + "]");

                        // add sequence
                        Icy.getMainInterface().addSequence(outSeq);
                    }
                });
            }
        });

        updateEnable();
    }

    private void updateSequenceDataTypeLabel()
    {
        final Sequence seq = getSequence();

        if (seq != null)
            seqDataTypeLabel.setText("Current data type is "
                    + TypeUtil.toLongString(seq.getDataType(), seq.isSignedDataType()));
        else
            seqDataTypeLabel.setText("No selected sequence");
    }

    /**
     * @return the dataType
     */
    public int getDataType()
    {
        return dataType;
    }

    /**
     * @param value
     *        the dataType to set
     */
    private void setDataType(int value)
    {
        if (dataType != value)
        {
            dataType = value;

            if (TypeUtil.isFloat(value))
            {
                if (signedCheck.isEnabled())
                {
                    saveSigned = signedCheck.isSelected();
                    signedCheck.setSelected(true);
                    signedCheck.setEnabled(false);
                }
            }
            else
            {
                if (!signedCheck.isEnabled())
                {
                    signedCheck.setEnabled(true);
                    signedCheck.setSelected(saveSigned);
                }
            }
        }
    }

    /**
     * @return the signed
     */
    public boolean getSigned()
    {
        return signed;
    }

    /**
     * @param signed
     *        the signed to set
     */
    private void setSigned(boolean value)
    {
        if (signed != value)
        {
            signed = value;
        }
    }

    /**
     * @return the scaled
     */
    public boolean getScaled()
    {
        return scaled;
    }

    /**
     * @param scale
     *        the scale to set
     */
    private void setScaled(boolean value)
    {
        if (scaled != value)
        {
            scaled = value;
        }
    }

    private void updateEnable()
    {
        // disable action if source is not defined
        getOkBtn().setEnabled(getSequence() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final String cmd = e.getActionCommand();

        if (DATATYPE_CMD.equals(cmd))
            setDataType(TypeUtil.getDataType((String) ((JComboBox) e.getSource()).getSelectedItem()));
        else if (SIGNED_CMD.equals(cmd))
            setSigned(((JCheckBox) e.getSource()).isSelected());
        else if (SCALE_CMD.equals(cmd))
            setScaled(((JCheckBox) e.getSource()).isSelected());
    }

    @Override
    public void sequenceChanged(Sequence sequence)
    {
        updateSequenceDataTypeLabel();
        updateEnable();
    }
}
