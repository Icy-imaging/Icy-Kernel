/**
 * 
 */
package icy.gui.component;

import icy.system.thread.ThreadUtil;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import jxl.Cell;
import jxl.Sheet;
import jxl.write.WritableSheet;

/**
 * Excel table view
 * 
 * @author Fabrice de Chaumont, Alexandre Dufour
 */
public class ExcelTable extends JScrollPane
{
    private static final long serialVersionUID = 1L;

    JTable table;

    public ExcelTable()
    {

    }

    public ExcelTable(WritableSheet page)
    {
        updateSheet(page);
        this.setViewportView(table);
        this.setAutoscrolls(true);
    }

    public synchronized void updateSheet(final WritableSheet page)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                table = new JTable();
                setViewportView(table);
                if (page != null)
                {
                    table.setModel(new SheetTableModel(page));
                }
            }
        });
    }

    private class SheetTableModel implements TableModel
    {
        private Sheet sheet = null;

        public SheetTableModel(Sheet sheet)
        {
            this.sheet = sheet;
        }

        @Override
        public int getRowCount()
        {
            return sheet.getRows();
        }

        @Override
        public int getColumnCount()
        {

            return sheet.getColumns();
        }

        /**
         * Copied from javax.swing.table.AbstractTableModel, to name columns using spreadsheet
         * conventions: A, B, C, . Z, AA, AB, etc.
         */
        @Override
        public String getColumnName(int column)
        {
            String result = "";
            for (; column >= 0; column = column / 26 - 1)
            {
                result = (char) ((char) (column % 26) + 'A') + result;
            }
            return result;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {

            try
            {
                Cell cell = sheet.getCell(columnIndex, rowIndex);
                return cell.getContents();
            }
            catch (Exception e)
            {
                //
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
        }

        @Override
        public void addTableModelListener(TableModelListener l)
        {
        }

        @Override
        public void removeTableModelListener(TableModelListener l)
        {
        }
    }
}
