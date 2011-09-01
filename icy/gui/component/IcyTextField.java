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

import java.util.EventListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Stephane
 */
public class IcyTextField extends JTextField implements DocumentListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 4294607311366304781L;

    public interface IcyTextListener extends EventListener
    {
        public void textChanged(IcyTextField source);
    }

    public IcyTextField()
    {
        super();

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
