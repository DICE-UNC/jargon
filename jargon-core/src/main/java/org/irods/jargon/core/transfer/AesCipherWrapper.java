/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

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
		KeyGenerator keyGen;
		try {
			Cipher encryptionCypher = Cipher.getInstance(this
					.getPipelineConfiguration().getEncryptionAlgorithmEnum()
					.getCypherKey());
			keyGen = KeyGenerator.getInstance(this.getPipelineConfiguration()
					.getEncryptionAlgorithmEnum().getKeyGenType());
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		keyGen.init(this.getPipelineConfiguration().getEncryptionKeySize());

		SecretKey SecKey = keyGen.generateKey();
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
