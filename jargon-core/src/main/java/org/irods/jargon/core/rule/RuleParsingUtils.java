/**
 *
 */
package org.irods.jargon.core.rule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utilities for parsing rules for processing by Jargon.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
class RuleParsingUtils {

	private static Logger log = LogManager.getLogger(RuleParsingUtils.class);

	/**
	 * Private constructor
	 */
	private RuleParsingUtils() {
	}

	/**
	 * Given a rule input parameter value which has already been separated from
	 * other input parameters by the , or % delimiter, separate into parameter name
	 * and parameter value. This essentially splits on the first '=' sign. <br/>
	 * This routine will trim the param names and strip leading $ in the value that
	 * typically mark interactive variables
	 *
	 * @param parameter
	 *            {@code String} which should reflect an individual rule parameter
	 *            in *parmName=parmValue format
	 * @return {@link RuleInputParameter}
	 */
	static RuleInputParameter parseInputParameterForNameAndValue(final String parameter) {

		log.info("parseInputParametersFromParameterLineOfRule()");
		if (parameter == null || parameter.isEmpty()) {
			throw new IllegalArgumentException("null or empty parameter");
		}

		// have parms to process..split at equal sign
		int idx = parameter.indexOf('=');
		if (idx == -1) {

			if (parameter.toUpperCase().indexOf("NULL") > -1) {
				return null;
			} else {
				throw new IllegalArgumentException("missing equal sign in given input parameter");
			}
		}

		String paramName = parameter.substring(0, idx).trim();
		String paramVal = null;
		if (idx + 1 == parameter.length()) {
			// have parm name but no value, this is ok, it may be overridden
			// later in code
			log.info("no value given for parameter");
			paramVal = "";
		} else {
			paramVal = parameter.substring(idx + 1);
			// strip $ leading char as interactive

			if (paramVal.charAt(0) == '$') {
				paramVal = paramVal.substring(1);
			}

		}

		return new RuleInputParameter(paramName, paramVal);
	}

}
