/**
 * 
 */
package icy.plugin.interface_;

/**
 * This <i>ugly hack</i> interface exists to force the Plugin constructor to be done outside the EDT
 * (Event Dispatch Thread).<br>
 * We need it as by default Plugin instance are created in the EDT (for historical reasons then to
 * preserve backward compatibility) and sometime we really want to avoid it as plugin using many
 * others classes make lock the EDT for severals second just with some heavy class loading work.
 * 
 * @author Stephane
 */
public interface PluginNoEDTConstructor
{

}
