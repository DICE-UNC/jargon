/**
 * 
 */
package org.irods.jargon.core.transfer;

import javax.crypto.Cipher;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.irods.jargon.core.exception.JargonRuntimeException;

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
	private int mode;

	/**
	 * Default constructor with configuration information needed to set up the
	 * algorithm
	 * 
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} with connection properties
	 * @param negotiatedClientServerConfiguration
	 *            {@link NegotiatedClientServerConfiguration} with result of
	 *            negotiation
	 * @param mode
	 *            <code>int</code> that indicates encrypt/decrypt using
	 *            {@link Cipher} mode definitions
	 * @throws ClientServerNegotiationException
	 */
	ParallelEncryptionCipherWrapper(
			PipelineConfiguration pipelineConfiguration,
			NegotiatedClientServerConfiguration negotiatedClientServerConfiguration,
			int mode) {
		super();
		if (pipelineConfiguration == null) {
			throw new IllegalArgumentException("null pipelineConfiguration");
		}
		if (negotiatedClientServerConfiguration == null) {
			throw new IllegalArgumentException("null IllegalArgumentException");
		}

		if (!negotiatedClientServerConfiguration.isSslConnection()) {
			throw new JargonRuntimeException(
					"attempting to encrypt when not an SSL enabled connection");
		}

		this.pipelineConfiguration = pipelineConfiguration;
		this.negotiatedClientServerConfiguration = negotiatedClientServerConfiguration;
		this.mode = mode;
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

	/**
	 * Encrypt the buffer
	 * 
	 * @param input
	 *            <code>byte[]</code> to encrypt
	 * @return {@link EncryptionBuffer} with the data optional initialization
	 *         vector
	 * @throws ClientServerNegotiationException
	 */
	abstract EncryptionBuffer encrypt(final byte[] input)
			throws ClientServerNegotiationException;

	/**
	 * @return the cipher {@link Cipher}
	 */
	Cipher getCipher() {
		return cipher;
	}

	/**
	 * @param cipher
	 *            {@link Cipher} the cipher to set
	 */
	void setCipher(Cipher cipher) {
		this.cipher = cipher;
	}

	/**
	 * Decrypt the data, given a byte buffer and optionally an initialization
	 * vector
	 * 
	 * @param input
	 *            {@link EncryptionBuffer}
	 * @return <code>byte[]</code> with the decrypted data
	 */
	abstract byte[] decrypt(EncryptionBuffer input);

	/**
	 * @return the mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

}
