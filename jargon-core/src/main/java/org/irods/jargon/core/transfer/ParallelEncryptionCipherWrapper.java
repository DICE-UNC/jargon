/**
 * 
 */
package org.irods.jargon.core.transfer;

import javax.crypto.Cipher;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.ClientServerNegotiationException;

/**
 * Wrapper for an implementation that can encrypt bytes in a parallel file
 * transfer. Concrete subclasses implement the actual encryption, and the proper
 * encryption method is built using a factory
 * 
 * @author Mike Conway - DICE
 *
 */
abstract class ParallelEncryptionCipherWrapper {

	private Cipher cipher;
	private PipelineConfiguration pipelineConfiguration;
	private NegotiatedClientServerConfiguration negotiatedClientServerConfiguration;

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

	/**
	 * @return the pipelineConfiguration
	 */
	PipelineConfiguration getPipelineConfiguration() {
		return pipelineConfiguration;
	}

	/**
	 * @return the negotiatedClientServerConfiguration
	 */
	NegotiatedClientServerConfiguration getNegotiatedClientServerConfiguration() {
		return negotiatedClientServerConfiguration;
	}

	abstract byte[] encrypt(final byte[] input)
			throws ClientServerNegotiationException;

	abstract byte[] decrypt(final byte[] input)
			throws ClientServerNegotiationException;

	/**
	 * @return the cipher
	 */
	Cipher getCipher() {
		return cipher;
	}

	/**
	 * @param cipher
	 *            the cipher to set
	 */
	void setCipher(Cipher cipher) {
		this.cipher = cipher;
	}

}
