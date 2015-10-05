/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.ClientServerNegotiationException;

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
class AesCipherWrapper extends ParallelEncryptionCipherWrapper {

	/**
	 * Default constructor with configuration information needed to set up the
	 * algorithm
	 * 
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration}
	 * @param negotiatedClientServerConfiguration
	 *            {@link NegotiatedClientServerConfiguration}
	 */
	AesCipherWrapper(
			PipelineConfiguration pipelineConfiguration,
			NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
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

			PBEKeySpec spec = new PBEKeySpec(this
					.getNegotiatedClientServerConfiguration().getSslCryptKey(),
					this.generateSalt(), this.getPipelineConfiguration()
							.getEncryptionNumberHashRounds(), this
							.getPipelineConfiguration().getEncryptionKeySize());

			SecretKey secretKey = factory.generateSecret(spec);
			SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(),
					"AES");

		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.ParallellEncryptionCipherWrapper#decrypt
	 * (byte[])
	 */
	@Override
	byte[] decrypt(byte[] input) {
		return null;
	}

}
