/**
 * 
 */
package icy.common.listener;

/**
 * Progress notification listener.
 * 
 * @author Stephane
 */
public interface ProgressListener
{
    public boolean notifyProgress(double position, double length);
}
