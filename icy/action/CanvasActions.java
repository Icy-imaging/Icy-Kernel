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
import java.awt.event.InputEvent;
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

    public static IcyAbstractAction globalDisableSyncAction = new IcyAbstractAction("Disabled (all)", new IcyIcon(
            ResourceUtil.ICON_LOCK_OPEN), "Synchronization disabled on all viewers", KeyEvent.VK_0,
            InputEvent.SHIFT_MASK)
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8167090991290743018L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            Icy.getMainInterface().setGlobalViewSyncId(0);

            return true;
        }
    };

    public static IcyAbstractAction globalSyncGroup1Action = new IcyAbstractAction("Group 1 (all)", new IcyIcon(
            ResourceUtil.getLockedImage(1)), "All viewers set to full synchronization group 1 (view and Z/T position)",
            KeyEvent.VK_1, InputEvent.SHIFT_MASK)
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2303919386920010513L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            Icy.getMainInterface().setGlobalViewSyncId(1);

            return true;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static IcyAbstractAction globalSyncGroup2Action = new IcyAbstractAction("Group 2 (all)", new IcyIcon(
            ResourceUtil.getLockedImage(2)), "All viewers set to full synchronization group 2 (view and Z/T position)",
            KeyEvent.VK_2, InputEvent.SHIFT_MASK)
    {
        /**
         * 
         */
        private static final long serialVersionUID = 3238069599592469829L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            Icy.getMainInterface().setGlobalViewSyncId(2);

            return true;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static IcyAbstractAction globalSyncGroup3Action = new IcyAbstractAction("Group 3 (all)", new IcyIcon(
            ResourceUtil.getLockedImage(3)),
            "All viewers set to view synchronization group (view synched but not Z/T position)", KeyEvent.VK_3,
            InputEvent.SHIFT_MASK)
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6943970700811154609L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            Icy.getMainInterface().setGlobalViewSyncId(3);

            return true;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static IcyAbstractAction globalSyncGroup4Action = new IcyAbstractAction("Group 4 (all)", new IcyIcon(
            ResourceUtil.getLockedImage(4)),
            "All viewers set to navigation synchronization group (Z/T position synched but not view)", KeyEvent.VK_4,
            InputEvent.SHIFT_MASK)
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4861151153688280102L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            Icy.getMainInterface().setGlobalViewSyncId(4);

            return true;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static IcyAbstractAction disableSyncAction = new IcyAbstractAction("disabled", new IcyIcon(
            ResourceUtil.ICON_LOCK_OPEN), "Synchronization disabled (global)", KeyEvent.VK_0)
    {
        /**
         * 
         */
        private static final long serialVersionUID = -5275762712812447215L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;

            if (canvas != null)
            {
                canvas.setSyncId(0);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static IcyAbstractAction syncGroup1Action = new IcyAbstractAction("Group 1", new IcyIcon(
            ResourceUtil.getLockedImage(1)), "Full synchronization group 1 (view and Z/T position)", KeyEvent.VK_1)
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5469991474868966986L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;

            if (canvas != null)
            {
                // already set --> remove it
                if (canvas.getSyncId() == 1)
                    canvas.setSyncId(0);
                else
                    canvas.setSyncId(1);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static IcyAbstractAction syncGroup2Action = new IcyAbstractAction("Group 2", new IcyIcon(
            ResourceUtil.getLockedImage(2)), "Full synchronization group 2 (view and Z/T position)", KeyEvent.VK_2)
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8000162851973321503L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;

            if (canvas != null)
            {
                // already set --> remove it
                if (canvas.getSyncId() == 2)
                    canvas.setSyncId(0);
                else
                    canvas.setSyncId(2);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static IcyAbstractAction syncGroup3Action = new IcyAbstractAction("Group 3", new IcyIcon(
            ResourceUtil.getLockedImage(3)), "View synchronization group (view synched but not Z/T position)",
            KeyEvent.VK_3)
    {
        /**
         * 
         */
        private static final long serialVersionUID = 2131076522855333994L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;

            if (canvas != null)
            {
                // already set --> remove it
                if (canvas.getSyncId() == 3)
                    canvas.setSyncId(0);
                else
                    canvas.setSyncId(3);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

    public static IcyAbstractAction syncGroup4Action = new IcyAbstractAction("Group 4", new IcyIcon(
            ResourceUtil.getLockedImage(4)), "Navigation synchronization group (Z/T position synched but not view)",
            KeyEvent.VK_4)
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7921163331144086906L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;

            if (canvas != null)
            {
                // already set --> remove it
                if (canvas.getSyncId() == 4)
                    canvas.setSyncId(0);
                else
                    canvas.setSyncId(4);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();
            return super.isEnabled() && (viewer != null);
        }
    };

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
