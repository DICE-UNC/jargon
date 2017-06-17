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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public static final Logger log = LoggerFactory
			.getLogger(AuthMechanism.class);

	/**
	 * Optional method that will be called before any startup pack is sent
	 *
	 * @throws JargonException
	 */
	protected void preConnectionStartup() throws JargonException {

	}

	/**
	 * Optional method that will be called after the startup pack is sent but
	 * before the actual authentication attempt, and before client/server
	 * negotiation
	 *
	 * @throws JargonException
	 */
	protected void postConnectionStartupPreAuthentication()
			throws JargonException {

	}

	/**
	 * After startup pack, the client/server negotiation commences here, based
	 * on configuration and the settings in the <code>IRODSAccount</code>
	 * visible here.
	 * 
	 * @param irodsMidLevelProtocol
	 * @param irodsAccount
	 * @return {@link StartupResponseData}
	 * @throws JargonException
	 */
	protected StartupResponseData clientServerNegotiationHook(
			final AbstractIRODSMidLevelProtocol irodsMidLevelProtocol,
			final IRODSAccount irodsAccount) throws JargonException {
		log.info("clientServerNegotiationHook()");
		StartupResponseData startupResponseData = null;
		if (irodsMidLevelProtocol.getIrodsConnection()
				.getOperativeClientServerNegotiationPolicy()
				.getSslNegotiationPolicy() != SslNegotiationPolicy.NO_NEGOTIATION) {
			log.info("negotiation is required");
			startupResponseData = clientServerNegotiation(
					irodsMidLevelProtocol, irodsAccount);
		} else {
			Tag versionPI = irodsMidLevelProtocol.readMessage();
			startupResponseData = buldStartupResponseFromVersionPI(versionPI);
		}

		log.info("startup response:{}", startupResponseData);
		irodsMidLevelProtocol.setStartupResponseData(startupResponseData);

		return startupResponseData;

	}

	/**
	 * Handy method to build startup response data from a VersionPI tag response
	 * from iRODS
	 *
	 * @param versionPI
	 *            {@link Tag} protocol representation of version info
	 * @return {@link StartupResponseData

	 */
	static StartupResponseData buldStartupResponseFromVersionPI(
			final Tag versionPI) {
		StartupResponseData startupResponseData;
		startupResponseData = new StartupResponseData(versionPI
				.getTag("status").getIntValue(), versionPI.getTag("relVersion")
				.getStringValue(), versionPI.getTag("apiVersion")
				.getStringValue(),
				versionPI.getTag("reconnPort").getIntValue(), versionPI.getTag(
						"reconnAddr").getStringValue(), versionPI.getTag(
						"cookie").getStringValue());
		return startupResponseData;
	}

	/**
	 * After startup pack send, do a client server negotiation, analogous to
	 * irods/lib/core/src/sockComm.cpp line 845
	 *
	 * @param irodsMidLevelProtocol
	 * @param irodsAccount
	 * @return
	 * @throws JargonException
	 */
	private StartupResponseData clientServerNegotiation(
			final AbstractIRODSMidLevelProtocol irodsMidLevelProtocol,
			final IRODSAccount irodsAccount) throws JargonException {

		log.info("clientServerNegotiation()");

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
			log.info("got version pi back instead of negotiation status, so treat as no SSL");
			return buldStartupResponseFromVersionPI(negResultPI);
		} else if (negResultPI.getName().equals(
				ClientServerNegotiationStructInitNegotiation.NEG_PI)) {

			/*
			 * NEG_PI sent in irods_client_negotiation.cpp line ~429
			 */

			ClientServerNegotiationStructInitNegotiation struct = ClientServerNegotiationStructInitNegotiation
					.instanceFromTag(negResultPI);

			if (!struct.wasThisASuccess()) {
				log.error("negotiation was unsuccesful:{}", struct);
				throw new ClientServerNegotiationException(
						"unsuccesful client-server negotiation");
			}

			log.info("have a server negotiation response:{}", struct.toString());

			/*
			 * Do the actual negotiation...The struct should have the response
			 * from the startup pack to launch the negotiation process.
			 * 
			 * here I am tracking lib/core/src/irods_client_negotiation.cpp ~
			 * line 293
			 */

			ClientServerNegotiationService clientServerNegotiationService = new ClientServerNegotiationService(
					irodsMidLevelProtocol);

			StartupResponseData startupResponseData = clientServerNegotiationService
					.negotiate(struct);
			log.info("negotiated configuration:{}", startupResponseData);
			return startupResponseData;

		} else {
			log.error("unknown response to startup pack:{}",
					negResultPI.getName());
			throw new ClientServerNegotiationException(
					"unexpected result from send of startup pack, was neither versionPI nor NegotiationPI");
		}

	}

	/**
	 * Given the initial connection, perform the authentication process. This
	 * process will return a <code>AbstractIRODSMidLevelProtocol</code>. This
	 * method will start the connection by sending the auth request, process the
	 * startup packet, and then call the authentication method of the actual
	 * auth mechanism implementation.
	 *
	 * @param irodsMidLevelProtocol
	 *            {@link AbstractIRODSMidLevelProtocol} that is already
	 *            connected, but not authenticated
	 * @param irodsAccount
	 *            {@link IRODSAccount} that defines the connection as requested
	 * @return {@link AbstractIRODSMidLevelProtocol} that represents a
	 *         connected, authenticated session with an iRODS agent. Note that
	 *         the protocol returned may not be the one originally provided,
	 *         based on the auth method.
	 *
	 * @throws AuthenticationException
	 * @throws JargonException
	 */
	protected AbstractIRODSMidLevelProtocol authenticate(
			final AbstractIRODSMidLevelProtocol irodsMidLevelProtocol,
			final IRODSAccount irodsAccount) throws AuthenticationException,
			JargonException {
		irodsMidLevelProtocol.setIrodsAccount(irodsAccount);
		preConnectionStartup();
		sendStartupPacket(irodsAccount, irodsMidLevelProtocol);
		StartupResponseData startupResponseData = clientServerNegotiationHook(
				irodsMidLevelProtocol, irodsAccount);
		postConnectionStartupPreAuthentication();
		AbstractIRODSMidLevelProtocol authenticatedProtocol = processAuthenticationAfterStartup(
				irodsAccount, irodsMidLevelProtocol, startupResponseData);
		authenticatedProtocol = processAfterAuthentication(
				authenticatedProtocol, startupResponseData);

		return authenticatedProtocol;
	}

	/**
	 * This method provides a life cycle hook after the authentication process
	 * has completed. By default, the method just returns the protocol as passed
	 * in. In some authentication scenarios, follow on steps may manipulate, or
	 * even create a different authenticated protocol layer and return that.
	 * <p>
	 * Note that the protocol contains a reference to the {@link AuthResponse}
	 * that details the authenticating and authenticated accounts and
	 * identities.
	 *
	 * @param irodsMidLevelProtocol
	 *            {@link AbstractIRODSMidLevelProtocol} that is already
	 *            connected, but not authenticated
	 * @param startupResponseData
	 *            {@link StartupResponseData} representing the response from
	 *            iRODS on initiation of connection
	 * @return {@link AbstractIRODSMidLevelProtocol} that represents a
	 *         connected, authenticated session with an iRODS agent. Note that
	 *         the protocol returned may not be the one originally provided,
	 *         based on the auth method.
	 *
	 * @throws AuthenticationException
	 * @throws JargonException
	 */
	protected AbstractIRODSMidLevelProtocol processAfterAuthentication(
			final AbstractIRODSMidLevelProtocol irodsMidLevelProtocol,
			final StartupResponseData startupResponseData)
			throws AuthenticationException, JargonException {
		return irodsMidLevelProtocol;
	}

	protected String sendAuthRequestAndGetChallenge(
			final AbstractIRODSMidLevelProtocol irodsCommands)
			throws JargonException {
		try {
			irodsCommands.sendHeader(
					RequestTypes.RODS_API_REQ.getRequestType(), 0, 0, 0,
					AUTH_REQUEST_AN);
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
		String cachedChallengeValue = message.getTag(StartupPack.CHALLENGE)
				.getStringValue();
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
	 * @param irodsMidLevelProtocol
	 *            {@link AbstractIRODSMidLevelProtocol} that is already
	 *            connected, but not authenticated
	 * @param irodsAccount
	 *            {@link IRODSAccount} that defines the connection as requested
	 * @param startupResponseData
	 *            {@link StartupResponseData} with information from the
	 *            handshake process
	 * @return {@link AbstractIRODSMidLevelProtocol}
	 * @throws AuthenticationException
	 * @throws JargonException
	 */
	protected abstract AbstractIRODSMidLevelProtocol processAuthenticationAfterStartup(
			IRODSAccount irodsAccount,
			AbstractIRODSMidLevelProtocol irodsMidLevelProtocol,
			final StartupResponseData startupResponseData)
			throws AuthenticationException, JargonException;

	protected void sendStartupPacket(final IRODSAccount irodsAccount,
			final AbstractIRODSMidLevelProtocol irodsCommands)
			throws JargonException {

		log.info("sendStartupPacket()");

		String myOption;
		if (irodsCommands.getIrodsConnection()
				.getOperativeClientServerNegotiationPolicy()
				.getSslNegotiationPolicy() == SslNegotiationPolicy.NO_NEGOTIATION) {
			myOption = "iinit";
		} else {
			myOption = StartupPack.NEGOTIATE_OPTION;
		}

		StartupPack startupPack = new StartupPack(irodsAccount, irodsCommands
				.getPipelineConfiguration().isReconnect(), myOption);

		String startupPackData = startupPack.getParsedTags();
		log.debug("startupPackData:{}", startupPackData);

		// FIXME: NEG_PI here
		/*
		 * 
		 * <CS_NEG_PI><status>1</status> <result>CS_NEG_DONT_CARE</result>
		 * </CS_NEG_PI>
		 */

		try {
			irodsCommands.sendHeader(
					RequestTypes.RODS_CONNECT.getRequestType(),
					startupPackData.length(), 0, 0, 0);
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
