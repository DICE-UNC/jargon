package org.irods.jargon.core.connection.auth;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.StartupResponseData;

/**
 * Represents information in response to an authentication attempt. This is
 * meant to hold generic responses to an authorization attempt.
 * <p>
 * Note that the authentication process may alter the iRODS account information,
 * and as such, the response contains both the {@code IRODSAccount} as presented
 * for login, and the account after the login process completes. For example,
 * when using PAM, the original account is presented as a PAM login, but the PAM
 * process creates a temporary account and then uses this account in a standard
 * iRODS login.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class AuthResponse {

	private String challengeValue = "";
	private boolean successful = false;
	private String authMessage = "";
	/**
	 * IRODSAccount as presented for authentication
	 */
	private IRODSAccount authenticatingIRODSAccount = null;
	/**
	 * IRODSAccount as finally authenticated, may be a different user, auth
	 * mechanism, etc
	 */
	private IRODSAccount authenticatedIRODSAccount = null;
	private Map<String, Object> responseProperties = new HashMap<String, Object>();
	/**
	 * response from the initial send of the startup packet, especially important if
	 * connection restarting is specified
	 */
	private StartupResponseData startupResponse;

	/**
	 * Get the (optional) challenge value used in the iRODS exchange.
	 *
	 * @return the challengeValue as an optional {@code String}, which is blank if
	 *         not used
	 */
	public String getChallengeValue() {
		return challengeValue;
	}

	/**
	 * @param challengeValue
	 *            the challengeValue to set
	 */
	public void setChallengeValue(final String challengeValue) {
		this.challengeValue = challengeValue;
	}

	/**
	 * Get a {@code Map<String,Object>} with optional properties generated as a
	 * result of this authentication process.
	 *
	 * @return the responseProperties {@code Map<String,Object>} with optional
	 *         properties from the authentication. Note that the {@code Map} will
	 *         always be returned, but may be empty.
	 */
	public Map<String, Object> getResponseProperties() {
		return responseProperties;
	}

	/**
	 * @param responseProperties
	 *            the responseProperties to set
	 */
	public void setResponseProperties(final Map<String, Object> responseProperties) {
		this.responseProperties = responseProperties;
	}

	// ? is this success thing even needed?

	/**
	 * @return a {@code boolean} that indicates success in the authentication
	 *         process
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * @param successful
	 *            {@code boolean} that will be true if the authentication process
	 *            did not succeed
	 */
	public void setSuccessful(final boolean successful) {
		this.successful = successful;
	}

	/**
	 * @return the authenticatedIRODSAccount {@link IRODSAccount} as a result of the
	 *         authentication process, including any augmented data. This may be
	 *         different than the account presented for authentication originally
	 */
	public IRODSAccount getAuthenticatedIRODSAccount() {
		return authenticatedIRODSAccount;
	}

	/**
	 * @param authenticatedIRODSAccount
	 *            {@link IRODSAccount} as a result of the authentication process,
	 *            including any augmented data
	 */
	public void setAuthenticatedIRODSAccount(final IRODSAccount authenticatedIRODSAccount) {
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
	public void setAuthMessage(final String authMessage) {
		this.authMessage = authMessage;
	}

	/**
	 * @return the startupResponse
	 */
	public StartupResponseData getStartupResponse() {
		return startupResponse;
	}

	/**
	 * @param startupResponse
	 *            the startupResponse to set
	 */
	public void setStartupResponse(final StartupResponseData startupResponse) {
		this.startupResponse = startupResponse;
	}

	/**
	 * @return the authenticatingIRODSAccount {@link IRODSAccount} as originally
	 *         presented for authentication
	 */
	public IRODSAccount getAuthenticatingIRODSAccount() {
		return authenticatingIRODSAccount;
	}

	/**
	 * @param authenticatingIRODSAccount
	 *            {@link IRODSAccount} as originally presented for authentication
	 */
	public void setAuthenticatingIRODSAccount(final IRODSAccount authenticatingIRODSAccount) {
		this.authenticatingIRODSAccount = authenticatingIRODSAccount;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 100;
		StringBuilder builder = new StringBuilder();
		builder.append("AuthResponse [successful=").append(successful).append(", ");
		if (authMessage != null) {
			builder.append("authMessage=").append(authMessage).append(", ");
		}
		if (authenticatingIRODSAccount != null) {
			builder.append("authenticatingIRODSAccount=").append(authenticatingIRODSAccount).append(", ");
		}
		if (authenticatedIRODSAccount != null) {
			builder.append("authenticatedIRODSAccount=").append(authenticatedIRODSAccount).append(", ");
		}
		if (responseProperties != null) {
			builder.append("responseProperties=").append(toString(responseProperties.entrySet(), maxLen)).append(", ");
		}
		if (startupResponse != null) {
			builder.append("startupResponse=").append(startupResponse);
		}
		builder.append("]");
		return builder.toString();
	}

	private String toString(final Collection<?> collection, final int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}

}
