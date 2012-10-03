package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.GenQueryField;
import org.irods.jargon.core.query.GenQueryOrderByField;
import org.irods.jargon.core.query.GenQueryOrderByField.OrderByType;
import org.irods.jargon.core.query.GenQuerySelectField;
import org.irods.jargon.core.query.TranslatedGenQueryCondition;
import org.irods.jargon.core.query.TranslatedIRODSGenQuery;
import org.irods.jargon.core.utils.IRODSConstants;

/**
 * Wrap a query to IRODS, note that the only shared object is
 * <code>IRODSQuery</code> which is immutable, so this class should be
 * thread-safe.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class GenQueryInp extends AbstractIRODSPackingInstruction implements
		IRodsPI {

	private final TranslatedIRODSGenQuery translatedIRODSQuery;
	private final int continueIndex;
	private final int partialStartIndex;
	private final int maxRowCount;
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

	public static final int ORDER_BY = 1024;// 0x400;
	public static final int ORDER_BY_DESC = 2048;// 0x800;
	public static final int UPPER_CASE_WHERE = 512; // 0x200;

	public static final int API_NBR = 702;

	private String zoneName = null;

	/**
	 * Return an instance of a query command that defaults to no partial start.
	 * 
	 * @param translatedIRODSQuery
	 * @param continueIndex
	 *            <code>int</code> with a 0 or 1 to indicate continuation of a
	 *            previous query that had more results
	 * @param zoneName
	 *            <code>String</code> (<code>null</code> or blank if not used)
	 *            that indicates an optional zone for the query
	 * @return <code>GenQueryInp</code> instance
	 * @throws JargonException
	 */
	public static GenQueryInp instance(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final int continueIndex, final String zoneName)
			throws JargonException {
		return new GenQueryInp(translatedIRODSQuery, continueIndex, 0, zoneName);
	}

	/**
	 * Static instance method for version of the packing instruction to close
	 * the query down.
	 * 
	 * @param continueIndex
	 *            <code>int</code> with value passed back from iRODS with the
	 *            last query result.
	 * @return
	 */
	public static GenQueryInp instanceForCloseQuery(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final int continueIndex) {
		return new GenQueryInp(translatedIRODSQuery, continueIndex);
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
			final int partialStartIndex, final String zoneName)
			throws JargonException {
		return new GenQueryInp(translatedIRODSQuery, 0, partialStartIndex,
				zoneName);
	}

	/**
	 * Special private constructor builds the packing instruction when this is a
	 * close of a result set that had been continued.
	 * 
	 * @param continueIndex
	 *            <code>int</code> with value passed back from iRODS with the
	 *            last query result.
	 */
	private GenQueryInp(final TranslatedIRODSGenQuery translatedIRODSGenQuery,
			final int continueIndex) {

		if (translatedIRODSGenQuery == null) {
			throw new IllegalArgumentException(
					"translatedIRODSGenQuery is null");
		}

		if (continueIndex <= 0) {
			throw new IllegalArgumentException(
					"continueIndex must be > 0 when sending a close");
		}

		this.translatedIRODSQuery = translatedIRODSGenQuery;
		this.continueIndex = continueIndex;
		this.partialStartIndex = 0;
		this.maxRowCount = -1;
	}

	private GenQueryInp(final TranslatedIRODSGenQuery translatedIRODSQuery,
			final int continueIndex, final int partialStartIndex,
			final String zoneName) throws JargonException {

		if (translatedIRODSQuery == null) {
			throw new IllegalArgumentException("irodsQuery is null");
		}

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException(
					"partialStartIndex is less than 0");
		}

		if (continueIndex < 0) {
			throw new IllegalArgumentException(
					"continue Index must be 0 or greater");
		}

		this.translatedIRODSQuery = translatedIRODSQuery;
		this.continueIndex = continueIndex;
		this.partialStartIndex = partialStartIndex;
		this.maxRowCount = translatedIRODSQuery.getIrodsQuery()
				.getNumberOfResultsDesired();
		this.setApiNumber(API_NBR);
		this.zoneName = zoneName;
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
			throw new IllegalArgumentException("no translated query");
		}
		return translatedIRODSQuery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue
	 * ()
	 */
	@Override
	public Tag getTagValue() throws JargonException {
		Tag message = new Tag(PI_TAG, new Tag[] {
				new Tag(MAX_ROWS, maxRowCount),
				new Tag(CONTINUE_INX, continueIndex), // new query
				new Tag(PARTIAL_START_INDEX, partialStartIndex) });

		int optionVal = 0;

		if (!this.getTranslatedIRODSQuery().isDistinct()) {
			optionVal += 1;
		}

		if (this.getTranslatedIRODSQuery().isUpperCase()) {
			optionVal += UPPER_CASE_WHERE;
		}

		message.addTag(new Tag(IRODSConstants.options, optionVal));

		/*
		 * If a zoneName is specified, this means the query is for another
		 * federated zone to process, add a keyword to the packing instruction
		 * to process the query.
		 */

		if (zoneName == null || zoneName.isEmpty()) {
			message.addTag(Tag.createKeyValueTag(null));
		} else {
			message.addTag(Tag.createKeyValueTag("zone", zoneName));
		}

		Tag[] subTags = null;
		int[] orderByFlags = new int[translatedIRODSQuery.getSelectFields()
				.size()];
		int j = 1;
		int k = 0;

		subTags = new Tag[translatedIRODSQuery.getSelectFields().size() * 2 + 1];
		subTags[0] = new Tag(IILEN, translatedIRODSQuery.getSelectFields()
				.size());

		for (GenQuerySelectField select : translatedIRODSQuery
				.getSelectFields()) {
			subTags[j] = new Tag(INX, select.getSelectFieldNumericTranslation());

			// see if there is a matching order by field and twiddle the bits

			int orderByFlag = 0;
			for (GenQueryOrderByField orderBy : translatedIRODSQuery
					.getOrderByFields()) {
				if (orderBy.getSelectFieldNumericTranslation().equals(
						select.getSelectFieldNumericTranslation())) {
					if (orderBy.getOrderByType() == OrderByType.ASC) {
						orderByFlag = ORDER_BY;
					} else if (orderBy.getOrderByType() == OrderByType.DESC) {
						orderByFlag = ORDER_BY_DESC;
					}
					break;
				}

			}
			orderByFlags[k++] = orderByFlag;
			j++;
		}

		k = 0;
		for (GenQuerySelectField select : translatedIRODSQuery
				.getSelectFields()) {
			int val = 1;
			if (select.getSelectFieldType() == GenQueryField.SelectFieldTypes.FIELD) {
				val = orderByFlags[k++];
				if (val == 0) {
					val = 1;
				}
			} else if (select.getSelectFieldType() == GenQueryField.SelectFieldTypes.AVG) {
				val = 5;
			} else if (select.getSelectFieldType() == GenQueryField.SelectFieldTypes.COUNT) {
				val = 6;
			} else if (select.getSelectFieldType() == GenQueryField.SelectFieldTypes.MAX) {
				val = 3;
			} else if (select.getSelectFieldType() == GenQueryField.SelectFieldTypes.MIN) {
				val = 2;
			} else if (select.getSelectFieldType() == GenQueryField.SelectFieldTypes.SUM) {
				val = 4;
			} else if (select.getSelectFieldType() == GenQueryField.SelectFieldTypes.FILE_ACCESS) {
				val = 1024;
			} else {
				throw new JargonException(
						"unknown select type, cannot translate to XML protocol:"
								+ select.getSelectFieldType());
			}

			subTags[j] = new Tag(IVALUE, val);
			j++;
		}

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
