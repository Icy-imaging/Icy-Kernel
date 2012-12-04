/**
 * 
 */
package icy.gui.sequence.tools;

import icy.gui.dialog.ActionDialog;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.util.ComponentUtil;
import icy.image.IcyBufferedImageUtil.FilterType;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * @author Stephane
 */
public class SequenceResizeFrame extends ActionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = -8638672567750415881L;

    class SequenceResizePanel extends SequenceBaseResizePanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5366610917009978874L;

        private JComboBox filterComboBox;
        private JLabel lblFilterType;

        public SequenceResizePanel(Sequence sequence)
        {
            super(sequence);

            keepRatioCheckBox.setSelected(true);

            filterComboBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    updatePreview();
                }
            });
        }

        @Override
        protected void initialize()
        {
            super.initialize();

            lblFilterType = new JLabel("Filter type");
            GridBagConstraints gbc_lblFilterType = new GridBagConstraints();
            gbc_lblFilterType.fill = GridBagConstraints.BOTH;
            gbc_lblFilterType.insets = new Insets(0, 0, 5, 5);
            gbc_lblFilterType.gridx = 5;
            gbc_lblFilterType.gridy = 2;
            settingPanel.add(lblFilterType, gbc_lblFilterType);

            filterComboBox = new JComboBox();
            filterComboBox.setModel(new DefaultComboBoxModel(new String[] {"Nearest", "Bilinear", "Bicubic"}));
            filterComboBox.setSelectedIndex(1);
            GridBagConstraints gbc_filterComboBox = new GridBagConstraints();
            gbc_filterComboBox.insets = new Insets(0, 0, 5, 5);
            gbc_filterComboBox.fill = GridBagConstraints.HORIZONTAL;
            gbc_filterComboBox.gridx = 5;
            gbc_filterComboBox.gridy = 3;
            settingPanel.add(filterComboBox, gbc_filterComboBox);
        }

        @Override
        public FilterType getFilterType()
        {
            switch (filterComboBox.getSelectedIndex())
            {
                default:
                case 0:
                    return FilterType.NEAREST;
                case 1:
                    return FilterType.BILINEAR;
                case 2:
                    return FilterType.BICUBIC;
            }
        }

        @Override
        public boolean getResizeContent()
        {
            return true;
        }

        @Override
        public int getXAlign()
        {
            return SwingConstants.CENTER;
        }

        @Override
        public int getYAlign()
        {
            return SwingConstants.CENTER;
        }
    }

    final SequenceResizePanel resizePanel;

    public SequenceResizeFrame(Sequence sequence)
    {
        super("Image size");

        resizePanel = new SequenceResizePanel(sequence);
        getMainPanel().add(resizePanel, BorderLayout.CENTER);
        validate();

        // action
        setOkAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // launch in background as it can take sometime
                ThreadUtil.bgRun(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final ProgressFrame pf = new ProgressFrame("Resizing sequence...");
                        try
                        {
                            final Sequence seqIn = resizePanel.getSequence();
                            final double pixelSize = resizePanel.getResolution();

                            // apply chosen pixel size
                            seqIn.setPixelSizeX(pixelSize);
                            seqIn.setPixelSizeY(pixelSize);

                            Icy.getMainInterface().addSequence(
                                    SequenceUtil.scale(resizePanel.getSequence(), resizePanel.getNewWidth(),
                                            resizePanel.getNewHeight(), resizePanel.getResizeContent(),
                                            resizePanel.getXAlign(), resizePanel.getYAlign(),
                                            resizePanel.getFilterType()));
                        }
                        finally
                        {
                            pf.close();
                        }
                    }
                });
            }
        });

        setSize(420, 520);
        ComponentUtil.center(this);

        setVisible(true);
    }
}
