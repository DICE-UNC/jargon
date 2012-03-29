package org.irods.jargon.core.connection;

import org.irods.jargon.core.connection.auth.AuthenticationFactory;
import org.irods.jargon.core.exception.JargonException;

public interface IRODSProtocolManager {

	/**
	 * Get the factory object that will create various authentication methods on
	 * demand
	 * 
	 * @return {@link AuthenticationFactory} that will create objects that can
	 *         authenticate <code>iRODSAccount</code>s
	 */
	AuthenticationFactory getAuthenticationFactory();

	/**
	 * Inject a factory that will be used to authentication
	 * <code>IRODSAccount</code>s when a new connection needs to be made
	 * 
	 * @param authenticationFactory
	 *            {@link AuthenticationFactory} that will create objects that
	 *            can authenticate <code>iRODSAccount</code>s
	 */
	void setAuthenticationFactory(
			final AuthenticationFactory authenticationFactory);

	/**
	 * For an account provided by the caller, return an open IRODS connection.
	 * This may be created new, cached from previous connection by the same
	 * user, or from a pool.
	 * 
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} that tunes the i/o pipeline and
	 *            other connection options
	 */
	IRODSCommands getIRODSProtocol(final IRODSAccount irodsAccount,
			PipelineConfiguration pipelineConfiguration) throws JargonException;

	/**
	 * This method is called by a client when the connection is no longer
	 * needed. The connection may be closed, or returned to a pool.
	 * 
	 * @param irodsConnection
	 *            an <code>IRODSConnection</code> that represents an open
	 *            session with IRODS that is to be returned
	 */
	void returnIRODSConnection(IRODSManagedConnection irodsConnection)
			throws JargonException;

	/**
	 * A connection is returned to the connection manager with an IO Exception.
	 * This can indicate a problem with the underlying socket, and the
	 * connection manager may choose to abandon the connection and
	 * re-initialize.
	 * 
	 * This implementation of a connection manager will do a callback to the
	 * {@link IRODSConnection IRODSConnection} and the connection will be
	 * closed.
	 * 
	 * @see org.irods.jargon.core.connection.IRODSConnectionManager#returnIRODSConnection(org.irods.jargon.core.connection.IRODSConnection)
	 */

	void returnConnectionWithIoException(IRODSManagedConnection irodsConnection);

	void destroy() throws JargonException;

	void initialize() throws JargonException;

}