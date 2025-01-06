package org.irods.jargon.datautils.datacache;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CacheEncryptor {

	private static final Logger log = LogManager.getLogger(CacheEncryptor.class);

	Cipher ecipher;
	Cipher dcipher;

	// 8-byte Salt
	byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35, (byte) 0xE3,
			(byte) 0x03 };

	// Iteration count
	int iterationCount = 19;

	public CacheEncryptor(final String passPhrase) {
		log.info("CacheEncryptor()");
		try {
			log.info("create key");
			// Create the key
			KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
			SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
			ecipher = Cipher.getInstance(key.getAlgorithm());
			dcipher = Cipher.getInstance(key.getAlgorithm());

			log.info("prepare the param to the ciphers");
			// Prepare the parameter to the ciphers
			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

			log.info("create cyphers");

			// Create the ciphers
			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
			log.info("cyphers created");
		} catch (java.security.InvalidAlgorithmParameterException e) {
			throw new JargonRuntimeException("error creating cacheEncryptor");

		} catch (java.security.spec.InvalidKeySpecException e) {
			throw new JargonRuntimeException("error creating cacheEncryptor");

		} catch (javax.crypto.NoSuchPaddingException e) {
			throw new JargonRuntimeException("error creating cacheEncryptor");

		} catch (java.security.NoSuchAlgorithmException e) {
			throw new JargonRuntimeException("error creating cacheEncryptor");

		} catch (java.security.InvalidKeyException e) {
			throw new JargonRuntimeException("error creating cacheEncryptor");

		}
	}

	public byte[] encrypt(final byte[] data) throws JargonException {
		try {
			// Encrypt
			byte[] enc = ecipher.doFinal(data);
			return enc;
		} catch (javax.crypto.BadPaddingException e) {
			throw new JargonException("BadPaddingException encrypting data", e);
		} catch (IllegalBlockSizeException e) {
			throw new JargonException("IllegalBlockSizeException encrypting data", e);
		}
	}

	public byte[] decrypt(final byte[] data) throws JargonException {
		try {
			// Decrypt
			byte[] decoded = dcipher.doFinal(data);
			return decoded;
		} catch (javax.crypto.BadPaddingException e) {
			throw new JargonException("BadPaddingException decrypting data", e);
		} catch (IllegalBlockSizeException e) {
			throw new JargonException("IllegalBlockSizeException decrypting data", e);
		}

	}

	/**
	 *
	 * Takes a single String as an argument and returns an Encrypted version of that
	 * String.
	 *
	 * @param str
	 *            {@code String} String to be encrypted
	 * @return {@code String} Encrypted version of the provided String
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public String encrypt(final String str) throws JargonException {
		try {
			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");

			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);

			// Encode bytes to base64 to get a string
			return Base64.getEncoder().encodeToString(enc);

		} catch (Exception e) {
			throw new JargonException(e);
		}

	}

	/**
	 * Takes a encrypted String as an argument, decrypts and returns the decrypted
	 * String.
	 *
	 * @param str
	 *            Encrypted String to be decrypted
	 * @return {@code String} Decrypted version of the provided String
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public String decrypt(final String str) throws JargonException {

		try {

			// Decode base64 to get bytes
			byte[] dec = Base64.getDecoder().decode(str);

			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);

			// Decode using utf-8
			return new String(utf8, "UTF8");

		} catch (Exception e) {
			throw new JargonException(e);
		}
	}

}
