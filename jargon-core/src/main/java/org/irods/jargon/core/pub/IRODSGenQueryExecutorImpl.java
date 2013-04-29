package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.AbstractIRODSGenQuery;
import org.irods.jargon.core.query.GenQueryProcessor;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;
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

		GenQueryProcessor genQueryProcessor = new GenQueryProcessor(
				getIRODSProtocol());

		TranslatedIRODSGenQuery translatedIRODSQuery = genQueryProcessor
				.translateProvidedQuery(irodsQuery);

		return genQueryProcessor.executeTranslatedIRODSQuery(
				translatedIRODSQuery, continueIndex, 0,
				QueryCloseBehavior.MANUAL_CLOSE, zoneName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSGenQueryExecutor#
	 * executeIRODSQueryAndCloseResult
	 * (org.irods.jargon.core.query.IRODSGenQuery, int)
	 */
	@Override
	public IRODSQueryResultSet executeIRODSQueryAndCloseResult(
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
	public IRODSQueryResultSet executeIRODSQueryAndCloseResultInZone(
			final AbstractIRODSGenQuery irodsQuery,
			final int partialStartIndex, final String zoneName)
			throws JargonException, JargonQueryException {

		log.info("executeIRODSQueryAndCloseResultInZone()");

		if (irodsQuery == null) {
			throw new IllegalArgumentException("null irodsQuery");
		}

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException("continueIndex is < 0");
		}

		log.info("query: {}", irodsQuery);

		GenQueryProcessor genQueryProcessor = new GenQueryProcessor(
				getIRODSProtocol());

		TranslatedIRODSGenQuery translatedIRODSQuery = genQueryProcessor
				.translateProvidedQuery(irodsQuery);

		return genQueryProcessor.executeTranslatedIRODSQuery(
				translatedIRODSQuery, 0, partialStartIndex,
				QueryCloseBehavior.AUTO_CLOSE, zoneName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSGenQueryExecutor#executeIRODSQueryWithPaging
	 * (org.irods.jargon.core.query.IRODSQuery, int)
	 */
	@Override
	public IRODSQueryResultSet executeIRODSQueryWithPaging(
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
	public IRODSQueryResultSet executeIRODSQueryWithPagingInZone(
			final AbstractIRODSGenQuery irodsQuery,
			final int partialStartIndex, final String zoneName)
			throws JargonException, JargonQueryException {

		log.info("executeIRODSQueryWithPagingInZone()");

		if (irodsQuery == null) {
			throw new IllegalArgumentException("null irodsQuery");
		}

		log.info("query: {}", irodsQuery);
		GenQueryProcessor genQueryProcessor = new GenQueryProcessor(
				getIRODSProtocol());
		TranslatedIRODSGenQuery translatedIRODSQuery = genQueryProcessor
				.translateProvidedQuery(irodsQuery);

		return genQueryProcessor.executeTranslatedIRODSQuery(
				translatedIRODSQuery, 0, partialStartIndex,
				QueryCloseBehavior.AUTO_CLOSE, zoneName);
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

		GenQueryProcessor genQueryProcessor = new GenQueryProcessor(
				getIRODSProtocol());

		return genQueryProcessor.executeTranslatedIRODSQuery(
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

		GenQueryProcessor genQueryProcessor = new GenQueryProcessor(
				getIRODSProtocol());

		return genQueryProcessor.executeTranslatedIRODSQuery(
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
	public void closeResults(final IRODSQueryResultSet irodsQueryResultSet)
			throws JargonException {

		log.info("closeResults()");
		GenQueryProcessor genQueryProcessor = new GenQueryProcessor(
				getIRODSProtocol());
		genQueryProcessor.closeResults(irodsQueryResultSet);
	}

}
