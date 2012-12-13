package icy.gui.menu.search;

import icy.search.SearchEngine;
import icy.search.SearchResult;
import icy.search.SearchResultProducer;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class SearchResultTableModel extends AbstractTableModel
{
    /**
     * 
     */
    private static final long serialVersionUID = -6476031165522752303L;

    public static final int COL_RESULT_OBJECT = 1;

    private final SearchEngine searchEngine;
    private int maxRowCount;

    public SearchResultTableModel(SearchEngine searchEngine, int maxRowCount)
    {
        this.searchEngine = searchEngine;
        this.maxRowCount = maxRowCount;
    }

    public int getMaxRowCount()
    {
        return maxRowCount;
    }

    public void setMaxRowCount(int value)
    {
        if (maxRowCount != value)
        {
            maxRowCount = value;
            fireTableDataChanged();
        }
    }

    private int adjustIndex(int index, int size)
    {
        if (maxRowCount > 0)
        {
            if (size > maxRowCount)
                return (index * size) / maxRowCount;
        }

        return index;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }

    @Override
    public int getRowCount()
    {
        final int size = searchEngine.getResults().size();

        if (maxRowCount > 0)
            return Math.min(maxRowCount, size);

        return size;
    }

    @Override
    public int getColumnCount()
    {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        final List<SearchResult> results = searchEngine.getResults();

        if (rowIndex < results.size())
        {
            final int adjustedRowIndex = adjustIndex(rowIndex, results.size());
            final SearchResult element = results.get(adjustedRowIndex);

            switch (columnIndex)
            {
                case 0:
                    final SearchResultProducer producer = element.getProducer();
                    final boolean displayProducer;

                    // only display producer on first producer result
                    if (adjustedRowIndex == 0)
                        displayProducer = true;
                    else
                        displayProducer = (results.get(adjustIndex(rowIndex - 1, results.size())).getProducer() != producer);

                    if (displayProducer)
                        return producer;

                    return null;

                case 1:
                    return element;
            }
        }

        return null;
    }
}
