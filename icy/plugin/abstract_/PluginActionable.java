/**
 * 
 */
package icy.plugin.abstract_;

import icy.plugin.interface_.PluginImageAnalysis;

/**
 * Base class for actionable Plugin.<br>
 * <br>
 * An actionable plugin will appear in workspace or plugin menu.<br>
 * Also it should implement the "run()" method which contains the main process code.
 * 
 * @author Stephane
 */
@SuppressWarnings("deprecation")
public abstract class PluginActionable extends Plugin implements PluginImageAnalysis, Runnable
{
    /**
     * Used to keep backward compatibility with {@link PluginImageAnalysis} interface
     */
    @SuppressWarnings("javadoc")
    @Override
    public void compute()
    {
        run();
    }
}
