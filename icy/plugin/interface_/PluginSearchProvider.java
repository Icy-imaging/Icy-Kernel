/**
 * 
 */
package icy.plugin.interface_;

import icy.searchbar.interfaces.SBProvider;

/**
 * Plugin Search Provider interface.<br>
 * Used to define a plugin which provide results for the global search tool.<br>
 * 
 * @author Stephane
 */
public interface PluginSearchProvider
{
    /**
     * Return the Search Provider.
     * 
     * @see SBProvider
     */
    public Class<? extends SBProvider> getSearchProviderClass();
}
