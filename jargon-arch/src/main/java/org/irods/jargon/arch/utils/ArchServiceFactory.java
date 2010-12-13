package org.irods.jargon.arch.utils;

import org.irods.jargon.arch.exception.ArchException;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.part.policydriven.PolicyDrivenRulesManager;
import org.irods.jargon.part.policydriven.PolicyDrivenRulesManagerImpl;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceConfigException;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceManager;
import org.irods.jargon.part.policydriven.PolicyManager;
import org.irods.jargon.part.policydriven.PolicyManagerImpl;
import org.irods.jargon.part.policydriven.SeriesManager;
import org.irods.jargon.part.policydriven.SeriesManagerImpl;

public interface ArchServiceFactory {

	/**
	 * Obtain a service object that can manipulate high level policy-driven apps
	 * @param irodsAccount <code>IRODSAccount</code> that indicates the target iRODS
	 * @return <code>PolicyDrivenRulesManager</code> service object
	 * @throws ArchException
	 */
	public PolicyDrivenServiceManager instancePolicyDrivenServiceManager(
			IRODSAccount irodsAccount) throws ArchException;

	/**
	 * Obtain a service object that can manipulate rule definitions and collections
	 * @param irodsAccount <code>IRODSAccount</code> that indicates the target iRODS
	 * @return <code>PolicyDrivenRulesManager</code> service object
	 * @throws ArchException
	 */
	public PolicyDrivenRulesManager instancePolicyDrivenRulesManager(final IRODSAccount irodsAccount)
			throws ArchException;

	/**
	 * Obtain a service object that can manipulate policy definitions and collections
	 * @param irodsAccount <code>IRODSAccount</code> that indicates the target iRODS
	 * @return <code>PolicyManager</code> service object
	 * @throws ArchException
	 */
	public PolicyManager instancePolicyManager(final IRODSAccount irodsAccount) throws ArchException;

	/**
	 * Obtain a service object that can manipulate series definitions and collections
	 * @param irodsAccount <code>IRODSAccount</code> that indicates the target iRODS
	 * @return <code>SeriesManager</code> service object
	 * @throws ArchException
	 */
	public SeriesManager instanceSeriesManager(final IRODSAccount irodsAccount) throws ArchException;

}