package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.PagingAwareCollectionListing;

public interface CollectionIteratorAO {

	public abstract PagingAwareCollectionListing retrivePagingAwareCollectionListing(
			String absolutePathToParent) throws FileNotFoundException,
			JargonException;

}