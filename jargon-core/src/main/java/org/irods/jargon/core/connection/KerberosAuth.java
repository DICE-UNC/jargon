/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Supports Kerberos authentication
 * 
 * @author conwaymc
 *
 */
public class KerberosAuth extends AuthMechanism {

	/**
	 * 
	 */
	public KerberosAuth() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.AuthMechanism#
	 * processAuthenticationAfterStartup(org.irods.jargon.core.connection.
	 * IRODSAccount, org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol,
	 * org.irods.jargon.core.connection.StartupResponseData)
	 */
	@Override
	protected AbstractIRODSMidLevelProtocol processAuthenticationAfterStartup(IRODSAccount irodsAccount,
			AbstractIRODSMidLevelProtocol irodsMidLevelProtocol, StartupResponseData startupResponseData)
			throws AuthenticationException, JargonException {
		return null;
	}

}
