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

	final TranslatedIRODSGenQuery translatedIRODSQuery;

	/**
	 * Creates an instance of a result set based on data coming back from iRODS
	 * GenQuery response data.
	 *
	 * @param translatedIRODSQuery
	 *            {@link TranslatedIRODSQuery} that had been sent to get these
	 *            results.
	 * @param results
	 *            <code>List</code> of {@link IRODSQueryResultRow} with the raw
	 *            results.
	 * @param continuationIndex
	 *            <code>int</code> that indicates the continuation sent by
	 *            iRODS. This indicates more data available, and the
	 *            continuation index will be sent with the request to get more
	 *            results.
	 * @totalRecords <code>int</code> with the total records, corresponding to
	 *               totalRowCount in the genQueryOut_t structure
	 * @return
	 * @throws JargonException
	 */
	public static IRODSQueryResultSet instance(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final List<IRODSQueryResultRow> results,
			final int continuationIndex, final int totalRecords)
					throws JargonException {

		// get a list of the column names
		List<String> columnNames = new ArrayList<String>();

		for (GenQuerySelectField selectField : translatedIRODSQuery
				.getSelectFields()) {
			columnNames.add(selectField.getSelectFieldColumnName());
		}

		return new IRODSQueryResultSet(translatedIRODSQuery, results,
				columnNames, continuationIndex, totalRecords);
	}

	private IRODSQueryResultSet(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final List<IRODSQueryResultRow> results,
			final List<String> columnNames, final int continuationIndex,
			final int totalRecords) throws JargonException {

		super(results, Collections.unmodifiableList(columnNames),
				continuationIndex > 0, continuationIndex, totalRecords);

		if (translatedIRODSQuery == null) {
			throw new JargonException("translated IRODS query is null");
		}

		this.translatedIRODSQuery = translatedIRODSQuery;

	}

	public TranslatedIRODSGenQuery getTranslatedIRODSQuery() {
		return translatedIRODSQuery;
	}

}
