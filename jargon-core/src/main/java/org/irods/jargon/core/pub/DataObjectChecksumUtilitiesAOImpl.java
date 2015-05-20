/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.checksum.ChecksumManager;
import org.irods.jargon.core.checksum.ChecksumManagerImpl;
import org.irods.jargon.core.checksum.ChecksumValue;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
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
public class DataObjectChecksumUtilitiesAOImpl extends IRODSGenericAO implements
		DataObjectChecksumUtilitiesAO {

	public static final Logger log = LoggerFactory
			.getLogger(DataObjectChecksumUtilitiesAOImpl.class);
	private final ChecksumManager checksumManager;
	private final CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO;

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	public DataObjectChecksumUtilitiesAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		this.checksumManager = new ChecksumManagerImpl(irodsAccount,
				this.getIRODSAccessObjectFactory());
		this.collectionAndDataObjectListAndSearchAO = this
				.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectChecksumUtilitiesAO#
	 * retrieveExistingChecksumForDataObject(java.lang.String)
	 */
	@Override
	public ChecksumValue retrieveExistingChecksumForDataObject(
			final String irodsDataObjectAbsolutePath)
			throws FileNotFoundException, JargonException {

		log.info("retrieveChecksumForDataObject()");

		if (irodsDataObjectAbsolutePath == null
				|| irodsDataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsDataObjectAbsolutePath");
		}
		log.info("irodsDataObjectAbsolutePath:{}", irodsDataObjectAbsolutePath);
		ObjStat objStat = this.collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsDataObjectAbsolutePath);
		return computeChecksumValueFromIrodsData(objStat.getChecksum());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectChecksumUtilitiesAO#
	 * computeChecksumOnDataObject(org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public ChecksumValue computeChecksumOnDataObject(final IRODSFile irodsFile)
			throws JargonException {

		log.info("computeChecksumOnDataObject()");

		if (irodsFile == null) {
			throw new IllegalArgumentException("irodsFile is null");
		}

		log.info("computing checksum on irodsFile: {}",
				irodsFile.getAbsolutePath());

		DataObjInp dataObjInp = DataObjInp
				.instanceForDataObjectChecksum(irodsFile.getAbsolutePath());
		Tag response = getIRODSProtocol().irodsFunction(dataObjInp);

		if (response == null) {
			log.error("invalid response to checksum call, response was null, expected checksum value");
			throw new JargonException(
					"invalid response to checksum call, received null response when doing checksum on file:"
							+ irodsFile);
		}

		String returnedChecksum = response.getTag(DataObjInp.MY_STR)
				.getStringValue();
		log.info("checksum is: {}", returnedChecksum);

		return computeChecksumValueFromIrodsData(returnedChecksum);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectChecksumUtilitiesAO#
	 * computeChecksumValueFromIrodsData(java.lang.String)
	 */
	@Override
	public ChecksumValue computeChecksumValueFromIrodsData(
			final String irodsValue) throws JargonException {
		// param checks in delegated method
		return checksumManager
				.determineChecksumEncodingFromIrodsData(irodsValue.trim());
	}

}
