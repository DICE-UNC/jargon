/**
 *
 */
package org.irods.jargon.core.connection;

import javax.net.ssl.SSLSocket;

import org.irods.jargon.core.connection.AbstractConnection.EncryptionType;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.AuthReqPluginRequestInp;
import org.irods.jargon.core.packinstr.PamAuthRequestInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for PAM (plug-able authentication module) contributed by Chris Smith
 * for iRODS 3.2+
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 *         see lib/core/src/cientLogin.c for main driver program
 *
 *         see lib/core/src/ sslSockCom.c
 */
public class PAMAuth extends AuthMechanism {

	private boolean needToWrapWithSsl = false;

	public static final Logger log = LoggerFactory.getLogger(PAMAuth.class);

	@Override
	protected IRODSMidLevelProtocol processAuthenticationAfterStartup(final IRODSAccount irodsAccount,
			final IRODSMidLevelProtocol irodsMidLevelProtocol, final StartupResponseData startupResponseData)
			throws AuthenticationException, JargonException {

		log.debug("processAuthenticationAfterStartup()");
		needToWrapWithSsl = irodsMidLevelProtocol.getIrodsConnection().getEncryptionType() == EncryptionType.NONE;

		IRODSMidLevelProtocol irodsMidLevelProtocolToUse = null;
		/*
		 * Save the original commands if we will temporarily use an SSL connection,
		 * otherwise will remain null. If, through client/server negotiation, we already
		 * have an SSL connection, then no need to wrap the PAM auth in SSL.
		 */
		if (needToWrapWithSsl) {
			log.debug("will wrap commands with ssl");
			irodsMidLevelProtocolToUse = establishSecureConnectionForPamAuth(irodsAccount, irodsMidLevelProtocol);
		} else {
			log.debug("no need to SSL tunnel for PAM");
			irodsMidLevelProtocolToUse = irodsMidLevelProtocol;
		}

		// send pam auth request

		int pamTimeToLive = irodsMidLevelProtocolToUse.getIrodsSession().getJargonProperties().getPAMTimeToLive();

		Tag response = null;

		if (startupResponseData.checkIs410OrLater()) {
			log.info("using pluggable pam auth request");
			AuthReqPluginRequestInp pi = AuthReqPluginRequestInp.instancePam(irodsAccount.getUserName(),
					MiscIRODSUtils.escapePasswordChars(irodsAccount.getPassword()), pamTimeToLive, startupResponseData);
			response = irodsMidLevelProtocolToUse.irodsFunction(pi);

		} else {
			log.info("using normal irods pam auth request");
			PamAuthRequestInp pamAuthRequestInp = PamAuthRequestInp.instance(irodsAccount.getUserName(),
					irodsAccount.getPassword(), pamTimeToLive);
			response = irodsMidLevelProtocolToUse.irodsFunction(pamAuthRequestInp);
		}

		if (response == null) {
			throw new JargonException("null response from pamAuthRequest");
		}

		String tempPasswordForPam;
		if (startupResponseData.checkIs410OrLater()) {
			tempPasswordForPam = response.getTag("result_").getStringValue();
		} else {
			tempPasswordForPam = response.getTag("irodsPamPassword").getStringValue();
		}

		if (tempPasswordForPam == null || tempPasswordForPam.isEmpty()) {
			throw new AuthenticationException(
					"unable to retrieve the temp password resulting from the pam auth response");
		}

		log.info("have the temporary password to use to log in via pam\nsending sslEnd...");
		shutdownSslAndCloseConnection(irodsMidLevelProtocolToUse);

		AuthResponse authResponse = new AuthResponse();

		IRODSAccount irodsAccountUsingTemporaryIRODSPassword = new IRODSAccount(irodsAccount.getHost(),
				irodsAccount.getPort(), irodsAccount.getUserName(), tempPasswordForPam, irodsAccount.getHomeDirectory(),
				irodsAccount.getZone(), irodsAccount.getDefaultStorageResource());
		irodsAccountUsingTemporaryIRODSPassword.setAuthenticationScheme(AuthScheme.STANDARD);

		log.info("derived and logging in with temporary password from a new agent:{}",
				irodsAccountUsingTemporaryIRODSPassword);
		authResponse.setAuthenticatingIRODSAccount(irodsAccount);
		authResponse.setAuthenticatedIRODSAccount(irodsAccountUsingTemporaryIRODSPassword);
		authResponse.setStartupResponse(startupResponseData);
		authResponse.setSuccessful(true);
		irodsMidLevelProtocolToUse.setAuthResponse(authResponse);

		return irodsMidLevelProtocolToUse;

	}

	private void shutdownSslAndCloseConnection(final IRODSMidLevelProtocol irodsCommandsToUse) throws JargonException {
		irodsCommandsToUse.shutdown();
	}

	private IRODSMidLevelProtocol establishSecureConnectionForPamAuth(final IRODSAccount irodsAccount,
			final IRODSMidLevelProtocol irodsMidLevelProtocol) throws JargonException, AssertionError {

		if (irodsMidLevelProtocol.getIrodsConnection().getEncryptionType() == EncryptionType.SSL_WRAPPED) {
			log.info("already ssl enabled");
			return irodsMidLevelProtocol;
		}

		log.info("not ssl wrapped, use an SSL connection for the pam auth");

		SSLSocket sslSocket = irodsMidLevelProtocol.getIrodsSession().instanceSslConnectionUtilities()
				.createSslSocketForProtocol(irodsAccount, irodsMidLevelProtocol, true);

		log.info("creating secure protcol connection layer");
		IRODSBasicTCPConnection secureConnection = new IRODSBasicTCPConnection(irodsAccount,
				irodsMidLevelProtocol.getPipelineConfiguration(), irodsMidLevelProtocol.getIrodsProtocolManager(),
				sslSocket, irodsMidLevelProtocol.getIrodsSession());

		IRODSMidLevelProtocol secureIRODSCommands = new IRODSMidLevelProtocol(secureConnection,
				irodsMidLevelProtocol.getIrodsProtocolManager());

		secureIRODSCommands.setIrodsConnectionNonEncryptedRef(irodsMidLevelProtocol.getIrodsConnection());

		log.info("carrying over startup pack with server info");
		secureIRODSCommands.setStartupResponseData(irodsMidLevelProtocol.getStartupResponseData());

		log.debug("created secureIRODSCommands wrapped around an SSL socket\nSending PamAuthRequest...");
		return secureIRODSCommands;
	}

	@Override
	protected IRODSMidLevelProtocol processAfterAuthentication(final IRODSMidLevelProtocol irodsMidLevelProtocol,
			final StartupResponseData startupResponseData) throws AuthenticationException, JargonException {

		/*
		 * I'm creating a new protocol for PAM, using the newly renegotiated account
		 * with the new password, So save the auth information from the prior one used
		 * in the pam bootstrapping process
		 */
		AuthResponse originalAuthResponse = irodsMidLevelProtocol.getAuthResponse();

		IRODSMidLevelProtocol actualProtocol = irodsMidLevelProtocol.getIrodsProtocolManager()
				.getIrodsMidLevelProtocolFactory().instance(irodsMidLevelProtocol.getIrodsSession(),
						irodsMidLevelProtocol.getAuthResponse().getAuthenticatedIRODSAccount(),
						irodsMidLevelProtocol.getIrodsProtocolManager());
		actualProtocol.setAuthResponse(originalAuthResponse);
		return actualProtocol;

	}

}
