/**
 * 
 */
package icy.plugin.interface_;

import icy.plugin.PluginLoader;

/**
 * Interface for inner bundled plugin.
 * This interface should be used for plugin which are packaged inside others plugins<br>
 * (in a single JAR plugin file).<br>
 * Generally you should avoid that as only one plugin can be correctly identified in a JAR<br>
 * and a descriptor but in some case it can be usefull.<br>
 * You have to implement the {@link #getMainPluginClassName()} method to return<br>
 * the main plugin class name so your plugin can be identified.
 * This class will also hide your plugin from the plugin list in {@link PluginLoader}.
 * 
 * @author Stephane
 */
public interface PluginBundled
{
    public String getMainPluginClassName();
}
