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
	 * Stringified representation of the checksum digest in the format expected by
	 * the packing instruction
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
	public void setChecksumTransmissionFormat(final String checksumTransmissionFormat) {
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

}
