/**
 * 
 */
package org.irods.jargon.arch.utils;

import org.irods.jargon.arch.exception.ArchException;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.part.policydriven.PolicyDrivenRulesManager;
import org.irods.jargon.part.policydriven.PolicyDrivenRulesManagerImpl;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceConfigException;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceManager;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceManagerImpl;
import org.irods.jargon.part.policydriven.PolicyManager;
import org.irods.jargon.part.policydriven.PolicyManagerImpl;
import org.irods.jargon.part.policydriven.SeriesManager;
import org.irods.jargon.part.policydriven.SeriesManagerImpl;

/**
 * Immutable service factory to create necessary services.  This centralizes the creation of the high-level service objects used by
 * the application.  It is difficult to inject these services directy into controllers as they must be initialized with a 
 * specific <code>IRODSAccount</code> at create time.
 * 
 * This factory is suitable for wiring in Spring and injection into controllers
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public final class ArchServiceFactoryImpl implements ArchServiceFactory {
	
	transient final IRODSAccessObjectFactoryI irodsAccessObjectFactory;
	
	public static ArchServiceFactory instance(final IRODSAccessObjectFactoryI irodsAccessObjectFactory) throws ArchException {
		return new ArchServiceFactoryImpl(irodsAccessObjectFactory);
	}
	
	private ArchServiceFactoryImpl(final IRODSAccessObjectFactoryI irodsAccessObjectFactory) throws ArchException {
		if (irodsAccessObjectFactory == null) {
			throw new ArchException("null irodsAccessObjectFactory");
		}
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.arch.utils.ArchServiceFactory#instancePolicyDrivenServiceManager(org.irods.jargon.core.connection.IRODSAccount)
	 */
	public PolicyDrivenServiceManager instancePolicyDrivenServiceManager(final IRODSAccount irodsAccount) throws ArchException {
		if (irodsAccount == null) {
			throw new ArchException("irodsAccount is null");
		}
		try {
			return new PolicyDrivenServiceManagerImpl(irodsAccessObjectFactory, irodsAccount);
		} catch (PolicyDrivenServiceConfigException e) {
			throw new ArchException("unable to create policyDrivenServiceManager due to underlying exception", e);
		}
	}
	
	/**
	 * Obtain a service object that can manipulate rule definitions and collections
	 * @param irodsAccount <code>IRODSAccount</code> that indicates the target iRODS
	 * @return <code>PolicyDrivenRulesManager</code> service object
	 * @throws ArchException
	 */
	public PolicyDrivenRulesManager instancePolicyDrivenRulesManager(final IRODSAccount irodsAccount) throws ArchException {
		if (irodsAccount == null) {
			throw new ArchException("irodsAccount is null");
		}
		try {
			return new PolicyDrivenRulesManagerImpl(irodsAccessObjectFactory, irodsAccount);
		} catch (PolicyDrivenServiceConfigException e) {
			throw new ArchException("unable to create policyDrivenRulesManager due to underlying exception", e);
		}
	}
	
	/**
	 * Obtain a service object that can manipulate policy definitions and collections
	 * @param irodsAccount <code>IRODSAccount</code> that indicates the target iRODS
	 * @return <code>PolicyManager</code> service object
	 * @throws ArchException
	 */
	public PolicyManager instancePolicyManager(final IRODSAccount irodsAccount) throws ArchException {
		if (irodsAccount == null) {
			throw new ArchException("irodsAccount is null");
		}
		try {
			return new PolicyManagerImpl(irodsAccessObjectFactory, irodsAccount);
		} catch (PolicyDrivenServiceConfigException e) {
			throw new ArchException("unable to create policyManager due to underlying exception", e);
		}
	}
	
	/**
	 * Obtain a service object that can manipulate series definitions and collections
	 * @param irodsAccount <code>IRODSAccount</code> that indicates the target iRODS
	 * @return <code>SeriesManager</code> service object
	 * @throws ArchException
	 */
	public SeriesManager instanceSeriesManager(final IRODSAccount irodsAccount) throws ArchException {
		if (irodsAccount == null) {
			throw new ArchException("irodsAccount is null");
		}
		try {
			return new SeriesManagerImpl(irodsAccessObjectFactory, irodsAccount);
		} catch (PolicyDrivenServiceConfigException e) {
			throw new ArchException("unable to create seriesManager due to underlying exception", e);
		}
	}

}
