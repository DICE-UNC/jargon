/**
 * 
 */
package org.irods.jargon.core.transfer;

/**
 * Carries an initialization vector and a set of bytes to encrypt or decrypt
 * 
 * @author Mike Conway - DICE
 *
 */
public class EncryptionBuffer {

	private final byte[] initializationVector;
	private final byte[] encryptedData;

	EncryptionBuffer(byte[] initializationVector, byte[] encryptedData) {
		super();
		if (initializationVector == null) {
			throw new IllegalArgumentException("null initializationVector");
		}
		this.initializationVector = initializationVector;
		this.encryptedData = encryptedData;
	}

	/**
	 * @return the initializationVector
	 */
	byte[] getInitializationVector() {
		return initializationVector;
	}

	/**
	 * @return the encryptedData
	 */
	byte[] getEncryptedData() {
		return encryptedData;
	}

}
