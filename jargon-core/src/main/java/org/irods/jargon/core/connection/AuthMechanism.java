package org.irods.jargon.core.connection;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.ClosedChannelException;

import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.StartupPack;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.protovalues.RequestTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	protected AbstractIRODSMidLevelProtocol authenticate(
			final AbstractIRODSMidLevelProtocol irodsCommands,
			final IRODSAccount irodsAccount) throws AuthenticationException,
			JargonException {
		preConnectionStartup();
		StartupResponseData startupResponseData = sendStartupPacket(
				irodsAccount, irodsCommands);
		postConnectionStartupPreAuthentication();
		AbstractIRODSMidLevelProtocol authenticatedProtocol = processAuthenticationAfterStartup(
				irodsAccount, irodsCommands, startupResponseData);
		authenticatedProtocol = processAfterAuthentication(
				authenticatedProtocol, startupResponseData);

		return irodsCommands;
	}

	protected AbstractIRODSMidLevelProtocol processAfterAuthentication(
			final AbstractIRODSMidLevelProtocol irodsCommands,
			final StartupResponseData startupResponseData)
			throws AuthenticationException, JargonException {
		return irodsCommands;
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

	protected abstract AbstractIRODSMidLevelProtocol processAuthenticationAfterStartup(
			IRODSAccount irodsAccount,
			AbstractIRODSMidLevelProtocol irodsCommands,
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

		return startupResponseData;
	}

}