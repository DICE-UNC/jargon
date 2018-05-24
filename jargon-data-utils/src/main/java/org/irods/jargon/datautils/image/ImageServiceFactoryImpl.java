package org.irods.jargon.datautils.image;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

/**
 * Factory for different image service classes
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ImageServiceFactoryImpl implements ImageServiceFactory {

	private final IRODSAccessObjectFactory irodsAccessObjectFactory;

	public ImageServiceFactoryImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("irodsAccessObjectFactory is null");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.datautils.image.ImageServiceFactory#instanceThumbnailService
	 * (org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public ThumbnailService instanceThumbnailService(final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		return new ThumbnailServiceImpl(irodsAccessObjectFactory, irodsAccount);
	}

}
