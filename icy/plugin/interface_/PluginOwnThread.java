/**
 * 
 */
package icy.plugin.interface_;

/**
 * Plugin OwnThread interface.<br>
 * <br>
 * By default a plugin is launched on the AWT Event Dispatch Thread<br>
 * so GUI creation can be done directly without <code>invokeLater</code> calls.<br>
 * The bad point is that long process will actually lock the EDT and make GUI not responding.<br>
 * <br>
 * A plugin implementing this interface will be launch in its own thread so
 * it won't lock the EDT but developer has to take care about using <code>invokeLater</code> method
 * to access GUI components.
 * 
 * @author Stephane
 */
public interface PluginOwnThread
{

}
