/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.SimpleQueryInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.query.AbstractAliasedQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.IRODSSimpleQueryResultSet;
import org.irods.jargon.core.query.SimpleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access object to execute queries using the iRODS Simple Query facility. This
 * is mainly used for administrative queries, as in the <code>iadmin</code>
 * icommand. Typically these commands require <code>rodsadmin</code>, and will
 * fail if executed without admin rights.
 * <p/>
 * Simple Query allows the the execution of queries as parameterized SQL. These
 * SQL statements are pre-loaded in iRODS and validated before being allowed to
 * run. Other techniques, such as GenQuery, and the new SpecificQueryDefinition
 * facility, allow other methods of querying system and user metadata from the
 * iCAT.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SimpleQueryExecutorAOImpl extends IRODSGenericAO implements
		SimpleQueryExecutorAO {

	private static final Logger log = LoggerFactory
			.getLogger(SimpleQueryExecutorAOImpl.class);

	public static final String OUT_BUF = "outBuf";

	/**
	 * Standard constructor for access objects.
	 * 
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected SimpleQueryExecutorAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.SimpleQueryExecutorAO#executeSimpleQuery(org
	 * .irods.jargon.core.query.SimpleQuery)
	 */
	@Override
	public IRODSQueryResultSetInterface executeSimpleQuery(
			final AbstractAliasedQuery simpleQuery) throws JargonException {

		List<IRODSQueryResultRow> result;
		List<String> colNames;
		IRODSQueryResultSetInterface resultSet;

		if (simpleQuery == null) {
			throw new IllegalArgumentException("null simpleQuery");
		}

		log.info("executeSimpleQuery:{}", simpleQuery);

		Tag response = getResponse((SimpleQuery) simpleQuery);

		if (response == null) {
			log.info("response from IRODS call indicates no rows found");

			resultSet = IRODSSimpleQueryResultSet.instance(simpleQuery,
					new ArrayList<IRODSQueryResultRow>(),
					new ArrayList<String>(), false);

			return resultSet;
		}

		List<String> rows = extractRows(response);
		colNames = parseColumnNames(rows);
		result = generateResultRows(rows, colNames);

		resultSet = IRODSSimpleQueryResultSet.instance(simpleQuery, result,
				colNames, true);

		return resultSet;
	}

	/**
	 * Passes simpleQuery to the connected iRODS instances and returns a
	 * response in the form of a Tag. Returns null if something goes wrong.
	 * 
	 * @param simpleQuery
	 *            a SimpleQuery to be run on the server.
	 * @return the response as a Tag instance.
	 */
	private Tag getResponse(final SimpleQuery simpleQuery)
			throws JargonException {
		SimpleQueryInp simpleQueryInp = SimpleQueryInp.instance(simpleQuery);
		Tag response = null;

		try {
			response = getIRODSProtocol().irodsFunction(simpleQueryInp);
		} catch (DataNotFoundException dnf) {
			log.info("no data found");
		}

		return response;
	}

	/**
	 * Takes a Tag and turns to into a List<String>. It does this by turning the
	 * tag into a string and splitting it on newlines.
	 * 
	 * @param response
	 *            A Tag, probably the reponse returned by getResponse().
	 * @return A List<String> created by splitting the raw tag on newlines.
	 */
	private List<String> extractRows(final Tag response) {
		String rawResponse = response.getTag(OUT_BUF).getStringValue();
		List<String> rows = Arrays.asList(rawResponse.split("\n"));

		return rows;
	}

	/**
	 * Divides the rows returned by extractRows() into grouped result rows. This
	 * is accomplished by looking for the blank rows between result rows. We end
	 * up with an ArrayList<ArrayList<String>>, where each inner
	 * ArrayList<String> represents one row in the result set returned by iRODS.
	 * 
	 * @param rows
	 *            The rows returned by extractRows().
	 * @return The rows divided into groups representing result rows.
	 */
	private ArrayList<ArrayList<String>> divideRows(final List<String> rows) {
		ArrayList<ArrayList<String>> outerList = new ArrayList<ArrayList<String>>();
		ArrayList<String> innerList = new ArrayList<String>();

		for (String r : rows) {
			if (!r.trim().isEmpty()) {
				innerList.add(r);
			} else {
				outerList.add(innerList);
				innerList = new ArrayList<String>();
			}
		}

		outerList.add(innerList);
		return outerList;
	}

	/**
	 * Takes in one of the grouped result rows created by divideRows() and turns
	 * it into an IRODSQueryResultRow.
	 * 
	 * @param rowList
	 *            A grouped result row created by extractRows().
	 * @param columnNames
	 *            A list of column names created by parseColumnNames().
	 * @return an IRODSQueryResultRow.
	 */
	private IRODSQueryResultRow convertRowToResultRow(
			final List<String> rowList, final List<String> columnNames)
			throws JargonException {
		char delimiter = ':';
		List<String> columnValues = new ArrayList<String>();

		for (String row : rowList) {
			columnValues.add(row.substring(row.indexOf(delimiter) + 1).trim());
		}

		return IRODSQueryResultRow.instance(columnValues, columnNames);
	}

	/**
	 * Iterates over the grouped result rows generated by divideRows() and turns
	 * them each into an IRODSQueryResultRow by calling convertRowToResultRow()
	 * on them. Returns a List of IRODSQueryResultRows.
	 * 
	 * @param rows
	 *            a list of rows created by extractRows().
	 * @param columnNames
	 *            a list of columnNames created by parseColumnNames().
	 * @return A list o f IRODSQueryResultRows.
	 */
	private List<IRODSQueryResultRow> generateResultRows(
			final List<String> rows, final List<String> columnNames)
			throws JargonException {
		List<IRODSQueryResultRow> results = new ArrayList<IRODSQueryResultRow>();

		for (List<String> row : divideRows(rows)) {
			results.add(convertRowToResultRow(row, columnNames));
		}

		return results;
	}

	/**
	 * Parses the column names for the result set from the first row in the
	 * response. This allows us to avoid parsing out the column names for each
	 * row in the result.
	 * 
	 * @param rows
	 *            The rows created by extractRows().
	 * @return the list of column names.
	 */
	private List<String> parseColumnNames(final List<String> rows) {
		List<String> colNames = new ArrayList<String>();

		if (rows.size() >= 0) {
			String thisCol;
			int idx = rows.get(0).indexOf(':');
			String firstColName = rows.get(0).substring(0, idx);
			colNames.add(firstColName.trim());

			for (int i = 1; i < rows.size(); i++) {
				idx = rows.get(i).indexOf(':');
				thisCol = rows.get(i).substring(0, idx).trim();
				if (thisCol.equals(firstColName)) {
					break;
				}
				colNames.add(thisCol.trim());
			}
		}
		return colNames;
	}

}
