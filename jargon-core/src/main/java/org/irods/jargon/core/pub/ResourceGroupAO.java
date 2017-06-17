package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Access object that represents resource groups and related operations in
 * iRODS.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface ResourceGroupAO {

	/**
	 * Convenience method to obtain a list of resource group names in the
	 * current zone
	 *
	 * @return {@code List<String>} of resoruce group names
	 * @throws JargonException
	 */
	List<String> listResourceGroupNames() throws JargonException;

}