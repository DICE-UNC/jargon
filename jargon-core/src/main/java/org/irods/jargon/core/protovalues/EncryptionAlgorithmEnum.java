package org.irods.jargon.core.protovalues;

/**
 * types of encryption algos (e.g. for parallel transfer encryption)
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public enum EncryptionAlgorithmEnum {

	AES_256_CBC("AES-256-CBC", "AES/CBC/PKCS5Padding", "PBKDF2WithHmacSha1",
			256);

	private String textValue;
	private String cypherKey;
	private String keyGenType;
	private int keySize;

	EncryptionAlgorithmEnum(final String textValue, final String cypherKey,
			final String keyGenType, final int keySize) {
		this.textValue = textValue;
		this.cypherKey = cypherKey;
		this.keyGenType = keyGenType;
		this.keySize = keySize;
	}

	public String getTextValue() {
		return textValue;
	}

	/**
	 * Given a text value resolve the encoding
	 * 
	 * @param userType
	 * @return
	 */
	public static EncryptionAlgorithmEnum findTypeByString(final String userType) {
		EncryptionAlgorithmEnum checksumEncodingValue = null;
		for (EncryptionAlgorithmEnum checksumEnumValue : EncryptionAlgorithmEnum
				.values()) {
			if (checksumEnumValue.getTextValue().equals(userType)) {
				checksumEncodingValue = checksumEnumValue;
				break;
			}
		}

		if (checksumEncodingValue == null) {
			checksumEncodingValue = EncryptionAlgorithmEnum.AES_256_CBC;
		}

		return checksumEncodingValue;

	}

	/**
	 * @return the cypherKey
	 */
	public String getCypherKey() {
		return cypherKey;
	}

	/**
	 * @return the keyGenType
	 */
	public String getKeyGenType() {
		return keyGenType;
	}

	/**
	 * @return the keySize
	 */
	public synchronized int getKeySize() {
		return keySize;
	}

}
