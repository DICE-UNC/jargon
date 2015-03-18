package org.irods.jargon.core.protovalues;

/**
 * types of requested and computed checksums
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public enum ChecksumEncodingEnum {

	DEFAULT("DEFAULT"), MD5("MD5"), SHA256("SHA256"), STRONG("STRONG");

	private String textValue;

	ChecksumEncodingEnum(final String textValue) {
		this.textValue = textValue;
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
	public static ChecksumEncodingEnum findTypeByString(final String userType) {
		ChecksumEncodingEnum checksumEncodingValue = null;
		for (ChecksumEncodingEnum checksumEnumValue : ChecksumEncodingEnum
				.values()) {
			if (checksumEnumValue.getTextValue().equals(userType)) {
				checksumEncodingValue = checksumEnumValue;
				break;
			}
		}
		if (checksumEncodingValue == null) {
			checksumEncodingValue = ChecksumEncodingEnum.DEFAULT;
		}
		return checksumEncodingValue;

	}
}
