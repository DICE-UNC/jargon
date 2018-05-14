/**
 *
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.AbstractIRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;

/**
 * Access object to process 'iquest-like' GenQuery.
 * <p>
 * This access object can accept a query in iquest form, and will process that
 * query and return a result set with the query response and other related
 * information.
 *
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface IRODSGenQueryExecutor extends IRODSAccessObject {

	/**
	 * Execute an iquest-like query and return results in a convenient POJO object.
	 * <p>
	 * Note: this command will not close the underlying result set, so that it may
	 * be paged by getting next result. It is up to the caller to call
	 * {@code closeResults()} when done with the result set. Alternately, the
	 * {@code executeIRODSQueryAndCloseResults()} method may be employed.
	 *
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.AbstractIRODSGenQuery} that
	 *            will wrap the given iquest-like query
	 * @param continueIndex
	 *            {@code int} that indicates whether this is a requery when more
	 *            resuts than the limit have been generated
	 * @return {@link org.irods.jargon.core.query.IRODSQueryResultSet} that contains
	 *         the results of the query
	 * @throws JargonException
	 *             for iRODS error
	 * @throws JargonQueryException
	 *             for query error
	 */
	IRODSQueryResultSet executeIRODSQuery(final AbstractIRODSGenQuery irodsQuery, final int continueIndex)
			throws JargonException, JargonQueryException;

	/**
	 * Execute an iquest-like query and return results in a convenient POJO object.
	 * This query allows specification of the target zone, so that queries may be
	 * run on federated zones.
	 * <p>
	 * Note: this command will not close the underlying result set, so that it may
	 * be paged by getting next result. It is up to the caller to call
	 * {@code closeResults()} when done with the result set. Alternately, the
	 * {@code executeIRODSQueryAndCloseResults()} method may be employed.
	 *
	 * @param irodsQuery
	 *            {@link AbstractIRODSGenQuery} that will wrap the given iquest-like
	 *            query
	 * @param continueIndex
	 *            {@code int} that indicates whether this is a requery when more
	 *            resuts than the limit have been generated
	 * @param zoneName
	 *            {@code String} or empty
	 *
	 * @return {@link IRODSQueryResultSet} that contains the results of the query
	 * @throws JargonException
	 *             for iRODS error
	 * @throws JargonQueryException
	 *             for query error
	 */
	IRODSQueryResultSet executeIRODSQueryInZone(AbstractIRODSGenQuery irodsQuery, int continueIndex, String zoneName)
			throws JargonException, JargonQueryException;

	/**
	 * Execute an iquest-like query and return results in a convenient POJO object.
	 * This method allows partial starts to do paging of large query results
	 *
	 * @param irodsQuery
	 *            {@link AbstractIRODSGenQuery} that will wrap the given iquest-like
	 *            query
	 * @param partialStartIndex
	 *            {@code int} that indicates an offset within the results from which
	 *            to build the returned result set.
	 * @return {@link IRODSQueryResultSet} that contains the results of the query
	 * @throws JargonException
	 *             for iRODS error
	 * @throws JargonQueryException
	 *             for query error
	 */
	IRODSQueryResultSet executeIRODSQueryWithPaging(final AbstractIRODSGenQuery irodsQuery, final int partialStartIndex)
			throws JargonException, JargonQueryException;

	/**
	 * Execute an iquest-like query and return results in a convenient POJO object.
	 * This method allows partial starts to do paging of large query results.
	 * <p>
	 * This version of the method allows the optional specification of a zone to run
	 * the query in.
	 *
	 * @param irodsQuery
	 *            {@link AbstractIRODSGenQuery} that will wrap the given iquest-like
	 *            query
	 * @param partialStartIndex
	 *            {@code int} that indicates an offset within the results from which
	 *            to build the returned result set.
	 * @param zoneName
	 *            {@code String} ({@code null} or blank if not used) that indicates
	 *            an optional zone for the query
	 * @return {@link IRODSQueryResultSet} that contains the results of the query
	 * @throws JargonException
	 *             for iRODS error
	 * @throws JargonQueryException
	 *             for query error
	 */
	IRODSQueryResultSet executeIRODSQueryWithPagingInZone(AbstractIRODSGenQuery irodsQuery, int partialStartIndex,
			String zoneName) throws JargonException, JargonQueryException;

	/**
	 * Execute a re-query meant to retrieve more results. The previous result set
	 * contains information to re-query iRODS. This query is targeted at a specific
	 * zone
	 *
	 * @param irodsQueryResultSet
	 *            {@link IRODSQueryResultSet} that contains the results of the
	 *            previous query.
	 * @param zoneName
	 *            {@code String} with the zone for the query
	 * @return {@code IRODSQueryResultSet} containing the previous batch of query
	 *         results.
	 * @throws JargonException
	 *             for iRODS error
	 * @throws JargonQueryException
	 *             for query error
	 */
	IRODSQueryResultSet getMoreResultsInZone(IRODSQueryResultSet irodsQueryResultSet, String zoneName)
			throws JargonException, JargonQueryException;

	/**
	 * Close the result set that had been continued
	 *
	 * @param resultSet
	 *            {@link IRODSQueryResultSet} that contains the results of the
	 *            previous query.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void closeResults(IRODSQueryResultSet resultSet) throws JargonException;

	/**
	 * Execute an iquest-like query and return results in a convenient POJO object.
	 * This method allows partial starts to do paging of large query results. This
	 * method will send a close to iRODS if more results are available.
	 * <p>
	 * Note that the {@code getMoreResults()} method will not work, since the result
	 * set was closed. This version of the query execute is suitable for 'session
	 * per request' situations, such as mid-tier web applications, where connections
	 * are not held for stateful interaction. In these situations, query can be
	 * accomplished with an offset.
	 *
	 * @param irodsQuery
	 *            {@link AbstractIRODSGenQuery} that will wrap the given query
	 * @param partialStartIndex
	 *            {@code int} that indicates an offset within the results from which
	 *            to build the returned result set.
	 * @return {@link IRODSQueryResultSet} that contains the results of the query
	 * @throws JargonException
	 *             for iRODS error
	 * @throws JargonQueryException
	 *             for query error
	 */
	IRODSQueryResultSet executeIRODSQueryAndCloseResult(AbstractIRODSGenQuery irodsQuery, int partialStartIndex)
			throws JargonException, JargonQueryException;

	/**
	 * Execute an iquest-like query and return results in a convenient POJO object.
	 * This method allows partial starts to do paging of large query results. This
	 * method will send a close to iRODS if more results are available.
	 * <p>
	 * This method allows optional specification of a target zone for the query.
	 * <p>
	 * Note that the {@code getMoreResults()} method will not work, since the result
	 * set was closed. This version of the query execute is suitable for 'session
	 * per request' situations, such as mid-tier web applications, where connections
	 * are not held for stateful interaction. In these situations, query can be
	 * accomplished with an offset.
	 *
	 * @param irodsQuery
	 *            {@link AbstractIRODSGenQuery} that will wrap the given query
	 * @param partialStartIndex
	 *            {@code int} that indicates an offset within the results from which
	 *            to build the returned result set.
	 * @param zoneName
	 *            {@code String} ({@code null} or blank if not used) that indicates
	 *            an optional zone for the query
	 * @return {@link IRODSQueryResultSet} that contains the results of the query
	 * @throws JargonException
	 *             for iRODS error
	 * @throws JargonQueryException
	 *             for query error
	 */
	IRODSQueryResultSet executeIRODSQueryAndCloseResultInZone(AbstractIRODSGenQuery irodsQuery, int partialStartIndex,
			String zoneName) throws JargonException, JargonQueryException;

	/**
	 * Get the next page of results
	 * 
	 * @param irodsQueryResultSet
	 *            {@link IRODSQueryResultSet} with the previous results
	 * @return {@link IRODSQueryResultSet} with the next set of results
	 * @throws JargonException
	 *             for iRODS error
	 * @throws JargonQueryException
	 *             for query error
	 */
	IRODSQueryResultSet getMoreResults(IRODSQueryResultSet irodsQueryResultSet)
			throws JargonException, JargonQueryException;

}
