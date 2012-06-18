package icy.gui.component.sequence;

import icy.gui.component.sequence.SequenceChooser.SequenceChooserListener;
import icy.gui.component.sequence.SequenceRangePreviewPanel.RangeChangeListener;
import icy.sequence.Sequence;
import icy.sequence.SequenceModel;

import java.awt.BorderLayout;
import java.awt.Image;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class SequenceChooserRangePreviewPanel extends JPanel implements SequenceChooserListener, SequenceModel
{
    /**
     * 
     */
    private static final long serialVersionUID = 4467616009787995713L;

    protected final boolean dimZ;

    protected SequenceChooserPanel sequenceChooser;
    protected SequenceRangePreviewPanel sequenceRangePreview;

    /**
     * Create the panel.
     * 
     * @param dimZ
     *        If true we display Z range selection else we display T range selection
     * @wbp.parser.constructor
     */
    public SequenceChooserRangePreviewPanel(String title, boolean dimZ)
    {
        super();

        this.dimZ = dimZ;

        initialize(title);

        sequenceRangePreview.setModel(this);
        sequenceChooser.addListener(this);

        // propagate range change event
        sequenceRangePreview.addRangeChangeListener(new RangeChangeListener()
        {
            @Override
            public void rangeChanged()
            {
                fireRangeChangedEvent();
            }
        });
    }

    /**
     * Create the panel.
     * 
     * @param dimZ
     *        If true we display Z range selection else we display T range selection
     */
    public SequenceChooserRangePreviewPanel(boolean dimZ)
    {
        this(null, dimZ);
    }

    private void initialize(String title)
    {
        setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        add(panel, BorderLayout.CENTER);
        panel.setLayout(new BorderLayout(0, 0));

        sequenceChooser = new SequenceChooserPanel(title);
        sequenceChooser.setBorder(new EmptyBorder(0, 0, 4, 0));
        panel.add(sequenceChooser, BorderLayout.NORTH);

        sequenceRangePreview = new SequenceRangePreviewPanel(dimZ);
        panel.add(sequenceRangePreview, BorderLayout.CENTER);

        validate();
    }

    public boolean isPreviewVisible()
    {
        return sequenceRangePreview.isPreviewVisible();
    }

    public void setPreviewVisible(boolean value)
    {
        sequenceRangePreview.setPreviewVisible(value);
    }

    public int getRangeMax()
    {
        return sequenceRangePreview.getRangeMax();
    }

    public boolean isRangeIndexSelected(int index)
    {
        return sequenceRangePreview.isRangeIndexSelected(index);
    }

    public int getRangeNumSelected()
    {
        return sequenceRangePreview.getRangeNumSelected();
    }

    public int getKeepValue()
    {
        return sequenceRangePreview.getKeepValue();
    }

    public int getIgnoreValue()
    {
        return sequenceRangePreview.getIgnoreValue();
    }

    public int getLoopValue()
    {
        return sequenceRangePreview.getLoopValue();
    }

    protected void fireRangeChangedEvent()
    {
        for (RangeChangeListener listener : getListeners(RangeChangeListener.class))
            listener.rangeChanged();
    }

    public void addRangeChangeListener(RangeChangeListener listener)
    {
        listenerList.add(RangeChangeListener.class, listener);
    }

    public void removeRangeChangeListener(RangeChangeListener listener)
    {
        listenerList.remove(RangeChangeListener.class, listener);
    }

    @Override
    public void sequenceChanged(Sequence sequence)
    {
        sequenceRangePreview.dimensionChanged();
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
