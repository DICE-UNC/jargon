package org.irods.jargon.core.connection;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.ClosedChannelException;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.ClientServerNegotiationStructInitNegotiation;
import org.irods.jargon.core.packinstr.StartupPack;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.protovalues.RequestTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a type of authentication scheme, outlining a life cycle model of
 * connections with events called at appropriate points where authentication
 * processing can be implemented
 *
 * @author Mike Conway - DICE
 *
 */
abstract class AuthMechanism {

	public static final int AUTH_REQUEST_AN = 703;
	public static final int AUTH_RESPONSE_AN = 704;
	public static final String VERSION_PI_TAG = "Version_PI";

	public String cachedChallenge = "";

	public static final Logger log = LogManager.getLogger(AuthMechanism.class);

	/**
	 * Optional method that will be called before any startup pack is sent
	 *
	 * @throws JargonException for iRODS error
	 */
	protected void preConnectionStartup() throws JargonException {

	}

	/**
	 * Optional method that will be called after the startup pack is sent but before
	 * the actual authentication attempt, and before client/server negotiation
	 *
	 * @throws JargonException for iRODS error
	 */
	protected void postConnectionStartupPreAuthentication() throws JargonException {

	}

	/**
	 * After startup pack, the client/server negotiation commences here, based on
	 * configuration and the settings in the {@code IRODSAccount} visible here.
	 *
	 * @param irodsMidLevelProtocol {@link IRODSMidLevelProtocol}
	 * @param irodsAccount          {@link IRODSAccount}
	 * @return {@link StartupResponseData}
	 * @throws JargonException for iRODS error
	 */
	protected StartupResponseData clientServerNegotiationHook(final IRODSMidLevelProtocol irodsMidLevelProtocol,
			final IRODSAccount irodsAccount) throws JargonException {
		log.debug("clientServerNegotiationHook()");
		StartupResponseData startupResponseData = null;
		if (irodsMidLevelProtocol.getIrodsConnection().getOperativeClientServerNegotiationPolicy()
				.getSslNegotiationPolicy() != SslNegotiationPolicy.NO_NEGOTIATION) {
			log.debug("negotiation is required");
			startupResponseData = clientServerNegotiation(irodsMidLevelProtocol, irodsAccount);
		} else {
			Tag versionPI = irodsMidLevelProtocol.readMessage();
			startupResponseData = buldStartupResponseFromVersionPI(versionPI);
		}

		log.debug("startup response:{}", startupResponseData);
		irodsMidLevelProtocol.setStartupResponseData(startupResponseData);

		return startupResponseData;

	}

	/**
	 * Handy method to build startup response data from a VersionPI tag response
	 * from iRODS
	 *
	 * @param versionPI {@link Tag} protocol representation of version info
	 * @return {@link StartupResponseData} with the result of the send of the
	 *         startup pack
	 *
	 */
	static StartupResponseData buldStartupResponseFromVersionPI(final Tag versionPI) {
		StartupResponseData startupResponseData;
		startupResponseData = new StartupResponseData(versionPI.getTag("status").getIntValue(),
				versionPI.getTag("relVersion").getStringValue(), versionPI.getTag("apiVersion").getStringValue(),
				versionPI.getTag("reconnPort").getIntValue(), versionPI.getTag("reconnAddr").getStringValue(),
				versionPI.getTag("cookie").getStringValue());
		return startupResponseData;
	}

