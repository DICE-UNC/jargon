/**
 * 
 */
package org.irods.jargon.part.policydriven.client;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.part.exception.DataNotFoundException;
import org.irods.jargon.part.exception.PartException;
import org.irods.jargon.part.policy.domain.Policy;
import org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceConfigException;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceManager;
import org.irods.jargon.part.policydriven.PolicyManager;
import org.irods.jargon.part.policydriven.PolicyManagerFactory;
import org.irods.jargon.part.policydriven.PolicyManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class that can assist a policy-driven client to discover relevant
 * policies
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ClientPolicyHelperImpl implements ClientPolicyHelper {

	public Logger log = LoggerFactory.getLogger(this.getClass());

	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	private final IRODSAccount irodsAccount;
	private final PolicyManagerFactory policyManagerFactory;

	protected ClientPolicyHelperImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount, final PolicyManagerFactory policyManagerFactory) throws PartException {

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
	 * @see org.irods.jargon.part.policydriven.client.ClientPolicyHelper#getRelevantPolicy(java.lang.String)
	 */
	public Policy getRelevantPolicy(final String irodsCollectionAbsolutePath)
			throws PartException {

		if (irodsCollectionAbsolutePath == null
				|| irodsCollectionAbsolutePath.isEmpty()) {
			throw new PartException(
					"null or empty irods collection absolute path");
		}
		
		String actualIrodsCollectionAbsolutePath = "";
		try {
			IRODSFile irodsFile = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount).instanceIRODSFile(irodsCollectionAbsolutePath);
			if (irodsFile.isDirectory()) {
				log.debug("a directory was passed, use that");
				actualIrodsCollectionAbsolutePath = irodsFile.getAbsolutePath();
			} else {
				log.debug("this is a file, go ahead and use the parent");
				actualIrodsCollectionAbsolutePath = irodsFile.getParent();
			}
		} catch (JargonException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		log.info("getting relevant policy for collection:{}",
				irodsCollectionAbsolutePath);
		Policy policy = null;

		// build a query to look for a policy marker for this collection
		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		AVUQueryElement avuQueryElement = null;
		try {
			avuQueryElement = AVUQueryElement
					.instanceForValueQuery(
							AVUQueryPart.ATTRIBUTE,
							AVUQueryOperatorEnum.EQUAL,
							PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_SERIES_TO_POLICY_MARKER_ATTRIBUTE);
			avuQueryElements.add(avuQueryElement);
			CollectionAO collectionAO = this.irodsAccessObjectFactory
					.getCollectionAO(irodsAccount);
			List<MetaDataAndDomainData> policyList = collectionAO
					.findMetadataValuesByMetadataQueryForCollection(
							avuQueryElements, actualIrodsCollectionAbsolutePath, 0);

			// any policy found?
			if (policyList.isEmpty()) {
				// null will be returned
				return null;
			}

			// get the policy pointed to by the series -> policy binding
			MetaDataAndDomainData seriesToPolicyBinding = policyList.get(0);
			String policyName = seriesToPolicyBinding.getAvuValue();
			log.info("collection has bound policy:{}", policyName);

			// locate the policy with the given policy name
			PolicyManager policyManager = policyManagerFactory.getPolicyManager();

			XMLToObjectUnmarshaller unmarshaller = new XMLToObjectUnmarshaller();
			policy = policyManager.getPolicyFromPolicyRepository(policyName,
					unmarshaller);

		} catch (JargonQueryException e) {
			log.error("Query exception for avu query: {}", avuQueryElement);
			throw new PartException("JargonQueryException", e);
		} catch (JargonException e) {
			log.error("Jargon exception for avu query: {}", avuQueryElement);
			throw new PartException("JargonException", e);
		} catch (PolicyDrivenServiceConfigException e) {
			log
					.error(
							"PolicyDrivenServiceConfigException obtaining policy for: {}",
							irodsCollectionAbsolutePath);
			throw new PartException("PolicyDrivenServiceConfigException", e);
		} catch (DataNotFoundException e) {
			log.error("Unable to find policy for collection {}",
					irodsCollectionAbsolutePath);
			throw new PartException("PolicyDrivenServiceConfigException", e);
		}

		if (log.isInfoEnabled()) {
			if (policy == null) {
				log.info("policy is null");
			} else {
				log.info("found policy:{}", policy);
			}
		}

		return policy;

	}

}
