package org.irods.jargon.usertagging;

import org.irods.jargon.core.connection.IRODSAccount;

/**
 * Factory for various services that support tagging of iRODS data objects and collections.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface TaggingServiceFactory {

	/**
	 * Retrieve an instance of <code>FreeTaggingService</code> that supports display and update of user tags as a space-delimited free tag string.
	 * @param irodsAccount <code>IRODSAccount</code> describing the user and desired iRODS host
	 * @return {@link FreeTaggingService} implementation
	 */
	public abstract FreeTaggingService instanceFreeTaggingService(
			final IRODSAccount irodsAccount);

	/**
	 * Retrieve an instance of <code>IRODSTaggingService</code> that supports display and update of user tags by maintaining special AVU's
	 * @param irodsAccount <code>IRODSAccount</code> describing the user and desired iRODS host
	 * @return {@link IRODSTaggingService} implementation
	 */
	public abstract IRODSTaggingService instanceIrodsTaggingService(
			final IRODSAccount irodsAccount);

	/**
	 * Retrieve an instance of <code>UserTagCloudService</code> that supports display of a tag cloud for a given user
	 * @param irodsAccount <code>IRODSAccount</code> describing the user and desired iRODS host
	 * @return {@link UserTagCloudService} implementation
	 */
	public abstract UserTagCloudService instanceUserTagCloudService(
			final IRODSAccount irodsAccount);

}