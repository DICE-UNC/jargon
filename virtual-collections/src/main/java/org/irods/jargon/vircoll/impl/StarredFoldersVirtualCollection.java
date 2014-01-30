/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.usertagging.domain.IRODSStarredFileOrCollection;
import org.irods.jargon.usertagging.starring.IRODSStarringService;
import org.irods.jargon.vircoll.AbstractVirtualCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a virtual collection of starred folders
 * 
 * @author mikeconway
 * 
 */
public class StarredFoldersVirtualCollection extends AbstractVirtualCollection {

	private final IRODSStarringService irodsStarringService;

	static Logger log = LoggerFactory
			.getLogger(StarredFoldersVirtualCollection.class);

	public static final String DESCRIPTION_KEY = "virtual.collections.starred";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.AbstractVirtualCollection#store()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.vircoll.impl.StarredFoldersVirtualCollection#store()
	 */
	@Override
	public void store() throws JargonException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.AbstractVirtualCollection#delete()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.vircoll.impl.StarredFoldersVirtualCollection#delete()
	 */
	@Override
	public void delete() throws JargonException {
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
	public List<CollectionAndDataObjectListingEntry> queryAll(int offset)
			throws JargonException {

		log.info("queryAll()");
		List<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();
		log.info("adding colls");
		entries.addAll(queryCollections(0));
		log.info("adding data objectx");
		entries.addAll(queryDataObjects(0));
		return entries;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.vircoll.AbstractVirtualCollection#queryCollections(int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.impl.StarredFoldersVirtualCollection#
	 * queryCollections(int)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> queryCollections(int offset)
			throws JargonException {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.vircoll.AbstractVirtualCollection#queryDataObjects(int)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.impl.StarredFoldersVirtualCollection#
	 * queryDataObjects(int)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> queryDataObjects(int offset)
			throws JargonException {

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

	/**
	 * @param irodsStarringService
	 *            {@link IRODSStarringService} that is connected to iRODS
	 */
	StarredFoldersVirtualCollection(IRODSStarringService irodsStarringService) {
		super();
		if (irodsStarringService == null) {
			throw new IllegalArgumentException("null irodsStarringService");
		}
		this.irodsStarringService = irodsStarringService;
		this.setDescription(DESCRIPTION_KEY);
	}
}
