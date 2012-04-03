package org.irods.jargon.core.connection.auth;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
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

	private final GSSManager gssManager;
	private final Oid krb5Mechanism;
	public static final Logger log = LoggerFactory
			.getLogger(KerberosAuth.class);

	public KerberosAuth() {
		gssManager = GSSManager.getInstance();
		try {
			krb5Mechanism = new Oid("1.2.840.113554.1.2.2");
		} catch (GSSException e) {
			log.error("exception creating kerberos mechanism", e);
			throw new AuthRuntimeException("error creating Oid for Kerberos", e);
		}
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
		
		GSSName clientName;
		try {
			clientName = gssManager.createName(irodsAccount.getUserName(),
					GSSName.NT_USER_NAME);
			log.debug("got gssClientName:{}", clientName);
			GSSCredential clientCreds = gssManager.createCredential(clientName,
					8 * 3600, krb5Mechanism, GSSCredential.DEFAULT_LIFETIME);
			log.debug("got gssCredential:{}", clientCreds);
		} catch (GSSException e) {
			log.error("GSSException occurred", e);
			throw new AuthRuntimeException(e);
		}

		return new AuthResponse();

		/*
		 * GSSContext gssContext = gssManager.createContext(clientName, null,
		 * myCred, GSSContext.DEFAULT_LIFETIME);
		 */
	}


}
