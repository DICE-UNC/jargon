package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class IRODSProtocolManager {

	/**
	 * Factory that will associate an iRODS authentication scheme handler with an
	 * iRODS authentication scheme in an {@code IRODSAccount} when logging in.
	 */
	private AuthenticationFactory authenticationFactory = new AuthenticationFactoryImpl();

	/**
	 * Factory that will create a factory that creates the networking layer to iRODS
	 * based on the settings in jargon.properties
	 */
	private IRODSConnectionFactoryProducingFactory irodsConnectionFactoryProducingFactory = new IRODSConnectionFactoryProducingFactory();

	private AbstractIRODSMidLevelProtocolFactory irodsMidLevelProtocolFactory;

	private Logger log = LogManager.getLogger(IRODSProtocolManager.class);

	/**
	 * Get the factory object that will create various authentication methods on
	 * demand
	 *
	 * @return {@link AuthenticationFactory} that will create objects that can
	 *         authenticate {@code iRODSAccount}s
	 */
	AuthenticationFactory getAuthenticationFactory() {
		return authenticationFactory;
	}

	/**
	 * Inject a factory that will be used to authentication {@code IRODSAccount}s
	 * when a new connection needs to be made
	 *
	 * @param authenticationFactory
	 *            {@link AuthenticationFactory} that will create objects that can
	 *            authenticate {@code iRODSAccount}s
	 */
	public void setAuthenticationFactory(final AuthenticationFactory authenticationFactory) {
		if (authenticationFactory == null) {
			throw new IllegalArgumentException("null authenticationFactory");
		}
		this.authenticationFactory = authenticationFactory;
	}

	/**
	 * For an account provided by the caller, return an open IRODS connection. This
	 * may be created new, cached from previous connection by the same user, or from
	 * a pool.
	 *
	 * <p>
	 * Note that this abstract class provides a protected method dedicated to
	 * creating a fresh protocol layer when this protocol is not originating from a
	 * pool or cache when invoked. Other variants will just create a new protocol
	 * layer each time it is asked.
	 * <p>
	 * This methods is typically not used by clients of this API. Instead, use the
	 * methods in {@link IRODSSession} to manage the connection life cycle. An
	 * exception would be a situation where one is implementing a custom pool or
	 * cache of connections
	 *
	 * @param irodsAccount
	 *            {@link IRODSAccount} that defines the connection
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} that tunes the i/o pipeline and
	 *            other connection options
	 * @param irodsSession
	 *            {@link IRODSSession} that will manage this connection and cache
	 *            information
	 * @return {@link IRODSMidLevelProtocol} subclass that represents a mid-level
	 *         api that talks iRODS protocols
	 * @exception AuthenticationException
	 *                if the irodsAccount is invalid
	 * @exception JargonException
	 *                if a general error occurs
	 */
	public abstract IRODSMidLevelProtocol getIRODSProtocol(IRODSAccount irodsAccount,
			PipelineConfiguration pipelineConfiguration, IRODSSession irodsSession)
			throws AuthenticationException, JargonException;

	/**
	 * Called by a client that no longer needs the connection to iRODS. This signals
	 * a normal close from the higher level API. The {@code IRODSProtocolManager}
	 * can then decide how to treat a normally returned connection. This can either
	 * be shut down, returned to a cache or pool, or any other sort of custom
	 * behavior.
	 *
	 * @param abstractIRODSMidLevelProtocol
	 *            {@link IRODSMidLevelProtocol} to return
	 * @throws JargonException
	 *             for iRODS error
	 */
	protected abstract void returnIRODSProtocol(IRODSMidLevelProtocol abstractIRODSMidLevelProtocol)
			throws JargonException;

	/**
	 * Create a fresh protocol (mid level interface to protocol operations) based on
	 * the underlying jargon.properties
	 *
	 * @param irodsAccount
	 *            {@link IRODSAccount} that defines the connection
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} that tunes the i/o pipeline and
	 *            other connection options
	 * @param irodsSession
	 *            {@link IRODSSession} that will manage this connection and cache
	 *            information
	 * @return {@link IRODSMidLevelProtocol} subclass that represents a mid-level
	 *         API that talks iRODS protocols
	 * @exception AuthenticationException
	 *                if the irodsAccount is invalid
	 * @exception JargonException
	 *                if a general error occurs
	 */
	protected IRODSMidLevelProtocol createNewProtocol(final IRODSAccount irodsAccount,
			final PipelineConfiguration pipelineConfiguration, final IRODSSession irodsSession)
			throws AuthenticationException, JargonException {

		log.debug("creating a fresh AbstractIRODSMidLevelProtocol for account:{}", irodsAccount);

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (pipelineConfiguration == null) {
			throw new IllegalArgumentException("null pipelineConfiguration");
		}

		if (irodsSession == null) {
			throw new IllegalArgumentException("null irodsSession");
		}

		return getIrodsMidLevelProtocolFactory().instance(irodsSession, irodsAccount, this);

	}

	/**
	 * Abandon a connection to iRODS for some error by forcefully shutting it down.
	 * <p>
	 * This is called by the {@link IRODSSession} when a signal has been sent that
	 * it longer needs the connection, and wishes to signal that there was an error
	 * or other condition that gives reason to suspect that the agent or connection
	 * is corrupted and should not be returned to a cache or pool.
	 *
	 * @param irodsMidLevelProtocol
	 *            {@link IRODSMidLevelProtocol} to be returned
	 */
	protected void returnWithForce(final IRODSMidLevelProtocol irodsMidLevelProtocol) {
		log.warn("connection returned with IOException, will forcefully close and remove from session cache");
		if (irodsMidLevelProtocol != null) {
			irodsMidLevelProtocol.obliterateConnectionAndDiscardErrors();
		}
	}

	/**
	 * Optional method for any cleanup when shutting down the operation of the
	 * protocol manager. This is useful if the protocol manager is acting as a pool
	 * or cache that must be cleared.
	 *
	 * @throws JargonException
	 *             for iRODS error
	 */
	protected synchronized void destroy() throws JargonException {
		log.debug("destroy called, this will terminate the session and clear it");

	}

	/**
	 * Optional method to do any startup when beginning operations of the protocol
	 * manager. This can be useful if the protocol manager is a pool or cache that
	 * must do startup tasks before being used.
	 *
	 * @throws JargonException
	 *             for iRODS error
	 */
	public synchronized void initialize() throws JargonException {
		log.debug("initialize called, does nothing by default");

	}

	/**
	 *
	 * @return
	 */
	synchronized IRODSConnectionFactoryProducingFactory getIrodsConnectionFactoryProducingFactory() {
		return irodsConnectionFactoryProducingFactory;
	}

	/**
	 *
	 * @param irodsConnectionFactoryProducingFactory
	 */
	synchronized void setIrodsConnectionFactoryProducingFactory(
			final IRODSConnectionFactoryProducingFactory irodsConnectionFactoryProducingFactory) {
		this.irodsConnectionFactoryProducingFactory = irodsConnectionFactoryProducingFactory;
	}

	/**
	 * @return the irodsMidLevelProtocolFactory
	 */
	public synchronized AbstractIRODSMidLevelProtocolFactory getIrodsMidLevelProtocolFactory() {
		return irodsMidLevelProtocolFactory;
	}

	/**
	 * @param irodsMidLevelProtocolFactory
	 *            the irodsMidLevelProtocolFactory to set
	 */
	public synchronized void setIrodsMidLevelProtocolFactory(
			final AbstractIRODSMidLevelProtocolFactory irodsMidLevelProtocolFactory) {
		this.irodsMidLevelProtocolFactory = irodsMidLevelProtocolFactory;
	}
}
