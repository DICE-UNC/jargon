/**
 * 
 */
package org.irods.jargon.vircoll;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.service.AbstractJargonService;

/**
 * Abstract model of a service that can execute operations on a given virtual
 * collection. This means CRUD operations on the virtual collection, as well as
 * execution of the embedded query
 * 
 * @author mikeconway
 * 
 */
public abstract class AbstractVirtualCollectionExecutor<T extends AbstractVirtualCollection>
		extends AbstractJargonService {

	private final T collection;

	/**
	 * Generate a result list based on executing the virtual collection query
	 * 
	 * @param offset
	 *            <code>int</code> with the offset into the result set (paging
	 *            may not be supported in all subclasses)
	 * @return <code>List</code> of {@link CollectionAndDataObjectListingEntry}
	 *         with the result of the query
	 * @throws JargonException
	 */
	public abstract List<CollectionAndDataObjectListingEntry> queryAll(
			int offset) throws JargonException;

	/**
	 * Generate a result list based on executing the virtual collection query
	 * for collections that are children of this parent.
	 * 
	 * @param offset
	 *            <code>int</code> with the offset into the result set (paging
	 *            may not be supported in all subclasses)
	 * @return <code>List</code> of {@link CollectionAndDataObjectListingEntry}
	 *         with the result of the query
	 * @throws JargonException
	 */
	public abstract List<CollectionAndDataObjectListingEntry> queryCollections(
			int offset) throws JargonException;

	/**
	 * Generate a result list based on executing the virtual query for data
	 * objects that are children of this parent.
	 * 
	 * @param offset
	 *            <code>int</code> with the offset into the result set (paging
	 *            may not be supported in all subclasses)
	 * @return <code>List</code> of {@link CollectionAndDataObjectListingEntry}
	 *         with the result of the query
	 * @throws JargonException
	 */
	public abstract List<CollectionAndDataObjectListingEntry> queryDataObjects(
			int offset) throws JargonException;

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
	protected AbstractVirtualCollectionExecutor(T collection,
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);

		if (collection == null) {
			throw new IllegalArgumentException("null collection");
		}

		this.collection = collection;
	}
}
