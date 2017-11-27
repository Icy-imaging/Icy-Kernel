/*
 * Copyright 2010-2015 Institut Pasteur.
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
package icy.gui.menu;

import icy.action.GeneralActions;
import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandToggleButton;
import icy.gui.main.MainFrame;
import icy.gui.util.LookAndFeelUtil;
import icy.gui.util.RibbonUtil;
import icy.image.ImageUtil;
import icy.imagej.ImageJWrapper;
import icy.imagej.ImageJWrapper.ImageJActiveImageListener;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.resource.icon.BasicResizableIcon;
import icy.resource.icon.IcyIcon;
import icy.system.SystemUtil;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import org.pushingpixels.flamingo.api.common.CommandToggleButtonGroup;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonComponent;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

import ij.Executer;
import ij.ImageJ;
import ij.WindowManager;
import ij.gui.ImageWindow;

/**
 * @author Stephane
 */
public class ImageJTask extends RibbonTask implements PropertyChangeListener
{
    static final Image detachForIJ = ResourceUtil.getImage("help/ij_detached.jpg");

    private static class ImageJRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4431425194428863269L;

        public static final String NAME = "ImageJ";

        // ImageJ instance
        ImageJWrapper imageJ;

        // internal
        final JRibbonComponent imageJComp;

        public ImageJRibbonBand()
        {
            super(NAME, new BasicResizableIcon(ImageUtil.load(ImageJ.class.getResource("/microscope.gif"))));

            // initialize some static ImageJ stuff

            // home directory
            SystemUtil.setProperty("plugins.dir", "ij");
            // background color
            ImageJ.backgroundColor = LookAndFeelUtil.getBackground(this);

            try
            {
                // create ImageJ wrapper
                imageJ = new ImageJWrapper();
            }
            catch (Throwable t)
            {
                // ImageJ can't initialize --> just display an error message
                System.err.println(t.getMessage());
                imageJ = null;
            }

            if (imageJ != null)
                imageJComp = new JRibbonComponent(imageJ.getSwingPanel());
            else
                imageJComp = new JRibbonComponent(new JPanel());

            // add ImageJ GUI wrapper to ribbon
            addRibbonComponent(imageJComp, 3);

            RibbonUtil.setRestrictiveResizePolicies(this);
        }
    }

    private static class ImageJToolRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6873081018953405306L;

        public static final String NAME = "Tools";

        /**
         * internals
         */
        final IcyCommandButton button;
        final IcyCommandToggleButton detachedBtn;
        final CommandToggleButtonGroup detachedGrp;

        public ImageJToolRibbonBand()
        {
            super(NAME, new IcyIcon("brackets"));

            // convert operation
            button = new IcyCommandButton(GeneralActions.toIJAction);

            addCommandButton(button, RibbonElementPriority.TOP);

            // detach windows button
            detachedBtn = new IcyCommandToggleButton(GeneralActions.detachedModeAction);

            final RichTooltip richToolTip = new RichTooltip("Detached windows",
                    "Icy need to be set in detached mode to use ImageJ efficiently and enable image conversion.");
            richToolTip.setMainImage(detachForIJ);
            richToolTip.addDescriptionSection(
                    "This button has the same effect as the detached mode button in the top toolbar.");
            detachedBtn.setActionRichTooltip(richToolTip);

            detachedGrp = new CommandToggleButtonGroup();
            detachedGrp.add(detachedBtn);

            addCommandButton(detachedBtn, RibbonElementPriority.TOP);

            RibbonUtil.setPermissiveResizePolicies(this);
        }

        public void setActionToIJ()
        {
            button.setAction(GeneralActions.toIJAction);
            updateButtonsState();
        }

        public void setActionToIcy()
        {
            button.setAction(GeneralActions.toIcyAction);
            updateButtonsState();
        }

        public void updateButtonsState()
        {
            final boolean isDetached = Icy.getMainInterface().isDetachedMode();

            detachedGrp.setSelected(detachedBtn, isDetached);

            if (button.getAction() == GeneralActions.toIcyAction)
                button.setEnabled(isDetached && (WindowManager.getCurrentImage() != null));
            else
                button.setEnabled(isDetached && (Icy.getMainInterface().getActiveSequence() != null));
        }
    }

    public static final String NAME = "ImageJ";

    protected final ImageJToolRibbonBand ijToolBand;

    public ImageJTask()
    {
        super(NAME, new ImageJRibbonBand(), new ImageJToolRibbonBand());

        ijToolBand = (ImageJToolRibbonBand) getBand(1);

        getImageJ().addActiveImageListener(new ImageJActiveImageListener()
        {
            @Override
            public void imageActived(ImageWindow iw)
            {
                if (iw != null)
                    ijToolBand.setActionToIcy();
                else if (Icy.getMainInterface().getActiveSequence() != null)
                    ijToolBand.setActionToIJ();
                else
                    ijToolBand.updateButtonsState();
            }
        });
    }

    /**
     * Initialization stuff which cannot be done at construction time
     */
    public void init()
    {
        // refresh band state
        propertyChange(null);

        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
        // we listen detach mode change
        if (mainFrame != null)
            mainFrame.addPropertyChangeListener(MainFrame.PROPERTY_DETACHEDMODE, this);
    }

    /**
     * @return the imageJ
     */
    public ImageJWrapper getImageJ()
    {
        return ((ImageJRibbonBand) getBand(0)).imageJ;
    }

    /**
     * Quit imageJ
     */
    public void quitImageJ()
    {
        new Executer("Quit", null);
    }

    public void onSequenceActivationChange()
    {
        if (Icy.getMainInterface().getActiveSequence() != null)
            ijToolBand.setActionToIJ();
        else if (WindowManager.getCurrentImage() != null)
            ijToolBand.setActionToIcy();
        else
            ijToolBand.updateButtonsState();
    }

    /**
     * Handle {@link MainFrame#PROPERTY_DETACHEDMODE} property change here.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        ijToolBand.updateButtonsState();
    }
}
