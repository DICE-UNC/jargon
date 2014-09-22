package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoMoreDataException;
import org.irods.jargon.core.query.PagingAwareCollectionListing;

public interface CollectionPagerAO {

	public abstract PagingAwareCollectionListing retrieveFirstPageUnderParent(
			String irodsAbsolutePath) throws FileNotFoundException,
			NoMoreDataException, JargonException;

}