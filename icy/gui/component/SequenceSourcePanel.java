package icy.gui.component;

import icy.sequence.DimensionId;
import icy.sequence.SequenceModel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class SequenceSourcePanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 4467616009787995713L;

    private JSpinner keepSpinner;
    private JSpinner bypassSpinner;
    private JLabel rangeLabel;

    private final SequenceModel model;
    private final DimensionId dim;

    /**
     * Create the panel.
     */
    public SequenceSourcePanel(SequenceModel model, DimensionId dim)
    {
        super();

        this.model = model;
        this.dim = dim;

        initialize();
    }

    private void initialize()
    {
        setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        add(panel, BorderLayout.CENTER);
        panel.setLayout(new BorderLayout(0, 0));

        SequencePreviewPanel sequencePreviewPanel = new SequencePreviewPanel();
        panel.add(sequencePreviewPanel, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel();
        panel.add(panelBottom, BorderLayout.SOUTH);
        panelBottom.setLayout(new BoxLayout(panelBottom, BoxLayout.PAGE_AXIS));

        JPanel panelLabel = new JPanel();
        panelBottom.add(panelLabel);
        panelLabel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        rangeLabel = new JLabel("Range to process");
        panelLabel.add(rangeLabel);
        rangeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        RangeComponent rangeComponent = new RangeComponent();
        panelBottom.add(rangeComponent);

        JPanel panelConservation = new JPanel();
        panelBottom.add(panelConservation);
        panelConservation.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JLabel lblConservationLabel = new JLabel("Extraction rules:");
        lblConservationLabel.setToolTipText("Define the extraction loop rules (ex: keep 1, ignore 2, keep 1, ...)");
        panelConservation.add(lblConservationLabel);

        Component horizontalStrut = Box.createHorizontalStrut(20);
        panelConservation.add(horizontalStrut);

        JLabel lblNewLabel_1 = new JLabel("Keep");
        panelConservation.add(lblNewLabel_1);

        keepSpinner = new JSpinner();
        keepSpinner.setToolTipText("Number of image to keep");
        keepSpinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
        panelConservation.add(keepSpinner);

        JLabel lblNewLabel_2 = new JLabel("then bypass");
        panelConservation.add(lblNewLabel_2);

        bypassSpinner = new JSpinner();
        bypassSpinner.setToolTipText("Number of image to bypass");
        bypassSpinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
        panelConservation.add(bypassSpinner);

    }

}
