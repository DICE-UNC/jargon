/**
 *
 */
package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.GenQueryOut;
import org.irods.jargon.core.packinstr.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Convenience methods for handling packing instructions that result from the
 * invocation of genquery or specific query
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class QueryResultProcessingUtils {

	private static final Logger log = LogManager.getLogger(QueryResultProcessingUtils.class);

	/**
	 * Given the raw response from iRODS, translate into a list of result rows for
	 * easier processing.
	 *
	 * @param queryResponse
	 *            {@code Tag} set with the raw GenQuery response from iRODS.
	 * @param columnNames
	 *            {@code List<String>} with the column names
	 * @param continuation
	 *            {@code int}
	 * @param partialStartIndex
	 *            {@code int} with the offset into the query results for the query
	 *            generating this response, this is so the record count begins at
	 *            the point in the overall results where the offset points to.
	 * @return {@code List} of
	 *         {@link org.irods.jargon.core.query.IRODSQueryResultRow} for each row
	 *         in the GenQuery result
	 * @throws JargonException
	 *             for iRODS error
	 */
	public static List<IRODSQueryResultRow> translateResponseIntoResultSet(final Tag queryResponse,
			final List<String> columnNames, final int continuation, final int partialStartIndex)
			throws JargonException {

		if (queryResponse == null) {
			// no response, create an empty result set, and never return null
			log.info("empty result set from query, returning as an empty result set ( no rows found)");
			return new ArrayList<IRODSQueryResultRow>();
		}

		int rows = queryResponse.getTag(GenQueryOut.ROW_CNT).getIntValue();
		log.info("rows returned from iRODS query: {}", rows);

		List<IRODSQueryResultRow> resultSet = new ArrayList<IRODSQueryResultRow>(rows);
		List<String> row;

		int recordCount;
		if (partialStartIndex == 0) {
			recordCount = 1;
		} else {
			recordCount = partialStartIndex + 1;
		}

		boolean lastRecord = (continuation == 0);
		log.debug("is this the last record? {}", lastRecord);

		int attributes = queryResponse.getTag(GenQueryOut.ATTRIB_CNT).getIntValue();

		for (int i = 0; i < rows; i++) {
			// new row
			row = new ArrayList<String>();
			for (int j = 0; j < attributes; j++) {

				row.add(queryResponse.getTags()[4 + j].getTags()[2 + i].getStringValue());
			}

			resultSet.add(IRODSQueryResultRow.instance(row, columnNames, recordCount++, lastRecord));
		}

		return resultSet;

	}

	/**
	 * Get the continuation value from the query response
	 *
	 * @param response
	 *            {@link Tag} from a gen or specific query
	 * @return {@code int} with the continuation value
	 * @throws JargonException
	 *             for iRODS error
	 */
	public static int getContinuationValue(final Tag response) throws JargonException {

		if (response == null) {
			throw new IllegalArgumentException("null response");
		}

		try {
			return response.getTag(GenQueryOut.CONTINUE_INX).getIntValue();

		} catch (Exception e) {
			throw new JargonException("unable to find continuation value in query result");
		}
	}

}
