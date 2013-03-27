/**
 * 
 */
package icy.gui.component;

import icy.gui.component.button.IcyButton;
import icy.resource.ResourceUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/**
 * @deprecated Use {@link CloseableTabbedPane} instead
 */
@Deprecated
public class CloseTabbedPane extends JTabbedPane
{
    public static interface CloseTabbedPaneListener extends EventListener
    {
        public void tabClosed(int index, String title);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 7946889981661387490L;

    private class CloseTabComponent extends JPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4841789742300589373L;

        final private IcyButton closeButton;
        final private JLabel label;
        final private Component sep;

        public CloseTabComponent(String title, Icon icon)
        {
            super();

            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setBorder(BorderFactory.createEmptyBorder());
            setOpaque(false);

            label = new JLabel(title, icon, SwingConstants.CENTER);
            label.setOpaque(false);

            sep = Box.createHorizontalStrut(6);

            closeButton = new IcyButton(ResourceUtil.ICON_DELETE, 12);
            closeButton.setFlat(true);
            // closeButton.setContentAreaFilled(false);
            closeButton.setToolTipText("close");
            closeButton.setOpaque(false);

            closeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent actionevent)
                {
                    final int index = indexOfTabComponent(CloseTabComponent.this);

                    if (index != -1)
                    {
                        CloseTabbedPane.this.removeTabAt(index);
                        CloseTabbedPane.this.fireTabClosed(index, getTitle());
                    }
                }
            });

            add(label);
            add(sep);
            add(closeButton);

            validate();
        }

        public boolean isClosable()
        {
            return closeButton.isVisible();
        }

        public void setClosable(boolean value)
        {
            sep.setVisible(value);
            closeButton.setVisible(value);
        }

        public String getTitle()
        {
            return label.getText();
        }

        public void setTitle(String title)
        {
            label.setText(title);
        }

        public void setIcon(Icon icon)
        {
            label.setIcon(icon);
        }

        public void setDisabledIcon(Icon disabledIcon)
        {
            label.setDisabledIcon(disabledIcon);
        }

        public void setBackgroundAll(Color background)
        {
            label.setBackground(background);
            closeButton.setBackground(background);
        }

        public void setForegroundAll(Color foreground)
        {
            label.setForeground(foreground);
            closeButton.setForeground(foreground);
        }
    }

    /**
     * {@link JTabbedPane}
     */
    public CloseTabbedPane()
    {
        super();
    }

    /**
     * {@link JTabbedPane}
     */
    public CloseTabbedPane(int tabPlacement)
    {
        super(tabPlacement);
    }

    /**
     * {@link JTabbedPane}
     */
    public CloseTabbedPane(int tabPlacement, int tabLayoutPolicy)
    {
        super(tabPlacement, tabLayoutPolicy);
    }

    /**
     * Returns the 'closable' state of tab component at <code>index</code>.
     * 
     * @param index
     *        the tab index where the check state is queried
     * @return true if tab component at <code>index</code> can be closed (close button visible).<br>
     *         Returns false otherwise
     * @exception IndexOutOfBoundsException
     *            if index is out of range (index < 0 || index >= tab count)
     * @see #setTabClosable(int, boolean)
     */
    public boolean isTabClosable(int index)
    {
        return ((CloseTabComponent) getTabComponentAt(index)).isClosable();
    }

    /**
     * Set the 'closable' state of tab component at <code>index</code>.
     * 
     * @param index
     *        the tab index we want to set the 'closable' state
     * @param value
     *        true if the tab should be 'closable' (close button visible), false otherwise.
     * @exception IndexOutOfBoundsException
     *            if index is out of range (index < 0 || index >= tab count)
     * @see #isTabClosable(int)
     */
    public void setTabClosable(int index, boolean value)
    {
        ((CloseTabComponent) getTabComponentAt(index)).setClosable(value);
    }

    @Override
    public void setIconAt(int index, Icon icon)
    {
        super.setIconAt(index, icon);

        final CloseTabComponent comp = (CloseTabComponent) getTabComponentAt(index);

        if (comp != null)
            comp.setIcon(icon);
    }

    @Override
    public void setDisabledIconAt(int index, Icon disabledIcon)
    {
        super.setDisabledIconAt(index, disabledIcon);

        final CloseTabComponent comp = (CloseTabComponent) getTabComponentAt(index);

        if (comp != null)
            comp.setDisabledIcon(disabledIcon);
    }

    @Override
    public void setBackgroundAt(int index, Color background)
    {
        super.setBackgroundAt(index, background);

        final CloseTabComponent comp = (CloseTabComponent) getTabComponentAt(index);

        if (comp != null)
            comp.setBackgroundAll(background);
    }

    @Override
    public void setForegroundAt(int index, Color foreground)
    {
        super.setForegroundAt(index, foreground);

        final CloseTabComponent comp = (CloseTabComponent) getTabComponentAt(index);

        if (comp != null)
            comp.setForegroundAll(foreground);
    }

    @Override
    public void setTitleAt(int index, String title)
    {
        super.setTitleAt(index, title);

        final CloseTabComponent comp = (CloseTabComponent) getTabComponentAt(index);

        if (comp != null)
            comp.setTitle(title);
    }

    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index)
    {
        super.insertTab(title, icon, component, tip, index);

        setTabComponentAt(index, new CloseTabComponent(title, icon));
    }

    protected void fireTabClosed(int index, String text)
    {
        for (CloseTabbedPaneListener l : listenerList.getListeners(CloseTabbedPaneListener.class))
            l.tabClosed(index, text);

    }

    public void addCloseTabbedPaneListener(CloseTabbedPaneListener l)
    {
        listenerList.add(CloseTabbedPaneListener.class, l);
    }

    public void removeCloseTabbedPaneListener(CloseTabbedPaneListener l)
    {
        listenerList.remove(CloseTabbedPaneListener.class, l);
    }
}
