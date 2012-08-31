package icy.gui.plugin;

import icy.gui.frame.TitledFrame;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.util.GuiUtil;
import icy.plugin.PluginDescriptor;
import icy.system.IcyExceptionHandler;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class PluginErrorReportFrame extends TitledFrame
{
    final PluginDescriptor plugin;
    final String devId;
    final String message;

    /**
     * Create the panel.
     */
    public PluginErrorReportFrame(PluginDescriptor plugin, String devId, String message)
    {
        super("Bug report", true, true, true, true);

        this.plugin = plugin;
        this.devId = devId;
        this.message = message;

        initialize();

        addToMainDesktopPane();
        setPreferredSize(new Dimension(450, 380));
        setVisible(true);
        requestFocus();
        center();
    }

    /**
     * Create the panel.
     */
    public PluginErrorReportFrame(String devId, String message)
    {
        this(null, devId, message);
    }

    /**
     * Create the panel.
     */
    public PluginErrorReportFrame(PluginDescriptor plugin, String message)
    {
        this(plugin, null, message);
    }

    private void initialize()
    {
        final JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setContentType("text/html");

        try
        {
            textPane.getStyledDocument().insertString(textPane.getStyledDocument().getLength(), message,
                    new SimpleAttributeSet());
        }
        catch (BadLocationException e)
        {
            System.err.println("PluginErrorReport(...) error :");
            IcyExceptionHandler.showErrorMessage(e, true);
        }

        textPane.setCaretPosition(textPane.getStyledDocument().getLength());

        final JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(new TitledBorder("Detail of the message"));

        // COMMENT PANE
        final JTextPane commentPane = new JTextPane();
        commentPane.setEditable(true);
        final Document doc = commentPane.getDocument();
        try
        {
            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setItalic(attributes, true);
            StyleConstants.setForeground(attributes, Color.GRAY);
            doc.insertString(0, "Please type here your comment", attributes);
        }
        catch (BadLocationException e1)
        {
        }
        commentPane.setMinimumSize(new Dimension(0, 200));
        commentPane.addMouseListener(new MouseAdapter()
        {
            // Displays a message at the beginning that
            // disappears when first clicked
            boolean firstClickDone = false;

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (!firstClickDone)
                {
                    commentPane.setText("");
                    SimpleAttributeSet attributes = new SimpleAttributeSet();
                    StyleConstants.setItalic(attributes, false);
                    StyleConstants.setForeground(attributes, Color.BLACK);
                    try
                    {
                        doc.insertString(0, " ", attributes);
                    }
                    catch (BadLocationException e1)
                    {
                    }
                    firstClickDone = true;
                }
            }
        });

        final JScrollPane scComment = new JScrollPane(commentPane);
        scComment.setBorder(new TitledBorder("Comment"));

        // Description
        String description;

        if (plugin != null)
            description = "<html><br>The plugin named <b>" + plugin.getName()
                    + "</b> has encountered a problem.<br><br>";
        else
            description = "<html><br>The plugin from the developer <b>" + devId
                    + "</b> has encountered a problem.<br><br>";

        final JLabel label = new JLabel(description
                + "Reporting this problem is anonymous and will help improving this plugin" + "<br><br></html>",
                SwingConstants.CENTER);
        final JButton reportButton = new JButton("Report");
        final JButton closeButton = new JButton("Close");
        final JButton btnShowDetails = new JButton("Show details");

        // North message + icon
        JPanel panelPluginAndMessage = new JPanel();
        panelPluginAndMessage.setLayout(new BoxLayout(panelPluginAndMessage, BoxLayout.X_AXIS));
        if (plugin != null)
            panelPluginAndMessage.add(new JLabel(plugin.getIcon()));
        panelPluginAndMessage.add(Box.createHorizontalStrut(10));
        panelPluginAndMessage.add(GuiUtil.besidesPanel(label));

        // north part
        JPanel panelNorth = new JPanel();
        panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.Y_AXIS));
        panelNorth.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));
        panelNorth.add(panelPluginAndMessage);
        panelNorth.add(GuiUtil.besidesPanel(new JLabel(" "), btnShowDetails, new JLabel(" ")));

        // middle part
        final JPanel detailsPanel = GuiUtil.generatePanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.PAGE_AXIS));
        detailsPanel.add(scComment);

        // generating the GUI
        final JPanel mainPanel = getMainPanel();
        mainPanel.add(panelNorth, BorderLayout.NORTH);
        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        mainPanel.add(
                GuiUtil.besidesPanel(new JLabel(" "), closeButton, new JLabel(" "), reportButton, new JLabel(" ")),
                BorderLayout.SOUTH);

        btnShowDetails.addActionListener(new ActionListener()
        {
            boolean active = false;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int w = getWidth();
                int h = getHeight();

                // add / remove panel
                if (active)
                {
                    detailsPanel.remove(scrollPane);
                    btnShowDetails.setText("Show Details");
                    setSize(w, h - (scrollPane.getHeight() + 10));
                }
                else
                {
                    detailsPanel.add(scrollPane, 0);
                    btnShowDetails.setText("Hide Details");
                    setSize(w, h + 200);
                }
                active = !active;
                detailsPanel.revalidate();
            }
        });

        reportButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final ProgressFrame progressFrame = new ProgressFrame("Sending report...");

                try
                {
                    final Document commentDoc = commentPane.getDocument();
                    final String comment = commentDoc.getText(0, commentDoc.getLength());

                    String error;

                    if (!StringUtil.isEmpty(comment))
                        error = "Comment:\n" + comment;
                    else
                        error = "";

                    final Document errorDoc = textPane.getDocument();
                    error = error + "\n\n" + errorDoc.getText(0, errorDoc.getLength());

                    IcyExceptionHandler.report(plugin, devId, error);
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
        closeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                close();
            }
        });
    }

    /**
     * @return the plugin
     */
    public PluginDescriptor getPlugin()
    {
        return plugin;
    }

    public String getDevId()
    {
        return devId;
    }
}
