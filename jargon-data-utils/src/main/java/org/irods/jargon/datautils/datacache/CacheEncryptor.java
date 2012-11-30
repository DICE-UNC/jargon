package org.irods.jargon.datautils.datacache;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;

class CacheEncryptor {
	Cipher ecipher;
	Cipher dcipher;

	// 8-byte Salt
	byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
			(byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03 };

	// Iteration count
	int iterationCount = 19;

	CacheEncryptor(final String passPhrase) {
		try {
			// Create the key
			KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt,
					iterationCount);
			SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
					.generateSecret(keySpec);
			ecipher = Cipher.getInstance(key.getAlgorithm());
			dcipher = Cipher.getInstance(key.getAlgorithm());
	
			// Prepare the parameter to the ciphers
			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt,
					iterationCount);
	
			// Create the ciphers
			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
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

	protected byte[] encrypt(final byte[] data) throws JargonException {
		try {
			// Encrypt
			byte[] enc = ecipher.doFinal(data);
			return enc;
		} catch (javax.crypto.BadPaddingException e) {
			throw new JargonException("BadPaddingException encrypting data", e);
		} catch (IllegalBlockSizeException e) {
			throw new JargonException(
					"IllegalBlockSizeException encrypting data", e);
		}
	}

	protected byte[] decrypt(final byte[] data) throws JargonException {
		try {
			// Decrypt
			byte[] decoded = dcipher.doFinal(data);
			return decoded;
		} catch (javax.crypto.BadPaddingException e) {
			throw new JargonException("BadPaddingException decrypting data", e);
		} catch (IllegalBlockSizeException e) {
			throw new JargonException(
					"IllegalBlockSizeException decrypting data", e);
		}

	}

}
