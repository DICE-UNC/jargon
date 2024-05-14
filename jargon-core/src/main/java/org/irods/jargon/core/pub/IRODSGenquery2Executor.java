package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;

/**
 * The interface to the GenQuery2 API.
 */
public interface IRODSGenquery2Executor extends IRODSAccessObject {

	/**
	 * Query the catalog using a GenQuery2 string.
	 * 
	 * The zone defined in the IRODSAccount will be used.
	 * 
	 * This method DOES NOT provide support for pagination.
	 * 
	 * @param queryString The GenQuery2 string to execute.
	 * @return A JSON string containing the results. The structure will be a list of
	 *         list of strings.
	 * @throws JargonException If an error occurs.
	 */
	String execute(final String queryString) throws JargonException;

	/**
	 * Query the catalog using a GenQuery2 string.
	 * 
	 * This method DOES NOT provide support for pagination.
	 * 
	 * @param queryString The GenQuery2 string to execute.
	 * @param zone        The zone to execute against.
	 * @return A JSON string containing the results. The structure will be a list of
	 *         list of strings.
	 * @throws JargonException If an error occurs.
	 */
	String execute(final String queryString, final String zone) throws JargonException;

	/**
	 * Get the SQL produced by the GenQuery2 API.
	 * 
	 * The zone defined in the IRODSAccount will be used. No SQL is executed.
	 * 
	 * @param queryString The GenQuery2 string to convert to SQL.
	 * @return The generated SQL.
	 * @throws JargonException If an error occurs.
	 */
	String getGeneratedSQL(final String queryString) throws JargonException;

	/**
	 * Get the SQL produced by the GenQuery2 API.
	 * 
	 * No SQL is executed.
	 * 
	 * @param queryString The GenQuery2 string to convert to SQL.
	 * @param zone        The zone to execute against.
	 * @return The generated SQL.
	 * @throws JargonException If an error occurs.
	 */
	String getGeneratedSQL(final String queryString, final String zone) throws JargonException;

	/**
	 * Get the list of column mappings supported by the GenQuery2 API.
	 * 
	 * The zone defined in the IRODSAccount will be used.
	 * 
	 * The results will be produced from the iRODS Catalog Provider.
	 * 
	 * @return A JSON string containing the mappings between GenQuery2 columns and
	 *         database columns.
	 * @throws JargonException If an error occurs.
	 */
	String getColumnMappings() throws JargonException;

	/**
	 * Get the list of column mappings supported by the GenQuery2 API.
	 * 
	 * The results will be produced from the iRODS Catalog Provider.
	 * 
	 * @param zone The zone to execute against.
	 * @return A JSON string containing the mappings between GenQuery2 columns and
	 *         database columns.
	 * @throws JargonException If an error occurs.
	 */
	String getColumnMappings(final String zone) throws JargonException;

}
