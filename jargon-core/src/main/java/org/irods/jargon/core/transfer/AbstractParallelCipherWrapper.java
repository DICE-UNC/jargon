/**
 *
 */
package org.irods.jargon.core.transfer;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import javax.crypto.Cipher;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Wrapper for an implementation that can encrypt/decrypt bytes in a parallel
 * file transfer. Concrete subclasses implement the actual encryption, and the
 * proper encryption method is built using a factory
 *
 * @author Mike Conway - DICE
 *
 */
abstract class AbstractParallelCipherWrapper {

	public static final Logger log = LogManager.getLogger(AbstractParallelCipherWrapper.class);

	/**
	 * @param pipelineConfiguration
	 * @param negotiatedClientServerConfiguration
	 */
	AbstractParallelCipherWrapper(final PipelineConfiguration pipelineConfiguration,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
		super();
		this.pipelineConfiguration = pipelineConfiguration;
		this.negotiatedClientServerConfiguration = negotiatedClientServerConfiguration;
	}

	private Cipher cipher;
	private final PipelineConfiguration pipelineConfiguration;
	private final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration;

	/**
	 * @return the pipelineConfiguration
	 */
	synchronized PipelineConfiguration getPipelineConfiguration() {
		return pipelineConfiguration;
	}

	/**
	 * @return the negotiatedClientServerConfiguration
	 */
	synchronized NegotiatedClientServerConfiguration getNegotiatedClientServerConfiguration() {
		return negotiatedClientServerConfiguration;
	}

	/**
	 * @return the cipher
	 */
	synchronized Cipher getCipher() {
		return cipher;
	}

	/**
	 * @param cipher
	 *            the cipher to set
	 */
	synchronized void setCipher(final Cipher cipher) {
		this.cipher = cipher;
	}

	/**
	 * Generate a salt value configured by the {@code PipelineConfiguration} that
	 * was the decided upon during client/server negotiation
	 *
	 * @return {@code byte[]} of the desired salt size
	 */
	byte[] generateSalt() throws JargonException {
		if (!negotiatedClientServerConfiguration.isSslConnection()) {
			throw new JargonRuntimeException("salt should not be generated when SSL not configured");
		}
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[pipelineConfiguration.getEncryptionSaltSize()];
		random.nextBytes(bytes);
		String s = new String(bytes);
		try {
			return s.getBytes(pipelineConfiguration.getDefaultEncoding());
		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding:{}", pipelineConfiguration.getDefaultEncoding(), e);
			throw new JargonException("Cannot encode salt value", e);
		}
	}
}
