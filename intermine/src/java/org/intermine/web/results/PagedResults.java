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

import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsInfo;
import org.intermine.objectstore.query.QueryHelper;

/**
 * A pageable and configurable table created from the Results object.
 *
 * @author Andrew Varley
 * @author Kim Rutherford
 */
public class PagedResults extends PagedTable
{
    protected Results results;

    /**
     * Create a new PagedResults object from the given Results object.
     *
     * @param results the Results object
     */
    public PagedResults(Results results) {
        this(QueryHelper.getColumnAliases(results.getQuery()), results);
    }

    /**
     * Create a new PagedResults object from the given Results object.
     *
     * @param results the Results object
     * @param columnNames the headings for the Results columns
     */
    public PagedResults(List columnNames, Results results) {
        super(columnNames);
        this.results = results;
    }

    /**
     * @see PagedTable#getRows
     */
    public List getRows() {
        return results.subList(startRow, getEndRow() + 1);
    }

    /**
     * @see PagedTable#getSize
     */
    public int getSize() {
        //this ensures that if we're on the last page then we get an exact count
        try {
            results.subList(startRow, startRow + pageSize + 1);
        } catch (IndexOutOfBoundsException e) {
        }
        
        try {
            return results.getInfo().getRows();
        } catch (ObjectStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see PagedTable#isSizeEstimate
     */
    public boolean isSizeEstimate() {
        try {
            return results.getInfo().getStatus() != ResultsInfo.SIZE;
        } catch (ObjectStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return information about the results
     * @return the relevant ResultsInfo
     * @throws ObjectStoreException if an error occurs accessing the underlying ObjectStore
     */
    public ResultsInfo getResultsInfo() throws ObjectStoreException {
        return results.getInfo();
    }
}
