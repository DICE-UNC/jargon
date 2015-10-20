/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.irods.jargon.core.utils.RandomUtils;
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
class AesCipherWrapper extends ParallelEncryptionCipherWrapper {

	public static final Logger log = LoggerFactory
			.getLogger(AesCipherWrapper.class);

	private KeyGenerator keyGen = null;
	PBEKeySpec keySpec = null;

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
	AesCipherWrapper(
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
		PipelineConfiguration pipelineConfiguration = this.getPipelineConfiguration();
		try {
			Cipher encryptionCypher = Cipher.getInstance(pipelineConfiguration.getEncryptionAlgorithmEnum()
					.getCypherKey());
			this.setCipher(encryptionCypher);
			keySpec =  new PBEKeySpec(this.getNegotiatedClientServerConfiguration().getSslCryptChars(), RandomUtils.generateRandomBytesOfLength(pipelineConfiguration.getEncryptionSaltSize(),
					pipelineConfiguration.getEncryptionNumberHashRounds(), pipelineConfiguration.getEncryptionAlgorithmEnum().getKeySize());
			keyGen = KeyGenerator.getInstance(this.getPipelineConfiguration()
					.getEncryptionAlgorithmEnum().getKeyGenType());
			keyGen.init(this.getPipelineConfiguration()
					.getEncryptionAlgorithmEnum().getKeySize());
			keyGen.generateKey();

		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			log.error("error generating key for cipher", e);
			throw new ClientServerNegotiationException(
					"cannot generate key for cipher", e);
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
	byte[] encrypt(byte[] input) throws ClientServerNegotiationException {
		AlgorithmParameters params = getCipher().getParameters();
		try {

			byte[] ivBytes = generateIv(this.getPipelineConfiguration()
					.getEncryptionKeySize());

			return getCipher().doFinal(input);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			log.error("error during encryption", e);
			throw new ClientServerNegotiationException(
					"parameter spec error in encryption", e);
		}
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

	/**
	 * Create an initialization vector of random bytes for the key length
	 * 
	 * @param ivLength
	 * @return
	 */
	private byte[] generateIv(final int ivLength) {
		if (ivLength <= 0) {
			throw new IllegalArgumentException("invalid iv length");
		}

		return RandomUtils.generateRandomBytesOfLength(ivLength);
	}

}
