/**
 *
 */
package org.irods.jargon.core.checksum;

import java.io.FileNotFoundException;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compute an SHA256 checksum on a local file
 *
 * @author Mike Conway - DICE
 */
public class SHA256LocalChecksumComputerStrategy extends AbstractChecksumComputeStrategy {

	public static final Logger log = LoggerFactory.getLogger(SHA256LocalChecksumComputerStrategy.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.checksum.AbstractChecksumComputer#
	 * instanceChecksumForPackingInstruction(java.lang.String)
	 */
	@Override
	public ChecksumValue computeChecksumValueForLocalFile(final String localFileAbsolutePath)
			throws FileNotFoundException, JargonException {

		log.info("instanceChecksumForPackingInstruction()");

		if (localFileAbsolutePath == null || localFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty localFileAbsolutePath");
		}

		byte[] digest = LocalFileUtils.computeSHA256FileCheckSumViaAbsolutePath(localFileAbsolutePath);

		ChecksumValue value = new ChecksumValue();
		value.setChecksumEncoding(ChecksumEncodingEnum.SHA256);
		value.setChecksumStringValue(Base64.encodeBase64String(digest).trim());
		value.setBinaryChecksumValue(digest);
		value.setHexChecksumValue(Hex.toHexString(digest));
		value.setBase64ChecksumValue(value.getChecksumStringValue());

		StringBuilder sb = new StringBuilder();
		sb.append("sha2:");
		sb.append(value.getChecksumStringValue());
		value.setChecksumTransmissionFormat(sb.toString().trim());
		return value;

	}

}
