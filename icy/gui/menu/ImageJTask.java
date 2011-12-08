/**
 * 
 */
package icy.gui.menu;

import icy.gui.util.RibbonUtil;
import icy.gui.util.SwingUtil;
import icy.image.ImageUtil;
import icy.resource.icon.BasicResizableIcon;
import ij.ImageJ;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonComponent;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

/**
 * @author Stephane
 */
public class ImageJTask extends RibbonTask
{
    private final ImageJ imageJ;

    private static class ImageJRibbonBand extends JRibbonBand
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4431425194428863269L;

        public static final String NAME = "ImageJ";

        public final ImageJ imageJ;

        public ImageJRibbonBand()
        {
            super(NAME, new BasicResizableIcon(ImageUtil.loadImage(ImageJ.class.getResource("/microscope.gif"))));

            // silent ImageJ instance creation
            imageJ = new ImageJ(ImageJ.NO_SHOW);

            final JPanel panel = new JPanel(new BorderLayout());
            final JPanel topPanel = new JPanel(new BorderLayout());

            JMenuBar menuBar = SwingUtil.getJMenuBar(imageJ.getMenuBar(), true);
            Component ijToolBar = imageJ.getComponent(0);
            Component ijMainPanel = imageJ.getComponent(1);

            // menubar
            topPanel.add(menuBar, BorderLayout.NORTH);
            // toolbar
            topPanel.add(ijToolBar, BorderLayout.CENTER);
            // top
            panel.add(topPanel, BorderLayout.NORTH);
            // main panel
            panel.add(ijMainPanel, BorderLayout.CENTER);

            addRibbonComponent(new JRibbonComponent(panel), 3);

            RibbonUtil.setRestrictiveResizePolicies(this);
        }
    }

    public static final String NAME = "ImageJ";

    public ImageJTask()
    {
        super(NAME, new ImageJRibbonBand());

        // save ImageJ reference
        imageJ = ((ImageJRibbonBand) getBand(0)).imageJ;
    }

    /**
     * @return the imageJ
     */
    public ImageJ getImageJ()
    {
        return imageJ;
    }
}
