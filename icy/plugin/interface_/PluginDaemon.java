/**
 * 
 */
package icy.plugin.interface_;

/**
 * Plugin Daemon interface.<br>
 * Daemon plugin are automatically loaded when the application is started.<br>
 * Icy will always execute them in a separate thread for safety so the interface extends
 * PluginThreaded.<br>
 * They can be enabled / disabled from the Icy preferences window.
 * 
 * @author Stephane
 */
public interface PluginDaemon extends PluginThreaded
{
    /**
     * Called by Icy to initialize the daemon plugin (init singleton, register listeners...)<br>
     * This method is synchronous and should not consume too much time.
     */
    public void init();

    /**
     * Called by Icy to execute the daemon plugin.<br>
     * This method is executed in a separate thread and should not return until <code>stop()</code>
     * is called.
     */
    @Override
    public void run();

    /**
     * Called by Icy to stop the daemon plugin.<br>
     * After this method has been called, the <code>run()</code> should terminate.<br>
     * The method is also used to "uninitialize" plugin (unregister listeners).<br>
     * This method is synchronous and should not consume too much time.
     */
    public void stop();
}
