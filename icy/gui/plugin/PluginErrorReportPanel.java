/**
 * 
 */
package icy.gui.plugin;

import icy.plugin.PluginDescriptor;
import icy.system.IcyExceptionHandler;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

/**
 * @author Stephane
 */
public class PluginErrorReportPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -2875740914347175762L;

    // GUI
    JTextPane errorMessageTextPane;
    JLabel label;
    JTextPane commentTextPane;
    JButton reportButton;
    JButton closeButton;
    JPanel bottomPanel;
    JScrollPane messageScrollPane;
    JPanel commentPanel;
    JPanel messagePanel;

    final PluginDescriptor plugin;
    final String devId;
    final String title;
    final String message;

    public PluginErrorReportPanel(PluginDescriptor plugin, String devId, String title, String message)
    {
        super();

        this.plugin = plugin;
        this.devId = devId;
        this.title = message;
        this.message = message;

        initialize();

        String str;

        if (plugin != null)
            str = "<html><br>The plugin named <b>" + plugin.getName() + "</b> has encountered a problem";
        else
            str = "<html><br>The plugin from the developer <b>" + devId + "</b> has encountered a problem";

        if (StringUtil.isEmpty(title))
            str += ".<br><br>";
        else
            str += " :<br><i>" + title + "</i><br><br>";

        str += "Reporting this problem is anonymous and will help improving this plugin.<br><br></html>";

        label.setText(str);

        try
        {
            errorMessageTextPane.getStyledDocument().insertString(errorMessageTextPane.getStyledDocument().getLength(),
                    message, new SimpleAttributeSet());
        }
        catch (BadLocationException e)
        {
            System.err.println("PluginErrorReport(...) error :");
            IcyExceptionHandler.showErrorMessage(e, true);
        }
        errorMessageTextPane.setCaretPosition(0);

        final Document doc = commentTextPane.getDocument();

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

        commentTextPane.addMouseListener(new MouseAdapter()
        {
            // Displays a message at the beginning that
            // disappears when first clicked
            boolean firstClickDone = false;

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (!firstClickDone)
                {
                    commentTextPane.setText("");

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
    }

    /**
     * @wbp.parser.constructor
     */
    PluginErrorReportPanel()
    {
        this(new PluginDescriptor(plugins.kernel.canvas.Canvas2DPlugin.class), null, null, "Error !!");
    }

    private void initialize()
    {
        if (plugin != null)
            label = new JLabel("", plugin.getIcon(), SwingConstants.CENTER);
        else
            label = new JLabel("", SwingConstants.CENTER);

        // center
        errorMessageTextPane = new JTextPane();
        errorMessageTextPane.setEditable(false);
        errorMessageTextPane.setContentType("text/html");

        messageScrollPane = new JScrollPane(errorMessageTextPane);

        messagePanel = new JPanel();
        messagePanel.setBorder(new TitledBorder(null, "Message", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        messagePanel.setLayout(new BorderLayout(0, 0));
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);

        // comment pane
        commentTextPane = new JTextPane();
        commentTextPane.setEditable(true);

        final JScrollPane scComment = new JScrollPane(commentTextPane);
        scComment.setPreferredSize(new Dimension(23, 60));
        scComment.setMinimumSize(new Dimension(23, 60));

        commentPanel = new JPanel();
        commentPanel.setBorder(new TitledBorder(null, "Comment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        commentPanel.setLayout(new BorderLayout(0, 0));
        commentPanel.add(scComment, BorderLayout.CENTER);

        // buttons panel
        reportButton = new JButton("Report");
        closeButton = new JButton("Close");

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(reportButton);
        buttonsPanel.add(closeButton);

        // bottom
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(0, 0));

        bottomPanel.add(commentPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout(0, 0));

        add(label, BorderLayout.NORTH);
        add(messagePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
