package org.irods.jargon.core.connection.auth;

import org.ietf.jgss.GSSManager;
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


}