	/**
	 * After startup pack send, do a client server negotiation, analogous to
	 * irods/lib/core/src/sockComm.cpp line 845
	 *
	 * @param irodsMidLevelProtocol {@link IRODSMidLevelProtocol}
	 * @param irodsAccount          {@link IRODSAccount}
	 * @return {@link StartupResponseData} with the result of the startup process
	 * @throws JargonException
	 */
	private StartupResponseData clientServerNegotiation(final IRODSMidLevelProtocol irodsMidLevelProtocol,
			final IRODSAccount irodsAccount) throws JargonException {

		log.debug("clientServerNegotiation()");

		/*
		 * I expect to read in a cd_neg_t structure
		 *
		 * struct cs_neg_t { int status_; char result_[MAX_NAME_LEN]; };
		 */

		Tag negResultPI = irodsMidLevelProtocol.readMessage();

		/*
		 * Did I just get a version pi back? If, so, no negotiation happened
		 */

		if (negResultPI.getName().equals(VERSION_PI_TAG)) {
			log.debug("got version pi back instead of negotiation status, so treat as no SSL");
			return buldStartupResponseFromVersionPI(negResultPI);
		} else if (negResultPI.getName().equals(ClientServerNegotiationStructInitNegotiation.NEG_PI)) {

			/*
			 * NEG_PI sent in irods_client_negotiation.cpp line ~429
			 */

			ClientServerNegotiationStructInitNegotiation struct = ClientServerNegotiationStructInitNegotiation
					.instanceFromTag(negResultPI);

			if (!struct.wasThisASuccess()) {
				log.error("negotiation was unsuccesful:{}", struct);
				throw new ClientServerNegotiationException("unsuccesful client-server negotiation");
			}

			log.debug("have a server negotiation response:{}", struct.toString());

			/*
			 * Do the actual negotiation...The struct should have the response from the
			 * startup pack to launch the negotiation process.
			 *
			 * here I am tracking lib/core/src/irods_client_negotiation.cpp ~ line 293
			 */

			ClientServerNegotiationService clientServerNegotiationService = new ClientServerNegotiationService(
					irodsMidLevelProtocol);

			StartupResponseData startupResponseData = clientServerNegotiationService.negotiate(struct);
			log.debug("negotiated configuration:{}", startupResponseData);
			return startupResponseData;

		} else {
			log.error("unknown response to startup pack:{}", negResultPI.getName());
			throw new ClientServerNegotiationException(
					"unexpected result from send of startup pack, was neither versionPI nor NegotiationPI");
		}

	}

	/**
	 * Given the initial connection, perform the authentication process. This
	 * process will return a {@code IRODSMidLevelProtocol}. This method will start
	 * the connection by sending the auth request, process the startup packet, and
	 * then call the authentication method of the actual auth mechanism
	 * implementation.
	 *
	 * @param irodsMidLevelProtocol {@link IRODSMidLevelProtocol} that is already
	 *                              connected, but not authenticated
	 * @param irodsAccount          {@link IRODSAccount} that defines the connection
	 *                              as requested
	 * @return {@link IRODSMidLevelProtocol} that represents a connected,
	 *         authenticated session with an iRODS agent. Note that the protocol
	 *         returned may not be the one originally provided, based on the auth
	 *         method.
	 *
	 * @throws AuthenticationException for error in authentication
	 * @throws JargonException         for iRODS error
	 */
	protected IRODSMidLevelProtocol authenticate(final IRODSMidLevelProtocol irodsMidLevelProtocol,
			final IRODSAccount irodsAccount) throws AuthenticationException, JargonException {
		log.debug("authenticate()");
		log.debug("irodsAccount:{}", irodsAccount);
		irodsMidLevelProtocol.setIrodsAccount(irodsAccount);
		preConnectionStartup();
		sendStartupPacket(irodsAccount, irodsMidLevelProtocol);
		StartupResponseData startupResponseData = clientServerNegotiationHook(irodsMidLevelProtocol, irodsAccount);
		postConnectionStartupPreAuthentication();
		IRODSMidLevelProtocol authenticatedProtocol = processAuthenticationAfterStartup(irodsAccount,
				irodsMidLevelProtocol, startupResponseData);
		authenticatedProtocol = processAfterAuthentication(authenticatedProtocol, startupResponseData);
		log.debug("authenticated...");

		return authenticatedProtocol;
	}

