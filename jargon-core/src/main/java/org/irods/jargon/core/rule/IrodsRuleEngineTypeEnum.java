/**
 * 
 */
package org.irods.jargon.core.rule;

/**
 * Enumerates the type of rule to be processed. This enum describes the type of
 * a specific rule instance, and is distinct from the
 * {@link IrodsRuleInvocationTypeEnum} value that represents a parameter in a
 * user request to process a rule. That is, this is the actual type of the rule,
 * the <code>IrodsRuleInvocationTypeEnum</code> is the user request. A user may
 * request 'Auto Detection', resulting in an assignment of a type.
 * 
 * @author Mike Conway
 *
 */
public enum IrodsRuleEngineTypeEnum {
	IRODS, PYTHON, UNKNOWN
}
