package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;

/**
 * Implements iRODS Specific Query, which is a special type of query where SQL may be identified as 
 * allowed to execute against the iCAT.  These specific queries are appropriate where GenQuery does not provide
 * an expressive enough language.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SpecificQueryExecutorAOImpl extends IRODSGenericAO implements SpecificQueryExcecutorAO {

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected SpecificQueryExecutorAOImpl(IRODSSession irodsSession,
			IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

}
