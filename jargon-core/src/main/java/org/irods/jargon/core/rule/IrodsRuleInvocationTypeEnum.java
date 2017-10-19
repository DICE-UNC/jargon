/**
 * 
 */
package org.irods.jargon.core.rule;

/**
 * Indicates type of iRODS rule engine (e.g. iRODS rule language, Python rule) a
 * rule invocate is bound for. <code>AUTO_DETECT</code> indicates a request to
 * have the service guess the type of rule it is.
 * 
 * 
 * @author Mike Conway
 *
 */
public enum IrodsRuleInvocationTypeEnum {
	IRODS, PYTHON, AUTO_DETECT

}
