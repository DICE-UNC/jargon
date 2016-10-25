/**
 * 
 */
package org.irods.jargon.core.transfer.encrypt;

import javax.crypto.Cipher;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.EncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for a cipher that will encrypt parallel transfer data
 * 
 * see http://www.digizol.com/2009/10/java-encrypt-decrypt-jce-salt.html
 * 
 * @author Mike Conway - DICE
 *
 */
public abstract class ParallelEncryptionCipherWrapper extends
		ParallelCipherWrapper {

	public static final Logger log = LoggerFactory
			.getLogger(ParallelEncryptionCipherWrapper.class);

	ParallelEncryptionCipherWrapper(
			PipelineConfiguration pipelineConfiguration,
			NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
		super(pipelineConfiguration, negotiatedClientServerConfiguration,
				Cipher.ENCRYPT_MODE);
	}

	/**
	 * Client method that will invoke the implementation-specific encrypt()
	 * method after checking for proper initialization <code>byte[]</code> of
	 * plaintext data
	 * 
	 * @return {@link EncryptionBuffer}
	 * @throws EncryptionException
	 */
	public EncryptionBuffer encrypt(byte[] input) throws EncryptionException {
		log.info("encrypt()");
		if (!isInitDone()) {
			log.error("encrypt was called before init() was called, must init the wrapper");
			throw new EncryptionException(
					"cannot call encrypt when init was not done");
		}
		return doEncrypt(input);
	}

	/**
	 * Encrypt the given data
	 * 
	 * @param input
	 *            <code>byte[]</code> of plaintext data
	 * @return {@link EncryptionBuffer}
	 * @throws EncryptionException
	 */
	protected abstract EncryptionBuffer doEncrypt(byte[] input)
			throws EncryptionException;

	/**
	 * Initialize the cipher for use,call before calling encryption.
	 */
	public void init() {
		log.info("init()");
		log.info("calling initImplementation() for the specific wrapper...");
		initImplementation();
		setInitDone(true);

	}

	/**
	 * Specific implementation code that is required to be called before doing
	 * any encryption. This fully initializes the given cipher, and may be done
	 * one time for multiple calls to encrypt
	 */
	protected abstract void initImplementation();

}
