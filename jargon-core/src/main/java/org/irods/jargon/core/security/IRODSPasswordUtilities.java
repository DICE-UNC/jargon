/**
 * 
 */
package org.irods.jargon.core.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
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
	public static final int MAX_PWD_LENGTH = 50;

	static final Logger log = LoggerFactory
			.getLogger(IRODSPasswordUtilities.class);

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
	 * Obfuscate a new password using the old password, suitable for calls to
	 * iRODS Admin to change a user password.
	 * 
	 * @param newPassword
	 *            <code>String</code> with the desired new password
	 * @param oldPassword
	 *            <code>String</code> with the current password
	 * @return <code>String</code> with the obfuscated password to send to iRODS
	 *         via the iRODS admin protocol.
	 * @throws JargonException
	 */
	public static String obfuscateIRODSPassword(final String newPassword,
			final String oldPassword) throws JargonException {

		if (newPassword == null || newPassword.isEmpty()) {
			throw new JargonException("newPassword is null or empty");
		}

		if (oldPassword == null || oldPassword.isEmpty()) {
			throw new JargonException("oldPassword is null or empty");
		}

		int lCopy = MAX_PWD_LENGTH - 10 - newPassword.length();
		StringBuilder pwdBuf = new StringBuilder();
		pwdBuf.append(newPassword);

		if (lCopy > 15) {
			pwdBuf.append(RAND_STRING.substring(0, lCopy));
		}

		String randPaddedNewPassword = pwdBuf.toString();

		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new JargonException("error getting MD5 MessageDigest", e);
		}

		byte[] oldPwdBuffer = new byte[100];

		// get MD5 hash of the old password
		byte[] oldPwdAsBytes = oldPassword.getBytes();
		for (int i = 0; i < oldPassword.length(); i++) {
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
		byte[] digestRound2 = messageDigest.digest();

		if (log.isDebugEnabled()) {
			log.debug("digestRound2:{}", getHexString(digestRound2));
		}

		// concatenate the first two hashes, and take the hash of that
		byte[] concatRound1AndRound2 = new byte[digestRound1.length
				+ digestRound2.length];
		int concatI = 0;

		for (byte element : digestRound1) {
			concatRound1AndRound2[concatI++] = element;
		}

		for (byte element : digestRound2) {
			concatRound1AndRound2[concatI++] = element;
		}

		messageDigest.reset();
		messageDigest.update(concatRound1AndRound2);
		byte[] digestRound3 = messageDigest.digest();
		if (log.isDebugEnabled()) {
			log.debug("digestRound3:{}", getHexString(digestRound3));
		}

		// concatenate the previous 3 rounds and take a fourth MD5 hash

		byte[] concatRound1AndRound2AndRound3 = new byte[digestRound1.length
				+ digestRound2.length + digestRound3.length];
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
		byte[] digestRound4 = messageDigest.digest();
		if (log.isDebugEnabled()) {
			log.debug("digestRound4:{}", getHexString(digestRound4));
		}

		// concatenate all four hash buffers
		byte[] cpKeyArray = new byte[digestRound1.length + digestRound2.length
				+ digestRound3.length + digestRound4.length];

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
		String cpInArray = randPaddedNewPassword;
		int k = 0;
		boolean found = false;
		int wheelVal;
		char wheelChr;
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
					int j = i + k;
					j = j % WHEEL_LENGTH;
					wheelVal = WHEEL.get(j);
					wheelChr = (char) wheelVal;
					cpOut.append(wheelChr);
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
	 * Get a hex representation of a byte array suitable for printing logs and
	 * debugging obfuscation routines.
	 * 
	 * @param b
	 *            <code>btye[]</code> to be converted into a hex representation
	 *            for logging.
	 * @return <code>String</code> with a hex representation.
	 */
	public static String getHexString(final byte[] b) {
		StringBuilder result = new StringBuilder();
		for (byte element : b) {
			result.append(Integer.toString((element & 0xff) + 0x100, 16)
					.substring(1));
		}
		return result.toString();
	}

	/**
	 * Drop the sign bits in a byte for conversion to an int.
	 * 
	 * @param b
	 *            <code>byte</code> to be converted to an int.
	 * @return <code>int</code> which is the equivilant of the unsigned version
	 *         of the <code>byte</code> param.
	 */
	public static int unsignedByteToInt(final byte b) {
		return b & 0xFF;
	}

	public static String getHashedPassword(final String passwordHashValue,
			final IRODSAccount irodsAccount) throws JargonException {
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
	 * @param str
	 *            <code>String</code> to be padded
	 * @param size
	 *            <code>int</code> with the length of the final padded String
	 *            value
	 * @param padChar
	 *            <code>char</code> that will pad the given string
	 * @return <code>String</code> that is padded out to the given length
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
