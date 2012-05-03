package org.irods.jargon.core.connection.auth;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

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
 *         refs
 *         http://docs.oracle.com/javase/1.4.2/docs/guide/security/jgss/single
 *         -signon.html
 *         http://docs.oracle.com/javase/1.5.0/docs/guide/security/jgss
 *         /tutorials/BasicClientServer.html
 * 
 *         http://stackoverflow.com/questions/370878/how-to-obtain-a-kerberos-
 *         service-ticket-via-gss-api
 * 
 */
public class KerberosAuth extends AuthMechanism {

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
		
		log.info("authenticate()");
		
		/*
		 * if (irodsCommands == null) { throw new
		 * IllegalArgumentException("null irodsCommands"); }
		 */
		
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		
		log.debug("..creating GSSContext");
		
		LoginContext lc = null;

		try {
			lc = new LoginContext("JargonKrb");
			// attempt authentication
			lc.login();
			Subject signedOnUserSubject = lc.getSubject();
			log.info("subject:{}", signedOnUserSubject);

			log.info("getting authResponse");
			AuthResponse authResponse = (AuthResponse) Subject.doAs(
					signedOnUserSubject, new ServiceTicketGenerator(
							irodsAccount));
			log.info("successfully got authResponse:{}", authResponse);

		} catch (Exception e) {
			log.error("LoginException occurred", e);
			throw new AuthRuntimeException(e);
		}

		return new AuthResponse();

	}


}
