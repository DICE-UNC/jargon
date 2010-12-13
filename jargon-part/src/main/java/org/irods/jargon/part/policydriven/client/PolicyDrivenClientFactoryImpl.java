/**
 * 
 */
package org.irods.jargon.part.policydriven.client;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.part.exception.PartException;
import org.irods.jargon.part.policydriven.PolicyManagerFactory;

/**
 * Implementation of a factory to create policy client services.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PolicyDrivenClientFactoryImpl implements PolicyDrivenClientFactory {
	
	private final IRODSAccount irodsAccount;
	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	private final PolicyManagerFactory policyManagerFactory;
	
	/**
	 * Create an instance of this factory through a static initializer.
	 * @param irodsAccessObjectFactory 
	 * @param irodsAccount
	 * @param policyManagerFactory
	 * @return
	 * @throws PartException
	 */
	public static PolicyDrivenClientFactory instance(final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount, final PolicyManagerFactory policyManagerFactory) throws PartException {
		return new PolicyDrivenClientFactoryImpl(irodsAccessObjectFactory, irodsAccount, policyManagerFactory);
	}
	
	private PolicyDrivenClientFactoryImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount, final PolicyManagerFactory policyManagerFactory) throws PartException {
		if (irodsAccessObjectFactory == null) {
			throw new PartException("irodsAccessObjectFactory is null");
		}

		if (irodsAccount == null) {
			throw new PartException("irodsAccount is null");
		}
		
		if (policyManagerFactory == null) {
			throw new PartException("policyManagerFactory is null");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
		this.policyManagerFactory = policyManagerFactory;
		
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.part.policydriven.client.PolicyDrivenClientFactory#instanceClientPolicyHelper(org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl, org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public ClientPolicyHelper instanceClientPolicyHelper() throws PartException {
		return new ClientPolicyHelperImpl(irodsAccessObjectFactory, irodsAccount, policyManagerFactory);
	}

}
