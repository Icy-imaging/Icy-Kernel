package icy.gui.main;

import icy.plugin.abstract_.Plugin;

import java.util.EventListener;

/**
 * Global {@link Plugin} listener class.
 * Used to listen start and end event for Plugin.
 * 
 * @author Stephane
 */
public interface GlobalPluginListener extends EventListener
{
    /**
     * The plugin was created and is about to start execution.
     */
    public void pluginStarted(Plugin plugin);

    /**
     * When this event occurs the plugin already exited and is now finalizing (garbage collection).
     */
    public void pluginEnded(Plugin plugin);
}
