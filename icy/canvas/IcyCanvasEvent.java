/**
 * 
 */
package icy.canvas;

import icy.common.EventHierarchicalChecker;
import icy.sequence.DimensionId;

/**
 * @author Stephane
 */
public class IcyCanvasEvent implements EventHierarchicalChecker
{
    public enum IcyCanvasEventType
    {
        POSITION_CHANGED, OFFSET_CHANGED, SCALE_CHANGED, ROTATION_CHANGED, MOUSE_IMAGE_POSITION_CHANGED, SYNC_CHANGED;
    }

    private IcyCanvas source;
    private final IcyCanvasEventType type;
    private final DimensionId dim;

    public IcyCanvasEvent(IcyCanvas source, IcyCanvasEventType type, DimensionId dim)
    {
        this.source = source;
        this.type = type;
        this.dim = dim;
    }

    public IcyCanvasEvent(IcyCanvas source, IcyCanvasEventType type)
    {
        this(source, type, DimensionId.NULL);
    }

    /**
     * @return the source
     */
    public IcyCanvas getSource()
    {
        return source;
    }

    /**
     * @return the type
     */
    public IcyCanvasEventType getType()
    {
        return type;
    }

    /**
     * @return the dimension
     */
    public DimensionId getDim()
    {
        return dim;
    }

    @Override
    public boolean isEventRedundantWith(EventHierarchicalChecker event)
    {
        if (event instanceof IcyCanvasEvent)
        {
            final IcyCanvasEvent e = (IcyCanvasEvent) event;

            return (e.getSource() == source) && (e.getType() == type) && (e.getDim() == dim);
        }

        return false;
    }
}
