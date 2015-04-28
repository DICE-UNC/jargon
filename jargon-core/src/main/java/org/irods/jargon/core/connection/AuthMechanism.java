package org.irods.jargon.core.connection;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.ClosedChannelException;

import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
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
	 * before the actual authentication attempt
	 * 
	 * @throws JargonException
	 */
	protected void postConnectionStartupPreAuthentication()
			throws JargonException {

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
		preConnectionStartup();
		StartupResponseData startupResponseData = sendStartupPacket(
				irodsAccount, irodsMidLevelProtocol);
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
	 * <p/>
	 * Note that the protocol contains a reference to the {@link AuthResponse}
	 * that details the authenticating and authenticated accounts and
	 * identities.
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
			irodsCommands.getIrodsConnection().send(
					irodsCommands.createHeader(
							RequestTypes.RODS_API_REQ.getRequestType(), 0, 0,
							0, AUTH_REQUEST_AN));
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
	 * <p/>
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

	protected StartupResponseData sendStartupPacket(
			final IRODSAccount irodsAccount,
			final AbstractIRODSMidLevelProtocol irodsCommands)
			throws JargonException {

		StartupPack startupPack = new StartupPack(irodsAccount, irodsCommands
				.getPipelineConfiguration().isReconnect());
		String startupPackData = startupPack.getParsedTags();
		try {

			irodsCommands.getIrodsConnection().send(
					irodsCommands.createHeader(
							RequestTypes.RODS_CONNECT.getRequestType(),
							startupPackData.length(), 0, 0, 0));
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
		Tag versionPI = irodsCommands.readMessage();
		StartupResponseData startupResponseData = new StartupResponseData(
				versionPI.getTag("status").getIntValue(), versionPI.getTag(
						"relVersion").getStringValue(), versionPI.getTag(
						"apiVersion").getStringValue(), versionPI.getTag(
						"reconnPort").getIntValue(), versionPI.getTag(
						"reconnAddr").getStringValue(), versionPI.getTag(
						"cookie").getStringValue());

		log.info("startup response:{}", startupResponseData);
		irodsCommands.setStartupResponseData(startupResponseData);
		return startupResponseData;
	}

}