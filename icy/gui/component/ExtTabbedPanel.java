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
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/**
 * Basically a JTabbedPane which can handle ExternalizablePanel.
 * 
 * @author Stephane
 */
public class ExtTabbedPanel extends JTabbedPane
{
    /**
     * 
     */
    private static final long serialVersionUID = -1217212007327960771L;

    private class TabComponent extends JPanel
    {
        /**
         * 
         */
        private static final long serialVersionUID = 4841789742300589373L;

        final ExternalizablePanel extPanel;
        final private IcyButton externButton;
        final JLabel label;

        /**
         * needed for data save
         */
        final int index;
        final String tip;

        public TabComponent(String title, Icon icon, ExternalizablePanel panel, String tip, int index)
        {
            super();

            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setBorder(BorderFactory.createEmptyBorder());
            setOpaque(false);

            this.index = index;
            this.tip = tip;
            extPanel = panel;

            label = new JLabel(title + " ", icon, SwingConstants.CENTER);
            label.setOpaque(false);

            externButton = new IcyButton(ResourceUtil.ICON_EXPAND, 16);
            externButton.setFlat(true);
            externButton.setOpaque(false);
            externButton.setContentAreaFilled(false);
            externButton.setToolTipText("Externalize panel");
            externButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // externalize panel
                    extPanel.externalize();
                }
            });

            add(label);
            add(externButton);

            validate();
        }

        public String getTitle()
        {
            return label.getText().trim();
        }

        public Icon getIcon()
        {
            return label.getIcon();
        }

        public void setTitle(String title)
        {
            label.setText(title + " ");
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
            externButton.setBackground(background);
            label.setBackground(background);
        }

        public void setForegroundAll(Color foreground)
        {
            externButton.setForeground(foreground);
            label.setForeground(foreground);
        }
    }

    final private ArrayList<TabComponent> tabComponents;

    public ExtTabbedPanel()
    {
        this(TOP, WRAP_TAB_LAYOUT);
    }

    public ExtTabbedPanel(int tabPlacement)
    {
        this(tabPlacement, WRAP_TAB_LAYOUT);
    }

    public ExtTabbedPanel(int tabPlacement, int tabLayoutPolicy)
    {
        super(tabPlacement, tabLayoutPolicy);

        tabComponents = new ArrayList<TabComponent>();
    }

    /**
     * Find the ExtTabComponent attached to the specified ExternalizablePanel.
     */
    protected TabComponent getTabComponent(ExternalizablePanel panel)
    {
        for (TabComponent extTabComp : tabComponents)
            if (extTabComp.extPanel == panel)
                return extTabComp;

        return null;
    }

    @Override
    public Component add(Component component)
    {
        // special case of externalizable panel
        if (component instanceof ExternalizablePanel)
        {
            final TabComponent tabComp = getTabComponent((ExternalizablePanel) component);

            // already existing ?
            if (tabComp != null)
            {
                // use its parameter
                insertTab(tabComp.getTitle(), tabComp.getIcon(), component, tabComp.tip, Math.min(tabComp.index, getTabCount()));
                return component;
            }
        }

        return super.add(component);
    }

    @Override
    public void setIconAt(int index, Icon icon)
    {
        super.setIconAt(index, icon);

        final Component comp = getTabComponentAt(index);
        if (comp instanceof TabComponent)
            ((TabComponent) comp).setIcon(icon);
    }

    @Override
    public void setDisabledIconAt(int index, Icon disabledIcon)
    {
        super.setDisabledIconAt(index, disabledIcon);

        final Component comp = getTabComponentAt(index);
        if (comp instanceof TabComponent)
            ((TabComponent) comp).setDisabledIcon(disabledIcon);
    }

    @Override
    public void setBackgroundAt(int index, Color background)
    {
        super.setBackgroundAt(index, background);

        final Component comp = getTabComponentAt(index);
        if (comp instanceof TabComponent)
            ((TabComponent) comp).setBackgroundAll(background);
    }

    @Override
    public void setForegroundAt(int index, Color foreground)
    {
        super.setForegroundAt(index, foreground);

        final Component comp = getTabComponentAt(index);
        if (comp instanceof TabComponent)
            ((TabComponent) comp).setForegroundAll(foreground);
    }

    @Override
    public void setTitleAt(int index, String title)
    {
        super.setTitleAt(index, title);

        final Component comp = getTabComponentAt(index);
        if (comp instanceof TabComponent)
            ((TabComponent) comp).setTitle(title);
    }

    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index)
    {
        TabComponent tabComp;

        if (component instanceof ExternalizablePanel)
        {
            final ExternalizablePanel panel = (ExternalizablePanel) component;
            tabComp = getTabComponent(panel);

            // not existing ?
            if (tabComp == null)
            {
                // create the associated tab component
                tabComp = new TabComponent(title, icon, panel, tip, index);
                // and save it in the list to keep a reference
                tabComponents.add(tabComp);
            }

            // externalized ?
            if (panel.isExternalized())
            {
                // manually set parent and exit
                panel.setParent(this);
                return;
            }
        }
        else
            tabComp = null;

        super.insertTab(title, icon, component, tip, index);

        // use custom panel for externalizable panel
        if (tabComp != null)
            setTabComponentAt(index, tabComp);
    }
}
