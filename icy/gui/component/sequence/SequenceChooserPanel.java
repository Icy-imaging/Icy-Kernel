package icy.gui.component.sequence;

import icy.common.listener.AcceptListener;
import icy.gui.component.sequence.SequenceChooser.SequenceChooserListener;
import icy.sequence.Sequence;
import icy.util.StringUtil;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class SequenceChooserPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 486511549781132187L;

    protected JLabel titleLabel;
    protected SequenceChooser sequenceChooser;

    /**
     * Create the panel.
     */
    public SequenceChooserPanel(String title)
    {
        super();

        initialize(title);
    }

    private void initialize(String title)
    {
        setLayout(new BorderLayout(0, 0));

        sequenceChooser = new SequenceChooser();
        add(sequenceChooser, BorderLayout.CENTER);

        if (!StringUtil.isEmpty(title))
        {
            titleLabel = new JLabel(title);
            titleLabel.setBorder(new EmptyBorder(4, 0, 0, 8));
            add(titleLabel, BorderLayout.WEST);
        }
    }

    /**
     * @return the filter
     */
    public AcceptListener getFilter()
    {
        return sequenceChooser.getFilter();
    }

    /**
     * @param filter
     *        the filter to set
     */
    public void setFilter(AcceptListener filter)
    {
        sequenceChooser.setFilter(filter);
    }

    /**
     * @return current sequence selected in combo. null if no sequence is selected or if the
     *         sequence do not exists anymore.
     */
    public Sequence getSelectedSequence()
    {
        return sequenceChooser.getSelectedSequence();
    }

    /**
     * @param sequence
     *        The sequence to select in the combo box
     */
    public void setSelectedSequence(Sequence sequence)
    {
        sequenceChooser.setSelectedSequence(sequence);
    }

    public ArrayList<SequenceChooserListener> getListeners()
    {
        return sequenceChooser.getListeners();
    }

    public void addListener(SequenceChooserListener listener)
    {
        sequenceChooser.addListener(listener);
    }

    public void removeListener(SequenceChooserListener listener)
    {
        sequenceChooser.removeListener(listener);
    }

}
