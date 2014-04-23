/**
 *
 */
package org.irods.jargon.vircoll.types;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.vircoll.AbstractVirtualCollectionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a virtual collection that is an actual iRODS collection (parent
 * folders are virtual collections themselves)
 * 
 * @author Mike Conway - DICE
 * 
 */
public class CollectionBasedVirtualCollectionExecutor extends
		AbstractVirtualCollectionExecutor<CollectionBasedVirtualCollection> {

	static Logger log = LoggerFactory
			.getLogger(CollectionBasedVirtualCollectionExecutor.class);

	/**
	 * Default constructor necessary to support mocks
	 */
	public CollectionBasedVirtualCollectionExecutor() {
		super();
	}

	/**
	 * @param collection
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public CollectionBasedVirtualCollectionExecutor(
			final CollectionBasedVirtualCollection collection,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(collection, irodsAccessObjectFactory, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.AbstractVirtualCollection#queryAll(int)
	 */
	@Override
	public PagingAwareCollectionListing queryAll(final int offset)
			throws JargonException {

		log.info("query()");

		log.info("offset:{}", offset);

		log.info("collection parent:{}", getCollection().getRootPath());

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIrodsAccount());
		return collectionAndDataObjectListAndSearchAO
				.listDataObjectsAndCollectionsUnderPathProducingPagingAwareCollectionListing(getCollection()
						.getRootPath());

	}

	public String getCollectionParentAbsolutePath() {
		return getCollection().getRootPath();
	}

}
