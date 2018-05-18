/**
 * 
 */
package org.irods.jargon.core.rule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.irods.jargon.core.utils.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool to look at the contents of a rule and guess the language
 * 
 * @author conwaymc
 *
 */
public class RuleTypeEvaluator {

	private final Pattern irodsRulePattern;
	private final Pattern irodsRulePattern2;

	private final Pattern pythonRulePattern;
	private static final Logger log = LoggerFactory.getLogger(RuleTypeEvaluator.class);

	/**
	 * annotation that can be added in a comment that can have a value of IRODS |
	 * PYTHON for now
	 */
	public static final String RULE_ENGINE_ANNOTATION = "@RuleEngine=\"";

	public RuleTypeEvaluator() {
		// [a-zA-Z0-9*][\\s*]\\{
		pythonRulePattern = Pattern.compile(".*def.*:");
		irodsRulePattern = Pattern.compile("[a-zA-Z0-9]*\\s*\\{");
		irodsRulePattern2 = Pattern.compile(".*;");
	}

	/**
	 * Guess at the type of a rule based on looking at the text
	 * 
	 * @param ruleText
	 *            <code>String</code> with the text of the rule
	 * @return {@link IrodsRuleInvocationTypeEnum} with the guessed type of the
	 *         rule, or an exception will be thrown if it cannot be determined
	 * @throws UnknownRuleTypeException
	 *             for rule type not supported
	 */
	public IrodsRuleInvocationTypeEnum guessRuleLanguageType(final String ruleText) throws UnknownRuleTypeException {
		if (ruleText == null || ruleText.isEmpty()) {
			throw new IllegalArgumentException("Cannot determine the rule type");
		}

		log.debug("first look for a rule annotation...");
		IrodsRuleInvocationTypeEnum enumFromAnnotation = detectTypeFromRuleTextAnnotation(ruleText);
		if (enumFromAnnotation != null) {
			log.debug("found type from annotation:{}", enumFromAnnotation);
			return enumFromAnnotation;
		}

		Matcher m = irodsRulePattern.matcher(ruleText);
		Matcher m2 = irodsRulePattern2.matcher(ruleText);
		boolean irodsMatcher = m.find();
		boolean irodsMatcher2 = m2.find();
		if (irodsMatcher && irodsMatcher2) {
			return IrodsRuleInvocationTypeEnum.IRODS;
		}

		m = pythonRulePattern.matcher(ruleText);
		boolean pythonMatcher = m.find();
		if (pythonMatcher) {
			return IrodsRuleInvocationTypeEnum.PYTHON;
		}

		// I have fallen through to an exception with no match
		throw new UnknownRuleTypeException("cannot determine rule type");

	}

	/**
	 * Guess a rule type based on the file extenstion
	 * 
	 * @param fileName
	 *            <code>String</code> with a file name or even a path
	 * @return {@link IrodsRuleInvocationTypeEnum} which can return
	 *         <code>null</code> if it can't guess
	 */
	public IrodsRuleInvocationTypeEnum detectTypeFromExtension(final String fileName) {
		log.info("detectTypeFromExtension()");
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		log.info("fileName:{}", fileName);
		String extension = LocalFileUtils.getFileExtension(fileName);
		log.debug("extension is:{}", extension);
		IrodsRuleInvocationTypeEnum enumVal;
		if (extension.trim().equals(".r")) {
			enumVal = IrodsRuleInvocationTypeEnum.IRODS;
		} else if (extension.trim().equals(".py")) {
			enumVal = IrodsRuleInvocationTypeEnum.PYTHON;
		} else {
			enumVal = null;
		}

		return enumVal;
	}

	/**
	 * Look for a rule engine type hint annotation
	 * 
	 * @param ruleText
	 *            <code>String</code> with the rule text
	 * @return {@link IrodsRuleInvocationTypeEnum} which can return
	 *         <code>null</code> if it can't guess
	 */
	public IrodsRuleInvocationTypeEnum detectTypeFromRuleTextAnnotation(final String ruleText) {
		log.info("detectTypeFromRuleTextAnnotation()");
		if (ruleText == null || ruleText.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleText");
		}

		log.info("ruleText:{}", ruleText);

		/*
		 * first look for an @Engine annotation! This is something that needs to be
		 * formalized but for now it's a handy trick if something goes wrong
		 */
		IrodsRuleInvocationTypeEnum enumVal;

		int reAnnotationIndex = ruleText.indexOf(RULE_ENGINE_ANNOTATION);
		if (reAnnotationIndex > -1) {
			log.debug("found an annotation!");
			// for now just looks for the next space and pulls out the string after the =,
			// yes
			// this is brittle
			int endOfAnnotation = reAnnotationIndex + RULE_ENGINE_ANNOTATION.length();
			int nextSpace = ruleText.indexOf("\"", endOfAnnotation);
			if (nextSpace == -1) {
				log.warn("couldn't find end of annotation, just bail");
				return null;
			}

			String annotationType = ruleText.substring(endOfAnnotation, nextSpace).trim();

			log.debug("annotationType:{}", annotationType);

			if (annotationType.equals(IrodsRuleInvocationTypeEnum.IRODS.toString())) {
				enumVal = IrodsRuleInvocationTypeEnum.IRODS;
			} else if (annotationType.equals(IrodsRuleInvocationTypeEnum.PYTHON.toString())) {
				enumVal = IrodsRuleInvocationTypeEnum.PYTHON;
			} else {
				enumVal = null;
			}
		} else {
			enumVal = null;
		}

		return enumVal;

	}

}
