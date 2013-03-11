/**
 * 
 */
package icy.search;

import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLoader;
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.plugin.interface_.PluginSearchProvider;
import icy.system.IcyExceptionHandler;
import icy.system.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SearchEngine for Icy.
 * 
 * @author Stephane
 */
public class SearchEngine implements SearchResultConsumer, PluginLoaderListener
{
    public interface SearchEngineListener
    {
        public void resultChanged(SearchEngine source, SearchResult result);

        public void resultsChanged(SearchEngine source);

        public void searchStarted(SearchEngine source);

        public void searchCompleted(SearchEngine source);
    }

    /** Search result producer list */
    final ArrayList<SearchResultProducer> producers;

    /** Listener list */
    private final List<SearchEngineListener> listeners;

    /** Internals */
    final Runnable searchProviderSetter;
    String lastSearch;

    public SearchEngine()
    {
        super();

        producers = new ArrayList<SearchResultProducer>();
        listeners = new ArrayList<SearchEngine.SearchEngineListener>();
        lastSearch = "";

        searchProviderSetter = new Runnable()
        {
            @Override
            public void run()
            {
                final String savedSearch = lastSearch;

                // cancel current search
                cancelSearch();

                synchronized (producers)
                {
                    producers.clear();
                }

                // get search providers from plugin
                for (PluginDescriptor plugin : PluginLoader.getPlugins(PluginSearchProvider.class))
                {
                    try
                    {
                        final PluginSearchProvider psp = (PluginSearchProvider) plugin.getPluginClass().newInstance();

                        synchronized (producers)
                        {
                            producers.add(psp.getSearchProviderClass().newInstance());
                        }
                    }
                    catch (Throwable t)
                    {
                        IcyExceptionHandler.handleException(plugin, t, true);
                    }
                }
                
                Collections.sort(producers);

                // restore last search
                search(savedSearch);
            }
        };

        PluginLoader.addListener(this);

        updateSearchProducers();
    }

    private void updateSearchProducers()
    {
        ThreadUtil.bgRunSingle(searchProviderSetter);
    }

    /**
     * Cancel the previous search request
     */
    public void cancelSearch()
    {
        search("");
    }

    /**
     * Performs the search request, mostly build the search result list.<br>
     * Previous search is automatically canceled and replaced by the new one.
     * 
     * @param text
     *        Text used for the search request.<br>
     *        If the text contains severals words then search is done by searching for all words in
     *        whatever order.
     * @see #cancelSearch()
     */
    public void search(String text)
    {
        // save search string
        lastSearch = text;
        // separate words
        final String[] words = (text.split(" "));

        // notify search started
        fireSearchStartedEvent();

        // launch new search
        synchronized (producers)
        {
            for (SearchResultProducer producer : producers)
                producer.search(words, this);
        }
    }

    /**
     * Returns {@link SearchResultProducer} attached to the search engine.
     */
    public List<SearchResultProducer> getSearchResultProducers()
    {
        synchronized (producers)
        {
            return new ArrayList<SearchResultProducer>(producers);
        }
    }

    /**
     * Returns the number of currently producer processing a search request.
     */
    public int getSearchingProducerCount()
    {
        int result = 0;

        synchronized (producers)
        {
            for (SearchResultProducer producer : producers)
                if (producer.isSearching())
                    result++;
        }

        return result;
    }

    /**
     * Returns true if the search engine is currently processing a search request.
     */
    public boolean isSearching()
    {
        synchronized (producers)
        {
            for (SearchResultProducer producer : producers)
                if (producer.isSearching())
                    return true;
        }

        return false;
    }

    // /**
    // * Set the list of provider classes.
    // *
    // * @param providers
    // * : list of provider.
    // */
    // public void setProducer(List<SearchResultProducer> providers)
    // {
    // synchronized (producers)
    // {
    // producers.clear();
    // producers.addAll(providers);
    // }
    // }
    //
    // /**
    // * This method will register the provider class into the list of provider
    // * classes. The {@link SearchResultProducer} object will not be used except for its
    // * class.
    // *
    // * @param providerClass
    // * : provider used to get the Class<?> from.
    // */
    // public void addProducer(Class<? extends SearchResultProducer> providerClass)
    // {
    // if (!providerClasses.contains(providerClass))
    // providerClasses.add(providerClass);
    // }
    //
    // /**
    // * This method will unregister the provider class from the list of provider
    // * class.
    // *
    // * @param providerClass
    // * : provider used to get the Class<?> from.
    // */
    // public void removeProducer(Class<? extends SearchResultProducer> providerClass)
    // {
    // providerClasses.remove(providerClass);
    // }

    /**
     * Returns the last search text.
     */
    public String getLastSearch()
    {
        return lastSearch;
    }

    /**
     * Returns SearchResult at specified index.
     */
    public SearchResult getResult(int index)
    {
        final List<SearchResult> results = getResults();

        if ((index >= 0) && (index < results.size()))
            return results.get(index);

        return null;
    }

    /**
     * Return all current results from all {@link SearchResultProducer}.
     */
    public List<SearchResult> getResults()
    {
        final List<SearchResult> results = new ArrayList<SearchResult>();

        for (SearchResultProducer producer : producers)
        {
            final List<SearchResult> producerResults = producer.getResults();

            // prevent modification of results while adding it
            synchronized (producerResults)
            {
                // sort producer results
                Collections.sort(producerResults);
                // and add
                results.addAll(producerResults);
            }
        }

        return results;
    }

    @Override
    public void pluginLoaderChanged(PluginLoaderEvent e)
    {
        // refresh producer list
        updateSearchProducers();
    }

    @Override
    public void resultChanged(SearchResultProducer producer, SearchResult result)
    {
        // notify listeners about results change
        fireResultChangedEvent(result);
    }

    @Override
    public void resultsChanged(SearchResultProducer producer)
    {
        // notify listeners about results change
        fireResultsChangedEvent();
    }

    @Override
    public void searchCompleted(SearchResultProducer producer)
    {
        // last producer search completed ? --> notify listeners about it
        if (getSearchingProducerCount() == 1)
            fireSearchCompletedEvent();
    }

    public void addListener(SearchEngineListener listener)
    {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(SearchEngineListener listener)
    {
        listeners.remove(listener);
    }

    public void fireResultChangedEvent(SearchResult result)
    {
        for (SearchEngineListener listener : listeners)
            listener.resultChanged(this, result);
    }

    public void fireResultsChangedEvent()
    {
        for (SearchEngineListener listener : listeners)
            listener.resultsChanged(this);
    }

    public void fireSearchStartedEvent()
    {
        for (SearchEngineListener listener : listeners)
            listener.searchStarted(this);
    }

    public void fireSearchCompletedEvent()
    {
        for (SearchEngineListener listener : listeners)
            listener.searchCompleted(this);
    }

}
