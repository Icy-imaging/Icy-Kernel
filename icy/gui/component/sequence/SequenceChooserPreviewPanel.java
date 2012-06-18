package icy.gui.component.sequence;

import icy.gui.component.sequence.SequenceChooser.SequenceChooserListener;
import icy.sequence.Sequence;
import icy.sequence.SequenceModel;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Image;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class SequenceChooserPreviewPanel extends JPanel implements SequenceChooserListener, SequenceModel
{
    /**
     * 
     */
    private static final long serialVersionUID = -1637532468722270264L;

    protected JLabel titleLabel;
    protected SequenceChooser sequenceChooser;
    SequencePreviewPanel sequencePreviewPanel;

    /**
     * Create the panel.
     */
    public SequenceChooserPreviewPanel(String title)
    {
        super();

        initialize(title);

        sequenceChanged(sequenceChooser.getSelectedSequence());

        sequencePreviewPanel.setModel(this);
        sequenceChooser.addListener(this);
    }

    /**
     * Create the panel.
     */
    public SequenceChooserPreviewPanel()
    {
        this(null);
    }

    private void initialize(String title)
    {
        setLayout(new BorderLayout(0, 0));

        sequencePreviewPanel = new SequencePreviewPanel();
        sequencePreviewPanel.setBorder(new EmptyBorder(4, 0, 0, 0));
        add(sequencePreviewPanel, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        add(panel, BorderLayout.NORTH);
        panel.setLayout(new BorderLayout(0, 0));

        sequenceChooser = new SequenceChooser();
        panel.add(sequenceChooser);

        if (!StringUtil.isEmpty(title))
        {
            titleLabel = new JLabel(title);
            titleLabel.setBorder(new EmptyBorder(4, 0, 0, 8));
            panel.add(titleLabel, BorderLayout.WEST);
        }
    }

    /**
     * @return the sequenceChooser
     */
    public SequenceChooser getSequenceChooser()
    {
        return sequenceChooser;
    }

    /**
     * @return the sequencePreviewPanel
     */
    public SequencePreviewPanel getSequencePreviewPanel()
    {
        return sequencePreviewPanel;
    }

    public boolean getPreviewVisible()
    {
        return sequencePreviewPanel.isVisible();
    }

    public void setPreviewVisible(boolean value)
    {
        sequencePreviewPanel.setVisible(value);
    }

    public void setTitle(String value)
    {
        sequencePreviewPanel.setTitle(value);
    }

    @Override
    public void sequenceChanged(Sequence sequence)
    {
        sequencePreviewPanel.dimensionChanged();
    }

    @Override
    public int getSizeX()
    {
        final Sequence sequence = sequenceChooser.getSelectedSequence();

        if (sequence != null)
            return sequence.getSizeX();

        return 0;
    }

    @Override
    public int getSizeY()
    {
        final Sequence sequence = sequenceChooser.getSelectedSequence();

        if (sequence != null)
            return sequence.getSizeY();

        return 0;
    }

    @Override
    public int getSizeZ()
    {
        final Sequence sequence = sequenceChooser.getSelectedSequence();

        if (sequence != null)
            return sequence.getSizeZ();

        return 0;
    }

    @Override
    public int getSizeT()
    {
        final Sequence sequence = sequenceChooser.getSelectedSequence();

        if (sequence != null)
            return sequence.getSizeT();

        return 0;
    }

    @Override
    public int getSizeC()
    {
        final Sequence sequence = sequenceChooser.getSelectedSequence();

        if (sequence != null)
            return sequence.getSizeC();

        return 0;
    }

    @Override
    public Image getImage(int t, int z)
    {
        final Sequence sequence = sequenceChooser.getSelectedSequence();

        if (sequence != null)
            return sequence.getImage(t, z);

        return null;
    }

    @Override
    public Image getImage(int t, int z, int c)
    {
        final Sequence sequence = sequenceChooser.getSelectedSequence();

        if (sequence != null)
            return sequence.getImage(t, z, c);

        return null;
    }
}
