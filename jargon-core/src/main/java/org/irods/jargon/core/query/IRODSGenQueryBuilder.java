package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.GenQueryField.SelectFieldSource;
import org.irods.jargon.core.query.GenQueryOrderByField.OrderByType;

/**
 * Builder facility for iRODS gen queries. This class allows an iRODS general
 * query to be built via code, including the specification of select fields,
 * conditions, and order by parameters.
 * <p/>
 * This technique allows richer query development then the original 'iquest'
 * syntax facility that relied on the building of queries as 'iquest' select
 * strings.
 * <p/>
 * This class is not thread-safe, but really does not need to be. The queries
 * produced by the builder are immutable references to the fields in this
 * builder.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSGenQueryBuilder {

	private final List<GenQuerySelectField> selectFields = new ArrayList<GenQuerySelectField>();
	private final List<GenQueryBuilderCondition> conditions = new ArrayList<GenQueryBuilderCondition>();
	private final List<GenQueryOrderByField> orderByFields = new ArrayList<GenQueryOrderByField>();
	private final boolean distinct;
	private final boolean upperCase;
	/**
	 * Indicates whether a total row count should be included by iRODS
	 */
	private final boolean computeTotalRowCount;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\t distinct? ");
		sb.append(distinct);
		sb.append("\n\t  upperCase? ");
		sb.append(upperCase);
		sb.append("\n\t computeTotalRowCount? ");
		sb.append(computeTotalRowCount);
		sb.append("IRODSGenQueryBuilder");
		sb.append("\n\t  selects:");
		sb.append(selectFields);
		sb.append("\n\t conditions:");
		sb.append(conditions);
		sb.append("\n\t  orderByFields:");
		sb.append(orderByFields);
		return sb.toString();
	}

	/**
	 * Constructor takes an optional <code>ExtensibleMetadataMapping</code> if
	 * extensible metadata is to be used in the query processing.
	 * <p/>
	 * This version allows the specification of case insensitive queries
	 *
	 * @param distinct
	 *            <code>boolean</code> that indicates whether the select is
	 *            distinct
	 * @param upperCase
	 *            <code>boolean</code> which indicates that upper case should be
	 *            used in the where (case-insensitive queries)
	 * @param extensibleMetadataMapping
	 *            {@link ExtensibleMetadataMapping} that may be used in queries.
	 *            This can be <code>null</code> if not required
	 */
	public IRODSGenQueryBuilder(final boolean distinct,
			final boolean upperCase,
			final ExtensibleMetaDataMapping extensibleMetadataMapping) {
		this.distinct = distinct;
		this.upperCase = upperCase;
		computeTotalRowCount = false;

	}

	/**
	 * Constructor takes an optional <code>ExtensibleMetadataMapping</code> if
	 * extensible metadata is to be used in the query processing.
	 * <p/>
	 * This version allows the specification of case insensitive queries
	 *
	 * @param distinct
	 *            <code>boolean</code> that indicates whether the select is
	 *            distinct
	 * @param upperCase
	 *            <code>boolean</code> which indicates that upper case should be
	 *            used in the where (case-insensitive queries)
	 * @param computeTotalRowCount
	 *            <code>boolean</code> with a value of <code>true</code>
	 *            indicating that a total row count should be computed by iRODS
	 *            from this query. This may introduce overhead in the ICAT
	 *            database processing on the iRODS server
	 * @param extensibleMetadataMapping
	 *            {@link ExtensibleMetadataMapping} that may be used in queries.
	 *            This can be <code>null</code> if not required
	 */
	public IRODSGenQueryBuilder(final boolean distinct,
			final boolean upperCase, final boolean computeTotalRowCount,
			final ExtensibleMetaDataMapping extensibleMetadataMapping) {
		this.distinct = distinct;
		this.upperCase = upperCase;
		this.computeTotalRowCount = computeTotalRowCount;

	}

	/**
	 * Constructor takes an optional <code>ExtensibleMetadataMapping</code> if
	 * extensible metadata is to be used in the query processing.
	 *
	 * @param distinct
	 *            <code>boolean</code> that indicates whether the select is
	 *            distinct
	 * @param extensibleMetadataMapping
	 *            {@link ExtensibleMetadataMapping} that may be used in queries.
	 *            This can be <code>null</code> if not required
	 */
	public IRODSGenQueryBuilder(final boolean distinct,
			final ExtensibleMetaDataMapping extensibleMetadataMapping) {
		this.distinct = distinct;
		upperCase = false;
		computeTotalRowCount = false;

	}

	/**
	 * Add a select represented by a value in <code>RodsGenQueryEnum</code>.
	 * These are standard select fields.
	 *
	 * @param rodsGenQueryEnumValue
	 *            {@link RodsGenQueryEnum} value
	 * @return a reference to this builder, so that builder statements may be
	 *         chained
	 * @throws GenQueryBuilderException
	 */
	public IRODSGenQueryBuilder addSelectAsGenQueryValue(
			final RodsGenQueryEnum rodsGenQueryEnumValue)
			throws GenQueryBuilderException {
		if (rodsGenQueryEnumValue == null) {
			throw new IllegalArgumentException("null rodsGenQueryEnumValue");
		}

		try {
			GenQuerySelectField selectField = GenQuerySelectField.instance(
					rodsGenQueryEnumValue,
					GenQueryField.SelectFieldTypes.FIELD,
					GenQueryField.SelectFieldSource.DEFINED_QUERY_FIELD);

			addSelect(selectField);
			return this;

		} catch (JargonException e) {
			throw new GenQueryBuilderException("error adding select", e);
		}

	}

	/**
	 * Add a select field represented by a value in
	 * <code>RodsGenQueryEnum</code> as an aggregate field (e.g. a count()).
	 *
	 * @param rodsGenQueryEnumValue
	 *            {@link RodsGenQueryEnum} value
	 * @param selectFieldType
	 *            {@link GenQueryField.SelectFieldTypes} enum value with the
	 *            type of aggregation.
	 * @return a reference to this builder, so that builder statements may be
	 *         chained
	 * @throws GenQueryBuilderException
	 */
	public IRODSGenQueryBuilder addSelectAsAgregateGenQueryValue(
			final RodsGenQueryEnum rodsGenQueryEnumValue,
			final GenQueryField.SelectFieldTypes selectFieldType)
			throws GenQueryBuilderException {
		if (rodsGenQueryEnumValue == null) {
			throw new IllegalArgumentException("null rodsGenQueryEnumValue");
		}

		try {
			GenQuerySelectField selectField = GenQuerySelectField.instance(
					rodsGenQueryEnumValue, selectFieldType,
					GenQueryField.SelectFieldSource.DEFINED_QUERY_FIELD);

			addSelect(selectField);
			return this;

		} catch (JargonException e) {
			throw new GenQueryBuilderException("error adding select", e);
		}

	}

	/**
	 * Add a gen query condition to the builder query.
	 *
	 * @param rodsGenQueryEnumValue
	 *            {@link RodsGenQueryEnumValue} for the condition
	 * @param operator
	 *            {@link QueryConditionOperators} enum value for the operator of
	 *            the condition
	 * @param value
	 *            <code>long</code> with the value for the condition
	 * @return a reference to this builder, so that builder statements may be
	 *         chained
	 */
	public IRODSGenQueryBuilder addConditionAsGenQueryField(
			final RodsGenQueryEnum rodsGenQueryEnumValue,
			final QueryConditionOperators operator, final long value) {

		return addConditionAsGenQueryField(rodsGenQueryEnumValue, operator,
				String.valueOf(value));

	}

	/**
	 * Add a gen query condition to the builder query.
	 *
	 * @param rodsGenQueryEnumValue
	 *            {@link RodsGenQueryEnumValue} for the condition
	 * @param operator
	 *            {@link QueryConditionOperators} enum value for the operator of
	 *            the condition
	 * @param value
	 *            <code>int</code> with the value for the condition
	 * @return a reference to this builder, so that builder statements may be
	 *         chained
	 */
	public IRODSGenQueryBuilder addConditionAsGenQueryField(
			final RodsGenQueryEnum rodsGenQueryEnumValue,
			final QueryConditionOperators operator, final int value) {

		return addConditionAsGenQueryField(rodsGenQueryEnumValue, operator,
				String.valueOf(value));

	}

	/**
	 * Add a condition that is multi-value operator condition such as IN and
	 * BETWEEN, with a <code>List<String></code> of values provided without
	 * quotes (these are added later)
	 *
	 * @param rodsGenQueryEnumValue
	 *            {@link RodsGenQueryEnumValue} for the condition
	 * @param operator
	 *            {@link QueryConditionOperators} enum value for the operator of
	 *            the condition that is a multi value operator such as IN
	 * @param nonQuotedValues
	 *            <code>List<String></code>
	 * @return
	 */
	public IRODSGenQueryBuilder addConditionAsMultiValueCondition(
			final RodsGenQueryEnum rodsGenQueryEnumValue,
			final QueryConditionOperators operator,
			final List<String> nonQuotedValues) {

		if (rodsGenQueryEnumValue == null) {
			throw new IllegalArgumentException("null rodsGenQueryEnumValue");
		}

		if (operator == null) {
			throw new IllegalArgumentException("null operator");
		}

		if (nonQuotedValues == null || nonQuotedValues.isEmpty()) {
			throw new IllegalArgumentException("null or empty nonQuotedValues");
		}

		/*
		 * Format the query based on the operator TODO: add handling for tables,
		 * in, etc
		 */

		if (operator == QueryConditionOperators.IN) {
			GenQueryBuilderCondition genQueryBuilderCondition = GenQueryBuilderCondition

			.instanceForIn(rodsGenQueryEnumValue.getName(),
					SelectFieldSource.DEFINED_QUERY_FIELD,
					String.valueOf(rodsGenQueryEnumValue.getNumericValue()),
					nonQuotedValues);
			conditions.add(genQueryBuilderCondition);

		} else if (operator == QueryConditionOperators.BETWEEN) {
			GenQueryBuilderCondition genQueryBuilderCondition = GenQueryBuilderCondition

			.instanceForBetween(rodsGenQueryEnumValue.getName(),
					SelectFieldSource.DEFINED_QUERY_FIELD,
					String.valueOf(rodsGenQueryEnumValue.getNumericValue()),
					nonQuotedValues);
			conditions.add(genQueryBuilderCondition);
		} else {
			throw new UnsupportedOperationException(
					"query operator not yet supported:" + operator);
		}

		return this;

	}

	/**
	 * Add a gen query condition to the builder query.
	 *
	 * @param rodsGenQueryEnumValue
	 *            {@link RodsGenQueryEnumValue} for the condition
	 * @param operator
	 *            {@link QueryConditionOperators} enum value for the operator of
	 *            the condition
	 * @param value
	 *            <code>String</code> with the value for the condition
	 * @return a reference to this builder, so that builder statements may be
	 *         chained
	 */
	public IRODSGenQueryBuilder addConditionAsGenQueryField(
			final RodsGenQueryEnum rodsGenQueryEnumValue,
			final QueryConditionOperators operator, final String value) {

		if (rodsGenQueryEnumValue == null) {
			throw new IllegalArgumentException("null rodsGenQueryEnumValue");
		}

		if (operator == null) {
			throw new IllegalArgumentException("null operator");
		}

		if (value == null) {
			throw new IllegalArgumentException("null unit value");
		}

		/*
		 * Format the query based on the operator TODO: add handling for tables,
		 * in, etc
		 */

		if (operator == QueryConditionOperators.IN) {
			GenQueryBuilderCondition genQueryBuilderCondition = GenQueryBuilderCondition
					.instance(rodsGenQueryEnumValue.getName(),
							SelectFieldSource.DEFINED_QUERY_FIELD, String
									.valueOf(rodsGenQueryEnumValue
											.getNumericValue()), operator,
							value);

			conditions.add(genQueryBuilderCondition);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("'");
			sb.append(value);
			sb.append("'");

			GenQueryBuilderCondition genQueryBuilderCondition = GenQueryBuilderCondition
					.instance(rodsGenQueryEnumValue.getName(),
							SelectFieldSource.DEFINED_QUERY_FIELD, String
									.valueOf(rodsGenQueryEnumValue
											.getNumericValue()), operator, sb
									.toString());

			conditions.add(genQueryBuilderCondition);
		}

		return this;

	}

	/**
	 * Add an order by field. Note that this field has to be already in the
	 * selects, and must be ascending or descending
	 *
	 * @param rodsGenQueryEnumValue
	 *            {@link RodsGenQueryEnumValue} for the given field
	 * @param orderByType
	 *            {@link OrderByType} that must be ascending or descending
	 * @return
	 * @throws GenQueryBuilderException
	 */
	public IRODSGenQueryBuilder addOrderByGenQueryField(
			final RodsGenQueryEnum rodsGenQueryEnumValue,
			final GenQueryOrderByField.OrderByType orderByType)
			throws GenQueryBuilderException {

		if (rodsGenQueryEnumValue == null) {
			throw new IllegalArgumentException("null rodsGenQueryEnumValue");
		}

		if (orderByType == null) {
			throw new IllegalArgumentException("null orderByType");
		}

		if (orderByType == OrderByType.NONE) {
			throw new IllegalArgumentException(
					"ascending or descending order by must be specified");
		}

		boolean selectFound = false;
		for (GenQuerySelectField genQuerySelectField : selectFields) {

			if (Integer.parseInt(genQuerySelectField
					.getSelectFieldNumericTranslation()) == rodsGenQueryEnumValue
					.getNumericValue()) {
				selectFound = true;
				break;
			}

		}

		if (!selectFound) {
			throw new GenQueryBuilderException(
					"order by field is not represented in the select statements");
		}

		GenQueryOrderByField orderByField = GenQueryOrderByField.instance(
				rodsGenQueryEnumValue.getName(),
				SelectFieldSource.DEFINED_QUERY_FIELD,
				String.valueOf(rodsGenQueryEnumValue.getNumericValue()),
				orderByType);

		orderByFields.add(orderByField);
		return this;

	}

	/**
	 * Add a gen query select field to the bulder query.
	 *
	 * @param genQuerySelectField
	 *            {@link GenQuerySelectField} with the select to add. These are
	 *            translated into a query in the order they are added.
	 * @return a reference to the builder, so that builder operations may be
	 *         chained
	 * @throws GenQueryBuilderException
	 *             if a duplicate select is added
	 */
	private IRODSGenQueryBuilder addSelect(
			final GenQuerySelectField genQuerySelectField)
			throws GenQueryBuilderException {
		if (genQuerySelectField == null) {
			throw new IllegalArgumentException("null genQuerySelectField");
		}

		for (GenQuerySelectField existingQuerySelectField : selectFields) {
			if (genQuerySelectField.getSelectFieldNumericTranslation()
					.equals(existingQuerySelectField
							.getSelectFieldNumericTranslation())) {
				throw new GenQueryBuilderException("duplicate select field");
			}
		}

		selectFields.add(genQuerySelectField);
		return this;

	}

	/**
	 * @return the distinct
	 */
	public boolean isDistinct() {
		return distinct;
	}

	/**
	 * Return a query that can be executed by the general query execution
	 * service
	 *
	 * @param numberOfResultsDesired
	 *            <code>int</code> that is >= 1 indicating the number of desired
	 *            results
	 * @return {@link IRODSGenQueryFromBuilder} that can be executed by the
	 *         general query executor service
	 * @throws GenQueryBuilderException
	 *             if hte query cannot be built
	 */
	public IRODSGenQueryFromBuilder exportIRODSQueryFromBuilder(
			final int numberOfResultsDesired) throws GenQueryBuilderException {
		if (numberOfResultsDesired <= 0) {
			throw new IllegalArgumentException(
					"numberOfResultsDesired must be >= 1");
		}
		IRODSGenQueryBuilderQueryData queryData = IRODSGenQueryBuilderQueryData
				.instance(selectFields, conditions, orderByFields, distinct,
						upperCase, computeTotalRowCount);

		if (!queryData.isQueryValid()) {
			throw new GenQueryBuilderException(
					"query is not valid, cannot export");
		}

		return IRODSGenQueryFromBuilder.instance(queryData,
				numberOfResultsDesired);
	}

	/**
	 * Is this a case-insensitive query? (supported in iRODS 3.2 and higher)
	 *
	 * @return
	 */
	public boolean isUpperCase() {
		return upperCase;
	}

	/**
	 * @return the computeTotalRowCount
	 */
	public boolean isComputeTotalRowCount() {
		return computeTotalRowCount;
	}

}
