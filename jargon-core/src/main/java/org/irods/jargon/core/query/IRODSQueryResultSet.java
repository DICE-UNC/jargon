/**
 * 
 */
package org.irods.jargon.core.query;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Immutable result set returned from an IRODS query. This class is not final to
 * assist in testability of complex query structures.
 * 
 * @author Mike Conway - DICE (www.irods.org) TODO: how will this be built? do I
 *         need to copy the results to an immutable list, etc
 */
public class IRODSQueryResultSet {

	private final TranslatedIRODSQuery translatedIRODSQuery;
	private final List<IRODSQueryResultRow> results;
	private final boolean hasMoreRecords;

	public static IRODSQueryResultSet instance(
			final TranslatedIRODSQuery translatedIRODSQuery,
			final List<IRODSQueryResultRow> results,
			final boolean hasMoreRecords) throws JargonException {
		return new IRODSQueryResultSet(translatedIRODSQuery, results,
				hasMoreRecords);
	}

	public static IRODSQueryResultSet instance(
			final TranslatedIRODSQuery translatedIRODSQuery,
			final List<IRODSQueryResultRow> results, final int continuationIndex)
			throws JargonException {

		boolean hasMore = (continuationIndex > 0);

		return new IRODSQueryResultSet(translatedIRODSQuery, results, hasMore);
	}

	private IRODSQueryResultSet(
			final TranslatedIRODSQuery translatedIRODSQuery,
			final List<IRODSQueryResultRow> results,
			final boolean hasMoreRecords) throws JargonException {

		if (translatedIRODSQuery == null) {
			throw new JargonException("translated IRODS query is null");
		}

		if (results == null) {
			throw new JargonException("results is null");
		}

		this.translatedIRODSQuery = translatedIRODSQuery;
		this.results = results;
		this.hasMoreRecords = hasMoreRecords;
	}

	/**
	 * Return the query that generated the result set
	 * 
	 * @return {@link org.irods.jargon.query.TranslatedIRODSQuery
	 *         TranslatedIRODSQuery} this is an immutable object that is
	 *         thread-safe
	 */
	public TranslatedIRODSQuery getTranslatedIRODSQuery() {
		return translatedIRODSQuery;
	}

	/**
	 * @return <code><List<List<String>>></code> containing the results in
	 *         row/column form. Note that the columns line up with the selected
	 *         column names in <code>TranslatedIRODSQuery</code>
	 */
	public List<IRODSQueryResultRow> getResults() {
		return results;
	}

	public IRODSQueryResultRow getFirstResult() throws DataNotFoundException {
		if (results.size() == 0) {
			throw new DataNotFoundException("no result found");
		}
		return results.get(0);
	}

	public boolean isHasMoreRecords() {
		return hasMoreRecords;
	}

	/**
	 * Convenience method to get the number of result columns, based on the
	 * number of selects.
	 * 
	 * @return <code>int</code> with count of result columns.
	 */
	public int getNumberOfResultColumns() {
		return translatedIRODSQuery.getSelectFields().size();
	}

}
