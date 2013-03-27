/**
 * 
 */
package icy.common;

import icy.gui.frame.progress.ProgressFrame;
import icy.resource.icon.IcyIcon;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.pushingpixels.flamingo.api.common.RichTooltip;

/**
 * AbstractAction class for Icy.
 * 
 * @author Stephane
 */
public abstract class IcyAbstractAction extends AbstractAction implements Runnable
{
    /**
     * Sets the tooltip text of a component from an Action.
     * 
     * @param c
     *        the Component to set the tooltip text on
     * @param a
     *        the Action to set the tooltip text from, may be null
     */
    public static void setToolTipTextFromAction(JComponent c, Action a)
    {
        if (a != null)
        {
            final String longDesc = (String) a.getValue(Action.LONG_DESCRIPTION);
            final String shortDesc = (String) a.getValue(Action.SHORT_DESCRIPTION);

            if (StringUtil.isEmpty(longDesc))
                c.setToolTipText(shortDesc);
            else
                c.setToolTipText(longDesc);
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -8544059445777661407L;

    private static final int DEFAULT_ICON_SIZE = 20;

    /**
     * internals
     */
    protected boolean bgProcess;
    protected boolean processing;
    protected String processMessage;
    protected ProgressFrame progressFrame;
    protected ActionEvent event;

    public IcyAbstractAction(String name, IcyIcon icon, String description, String longDescription, int keyCode,
            int modifiers, boolean bgProcess, String processMessage)
    {
        super(name, icon);

        if (keyCode != 0)
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode, modifiers));
        if (!StringUtil.isEmpty(description))
            putValue(SHORT_DESCRIPTION, description);
        if (!StringUtil.isEmpty(longDescription))
            putValue(LONG_DESCRIPTION, longDescription);

        this.bgProcess = bgProcess;
        this.processMessage = processMessage;
        progressFrame = null;
        processing = false;
        event = null;
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, String longDescription, int keyCode,
            int modifiers)
    {
        this(name, icon, description, longDescription, keyCode, modifiers, false, null);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, String longDescription, boolean bgProcess,
            String processMessage)
    {
        this(name, icon, description, longDescription, 0, 0, bgProcess, processMessage);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, boolean bgProcess, String processMessage)
    {
        this(name, icon, description, null, 0, 0, bgProcess, processMessage);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, int keyCode, int modifiers)
    {
        this(name, icon, description, null, keyCode, modifiers, false, null);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, int keyCode)
    {
        this(name, icon, description, null, keyCode, 0, false, null);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description, String longDescription)
    {
        this(name, icon, description, longDescription, 0, 0, false, null);
    }

    public IcyAbstractAction(String name, IcyIcon icon, String description)
    {
        this(name, icon, description, null, 0, 0, false, null);
    }

    public IcyAbstractAction(String name, IcyIcon icon)
    {
        this(name, icon, null, null, 0, 0, false, null);
    }

    public IcyAbstractAction(String name)
    {
        this(name, null, null, null, 0, 0, false, null);
    }

    /**
     * @deprecated Use {@link #IcyAbstractAction(String, IcyIcon, String, int, int)} instead.
     */
    @Deprecated
    public IcyAbstractAction(String name, String iconName, String description, int keyCode, int modifiers)
    {
        this(name, new IcyIcon(iconName, DEFAULT_ICON_SIZE), description, null, keyCode, modifiers, false, null);
    }

    /**
     * @deprecated Use {@link #IcyAbstractAction(String, IcyIcon, String, int)} instead.
     */
    @Deprecated
    public IcyAbstractAction(String name, String iconName, String description, int keyCode)
    {
        this(name, new IcyIcon(iconName, DEFAULT_ICON_SIZE), description, null, keyCode, 0, false, null);
    }

    /**
     * @deprecated Use {@link #IcyAbstractAction(String, IcyIcon, String)} instead.
     */
    @Deprecated
    public IcyAbstractAction(String name, String iconName, String description)
    {
        this(name, new IcyIcon(iconName, DEFAULT_ICON_SIZE), description, null, 0, 0, false, null);
    }

    /**
     * @deprecated Use {@link #IcyAbstractAction(String, IcyIcon)} instead.
     */
    @Deprecated
    public IcyAbstractAction(String name, String iconName)
    {
        this(name, new IcyIcon(iconName, DEFAULT_ICON_SIZE), null, null, 0, 0, false, null);
    }

    /**
     * @return true if this action process is done in a background thread.
     */
    public boolean isBgProcess()
    {
        return bgProcess;
    }

    /**
     * Set to true if you want to action to be processed in a background thread.
     * 
     * @see #isBgProcess()
     * @see #setProcessMessage(String)
     */
    public void setBgProcess(boolean bgProcess)
    {
        this.bgProcess = bgProcess;
    }

    /**
     * @return the process message to display for background action process.
     * @see #setProcessMessage(String)
     * @see #isBgProcess()
     */
    public String getProcessMessage()
    {
        return processMessage;
    }

    /**
     * @return the {@link ProgressFrame} (only available at process time and only if
     *         {@link #isBgProcess()} is true.
     * @see #isBgProcess()
     */
    public ProgressFrame getProgressFrame()
    {
        return progressFrame;
    }

    public RichTooltip getRichToolTip()
    {
        final String desc = getDescription();
        final String longDesc = getLongDescription();
        final IcyIcon icon = getIcon();

        if (StringUtil.isEmpty(desc) && StringUtil.isEmpty(longDesc))
            return null;

        final RichTooltip result = new RichTooltip();

        if (!StringUtil.isEmpty(desc))
            result.setTitle(desc);

        if (!StringUtil.isEmpty(longDesc))
        {
            for (String ld : longDesc.split("\n"))
                result.addDescriptionSection(ld);
        }

        if (icon != null)
            result.setMainImage(icon.getImage());

        return result;
    }

    /**
     * Set the process message to display for background action process.<br>
     * If set to null then no message is displayed (default).
     * 
     * @see #setBgProcess(boolean)
     */
    public void setProcessMessage(String processMessage)
    {
        this.processMessage = processMessage;
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

    /**
     * @return true if action is currently processing.<br>
     *         Meaningful only when {@link #setBgProcess(boolean)} is set to true)
     */
    public boolean isProcessing()
    {
        return processing;
    }

    @Override
    public boolean isEnabled()
    {
        return super.isEnabled() && !processing;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        event = e;

        final boolean wasEnabled = isEnabled();
        processing = true;
        final boolean isEnabled = isEnabled();

        // notify enabled change
        if (wasEnabled != isEnabled)
            firePropertyChange("enabled", Boolean.valueOf(wasEnabled), Boolean.valueOf(isEnabled));

        if (bgProcess)
            ThreadUtil.bgRun(this);
        else
            run();
    }

    @Override
    public void run()
    {
        if (isBgProcess() && !StringUtil.isEmpty(processMessage))
            progressFrame = new ProgressFrame(processMessage);
        else
            progressFrame = null;

        try
        {
            doAction(event);
        }
        finally
        {
            if (progressFrame != null)
                progressFrame.close();

            final boolean wasEnabled = isEnabled();
            processing = false;
            final boolean isEnabled = isEnabled();

            // notify enabled change
            if (wasEnabled != isEnabled)
                firePropertyChange("enabled", Boolean.valueOf(wasEnabled), Boolean.valueOf(isEnabled));
        }
    }

    public void doAction()
    {
        doAction(new ActionEvent(this, 0, ""));
    }

    public abstract void doAction(ActionEvent e);
}
