/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.core.packinstr;

import static edu.sdsc.grid.io.irods.IRODSConstants.options;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.GenQuerySelectField;
import org.irods.jargon.core.query.TranslatedIRODSGenQuery;
import org.irods.jargon.core.query.TranslatedGenQueryCondition;

import edu.sdsc.grid.io.irods.Tag;

/**
 * Wrap a query to IRODS, note that the only shared object is
 * <code>IRODSQuery</code> which is immutable, so this class should be
 * thread-safe.
 * 
 * lib/core/include/rodsGenQuery.h
 * 
 * #define GenQueryInp_PI "int maxRows; int continueInx; int partialStartIndex;
 * int options; struct KeyValPair_PI; struct InxIvalPair_PI; struct
 * InxValPair_PI;"
 * 
 * @author toaster
 */
public class GenQueryInp extends AbstractIRODSPackingInstruction implements
		IRodsPI {

	private final TranslatedIRODSGenQuery translatedIRODSQuery;
	private final int continueIndex;
	private final int partialStartIndex;
	public static final String PI_TAG = "GenQueryInp_PI";
	public static final String MAX_ROWS = "maxRows";
	public static final String CONTINUE_INX = "continueInx";
	public static final String PARTIAL_START_INDEX = "partialStartIndex";
	public static final String IILEN = "iiLen";
	public static final String ISLEN = "isLen";
	public static final String INX = "inx";
	public static final String IVALUE = "ivalue";
	public static final String SVALUE = "svalue";
	public static final String INX_VAL_PAIR_PI = "InxValPair_PI";
	public static final String INX_IVAL_PAIR_PI = "InxIvalPair_PI";

	public static final int API_NBR = 702;

	/**
	 * Return an instance of a query command that defaults to no partial start.
	 * 
	 * @param translatedIRODSQuery
	 * @param continueIndex
	 *            <code>int</code> with a 0 or 1 to indicate continuation of a
	 *            previous query that had more results
	 * @return <code>GenQueryInp</code> instance
	 * @throws JargonException
	 */
	public static GenQueryInp instance(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final int continueIndex) throws JargonException {
		return new GenQueryInp(translatedIRODSQuery, continueIndex, 0);
	}

	/**
	 * Return an instance of a query command that has a partial start index for
	 * paging behavior.
	 * 
	 * @param translatedIRODSQuery
	 * @param continueIndex
	 *            <code>int</code> with a 0 or 1 to indicate continuation of a
	 *            previous query that had more results
	 * @return <code>GenQueryInp</code> instance
	 * @param partialStartIndex
	 *            <code>int</code> with the offset within the result set to
	 *            start returning rows from
	 * @return <code>GenQueryInp</code> instance
	 * @throws JargonException
	 */
	public static GenQueryInp instanceWithPartialStart(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final int partialStartIndex) throws JargonException {
		return new GenQueryInp(translatedIRODSQuery, 0, partialStartIndex);
	}

	private GenQueryInp(final TranslatedIRODSGenQuery translatedIRODSQuery,
			final int continueIndex, final int partialStartIndex)
			throws JargonException {
		if (translatedIRODSQuery == null) {
			throw new JargonException("irodsQuery is null");
		}

		if (partialStartIndex < 0) {
			throw new JargonException("partialStartIndex is less than 0");
		}

		if (continueIndex < 0) {
			throw new JargonException("continue Index must be 0 or greater");
		}

		this.translatedIRODSQuery = translatedIRODSQuery;
		this.continueIndex = continueIndex;
		this.partialStartIndex = partialStartIndex;
		this.setApiNumber(API_NBR);
	}

	public Object responseAsObject() throws JargonException {
		return null;

	}

	/**
	 * @return <code>int</code> with the offset requested into the result set
	 */
	public int getContinueIndex() {
		return continueIndex;
	}

	/**
	 * @return {@link org.irods.jargon.core.TranslatedIRODSGenQuery.TranslatedIRODSQuery
	 *         TranslatedIRODSQuery} represents the parsed view of the query.
	 *         Note that an exception is thrown if the translated query has not
	 *         been derived TODO: refactor out, possibly with a return container
	 *         of multiple objects in getParsedTags()
	 * @throws JargonException
	 */
	public TranslatedIRODSGenQuery getTranslatedIRODSQuery()
			throws JargonException {
		if (translatedIRODSQuery == null) {
			throw new JargonException("no translated query");
		}
		return translatedIRODSQuery;
	}

	@Override
	public Tag getTagValue() throws JargonException {
		Tag message = new Tag(PI_TAG, new Tag[] {
				new Tag(MAX_ROWS, translatedIRODSQuery.getIrodsQuery()
						.getNumberOfResultsDesired()),
				new Tag(CONTINUE_INX, continueIndex), // new query
				new Tag(PARTIAL_START_INDEX, partialStartIndex) });

		// set distinct;
		if (this.getTranslatedIRODSQuery().isDistinct()) {
			message.addTag(new Tag(options, 0));
		} else {
			message.addTag(new Tag(options, 1));
		}

		message.addTag(Tag.createKeyValueTag(null));

		Tag[] subTags = null;
		int j = 1;

		subTags = new Tag[translatedIRODSQuery.getSelectFields().size() * 2 + 1];
		subTags[0] = new Tag(IILEN, translatedIRODSQuery.getSelectFields()
				.size());

		for (GenQuerySelectField select : translatedIRODSQuery.getSelectFields()) {
			subTags[j] = new Tag(INX, select.getSelectFieldNumericTranslation());
			j++;

		}

		for (GenQuerySelectField select : translatedIRODSQuery.getSelectFields()) {
			if (select.getSelectFieldType() == GenQuerySelectField.SelectFieldTypes.FIELD) {
				subTags[j] = new Tag(IVALUE, 1);
			} else if (select.getSelectFieldType() == GenQuerySelectField.SelectFieldTypes.AVG) {
				subTags[j] = new Tag(IVALUE, 5);
			} else if (select.getSelectFieldType() == GenQuerySelectField.SelectFieldTypes.COUNT) {
				subTags[j] = new Tag(IVALUE, 6);
			} else if (select.getSelectFieldType() == GenQuerySelectField.SelectFieldTypes.MAX) {
				subTags[j] = new Tag(IVALUE, 3);
			} else if (select.getSelectFieldType() == GenQuerySelectField.SelectFieldTypes.MIN) {
				subTags[j] = new Tag(IVALUE, 2);
			} else if (select.getSelectFieldType() == GenQuerySelectField.SelectFieldTypes.SUM) {
				subTags[j] = new Tag(IVALUE, 4);
			} else if (select.getSelectFieldType() == GenQuerySelectField.SelectFieldTypes.FILE_ACCESS) {
				subTags[j] = new Tag(IVALUE, 1024);
			} else {
				throw new JargonException(
						"unknown select type, cannot translate to XML protocol:"
								+ select.getSelectFieldType());
			}
			j++;
		}

		// FIXME: bug 112 fixes like in jargon trunk for query performance, need
		// to fix here
		message.addTag(new Tag(INX_IVAL_PAIR_PI, subTags));

		if (translatedIRODSQuery.getTranslatedQueryConditions().size() > 0) {

			// package the conditions

			subTags = new Tag[translatedIRODSQuery
					.getTranslatedQueryConditions().size() * 2 + 1];
			subTags[0] = new Tag(ISLEN, translatedIRODSQuery
					.getTranslatedQueryConditions().size());
			j = 1;
			for (TranslatedGenQueryCondition queryCondition : translatedIRODSQuery
					.getTranslatedQueryConditions()) {
				subTags[j] = new Tag(INX,
						queryCondition.getColumnNumericTranslation());
				j++;
			}
			for (TranslatedGenQueryCondition queryCondition : translatedIRODSQuery
					.getTranslatedQueryConditions()) {
				// New for loop because they have to be in a certain order...
				subTags[j] = new Tag(SVALUE, " " + queryCondition.getOperator()
						+ " " + queryCondition.getValue() + " ");
				j++;
			}
			message.addTag(new Tag(INX_VAL_PAIR_PI, subTags));
		} else {
			// need this tag, just create a blank one
			message.addTag(new Tag(INX_VAL_PAIR_PI, new Tag(ISLEN, 0)));
		}

		return message;
	}

}
