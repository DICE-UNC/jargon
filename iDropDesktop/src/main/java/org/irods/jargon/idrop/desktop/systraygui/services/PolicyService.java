package org.irods.jargon.idrop.desktop.systraygui.services;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.part.exception.PartException;
import org.irods.jargon.part.policy.domain.Policy;
import org.irods.jargon.part.policydriven.PolicyManagerFactory;
import org.irods.jargon.part.policydriven.PolicyManagerFactoryImpl;
import org.irods.jargon.part.policydriven.client.*;
import org.slf4j.LoggerFactory;

/**
 * Service to provide policy-based information stored within iRODS.
 * @author Mike Conway - DICE (www.irods.org)
 */
public class PolicyService {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(PolicyService.class);
    private PolicyDrivenClientFactory policyDrivenClientFactory;

    public static PolicyService instance(final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount) throws IdropException {
        return new PolicyService(irodsAccessObjectFactory, irodsAccount);
    }

    private PolicyService(final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount) throws IdropException {

        if (irodsAccessObjectFactory == null) {
            throw new IdropException("null irodsAccessObjectFactory");
        }

        if (irodsAccount == null) {
            throw new IdropException("irodsAccount is null");
        }

        log.info("building policyManagerFactory");
        PolicyManagerFactory policyManagerFactory;
        try {
            policyManagerFactory = PolicyManagerFactoryImpl.instance(irodsAccessObjectFactory, irodsAccount);
            policyDrivenClientFactory = PolicyDrivenClientFactoryImpl.instance(irodsAccessObjectFactory, irodsAccount, policyManagerFactory);

        } catch (PartException ex) {
            Logger.getLogger(PolicyService.class.getName()).log(Level.SEVERE, null, ex);
        }

        log.info("policyManagerFactory is created");

    }

    /**
     * Given a collection, return the Policy associated with the collection, or null if no policy is bound
     * to the collection
     * @param collectionAbsolutePath <code>String</code> absolute path to the iRODS collection for which
     * a policy will be found.
     * @return <code>Policy</code> that controls the collection, or null if no policy is bound to the collection
     * @throws IdropException
     */
    public Policy getPolicyForCollection(final String collectionAbsolutePath) throws IdropException {
        try {
            ClientPolicyHelper clientPolicyHelper = policyDrivenClientFactory.instanceClientPolicyHelper();
            return clientPolicyHelper.getRelevantPolicy(collectionAbsolutePath);
        } catch (PartException ex) {
            Logger.getLogger(PolicyService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("error getting policy for collection", ex);
        }


    }
}
