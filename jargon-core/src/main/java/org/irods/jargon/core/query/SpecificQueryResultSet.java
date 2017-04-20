/**
 *
 */
package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Result set for execution of a 'specific' or SQL query.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SpecificQueryResultSet extends AbstractIRODSQueryResultSet {

	private final SpecificQuery specificQuery;

	/**
	 * Creates an instance of a result set for a specific query
	 *
	 * @param specificQuery
	 *            {@link SpecificQuery} that was used to generate the result set
	 * @param results
	 *            <code>List</code> of {@link IRODSQueryResultRow} with each row
	 *            of the query results
	 * @param columnNames
	 *            <code>List<String></code> of columns in the results
	 * @param hasMoreRecords
	 *            <code>boolean</code> indicating whether more records are
	 *            available
	 * @param continuationIndex
	 *            <code>int</code> with a continuation index, this can be used
	 *            in subsequent queries to re-query for more result pages
	 */
	public SpecificQueryResultSet(final SpecificQuery specificQuery,
			final List<IRODSQueryResultRow> results,
			final List<String> columnNames, final boolean hasMoreRecords,
			final int continuationIndex) {
		super(results, columnNames, hasMoreRecords, continuationIndex);
		if (specificQuery == null) {
			throw new IllegalArgumentException("null specificQuery");
		}
		this.specificQuery = specificQuery;
	}

	/**
	 * Constructor for an empty result set
	 *
	 * @param specificQuery
	 *            {@link SpecificQuery} that was used to generate the result set
	 * @param columnNames
	 *            <code>List<String></code> of columns in the results
	 */
	public SpecificQueryResultSet(final SpecificQuery specificQuery,
			final List<String> columnNames) {
		super(new ArrayList<IRODSQueryResultRow>(), columnNames, false, 0);
		if (specificQuery == null) {
			throw new IllegalArgumentException("null specificQuery");
		}
		this.specificQuery = specificQuery;
	}

	/**
	 * @return the {@link SpecificQuery}
	 */
	public SpecificQuery getSpecificQuery() {
		return specificQuery;
	}

}
