/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
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
