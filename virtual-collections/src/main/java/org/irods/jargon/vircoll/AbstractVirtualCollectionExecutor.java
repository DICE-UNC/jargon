/**
 *
 */
package org.irods.jargon.vircoll;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.service.AbstractJargonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract model of a service that can execute operations on a given virtual
 * collection. This means CRUD operations on the virtual collection, as well as
 * execution of the embedded query
 * 
 * @author Mike Conway - DICE
 * 
 */
public abstract class AbstractVirtualCollectionExecutor<T extends AbstractVirtualCollection>
		extends AbstractJargonService {

	static Logger log = LoggerFactory
			.getLogger(AbstractVirtualCollectionExecutor.class);

	private final T collection;

	/**
	 * Public constructor so I can mock this, even though this is stupid. Don't
	 * use this constructor
	 */
	public AbstractVirtualCollectionExecutor() {
		super();
		collection = null;
	}

	/**
	 * Generate a result list based on executing the virtual collection query
	 * 
	 * @param offset
	 *            <code>int</code> with the offset into the result set (paging
	 *            may not be supported in all subclasses)
	 * @return {@link PagingAwareCollectionListing} with the result of the query
	 * @throws JargonException
	 */
	public abstract PagingAwareCollectionListing queryAll(int offset)
			throws JargonException;

	/**
	 * Get the abstract virtual collection associated with this executor
	 * 
	 * @return {@link AbstractVirtualCollection} subtype
	 */
	public T getCollection() {
		return collection;
	}

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	protected AbstractVirtualCollectionExecutor(final T collection,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);

		if (collection == null) {
			throw new IllegalArgumentException("null collection");
		}

		this.collection = collection;
	}

	/**
	 * Add collection entries to the provided
	 * <code>PagingAwareCollectionListing</code> and characterize that listing
	 * with information from the guery result listing
	 * 
	 * @param pagingAwareCollectionListing
	 *            {@link PagingAwareCollectionListing} that will be augmented
	 *            with entries and metadata about those entries
	 * @param entries
	 *            <code>List</code> of
	 *            {@link CollectionAndDataObjectListingEntry} that will be added
	 *            to the <code>PagingAwareCollectionListing</code>
	 */
	protected void addAndCharacterizeCollectionListingForSplitListing(
			final PagingAwareCollectionListing pagingAwareCollectionListing,
			final List<CollectionAndDataObjectListingEntry> entries) {
		if (entries.isEmpty()) {
			log.info("no child collections");
			pagingAwareCollectionListing.setCollectionsComplete(true);
			pagingAwareCollectionListing.setCount(0);
			pagingAwareCollectionListing.setOffset(0);
		} else {
			log.info("adding child collections");
			pagingAwareCollectionListing.setCollectionsComplete(entries.get(
					entries.size() - 1).isLastResult());
			pagingAwareCollectionListing.setCount(entries.get(
					entries.size() - 1).getCount());
			pagingAwareCollectionListing.setTotalRecords(entries.get(0)
					.getTotalRecords());
			pagingAwareCollectionListing
					.getCollectionAndDataObjectListingEntries().addAll(entries);
		}

	}

	/**
	 * Add dataObject entries to the provided
	 * <code>PagingAwareCollectionListing</code> and characterize that listing
	 * with information from the guery result listing
	 * 
	 * @param pagingAwareCollectionListing
	 *            {@link PagingAwareCollectionListing} that will be augmented
	 *            with entries and metadata about those entries
	 * @param entries
	 *            <code>List</code> of
	 *            {@link CollectionAndDataObjectListingEntry} containing data
	 *            objects that will be added to the
	 *            <code>PagingAwareCollectionListing</code>
	 */
	protected void addAndCharacterizeDataObjectListingForSplitListing(
			final PagingAwareCollectionListing pagingAwareCollectionListing,
			final List<CollectionAndDataObjectListingEntry> entries) {
		if (entries.isEmpty()) {
			log.info("no child data objects");
			pagingAwareCollectionListing.setDataObjectsComplete(true);
			pagingAwareCollectionListing.setDataObjectsCount(0);
			pagingAwareCollectionListing.setDataObjectsOffset(0);
		} else {
			log.info("adding child data objects");
			pagingAwareCollectionListing.setDataObjectsComplete(entries.get(
					entries.size() - 1).isLastResult());
			pagingAwareCollectionListing.setDataObjectsCount(entries.get(
					entries.size() - 1).getCount());
			pagingAwareCollectionListing.setDataObjectsTotalRecords(entries
					.get(0).getTotalRecords());
			pagingAwareCollectionListing
					.getCollectionAndDataObjectListingEntries().addAll(entries);
		}

	}

	/**
	 * Handy method builds a 'blank' listing instance that can than be augmented
	 * with listing data and metadata as it is further built based on queries.
	 * 
	 * @return {@link PagingAwareCollectionListing} with basic initialized data
	 * @throws JargonException
	 */
	protected PagingAwareCollectionListing buildInitialPagingAwareCollectionListing()
			throws JargonException {
		PagingAwareCollectionListing pagingAwareCollectionListing = new PagingAwareCollectionListing();
		pagingAwareCollectionListing.setPageSizeUtilized(this
				.getIrodsAccessObjectFactory().getJargonProperties()
				.getMaxFilesAndDirsQueryMax());
		pagingAwareCollectionListing.setPagingStyle(this.getCollection()
				.getPagingStyle());
		return pagingAwareCollectionListing;
	}

}
