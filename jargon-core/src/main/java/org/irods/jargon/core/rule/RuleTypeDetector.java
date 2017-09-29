/**
 * 
 */
package org.irods.jargon.core.rule;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway
 *
 */
public class RuleTypeDetector {

	private static final Logger log = LoggerFactory.getLogger(RuleTypeDetector.class);
	/**
	 * annotation that can be added in a comment that can have a value of IRODS |
	 * PYTHON for now
	 */
	public static final String RULE_ENGINE_ANNOTATION = "@RuleEngine=\"";

	/**
	 * Guess a rule type based on the file extenstion
	 * 
	 * @param fileName
	 *            <code>String</code> with a file name or even a path
	 * @return {@link IrodsRuleEngineTypeEnum} which can return UNKNOWN if it can't
	 *         guess
	 */
	public IrodsRuleEngineTypeEnum detectTypeFromExtension(final String fileName) {
		log.info("detectTypeFromExtension()");
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		log.info("fileName:{}", fileName);
		String extension = LocalFileUtils.getFileExtension(fileName);
		log.debug("extension is:{}", extension);
		IrodsRuleEngineTypeEnum enumVal;
		if (extension.trim().equals(".r")) {
			enumVal = IrodsRuleEngineTypeEnum.IRODS;
		} else if (extension.trim().equals(".py")) {
			enumVal = IrodsRuleEngineTypeEnum.PYTHON;
		} else {
			enumVal = IrodsRuleEngineTypeEnum.UNKNOWN;
		}

		return enumVal;
	}

	public IrodsRuleEngineTypeEnum detectTypeFromRuleText(final String ruleText) throws JargonException {
		log.info("detectTypeFromRuleText()");
		if (ruleText == null || ruleText.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleText");
		}

		log.info("ruleText:{}", ruleText);

		/*
		 * first look for an @Engine annotation! This is something that needs to be
		 * formalized but for now it's a handy trick if something goes wrong
		 */
		IrodsRuleEngineTypeEnum enumVal;

		int reAnnotationIndex = ruleText.indexOf(RULE_ENGINE_ANNOTATION);
		if (reAnnotationIndex > -1) {
			log.debug("found an annotation!");
			// for now just looks for the next space and pulls out the string after the =,
			// yes
			// this is brittle
			int endOfAnnotation = reAnnotationIndex + RULE_ENGINE_ANNOTATION.length();
			int nextSpace = ruleText.indexOf("\"", endOfAnnotation);
			if (nextSpace == -1) {
				throw new JargonException("found a rule engine annotation but could not find type after =");
			}

			String annotationType = ruleText.substring(endOfAnnotation, nextSpace).trim();

			log.debug("annotationType:{}", annotationType);

			if (annotationType.equals(IrodsRuleEngineTypeEnum.IRODS.toString())) {
				enumVal = IrodsRuleEngineTypeEnum.IRODS;
			} else if (annotationType.equals(IrodsRuleEngineTypeEnum.PYTHON.toString())) {
				enumVal = IrodsRuleEngineTypeEnum.PYTHON;
			} else {
				enumVal = IrodsRuleEngineTypeEnum.UNKNOWN;
			}
		} else {
			enumVal = IrodsRuleEngineTypeEnum.UNKNOWN;
		}

		return enumVal;

	}

}
