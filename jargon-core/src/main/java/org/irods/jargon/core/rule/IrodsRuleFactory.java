/**
 *
 */
package org.irods.jargon.core.rule;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create iRODS rule representation {@link IRODSRule} from a raw
 * textual form
 *
 * @author conwaymc
 *
 */
public class IrodsRuleFactory {

	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	private final IRODSAccount irodsAccount;

	Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Constructor with required fields
	 *
	 * @param irodsAccessObjectFactory {@link IRODSAccessObjectFactory}
	 * @param irodsAccount             {@link IRODSAccount} for the current user
	 *
	 */
	public IrodsRuleFactory(final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount) {
		super();

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
	}

	/**
	 * Create an <code>IRODSRule</code> representation from the base text
	 *
	 * @param irodsRuleAsString           <code>String</code> with the rule text,
	 *                                    including the input and output lines
	 * @param inputParameterOverrides     <code>List</code> of optional
	 *                                    {@link IRODSRuleParameter} (can be null)
	 *                                    overrides to the input parameters.
	 * @param ruleInvocationConfiguration {@link RuleInvocationConfiguration} with
	 *                                    config hints
	 * @return {@link IRODSRule} ready to submit to iRODS for processing
	 * @throws JargonRuleException for rule error
	 * @throws JargonException     for iRODS error
	 */
	public IRODSRule instanceIrodsRule(final String irodsRuleAsString,
			final List<IRODSRuleParameter> inputParameterOverrides,
			final RuleInvocationConfiguration ruleInvocationConfiguration) throws JargonRuleException, JargonException {

		log.info("instanceIrodsRule()");

		if (irodsRuleAsString == null || irodsRuleAsString.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsRuleAsString");
		}

		if (ruleInvocationConfiguration == null) {
			throw new IllegalArgumentException("null ruleInvocationConfiguration");
		}

		/*
		 * Copy the rule invocation so as not to alter the method parameter. This may be
		 * altered by auto detection of rule type
		 */
		RuleInvocationConfiguration copiedRuleInvocationConfiguration = ruleInvocationConfiguration
				.copyRuleInvocationConfiguration(ruleInvocationConfiguration);

		log.debug("irodsRuleAsString:{}", irodsRuleAsString);
		log.info("determining the rule type");

		if (ruleInvocationConfiguration.getRuleEngineSpecifier().isEmpty())

			if (copiedRuleInvocationConfiguration
					.getIrodsRuleInvocationTypeEnum() == IrodsRuleInvocationTypeEnum.AUTO_DETECT) {
				log.info("will attempt to auto-detect based on the rule text");
				RuleTypeEvaluator ruleTypeEvaluator = new RuleTypeEvaluator();
				IrodsRuleInvocationTypeEnum invocationType = ruleTypeEvaluator.guessRuleLanguageType(irodsRuleAsString);
				copiedRuleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(invocationType);
				log.info("rule invocation type determined to be:{}", invocationType);
			} else {
				log.info("using the user-supplied rule type of:{}",
						copiedRuleInvocationConfiguration.getIrodsRuleInvocationTypeEnum());
			}

		AbstractRuleTranslator ruleTranslator = null;

		switch (copiedRuleInvocationConfiguration.getIrodsRuleInvocationTypeEnum()) {
		case IRODS:
			copiedRuleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.IRODS);
			ruleTranslator = new IrodsRuleEngineRuleTranslator(
					irodsAccessObjectFactory.getIRODSServerProperties(irodsAccount), copiedRuleInvocationConfiguration,
					irodsAccessObjectFactory.getJargonProperties());
			break;
		case PYTHON:
			copiedRuleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.PYTHON);
			ruleTranslator = new PythonRuleTranslator(irodsAccessObjectFactory.getIRODSServerProperties(irodsAccount),
					copiedRuleInvocationConfiguration, irodsAccessObjectFactory.getJargonProperties());
			break;
		case OTHER:
			copiedRuleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.OTHER);
			ruleTranslator = new OtherRuleTranslator(irodsAccessObjectFactory.getIRODSServerProperties(irodsAccount),
					copiedRuleInvocationConfiguration, irodsAccessObjectFactory.getJargonProperties());
			break;
		default:
			log.error("cannot create a ruleTranslator based on the provided configuration, not supported:{}",
					copiedRuleInvocationConfiguration);
			throw new JargonRuleException("unable to determine rule processing type");

		}

		IRODSRule irodsRule = ruleTranslator.translatePlainTextRuleIntoIrodsRule(irodsRuleAsString,
				inputParameterOverrides);
		return irodsRule;

	}

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

}
