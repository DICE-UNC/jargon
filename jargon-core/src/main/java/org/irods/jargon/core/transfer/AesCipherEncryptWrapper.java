/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps encryption of a byte buffer using AES
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
class AesCipherEncryptWrapper extends ParallelEncryptionCipherWrapper {

	public static final Logger log = LoggerFactory
			.getLogger(AesCipherEncryptWrapper.class);

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
	AesCipherEncryptWrapper(
			PipelineConfiguration pipelineConfiguration,
			NegotiatedClientServerConfiguration negotiatedClientServerConfiguration)
			throws ClientServerNegotiationException {
		super(pipelineConfiguration, negotiatedClientServerConfiguration);
	}

	/**
	 * Given the configuration, initialize the cipher
	 * 
	 * see rcPortalOper at about line 335 in rcPartialDataPut
	 */
	private void initCipher() throws ClientServerNegotiationException {
		PipelineConfiguration pipelineConfiguration = this
				.getPipelineConfiguration();
		try {
			log.info("initCipher()");
			this.setCipher(Cipher.getInstance(pipelineConfiguration
					.getEncryptionAlgorithmEnum().getCypherKey()));

			SecretKey secretKey = initSecretKey(pipelineConfiguration);

			this.getCipher().init(Cipher.ENCRYPT_MODE, secretKey);

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

	@Override
	EncryptionBuffer encrypt(byte[] input) {

		log.info("encrypt");
		if (input == null) {
			throw new IllegalArgumentException("null input");
		}

		try {

			log.debug("init cipher");
			initCipher();
			log.debug("init done");

			// get the initialization vector and store as member var
			byte[] mInitVec = this.getCipher().getIV();

			log.debug("encrypting");
			byte[] encrypted = getCipher().doFinal(input);
			return new EncryptionBuffer(mInitVec, encrypted);

		} catch (IllegalBlockSizeException | BadPaddingException

		| ClientServerNegotiationException e) {
			log.error("error during encryption", e);
			throw new JargonRuntimeException(
					"parameter spec error in encryption", e);
		}
	}
}
