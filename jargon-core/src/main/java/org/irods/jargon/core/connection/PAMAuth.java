/**
 * 
 */
package org.irods.jargon.core.connection;

import java.io.IOException;

import javax.net.ssl.SSLSocket;

import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.AuthReqPluginRequestInp;
import org.irods.jargon.core.packinstr.PamAuthRequestInp;
import org.irods.jargon.core.packinstr.SSLEndInp;
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

	@Override
	protected AbstractIRODSMidLevelProtocol processAuthenticationAfterStartup(
			final IRODSAccount irodsAccount,
			final AbstractIRODSMidLevelProtocol irodsCommands,
			final StartupResponseData startupResponseData)
			throws AuthenticationException, JargonException {

		SSLSocket sslSocket = irodsCommands.getIrodsSession()
				.instanceSslConnectionUtilities()
				.createSslSocketForProtocol(irodsAccount, irodsCommands);

		log.info("creating secure protcol connection layer");
		IRODSBasicTCPConnection secureConnection = new IRODSBasicTCPConnection(
				irodsAccount, irodsCommands.getPipelineConfiguration(),
				irodsCommands.getIrodsProtocolManager(), sslSocket,
				irodsCommands.getIrodsSession());

		IRODSMidLevelProtocol secureIRODSCommands = new IRODSMidLevelProtocol(
				secureConnection, irodsCommands.getIrodsProtocolManager());

		log.info("carrying over startup pack with server info");
		secureIRODSCommands.setStartupResponseData(irodsCommands
				.getStartupResponseData());

		log.debug("created secureIRODSCommands wrapped around an SSL socket\nSending PamAuthRequest...");

		// send pam auth request

		int pamTimeToLive = irodsCommands.getIrodsSession()
				.getJargonProperties().getPAMTimeToLive();

		Tag response = null;
		if (startupResponseData.isEirods()) {
			secureIRODSCommands.setForceSslFlush(true);
			log.info("using irods pluggable pam auth request");
			AuthReqPluginRequestInp pi = AuthReqPluginRequestInp.instancePam(
					irodsAccount.getProxyName(), irodsAccount.getPassword(),
					pamTimeToLive);
			response = secureIRODSCommands.irodsFunction(pi);

		} else {
			log.info("using normal irods pam auth request");
			PamAuthRequestInp pamAuthRequestInp = PamAuthRequestInp.instance(
					irodsAccount.getProxyName(), irodsAccount.getPassword(),
					pamTimeToLive);
			response = secureIRODSCommands.irodsFunction(pamAuthRequestInp);
		}

		if (response == null) {
			throw new JargonException("null response from pamAuthRequest");
		}

		String tempPasswordForPam;
		if (startupResponseData.isEirods()) {
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
		SSLEndInp sslEndInp = SSLEndInp.instance();
		secureIRODSCommands.irodsFunction(sslEndInp);

		try {
			secureIRODSCommands.closeOutSocketAndSetAsDisconnected();
		} catch (IOException e) {
			log.error("error closing ssl socket", e);
			throw new JargonException("error closing ssl socket", e);
		}

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

		AuthResponse authResponse = new AuthResponse();
		authResponse
				.setAuthenticatedIRODSAccount(irodsAccountUsingTemporaryIRODSPassword);
		authResponse.setAuthenticatingIRODSAccount(irodsAccount);
		authResponse.setStartupResponse(startupResponseData);
		authResponse.setSuccessful(true);
		irodsCommands.setAuthResponse(authResponse);

		return irodsCommands;

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

		IRODSAccount originalAuthenticatingAccount = irodsMidLevelProtocol
				.getAuthResponse().getAuthenticatingIRODSAccount();

		AbstractIRODSMidLevelProtocol actualProtocol = null;

		irodsMidLevelProtocol.disconnectWithForce();

		actualProtocol = irodsMidLevelProtocol
				.getIrodsProtocolManager()
				.getIrodsMidLevelProtocolFactory()
				.instance(
						irodsMidLevelProtocol.getIrodsSession(),
						irodsMidLevelProtocol.getAuthResponse()
								.getAuthenticatedIRODSAccount(),
						irodsMidLevelProtocol.getIrodsProtocolManager());
		actualProtocol.getAuthResponse().setAuthenticatingIRODSAccount(
				originalAuthenticatingAccount);
		actualProtocol.setIrodsAccount(originalAuthenticatingAccount);
		return actualProtocol;

	}

}
