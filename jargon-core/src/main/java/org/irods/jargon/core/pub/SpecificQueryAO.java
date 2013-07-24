package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.SpecificQueryDefinition;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.SpecificQuery;
import org.irods.jargon.core.query.SpecificQueryResultSet;

/**
 * Interface for an object to interact with specific query in IRODS.
 * 
 * @author Lisa Stillwell RENCI (www.renci.org)
 * 
 **/

public interface SpecificQueryAO extends IRODSAccessObject {

	/**
	 * Add a specific query to iRODS
	 * 
	 * @param sqlQuery
	 *            <code>String</code> with the a valid SQL query
	 * @param alias
	 *            <code>String</code> with a unique alias name for this SQL
	 *            query
	 * @throws IllegalArgumentException
	 * @throws DuplicateDataException
	 */
	void addSpecificQuery(SpecificQueryDefinition specificQuery)
			throws JargonException, DuplicateDataException;

	/**
	 * Remove a specific query from iRODS
	 * 
	 * @param specificQuery
	 *            {@link org.irods.jargon.core.pub.domain.SpecificQueryDefinition}
	 *            to be added to iRODS.
	 * @throws IllegalArgumentException
	 * @throws DuplicateDataException
	 */
	void removeSpecificQuery(SpecificQueryDefinition specificQuery)
			throws JargonException;

	/**
	 * Remove a specific query from iRODS using alias name as identifier
	 * 
	 * @param alias
	 *            <code>String</code> with a unique alias name for this SQL
	 *            query
	 * @throws IllegalArgumentException
	 * @throws DuplicateDataException
	 */
	void removeSpecificQueryByAlias(String alias) throws JargonException;

	/**
	 * Remove a specific query from iRODS using SQL query as identifier
	 * <p>
	 * Please note that this method will remove all existing Specific Queries
	 * that match the provided SQL query String
	 * 
	 * @param sqlQuery
	 *            <code>String</code> with the a valid SQL query
	 * @throws IllegalArgumentException
	 * @throws DuplicateDataException
	 * 
	 */
	void removeAllSpecificQueryBySQL(String sqlQuery) throws JargonException,
			DuplicateDataException;

	/**
	 * Execute a specific query by providing the alias that the sql had been
	 * registered under. These queries contain an sql statement that can include
	 * bind parameters. This method allows the optional specification of those
	 * parameters. Note that this variant of the query will not close the query
	 * out, instead, it supports paging by the specification of the
	 * <code>continueIndex</code> that may have been returned in a previous
	 * query paging call.
	 * <p/>
	 * Note that a <code>DataNotFoundException</code> will occur if the query
	 * alias is not found.
	 * 
	 * @param specificQuery
	 *            {@link SpecificQuery} that defines the query alias or sql, and
	 *            any associated parameters to use
	 * @param maxRows
	 *            <code>int</code> with the maximum number of rows to return.
	 *            Note that setting this to 0 causes the query to close
	 *            automatically.
	 * @return {@link IRODSQueryResultSet} implementation with the result rows
	 *         and other information from the invocation of the query
	 * @throws DataNotFoundException
	 *             if the alias cannot be located
	 * @throws JargonException
	 *             general exception
	 * @throws JargonQueryException
	 *             exception in the forumulation of the query
	 */
	SpecificQueryResultSet executeSpecificQueryUsingAlias(
			SpecificQuery specificQuery, int maxRows)
			throws DataNotFoundException, JargonException, JargonQueryException;

