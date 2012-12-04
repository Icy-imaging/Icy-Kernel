/**
 * 
 */
package icy.gui.component.menu;

import icy.common.IcyAbstractAction;
import icy.resource.icon.IcyIcon;

import java.awt.event.ActionListener;

import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;

/**
 * @author Stephane
 */
public class IcyRibbonApplicationMenuEntrySecondary extends RibbonApplicationMenuEntrySecondary
{
    private final IcyAbstractAction action;

    public IcyRibbonApplicationMenuEntrySecondary(ResizableIcon icon, String text, ActionListener mainActionListener,
            CommandButtonKind entryKind)
    {
        super(icon, text, mainActionListener, entryKind);

        action = null;
    }

    public IcyRibbonApplicationMenuEntrySecondary(IcyAbstractAction action)
    {
        super((action.getIcon() != null) ? new IcyIcon(action.getIcon()) : null, action.getName(), action,
                CommandButtonKind.ACTION_ONLY);

        this.action = action;

        // set tooltip
        setActionRichTooltip(action.getRichToolTip());
    }

    @Override
    public boolean isEnabled()
    {
        return super.isEnabled() && ((action == null) || action.isEnabled());
    }

    @Override
    public void setEnabled(boolean b)
    {
        final boolean oldValue = isEnabled();

        super.setEnabled(b);

        if ((oldValue != b) && (action != null))
            action.setEnabled(b);
    }
}
