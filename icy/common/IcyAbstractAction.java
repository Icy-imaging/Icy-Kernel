/**
 * 
 */
package icy.common;

import icy.resource.icon.IcyIcon;
import icy.util.StringUtil;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

/**
 * @author Stephane
 */
public abstract class IcyAbstractAction extends AbstractAction
{
    /**
     * 
     */
    private static final long serialVersionUID = -8544059445777661407L;

    private static final int DEFAULT_ICON_SIZE = 20;

    public IcyAbstractAction(String name, IcyIcon icon, String description, int keyCode, int modifiers)
    {
        super(name, icon);

        if (keyCode != 0)
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode, modifiers));
        if (!StringUtil.isEmpty(description))
            putValue(SHORT_DESCRIPTION, description);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, int keyCode)
    {
        this(name, icon, description, keyCode, 0);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description)
    {
        this(name, icon, description, 0, 0);
    }

    public IcyAbstractAction(String name, IcyIcon icon)
    {
        super(name, icon);
    }

    public IcyAbstractAction(String name)
    {
        super(name);
    }

    /**
     * @deprecated Uses {@link #IcyAbstractAction(String, IcyIcon, String, int, int)} instead.
     */
    @Deprecated
    public IcyAbstractAction(String name, String iconName, String description, int keyCode, int modifiers)
    {
        this(name, new IcyIcon(iconName, DEFAULT_ICON_SIZE), description, keyCode, modifiers);
    }

    /**
     * @deprecated Uses {@link #IcyAbstractAction(String, IcyIcon, String, int)} instead.
     */
    @Deprecated
    public IcyAbstractAction(String name, String iconName, String description, int keyCode)
    {
        this(name, new IcyIcon(iconName, DEFAULT_ICON_SIZE), description, keyCode, 0);
    }

    /**
     * @deprecated Uses {@link #IcyAbstractAction(String, IcyIcon, String)} instead.
     */
    @Deprecated
    public IcyAbstractAction(String name, String iconName, String description)
    {
        this(name, new IcyIcon(iconName, DEFAULT_ICON_SIZE), description, 0, 0);
    }

    /**
     * @deprecated Uses {@link #IcyAbstractAction(String, IcyIcon)} instead.
     */
    @Deprecated
    public IcyAbstractAction(String name, String iconName)
    {
        this(name, new IcyIcon(iconName, DEFAULT_ICON_SIZE), null, 0, 0);
    }

    public void setName(String value)
    {
        putValue(Action.NAME, value);
    }

    public String getName()
    {
        return (String) getValue(Action.NAME);
    }

    public void setIcon(IcyIcon value)
    {
        putValue(Action.SMALL_ICON, value);
    }

    public IcyIcon getIcon()
    {
        return (IcyIcon) getValue(Action.SMALL_ICON);
    }

    public void setDescription(String value)
    {
        putValue(Action.SHORT_DESCRIPTION, value);
    }

    public String getDescription()
    {
        return (String) getValue(Action.SHORT_DESCRIPTION);
    }

    public void setLongDescription(String value)
    {
        putValue(Action.LONG_DESCRIPTION, value);
    }

    public String getLongDescription()
    {
        return (String) getValue(Action.LONG_DESCRIPTION);
    }

    public void setAccelerator(int keyCode, int modifiers)
    {
        if (keyCode != 0)
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode, modifiers));
        else
            putValue(ACCELERATOR_KEY, null);
    }

    public void setAccelerator(int keyCode)
    {
        setAccelerator(keyCode, 0);
    }
}
