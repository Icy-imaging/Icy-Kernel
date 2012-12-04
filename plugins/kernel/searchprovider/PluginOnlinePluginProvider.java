/**
 * 
 */
package plugins.kernel.searchprovider;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginSearchProvider;
import icy.searchbar.interfaces.SBProvider;

/**
 * @author Stephane
 */
public class PluginOnlinePluginProvider extends Plugin implements PluginSearchProvider
{
    @Override
    public Class<? extends SBProvider> getSearchProviderClass()
    {
        return OnlinePluginProvider.class;
    }
}
