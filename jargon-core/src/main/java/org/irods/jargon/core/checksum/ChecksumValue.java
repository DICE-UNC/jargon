/**
 *
 */
package org.irods.jargon.core.checksum;

import java.util.Arrays;

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
	 * Actual checksum value as stored in iRODS in the default representation, this
	 * may vary based on the implementation
	 */
	private String checksumStringValue = "";

	/**
	 * Checksum represented as a hex string
	 */
	private String hexChecksumValue = "";

	/**
	 * Checksum represented as a base64 string
	 */
	private String base64ChecksumValue = "";

	/**
	 * byte array representation of the checksum value
	 */
	private byte[] binaryChecksumValue = new byte[0];
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
	public void setChecksumTransmissionFormat(final String checksumTransmissionFormat) {
		this.checksumTransmissionFormat = checksumTransmissionFormat;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("ChecksumValue [");
		if (checksumTransmissionFormat != null) {
			builder.append("checksumTransmissionFormat=").append(checksumTransmissionFormat).append(", ");
		}
		if (checksumStringValue != null) {
			builder.append("checksumStringValue=").append(checksumStringValue).append(", ");
		}
		if (hexChecksumValue != null) {
			builder.append("hexChecksumValue=").append(hexChecksumValue).append(", ");
		}
		if (base64ChecksumValue != null) {
			builder.append("base64ChecksumValue=").append(base64ChecksumValue).append(", ");
		}
		if (binaryChecksumValue != null) {
			builder.append("binaryChecksumValue=")
					.append(Arrays
							.toString(Arrays.copyOf(binaryChecksumValue, Math.min(binaryChecksumValue.length, maxLen))))
					.append(", ");
		}
		if (checksumEncoding != null) {
			builder.append("checksumEncoding=").append(checksumEncoding);
		}
		builder.append("]");
		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((checksumEncoding == null) ? 0 : checksumEncoding.hashCode());
		result = prime * result + ((checksumStringValue == null) ? 0 : checksumStringValue.hashCode());
		result = prime * result + ((checksumTransmissionFormat == null) ? 0 : checksumTransmissionFormat.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ChecksumValue other = (ChecksumValue) obj;
		if (checksumEncoding != other.checksumEncoding) {
			return false;
		}
		if (checksumStringValue == null) {
			if (other.checksumStringValue != null) {
				return false;
			}
		} else if (!checksumStringValue.equals(other.checksumStringValue)) {
			return false;
		}
		if (checksumTransmissionFormat == null) {
			if (other.checksumTransmissionFormat != null) {
				return false;
			}
		} else if (!checksumTransmissionFormat.equals(other.checksumTransmissionFormat)) {
			return false;
		}
		return true;
	}

	public String getHexChecksumValue() {
		return hexChecksumValue;
	}

	public void setHexChecksumValue(String hexChecksumValue) {
		this.hexChecksumValue = hexChecksumValue;
	}

	public String getBase64ChecksumValue() {
		return base64ChecksumValue;
	}

	public void setBase64ChecksumValue(String base64ChecksumValue) {
		this.base64ChecksumValue = base64ChecksumValue;
	}

	public byte[] getBinaryChecksumValue() {
		return binaryChecksumValue;
	}

	public void setBinaryChecksumValue(byte[] binaryChecksumValue) {
		this.binaryChecksumValue = binaryChecksumValue;
	}

}
