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

abstract class AuthMechanism {

	public static final Logger log = LoggerFactory
			.getLogger(AuthMechanism.class);

	/**
	 * Take the given account, and perform the authentication step using the
	 * connection in the given <code>irodsCommands</code>
	 * 
	 * @param irodsCommands
	 *            {@link IRODSCommands} that will be authenticating
	 * @param irodsAccount
	 *            {@link IRODSAccount} with the zone and principle information
	 * @return {@link AuthResponse} with information about the authentication
	 *         attempt
	 * @throws AuthenticationException
	 *             if the authentication proceeded normally, but the principle
	 *             could not be authenticated
	 * @throws JargonException
	 *             if the authentication proceeded abnormally, not caused by
	 *             simply being authorized
	 */
	abstract AuthResponse authenticate(
			final IRODSCommands irodsCommands, final IRODSAccount irodsAccount)
			throws AuthenticationException, JargonException;

	/**
	 * Handles sending the userinfo connection protocol. First, sends initial
	 * handshake with IRODS.
	 * <P>
	 * 
	 * @throws IOException
	 *             if the host cannot be opened or created.
	 */
	protected Tag sendStartupPacket(final IRODSAccount irodsAccount,
			final IRODSCommands irodsCommands) throws JargonException {

		StartupPack startupPack = new StartupPack(irodsAccount);
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
		Tag responseMessage = irodsCommands.readMessage();
		return responseMessage;
	}

}