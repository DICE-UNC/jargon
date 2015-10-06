/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps encryption/decryption of a byte buffer using AES
 * 
 * see http://karanbalkar.com/2014/02/tutorial-76-implement-aes-256-
 * encryptiondecryption-using-java/
 * 
 * @author Mike Conway - DICE
 * 
 * 
 */
class AesCipherEncryptWrapper extends AbstractParallelEncryptWrapper {

	public static final Logger log = LoggerFactory
			.getLogger(AesCipherEncryptWrapper.class);

	/**
	 * Default constructor with configuration information needed to set up the
	 * algorithm
	 * 
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration}
	 * @param negotiatedClientServerConfiguration
	 *            {@link NegotiatedClientServerConfiguration}
	 * @throws ClientServerNegotiationException
	 */
	AesCipherEncryptWrapper(
			PipelineConfiguration pipelineConfiguration,
			NegotiatedClientServerConfiguration negotiatedClientServerConfiguration)
			throws ClientServerNegotiationException {
		super(pipelineConfiguration, negotiatedClientServerConfiguration);
		initCipher();
	}

	/**
	 * Given the configuration, initialize the cipher
	 */
	private void initCipher() throws ClientServerNegotiationException {

		/*
		 * see lib/core/src/irods_buffer_encryption.cpp ~ line 178 encrypt
		 * routine
		 */

		try {
			final SecretKey key = new SecretKeySpec(this
					.getNegotiatedClientServerConfiguration().getSslCryptKey(),
					this.getPipelineConfiguration()
							.getEncryptionAlgorithmEnum().getCypherKey());

			Cipher cipher = Cipher.getInstance(this.getPipelineConfiguration()
					.getEncryptionAlgorithmEnum().getCypherKey());
			cipher.init(Cipher.ENCRYPT_MODE, key);

		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeyException e) {
			log.error("error initCypher()", e);
			throw new ClientServerNegotiationException(
					"unable to initialze negotiated cypher");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.ParallellEncryptionCipherWrapper#encrypt
	 * (byte[])
	 */
	@Override
	byte[] encrypt(byte[] input) {
		return null;
	}

}
