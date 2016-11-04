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
 * Wrapper for a cipher that will decrypt parallel transfer data
 *
 * @author Mike Conway - DICE
 *
 */
public abstract class ParallelDecryptionCipherWrapper extends
		ParallelCipherWrapper {

	public static final Logger log = LoggerFactory
			.getLogger(ParallelDecryptionCipherWrapper.class);

	ParallelDecryptionCipherWrapper(
			final PipelineConfiguration pipelineConfiguration,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
		super(pipelineConfiguration, negotiatedClientServerConfiguration);
	}

	/**
	 * Decrypt the given data, called by the client of this wrapper, this will
	 * call the
	 *
	 * @param input
	 *            {@link EncryptionBuffer}
	 * @return <code>byte[]</code> of plaintext data
	 * @throws EncryptionException
	 */
	public byte[] decrypt(final EncryptionBuffer input)
			throws EncryptionException {
		log.info("decrypt()");

		return doDecrypt(input);
	}

	/**
	 * Decryption method that will be overriden by the particular algo, and will
	 * happen after any init is checked
	 *
	 * @param input
	 *            <code>byte[]</code> of plaintext data
	 * @return {@link EncryptionBuffer}
	 * @throws EncryptionException
	 */
	protected abstract byte[] doDecrypt(EncryptionBuffer input)
			throws EncryptionException;

}
