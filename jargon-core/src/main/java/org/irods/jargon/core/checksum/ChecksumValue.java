/**
 *
 */
package org.irods.jargon.core.checksum;

import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;

/**
 * Represents a checksum and the type of checksum
 *
 * @author Mike Conway - DICE
 *
 */
public class ChecksumValue {

	/**
	 * representation of the checksum as transmitted to iRODS, may include a
	 * checksum type prefix
	 */
	private String checksumTransmissionFormat = "";
	/**
	 * Stringified representation of the checksum digest in the format expected
	 * by the packing instruction
	 */
	private String checksumStringValue = "";

	/**
	 * Encoding used for the checksum
	 */
	private ChecksumEncodingEnum checksumEncoding = ChecksumEncodingEnum.MD5;

	/**
	 *
	 */
	public ChecksumValue() {
	}

	/**
	 * @return the checksumStringValue
	 */
	public String getChecksumStringValue() {
		return checksumStringValue;
	}

	/**
	 * @return the checksumEncoding
	 */
	public ChecksumEncodingEnum getChecksumEncoding() {
		return checksumEncoding;
	}

	/**
	 * @param checksumStringValue
	 *            the checksumStringValue to set
	 */
	public void setChecksumStringValue(final String checksumStringValue) {
		this.checksumStringValue = checksumStringValue;
	}

	/**
	 * @param checksumEncoding
	 *            the checksumEncoding to set
	 */
	public void setChecksumEncoding(final ChecksumEncodingEnum checksumEncoding) {
		this.checksumEncoding = checksumEncoding;
	}

	/**
	 * @return the checksumTransmissionFormat
	 */
	public String getChecksumTransmissionFormat() {
		return checksumTransmissionFormat;
	}

	/**
	 * @param checksumTransmissionFormat
	 *            the checksumTransmissionFormat to set
	 */
	public void setChecksumTransmissionFormat(
			final String checksumTransmissionFormat) {
		this.checksumTransmissionFormat = checksumTransmissionFormat;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChecksumValue [");
		if (checksumTransmissionFormat != null) {
			builder.append("checksumTransmissionFormat=");
			builder.append(checksumTransmissionFormat);
			builder.append(", ");
		}
		if (checksumStringValue != null) {
			builder.append("checksumStringValue=");
			builder.append(checksumStringValue);
			builder.append(", ");
		}
		if (checksumEncoding != null) {
			builder.append("checksumEncoding=");
			builder.append(checksumEncoding);
		}
		builder.append("]");
		return builder.toString();
	}

}
