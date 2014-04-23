/**
 *
 */
package org.irods.jargon.vircoll.types;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.usertagging.domain.IRODSStarredFileOrCollection;
import org.irods.jargon.usertagging.starring.IRODSStarringService;
import org.irods.jargon.vircoll.AbstractVirtualCollectionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a virtual collection of starred folders
 * 
 * @author mikeconway
 * 
 */
public class StarredFoldersVirtualCollectionExecutor extends
		AbstractVirtualCollectionExecutor<StarredFoldersVirtualCollection> {

	private final IRODSStarringService irodsStarringService;

	static Logger log = LoggerFactory
			.getLogger(StarredFoldersVirtualCollectionExecutor.class);

	/**
	 * Create an instance of an executor for starred folders
	 * 
	 * @param starredFoldersVirtualCollection
	 *            {@link StarredFoldersVirtualCollection} that describes the
	 *            collection
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} used to connect to iRODS
	 * @param irodsAccount
	 *            {@link IRODSAccount} with host and login information
	 */
	public StarredFoldersVirtualCollectionExecutor(
			final StarredFoldersVirtualCollection starredFoldersVirtualCollection,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final IRODSStarringService irodsStarringService) {
		super(starredFoldersVirtualCollection, irodsAccessObjectFactory,
				irodsAccount);

		if (irodsStarringService == null) {
			throw new IllegalArgumentException("null irodsStarringService");
		}

		this.irodsStarringService = irodsStarringService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.AbstractVirtualCollection#queryAll(int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.vircoll.impl.StarredFoldersVirtualCollection#queryAll
	 * (int)
	 */
	@Override
	public PagingAwareCollectionListing queryAll(final int offset)
			throws JargonException {

		PagingAwareCollectionListing listing = buildInitialPagingAwareCollectionListing();
		log.info("queryAll()");
		log.info("adding colls");
		this.addAndCharacterizeCollectionListingForSplitListing(listing,
				queryCollections(0));
		log.info("adding data objects");
		this.addAndCharacterizeDataObjectListingForSplitListing(listing,
				queryDataObjects(0));
		return listing;

	}

	private List<CollectionAndDataObjectListingEntry> queryCollections(
			final int offset) throws JargonException {

		List<IRODSStarredFileOrCollection> starred = irodsStarringService
				.listStarredCollections(offset);

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		log.info("have entries, now format");

		CollectionAndDataObjectListingEntry entry;
		for (IRODSStarredFileOrCollection coll : starred) {
			entry = new CollectionAndDataObjectListingEntry();
			entry.setCount(coll.getCount());
			entry.setDataSize(0);
			entry.setLastResult(coll.isLastResult());
			entry.setObjectType(ObjectType.COLLECTION);
			CollectionAndPath collAndPath = MiscIRODSUtils
					.separateCollectionAndPathFromGivenAbsolutePath(coll
							.getDomainUniqueName());
			entry.setParentPath(collAndPath.getCollectionParent());
			entry.setPathOrName(coll.getDomainUniqueName());
			entry.setDescription(coll.getDescription());
			entry.setTotalRecords(coll.getTotalRecords());
			entries.add(entry);
		}

		return entries;

	}

	private List<CollectionAndDataObjectListingEntry> queryDataObjects(
			final int offset) throws JargonException {

		List<IRODSStarredFileOrCollection> starred = irodsStarringService
				.listStarredDataObjects(offset);

		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		log.info("have entries, now format");

		CollectionAndDataObjectListingEntry entry;
		for (IRODSStarredFileOrCollection coll : starred) {
			entry = new CollectionAndDataObjectListingEntry();
			entry.setCount(coll.getCount());
			entry.setDataSize(0);
			entry.setLastResult(coll.isLastResult());
			entry.setObjectType(ObjectType.DATA_OBJECT);
			CollectionAndPath collAndPath = MiscIRODSUtils
					.separateCollectionAndPathFromGivenAbsolutePath(coll
							.getDomainUniqueName());
			entry.setParentPath(collAndPath.getCollectionParent());
			entry.setDescription(collAndPath.getChildName());
			entries.add(entry);
		}

		return entries;
	}

}
