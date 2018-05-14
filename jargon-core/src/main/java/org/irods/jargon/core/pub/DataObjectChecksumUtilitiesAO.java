package org.irods.jargon.core.pub;

import org.irods.jargon.core.checksum.ChecksumValue;
import org.irods.jargon.core.exception.ChecksumInvalidException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;

public interface DataObjectChecksumUtilitiesAO {

	/**
	 * Retrieve the checksum information if and only if it has been previously
	 * computed in iRODS. This call will NOT create a new checksum in iRODS, and is
	 * query only.
	 *
	 * @param irodsDataObjectAbsolutePath
	 *            {@code String} with the absolute iRODS path to a data object
	 * @return {@link ChecksumValue} with checksum and algo information
	 * @throws FileNotFoundException
	 *             if file missing
	 * @throws JargonException
	 *             for iRODS error
	 */
	ChecksumValue retrieveExistingChecksumForDataObject(String irodsDataObjectAbsolutePath)
			throws FileNotFoundException, JargonException;

	/**
	 * Given a value (iRODS representation of a checksum), as one might find in
	 * {@link ObjStat}, and parse it out into algo and checksum value
	 *
	 * @param irodsValue
	 *            {@code String}, typically in algo:string value format.
	 * @return {@link ChecksumValue}, parsed out
	 * @throws JargonException
	 *             for iRODS error
	 */
	ChecksumValue computeChecksumValueFromIrodsData(String irodsValue) throws JargonException;

	/**
	 * Given a data object in iRODS, compute its checksum using the defaut algo and
	 * return it. The algo is determined by iRODS policy
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} representing the data object
	 * @return {@link ChecksumValue} with checksum and algo information
	 * @throws JargonException
	 *             for iRODS error
	 */
	ChecksumValue computeChecksumOnDataObject(IRODSFile irodsFile) throws JargonException;

	/**
	 * Compare a local file against iRODS using the configured checksum algo on the
	 * iRODS side. Return the checksum information, or throw an exception if the
	 * checksums do not match
	 *
	 * @param localAbsolutePath
	 *            {@code String} with the local file absolute path
	 * @param irodsAbsolutePath
	 *            {@code String} with the irods file absolute path
	 * @return {@link ChecksumValue} for a validated checksum
	 * @throws FileNotFoundException
	 *             if either file is missing
	 * @throws ChecksumInvalidException
	 *             if the checksums do not match
	 * @throws JargonException
	 *             for iRODS error
	 */
	ChecksumValue verifyLocalFileAgainstIrodsFileChecksum(final String localAbsolutePath,
			final String irodsAbsolutePath) throws FileNotFoundException, ChecksumInvalidException, JargonException;

}