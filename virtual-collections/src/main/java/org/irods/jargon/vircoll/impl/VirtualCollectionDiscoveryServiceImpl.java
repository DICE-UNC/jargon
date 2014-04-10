/**
 *
 */
package org.irods.jargon.vircoll.impl;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.vircoll.AbstractVirtualCollection;
import org.irods.jargon.vircoll.VirtualCollectionDiscoveryService;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollection;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for discovring and listing virtual collections (as opposed to listing
 * their contents). This can discover them, and list them as pojos that are
 * suitible for listing
 *
 * @author Mike Conway (DICE)
 *
 */
public class VirtualCollectionDiscoveryServiceImpl extends
		AbstractJargonService implements VirtualCollectionDiscoveryService {

	private static Logger log = LoggerFactory
			.getLogger(VirtualCollectionDiscoveryServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public VirtualCollectionDiscoveryServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 *
	 */
	public VirtualCollectionDiscoveryServiceImpl() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.impl.VirtualCollectionFactory#
	 * listDefaultUserCollections()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.impl.VirtualCollectionMaintenanceService#
	 * listDefaultUserCollections()
	 */
	@Override
	public List<AbstractVirtualCollection> listDefaultUserCollections() {
		log.info("listDefaultUserCollections()");

		List<AbstractVirtualCollection> virtualCollections = new ArrayList<AbstractVirtualCollection>();
		// add root
		virtualCollections
				.add(new CollectionBasedVirtualCollection("root", "/"));
		// add user dir
		virtualCollections
				.add(new CollectionBasedVirtualCollection(
						"home",
						MiscIRODSUtils
								.computeHomeDirectoryForIRODSAccount(getIrodsAccount())));
		// add starred folders
		virtualCollections.add(new StarredFoldersVirtualCollection());
		log.info("done...");
		return virtualCollections;

	}

}
