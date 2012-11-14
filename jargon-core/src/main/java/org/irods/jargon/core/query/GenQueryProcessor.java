/**
 * 
 */
package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.GenQueryInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.IRODSGenQueryExecutorImpl.QueryCloseBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles lower-level processing of GenQuery
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class GenQueryProcessor {
	private final IRODSCommands irodsCommands;
	private static final Logger log = LoggerFactory
			.getLogger(GenQueryProcessor.class);

	/**
	 * 
	 */
	public GenQueryProcessor(final IRODSCommands irodsCommands) {
		if (irodsCommands == null) {
			throw new IllegalArgumentException("null irodsCommands");
		}
		this.irodsCommands = irodsCommands;

	}

	/**
	 * Take a result set from a previous query and do a paging operation. This
	 * result set contains information on the original query, and the state of
	 * the original query (more results, etc).
	 * 
	 * @param translatedIRODSQuery
	 * @param continueIndex
	 * @param partialStartIndex
	 * @param queryCloseBehavior
	 * @param zoneName
	 *            <code>String</code> (<code>null</code> or blank if not used)
	 *            that indicates an optional zone for the query
	 * @return {@link IRODSQueryResultSet}
	 * @throws JargonException
	 */
	public IRODSQueryResultSet executeTranslatedIRODSQuery(
			final TranslatedIRODSGenQuery translatedIRODSQuery,
			final int continueIndex, final int partialStartIndex,
			final QueryCloseBehavior queryCloseBehavior, final String zoneName)
			throws JargonException {

		if (continueIndex < 0) {
			throw new JargonException("continue index must be >= 0");
		}

		if (partialStartIndex < 0) {
			throw new JargonException(
					"partial start index cannot be less than zero");
		}

		GenQueryInp genQueryInp;

		if (partialStartIndex == 0) {
			genQueryInp = GenQueryInp.instance(translatedIRODSQuery,
					continueIndex, zoneName);
		} else {
			genQueryInp = GenQueryInp.instanceWithPartialStart(
					translatedIRODSQuery, partialStartIndex, zoneName);
		}

		Tag response = null;
		List<IRODSQueryResultRow> result = null;
		IRODSQueryResultSet resultSet = null;
		try {
			response = sendGenQueryAndReturnResponse(genQueryInp);

			int continuation = QueryResultProcessingUtils
					.getContinuationValue(response);

			log.info("continuation value: {}", continuation);

			// get a list of the column names
			List<String> columnNames = new ArrayList<String>();

			for (GenQuerySelectField selectField : translatedIRODSQuery
					.getSelectFields()) {
				columnNames.add(selectField.getSelectFieldColumnName());
			}

			result = QueryResultProcessingUtils.translateResponseIntoResultSet(
					response, columnNames, continuation, partialStartIndex);

			int totalRecords = response.getTag("totalRowCount").getIntValue();
			log.info("total records:{}", totalRecords);

			resultSet = IRODSQueryResultSet.instance(translatedIRODSQuery,
					result, continuation, totalRecords);

			if (resultSet.isHasMoreRecords()
					&& queryCloseBehavior == QueryCloseBehavior.AUTO_CLOSE) {
				log.info("auto closing result set");
				this.closeResults(resultSet);
			}

			return resultSet;
		} catch (DataNotFoundException dnf) {
			log.info("response from IRODS call indicates no rows found");
			result = new ArrayList<IRODSQueryResultRow>();
			resultSet = IRODSQueryResultSet.instance(translatedIRODSQuery,
					result, 0, 0);
			return resultSet;
		} finally {
			if (resultSet != null // && resultSet.isHasMoreRecords()
					&& queryCloseBehavior == QueryCloseBehavior.AUTO_CLOSE) {
				log.info("auto closing result set");
				this.closeResults(resultSet);
			}
		}
	}

	/**
	 * @param translatedIRODSQuery
	 * @param continueIndex
	 * @param partialStartIndex
	 * @return
	 * @throws JargonException
	 */
	public Tag sendGenQueryAndReturnResponse(final GenQueryInp genQueryInp)
			throws JargonException, DataNotFoundException {

		Tag response = irodsCommands.irodsFunction(GenQueryInp.PI_TAG,
				genQueryInp.getParsedTags(), GenQueryInp.API_NBR);

		return response;
	}

	/**
	 * send the notification to iRODS to close the query result set.
	 * 
	 * @param irodsQueryResultSet
	 * @throws JargonException
	 */
	public void closeResults(
			final IRODSQueryResultSetInterface irodsQueryResultSet)
			throws JargonException {

		log.info("getting more results for query");
		if (irodsQueryResultSet == null) {
			throw new JargonException("null irodsQueryResultSet");
		}

		if (!irodsQueryResultSet.isHasMoreRecords()) {
			log.info("no results to close, ignore");
			return;
		}

		IRODSQueryResultSet genQueryResult = (IRODSQueryResultSet) irodsQueryResultSet;
		GenQueryInp genQueryInp = GenQueryInp.instanceForCloseQuery(
				genQueryResult.getTranslatedIRODSQuery(),
				genQueryResult.getContinuationIndex());
		sendGenQueryAndReturnResponse(genQueryInp);

	}

	/**
	 * @param irodsQuery
	 * @return
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	public TranslatedIRODSGenQuery translateProvidedQuery(
			final AbstractIRODSGenQuery irodsQuery) throws JargonException,
			JargonQueryException {
		TranslatedIRODSGenQuery translatedIRODSQuery = null;

		if (irodsQuery instanceof IRODSGenQuery) {
			IRODSGenQueryTranslator irodsQueryTranslator = new IRODSGenQueryTranslator(
					this.irodsCommands.getIRODSServerProperties());
			translatedIRODSQuery = irodsQueryTranslator
					.getTranslatedQuery((IRODSGenQuery) irodsQuery);

		} else if (irodsQuery instanceof IRODSGenQueryFromBuilder) {
			try {
				translatedIRODSQuery = ((IRODSGenQueryFromBuilder) irodsQuery)
						.convertToTranslatedIRODSGenQuery();
			} catch (GenQueryBuilderException e) {
				throw new JargonException("invalid builder query", e);
			}
		} else {
			throw new JargonException("unknown type of irodsGenQuery");
		}
		return translatedIRODSQuery;
	}

}
