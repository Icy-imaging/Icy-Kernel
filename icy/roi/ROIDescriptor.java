/**
 * 
 */
package icy.roi;

import icy.sequence.Sequence;
import icy.util.StringUtil;

/**
 * Abstract class providing the basic methods to retrieve properties and compute a specific
 * descriptor for a region of interest (ROI)
 * 
 * @author Stephane Dallongeville, Alexandre Dufour
 */
public abstract class ROIDescriptor
{
    /**
     * Returns the id of this descriptor.<br/>
     * By default it is the same as the name but it can be overridden to be different.
     */
    public String getId()
    {
        return getName();
    }

    /**
     * Returns the name of this descriptor.<br/>
     * The name is used as title (column header) in the ROI panel so keep it short and self
     * explanatory.
     */
    public abstract String getName();

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
     * @see #compute(ROI, Sequence, int, int, int)
     */
    public abstract Class<?> getType();

    /**
     * Computes the descriptor on the specified ROI and return the result.
     * 
     * @param roi
     *        the ROI on which the descriptor(s) should be computed
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @param z
     *        the specific Z position (slice) where we want to compute the descriptor or
     *        <code>-1</code> to compute it over the whole ROI Z dimension.
     * @param t
     *        the specific T position (frame) where we want to compute the descriptor or
     *        <code>-1</code> to compute it over the whole ROI T dimension.
     * @param c
     *        the specific C position (channel) where we want to compute the descriptor or
     *        <code>-1</code> to compute it over the whole ROI C dimension.
     * @return the result of this descriptor computed from the specified parameters.
     * @throws UnsupportedOperationException
     *         if the type of the given ROI is not supported by this descriptor, or if
     *         <code>sequence</code> is <code>null</code> while the calculation requires it, or if
     *         the specified Z, T or C position are not supported for this descriptor
     */
    public abstract Object compute(ROI roi, Sequence sequence, int z, int t, int c)
            throws UnsupportedOperationException;

    /**
     * Computes the descriptor on the specified ROI and return the result.
     * 
     * @param roi
     *        the ROI on which the descriptor(s) should be computed
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @return the result of this descriptor computed from the specified parameters.
     * @throws UnsupportedOperationException
     *         if the type of the given ROI is not supported by this descriptor, or if
     *         <code>sequence</code> is <code>null</code> while the calculation requires it
     */
    public Object compute(ROI roi, Sequence sequence) throws UnsupportedOperationException
    {
        return compute(roi, sequence, -1, -1, -1);
    }

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