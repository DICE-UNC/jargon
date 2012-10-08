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


	@Override
	public AuthMechanism instanceAuthMechanism(final IRODSAccount irodsAccount)
			throws AuthUnavailableException, JargonException {

		log.info("instanceAuthMechanism()");
		
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null or blank irodsAccount");
		}

		AuthScheme authScheme ;
		
		if (irodsAccount.getUserName().equals(IRODSAccount.PUBLIC_USERNAME)) {
			log.info("account is anonymous, use default auth scheme");
			authScheme = AuthScheme.STANDARD;
		} else {
			authScheme = irodsAccount.getAuthenticationScheme();
		}
		

		log.info("authScheme:{}", authScheme);

		if (authScheme.equals(AuthScheme.STANDARD)) {
			log.info("using standard auth");
			return new StandardIRODSAuth();
		} else if (authScheme.equals(AuthScheme.PAM)) {
			log.info("using PAM auth");
			return new PAMAuth();
		} else {
			throw new AuthUnavailableException("auth method not avaialble for:"
					+ authScheme);
		}

	}

}
