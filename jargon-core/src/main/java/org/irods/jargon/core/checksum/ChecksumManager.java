package org.irods.jargon.core.checksum;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;

/**
 * Interface representing code to handle negotiation and determination of
 * checksum type
 *
 * @author Mike Conway - DICXE
 *
 */
public interface ChecksumManager {

	/**
	 * Using the values in the jargon properties, as well as info on the target
	 * server, determine the type of checksum to use
	 *
	 * @return {@link ChecksumEncodingEnum} used for the server
	 * @throws JargonException
	 *             for general error
	 */
	public abstract ChecksumEncodingEnum determineChecksumEncodingForTargetServer() throws JargonException;

	/**
	 * Using a value from iRODS describing a checksum on a file, determine the type
	 * of encoding used. Note that if no checksum is present, {@code null} will be
	 * returned
	 *
	 * @param irodsChecksumValue
	 *            {@code String} with the checksum value from iRODS, potentially
	 *            including a prefix
	 * @return {@link ChecksumValue} indicating the hashing algorithm used to
	 *         determine the checksum and the actual value or {@code null} if no
	 *         checksum available
	 * @throws ChecksumMethodUnavailableException
	 *             for non-existent method
	 */
	public ChecksumValue determineChecksumEncodingFromIrodsData(String irodsChecksumValue)
			throws ChecksumMethodUnavailableException;

}