/**
 * 
 */
package org.irods.jargon.core.rule;

/**
 * Indicates type of iRODS rule engine (e.g. iRODS rule language, Python rule) a
 * rule invocate is bound for. <code>AUTO_DETECT</code> indicates a request to
 * have the service guess the type of rule it is.
 * <p/>
 * Note that <code>SPECIFIED</code> indicates that the full name of the target
 * rule engine will be provided by the user.
 * 
 * @author Mike Conway
 *
 */
public enum IrodsRuleInvocationTypeEnum {
	IRODS, PYTHON, JAVASCRIPT, AUTO_DETECT, SPECIFIED

}
