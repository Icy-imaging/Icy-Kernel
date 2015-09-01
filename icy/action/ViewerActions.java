/**
 * 
 */
package icy.action;

import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;
import icy.image.lut.LUT;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.util.ClassUtil;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import plugins.kernel.canvas.VtkCanvas;

/**
 * Viewer associated actions (Duplicate, externalize...)
 * 
 * @author Stephane
 */
public class ViewerActions
{
    public static IcyAbstractAction duplicateAction = new IcyAbstractAction("Duplicate view", new IcyIcon(
            ResourceUtil.ICON_DUPLICATE), "Duplicate view (no data duplication)", KeyEvent.VK_F2)
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8660425976560135450L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            ThreadUtil.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    // so it won't change during process
                    final Viewer viewer = Icy.getMainInterface().getActiveViewer();
                    final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;
                    final Sequence sequence = (viewer != null) ? viewer.getSequence() : null;

                    if ((sequence != null) && (canvas != null))
                    {
                        final Viewer v = new Viewer(sequence);
                        final LUT oldLut = viewer.getLut();
                        final LUT newLut = v.getLut();

                        // copy LUT
                        if (canvas instanceof VtkCanvas)
                        {
                            // don't copy alpha colormap
                            newLut.setColorMaps(oldLut, false);
                            newLut.setScalers(oldLut);
                        }
                        else
                            newLut.copyFrom(oldLut);
                    }
                }
            });

            return true;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveViewer() != null);
        }
    };

    /**
     * Return all actions of this class
     */
    public static List<IcyAbstractAction> getAllActions()
    {
        final List<IcyAbstractAction> result = new ArrayList<IcyAbstractAction>();

        for (Field field : ViewerActions.class.getFields())
        {
            final Class<?> type = field.getType();

            try
            {
                if (ClassUtil.isSubClass(type, IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (ClassUtil.isSubClass(type, IcyAbstractAction.class))
                    result.add((IcyAbstractAction) field.get(null));
            }
            catch (Exception e)
            {
                // ignore
            }
        }

        return result;
    }
}
