/**
 * 
 */
package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents an iRODS general query as specified using the
 * <code>IRODSGenQueryBuilder</code> tool.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSGenQueryFromBuilder extends AbstractIRODSGenQuery {

	private final IRODSGenQueryBuilderQueryData irodsGenQueryBuilderData;

	/**
	 * Constructor creates a query that can be processed against iRODS.
	 * 
	 * @param irodsGenQueryBuilderData
	 *            {@link IRODSGenQueryBuilderQueryData} that contains the actual
	 *            query data
	 * @param numberOfResultsDesired
	 *            <code>int</code> with the number of results desired from the
	 *            query
	 */
	private IRODSGenQueryFromBuilder(
			final IRODSGenQueryBuilderQueryData irodsGenQueryBuilderData,
			final int numberOfResultsDesired) {
		super(numberOfResultsDesired);

		if (irodsGenQueryBuilderData == null) {
			throw new IllegalArgumentException(
					"irodsGenQueryBuilderData is null");
		}

		if (!irodsGenQueryBuilderData.isQueryValid()) {
			throw new IllegalArgumentException("query is not valid to process");
		}

		this.irodsGenQueryBuilderData = irodsGenQueryBuilderData;

	}

	/**
	 * @return the irodsGenQueryBuilderData
	 */
	public IRODSGenQueryBuilderQueryData getIrodsGenQueryBuilderData() {
		return irodsGenQueryBuilderData;
	}

	/**
	 * Format the query in a format understandable by the mechanism that
	 * translates the query to iRODS protocol and sends to iRODS.
	 * 
	 * @return {@link TranslatedIRODSGenQuery} in a format ready to send to
	 *         iRODS
	 * @throws GenQueryBuilderException
	 */
	public TranslatedIRODSGenQuery convertToTranslatedIRODSGenQuery()
			throws GenQueryBuilderException {
		if (!irodsGenQueryBuilderData.isQueryValid()) {
			throw new GenQueryBuilderException("Query is not valid");
		}

		List<TranslatedGenQueryCondition> conditions = new ArrayList<TranslatedGenQueryCondition>();

		for (GenQueryBuilderCondition builderCondition : irodsGenQueryBuilderData
				.getConditions()) {
			try {
				conditions.add(TranslatedGenQueryCondition
						.instanceWithFieldNameAndNumericTranslation(
								builderCondition.getSelectFieldColumnName(),
								builderCondition.getOperator()
										.getOperatorAsString(),
								builderCondition.getValue(), builderCondition
										.getSelectFieldNumericTranslation()));
			} catch (JargonQueryException e) {
				throw new GenQueryBuilderException(
						"error building translated query", e);
			}
		}

		try {
			return TranslatedIRODSGenQuery.instance(
					irodsGenQueryBuilderData.getSelectFields(), conditions,
					irodsGenQueryBuilderData.getOrderByFields(), this,
					irodsGenQueryBuilderData.isDistinct());
		} catch (JargonException e) {
			throw new GenQueryBuilderException(
					"exception building a translated query from this builder query",
					e);
		}

	}

}
