/**
 * 
 */
package org.irods.jargon.core.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for parsing rules for processing by Jargon.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
class RuleParsingUtils {

	private static Logger log = LoggerFactory.getLogger(RuleParsingUtils.class);

	/**
	 * Private constructor
	 */
	private RuleParsingUtils() {
	}

	/**
	 * Given a rule input parameter value which has already been separated from
	 * other input parameters by the , or % delimiter, separate into parameter
	 * name and parameter value. This essentially splits on the first '=' sign.
	 * 
	 * @param parameter
	 *            <code>String<code> which should reflect an individual rule parameter in *parmName=parmValue format
	 * @return {@link RuleInputParameter}
	 */
	static RuleInputParameter parseInputParameterForNameAndValue(
			final String parameter) {

		log.info("parseInputParametersFromParameterLineOfRule()");
		if (parameter == null || parameter.isEmpty()) {
			throw new IllegalArgumentException("null or empty parameter");
		}

		// have parms to process..split at equal sign
		int idx = parameter.indexOf('=');
		if (idx==-1) {
			throw new IllegalArgumentException("missing equal sign in given input parameter"); 
		}
		
		if (idx + 1 == parameter.length()) {
			// have parm name but no value, this is ok, it may be overridden
			// later in code
			log.info("no value given for parameter");
			new RuleInputParameter(parameter.substring(0, idx), "");
		}

		return new RuleInputParameter(
				parameter.substring(0, idx), parameter.substring(idx + 1));
	}

}

