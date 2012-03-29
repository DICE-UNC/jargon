package org.irods.jargon.core.connection.auth;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.ClosedChannelException;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.RequestTypes;
import org.irods.jargon.core.protovalues.XmlProtApis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for Globus GSI authentication for iRODS
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class GSIAuth extends AuthMechanism {

	public static final Logger log = LoggerFactory.getLogger(GSIAuth.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.auth.AuthMechanism#authenticate(org.
	 * irods.jargon.core.connection.IRODSCommands,
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public AuthResponse authenticate(IRODSCommands irodsCommands,
			IRODSAccount irodsAccount) throws AuthenticationException,
			JargonException {
		return null;
	}

	protected void sendGSIPassword(final IRODSAccount irodsAccount,
			final IRODSCommands irodsCommands) throws JargonException {

		if (irodsAccount == null) {
			throw new JargonException("irods account is null");
		}

		try {
			irodsCommands.getIrodsConnection().send(
					irodsCommands.createHeader(
							RequestTypes.RODS_API_REQ.getRequestType(), 0, 0,
							0, XmlProtApis.GSI_AUTH_REQUEST_AN.getApiNumber()));
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

		// FIXME: not yet implemented
		/*
		 * Create and send the response note that this is the one use of the get
		 * methods for the socket and streams of the connection in Jargon. This
		 * is not optimal, and will be refactored at a later time
		 */

		/*
		 * String serverDn =
		 * readMessage(false).getTag(AuthResponseInp_PI.SERVER_DN)
		 * .getStringValue(); new GSIAuth(account,
		 * irodsConnection.getConnection(), irodsConnection
		 * .getIrodsOutputStream(), irodsConnection.getIrodsInputStream());
		 */
	}

	private IRODSAccount lookupAdditionalIRODSAccountInfoWhenGSI(
			final IRODSAccount irodsAccount2, final IRODSCommands irodsCommands) {
		// FIXME: implement with GSI
		return null;
	}

}
