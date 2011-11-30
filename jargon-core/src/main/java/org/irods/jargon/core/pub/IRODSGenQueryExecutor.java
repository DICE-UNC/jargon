/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;

/**
 * Access object to process 'iquest-like' GenQuery.
 * <p/>
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
	 * Execute an iquest-like query and return results in a convenient POJO
	 * object.
	 * <p/>
	 * Note: this command will not close the underlying result set, so that it
	 * may be paged by getting next result. It is up to the caller to call
	 * <code>closeResults()</code> when done with the result set. Alternately,
	 * the <code>executeIRODSQueryAndCloseResults()</code> method may be
	 * employed.
	 * 
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSGenQuery} that will
	 *            wrap the given iquest-like query
	 * @param continueIndex
	 *            <code>int</code> that indicates whether this is a requery when
	 *            more resuts than the limit have been generated
	 * @return {@link org.irods.jargon.core.query.IRODSQueryResultSet} that
	 *         contains the results of the query
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	IRODSQueryResultSet executeIRODSQuery(final IRODSGenQuery irodsQuery,
			final int continueIndex) throws JargonException,
			JargonQueryException;
	
	/**
	 * Execute an iquest-like query and return results in a convenient POJO
	 * object.  This query allows specification of the target zone, so that queries may be run on federated zones.
	 * <p/>
	 * Note: this command will not close the underlying result set, so that it
	 * may be paged by getting next result. It is up to the caller to call
	 * <code>closeResults()</code> when done with the result set. Alternately,
	 * the <code>executeIRODSQueryAndCloseResults()</code> method may be
	 * employed.
	 * 
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSGenQuery} that will
	 *            wrap the given iquest-like query
	 * @param continueIndex
	 *            <code>int</code> that indicates whether this is a requery when
	 *            more resuts than the limit have been generated
	 * @param zoneName <code>String</code> (<code>null</code> or blank if not used) that indicates an optional zone for the query
	 * @return {@link org.irods.jargon.core.query.IRODSQueryResultSet} that
	 *         contains the results of the query
	 * @throws JargonException
	 * @throws JargonQueryException
	
	 */
	IRODSQueryResultSet executeIRODSQueryInZone(IRODSGenQuery irodsQuery,
			int continueIndex, String zoneName) throws JargonException,
			JargonQueryException;

	/**
	 * Execute an iquest-like query and return results in a convenient POJO
	 * object. This method allows partial starts to do paging of large query
	 * results
	 * 
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSGenQuery} that will
	 *            wrap the given iquest-like query
	 * @param partialStartIndex
	 *            <code>int</code> that indicates an offset within the results
	 *            from which to build the returned result set.
	 * @return {@link org.irods.jargon.core.query.IRODSQueryResultSet} that
	 *         contains the results of the query
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	IRODSQueryResultSetInterface executeIRODSQueryWithPaging(
			final IRODSGenQuery irodsQuery, final int partialStartIndex)
			throws JargonException, JargonQueryException;
	
	/**
	 * Execute an iquest-like query and return results in a convenient POJO
	 * object. This method allows partial starts to do paging of large query
	 * results.
	 * <p/>
	 * This version of the method allows the optional specification of a zone to run the query in.
	 * 
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSGenQuery} that will
	 *            wrap the given iquest-like query
	 * @param partialStartIndex
	 *            <code>int</code> that indicates an offset within the results
	 *            from which to build the returned result set.
	 *  @param zoneName <code>String</code> (<code>null</code> or blank if not used) that indicates an optional zone for the query
	 * @return {@link org.irods.jargon.core.query.IRODSQueryResultSet} that
	 *         contains the results of the query
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	IRODSQueryResultSetInterface executeIRODSQueryWithPagingInZone(
			IRODSGenQuery irodsQuery, int partialStartIndex, String zoneName)
			throws JargonException, JargonQueryException;
	

	/**
	 * Execute a requery meant to retrieve more results. The previous result set
	 * contains information to requery iRODS.
	 * 
	 * @param irodsQueryResultSet
	 *            {@link org.irods.jargon.core.query.IRODSQueryResultSet} that
	 *            contains the results of the previous query.
	 * @return <code>IRODSQueryResultSet</code> containing the previous batch of
	 *         query results.
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	IRODSQueryResultSet getMoreResults(IRODSQueryResultSet irodsQueryResultSet)
			throws JargonException, JargonQueryException;

	/**
	 * Close the result set that had been continued
	 * 
	 * @param resultSet
	 *            {@link org.irods.jargon.core.query.IRODSQueryResultSetInterface} that
	 *            contains the results of the previous query.
	 * @throws JargonException
	 */
	void closeResults(IRODSQueryResultSetInterface resultSet)
			throws JargonException;

	/**
	 * Execute an iquest-like query and return results in a convenient POJO
	 * object. This method allows partial starts to do paging of large query
	 * results. This method will send a close to iRODS if more results are
	 * available.
	 * <p/>
	 * Note that the <code>getMoreResults()</code> method will not work, since
	 * the result set was closed. This version of the query execute is suitable
	 * for 'session per request' situations, such as mid-tier web applications,
	 * where connections are not held for stateful interaction. In these
	 * situations, query can be accomplished with an offset.
	 * 
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSGenQuery} that will
	 *            wrap the given iquest-like query
	 * @param partialStartIndex
	 *            <code>int</code> that indicates an offset within the results
	 *            from which to build the returned result set.
	 * @return {@link org.irods.jargon.core.query.IRODSQueryResultSet} that
	 *         contains the results of the query
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	IRODSQueryResultSet executeIRODSQueryAndCloseResult(
			IRODSGenQuery irodsQuery, int continueIndex)
			throws JargonException, JargonQueryException;

	/**
	 * Execute an iquest-like query and return results in a convenient POJO
	 * object. This method allows partial starts to do paging of large query
	 * results. This method will send a close to iRODS if more results are
	 * available.
	 * <p/>
	 * This method allows optional specification of a target zone for the query.
	 * <p/>
	 * Note that the <code>getMoreResults()</code> method will not work, since
	 * the result set was closed. This version of the query execute is suitable
	 * for 'session per request' situations, such as mid-tier web applications,
	 * where connections are not held for stateful interaction. In these
	 * situations, query can be accomplished with an offset.
	 * 
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSGenQuery} that will
	 *            wrap the given iquest-like query
	 * @param partialStartIndex
	 *            <code>int</code> that indicates an offset within the results
	 *            from which to build the returned result set.
	 * @param zoneName <code>String</code> (<code>null</code> or blank if not used) that indicates an optional zone for the query
	 * @return {@link org.irods.jargon.core.query.IRODSQueryResultSet} that
	 *         contains the results of the query
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	IRODSQueryResultSet executeIRODSQueryAndCloseResultInZone(
			IRODSGenQuery irodsQuery, int continueIndex, String zoneName)
			throws JargonException, JargonQueryException;

	



}
