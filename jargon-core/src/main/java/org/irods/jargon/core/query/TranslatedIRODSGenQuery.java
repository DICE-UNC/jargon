/**
 *
 */
package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents a query translated into a format that can be processed into the
 * IRODS protocol. Essentially this is a bridge between a query stated as plain
 * text or via the builder, and the format of queries understood in a
 * <code>Tag</code> format.
 * 
 * This object is immutable, and is safe to share between threads. This class is
 * not marked final to assist in testability.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TranslatedIRODSGenQuery {
	private final List<GenQuerySelectField> selectFields;
	private final List<TranslatedGenQueryCondition> translatedQueryConditions;
	private final List<GenQueryOrderByField> orderByFields;
	private final AbstractIRODSGenQuery irodsQuery;
	private final boolean distinct;
	private final boolean upperCase;
	/**
	 * Indicates whether a total row count should be included by iRODS
	 */
	private final boolean computeTotalRowCount;

	/**
	 * Create an instance of the query translation, this contains information
	 * about the original iquest-like query, as well as information about the
	 * parsed and translated query.
	 * <p/>
	 * This version allows specification of 'upperCase'
	 * 
	 * @param translatedSelectFields
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.GenQuerySelectField.SelectField}
	 *            representing the selects.
	 * @param translatedQueryConditions
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.TranslatedGenQueryCondition.TranslatedQueryCondition}
	 *            representing the parsed conditions.
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSGenQuery} that
	 *            encapsulates the original user query.
	 * @param distinct
	 *            <code>boolean</code> indicating whether this is a distinct
	 *            query.
	 * @param upperCase
	 *            <code>boolean</code> which indicates that upper case should be
	 *            used in the where (case-insensitive queries)
	 * @return <code>TranslatedIRODSQuery</code>
	 * @throws JargonException
	 */
	public static TranslatedIRODSGenQuery instance(
			final List<GenQuerySelectField> translatedSelectFields,
			final List<TranslatedGenQueryCondition> translatedQueryConditions,
			final AbstractIRODSGenQuery irodsQuery, final boolean distinct,
			final boolean upperCase) throws JargonException {
		return new TranslatedIRODSGenQuery(translatedSelectFields,
				translatedQueryConditions, null, irodsQuery, distinct,
				upperCase, false);

	}

	/**
	 * Create an instance of the query translation, this contains information
	 * about the original iquest-like query, as well as information about the
	 * parsed and translated query.
	 * 
	 * @param translatedSelectFields
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.GenQuerySelectField.SelectField}
	 *            representing the selects.
	 * @param translatedQueryConditions
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.TranslatedGenQueryCondition.TranslatedQueryCondition}
	 *            representing the parsed conditions.
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSGenQuery} that
	 *            encapsulates the original user query.
	 * @param distinct
	 *            <code>boolean</code> indicating whether this is a distinct
	 *            query.
	 * @return <code>TranslatedIRODSQuery</code>
	 * @throws JargonException
	 */
	public static TranslatedIRODSGenQuery instance(
			final List<GenQuerySelectField> translatedSelectFields,
			final List<TranslatedGenQueryCondition> translatedQueryConditions,
			final AbstractIRODSGenQuery irodsQuery, final boolean distinct)
			throws JargonException {
		return new TranslatedIRODSGenQuery(translatedSelectFields,
				translatedQueryConditions, null, irodsQuery, distinct, false,
				false);

	}

	/**
	 * Create an instance of the query translation, this contains information
	 * about the original query, as well as information about the parsed and
	 * translated query.
	 * 
	 * @param translatedSelectFields
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.GenQuerySelectField.SelectField}
	 *            representing the selects.
	 * @param translatedQueryConditions
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.TranslatedGenQueryCondition.TranslatedQueryCondition}
	 *            representing the parsed conditions.
	 * @param orderByFields
	 *            <code>List</code> of {@link GenQueryOrderByField} that has
	 *            order by data
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSGenQuery} that
	 *            encapsulates the original user query.
	 * @param distinct
	 *            <code>boolean</code> indicating whether this is a distinct
	 *            query.
	 * @param caseInsensitive
	 *            <code>boolean</code> indicating that the query will be
	 *            case-insensitive for condition values
	 * @return <code>TranslatedIRODSQuery</code>
	 * @throws JargonException
	 */
	public static TranslatedIRODSGenQuery instance(
			final List<GenQuerySelectField> translatedSelectFields,
			final List<TranslatedGenQueryCondition> translatedQueryConditions,
			final List<GenQueryOrderByField> orderByFields,
			final AbstractIRODSGenQuery irodsQuery, final boolean distinct,
			final boolean caseInsensitive) throws JargonException {
		return new TranslatedIRODSGenQuery(translatedSelectFields,
				translatedQueryConditions, orderByFields, irodsQuery, distinct,
				caseInsensitive, false);

	}

	/**
	 * Create an instance of the query translation, this contains information
	 * about the original query, as well as information about the parsed and
	 * translated query.
	 * 
	 * @param translatedSelectFields
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.GenQuerySelectField.SelectField}
	 *            representing the selects.
	 * @param translatedQueryConditions
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.TranslatedGenQueryCondition.TranslatedQueryCondition}
	 *            representing the parsed conditions.
	 * @param orderByFields
	 *            <code>List</code> of {@link GenQueryOrderByField} that has
	 *            order by data
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSGenQuery} that
	 *            encapsulates the original user query.
	 * @param distinct
	 *            <code>boolean</code> indicating whether this is a distinct
	 *            query.
	 * @param caseInsensitive
	 *            <code>boolean</code> indicating that the query will be
	 *            case-insensitive for condition values
	 * @param computeTotalRowCount
	 *            <code>boolean</code> that indicates that the total row count
	 *            should be returned, this might carry a performance penalty. If
	 *            this is <code>true</code> the eventual result set will contain
	 *            the iRODS response from the query with the total rows to be
	 *            returned
	 * @return <code>TranslatedIRODSQuery</code>
	 * @throws JargonException
	 */
	public static TranslatedIRODSGenQuery instance(
			final List<GenQuerySelectField> translatedSelectFields,
			final List<TranslatedGenQueryCondition> translatedQueryConditions,
			final List<GenQueryOrderByField> orderByFields,
			final AbstractIRODSGenQuery irodsQuery, final boolean distinct,
			final boolean caseInsensitive, final boolean computeTotalRowCount)
			throws JargonException {
		return new TranslatedIRODSGenQuery(translatedSelectFields,
				translatedQueryConditions, orderByFields, irodsQuery, distinct,
				caseInsensitive, computeTotalRowCount);

	}

	/**
	 * Create an instance of the query translation, this contains information
	 * about the original iquest-like query, as well as information about the
	 * parsed and translated query.
	 * 
	 * @param translatedSelectFields
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.GenQuerySelectField.SelectField}
	 *            representing the selects.
	 * @param translatedQueryConditions
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.TranslatedGenQueryCondition.TranslatedQueryCondition}
	 *            representing the parsed conditions.
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSGenQuery} that
	 *            encapsulates the original user query.
	 * @return
	 * @throws JargonException
	 */
	public static TranslatedIRODSGenQuery instance(
			final List<GenQuerySelectField> translatedSelectFields,
			final List<TranslatedGenQueryCondition> translatedQueryConditions,
			final AbstractIRODSGenQuery irodsQuery) throws JargonException {
		return new TranslatedIRODSGenQuery(translatedSelectFields,
				translatedQueryConditions, null, irodsQuery, true, false, false);

	}

	private TranslatedIRODSGenQuery(
			final List<GenQuerySelectField> selectFields,
			final List<TranslatedGenQueryCondition> translatedQueryConditions,
			final List<GenQueryOrderByField> orderByFields,
			final AbstractIRODSGenQuery irodsQuery, final boolean distinct,
			final boolean upperCase, final boolean computeTotalRowCount)
			throws JargonException {

		if (translatedQueryConditions == null) {
			throw new JargonException("conditions are null");
		}

		if (selectFields == null) {
			throw new JargonException("no select column names");
		}

		if (orderByFields == null) {
			this.orderByFields = new ArrayList<GenQueryOrderByField>();
		} else {
			this.orderByFields = orderByFields;
		}

		if (selectFields.isEmpty()) {
			throw new JargonException("no select column names");
		}

		if (irodsQuery == null) {
			throw new JargonException("irodsQuery is null");
		}

		this.translatedQueryConditions = translatedQueryConditions;
		this.selectFields = selectFields;
		this.irodsQuery = irodsQuery;
		this.distinct = distinct;
		this.upperCase = upperCase;
		this.computeTotalRowCount = computeTotalRowCount;

	}

	/**
	 * Get the {@link org.irods.jargon.core.query.RodsGenQueryEnum
	 * RodsGenQueryEnum} data that describes the particular select column.
	 * 
	 * @return <code>RodsGenQueryEnum</code> with the column names translated in
	 *         the internal representation.
	 */
	public List<GenQuerySelectField> getSelectFields() {
		return selectFields;
	}

	/**
	 * Get the condition portion of a query translated into the internal
	 * representation.
	 * 
	 * @return {@link org.irods.jargon.core.query.TranslatedGenQueryCondition
	 *         TranslatedQueryCondition} containing the internal represntation
	 *         of the condition portion of the query.
	 */
	public List<TranslatedGenQueryCondition> getTranslatedQueryConditions() {
		return translatedQueryConditions;
	}

	public AbstractIRODSGenQuery getIrodsQuery() {
		return irodsQuery;
	}

	public boolean isDistinct() {
		return distinct;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("translatedIRODSQuery:");
		sb.append("\n   selectFields:");
		sb.append(selectFields);
		sb.append("\n   translatedQueryConditions:");
		sb.append(translatedQueryConditions);
		sb.append("\n   orderByFields:");
		sb.append(orderByFields);
		sb.append("\n   irodsQuery:");
		sb.append(irodsQuery);
		sb.append("\n   distinct:");
		sb.append(distinct);
		sb.append("\n   upperCase:");
		sb.append(upperCase);
		sb.append("\n   computeTotalRowCount:");
		sb.append(computeTotalRowCount);
		return sb.toString();
	}

	/**
	 * @return the orderByFields
	 */
	public List<GenQueryOrderByField> getOrderByFields() {
		return orderByFields;
	}

	/**
	 * Indicates that
	 * 
	 * @return
	 */
	public boolean isUpperCase() {
		return upperCase;
	}

	public boolean isComputeTotalRowCount() {
		return computeTotalRowCount;
	}

}
