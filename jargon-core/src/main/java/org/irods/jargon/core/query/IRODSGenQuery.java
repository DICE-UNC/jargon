/**
 *
 */
package org.irods.jargon.core.query;

import org.irods.jargon.core.exception.JargonException;

/**
 * Immutable representation of a query of IRODS data as presented in iquest
 * form, along with other specifications that guide the execution of the query.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public final class IRODSGenQuery extends AbstractIRODSGenQuery {

	private final String queryString;

	/**
	 * Creates an immutable description of a general query to IRODS with
	 * defaults.
	 * 
	 * @param queryString
	 *            <code>String</code> version of an IRODS Query
	 * @param numberOfResultsDesired
	 *            <code>int</code> with the number of desired results
	 * @param partialStartIndex
	 *            <code>int</code> with offest into results
	 * @return
	 * @throws JargonException
	 */
	public static IRODSGenQuery instance(final String queryString,
			final int numberOfResultsDesired) throws JargonException {
		return new IRODSGenQuery(queryString, numberOfResultsDesired,
				RowCountOptions.NO_ROW_COUNT, 0);
	}

	/**
	 * Creates an immutable description of a general query to IRODS.
	 * 
	 * @param queryString
	 *            <code>String</code> version of an IRODS Query
	 * @param numberOfResultsDesired
	 *            <code>int</code> with the number of desired results
	 * @param partialStartIndex
	 *            <code>int</code> with offest into results
	 * @param rowCountOption
	 * @return <code>RowCountOptions</code> enumeration indicating the type of
	 *         row count to be returned.
	 * @throws JargonException
	 */
	public static IRODSGenQuery instance(final String queryString,
			final int numberOfResultsDesired,
			final RowCountOptions rowCountOption) throws JargonException {
		return new IRODSGenQuery(queryString, numberOfResultsDesired,
				rowCountOption, 0);
	}

	private IRODSGenQuery(final String queryString,
			final int numberOfResultsDesired,
			final RowCountOptions rowCountOption, final int skip)
			throws JargonException {

		super(numberOfResultsDesired);

		if (queryString == null || queryString.length() == 0) {
			throw new JargonException("query string must be supplied");
		}

		if (numberOfResultsDesired <= 0) {
			throw new JargonException(
					"number of results desired must be greater than zero");
		}

		if (rowCountOption == null) {
			throw new JargonException("row count option cannot be null");
		}

		if (skip < 0) {
			throw new JargonException("skip value cannot be negative");
		}

		this.queryString = queryString;
	}

	private IRODSGenQuery(final String queryString,
			final int numberOfResultsDesired) throws JargonException {
		this(queryString, numberOfResultsDesired, RowCountOptions.NO_ROW_COUNT,
				0);
	}

	public String getQueryString() {
		return queryString;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IRODSQuery:");
		sb.append("\n   queryString:");
		sb.append(queryString);
		sb.append("\n    numberOfResultsDesired:");
		sb.append(numberOfResultsDesired);
		return sb.toString();
	}

}
