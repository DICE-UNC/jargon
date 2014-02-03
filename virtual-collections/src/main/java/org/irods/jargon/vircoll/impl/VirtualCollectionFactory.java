package org.irods.jargon.vircoll.impl;

import java.util.List;

import org.irods.jargon.vircoll.AbstractVirtualCollection;
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
	 * Create a list of virtual collection types for a user. This will include
	 * the default set (root, user home, starred, shared) as well as custom
	 * configured virtual collections in the user home directory
	 * 
	 * @return
	 */
	public abstract List<AbstractVirtualCollection> listDefaultUserCollections();

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