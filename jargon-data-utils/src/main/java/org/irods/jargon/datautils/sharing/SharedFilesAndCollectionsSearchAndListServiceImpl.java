package org.irods.jargon.datautils.sharing;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.datautils.AbstractDataUtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Services for searching and listing files and collections based on sharing
 * relationships.
 * 
 * @author Mike Conway - DICE (www.irods.org) NOTE: work in progress, API very
 *         subject to change!
 */
public class SharedFilesAndCollectionsSearchAndListServiceImpl extends AbstractDataUtilsService {

	/**
	 * Constructor with required dependencies
	 * @param irodsAccessObjectFactory {@link IRODSAccessObjectFactory} that can create necessary objects
	 * @param irodsAccount {@link IRODSAccount} that contains the login information
	 */
	public SharedFilesAndCollectionsSearchAndListServiceImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	public static final Logger log = LoggerFactory
			.getLogger(SharedFilesAndCollectionsSearchAndListServiceImpl.class);

	public List<CollectionAndDataObjectListingEntry> listDataObjectsSharedWithUserByOwner(
			String searchStartAbsolutePath, final String ownerName,
			final String sharedWithName) throws JargonException {
		log.info("listDataObjectsSharedWithUserByOwner");
		
		this.checkContracts();

		// if the absolute path is null or not provided, just use root

		if (searchStartAbsolutePath == null
				|| searchStartAbsolutePath.isEmpty()) {
			searchStartAbsolutePath = "/";
		}

		// if they've specified an owner name, make it part of the search,
		// otherwise, it will be any file shared with the target user

		boolean isOwnerInSearch = true;

		if (ownerName == null || ownerName.isEmpty()) {
			log.info("owner is not in search");
			isOwnerInSearch = false;
		}

		// if they've specified a shared-with user name, it will be in the
		// search

		boolean isUserInSearch = true;

		if (sharedWithName == null || sharedWithName.isEmpty()) {
			// user is not in search
			isUserInSearch = false;
		}

		// if they've specified neither ower or shared with name, don't search
		// (it would just list all files)

		if (!isOwnerInSearch && !isUserInSearch) {
			throw new IllegalArgumentException(
					"search specified neither owner or shared-with user");
		}

		return null;

	}

}
