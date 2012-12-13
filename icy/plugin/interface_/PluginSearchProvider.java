/**
 * 
 */
package icy.plugin.interface_;

import icy.search.SearchResultProducer;

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
     * @see SearchResultProducer
     */
    public Class<? extends SearchResultProducer> getSearchProviderClass();
}
