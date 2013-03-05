/**
 * 
 */
package icy.painter;

import icy.common.EventHierarchicalChecker;
import icy.util.StringUtil;

/**
 * @author Stephane
 */
public class OverlayEvent implements EventHierarchicalChecker
{
    public enum OverlayEventType
    {
        PAINTER_CHANGED, PROPERTY_CHANGED;
    }

    private final Overlay source;
    private final OverlayEventType type;
    private String propertyName;

    public OverlayEvent(Overlay source, OverlayEventType type, String propertyName)
    {
        this.source = source;
        this.type = type;
        this.propertyName = propertyName;
    }

    public OverlayEvent(Overlay source, OverlayEventType type)
    {
        this(source, type, null);
    }

    /**
     * @return the source
     */
    public Overlay getSource()
    {
        return source;
    }

    /**
     * @return the type
     */
    public OverlayEventType getType()
    {
        return type;
    }

    /**
     * @return the propertyName
     */
    public String getPropertyName()
    {
        return propertyName;
    }

    private boolean optimizeEventWith(OverlayEvent e)
    {
        if (e.getType() == type)
        {
            if (type == OverlayEventType.PROPERTY_CHANGED)
            {
                // join properties
                if (!StringUtil.equals(e.getPropertyName(), propertyName))
                    propertyName = null;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof OverlayEvent)
            return optimizeEventWith((OverlayEvent) event);

        return false;
    }
}
