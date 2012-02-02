/**
 * 
 */
package icy.common.listener;

import java.util.EventListener;

/**
 * Basic notification listener.
 * 
 * @author stephane
 */
public interface NotifyListener extends EventListener
{
    public void notify(Object obj);
}
