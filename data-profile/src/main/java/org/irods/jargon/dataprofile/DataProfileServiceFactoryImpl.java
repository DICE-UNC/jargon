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
	 * @param irodsAccount
	 */
	public DataProfileServiceFactoryImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * 
	 */
	public DataProfileServiceFactoryImpl() {
	}

	public DataProfileService instanceDataProfileService()
			throws JargonException {

		DataTypeResolutionService dataTypeResolutionService = new DataTypeResolutionServiceImpl(
				this.getIrodsAccessObjectFactory(), this.getIrodsAccount());
		return new DataProfileServiceImpl(this.getIrodsAccessObjectFactory(),
				this.getIrodsAccount(), dataTypeResolutionService);

	}

}
