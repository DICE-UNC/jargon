/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating the <code>AbstractIRODSMidLevelProtocol</code> object
 * that will create the protocol (packing instruction) level interface to iRODS.
 * <p/>
 * This factory will be provided with the appropriate factories to create the
 * lower level networking layer (the <code>AbstractConnection</code>) as well as
 * the factory used to create pluggable authentication schemes.
 * <p/>
 * This whole arrangement is then used by the <code>IRODSProtocolManager</code>
 * to create new connections when requested.
 * 
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 * 
 */
abstract class AbstractIRODSMidLevelProtocolFactory {

	private final IRODSConnectionFactory irodsConnectionFactory;
	private final AuthenticationFactory authenticationFactory;

	Logger log = LoggerFactory
			.getLogger(AbstractIRODSMidLevelProtocolFactory.class);

	/**
	 * Create the factory that will produced connected 'mid level protocol
	 * handlers'. This represents the layer that jargon uses internally to send
	 * different protocol operations and receive protocol responses from iRODS.
	 * This is above the low level networking level, which is produced by the
	 * provided <code>IRODSConnectionFactory</code>.
	 * <p/>
	 * The goal of this factory is to return a live, connected, and
	 * authentication connection to an iRODS agent with the correct version of
	 * the mid level protocol handler, therefore an authentication factory is
	 * also required, allowing a level of plug-ability to the authentication
	 * layer.
	 * 
	 * @param irodsConnectionFactory
	 *            {@link IRODSConnectionFactory} implementation that can provide
	 *            a low-level networking layer
	 * @param authenticationFactory
	 *            {@link AuthenticationFactory} that can provide authentication
	 *            implementations
	 */
	protected AbstractIRODSMidLevelProtocolFactory(
			final IRODSConnectionFactory irodsConnectionFactory,
			final AuthenticationFactory authenticationFactory) {

		if (irodsConnectionFactory == null) {
			throw new IllegalArgumentException("null irodsConnectionFactory");
		}

		if (authenticationFactory == null) {
			throw new IllegalArgumentException("null authenticationFactory");
		}

		this.irodsConnectionFactory = irodsConnectionFactory;
		this.authenticationFactory = authenticationFactory;

	}

	/**
	 * @return the irodsConnectionFactory
	 */
	protected IRODSConnectionFactory getIrodsConnectionFactory() {
		return irodsConnectionFactory;
	}

	/**
	 * @return the authenticationFactory
	 */
	protected AuthenticationFactory getAuthenticationFactory() {
		return authenticationFactory;
	}

	protected AbstractIRODSMidLevelProtocol instance(
			final IRODSSession irodsSession, final IRODSAccount irodsAccount,
			final IRODSProtocolManager irodsProtocolManager)
			throws AuthenticationException, JargonException {

		log.info("instance() method...calling connection life cycle");

		log.info("create connection....");
		AbstractConnection connection = irodsConnectionFactory.instance(
				irodsAccount, irodsSession);

		AbstractIRODSMidLevelProtocol protocol = this.createInitialProtocol(
				connection, irodsProtocolManager);

		log.info("...have connection, now authenticate given the auth scheme in the iRODS account...");
		protocol = authenticate(protocol, irodsAccount, irodsSession,
				irodsProtocolManager);
		log.info("..authenticated...now decorate and return...");
		return decorate(protocol, irodsAccount, irodsSession);
	}

	/**
	 * Initial creation step gives individual factories a hook to insert their
	 * own subclass of the iRODS protocol layer
	 * 
	 * @param connection
	 *            {@link AbstractConnection} to iRODS
	 * 
	 * @param irodsProtocolManager
	 *            {@link IRODSProtocolManager} that may have connected this
	 *            session, may be null
	 * @return {@link AbstractIRODSMidLevelProtocol} that is not yet
	 *         initialized, but of the right base class
	 */
	protected abstract AbstractIRODSMidLevelProtocol createInitialProtocol(
			final AbstractConnection connection,
			final IRODSProtocolManager irodsProtocolManager);

