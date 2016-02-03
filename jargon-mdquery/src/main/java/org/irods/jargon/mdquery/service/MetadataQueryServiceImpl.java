/**
 * 
 */
package org.irods.jargon.mdquery.service;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.ListAndCount;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.mdquery.MetadataQuery;
import org.irods.jargon.mdquery.MetadataQuery.QueryType;
import org.irods.jargon.mdquery.exception.MetadataQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a service to generate metadata queries on iRODS. These are
 * actually GenQuery under the covers, but have been abstracted to make higher
 * level services simpler, and to centralize functional testing (e.g. iRODS gen
 * query limitiations) at this layer. Thus this class can enforce any necessary
 * restrictions on queries (number of elements, etc)
 * 
 * @author Mike Conway - DICE
 *
 */
public class MetadataQueryServiceImpl extends AbstractJargonService {

	static Logger log = LoggerFactory.getLogger(MetadataQueryServiceImpl.class);

	/**
	 * Constructor takes dependencies
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} to create various connected
	 *            services
	 * @param irodsAccount
	 *            {@link IRODSAccount} with authentication credentials
	 */
	public MetadataQueryServiceImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * 
	 */
	public MetadataQueryServiceImpl() {
	}

	public PagingAwareCollectionListing executeQuery(
			final MetadataQuery metadataQuery) throws MetadataQueryException {

		log.info("executeQuery()");
		if (metadataQuery == null) {
			throw new IllegalArgumentException("null metadataQuery");
		}

		log.info("metadataQuery:{}", metadataQuery);

		PagingAwareCollectionListing listing = new PagingAwareCollectionListing();

		try {
			listing.getPagingAwareCollectionListingDescriptor()
					.setPageSizeUtilized(
							this.getIrodsAccessObjectFactory()
									.getJargonProperties()
									.getMaxFilesAndDirsQueryMax());
		} catch (JargonException e) {
			log.error("jargon exception in query", e);
			throw new MetadataQueryException(e);
		}

		if (metadataQuery.getQueryType() == QueryType.BOTH
				|| metadataQuery.getQueryType() == QueryType.COLLECTIONS) {

			ListAndCount collections = queryCollections(metadataQuery);
			listing.getCollectionAndDataObjectListingEntries().addAll(
					collections.getCollectionAndDataObjectListingEntries());
			listing.getPagingAwareCollectionListingDescriptor()
					.setCollectionsComplete(collections.isEndOfRecords());

			listing.getPagingAwareCollectionListingDescriptor().setCount(
					collections.getCountThisPage());

		} else if (metadataQuery.getQueryType() == QueryType.BOTH
				|| metadataQuery.getQueryType() == QueryType.DATA) {

			log.info("querying data objects");
			ListAndCount dataObjects = queryDataObjects(metadataQuery);
			listing.getCollectionAndDataObjectListingEntries().addAll(
					dataObjects.getCollectionAndDataObjectListingEntries());
			listing.getPagingAwareCollectionListingDescriptor()
					.setDataObjectsComplete(dataObjects.isEndOfRecords());

			listing.getPagingAwareCollectionListingDescriptor()
					.setDataObjectsCount(dataObjects.getCountThisPage());

		}

		log.info("listing generated:{}", listing);
		return listing;

	}

	private ListAndCount queryDataObjects(MetadataQuery metadataQuery) {
		return null;
	}

	private ListAndCount queryCollections(MetadataQuery metadataQuery) {
		return null;
	}

	private ListAndCount characterizeListing(
			final List<CollectionAndDataObjectListingEntry> listing) {

		ListAndCount listAndCount = new ListAndCount();
		listAndCount.setCollectionAndDataObjectListingEntries(listing);

		if (listing.isEmpty()) {
			listAndCount.setCountTotal(0);
			listAndCount.setEndOfRecords(true);
			log.info("empty results returned");
			return listAndCount;
		}

		int lastEntryIdx = listing.size() - 1;
		CollectionAndDataObjectListingEntry lastEntry = listing
				.get(lastEntryIdx);
		listAndCount.setCountThisPage(lastEntry.getCount());
		listAndCount.setEndOfRecords(lastEntry.isLastResult());
		listAndCount.setOffsetStart(listing.get(0).getCount());

		int count = listing.get(0).getTotalRecords();
		if (count > 0) {
			listAndCount.setCountTotal(count);
			log.info("total records was in the result set already");
		}

		return listAndCount;

	}
}
