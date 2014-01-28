/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import org.irods.jargon.usertagging.starring.IRODSStarringService;
import org.irods.jargon.usertagging.starring.IRODSStarringServiceImpl;
import org.irods.jargon.vircoll.VirtualCollectionContext;

/**
 * Factory implementation for virtual collections.
 * 
 * @author mikeconway
 * 
 */
public class VirtualCollectionFactoryImpl {

	/**
	 * Required dependency on the context for obtaining virtual collections
	 */
	private final VirtualCollectionContext virtualCollectionContext;

	/**
	 * Create a new factory for virtual collections
	 * 
	 * @param virtualCollectionContext
	 *            {@link VirtualCollectionContext} that describes the
	 *            environment used to obtain virtual collections
	 */
	public VirtualCollectionFactoryImpl(
			final VirtualCollectionContext virtualCollectionContext) {
		if (virtualCollectionContext == null) {
			throw new IllegalArgumentException("null virtualCollectionContext");
		}
		this.virtualCollectionContext = virtualCollectionContext;
	}

	public VirtualCollectionContext getVirtualCollectionContext() {
		return virtualCollectionContext;
	}

	/**
	 * Create a virtual collection that actually just wraps an underlying irods
	 * collection
	 * 
	 * @param parentPath
	 *            <code>String</code> with the absolute path to an iRODS parent
	 *            collection
	 * @return {@link CollectionBasedVirtualCollection}
	 */
	public CollectionBasedVirtualCollection instanceCollectionBasedVirtualCollection(
			final String parentPath) {

		if (parentPath == null || parentPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty parentPath");
		}
		CollectionBasedVirtualCollection coll = new CollectionBasedVirtualCollection(
				parentPath);
		coll.setContext(this.getVirtualCollectionContext());
		return coll;
	}

	/**
	 * Create a virtual collection of starred files and folders
	 * 
	 * @param parentPath
	 * @return
	 */
	public StarredFoldersVirtualCollectionImpl instanceStarredFolderVirtualCollection(
			final String parentPath) {

		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				virtualCollectionContext.getIrodsAccessObjectFactory(),
				virtualCollectionContext.getIrodsAccount());
		StarredFoldersVirtualCollectionImpl coll = new StarredFoldersVirtualCollectionImpl(
				irodsStarringService);
		coll.setContext(this.getVirtualCollectionContext());
		return coll;
	}
}
