package org.irods.jargon.datautils.sharing;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFileSystemAOHelper;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
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
		AbstractDataUtilsServiceImpl implements SharedFilesAndCollectionsSearchAndListService {

	private static final char COMMA = ',';

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

		StringBuilder sb = new StringBuilder(
				buildSelectsForListAllDataObjectsSharedWithAGivenUser());
		sb.append(" WHERE ");

		// add owner to query

		if (isOwnerInSearch) {
			sb.append(RodsGenQueryEnum.COL_D_OWNER_NAME.getName());
			sb.append(" = '");
			sb.append(ownerName.trim());
			sb.append("' ");
		}

		if (isOwnerInSearch && isUserInSearch) {
			sb.append(" AND ");
		}

		if (isUserInSearch) {
			sb.append(RodsGenQueryEnum.COL_DATA_ACCESS_USER_ID.getName());
			sb.append(" = '");
			sb.append(sharedWithName.trim());
			sb.append("' ");
		}

		sb.append(" AND ");

		sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		sb.append(" LIKE '");
		sb.append(IRODSDataConversionUtil
				.escapeSingleQuotes(searchStartAbsolutePath.trim()));
		sb.append("%'");

		return null;

	}

	public static String buildSelectsForListAllDataObjectsSharedWithAGivenUser()
			throws JargonException {

		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT ");
		query.append(IRODSFileSystemAOHelper.buildDataObjectQuerySelects());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_ACCESS_USER_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_ACCESS_TYPE.getName());

		/*
		 * query.append(" WHERE ");
		 * query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		 * query.append(" LIKE '");
		 * query.append(IRODSDataConversionUtil.escapeSingleQuotes
		 * (path.trim())); query.append('%'); query.append("' AND ");
		 * query.append(RodsGenQueryEnum.COL_COLL_ACCESS_USER_NAME.getName());
		 * query.append(" = '"); query.append(userName.trim());
		 * query.append("'");
		 */
		return query.toString();

	}

}
