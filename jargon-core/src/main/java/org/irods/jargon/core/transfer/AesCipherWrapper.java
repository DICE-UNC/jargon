/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
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
 * and
 * 
 * http://pastebin.com/YiwbCAW8
 * 
 * and
 * 
 * http://stackoverflow.com/questions/1440030/how-to-implement-java-256-bit-aes-
 * encryption-with-cbc
 * 
 * and
 * 
 * http://stackoverflow.com/questions/20796042/aes-encryption-and-decryption-
 * with-java
 * 
 * @author Mike Conway - DICE
 * 
 *
 */
class AesCipherWrapper extends ParallelEncryptionCipherWrapper {

	public static final Logger log = LoggerFactory
			.getLogger(AesCipherWrapper.class);

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
	 *            {@link Mode} that indicates encrypt/decrypt
	 * @throws ClientServerNegotiationException
	 */
	AesCipherWrapper(
			PipelineConfiguration pipelineConfiguration,
			NegotiatedClientServerConfiguration negotiatedClientServerConfiguration,
			final int mode) throws ClientServerNegotiationException {
		super(pipelineConfiguration, negotiatedClientServerConfiguration, mode);
	}

	/**
	 * Given the configuration, initialize the cipher
	 * 
	 * see rcPortalOper at about line 335 in rcPartialDataPut
	 */
	private void initCipher(final int mode)
			throws ClientServerNegotiationException {
		PipelineConfiguration pipelineConfiguration = this
				.getPipelineConfiguration();
		try {
			log.info("initCipher()");
			Cipher encryptionCipher = Cipher.getInstance(pipelineConfiguration
					.getEncryptionAlgorithmEnum().getCypherKey());
			this.setCipher(encryptionCipher);
			log.debug("have cipher:{}", encryptionCipher);

			SecretKey secretKey = initSecretKey(pipelineConfiguration);

			encryptionCipher.init(mode, secretKey);

		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeySpecException | InvalidKeyException e) {
			log.error("error generating key for cipher", e);
			throw new ClientServerNegotiationException(
					"cannot generate key for cipher", e);
		}

	}

	/**
	 * @param pipelineConfiguration
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	private SecretKey initSecretKey(PipelineConfiguration pipelineConfiguration)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory
				.getInstance(pipelineConfiguration.getEncryptionAlgorithmEnum()
						.getKeyGenType());
		KeySpec keySpec = new PBEKeySpec(this
				.getNegotiatedClientServerConfiguration().getSslCryptChars(),
				RandomUtils.generateRandomBytesOfLength(pipelineConfiguration
						.getEncryptionSaltSize()),
				pipelineConfiguration.getEncryptionNumberHashRounds(),
				pipelineConfiguration.getEncryptionAlgorithmEnum().getKeySize());

		SecretKey temp = factory.generateSecret(keySpec);
		SecretKey secretKey = new SecretKeySpec(temp.getEncoded(), "AES");
		return secretKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.ParallellEncryptionCipherWrapper#encrypt
	 * (byte[])
	 */
	@Override
	EncryptionBuffer encrypt(byte[] input)
			throws ClientServerNegotiationException {

		log.info("encrypt");
		if (input == null) {
			throw new IllegalArgumentException("null input");
		}

		try {

			log.debug("init cipher");
			initCipher(this.getMode());
			log.debug("init done");
			AlgorithmParameters params = this.getCipher().getParameters();

			// get the initialization vector and store as member var
			byte[] mInitVec = params.getParameterSpec(IvParameterSpec.class)
					.getIV();

			log.debug("encrypting");
			byte[] encrypted = getCipher().doFinal(input);
			return new EncryptionBuffer(mInitVec, encrypted);

		} catch (IllegalBlockSizeException | BadPaddingException
				| InvalidParameterSpecException e) {
			log.error("error during encryption", e);
			throw new ClientServerNegotiationException(
					"parameter spec error in encryption", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.ParallelEncryptionCipherWrapper#decrypt
	 * (org.irods.jargon.core.transfer.EncryptionBuffer)
	 */
	@Override
	byte[] decrypt(EncryptionBuffer input) {
		try {

			IvParameterSpec iv = new IvParameterSpec(
					input.getInitializationVector());

			byte[] original = getCipher().doFinal(input.getEncryptedData());
			return original;

		} catch (IllegalBlockSizeException | BadPaddingException e) {
			log.error("error during encryption", e);
			throw new ClientServerNegotiationException(
					"Unable to decrypt given negotiated settings", e);
		}
	}

	/**
	 * Create an initialization vector of random bytes for the key length
	 * 
	 * @param ivLength
	 * @return
	 */
	@SuppressWarnings("unused")
	// TODO: might not need this
	private byte[] generateIv(final int ivLength) {
		if (ivLength <= 0) {
			throw new IllegalArgumentException("invalid iv length");
		}

		return RandomUtils.generateRandomBytesOfLength(ivLength);
	}

}
