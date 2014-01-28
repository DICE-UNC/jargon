/**
 * 
 */
package org.irods.jargon.vircoll;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;

/**
 * Abstract model of a virtual collection, which is an arbitrary source that can
 * be serialized into an iRODS file, and which produces an 'ils' like listing.
 * <p/>
 * The function of a virtual collection is to break away from reliance on a
 * hierarchical file tree as the sole arrangement of collections.
 * 
 * @author mikeconway
 * 
 */
public abstract class AbstractVirtualCollection {

	private String name = "";
	private String description = "";
	private String sourcePath = "";
	private VirtualCollectionContext context;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public VirtualCollectionContext getContext() {
		return context;
	}
	
	/**
	 * Handy accessor to grab the <code>IRODSAccessObjectFactory</code> used to communicate with irods
	 * @return
	 */
	protected IRODSAccessObjectFactory irodsAccessObjectFactoryFromContext() {
		if (this.getContext() == null || this.getContext().getIrodsAccessObjectFactory() == null) {
			throw new IllegalStateException("context or irodsAccessObjectFactory not initialized");
		}
		
		return this.getContext().getIrodsAccessObjectFactory();
	}
	
	/**
	 * Handy accessor to grab the <code>IRODSAccount</code> used to communicate with irods
	 * @return
	 */
	protected IRODSAccount irodsAccountFromContext() {
		if (this.getContext() == null || this.getContext().getIrodsAccount() == null) {
			throw new IllegalStateException("context or irodsAccount not initialized");
		}
		
		return this.getContext().getIrodsAccount();
	}

	/**
	 * Save the given abstract virtual collection to the configured sourcePath
	 * 
	 * @throws JargonException
	 */
	public abstract void store() throws JargonException;

	/**
	 * Delete the given abstract virtual collection from the configured
	 * sourcePath
	 * 
	 * @throws JargonException
	 */
	public abstract void delete() throws JargonException;

	public void setContext(VirtualCollectionContext context) {
		this.context = context;
	}

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
	 * Generate a result list based on executing the virtual collection query for collections that are children of this parent.
	 * 
	 * @param offset
	 *            <code>int</code> with the offset into the result set (paging
	 *            may not be supported in all subclasses)
	 * @return <code>List</code> of {@link CollectionAndDataObjectListingEntry}
	 *         with the result of the query
	 * @throws JargonException
	 */
	public abstract List<CollectionAndDataObjectListingEntry> queryCollections(int offset)
			throws JargonException;
	
	
	/**
	 * Generate a result list based on executing the virtual query for data objects that are children of this parent.
	 * 
	 * @param offset
	 *            <code>int</code> with the offset into the result set (paging
	 *            may not be supported in all subclasses)
	 * @return <code>List</code> of {@link CollectionAndDataObjectListingEntry}
	 *         with the result of the query
	 * @throws JargonException
	 */
	public abstract List<CollectionAndDataObjectListingEntry> queryDataObjects(int offset)
			throws JargonException;
	
	
}
