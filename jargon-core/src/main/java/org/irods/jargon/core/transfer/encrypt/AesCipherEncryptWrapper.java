/**
 *
 */
package org.irods.jargon.core.transfer.encrypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.irods.jargon.core.exception.EncryptionException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	public static final Logger log = LogManager.getLogger(AesCipherEncryptWrapper.class);
	public static byte[] ivPad = new byte[16];

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
	AesCipherEncryptWrapper(final PipelineConfiguration pipelineConfiguration,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration)
			throws ClientServerNegotiationException {
		super(pipelineConfiguration, negotiatedClientServerConfiguration);
		initImplementation();
	}

	/**
	 * Given the configuration, initialize the cipher
	 *
	 * see rcPortalOper at about line 335 in rcPartialDataPut
	 */

	private void initImplementation() {
		PipelineConfiguration pipelineConfiguration = getPipelineConfiguration();
		try {
			log.info("initCipher()");
			setCipher(Cipher.getInstance(pipelineConfiguration.getEncryptionAlgorithmEnum().getCypherKey()));

			SecretKey secretKey = getNegotiatedClientServerConfiguration().getSecretKey();
			getCipher().init(Cipher.ENCRYPT_MODE, secretKey);

		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			log.error("error generating key for cipher", e);
			throw new JargonRuntimeException("cannot generate key for cipher", e);
		}

	}

	@Override
	protected EncryptionBuffer doEncrypt(final byte[] input) throws EncryptionException {

		log.info("encrypt");
		if (input == null) {
			throw new IllegalArgumentException("null input");
		}

		try {

			AlgorithmParameters params = getCipher().getParameters();
			byte[] mInitVec = params.getParameterSpec(IvParameterSpec.class).getIV();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(mInitVec);
			// TODO: add version checking
			bos.write(ivPad);

			// get the initialization vector and store as member var
			// byte[] mInitVec = getCipher().getIV();

			log.debug("encrypting");
			byte[] encrypted;

			encrypted = getCipher().doFinal(input);
			log.debug("encrypted length:{}", encrypted.length);
			return new EncryptionBuffer(bos.toByteArray(), encrypted);

		} catch (IllegalBlockSizeException | BadPaddingException | InvalidParameterSpecException | IOException e) {
			log.error("encryption exception", e);
			throw new EncryptionException("encryption exception", e);
		}

	}
}
