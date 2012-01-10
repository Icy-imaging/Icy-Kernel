/**
 * 
 */
package icy.gui.menu;

import icy.gui.util.RibbonUtil;
import icy.gui.util.SwingUtil;
import icy.image.ImageUtil;
import icy.resource.icon.BasicResizableIcon;
import icy.system.thread.ThreadUtil;
import ij.Executer;
import ij.ImageJ;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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

        // ImageJ instance
        final ImageJ imageJ;
        final JPanel topPanel;
        JMenuBar menuBar;

        public ImageJRibbonBand()
        {
            super(NAME, new BasicResizableIcon(ImageUtil.loadImage(ImageJ.class.getResource("/microscope.gif"))));

            // set ImageJ home directory
            System.setProperty("plugins.dir", "ij");

            // silent ImageJ instance creation
            imageJ = new ImageJ(ImageJ.NO_SHOW);
            imageJ.addPropertyChangeListener("menu", new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent evt)
                {
                    ThreadUtil.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            updateMenu();
                        }
                    });
                }
            });

            final JPanel panel = new JPanel(new BorderLayout());
            topPanel = new JPanel(new BorderLayout());

            Component ijToolBar = imageJ.getComponent(0);
            Component ijMainPanel = imageJ.getComponent(1);

            // menubar
            menuBar = null;
            updateMenu();
            // toolbar
            topPanel.add(ijToolBar, BorderLayout.CENTER);
            // top
            panel.add(topPanel, BorderLayout.NORTH);
            // main panel
            panel.add(ijMainPanel, BorderLayout.CENTER);

            addRibbonComponent(new JRibbonComponent(panel), 3);

            RibbonUtil.setRestrictiveResizePolicies(this);
        }

        void updateMenu()
        {
            if (menuBar != null)
                topPanel.remove(menuBar);

            // update menu
            menuBar = SwingUtil.getJMenuBar(imageJ.getMenuBar(), true);

            topPanel.add(menuBar, BorderLayout.NORTH);
            topPanel.validate();
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

    /**
     * Quit imageJ
     */
    public void quitImageJ()
    {
        new Executer("Quit", null);
    }
}
