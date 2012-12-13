/**
 * 
 */
package icy.gui.menu.action;

import icy.common.IcyAbstractAction;
import icy.gui.frame.AboutFrame;
import icy.gui.viewer.Viewer;
import icy.imagej.ImageJUtil;
import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.plugin.PluginUpdater;
import icy.preferences.GeneralPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.update.IcyUpdater;
import ij.ImagePlus;
import ij.WindowManager;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * General actions.
 * 
 * @author Stephane
 */
public class GeneralActions
{
    public static IcyAbstractAction exitApplicationAction = new IcyAbstractAction("Exit", new IcyIcon(
            ResourceUtil.ICON_ON_OFF))
    {
        /**
         * 
         */
        private static final long serialVersionUID = -3238298900158332179L;

        @Override
        public void doAction(ActionEvent e)
        {
            Icy.exit(false);
        }
    };

    public static IcyAbstractAction detachedModeAction = new IcyAbstractAction("Detached Mode", new IcyIcon(
            ResourceUtil.ICON_DETACHED_WINDOW), "Detached mode ON/OFF",
            "Switch application to detached / attached mode")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 6632773548066123185L;

        @Override
        public void doAction(ActionEvent e)
        {
            final boolean value = !Icy.getMainInterface().isDetachedMode();

            // set detached mode
            Icy.getMainInterface().setDetachedMode(value);
            // and save state
            GeneralPreferences.setMultiWindowMode(value);
        }
    };

    public static IcyAbstractAction toIJAction = new IcyAbstractAction("Convert to IJ", new IcyIcon(
            ResourceUtil.ICON_TOIJ), "Convert to ImageJ", "Convert the selected Icy sequence to ImageJ image.", true,
            "Converting to ImageJ image...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -5506310360653637920L;

        @Override
        public void doAction(ActionEvent e)
        {
            final Sequence seq = Icy.getMainInterface().getFocusedSequence();

            if (seq != null)
            {
                final ImagePlus ip = ImageJUtil.convertToImageJImage(seq, getProgressFrame());

                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // show the image
                        ip.show();
                    }
                });
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (Icy.getMainInterface().getFocusedSequence() != null);
        }
    };

    public static IcyAbstractAction toIcyAction = new IcyAbstractAction("Convert to Icy", new IcyIcon(
            ResourceUtil.ICON_TOICY), "Convert to Icy", "Convert the selected ImageJ image to Icy sequence.", true,
            "Converting to Icy image...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5713619465058087088L;

        @Override
        public void doAction(ActionEvent e)
        {
            final ImagePlus ip = WindowManager.getCurrentImage();

            if (ip != null)
            {
                final Sequence seq = ImageJUtil.convertToIcySequence(ip, getProgressFrame());

                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // show the sequence
                        new Viewer(seq);
                    }
                });
            }
        }

        @Override
        public boolean isEnabled()
        {
            return !processing && (WindowManager.getCurrentImage() != null);
        }
    };

    public static IcyAbstractAction onlineHelpAction = new IcyAbstractAction("Online help", new IcyIcon(
            ResourceUtil.ICON_HELP))
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8702011381533907199L;

        @Override
        public void doAction(ActionEvent e)
        {
            // open browser on help page
            NetworkUtil.openURL(NetworkUtil.WEBSITE_URL + "support");
        }
    };

    public static IcyAbstractAction websiteAction = new IcyAbstractAction("Website", new IcyIcon(
            ResourceUtil.ICON_BROWSER))
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4447276299627488427L;

        @Override
        public void doAction(ActionEvent e)
        {
            // open browser on help page
            NetworkUtil.openURL(NetworkUtil.WEBSITE_URL);
        }
    };

    public static IcyAbstractAction checkUpdateAction = new IcyAbstractAction("Check for update", new IcyIcon(
            ResourceUtil.ICON_DOWNLOAD), "Check for updates",
            "Search updates for application and plugins in all referenced repositories.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5070966391369409880L;

        @Override
        public void doAction(ActionEvent e)
        {
            // check core update
            IcyUpdater.checkUpdate(true, false);
            // check plugin update
            PluginUpdater.checkUpdate(true, false);
        }
    };

    public static IcyAbstractAction aboutAction = new IcyAbstractAction("About", new IcyIcon(ResourceUtil.ICON_INFO),
            "About Icy", "Information about ICY's authors, license and copyrights.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 2564352020620899851L;

        @Override
        public void doAction(ActionEvent e)
        {
            new AboutFrame();
        }
    };

    static
    {
        toIJAction.getRichToolTip().addFooterSection("Icy needs to be in detached mode to enabled this feature.");
        toIcyAction.getRichToolTip().addFooterSection("Icy needs to be in detached mode to enabled this feature.");
    }

    /**
     * Return all actions of this class
     */
    public static List<IcyAbstractAction> getAllActions()
    {
        final List<IcyAbstractAction> result = new ArrayList<IcyAbstractAction>();

        for (Field field : GeneralActions.class.getFields())
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
