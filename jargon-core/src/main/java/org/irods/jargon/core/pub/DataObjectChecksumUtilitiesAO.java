package org.irods.jargon.core.pub;

import org.irods.jargon.core.checksum.ChecksumValue;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;

public interface DataObjectChecksumUtilitiesAO {

	/**
	 * Retrieve the checksum information if and only if it has been previously
	 * computed in iRODS. This call will NOT create a new checksum in iRODS, and
	 * is query only.
	 * 
	 * @param irodsDataObjectAbsolutePath
	 *            <code>String</code> with the absolute iRODS path to a data
	 *            object
	 * @return {@link ChecksumValue} with checksum and algo information
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	ChecksumValue retrieveExistingChecksumForDataObject(
			String irodsDataObjectAbsolutePath) throws FileNotFoundException,
			JargonException;

	/**
	 * Given a value (iRODS representation of a checksum), as one might find in
	 * {@link ObjStat}, and parse it out into algo and checksum value
	 * 
	 * @param irodsValue
	 *            <code>String</code>, typically in algo:string value format.
	 * @return {@link ChecksumValue}, parsed out
	 * @throws JargonException
	 */
	ChecksumValue computeChecksumValueFromIrodsData(String irodsValue)
			throws JargonException;

	/**
	 * Given a data object in iRODS, compute its checksum using the defaut algo
	 * and return it. The algo is determined by iRODS policy
	 * 
	 * @param irodsFile
	 *            {@link IRODSFile} representing the data object
	 * @return {@link ChecksumValue} with checksum and algo information
	 * @throws JargonException
	 */
	ChecksumValue computeChecksumOnDataObject(IRODSFile irodsFile)
			throws JargonException;

}