/**
 *
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This connection manager simply returns a stand-alone connection to IRODS.
 * based on the information in the IRODSAccount.
 * <p/>
 * This particular implementation has no shared data between connections that
 * are created, and as such does not need to be thread-safe. A pooled connection
 * manager will need to process getting and returning connections from multiple
 * threads.
 * 
 * @author Mike Conway - DICE
 */
public final class IRODSSimpleProtocolManager implements IRODSProtocolManager {

	private Logger log = LoggerFactory
			.getLogger(IRODSSimpleProtocolManager.class);

	public static IRODSProtocolManager instance() {
		return new IRODSSimpleProtocolManager();
	}

	private IRODSSimpleProtocolManager() {
		log.info("creating simple protocol manager");
	}

	/**
	 * This implementation simply creates a connection to IRODS. This is a live
	 * connection that represents not only an open socket to the IRODS server,
	 * but also a 'connected' connection, meaning that the startup and handshake
	 * activities have been accomplished, leaving the IRODSProtocol in a ready
	 * state.
	 * <p/>
	 * This method also is the 'hook' that would allow alternative login
	 * methods, such as GSI, to be fully resolved, and all necessary
	 * transformations to the irodsAccount information will be accomplished when
	 * this method returns.
	 * <p/>
	 * @see org.irods.jargon.core.connection.IRODSProtocolManager#getIRODSProtocol
	 *      (org.irods.jargon.core.domain.IRODSAccount)
	 */
	@Override
	public IRODSCommands getIRODSProtocol(final IRODSAccount irodsAccount)
			throws JargonException {
		log.debug("creating an IRODSSimpleConnection for account:"
				+ irodsAccount);

		return IRODSCommands.instance(irodsAccount, this);
	}

	/**
	 * A connection is returned to the connection manager. This implementation
	 * of a connection manager will do a callback to the {@link IRODSConnection
	 * IRODSConnection} and the connection will be closed. Other implementations
	 * may return the connection to a pool.
	 * <p/>
	 * @see org.irods.jargon.core.connection.IRODSConnectionManager#returnIRODSConnection
	 *      (org.irods.jargon.core.connection.IRODSConnection)
	 */

	@Override
	public void returnIRODSConnection(
			final IRODSManagedConnection irodsConnection)
			throws JargonException {
		log.debug("connection returned:" + irodsConnection);
		irodsConnection.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSProtocolManager#
	 * returnConnectionWithIoException
	 * (org.irods.jargon.core.connection.IRODSManagedConnection)
	 */
	@Override
	public void returnConnectionWithIoException(
			final IRODSManagedConnection irodsConnection) {
		if (irodsConnection != null) {
			irodsConnection.obliterateConnectionAndDiscardErrors();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSProtocolManager#destroy()
	 */
	@Override
	public void destroy() throws JargonException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.IRODSProtocolManager#initialize()
	 */
	@Override
	public void initialize() throws JargonException {

	}

}
