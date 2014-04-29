/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.action;

import icy.gui.frame.progress.ProgressFrame;
import icy.main.Icy;
import icy.resource.icon.IcyIcon;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import org.pushingpixels.flamingo.api.common.RichTooltip;

/**
 * Icy basic AbstractAction class.
 * 
 * @author Stephane
 */
public abstract class IcyAbstractAction extends AbstractAction
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

    private class ActionRunner implements Runnable
    {
        final ActionEvent event;

        public ActionRunner(ActionEvent e)
        {
            super();

            event = e;
        }

        @Override
        public void run()
        {
            final String mess = getProcessMessage();

            if (isBgProcess() && !StringUtil.isEmpty(mess) && !Icy.getMainInterface().isHeadLess())
                progressFrame = new ProgressFrame(mess);
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

                // need to be done on the EDT (can change the enabled state)
                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setProcessing(false);
                    }
                });
            }
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -8544059445777661407L;

    private static final int DEFAULT_ICON_SIZE = 20;

    /**
     * The "enabled" property key.
     */
    public static final String ENABLED_KEY = "enabled";

    /**
     * internals
     */
    protected boolean bgProcess;
    protected boolean processing;
    protected String processMessage;
    protected ProgressFrame progressFrame;

    public IcyAbstractAction(String name, IcyIcon icon, String description, String longDescription, int keyCode,
            int modifiers, boolean bgProcess, String processMessage)
    {
        super(name, icon);

        // by default we use the name as Action Command
        putValue(ACTION_COMMAND_KEY, name);
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
     * Returns the selected state (for toggle button type).
     */
    public boolean isSelected()
    {
        return Boolean.TRUE.equals(getValue(SELECTED_KEY));
    }

    /**
     * Sets the selected state (for toggle button type).
     */
    public void setSelected(boolean value)
    {
        putValue(SELECTED_KEY, Boolean.valueOf(value));
    }

    /**
     * Returns the {@link KeyStroke} for this action (can be null).
     */
    public KeyStroke getKeyStroke()
    {
        return (KeyStroke) getValue(ACCELERATOR_KEY);
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
        return enabled && !processing;
    }

    @Override
    public void setEnabled(boolean value)
    {
        if (enabled != value)
        {
            final boolean wasEnabled = isEnabled();
            enabled = value;
            final boolean isEnabled = isEnabled();

            // notify enabled change
            if (wasEnabled != isEnabled)
                firePropertyChange(ENABLED_KEY, Boolean.valueOf(wasEnabled), Boolean.valueOf(isEnabled));
        }
    }

    protected void setProcessing(boolean value)
    {
        if (processing != value)
        {
            final boolean wasEnabled = isEnabled();
            processing = value;
            final boolean isEnabled = isEnabled();

            // notify enabled change
            if (wasEnabled != isEnabled)
                firePropertyChange(ENABLED_KEY, Boolean.valueOf(wasEnabled), Boolean.valueOf(isEnabled));
        }
    }

    /**
     * Helper method to fire enabled changed event (this force component refresh)
     */
    public void enabledChanged()
    {
        final boolean enabledState = isEnabled();

        // notify enabled change
        firePropertyChange(ENABLED_KEY, Boolean.valueOf(!enabledState), Boolean.valueOf(enabledState));
    }

    /**
     * Returns a {@link JLabel} component representing the action.
     */
    public JLabel getLabelComponent(boolean wantIcon, boolean wantText)
    {
        final JLabel result = new JLabel();

        if (wantIcon)
            result.setIcon(getIcon());
        if (wantText)
            result.setText(getName());

        final String desc = getDescription();

        if (StringUtil.isEmpty(desc))
            result.setToolTipText(getLongDescription());
        else
            result.setToolTipText(getDescription());

        return result;
    }

    /**
     * Returns a {@link JLabel} component representing the action.
     */
    public JLabel getLabelComponent()
    {
        return getLabelComponent(true, true);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        setProcessing(true);

        final ActionRunner runner = new ActionRunner(e);

        if (isBgProcess())
            ThreadUtil.bgRun(runner);
        else
            runner.run();
    }

    /**
     * Execute action (delayed execution if action requires it)
     */
    public void execute()
    {
        actionPerformed(new ActionEvent(this, 0, ""));
    }

    /**
     * @deprecated Use {@link #executeNow()} instead
     */
    @Deprecated
    public boolean doAction()
    {
        return doAction(new ActionEvent(this, 0, ""));
    }

    /**
     * Execute action now (wait for execution to complete)
     */
    public boolean executeNow()
    {
        return doAction(new ActionEvent(this, 0, ""));
    }

    protected abstract boolean doAction(ActionEvent e);
}
