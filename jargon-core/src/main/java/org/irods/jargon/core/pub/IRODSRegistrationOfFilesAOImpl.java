package org.irods.jargon.core.pub;

import java.io.File;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.CollectionNotEmptyException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.CollInp;
import org.irods.jargon.core.packinstr.DataObjInpForReg;
import org.irods.jargon.core.packinstr.DataObjInpForReg.ChecksumHandling;
import org.irods.jargon.core.packinstr.DataObjInpForUnregister;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access Object that manages the registration of files, as managed by the iRODS
 * ireg command.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSRegistrationOfFilesAOImpl extends IRODSGenericAO implements
		IRODSRegistrationOfFilesAO {

	static Logger log = LoggerFactory
			.getLogger(IRODSRegistrationOfFilesAOImpl.class);

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected IRODSRegistrationOfFilesAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSRegistrationOfFilesAO#
	 * registerPhysicalCollectionRecursivelyToIRODS(java.lang.String,
	 * java.lang.String, boolean, java.lang.String, java.lang.String)
	 */
	@Override
	public void registerPhysicalCollectionRecursivelyToIRODS(
			final String physicalPath, final String irodsAbsolutePath,
			final boolean force, final String destinationResource,
			final String resourceGroup) throws DataNotFoundException,
			DuplicateDataException, JargonException {

		log.info("registerPhysicalCollectionRecursivelyToIRODS()");

		if (physicalPath == null || physicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty physical path");
		}

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (destinationResource == null || destinationResource.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty destination resource");
		}

		if (resourceGroup == null) {
			throw new IllegalArgumentException(
					"null resourceGroup, set to blank if not used");
		}

		log.info("physicalPath:{}", physicalPath);
		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("force:{}", force);
		log.info("destinationResource:{}", destinationResource);
		log.info("resourceGroup:{}", resourceGroup);

		File localFile = new File(physicalPath);
		if (!localFile.exists()) {
			log.error("cannot find local file");
			throw new DataNotFoundException("file to register does not exist");
		}

		if (localFile.isFile()) {
			throw new JargonException("given file is a file, not a collection");
		}

		ChecksumHandling checksumHandling = ChecksumHandling.NONE;

		DataObjInpForReg dataObjInp = DataObjInpForReg.instance(physicalPath,
				irodsAbsolutePath, resourceGroup, destinationResource, force,
				true, checksumHandling, false, "");

		getIRODSProtocol().irodsFunction(dataObjInp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSRegistrationOfFilesAO#
	 * registerPhysicalDataFileToIRODS(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void registerPhysicalDataFileToIRODS(final String physicalPath,
			final String irodsAbsolutePath, final String destinationResource,
			final String resourceGroup, final boolean generateChecksumInIRODS)
			throws DataNotFoundException, DuplicateDataException,
			JargonException {

		log.info("registerPhysicalDataToFileInIRODS()");

		if (physicalPath == null || physicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty physical path");
		}

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (destinationResource == null || destinationResource.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty destination resource");
		}

		if (resourceGroup == null) {
			throw new IllegalArgumentException(
					"null resourceGroup, set to blank if not used");
		}

		log.info("physicalPath:{}", physicalPath);
		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("destinationResource:{}", destinationResource);
		log.info("resourceGroup:{}", resourceGroup);
		log.info("generateChecksumInIrods:{}", generateChecksumInIRODS);

		File localFile = new File(physicalPath);
		if (!localFile.exists()) {
			log.error("cannot find local file");
			throw new DataNotFoundException("file to register does not exist");
		}

		if (!localFile.isFile()) {
			throw new JargonException(
					"given file is a collection, not a data object");
		}

		ChecksumHandling checksumHandling;
		if (generateChecksumInIRODS) {
			checksumHandling = ChecksumHandling.REGISTER_CHECKSUM;
		} else {
			checksumHandling = ChecksumHandling.NONE;
		}

		DataObjInpForReg dataObjInp = DataObjInpForReg.instance(physicalPath,
				irodsAbsolutePath, resourceGroup, destinationResource, false,
				false, checksumHandling, false, "");

		getIRODSProtocol().irodsFunction(dataObjInp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSRegistrationOfFilesAO#
	 * registerPhysicalDataFileToIRODSWithVerifyLocalChecksum(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String registerPhysicalDataFileToIRODSWithVerifyLocalChecksum(
			final String physicalPath, final String irodsAbsolutePath,
			final String destinationResource, final String resourceGroup)
			throws DataNotFoundException, DuplicateDataException,
			JargonException {

		log.info("registerPhysicalDataFileToIRODSWithVerifyLocalChecksum()");

		if (physicalPath == null || physicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty physical path");
		}

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (destinationResource == null || destinationResource.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty destination resource");
		}

		if (resourceGroup == null) {
			throw new IllegalArgumentException(
					"null resourceGroup, set to blank if not used");
		}

		log.info("physicalPath:{}", physicalPath);
		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("destinationResource:{}", destinationResource);
		log.info("resourceGroup:{}", resourceGroup);

		File localFile = new File(physicalPath);
		if (!localFile.exists()) {
			log.error("cannot find local file");
			throw new DataNotFoundException("file to register does not exist");
		}

		if (!localFile.isFile()) {
			throw new JargonException(
					"given file is a collection, not a data object");
		}

		log.info("calculating local checksum..");
		ChecksumHandling checksumHandling = ChecksumHandling.VERFIY_CHECKSUM;
		String localChecksum = LocalFileUtils
				.md5ByteArrayToString(LocalFileUtils
						.computeMD5FileCheckSumViaAbsolutePath(physicalPath));

		log.info("local file checksum:{}", localChecksum);

		DataObjInpForReg dataObjInp = DataObjInpForReg.instance(physicalPath,
				irodsAbsolutePath, resourceGroup, destinationResource, false,
				false, checksumHandling, false, localChecksum);

		getIRODSProtocol().irodsFunction(dataObjInp);

		return localChecksum;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSRegistrationOfFilesAO#
	 * unregisterDataObject(java.lang.String)
	 */
	@Override
	public boolean unregisterDataObject(final String irodsAbsolutePath)
			throws JargonException {
		log.info("unregisterButDoNotDeletePhysicalFile()");

		boolean success = true;
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		DataObjInpForUnregister dataObjInp = DataObjInpForUnregister
				.instanceForDelete(irodsAbsolutePath, false);

		try {
			Tag response = getIRODSProtocol().irodsFunction(dataObjInp);

			if (response != null) {
				log.warn("unexpected response from irods, expected null message - logged and ignored ");
			}
		} catch (DuplicateDataException dde) {
			log.warn("duplicate data exception logged and ignored, see GForge: [#639] 809000 errors on delete operations when trash file already exists");
		} catch (DataNotFoundException dnf) {
			success = false;
		}

		return success;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSRegistrationOfFilesAO#unregisterCollection
	 * (java.lang.String, boolean)
	 */
	@Override
	public boolean unregisterCollection(final String irodsAbsolutePath,
			final boolean recursive) throws CollectionNotEmptyException,
			JargonException {
		log.info("unregisterCollection()");

		boolean success = true;
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		CollInp collInp = CollInp.instanceForUnregisterCollection(
				irodsAbsolutePath, false, recursive);

		try {
			Tag response = getIRODSProtocol().irodsFunction(CollInp.PI_TAG,
					collInp.getParsedTags(), CollInp.RMDIR_API_NBR);

			if (response != null) {
				log.warn("unexpected response from irods, expected null message - logged and ignored ");
			}

			log.debug("process any client status messages");
			this.getIRODSProtocol().processClientStatusMessages(response);

		} catch (DuplicateDataException dde) {
			log.warn("duplicate data exception logged and ignored, see GForge: [#639] 809000 errors on delete operations when trash file already exists");
		} catch (DataNotFoundException dnf) {
			success = false;
		}

		return success;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSRegistrationOfFilesAO#
	 * registerPhysicalDataFileToIRODSAsAReplica(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void registerPhysicalDataFileToIRODSAsAReplica(
			final String physicalPath, final String irodsAbsolutePath,
			final String destinationResource, final String resourceGroup,
			final boolean generateChecksumInIRODS)
			throws DataNotFoundException, DuplicateDataException,
			JargonException {

		log.info("registerPhysicalDataFileToIRODSAsAReplica()");

		if (physicalPath == null || physicalPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty physical path");
		}

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (destinationResource == null || destinationResource.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty destination resource");
		}

		if (resourceGroup == null) {
			throw new IllegalArgumentException(
					"null resourceGroup, set to blank if not used");
		}

		log.info("physicalPath:{}", physicalPath);
		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("destinationResource:{}", destinationResource);
		log.info("resourceGroup:{}", resourceGroup);
		log.info("generateChecksumInIrods:{}", generateChecksumInIRODS);

		File localFile = new File(physicalPath);
		if (!localFile.exists()) {
			log.error("cannot find local file");
			throw new DataNotFoundException("file to register does not exist");
		}

		if (!localFile.isFile()) {
			throw new JargonException(
					"given file is a collection, not a data object");
		}

		ChecksumHandling checksumHandling;
		if (generateChecksumInIRODS) {
			checksumHandling = ChecksumHandling.REGISTER_CHECKSUM;
		} else {
			checksumHandling = ChecksumHandling.NONE;
		}

		DataObjInpForReg dataObjInp = DataObjInpForReg.instance(physicalPath,
				irodsAbsolutePath, resourceGroup, destinationResource, false,
				false, checksumHandling, true, "");

		getIRODSProtocol().irodsFunction(dataObjInp);

	}

}
