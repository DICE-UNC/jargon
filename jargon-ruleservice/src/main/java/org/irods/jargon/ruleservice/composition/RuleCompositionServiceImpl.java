/**
 * 
 */
package org.irods.jargon.ruleservice.composition;

import java.io.IOException;
import java.io.StringWriter;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileReader;
import org.irods.jargon.core.rule.IRODSRule;
import org.irods.jargon.core.rule.IRODSRuleTranslator;
import org.irods.jargon.core.service.AbstractJargonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service implementation to support composition of rules
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RuleCompositionServiceImpl extends AbstractJargonService implements
		RuleCompositionService {

	public static final Logger log = LoggerFactory
			.getLogger(RuleCompositionServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ruleservice.composition.RuleCompositionService#
	 * parseStringIntoRule(java.lang.String)
	 */
	@Override
	public Rule parseStringIntoRule(final String inputRuleAsString)
			throws JargonException {
		log.info("parseStringIntoRule()");

		if (inputRuleAsString == null || inputRuleAsString.isEmpty()) {
			throw new IllegalArgumentException(
					"inputRuleAsString is null or empty");
		}

		log.info("inputRuleAsString:{}", inputRuleAsString);

		final IRODSRuleTranslator irodsRuleTranslator = new IRODSRuleTranslator(
				getIrodsAccessObjectFactory().getIRODSServerProperties(
						getIrodsAccount()));

		IRODSRule irodsRule = irodsRuleTranslator
				.translatePlainTextRuleIntoIRODSRule(inputRuleAsString);

		log.info("got irodsRule:{}", irodsRule);

		Rule rule = new Rule();
		rule.setRuleBody(irodsRule.getRuleBody());
		rule.setInputParameters(irodsRule.getIrodsRuleInputParameters());
		rule.setOutputParameters(irodsRule.getIrodsRuleOutputParameters());
		log.info("resulting rule:{}", rule);
		return rule;

	}

	@Override
	public Rule loadRuleFromIrods(final String absolutePathToRuleFile)
			throws MissingOrInvalidRuleException, JargonException {

		log.info("loadRuleFromIrods()");

		if (absolutePathToRuleFile == null || absolutePathToRuleFile.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absolutepPathToRuleFile");
		}

		IRODSFileFactory irodsFileFactory = getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount());

		IRODSFileReader irodsFileReader = irodsFileFactory
				.instanceIRODSFileReader(absolutePathToRuleFile);

		StringWriter writer = null;
		String ruleString = null;

		try {
			writer = new StringWriter();
			char[] buff = new char[1024];
			int i = 0;
			while ((i = irodsFileReader.read(buff)) > -1) {
				writer.write(buff, 0, i);
			}

			ruleString = writer.toString();

			if (ruleString == null || ruleString.isEmpty()) {
				log.error("null or empty rule string");
				throw new MissingOrInvalidRuleException("no rule found");
			}

			return parseStringIntoRule(ruleString);

		} catch (IOException ioe) {
			log.error("io exception reading rule data from resource", ioe);
			throw new JargonException("error reading rule from resource", ioe);
		} finally {
			try {
				irodsFileReader.close();
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				// ignore
			}

		}

	}

	public RuleCompositionServiceImpl() {
		super();
	}

	public RuleCompositionServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

}
