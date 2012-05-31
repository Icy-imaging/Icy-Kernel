package icy.gui.component;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

public class SequencePreviewPanel extends JPanel
{
    public interface SequencePreviewImageProvider
    {
        public Image getImage(int t, int z);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 4985194381532600393L;

    private class CustomPanel extends JPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = 6307431557815572470L;

        public CustomPanel()
        {
            super();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            g.drawImage(getImage(), 0, 0, getWidth(), getHeight(), null);
        }
    }

    private JSlider tSlider;
    private JSlider zSlider;
    private SequencePreviewImageProvider provider;

    /**
     * Create the panel.
     */
    public SequencePreviewPanel()
    {
        super();

        provider = null;

        initializeGui();
    }

    private void initializeGui()
    {
        setLayout(new BorderLayout(0, 0));

        tSlider = new JSlider(SwingConstants.HORIZONTAL);
        add(tSlider, BorderLayout.SOUTH);

        zSlider = new JSlider(SwingConstants.VERTICAL);
        add(zSlider, BorderLayout.WEST);

        add(new CustomPanel(), BorderLayout.CENTER);
    }

    public boolean isZSliderVisible()
    {
        return zSlider.isVisible();
    }

    public boolean isTSliderVisible()
    {
        return tSlider.isVisible();
    }

    public void setZSliderVisible(boolean value)
    {
        zSlider.setVisible(value);
    }

    public void setTSliderVisible(boolean value)
    {
        tSlider.setVisible(value);
    }

    public void setZRange(int min, int max)
    {
        zSlider.setMinimum(min);
        zSlider.setMaximum(max);
    }

    public void setTRange(int min, int max)
    {
        tSlider.setMinimum(min);
        tSlider.setMaximum(max);
    }

    public void setImageProvider(SequencePreviewImageProvider provider)
    {
        this.provider = provider;
    }

    Image getImage()
    {
        if (provider == null)
            return null;

        return provider.getImage(isZSliderVisible() ? zSlider.getValue() : 0, isTSliderVisible() ? tSlider.getValue()
                : 0);
    }
}
