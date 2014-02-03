/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.IRODSFileSystemAOImpl;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.vircoll.AbstractVirtualCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a virtual collection that is an actual iRODS collection (parent
 * folders are virtual collections themselves)
 * 
 * @author mikeconway
 * 
 */
public class CollectionBasedVirtualCollection extends AbstractVirtualCollection {

	private final String collectionParentAbsolutePath;
	static Logger log = LoggerFactory.getLogger(IRODSFileSystemAOImpl.class);

	public static final String DESCRIPTION_KEY_HOME = "virtual.collections.home";
	public static final String DESCRIPTION_KEY_ROOT = "virtual.collections.root";

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

		if (collectionParentAbsolutePath == null
				|| collectionParentAbsolutePath.isEmpty()) {
			throw new JargonException(
					"no collectionParentAbsolutePath provided");
		}

		log.info("collection parent:{}", collectionParentAbsolutePath);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = this
				.getContext()
				.getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(
						this.getContext().getIrodsAccount());
		return collectionAndDataObjectListAndSearchAO
				.listDataObjectsAndCollectionsUnderPath(collectionParentAbsolutePath);

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

		if (collectionParentAbsolutePath == null
				|| collectionParentAbsolutePath.isEmpty()) {
			throw new JargonException(
					"no collectionParentAbsolutePath provided");
		}

		log.info("collection parent:{}", collectionParentAbsolutePath);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = this
				.getContext()
				.getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(
						this.getContext().getIrodsAccount());
		return collectionAndDataObjectListAndSearchAO.listCollectionsUnderPath(
				collectionParentAbsolutePath, offset);

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

		if (collectionParentAbsolutePath == null
				|| collectionParentAbsolutePath.isEmpty()) {
			throw new JargonException(
					"no collectionParentAbsolutePath provided");
		}

		log.info("collection parent:{}", collectionParentAbsolutePath);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = this
				.getContext()
				.getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(
						this.getContext().getIrodsAccount());
		return collectionAndDataObjectListAndSearchAO.listDataObjectsUnderPath(
				collectionParentAbsolutePath, offset);

	}

	public String getCollectionParentAbsolutePath() {
		return collectionParentAbsolutePath;
	}

	@Override
	public void store() throws JargonException {
		// does nothing

	}

	@Override
	public void delete() throws JargonException {
		// does nothing

	}

	/**
	 * @param collectionParentAbsolutePath
	 *            <code>String</code> with the parent path of this virtual
	 *            collection
	 */
	CollectionBasedVirtualCollection(final String collectionParentAbsolutePath) {
		super();

		if (collectionParentAbsolutePath == null
				|| collectionParentAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null collectionParentAbsolutePath");
		}

		this.collectionParentAbsolutePath = collectionParentAbsolutePath;
	}

}
