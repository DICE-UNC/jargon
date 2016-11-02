/**
 * 
 */
package org.irods.jargon.core.transfer.encrypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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

	public static final Logger log = LoggerFactory
			.getLogger(AesCipherDecryptWrapper.class);

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

			Cipher cipher = Cipher.getInstance(this.getPipelineConfiguration()
					.getEncryptionAlgorithmEnum().getCypherKey());
			cipher.init(Cipher.DECRYPT_MODE, this
					.getNegotiatedClientServerConfiguration().getSecretKey(),
					new IvParameterSpec(input.getInitializationVector()));
			this.setCipher(cipher);

			byte[] original = getCipher().doFinal(input.getEncryptedData());
			return original;

		} catch (IllegalBlockSizeException | BadPaddingException
				| InvalidKeyException | InvalidAlgorithmParameterException
				| NoSuchAlgorithmException | NoSuchPaddingException e) {
			log.error("error during encryption", e);
			throw new JargonRuntimeException(
					"Unable to decrypt given negotiated settings", e);
		}
	}
}
