/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

import edu.sdsc.grid.io.irods.Tag;

/**
 * Translate a request into an AuthResponse_PI xml protocol request.
 * 
 * @auther Mike Conway - DICE (www.irods.org)
 * 
 */
public class AuthResponseInp extends AbstractIRODSPackingInstruction {

	private String username;
	private String response;
	public static final String PI_TAG = "authResponseInp_PI";
	public static final String RESPONSE_TAG = "response";
	public static final String ACCOUNT_TAG = "username";
	public static final String SERVER_DN = "ServerDN";

	public AuthResponseInp(final String username, final String response) {
		super();
		this.username = username;
		this.response = response;
	}

	public String getResponse() {
		return response;
	}

	public String getUsername() {
		return username;
	}

	@Override
	public Tag getTagValue() throws JargonException {
		Tag authResponse = new Tag(AuthResponseInp.PI_TAG,
				new Tag[] { new Tag(RESPONSE_TAG, response),
						new Tag(ACCOUNT_TAG, username) });
		return authResponse;
	}

}
