/**
 *
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
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
	 */
	@Override
	public IRODSCommands getIRODSProtocol(final IRODSAccount irodsAccount,
			final PipelineConfiguration pipelineConfiguration)
			throws JargonException {
		log.debug("creating an IRODSSimpleConnection for account:{}",
				irodsAccount);

		return IRODSCommands
				.instance(irodsAccount, this, pipelineConfiguration);
	}

	/**
	 * A connection is returned to the connection manager. This implementation
	 * of a connection manager will do a call-back to the
	 * {@link IRODSManagedConnection } and the connection will be closed. Other
	 * implementations may return the connection to a pool.
	 * <p/>
	 * 
	 * @see org.irods.jargon.core.connection.IRODSConnectionManager#returnIRODSConnection
	 *      (org.irods.jargon.core.connection.IRODSConnection)
	 */

	@Override
	public void returnIRODSConnection(
			final IRODSManagedConnection irodsConnection)
			throws JargonException {
		log.debug("connection returned:{}", irodsConnection);
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
		log.warn("connection returned with IOException, will forcefully close and remove from session cache");
		if (irodsConnection != null) {
			irodsConnection.obliterateConnectionAndDiscardErrors();
			try {
				irodsConnection.getIrodsSession().discardSessionForErrors(
						irodsConnection.getIrodsAccount());
			} catch (JargonException e) {
				log.error("unable to obliterate connection");
				throw new JargonRuntimeException(
						"unable to obliterate connection", e);
			}
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
