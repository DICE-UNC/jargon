package org.irods.jargon.vircoll;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.PagingAwareCollectionListing;

/**
 * Interface marks a virtual collection as allowing query by path hints
 * 
 * @author Mike Conway
 *
 * @param <T>
 */
public interface PathHintable {

	/**
	 * Generate a result list based on executing the virtual collection query
	 * providing extra info, such as an absolute path
	 * 
	 * @param offset
	 *            <code>int</code> with the offset into the result set (paging
	 *            may not be supported in all subclasses)
	 * @param path
	 *            <code>String</code> with an additional path queue that may be
	 *            used to tune the retrieved data. This may not be supported by
	 *            all subclasses
	 * @return {@link PagingAwareCollectionListing} with the result of the query
	 * @throws JargonException
	 */
	public abstract PagingAwareCollectionListing queryAll(String path,
			int offset) throws JargonException;

}