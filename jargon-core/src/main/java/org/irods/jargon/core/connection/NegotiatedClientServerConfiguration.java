/**
 * 
 */
package org.irods.jargon.core.connection;

import org.irods.jargon.core.utils.RandomUtils;

/**
 * Captures characteristics resulting from a client-server negotiation
 * 
 * @author Mike Conway - DICE
 */
public class NegotiatedClientServerConfiguration {
	private final boolean sslConnection;
	private byte[] sslCryptKey;
	private char[] sslCryptChars;

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
	 * Initialize the key as bytes and chars for use in encryption algorithms.
	 * 
	 * @param pipelineConfiguration
	 */
	public synchronized void initKey(PipelineConfiguration pipelineConfiguration) {
		sslCryptChars = RandomUtils
				.generateRandomCharsForNBytes(pipelineConfiguration
						.getEncryptionAlgorithmEnum().getKeySize());
		sslCryptKey = new String(sslCryptChars).getBytes();

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
	 * @return <code>byte[]</code> with the sslCryptKey if client/server
	 *         negotiation uses SSL and wants to encrypt parallel transfers
	 */
	public byte[] getSslCryptKey() {
		return sslCryptKey;
	}

	public char[] getSslCryptChars() {
		return sslCryptChars;
	}

}
