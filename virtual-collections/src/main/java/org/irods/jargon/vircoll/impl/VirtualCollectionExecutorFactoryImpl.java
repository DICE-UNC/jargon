/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.usertagging.starring.IRODSStarringService;
import org.irods.jargon.usertagging.starring.IRODSStarringServiceImpl;
import org.irods.jargon.vircoll.StarredFoldersVirtualCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory implementation for virtual collections.
 * 
 * @author Mike Conway - DICE
 * 
 */
public class VirtualCollectionExecutorFactoryImpl extends AbstractJargonService
		implements VirtualCollectionExecutorFactory {

	static Logger log = LoggerFactory
			.getLogger(VirtualCollectionExecutorFactoryImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.impl.VirtualCollectionFactory#
	 * instanceCollectionBasedVirtualCollection(java.lang.String)
	 */
	@Override
	public CollectionBasedVirtualCollectionExecutor instanceCollectionBasedVirtualCollectionExecutor(
			final String parentPath) {

		if (parentPath == null || parentPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty parentPath");
		}
		CollectionBasedVirtualCollection coll = new CollectionBasedVirtualCollection(
				parentPath);

		return new CollectionBasedVirtualCollectionExecutor(coll,
				this.getIrodsAccessObjectFactory(), this.getIrodsAccount());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.impl.VirtualCollectionFactory#
	 * instanceStarredFolderVirtualCollection()
	 */
	@Override
	public StarredFoldersVirtualCollectionExecutor instanceStarredFolderVirtualCollection() {
		StarredFoldersVirtualCollection coll = new StarredFoldersVirtualCollection();
		IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
				this.getIrodsAccessObjectFactory(), this.getIrodsAccount());
		return new StarredFoldersVirtualCollectionExecutor(coll,
				this.getIrodsAccessObjectFactory(), this.getIrodsAccount(),
				irodsStarringService);
	}

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public VirtualCollectionExecutorFactoryImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}
}
