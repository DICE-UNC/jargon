/**
 * 
 */
package org.irods.jargon.core.checksum;

import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;

/**
 * Value of a checksum stored in iRODS
 * 
 * @author Mike Conway - DICE
 *
 */
public class IrodsChecksumValue {
	/**
	 * Actual checksum value as stored in iRODS as a hex string
	 */
	private String checksumValue = "";
	/**
	 * Type of checksum stored in iRODS
	 */
	private ChecksumEncodingEnum checksumEncodingEnum = ChecksumEncodingEnum.MD5;

	public String getChecksumValue() {
		return checksumValue;
	}

	public void setChecksumValue(String checksumValue) {
		this.checksumValue = checksumValue;
	}

	public ChecksumEncodingEnum getChecksumEncodingEnum() {
		return checksumEncodingEnum;
	}

	public void setChecksumEncodingEnum(
			ChecksumEncodingEnum checksumEncodingEnum) {
		this.checksumEncodingEnum = checksumEncodingEnum;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IrodsChecksumValue [");
		if (checksumValue != null) {
			builder.append("checksumValue=").append(checksumValue).append(", ");
		}
		if (checksumEncodingEnum != null) {
			builder.append("checksumEncodingEnum=")
					.append(checksumEncodingEnum);
		}
		builder.append("]");
		return builder.toString();
	}

}
