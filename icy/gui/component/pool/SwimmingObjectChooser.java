/**
 * 
 */
package icy.gui.component.pool;

import icy.main.Icy;
import icy.swimmingPool.SwimmingObject;
import icy.swimmingPool.SwimmingPoolEvent;
import icy.swimmingPool.SwimmingPoolListener;
import icy.swimmingPool.WeakSwimmingPoolListener;
import icy.util.StringUtil;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

/**
 * @author Nicolas Chenouard
 * @author Stephane
 */
public class SwimmingObjectChooser extends JComboBox implements SwimmingPoolListener
{
    public interface SwimmingObjectChooserListener
    {
        public void objectChanged(Object object);
    }

    private static final long serialVersionUID = 1594001236878708868L;

    private ArrayList<SwimmingObjectChooserListener> listeners;
    private final Class<? extends Object> itemClass;

    public SwimmingObjectChooser(Class<? extends Object> itemClass)
    {
        this(itemClass, 50, "No valid object to display in SwimmingPool");
    }

    public SwimmingObjectChooser(Class<? extends Object> itemClass, final int maxSize, final String defaultMessage)
    {
        super();

        this.itemClass = itemClass;
        this.listeners = new ArrayList<SwimmingObjectChooserListener>();

        Icy.getMainInterface().getSwimmingPool().addListener(new WeakSwimmingPoolListener(this));

        this.setRenderer(new ListCellRenderer()
        {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus)
            {
                if (value == null)
                    return new JLabel(defaultMessage);

                if (value instanceof SwimmingObject)
                {
                    JLabel label = new JLabel(StringUtil.limit(((SwimmingObject) value).getName(), maxSize));
                    label.setToolTipText(((SwimmingObject) value).getName());
                    return label;
                }

                return new JLabel(value.toString());
            }
        });
    }

    public Object getSelectedObject()
    {
        final Object o = getSelectedItem();

        if (o != null)
            return ((SwimmingObject) o).getObject();

        return null;
    }

    @Override
    public void swimmingPoolChangeEvent(final SwimmingPoolEvent event)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                refreshList();

                final Object obj = event.getResult().getObject();

                // Select the last entry computed
                if (obj != null)
                    setSelectedItem(obj);
            }
        });

    }

    void refreshList()
    {
        // save old selection
        final Object oldSelected = getSelectedItem();
        // rebuild model
        setModel(new DefaultComboBoxModel(getSwimmingObjects()));
        // restore selection
        setSelectedItem(oldSelected);
    }

    Object[] getSwimmingObjects()
    {
        final List<Object> objectList = new ArrayList<Object>();
        final ArrayList<SwimmingObject> objects = Icy.getMainInterface().getSwimmingPool().getObjects();

        for (SwimmingObject so : objects)
        {
            final Object o = so.getObject();

            if (o.getClass() == itemClass)
                objectList.add(so);
        }

        return objectList.toArray();
    }

    public void addListener(SwimmingObjectChooserListener listener)
    {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(SwimmingObjectChooserListener listener)
    {
        listeners.remove(listener);
    }

    @Override
    public void fireItemStateChanged(ItemEvent e)
    {
        for (SwimmingObjectChooserListener listener : listeners)
            listener.objectChanged(getSelectedObject());

        super.fireItemStateChanged(e);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        for (SwimmingObjectChooserListener listener : listeners)
            listener.objectChanged(getSelectedObject());

        super.actionPerformed(e);
    }
}
