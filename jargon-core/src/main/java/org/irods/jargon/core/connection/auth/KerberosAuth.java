package org.irods.jargon.core.connection.auth;

import org.ietf.jgss.GSSManager;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for Kerberos authentication to iRODS
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class KerberosAuth extends AuthMechanism {

	private GSSManager gssManager;
	public static final Logger log = LoggerFactory
			.getLogger(KerberosAuth.class);

	public KerberosAuth() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.connection.auth.AuthMechanism#authenticate(org.
	 * irods.jargon.core.connection.IRODSCommands,
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public AuthResponse authenticate(IRODSCommands irodsCommands,
			IRODSAccount irodsAccount) throws AuthenticationException,
			JargonException {
		return null;
	}


}
