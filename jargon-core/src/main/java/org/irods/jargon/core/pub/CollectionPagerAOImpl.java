/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoMoreDataException;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.query.PagingAwareCollectionListing.PagingStyle;
import org.irods.jargon.core.query.PagingAwareCollectionListingDescriptor;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Improved facility to list under a parent collection in a manner that supports
 * paging by clients
 * 
 * @author Mike Conway - DICE
 * 
 */
public class CollectionPagerAOImpl extends IRODSGenericAO implements CollectionPagerAO {

	public static final Logger log = LoggerFactory.getLogger(CollectionPagerAOImpl.class);

	private final DataObjectAO dataObjectAO;
	private final CollectionAO collectionAO;
	private final CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO;

	/**
	 * Standad constructor with session and account
	 * 
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	public CollectionPagerAOImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		super(irodsSession, irodsAccount);

		this.getIRODSAccessObjectFactory().getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		this.dataObjectAO = this.getIRODSAccessObjectFactory().getDataObjectAO(getIRODSAccount());
		this.collectionAO = this.getIRODSAccessObjectFactory().getCollectionAO(getIRODSAccount());
		this.collectionAndDataObjectListAndSearchAO = this.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionPagerAO#retrieveNextPage(org.irods
	 * .jargon.core.query.PagingAwareCollectionListingDescriptor)
	 */
	@Override
	public PagingAwareCollectionListing retrieveNextPage(
			final PagingAwareCollectionListingDescriptor lastListingDescriptor)
			throws FileNotFoundException, NoMoreDataException, JargonException {

		log.info("retrieveNextPage()");

		if (lastListingDescriptor == null) {
			throw new IllegalArgumentException("null lastListingDescriptor");
		}

		log.info("next page based on descriptor:{}", lastListingDescriptor);

		if (!lastListingDescriptor.isCollectionsComplete()) {
			log.info("more collections to page..");
			final PagingAwareCollectionListing listing = pageForwardInCollections(lastListingDescriptor);
			// if I've paged out of collections add the first page of data
			// objects
			if (listing.getPagingAwareCollectionListingDescriptor().isCollectionsComplete()) {
				log.info("colletions complete, page into data objects");
				addDataObjectsToExistingListing(listing);
			}
			return listing;

		} else if (!lastListingDescriptor.isDataObjectsComplete()) {
			log.info("more data objects to page...");
			return pageForwardInDataObjects(lastListingDescriptor);
		} else {
			log.error("no more listings to page for:{}", lastListingDescriptor);
			throw new NoMoreDataException("no more listings, cannot page forward");
		}

	}

	private PagingAwareCollectionListing pageForwardInDataObjects(
			final PagingAwareCollectionListingDescriptor lastListingDescriptor)
			throws FileNotFoundException, JargonException {

		log.info("pageForwardInDataObjects()");
		// since I fold the provided descriptor into the listing I generate, it
		// should propagate most of its information forward

		final PagingAwareCollectionListing pagingAwareCollectionListing = this
				.obtainObjStatAndBuildSkeletonPagingAwareCollectionListing(lastListingDescriptor);
		final ListAndCount listAndCount = listDataObjectsGivenObjStat(
				pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor().getObjStat(),
				lastListingDescriptor.getDataObjectsCount());

		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsTotalRecords(listAndCount.getCountTotal());
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsCount(listAndCount.getCountThisPage());
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsOffset(listAndCount.getOffsetStart());
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsComplete(listAndCount.isEndOfRecords());

		pagingAwareCollectionListing
				.setCollectionAndDataObjectListingEntries(listAndCount.getCollectionAndDataObjectListingEntries());

		return pagingAwareCollectionListing;

	}

