package org.irods.jargon.core.query;

import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;

/**
 * Interface that represents a result from some type of query of iRODS data.  Each query type (GenQuery, SimpleQuery) will have a different
 * implementation that represents the result.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface IRODSQueryResultSetInterface {

	/**
	 * @return <code><List<List<String>>></code> containing the results in
	 *         row/column form. Note that the columns line up with the selected
	 *         column names in <code>TranslatedIRODSQuery</code>
	 */
	public abstract List<IRODSQueryResultRow> getResults();

	public abstract IRODSQueryResultRow getFirstResult()
			throws DataNotFoundException;

	public abstract boolean isHasMoreRecords();

	/**
	 * Convenience method to get the number of result columns, based on the
	 * number of selects.
	 * 
	 * @return <code>int</code> with count of result columns.
	 */
	public abstract int getNumberOfResultColumns();

	/**
	 * Returns a list of the column names in the results;
	 * @return <code>List<String></code> with the query column names.
	 */
	public abstract List<String> getColumnNames();

}