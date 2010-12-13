/**
 * 
 */
package org.irods.jargon.part.policy.parameter;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.part.exception.PartException;

/**
 * Abstract superclass for dynamic property resolvers.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
 abstract class AbstractDynamicPropertyResolver implements DynamicPropertyResolver {

	 private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	 private final IRODSAccount irodsAccount;
	 
	 /**
	  * Create an instance of a dynamic property resolver, with the necessary objects to access iRODS attributes.
	  * @param irodsAccessObjectFactory <code>IRODSAccessObjectFactory</code> that can create iRODS access objects.
	  * @param irodsAccount <code>IRODSAccount</code> with iRODS connection information.
	  * @throws PartException
	  */
	protected AbstractDynamicPropertyResolver(final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount) throws PartException {
		
		if (irodsAccessObjectFactory == null) {
			throw new PartException("null irodsAccessObjectFactory");
		}
		
		if (irodsAccount == null) {
			throw new PartException("null irodsAccount");
		}
		
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
		
	}

	protected IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	protected IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}
	
}
