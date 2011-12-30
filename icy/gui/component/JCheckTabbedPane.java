/**
 * 
 */
package icy.gui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * @author Stephane
 */
public class JCheckTabbedPane extends JTabbedPane
{
    /***/
    private static final long serialVersionUID = 1274171822668858593L;

    private class CustomCheck extends JPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4841789742300589373L;

        final private JCheckBox checkBox;
        final private JLabel label;

        public CustomCheck(String title)
        {
            super();

            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setBorder(BorderFactory.createEmptyBorder());

            checkBox = new JCheckBox(null, null, defaultSelected);
            checkBox.setBorder(BorderFactory.createEmptyBorder());
            checkBox.setFocusable(false);

            checkBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent actionevent)
                {
                    JCheckTabbedPane.this.fireStateChanged();
                }
            });

            label = new JLabel(" " + title);

            add(checkBox);
            add(label);

            validate();
        }

        public boolean isSelected()
        {
            return checkBox.isSelected();
        }

        public void setSelected(boolean value)
        {
            checkBox.setSelected(value);
        }

        public void setTitle(String title)
        {
            label.setText(" " + title);
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
            checkBox.setBackground(background);
            label.setBackground(background);
        }

        public void setForegroundAll(Color foreground)
        {
            checkBox.setBackground(foreground);
            label.setBackground(foreground);
        }
    }

    /**
     * default checkbox selected state
     */
    boolean defaultSelected;

    /**
     * Constructor.
     * 
     * @param defaultSelected
     *        by default checkbox is selected
     * @see JTabbedPane
     */
    public JCheckTabbedPane(int tabPlacement, boolean defaultSelected)
    {
        super(tabPlacement);

        this.defaultSelected = defaultSelected;
    }

    public boolean isDefaultSelected()
    {
        return defaultSelected;
    }

    public void setDefaultSelected(boolean defaultSelected)
    {
        this.defaultSelected = defaultSelected;
    }

    /**
     * Returns the check state of tab component at <code>index</code>.
     * 
     * @param index
     *        the tab index where the check state is queried
     * @return true if tab component at <code>index</code> is checked, false
     *         otherwise
     * @exception IndexOutOfBoundsException
     *            if index is out of range (index < 0 || index >= tab count)
     * @see #setTabChecked(int, boolean)
     */
    public boolean isTabChecked(int index)
    {
        final CustomCheck tabComponent = (CustomCheck) getTabComponentAt(index);

        // can be null here
        if (tabComponent != null)
            return tabComponent.isSelected();

        // default is true
        return true;
    }

    /**
     * Set the check state of tab component at <code>index</code>.
     * 
     * @param index
     *        the tab index we want to set the check state
     * @param value
     *        the check state
     * @exception IndexOutOfBoundsException
     *            if index is out of range (index < 0 || index >= tab count)
     * @see #isTabChecked(int)
     */
    public void setTabChecked(int index, boolean value)
    {
        ((CustomCheck) getTabComponentAt(index)).setSelected(value);
    }

    @Override
    public void setIconAt(int index, Icon icon)
    {
        super.setIconAt(index, icon);

        ((CustomCheck) getTabComponentAt(index)).setIcon(icon);
    }

    @Override
    public void setDisabledIconAt(int index, Icon disabledIcon)
    {
        super.setDisabledIconAt(index, disabledIcon);

        ((CustomCheck) getTabComponentAt(index)).setDisabledIcon(disabledIcon);
    }

    @Override
    public void setBackgroundAt(int index, Color background)
    {
        super.setBackgroundAt(index, background);

        ((CustomCheck) getTabComponentAt(index)).setBackgroundAll(background);
    }

    @Override
    public void setForegroundAt(int index, Color foreground)
    {
        super.setForegroundAt(index, foreground);

        ((CustomCheck) getTabComponentAt(index)).setForegroundAll(foreground);
    }

    @Override
    public void setTitleAt(int index, String title)
    {
        super.setTitleAt(index, title);

        ((CustomCheck) getTabComponentAt(index)).setTitle(title);
    }

    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index)
    {
        super.insertTab(title, icon, component, tip, index);

        setTabComponentAt(index, new CustomCheck(title));
    }
}
