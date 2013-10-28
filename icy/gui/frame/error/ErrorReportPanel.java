package icy.gui.frame.error;

import icy.gui.component.IcyTextField;
import icy.system.IcyExceptionHandler;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
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

public class ErrorReportPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -6672076887465746832L;

    // GUI
    JTextPane errorMessageTextPane;
    JTextPane commentTextPane;
    IcyTextField emailTextField;
    JButton reportButton;
    JButton closeButton;
    JLabel label;

    public ErrorReportPanel(Icon icon, String title, String message)
    {
        super();

        initialize();

        if (!StringUtil.isEmpty(title))
            label.setText(title);
        if (icon != null)
            label.setIcon(icon);

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
    ErrorReportPanel()
    {
        this(null, "Test", "An error occured");
    }

    private void initialize()
    {
        // top
        label = new JLabel("An error occured !", SwingConstants.CENTER);

        // center
        errorMessageTextPane = new JTextPane();
        errorMessageTextPane.setEditable(false);
        errorMessageTextPane.setContentType("text/html");

        JScrollPane messageScrollPane = new JScrollPane(errorMessageTextPane);

        JPanel messagePanel = new JPanel();
        messagePanel.setBorder(new TitledBorder(null, "Message", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        messagePanel.setLayout(new BorderLayout(0, 0));
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);

        JPanel userPanel = new JPanel();
        userPanel.setBorder(new TitledBorder(null, "Comment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        userPanel.setLayout(new BorderLayout(0, 0));

        // buttons panel
        reportButton = new JButton("Report");
        closeButton = new JButton("Close");

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(reportButton);
        buttonsPanel.add(closeButton);

        // bottom
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(0, 0));

        bottomPanel.add(userPanel, BorderLayout.CENTER);

        JPanel commentPanel = new JPanel();
        userPanel.add(commentPanel, BorderLayout.CENTER);
        commentPanel.setLayout(new BorderLayout(0, 0));

        // comment pane
        commentTextPane = new JTextPane();
        commentTextPane.setEditable(true);

        final JScrollPane scComment = new JScrollPane(commentTextPane);
        commentPanel.add(scComment, BorderLayout.NORTH);
        scComment.setPreferredSize(new Dimension(23, 60));
        scComment.setMinimumSize(new Dimension(23, 60));

        JPanel emailPanel = new JPanel();
        userPanel.add(emailPanel, BorderLayout.SOUTH);
        GridBagLayout gbl_emailPanel = new GridBagLayout();
        gbl_emailPanel.columnWidths = new int[] {0, 0, 0};
        gbl_emailPanel.rowHeights = new int[] {0, 0};
        gbl_emailPanel.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
        gbl_emailPanel.rowWeights = new double[] {0.0, Double.MIN_VALUE};
        emailPanel.setLayout(gbl_emailPanel);

        JLabel lblEmail = new JLabel("Email:");
        GridBagConstraints gbc_lblEmail = new GridBagConstraints();
        gbc_lblEmail.insets = new Insets(0, 0, 0, 5);
        gbc_lblEmail.anchor = GridBagConstraints.WEST;
        gbc_lblEmail.gridx = 0;
        gbc_lblEmail.gridy = 0;
        emailPanel.add(lblEmail, gbc_lblEmail);

        emailTextField = new IcyTextField();
        GridBagConstraints gbc_emailTextField = new GridBagConstraints();
        gbc_emailTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_emailTextField.gridx = 1;
        gbc_emailTextField.gridy = 0;
        emailPanel.add(emailTextField, gbc_emailTextField);
        emailTextField.setColumns(10);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout(0, 0));

        add(label, BorderLayout.NORTH);
        add(messagePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Returns formatted report message (ready to send to web site).
     * 
     * @throws BadLocationException
     */
    public String getReportMessage() throws BadLocationException
    {
        final String email = "";
        final Document commentDoc = commentTextPane.getDocument();
        final Document errorDoc = errorMessageTextPane.getDocument();
        String comment = commentDoc.getText(0, commentDoc.getLength());
        String result = "";

        if (!StringUtil.isEmpty(email))
            result += "Email: " + email + "\n";
        if (!StringUtil.isEmpty(comment))
            result += "Comment:\n" + comment + "\n\n";

        result += errorDoc.getText(0, errorDoc.getLength());

        return result;
    }

}
