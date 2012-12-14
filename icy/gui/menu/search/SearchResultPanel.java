package icy.gui.menu.search;

import icy.main.Icy;
import icy.search.SearchEngine;
import icy.search.SearchResult;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.internal.ui.common.JRichTooltipPanel;

/**
 * This class is the most important part of this plugin: it will handle and
 * display all local and online requests when characters are being typed in the {@link SearchBar}.
 * 
 * @author Thomas Provoost & Stephane
 */
public class SearchResultPanel extends JWindow implements ListSelectionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = -7794681892496197765L;

    private static final int ROW_HEIGHT = 48;
    private static final int MAX_ROW = 15;

    /** Associated Search Bar */
    private final SearchBar searchBar;

    /** PopupMenu */
    JRichTooltipPanel tooltipPanel;
    Popup tooltip;

    /** GUI */
    private final SearchResultTableModel tableModel;
    final JTable table;
    private final JButton moreResultBtn;
    final JScrollPane scrollPane;

    /**
     * Internals
     */
    private final Runnable refresher;
    private final Runnable toolTipRefresher;
    private boolean firstResultsDisplay;

    public SearchResultPanel(final SearchBar sb)
    {
        super(Icy.getMainInterface().getMainFrame());

        searchBar = sb;

        tooltipPanel = null;
        tooltip = null;
        firstResultsDisplay = true;

        refresher = new Runnable()
        {
            @Override
            public void run()
            {
                refreshInternal();
            }
        };

        toolTipRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                updateToolTip();
            }
        };

        // build table (display 15 rows max)
        tableModel = new SearchResultTableModel(sb.getSearchEngine(), MAX_ROW);
        table = new JTable(tableModel);

        // sets the different column values and renderers
        final TableColumnModel colModel = table.getColumnModel();
        TableColumn col;

        // provider name column
        col = colModel.getColumn(0);
        col.setCellRenderer(new SearchProducerTableCellRenderer());
        col.setPreferredWidth(140);

        // result text column
        col = colModel.getColumn(1);
        col.setCellRenderer(new SearchResultTableCellRenderer());
        col.setPreferredWidth(600);

        // sets the table properties
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setColumnSelectionAllowed(false);
        table.setTableHeader(null);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                final SearchResult result = getResultAtPosition(e.getPoint());

                if ((result != null) && result.isEnabled())
                {
                    if (SwingUtilities.isLeftMouseButton(e))
                        result.execute();
                    else
                        result.executeAlternate();

                    close(true);
                    e.consume();
                }
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                // clear selection
                table.getSelectionModel().removeSelectionInterval(0, table.getRowCount() - 1);
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                // select row under mouse position
                final int row = table.rowAtPoint(e.getPoint());

                if (row != -1)
                    table.getSelectionModel().setSelectionInterval(row, row);
                else
                    table.getSelectionModel().removeSelectionInterval(0, table.getRowCount() - 1);
            }
        });
        table.addMouseMotionListener(new MouseAdapter()
        {
            @Override
            public void mouseMoved(MouseEvent e)
            {
                // select row under mouse position
                final int row = table.rowAtPoint(e.getPoint());

                if (row != -1)
                    table.getSelectionModel().setSelectionInterval(row, row);
                else
                    table.getSelectionModel().removeSelectionInterval(0, table.getRowCount() - 1);
            }
        });

        // build GUI
        moreResultBtn = new JButton("");
        moreResultBtn.setHorizontalAlignment(SwingConstants.RIGHT);
        moreResultBtn.setVerticalAlignment(SwingConstants.CENTER);
        moreResultBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showCompleteList();
            }
        });

        scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // window used to display quick result list
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(moreResultBtn, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(600, 400));
        setAlwaysOnTop(true);
        setVisible(false);
    }

    protected SearchEngine getSearchEngine()
    {
        return searchBar.getSearchEngine();
    }

    /**
     * Returns SearchResult located at specified index.
     */
    protected SearchResult getResult(int index)
    {
        if ((index >= 0) && (index < table.getRowCount()))
            return (SearchResult) table.getValueAt(index, SearchResultTableModel.COL_RESULT_OBJECT);

        return null;
    }

    /**
     * Returns the index in the table for the specified SearchResult (-1 if not found)
     */
    protected int getRowIndex(SearchResult result)
    {
        if (result != null)
        {
            for (int i = 0; i < table.getRowCount(); i++)
                if (result == table.getValueAt(i, SearchResultTableModel.COL_RESULT_OBJECT))
                    return i;
        }

        return -1;
    }

    /**
     * Returns SearchResult located at specified point position.
     */
    protected SearchResult getResultAtPosition(Point pt)
    {
        return getResult(table.rowAtPoint(pt));
    }

    /**
     * Returns selected result
     */
    public SearchResult getSelectedResult()
    {
        return getResult(table.getSelectedRow());
    }

    /**
     * Set selected result
     */
    public void setSelectedResult(SearchResult result)
    {
        final int row = getRowIndex(result);

        if (row != -1)
            table.getSelectionModel().setSelectionInterval(row, row);
        else
            table.getSelectionModel().removeSelectionInterval(0, table.getRowCount() - 1);
    }

    void showCompleteList()
    {
        // no more limit on row count
        tableModel.setMaxRowCount(-1);
        // hide button
        moreResultBtn.setVisible(false);

        // update size
        setSize(600, getPanelHeight());
    }

    void hideToolTip()
    {
        if (tooltip != null)
        {
            tooltip.hide();
            tooltip = null;
        }
    }

    /**
     * Calculates and returns panel height.
     */
    int getPanelHeight()
    {
        final Insets margin = getInsets();
        final Insets marginSC = scrollPane.getInsets();
        final Insets marginT = table.getInsets();
        int result;

        result = Math.min(table.getRowCount(), MAX_ROW) * ROW_HEIGHT;
        result += (margin.top + margin.bottom) + (marginSC.top + marginSC.bottom) + (marginT.top + marginT.bottom);
        if (moreResultBtn.isVisible())
            result += moreResultBtn.getPreferredSize().height;

        return result;
    }

    /**
     * Updates the popup menu: asks the tablemodel for the right popupmenu and
     * displays it.
     */
    void updateToolTip()
    {
        hideToolTip();

        if (!isVisible())
            return;

        final SearchResult searchResult = getSelectedResult();

        if (searchResult != null)
        {
            final RichTooltip rtp = searchResult.getRichToolTip();

            if (rtp != null)
            {
                final Rectangle bounds = getBounds();

                tooltipPanel = new JRichTooltipPanel(rtp);

                int x = bounds.x + bounds.width;
                int y = bounds.y + (ROW_HEIGHT * table.getSelectedRow());

                // adjust vertical position
                y -= scrollPane.getVerticalScrollBar().getValue();

                // show tooltip
                tooltip = PopupFactory.getSharedInstance().getPopup(Icy.getMainInterface().getMainFrame(),
                        tooltipPanel, x, y);
                tooltip.show();
            }
            else
                tooltipPanel = null;
        }
    }

    /**
     * Close the results panel.<br>
     * If <code>reset</br> is true that also reset search.
     */
    public void close(boolean reset)
    {
        // reset search
        if (reset)
            searchBar.cancelSearch();

        // hide popup and panel
        setVisible(false);
        hideToolTip();
    }

    /**
     * Execute selected result.
     * Return false if we don't have any selected result.
     */
    public void executeSelected()
    {
        final SearchResult sr = getSelectedResult();

        if ((sr != null) && sr.isEnabled())
        {
            sr.execute();
            close(true);
        }
    }

    /**
     * Update display
     */
    public void refresh()
    {
        ThreadUtil.bgRunSingle(refresher, true);
    }

    /**
     * Update display internal
     */
    void refreshInternal()
    {
        final SearchEngine searchEngine = getSearchEngine();
        final List<SearchResult> results = searchEngine.getResults();
        final int resultCount = results.size();

        if (resultCount == 0)
        {
            close(false);
            return;
        }

        // fix row height (can be changed on LAF change)
        table.setRowHeight(ROW_HEIGHT);

        // save selected
        final SearchResult selected = getSelectedResult();

        if (firstResultsDisplay)
        {
            // limit result list size to MAX_ROW and refresh table data
            if (tableModel.getMaxRowCount() != MAX_ROW)
                tableModel.setMaxRowCount(MAX_ROW);
            else
                tableModel.fireTableDataChanged();

            // no more need to re init the limited display
            firstResultsDisplay = false;
        }
        else
            tableModel.fireTableDataChanged();

        // restore selected
        setSelectedResult(selected);

        final int maxRow = tableModel.getMaxRowCount();

        // result list do not display all results ?
        if ((maxRow > 0) && (resultCount > maxRow))
        {
            moreResultBtn.setText(maxRow + " / " + resultCount + " (show all)");
            moreResultBtn.setVisible(true);
        }
        else
            moreResultBtn.setVisible(false);

        // update bounds and display window
        final Point p = searchBar.getLocationOnScreen();
        setBounds(p.x, p.y + searchBar.getHeight(), 600, getPanelHeight());

        // show the result list
        setVisible(true);
        // update tooltip
        updateToolTip();
    }

    /**
     * Selection movement in the table: up or down.
     * 
     * @param direction
     *        : should be 1 or -1.
     */
    public void moveSelection(int direction)
    {
        final int rowCount = table.getRowCount();

        if (rowCount == 0)
            return;

        final int rowIndex = table.getSelectedRow();
        final int newIndex;

        if (rowIndex == -1)
        {
            if (direction > 0)
                newIndex = 0;
            else
                newIndex = rowCount - 1;
        }
        else
            newIndex = Math.abs((rowIndex + direction) % rowCount);

        table.setRowSelectionInterval(newIndex, newIndex);
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        // selection changed --> update tooltip
        updateToolTip();
    }

    public void searchStarted()
    {
        firstResultsDisplay = true;
    }

    public void resultChanged(SearchResult result)
    {
        if (isVisible())
        {
            // only update the specified result
            final int rowIndex = getRowIndex(result);

            if (rowIndex != -1)
                tableModel.fireTableRowsUpdated(rowIndex, rowIndex);
            // refresh toolTip if needed
            if (result == getSelectedResult())
                ThreadUtil.bgRunSingle(toolTipRefresher, true);
        }
    }

    public void resultsChanged()
    {
        // refresh table
        refresh();
    }
}
