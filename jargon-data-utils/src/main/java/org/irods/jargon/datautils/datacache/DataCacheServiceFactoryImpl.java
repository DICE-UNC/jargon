/**
 *
 */
package org.irods.jargon.datautils.datacache;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

/**
 * Factory for creating {@code DataCacheService} components.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DataCacheServiceFactoryImpl implements DataCacheServiceFactory {

	private final IRODSAccessObjectFactory irodsAccessObjectFactory;

	public DataCacheServiceFactoryImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("irodsAccessObjectFactory is null");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;

	}

	@Override
	public DataCacheService instanceDataCacheService(final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		return new DataCacheServiceImpl(irodsAccessObjectFactory, irodsAccount);
	}

	@Override
	public DataCacheService instanceNoEncryptDataCacheService(final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		return new NoEncryptDataCacheServiceImpl(irodsAccessObjectFactory, irodsAccount);
	}

}
