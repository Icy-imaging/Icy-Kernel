/**
 * 
 */
package icy.search;

/**
 * @author Stephane
 */
public interface SearchResultConsumer
{
    /**
     * Notify consumer than a single result has just been modified.
     */
    public void resultChanged(SearchResultProducer producer, SearchResult result);

    /**
     * Notify consumer than results list changed.
     */
    public void resultsChanged(SearchResultProducer producer);

    /**
     * Notify consumer than search request completed.
     */
    public void searchCompleted(SearchResultProducer producer);
}
