/**
 * 
 */
package icy.gui.component;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JTabbedPane;

/**
 * @author Stephane
 */
public class JCheckTabbedPane extends JTabbedPane {
    // public interface Check

	/** */
	private static final long serialVersionUID = 1274171822668858593L;
	private ActionListener boxChecked = new ActionListener() 
	{
		@Override
		public void actionPerformed(ActionEvent actionevent) {
			fireStateChanged();
		}
	};

	/**
	 * @param tabPlacement
	 * @param tabLayoutPolicy
	 */
	public JCheckTabbedPane(int tabPlacement, int tabLayoutPolicy) 
	{
		super(tabPlacement, tabLayoutPolicy);
	}

	/**
	 * @param tabPlacement
	 */
	public JCheckTabbedPane(int tabPlacement) 
	{
		super(tabPlacement);
	}

	public JCheckTabbedPane() 
	{
		super();
	}

	/**
	 * Returns the check state of tab component at <code>index</code>.
	 * 
	 * @param index
	 *            the tab index where the check state is queried
	 * @return true if tab component at <code>index</code> is checked, false
	 *         otherwise
	 * @exception IndexOutOfBoundsException
	 *                if index is out of range (index < 0 || index >= tab count)
	 * @see #setCheckedTab
	 */
	public boolean isTabChecked(int index) 
	{
		return ((JCheckBox) getTabComponentAt(index)).isSelected();
	}

	@Override
	public void addTab(String s, Component component) {
		super.addTab(s, component);
		JCheckBox cb = new JCheckBox(s);
		cb.addActionListener(boxChecked);
		setTabComponentAt(getTabCount() - 1, cb);
	}
}
