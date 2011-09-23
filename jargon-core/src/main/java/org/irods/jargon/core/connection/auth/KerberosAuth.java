package org.irods.jargon.core.connection.auth;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for Kerberos authentication to iRODS
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class KerberosAuth {

	private final GSSName irodsServerGSSPeer;
	private final GSSCredential clientCredential;
	private GSSManager gssManager;
	public static final Logger log = LoggerFactory
			.getLogger(KerberosAuth.class);

	public KerberosAuth(final GSSName irodsServerGSSPeer,
			final GSSCredential clientCredential) {

		if (irodsServerGSSPeer == null) {
			throw new IllegalArgumentException("null irodsServerGSSPeer");
		}

		if (clientCredential == null) {
			throw new IllegalArgumentException("null clientCredential");
		}

		this.irodsServerGSSPeer = irodsServerGSSPeer;
		this.clientCredential = clientCredential;

	}

	public void initializeContext() {
		gssManager = GSSManager.getInstance();
	}

	/**
	 * @return the irodsServerGSSPeer
	 */
	public GSSName getIrodsServerGSSPeer() {
		return irodsServerGSSPeer;
	}

	/**
	 * @return the clientCredential
	 */
	public GSSCredential getClientCredential() {
		return clientCredential;
	}

}
