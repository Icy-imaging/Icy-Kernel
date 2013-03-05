/**
 * 
 */
package icy.clipboard;

import icy.main.Icy;
import icy.swimmingPool.SwimmingObject;
import icy.swimmingPool.SwimmingPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Clipboard facilities (internally use the {@link SwimmingPool} object.
 * 
 * @author Stephane
 */
public class Clipboard
{
    public interface ClipboardListener
    {
        public void clipboardChanged();
    }

    private final static List<ClipboardListener> listeners = new ArrayList<Clipboard.ClipboardListener>();

    /**
     * Return true if clipboard contains an object with specified id (or id starting with specified
     * one).
     */
    public static boolean hasObjects(String id, boolean startWith)
    {
        final SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();

        if (sp != null)
            return sp.hasObjects(id, startWith);

        return false;
    }

    /**
     * Return the number of object with specified id (or id starting with specified
     * one) in the clipboard.
     */
    public static int getCount(String id, boolean startWith)
    {
        final SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();

        if (sp != null)
            return sp.getCount(id, startWith);

        return 0;
    }

    /**
     * Return all objects with specified id (or id starting with specified one) from the
     * clipboard.
     */
    public static List<Object> get(String id, boolean startWith)
    {
        final ArrayList<Object> result = new ArrayList<Object>();
        final SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();

        if (sp != null)
        {
            final ArrayList<SwimmingObject> sos = sp.getObjects(id, startWith);

            if (sos != null)
            {
                for (SwimmingObject so : sos)
                {
                    final Object o = so.getObject();

                    if (o != null)
                        result.add(o);
                }
            }
        }

        return result;
    }

    /**
     * Return and remove all objects with specified id (or id starting with specified one) from the
     * clipboard.
     */
    public static List<Object> pop(String id, boolean startWith)
    {
        final ArrayList<Object> result = new ArrayList<Object>();
        final SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();

        if (sp != null)
        {
            final ArrayList<SwimmingObject> sos = sp.popObjects(id, startWith);

            for (SwimmingObject so : sos)
            {
                final Object o = so.getObject();

                if (o != null)
                    result.add(o);
            }

            // notify change
            if (sos.size() > 0)
                fireChangedEvent();
        }

        return result;
    }

    /**
     * Remove all objects with specified id (or id starting with specified one) from the
     * clipboard.
     */
    public static void remove(String id, boolean startWith)
    {
        final SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();

        if (sp != null)
        {
            sp.removeAll(id, startWith);

            // notify change
            fireChangedEvent();
        }
    }

    /**
     * Put the specified object in the clipboard under the specified id.<br>
     * Return true if the operation succeed.
     */
    public static boolean put(Object obj, String id)
    {
        if (obj != null)
        {
            final SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();

            if (sp != null)
            {
                sp.add(new SwimmingObject(obj, id));

                // notify change
                fireChangedEvent();

                return true;
            }
        }

        return false;
    }

    /**
     * Put objects in the clipboard under the specified id.<br>
     * Return true if the operation succeed.
     */
    public static boolean putAll(List<? extends Object> objects, String id)
    {
        final SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();

        if (sp != null)
        {
            for (Object obj : objects)
                if (obj != null)
                    sp.add(new SwimmingObject(obj, id));

            // notify change
            if (objects.size() > 0)
                fireChangedEvent();

            return true;
        }

        return false;
    }

    public static void addListener(ClipboardListener listener)
    {
        if (!listeners.contains(listener))
            listeners.add(listener);

    }

    public static void removeListener(ClipboardListener listener)
    {
        listeners.remove(listener);
    }

    public static void fireChangedEvent()
    {
        for (ClipboardListener l : listeners)
            l.clipboardChanged();
    }
}
