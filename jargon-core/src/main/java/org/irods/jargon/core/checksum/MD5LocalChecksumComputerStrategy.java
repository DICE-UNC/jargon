/**
 * 
 */
package org.irods.jargon.core.checksum;

import java.io.FileNotFoundException;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compute an MD5 checksum on a local file
 * 
 * @author Mike Conway - DICE
 */
public class MD5LocalChecksumComputerStrategy extends
		AbstractChecksumComputeStrategy {

	public static final Logger log = LoggerFactory
			.getLogger(MD5LocalChecksumComputerStrategy.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.checksum.AbstractChecksumComputer#
	 * instanceChecksumForPackingInstruction(java.lang.String)
	 */
	@Override
	public String instanceChecksumForPackingInstruction(
			String localFileAbsolutePath) throws FileNotFoundException,
			JargonException {

		log.info("instanceChecksumForPackingInstruction()");

		if (localFileAbsolutePath == null || localFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty localFileAbsolutePath");
		}

		byte[] digest = LocalFileUtils
				.computeMD5FileCheckSumViaAbsolutePath(localFileAbsolutePath);
		return LocalFileUtils.digestByteArrayToString(digest);

	}

}
