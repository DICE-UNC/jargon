/**
 * 
 */
package org.irods.jargon.part.policydriven;

import org.irods.jargon.part.exception.PartException;

/**
 * Interface for a factory to create policy manager services.
 * TODO: code currently uses constructors, switch them over to protected initializers and creation by factory
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface PolicyManagerFactory {

	PolicyManager getPolicyManager() throws PartException;
	
}
