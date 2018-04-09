/**
 *
 */
package org.irods.jargon.datautils.avuautocomplete;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;

/**
 * @author Mike Conway - NIEHS
 *
 */
public class AvuAutocompleteServiceImpl extends AbstractJargonService implements AvuAutocompleteService {

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
