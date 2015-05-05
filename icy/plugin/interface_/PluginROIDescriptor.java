/**
 * 
 */
package icy.plugin.interface_;

import icy.roi.ROI;
import icy.roi.ROIDescriptor;
import icy.sequence.Sequence;

import java.util.List;
import java.util.Map;

/**
 * Plugin interface providing the basic functionalities to compute various descriptors for regions
 * of interest (ROI)
 * 
 * @author Alexandre Dufour, Stephane Dallongeville
 */
public interface PluginROIDescriptor extends PluginNoEDTConstructor
{
    /**
     * Returns the list of {@link ROIDescriptor} available in this plug-in.<br/>
     * While simple descriptors usually have just to return a single a real-valued number (e.g.
     * volume), advanced descriptors may need to produce several results for a given ROI (e.g. a
     * best-fitting ellipse is defined by multiple parameters), or adjust the name of the
     * descriptor(s) based on the type of ROI (e.g. "surface" would only apply in 2D, while "volume"
     * would only apply in 3D).<br/>
     * Example:
     * 
     * <pre>
     * List<ROIDescriptor> result = ArrayList<ROIDescriptor>();
     * result.add(new ROIDescriptor("area", Double.class));
     * result.add(new ROIDescriptor("volume", Double.class));
     * result.add(new ROIDescriptor("...", Double.class));
     * </pre>
     */
    public List<ROIDescriptor> getDescriptors();

    /**
     * Computes the descriptor(s) (declared in the {@link #getDescriptors()}) on the
     * specified ROI. Depending on the type of descriptor and ROI, this method may compute (and
     * populate the map with) only a subset of the declared descriptors.<br/>
     * Note this using this method may be faster than calling the <code>compute</code> method for
     * each descriptor separately as some descriptor may group their calculation.
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
     * @return a map where each entry associates the a descriptor to its value.
     * @throws UnsupportedOperationException
     *         if the type of the given ROI is not supported by this descriptor, or if
     *         <code>sequence</code> is <code>null</code> while the calculation requires it, or if
     *         the specified Z, T or C position are not supported for this descriptor
     */
    public Map<ROIDescriptor, Object> compute(ROI roi, Sequence sequence, int z, int t, int c)
            throws UnsupportedOperationException;
}
