/**
 * 
 */
package icy.gui.sequence.tools;

import icy.gui.frame.ActionFrame;
import icy.gui.frame.progress.ProgressFrame;
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

import javax.swing.JLabel;

/**
 * @author Stephane
 */
public class SequenceCanvasResizeFrame extends ActionFrame
{
    private class SequenceCanvasResizePanel extends SequenceBaseResizePanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7929109041552115932L;

        private JLabel lblNewLabel_1;
        private PositionAlignmentPanel positionAlignmentPanel;

        public SequenceCanvasResizePanel(Sequence sequence)
        {
            super(sequence);
            
            keepRatioCheckBox.setSelected(false);

            positionAlignmentPanel.addActionListener(new ActionListener()
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

            lblNewLabel_1 = new JLabel("Content alignment");
            GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
            gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
            gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
            gbc_lblNewLabel_1.gridx = 5;
            gbc_lblNewLabel_1.gridy = 2;
            settingPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

            positionAlignmentPanel = new PositionAlignmentPanel();
            GridBagConstraints gbc_positionAlignmentPanel = new GridBagConstraints();
            gbc_positionAlignmentPanel.gridheight = 4;
            gbc_positionAlignmentPanel.insets = new Insets(0, 0, 5, 5);
            gbc_positionAlignmentPanel.fill = GridBagConstraints.BOTH;
            gbc_positionAlignmentPanel.gridx = 5;
            gbc_positionAlignmentPanel.gridy = 3;
            settingPanel.add(positionAlignmentPanel, gbc_positionAlignmentPanel);
        }

        @Override
        public FilterType getFilterType()
        {
            return null;
        }

        @Override
        public boolean getResizeContent()
        {
            return false;
        }

        @Override
        public int getXAlign()
        {
            return positionAlignmentPanel.getXAlign();
        }

        @Override
        public int getYAlign()
        {
            return positionAlignmentPanel.getYAlign();
        }
    }

    final SequenceCanvasResizePanel resizePanel;

    public SequenceCanvasResizeFrame(Sequence sequence)
    {
        super("Canvas size", true);

        // GUI
        setTitleVisible(false);

        resizePanel = new SequenceCanvasResizePanel(sequence);
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

        setSizeExternal(420, 520);
        setSizeInternal(420, 520);
        addToMainDesktopPane();
        center();
        setVisible(true);
    }
}