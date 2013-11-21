package icy.gui.system;

import icy.gui.frame.IcyFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class NewVersionFrame extends IcyFrame
{
    private JPanel contentPane;
    private JTextPane changesLogTextPane;

    /**
     * Create the New Version Frame.
     */
    public NewVersionFrame(String changesLog)
    {
        super("New version installed !", true, true, false, false);
        
        setPreferredSize(new Dimension(640, 480));
        setSize(640, 480);

        initialize();

        changesLogTextPane.setText(changesLog);
        changesLogTextPane.setCaretPosition(2);

        addToMainDesktopPane();
        center();
        setVisible(true);
        toFront();
    }

    private void initialize()
    {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        final JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.NORTH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        final JPanel panel_1 = new JPanel();
        panel_1.setBorder(new EmptyBorder(2, 0, 2, 0));
        panel.add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.LINE_AXIS));

        final JLabel lblHeresTheChanges = new JLabel("A new version has been installed !");
        panel_1.add(lblHeresTheChanges);
        lblHeresTheChanges.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblHeresTheChanges.setHorizontalAlignment(SwingConstants.CENTER);

        final JPanel panel_2 = new JPanel();
        panel_2.setBorder(new EmptyBorder(2, 0, 2, 0));
        panel.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.LINE_AXIS));

        final JLabel lblNewLabel = new JLabel("Checkout what is new in this version:");
        panel_2.add(lblNewLabel);
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));

        final JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        changesLogTextPane = new JTextPane();
        changesLogTextPane.setEditable(false);
        scrollPane.setViewportView(changesLogTextPane);
    }
}
