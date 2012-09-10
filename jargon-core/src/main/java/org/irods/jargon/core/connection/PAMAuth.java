/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;

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


	/* (non-Javadoc)
	 * @see org.irods.jargon.core.connection.AuthMechanism#processAuthenticationAfterStartup(org.irods.jargon.core.connection.IRODSAccount, org.irods.jargon.core.connection.IRODSCommands)
	 */
	@Override
	protected AuthResponse processAuthenticationAfterStartup(
			IRODSAccount irodsAccount, IRODSCommands irodsCommands)
			throws AuthenticationException, JargonException {

		// start ssl

		// send pam auth request
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.AuthMechanism#
	 * postConnectionStartupPreAuthentication()
	 */
	@Override
	protected void postConnectionStartupPreAuthentication()
			throws JargonException {

		super.postConnectionStartupPreAuthentication();

	}

}
