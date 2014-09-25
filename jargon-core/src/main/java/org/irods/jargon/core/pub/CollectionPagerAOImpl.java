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
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.query.PagingAwareCollectionListing.PagingStyle;
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
	 * actually an affordence for testing.
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

		log.info("obtain objStat for path:{}", irodsAbsolutePath);

		ObjStat objStat;
		PagingAwareCollectionListing pagingAwareCollectionListing = new PagingAwareCollectionListing();
		pagingAwareCollectionListing.setPageSizeUtilized(getJargonProperties()
				.getMaxFilesAndDirsQueryMax());
		objStat = collectionListingUtils
				.retrieveObjectStatForPath(irodsAbsolutePath);
		log.info("objStat:{}", objStat);

		if (!objStat.isSomeTypeOfCollection()) {
			log.error("this is not a collection:{}", irodsAbsolutePath);
			throw new JargonException(
					"cannot list contents under a dataObject, must be a collection");
		}

		/*
		 * See if jargon supports the given object type
		 */
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		/*
		 * Special collections like mounted collections dont support paging, and
		 * will just retrieve everything
		 */
		if (objStat.getSpecColType() == SpecColType.NORMAL
				|| objStat.getSpecColType() == SpecColType.LINKED_COLL) {
			pagingAwareCollectionListing.setPagingStyle(PagingStyle.NONE);
		} else {
			pagingAwareCollectionListing
					.setPagingStyle(PagingStyle.SPLIT_COLLECTIONS_AND_FILES);
		}

		log.info("try and list collections");

		ListAndCount listAndCount = listCollectionsGivenObjStat(objStat, 0);

		pagingAwareCollectionListing.setTotalRecords(listAndCount
				.getCountTotal());
		pagingAwareCollectionListing.setCount(listAndCount.getCountThisPage());
		pagingAwareCollectionListing.setOffset(listAndCount.getOffsetStart());
		pagingAwareCollectionListing.setCollectionsComplete(listAndCount
				.isEndOfRecords());

		if (listAndCount.getCollectionAndDataObjectListingEntries().isEmpty()) {
			log.info("no collections, so get data objects");

			listAndCount = listDataObjectsGivenObjStat(objStat, 0);
			if (listAndCount.getCollectionAndDataObjectListingEntries()
					.isEmpty()) {
				log.info("data objects empty as well");
				pagingAwareCollectionListing.setDataObjectsComplete(true);
			}
		}

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
}
