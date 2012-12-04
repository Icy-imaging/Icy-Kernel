package icy.searchbar.interfaces;

import java.awt.Image;

import javax.swing.JButton;

import org.pushingpixels.flamingo.api.common.RichTooltip;

/**
 * Defines an item in the SBDisplay table.
 * 
 * @author Thomas Provoost
 */
public abstract class SBLink
{

    private SBProvider provider;

    /**
     * @param provider
     */
    public SBLink(SBProvider provider)
    {
        super();
        this.provider = provider;
    }

    /**
     * Returns the label to be displayed to the user.
     * 
     * @return the label
     */
    public abstract String getLabel();

    /**
     * Returns the image descriptor for this element.
     * 
     * @return an image descriptor, or null if no image is available
     */
    public abstract Image getImage();

    /**
     * @return Returns the provider.
     */
    public SBProvider getProvider()
    {
        return provider;
    }

    /**
     * Executes the associated action for this element.
     */
    public abstract void execute();

    // /**
    // * Get the JWindow used as a Popup associated with the item.
    // *
    // * @return
    // */
    // public abstract JWindow getPopup();
    /**
     * Get the JWindow used as a Popup associated with the item.
     * 
     * @return
     */
    public abstract RichTooltip getRichToolTip();

    /**
     * The right click will trigger the execution of
     * the action in the getActionB()
     * button. The use of a button and not a method is explained for an eventual evolution of the
     * system: in case a button is added in the GUI, it is already created, and does not require
     * more
     * development from Provider developers.
     * 
     * @return
     */
    public abstract JButton getActionB();

    @Override
    public String toString()
    {
        return getLabel();
    }
}
