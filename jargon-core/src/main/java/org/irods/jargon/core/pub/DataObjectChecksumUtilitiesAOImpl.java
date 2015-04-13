/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.checksum.ChecksumValue;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Introduced as checksum operations became more complicated in
 * <code>DataObjectAO</code>, this class provides a convenient set of methods to
 * obtain and compare various types of checksums, including utilities to verify
 * checksums between local files and iRODS files.
 * 
 * @author Mike Conway - DICE
 *
 */
public class DataObjectChecksumUtilitiesAOImpl extends IRODSGenericAO {

	public static final Logger log = LoggerFactory
			.getLogger(DataObjectChecksumUtilitiesAOImpl.class);
	private final DataObjectAO dataObjectAO;

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	public DataObjectChecksumUtilitiesAOImpl(IRODSSession irodsSession,
			IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		this.dataObjectAO = this.getIRODSAccessObjectFactory().getDataObjectAO(
				irodsAccount);
	}

	public ChecksumValue retrieveOrComputeIrodsChecksumOfType(
			final ChecksumEncodingEnum checksumEncoding,
			final String irodsDataObjectAbsolutePath)
			throws FileNotFoundException, JargonException {

		log.info("retrieveOrComputeIrodsChecksumOfType()");
		if (checksumEncoding == null) {
			throw new IllegalArgumentException("null checksumEncoding");
		}

		if (irodsDataObjectAbsolutePath == null
				|| irodsDataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsDataObjectAbsolutePath");
		}

		log.info("checksumEncoding:{}", checksumEncoding);
		log.info("irodsDataObjectAbsolutePath:{}", irodsDataObjectAbsolutePath);

	}

}
