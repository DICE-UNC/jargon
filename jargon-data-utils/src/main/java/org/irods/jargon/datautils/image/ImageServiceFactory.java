package org.irods.jargon.datautils.image;

import org.irods.jargon.core.connection.IRODSAccount;

/**
 * Factory for various services that support image processing for iRODS
 * applications
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface ImageServiceFactory {

	/**
	 * Retrieve an instance of <code>ImageServiceFactory</code> that supports
	 * image processing for iRODS
	 * 
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> describing the user and desired
	 *            iRODS host
	 * @return {@link ImageServiceFactory} implementation
	 */
	public abstract ThumbnailService instanceThumbnailService(
			final IRODSAccount irodsAccount);

}