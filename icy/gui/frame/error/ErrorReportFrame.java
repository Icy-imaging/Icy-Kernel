package icy.gui.frame.error;

import icy.gui.frame.IcyFrame;
import icy.gui.frame.TitledFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.system.IcyExceptionHandler;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.text.BadLocationException;

public class ErrorReportFrame extends TitledFrame implements ActionListener
{
    /**
     * This function test if we already have an active error report frame opened.
     */
    public static boolean hasErrorFrameOpened()
    {
        return !IcyFrame.getAllFrames(ErrorReportFrame.class).isEmpty();
    }

    // GUI
    protected ErrorReportPanel panel;

    // internals
    protected ActionListener reportAction;

    /**
     * Create the frame.
     */
    public ErrorReportFrame(Icon icon, String title, String message)
    {
        super("Bug report", true, true, true, true);

        panel = new ErrorReportPanel(icon, title, message);

        panel.reportButton.addActionListener(this);
        panel.closeButton.addActionListener(this);

        // default report action
        reportAction = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final ProgressFrame progressFrame = new ProgressFrame("Sending report...");

                try
                {
                    IcyExceptionHandler.report(panel.getReportMessage());
                }
                catch (BadLocationException ex)
                {
                    System.err.println("Error while reporting error :");
                    IcyExceptionHandler.showErrorMessage(ex, true);
                }
                finally
                {
                    progressFrame.close();
                }
            }
        };

        mainPanel.add(panel, BorderLayout.CENTER);

        addToMainDesktopPane();
        setSize(new Dimension(520, 450));
        setVisible(true);
        requestFocus();
        center();
    }

    /**
     * Returns formatted report message (ready to send to web site).
     * 
     * @throws BadLocationException
     */
    public String getReportMessage() throws BadLocationException
    {
        return panel.getReportMessage();
    }

    /**
     * Set a specific action on the report button
     */
    public void setReportAction(ActionListener action)
    {
        reportAction = action;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if ((e.getSource() == panel.reportButton) && (reportAction != null))
            reportAction.actionPerformed(e);

        close();
    }
}
