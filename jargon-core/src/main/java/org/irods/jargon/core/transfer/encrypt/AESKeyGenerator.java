/**
 *
 */
package org.irods.jargon.core.transfer.encrypt;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.irods.jargon.core.connection.NegotiatedClientServerConfiguration;
import org.irods.jargon.core.connection.PipelineConfiguration;
import org.irods.jargon.core.exception.EncryptionException;
import org.irods.jargon.core.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate an AES key based on pipeline config, assumes using salt and other
 * settings
 *
 * @author Mike Conway -DFC
 *
 */
public class AESKeyGenerator extends AbstractKeyGenerator {

	public static final Logger log = LoggerFactory.getLogger(AESKeyGenerator.class);

	/**
	 * @param pipelineConfiguration
	 *            {@link PipelineConfiguration}
	 * @param negotiatedClientServerConfiguration
	 *            {@link NegotiatedClientServerConfiguration}
	 */
	public AESKeyGenerator(final PipelineConfiguration pipelineConfiguration,
			final NegotiatedClientServerConfiguration negotiatedClientServerConfiguration) {
		super(pipelineConfiguration, negotiatedClientServerConfiguration);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.transfer.encrypt.AbstractKeyGenerator#generateKey()
	 */
	@Override
	public SecretKey generateKey() throws EncryptionException {
		log.info("generateKey()");
		return initSecretKey();

	}

	private SecretKeySpec initSecretKey() throws EncryptionException {
		try {
			SecretKeyFactory factory = SecretKeyFactory
					.getInstance(getPipelineConfiguration().getEncryptionAlgorithmEnum().getKeyGenType());
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(256); // 192 and 256 bits may not be available
			char[] randPwd = new String(kgen.generateKey().getEncoded()).toCharArray();

			// Generate the secret key specs.
			KeySpec keySpec = new PBEKeySpec(randPwd,
					RandomUtils.generateRandomBytesOfLength(getPipelineConfiguration().getEncryptionSaltSize()),
					getPipelineConfiguration().getEncryptionNumberHashRounds(),
					getPipelineConfiguration().getEncryptionAlgorithmEnum().getKeySize());

			SecretKey secretKey = factory.generateSecret(keySpec);
			SecretKeySpec secretSpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
			return secretSpec;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			log.error("error creating secret key", e);
			throw new EncryptionException(e);
		}
	}

}
