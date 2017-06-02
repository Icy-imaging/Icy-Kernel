package icy.gui.menu;

/**
 * @deprecated Use {@link ROITask} instead.
 */
@Deprecated
public class ToolRibbonTask extends ROITask
{
    // just for backward compatibility
    public interface ToolRibbonTaskListener extends ROITaskListener
    {
        //
    }

    // just for backward compatibility
    public void addListener(ToolRibbonTaskListener listener)
    {
        super.addListener(listener);
    }

    // just for backward compatibility
    public void removeListener(ToolRibbonTaskListener listener)
    {
        super.removeListener(listener);
    }
}
