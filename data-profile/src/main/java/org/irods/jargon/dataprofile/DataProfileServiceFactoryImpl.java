/**
 *
 */
package org.irods.jargon.dataprofile;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;

/**
 * Simple class that produces a configured instance of the DataProfileService
 * with the default data type resolution service configured
 *
 * @author Mike Conway - DICE
 *
 */
public class DataProfileServiceFactoryImpl extends AbstractJargonService {

	/**
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 */
	public DataProfileServiceFactoryImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 *
	 */
	public DataProfileServiceFactoryImpl() {
	}

	/**
	 * Factory method to create a properly provisioned service instance
	 * 
	 * @return {@link DataProfileService}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public DataProfileService instanceDataProfileService() throws JargonException {

		DataTypeResolutionService dataTypeResolutionService = new DataTypeResolutionServiceImpl(
				getIrodsAccessObjectFactory(), getIrodsAccount());
		return new DataProfileServiceImpl(getIrodsAccessObjectFactory(), getIrodsAccount(), dataTypeResolutionService);

	}

}
