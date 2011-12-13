/**
 * 
 */
package icy.gui.component;

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

        public CustomCheck(String title)
        {
            super();

            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setBorder(BorderFactory.createEmptyBorder());

            checkBox = new JCheckBox();
            checkBox.setBorder(BorderFactory.createEmptyBorder());
            checkBox.setFocusable(false);

            checkBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent actionevent)
                {
                    fireStateChanged();
                }
            });

            add(checkBox);
            add(new JLabel(" " + title));
            validate();
        }

        public boolean isSelected()
        {
            return checkBox.isSelected();
        }
    }

    private boolean defaultSelected;

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
     * @see #setCheckedTab
     */
    public boolean isTabChecked(int index)
    {
        return ((CustomCheck) getTabComponentAt(index)).isSelected();
    }

    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index)
    {
        super.insertTab(title, icon, component, tip, index);

        setTabComponentAt(index, new CustomCheck(title));
    }
}
