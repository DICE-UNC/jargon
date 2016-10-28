/**
 *
 */
package org.irods.jargon.core.transfer.encrypt;

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
import org.irods.jargon.core.exception.EncryptionException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps encryption of a byte buffer using AES
 *
 * see
 * 
 * http://stackoverflow.com/questions/28622438/aes-256-password-based-
 * encryption-decryption-in-java
 * 
 * @author Mike Conway - DICE
 * 
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
			final PipelineConfiguration pipelineConfiguration,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration)
			throws ClientServerNegotiationException {
		super(pipelineConfiguration, negotiatedClientServerConfiguration);
	}

	/**
	 * Given the configuration, initialize the cipher
	 *
	 * see rcPortalOper at about line 335 in rcPartialDataPut
	 */
	@Override
	protected void initImplementation() {
		PipelineConfiguration pipelineConfiguration = getPipelineConfiguration();
		try {
			log.info("initCipher()");
			setCipher(Cipher.getInstance(pipelineConfiguration
					.getEncryptionAlgorithmEnum().getCypherKey()));

			SecretKeySpec secretKey = initSecretKey(pipelineConfiguration);

			getCipher().init(Cipher.ENCRYPT_MODE, secretKey);

		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeySpecException | InvalidKeyException e) {
			log.error("error generating key for cipher", e);
			throw new JargonRuntimeException("cannot generate key for cipher",
					e);
		}

	}

	/**
	 * @param pipelineConfiguration
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	private SecretKeySpec initSecretKey(
			final PipelineConfiguration pipelineConfiguration)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory
				.getInstance(pipelineConfiguration.getEncryptionAlgorithmEnum()
						.getKeyGenType());
		KeySpec keySpec = new PBEKeySpec(
				getNegotiatedClientServerConfiguration().getSslCryptChars(),
				RandomUtils.generateRandomBytesOfLength(pipelineConfiguration
						.getEncryptionSaltSize()),
				pipelineConfiguration.getEncryptionNumberHashRounds(),
				pipelineConfiguration.getEncryptionAlgorithmEnum().getKeySize());

		SecretKey secretKey = factory.generateSecret(keySpec);
		SecretKeySpec secretSpec = new SecretKeySpec(secretKey.getEncoded(),
				"AES");
		return secretSpec;
	}

	@Override
	protected EncryptionBuffer doEncrypt(final byte[] input)
			throws EncryptionException {

		log.info("encrypt");
		if (input == null) {
			throw new IllegalArgumentException("null input");
		}

		try {

			AlgorithmParameters params = getCipher().getParameters();
			byte[] mInitVec = params.getParameterSpec(IvParameterSpec.class)
					.getIV();

			// get the initialization vector and store as member var
			// byte[] mInitVec = getCipher().getIV();

			log.debug("encrypting");
			byte[] encrypted;

			encrypted = getCipher().doFinal(input);
			return new EncryptionBuffer(mInitVec, encrypted);

		} catch (IllegalBlockSizeException | BadPaddingException
				| InvalidParameterSpecException e) {
			log.error("encryption exception", e);
			throw new EncryptionException("encryption exception", e);
		}

	}
}
