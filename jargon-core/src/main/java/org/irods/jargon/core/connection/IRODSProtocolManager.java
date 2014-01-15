package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IRODSProtocolManager {

	private AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();

	Logger log = LoggerFactory.getLogger(IRODSProtocolManager.class);

	/**
	 * Get the factory object that will create various authentication methods on
	 * demand
	 * 
	 * @return {@link AuthenticationFactory} that will create objects that can
	 *         authenticate <code>iRODSAccount</code>s
	 */
	public AuthenticationFactory getAuthenticationFactory() {
		return authenticationFactory;
	}

	/**
	 * Inject a factory that will be used to authentication
	 * <code>IRODSAccount</code>s when a new connection needs to be made
	 * 
	 * @param authenticationFactory
	 *            {@link AuthenticationFactory} that will create objects that
	 *            can authenticate <code>iRODSAccount</code>s
	 */
	public void setAuthenticationFactory(
			final AuthenticationFactory authenticationFactory) {
		if (authenticationFactory == null) {
			throw new IllegalArgumentException("null authenticationFactory");
		}
		this.authenticationFactory = authenticationFactory;
	}

	/**
	 * A connection is returned to the connection manager with an IO Exception.
	 * This can indicate a problem with the underlying socket, and the
	 * connection manager may choose to abandon the connection and
	 * re-initialize.
	 * 
	 * This implementation of a connection manager will do a callback to the
	 * {@link IRODSBasicTCPConnection IRODSConnection} and the connection will
	 * be closed.
	 * 
	 * @see org.irods.jargon.core.connection.IRODSConnectionManager#returnIRODSConnection(org.irods.jargon.core.connection.AbstractConnection)
	 */

	public void returnConnectionWithForce(
			final AbstractConnection irodsConnection) {
		log.warn("connection returned with force, will forcefully close and remove from session cache");
		if (irodsConnection != null) {
			irodsConnection.obliterateConnectionAndDiscardErrors();
			try {
				if (irodsConnection.getIrodsSession() == null) {
					log.info("returning connection, no session, so do not discard in session, this can be a normal case in authentication processing, or in areas where a connection is manually done outside of the normal access object factory scheme, otherwise, it might signify a logic error");
				} else {
					irodsConnection.getIrodsSession().discardSessionForErrors(
							irodsConnection.getIrodsAccount());
				}
			} catch (JargonException e) {
				log.error("unable to obliterate connection");
				throw new JargonRuntimeException(
						"unable to obliterate connection", e);
			}
		}
	}

	/**
	 * For an account provided by the caller, return an open IRODS connection.
	 * This may be created new, cached from previous connection by the same
	 * user, or from a pool.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that defines the connection
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} that tunes the i/o pipeline and
	 *            other connection options
	 * @params irodsSession {@link IRODSSession} that will manage this
	 *         connection and cache information
	 */
	public abstract IRODSMidLevelProtocol getIRODSProtocol(
			IRODSAccount irodsAccount,
			PipelineConfiguration pipelineConfiguration,
			IRODSSession irodsSession) throws JargonException;

	// here's where the biz happens

	/**
	 * Called by a client that no longer needs the connection to iRODS. This
	 * signals a normal close from the higher level API. The
	 * <code>IRODSProtocolManager</code> can then decide how to treat a normally
	 * returned connection. This can either be shut down, returned to a cache or
	 * pool, or any other sort of custom behavior.
	 * 
	 * @param abstractIRODSMidLevelProtocol
	 * @throws JargonException
	 */
	public abstract void returnIRODSProtocol(
			AbstractIRODSMidLevelProtocol abstractIRODSMidLevelProtocol)
			throws JargonException;

	/**
	 * Abandon a connection to iRODS by shutting down the socket, and ensure
	 * that the session is cleared.
	 * <p/>
	 * This is called from a client that no longer needs the connection, and
	 * wishes to signal that there was an error or other condition that gives
	 * reason to suspect that the agent or connection is corrupted and should
	 * not be returned to a cache or pool.
	 * <p/>
	 * There also exists a method that can do the same operation usign the
	 * low-level <code>AbstractIRODSConnection</code>. That method is used
	 * internally.
	 * 
	 * @param abstractIRODSMidLevelProtocol
	 *            {@link AbstractIRODSMidLevelProtocol} to be returned
	 */
	public void returnWithForce(
			final AbstractIRODSMidLevelProtocol abstractIRODSMidLevelProtocol) {
		log.warn("connection returned with IOException, will forcefully close and remove from session cache");
		if (abstractIRODSMidLevelProtocol != null) {
			abstractIRODSMidLevelProtocol
					.obliterateConnectionAndDiscardErrors();
			try {

				if (abstractIRODSMidLevelProtocol.getIrodsSession() != null) {

					abstractIRODSMidLevelProtocol.getIrodsSession()
							.discardSessionForErrors(
									abstractIRODSMidLevelProtocol
											.getIrodsAccount());
				}
			} catch (JargonException e) {
				log.error("unable to obliterate connection");
				throw new JargonRuntimeException(
						"unable to obliterate connection", e);
			}
		}
	}

	/**
	 * Optional method for any cleanup when shutting down the operation of the
	 * protocol manager. This is useful if the protocol manager is acting as a
	 * pool or cache that must be cleared.
	 * 
	 * @throws JargonException
	 */
	public void destroy() throws JargonException {
		log.info("destroy called, does nothing by default");
	}

	/**
	 * Optional method to do any startup when beginning operations of the
	 * protocol manager. This can be useful if the protocol manager is a pool or
	 * cache that must do startup tasks before being used.
	 * 
	 * @throws JargonException
	 */
	public void initialize() throws JargonException {
		log.info("initialize called, does nothing by default");

	}

	/*
	 * protected synchronized AuthMechanism getAuthMechanismForIRODSAccount(
	 * final IRODSAccount irodsAccount) throws AuthUnavailableException,
	 * JargonException { if (irodsAccount == null) { throw new
	 * IllegalArgumentException("null irodsAccount"); }
	 * 
	 * return authenticationFactory.instanceAuthMechanism(irodsAccount); }
	 */

}