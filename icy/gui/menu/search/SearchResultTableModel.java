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
        final int resultsCount = results.size();

        if (rowIndex < resultsCount)
        {
            final int adjustedRowIndex = adjustIndex(rowIndex, resultsCount);

            // can happen in rare case...
            if (adjustedRowIndex >= resultsCount)
                return null;

            final SearchResult element = results.get(adjustedRowIndex);

            switch (columnIndex)
            {
                case 0:
                    final SearchResultProducer producer = element.getProducer();

                    // display producer on first producer result
                    if (adjustedRowIndex == 0)
                        return producer;
                    else if (results.get(adjustIndex(rowIndex - 1, resultsCount)).getProducer() != producer)
                        return producer;

                    return null;

                case 1:
                    return element;
            }
        }

        return null;
    }
}
