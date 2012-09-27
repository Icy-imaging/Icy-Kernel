package icy.gui.plugin;

import icy.gui.frame.TitledFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.plugin.PluginDescriptor;
import icy.system.IcyExceptionHandler;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class PluginErrorReportFrame extends TitledFrame
{
    // GUI
    PluginErrorReportPanel panel;

    /**
     * Create the panel.
     */
    PluginErrorReportFrame(PluginDescriptor plugin, String devId, String title, String message)
    {
        super("Bug report", true, true, true, true);

        panel = new PluginErrorReportPanel(plugin, devId, title, message);

        panel.reportButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final ProgressFrame progressFrame = new ProgressFrame("Sending report...");

                try
                {
                    final Document commentDoc = panel.commentTextPane.getDocument();
                    final String comment = commentDoc.getText(0, commentDoc.getLength());

                    String error;

                    if (!StringUtil.isEmpty(comment))
                        error = "Comment:\n" + comment;
                    else
                        error = "";

                    final Document errorDoc = panel.errorMessageTextPane.getDocument();
                    error = error + "\n\n" + errorDoc.getText(0, errorDoc.getLength());

                    IcyExceptionHandler.report(panel.plugin, panel.devId, error);
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

                close();
            }
        });
        panel.closeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                close();
            }
        });

        mainPanel.add(panel, BorderLayout.CENTER);

        addToMainDesktopPane();
        setSize(new Dimension(520, 450));
        setVisible(true);
        requestFocus();
        center();
    }

    /**
     * @return the plugin
     */
    public PluginDescriptor getPlugin()
    {
        return panel.plugin;
    }

    public String getDevId()
    {
        return panel.devId;
    }
}
