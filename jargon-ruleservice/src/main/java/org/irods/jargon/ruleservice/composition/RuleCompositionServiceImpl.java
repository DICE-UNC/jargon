/**
 * 
 */
package org.irods.jargon.ruleservice.composition;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
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

	/* (non-Javadoc)
	 * @see org.irods.jargon.ruleservice.composition.RuleCompositionService#loadRuleFromIrods(java.lang.String)
	 */
	@Override
	public Rule loadRuleFromIrods(final String absolutePathToRuleFile)
			throws FileNotFoundException, MissingOrInvalidRuleException, JargonException {

		log.info("loadRuleFromIrods()");

		if (absolutePathToRuleFile == null || absolutePathToRuleFile.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absolutepPathToRuleFile");
		}

		IRODSFileFactory irodsFileFactory = getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount());
		
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(absolutePathToRuleFile);
		if (!irodsFile.exists()) {
			log.error("did not find rule file");
			throw new FileNotFoundException("rule file not found");
		}

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
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.ruleservice.composition.RuleCompositionService#storeRuleFromParts(java.lang.String, java.lang.String, java.util.List, java.util.List)
	 */
	@Override
	public Rule storeRuleFromParts(final String ruleAbsolutePath, final String ruleBody, final List<String> inputParameters, final List<String> outputParameters) throws JargonException {
		log.info("storeRuleFromParts()");
		
		if (ruleAbsolutePath == null || ruleAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleAbsolutePath");
		}
		
		if (inputParameters == null) {
			throw new IllegalArgumentException("null inputParameters");
		}
		
		if (outputParameters == null) {
			throw new IllegalArgumentException("null outputParameters");
		}
		
		log.info("ruleAbsolutePath:{}", ruleAbsolutePath);
		log.info("inputParameters:{}", inputParameters);
		log.info("outputParameters:{}", outputParameters);
		
		
		StringBuilder sb = new StringBuilder();
		sb.append(ruleBody);
		sb.append("\n");
		sb.append("INPUT ");

		
		if (inputParameters.isEmpty()) {
			sb.append("null");
		} else {
			int i = 0;
			for (String param : inputParameters) {
				if (i++ > 0) {
					sb.append(", ");
				}				
				sb.append(param);
			}
		}
		
		sb.append("\n");
		sb.append("OUTPUT ");
		
		if (outputParameters.isEmpty()) {
			sb.append("null");
		} else {
			int i = 0;
			for (String param : outputParameters) {
				if (i++ > 0) {
					sb.append(", ");
				}
				sb.append(param);
			}
		}
		
		sb.append("\n");
		
		String ruleAsString = sb.toString();
		IRODSFile irodsFile = this.getIrodsAccessObjectFactory().getIRODSFileFactory(getIrodsAccount()).instanceIRODSFile(ruleAbsolutePath);
		
		Stream2StreamAO stream2StreamAO = this.getIrodsAccessObjectFactory().getStream2StreamAO(getIrodsAccount());
		try {
			stream2StreamAO.streamBytesToIRODSFile(ruleAsString.getBytes( this.getIrodsAccessObjectFactory().getJargonProperties().getEncoding() ), irodsFile);
		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding streaming to file", e);
			throw new JargonException("error writing rule file", e);
		}
		
		log.info("rule stored:{}", ruleAsString);
		
		return parseStringIntoRule(ruleAsString);
		
	}
	
	public Rule deleteInputParameterFromRule(final String ruleAbsolutePath, final String parameterToDelete) throws FileNotFoundException, JargonException {
		log.info("deleteInputParameterFromRule()");
		
		if (ruleAbsolutePath == null || ruleAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleAbsolutePath");
		}
		
		if (parameterToDelete ==  null || parameterToDelete.isEmpty()) {
			throw new IllegalArgumentException("null or empty parameterToDelete");
		}
		
		Rule currentRule = this.loadRuleFromIrods(ruleAbsolutePath);
		log.info("found current rule, recompose by deleting input parameter and reformatting");
		
		return null;
	
	}
	
	public Rule deleteOutputParameterFromRule(final String ruleAbsolutePath, final String parameterToDelete) throws FileNotFoundException, JargonException {
		log.info("deleteOutputParameterFromRule()");
		
		if (ruleAbsolutePath == null || ruleAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleAbsolutePath");
		}
		
		if (parameterToDelete ==  null || parameterToDelete.isEmpty()) {
			throw new IllegalArgumentException("null or empty parameterToDelete");
		}
		
		return null;
		
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
