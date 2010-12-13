/**
 * 
 */
package org.irods.jargon.part.policydriven;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.part.exception.PartException;

/**
 * Implementation of a factory to create various policy management services
 * FIXME: implement for all
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PolicyManagerFactoryImpl implements PolicyManagerFactory {
	
	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	private final IRODSAccount irodsAccount;
	
	public static PolicyManagerFactory instance(final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount) throws PartException {
		return new PolicyManagerFactoryImpl(irodsAccessObjectFactory, irodsAccount);
	}
	
	
	private PolicyManagerFactoryImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount) throws PartException {
		if (irodsAccessObjectFactory == null) {
			throw new PartException("null irodsAccessObjectFactory");
		}
		
		if (irodsAccount == null) {
			throw new PartException("null irodsAccount");
		}
		
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;

	}
	

	/* (non-Javadoc)
	 * @see org.irods.jargon.part.policydriven.PolicyManagerFactory#getPolicyManager()
	 */
	@Override
	public PolicyManager getPolicyManager() throws PartException {
		
		try {
			return new PolicyManagerImpl(irodsAccessObjectFactory, irodsAccount);
		} catch (PolicyDrivenServiceConfigException e) {
			throw new PartException("error creating PolicyManager", e);
		}
		
	}

}
