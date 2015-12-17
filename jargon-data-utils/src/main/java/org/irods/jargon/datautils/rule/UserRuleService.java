package org.irods.jargon.datautils.rule;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Service to maintain and execute user defined rules stored within iRODS. Thas
 * allows tagging of rules with metadata that allows them to be integrated into
 * applications that want to treat user-defined rules as server-defined 'macros'
 * that can be applied to data of certain types, and appear in interfaces as
 * modules.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface UserRuleService {

	/**
	 * List the user defined rules that exist for the user in their user rules
	 * catalog in their iRODS home directory for the given server
	 *
	 * @return <code>List</code> of {@link UserRuleDefinition}. This will be
	 *         empty if no rules are found
	 * @throws JargonException
	 */
	List<UserRuleDefinition> listUserRulesInUserHomeDir()
			throws JargonException;

}