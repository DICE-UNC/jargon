package org.irods.jargon.datautils.datacache;

import org.irods.jargon.core.connection.IRODSAccount;

/**
 * Interface describing a factory to create a {@code DataCacheService}
 * component.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface DataCacheServiceFactory {

	/**
	 * Get the normal encrypting data cache service
	 * 
	 * @param irodsAccount {@link IRODSAccount}
	 * @return {@link DataCacheService} implementation
	 */
	public abstract DataCacheService instanceDataCacheService(IRODSAccount irodsAccount);

	/**
	 * Get a data cache service that does not encrypt
	 * 
	 * @param irodsAccount {@link IRODSAccount}
	 * @return {@link DataCacheService} implementation
	 */
	public DataCacheService instanceNoEncryptDataCacheService(IRODSAccount irodsAccount);

}