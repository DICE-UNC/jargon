package org.irods.jargon.vircoll.impl;

import org.irods.jargon.vircoll.VirtualCollectionContext;

/**
 * Represents a factory service that can find and initialize lists of virtual
 * collections, as well as create new instances of a virtual collection type
 * 
 * @author mikeconway
 * 
 */
public interface VirtualCollectionFactory {

	public abstract VirtualCollectionContext getVirtualCollectionContext();

	/**
	 * Create a virtual collection that actually just wraps an underlying irods
	 * collection
	 * 
	 * @param parentPath
	 *            <code>String</code> with the absolute path to an iRODS parent
	 *            collection
	 * @return {@link CollectionBasedVirtualCollection}
	 */
	public abstract CollectionBasedVirtualCollection instanceCollectionBasedVirtualCollection(
			String parentPath);

	/**
	 * Create a virtual collection of starred files and folders
	 * 
	 * @param parentPath
	 * @return
	 */
	public abstract StarredFoldersVirtualCollection instanceStarredFolderVirtualCollection();

}