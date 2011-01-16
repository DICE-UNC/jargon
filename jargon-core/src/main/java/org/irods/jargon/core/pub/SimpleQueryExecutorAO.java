/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.SimpleQuery;

/**
 * Access object to execute queries using the iRODS Simple Query facility.  This is mainly used for administrative queries, as in the <code>iadmin</code> 
 * icommand.  Typically these commands require <code>rodsadmin</code>, and will fail if executed without admin rights.
 * <p/>
 * Simple Query allows the the execution of queries as parameterized SQL.  These SQL statements are pre-loaded in iRODS and validated before being allowed to
 * run.  Other techniques, such as GenQuery, and the new SpecialQuery facility, allow other methods of querying system and user metadata from the iCAT.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface SimpleQueryExecutorAO {

	/**
	 * Execute a simpleQuery and return a result set.
	 * @param simpleQuery {@link SimpleQuery} with special, permitted SQL to run on iRODS.
	 * @return {@link IRODSQueryResultSetInterface} that contains the result of the query.
	 * @throws JargonException
	 */
	IRODSQueryResultSetInterface executeSimpleQuery(SimpleQuery simpleQuery)
			throws JargonException;

}
