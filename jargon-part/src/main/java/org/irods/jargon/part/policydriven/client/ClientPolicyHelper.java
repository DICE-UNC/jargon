package org.irods.jargon.part.policydriven.client;

import org.irods.jargon.part.exception.PartException;
import org.irods.jargon.part.policy.domain.Policy;

public interface ClientPolicyHelper {

	/**
	 * Given an absolute path, find a policy that is bound to the collection.
	 * This currently is limited to a policy bound to the collection at the path
	 * given. In the future, this can be extended to a recursive search up the
	 * tree. NOTE: this method returns null if no policy is found.
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS query.
	 * @return <code>Policy</code> that applies to the collection, or
	 *         <code>null</code> if no applicable policy is found.
	 * @throws PartException
	 */
	public abstract Policy getRelevantPolicy(
			final String irodsCollectionAbsolutePath) throws PartException;

}