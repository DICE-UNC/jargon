/**
 * 
 */
package org.irods.jargon.core.transfer.encrypt;

import java.security.InvalidAlgorithmParameterException;
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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.ClientServerNegotiationException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps decryption of a byte buffer using AES
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
class AesCipherDecryptWrapper extends ParallelDecryptionCipherWrapper {

	public static final Logger log = LoggerFactory
			.getLogger(AesCipherDecryptWrapper.class);

	private SecretKey secretKey = null;

	/**
	 * Default constructor with configuration information needed to set up the
	 * algorithm
	 * 
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration} with connection properties
	 * @param negotiatedClientServerConfiguration
	 *            {@link NegotiatedClientServerConfiguration} with result of
	 *            negotiation
	 *
	 * @throws ClientServerNegotiationException
	 */
	AesCipherDecryptWrapper(
			PipelineConfiguration pipelineConfiguration,
			NegotiatedClientServerConfiguration negotiatedClientServerConfiguration)
			throws ClientServerNegotiationException {
		super(pipelineConfiguration, negotiatedClientServerConfiguration);
		this.setInitDone(true); // will init on each decrypt call with an iv
	}

	/**
	 * @param pipelineConfiguration
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	private SecretKey initSecretKey(
			PipelineConfiguration pipelineConfiguration, byte[] iv)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		if (secretKey == null) {
			SecretKeyFactory factory = SecretKeyFactory
					.getInstance(pipelineConfiguration
							.getEncryptionAlgorithmEnum().getKeyGenType());
			KeySpec keySpec = new PBEKeySpec(this
					.getNegotiatedClientServerConfiguration()
					.getSslCryptChars(), iv,
					pipelineConfiguration.getEncryptionNumberHashRounds(),
					pipelineConfiguration.getEncryptionAlgorithmEnum()
							.getKeySize());

			SecretKey temp = factory.generateSecret(keySpec);
			secretKey = new SecretKeySpec(temp.getEncoded(), "AES");
		}
		return secretKey;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.ParallelEncryptionCipherWrapper#decrypt
	 * (org.irods.jargon.core.transfer.EncryptionBuffer)
	 */
	@Override
	protected byte[] doDecrypt(EncryptionBuffer input) {
		try {
			this.initImplementation(input.getInitializationVector());
			byte[] original = getCipher().doFinal(input.getEncryptedData());
			return original;

		} catch (IllegalBlockSizeException | BadPaddingException e) {
			log.error("error during encryption", e);
			throw new JargonRuntimeException(
					"Unable to decrypt given negotiated settings", e);
		}
	}

	private void initImplementation(byte[] iv) {
		log.info("initing with new iv");
		PipelineConfiguration pipelineConfiguration = this
				.getPipelineConfiguration();
		try {
			log.info("initCipher()");
			Cipher encryptionCipher = Cipher.getInstance(pipelineConfiguration
					.getEncryptionAlgorithmEnum().getCypherKey());
			this.setCipher(encryptionCipher);
			log.debug("have cipher:{}", encryptionCipher);

			SecretKey myKey = initSecretKey(pipelineConfiguration, iv);

			encryptionCipher.init(Cipher.DECRYPT_MODE, myKey,
					new IvParameterSpec(iv));

		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeySpecException | InvalidKeyException
				| InvalidAlgorithmParameterException e) {
			log.error("error generating key for cipher", e);
			throw new JargonRuntimeException("cannot generate key for cipher",
					e);
		}

	}
}
