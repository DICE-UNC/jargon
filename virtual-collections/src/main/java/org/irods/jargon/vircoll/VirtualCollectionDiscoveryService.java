package org.irods.jargon.vircoll;

import java.util.List;

public interface VirtualCollectionDiscoveryService {

	/**
	 * Create a list of virtual collection types for a user. This will include
	 * the default set (root, user home, starred, shared) as well as custom
	 * configured virtual collections in the user home directory
	 *
	 * @return
	 */
	public List<AbstractVirtualCollection> listDefaultUserCollections();

}