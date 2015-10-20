/**
 * 
 */
package org.irods.jargon.core.transfer;

/**
 * Represents the result of encrypting bytes in a transfer, carrying the
 * encrypted data as well as the computed initialization vector.
 * 
 * @author Mike Conway - DICE
 *
 */
public class EncryptionResult {

	private final byte[] initializationVector;
	private final byte[] encryptedData;

	EncryptionResult(byte[] initializationVector, byte[] encryptedData) {
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
