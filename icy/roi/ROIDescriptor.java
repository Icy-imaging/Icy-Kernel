/**
 * 
 */
package icy.roi;

import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.interface_.PluginROIDescriptor;
import icy.roi.ROIEvent.ROIEventType;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.system.IcyExceptionHandler;
import icy.util.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import plugins.kernel.roi.descriptor.measure.ROIBasicMeasureDescriptorsPlugin;

/**
 * Abstract class providing the basic methods to retrieve properties and compute a specific
 * descriptor for a region of interest (ROI)
 * 
 * @author Stephane Dallongeville, Alexandre Dufour
 */
public abstract class ROIDescriptor
{
    /**
     * Returns all available ROI descriptors (see {@link ROIDescriptor}) and their attached plugin
     * (see {@link PluginROIDescriptor}).<br/>
     * This list can be extended by installing new plugin(s) implementing the {@link PluginROIDescriptor} interface.
     * 
     * @see ROIDescriptor#compute(ROI, Sequence)
     * @see PluginROIDescriptor#compute(ROI, Sequence)
     */
    public static Map<ROIDescriptor, PluginROIDescriptor> getDescriptors()
    {
        final Map<ROIDescriptor, PluginROIDescriptor> result = new HashMap<ROIDescriptor, PluginROIDescriptor>();
        final List<PluginDescriptor> pluginDescriptors = PluginLoader.getPlugins(PluginROIDescriptor.class);

        for (PluginDescriptor pluginDescriptor : pluginDescriptors)
        {
            try
            {
                final PluginROIDescriptor plugin = (PluginROIDescriptor) PluginLauncher.create(pluginDescriptor);
                final List<ROIDescriptor> descriptors = plugin.getDescriptors();

                if (descriptors != null)
                {
                    for (ROIDescriptor roiDescriptor : descriptors)
                        result.put(roiDescriptor, plugin);
                }
            }
            catch (Throwable e)
            {
                // show a message in the output console
                IcyExceptionHandler.showErrorMessage(e, false, true);
                // and send an error report (silent as we don't want a dialog appearing here)
                IcyExceptionHandler.report(pluginDescriptor, IcyExceptionHandler.getErrorMessage(e, true));
            }
        }

        return result;
    }

    /**
     * Returns the descriptor identified by the given id from the given list of {@link ROIDescriptor}.<br>
     * It can return <code>null</code> if the descriptor is not found in the given list.
     * 
     * @param id
     *        the id of the descriptor ({@link ROIBasicMeasureDescriptorsPlugin#ID_VOLUME} for instance) @see
     *        #getDescriptors()
     * @see #computeDescriptor(String, ROI, Sequence)
     */
    public static ROIDescriptor getDescriptor(Collection<ROIDescriptor> descriptors, String id)
    {
        for (ROIDescriptor roiDescriptor : descriptors)
            if (StringUtil.equals(roiDescriptor.getId(), id))
                return roiDescriptor;

        return null;
    }
    
    /**
     * Returns the descriptor identified by the given id from the given list of {@link ROIDescriptor}.<br>
     * It can return <code>null</code> if the descriptor is not found in the given list.
     * 
     * @param id
     *        the id of the descriptor ({@link ROIBasicMeasureDescriptorsPlugin#ID_VOLUME} for instance) @see
     *        #getDescriptors()
     * @see #computeDescriptor(String, ROI, Sequence)
     */
    public static ROIDescriptor getDescriptor(String id)
    {
        return getDescriptor(getDescriptors().keySet(), id);
    }

    /**
     * Computes the specified descriptor from the input {@link ROIDescriptor} set on given ROI
     * and returns the result (or <code>null</code> if the descriptor is not found).
     * 
     * @param roiDescriptors
     *        the input {@link ROIDescriptor} set (see {@link #getDescriptors()} method)
     * @param descriptorId
     *        the id of the descriptor we want to compute ({@link ROIBasicMeasureDescriptorsPlugin#ID_VOLUME} for
     *        instance)
     * @param roi
     *        the ROI on which the descriptor(s) should be computed
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @return the computed descriptor or <code>null</code> if the descriptor if not found in the
     *         specified set
     * @throws UnsupportedOperationException
     *         if the type of the given ROI is not supported by this descriptor, or if <code>sequence</code> is
     *         <code>null</code> while the calculation requires it, or if
     *         the specified Z, T or C position are not supported by the descriptor
     */
    public static Object computeDescriptor(Collection<ROIDescriptor> roiDescriptors, String descriptorId, ROI roi,
            Sequence sequence)
    {
        final ROIDescriptor roiDescriptor = getDescriptor(roiDescriptors, descriptorId);

        if (roiDescriptor != null)
            return roiDescriptor.compute(roi, sequence);

        return null;
    }

    /**
     * Computes the specified descriptor on given ROI and returns the result (or <code>null</code> if the descriptor is
     * not found).
     * 
     * @param descriptorId
     *        the id of the descriptor we want to compute ({@link ROIBasicMeasureDescriptorsPlugin#ID_VOLUME} for
     *        instance)
     * @param roi
     *        the ROI on which the descriptor(s) should be computed
     * @param sequence
     *        an optional sequence where the pixel size can be retrieved
     * @return the computed descriptor or <code>null</code> if the descriptor if not found in the
     *         specified set
     * @throws UnsupportedOperationException
     *         if the type of the given ROI is not supported by this descriptor, or if <code>sequence</code> is
     *         <code>null</code> while the calculation requires it, or if
     *         the specified Z, T or C position are not supported by the descriptor
     */
    public static Object computeDescriptor(String descriptorId, ROI roi, Sequence sequence)
    {
        return computeDescriptor(getDescriptors().keySet(), descriptorId, roi, sequence);
    }

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

    @Override
    public String toString()
    {
        // default implementation
        return getName();
    }
}