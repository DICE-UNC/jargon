/**
 *
 */
package org.irods.jargon.core.checksum;

import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;

/**
 * Factory to create methods to compute local checksums based on a type that can
 * be derived from the {@link ChecksumManager}. *
 * <p>
 * This factory is available from the {@link IRODSSession}
 *
 * @author Mike Conway - DICE
 *
 */
public class LocalChecksumComputerFactoryImpl implements LocalChecksumComputerFactory {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.checksum.LocalChecksumComputerFactory#instance(
	 * org.irods.jargon.core.protovalues.ChecksumEncodingEnum)
	 */
	@Override
	public AbstractChecksumComputeStrategy instance(final ChecksumEncodingEnum checksumEncodingEnum)
			throws ChecksumMethodUnavailableException {

		if (checksumEncodingEnum == null) {
			throw new IllegalArgumentException("null checksumEncodingEnum");
		}

		if (checksumEncodingEnum == ChecksumEncodingEnum.MD5) {
			return new MD5LocalChecksumComputerStrategy();
		} else if (checksumEncodingEnum == ChecksumEncodingEnum.SHA256) {
			return new SHA256LocalChecksumComputerStrategy();
		} else {
			throw new ChecksumMethodUnavailableException(
					"unable to find a checksum encoding method for:" + checksumEncodingEnum);
		}

	}

}
