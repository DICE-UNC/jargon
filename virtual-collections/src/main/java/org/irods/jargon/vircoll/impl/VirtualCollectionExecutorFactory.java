package org.irods.jargon.vircoll.impl;

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
	 * @param parentPath
	 *            <code>String</code> with the absolute path to an iRODS parent
	 *            collection
	 * @return {@link CollectionBasedVirtualCollectionExecutor} that can execute
	 *         the query to produce the collection listing data
	 */
	CollectionBasedVirtualCollectionExecutor instanceCollectionBasedVirtualCollectionExecutor(
			String parentPath);

}