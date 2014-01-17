package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IRODSProtocolManager {

	/**
	 * Factory that will associate an iRODS authentication scheme handler with
	 * an iRODS authentication scheme in an <code>IRODSAccount</code> when
	 * logging in.
	 */
	private AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();

	/**
	 * Factory that will create a factory that creates the networking layer to
	 * iRODS based on the settings in jargon.properties
	 */
	private IRODSConnectionFactoryProducingFactory irodsConnectionFactoryProducingFactory = new IRODSConnectionFactoryProducingFactory();

	private AbstractIRODSMidLevelProtocolFactory irodsMidLevelProtocolFactory;

	Logger log = LoggerFactory.getLogger(IRODSProtocolManager.class);

	/**
	 * Get the factory object that will create various authentication methods on
	 * demand
	 * 
	 * @return {@link AuthenticationFactory} that will create objects that can
	 *         authenticate <code>iRODSAccount</code>s
	 */
	AuthenticationFactory getAuthenticationFactory() {
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
	 * <p/>
	 * Note that this abstract class provides a protected method dedicated to
	 * creating a fresh protocol layer when this protocol is not originating
	 * from a pool or cache when invoked. Other variants will just create a new
	 * protocol layer each time it is asked.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that defines the connection
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} that tunes the i/o pipeline and
	 *            other connection options
	 * @param irodsSession
	 *            {@link IRODSSession} that will manage this connection and
	 *            cache information
	 * @return {@link AbstractIRODSMidLevelProtocol} subclass that represents a
	 *         mid-level api that talks iRODS protocols
	 * @exception AuthenticationException
	 *                if the irodsAccount is invalid
	 * @exception JargonException
	 *                if a general error occurs
	 */
	public abstract AbstractIRODSMidLevelProtocol getIRODSProtocol(
			IRODSAccount irodsAccount,
			PipelineConfiguration pipelineConfiguration,
			IRODSSession irodsSession) throws AuthenticationException,
			JargonException;

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
	 * Create a fresh protocol (mid level interface to protocol operations)
	 * based on the underlying jargon.properties
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that defines the connection
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} that tunes the i/o pipeline and
	 *            other connection options
	 * @param irodsSession
	 *            {@link IRODSSession} that will manage this connection and
	 *            cache information
	 * @return {@link AbstractIRODSMidLevelProtocol} subclass that represents a
	 *         mid-level api that talks iRODS protocols
	 * @exception AuthenticationException
	 *                if the irodsAccount is invalid
	 * @exception JargonException
	 *                if a general error occurs
	 */
	protected AbstractIRODSMidLevelProtocol createNewProtocol(
			final IRODSAccount irodsAccount,
			final PipelineConfiguration pipelineConfiguration,
			final IRODSSession irodsSession) throws AuthenticationException,
			JargonException {

		log.debug(
				"creating a fresh AbstractIRODSMidLevelProtocol for account:{}",
				irodsAccount);

		return this.getIrodsMidLevelProtocolFactory().instance(irodsSession,
				irodsAccount, this);
	}

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

	/**
	 * 
	 * @return
	 */
	IRODSConnectionFactoryProducingFactory getIrodsConnectionFactoryProducingFactory() {
		return irodsConnectionFactoryProducingFactory;
	}

	/**
	 * 
	 * @param irodsConnectionFactoryProducingFactory
	 */
	void setIrodsConnectionFactoryProducingFactory(
			final IRODSConnectionFactoryProducingFactory irodsConnectionFactoryProducingFactory) {
		this.irodsConnectionFactoryProducingFactory = irodsConnectionFactoryProducingFactory;
	}

	/**
	 * @return the irodsMidLevelProtocolFactory
	 */
	public AbstractIRODSMidLevelProtocolFactory getIrodsMidLevelProtocolFactory() {
		return irodsMidLevelProtocolFactory;
	}

	/**
	 * @param irodsMidLevelProtocolFactory
	 *            the irodsMidLevelProtocolFactory to set
	 */
	public void setIrodsMidLevelProtocolFactory(
			final AbstractIRODSMidLevelProtocolFactory irodsMidLevelProtocolFactory) {
		this.irodsMidLevelProtocolFactory = irodsMidLevelProtocolFactory;
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