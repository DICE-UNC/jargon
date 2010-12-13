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
public class TranslatedIRODSQuery {
	private final List<SelectField> selectFields;
	private final List<TranslatedQueryCondition> translatedQueryConditions;
	private final List<SelectField> groupByFields;
	private final IRODSQuery irodsQuery;
	private final boolean distinct;

	/**
	 * Create an instance of the query translation, this contains information
	 * about the original iquest-like query, as well as information about the
	 * parsed and translated query.
	 * 
	 * @param translatedSelectFields
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.query.SelectField}
	 *            representing the selects.
	 * @param translatedQueryConditions
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.query.TranslatedQueryCondition}
	 *            representing the parsed conditions.
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSQuery} that
	 *            encapsulates the original user query.
	 * @param distinct
	 *            <code>boolean</code> indicating whether this is a distinct
	 *            query.
	 * @return <code>TranslatedIRODSQuery</code>
	 * @throws JargonException
	 */
	public static TranslatedIRODSQuery instance(
			final List<SelectField> translatedSelectFields,
			final List<TranslatedQueryCondition> translatedQueryConditions,
			final IRODSQuery irodsQuery, final boolean distinct)
			throws JargonException {
		return new TranslatedIRODSQuery(translatedSelectFields,
				translatedQueryConditions, new ArrayList<SelectField>(),
				irodsQuery, distinct);

	}

	public static TranslatedIRODSQuery instanceWithGroupBy(
			final List<SelectField> translatedSelectFields,
			final List<TranslatedQueryCondition> translatedQueryConditions,
			final List<SelectField> groupByFields, final IRODSQuery irodsQuery,
			final boolean distinct) throws JargonException {
		return new TranslatedIRODSQuery(translatedSelectFields,
				translatedQueryConditions, groupByFields, irodsQuery, distinct);

	}

	/**
	 * Create an instance of the query translation, this contains information
	 * about the original iquest-like query, as well as information about the
	 * parsed and translated query.
	 * 
	 * @param translatedSelectFields
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.query.SelectField}
	 *            representing the selects.
	 * @param translatedQueryConditions
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.pub.query.TranslatedQueryCondition}
	 *            representing the parsed conditions.
	 * @param irodsQuery
	 *            {@link org.irods.jargon.core.query.IRODSQuery} that
	 *            encapsulates the original user query.
	 * @return
	 * @throws JargonException
	 */
	public static TranslatedIRODSQuery instance(
			final List<SelectField> translatedSelectFields,
			final List<TranslatedQueryCondition> translatedQueryConditions,
			final IRODSQuery irodsQuery) throws JargonException {
		return new TranslatedIRODSQuery(translatedSelectFields,
				translatedQueryConditions, new ArrayList<SelectField>(),
				irodsQuery, true);

	}

	private TranslatedIRODSQuery(final List<SelectField> selectFields,
			final List<TranslatedQueryCondition> translatedQueryConditions,
			final List<SelectField> groupByFields, final IRODSQuery irodsQuery,
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
	public List<SelectField> getSelectFields() {
		return selectFields;
	}

	/**
	 * Get the condition portion of a query translated into the internal
	 * representation.
	 * 
	 * @return {@link org.irods.jargon.core.query.TranslatedQueryCondition
	 *         TranslatedQueryCondition} containing the internal represntation
	 *         of the condition portion of the query.
	 */
	public List<TranslatedQueryCondition> getTranslatedQueryConditions() {
		return translatedQueryConditions;
	}

	public IRODSQuery getIrodsQuery() {
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

	public List<SelectField> getGroupByFields() {
		return groupByFields;
	}

}
