/**
 * 
 */
package org.irods.jargon.core.transfer;

import javax.crypto.Cipher;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;

/**
 * Wrapper for a cipher that will decrypt parallel transfer data
 * 
 * @author Mike Conway - DICE
 *
 */
public abstract class ParallelDecryptionCipherWrapper extends
		ParallelCipherWrapper {

	ParallelDecryptionCipherWrapper(
			PipelineConfiguration pipelineConfiguration,
			NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
		super(pipelineConfiguration, negotiatedClientServerConfiguration,
				Cipher.ENCRYPT_MODE);
	}

	/**
	 * Decrypt the given data
	 * 
	 * @param input
	 *            {@link EncryptionBuffer}
	 * @return <code>byte[]</code> of plaintext data
	 */
	abstract byte[] decrypt(EncryptionBuffer input);

}
