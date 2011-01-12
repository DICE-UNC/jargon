/**
 * 
 */
package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.irods.jargon.core.connection.IRODSServerProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.SelectField.SelectFieldTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translate an IRODSQuery posed as a <code>String</code> query statement (as in
 * iquery) into a format that IRODS understands see
 * lib/core/include/rodsGenQueryNames.h
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IRODSQueryTranslator {

	private IRODSServerProperties irodsServerProperties;
	private static Logger log = LoggerFactory
			.getLogger(IRODSQueryTranslator.class);
	private ExtensibleMetaDataMapping extensibleMetaDataMapping = null;

	public static String[] operatorStrings = { "=", "<>", "<", ">", "<=", ">=",
			"in", "not in", "between", "not between", "like", "not like",
			"sounds like", "sounds not like", "TABLE", "num<", "num>", "num<=",
			"num>=", };

	/**
	 * Public constructor takes a <code>IRODSServerProperties</code> object that
	 * describes the current iRODS server.
	 * 
	 * @param irodsServerProperties
	 *            <code>IRODSServerProperties</code> that describes the iRODS
	 *            server.
	 * @throws JargonException
	 */
	public IRODSQueryTranslator(
			final IRODSServerProperties irodsServerProperties)
			throws JargonException {
		if (irodsServerProperties == null) {
			throw new JargonException("server properties is null");
		}

		this.irodsServerProperties = irodsServerProperties;
	}

	/**
	 * Public constructor allows specification of a mapping of extensible
	 * meta-data values.
	 * 
	 * @param irodsServerProperties
	 *            <code>IRODSServerProperties</code> that describes the iRODS
	 *            server.
	 * @param extensibleMetaDataMapping
	 *            (@link org.irods.jargon.core.query.ExtensibleMetaDataMapping}
	 *            that maps extensible meta-data value to GenQuery values. This
	 *            mapping may be set to null and will be ignored.
	 * @throws JargonException
	 */
	public IRODSQueryTranslator(
			final IRODSServerProperties irodsServerProperties,
			final ExtensibleMetaDataMapping extensibleMetaDataMapping)
			throws JargonException {
		this(irodsServerProperties);
		this.extensibleMetaDataMapping = extensibleMetaDataMapping;
	}

	/**
	 * Given a query (as in iquest) that has been formatted into an
	 * <code>IRODSQuery</code> object, translate the query such that selects and
	 * conditions are formated such that iRODS can understand the fields.
	 * 
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSQuery} containing the
	 *            desired GenQuery
	 * @return {@link org.irods.jargon.core.query.TranslatedIRODSQuery} that
	 *         encapsulates a query where selects and conditions have been
	 *         resolved and translated into a format that GenQuery can
	 *         understand.
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	public TranslatedIRODSQuery getTranslatedQuery(final IRODSQuery irodsQuery)
			throws JargonQueryException, JargonException {
		List<String> selects = parseSelectsIntoListOfNames(irodsQuery
				.getQueryString());

		boolean isDistinct = true;
		// is this a non-distinct query
		if (doesQueryFlagNonDistinct(irodsQuery.getQueryString())) {
			isDistinct = false;
		}

		List<SelectField> translatedSelects = translateSelects(selects);

		List<TranslatedQueryCondition> translatedConditions = translateConditions(irodsQuery);
		
		// do a final check to make sure everything translated
		reviewTranslationBeforeReturningQuery(translatedSelects,
				translatedConditions);

		return TranslatedIRODSQuery.instance(translatedSelects,
				translatedConditions, irodsQuery, isDistinct);
	}

	/**
	 * @param translatedSelects
	 * @param translatedConditions
	 * @throws JargonQueryException
	 */
	private void reviewTranslationBeforeReturningQuery(
			final List<SelectField> translatedSelects,
			final List<TranslatedQueryCondition> translatedConditions)
			throws JargonQueryException {
		int i;
		if (translatedSelects.isEmpty()) {
			throw new JargonQueryException("no selects found in query");
		}

		i = 0;
		for (SelectField selectField : translatedSelects) {

			if (selectField == null) {
				throw new JargonQueryException(
						"untranslated select field in position:" + i);
			}

			if (selectField.getSelectFieldNumericTranslation() == null) {
				throw new JargonQueryException(
						"untranslated select field in position:" + i);
			}
			i++;
		}

		i = 0;
		for (TranslatedQueryCondition condition : translatedConditions) {
			if (condition.getColumnNumericTranslation() == null) {
				throw new JargonQueryException(
						"untranslated condition field in position:" + i
								+ " after the WHERE");
			}
			i++;
		}
	}

	/**
	 * @param irodsQuery
	 * @return
	 * @throws JargonQueryException
	 * @throws JargonException
	 */
	protected List<TranslatedQueryCondition> translateConditions(
			final IRODSQuery irodsQuery) throws JargonQueryException,
			JargonException {
		int i;
		List<QueryCondition> conditions = parseConditionsIntoList(irodsQuery
				.getQueryString());

		// FIXME: condition like x ='14' does not work....need to detect the
		// conditional and compensate by putting spaces around

		List<TranslatedQueryCondition> translatedConditions = new ArrayList<TranslatedQueryCondition>();

		// process conditions
		TranslatedQueryCondition translatedCondition;
		i = 0;
		for (QueryCondition queryCondition : conditions) {
			if (log.isDebugEnabled()) {
				log.debug("translating condition:" + queryCondition);
			}

			RodsGenQueryEnum conditionEnumVal = RodsGenQueryEnum
					.getAttributeBasedOnName(queryCondition.getFieldName());

			if (conditionEnumVal != null) {

				// check condition as IRODS data
				translatedCondition = TranslatedQueryCondition.instance(
						conditionEnumVal, queryCondition.getOperator(),
						queryCondition.getValue());
				translatedConditions.add(translatedCondition);
				if (log.isDebugEnabled()) {
					log.debug("added query condition:");
					log.debug(translatedCondition.toString());
				}

				i++;

				continue;

			}

			// not iRODS GenQuery field, see if this is extensible meta-data

			if (extensibleMetaDataMapping != null) {
				String extensibleMetaDataTranslationValue = extensibleMetaDataMapping
						.getIndexFromColumnName(queryCondition.getFieldName());

				if (extensibleMetaDataTranslationValue != null) {

					translatedCondition = TranslatedQueryCondition
							.instanceForExtensibleMetaData(
									queryCondition.getFieldName(),
									queryCondition.getOperator(),
									queryCondition.getValue(),
									extensibleMetaDataTranslationValue);
					translatedConditions.add(translatedCondition);
					if (log.isDebugEnabled()) {
						log.debug("added query condition as extensible metadata:");
						log.debug(translatedCondition.toString());
					}

					i++;
					continue;

				}
			}

			// I was not able to translate this field.

			throw new JargonQueryException(
					"untranslatable condition in position:" + i
							+ " after the where");

		}
		return translatedConditions;
	}

	/**
	 * @param selects
	 * @return
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	protected List<SelectField> translateSelects(final List<String> selects)
			throws JargonException, JargonQueryException {
		List<SelectField> translatedSelects = new ArrayList<SelectField>();

		// go through selects and get the translation from the 'string' name to
		// the index and other information contained in the enumeration
		int i = 0;
		SelectField translated = null;
		for (String select : selects) {
			translated = translateSelectFieldAsIRODSQueryValue(select
					.toUpperCase());

			if (translated != null) {
				translatedSelects.add(translated);
				i++;
				continue;
			}

			translated = translateSelectAsMetadataSelectField(select);
			if (translated != null) {
				translatedSelects.add(translated);
				i++;
				continue;
			}

			log.error("did not translate select field:{}", select);
			throw new JargonQueryException("untranslatable select in position:"
					+ i);
		}
		return translatedSelects;
	}

	/**
	 * Given a textual name, attempt to translate this field as an IRODS
	 * GenQuery field. This method accepts fields that are aggregations, such as
	 * sum(field). Warning: this method returns null if lookup is unsuccessful.
	 * 
	 * @param originalSelectField
	 *            <code>String</code> with query field
	 * @return {@link org.irods.jargon.core.query.SelectField} with the
	 *         translation, or null if not found.
	 * @throws JargonQueryException
	 *             indicates malformed query field.
	 */
	protected SelectField translateSelectFieldAsIRODSQueryValue(
			final String originalSelectField) throws JargonException,
			JargonQueryException {
		String rawField;

		if (log.isDebugEnabled()) {
			log.debug("translating select field:" + originalSelectField);
		}

		rawField = extractRawFieldFromSelectValue(originalSelectField);

		RodsGenQueryEnum field = RodsGenQueryEnum
				.getAttributeBasedOnName(rawField);

		if (field == null) {
			log.debug("retuning null, this is not an IRODS query field");
			return null;
		}

		// if I get here, I know that I translated this to an iRODS query field

		log.debug("field translated as iRODS GenQuery value");

		SelectFieldTypes selectFieldType = extractSelectFieldTypeFromSelectValue(originalSelectField);

		return SelectField.instance(field, selectFieldType,
				SelectField.SelectFieldSource.DEFINED_QUERY_FIELD);
	}

	/**
	 * Given a textual name, attempt to translate this field as an IRODS
	 * GenQuery field that represents extensible metadata. This method accepts
	 * fields that are aggregations, such as sum(field). Warning: this method
	 * returns null if lookup is unsuccessful.
	 * 
	 * TODO:verify that extensible metadata fields support aggregations
	 * 
	 * @param originalSelectField
	 *            <code>String</code> with query field
	 * @return {@link org.irods.jargon.core.query.SelectField} with the
	 *         translation, or null if not found.
	 * @throws JargonQueryException
	 *             indicates malformed query field.
	 */
	protected SelectField translateSelectAsMetadataSelectField(
			final String originalSelectField) throws JargonException,
			JargonQueryException {

		String rawField;

		if (log.isDebugEnabled()) {
			log.debug("translating select field:" + originalSelectField);
		}

		rawField = extractRawFieldFromSelectValue(originalSelectField);

		RodsGenQueryEnum field = RodsGenQueryEnum
				.getAttributeBasedOnName(rawField);

		if (field == null) {
			log.debug("retuning null, this is not an IRODS extensible metadata field");
			return null;
		}

		// if I get here, I know that I translated this to an extensible
		// metadata field.

		log.debug("translating as iRODS extensbile metadata field?");
		SelectFieldTypes selectFieldType = extractSelectFieldTypeFromSelectValue(originalSelectField);

		return SelectField.instance(field, selectFieldType,
				SelectField.SelectFieldSource.EXTENSIBLE_METADATA);
	}

	protected boolean doesQueryFlagNonDistinct(final String query) {
		if (query.toUpperCase().indexOf("NON-DISTINCT") > -1) {
			return true;
		} else {
			return false;
		}
	}

	private String extractRawFieldFromSelectValue(final String selectField)
			throws JargonQueryException {
		int parenOpen = selectField.indexOf('(');
		int parenClose = -1;
		String rawField;

		// is this a plain select or a computed select like sum()?
		if (parenOpen > -1) {
			parenClose = selectField.indexOf(')');
			if (parenClose == -1) {
				throw new JargonQueryException("malformed select field:"
						+ selectField);
			}

			rawField = selectField.substring(parenOpen + 1, parenClose);
			if (rawField.length() == 0) {
				throw new JargonQueryException(
						"malformed select, aggregation but no field name specified in between ( and )");
			}

		} else {
			rawField = selectField;
		}
		return rawField;
	}

	private SelectFieldTypes extractSelectFieldTypeFromSelectValue(
			final String selectField) throws JargonQueryException {

		int parenOpen = selectField.indexOf('(');
		SelectFieldTypes selectFieldType;

		if (parenOpen == -1) {
			// no paren, so this is a raw field, no aggregation
			selectFieldType = SelectFieldTypes.FIELD;
		} else {

			String aggregationComponent = selectField.substring(0, parenOpen);

			if (aggregationComponent.equals("SUM")) {
				selectFieldType = SelectFieldTypes.SUM;
			} else if (aggregationComponent.equals("AVG")) {
				selectFieldType = SelectFieldTypes.AVG;
			} else if (aggregationComponent.equals("COUNT")) {
				selectFieldType = SelectFieldTypes.COUNT;
			} else if (aggregationComponent.equals("MIN")) {
				selectFieldType = SelectFieldTypes.MIN;
			} else if (aggregationComponent.equals("MAX")) {
				selectFieldType = SelectFieldTypes.MAX;
			} else {
				throw new JargonQueryException(
						"malformed select, unknown aggregation type of "
								+ aggregationComponent + " in field:"
								+ selectField);
			}

		}
		return selectFieldType;

	}

	/**
	 * Make a list of strings that are each considered a select field
	 * 
	 * @param query
	 * @return
	 * @throws JargonQueryException
	 */
	protected List<String> parseSelectsIntoListOfNames(final String query)
			throws JargonQueryException {
		List<String> selectFieldNames = new ArrayList<String>();
		boolean readPastSelect = false;
		boolean haveNotHitWhere = true;
		String token;
		// convert ',' to ' ' for easier tokenizing
		String queryNoCommas = query.replaceAll(",", " ");
		StringTokenizer tokenizer = new StringTokenizer(queryNoCommas, " ");
		int tokenCtr = 0;

		while (tokenizer.hasMoreElements() && haveNotHitWhere) {
			token = tokenizer.nextToken();

			// I'm interested in the content between the select and the where
			if (token.equalsIgnoreCase("select")) {
				readPastSelect = true;
				continue;
			}

			if (!readPastSelect) {
				JargonQueryException qe = new JargonQueryException(
						"error in query, no select detected");
				qe.setQuery(query);
				throw qe;
			}

			if (token.equalsIgnoreCase("where")) {
				haveNotHitWhere = false;
				continue;
			}

			if (token.equalsIgnoreCase("distinct")
					|| token.equalsIgnoreCase("non-distinct")) {
				if (tokenCtr != 1) {
					JargonQueryException qe = new JargonQueryException(
							"distinct/non-distinct must be second token after select");
					qe.setQuery(query);
				}
				continue;
			}

			// have a token that should be a select

			selectFieldNames.add(token.trim());
			tokenCtr++;

		}
		// first statement is select
		return selectFieldNames;

	}

	/**
	 * Create a list where each entry is one condition from the raw query.
	 * 
	 * @param query
	 * @return
	 * @throws JargonQueryException
	 */
	protected List<QueryCondition> parseConditionsIntoList(final String query)
			throws JargonQueryException {

		String testQuery = query;
		List<QueryCondition> conditions = new ArrayList<QueryCondition>();

		int idxOfWhere = -1;
		int indexOfWhereUC = testQuery.indexOf(" WHERE ");
		int indexOfWhereLC = testQuery.indexOf(" where ");

		if (indexOfWhereUC != -1) {
			idxOfWhere = indexOfWhereUC;
		} else if (indexOfWhereLC != -1) {
			idxOfWhere = indexOfWhereLC;
		}

		if (idxOfWhere == -1) {
			log.debug("no where conditions, returning");
			return conditions;
		}

		int conditionOffset = idxOfWhere + 7;

		// stop condition tokenizing if a 'GROUP BY' was found, if entered
		String findGroupByInStringMakingItAllUpperCase = testQuery.substring(
				conditionOffset).toUpperCase();
		int indexOfGroupBy = findGroupByInStringMakingItAllUpperCase
				.indexOf("GROUP BY");

		if (indexOfGroupBy != -1) {
			log.debug("found group by in query {}",
					findGroupByInStringMakingItAllUpperCase);
			testQuery = testQuery.substring(0, testQuery.length()
					- indexOfGroupBy - 8);
		}

		// have a condition, begin parsing into discrete tokens, treating quoted
		// literals as a token.
		List<QueryConditionToken> tokens = tokenizeConditions(testQuery,
				conditionOffset);

		log.debug("query condition tokens were: {}", tokens);
		// evalutate the tokens as components of a condition and return a
		// 'classified' list
		return buildListOfQueryConditionsFromParsedTokens(tokens);

	}

	/**
	 * Given a list of parsed-out query conditions, build a list of parsed query
	 * conditions
	 * 
	 * @param conditions
	 * @param tokens
	 * @param i
	 * @param tokenCtr
	 * @param parsedField
	 * @param parsedOperator
	 * @return
	 * @throws JargonQueryException
	 */
	private List<QueryCondition> buildListOfQueryConditionsFromParsedTokens(
			final List<QueryConditionToken> tokens) throws JargonQueryException {

		QueryCondition queryCondition;
		List<QueryCondition> queryConditions = new ArrayList<QueryCondition>();

		int i = 0;
		int tokenCtr = 0;

		String parsedField = "";
		String parsedValue = "";
		String parsedOperator = "";

		for (QueryConditionToken token : tokens) {

			// I'm interested in the content after the where, and will skip
			// before that
			if (token.getValue().equalsIgnoreCase("where")) {
				throw new JargonQueryException(
						"multiple where statements?, encountered at the "
								+ tokenCtr + " token after the WHERE");
			}

			// have an and, if I did not finish the last condition, it's an
			// error, otherwise, discard
			if (token.getValue().equalsIgnoreCase("and")) {
				if (i > 0) {
					throw new JargonQueryException(
							"I found an AND operator after an incomplete condition at token "
									+ tokenCtr + " after the where");
				}
				continue;
			}

			// I am now in the conditions, up each of the elements, should be in
			// the form of field, operator, condition(s)
			if (i == 0) {
				// treat as field
				parsedField = token.getValue().trim();
				i++;
			} else if (i == 1) {
				parsedOperator = token.getValue().trim();
				i++;
			} else if (i == 2) {
				parsedValue = token.getValue().trim();
				// TODO: add multiple values for BETWEEN, etc

				if (parsedField.isEmpty() || parsedOperator.isEmpty()
						|| parsedValue.isEmpty()) {
					throw new JargonQueryException(
							"query attribute/value/condition malformed around element:"
									+ tokenCtr);
				}

				queryCondition = QueryCondition.instance(parsedField,
						parsedOperator, parsedValue);
				queryConditions.add(queryCondition);
				i = 0;
			}

			tokenCtr++;
		}

		if (i > 0) {
			throw new JargonQueryException(
					"the last query condition is incomplete");
		}

		return queryConditions;
	}

	/**
	 * Take a single condition as a string and parse it out into components
	 * 
	 * @param query
	 * @param conditionOffset
	 * @throws JargonQueryException
	 */
	private List<QueryConditionToken> tokenizeConditions(final String query,
			final int conditionOffset) throws JargonQueryException {
		String conditionString = query.substring(conditionOffset);
		log.debug("conditions in string: {}", conditionString);

		// break conditions into tokens and store in an Array
		List<QueryConditionToken> queryTokens = new ArrayList<QueryConditionToken>();

		StringBuilder tokenAccum = new StringBuilder();

		QueryConditionToken token = null;
		boolean accumulatingQuotedLiteral = false;
		boolean escaped = false;
		char nextChar = ' ';

		// march through what's after the where clause
		for (int i = 0; i < conditionString.length(); i++) {

			nextChar = conditionString.charAt(i);
			// LOG.debug("nextChar:{}", nextChar);

			if (escaped) {
				tokenAccum.append(nextChar);
				escaped = false;
				continue;
			}

			if (nextChar == '\\') {
				if (escaped) {
					// LOG.debug("escaped, go ahead and append");
					tokenAccum.append(nextChar);
					escaped = false;
					continue;
				} else {
					// LOG.debug("slash indicates start of escape");
					escaped = true;
					continue;
				}
			}

			if (nextChar == ' ') {

				// space is delim unless in a quoted literal

				if (accumulatingQuotedLiteral) {
					tokenAccum.append(nextChar);
				} else {
					if (tokenAccum.length() > 0) {
						token = new QueryConditionToken();
						token.setValue(tokenAccum.toString());
						queryTokens.add(token);
						// LOG.debug("part is:{}", token);
						tokenAccum = new StringBuilder();
					}
				}
				continue;
			}

			// quote either starts or ends accumulation of a quoted literal

			if (nextChar == '\'') {
				if (accumulatingQuotedLiteral) {
					// this ends a literal, save the closing quote, and the next
					// space will cause the literal
					// to be tokenized
					tokenAccum.append(nextChar);
					accumulatingQuotedLiteral = false;
				} else {
					if (tokenAccum.length() > 0) {
						// this is an opening quote, but there are characters
						// accumulated before it, error
						throw new JargonQueryException(
								"error in condition at position "
										+ (conditionOffset + i)
										+ " an invalid quote character was encountered");
					} else {
						accumulatingQuotedLiteral = true;
						tokenAccum.append(nextChar);
					}
				}
				continue;
			}

			tokenAccum.append(nextChar);

		}

		// end of loop that was accumulating tokens, put out the last token
		if (accumulatingQuotedLiteral) {
			throw new JargonQueryException(
					"unclosed quoted literal found in condition");
		}

		// if I have accumulated a token, at the end put it into the tokens
		// table
		if (tokenAccum.length() > 0) {
			token = new QueryConditionToken();
			token.setValue(tokenAccum.toString());
			queryTokens.add(token);
		}

		return queryTokens;
	}

	public IRODSServerProperties getIrodsServerProperties() {
		return irodsServerProperties;
	}

	public void setIrodsServerProperties(
			final IRODSServerProperties irodsServerProperties) {
		this.irodsServerProperties = irodsServerProperties;
	}

	public ExtensibleMetaDataMapping getExtensibleMetaDataMapping() {
		return extensibleMetaDataMapping;
	}

	public void setExtensibleMetaDataMapping(
			final ExtensibleMetaDataMapping extensibleMetaDataMapping) {
		this.extensibleMetaDataMapping = extensibleMetaDataMapping;
	}
}
