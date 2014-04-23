package org.irods.jargon.vircoll;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollectionExecutor;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollectionExecutor;

/**
 * Represents a factory service that can find and initialize lists of virtual
 * collections, as well as create new instances of a virtual collection type
 * 
 * @author Mike Conway - DICE
 * 
 */
public interface VirtualCollectionExecutorFactory {

	/**
	 * Create a virtual collection of starred files and folders
	 * 
	 * @param parentPath
	 * @return
	 */
	StarredFoldersVirtualCollectionExecutor instanceStarredFolderVirtualCollection();

	/**
	 * Create a virtual collection executor that actually just wraps an
	 * underlying irods collection
	 * 
	 * @param uniqueName
	 *            <code>String</code> with the unique name of the virtual
	 *            collection
	 * @param parentPath
	 *            <code>String</code> with the absolute path to an iRODS parent
	 *            collection
	 * @return {@link CollectionBasedVirtualCollectionExecutor} that can execute
	 *         the query to produce the collection listing data
	 */
	CollectionBasedVirtualCollectionExecutor instanceCollectionBasedVirtualCollectionExecutor(
			String uniqueName, String parentPath);

	/**
	 * Given some form of virtual collection, return the associated executor
	 * 
	 * @param virtualCollection
	 *            {@link AbstractVirtualCollection} subtype
	 * @return associated {@link AbstractVirtualCollectionExecutor}
	 * @throws DataNotFoundException
	 * @throws JargonException
	 */
	@SuppressWarnings("rawtypes")
	AbstractVirtualCollectionExecutor instanceExecutorBasedOnVirtualCollection(
			final AbstractVirtualCollection virtualCollection)
			throws DataNotFoundException, JargonException;

}