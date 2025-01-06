/**
 *
 */
package org.irods.jargon.core.transfer.encrypt;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.EncryptionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Wrapper for a cipher that will decrypt parallel transfer data
 *
 * @author Mike Conway - DICE
 *
 */
public abstract class ParallelDecryptionCipherWrapper extends ParallelCipherWrapper {

	public static final Logger log = LogManager.getLogger(ParallelDecryptionCipherWrapper.class);

	ParallelDecryptionCipherWrapper(final PipelineConfiguration pipelineConfiguration,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
		super(pipelineConfiguration, negotiatedClientServerConfiguration);
	}

	/**
	 * Decrypt the given data, called by the client of this wrapper, this will call
	 * the
	 *
	 * @param input
	 *            {@link EncryptionBuffer}
	 * @return {@code byte[]} of plaintext data
	 * @throws EncryptionException
	 *             {@link EncryptionException}
	 */
	public byte[] decrypt(final EncryptionBuffer input) throws EncryptionException {
		log.info("decrypt()");

		return doDecrypt(input);
	}

	/**
	 * Decrypt given a complete buffer from iRODS. This can involve parsing out the
	 * buffer for encryption values such as initialization vectors, dependent on the
	 * underlying algorithm.
	 *
	 * @param fullBuffer
	 *            {@code byte[]} with the full buffer form iRODS, including any
	 *            encryption related payload
	 * @return {@code byte[]} with decrypted data
	 * @throws EncryptionException
	 *             {@link EncryptionException}
	 */
	public byte[] decrypt(final byte[] fullBuffer) throws EncryptionException {
		log.info("decrypt");
		return doDecrypt(fullBuffer);
	}

	protected abstract byte[] doDecrypt(byte[] fullBuffer);

	/**
	 * Decryption method that will be overriden by the particular algo, and will
	 * happen after any init is checked
	 *
	 * @param input
	 *            {@code byte[]} of plaintext data
	 * @return {@link EncryptionBuffer}
	 * @throws EncryptionException
	 *             {@link EncryptionException}
	 */
	protected abstract byte[] doDecrypt(EncryptionBuffer input) throws EncryptionException;

}
