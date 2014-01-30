/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.usertagging.starring.IRODSStarringService;
import org.irods.jargon.usertagging.starring.IRODSStarringServiceImpl;
import org.irods.jargon.vircoll.AbstractVirtualCollection;
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
	 * listDefaultUserCollections()
	 */
	@Override
	public List<AbstractVirtualCollection> listDefaultUserCollections() {
		log.info("listDefaultUserCollections()");
		assert hasValidState();

		List<AbstractVirtualCollection> virtualCollections = new ArrayList<AbstractVirtualCollection>();
		// add root
		virtualCollections.add(instanceCollectionBasedVirtualCollection("/"));
		// add user dir
		virtualCollections
				.add(instanceCollectionBasedVirtualCollection(MiscIRODSUtils
						.computeHomeDirectoryForIRODSAccount(this.virtualCollectionContext
								.getIrodsAccount())));
		// add starred folders
		virtualCollections.add(instanceStarredFolderVirtualCollection());
		log.info("done...");
		return virtualCollections;

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
