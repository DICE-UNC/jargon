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

/**
 * Service for listing and maintaining virtual collections. This can discover
 * them, and ask a virtual collection to serialize itself, and is different than
 * getting an operational virtual collection so that it can be queried.
 * 
 * @author Mike Conway (DICE)
 * 
 */
public class VirtualCollectionMaintenanceServiceImpl extends
		AbstractJargonService implements VirtualCollectionMaintenanceService {

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public VirtualCollectionMaintenanceServiceImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * 
	 */
	public VirtualCollectionMaintenanceServiceImpl() {
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

}
