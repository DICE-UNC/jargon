package org.irods.jargon.core.connection.auth;

import java.util.HashMap;
import java.util.Map;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSAccount.AuthScheme;

/**
 * Represents information in response to an authentication attempt. This is
 * meant to hold generic responses to an authorization attempt.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class AuthResponse {

	private AuthScheme authType = AuthScheme.STANDARD;
	private String challengeValue = "";
	private boolean successful = false;
	private String authMessage = "";
	private IRODSAccount authenticatedIRODSAccount = null;
	private Map<String, Object> responseProperties = new HashMap<String, Object>();

	/**
	 * Get the (optional) challenge value used in the iRODS exchange.
	 * 
	 * @return the challengeValue as an optional <code>String</code>, which is
	 *         blank if not used
	 */
	public String getChallengeValue() {
		return challengeValue;
	}

	/**
	 * @param challengeValue
	 *            the challengeValue to set
	 */
	public void setChallengeValue(String challengeValue) {
		this.challengeValue = challengeValue;
	}

	/**
	 * Get a <code>Map<String,Object></code> with optional properties generated
	 * as a result of this authentication process.
	 * 
	 * @return the responseProperties <code>Map<String,Object></code> with
	 *         optional properties from the authentication. Note that the
	 *         <code>Map</code> will always be returned, but may be empty.
	 */
	public Map<String, Object> getResponseProperties() {
		return responseProperties;
	}

	/**
	 * @param responseProperties
	 *            the responseProperties to set
	 */
	public void setResponseProperties(Map<String, Object> responseProperties) {
		this.responseProperties = responseProperties;
	}

	// ? is this success thing even needed?

	/**
	 * @return a <code>boolean</code> that indicates success in the
	 *         authentication process
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * @param successful
	 *            <code>boolean</code> that will be true if the authentication
	 *            process did not succeed
	 */
	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	/**
	 * @return the authenticatedIRODSAccount
	 */
	public IRODSAccount getAuthenticatedIRODSAccount() {
		return authenticatedIRODSAccount;
	}

	/**
	 * @param authenticatedIRODSAccount
	 *            the authenticatedIRODSAccount to set
	 */
	public void setAuthenticatedIRODSAccount(
			IRODSAccount authenticatedIRODSAccount) {
		this.authenticatedIRODSAccount = authenticatedIRODSAccount;
	}

	/**
	 * @return the authMessage if any
	 */
	public String getAuthMessage() {
		return authMessage;
	}

	/**
	 * @param authMessage
	 *            the authMessage to set
	 */
	public void setAuthMessage(String authMessage) {
		this.authMessage = authMessage;
	}

	/**
	 * @return the authType
	 */
	public AuthScheme getAuthType() {
		return authType;
	}

	/**
	 * @param authType the authType to set
	 */
	public void setAuthType(AuthScheme authType) {
		this.authType = authType;
	}

}
