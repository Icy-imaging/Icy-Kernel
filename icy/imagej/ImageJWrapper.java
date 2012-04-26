/**
 * 
 */
package icy.imagej;

import icy.gui.main.MainFrame;
import icy.gui.util.SwingUtil;
import icy.main.Icy;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;
import icy.util.ReflectionUtil;
import ij.ImageJ;
import ij.gui.ImageWindow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuBar;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * ImageJ Wrapper class.
 * 
 * @author Stephane
 */
public class ImageJWrapper extends ImageJ
{
    /**
     * 
     */
    private static final long serialVersionUID = -4361946959228494782L;

    public interface ImageJActiveImageListener
    {
        public void imageActived(ImageWindow iw);

    }

    public final static String PROPERTY_MENU = "menu";

    /**
     * swing GUI
     */
    final JPanel swingPanel;
    JMenuBar swingMenuBar;
    final JPanel swingStatusPanel;
    final JLabel swingStatusLabel;
    final JProgressBar swingProgressBar;
    ToolbarWrapper swingToolBar;

    /**
     * internal
     */
    final ArrayList<ImageJActiveImageListener> listeners;

    public ImageJWrapper()
    {
        // silent creation
        super(ImageJ.NO_SHOW);

        swingPanel = new JPanel();
        swingPanel.setLayout(new BorderLayout());

        // menubar
        swingMenuBar = null;
        updateMenu();

        // toolbar
        swingToolBar = new ToolbarWrapper(this);
        swingPanel.add(swingToolBar.getSwingComponent(), BorderLayout.CENTER);

        try
        {
            // patch imageJ toolbar to uses our wrapper
            ReflectionUtil.getField(this, "toolbar", true).set(this, swingToolBar);
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, false);
            System.err.println("Cannot install ImageJ toolbar wrapper");
        }

        // status bar
        swingStatusPanel = new JPanel();
        swingStatusPanel.setLayout(new BorderLayout());

        swingStatusLabel = new JLabel();
        swingStatusLabel.setPreferredSize(new Dimension(420, 24));
        swingStatusLabel.setMaximumSize(new Dimension(420, 24));
        swingStatusLabel.setFocusable(true);
        swingStatusLabel.addKeyListener(this);
        swingStatusLabel.addMouseListener(this);
        swingStatusPanel.add(swingStatusLabel, BorderLayout.CENTER);
        swingProgressBar = new JProgressBar(0, 1000);
        swingProgressBar.setBorder(BorderFactory.createEmptyBorder());
        swingProgressBar.setFocusable(true);
        swingProgressBar.setPreferredSize(new Dimension(120, 20));
        swingProgressBar.setMaximumSize(new Dimension(120, 20));
        swingProgressBar.setValue(1000);
        swingProgressBar.addKeyListener(this);
        swingProgressBar.addMouseListener(this);
        swingStatusPanel.add(swingProgressBar, BorderLayout.EAST);

        swingPanel.add(swingStatusPanel, BorderLayout.SOUTH);

        swingPanel.validate();

        listeners = new ArrayList<ImageJWrapper.ImageJActiveImageListener>();
    }

    void updateMenu()
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (swingPanel != null)
                {
                    if (swingMenuBar != null)
                        swingPanel.remove(swingMenuBar);

                    // update menu
                    swingMenuBar = SwingUtil.getJMenuBar(getMenuBar(), true);

                    swingPanel.add(swingMenuBar, BorderLayout.NORTH);
                    swingPanel.validate();
                }
            }
        });
    }
    
    public void setActiveImage(ImageWindow iw)
    {
        // notify active image changed
        fireActiveImageChanged(iw);
    }

    public void showSwingProgress(final int currentIndex, final int finalIndex)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                final BoundedRangeModel model = swingProgressBar.getModel();

                model.setMaximum(finalIndex);
                model.setValue(currentIndex);
            }
        });
    }

    public void showSwingStatus(String s)
    {
        swingStatusLabel.setText(s);
    }

    /**
     * @return the Swing menuBar
     */
    public JMenuBar getSwingMenuBar()
    {
        return swingMenuBar;
    }

    /**
     * @return the swingPanel
     */
    public JPanel getSwingPanel()
    {
        return swingPanel;
    }

    /**
     * @return the Swing statusPanel
     */
    public JPanel getSwingStatusPanel()
    {
        return swingStatusPanel;
    }

    /**
     * @return the Swing statusLabel
     */
    public JLabel getSwingStatusLabel()
    {
        return swingStatusLabel;
    }

    /**
     * @return the Swing progressBar
     */
    public JProgressBar getSwingProgressBar()
    {
        return swingProgressBar;
    }

    /**
     * ICY integration
     */
    public void menuChanged()
    {
        // rebuild menu
        updateMenu();
    }

    /**
     * ICY integration
     */
    @Override
    public Point getLocationOnScreen()
    {
        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

        if (mainFrame != null)
            return mainFrame.getLocationOnScreen();

        return new Point(0, 0);
    }

    @Override
    public void setMenuBar(MenuBar mb)
    {
        super.setMenuBar(mb);
        menuChanged();
    }

    @Override
    public Image createImage(int width, int height)
    {
        return swingPanel.createImage(width, height);
    }
    
    public void addActiveImageListener(ImageJActiveImageListener listener)
    {
        listeners.add(listener);
    }

    public void removeActiveImageListener(ImageJActiveImageListener listener)
    {
        listeners.remove(listener);
    }

    public void fireActiveImageChanged(ImageWindow iw)
    {
        for (ImageJActiveImageListener listener : listeners)
            listener.imageActived(iw);
    }
}
