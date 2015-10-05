package org.irods.jargon.core.protovalues;

/**
 * types of encryption algos (e.g. for parallel transfer encryption)
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public enum EncryptionAlgorithmEnum {

	AES_256_CBC("AES-256-CBC", "AES/CBC/NoPadding", "AES");

	private String textValue;
	private String cypherKey;
	private String keyGenType;

	EncryptionAlgorithmEnum(final String textValue, final String cypherKey,
			final String keyGenType) {
		this.textValue = textValue;
		this.cypherKey = cypherKey;
		this.keyGenType = keyGenType;
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
	public synchronized String getCypherKey() {
		return cypherKey;
	}

	/**
	 * @return the keyGenType
	 */
	public synchronized String getKeyGenType() {
		return keyGenType;
	}

	/**
	 * @param keyGenType
	 *            the keyGenType to set
	 */
	public synchronized void setKeyGenType(String keyGenType) {
		this.keyGenType = keyGenType;
	}
}
