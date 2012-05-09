package org.irods.jargon.core.connection;

import org.irods.jargon.core.connection.IRODSAccount.AuthScheme;
import org.irods.jargon.core.connection.auth.AuthUnavailableException;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of a factory that can create an implementation of
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class AuthenticationFactoryImpl implements AuthenticationFactory {

	private Logger log = LoggerFactory
			.getLogger(AuthenticationFactoryImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.auth.AuthenticationFactory#
	 * instanceAuthMechanism(java.lang.String)
	 */
	@Override
	public AuthMechanism instanceAuthMechanism(final String authScheme)
			throws AuthUnavailableException, JargonException {

		log.info("instanceAuthMechanism()");

		if (authScheme == null || authScheme.isEmpty()) {
			throw new IllegalArgumentException("null or blank authScheme");
		}

		log.info("authScheme:{}", authScheme);

		if (authScheme == AuthScheme.GSI.name()) {
			log.info("generating GSI Auth");
			return new GSIAuth();
		} else if (authScheme == AuthScheme.KERBEROS.name()) {
			log.info("generating Kerberos auth");
			return new KerberosAuth();
		} else if (authScheme == AuthScheme.STANDARD.name()) {
			log.info("using standard auth");
			return new StandardIRODSAuth();
		} else {
			throw new AuthUnavailableException("auth method not avaialble for:"
					+ authScheme);
		}

	}

}
