package org.irods.jargon.core.checksum;

import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;

/**
 * Factory interface to create methods to compute local checksums based on a
 * type that can be derived from the {@link ChecksumManager}.
 * <p/>
 * This factory is available from the {@link IRODSSession}
 *
 * @author Mike Conway - DICE
 */
public interface LocalChecksumComputerFactory {

	/**
	 * Based on the checksum encoding type, find an instance of an encoder
	 *
	 * @param checksumEncodingEnum
	 *            {@link ChecksumEncodingEnum} value of a specific type
	 * @return {@link AbstractChecksumComputeStrategy} that implements that
	 *         encoding algorithm
	 * @throws ChecksumMethodUnavailableException
	 *             if the algorithm is unsupported
	 */
	public abstract AbstractChecksumComputeStrategy instance(
			ChecksumEncodingEnum checksumEncodingEnum)
			throws ChecksumMethodUnavailableException;

}