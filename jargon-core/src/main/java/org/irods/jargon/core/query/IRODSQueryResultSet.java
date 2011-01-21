/**
 * 
 */
package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Immutable result set returned from an IRODS general query.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IRODSQueryResultSet extends AbstractIRODSQueryResultSet {

	private final TranslatedIRODSGenQuery translatedIRODSQuery;
	/**
	 * Used internally by gen query to signal a continuation of a query when sending a re-query
	 */
	private final int continuationIndex;

	
	/**
	 * Creates an instance of a result set based on data coming back from iRODS GenQuery response data.
	 * @param translatedIRODSQuery {@link TranslatedIRODSQuery} that had been sent to get these results.
	 * @param results <code>List</code> of {@link IRODSQueryResultRow} with the raw results.
	 * @param continuationIndex <code>int</code> that indicates the continuation sent by iRODS.  This indicates more data available, and the continuation index
	 * will be sent with the request to get more results.
	 * @return 
	 * @throws JargonException
	 */
	public static IRODSQueryResultSet instance(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final List<IRODSQueryResultRow> results, final int continuationIndex)
			throws JargonException {

		// get a list of the column names
		List<String> columnNames = new ArrayList<String>();

		for (GenQuerySelectField selectField : translatedIRODSQuery
				.getSelectFields()) {
			columnNames.add(selectField.getSelectFieldColumnName());
		}

		return new IRODSQueryResultSet(translatedIRODSQuery, results,
				columnNames, continuationIndex);
	}

	private IRODSQueryResultSet(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final List<IRODSQueryResultRow> results,
			final List<String> columnNames, final int continuationIndex)
			throws JargonException {

		super(results, Collections.unmodifiableList(columnNames),
				continuationIndex > 0);

		if (translatedIRODSQuery == null) {
			throw new JargonException("translated IRODS query is null");
		}

		this.translatedIRODSQuery = translatedIRODSQuery;
		this.continuationIndex = continuationIndex;

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

	public int getContinuationIndex() {
		return continuationIndex;
	}


}
