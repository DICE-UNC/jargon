package org.irods.jargon.core.connection;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.ClosedChannelException;

import org.globus.common.CoGProperties;
import org.globus.gsi.gssapi.net.impl.GSIGssInputStream;
import org.globus.gsi.gssapi.net.impl.GSIGssOutputStream;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.protovalues.RequestTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Support for Globus GSI authentication for iRODS
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
class GSIAuth extends AuthMechanism {

	public static final Logger log = LogManager.getLogger(GSIAuth.class);
	private static final int GSI_AUTH_REQUEST_AN = 711;

	/**
	 * Sends the GSI auth request to iRODS and obtains the server DN. The server DN
	 * is augmented in the irodsAccount
	 *
	 * @param irodsAccount
	 *            {@link GSIIRODSAccount} that will be used to log in to iRODS. Note
	 *            that this account information will be augmented during the
	 *            authentication process
	 * @param irodsCommands
	 * @throws JargonException
	 */
	void sendGSIPassword(final GSIIRODSAccount irodsAccount, final IRODSMidLevelProtocol irodsCommands)
			throws JargonException {

		log.info("sendGSIPassword()");

		if (irodsAccount == null) {
			throw new JargonException("irods account is null");
		}

		if (irodsCommands == null) {
			throw new IllegalArgumentException("null irodsCommands");
		}

		log.debug("sending gsi auth request after startup...");

		try {
			irodsCommands.sendHeader(

					RequestTypes.RODS_API_REQ.getRequestType(), 0, 0, 0, GSI_AUTH_REQUEST_AN);
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

		log.debug("reading iRODS response to gsi auth request, extracting server DN...");

		String serverDn = irodsCommands.readMessage(false).getTag("ServerDN").getStringValue();

		log.debug("serverDN:{}", serverDn);
		irodsAccount.setServerDistinguishedName(serverDn);

	}

	@SuppressWarnings("resource")
	IRODSMidLevelProtocol sendGSIAuth(final GSIIRODSAccount irodsAccount, final IRODSMidLevelProtocol irodsCommands)
			throws AuthenticationException, JargonException {

		log.info("sendGSIAuth()");

		IRODSAccount originalIrodsAccount = irodsAccount;

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		// go ahead and verfiy there is a gssCredential in the irodsAccount

		if (irodsAccount.getGSSCredential() == null) {
			throw new IllegalArgumentException("null gssCredential");
		}

		if (irodsCommands == null) {
			throw new IllegalArgumentException("null irodsCommands");
		}

		sendGSIPassword(irodsAccount, irodsCommands);

		CoGProperties cog = null;
		String defaultCA = null;

		String caLocations = irodsAccount.getCertificateAuthority();

		ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();

		GSIGssOutputStream gssout = null;
		GSIGssInputStream gssin = null;

		try {

			if (caLocations != null && caLocations.length() > 0) {
				cog = CoGProperties.getDefault();
				defaultCA = cog.getCaCertLocations();
				cog.setCaCertLocations(caLocations);
			} else {
				log.error("Unable to set cog value");
				throw new JargonRuntimeException("unable to set cog value");
			}

			GSSContext context = manager.createContext(null, null, irodsAccount.getGSSCredential(),
					GSSContext.DEFAULT_LIFETIME);

			context.requestCredDeleg(false);
			context.requestMutualAuth(true);

			gssout = new GSIGssOutputStream(irodsCommands.getIrodsConnection().getIrodsOutputStream(), context);
			gssin = new GSIGssInputStream(irodsCommands.getIrodsConnection().getIrodsInputStream(), context);

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
			response.setAuthenticatingIRODSAccount(originalIrodsAccount);
			response.setAuthenticatedIRODSAccount(irodsAccount);
			irodsCommands.setAuthResponse(response);
			return irodsCommands;

		} catch (GSSException e) {
			AuthenticationException gsiException = null;
			String message = e.getMessage();
			if (message.indexOf("Invalid buffer") >= 0) {
				gsiException = new AuthenticationException("GSI Authentication Failed - Invalid Proxy File");
				gsiException.initCause(e);
			} else if (message.indexOf("Unknown CA") >= 0) {
				gsiException = new AuthenticationException(
						"GSI Authentication Failed - Cannot find Certificate Authority (CA)");
				gsiException.initCause(e);
			} else {
				gsiException = new AuthenticationException("GSI Authentication Failed");
				gsiException.initCause(e);
			}
			throw gsiException;
		} catch (Throwable e) {
			SecurityException exception = new SecurityException("GSI Authentication Failed");
			exception.initCause(e);
			throw exception;
		} finally {
			// TODO: This setting in a finally is a code smell - mcc
			if (defaultCA != null) {
				if (cog == null) {
					log.error("Unable to set cog value");
					throw new JargonRuntimeException("unable to set cog value");
				} else {
					cog.setCaCertLocations(defaultCA);
				}
			}
		}
	}

	@Override
	protected IRODSMidLevelProtocol processAuthenticationAfterStartup(final IRODSAccount irodsAccount,
			final IRODSMidLevelProtocol irodsCommands, final StartupResponseData startupResponseData)
			throws AuthenticationException, JargonException {

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		log.info("processAuthenticationAfterStartup()..checking if GSIIRODSAccount and validating credential");

		GSIIRODSAccount gsiIRODSAccount;

		if (irodsAccount instanceof GSIIRODSAccount) {
			gsiIRODSAccount = (GSIIRODSAccount) irodsAccount;
		} else {
			throw new IllegalArgumentException("irodsAccount should be an instance of GSIIRODSAccount");
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

		return sendGSIAuth(gsiIRODSAccount, irodsCommands);

	}
}
