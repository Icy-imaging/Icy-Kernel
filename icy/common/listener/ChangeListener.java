/**
 * 
 */
package icy.common.listener;

import icy.common.EventHierarchicalChecker;

/**
 * Common interface for change notification
 * 
 * @author Stephane
 */
public interface ChangeListener
{
    /**
     * fire changed event
     */
    public void onChanged(EventHierarchicalChecker object);
}
