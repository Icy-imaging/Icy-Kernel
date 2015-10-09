/*
 * Copyright 2010-2015 Institut Pasteur.
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
package icy.gui.component;

import icy.util.StringUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.text.Format;
import java.util.EventListener;

import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * IcyTextField extends JFormattedTextField and provide easier text change handling.
 * 
 * @author Stephane
 */
public class IcyTextField extends JFormattedTextField implements DocumentListener, ActionListener, FocusListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 4294607311366304781L;

    public interface TextChangeListener extends EventListener
    {
        public void textChanged(IcyTextField source, boolean validate);
    }

    // internals
    /**
     * @deprecated Don't use this property.
     */
    @Deprecated
    protected boolean consumeCharKeyPressEvent;
    protected boolean changed;

    /**
     * Creates a <code>IcyTextField</code> with no <code>AbstractFormatterFactory</code>. Use
     * <code>setMask</code> or <code>setFormatterFactory</code> to configure the
     * <code>JFormattedTextField</code> to edit a particular type of
     * value.
     */
    public IcyTextField()
    {
        super();

        init();
    }

    /**
     * Creates a <code>IcyTextField</code> with the specified <code>AbstractFormatter</code>. The
     * <code>AbstractFormatter</code> is placed in an <code>AbstractFormatterFactory</code>.
     * 
     * @param formatter
     *        AbstractFormatter to use for formatting.
     */
    public IcyTextField(AbstractFormatter formatter)
    {
        super(formatter);

        init();
    }

    /**
     * Creates a <code>IcyTextField</code>. <code>format</code> is
     * wrapped in an appropriate <code>AbstractFormatter</code> which is
     * then wrapped in an <code>AbstractFormatterFactory</code>.
     * 
     * @param format
     *        Format used to look up an AbstractFormatter
     */
    public IcyTextField(Format format)
    {
        super(format);

        init();
    }

    /**
     * Creates a IcyTextField with the specified value. This will
     * create an <code>AbstractFormatterFactory</code> based on the
     * type of <code>value</code>.
     * 
     * @param value
     *        Initial value for the IcyTextField
     */
    public IcyTextField(Object value)
    {
        super(value);

        init();
    }

    protected void init()
    {
        changed = false;
        consumeCharKeyPressEvent = false;

        getDocument().addDocumentListener(this);
        addActionListener(this);
        addFocusListener(this);
    }

    protected void internalTextChanged(boolean validate)
    {
        // simple text change
        if (!validate)
        {
            // keep mark of text change
            changed = true;
            textChanged(false);
        }
        else
        {
            // previous text change
            if (changed)
            {
                textChanged(true);
                changed = false;
            }
        }
    }

    protected void textChanged(boolean validate)
    {
        fireTextChanged(validate);
    }

    protected void fireTextChanged(boolean validate)
    {
        for (TextChangeListener listener : listenerList.getListeners(TextChangeListener.class))
            listener.textChanged(this, validate);
    }

    /**
     * Force the field to pass to unchanged state (after a {@link #setText(String)} for instance)<br>
     * so it won't generate further <code>textChanged</code> event.
     */
    public void setUnchanged()
    {
        changed = false;
    }

    public void addTextChangeListener(TextChangeListener listener)
    {
        listenerList.add(TextChangeListener.class, listener);
    }

    public void removeTextChangeListener(TextChangeListener listener)
    {
        listenerList.remove(TextChangeListener.class, listener);
    }

    /**
     * @deprecated Should not be used (keep it to false)
     */
    @Deprecated
    public void setConsumeCharKeyPressEvent(boolean consumeCharKeyPressEvent)
    {
        this.consumeCharKeyPressEvent = consumeCharKeyPressEvent;
    }

    /**
     * @deprecated Should not be used.
     */
    @Deprecated
    public boolean getConsumeCharKeyPressEvent()
    {
        return consumeCharKeyPressEvent;
    }

    @Override
    protected void processComponentKeyEvent(KeyEvent e)
    {
        super.processComponentKeyEvent(e);

        if (consumeCharKeyPressEvent)
        {
            final char c = e.getKeyChar();

            // consume KEY_PRESSED character event
            if ((e.getID() == KeyEvent.KEY_PRESSED) && Character.isDefined(c) && !Character.isISOControl(c))
                e.consume();
        }
    }

    @Override
    public void setText(String t)
    {
        if (StringUtil.equals(t, getText(), false))
            return;

        super.setText(t);

        // validate change
        internalTextChanged(true);
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        internalTextChanged(false);
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        internalTextChanged(false);
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        internalTextChanged(false);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        internalTextChanged(true);
    }

    @Override
    public void focusGained(FocusEvent e)
    {
        // nothing to do here
    }

    @Override
    public void focusLost(FocusEvent e)
    {
        internalTextChanged(true);
    }
}