	/**
	 * This method provides a life cycle hook after the authentication process has
	 * completed. By default, the method just returns the protocol as passed in. In
	 * some authentication scenarios, follow on steps may manipulate, or even create
	 * a different authenticated protocol layer and return that.
	 * <p>
	 * Note that the protocol contains a reference to the {@link AuthResponse} that
	 * details the authenticating and authenticated accounts and identities.
	 *
	 * @param irodsMidLevelProtocol {@link IRODSMidLevelProtocol} that is already
	 *                              connected, but not authenticated
	 * @param startupResponseData   {@link StartupResponseData} representing the
	 *                              response from iRODS on initiation of connection
	 * @return {@link IRODSMidLevelProtocol} that represents a connected,
	 *         authenticated session with an iRODS agent. Note that the protocol
	 *         returned may not be the one originally provided, based on the auth
	 *         method.
	 *
	 * @throws AuthenticationException for auth error
	 * @throws JargonException         for iRODS error
	 */
	protected IRODSMidLevelProtocol processAfterAuthentication(final IRODSMidLevelProtocol irodsMidLevelProtocol,
			final StartupResponseData startupResponseData) throws AuthenticationException, JargonException {
		return irodsMidLevelProtocol;
	}

	protected String sendAuthRequestAndGetChallenge(final IRODSMidLevelProtocol irodsCommands) throws JargonException {
		try {
			irodsCommands.sendHeader(RequestTypes.RODS_API_REQ.getRequestType(), 0, 0, 0, AUTH_REQUEST_AN);
			irodsCommands.getIrodsConnection().flush();
		} catch (ClosedChannelException e) {
			log.error("closed channel", e);
			e.printStackTrace();
			throw new JargonException(e);
		} catch (InterruptedIOException e) {
			log.error("interrupted io", e);
			e.printStackTrace();
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("io exception", e);
			e.printStackTrace();
			throw new JargonException(e);
		}

		Tag message = irodsCommands.readMessage(false);

		// Create and send the response
		String cachedChallengeValue = message.getTag(StartupPack.CHALLENGE).getStringValue();
		log.debug("cached challenge response:{}", cachedChallengeValue);

		return cachedChallengeValue;

	}

	/**
	 * Hook method in the life cycle after the startup packet has been sent,
	 * encapsulating the actual authentication process.
	 * <p>
	 * This abstract method should be implemented in a subclass authentication
	 * handler
	 *
	 * @param irodsMidLevelProtocol {@link IRODSMidLevelProtocol} that is already
	 *                              connected, but not authenticated
	 * @param irodsAccount          {@link IRODSAccount} that defines the connection
	 *                              as requested
	 * @param startupResponseData   {@link StartupResponseData} with information
	 *                              from the handshake process
	 * @return {@link IRODSMidLevelProtocol}
	 * @throws AuthenticationException for auth error
	 * @throws JargonException         for iRODS error
	 */
	protected abstract IRODSMidLevelProtocol processAuthenticationAfterStartup(IRODSAccount irodsAccount,
			IRODSMidLevelProtocol irodsMidLevelProtocol, final StartupResponseData startupResponseData)
			throws AuthenticationException, JargonException;

	protected void sendStartupPacket(final IRODSAccount irodsAccount, final IRODSMidLevelProtocol irodsCommands)
			throws JargonException {

		log.debug("sendStartupPacket()");

		String myOption;
		if (irodsCommands.getIrodsConnection().getOperativeClientServerNegotiationPolicy()
				.getSslNegotiationPolicy() == SslNegotiationPolicy.NO_NEGOTIATION) {
			myOption = "iinit";
		} else {
			myOption = StartupPack.NEGOTIATE_OPTION;
		}

		StartupPack startupPack = new StartupPack(irodsAccount, irodsCommands.getPipelineConfiguration().isReconnect(),
				myOption);

		String startupPackData = startupPack.getParsedTags();
		log.debug("startupPackData:{}", startupPackData);
		try {
			irodsCommands.sendHeader(RequestTypes.RODS_CONNECT.getRequestType(), startupPackData.length(), 0, 0, 0);
			irodsCommands.getIrodsConnection().send(startupPackData);
			irodsCommands.getIrodsConnection().flush();
		} catch (ClosedChannelException e) {
			log.error("closed channel", e);
			e.printStackTrace();
			throw new JargonException(e);
		} catch (InterruptedIOException e) {
			log.error("interrupted io", e);
			e.printStackTrace();
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("io exception", e);
			e.printStackTrace();
			throw new JargonException(e);
		}

	}

}
