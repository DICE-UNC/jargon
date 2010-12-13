
package org.irods.jargon.part.policy.parameter;

import org.irods.jargon.part.exception.PartException;

/**
 * Interface for a resolver that will produce a list of dynamically-derived attribute values from iRODS properties
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface DynamicPropertyResolver {
	
	public DynamicPropertyValues resolve() throws PartException;

}
