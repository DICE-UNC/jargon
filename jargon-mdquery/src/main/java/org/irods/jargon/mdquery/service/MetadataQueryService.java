package org.irods.jargon.mdquery.service;

import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.mdquery.MetadataQuery;
import org.irods.jargon.mdquery.exception.MetadataQueryException;

/**
 * Service to query iRODS based on a POJO {@link MetadataQuery}
 * 
 * @author Mike Conway - DICE
 *
 */
public interface MetadataQueryService {

	/**
	 * Execute the query and return a listing result
	 * 
	 * @param metadataQuery
	 *            {@link MetadataQuery} which is an abbreviated GenQuery format
	 * @return {@link PagingAwareCollectionListing} with results
	 * @throws MetadataQueryException
	 */
	public abstract PagingAwareCollectionListing executeQuery(
			MetadataQuery metadataQuery) throws MetadataQueryException;

}