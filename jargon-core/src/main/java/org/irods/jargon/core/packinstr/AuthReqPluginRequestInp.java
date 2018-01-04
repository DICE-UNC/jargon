/**
 *
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.connection.IrodsVersion;
import org.irods.jargon.core.connection.StartupResponseData;
import org.irods.jargon.core.exception.JargonException;

/**
 *
 * @author Mike Conway - DICE (www.irods.org) see http://code.renci.org for
 *         trackers, access info, and documentation
 *
 */
public class AuthReqPluginRequestInp extends AbstractIRODSPackingInstruction {

	public static final int AUTH_REQ_API_NBR = 1201;

	private String authScheme = "";
	private String userName = "";
	private String password = "";
	private int timeToLive = 0;

	public static final String AUTH_SCHEME_PAM = "PAM";

	/**
	 * Create an auth request for PAM
	 *
	 * @param userName
	 *            {@code String} with the userName
	 * @param password
	 *            {@code String} with the password
	 * @param timeToLive
	 *            {@code int} with time to live for password
	 * @param startupResponseData
	 *            {@link StartupResponseData} acquired during handshake, carrying
	 *            iRODS version information necessary to send the correct auth
	 *            request
	 * @return {@link AuthReqPluginRequestInp}
	 */
	public static AuthReqPluginRequestInp instancePam(final String userName, final String password,
			final int timeToLive, final StartupResponseData startupResponseData) {

		return new AuthReqPluginRequestInp(AUTH_SCHEME_PAM, userName, password, timeToLive, startupResponseData);
	}

	private AuthReqPluginRequestInp(final String authScheme, final String userName, final String password,
			final int timeToLive, StartupResponseData startupResponseData) {

		if (authScheme == null || authScheme.isEmpty()) {
			throw new IllegalArgumentException("null or empty authScheme");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("null or empty password");
		}

		this.authScheme = authScheme;
		this.userName = userName;
		IrodsVersion irodsVersion = new IrodsVersion(startupResponseData.getRelVersion());

		if (!irodsVersion.hasVersionOfAtLeast("rods4.2.0")) {
			this.password = password.replaceAll(";", "\\\\;");
		} else {
			this.password = password;
		}

		setApiNumber(AUTH_REQ_API_NBR);
		this.timeToLive = timeToLive;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue
	 * ()
	 */
	@Override
	public Tag getTagValue() throws JargonException {
		Tag message = new Tag("authPlugReqInp_PI",

				new Tag[] { new Tag("auth_scheme_", authScheme), new Tag("context_", getContext()) });
		return message;
	}

	/**
	 * Get the context string, right now assumes pam, will need a switch based on
	 * auth method later
	 *
	 * @return
	 */
	private String getContext() {
		StringBuilder sb = new StringBuilder();
		sb.append("a_user=");
		sb.append(userName);
		sb.append(";a_pw=");
		sb.append(password);
		sb.append(";a_ttl=");
		sb.append(timeToLive);
		return sb.toString();
	}

}
