package org.irods.jargon.core.query;

import java.util.Collections;
import java.util.List;

/**
 * Implements a simple result set view resulting from the execution of an iRODS
 * SimpleQuery. See {@link org.irods.jargon.core.pub.SimpleQueryExecutorAO} for
 * the public API to query iRODS via SimpleQuery and produce this result.
 * <p/>
 * This class is immutable.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSSimpleQueryResultSet extends AbstractIRODSQueryResultSet {

	private final SimpleQuery simpleQuery;
	private final List<String> columnNames;


	/**
	 * Static instance method will create an initialized instance of the result set.
	 * @param simpleQuery {@link org.irods.jargon.core.query.SimpleQuery} that created this result set.
	 * @param results <code>List<List<String>></code> with the results in row/column format.
	 * @param columnNames <code>List<String></code> with the headers for each column.
	 * @param hasMoreRecords <code>boolean</code> that indicates whether there are more records to retrieve.
	 * @return
	 */
	public static IRODSSimpleQueryResultSet instance(
			final SimpleQuery simpleQuery,
			final List<IRODSQueryResultRow> results,
			final List<String> columnNames, final boolean hasMoreRecords) {
		return new IRODSSimpleQueryResultSet(simpleQuery, results, columnNames,
				hasMoreRecords);
	}

	/**
	 * Constructor is private.
	 * @param simpleQuery
	 * @param results
	 * @param columnNames
	 * @param hasMoreRecords
	 */
	private IRODSSimpleQueryResultSet(final SimpleQuery simpleQuery,
			final List<IRODSQueryResultRow> results,
			final List<String> columnNames, final boolean hasMoreRecords) {
		super(results, hasMoreRecords);

		if (simpleQuery == null) {
			throw new IllegalArgumentException("null simpleQuery");
		}
		
		if (results == null) {
			throw new IllegalArgumentException("null results");
		}

		if (columnNames == null ) {
			throw new IllegalArgumentException("null columnNames");
		}

		this.simpleQuery = simpleQuery;
		this.columnNames = Collections.unmodifiableList(columnNames);
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.query.AbstractIRODSQueryResultSet#getNumberOfResultColumns()
	 */
	@Override
	public int getNumberOfResultColumns() {
		return columnNames.size();
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.query.AbstractIRODSQueryResultSet#getColumnNames()
	 */
	@Override
	public List<String> getColumnNames() {
		return columnNames;
	}
	
	public SimpleQuery getSimpleQuery() {
		return simpleQuery;
	}


}
