/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;

/**
 * Abstract superclass for a factory for iRODS connections. This factory will
 * produce instances of {@link AbstractConnection} which represent the lowest
 * networking layer in Jargon.
 * <p/>
 * Other implementations may follow, such as an nio layer.
 * 
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 * 
 */
public abstract class IRODSConnectionFactory {

	protected IRODSConnectionFactory() {
	};

	/**
	 * Create the basic network layer connection to iRODS
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} describing the connection host and
	 *            principal
	 * @param irodsSession
	 *            {@link IRDOSSession} that is producing this connection
	 * @return
	 * @throws JargonException
	 */
	protected abstract AbstractConnection instance(
			final IRODSAccount irodsAccount, final IRODSSession irodsSession)
			throws JargonException;

}