	private PagingAwareCollectionListing pageForwardInCollections(
			final PagingAwareCollectionListingDescriptor lastListingDescriptor)
			throws FileNotFoundException, JargonException {
		log.info("pageForwardInCollections()");

		// since I fold the provided descriptor into the listing I generate, it
		// should propagate most of its information forward
		final PagingAwareCollectionListing pagingAwareCollectionListing = this
				.obtainObjStatAndBuildSkeletonPagingAwareCollectionListing(lastListingDescriptor);
		final ListAndCount listAndCount = listCollectionsGivenObjStat(
				pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor().getObjStat(),
				lastListingDescriptor.getCount());

		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setTotalRecords(listAndCount.getCountTotal());
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setCount(listAndCount.getCountThisPage());
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setOffset(listAndCount.getOffsetStart());
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setCollectionsComplete(listAndCount.isEndOfRecords());

		pagingAwareCollectionListing
				.setCollectionAndDataObjectListingEntries(listAndCount.getCollectionAndDataObjectListingEntries());

		return pagingAwareCollectionListing;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionPagerAO#retrieveFirstPageUnderParent
	 * (java.lang.String)
	 */
	@Override
	public PagingAwareCollectionListing retrieveFirstPageUnderParent(final String irodsAbsolutePath)
			throws FileNotFoundException, NoMoreDataException, JargonException {

		log.info("retrieveFirstPageUnderParent()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		log.info("try and list collections");

		final PagingAwareCollectionListing pagingAwareCollectionListing = this
				.obtainObjStatAndBuildSkeletonPagingAwareCollectionListing(irodsAbsolutePath);
		final ListAndCount listAndCount = listCollectionsGivenObjStat(
				pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor().getObjStat(), 0);

		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setTotalRecords(listAndCount.getCountTotal());
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setCount(listAndCount.getCountThisPage());
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setOffset(listAndCount.getOffsetStart());
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setCollectionsComplete(listAndCount.isEndOfRecords());

		pagingAwareCollectionListing
				.setCollectionAndDataObjectListingEntries(listAndCount.getCollectionAndDataObjectListingEntries());

		if (listAndCount.getCollectionAndDataObjectListingEntries().isEmpty()
				|| listAndCount.getCountThisPage() + 1 < this.getJargonProperties().getMaxFilesAndDirsQueryMax()) {
			log.info("collections are empty or less then max, so get data objects");

			addDataObjectsToExistingListing(pagingAwareCollectionListing);
		}

		return pagingAwareCollectionListing;

	}

	private void addDataObjectsToExistingListing(final PagingAwareCollectionListing pagingAwareCollectionListing)
			throws JargonException {
		final ListAndCount listAndCount = listDataObjectsGivenObjStat(
				pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor().getObjStat(), 0);
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsTotalRecords(listAndCount.getCountTotal());
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsCount(listAndCount.getCountThisPage());
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsOffset(listAndCount.getOffsetStart());
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsComplete(listAndCount.isEndOfRecords());
		pagingAwareCollectionListing.getCollectionAndDataObjectListingEntries()
				.addAll(listAndCount.getCollectionAndDataObjectListingEntries());
	}

	/**
	 * Wraps the provided path as a descriptor for initially obtaining the first
	 * page
	 * 
	 * @param irodsAbsolutePath
	 * @return
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	private PagingAwareCollectionListing obtainObjStatAndBuildSkeletonPagingAwareCollectionListing(
			final String irodsAbsolutePath) throws FileNotFoundException, JargonException {

		log.info("obtainObjStatAndBuildSkeletonPagingAwareCollectionListing()");

		final PagingAwareCollectionListingDescriptor descriptor = new PagingAwareCollectionListingDescriptor();
		descriptor.setParentAbsolutePath(irodsAbsolutePath);
		// see if colls and data objs exist and get counts
		computeTotalCounts(irodsAbsolutePath, descriptor);

		return obtainObjStatAndBuildSkeletonPagingAwareCollectionListing(descriptor);

	}

	private void computeTotalCounts(final String irodsAbsolutePath,
			final PagingAwareCollectionListingDescriptor descriptor) {
		log.info("computeTotalCounts()");

	}

	private PagingAwareCollectionListing obtainObjStatAndBuildSkeletonPagingAwareCollectionListing(
			final PagingAwareCollectionListingDescriptor pagingAwareCollectionListingDescriptor)
			throws FileNotFoundException, JargonException {
		log.info("obtain objStat for path:{}", pagingAwareCollectionListingDescriptor);

		ObjStat objStat;
		final PagingAwareCollectionListing pagingAwareCollectionListing = new PagingAwareCollectionListing();
		pagingAwareCollectionListing.setPagingAwareCollectionListingDescriptor(pagingAwareCollectionListingDescriptor);

		objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(pagingAwareCollectionListingDescriptor.getParentAbsolutePath());
		pagingAwareCollectionListingDescriptor.setPathComponents(MiscIRODSUtils
				.breakIRODSPathIntoComponents(pagingAwareCollectionListingDescriptor.getParentAbsolutePath()));
		pagingAwareCollectionListingDescriptor.setObjStat(objStat);
		pagingAwareCollectionListingDescriptor.setPagingStyle(PagingStyle.SPLIT_COLLECTIONS_AND_FILES);

		log.info("objStat:{}", objStat);

		if (!objStat.isSomeTypeOfCollection()) {
			log.error("this is not a collection:{}", pagingAwareCollectionListingDescriptor.getParentAbsolutePath());
			throw new JargonException("cannot list contents under a dataObject, must be a collection");
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);
		return pagingAwareCollectionListing;
	}

	private ListAndCount listCollectionsGivenObjStat(final ObjStat objStat, final int offset) throws JargonException {

		log.info("listCollectionsGivenObjStat()");
		log.info("objStat:{}", objStat);
		log.info("offset:{}", offset);
		final ListAndCount listAndCount = new ListAndCount();
		listAndCount.setCollectionAndDataObjectListingEntries(
				collectionAndDataObjectListAndSearchAO.listCollectionsUnderPath(objStat, offset));

		/*
		 * see if the query had total records, if it did not, do a separate
		 * query to establish total records
		 */

		if (listAndCount.getCollectionAndDataObjectListingEntries().isEmpty()) {
			listAndCount.setCountTotal(0);
			log.info("empty results returned");
			listAndCount.setEndOfRecords(true);
			return listAndCount;
		}

		final int lastEntryIdx = listAndCount.getCollectionAndDataObjectListingEntries().size() - 1;
		final CollectionAndDataObjectListingEntry lastEntry = listAndCount.getCollectionAndDataObjectListingEntries()
				.get(lastEntryIdx);
		listAndCount.setCountThisPage(lastEntry.getCount());
		listAndCount.setEndOfRecords(lastEntry.isLastResult());
		listAndCount.setOffsetStart(listAndCount.getCollectionAndDataObjectListingEntries().get(0).getCount());

		int count = lastEntry.getTotalRecords();
		if (count > 0) {
			listAndCount.setCountTotal(count);
			log.info("total records was in the result set already");
			return listAndCount;
		}

		/*
		 * Requires a separate query to get the count of total records
		 */

		log.info("separate query to get a count");
		count = collectionAndDataObjectListAndSearchAO.countCollectionsUnderPath(objStat);

		listAndCount.setCountTotal(count);
		return listAndCount;

	}

	private ListAndCount listDataObjectsGivenObjStat(final ObjStat objStat, final int offset) throws JargonException {

		log.info("listDataObjectsGivenObjStat()");
		final ListAndCount listAndCount = new ListAndCount();
		listAndCount.setCollectionAndDataObjectListingEntries(
				collectionAndDataObjectListAndSearchAO.listDataObjectsUnderPath(objStat, offset));

		if (listAndCount.getCollectionAndDataObjectListingEntries().isEmpty()) {
			listAndCount.setCountTotal(0);
			listAndCount.setEndOfRecords(true);
			log.info("empty results returned");
			return listAndCount;
		}

		final int lastEntryIdx = listAndCount.getCollectionAndDataObjectListingEntries().size() - 1;
		final CollectionAndDataObjectListingEntry lastEntry = listAndCount.getCollectionAndDataObjectListingEntries()
				.get(lastEntryIdx);
		listAndCount.setCountThisPage(lastEntry.getCount());
		listAndCount.setEndOfRecords(lastEntry.isLastResult());
		listAndCount.setOffsetStart(listAndCount.getCollectionAndDataObjectListingEntries().get(0).getCount());

		int count = listAndCount.getCollectionAndDataObjectListingEntries().get(0).getTotalRecords();
		if (count > 0) {
			listAndCount.setCountTotal(count);
			log.info("total records was in the result set already");
			return listAndCount;
		}

		log.info("separate query to get a count");
		count = collectionAndDataObjectListAndSearchAO.countDataObjectsUnderPath(objStat);
		listAndCount.setCountTotal(count);
		return listAndCount;

	}

}
