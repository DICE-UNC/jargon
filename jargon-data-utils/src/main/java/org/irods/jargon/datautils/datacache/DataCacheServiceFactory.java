package org.irods.jargon.datautils.datacache;

import org.irods.jargon.core.connection.IRODSAccount;

/**
 * Interface describing a factory to create a <code>DataCacheService</code>
 * component.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface DataCacheServiceFactory {

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.datautils.image.ImageServiceFactory#instanceThumbnailService
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	public abstract DataCacheService instanceDataCacheService(
			IRODSAccount irodsAccount);

}