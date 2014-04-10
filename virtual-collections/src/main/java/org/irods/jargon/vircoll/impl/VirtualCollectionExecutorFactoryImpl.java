/**
 *
 */
package org.irods.jargon.vircoll.impl;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.usertagging.starring.IRODSStarringService;
import org.irods.jargon.usertagging.starring.IRODSStarringServiceImpl;
import org.irods.jargon.vircoll.VirtualCollectionExecutorFactory;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollection;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollectionExecutor;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollection;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollectionExecutor;
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
			final String uniqueName, final String parentPath) {

		if (uniqueName == null || uniqueName.isEmpty()) {
			throw new IllegalArgumentException("null or empty uniqueName");
		}

		if (parentPath == null || parentPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty parentPath");
		}

		CollectionBasedVirtualCollection coll = new CollectionBasedVirtualCollection(
				uniqueName, parentPath);

		return new CollectionBasedVirtualCollectionExecutor(coll,
				getIrodsAccessObjectFactory(), getIrodsAccount());
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
				getIrodsAccessObjectFactory(), getIrodsAccount());
		return new StarredFoldersVirtualCollectionExecutor(coll,
				getIrodsAccessObjectFactory(), getIrodsAccount(),
				irodsStarringService);
	}

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public VirtualCollectionExecutorFactoryImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}
}
