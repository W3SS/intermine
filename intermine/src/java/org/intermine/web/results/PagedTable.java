package org.intermine.web.results;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * A pageable and configurable table, eg. a Results object
 *
 * @author Andrew Varley
 * @author Kim Rutherford
 */
public abstract class PagedTable
{
    protected List columns = new ArrayList();
    protected int startRow = 0;
    protected int pageSize = 10;

    /**
     * Construct a PageTable with a list of column names
     * @param columnNames the column headings
     */
    public PagedTable(List columnNames) {
        for (int i = 0; i < columnNames.size(); i++) {
            Column column = new Column();
            column.setName((String) columnNames.get(i));
            column.setIndex(i);
            column.setVisible(true);
            columns.add(column);
        }
    }

    /**
     * Get the list of column configurations
     *
     * @return the List of columns in the order they are to be displayed
     */
    public List getColumns() {
        return Collections.unmodifiableList(columns);
    }

    /**
     * Return the number of visible columns.  Used by JSP pages.
     * @return the number of visible columns.
     */
    public int getVisibleColumnCount() {
        int count = 0;
        for (Iterator i = columns.iterator(); i.hasNext();) {
            if (((Column) i.next()).isVisible())  {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Return the width (number of columns) of the table.  Used by the JSP because
     * getColumns().size() isn't possible in JSTL.
     * @return the table width
     */
    public int getColumnCount() {
        return columns.size();
    }

    /**
     * Move a column left
     *
     * @param index the index of the column to move
     */
    public void moveColumnLeft(int index) {
        if (index > 0 && index <= columns.size() - 1) {
            columns.add(index - 1, columns.remove(index));
        }
    }
    
    /**
     * Move a column right
     *
     * @param index the index of the column to move
     */
    public void moveColumnRight(int index) {
        if (index >= 0 && index < columns.size() - 1) {
            columns.add(index + 1, columns.remove(index));
        }
    }
    
    /**
     * Go to the first page
     */
    public void firstPage() {
        startRow = 0;
    }

    /**
     * Check if we are on the first page
     *
     * @return true if we are on the first page
     */
    public boolean isFirstPage() {
        return (startRow == 0);
    }

    /**
     * Go to the last page
     */
    public void lastPage() {
        startRow = ((getSize() - 1) / pageSize) * pageSize;
    }

    /**
     * Check if we are on the last page
     *
     * @return true if we are on the last page
     */
    public boolean isLastPage() {
        return (!isSizeEstimate() && getEndRow() == getSize() - 1);
    }

    /**
     * Go to the previous page
     */
    public void previousPage() {
        startRow -= pageSize;
    }

    /**
     * Go to the next page
     */
    public void nextPage() {
        startRow += pageSize;
    }

    /**
     * Set the page size of the table
     *
     * @param pageSize the page size
     */    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        startRow = (startRow / pageSize) * pageSize;
    }

    /**
     * Get the page size of the current page
     *
     * @return the page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Get the index of the first row of this page
     * @return the index
     */
    public int getStartRow() {
        return startRow;
    }

    /**
     * Get the index of the last row of this page
     * @return the index
     */
    public int getEndRow() {
        int endRow = startRow + pageSize - 1;
        if (!isSizeEstimate() && (endRow + 1 > getSize())) {
            return getSize() - 1;
        } else {
            return endRow;
        }
    }

    /**
     * Return the rows of the table as a List of Lists.
     *
     * @return the rows of the table
     */
    public abstract List getRows();

    /**
     * Get the (possibly estimated) number of rows of this table
     * @return the number of rows
     */
    public abstract int getSize();

    /**
     * Check whether the result of getSize is an estimate
     * @return true if the size is an estimate
     */
    public abstract boolean isSizeEstimate();
}
