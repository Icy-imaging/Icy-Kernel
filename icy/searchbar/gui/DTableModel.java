package icy.searchbar.gui;

import icy.searchbar.interfaces.SBLink;
import icy.searchbar.interfaces.SBProvider;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.table.AbstractTableModel;

import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.internal.ui.common.JRichTooltipPanel;

public class DTableModel extends AbstractTableModel implements ActionListener
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int ROW_COUNT = 3;

    ArrayList<SBLink> elements;

    private JDialog dlg;

    public DTableModel()
    {
        elements = new ArrayList<SBLink>();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        if (columnIndex == 3)
            return true;
        return false;
    }

    @Override
    public int getRowCount()
    {
        return elements.size();
    }

    @Override
    public int getColumnCount()
    {
        return ROW_COUNT;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {

        if (rowIndex >= elements.size())
            return null;

        final SBLink element = elements.get(rowIndex);

        switch (columnIndex)
        {
            case 0:
                SBProvider p = element.getProvider();
                if (p == null)
                    return "";
                ArrayList<SBLink> providerElements = p.getElements();
                if (providerElements == null || providerElements.isEmpty())
                    return "";
                if (providerElements.get(0) == element)
                    return p.getName();
                return "";

            case 1:
                return element.getImage();

            case 2:
                return element;

                // case 3:
                // return element.getActionB();

            default:
                return null;
        }

    }

    public JRichTooltipPanel openPopupMenu(int rowIndex)
    {
        if (rowIndex >= 0 && rowIndex < elements.size())
        {
            // return elements.get(rowIndex).getPopup();
            final RichTooltip tp = elements.get(rowIndex).getRichToolTip();

            if (tp != null)
                return new JRichTooltipPanel(tp);
        }

        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        dlg.setVisible(false);
    }

    public void fireLeftClickEvent(int rowIndex)
    {
        if (rowIndex >= 0 && rowIndex < elements.size())
        {
            elements.get(rowIndex).execute();
        }
    }

    public void fireRightClickEvent(int rowIndex)
    {
        if (rowIndex >= 0 && rowIndex < elements.size())
        {
            JButton btn = elements.get(rowIndex).getActionB();
            if (btn != null)
                btn.doClick();
        }
    }

}
