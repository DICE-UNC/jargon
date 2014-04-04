/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.usertagging.starring.IRODSStarringService;
import org.irods.jargon.usertagging.starring.IRODSStarringServiceImpl;
import org.irods.jargon.vircoll.VirtualCollectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory implementation for virtual collections.
 * 
 * @author mikeconway
 * 
 */
public class VirtualCollectionFactoryImpl implements VirtualCollectionFactory {

	static Logger log = LoggerFactory
			.getLogger(VirtualCollectionFactoryImpl.class);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.impl.VirtualCollectionFactory#
	 * getVirtualCollectionContext()
	 */
	@Override
	public VirtualCollectionContext getVirtualCollectionContext() {
		return virtualCollectionContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.impl.VirtualCollectionFactory#
	 * instanceCollectionBasedVirtualCollection(java.lang.String)
	 */
	@Override
	public CollectionBasedVirtualCollection instanceCollectionBasedVirtualCollection(
			final String parentPath) {
		assert hasValidState();

		if (parentPath == null || parentPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty parentPath");
		}
		CollectionBasedVirtualCollection coll = new CollectionBasedVirtualCollection(
				parentPath);
		coll.setContext(this.getVirtualCollectionContext());
		return coll;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.impl.VirtualCollectionFactory#
	 * instanceStarredFolderVirtualCollection()
	 */
	@Override
	public StarredFoldersVirtualCollection instanceStarredFolderVirtualCollection() {
		assert hasValidState();

		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				virtualCollectionContext.getIrodsAccessObjectFactory(),
				virtualCollectionContext.getIrodsAccount());
		StarredFoldersVirtualCollection coll = new StarredFoldersVirtualCollection(
				irodsStarringService);
		coll.setContext(this.getVirtualCollectionContext());
		return coll;
	}

	public IRODSAccount getIrodsAccount() {
		assert hasValidState();
		return virtualCollectionContext.getIrodsAccount();
	}

	private boolean hasValidState() {
		return (virtualCollectionContext != null);
	}
}
