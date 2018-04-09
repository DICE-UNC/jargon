/**
 *
 */
package org.irods.jargon.datautils.avuautocomplete;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.datautils.metadatamanifest.MetadataManifestProcessorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - NIEHS
 *
 */
public class AvuAutocompleteServiceImpl extends AbstractJargonService implements AvuAutocompleteService {
	
	public static final Logger log = LoggerFactory
			.getLogger(AvuAutocompleteServiceImpl.class);
	
	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public AvuAutocompleteServiceImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 *
	 */
	public AvuAutocompleteServiceImpl() {
	}

	@Override
	public AvuSearchResult gatherAvailableAttributes(final String prefix, final int offset,
			final AvuTypeEnum avuTypeEnum) throws JargonException {
		
		log.info("gatherAvailableAttributes()");
		
		if (prefix == null || prefix.isEmpty()) {
			throw new IllegalArgumentException("null prefix");
		}
		
		if (offset < 0) {
			throw new IllegalArgumentException("offset must be >= 0");
		}
		
		if (avuTypeEnum == null) {
			throw new IllegalArgumentException("null avuTypeEnum");
		}
		log.info("prefix:{}", prefix);
		log.info("offset:{}", offset);
		log.info("avuTypeEnum:{}", avuTypeEnum);
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.avuautocomplete.AvuAutocompleteService#
	 * gatherAvailableValues(java.lang.String, java.lang.String, int)
	 */
	@Override
	public AvuSearchResult gatherAvailableValues(final String forAttribute, final String prefix, final int offset,
			final AvuTypeEnum avuTypeEnum) throws JargonException {
		return null;

	}

}
