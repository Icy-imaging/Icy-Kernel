/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.action;

import icy.clipboard.Clipboard;
import icy.clipboard.TransferableImage;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.frame.AboutFrame;
import icy.gui.main.MainFrame;
import icy.gui.menu.search.SearchBar;
import icy.gui.viewer.Viewer;
import icy.image.ImageUtil;
import icy.imagej.ImageJUtil;
import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.plugin.PluginUpdater;
import icy.preferences.GeneralPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.system.audit.Audit;
import icy.system.thread.ThreadUtil;
import icy.update.IcyUpdater;
import ij.ImagePlus;
import ij.WindowManager;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pushingpixels.flamingo.api.common.RichTooltip;

/**
 * General actions.
 * 
 * @author Stephane
 */
public class GeneralActions
{
    public static IcyAbstractAction searchAction = new IcyAbstractAction("Search",
            new IcyIcon(ResourceUtil.ICON_SEARCH), "Application search tool", KeyEvent.VK_F,
            SystemUtil.getMenuCtrlMask())
    {
        /**
         * 
         */
        private static final long serialVersionUID = -7457421618693984393L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final MainFrame mf = Icy.getMainInterface().getMainFrame();

            if (mf != null)
            {
                final SearchBar sb = mf.getSearchBar();

                if (sb != null)
                {
                    sb.setFocus();
                    return true;
                }
            }
            return false;
        }
    };

    public static IcyAbstractAction exitApplicationAction = new IcyAbstractAction("Exit", new IcyIcon(
            ResourceUtil.ICON_ON_OFF))
    {
        /**
         * 
         */
        private static final long serialVersionUID = -3238298900158332179L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            Icy.exit(false);
            return true;
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
        public boolean doAction(ActionEvent e)
        {
            final boolean value = !Icy.getMainInterface().isDetachedMode();

            // set detached mode
            Icy.getMainInterface().setDetachedMode(value);
            // and save state
            GeneralPreferences.setMultiWindowMode(value);

            return true;
        }
    };

    public static IcyAbstractAction copyImageAction = new IcyAbstractAction("Copy image", new IcyIcon(
            ResourceUtil.ICON_PICTURE_COPY), "Copy image to clipboard",
            "Copy the active image to the system clipboard.", KeyEvent.VK_C, SystemUtil.getMenuCtrlMask(), true,
            "Copying image to the clipboard...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 8181120519734955113L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null)
            {
                final Sequence seq = viewer.getSequence();

                if (seq != null)
                {
                    try
                    {
                        final BufferedImage img = viewer.getRenderedImage(viewer.getPositionT(), viewer.getPositionZ(),
                                viewer.getPositionC(), false);

                        // put image in system clipboard
                        Clipboard.putSystem(new TransferableImage(img), null);
                        // clear content of Icy clipboard
                        Clipboard.clear();

                        return true;
                    }
                    catch (Throwable e1)
                    {
                        System.err.println("Can't copy image to clipboard:");
                        IcyExceptionHandler.showErrorMessage(e1, false);
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static IcyAbstractAction pasteImageAction = new IcyAbstractAction("Paste image", new IcyIcon(
            ResourceUtil.ICON_PICTURE_PASTE), "Paste image from clipboard",
            "Paste image from the system clipboard in a new sequence.", KeyEvent.VK_V, SystemUtil.getMenuCtrlMask(),
            true, "Creating new sequence from clipboard image...")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 8181120519734955113L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            if (Clipboard.hasTypeSystem(DataFlavor.imageFlavor))
            {
                try
                {
                    final Image img = (Image) Clipboard.getSystem(DataFlavor.imageFlavor);
                    Icy.getMainInterface().addSequence(new Sequence("Clipboard image", ImageUtil.toBufferedImage(img)));
                    return true;
                }
                catch (Throwable e1)
                {
                    System.err.println("Can't paste image from clipboard:");
                    IcyExceptionHandler.showErrorMessage(e1, false);
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && Clipboard.hasTypeSystem(DataFlavor.imageFlavor);
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
        public boolean doAction(ActionEvent e)
        {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            if (seq != null)
            {
                final ImagePlus ip = ImageJUtil.convertToImageJImage(seq, progressFrame);

                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // show the image
                        ip.show();
                    }
                });

                return true;
            }

            return false;
        }

        @Override
        public RichTooltip getRichToolTip()
        {
            final RichTooltip result = super.getRichToolTip();

            result.addFooterSection("Icy needs to be in detached mode to enabled this feature.");

            return result;
        };

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
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
        public boolean doAction(ActionEvent e)
        {
            final ImagePlus ip = WindowManager.getCurrentImage();

            if (ip != null)
            {
                final Sequence seq = ImageJUtil.convertToIcySequence(ip, progressFrame);

                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // show the sequence
                        new Viewer(seq);
                    }
                });

                return true;
            }

            return false;
        }

        @Override
        public RichTooltip getRichToolTip()
        {
            final RichTooltip result = super.getRichToolTip();

            result.addFooterSection("Icy needs to be in detached mode to enabled this feature.");

            return result;
        };

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && (WindowManager.getCurrentImage() != null);
        }
    };

    public static IcyAbstractAction onlineHelpAction = new IcyAbstractAction("Online help (F1)", new IcyIcon(
            ResourceUtil.ICON_HELP), "Open a browser and display support forum", KeyEvent.VK_F1)
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8702011381533907199L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            // open browser on help page
            NetworkUtil.openBrowser(NetworkUtil.WEBSITE_URL + "support");
            return true;
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
        public boolean doAction(ActionEvent e)
        {
            // open browser on help page
            NetworkUtil.openBrowser(NetworkUtil.WEBSITE_URL);
            return true;
        }
    };

    public static IcyAbstractAction linkAction = new IcyAbstractAction("Link", new IcyIcon(ResourceUtil.ICON_LINK),
            "Link / unlink online user account",
            "Link / unlink with online user account.\nGive access to extra features as plugin rating")
    {

        /**
         * 
         */
        private static final long serialVersionUID = 3449298011169150396L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            // open browser on help page
            if (Audit.isUserLinked())
            {
                // ask for confirmation
                if (!Icy.getMainInterface().isHeadLess()
                        && !ConfirmDialog.confirm("Do you want to unlink user account ?"))
                    return false;

                // unlink user
                Audit.unlinkUser();
            }
            else
            {
                // update link first
                Audit.updateUserLink();

                // still not linked --> link user
                if (!Audit.isUserLinked())
                    Audit.linkUser();
            }

            // refresh user infos
            final MainFrame frame = Icy.getMainInterface().getMainFrame();
            if (frame != null)
                frame.refreshUserInfos();

            return true;
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
        public boolean doAction(ActionEvent e)
        {
            // check core update
            if (!IcyUpdater.isCheckingForUpdate())
                IcyUpdater.checkUpdate(false);
            // check plugin update
            if (!PluginUpdater.isCheckingForUpdate())
                PluginUpdater.checkUpdate(false);

            return true;
        }

        @Override
        public boolean isEnabled()
        {
            return super.isEnabled() && !(IcyUpdater.isCheckingForUpdate() || PluginUpdater.isCheckingForUpdate());
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
        public boolean doAction(ActionEvent e)
        {
            new AboutFrame(0);
            return true;
        }
    };

    public static IcyAbstractAction changeLogAction = new IcyAbstractAction("ChangeLog", new IcyIcon("notepad_2.png"),
            "ChangeLog", "See the changelog informations.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 2564352020620899851L;

        @Override
        public boolean doAction(ActionEvent e)
        {
            new AboutFrame(1);
            return true;
        }
    };

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
