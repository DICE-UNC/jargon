/**
 *
 */
package org.irods.jargon.datautils.rule;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.datautils.rule.UserRuleDefinition.RuleAproposTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to manage user defined rules in iRODS via a convention for a user
 * rules dir
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UserRuleServiceImpl extends AbstractJargonService implements
		UserRuleService {

	public static final Logger log = LoggerFactory
			.getLogger(UserRuleServiceImpl.class);

	private static final String USER_RULE_UNIT = "iRODSUserTagging:UserRule";

	private UserRuleServiceConfiguration userRuleServiceConfiguration = new UserRuleServiceConfiguration();

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public UserRuleServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsAccessObjectFactory, irodsAccount);
		getIrodsAccessObjectFactory().getRuleProcessingAO(getIrodsAccount());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.rule.UserRulesService#listUserRules()
	 */
	@Override
	public List<UserRuleDefinition> listUserRulesInUserHomeDir()
			throws JargonException {
		log.info("listUserRules()");

		List<AVUQueryElement> avuQueryElements = buildAVUQueryForUserRule();

		List<UserRuleDefinition> userRules = new ArrayList<UserRuleDefinition>();

		// Do data objects only
		log.info("querying metadata as a data object to look for rules in the user rule directory");
		DataObjectAO dataObjectAO = getIrodsAccessObjectFactory()
				.getDataObjectAO(getIrodsAccount());
		try {
			List<MetaDataAndDomainData> metadata = dataObjectAO
					.findMetadataValuesForDataObjectUsingAVUQuery(
							avuQueryElements, buildRuleSubidrPath());
			for (MetaDataAndDomainData metadataAndDomainData : metadata) {
				log.debug("adding rule file:{}", metadataAndDomainData);
				userRules
						.add(transformMetadataValueToUserRule(metadataAndDomainData));
			}

		} catch (JargonQueryException e) {
			throw new JargonException("error querying for metadata", e);
		}

		return userRules;

	}

	public void addNewUserRuleInUserHomeDir(final String userFileName,
			final String description, final String ruleName,
			final RuleAproposTo aproposTo, final String ruleBody)
			throws NoUserRuleSubdirException, DuplicateDataException,
			JargonException {
		log.info("addNewUserRule()");

		if (userFileName == null || userFileName.isEmpty()) {
			throw new IllegalArgumentException("null userFileName");
		}

		if (description == null) {
			throw new IllegalArgumentException(
					"null description, make blank if not used");
		}

		if (ruleName == null || ruleName.isEmpty()) {
			throw new IllegalArgumentException("ruleName is null or empty");
		}

		if (aproposTo == null) {
			throw new IllegalArgumentException("null aproposTo");
		}

		if (ruleBody == null || ruleBody.isEmpty()) {
			throw new IllegalArgumentException("null or empty rule body");
		}

	}

	private UserRuleDefinition transformMetadataValueToUserRule(
			final MetaDataAndDomainData metadataAndDomainData) {
		log.info("transformMetadataValueToUserRule()");

		if (metadataAndDomainData == null) {
			throw new IllegalArgumentException("null metadataAndDomainData");
		}

		log.info("metadataAndDomainData:{}", metadataAndDomainData);

		UserRuleDefinition userRuleDefinition = new UserRuleDefinition();
		userRuleDefinition.setRuleAbsolutePath(metadataAndDomainData
				.getDomainObjectUniqueName());

		userRuleDefinition.setCount(metadataAndDomainData.getCount());
		userRuleDefinition.setLastResult(metadataAndDomainData.isLastResult());
		return userRuleDefinition;
	}

	private List<AVUQueryElement> buildAVUQueryForUserRule()
			throws JargonException {
		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		try {
			avuQueryElements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryPart.UNITS, QueryConditionOperators.EQUAL,
					USER_RULE_UNIT));
		} catch (JargonQueryException e) {
			log.error("error on metadata query, rethrow as JargonException", e);
			throw new JargonException(e);
		}
		return avuQueryElements;
	}

	private String buildRuleSubidrPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(MiscIRODSUtils
				.buildIRODSUserHomeForAccountUsingDefaultScheme(getIrodsAccount()));
		sb.append(userRuleServiceConfiguration.getRuleSubdirName());
		String expectedPath = sb.toString();
		log.info("computed path:{}", expectedPath);
		return expectedPath;
	}

}
