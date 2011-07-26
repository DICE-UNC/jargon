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

	protected AbstractIRODSQueryResultSet(
			final List<IRODSQueryResultRow> results,
			final List<String> columnNames, final boolean hasMoreRecords) {

		if (results == null) {
			throw new IllegalArgumentException("results was null");
		}

		if (columnNames == null) {
			throw new IllegalArgumentException("columnNames is null");
		}

		this.results = Collections.unmodifiableList(results);
		this.hasMoreRecords = hasMoreRecords;
		this.columnNames = columnNames;
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
	public abstract int getNumberOfResultColumns();

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
}