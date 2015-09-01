/**
 * 
 */
package icy.gui.component;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Basically a JTable component with minor improvement.
 * 
 * @author Stephane
 */
public class IcyTable extends JTable
{
    /**
     * 
     */
    private static final long serialVersionUID = -3434771353006383970L;

    /**
     * @see JTable#JTable(int, int)
     */
    public IcyTable(int numRows, int numColumns)
    {
        super(numRows, numColumns);
    }

    /**
     * @see JTable#JTable(Object[][], Object[])
     */
    public IcyTable(Object[][] rowData, Object[] columnNames)
    {
        super(rowData, columnNames);
    }

    /**
     * @see JTable#JTable(TableModel, TableColumnModel, ListSelectionModel)
     */
    public IcyTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm)
    {
        super(dm, cm, sm);
    }

    /**
     * @see JTable#JTable(TableModel, TableColumnModel)
     */
    public IcyTable(TableModel dm, TableColumnModel cm)
    {
        super(dm, cm);
    }

    /**
     * @see JTable#JTable(TableModel)
     */
    public IcyTable(TableModel dm)
    {
        super(dm);
    }

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        final boolean result = super.getScrollableTracksViewportWidth();

        if (result)
            return getPreferredSize().width < getParent().getWidth();

        return result;
    }
}
