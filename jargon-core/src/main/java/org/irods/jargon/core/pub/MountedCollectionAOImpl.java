package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInpForMcoll;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
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
	 * org.irods.jargon.core.pub.MountedCollectionAO#createASoftLink(java.lang
	 * .String, java.lang.String)
	 */

	/*
	 * todo: should I specify a resource in the parms or take the default?
	 */
	@Override
	public void createASoftLink(
			final String absolutePathToTheIRODSCollectionToBeMounted,
			final String absolutePathToLinkedCollectionToBeCreated)
			throws FileNotFoundException, JargonException {

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

		CollectionAndDataObjectListAndSearchAO listAndSearchAO = this
				.getIRODSAccessObjectFactory()
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
			IRODSFile targetCollection = this.getIRODSFileFactory()
					.instanceIRODSFile(
							absolutePathToLinkedCollectionToBeCreated);
			targetCollection.mkdirs();

		}

		log.info("all is well, make the call to mount the soft link...");

		DataObjInpForMcoll dataObjInp = DataObjInpForMcoll
				.instanceForSoftLinkMount(
						absolutePathToTheIRODSCollectionToBeMounted,
						absolutePathToLinkedCollectionToBeCreated, this
								.getIRODSAccount().getDefaultStorageResource());

		getIRODSProtocol().irodsFunction(dataObjInp);

		log.debug("soft link creation successful");

	}

}
