/**
 * 
 */
package org.irods.jargon.core.connection;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Captures characteristics resulting from a client-server negotiation
 * 
 * @author Mike Conway - DICE
 */
public class NegotiatedClientServerConfiguration {
	private final boolean sslConnection;
	private SecretKey secretKey;

	private Logger log = LoggerFactory
			.getLogger(NegotiatedClientServerConfiguration.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public synchronized String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NegotiatedClientServerConfiguration [sslConnection=");
		builder.append(sslConnection);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the sslConnection
	 */
	public synchronized boolean isSslConnection() {
		return sslConnection;
	}

	/**
	 * Default constructor
	 * 
	 * @param sslConnection
	 *            <code>boolean</code> that will be <code>true</code> if ssl is
	 *            used
	 */
	public NegotiatedClientServerConfiguration(final boolean sslConnection) {
		super();
		this.sslConnection = sslConnection;
	}

	/**
	 * @return the secretKey
	 */
	public SecretKey getSecretKey() {
		return secretKey;
	}

	/**
	 * @param secretKey the secretKey to set
	 */
	public void setSecretKey(SecretKey secretKey) {
		this.secretKey = secretKey;
	}

}
