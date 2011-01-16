/**
 * 
 */
package org.irods.jargon.core.query;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Immutable result set returned from an IRODS general query. 
 * 
 * @author Mike Conway - DICE (www.irods.org) 
 */
public class IRODSQueryResultSet extends AbstractIRODSQueryResultSet {

	final TranslatedIRODSGenQuery translatedIRODSQuery;
	
	public static IRODSQueryResultSet instance(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final List<IRODSQueryResultRow> results,
			final boolean hasMoreRecords) throws JargonException {
		return new IRODSQueryResultSet(translatedIRODSQuery, results,
				hasMoreRecords);
	}

	public static IRODSQueryResultSet instance(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final List<IRODSQueryResultRow> results, final int continuationIndex)
			throws JargonException {

		boolean hasMore = (continuationIndex > 0);

		return new IRODSQueryResultSet(translatedIRODSQuery, results, hasMore);
	}

	private IRODSQueryResultSet(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final List<IRODSQueryResultRow> results,
			final boolean hasMoreRecords) throws JargonException {
		
		super(results, hasMoreRecords);

		if (translatedIRODSQuery == null) {
			throw new JargonException("translated IRODS query is null");
		}

		this.translatedIRODSQuery = translatedIRODSQuery;
		
	}

	/**
	 * Return the query that generated the result set
	 * 
	 * @return {@link org.irods.jargon.TranslatedIRODSGenQuery.TranslatedIRODSQuery
	 *         TranslatedIRODSQuery} this is an immutable object that is
	 *         thread-safe
	 */
	public TranslatedIRODSGenQuery getTranslatedIRODSQuery() {
		return translatedIRODSQuery;
	}
	
	/**
	 * Convenience method to get the number of result columns, based on the
	 * number of selects.
	 * 
	 * @return <code>int</code> with count of result columns.
	 */
	@Override
	public int getNumberOfResultColumns() {
		return translatedIRODSQuery.getSelectFields().size();
	}

	@Override
	public List<String> getColumnNames() {
		// TODO implement for gen query based on selects
		return null;
	}
	
	

}
