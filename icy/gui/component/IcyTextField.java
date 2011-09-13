/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.component;

import java.text.Format;
import java.util.EventListener;

import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * IcyTextField extends JFormattedTextField and provide easier change handling
 * 
 * @author Stephane
 */
public class IcyTextField extends JFormattedTextField implements DocumentListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 4294607311366304781L;

    public interface IcyTextListener extends EventListener
    {
        public void textChanged(IcyTextField source);
    }

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

    private void init()
    {
        getDocument().addDocumentListener(this);
    }

    private void textChanged()
    {
        for (IcyTextListener listener : listenerList.getListeners(IcyTextListener.class))
            listener.textChanged(this);
    }

    public void addTextListener(IcyTextListener listener)
    {
        listenerList.add(IcyTextListener.class, listener);
    }

    public void removeTextListener(IcyTextListener listener)
    {
        listenerList.remove(IcyTextListener.class, listener);
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        textChanged();
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        textChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        textChanged();
    }
}
