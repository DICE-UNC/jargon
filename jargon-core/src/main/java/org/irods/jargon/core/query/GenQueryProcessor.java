/**
 *
 */
package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSMidLevelProtocol;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.GenQueryInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.IRODSGenQueryExecutorImpl.QueryCloseBehavior;
import org.irods.jargon.core.utils.IRODSConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles lower-level processing of GenQuery
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class GenQueryProcessor {
	private final IRODSMidLevelProtocol irodsCommands;
	private static final Logger log = LogManager.getLogger(GenQueryProcessor.class);

	/**
	 * @param irodsCommands {@link IRODSMidLevelProtocol}
	 */
	public GenQueryProcessor(final IRODSMidLevelProtocol irodsCommands) {
		if (irodsCommands == null) {
			throw new IllegalArgumentException("null irodsCommands");
		}
		this.irodsCommands = irodsCommands;

	}

	/**
	 * Take a result set from a previous query and do a paging operation. This
	 * result set contains information on the original query, and the state of the
	 * original query (more results, etc).
	 *
	 * @param translatedIRODSQuery {@link TranslatedIRODSGenQuery} to be run
	 * @param continueIndex        {@code int} if this is a continuation of a
	 *                             non-closed result
	 * @param partialStartIndex    {@code int} with an offset
	 * @param queryCloseBehavior   {@link QueryCloseBehavior} describing desired
	 *                             behavior upon completion of this query
	 * @param zoneName             {@code String} ({@code null} or blank if not
	 *                             used) that indicates an optional zone for the
	 *                             query
	 * @return {@link IRODSQueryResultSet}
	 * @throws JargonException {@link JargonException}
	 */
	public IRODSQueryResultSet executeTranslatedIRODSQuery(final TranslatedIRODSGenQuery translatedIRODSQuery,
			final int continueIndex, final int partialStartIndex, final QueryCloseBehavior queryCloseBehavior,
			final String zoneName) throws JargonException {

		if (continueIndex < 0) {
			throw new JargonException("continue index must be >= 0");
		}

		if (partialStartIndex < 0) {
			throw new JargonException("partial start index cannot be less than zero");
		}

		GenQueryInp genQueryInp;

		if (partialStartIndex == 0) {
			genQueryInp = GenQueryInp.instance(translatedIRODSQuery, continueIndex, zoneName);
		} else {
			genQueryInp = GenQueryInp.instanceWithPartialStart(translatedIRODSQuery, partialStartIndex, zoneName);
		}

		Tag response = null;
		List<IRODSQueryResultRow> result = null;
		IRODSQueryResultSet resultSet = null;
		try {
			response = sendGenQueryAndReturnResponse(genQueryInp);

			int continuation = QueryResultProcessingUtils.getContinuationValue(response);

			log.info("continuation value: {}", continuation);

			// get a list of the column names
			List<String> columnNames = new ArrayList<String>();

			for (GenQuerySelectField selectField : translatedIRODSQuery.getSelectFields()) {
				columnNames.add(selectField.getSelectFieldColumnName());
			}

			int totalRecords = response.getTag("totalRowCount").getIntValue();
			log.info("total records:{}", totalRecords);

			result = QueryResultProcessingUtils.translateResponseIntoResultSet(response, columnNames, continuation,
					partialStartIndex);

			resultSet = IRODSQueryResultSet.instance(translatedIRODSQuery, result, continuation, totalRecords);

			if (resultSet.isHasMoreRecords() && queryCloseBehavior == QueryCloseBehavior.AUTO_CLOSE) {
				log.info("auto closing result set");
				closeResults(resultSet);
			}

			return resultSet;
		} catch (DataNotFoundException dnf) {
			log.info("response from IRODS call indicates no rows found");
			result = new ArrayList<IRODSQueryResultRow>();
			resultSet = IRODSQueryResultSet.instance(translatedIRODSQuery, result, 0, 0);
			return resultSet;
		} finally {
			if (resultSet != null // && resultSet.isHasMoreRecords()
					&& queryCloseBehavior == QueryCloseBehavior.AUTO_CLOSE) {
				log.info("auto closing result set");
				closeResults(resultSet);
			}
		}
	}

	/**
	 * Send the query
	 *
	 * @param genQueryInp {@link GenQueryInp} with the packing instruction
	 * @return {@link Tag} with the return result
	 * @throws JargonException for iRODS error
	 */
	public Tag sendGenQueryAndReturnResponse(final GenQueryInp genQueryInp) throws JargonException {

		Tag response = irodsCommands.irodsFunction(IRODSConstants.RODS_API_REQ, genQueryInp.getParsedTags(),
				GenQueryInp.API_NBR);

		return response;
	}

	/**
	 * send the notification to iRODS to close the query result set.
	 *
	 * @param irodsQueryResultSet {@link IRODSQueryResultSet} to close
	 * @throws JargonException for iRODS error
	 */
	public void closeResults(final IRODSQueryResultSet irodsQueryResultSet) throws JargonException {

		log.info("closeResults()");
		if (irodsQueryResultSet == null) {
			throw new JargonException("null irodsQueryResultSet");
		}

		if (!irodsQueryResultSet.isHasMoreRecords()) {
			log.info("no results to close, ignore");
			return;
		}

		GenQueryInp genQueryInp = GenQueryInp.instanceForCloseQuery(irodsQueryResultSet.getTranslatedIRODSQuery(),
				irodsQueryResultSet.getContinuationIndex());
		sendGenQueryAndReturnResponse(genQueryInp);

	}

	/**
	 * translate the given query
	 *
	 * @param irodsQuery {@link AbstractIRODSGenQuery}
	 * @return {@link TranslatedIRODSGenQuery} translated into runnable form
	 * @throws JargonException      for iRODS error
	 * @throws JargonQueryException if the query is malformed
	 */
	public TranslatedIRODSGenQuery translateProvidedQuery(final AbstractIRODSGenQuery irodsQuery)
			throws JargonException, JargonQueryException {
		TranslatedIRODSGenQuery translatedIRODSQuery = null;

		if (irodsQuery instanceof IRODSGenQuery) {
			IRODSGenQueryTranslator irodsQueryTranslator = new IRODSGenQueryTranslator(
					irodsCommands.getIRODSServerProperties());
			translatedIRODSQuery = irodsQueryTranslator.getTranslatedQuery((IRODSGenQuery) irodsQuery);

		} else if (irodsQuery instanceof IRODSGenQueryFromBuilder) {
			try {
				translatedIRODSQuery = ((IRODSGenQueryFromBuilder) irodsQuery).convertToTranslatedIRODSGenQuery();
			} catch (GenQueryBuilderException e) {
				throw new JargonException("invalid builder query", e);
			}
		} else {
			throw new JargonException("unknown type of irodsGenQuery");
		}
		return translatedIRODSQuery;
	}

}
