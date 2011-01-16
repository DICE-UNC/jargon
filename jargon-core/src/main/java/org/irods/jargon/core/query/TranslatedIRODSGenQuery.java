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
 * text and the format of queries understood in a <code>Tag</code> format.
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
	private final List<GenQuerySelectField> groupByFields;
	private final IRODSGenQuery irodsQuery;
	private final boolean distinct;

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
			final IRODSGenQuery irodsQuery, final boolean distinct)
			throws JargonException {
		return new TranslatedIRODSGenQuery(translatedSelectFields,
				translatedQueryConditions, new ArrayList<GenQuerySelectField>(),
				irodsQuery, distinct);

	}

	public static TranslatedIRODSGenQuery instanceWithGroupBy(
			final List<GenQuerySelectField> translatedSelectFields,
			final List<TranslatedGenQueryCondition> translatedQueryConditions,
			final List<GenQuerySelectField> groupByFields, final IRODSGenQuery irodsQuery,
			final boolean distinct) throws JargonException {
		return new TranslatedIRODSGenQuery(translatedSelectFields,
				translatedQueryConditions, groupByFields, irodsQuery, distinct);

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
			final IRODSGenQuery irodsQuery) throws JargonException {
		return new TranslatedIRODSGenQuery(translatedSelectFields,
				translatedQueryConditions, new ArrayList<GenQuerySelectField>(),
				irodsQuery, true);

	}

	private TranslatedIRODSGenQuery(final List<GenQuerySelectField> selectFields,
			final List<TranslatedGenQueryCondition> translatedQueryConditions,
			final List<GenQuerySelectField> groupByFields, final IRODSGenQuery irodsQuery,
			final boolean distinct) throws JargonException {

		if (translatedQueryConditions == null) {
			throw new JargonException("conditions are null");
		}

		if (selectFields == null) {
			throw new JargonException("no select column names");
		}

		if (groupByFields == null) {
			throw new JargonException("null groupByFields");
		}

		if (selectFields.isEmpty()) {
			throw new JargonException("no select column names");
		}

		if (irodsQuery == null) {
			throw new JargonException("irodsQuery is null");
		}

		this.translatedQueryConditions = translatedQueryConditions;
		this.selectFields = selectFields;
		this.groupByFields = groupByFields;
		this.irodsQuery = irodsQuery;
		this.distinct = distinct;

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

	public IRODSGenQuery getIrodsQuery() {
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
		sb.append("\n   irodsQuery:");
		sb.append(irodsQuery);
		sb.append("\n   distinct:");
		sb.append(distinct);
		return sb.toString();
	}

	public List<GenQuerySelectField> getGroupByFields() {
		return groupByFields;
	}

}
