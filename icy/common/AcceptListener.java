/**
 * 
 */
package icy.common;

import java.util.EventListener;

/**
 * Basic accept callback.<br>
 * The listener can refuse action from source by returning false.<br>
 * <br>
 * For instance it can be used to prevent main frame from being closed :
 * 
 * <pre>
 * public boolean accept(Object source)
 * {
 *     if (active)
 *         return ConfirmDialog.confirm(&quot;Do you want to interrupt the process ?&quot;);
 * 
 *     return true;
 * }
 * </pre>
 * 
 * @author Stephane
 */
public interface AcceptListener extends EventListener
{
    public boolean accept(Object source);
}
