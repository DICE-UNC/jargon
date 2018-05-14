package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.CollectionNotEmptyException;
import org.irods.jargon.core.exception.CollectionNotMountedException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Manages soft links and mounted collections in iRODS. This access object can
 * be used to define and manipulate mounted collections. Note that mounted
 * collections are then accessed using the normal iRODS operations found
 * elsewhere in the API (e.g. get, put list operations)
 * <p>
 * This access object implements various operations that are accomplished using
 * the imcoll icommand: https://www.irods.org/index.php/imcoll
 * <p>
 * See also: https://www.irods.org/index.php/Mounted_iRODS_Collection
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface MountedCollectionAO {

	/**
	 * Create a soft link to the given iRODS collection. This mirrors the imcoll
	 * command with the -l option
	 * <p>
	 * If the mountType is 'l' or 'link', the request is for a collection soft link.
	 * the first argument is the iRODS collection to be linked or the target
	 * collection name. The second argument is the link collection name The link
	 * collection must not exist or must be an empty collection
	 *
	 * @param absolutePathToTheIRODSCollectionToBeMounted
	 *            {@code String} with the absolute path to an existing iRODS
	 *            collection that will be soft linked at the name provided in the
	 *            second parameter. This must exist and be a collection.
	 * @param absolutePathToLinkedCollectionToBeCreated
	 *            {@code String} with the absolute path to the iRODS collection that
	 *            will be created, pointing to the soft link. If this does not
	 *            exist, it will be created.
	 *
	 * @throws FileNotFoundException
	 *             occurs if the {@code absolutePathToTheIRODSCollectionToBeMounted}
	 *             does not exist
	 * @throws CollectionNotEmptyException
	 *             occurs if the collection for the created link is not empty
	 * @throws JargonException
	 *             for iRODS error
	 * @throws FileNotFoundException
	 *             for no file
	 * @throws CollectionNotEmptyException
	 *             for data already present in soft link target
	 */
	void createASoftLink(final String absolutePathToTheIRODSCollectionToBeMounted,
			final String absolutePathToLinkedCollectionToBeCreated)
			throws FileNotFoundException, CollectionNotEmptyException, JargonException;

	/**
	 * Unmount the collection at the given absolute path
	 *
	 * @param absolutePathToCollectionToUnmount
	 *            {@code String} with the absolute path to the collection to be
	 *            unmounted
	 * @param resourceName
	 *            {@code String} with the optional (blank if not used) resource name
	 * @return {@code boolean} will return {@code true} if unmounted, {@code false}
	 *         if the collection to unmount was not found
	 * @throws JargonException
	 *             for iRODS error
	 */
	boolean unmountACollection(String absolutePathToCollectionToUnmount, String resourceName) throws JargonException;

	/**
	 * Create an MSSO mount using the given MSSO file, mounting the result to the
	 * provided collection path.
	 * <p>
	 * This method takes a local file path to the mso object that will be 'put' to
	 * iRODS as an mso file. Then the given collection is 'mounted' as a WSSO given
	 * the provided path to the desired collection, and the .wss file that was just
	 * 
	 * @param absoluteLocalPathToWssFile
	 *            {@link String} with the absolute path to wss structured object to
	 *            mount
	 * @param absoluteIRODSTargetPathToTheWssToBeMounted
	 *            {@link String}
	 * @param absolutePathToMountedCollection
	 *            {@link String}
	 * @throws FileNotFoundException
	 *             file not found
	 * @throws JargonException
	 *             for iRODS error
	 */
	void createAnMSSOMountForWorkflow(String absoluteLocalPathToWssFile,
			String absoluteIRODSTargetPathToTheWssToBeMounted, String absolutePathToMountedCollection)
			throws FileNotFoundException, JargonException;

	/**
	 * Create a file system mount point in iRODS. Mounting the file system at the
	 * given local absolute path on the server to the given collection.
	 * <p>
	 * Be aware that this is a physical file path on the iRODS server in question,
	 * this does not mount a local (to the client) file system!
	 * <p>
	 * See https://www.irods.org/index.php/Mounted_iRODS_Collection for notes on
	 * mounted collections
	 *
	 * @param absolutePhysicalPathOnServer
	 *            {@code String} with the absolute path to the local file system
	 *            (local to the iRODS server) that is to be mounted.
	 * @param absoluteIRODSTargetPathToBeMounted
	 *            {@code String} with the iRODS absolute path to the new mounted
	 *            collection
	 * @param storageResource
	 *            {@code String} with the required storage resource for the mount
	 * @throws CollectionNotMountedException
	 *             for cases such as duplicate mount points
	 * @throws FileNotFoundException
	 *             when local file path to be mounted is not found
	 * @throws JargonException
	 *             for iRODS error
	 */
	void createMountedFileSystemCollection(String absolutePhysicalPathOnServer,
			String absoluteIRODSTargetPathToBeMounted, String storageResource)
			throws CollectionNotMountedException, FileNotFoundException, JargonException;

}
