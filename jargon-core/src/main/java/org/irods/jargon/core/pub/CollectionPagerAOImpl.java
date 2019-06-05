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
 * paging by clients. This implementation is meant to be a lower level interface
 * to paging, returning a detailed view of the paging status.
 * <p/>
 * This interface focuses on paging via offsets, and handles paging across
 * collections and data objects, which are treated as distinct data sources.
 *
 * @author Mike Conway - DICE
 *
 */
public class CollectionPagerAOImpl extends IRODSGenericAO implements CollectionPagerAO {

	public static final Logger log = LoggerFactory.getLogger(CollectionPagerAOImpl.class);

	private CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO;

	/**
	 * Default page size will be initialized with the jargon properties files and
	 * dirs query max.
	 */
	private final int defaultPageSize;

	public CollectionPagerAOImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		super(irodsSession, irodsAccount);
		this.collectionAndDataObjectListAndSearchAO = getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());
		this.defaultPageSize = irodsSession.getJargonProperties().getMaxFilesAndDirsQueryMax();
	}

	@Override
	public PagingAwareCollectionListing retrieveNextOffset(final String irodsAbsolutePath, final boolean inCollections,
			final int offset, final long pageSize) throws FileNotFoundException, NoMoreDataException, JargonException {

		log.info("retrieveNextPage()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("offset:{}", offset);
		log.info("pageSize:{}", pageSize);

		final PagingAwareCollectionListing pagingAwareCollectionListing = this
				.obtainObjStatAndBuildSkeletonPagingAwareCollectionListing(irodsAbsolutePath);

		if (inCollections) {
			log.info("paging into collections to offset:{}", offset);
		} else {
			log.info("paging into data objects to offset:{}", offset);
		}

		return null;

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

	@Override
	public PagingAwareCollectionListing retrieveFirstResultUnderParent(final String irodsAbsolutePath)
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
		pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor().setPageSizeUtilized(defaultPageSize);

		pagingAwareCollectionListing
				.setCollectionAndDataObjectListingEntries(listAndCount.getCollectionAndDataObjectListingEntries());

		if (listAndCount.isEndOfRecords() && listAndCount.getCountThisPage() < pagingAwareCollectionListing
				.getPagingAwareCollectionListingDescriptor().getPageSizeUtilized()) {
			log.info("adding data objects to incomplete listing");
			addDataObjectsToExistingListing(pagingAwareCollectionListing);
		}

		if (pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor().isCollectionsComplete()
				&& pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor().isDataObjectsComplete()) {
			pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor().setHasMore(false);
		} else {
			pagingAwareCollectionListing.getPagingAwareCollectionListingDescriptor().setHasMore(true);

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

	/*
	 * Wraps the provided path as a descriptor for initially obtaining the first
	 * page
	 *
	 * @param irodsAbsolutePath
	 *
	 * @return
	 *
	 * @throws FileNotFoundException
	 *
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
		 * see if the query had total records, if it did not, do a separate query to
		 * establish total records
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
		listAndCount.setCountThisPage(lastEntry.getCount() - 1); // entry record index is 1 based for a probably not
																	// good reason
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
		listAndCount.setCountThisPage(lastEntry.getCount() - 1); // entry record index is 1 based for some forgotten
																	// reason
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

	/**
	 * Optional impl level hook for injection to support testing of complex
	 * behavior.
	 * 
	 * @param collectionAndDataObjectListAndSearchAO {@link CollectionAndDataObjectListAndSearchAO}
	 *                                               that can override the default.
	 */
	public void setCollectionAndDataObjectListAndSearchAO(
			CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO) {
		this.collectionAndDataObjectListAndSearchAO = collectionAndDataObjectListAndSearchAO;
	}

}
