/**
 * 
 */
package org.irods.jargon.core.connection;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;

import org.irods.jargon.core.exception.EncryptionException;
import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Captures characteristics resulting from a client-server negotiation
 * 
 * @author Mike Conway - DICE
 */
public class NegotiatedClientServerConfiguration {
	private final boolean sslConnection;
	private byte[] sslCryptKey;
	private char[] sslCryptChars;

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
	 * Initialize the key as bytes and chars for use in encryption algorithms.
	 * 
	 * @param pipelineConfiguration
	 * @throws EncryptionException
	 */
	public synchronized void initKey(PipelineConfiguration pipelineConfiguration)
			throws EncryptionException {

		if (pipelineConfiguration.getEncryptionAlgorithmEnum() == EncryptionAlgorithmEnum.AES_256_CBC) {
			log.info("generated AES 256 key");
			Key key;
			SecureRandom rand = new SecureRandom();
			KeyGenerator generator;
			try {
				generator = KeyGenerator.getInstance("AES");
				generator.init(rand);
				generator.init(256);
				key = generator.generateKey();
				sslCryptKey = key.getEncoded();
				sslCryptChars = new String(sslCryptKey, StandardCharsets.UTF_8)
						.toCharArray();
			} catch (NoSuchAlgorithmException e) {
				log.error("unsupported secret key algorithm:{}",
						pipelineConfiguration.getEncryptionAlgorithmEnum(), e);
				throw new EncryptionException(
						"unable to generate a suitable key for the given encryption algo",
						e);
			}

		} else {
			log.error("unsupported secret key algorithm:{}",
					pipelineConfiguration.getEncryptionAlgorithmEnum());
			throw new EncryptionException(
					"unable to generate a suitable key for the given encryption algo");
		}

		/*
		 * sslCryptChars = RandomUtils.generateRandomChars(pipelineConfiguration
		 * .getEncryptionAlgorithmEnum().getKeySize() / 16); // bits to // chars
		 * sslCryptKey = new String(sslCryptChars)
		 * .getBytes(StandardCharsets.UTF_8);
		 */

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
	public synchronized byte[] getSslCryptKey() {
		return sslCryptKey;
	}

	public synchronized char[] getSslCryptChars() {
		return sslCryptChars;
	}

}
