/**
 *
 */
package org.irods.jargon.core.pub;

import java.io.File;

import org.irods.jargon.core.checksum.AbstractChecksumComputeStrategy;
import org.irods.jargon.core.checksum.ChecksumManager;
import org.irods.jargon.core.checksum.ChecksumManagerImpl;
import org.irods.jargon.core.checksum.ChecksumValue;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.ChecksumInvalidException;
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
 * {@code DataObjectAO}, this class provides a convenient set of methods to
 * obtain and compare various types of checksums, including utilities to verify
 * checksums between local files and iRODS files.
 *
 * @author Mike Conway - DICE
 *
 */
public class DataObjectChecksumUtilitiesAOImpl extends IRODSGenericAO implements DataObjectChecksumUtilitiesAO {

	public static final Logger log = LoggerFactory.getLogger(DataObjectChecksumUtilitiesAOImpl.class);
	private final ChecksumManager checksumManager;
	private final CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO;

	/**
	 * @param irodsSession
	 *            {@link IRODSSession}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @throws JargonException
	 *             for iRODS error
	 */
	public DataObjectChecksumUtilitiesAOImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		super(irodsSession, irodsAccount);
		checksumManager = new ChecksumManagerImpl(irodsAccount, getIRODSAccessObjectFactory());
		collectionAndDataObjectListAndSearchAO = getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.DataObjectChecksumUtilitiesAO#
	 * retrieveExistingChecksumForDataObject(java.lang.String)
	 */
	@Override
	public ChecksumValue retrieveExistingChecksumForDataObject(final String irodsDataObjectAbsolutePath)
			throws FileNotFoundException, JargonException {

		log.info("retrieveChecksumForDataObject()");

		if (irodsDataObjectAbsolutePath == null || irodsDataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsDataObjectAbsolutePath");
		}
		log.info("irodsDataObjectAbsolutePath:{}", irodsDataObjectAbsolutePath);
		ObjStat objStat = collectionAndDataObjectListAndSearchAO.retrieveObjectStatForPath(irodsDataObjectAbsolutePath);
		return computeChecksumValueFromIrodsData(objStat.getChecksum());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.DataObjectChecksumUtilitiesAO#
	 * computeChecksumOnDataObject(org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public ChecksumValue computeChecksumOnDataObject(final IRODSFile irodsFile) throws JargonException {

		log.info("computeChecksumOnDataObject()");

		if (irodsFile == null) {
			throw new IllegalArgumentException("irodsFile is null");
		}
		// FIXME: here! should take props into account?
		log.info("computing checksum on irodsFile: {}", irodsFile.getAbsolutePath());

		DataObjInp dataObjInp = DataObjInp.instanceForDataObjectChecksum(irodsFile.getAbsolutePath());
		dataObjInp.setTransferOptions(getIRODSAccessObjectFactory().buildTransferOptionsBasedOnJargonProperties());
		Tag response = getIRODSProtocol().irodsFunction(dataObjInp);

		if (response == null) {
			log.error("invalid response to checksum call, response was null, expected checksum value");
			throw new JargonException(
					"invalid response to checksum call, received null response when doing checksum on file:"
							+ irodsFile);
		}

		String returnedChecksum = response.getTag(DataObjInp.MY_STR).getStringValue();
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
	public ChecksumValue computeChecksumValueFromIrodsData(final String irodsValue) throws JargonException {
		// param checks in delegated method
		return checksumManager.determineChecksumEncodingFromIrodsData(irodsValue.trim());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.DataObjectChecksumUtilitiesAO#
	 * verifyLocalFileAgainstIrodsFileChecksum(java.lang.String, java.lang.String)
	 */
	@Override
	public ChecksumValue verifyLocalFileAgainstIrodsFileChecksum(final String localAbsolutePath,
			final String irodsAbsolutePath) throws FileNotFoundException, ChecksumInvalidException, JargonException {
		log.info("verifyLocalFileAgainstIrodsFileChecksum()");
		if (localAbsolutePath == null || localAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty localAbsolutePath");
		}

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		log.info("localAbsolutePath:{}", localAbsolutePath);
		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		File localFile = new File(localAbsolutePath);

		if (!localFile.exists()) {
			throw new FileNotFoundException("local file does not exist");
		}

		if (!localFile.isFile()) {
			throw new JargonException("local file is not a file, it is a collection");
		}

		IRODSFile irodsFile = getIRODSFileFactory().instanceIRODSFile(irodsAbsolutePath);

		if (!irodsFile.exists()) {
			throw new FileNotFoundException("irods file does not exist");
		}

		if (!irodsFile.isFile()) {
			throw new JargonException("irods file is not a file, it is a collection");
		}

		ChecksumValue checksumValue = computeChecksumOnDataObject(irodsFile);

		AbstractChecksumComputeStrategy checksumComputeStrategy = getIRODSSession().getLocalChecksumComputerFactory()
				.instance(checksumValue.getChecksumEncoding());
		ChecksumValue localValue;
		try {
			localValue = checksumComputeStrategy.computeChecksumValueForLocalFile(localAbsolutePath);
		} catch (java.io.FileNotFoundException e) {
			// Jargon has it's own file not found exception, dumb or not
			throw new FileNotFoundException("local file not found during checksum");
		}

		if (!localValue.equals(checksumValue)) {
			log.error("checksum mismatch");
			log.error("local checksum:{}", localValue);
			log.error("irods checksum:{}", checksumValue);
			throw new ChecksumInvalidException("checksum mismatch between local and iRODS");
		}

		return checksumValue;

	}

}
