package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.CollectionNotEmptyException;
import org.irods.jargon.core.exception.CollectionNotMountedException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.UnixFileMkdirException;
import org.irods.jargon.core.packinstr.DataObjInpForMcoll;
import org.irods.jargon.core.packinstr.DataObjInpForUnmount;
import org.irods.jargon.core.packinstr.TransferOptions.PutOptions;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.utils.Overheaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages soft links and mounted collections in iRODS. This access object can
 * be used to define and manipulate mounted collections. Note that mounted
 * collections are then accessed using the normal iRODS operations found
 * elsewhere in the API (e.g. get, put list operations)
 * <p/>
 * This access object implements various operations that are accomplished using
 * the imcoll icommand: https://www.irods.org/index.php/imcoll
 * <p/>
 * See also: https://www.irods.org/index.php/Mounted_iRODS_Collection
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class MountedCollectionAOImpl extends IRODSGenericAO implements
		MountedCollectionAO {

	public static final Logger log = LoggerFactory
			.getLogger(MountedCollectionAOImpl.class);

	/**
	 * Default constructor to be called by the {@link IRODSAccessObjectFactory}
	 * 
	 * @param irodsSession
	 *            {@link IRODSSession} that manages connections to iRODS
	 * @param irodsAccount
	 *            {@link IRODSAccount} that defines the server connection
	 * @throws FileNotFoundException
	 *             if the file to be mounted does not exist
	 * @throws JargonException
	 */
	protected MountedCollectionAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.MountedCollectionAO#unmountACollection(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public boolean unmountACollection(
			final String absolutePathToCollectionToUnmount,
			final String resourceName) throws JargonException {
		log.info("unmountACollection()");

		if (absolutePathToCollectionToUnmount == null
				|| absolutePathToCollectionToUnmount.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absolutePathToCollectionToUnmount");
		}

		if (resourceName == null) {
			throw new IllegalArgumentException(
					"null resource name, set to blank if not specified");
		}

		boolean success = true;

		log.info("absolutePathToCollectionToUnmount:{}",
				absolutePathToCollectionToUnmount);

		log.info("resourceName:{}", resourceName);

		DataObjInpForUnmount dataObjInp = DataObjInpForUnmount
				.instanceForUnmount(absolutePathToCollectionToUnmount,
						resourceName);

		try {
			getIRODSProtocol().irodsFunction(dataObjInp);
		} catch (DataNotFoundException e) {
			success = false;
		} catch (CollectionNotMountedException e) {
			success = false;
		}

		log.debug("unmount complete, success?:{}", success);

		/*
		 * There seems to be an issue with un-mounting a collection and then
		 * doing things with it. Even if the unmount is successful, a successive
		 * mount command will return a -79000 error. This overhead of this
		 * effect is meant to smooth over this effect. If you are reading this
		 * intently then this close is messing something up for you! Post
		 * something to IRODS Chat if this trips you up, it's meant to be a
		 * sensible default.
		 * 
		 * The call to close below should not really be visible to the caller,
		 * as subsequent operations will just open a new one!
		 */
		getIRODSAccessObjectFactory().closeSession(getIRODSAccount());
		return success;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.MountedCollectionAO#createASoftLink(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public void createASoftLink(
			final String absolutePathToTheIRODSCollectionToBeMounted,
			final String absolutePathToLinkedCollectionToBeCreated)
			throws FileNotFoundException, CollectionNotEmptyException,
			JargonException {

		log.info("createASoftLink()");

		if (absolutePathToTheIRODSCollectionToBeMounted == null
				|| absolutePathToTheIRODSCollectionToBeMounted.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absolutePathToTheIRODSCollectionToBeMounted");
		}

		if (absolutePathToLinkedCollectionToBeCreated == null
				|| absolutePathToLinkedCollectionToBeCreated.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absolutePathToLinkedCollectionToBeCreated");
		}

		log.info("absolutePathToTheIRODSCollectionToBeMounted:{}",
				absolutePathToTheIRODSCollectionToBeMounted);
		log.info("absolutePathToLinkedCollectionToBeCreated:{}",
				absolutePathToLinkedCollectionToBeCreated);

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());

		log.info("getting objstat for collection to be mounted...");

		ObjStat statForCollectionToBeMounted = listAndSearchAO
				.retrieveObjectStatForPath(absolutePathToTheIRODSCollectionToBeMounted);

		// a file not found exception will have occurred if the source was not
		// there

		log.info("statForCollectionToBeMounted:{}",
				statForCollectionToBeMounted);

		// is this an irods collection?

		if (statForCollectionToBeMounted.getObjectType() != ObjectType.COLLECTION) {
			log.error(
					"object to be mounted is not an iRODS collection, is type:{}",
					statForCollectionToBeMounted.getObjectType());
			throw new JargonException(
					"object to be mounted is not an iRODS collection, mount failed");
		}

		/*
		 * The target directory must exist as an iRODS collection
		 */

		log.info("getting objstat for collection to be created...");

		try {
			ObjStat statForCollectionTarget = listAndSearchAO
					.retrieveObjectStatForPath(absolutePathToLinkedCollectionToBeCreated);
			if (statForCollectionTarget.getObjectType() != ObjectType.COLLECTION) {
				log.error("target is not an iRODS collection, is type:{}",
						statForCollectionTarget.getObjectType());
				throw new JargonException(
						"link target is not an iRODS collection, mount failed");
			}
		} catch (FileNotFoundException fnf) {
			log.info("file was not found, go ahead and create this collection");
			IRODSFile targetCollection = getIRODSFileFactory()
					.instanceIRODSFile(
							absolutePathToLinkedCollectionToBeCreated);
			targetCollection.mkdirs();

		}

		log.info("all is well, make the call to mount the soft link...");
		DataObjInpForMcoll dataObjInp = DataObjInpForMcoll
				.instanceForSoftLinkMount(
						absolutePathToTheIRODSCollectionToBeMounted,
						absolutePathToLinkedCollectionToBeCreated,
						getIRODSAccount().getDefaultStorageResource());

		getIRODSProtocol().irodsFunction(dataObjInp);
		log.debug("soft link creation successful");

	}

	public void removeMountedWorkflowAndWorkflowFile(
			final String absolutePathToMssFile,
			final String absolutePathToWorkflowCollectionToBeUnmounted)
			throws JargonException {
		log.info("removeMountedWorkflowAndWorkflowFile(final String absolutePathToMssFile, final String absolutePathToWorkflowCollectionToBeUnmounted)");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.MountedCollectionAO#
	 * createMountedFileSystemCollection(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	@Overheaded
	// [#1634] Strange erroneous DataNotFoundExceptions mounting a file system
	public void createMountedFileSystemCollection(
			final String absolutePhysicalPathOnServer,
			final String absoluteIRODSTargetPathToBeMounted,
			final String storageResource) throws CollectionNotMountedException,
			FileNotFoundException, JargonException {

		log.info("createMountedFileSystemCollection()");

		if (absolutePhysicalPathOnServer == null
				|| absolutePhysicalPathOnServer.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absolutePhysicalPathOnServer");
		}

		if (absoluteIRODSTargetPathToBeMounted == null
				|| absoluteIRODSTargetPathToBeMounted.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absoluteIRODSTargetPathToBeMounted");
		}

		if (storageResource == null || storageResource.isEmpty()) {
			throw new IllegalArgumentException("null or empty storageResource");
		}

		log.info("absolutePhysicalPathOnServer:{}",
				absolutePhysicalPathOnServer);
		log.info("absoluteIRODSTargetPathToBeMounted:{}",
				absoluteIRODSTargetPathToBeMounted);

		getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());

		log.info("making the directory for the mount if not exists...");
		IRODSFile mountColl = getIRODSFileFactory().instanceIRODSFile(
				absoluteIRODSTargetPathToBeMounted);
		mountColl.mkdirs();
		log.info("...dirs made");

		log.info("all is well, make the call to mount file system...");
		DataObjInpForMcoll dataObjInp = DataObjInpForMcoll
				.instanceForFileSystemMount(absolutePhysicalPathOnServer,
						absoluteIRODSTargetPathToBeMounted, storageResource);

		try {
			getIRODSProtocol().irodsFunction(dataObjInp);
		} catch (DataNotFoundException dnf) {
			log.warn(
					"Data not found exception?  Seems to be a bug in iRODS...",
					dnf);
			log.warn("overheaded for  [#1634] Strange erroneous DataNotFoundExceptions mounting a file system");
			CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = this
					.getIRODSAccessObjectFactory()
					.getCollectionAndDataObjectListAndSearchAO(
							getIRODSAccount());

			log.warn("got data not found, do a compensating lookup, will throw FNF if the file really is not found");
			ObjStat objStat = collectionAndDataObjectListAndSearchAO
					.retrieveObjectStatForPath(absoluteIRODSTargetPathToBeMounted);
			log.info("really did find the file:{}", objStat);

		} catch (UnixFileMkdirException e) {
			log.error(
					"unix file level mkdir error, will wrap in collection not mounted exception",
					e);
			throw new CollectionNotMountedException(e);
		}
		log.debug("file system mount successful");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.MountedCollectionAO#createAnMSSOMount(java.
	 * lang.String, java.lang.String)
	 */
	@Override
	public void createAnMSSOMountForWorkflow(
			final String absoluteLocalPathToWorkflowFile,
			final String absoluteIRODSTargetPathToTheWssToBeMounted,
			final String absolutePathToMountedCollection)
			throws FileNotFoundException, JargonException {

		log.info("createAnMSSOMount()");

		if (absoluteLocalPathToWorkflowFile == null
				|| absoluteLocalPathToWorkflowFile.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absoluteLocalPathToWssFile");
		}

		if (absoluteIRODSTargetPathToTheWssToBeMounted == null
				|| absoluteIRODSTargetPathToTheWssToBeMounted.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absoluteIRODSTargetPathToTheWssToBeMounted");
		}

		if (absolutePathToMountedCollection == null
				|| absolutePathToMountedCollection.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absolutePathToMountedCollection");
		}

		log.info("absoluteLocalPathToWssFile:{}",
				absoluteLocalPathToWorkflowFile);
		log.info("absoluteIRODSTargetPathToTheWssToBeMounted:{}",
				absoluteIRODSTargetPathToTheWssToBeMounted);
		log.info("absolutePathToMountedCollection:{}",
				absolutePathToMountedCollection);

		log.info("putting the wss file as type msso");

		TransferControlBlock tcb = buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setPutOption(PutOptions.MSSO_FILE);

		DataTransferOperations dto = getIRODSAccessObjectFactory()
				.getDataTransferOperations(getIRODSAccount());

		dto.putOperation(absoluteLocalPathToWorkflowFile,
				absoluteIRODSTargetPathToTheWssToBeMounted, getIRODSAccount()
						.getDefaultStorageResource(), null, tcb);

		log.info("wss file is put as an msso file...now mount the collection");

		getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());

		log.info("making the directory for the mount if not exists...");
		IRODSFile mountColl = getIRODSFileFactory().instanceIRODSFile(
				absolutePathToMountedCollection);
		mountColl.mkdirs();
		log.info("...dirs made");

		log.info("all is well, make the call to mount the soft link...");
		DataObjInpForMcoll dataObjInp = DataObjInpForMcoll
				.instanceForMSSOMount(
						absoluteIRODSTargetPathToTheWssToBeMounted,
						absolutePathToMountedCollection, getIRODSAccount()
								.getDefaultStorageResource());

		getIRODSProtocol().irodsFunction(dataObjInp);
		log.debug("MSSO creation successful");

	}

}
