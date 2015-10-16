/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

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
class AesCipherWrapper extends ParallelEncryptionCipherWrapper {

	public static final Logger log = LoggerFactory
			.getLogger(AesCipherWrapper.class);

	private KeyGenerator keyGen = null;

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
		try {
			Cipher encryptionCypher = Cipher.getInstance(this
					.getPipelineConfiguration().getEncryptionAlgorithmEnum()
					.getCypherKey());
			this.setCipher(encryptionCypher);
			keyGen = KeyGenerator.getInstance(this.getPipelineConfiguration()
					.getEncryptionAlgorithmEnum().getKeyGenType());

		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			log.error("error generating key for cipher", e);
			throw new ClientServerNegotiationException(
					"cannot generate key for cipher", e);
		}
		keyGen.init(this.getPipelineConfiguration().getEncryptionKeySize());
		keyGen.generateKey();

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
			byte[] ivBytes = params.getParameterSpec(IvParameterSpec.class)
					.getIV();
			return getCipher().doFinal(input);
		} catch (InvalidParameterSpecException | IllegalBlockSizeException
				| BadPaddingException e) {
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

}