	/**
	 * After the <code>authenticate()</code> phase, a final phase allows
	 * provision of any additional information or processing. At the end of this
	 * phase the protocol level connection is ready for use by higher-level API
	 * functions
	 * 
	 * @param protocol
	 *            connected {@link AbstractIRODSMidLevelProtocol} that has been
	 *            authenticated
	 * @param irodsAccount
	 *            {@link IRODSAccount} that defines the connection as requested
	 * @param irodsSession
	 *            {@link IRODSSession} with information about Jargon and its
	 *            environment
	 * @return {@link AbstractIRODSMidLevelProtocol} ready for use. This may or
	 *         may not be the same protocol implementation passed in to the
	 *         method
	 */
	protected AbstractIRODSMidLevelProtocol decorate(
			final AbstractIRODSMidLevelProtocol protocol,
			final IRODSAccount irodsAccount, final IRODSSession irodsSession) {

		log.info("decorate()");

		if (protocol == null) {
			throw new IllegalArgumentException("null protocol");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (irodsSession == null) {
			throw new IllegalArgumentException("null irodsSession");
		}
		/*
		 * EnvironmentalInfoAccessor environmentalInfoAccessor = new
		 * EnvironmentalInfoAccessor( this); irodsServerProperties =
		 * environmentalInfoAccessor .getIRODSServerProperties();
		 * 
		 * // add startup response cookie info indicating if eirods int cookie =
		 * Integer.parseInt(authResponse.getStartupResponse() .getCookie());
		 * 
		 * if (cookie >= EIRODS_MIN && cookie <= EIRODS_MAX) {
		 * log.info("setting to eirods based on cookie value");
		 * irodsServerProperties.setEirods(true); } else if
		 * (irodsServerProperties
		 * .isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods4")) {
		 * irodsServerProperties.setEirods(true); }
		 * 
		 * log.info(irodsServerProperties.toString());
		 */
		return protocol;

	}

	/**
	 * Life cycle method that will take an open connection to an iRODS agent and
	 * produce an authenticated mid level protocol handler that wraps a live
	 * connection
	 * 
	 * @param connection
	 *            {@link AbstractConnection} to iRODS
	 * @param irodsAccount
	 *            {@link IRODSAccount} that defines the instance and principal
	 *            information
	 * @param irodsSession
	 *            {@link IRODSSession} that contains common services and
	 *            information
	 * @param irodsProtocolManager
	 *            {@link IRODSProtocolManager} that may have connected this
	 *            session, may be null
	 * @return {@link AbstractIRODSMidLevelProtocol} that is connected and
	 *         authenticated. This may be decorated with additional information
	 *         in later steps in the creating life-cycle.
	 * @throws authenticationException
	 *             if the authentication failed for invalid credentials
	 * @throws jargonException
	 *             for general errors
	 */

	protected AbstractIRODSMidLevelProtocol authenticate(
			AbstractIRODSMidLevelProtocol protocol, IRODSAccount irodsAccount,
			IRODSSession irodsSession, IRODSProtocolManager irodsProtocolManager)
			throws AuthenticationException, JargonException {

		log.info("authenticate()");

		if (protocol == null) {
			throw new IllegalArgumentException("null connection");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (irodsSession == null) {
			throw new IllegalArgumentException("null irodsSession");
		}

		log.info("get auth mechanism");
		AuthMechanism authMechanism = this.getAuthenticationFactory()
				.instanceAuthMechanism(irodsAccount);

		log.info("authenticate...");
		AuthResponse authResponse = null;
		try {
			authResponse = authMechanism.authenticate(protocol, irodsAccount);
			protocol.setIrodsAccount(authResponse
					.getAuthenticatingIRODSAccount());
			protocol.setAuthResponse(authResponse);
		} catch (AuthenticationException e) {
			log.error(
					"authentication exception, will close iRODS connection and re-throw",
					e);
			protocol.disconnectWithForce();
			throw e;
		}

		return null;

	}
}
