package org.irods.jargon.core.connection;

import java.io.IOException;
import java.security.PrivilegedActionException;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.irods.jargon.core.connection.IRODSAccount.AuthScheme;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.connection.auth.AuthRuntimeException;
import org.irods.jargon.core.connection.auth.ServiceTicketGenerator;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.protovalues.RequestTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for Kerberos authentication to iRODS
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 *         refs
 *         http://docs.oracle.com/javase/1.4.2/docs/guide/security/jgss/single
 *         -signon.html
 *         http://docs.oracle.com/javase/1.5.0/docs/guide/security/jgss
 *         /tutorials/BasicClientServer.html
 * 
 *         http://stackoverflow.com/questions/370878/how-to-obtain-a-kerberos-
 *         service-ticket-via-gss-api
 * 
 *         note that this code mirrors the functionality found in
 *         /lib/core/src/clientLogin.c (see clientLoginKrb)
 * 
 */
class KerberosAuth extends AuthMechanism {

	public static final Logger log = LoggerFactory
			.getLogger(KerberosAuth.class);
	private static final int KERBEROS_AUTH_REQUEST_AN = 717;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.auth.AuthMechanism#authenticate(org.
	 * irods.jargon.core.connection.IRODSCommands,
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	AuthResponse authenticate(final IRODSCommands irodsCommands,
			final IRODSAccount irodsAccount) throws AuthenticationException,
			JargonException {

		log.info("authenticate()");

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (irodsCommands == null) {
			throw new IllegalArgumentException("null irodsCommands");
		}

		log.debug("..creating GSSContext");

		LoginContext lc = null;
		AuthResponse authResponse = null;
		try {
			lc = new LoginContext("JargonKrb");
			// attempt authentication
			lc.login();
			Subject signedOnUserSubject = lc.getSubject();
			log.info("subject:{}", signedOnUserSubject);

			log.info("getting servername");
			authResponse = new AuthResponse();
			authResponse.setAuthenticatedIRODSAccount(irodsAccount);
			authResponse.setAuthType(AuthScheme.KERBEROS);
			String serverName = sendStartupPackAndGetServerName(irodsCommands,
					irodsAccount);
			log.info("got serverName, setting in irodsAccount");
			irodsAccount.setServiceName(serverName);
			authResponse.getResponseProperties().put("serverName", serverName);

			byte[] serviceTicket = Subject.doAs(signedOnUserSubject,
					new ServiceTicketGenerator(irodsAccount));
			authResponse.getResponseProperties()
					.put("serviceTicket", serviceTicket);

			log.debug("sending token to iRODS");
			// send the length of the token, then the token, to iRODS
			irodsCommands.sendInNetworkOrder(serviceTicket.length);
			irodsCommands.getIrodsConnection().send(serviceTicket);
			irodsCommands.getIrodsConnection().flush();
			log.debug("token sent and flushed...reading follow up");
			/*
			 * byte[] serverToken = new byte[1024]; int read =
			 * irodsCommands.getIrodsConnection().read(serverToken);
			 * log.debug("read {} bytes for server token", read);
			 * log.debug("serverToken:{}", serverToken);
			 */
			int lengthOfTokenFromServer = irodsCommands.readHeaderLength();
			log.debug("length of server token to be read:{}",
					lengthOfTokenFromServer);
			if (lengthOfTokenFromServer <= 0) {
				throw new JargonRuntimeException(
						"cannot read token from server, length invalid");
			}

			byte[] serverToken = new byte[lengthOfTokenFromServer];
			irodsCommands.read(serverToken, 0, lengthOfTokenFromServer);
			log.debug("serverToken:{}", serverToken);

			// now receive the server token
			// Tag followUpFromSendToken = irodsCommands.readMessage();
			// log.info("followUpFromSendToken:{}", followUpFromSendToken);

			// sendFollowUpRequest(irodsCommands, irodsAccount);

			authResponse.setSuccessful(true);
			log.info("successfully got authResponse:{}", authResponse);

		} catch (PrivilegedActionException pae) {
			log.warn("privilegedActionException indicates no credentials, treat as unsuccessful auth");
			authResponse = new AuthResponse();
			authResponse.setAuthenticatedIRODSAccount(irodsAccount);
			authResponse.setAuthMessage(pae.getLocalizedMessage());
			authResponse.setSuccessful(false);
			authResponse.setAuthMessage(pae.getLocalizedMessage());
		} catch (Exception e) {
			log.error("LoginException occurred", e);
			throw new AuthRuntimeException(e);
		}

		return authResponse;
	}

	private Tag sendFollowUpRequest(final IRODSCommands irodsCommands,
			final IRODSAccount irodsAccount) throws JargonException {
		
		log.info("sendFollowUpRequest()");

		if (irodsCommands == null) {
			throw new IllegalArgumentException("null irodsCommands");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		
		try {
			irodsCommands.getIrodsConnection().send(
					irodsCommands.createHeader(
							RequestTypes.RODS_API_REQ.getRequestType(), 0, 0,
							0, KERBEROS_AUTH_REQUEST_AN));
			irodsCommands.getIrodsConnection().flush();

			Tag message = irodsCommands.readMessage(false);
			log.info("message from follow up:{}", message);
			return message;
		} catch (IOException e) {
			log.error("IOException on follow up request", e);
			throw new JargonException(
					"got an IOException on follow up krb auth request", e);
		}
	}

	/**
	 * Get the kerberos server name for iRODS by inquiring
	 * 
	 * @param irodsCommands
	 *            {@link IRODSCommands} representing the connection I am trying
	 *            to open
	 * @param irodsAccount
	 *            {@link IRODSAcocunt}
	 * @throws JargonException
	 */
	String sendStartupPackAndGetServerName(final IRODSCommands irodsCommands,
			final IRODSAccount irodsAccount)
			throws AuthenticationException, JargonException {
		log.info("sendStartupPackAndGetServerName()");

		if (irodsCommands == null) {
			throw new IllegalArgumentException("null irodsCommands");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		sendStartupPacket(irodsAccount, irodsCommands);
		try {
			irodsCommands.getIrodsConnection().send(
					irodsCommands.createHeader(
							RequestTypes.RODS_API_REQ.getRequestType(), 0, 0,
							0, KERBEROS_AUTH_REQUEST_AN));
			irodsCommands.getIrodsConnection().flush();

			Tag message = irodsCommands.readMessage(false);
			Tag serverNameTag = message.getTag("ServerName");
			if (serverNameTag == null) {
				throw new JargonException(
						"null tag returned from kerberos request");
			}

			String serverName = serverNameTag.getStringValue();

			if (serverName.isEmpty()) {
				throw new JargonException("serverName is empty");
			}

			log.info("serverName:{}", serverName);
			return serverName;

		} catch (IOException e) {
			log.error("IOException getting server name", e);
			throw new JargonException(
					"got an IOException obtaining the server name", e);
		}

		/*
		 * Create and send the response note that this is the one use of the get
		 * methods for the socket and streams of the connection in Jargon. This
		 * is not optimal, and will be refactored at a later time
		 */

		/*
		 * new GSIAuth(getIrodsAccount(), irodsConnection.getConnection(),
		 * irodsConnection.getIrodsOutputStream(),
		 * irodsConnection.getIrodsInputStream());
		 */
	}

}
