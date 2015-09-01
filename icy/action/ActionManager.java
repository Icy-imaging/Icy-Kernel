/**
 * 
 */
package icy.action;

import icy.gui.main.ActiveSequenceListener;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stephane
 */
public class ActionManager
{
    /**
     * All registered action
     */
    public static List<IcyAbstractAction> actions = null;

    // internals
    private static ActiveSequenceListener activeSequenceListener;

    public static synchronized void init()
    {
        // init actions
        if (actions == null)
        {
            actions = new ArrayList<IcyAbstractAction>();

            // add all kernels actions
            actions.addAll(FileActions.getAllActions());
            actions.addAll(GeneralActions.getAllActions());
            actions.addAll(PreferencesActions.getAllActions());
            actions.addAll(SequenceOperationActions.getAllActions());
            actions.addAll(RoiActions.getAllActions());
            actions.addAll(CanvasActions.getAllActions());
            actions.addAll(ViewerActions.getAllActions());
            actions.addAll(WindowActions.getAllActions());

            activeSequenceListener = new ActiveSequenceListener()
            {
                @Override
                public void sequenceDeactivated(Sequence sequence)
                {
                    // nothing here
                }

                @Override
                public void sequenceActivated(Sequence sequence)
                {
                    // force action components refresh
                    for (IcyAbstractAction action : actions)
                        action.enabledChanged();
                }

                @Override
                public void activeSequenceChanged(SequenceEvent event)
                {
                    // nothing here
                }
            };

            // listen these event
            Icy.getMainInterface().addActiveSequenceListener(activeSequenceListener);
        }
    }
}
