package org.irods.jargon.core.query;

import java.util.Collections;
import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;

/**
 * Abstract superclass for a simple set of results from some type of iRODS
 * query, such as GenQuery or SimpleQuery. This result set will contain
 * rows/columns, as well as column names. This ResultSet is somewhat like a
 * <code>java.sql.ResultSet</code>, but is greatly simplified.
 * <p/>
 * Jargon may contain, in the future, an actual implementation of
 * <code>java.sql.ResultSet</code>, and that implementation would wrap this
 * simple result set based on rows and columns, and delegate operations to this
 * simpler implementation.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractIRODSQueryResultSet implements
		IRODSQueryResultSetInterface {

	protected final List<IRODSQueryResultRow> results;
	protected final boolean hasMoreRecords;
	protected final List<String> columnNames;
	protected final int continuationIndex;
	protected final int totalRecords;

	/**
	 * Constructor for a result set without supplying total records
	 * 
	 * @param results
	 * @param columnNames
	 * @param hasMoreRecords
	 * @param continuationIndex
	 */
	protected AbstractIRODSQueryResultSet(
			final List<IRODSQueryResultRow> results,
			final List<String> columnNames, final boolean hasMoreRecords,
			final int continuationIndex) {
		this(results, columnNames, hasMoreRecords, continuationIndex, 0);
	}

	/**
	 * Constructor for a result set
	 * 
	 * @param results
	 * @param columnNames
	 * @param hasMoreRecords
	 * @param continuationIndex
	 * @param totalRecords
	 */
	protected AbstractIRODSQueryResultSet(
			final List<IRODSQueryResultRow> results,
			final List<String> columnNames, final boolean hasMoreRecords,
			final int continuationIndex, final int totalRecords) {

		if (results == null) {
			throw new IllegalArgumentException("results was null");
		}

		if (columnNames == null) {
			throw new IllegalArgumentException("columnNames is null");
		}

		if (totalRecords < 0) {
			throw new IllegalArgumentException("totalRecords < 0");
		}

		this.results = Collections.unmodifiableList(results);
		this.hasMoreRecords = hasMoreRecords;
		this.columnNames = columnNames;
		this.continuationIndex = continuationIndex;
		this.totalRecords = totalRecords;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.query.IRODSQueryResultSetInterface#getResults()
	 */
	@Override
	public List<IRODSQueryResultRow> getResults() {
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.query.IRODSQueryResultSetInterface#getFirstResult()
	 */
	@Override
	public IRODSQueryResultRow getFirstResult() throws DataNotFoundException {
		if (results.size() == 0) {
			throw new DataNotFoundException("no result found");
		}
		return results.get(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.query.IRODSQueryResultSetInterface#isHasMoreRecords
	 * ()
	 */
	@Override
	public boolean isHasMoreRecords() {
		return hasMoreRecords;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.query.IRODSQueryResultSetInterface#
	 * getNumberOfResultColumns()
	 */
	@Override
	public int getNumberOfResultColumns() {
		return columnNames.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.query.IRODSQueryResultSetInterface#getColumnNames()
	 */
	@Override
	public List<String> getColumnNames() {
		return columnNames;
	}

	public int getContinuationIndex() {
		return continuationIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.query.IRODSQueryResultSetInterface#getTotalRecords
	 * ()
	 */
	@Override
	public int getTotalRecords() {
		return totalRecords;
	}
}