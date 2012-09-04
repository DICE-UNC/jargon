package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.GenQueryInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.query.AbstractIRODSGenQuery;
import org.irods.jargon.core.query.AbstractIRODSQueryResultSet;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQuerySelectField;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSGenQueryTranslator;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryResultProcessingUtils;
import org.irods.jargon.core.query.TranslatedIRODSGenQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation class that can process iquest-like queries using the genquery
 * facility of iRODS.
 * <p/>
 * Note that this implementation provides the ability to close query results
 * held in iRODS, or, alternately, to leave the results open so that the next
 * set of results may be retrieved. The former mode is more suitable for
 * session-per-request operations, such as mid-tier web applications. The later
 * is suitable for persistently connected clients.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class IRODSGenQueryExecutorImpl extends IRODSGenericAO implements
		IRODSGenQueryExecutor {

	private static final Logger log = LoggerFactory
			.getLogger(IRODSGenQueryExecutorImpl.class);

	/**
	 * enum describes how to handle closing of a query
	 */
	public enum QueryCloseBehavior {
		AUTO_CLOSE, MANUAL_CLOSE
	}

	/**
	 * Constructor for implementation class, called by
	 * {@link org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl}, this is
	 * not meant to be created directly by API users. The visibility of this
	 * constructor is public so it may be invoked by
	 * <code>org.irods.jargon.core.pub.io</code> classes. Those classes may
	 * later be converted to create this object via factory
	 * 
	 * @param irodsSession
	 *            {@link org.irods.jargon.core.connection.IRODSSession}
	 *            implementation that contains connections to iRODS.
	 * @param irodsAccount
	 *            {@link org.irods.jargon.core.connection.IRODSAccount} that
	 *            contains connection information.
	 * @throws JargonException
	 */
	public IRODSGenQueryExecutorImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSGenQueryExecutor#executeIRODSQuery(org
	 * .irods.jargon.core.query.IRODSQuery, int)
	 */
	@Override
	public IRODSQueryResultSet executeIRODSQuery(
			final AbstractIRODSGenQuery irodsQuery, final int continueIndex)
			throws JargonException, JargonQueryException {
		log.info("executeIRODSQuery()");

		return executeIRODSQueryInZone(irodsQuery, continueIndex, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSGenQueryExecutor#executeIRODSQueryInZone
	 * (org.irods.jargon.core.query.IRODSGenQuery, int, java.lang.String)
	 */
	@Override
	public IRODSQueryResultSet executeIRODSQueryInZone(
			final AbstractIRODSGenQuery irodsQuery, final int continueIndex,
			final String zoneName) throws JargonException, JargonQueryException {

		log.info("executeIRODSQueryInZone()");

		if (irodsQuery == null) {
			throw new IllegalArgumentException("null irodsQuery");
		}

		if (continueIndex < 0) {
			throw new IllegalArgumentException("continue index must be > 0");
		}

		log.info("query: {}", irodsQuery);

		
		TranslatedIRODSGenQuery translatedIRODSQuery = translateProvidedQuery(irodsQuery);

		return executeTranslatedIRODSQuery(translatedIRODSQuery, continueIndex,
				0, QueryCloseBehavior.MANUAL_CLOSE, zoneName);
	}

	/**
	 * @param irodsQuery
	 * @return
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	private TranslatedIRODSGenQuery translateProvidedQuery(
			final AbstractIRODSGenQuery irodsQuery) throws JargonException,
			JargonQueryException {
		TranslatedIRODSGenQuery translatedIRODSQuery = null;
		
		if (irodsQuery instanceof IRODSGenQuery) {
			IRODSGenQueryTranslator irodsQueryTranslator = new IRODSGenQueryTranslator(
					getIRODSServerProperties());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSGenQueryExecutor#
	 * executeIRODSQueryAndCloseResult
	 * (org.irods.jargon.core.query.IRODSGenQuery, int)
	 */
	@Override
	public AbstractIRODSQueryResultSet executeIRODSQueryAndCloseResult(
			final AbstractIRODSGenQuery irodsQuery, final int partialStartIndex)
			throws JargonException, JargonQueryException {
		log.info("executeIRODSQueryAndCloseResult()");

		return executeIRODSQueryAndCloseResultInZone(irodsQuery,
				partialStartIndex, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSGenQueryExecutor#
	 * executeIRODSQueryAndCloseResultInZone
	 * (org.irods.jargon.core.query.IRODSGenQuery, int, java.lang.String)
	 */
	@Override
	public AbstractIRODSQueryResultSet executeIRODSQueryAndCloseResultInZone(
			final AbstractIRODSGenQuery irodsQuery,
			final int partialStartIndex,
			final String zoneName) throws JargonException, JargonQueryException {

		log.info("executeIRODSQueryAndCloseResultInZone()");

		if (irodsQuery == null) {
			throw new IllegalArgumentException("null irodsQuery");
		}

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException("continueIndex is < 0");
		}

		log.info("query: {}", irodsQuery);
		TranslatedIRODSGenQuery translatedIRODSQuery = translateProvidedQuery(irodsQuery);

		return executeTranslatedIRODSQuery(translatedIRODSQuery, 0,
				partialStartIndex, QueryCloseBehavior.AUTO_CLOSE, zoneName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSGenQueryExecutor#executeIRODSQueryWithPaging
	 * (org.irods.jargon.core.query.IRODSQuery, int)
	 */
	@Override
	public IRODSQueryResultSetInterface executeIRODSQueryWithPaging(
			final AbstractIRODSGenQuery irodsQuery, final int partialStartIndex)
			throws JargonException, JargonQueryException {

		log.info("executeIRODSQueryWithPaging()");
		return executeIRODSQueryWithPagingInZone(irodsQuery, partialStartIndex,
				null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSGenQueryExecutor#
	 * executeIRODSQueryWithPagingInZone
	 * (org.irods.jargon.core.query.IRODSGenQuery, int, java.lang.String)
	 */
	@Override
	public IRODSQueryResultSetInterface executeIRODSQueryWithPagingInZone(
			final AbstractIRODSGenQuery irodsQuery,
			final int partialStartIndex,
			final String zoneName) throws JargonException, JargonQueryException {

		log.info("executeIRODSQueryWithPagingInZone()");

		if (irodsQuery == null) {
			throw new IllegalArgumentException("null irodsQuery");
		}

		log.info("query: {}", irodsQuery);
		TranslatedIRODSGenQuery translatedIRODSQuery = translateProvidedQuery(irodsQuery);

		return executeTranslatedIRODSQuery(translatedIRODSQuery, 0,
				partialStartIndex, QueryCloseBehavior.AUTO_CLOSE, zoneName);
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
	private IRODSQueryResultSet executeTranslatedIRODSQuery(
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
					response,
 columnNames, continuation,
					partialStartIndex);

			resultSet = IRODSQueryResultSet.instance(translatedIRODSQuery,
					result, continuation);

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
					result, 0);
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
	private Tag sendGenQueryAndReturnResponse(final GenQueryInp genQueryInp)
			throws JargonException, DataNotFoundException {

		Tag response = getIRODSProtocol().irodsFunction(GenQueryInp.PI_TAG,
				genQueryInp.getParsedTags(), GenQueryInp.API_NBR);

		return response;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSGenQueryExecutor#getMoreResults(org.irods
	 * .jargon.core.query.IRODSQueryResultSet)
	 */
	@Override
	public IRODSQueryResultSet getMoreResults(
			final IRODSQueryResultSet irodsQueryResultSet)
			throws JargonException, JargonQueryException {

		log.info("getting more results for query");
		if (irodsQueryResultSet == null) {
			throw new JargonException("null irodsQueryResultSet");
		}

		if (!irodsQueryResultSet.isHasMoreRecords()) {
			throw new JargonQueryException("no more results");
		}

		return executeTranslatedIRODSQuery(
				irodsQueryResultSet.getTranslatedIRODSQuery(),
				irodsQueryResultSet.getContinuationIndex(), 0,
				QueryCloseBehavior.MANUAL_CLOSE, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSGenQueryExecutor#getMoreResultsInZone(
	 * org.irods.jargon.core.query.IRODSQueryResultSet, java.lang.String)
	 */
	@Override
	public IRODSQueryResultSet getMoreResultsInZone(
			final IRODSQueryResultSet irodsQueryResultSet, final String zoneName)
			throws JargonException, JargonQueryException {

		log.info("getting more results for query");
		if (irodsQueryResultSet == null) {
			throw new JargonException("null irodsQueryResultSet");
		}

		if (!irodsQueryResultSet.isHasMoreRecords()) {
			throw new JargonQueryException("no more results");
		}

		return executeTranslatedIRODSQuery(
				irodsQueryResultSet.getTranslatedIRODSQuery(),
				irodsQueryResultSet.getContinuationIndex(), 0,
				QueryCloseBehavior.MANUAL_CLOSE, zoneName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSGenQueryExecutor#closeResults(org.irods
	 * .jargon.core.query.IRODSQueryResultSetInterface)
	 */
	@Override
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

}
