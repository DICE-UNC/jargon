package org.irods.jargon.usertagging;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

/**
 * Factory for different user tagging service classes.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TaggingServiceFactoryImpl implements TaggingServiceFactory {

	private final IRODSAccessObjectFactory irodsAccessObjectFactory;

	public TaggingServiceFactoryImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException(
					"irodsAccessObjectFactory is null");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;

	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.usertagging.TaggingServiceFactory#instanceFreeTaggingService(org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public FreeTaggingService instanceFreeTaggingService(
			final IRODSAccount irodsAccount) {

		checkDependencies();

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		return FreeTaggingServiceImpl.instance(irodsAccessObjectFactory,
				irodsAccount);

	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.usertagging.TaggingServiceFactory#instanceIrodsTaggingService(org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public IRODSTaggingService instanceIrodsTaggingService(
			final IRODSAccount irodsAccount) {

		checkDependencies();

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		return IRODSTaggingServiceImpl.instance(irodsAccessObjectFactory,
				irodsAccount);

	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.usertagging.TaggingServiceFactory#instanceUserTagCloudService(org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public UserTagCloudService instanceUserTagCloudService(
			final IRODSAccount irodsAccount) {

		checkDependencies();

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		return UserTagCloudServiceImpl.instance(irodsAccessObjectFactory,
				irodsAccount);

	}

	private void checkDependencies() {
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException(
					"the irodsAccessObjectFactory was not set for this instance");
		}
	}

}
