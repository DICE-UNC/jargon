/**
 *
 */
package org.irods.jargon.core.transfer.encrypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

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

	public static final Logger log = LoggerFactory.getLogger(AesCipherDecryptWrapper.class);

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
	AesCipherDecryptWrapper(final PipelineConfiguration pipelineConfiguration,
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
		try {
			log.info("initCipher()");
			setCipher(Cipher.getInstance(getPipelineConfiguration().getEncryptionAlgorithmEnum().getCypherKey()));

		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			log.error("error initing for cipher", e);
			throw new JargonRuntimeException("cannot init for cipher", e);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.transfer.ParallelEncryptionCipherWrapper#decrypt
	 * (org.irods.jargon.core.transfer.EncryptionBuffer)
	 */
	@Override
	protected byte[] doDecrypt(final EncryptionBuffer input) {
		try {
			getCipher().init(Cipher.DECRYPT_MODE, getNegotiatedClientServerConfiguration().getSecretKey(),
					new IvParameterSpec(input.getInitializationVector()));

			byte[] original = getCipher().doFinal(input.getEncryptedData());
			return original;

		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException
				| InvalidAlgorithmParameterException e) {
			log.error("error during encryption", e);
			throw new JargonRuntimeException("Unable to decrypt given negotiated settings", e);
		}
	}

	@Override
	protected byte[] doDecrypt(final byte[] fullBuffer) {
		log.info("doDecrypt()");
		// need to split out iv and buffer data, note that there is currently 16
		// bytes of unused data in the IV from iRODS

		if (fullBuffer.length < 32) {
			log.error("unusable data in buffer, less than 32 bytes");
			throw new JargonRuntimeException("unusable data in data buffer");
		}

		if (fullBuffer.length == 32) {
			log.warn("no data in buffer to decrypt, return empty buffer");
			return new byte[0];
		}

		log.debug("fullbuffer length:{}", fullBuffer.length);
		log.debug("buffer - iv length:{}", fullBuffer.length - 32);

		EncryptionBuffer encryptionBuffer = new EncryptionBuffer(Arrays.copyOfRange(fullBuffer, 0, 16),
				extractEncryptedData(fullBuffer));
		log.debug("length of encrypted buffer:{}", encryptionBuffer.getEncryptedData().length);
		return doDecrypt(encryptionBuffer);

	}

	private byte[] extractEncryptedData(final byte[] fullBuffer) {
		byte[] returned = new byte[fullBuffer.length - 32];
		System.arraycopy(fullBuffer, 32, returned, 0, fullBuffer.length - 32);
		return returned;
	}
}
