/**
 * 
 */
package icy.gui.sequence.tools;

import icy.gui.frame.IcyFrame;
import icy.gui.util.GuiUtil;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Advanced conversion of Z and T dimension.
 * 
 * @author Stephane
 */
public class SequenceDimensionConvertFrame implements ActionListener
{
    IcyFrame mainFrame;
    JButton startButton = new JButton("Convert !");
    JTextField numberOfSlicePerStackTextField = new JTextField("10");

    public SequenceDimensionConvertFrame()
    {
        super();

        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                JPanel panel = new JPanel();
                mainFrame = GuiUtil.generateTitleFrame("Volume to stack converter", panel, new Dimension(300, 100),
                        false, true, false, true);

                panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                panel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(30)));
                panel.add(GuiUtil.createLineBoxPanel(new JLabel("Number of Z-slices per stack: ")));
                panel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(15)));
                panel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(50), numberOfSlicePerStackTextField,
                        Box.createHorizontalStrut(50)));
                panel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(30)));
                panel.add(GuiUtil.createLineBoxPanel(startButton));
                panel.add(GuiUtil.createLineBoxPanel(Box.createVerticalStrut(30)));

                startButton.addActionListener(SequenceDimensionConvertFrame.this);

                mainFrame.pack();
                mainFrame.addToMainDesktopPane();
                mainFrame.center();
                mainFrame.requestFocus();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == startButton)
        {
            int numberOfSlicePerStack = 0;
            try
            {
                numberOfSlicePerStack = Integer.parseInt(numberOfSlicePerStackTextField.getText());
            }
            catch (Exception e1)
            {
                System.out.println("invalid number");
                return;
            }

            if (numberOfSlicePerStack < 2)
            {
                mainFrame.close();
                return;
            }

            Sequence sequence = Icy.getMainInterface().getFocusedSequence();

            if (sequence == null)
            {
                System.out.println("No sequence selected");
                return;
            }

            sequence.beginUpdate();
            try
            {
                int cursorZ = 0;
                int cursorT = 0;
                for (int t = 0; t < sequence.getSizeT(); t++)
                {
                    sequence.setImage(cursorT, cursorZ, sequence.getImage(t, 0));
                    if (t != cursorT)
                    {
                        sequence.removeImage(t, 0);
                    }
                    cursorZ++;
                    if (cursorZ > numberOfSlicePerStack - 1)
                    {
                        cursorT++;
                        cursorZ = 0;
                    }
                }
            }
            finally
            {
                sequence.endUpdate();
            }

            mainFrame.close();
        }
    }
}
