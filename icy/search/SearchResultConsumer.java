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
     * Notify consumer than results changed.
     */
    public void resultsChanged(SearchResultProducer producer);

    /**
     * Notify consumer than search request completed.
     */
    public void searchCompleted(SearchResultProducer producer);
}
