package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an immutable iRODS gen query built using the gen query builder
 * tool. This is an improvement over the original 'iquest'-like string allowing
 * more expressive queries.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
class IRODSGenQueryBuilderQueryData {

	private final List<GenQuerySelectField> selectFields;
	private final List<GenQueryBuilderCondition> conditions;
	private final List<GenQueryOrderByField> orderByFields;
	private final boolean distinct;
	private final boolean upperCase;
	/**
	 * Indicates whether to ask iRODS to provide a total row count in the query
	 * results
	 */
	private final boolean computeTotalRowCount;

	/**
	 * Instance to create an immutable representation of the query
	 *
	 * @param selectFields
	 *            {@link GenQuerySelectField} that describes a select fields for
	 *            the query, of which there must be at least one
	 * @param conditions
	 *            {@link GenQueryBuilderCondition} that describes optional
	 *            conditions for the query, may be set to {@code null}
	 * @param orderByFields
	 *            {@link GenQueryOrderByField} that describes optional order by
	 *            values for the query, may be set to {@code null}
	 * @param distinct
	 *            {@code boolean} that indicates whether the query is a
	 *            select distinct
	 * @param upperCase
	 *            {@code boolean} indicates whether the query uses
	 *            case-insensitive conditions
	 * @return immutable instance of {@code IRODSGenBuilderQuery}
	 */
	public static IRODSGenQueryBuilderQueryData instance(
			final List<GenQuerySelectField> selectFields,
			final List<GenQueryBuilderCondition> conditions,
			final List<GenQueryOrderByField> orderByFields,
			final boolean distinct, final boolean upperCase,
			final boolean computeTotalRowCount) {
		return new IRODSGenQueryBuilderQueryData(selectFields, conditions,
				orderByFields, distinct, upperCase, computeTotalRowCount);
	}

	/**
	 * Instance to create an immutable representation of the query
	 *
	 * @param selectFields
	 *            {@link GenQuerySelectField} that describes a select fields for
	 *            the query, of which there must be at least one
	 * @param conditions
	 *            {@link GenQueryBuilderCondition} that describes optional
	 *            conditions for the query, may be set to {@code null}
	 * @param orderByFields
	 *            {@link GenQueryOrderByField} that describes optional order by
	 *            values for the query, may be set to {@code null}
	 * @param distinct
	 *            {@code boolean} that indicates whether the query is a
	 *            select distinct
	 * @param upperCase
	 *            {@code boolean} indicates whether the query uses
	 *            case-insensitive conditions
	 * @return immutable instance of {@code IRODSGenBuilderQuery}
	 */
	public static IRODSGenQueryBuilderQueryData instance(
			final List<GenQuerySelectField> selectFields,
			final List<GenQueryBuilderCondition> conditions,
			final List<GenQueryOrderByField> orderByFields,
			final boolean distinct, final boolean upperCase) {
		return new IRODSGenQueryBuilderQueryData(selectFields, conditions,
				orderByFields, distinct, upperCase, false);
	}

	/**
	 * Private constructor (instance method is used to create)
	 *
	 * @param selectFields
	 *            {@link GenQuerySelectField} that describes a select fields for
	 *            the query, of which there must be at least one
	 * @param conditions
	 *            {@link GenQueryBuilderCondition} that describes optional
	 *            conditions for the query, may be set to {@code null}
	 * @param orderByFields
	 *            {@link GenQueryOrderByField} that describes optional order by
	 *            values for the query, may be set to {@code null}
	 * @param distinct
	 *            {@code boolean} that indicates whether the query is a
	 *            select distinct
	 * @param upperCase
	 *            {@code boolean} indicates whether the query uses
	 *            case-insensitive conditions
	 * @param computeTotalRowCount
	 *            {@code boolean} that indicates that the total row count
	 *            should be returned, this might carry a performance penalty. If
	 *            this is {@code true} the eventual result set will contain
	 *            the iRODS response from the query with the total rows to be
	 *            returned
	 */
	private IRODSGenQueryBuilderQueryData(
			final List<GenQuerySelectField> selectFields,
			final List<GenQueryBuilderCondition> conditions,
			final List<GenQueryOrderByField> orderByFields,
			final boolean distinct, final boolean upperCase,
			final boolean computeTotalRowCount) {

		if (selectFields == null || selectFields.isEmpty()) {
			throw new IllegalArgumentException("null or empty selectFields");
		}

		this.selectFields = Collections.unmodifiableList(selectFields);

		if (conditions == null) {
			this.conditions = Collections
					.unmodifiableList(new ArrayList<GenQueryBuilderCondition>());
		} else {
			this.conditions = Collections.unmodifiableList(conditions);
		}

		if (orderByFields == null) {
			this.orderByFields = Collections
					.unmodifiableList(new ArrayList<GenQueryOrderByField>());
		} else {
			this.orderByFields = Collections.unmodifiableList(orderByFields);
		}

		this.distinct = distinct;
		this.upperCase = upperCase;
		this.computeTotalRowCount = computeTotalRowCount;

	}

	/**
	 * @return the selectFields
	 */
	public List<GenQuerySelectField> getSelectFields() {
		return selectFields;
	}

	/**
	 * @return the conditions
	 */
	public List<GenQueryBuilderCondition> getConditions() {
		return conditions;
	}

	/**
	 * @return the orderByFields
	 */
	public List<GenQueryOrderByField> getOrderByFields() {
		return orderByFields;
	}

	/**
	 * Is the query sufficiently defined to process? Right now, this means
	 * there's at least one field selected. Other edits may be added as this is
	 * fully implemented
	 *
	 * @return {@code boolean} of {@code true} if the query is
	 *         correctly defined to process
	 */
	public boolean isQueryValid() {
		boolean valid = true;
		if (getSelectFields().isEmpty()) {
			valid = false;
		}
		return valid;
	}

	/**
	 * @return distinct {@code boolean} indicates whether the query
	 *         specifies distinct
	 */
	public boolean isDistinct() {
		return distinct;
	}

	/**
	 * @return the upperCase {@code boolean} indicates whether the query
	 *         uses case-insensitive conditions
	 */
	public boolean isUpperCase() {
		return upperCase;
	}

	public boolean isComputeTotalRowCount() {
		return computeTotalRowCount;
	}

}
