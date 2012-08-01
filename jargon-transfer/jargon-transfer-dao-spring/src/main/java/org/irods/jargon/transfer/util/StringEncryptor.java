/**
 * 
 */
package org.irods.jargon.transfer.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;

import org.irods.jargon.core.utils.Base64;

import sun.misc.BASE64Decoder;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@SuppressWarnings("restriction")
public class StringEncryptor {

	public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";

	public static final String DES_ENCRYPTION_SCHEME = "DES";

	// public static final String DES_PADDED_ENCRYPTION_SCHEME =
	// "DES/CFB/PKCS5Padding";
	public static final String DES_PADDED_ENCRYPTION_SCHEME = "DES/CBC/PKCS5Padding";

	public static final String DEFAULT_ENCRYPTION_KEY = "jfiadsfijaisejflaisdfjjieiefjakdlfjasdlkfjasliejfasfjaiseajfas;irijgirgaisjfa;sidfja;seijfgas;ihgar;iafjas;df";

	private KeySpec keySpec;

	private SecretKeyFactory keyFactory;

	private Cipher cipher;

	private static final String UNICODE_FORMAT = "UTF8";

	public StringEncryptor(final String encryptionScheme)
			throws EncryptionException {
		this(encryptionScheme, DEFAULT_ENCRYPTION_KEY);
	}

	public StringEncryptor(final String encryptionScheme,
			final String encryptionKey) throws EncryptionException {

		if (encryptionKey == null) {
			throw new IllegalArgumentException("encryption key was null");
		}
		if (encryptionKey.trim().length() < 24) {
			throw new IllegalArgumentException(
					"encryption key was less than 24 characters");
		}

		try {
			byte[] keyAsBytes = encryptionKey.getBytes(UNICODE_FORMAT);

			if (encryptionScheme.equals(DESEDE_ENCRYPTION_SCHEME)) {
				keySpec = new DESedeKeySpec(keyAsBytes);
			} else if (encryptionScheme.equals(DES_ENCRYPTION_SCHEME)) {
				keySpec = new DESKeySpec(keyAsBytes);
			} else {
				throw new IllegalArgumentException(
						"Encryption scheme not supported: " + encryptionScheme);
			}

			keyFactory = SecretKeyFactory.getInstance(encryptionScheme);
			cipher = Cipher.getInstance(encryptionScheme);

		} catch (InvalidKeyException e) {
			throw new EncryptionException(e);
		} catch (UnsupportedEncodingException e) {
			throw new EncryptionException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new EncryptionException(e);
		} catch (NoSuchPaddingException e) {
			throw new EncryptionException(e);
		}

	}

	public String encrypt(final String unencryptedString)
			throws EncryptionException {
		if (unencryptedString == null) {
			throw new IllegalArgumentException("unencrypted string was null ");
		}

		// if blank return blank
		if (unencryptedString.isEmpty()) {
			return "";
		}

		try {
			SecretKey key = keyFactory.generateSecret(keySpec);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] cleartext = unencryptedString.getBytes(UNICODE_FORMAT);
			byte[] ciphertext = cipher.doFinal(cleartext);
			/*
			 * Go from an unencrypted byte array to an encrypted byte array
			 */
			return Base64.toString(ciphertext);
		} catch (Exception e) {
			throw new EncryptionException(e);
		}
	}

	public String decrypt(final String encryptedString)
			throws EncryptionException {
		if (encryptedString == null) {
			throw new IllegalArgumentException("encrypted string was null ");
		}

		// if blank just return blank
		if (encryptedString.isEmpty()) {
			return "";
		}

		try {
			SecretKey key = keyFactory.generateSecret(keySpec);
			cipher.init(Cipher.DECRYPT_MODE, key);
			BASE64Decoder base64decoder = new BASE64Decoder();
			byte[] cleartext = base64decoder.decodeBuffer(encryptedString);
			byte[] ciphertext = cipher.doFinal(cleartext);

			return bytes2String(ciphertext);
		} catch (Exception e) {
			throw new EncryptionException(e);
		}
	}

	private static String bytes2String(final byte[] bytes) {
		StringBuffer stringBuffer = new StringBuffer();
		for (byte b : bytes) {
			stringBuffer.append((char) b);
		}
		return stringBuffer.toString();
	}

	public static class EncryptionException extends Exception {

		private static final long serialVersionUID = 3877601749214910931L;

		public EncryptionException(final Throwable t) {
			super(t);
		}
	}
}
