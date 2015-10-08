/**
 * 
 */
package icy.roi;

import icy.roi.ROIEvent.ROIEventType;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.util.StringUtil;

/**
 * Abstract class providing the basic methods to retrieve properties and compute a specific
 * descriptor for a region of interest (ROI)
 * 
 * @author Stephane Dallongeville, Alexandre Dufour
 */
public abstract class ROIDescriptor
{
    protected final String id;
    protected final String name;
    protected final Class<?> type;

    /**
     * Create a new {@link ROIDescriptor} with given id, name and type
     */
    protected ROIDescriptor(String id, String name, Class<?> type)
    {
        super();

        this.id = id;
        this.name = name;
        this.type = type;
    }

    /**
     * Create a new {@link ROIDescriptor} with given name and type
     */
    protected ROIDescriptor(String name, Class<?> type)
    {
        this(name, name, type);
    }

    /**
     * Returns the id of this descriptor.<br/>
     * By default it uses the descriptor's name but it can be overridden to be different.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Returns the name of this descriptor.<br/>
     * The name is used as title (column header) in the ROI panel so keep it short and self
     * explanatory.
     */
    public String getName()
    {
        return name;
    };

    /**
     * Returns a single line description (used as tooltip) for this descriptor
     */
    public abstract String getDescription();

    /**
     * Returns the unit of this descriptor (<code>ex: "px", "mm", "µm2"...</code>).</br>
     * It can return an empty or <code>null</code> string (default implementation) if there is no
     * specific unit attached to the descriptor.<br/>
     * Note that unit is concatenated to the name to build the title (column header) in the ROI
     * panel.
     * 
     * @param sequence
     *        the sequence on which we want to compute the descriptor (if required) to get access to
     *        the pixel size informations and return according unit
     */
    public String getUnit(Sequence sequence)
    {
        return null;
    }

    /**
     * Returns the type of result for this descriptor
     * 
     * @see #compute(ROI, Sequence)
     */
    public Class<?> getType()
    {
        return type;
    };

    /**
     * Returns <code>true</code> if this descriptor compute its result on {@link Sequence} data and *per channel* (as
     * pixel intensity information).<br>
     * By default it returns <code>false</code>, override this method if a descriptor require per channel computation.
     * 
     * @see #compute(ROI, Sequence)
     */
    public boolean separateChannel()
    {
        return false;
    }

    /**
     * Returns <code>true</code> if this descriptor need to be recomputed when the specified Sequence change event
     * happen.<br>
     * By default it returns <code>false</code>, override this method if a descriptor need a specific implementation.
     * 
     * @see #compute(ROI, Sequence)
     */
    public boolean needRecompute(SequenceEvent change)
    {
        return false;
    }

    /**
     * Returns <code>true</code> if this descriptor need to be recomputed when the specified ROI change event happen.<br>
     * By default it returns <code>true</code> on ROI content change, override this method if a descriptor need a
     * specific implementation.
     * 
     * @see #compute(ROI, Sequence)
     */
    public boolean needRecompute(ROIEvent change)
    {
        return (change.getType() == ROIEventType.ROI_CHANGED);
    };

    /**
     * Computes the descriptor on the specified ROI and return the result.
     * 
     * @param roi
     *        the ROI on which the descriptor(s) should be computed
     * @param sequence
     *        an optional sequence where the pixel informations can be retrieved (see {@link #separateChannel()})
     * @return the result of this descriptor computed from the specified parameters.
     * @throws UnsupportedOperationException
     *         if the type of the given ROI is not supported by this descriptor, or if <code>sequence</code> is
     *         <code>null</code> while the calculation requires it
     */
    public abstract Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException;

    /*
     * We want a unique id for each {@link ROIDescriptor}
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ROIDescriptor)
            return StringUtil.equals(((ROIDescriptor) obj).getId(), getId());

        return super.equals(obj);
    }

    /*
     * We want a unique id for each {@link ROIDescriptor}
     */
    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }
}