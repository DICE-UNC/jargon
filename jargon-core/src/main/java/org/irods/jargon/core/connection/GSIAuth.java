package org.irods.jargon.core.connection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.ClosedChannelException;

import org.globus.common.CoGProperties;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.net.impl.GSIGssInputStream;
import org.globus.gsi.gssapi.net.impl.GSIGssOutputStream;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.irods.jargon.core.connection.IRODSAccount.AuthScheme;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
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
	AuthResponse sendGSIAuth(final IRODSAccount irodsAccount,  
			final OutputStream out, final InputStream in) throws IOException {
		CoGProperties cog = null;
		String defaultCA = null;
		GSSCredential credential = null;
		String caLocations = irodsAccount.getCertificateAuthority();

		ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
				.getInstance(); 

		try {
			credential = getCredential(irodsAccount);

			if (caLocations != null) {
				// there is no other way to do this.
				// so I'm overwriting the default then changing it back.
				cog = CoGProperties.getDefault();
				defaultCA = cog.getCaCertLocations();
				cog.setCaCertLocations(caLocations);
			}

			GSSContext context = null;
			GSIGssOutputStream gssout = null;
			GSIGssInputStream gssin = null;

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
			response.setAuthenticatedIRODSAccount(irodsAccount);
			response.setAuthType(AuthScheme.GSI);
			return response;

		} catch (GSSException e) {
			SecurityException gsiException = null;
			String message = e.getMessage();
			if (message.indexOf("Invalid buffer") >= 0) {
				gsiException = new SecurityException(
						"GSI Authentication Failed - Invalid Proxy File");
				gsiException.initCause(e);
			} else if (message.indexOf("Unknown CA") >= 0) {
				gsiException = new SecurityException(
						"GSI Authentication Failed - Cannot find "
								+ "Certificate Authority (CA)");
				gsiException.initCause(e);
			} else {
				gsiException = new SecurityException(
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
			if (defaultCA != null) {
				cog.setCaCertLocations(defaultCA);
			}
		}
	}

	static String getDN(final IRODSAccount account) throws IOException {
		StringBuffer dn = null;
		int index = -1, index2 = -1;
		try {
			GlobusGSSCredentialImpl credential = ((GlobusGSSCredentialImpl) getCredential(account));
			dn = new StringBuffer(credential.getName().toString());
		} catch (GSSException e) {
			throw new IllegalArgumentException("Invalid or missing credentials");// ,
																					// e);
		}

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

	static GSSCredential getCredential(final IRODSAccount account)
			throws GSSException, IOException {
		byte[] data = null;
		GSSCredential credential = account.getGSSCredential();
		if (credential != null) {
			if (credential.getRemainingLifetime() <= 0) {
				throw new GSSException(GSSException.CREDENTIALS_EXPIRED);
			}

			return credential;
		}

		String password = account.getPassword();
		ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
				.getInstance();

		if (password == null) {
			throw new IllegalArgumentException(
					"Password/Proxyfile and GSSCredential cannot be null.");
		} else if (password.startsWith("-----BEGIN CERTIFICATE-----")) {
			data = password.getBytes();

			credential = manager.createCredential(data,
					ExtendedGSSCredential.IMPEXP_OPAQUE,
					GSSCredential.DEFAULT_LIFETIME, null,
					GSSCredential.INITIATE_AND_ACCEPT);
		} else {
			File f = new File(password);
			if (f.exists()) {
				RandomAccessFile inputFile = new RandomAccessFile(f, "r");
				data = new byte[(int) f.length()];
				// read in the credential data
				inputFile.read(data);
				inputFile.close();
			} else {
				throw new IOException("Proxy file path invalid");
			}

			credential = manager.createCredential(data,
					ExtendedGSSCredential.IMPEXP_OPAQUE,
					GSSCredential.DEFAULT_LIFETIME, null,
					GSSCredential.INITIATE_AND_ACCEPT);
		}

		if (credential.getRemainingLifetime() <= 0) {
			throw new GSSException(GSSException.CREDENTIALS_EXPIRED);
		}

		return credential;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.AuthMechanism#
	 * processAuthenticationAfterStartup
	 * (org.irods.jargon.core.connection.IRODSAccount,
	 * org.irods.jargon.core.connection.IRODSCommands)
	 */
	@Override
	protected AuthResponse processAuthenticationAfterStartup(
			final IRODSAccount irodsAccount, final IRODSCommands irodsCommands)
			throws AuthenticationException, JargonException {
		try {
			return sendGSIAuth(irodsAccount, irodsCommands.getIrodsConnection()
					.getIrodsOutputStream(), irodsCommands.getIrodsConnection()
					.getIrodsInputStream());
		} catch (IOException ioe) {
			log.error("IOException attempting authentication", ioe);
			throw new JargonRuntimeException(
					"io exception attempting authentication");
		}
	}

}
