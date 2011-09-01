/**
 * 
 */
package icy.type.value;

import icy.file.xml.XMLPersistent;
import icy.util.XMLUtil;

import java.util.ArrayList;

import org.w3c.dom.Node;

/**
 * @author Stephane
 */
public abstract class AbstractValue<T> implements Comparable<T>, XMLPersistent
{
    public interface ValueChangeListener<T>
    {
        public void valueChanged(AbstractValue<T> source);
    }

    protected static final String ID_VALUE = "value";

    /**
     * value
     */
    protected T value;

    /**
     * listeners
     */
    private final ArrayList<ValueChangeListener<T>> listeners;

    public AbstractValue(T value)
    {
        super();

        this.value = value;
        listeners = new ArrayList<ValueChangeListener<T>>();
    }

    public AbstractValue()
    {
        super();

        this.value = getDefaultValue();
        listeners = new ArrayList<ValueChangeListener<T>>();
    }

    public T getValue()
    {
        return value;
    }

    public void setValue(T value)
    {
        if (!this.value.equals(value))
        {
            this.value = value;
            changed();
        }
    }

    public abstract boolean loadFromString(String s);

    public abstract T getDefaultValue();

    public void loadFromString(String s, T def)
    {
        if (!loadFromString(s))
            value = def;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (node == null)
            return false;

        XMLUtil.setElementValue(node, ID_VALUE, value.toString());

        return true;
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        if (node == null)
            return false;

        return loadFromString(XMLUtil.getElementValue(node, ID_VALUE, ""));
    }

    // value changed
    private void changed()
    {
        fireChangeEvent();
    }

    public void addListener(ValueChangeListener<T> listener)
    {
        synchronized (listeners)
        {
            if (!listeners.contains(listener))
                listeners.add(listener);
        }
    }

    public void removeListener(ValueChangeListener<T> listener)
    {
        synchronized (listeners)
        {
            listeners.remove(listener);
        }
    }

    protected void fireChangeEvent()
    {
        final ArrayList<ValueChangeListener<T>> listenersCopy;

        synchronized (listeners)
        {
            listenersCopy = new ArrayList<ValueChangeListener<T>>(listeners);
        }

        for (ValueChangeListener<T> l : listenersCopy)
            l.valueChanged(this);
    }
}
