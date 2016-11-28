/**
 *
 */
package org.irods.jargon.core.transfer.encrypt;

import javax.crypto.Cipher;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.protovalues.EncryptionAlgorithmEnum;

/**
 * Factory to generate the encryption wrapper type based on configuration
 * information. This is used for encryption determined by the client-server
 * negotiation process.
 *
 * @author Mike Conway - DICE
 *
 */
public class EncryptionWrapperFactory {

	/**
	 * Given the properties from configuration and negotiation, return the
	 * correct encryption wrapper for a parallel transfer thread. This method
	 * should not be called if no negotiated encryption was established.
	 *
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration}
	 * @param negotiatedClientServerConfiguration
	 *            {@link NegotiatedClientServerConfiguration}
	 * @return {@link ParallelCipherWrapper} to be used by parallel transfer
	 *         threads
	 *
	 * @throws ClientServerNegotiationException
	 */
	public static ParallelEncryptionCipherWrapper instanceEncrypt(
			final PipelineConfiguration pipelineConfiguration,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration)
					throws ClientServerNegotiationException {

		if (pipelineConfiguration == null) {
			throw new IllegalArgumentException("null pipelineConfiguration");
		}

		if (negotiatedClientServerConfiguration == null) {
			throw new IllegalArgumentException(
					"null negotiatedClientServerConfiguration");
		}

		if (!negotiatedClientServerConfiguration.isSslConnection()) {
			throw new JargonRuntimeException(
					"no encryption was negotiated,should not call this factory");
		}

		if (pipelineConfiguration.getEncryptionAlgorithmEnum() == EncryptionAlgorithmEnum.AES_256_CBC) {

			return new AesCipherEncryptWrapper(pipelineConfiguration,
					negotiatedClientServerConfiguration);
		} else {
			throw new ClientServerNegotiationException(
					"unsuppored encryption algo:"
							+ pipelineConfiguration
							.getEncryptionAlgorithmEnum());
		}

	}

	/**
	 * Given the properties from configuration and negotiation, return the
	 * correct decryption wrapper for a parallel transfer thread. This method
	 * should not be called if no negotiated encryption was established.
	 *
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration}
	 * @param negotiatedClientServerConfiguration
	 *            {@link NegotiatedClientServerConfiguration}
	 * @return {@link ParallelCipherWrapper} to be used by parallel transfer
	 *         threads
	 * @param mode
	 *            <code>int</code> that indicates encrypt/decrypt using the
	 *            constants in {@link Cipher}
	 * @throws ClientServerNegotiationException
	 */
	public static ParallelDecryptionCipherWrapper instanceDecrypt(
			final PipelineConfiguration pipelineConfiguration,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration)
					throws ClientServerNegotiationException {
		if (pipelineConfiguration == null) {
			throw new IllegalArgumentException("null pipelineConfiguration");
		}

		if (negotiatedClientServerConfiguration == null) {
			throw new IllegalArgumentException(
					"null negotiatedClientServerConfiguration");
		}

		if (!negotiatedClientServerConfiguration.isSslConnection()) {
			throw new JargonRuntimeException(
					"no encryption was negotiated,should not call this factory");
		}

		if (pipelineConfiguration.getEncryptionAlgorithmEnum() == EncryptionAlgorithmEnum.AES_256_CBC) {

			return new AesCipherDecryptWrapper(pipelineConfiguration,
					negotiatedClientServerConfiguration);
		} else {
			throw new ClientServerNegotiationException(
					"unsuppored decryption algo:"
							+ pipelineConfiguration
							.getEncryptionAlgorithmEnum());
		}

	}
}
