/**
 * 
 */
package org.irods.jargon.part.policydriven.client;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.part.exception.PartException;

/**
 * Factory for obtaining services useful to a client application that interacts with policies (as opposed to applications that manage policies within iRODS.
 * @author Mike Conway - DICE (www.irods.org)
 */
public interface PolicyDrivenClientFactory {

	/**
	 * Create an implementation of the <code>ClientPolicyHelper</code> interface.
	 * @return {@link org.irods.jargon.part.policydriven.client.ClientPolicyHelperImpl} implementation.
	 * @throws PartException
	 */
	ClientPolicyHelper instanceClientPolicyHelper() throws PartException;
	
}
