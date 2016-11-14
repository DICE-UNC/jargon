/**
 *
 */
package org.irods.jargon.core.transfer.encrypt;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.EncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for a cipher that will encrypt parallel transfer data
 *
 * see http://www.digizol.com/2009/10/java-encrypt-decrypt-jce-salt.html
 *
 * @author Mike Conway - DICE
 *
 */
public abstract class ParallelEncryptionCipherWrapper extends
ParallelCipherWrapper {

	public static final Logger log = LoggerFactory
			.getLogger(ParallelEncryptionCipherWrapper.class);

	ParallelEncryptionCipherWrapper(
			final PipelineConfiguration pipelineConfiguration,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
		super(pipelineConfiguration, negotiatedClientServerConfiguration);
	}

	/**
	 * Client method that will invoke the implementation-specific encrypt()
	 * method after checking for proper initialization <code>byte[]</code> of
	 * plaintext data
	 *
	 * @return {@link EncryptionBuffer}
	 * @throws EncryptionException
	 */
	public EncryptionBuffer encrypt(final byte[] input)
			throws EncryptionException {
		log.info("encrypt()");
		return doEncrypt(input);
	}

	/**
	 * Encrypt the given data
	 *
	 * @param input
	 *            <code>byte[]</code> of plaintext data
	 * @return {@link EncryptionBuffer}
	 * @throws EncryptionException
	 */
	protected abstract EncryptionBuffer doEncrypt(byte[] input)
			throws EncryptionException;

}
