/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
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
	 * @param collection
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	protected CollectionBasedVirtualCollectionExecutor(
			CollectionBasedVirtualCollection collection,
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(collection, irodsAccessObjectFactory, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.AbstractVirtualCollection#queryAll(int)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> queryAll(int offset)
			throws JargonException {

		log.info("query()");

		log.info("offset:{}", offset);

		log.info("collection parent:{}", this.getCollection().getRootPath());

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = this

		.getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(
						this.getIrodsAccount());
		return collectionAndDataObjectListAndSearchAO
				.listDataObjectsAndCollectionsUnderPath(this.getCollection()
						.getRootPath());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.vircoll.AbstractVirtualCollection#queryCollections(int)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> queryCollections(int offset)
			throws JargonException {

		log.info("queryCollections()");

		log.info("offset:{|}", offset);

		if (this.getCollection().getRootPath() == null
				|| this.getCollection().getRootPath().isEmpty()) {
			throw new JargonException(
					"no collectionParentAbsolutePath provided");
		}

		log.info("collection parent:{}", this.getCollection().getRootPath());

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = this

		.getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(
						this.getIrodsAccount());
		return collectionAndDataObjectListAndSearchAO.listCollectionsUnderPath(
				this.getCollection().getRootPath(), offset);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.vircoll.AbstractVirtualCollection#queryDataObjects(int)
	 */
	@Override
	public List<CollectionAndDataObjectListingEntry> queryDataObjects(int offset)
			throws JargonException {

		log.info("queryDataObjects()");

		log.info("offset:{}", offset);

		if (this.getCollection().getRootPath() == null
				|| this.getCollection().getRootPath().isEmpty()) {
			throw new JargonException(
					"no collectionParentAbsolutePath provided");
		}

		log.info("collection parent:{}", this.getCollection().getRootPath());

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = this

		.getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(
						this.getIrodsAccount());
		return collectionAndDataObjectListAndSearchAO.listDataObjectsUnderPath(
				this.getCollection().getRootPath(), offset);

	}

	public String getCollectionParentAbsolutePath() {
		return this.getCollection().getRootPath();
	}

}
