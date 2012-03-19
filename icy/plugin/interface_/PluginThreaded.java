/**
 * 
 */
package icy.plugin.interface_;

/**
 * Plugin Threaded interface.<br>
 * <br>
 * By default a plugin is launched on the AWT Event Dispatch Thread so GUI creation
 * can be done directly without <code>invokeLater</code> calls.<br>
 * A common problem is that long process will actually lock the EDT and make GUI not responding.<br>
 * <br>
 * A plugin implementing this interface will have its <code>run()</code> method called
 * in a separate thread but developer has to use <code>invokeLater</code> method
 * for GUI creation / modification.
 * 
 * @author Stephane
 */
public interface PluginThreaded extends Runnable, PluginStartAsThread
{

}
