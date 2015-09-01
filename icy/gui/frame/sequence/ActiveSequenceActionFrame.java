/**
 * 
 */
package icy.gui.frame.sequence;

import icy.gui.frame.ActionFrame;
import icy.gui.main.ActiveSequenceListener;
import icy.gui.util.ComponentUtil;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Basic frame to do a simple action on the current active sequence.
 * 
 * @author Stephane
 */
public class ActiveSequenceActionFrame extends ActionFrame implements ActiveSequenceListener
{
    public interface SourceChangeListener
    {
        public void sourceSequenceChanged(Sequence seq);
    }

    /**
     * listeners and event handler
     */
    private final List<SourceChangeListener> sourceChangeListeners;

    /**
     * gui
     */
    JPanel sourcePanel;
    JLabel sequenceLabel;

    /**
     * @param title
     * @param resizable
     * @param iconifiable
     */
    public ActiveSequenceActionFrame(String title, boolean resizable, boolean iconifiable)
    {
        super(title, resizable, iconifiable);

        sourceChangeListeners = new ArrayList<SourceChangeListener>();

        buildGUI();

        final Sequence sequence = getSequence();

        if (sequence != null)
            sequenceLabel.setText(sequence.getName());
        else
            sequenceLabel.setText("no sequence");

        // add listener
        Icy.getMainInterface().addActiveSequenceListener(this);
    }

    /**
     * @param title
     * @param resizable
     */
    public ActiveSequenceActionFrame(String title, boolean resizable)
    {
        this(title, resizable, false);
    }

    /**
     * @param title
     */
    public ActiveSequenceActionFrame(String title)
    {
        this(title, false);
    }

    @Override
    public void onClosed()
    {
        Icy.getMainInterface().removeActiveSequenceListener(this);

        super.onClosed();
    }

    protected void buildGUI()
    {
        // GUI
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        sourcePanel = new JPanel();
        sourcePanel.setBorder(BorderFactory.createTitledBorder("Selected sequence"));
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.LINE_AXIS));

        // sequence label
        sequenceLabel = new JLabel();
        sequenceLabel.setMinimumSize(new Dimension(100, 24));

        sourcePanel.add(Box.createHorizontalStrut(10));
        sourcePanel.add(sequenceLabel);
        sourcePanel.add(Box.createHorizontalGlue());

        // fix the height of source panel
        ComponentUtil.setFixedHeight(sourcePanel, 54);

        mainPanel.add(sourcePanel);
    }

    /**
     * @return the sourcePanel
     */
    public JPanel getSourcePanel()
    {
        return sourcePanel;
    }

    /**
     * @deprecated Use {@link #getSequence()} instead.
     */
    @Deprecated
    public Sequence getSeqIn()
    {
        return getSequence();
    }

    /**
     * @return the active sequence
     */
    public Sequence getSequence()
    {
        return Icy.getMainInterface().getActiveSequence();
    }

    public void addSourceChangeListener(SourceChangeListener listener)
    {
        if (!sourceChangeListeners.contains(listener))
            sourceChangeListeners.add(listener);
    }

    public void removeSourceChangeListener(SourceChangeListener listener)
    {
        sourceChangeListeners.remove(listener);
    }

    private void fireSequenceChangeEvent(Sequence seq)
    {
        for (SourceChangeListener listener : sourceChangeListeners)
            listener.sourceSequenceChanged(seq);
    }

    @Override
    public void sequenceActivated(Sequence sequence)
    {
        if (sequence != null)
            sequenceLabel.setText(sequence.getName());
        else
            sequenceLabel.setText("no sequence");

        fireSequenceChangeEvent(sequence);
    }

    @Override
    public void sequenceDeactivated(Sequence sequence)
    {
        // nothing here
    }

    @Override
    public void activeSequenceChanged(SequenceEvent event)
    {
        // nothing here
    }
}
