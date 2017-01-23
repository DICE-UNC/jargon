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

	@Override
	protected AbstractIRODSMidLevelProtocol processAuthenticationAfterStartup(
			final IRODSAccount irodsAccount,
			final AbstractIRODSMidLevelProtocol irodsMidLevelProtocol,
			final StartupResponseData startupResponseData)
			throws AuthenticationException, JargonException {

		needToWrapWithSsl = irodsMidLevelProtocol.getIrodsConnection()
				.getEncryptionType() == EncryptionType.NONE;

		AbstractIRODSMidLevelProtocol irodsMidLevelProtocolToUse = null;
		/*
		 * Save the original commands if we will temporarily use an SSL
		 * connection, otherwise will remain null. If, through client/server
		 * negotiation, we already have an SSL connection, then no need to wrap
		 * the PAM auth in SSL.
		 */
		if (needToWrapWithSsl) {
			log.info("will wrap commands with ssl");
			irodsMidLevelProtocolToUse = establishSecureConnectionForPamAuth(
					irodsAccount, irodsMidLevelProtocol);
		} else {
			log.info("no need to SSL tunnel for PAM");
			irodsMidLevelProtocolToUse = irodsMidLevelProtocol;

		}

		// send pam auth request

		int pamTimeToLive = irodsMidLevelProtocolToUse.getIrodsSession()
				.getJargonProperties().getPAMTimeToLive();

		Tag response = null;

		if (startupResponseData.checkIs410OrLater()) {
			log.info("using eirods pluggable pam auth request");
			AuthReqPluginRequestInp pi = AuthReqPluginRequestInp.instancePam(
					irodsAccount.getProxyName(), irodsAccount.getPassword(),
					pamTimeToLive);
			response = irodsMidLevelProtocolToUse.irodsFunction(pi);

		} else {
			log.info("using normal irods pam auth request");
			PamAuthRequestInp pamAuthRequestInp = PamAuthRequestInp.instance(
					irodsAccount.getProxyName(), irodsAccount.getPassword(),
					pamTimeToLive);
			response = irodsMidLevelProtocolToUse
					.irodsFunction(pamAuthRequestInp);
		}

		if (response == null) {
			throw new JargonException("null response from pamAuthRequest");
		}

		String tempPasswordForPam;
		if (startupResponseData.checkIs410OrLater()) {
			tempPasswordForPam = response.getTag("result_").getStringValue();
		} else {
			tempPasswordForPam = response.getTag("irodsPamPassword")
					.getStringValue();
		}

		if (tempPasswordForPam == null || tempPasswordForPam.isEmpty()) {
			throw new AuthenticationException(
					"unable to retrieve the temp password resulting from the pam auth response");
		}

		log.info("have the temporary password to use to log in via pam\nsending sslEnd...");
		shutdownSslAndCloseConnection(irodsMidLevelProtocolToUse);

		AuthResponse authResponse = new AuthResponse();

		IRODSAccount irodsAccountUsingTemporaryIRODSPassword = new IRODSAccount(
				irodsAccount.getHost(), irodsAccount.getPort(),
				irodsAccount.getUserName(), tempPasswordForPam,
				irodsAccount.getHomeDirectory(), irodsAccount.getZone(),
				irodsAccount.getDefaultStorageResource());
		irodsAccountUsingTemporaryIRODSPassword
				.setAuthenticationScheme(AuthScheme.STANDARD);

		log.info(
				"derived and logging in with temporary password from a new agent:{}",
				irodsAccountUsingTemporaryIRODSPassword);
		authResponse.setAuthenticatingIRODSAccount(irodsAccount);
		authResponse
				.setAuthenticatedIRODSAccount(irodsAccountUsingTemporaryIRODSPassword);
		authResponse.setStartupResponse(startupResponseData);
		authResponse.setSuccessful(true);
		irodsMidLevelProtocolToUse.setAuthResponse(authResponse);

		return irodsMidLevelProtocolToUse;

	}

	/**
	 * @param irodsCommandsToUse
	 * @throws JargonException
	 */
	private void shutdownSslAndCloseConnection(
			final AbstractIRODSMidLevelProtocol irodsCommandsToUse)
			throws JargonException {
		// SSLEndInp sslEndInp = SSLEndInp.instance();
		// irodsCommandsToUse.irodsFunction(sslEndInp);
		irodsCommandsToUse.shutdown();
		/*
		 * try { irodsCommandsToUse.closeOutSocketAndSetAsDisconnected(); }
		 * catch (IOException e) { log.error("error closing ssl socket", e);
		 * throw new JargonException("error closing ssl socket", e); }
		 */
	}

	/**
	 * @param irodsAccount
	 * @param irodsCommands
	 * @return
	 * @throws JargonException
	 * @throws AssertionError
	 */
	private AbstractIRODSMidLevelProtocol establishSecureConnectionForPamAuth(
			final IRODSAccount irodsAccount,
			final AbstractIRODSMidLevelProtocol irodsCommands)
			throws JargonException, AssertionError {

		if (irodsCommands.getIrodsConnection().getEncryptionType() == EncryptionType.SSL_WRAPPED) {
			log.info("already ssl enabled");
			return irodsCommands;
		}

		log.info("not ssl wrapped, use an SSL connection for the pam auth");

		SSLSocket sslSocket = irodsCommands.getIrodsSession()
				.instanceSslConnectionUtilities()
				.createSslSocketForProtocol(irodsAccount, irodsCommands, true);

		log.info("creating secure protcol connection layer");
		IRODSBasicTCPConnection secureConnection = new IRODSBasicTCPConnection(
				irodsAccount, irodsCommands.getPipelineConfiguration(),
				irodsCommands.getIrodsProtocolManager(), sslSocket,
				irodsCommands.getIrodsSession());

		IRODSMidLevelProtocol secureIRODSCommands = new IRODSMidLevelProtocol(
				secureConnection, irodsCommands.getIrodsProtocolManager());

		secureIRODSCommands.setIrodsConnectionNonEncryptedRef(irodsCommands
				.getIrodsConnection());

		log.info("carrying over startup pack with server info");
		secureIRODSCommands.setStartupResponseData(irodsCommands
				.getStartupResponseData());

		log.debug("created secureIRODSCommands wrapped around an SSL socket\nSending PamAuthRequest...");
		return secureIRODSCommands;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.AuthMechanism#processAfterAuthentication
	 * (org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol,
	 * org.irods.jargon.core.connection.StartupResponseData)
	 */
	@Override
	protected AbstractIRODSMidLevelProtocol processAfterAuthentication(
			final AbstractIRODSMidLevelProtocol irodsMidLevelProtocol,
			final StartupResponseData startupResponseData)
			throws AuthenticationException, JargonException {

		/*
		 * I'm creating a new protocol for PAM, using the newly renegotiated
		 * account with the new password, So save the auth information from the
		 * prior one used in the pam bootstrapping process
		 */
		AuthResponse originalAuthResponse = irodsMidLevelProtocol
				.getAuthResponse();

		AbstractIRODSMidLevelProtocol actualProtocol = irodsMidLevelProtocol
				.getIrodsProtocolManager()
				.getIrodsMidLevelProtocolFactory()
				.instance(
						irodsMidLevelProtocol.getIrodsSession(),
						irodsMidLevelProtocol.getAuthResponse()
								.getAuthenticatedIRODSAccount(),
						irodsMidLevelProtocol.getIrodsProtocolManager());
		actualProtocol.setAuthResponse(originalAuthResponse);
		return actualProtocol;

	}

}
