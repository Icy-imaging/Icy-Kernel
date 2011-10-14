package icy.gui.sequence;

import icy.gui.component.ComponentUtil;
import icy.sequence.Sequence;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class SequencePropertiesPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -1568878218022361239L;

    private JTextField nameField;
    private JTextField pixelSizeXField;
    private JTextField pixelSizeYField;
    private JTextField pixelSizeZField;
    private JTextField timeIntervalField;
    private JPanel channelsPanel;
    private JTextField[] channelsField;

    /**
     * Create the panel.
     */
    public SequencePropertiesPanel()
    {
        super();

        initialize();
    }

    void initialize()
    {
        setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(4, 4, 0, 4));
        add(panel, BorderLayout.NORTH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel panel_1 = new JPanel();
        panel.add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.LINE_AXIS));

        JLabel lblNewLabel = new JLabel("Name");
        lblNewLabel.setToolTipText("Sequence name");
        panel_1.add(lblNewLabel);
        lblNewLabel.setPreferredSize(new Dimension(100, 14));
        lblNewLabel.setMinimumSize(new Dimension(100, 14));
        lblNewLabel.setMaximumSize(new Dimension(100, 14));

        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(200, 20));
        nameField.setMinimumSize(new Dimension(80, 20));
        panel_1.add(nameField);

        Component verticalStrut_1 = Box.createVerticalStrut(20);
        verticalStrut_1.setPreferredSize(new Dimension(0, 4));
        panel.add(verticalStrut_1);

        JPanel panel_2 = new JPanel();
        panel.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.LINE_AXIS));

        JLabel lblNewLabel_1 = new JLabel("Pixel Size XYZT");
        lblNewLabel_1.setMaximumSize(new Dimension(100, 14));
        lblNewLabel_1.setMinimumSize(new Dimension(100, 14));
        lblNewLabel_1.setPreferredSize(new Dimension(100, 14));
        lblNewLabel_1
                .setToolTipText("Pixel size for X, Y, Z dimension (in mm) and time resolution for T dimension (in ms)");
        panel_2.add(lblNewLabel_1);

        JPanel panel_4 = new JPanel();
        panel_2.add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.LINE_AXIS));

        pixelSizeXField = new JTextField();
        pixelSizeXField.setPreferredSize(new Dimension(40, 20));
        pixelSizeXField.setMinimumSize(new Dimension(40, 20));
        pixelSizeXField.setToolTipText("X pixel size (in mm).");
        panel_4.add(pixelSizeXField);

        Component horizontalStrut = Box.createHorizontalStrut(10);
        panel_4.add(horizontalStrut);

        pixelSizeYField = new JTextField();
        pixelSizeYField.setPreferredSize(new Dimension(40, 20));
        pixelSizeYField.setMinimumSize(new Dimension(40, 20));
        pixelSizeYField.setToolTipText("Y pixel size (in mm).");
        panel_4.add(pixelSizeYField);

        Component horizontalStrut_1 = Box.createHorizontalStrut(10);
        panel_4.add(horizontalStrut_1);

        pixelSizeZField = new JTextField();
        pixelSizeZField.setPreferredSize(new Dimension(40, 20));
        pixelSizeZField.setMinimumSize(new Dimension(40, 20));
        pixelSizeZField.setToolTipText("Z pixel size (in mm).");
        panel_4.add(pixelSizeZField);

        Component horizontalStrut_2 = Box.createHorizontalStrut(10);
        panel_4.add(horizontalStrut_2);

        timeIntervalField = new JTextField();
        timeIntervalField.setPreferredSize(new Dimension(40, 20));
        timeIntervalField.setMinimumSize(new Dimension(40, 20));
        timeIntervalField.setToolTipText("T time resolution (in ms).");
        panel_4.add(timeIntervalField);

        Component verticalStrut = Box.createVerticalStrut(20);
        verticalStrut.setPreferredSize(new Dimension(0, 4));
        panel.add(verticalStrut);

        channelsPanel = new JPanel();
        panel.add(channelsPanel);
        channelsPanel.setLayout(new BoxLayout(channelsPanel, BoxLayout.PAGE_AXIS));

        Component glue = Box.createGlue();
        add(glue, BorderLayout.CENTER);
    }

    public void setSequence(Sequence sequence)
    {
        nameField.setText(sequence.getName());
        pixelSizeXField.setText(StringUtil.toString(sequence.getPixelSizeX()));
        pixelSizeYField.setText(StringUtil.toString(sequence.getPixelSizeY()));
        pixelSizeZField.setText(StringUtil.toString(sequence.getPixelSizeZ()));
        timeIntervalField.setText(StringUtil.toString(sequence.getTimeInterval()));

        final int sizeC = sequence.getSizeC();

        channelsPanel.removeAll();
        channelsField = new JTextField[sizeC];

        for (int c = 0; c < sizeC; c++)
        {
            final JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

            final JLabel label = new JLabel("Channel " + c + " name");
            label.setToolTipText("Channel " + c + " name");
            ComponentUtil.setFixedWidth(label, 100);

            final JTextField field = new JTextField();
            field.setText(sequence.getChannelName(c));

            panel.add(label);
            panel.add(field);

            channelsField[c] = field;
            channelsPanel.add(panel);
        }

        channelsPanel.revalidate();
    }

    public String getNameFieldValue()
    {
        return nameField.getText();
    }

    public double getPixelSizeXFieldValue()
    {
        return StringUtil.parseDouble(pixelSizeXField.getText(), 1d);
    }

    public double getPixelSizeYFieldValue()
    {
        return StringUtil.parseDouble(pixelSizeYField.getText(), 1d);
    }

    public double getPixelSizeZFieldValue()
    {
        return StringUtil.parseDouble(pixelSizeZField.getText(), 1d);
    }

    public double getTimeIntervalFieldValue()
    {
        return StringUtil.parseDouble(timeIntervalField.getText(), 1d);
    }

    public String getChannelNameFieldValue(int index)
    {
        return channelsField[index].getText();
    }
}
