/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.List;

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
public class CollectionPagerAOImpl extends IRODSGenericAO implements
		CollectionPagerAO {

	public static final Logger log = LoggerFactory
			.getLogger(CollectionPagerAOImpl.class);

	private final CollectionListingUtils collectionListingUtils;

	/**
	 * Standad constructor with session and account
	 * 
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	public CollectionPagerAOImpl(IRODSSession irodsSession,
			IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);

		this.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		this.collectionListingUtils = new CollectionListingUtils(
				this.getIRODSAccount(), this.getIRODSAccessObjectFactory());
	}

	/**
	 * Constructor allows specification of a collection listing utils, this is
	 * actually an affordance for testing.
	 * 
	 * @param irodsSession
	 * @param irodsAccount
	 * @param collectionListingUtils
	 * @throws JargonException
	 */
	CollectionPagerAOImpl(IRODSSession irodsSession, IRODSAccount irodsAccount,
			final CollectionListingUtils collectionListingUtils)
			throws JargonException {
		super(irodsSession, irodsAccount);
		if (collectionListingUtils == null) {
			throw new IllegalArgumentException("null collectionListingUtils");
		}
		this.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		this.collectionListingUtils = collectionListingUtils;
	}

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
			PagingAwareCollectionListing listing = pageForwardInCollections(lastListingDescriptor);
			// if I've paged out of collections add the first page of data
			// objects
			if (listing.getPagingAwareCollectionListingDescriptor()
					.isCollectionsComplete()) {
				log.info("colletions complete, page into data objects");
				addDataObjectsToExistingListing(listing);
			}
			return listing;

		} else if (!lastListingDescriptor.isDataObjectsComplete()) {
			log.info("more data objects to page...");
			return pageForwardInDataObjects(lastListingDescriptor);
		} else {
			log.error("no more listings to page for:{}", lastListingDescriptor);
			throw new NoMoreDataException(
					"no more listings, cannot page forward");
		}

	}

	private PagingAwareCollectionListing pageForwardInDataObjects(
			PagingAwareCollectionListingDescriptor lastListingDescriptor)
			throws FileNotFoundException, JargonException {

		log.info("pageForwardInDataObjects()");
		// since I fold the provided descriptor into the listing I generate, it
		// should propagate most of its information forward

		PagingAwareCollectionListing pagingAwareCollectionListing = this
				.obtainObjStatAndBuildSkeletonPagingAwareCollectionListing(lastListingDescriptor);
		ListAndCount listAndCount = listDataObjectsGivenObjStat(
				pagingAwareCollectionListing
						.getPagingAwareCollectionListingDescriptor()
						.getObjStat(),
				lastListingDescriptor.getDataObjectsCount());

		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsTotalRecords(listAndCount.getCountTotal());
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsCount(listAndCount.getCountThisPage());
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsOffset(listAndCount.getOffsetStart());
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsComplete(listAndCount.isEndOfRecords());

		pagingAwareCollectionListing
				.setCollectionAndDataObjectListingEntries(listAndCount
						.getCollectionAndDataObjectListingEntries());

		return pagingAwareCollectionListing;

	}

	private PagingAwareCollectionListing pageForwardInCollections(
			PagingAwareCollectionListingDescriptor lastListingDescriptor)
			throws FileNotFoundException, JargonException {
		log.info("pageForwardInCollections()");

		// since I fold the provided descriptor into the listing I generate, it
		// should propagate most of its information forward
		PagingAwareCollectionListing pagingAwareCollectionListing = this
				.obtainObjStatAndBuildSkeletonPagingAwareCollectionListing(lastListingDescriptor);
		ListAndCount listAndCount = listCollectionsGivenObjStat(
				pagingAwareCollectionListing
						.getPagingAwareCollectionListingDescriptor()
						.getObjStat(), lastListingDescriptor.getCount());

		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor().setTotalRecords(
						listAndCount.getCountTotal());
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor().setCount(
						listAndCount.getCountThisPage());
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor().setOffset(
						listAndCount.getOffsetStart());
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor()
				.setCollectionsComplete(listAndCount.isEndOfRecords());

		pagingAwareCollectionListing
				.setCollectionAndDataObjectListingEntries(listAndCount
						.getCollectionAndDataObjectListingEntries());

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
	public PagingAwareCollectionListing retrieveFirstPageUnderParent(
			final String irodsAbsolutePath) throws FileNotFoundException,
			NoMoreDataException, JargonException {

		log.info("initialListingUnderParent()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("try and list collections");

		PagingAwareCollectionListing pagingAwareCollectionListing = this
				.obtainObjStatAndBuildSkeletonPagingAwareCollectionListing(irodsAbsolutePath);
		ListAndCount listAndCount = listCollectionsGivenObjStat(
				pagingAwareCollectionListing
						.getPagingAwareCollectionListingDescriptor()
						.getObjStat(), 0);

		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor().setTotalRecords(
						listAndCount.getCountTotal());
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor().setCount(
						listAndCount.getCountThisPage());
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor().setOffset(
						listAndCount.getOffsetStart());
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor()
				.setCollectionsComplete(listAndCount.isEndOfRecords());

		pagingAwareCollectionListing
				.setCollectionAndDataObjectListingEntries(listAndCount
						.getCollectionAndDataObjectListingEntries());

		if (listAndCount.getCollectionAndDataObjectListingEntries().isEmpty()
				|| listAndCount.getCountThisPage() < this.getJargonProperties()
						.getMaxFilesAndDirsQueryMax()) {
			log.info("collections are empty or less then max, so get data objects");

			addDataObjectsToExistingListing(pagingAwareCollectionListing);
		}

		return pagingAwareCollectionListing;

	}

	private void addDataObjectsToExistingListing(
			PagingAwareCollectionListing pagingAwareCollectionListing)
			throws JargonException {
		ListAndCount listAndCount = listDataObjectsGivenObjStat(
				pagingAwareCollectionListing
						.getPagingAwareCollectionListingDescriptor()
						.getObjStat(), 0);
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsTotalRecords(listAndCount.getCountTotal());
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsCount(listAndCount.getCountThisPage());
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsOffset(listAndCount.getOffsetStart());
		pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor()
				.setDataObjectsComplete(listAndCount.isEndOfRecords());
		pagingAwareCollectionListing
				.getCollectionAndDataObjectListingEntries()
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
			String irodsAbsolutePath) throws FileNotFoundException,
			JargonException {

		log.info("obtainObjStatAndBuildSkeletonPagingAwareCollectionListing()");

		PagingAwareCollectionListingDescriptor descriptor = new PagingAwareCollectionListingDescriptor();
		descriptor.setParentAbsolutePath(irodsAbsolutePath);
		return obtainObjStatAndBuildSkeletonPagingAwareCollectionListing(descriptor);

	}

	private PagingAwareCollectionListing obtainObjStatAndBuildSkeletonPagingAwareCollectionListing(
			final PagingAwareCollectionListingDescriptor pagingAwareCollectionListingDescriptor)
			throws FileNotFoundException, JargonException {
		log.info("obtain objStat for path:{}",
				pagingAwareCollectionListingDescriptor);

		ObjStat objStat;
		PagingAwareCollectionListing pagingAwareCollectionListing = new PagingAwareCollectionListing();
		pagingAwareCollectionListing
				.setPagingAwareCollectionListingDescriptor(pagingAwareCollectionListingDescriptor);

		objStat = collectionListingUtils
				.retrieveObjectStatForPath(pagingAwareCollectionListingDescriptor
						.getParentAbsolutePath());
		pagingAwareCollectionListingDescriptor
				.setPathComponents(MiscIRODSUtils
						.breakIRODSPathIntoComponents(pagingAwareCollectionListingDescriptor
								.getParentAbsolutePath()));
		pagingAwareCollectionListingDescriptor.setObjStat(objStat);
		pagingAwareCollectionListingDescriptor
				.setPagingStyle(PagingStyle.SPLIT_COLLECTIONS_AND_FILES);
		log.info("objStat:{}", objStat);

		if (!objStat.isSomeTypeOfCollection()) {
			log.error("this is not a collection:{}",
					pagingAwareCollectionListingDescriptor
							.getParentAbsolutePath());
			throw new JargonException(
					"cannot list contents under a dataObject, must be a collection");
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		/*
		 * if (objStat.getSpecColType() == SpecColType.NORMAL ||
		 * objStat.getSpecColType() == SpecColType.LINKED_COLL) {
		 * descriptor.setPagingStyle(PagingStyle.NONE); } else {
		 * descriptor.setPagingStyle(PagingStyle.SPLIT_COLLECTIONS_AND_FILES); }
		 */

		return pagingAwareCollectionListing;
	}

	private ListAndCount listCollectionsGivenObjStat(final ObjStat objStat,
			final int offset) throws JargonException {

		log.info("listCollectionsGivenObjStat()");
		ListAndCount listAndCount = new ListAndCount();
		listAndCount
				.setCollectionAndDataObjectListingEntries(collectionListingUtils
						.listCollectionsUnderPath(objStat, offset));

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

		int lastEntryIdx = listAndCount
				.getCollectionAndDataObjectListingEntries().size() - 1;
		CollectionAndDataObjectListingEntry lastEntry = listAndCount
				.getCollectionAndDataObjectListingEntries().get(lastEntryIdx);
		listAndCount.setCountThisPage(lastEntry.getCount());
		listAndCount.setEndOfRecords(lastEntry.isLastResult());
		listAndCount.setOffsetStart(listAndCount
				.getCollectionAndDataObjectListingEntries().get(0).getCount());

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
		count = collectionListingUtils.countCollectionsUnderPath(objStat);

		listAndCount.setCountTotal(count);
		return listAndCount;

	}

	private ListAndCount listDataObjectsGivenObjStat(final ObjStat objStat,
			final int offset) throws JargonException {

		log.info("listDataObjectsGivenObjStat()");
		ListAndCount listAndCount = new ListAndCount();
		listAndCount
				.setCollectionAndDataObjectListingEntries(collectionListingUtils
						.listDataObjectsUnderPath(objStat, offset));

		/*
		 * see if the query had total records, if it did not, do a separate
		 * query to establish total records
		 */

		if (listAndCount.getCollectionAndDataObjectListingEntries().isEmpty()) {
			listAndCount.setCountTotal(0);
			listAndCount.setEndOfRecords(true);
			log.info("empty results returned");
			return listAndCount;
		}

		int lastEntryIdx = listAndCount
				.getCollectionAndDataObjectListingEntries().size() - 1;
		CollectionAndDataObjectListingEntry lastEntry = listAndCount
				.getCollectionAndDataObjectListingEntries().get(lastEntryIdx);
		listAndCount.setCountThisPage(lastEntry.getCount());
		listAndCount.setEndOfRecords(lastEntry.isLastResult());
		listAndCount.setOffsetStart(listAndCount
				.getCollectionAndDataObjectListingEntries().get(0).getCount());

		int count = listAndCount.getCollectionAndDataObjectListingEntries()
				.get(0).getTotalRecords();
		if (count > 0) {
			listAndCount.setCountTotal(count);
			log.info("total records was in the result set already");
			return listAndCount;
		}

		/*
		 * Requires a separate query to get the count of total records
		 */

		log.info("separate query to get a count");
		count = collectionListingUtils.countDataObjectsUnderPath(objStat);
		listAndCount.setCountTotal(count);
		return listAndCount;

	}

}

class ListAndCount {
	private int countTotal = 0;
	private int countThisPage = 0;
	private boolean endOfRecords = false;
	private int offsetStart = 0;
	private int offsetEnd = 0;
	private List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries;

	/**
	 * @return the countTotal
	 */
	int getCountTotal() {
		return countTotal;
	}

	/**
	 * @param countTotal
	 *            the countTotal to set
	 */
	void setCountTotal(int countTotal) {
		this.countTotal = countTotal;
	}

	/**
	 * @return the collectionAndDataObjectListingEntries
	 */
	List<CollectionAndDataObjectListingEntry> getCollectionAndDataObjectListingEntries() {
		return collectionAndDataObjectListingEntries;
	}

	/**
	 * @param collectionAndDataObjectListingEntries
	 *            the collectionAndDataObjectListingEntries to set
	 */
	void setCollectionAndDataObjectListingEntries(
			List<CollectionAndDataObjectListingEntry> collectionAndDataObjectListingEntries) {
		this.collectionAndDataObjectListingEntries = collectionAndDataObjectListingEntries;
	}

	/**
	 * @return the countThisPage
	 */
	int getCountThisPage() {
		return countThisPage;
	}

	/**
	 * @param countThisPage
	 *            the countThisPage to set
	 */
	void setCountThisPage(int countThisPage) {
		this.countThisPage = countThisPage;
	}

	/**
	 * @return the endOfRecords
	 */
	boolean isEndOfRecords() {
		return endOfRecords;
	}

	/**
	 * @param endOfRecords
	 *            the endOfRecords to set
	 */
	void setEndOfRecords(boolean endOfRecords) {
		this.endOfRecords = endOfRecords;
	}

	/**
	 * @return the offsetStart
	 */
	int getOffsetStart() {
		return offsetStart;
	}

	/**
	 * @param offsetStart
	 *            the offsetStart to set
	 */
	void setOffsetStart(int offsetStart) {
		this.offsetStart = offsetStart;
	}

	/**
	 * @return the offsetEnd
	 */
	int getOffsetEnd() {
		return offsetEnd;
	}

	/**
	 * @param offsetEnd
	 *            the offsetEnd to set
	 */
	void setOffsetEnd(int offsetEnd) {
		this.offsetEnd = offsetEnd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 5;
		StringBuilder builder = new StringBuilder();
		builder.append("ListAndCount [countTotal=");
		builder.append(countTotal);
		builder.append(", countThisPage=");
		builder.append(countThisPage);
		builder.append(", endOfRecords=");
		builder.append(endOfRecords);
		builder.append(", offsetStart=");
		builder.append(offsetStart);
		builder.append(", offsetEnd=");
		builder.append(offsetEnd);
		builder.append(", ");
		if (collectionAndDataObjectListingEntries != null) {
			builder.append("collectionAndDataObjectListingEntries=");
			builder.append(collectionAndDataObjectListingEntries.subList(0,
					Math.min(collectionAndDataObjectListingEntries.size(),
							maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}
}
