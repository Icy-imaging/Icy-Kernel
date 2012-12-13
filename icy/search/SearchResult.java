package icy.search;

import java.awt.Image;

import org.pushingpixels.flamingo.api.common.RichTooltip;

/**
 * Defines an item in the SearchResultPanel.
 * 
 * @author Thomas Provoost & Stephane Dallongeville
 */
public abstract class SearchResult
{
    private final SearchResultProducer producer;

    public SearchResult(SearchResultProducer producer)
    {
        super();

        this.producer = producer;
    }

    /**
     * @return Returns the producer.
     */
    public SearchResultProducer getProducer()
    {
        return producer;
    }

    /**
     * Returns the title of the result.
     */
    public abstract String getTitle();

    /**
     * Returns the image of the result (can be null).
     */
    public abstract Image getImage();

    /**
     * Returns the description of the result.
     */
    public abstract String getDescription();

    /**
     * Returns the tooltip that will be displayed for this result.
     */
    public abstract String getTooltip();

    /**
     * Returns enabled state of the result.
     */
    public boolean isEnabled()
    {
        return true;
    }

    // /**
    // * Returns the JLabel component used to display result in result table.
    // */
    // public JLabel getLabel()
    // {
    // final JLabel result = new JLabel();
    //
    // final Icon icon = getIcon();
    // final String title = getTitle();
    // final String description = getDescription();
    // final String tooltip = getTooltip();
    // String text;
    //
    // if (icon != null)
    // result.setIcon(icon);
    // if (StringUtil.isEmpty(title))
    // text = "Unknow";
    // else
    // text = title;
    // if (!StringUtil.isEmpty(description))
    // text = text + "<br>" + description;
    // result.setText(text);
    // if (!StringUtil.isEmpty(tooltip))
    // result.setToolTipText(tooltip);
    //
    // return result;
    // }

    /**
     * Executes the associated action for this result.
     */
    public abstract void execute();

    /**
     * Executes the associated alternate action (right mouse button) for this result.
     */
    public abstract void executeAlternate();

    // /**
    // * Get the JWindow used as a Popup associated with the item.
    // *
    // * @return
    // */
    // public abstract JWindow getPopup();
    /**
     * Get the RichTooltip associated to the result.
     */
    public abstract RichTooltip getRichToolTip();

    // /**
    // * The right click will trigger the execution of
    // * the action in the getActionB()
    // * button. The use of a button and not a method is explained for an eventual evolution of the
    // * system: in case a button is added in the GUI, it is already created, and does not require
    // * more
    // * development from Provider developers.
    // *
    // * @return
    // */
    // public abstract JButton getActionB();
}
