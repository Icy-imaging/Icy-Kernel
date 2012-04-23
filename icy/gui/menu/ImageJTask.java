/**
 * 
 */
package icy.gui.menu;

import icy.gui.util.LookAndFeelUtil;
import icy.gui.util.RibbonUtil;
import icy.image.ImageUtil;
import icy.imagej.ImageJWrapper;
import icy.resource.icon.BasicResizableIcon;
import ij.Executer;
import ij.ImageJ;

import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonComponent;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

/**
 * @author Stephane
 */
public class ImageJTask extends RibbonTask
{
    private static class ImageJRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4431425194428863269L;

        public static final String NAME = "ImageJ";

        // ImageJ instance
        final ImageJWrapper imageJ;

        public ImageJRibbonBand()
        {
            super(NAME, new BasicResizableIcon(ImageUtil.loadImage(ImageJ.class.getResource("/microscope.gif"))));

            // initialize some static ImageJ stuff
            
            // home directory
            System.setProperty("plugins.dir", "ij");
            // background color
            ImageJ.backgroundColor = LookAndFeelUtil.getBackground(this);

            // create ImageJ wrapper
            imageJ = new ImageJWrapper();

            // add ImageJ GUI wrapper to ribbon
            addRibbonComponent(new JRibbonComponent(imageJ.getSwingPanel()), 3);

            RibbonUtil.setRestrictiveResizePolicies(this);
        }
    }

    public static final String NAME = "ImageJ";

    public ImageJTask()
    {
        super(NAME, new ImageJRibbonBand());
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
}
