/**
 * 
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * PAM module authorization request for Chris Smith's PAM enhancement to iRODS
 * as of 3.2
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PamAuthRequestInp extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "pamAuthRequestInp_PI";
	public static final int PAM_API_NBR = 725;

	private final String userName;
	private final String password;
	private final int pamTimeToLive;

	/**
	 * Instance method creates a PAM auth request given a user name and password
	 * 
	 * @param userName
	 *            <code>String</code> with the user name for PAM
	 * @param password
	 *            <code>String</code> with the PAM password
	 * @param pamTimeToLive
	 *            <code>int</code> with the pam time to live in seconds
	 * @return <code>PamAuthRequestInp</code>
	 */
	public static PamAuthRequestInp instance(final String userName,
			final String password, final int pamTimeToLive) {
		return new PamAuthRequestInp(userName, password, pamTimeToLive);
	}

	/**
	 * Private constructor (use instance method)
	 * 
	 * @param userName
	 *            <code>String</code> with the user name for PAM
	 * @param password
	 *            <code>String</code> with the PAM password
	 * @param pamTimeToLive
	 *            <code>int</code> with the pam time to live in seconds
	 */
	private PamAuthRequestInp(final String userName, final String password,
			final int pamTimeToLive) {

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userId");
		}

		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("null or empty password");
		}

		if (pamTimeToLive <= 0) {
			throw new IllegalArgumentException("pamTimeToLive <= 0");
		}

		this.userName = userName;
		this.password = password;
		this.pamTimeToLive = pamTimeToLive;
		this.setApiNumber(PAM_API_NBR);

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
		Tag message = new Tag(PI_TAG);
		message.addTag("pamUser", userName);
		message.addTag("pamPassword", password);
		message.addTag("timeToLive", pamTimeToLive);
		return message;
	}

}
