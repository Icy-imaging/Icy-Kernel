/**
 * 
 */
package icy.gui.menu;

import icy.gui.component.button.IcyCommandButton;
import icy.gui.component.button.IcyCommandToggleButton;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.main.MainFrame;
import icy.gui.util.LookAndFeelUtil;
import icy.gui.util.RibbonUtil;
import icy.gui.viewer.Viewer;
import icy.image.ImageUtil;
import icy.imagej.ImageJUtil;
import icy.imagej.ImageJWrapper;
import icy.imagej.ImageJWrapper.ImageJActiveImageListener;
import icy.main.Icy;
import icy.preferences.GeneralPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.BasicResizableIcon;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import ij.Executer;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pushingpixels.flamingo.api.common.CommandToggleButtonGroup;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonComponent;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

/**
 * @author Stephane
 */
public class ImageJTask extends RibbonTask implements PropertyChangeListener
{
    static final BufferedImage detachForIJ = ResourceUtil.getImage("help/ij_detached.jpg");

    private static class ImageJRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4431425194428863269L;

        public static final String NAME = "ImageJ";

        // ImageJ instance
        final ImageJWrapper imageJ;

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

            // create ImageJ wrapper
            imageJ = new ImageJWrapper();
            imageJComp = new JRibbonComponent(imageJ.getSwingPanel());

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

        boolean toIJ;
        final ActionListener toIJAction;
        final ActionListener toICYAction;

        public ImageJToolRibbonBand()
        {
            super(NAME, new IcyIcon("brackets"));

            // action to convert to IJ image
            toIJAction = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final Sequence seq = Icy.getMainInterface().getFocusedSequence();

                            if (seq != null)
                            {
                                final ProgressFrame pf = new ProgressFrame("Converting to ImageJ image...");
                                final ImagePlus ip = ImageJUtil.convertToImageJImage(seq, pf);
                                pf.close();

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
                    });
                }
            };

            // action to convert to ICY sequence
            toICYAction = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ThreadUtil.bgRun(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final ImagePlus ip = WindowManager.getCurrentImage();

                            if (ip != null)
                            {
                                final ProgressFrame pf = new ProgressFrame("Converting to ImageJ image...");
                                final Sequence seq = ImageJUtil.convertToIcySequence(ip, pf);
                                pf.close();

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
                    });
                }
            };

            // convert operation
            button = new IcyCommandButton("Convert to ImageJ", new IcyIcon("to_ij"));

            toIJ = false;
            updateAction(true);

            addCommandButton(button, RibbonElementPriority.TOP);

            final RichTooltip richToolTip = new RichTooltip("Detached windows",
                    "Icy need to be set in detached mode to use ImageJ efficiently and enable image conversion.");
            richToolTip.setMainImage(detachForIJ);
            richToolTip
                    .addDescriptionSection("This button has the same effect as the detached mode button in the top toolbar.");

            // detach windows button
            detachedBtn = new IcyCommandToggleButton("Detached mode", new IcyIcon(ResourceUtil.ICON_DETACHED_WINDOW));
            detachedBtn.setActionRichTooltip(richToolTip);
            detachedBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final boolean value = (detachedGrp.getSelected() == detachedBtn);

                    // set detached mode
                    Icy.getMainInterface().setDetachedMode(value);
                    // and save state
                    GeneralPreferences.setMultiWindowMode(value);
                }
            });

            detachedGrp = new CommandToggleButtonGroup();
            detachedGrp.add(detachedBtn);

            addCommandButton(detachedBtn, RibbonElementPriority.TOP);

            RibbonUtil.setPermissiveResizePolicies(this);
        }

        public void updateAction(boolean ij)
        {
            if (toIJ != ij)
            {
                final RichTooltip toolTip = new RichTooltip();
                toIJ = ij;

                if (ij)
                {
                    toolTip.setTitle("Convert to ImageJ");
                    toolTip.addDescriptionSection("Convert the selected Icy sequence to ImageJ image.");
                    toolTip.addFooterSection("Icy needs to be in detached mode to enabled this feature.");

                    // convert to IJ image
                    button.setText("Convert to IJ");
                    // button.setIcon(new IcyIcon("icon_icy_ij_2", false));
                    button.setIcon(new IcyIcon("to_ij", true));
                    button.setActionRichTooltip(toolTip);
                    button.removeActionListener(toICYAction);
                    button.addActionListener(toIJAction);
                }
                else
                {
                    toolTip.setTitle("Convert to Icy");
                    toolTip.addDescriptionSection("Convert the selected ImageJ image to Icy sequence.");
                    toolTip.addFooterSection("Icy needs to be in detached mode to enabled this feature.");

                    // convert to ICY sequence
                    button.setText("Convert to Icy");
                    // button.setIcon(new IcyIcon("icon_ij_icy_2", false));
                    button.setIcon(new IcyIcon("to_icy", true));
                    button.setActionRichTooltip(toolTip);
                    button.removeActionListener(toIJAction);
                    button.addActionListener(toICYAction);
                }
            }
        }

        public void updateEnable(boolean value)
        {
            button.setEnabled(value);
        }

        public void setDetachedBtnPressed(boolean value)
        {
            detachedGrp.setSelected(detachedBtn, value);
        }
    }

    public static final String NAME = "ImageJ";

    public ImageJTask()
    {
        super(NAME, new ImageJRibbonBand(), new ImageJToolRibbonBand());

        getImageJ().addActiveImageListener(new ImageJActiveImageListener()
        {
            @Override
            public void imageActived(ImageWindow iw)
            {
                ((ImageJToolRibbonBand) getBand(1)).updateAction(iw == null);
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

    /**
     * Handle {@link MainFrame#PROPERTY_DETACHEDMODE} property change here.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        final boolean isDetached = Icy.getMainInterface().isDetachedMode();
        final ImageJToolRibbonBand band = (ImageJToolRibbonBand) getBand(1);

        band.setDetachedBtnPressed(isDetached);
        band.updateEnable(isDetached);
    }

}