	/**
	 * Execute a specific query by providing the alias that the sql had been
	 * registered under. These queries contain an sql statement that can include
	 * bind parameters. This method allows the optional specification of those
	 * parameters. Note that this variant of the query will not close the query
	 * out, instead, it supports paging by the specification of the
	 * <code>continueIndex</code> that may have been returned in a previous
	 * query paging call.
	 * <p/>
	 * Note that a <code>DataNotFoundException</code> will occur if the query
	 * alias is not found.
	 * 
	 * <p/>
	 * 
	 * This variant supports a common user practice with specific query, where
	 * the provided SQL has parameterized options for offset and result set
	 * size, for example, this query:
	 * 
	 * <code>
	 * SELECT c.parent_coll_name, c.coll_name, c.create_ts, c.modify_ts, c.coll_id, c.coll_owner_name, c.coll_owner_zone, c.coll_type,
	 *  u.user_name, u.zone_name, a.access_type_id, u.user_id FROM r_coll_main c JOIN r_objt_access a ON c.coll_id = a.object_id 
	 *  JOIN r_user_main u ON a.user_id = u.user_id WHERE c.parent_coll_name = ? LIMIT ? OFFSET ?
	 * </code>
	 * 
	 * The 'userDefinedOffset' is a clue that sends this OFFSET value to the
	 * code that prepares the returned result set, so that the 'count' fields in
	 * the result accurately reflect the
	 * 
	 * 
	 * @param specificQuery
	 *            {@link SpecificQuery} that defines the query alias or sql, and
	 *            any associated parameters to use
	 * @param maxRows
	 *            <code>int</code> with the maximum number of rows to return.
	 *            Note that setting this to 0 causes the query to close
	 *            automatically.
	 * @param userDefinedOffset
	 *            <code>int</code> that represents an offset to use in the
	 *            returned record counts that is enforced within the sql itself.
	 *            This is used because users often use LIMIT and OFFSET
	 *            statements inside the actual SQL to accomplish custom paging.
	 *            This allows the result set to reflect any user supplied
	 *            offsets
	 * @return {@link IRODSQueryResultSet} implementation with the result rows
	 *         and other information from the invocation of the query
	 * @throws DataNotFoundException
	 *             if the alias cannot be located
	 * @throws JargonException
	 *             general exception
	 * @throws JargonQueryException
	 *             exception in the forumulation of the query
	 */
	SpecificQueryResultSet executeSpecificQueryUsingAlias(
			SpecificQuery specificQuery, int maxRows, int userDefinedOffset)
			throws DataNotFoundException, JargonException, JargonQueryException;

	/**
	 * Execute a specific query by providing the exact sql that was registered
	 * in iRODS. These queries contain an sql statement that can include bind
	 * parameters. This method allows the optional specification of those
	 * parameters. Note that this variant of the query will not close the query
	 * out, instead, it supports paging by the specification of the
	 * <code>continueIndex</code> that may have been returned in a previous
	 * query paging call.
	 * 
	 * @param specificQuery
	 *            {@link SpecificQuery} that defines the query alias or sql, and
	 *            any associated parameters to use
	 * @param maxRows
	 *            <code>int</code> with the maximum number of rows to return.
	 *            Note that setting this to 0 causes the query to close
	 *            automatically.
	 * @return {@link IRODSQueryResultSet} implementation with the result rows
	 *         and other information from the invocation of the query
	 * @throws DataNotFoundException
	 *             if the alias cannot be located
	 * @throws JargonException
	 *             general exception
	 * @throws JargonQueryException
	 *             exception in the forumulation of the query
	 */
	SpecificQueryResultSet executeSpecificQueryUsingSql(
			SpecificQuery specificQuery, int maxRows)
			throws DataNotFoundException, JargonException, JargonQueryException;

	/**
	 * Execute a specific query by providing the exact sql that was registered
	 * in iRODS. These queries contain an sql statement that can include bind
	 * parameters. This method allows the optional specification of those
	 * parameters. Note that this variant of the query will not close the query
	 * out, instead, it supports paging by the specification of the
	 * <code>continueIndex</code> that may have been returned in a previous
	 * query paging call.
	 * 
	 * @param specificQuery
	 *            {@link SpecificQuery} that defines the query alias or sql, and
	 *            any associated parameters to use
	 * @param maxRows
	 *            <code>int</code> with the maximum number of rows to return.
	 *            Note that setting this to 0 causes the query to close
	 *            automatically.
	 * @param userDefinedOffset
	 *            <code>int</code> that represents an offset to use in the
	 *            returned record counts that is enforced within the sql itself.
	 *            This is used because users often use LIMIT and OFFSET
	 *            statements inside the actual SQL to accomplish custom paging.
	 *            This allows the result set to reflect any user supplied
	 *            offsets
	 * @return {@link IRODSQueryResultSet} implementation with the result rows
	 *         and other information from the invocation of the query
	 * @throws DataNotFoundException
	 *             if the alias cannot be located
	 * @throws JargonException
	 *             general exception
	 * @throws JargonQueryException
	 *             exception in the forumulation of the query
	 */
	SpecificQueryResultSet executeSpecificQueryUsingSql(
			SpecificQuery specificQuery, int maxRows, int userDefinedOffset)
			throws DataNotFoundException, JargonException, JargonQueryException;

