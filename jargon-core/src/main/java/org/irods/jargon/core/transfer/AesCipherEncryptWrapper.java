/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

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

	byte[] mInitVec = null;

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
		PipelineConfiguration pipelineConfiguration = this
				.getPipelineConfiguration();
		try {
			log.info("initCipher()");
			Cipher encryptionCipher = Cipher.getInstance(pipelineConfiguration
					.getEncryptionAlgorithmEnum().getCypherKey());
			this.setCipher(encryptionCipher);
			log.debug("have cipher:{}", encryptionCipher);

			SecretKeyFactory factory = SecretKeyFactory
					.getInstance(pipelineConfiguration
							.getEncryptionAlgorithmEnum().getKeyGenType());
			PBEKeySpec keySpec = new PBEKeySpec(this
					.getNegotiatedClientServerConfiguration()
					.getSslCryptChars(),
					RandomUtils
							.generateRandomBytesOfLength(pipelineConfiguration
									.getEncryptionSaltSize()),
					pipelineConfiguration.getEncryptionNumberHashRounds(),
					pipelineConfiguration.getEncryptionAlgorithmEnum()
							.getKeySize());

			SecretKey secretKey = factory.generateSecret(keySpec);
			encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey);
			AlgorithmParameters params = encryptionCipher.getParameters();

			// get the initialization vector and store as member var
			mInitVec = params.getParameterSpec(IvParameterSpec.class).getIV();

		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeySpecException | InvalidKeyException
				| InvalidParameterSpecException e) {
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
