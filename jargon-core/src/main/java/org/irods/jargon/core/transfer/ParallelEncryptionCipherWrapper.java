/**
 * 
 */
package org.irods.jargon.core.transfer;

import javax.crypto.Cipher;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;

/**
 * Wrapper for an implementation that can encrypt bytes in a parallel file
 * transfer. Concrete subclasses implement the actual encryption, and the proper
 * encryption method is built using a factory
 * 
 * @author Mike Conway - DICE
 *
 */
abstract class ParallelEncryptionCipherWrapper {

	/**
	 * @param pipelineConfiguration
	 * @param negotiatedClientServerConfiguration
	 */
	ParallelEncryptionCipherWrapper(
			PipelineConfiguration pipelineConfiguration,
			NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
		super();
		this.pipelineConfiguration = pipelineConfiguration;
		this.negotiatedClientServerConfiguration = negotiatedClientServerConfiguration;
	}

	private Cipher cipher;
	private PipelineConfiguration pipelineConfiguration;
	private NegotiatedClientServerConfiguration negotiatedClientServerConfiguration;

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

	abstract byte[] encrypt(final byte[] input);

	abstract byte[] decrypt(final byte[] input);

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
	synchronized void setCipher(Cipher cipher) {
		this.cipher = cipher;
	}

}