	/**
	 * Given a portion of a query alias, find matching specific queries as
	 * stored in iRODS. Note that wildcards in the 'like' statement are not
	 * imposed by this method and must be provided by the caller in the provided
	 * <code>specificQueryAlias</code>.
	 * 
	 * @param specificQueryAlias
	 *            <code>String</code> with a part of a query alias to search
	 *            for.
	 * @return <code>List</code> of {@link SpecificQueryDefinition}
	 * @throws DataNotFoundException
	 *             if no queries found with a matching alias
	 * @throws JargonException
	 */
	List<SpecificQueryDefinition> listSpecificQueryByAliasLike(
			String specificQueryAlias) throws DataNotFoundException,
			JargonException;

	/**
	 * Given a portion of a query alias, find matching specific queries as
	 * stored in iRODS. This variant allows provision of a zohe hint that
	 * indicates which federated zone to query. Note that wildcards in the
	 * 'like' statement are not imposed by this method and must be provided by
	 * the caller in the provided <code>specificQueryAlias</code>.
	 * 
	 * @param specificQueryAlias
	 *            <code>String</code> with a part of a query alias to search
	 *            for.
	 * @param zoneHint
	 *            <code>String</code> with a zone hint used to decide which
	 *            federated zone to query. Note that this should be set to blank
	 *            if not used
	 * 
	 * @return <code>List</code> of {@link SpecificQueryDefinition}
	 * @throws DataNotFoundException
	 *             if no queries found with a matching alias
	 * @throws JargonException
	 */
	List<SpecificQueryDefinition> listSpecificQueryByAliasLike(
			String specificQueryAlias, String zoneHint)
			throws DataNotFoundException, JargonException;

	/**
	 * Given a specific query alias name, return the associated specific query
	 * definition information.
	 * 
	 * @param specificQueryAlias
	 *            <code>String</code> with the given alias for the query
	 * @return {@list SpecificQueryDefinition} with details about the given
	 *         query
	 * @throws DataNotFoundException
	 *             if the query with the given alias cannot be found
	 * @throws JargonException
	 */
	SpecificQueryDefinition findSpecificQueryByAlias(String specificQueryAlias)
			throws DataNotFoundException, JargonException;

	/**
	 * Given a specific query alias name, return the associated specific query
	 * definition information.
	 * 
	 * @param specificQueryAlias
	 *            <code>String</code> with the given alias for the query
	 * @param zoneHint
	 *            <code>String</code> with a zone hint used to decide which
	 *            federated zone to query. Note that this should be set to blank
	 *            if not used
	 * @return {@list SpecificQueryDefinition} with details about the given
	 *         query
	 * @throws DataNotFoundException
	 *             if the query with the given alias cannot be found
	 * @throws JargonException
	 */
	SpecificQueryDefinition findSpecificQueryByAlias(String specificQueryAlias,
			String zoneHint) throws DataNotFoundException, JargonException;

	/**
	 * Check and see if, as a result of previous requests, or based on the
	 * server version, I know that the jargon specific queries required to
	 * support specific query via this API are not available. This method will
	 * return <code>true</code> only if I know that the support is not there. If
	 * I have not checked previously, or I am not using the dynamic properties
	 * cache, which is configured via {@link JargonProperties}, then a
	 * <code>false</code> will be returned.
	 * 
	 * @return <code>boolean</code> that will only be <code>true</code> if I
	 *         know that the jargon specific query support is not configured.
	 *         This can be used to determine whether it is worth bothering to
	 *         issue such requests.
	 *         <p/>
	 *         Currently, this still needs to be wired into the specific query
	 *         support, so consider this experimental
	 */
	boolean isSpecificQueryToBeBypassed() throws JargonException;

}
