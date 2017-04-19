/**
 *
 */
package org.irods.jargon.core.packinstr;

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
	 *            <code>String</code> with the userName
	 * @param password
	 *            <code>String</code> with the password
	 * @param timeToLive
	 *            <code>int</code> with time to live for password
	 * @return {@link AuthReqPluginRequestInp}
	 */
	public static AuthReqPluginRequestInp instancePam(final String userName,
			final String password, final int timeToLive) {

		return new AuthReqPluginRequestInp(AUTH_SCHEME_PAM, userName, password,
				timeToLive);
	}

	/**
	 *
	 */
	private AuthReqPluginRequestInp(final String authScheme,
			final String userName, final String password, final int timeToLive) {

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
		this.password = password.replaceAll(";", "\\\\;");
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
		Tag message = new Tag("authPlugReqInp_PI", new Tag[] {
				new Tag("auth_scheme_", authScheme),
				new Tag("context_", getContext()) });
		return message;
	}

	/**
	 * Get the context string, right now assumes pam, will need a switch based
	 * on auth method later
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
