package org.irods.jargon.core.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.Base64;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for dealing with iRODS password (obfuscation, etc).
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IRODSPasswordUtilities {

	public static final int WHEEL_LENGTH;
	public static final int VAL_0 = '0';
	public static final int VAL_A = 'A';
	public static final int VAL_LC_A = 'a';
	public static final int VAL_BANG = '!';
	public static final List<Integer> WHEEL;
	public static final String RAND_STRING = "1gCBizHWbwIYyWLoysGzTe6SyzqFKMniZX05faZHWAwQKXf6Fs";
	public static final String RAND_STRING_V2 = "A.ObfV2";
	public static final int MAX_PWD_LENGTH = 50;

	static final Logger log = LoggerFactory.getLogger(IRODSPasswordUtilities.class);

	static {

		WHEEL = new ArrayList<Integer>();

		for (int i = 0; i < 10; i++) {
			WHEEL.add(VAL_0 + i);
		}

		for (int i = 0; i < 26; i++) {
			WHEEL.add(VAL_A + i);
		}

		for (int i = 0; i < 26; i++) {
			WHEEL.add(VAL_LC_A + i);
		}

		for (int i = 0; i < 15; i++) {
			WHEEL.add(VAL_BANG + i);
		}

		WHEEL_LENGTH = WHEEL.size();

	}

	/**
	 * Obfuscate the irods password for a password change via an admin. This is
	 * analogous to the obfEncodeByKeyV2 method in lib/core/src/obf.c
	 *
	 * @param newPassword    {@code String} with the password value to set
	 * @param adminPassword  {@code String} with the password of the administrator
	 * @param challengeValue {@code String} with the challenge value used during the
	 *                       login process for the administrator making this change
	 * @return {@code String} with an obfuscated value for the password
	 * @throws JargonException for iRODS error
	 */
	public static String obfuscateIRODSPasswordForAdminPasswordChange(final String newPassword,
			final String adminPassword, final String challengeValue) throws JargonException {

		log.info("obfuscateIRODSPasswordForAdminPasswordChange()");

		if (newPassword == null || newPassword.isEmpty()) {
			throw new IllegalArgumentException("null or empty newPassword");
		}

		if (adminPassword == null || adminPassword.isEmpty()) {
			throw new IllegalArgumentException("null or empty adminPassword");
		}

		if (challengeValue == null || challengeValue.isEmpty()) {
			throw new IllegalArgumentException("null or empty challengeValue");
		}

		int secs = Calendar.getInstance().get(Calendar.MILLISECOND);
		secs = secs & 0x1f;

		StringBuilder myIn = new StringBuilder();
		myIn.append(RAND_STRING_V2);
		myIn.append(newPassword);

		StringBuilder myKey = new StringBuilder();
		myKey.append(adminPassword);
		myKey.append(challengeValue);

		String myKey2 = MiscIRODSUtils.computeMD5HashOfAStringValue(pad(myKey.toString(), 100, '\0'));

		// get a rand val based on current time
		int firstCharOfMyIn = myIn.charAt(0);
		firstCharOfMyIn += secs;
		myIn.setCharAt(0, (char) firstCharOfMyIn);

		String obfuscatedValue = obfuscateIRODSPasswordWithCypherChaining(myIn.toString(), myKey2);
		return obfuscatedValue;

	}

	/**
	 * iRODS (see clientLogin.c) uses a subset of the challenge value in hex form
	 * for its obfuscation purposes. This method takes the raw challenge value and
	 * creates the same representation.
	 *
	 * @param challengeValue {@code String} with the raw iRODS challenge value from
	 *                       the login process
	 * @return {@code String} with a hex represnetation of a subset of the
	 *         challenge, as in clientLogin.c
	 */
	public static String deriveHexSubsetOfChallenge(final String challengeValue) {
		if (challengeValue == null || challengeValue.isEmpty()) {
			throw new IllegalArgumentException("challengeValue is null or empty");
		}

		byte[] temp = Base64.fromString(challengeValue);
		int subsetLength = 16;

		if (temp.length < subsetLength) {
			throw new IllegalArgumentException("challengeValue length is < 16");
		}

		byte[] subsetBytes = Arrays.copyOfRange(temp, 0, 16);

		return getHexString(subsetBytes);

	}

	public static String obfEncodeByKeyUsingPassword(final String sourceData, final String key, final boolean pad)
			throws JargonException {

		if (sourceData == null || sourceData.isEmpty()) {
			throw new JargonException("newPassword is null or empty");
		}

		if (key == null || key.isEmpty()) {
			throw new JargonException("oldPassword is null or empty");
		}

		/**
		 * If the password is already padded, this has no effect, so it's left in
		 * without adding another signature or flag. If I am doing an admin change the
		 * padding has already happened.
		 */
		String randPaddedNewPassword;
		if (pad) {
			randPaddedNewPassword = padPasswordWithRandomStringData(sourceData);
		} else {
			randPaddedNewPassword = sourceData;
		}

		return obfuscateIRODSPasswordWithCypherChainingSHA1(randPaddedNewPassword, key);

	}

	/**
	 * Obfuscate a given value using a key, suitable to change a user password.
	 *
	 * @param sourceData {@code String} with the desired value to encrypt
	 * @param key        {@code String} with the encryption key
	 * @param pad        {@code boolean} indicating whether value source value
	 *                   should be padded
	 * @return {@code String} with the obfuscated password to send to iRODS via the
	 *         iRODS admin protocol.
	 * @throws JargonException for iRODS error
	 */
	public static String obfEncodeByKey(final String sourceData, final String key, final boolean pad)
			throws JargonException {

		if (sourceData == null || sourceData.isEmpty()) {
			throw new JargonException("newPassword is null or empty");
		}

		if (key == null || key.isEmpty()) {
			throw new JargonException("oldPassword is null or empty");
		}

		/**
		 * If the password is already padded, this has no effect, so it's left in
		 * without adding another signature or flag. If I am doing an admin change the
		 * padding has already happened.
		 */
		String randPaddedNewPassword;
		if (pad) {
			randPaddedNewPassword = padPasswordWithRandomStringData(sourceData);
		} else {
			randPaddedNewPassword = sourceData;
		}

		// FIXME: add switch for md5 vs sha1

		return obfuscateIRODSPasswordWithCypherChainingMD5(randPaddedNewPassword, key);

	}

	public static String obfuscateIRODSPasswordWithCypherChaining(final String newPassword, final String oldPassword)
			throws JargonException {

		return obfuscateIRODSPasswordWithCypherChainingSHA1(newPassword, oldPassword);

	}

	private static String obfuscateIRODSPasswordWithCypherChainingMD5(final String newPassword,
			final String encryptionKey) throws JargonException {

		if (newPassword == null || newPassword.isEmpty()) {
			throw new JargonException("newPassword is null or empty");
		}

		if (encryptionKey == null || encryptionKey.isEmpty()) {
			throw new JargonException("encryptionKey is null or empty");
		}

		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new JargonException("error getting MD5 MessageDigest", e);
		}

		byte[] oldPwdBuffer = new byte[100];

		// get MD5 hash of the old password
		byte[] oldPwdAsBytes = encryptionKey.getBytes();
		for (int i = 0; i < encryptionKey.length(); i++) {
			oldPwdBuffer[i] = oldPwdAsBytes[i];
		}

		messageDigest.update(oldPwdBuffer);
		byte[] digestRound1 = messageDigest.digest();

		if (log.isDebugEnabled()) {
			log.debug("digestRound1:{}", getHexString(digestRound1));
		}

		// get the MD5 hash of the first hash
		messageDigest.reset();
		messageDigest.update(digestRound1);
		byte[] digestRound2 = messageDigest.digest(); // digest of the digest of the key, this is concatenated together

		if (log.isDebugEnabled()) {
			log.debug("digestRound2:{}", getHexString(digestRound2));
		}

		// concatenate the first two hashes, and take the hash of that
		byte[] concatRound1AndRound2 = new byte[digestRound1.length + digestRound2.length];
		int concatI = 0;

		for (byte element : digestRound1) {
			concatRound1AndRound2[concatI++] = element;
		}

		for (byte element : digestRound2) {
			concatRound1AndRound2[concatI++] = element;
		}

		messageDigest.reset();
		messageDigest.update(concatRound1AndRound2);
		byte[] digestRound3 = messageDigest.digest(); // hash of key hash and hash of key hash
		if (log.isDebugEnabled()) {
			log.debug("digestRound3:{}", getHexString(digestRound3));
		}

		// concatenate the previous 3 rounds and take a fourth MD5 hash

		byte[] concatRound1AndRound2AndRound3 = new byte[digestRound1.length + digestRound2.length
				+ digestRound3.length];
		concatI = 0;

		for (byte element : digestRound1) {
			concatRound1AndRound2AndRound3[concatI++] = element;
		}

		for (byte element : digestRound2) {
			concatRound1AndRound2AndRound3[concatI++] = element;
		}

		for (byte element : digestRound3) {
			concatRound1AndRound2AndRound3[concatI++] = element;
		}

		messageDigest.reset();
		messageDigest.update(concatRound1AndRound2AndRound3);
		byte[] digestRound4 = messageDigest.digest();
		if (log.isDebugEnabled()) {
			log.debug("digestRound4:{}", getHexString(digestRound4));
		}

		// concatenate all four hash buffers
		byte[] cpKeyArray = new byte[digestRound1.length + digestRound2.length + digestRound3.length
				+ digestRound4.length];

		concatI = 0;

		for (byte element : digestRound1) {
			cpKeyArray[concatI++] = element;
		}

		for (byte element : digestRound2) {
			cpKeyArray[concatI++] = element;
		}

		for (byte element : digestRound3) {
			cpKeyArray[concatI++] = element;
		}

		for (byte element : digestRound4) {
			cpKeyArray[concatI++] = element;
		}

		if (log.isDebugEnabled()) {
			log.debug("cpKeyArray:{}", getHexString(cpKeyArray));
		}

		// match the PHP code for obfuscation as closely as possible
		int cpInIdx = 0;
		int cpKeyArrayIdx = 0;
		int cpInVal;
		// in order to mimic the PHP code...
		String cpInArray = newPassword;
		int k = 0;
		boolean found = false;
		int wheelVal;
		char wheelChr;
		int prevChar = 0;
		StringBuilder cpOut = new StringBuilder();

		for (; cpInIdx < cpInArray.length(); cpInIdx++) {

			k = 0;
			found = false;
			byte b = cpKeyArray[cpKeyArrayIdx++];
			k = unsignedByteToInt(b);
			if (cpKeyArrayIdx > 60) {
				cpKeyArrayIdx = 0;
			}

			for (int i = 0; i < WHEEL_LENGTH; i++) {

				cpInVal = unsignedByteToInt(cpInArray.getBytes()[cpInIdx]);
				wheelVal = WHEEL.get(i);
				if (cpInVal == wheelVal) {
					int j = i + k + prevChar;
					j = j % WHEEL_LENGTH;
					wheelVal = WHEEL.get(j);
					wheelChr = (char) wheelVal;
					cpOut.append(wheelChr);
					// prevChar = wheelChr & 0xff;
					found = true;
					break;
				}
			}

			if (!found) {
				if (cpInIdx == cpInArray.length()) {
					break;
				} else {
					cpOut.append(cpInArray.charAt(cpInIdx));
				}
			}

		}

		return cpOut.toString();

	}

	private static String obfuscateIRODSPasswordWithCypherChainingSHA1(final String newPassword,
			final String oldPassword) throws JargonException {

		if (newPassword == null || newPassword.isEmpty()) {
			throw new JargonException("newPassword is null or empty");
		}

		if (oldPassword == null || oldPassword.isEmpty()) {
			throw new JargonException("oldPassword is null or empty");
		}

		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new JargonException("error getting SHA1 MessageDigest", e);
		}

		byte[] oldPwdBuffer = new byte[100];

		// get SHA1 hash of the old password
		byte[] oldPwdAsBytes = oldPassword.getBytes();
		for (int i = 0; i < oldPassword.length(); i++) {
			oldPwdBuffer[i] = oldPwdAsBytes[i];
		}

		messageDigest.update(oldPwdBuffer);
		byte[] temp = messageDigest.digest();
		byte[] digestRound1 = new byte[16];
		System.arraycopy(temp, 0, digestRound1, 0, 16);

		if (log.isDebugEnabled()) {
			log.debug("digestRound1:{}", getHexString(digestRound1));
		}

		// get the hash of the first hash
		messageDigest.reset();
		messageDigest.update(digestRound1);
		temp = messageDigest.digest();
		byte[] digestRound2 = new byte[16];
		System.arraycopy(temp, 0, digestRound2, 0, 16);

		if (log.isDebugEnabled()) {
			log.debug("digestRound2:{}", getHexString(digestRound2));
		}

		// concatenate the first two hashes, and take the hash of that
		byte[] concatRound1AndRound2 = new byte[digestRound1.length + digestRound2.length];
		int concatI = 0;

		for (byte element : digestRound1) {
			concatRound1AndRound2[concatI++] = element;
		}

		for (byte element : digestRound2) {
			concatRound1AndRound2[concatI++] = element;
		}

		messageDigest.reset();
		messageDigest.update(concatRound1AndRound2);

		temp = messageDigest.digest();
		byte[] digestRound3 = new byte[16];
		System.arraycopy(temp, 0, digestRound3, 0, 16);
		if (log.isDebugEnabled()) {
			log.debug("digestRound3:{}", getHexString(digestRound3));
		}

		// concatenate the previous 3 rounds and take a fourth MD5 hash

		byte[] concatRound1AndRound2AndRound3 = new byte[digestRound1.length + digestRound2.length
				+ digestRound3.length];
		concatI = 0;

		for (byte element : digestRound1) {
			concatRound1AndRound2AndRound3[concatI++] = element;
		}

		for (byte element : digestRound2) {
			concatRound1AndRound2AndRound3[concatI++] = element;
		}

		for (byte element : digestRound3) {
			concatRound1AndRound2AndRound3[concatI++] = element;
		}

		messageDigest.reset();
		messageDigest.update(concatRound1AndRound2);
		temp = messageDigest.digest();
		byte[] digestRound4 = new byte[16];
		System.arraycopy(temp, 0, digestRound4, 0, 16);

		// byte[] digestRound4 = messageDigest.digest();
		if (log.isDebugEnabled()) {
			log.debug("digestRound4:{}", getHexString(digestRound4));
		}

		// concatenate all four hash buffers
		byte[] cpKeyArray = new byte[digestRound1.length + digestRound2.length + digestRound3.length
				+ digestRound4.length];

		concatI = 0;

		for (byte element : digestRound1) {
			cpKeyArray[concatI++] = element;
		}

		for (byte element : digestRound2) {
			cpKeyArray[concatI++] = element;
		}

		for (byte element : digestRound3) {
			cpKeyArray[concatI++] = element;
		}

		for (byte element : digestRound4) {
			cpKeyArray[concatI++] = element;
		}

		if (log.isDebugEnabled()) {
			log.debug("cpKeyArray:{}", getHexString(cpKeyArray));
		}

		// match the PHP code for obfuscation as closely as possible
		int cpInIdx = 0;
		int cpKeyArrayIdx = 0;
		int cpInVal;
		// in order to mimic the PHP code...
		String cpInArray = newPassword;
		int k = 0;
		boolean found = false;
		int wheelVal;
		char wheelChr;
		int prevChar = 0;
		StringBuilder cpOut = new StringBuilder();
		cpOut.append("sha1");

		for (; cpInIdx < cpInArray.length(); cpInIdx++) {

			k = 0;
			found = false;
			byte b = cpKeyArray[cpKeyArrayIdx++];
			k = unsignedByteToInt(b);
			if (cpKeyArrayIdx > 60) {
				cpKeyArrayIdx = 0;
			}

			for (int i = 0; i < WHEEL_LENGTH; i++) {

				cpInVal = unsignedByteToInt(cpInArray.getBytes()[cpInIdx]);
				wheelVal = WHEEL.get(i);
				if (cpInVal == wheelVal) {
					int j = i + k + prevChar;
					j = j % WHEEL_LENGTH;
					wheelVal = WHEEL.get(j);
					wheelChr = (char) wheelVal;
					cpOut.append(wheelChr);
					prevChar = wheelChr & 0xff;
					found = true;
					break;
				}
			}

			if (!found) {
				if (cpInIdx == cpInArray.length()) {
					break;
				} else {
					cpOut.append(cpInArray.charAt(cpInIdx));
				}
			}

		}

		return cpOut.toString();

	}

	/**
	 * @param newPassword {@code String} with desired new password
	 * @return {@code String} with padded password
	 */
	public static String padPasswordWithRandomStringData(final String newPassword) {
		int lCopy = MAX_PWD_LENGTH - 10 - newPassword.length();
		StringBuilder pwdBuf = new StringBuilder();
		pwdBuf.append(newPassword);

		if (lCopy > 15) {
			pwdBuf.append(RAND_STRING.substring(0, lCopy));
		}

		String randPaddedNewPassword = pwdBuf.toString();
		return randPaddedNewPassword;
	}

	/**
	 * Get a hex representation of a byte array suitable for printing logs and
	 * debugging obfuscation routines.
	 *
	 * @param b {@code btye[]} to be converted into a hex representation for
	 *          logging.
	 * @return {@code String} with a hex representation.
	 */
	public static String getHexString(final byte[] b) {
		StringBuilder result = new StringBuilder();
		for (byte element : b) {

			result.append(Integer.toString((element & 0xff) + 0x100, 16).substring(1));
		}
		return result.toString();
	}

	public static String getHexString2(final byte[] b) {
		StringBuilder result = new StringBuilder();
		for (byte element : b) {
			int asInt = unsignedByteToInt(element);
			result.append(Integer.toHexString(asInt));
		}
		return result.toString();
	}

	/**
	 * Drop the sign bits in a byte for conversion to an int.
	 *
	 * @param b {@code byte} to be converted to an int.
	 * @return {@code int} which is the equivilant of the unsigned version of the
	 *         {@code byte} param.
	 */
	public static int unsignedByteToInt(final byte b) {
		return b & 0xFF;
	}

	public static String getHashedPassword(final String passwordHashValue, final IRODSAccount irodsAccount)
			throws JargonException {
		StringBuilder sb = new StringBuilder();
		sb.append(passwordHashValue);
		sb.append(irodsAccount.getPassword());
		String hashBuff = pad(sb.toString(), 100, '\0');

		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new JargonException("error getting MD5 MessageDigest", e);
		}

		byte[] hashBytes = hashBuff.getBytes();

		messageDigest.update(hashBytes);
		byte[] digestRound1 = messageDigest.digest();

		String hexString = getHexString(digestRound1);
		return hexString;

	}

	/**
	 * Pad a given string to a given length with the given pad character
	 *
	 * @param str     {@code String} to be padded
	 * @param size    {@code int} with the length of the final padded String value
	 * @param padChar {@code char} that will pad the given string
	 * @return {@code String} that is padded out to the given length
	 */
	public static String pad(String str, final int size, final char padChar) {
		if (str.length() < size) {
			char[] temp = new char[size];
			int i = 0;

			while (i < str.length()) {
				temp[i] = str.charAt(i);
				i++;
			}

			while (i < size) {
				temp[i] = padChar;
				i++;
			}

			str = new String(temp);
		}

		return str;
	}

}
