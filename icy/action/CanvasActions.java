/**
 * 
 */
package icy.action;

import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.common.listener.weak.WeakActiveViewerListener;
import icy.gui.inspector.LayersPanel;
import icy.gui.main.ActiveViewerListener;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Canvas associated actions (disable/enable layers, fit, remove layer...)
 * 
 * @author Stephane
 */
public class CanvasActions
{
    public static class ToggleLayersAction extends IcyAbstractAction implements ActiveViewerListener
    {
        public ToggleLayersAction(boolean selected)
        {
            super("Layers", new IcyIcon(ResourceUtil.ICON_LAYER_H2), "Show/Hide layers", KeyEvent.VK_L);

            setSelected(selected);
            if (selected)
                setDescription("Hide layers");
            else
                setDescription("Show layers");

            Icy.getMainInterface().addActiveViewerListener(new WeakActiveViewerListener(this));
        }

        public ToggleLayersAction()
        {
            this(false);
        }

        /**
         * 
         */
        private static final long serialVersionUID = 923175461167344847L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;

            if (canvas != null)
            {
                final boolean visible = !canvas.isLayersVisible();

                canvas.setLayersVisible(visible);

                if (visible)
                    setDescription("Hide layers");
                else
                    setDescription("Show layers");

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveViewer() != null);
        }

        @Override
        public void viewerActivated(Viewer viewer)
        {
            // notify enabled change
            enabledChanged();
        }

        @Override
        public void viewerDeactivated(Viewer viewer)
        {
        }

        @Override
        public void activeViewerChanged(ViewerEvent event)
        {
        }
    };

    public static IcyAbstractAction screenShotAction = new IcyAbstractAction("Screeshot (view)", new IcyIcon(
            ResourceUtil.ICON_PHOTO), "Take a screenshot of current view", true, "Rendering...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8320047127782258236L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            // so it won't change during process
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;
            final Sequence sequence = (viewer != null) ? viewer.getSequence() : null;

            if ((sequence != null) && (canvas != null))
            {
                final Sequence seqOut = canvas.getRenderedSequence(true, progressFrame);

                if (seqOut != null)
                {
                    // set sequence name
                    seqOut.setName("Screen shot of '" + sequence.getName() + "' view");
                    // add sequence
                    Icy.getMainInterface().addSequence(seqOut);

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveViewer() != null);
        }
    };

    public static IcyAbstractAction screenShotAlternateAction = new IcyAbstractAction("Screenshot (global)",
            new IcyIcon(ResourceUtil.ICON_PHOTO_SMALL),
            "Take a screenshot of current view with original sequence dimension", true, "Rendering...")
    {

        /**
         * 
         */
        private static final long serialVersionUID = -6434663157861847013L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            // so it won't change during process
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;
            final Sequence sequence = (viewer != null) ? viewer.getSequence() : null;

            if ((sequence != null) && (canvas != null))
            {
                final Sequence seqOut = canvas.getRenderedSequence(false, progressFrame);

                if (seqOut != null)
                {
                    // set sequence name
                    seqOut.setName("Rendering of '" + sequence.getName() + "' view");
                    // add sequence
                    Icy.getMainInterface().addSequence(seqOut);

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveViewer() != null);
        }
    };

    public static IcyAbstractAction unselectAction = new IcyAbstractAction("Unselect", (IcyIcon) null,
            "Unselect layer(s)", KeyEvent.VK_ESCAPE)
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6136680076368815566L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final LayersPanel layersPanel = Icy.getMainInterface().getLayersPanel();

            if (layersPanel != null)
            {
                layersPanel.clearSelected();

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction deleteLayersAction = new IcyAbstractAction("Delete", new IcyIcon(
            ResourceUtil.ICON_DELETE), "Delete selected layer(s)", KeyEvent.VK_DELETE)
    {
        /**
         * 
         */
        private static final long serialVersionUID = 929998190473791930L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            final LayersPanel layersPanel = Icy.getMainInterface().getLayersPanel();

            if ((sequence != null) && (layersPanel != null))
            {
                final List<Layer> layers = layersPanel.getSelectedLayers();

                if (layers.size() > 0)
                {
                    sequence.beginUpdate();
                    try
                    {
                        // delete selected layer
                        for (Layer layer : layers)
                            if (layer.getCanBeRemoved())
                                sequence.removeOverlay(layer.getOverlay());
                    }
                    finally
                    {
                        sequence.endUpdate();
                    }

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            final LayersPanel layersPanel = Icy.getMainInterface().getLayersPanel();
            return super.isEnabled() && (sequence != null) && (layersPanel != null)
                    && (layersPanel.getSelectedLayers().size() > 0);
        }
    };

    public static IcyAbstractAction toggleLayersAction = new ToggleLayersAction();

    /**
     * Return all actions of this class
     */
    public static List<IcyAbstractAction> getAllActions()
    {
        final List<IcyAbstractAction> result = new ArrayList<IcyAbstractAction>();

        for (Field field : CanvasActions.class.getFields())
        {
            final Class<?> type = field.getType();

            try
            {
                if (type.isAssignableFrom(IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (type.isAssignableFrom(IcyAbstractAction.class))
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
