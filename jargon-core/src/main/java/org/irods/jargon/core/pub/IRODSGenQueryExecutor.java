/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.IRODSQuery;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;

/**
 * Access object to process 'iquest-like' GenQuery.
 * 
 * This access object can accept a query in iquest form, and will process that
 * query and return a result set with the query response and other related
 * information.
 * 
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface IRODSGenQueryExecutor {

	/**
	 * Execute an iquest-like query and return results in a convenient POJO
	 * object.
	 * 
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSQuery} that will wrap
	 *            the given iquest-like query
	 * @param continueIndex
	 *            <code>int</code> that indicates whether this is a requery when
	 *            more resuts than the limit have been generated
	 * @return {@link org.irods.jargon.core.query.IRODSQueryResultSet} that
	 *         contains the results of the query
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	IRODSQueryResultSet executeIRODSQuery(final IRODSQuery irodsQuery,
			final int continueIndex) throws JargonException,
			JargonQueryException;

	/**
	 * Execute an iquest-like query and return results in a convenient POJO
	 * object. This method allows partial starts to do paging of large query
	 * results
	 * 
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSQuery} that will wrap
	 *            the given iquest-like query
	 * @param partialStartIndex
	 *            <code>int</code> that indicates an offset within the results
	 *            from which to build the returned result set.
	 * @return {@link org.irods.jargon.core.query.IRODSQueryResultSet} that
	 *         contains the results of the query
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	IRODSQueryResultSet executeIRODSQueryWithPaging(
			final IRODSQuery irodsQuery, final int partialStartIndex)
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

}
