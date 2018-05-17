/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.component;

import icy.system.thread.ThreadUtil;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

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

    public ExcelTable(Sheet page)
    {
        updateSheet(page);
        setViewportView(table);
        setAutoscrolls(true);
    }

    public ExcelTable(WritableSheet page)
    {
        this((Sheet) page);
    }

    public synchronized void updateSheet(final Sheet page)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                table = new JTable();
                setViewportView(table);
                if (page != null)
                    table.setModel(new SheetTableModel(page));
            }
        });
    }

    public synchronized void updateSheet(final WritableSheet page)
    {
        updateSheet((Sheet) page);
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
                return sheet.getCell(columnIndex, rowIndex).getContents();
            }
            catch (Exception e)
            {
                return null;
            }
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
