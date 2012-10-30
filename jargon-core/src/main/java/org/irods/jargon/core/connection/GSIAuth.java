package org.irods.jargon.core.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.channels.ClosedChannelException;

import org.globus.common.CoGProperties;
import org.globus.gsi.gssapi.net.impl.GSIGssInputStream;
import org.globus.gsi.gssapi.net.impl.GSIGssOutputStream;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.irods.jargon.core.connection.auth.AuthResponse;
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
class GSIAuth extends AuthMechanism {

	public static final Logger log = LoggerFactory.getLogger(GSIAuth.class);

	void sendGSIPassword(final IRODSAccount irodsAccount,
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

		/*
		 * Create and send the response note that this is the one use of the get
		 * methods for the socket and streams of the connection in Jargon. This
		 * is not optimal, and will be refactored at a later time
		 */
		/*
		 * String serverDn = irodsCommands.readMessage(false).getTag(
		 * AuthResponseInp_PI.SERVER_DN).getStringValue(); new
		 * GSIAuth(irodsAccount, irodsCommands. getConnection(),
		 * irodsConnection.getIrodsOutputStream(),
		 * irodsConnection.getIrodsInputStream());
		 */

	}

	/**
	 * GSI authorization method. Makes a connection to the iRODS using the GSI
	 * authorization scheme.
	 * 
	 * @param account
	 *            the iRODS connection information
	 * @param out
	 *            The output stream from that socket.
	 * @param in
	 *            The input stream from that socket.
	 * @return {@link AuthResponse} with the result of the authentication
	 * @throws IOException
	 *             If the authentication to the iRODS fails.
	 */
	AuthResponse sendGSIAuth(final GSIIRODSAccount irodsAccount,
			final OutputStream out, final InputStream in)
			throws AuthenticationException, JargonException {

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		// go ahead and verfiy there is a gssCredential in the irodsAccount

		if (irodsAccount.getGSSCredential() == null) {
			throw new IllegalArgumentException("null gssCredential");
		}

		CoGProperties cog = null;
		String defaultCA = null;
		GSSCredential credential = irodsAccount.getGSSCredential();

		String userDN = getDN(irodsAccount);

		// String caLocations = irodsAccount.getCertificateAuthority();

		ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
				.getInstance();

		GSIGssOutputStream gssout = null;
		GSIGssInputStream gssin = null;

		try {
			/*
			 * credential = getCredential(irodsAccount);
			 * 
			 * if (caLocations != null) { // there is no other way to do this.
			 * // so I'm overwriting the default then changing it back. cog =
			 * CoGProperties.getDefault(); defaultCA = cog.getCaCertLocations();
			 * cog.setCaCertLocations(caLocations); }
			 */
			GSSContext context = null;

			context = manager.createContext(null, null, credential,
					GSSContext.DEFAULT_LIFETIME);

			context.requestCredDeleg(false);
			context.requestMutualAuth(true);

			gssout = new GSIGssOutputStream(out, context);
			gssin = new GSIGssInputStream(in, context);

			byte[] inToken = new byte[0];
			byte[] outToken = null;

			while (!context.isEstablished()) {
				outToken = context.initSecContext(inToken, 0, inToken.length);

				if (outToken != null) {
					gssout.writeToken(outToken);
				}

				if (!context.isEstablished()) {
					inToken = gssin.readHandshakeToken();
				}
			}

			AuthResponse response = new AuthResponse();
			response.setAuthenticatingIRODSAccount(irodsAccount);
			response.setAuthenticatedIRODSAccount(irodsAccount);
			return response;

		} catch (GSSException e) {
			AuthenticationException gsiException = null;
			String message = e.getMessage();
			if (message.indexOf("Invalid buffer") >= 0) {
				gsiException = new AuthenticationException(
						"GSI Authentication Failed - Invalid Proxy File");
				gsiException.initCause(e);
			} else if (message.indexOf("Unknown CA") >= 0) {
				gsiException = new AuthenticationException(
						"GSI Authentication Failed - Cannot find Certificate Authority (CA)");
				gsiException.initCause(e);
			} else {
				gsiException = new AuthenticationException(
						"GSI Authentication Failed");
				gsiException.initCause(e);
			}
			throw gsiException;
		} catch (Throwable e) {
			SecurityException exception = new SecurityException(
					"GSI Authentication Failed");
			exception.initCause(e);
			throw exception;
		} finally {

			try {
				gssin.close();
			} catch (IOException e) {
				// ignore
			}

			try {
				gssout.close();
			} catch (IOException e) {
				// ignore
			}

			if (defaultCA != null) {
				cog.setCaCertLocations(defaultCA);
			}

		}
	}

	static String getDN(final GSIIRODSAccount account) throws JargonException {
		StringBuffer dn = null;
		int index = -1, index2 = -1;

		dn = new StringBuffer(account.getDistinguishedName());

		// remove the extra /CN if exists
		index = dn.indexOf("UID");
		if (index >= 0) {
			index2 = dn.lastIndexOf("CN");
			if (index2 > index) {
				dn = dn.delete(index2 - 1, dn.length());
			}
		}

		// The DN gets returned with commas.
		index = dn.indexOf(",");
		while (index >= 0) {
			dn = dn.replace(index, index + 1, "/");
			index = dn.indexOf(",");
		}

		// add / to front if necessary
		if (dn.indexOf("/") != 0) {
			return "/" + dn;
		} else {
			return dn.toString();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.AuthMechanism#
	 * processAuthenticationAfterStartup
	 * (org.irods.jargon.core.connection.IRODSAccount,
	 * org.irods.jargon.core.connection.IRODSCommands,
	 * org.irods.jargon.core.connection.StartupResponseData)
	 */
	@Override
	protected AuthResponse processAuthenticationAfterStartup(
			final IRODSAccount irodsAccount, final IRODSCommands irodsCommands,
			final StartupResponseData startupResponseData)
			throws AuthenticationException, JargonException {

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		log.info("processAuthenticationAfterStartup()..checking if GSIIRODSAccount and validating credential");

		GSIIRODSAccount gsiIRODSAccount;

		if (irodsAccount instanceof GSIIRODSAccount) {
			gsiIRODSAccount = (GSIIRODSAccount) irodsAccount;
		} else {
			throw new IllegalArgumentException(
					"irodsAccount should be an instance of GSIIRODSAccount");
		}

		log.info("have credential, check if valid...");

		try {
			if (gsiIRODSAccount.getGSSCredential().getRemainingLifetime() <= 0) {
				throw new AuthenticationException("gss credentials are expired");
			}
		} catch (GSSException e) {
			log.error("GSSException processing credential");
			throw new JargonException("gss exception processing credential", e);
		}

		log.info("all valid...send GSI auth to iRODS...");

		return sendGSIAuth(gsiIRODSAccount, irodsCommands.getIrodsConnection()
				.getIrodsOutputStream(), irodsCommands.getIrodsConnection()
				.getIrodsInputStream());

	}

}
