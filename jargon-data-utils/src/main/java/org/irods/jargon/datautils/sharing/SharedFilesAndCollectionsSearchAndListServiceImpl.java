package org.irods.jargon.datautils.sharing;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFileSystemAOHelper;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.datautils.AbstractDataUtilsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Services for searching and listing files and collections based on sharing
 * relationships.
 * 
 * @author Mike Conway - DICE (www.irods.org) NOTE: work in progress, API very
 *         subject to change!
 */
/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SharedFilesAndCollectionsSearchAndListServiceImpl extends
		AbstractDataUtilsServiceImpl implements
		SharedFilesAndCollectionsSearchAndListService {

	public static final Logger log = LoggerFactory
			.getLogger(SharedFilesAndCollectionsSearchAndListServiceImpl.class);

	/**
	 * Constructor with required dependencies
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} that can create necessary
	 *            objects
	 * @param irodsAccount
	 *            {@link IRODSAccount} that contains the login information
	 */
	public SharedFilesAndCollectionsSearchAndListServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.sharing.
	 * SharedFilesAndCollectionsSearchAndListService
	 * #listDataObjectsSharedWithUserByOwner(java.lang.String, java.lang.String,
	 * java.lang.String, int)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> listDataObjectsSharedWithUserByOwner(
			String searchStartAbsolutePath, final String ownerName,
			final String sharedWithName, final int partialStartIndex)
			throws JargonException {

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

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		buildSelectsForListAllDataObjectsSharedWithAGivenUser(builder);

		// add owner to query

		if (isOwnerInSearch) {
			builder.addConditionAsGenQueryField(
					RodsGenQueryEnum.COL_D_OWNER_NAME,
					QueryConditionOperators.EQUAL, ownerName);
		}

		if (isUserInSearch) {
			builder.addConditionAsGenQueryField(
					RodsGenQueryEnum.COL_DATA_ACCESS_USER_ID,
					QueryConditionOperators.EQUAL, sharedWithName);
		}

		builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
				QueryConditionOperators.LIKE, searchStartAbsolutePath + "%");
		return null;

	}

	public static void buildSelectsForListAllDataObjectsSharedWithAGivenUser(
			final IRODSGenQueryBuilder builder) throws JargonException {

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		try {
			IRODSFileSystemAOHelper.buildDataObjectQuerySelects(builder);
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_DATA_ACCESS_USER_ID)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_DATA_ACCESS_TYPE);
		} catch (GenQueryBuilderException e) {
			throw new JargonException(e);
		}

	}

}
