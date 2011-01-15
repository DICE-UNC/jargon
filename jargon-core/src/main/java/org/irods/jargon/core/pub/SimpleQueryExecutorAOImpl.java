/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;

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
public class SimpleQueryExecutorAOImpl extends IRODSGenericAO implements SimpleQueryExecutorAO {

	/**
	 * Standard constructor for access objects.
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected SimpleQueryExecutorAOImpl(IRODSSession irodsSession,
			IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

}
