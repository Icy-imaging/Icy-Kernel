/**
 * 
 */
package plugins.kernel.searchprovider;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginSearchProvider;
import icy.search.SearchResultProducer;

/**
 * @author Stephane
 */
public class PluginLocalPluginProvider extends Plugin implements PluginSearchProvider
{
    @Override
    public Class<? extends SearchResultProducer> getSearchProviderClass()
    {
        return LocalPluginSearchResultProducer.class;
    }
}
