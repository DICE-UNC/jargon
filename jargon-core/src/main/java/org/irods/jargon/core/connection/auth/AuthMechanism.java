package org.irods.jargon.core.connection.auth;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;

public abstract class AuthMechanism {

	/**
	 * Take the given account, and perform the authentication step using the
	 * connection in the given <code>irodsCommands</code>
	 * 
	 * @param irodsCommands
	 *            {@link IRODSCommands} that will be authenticating
	 * @param irodsAccount
	 *            {@link IRODSAccount} with the zone and principle information
	 * @return {@link AuthResponse} with information about the authentication
	 *         attempt
	 * @throws AuthenticationException
	 *             if the authentication proceeded normally, but the principle
	 *             could not be authenticated
	 * @throws JargonException
	 *             if the authentication proceeded abnormally, not caused by
	 *             simply being authorized
	 */
	public abstract AuthResponse authenticate(
			final IRODSCommands irodsCommands, final IRODSAccount irodsAccount)
			throws AuthenticationException, JargonException;

}