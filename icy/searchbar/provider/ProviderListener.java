package icy.searchbar.provider;

import icy.searchbar.interfaces.SBProvider;

/**
 * Contains necessary methods to update the display depending on the state of the provider.
 * 
 * @author Thomas Provoost
 */
public interface ProviderListener
{
    public void providerItemChanged();

    public void loadedProvider(SBProvider sbProvider);

    public void updateDisplay();

}
