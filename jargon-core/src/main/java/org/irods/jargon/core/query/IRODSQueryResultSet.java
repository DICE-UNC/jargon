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

	public static IRODSQueryResultSet instance(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final List<IRODSQueryResultRow> results, final int continuationIndex)
			throws JargonException {

		boolean hasMore = (continuationIndex > 0);
		// get a list of the column names
		List<String> columnNames = new ArrayList<String>();

		for (GenQuerySelectField selectField : translatedIRODSQuery
				.getSelectFields()) {
			columnNames.add(selectField.getSelectFieldColumnName());
		}

		return new IRODSQueryResultSet(translatedIRODSQuery, results,
				columnNames, hasMore);
	}

	private IRODSQueryResultSet(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final List<IRODSQueryResultRow> results,
			final List<String> columnNames, final boolean hasMoreRecords)
			throws JargonException {

		super(results, Collections.unmodifiableList(columnNames),
				hasMoreRecords);

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
