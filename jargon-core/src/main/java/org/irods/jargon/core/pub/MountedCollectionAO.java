package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.CollectionNotEmptyException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;

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
public interface MountedCollectionAO {

	/**
	 * Create a soft link to the given iRODS collection. This mirrors the imcoll
	 * command with the -l option
	 * <p/>
	 * If the mountType is 'l' or 'link', the request is for a collection soft
	 * link. the first argument is the iRODS collection to be linked or the
	 * target collection name. The second argument is the link collection name
	 * The link collection must not exist or must be an empty collection
	 * 
	 * @param absolutePathToTheIRODSCollectionToBeMounted
	 *            <code>String</code> with the absolute path to an existing
	 *            iRODS collection that will be soft linked at the name provided
	 *            in the second parameter. This must exist and be a collection.
	 * @param absolutePathToLinkedCollectionToBeCreated
	 *            <code>String</code> with the absolute path to the iRODS
	 *            collection that will be created, pointing to the soft link. If
	 *            this does not exist, it will be created.
	 * 
	 * @throws FileNotFoundException
	 *             occurs if the
	 *             <code>absolutePathToTheIRODSCollectionToBeMounted</code> does
	 *             not exist
	 * @throws CollectionNotEmptyException
	 *             occurs if the collection for the created link is not empty
	 * @throws JargonException
	 */
	void createASoftLink(
			final String absolutePathToTheIRODSCollectionToBeMounted,
			final String absolutePathToLinkedCollectionToBeCreated)
			throws FileNotFoundException, CollectionNotEmptyException,
			JargonException;

	/**
	 * Unmount the collection at the given absolute path
	 * 
	 * @param absolutePathToCollectionToUnmount
	 *            <code>String</code> with the absolute path to the collection
	 *            to be unmounted
	 * @param resourceName
	 *            <code>String</code> with the optional (blank if not nused)
	 *            resource name
	 * @return <code>boolean</code> will return <code>true</code> if unmounted,
	 *         <code>false</code> if the collection to unmount was not found
	 * @throws JargonException
	 */
	boolean unmountACollection(String absolutePathToCollectionToUnmount,
			String resourceName) throws JargonException;

}