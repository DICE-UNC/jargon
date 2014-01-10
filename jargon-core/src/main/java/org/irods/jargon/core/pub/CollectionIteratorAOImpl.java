/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an object that iterates over an iRODS collection, listing
 * child objects in a paging aware fashion
 * 
 * @author Mike Conway DICE (www.irods.org)
 * 
 */
public class CollectionIteratorAOImpl extends IRODSGenericAO {

	public static final Logger log = LoggerFactory
			.getLogger(CollectionIteratorAOImpl.class);

	private final CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO;
	private final CollectionListingUtils collectionListingUtils;

	/**
	 * Protected constructor, use the {@link IRODSAccessObjectFactory} to obtain
	 * this object
	 * 
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected CollectionIteratorAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		collectionAndDataObjectListAndSearchAO = getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());
		collectionListingUtils = new CollectionListingUtils(
				collectionAndDataObjectListAndSearchAO);
	}

	public PagingAwareCollectionListing retrivePagingAwareCollectionListing(
			final String absolutePathToParent) throws FileNotFoundException,
			JargonException {

		log.info("retrivePagingAwareCollectionListing()");
		if (absolutePathToParent == null || absolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException(
					"absolutePathToParent is null or empty");
		}

		log.info("absolutePath:{}", absolutePathToParent);

		PagingAwareCollectionListing pagingAwareCollectionListing = new PagingAwareCollectionListing();
		pagingAwareCollectionListing.setPageSizeUtilized(getJargonProperties()
				.getMaxFilesAndDirsQueryMax());
		List<CollectionAndDataObjectListingEntry> entries = null;
		ObjStat objStat = null;

		try {
			objStat = collectionAndDataObjectListAndSearchAO
					.retrieveObjectStatForPath(absolutePathToParent);
		} catch (FileNotFoundException fnf) {
			log.info("didnt find an objStat for the path, account for cases where there are strict acls and give Jargon a chance to drill down to a place where the user has permissions");
			entries = collectionListingUtils
					.handleNoListingUnderRootOrHomeByLookingForPublicAndHome(absolutePathToParent);
			pagingAwareCollectionListing
					.setCollectionAndDataObjectListingEntries(entries);
			pagingAwareCollectionListing.setCollectionsComplete(true);
			pagingAwareCollectionListing.setCollectionsCount(entries.size());
			return pagingAwareCollectionListing;
		}

		// I can actually get the objStat and do a real listing...otherwise
		// would have returned

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		List<CollectionAndDataObjectListingEntry> queriedEntries = collectionListingUtils
				.listCollectionsUnderPath(objStat, 0);

		/*
		 * characterize the collections listing by looking at the returned data
		 */
		if (queriedEntries.isEmpty()) {
			log.info("no child collections");
			pagingAwareCollectionListing.setCollectionsComplete(true);
			pagingAwareCollectionListing.setCollectionsCount(0);
			pagingAwareCollectionListing.setCollectionsOffset(0);
		} else {
			log.info("adding child collections");
			pagingAwareCollectionListing.setCollectionsComplete(queriedEntries
					.get(queriedEntries.size() - 1).isLastResult());
			pagingAwareCollectionListing.setCollectionsCount(queriedEntries
					.get(queriedEntries.size() - 1).getCount());
			pagingAwareCollectionListing
					.setCollectionsTotalRecords(queriedEntries.get(0)
							.getTotalRecords());
			pagingAwareCollectionListing
					.getCollectionAndDataObjectListingEntries().addAll(
							queriedEntries);
		}

		queriedEntries = collectionListingUtils.listDataObjectsUnderPath(
				objStat, 0);

		/*
		 * characterize the data objects listing
		 */
		if (queriedEntries.isEmpty()) {
			log.info("no child data objects");
			pagingAwareCollectionListing.setDataObjectsComplete(true);
			pagingAwareCollectionListing.setDataObjectsCount(0);
			pagingAwareCollectionListing.setDataObjectsOffset(0);
		} else {
			log.info("adding child data objects");
			pagingAwareCollectionListing.setDataObjectsComplete(queriedEntries
					.get(queriedEntries.size() - 1).isLastResult());
			pagingAwareCollectionListing.setDataObjectsCount(queriedEntries
					.get(queriedEntries.size() - 1).getCount());
			pagingAwareCollectionListing
					.setDataObjectsTotalRecords(queriedEntries.get(0)
							.getTotalRecords());
			pagingAwareCollectionListing
					.getCollectionAndDataObjectListingEntries().addAll(
							queriedEntries);
		}

		log.info("pagingAwareCollectionListing:{}",
				pagingAwareCollectionListing);
		return pagingAwareCollectionListing;

	}

}
