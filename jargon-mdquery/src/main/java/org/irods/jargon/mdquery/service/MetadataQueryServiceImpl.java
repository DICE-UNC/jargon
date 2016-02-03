/**
 * 
 */
package org.irods.jargon.mdquery.service;

import java.util.ArrayList;
import java.util.Collection;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.mdquery.MetadataQuery;
import org.irods.jargon.mdquery.MetadataQuery.QueryType;
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
			final MetadataQuery metadataQuery) {

		log.info("executeQuery()");
		if (metadataQuery == null) {
			throw new IllegalArgumentException("null metadataQuery");
		}

		log.info("metadataQuery:{}", metadataQuery);

		PagingAwareCollectionListing listing = new PagingAwareCollectionListing();
		ArrayList<CollectionAndDataObjectListingEntry> entries = new ArrayList<CollectionAndDataObjectListingEntry>();

		if (metadataQuery.getQueryType() == QueryType.BOTH
				|| metadataQuery.getQueryType() == QueryType.COLLECTIONS) {

			log.info("querying collections");

			entries.addAll(queryCollections(metadataQuery));

		} else if (metadataQuery.getQueryType() == QueryType.BOTH
				|| metadataQuery.getQueryType() == QueryType.DATA) {

			log.info("querying data objects");
			entries.addAll(queryDataObjects(metadataQuery));

		}

		return null;

	}

	private Collection<? extends CollectionAndDataObjectListingEntry> queryDataObjects(
			MetadataQuery metadataQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	private ArrayList<CollectionAndDataObjectListingEntry> queryCollections(
			MetadataQuery metadataQuery) {
		// TODO Auto-generated method stub
		return null;
	}
}
