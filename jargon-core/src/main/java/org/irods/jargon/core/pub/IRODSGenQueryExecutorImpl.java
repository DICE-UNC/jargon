package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.GenQueryInp;
import org.irods.jargon.core.packinstr.GenQueryOut;
import org.irods.jargon.core.query.IRODSQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryTranslator;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.TranslatedIRODSQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sdsc.grid.io.irods.Tag;

/**
 * Implementation class that can process iquest-like queries using the genquery
 * facility of iRODS.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class IRODSGenQueryExecutorImpl extends IRODSGenericAO implements
		IRODSGenQueryExecutor {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Constructor for implementation class, called by
	 * {@link org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl}, this is
	 * not meant to be created directly by API users. The visibility of this
	 * constructor is public so it may be invoked by
	 * <code>org.irods.jargon.core.pub.io</code> classes. Those classes may
	 * later be converted to create this object via factory (TODO: have io
	 * classes use factory)
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
	public IRODSQueryResultSet executeIRODSQuery(final IRODSQuery irodsQuery,
			final int continueIndex) throws JargonException,
			JargonQueryException {
		log.info("executing irods query: {}", irodsQuery.getQueryString());
		IRODSQueryTranslator irodsQueryTranslator = new IRODSQueryTranslator(
				getIRODSServerProperties());
		TranslatedIRODSQuery translatedIRODSQuery = irodsQueryTranslator
				.getTranslatedQuery(irodsQuery);
		return executeTranslatedIRODSQuery(translatedIRODSQuery, 0, 0);
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
			final IRODSQuery irodsQuery, final int partialStartIndex)
			throws JargonException, JargonQueryException {

		log.info("executing irods query: {}", irodsQuery.getQueryString());
		IRODSQueryTranslator irodsQueryTranslator = new IRODSQueryTranslator(
				getIRODSServerProperties());
		TranslatedIRODSQuery translatedIRODSQuery = irodsQueryTranslator
				.getTranslatedQuery(irodsQuery);

		return executeTranslatedIRODSQuery(translatedIRODSQuery, 0,
				partialStartIndex);
	}

	/**
	 * Take a result set from a previous query and do a paging operation. This
	 * result set contains information on the original query, and the state of
	 * the original query (more results, etc).
	 * 
	 * @param translatedIRODSQuery
	 * @param continueIndex
	 * @param partialStartIndex
	 * @return
	 * @throws JargonException
	 */
	private IRODSQueryResultSet executeTranslatedIRODSQuery(
			final TranslatedIRODSQuery translatedIRODSQuery,
			final int continueIndex, final int partialStartIndex)
			throws JargonException {

		if (!(continueIndex == 1 || continueIndex == 0)) {
			throw new JargonException("continue index must be 0 or 1");
		}

		if (partialStartIndex < 0) {
			throw new JargonException(
					"partial start index cannot be less than zero");
		}

		GenQueryInp genQueryInp;

		if (partialStartIndex == 0) {
			genQueryInp = GenQueryInp.instance(translatedIRODSQuery,
					continueIndex);
		} else {
			genQueryInp = GenQueryInp.instanceWithPartialStart(
					translatedIRODSQuery, partialStartIndex);
		}

		Tag response = getIRODSProtocol().irodsFunction(GenQueryInp.PI_TAG,
				genQueryInp.getParsedTags(), GenQueryInp.API_NBR);

		if (response == null) {
			log.info("null response from IRODS call indicates no rows found, original query was:"
					+ translatedIRODSQuery);
			List<IRODSQueryResultRow> result = translateResponseIntoResultSet(
					response, translatedIRODSQuery, 0);
			IRODSQueryResultSet resultSet = IRODSQueryResultSet.instance(
					translatedIRODSQuery, result, 0);
			return resultSet;
		}

		if (log.isDebugEnabled()) {
			log.debug("query reult for query:{}", translatedIRODSQuery);
		}

		int continuation = response.getTag(GenQueryOut.CONTINUE_INX)
				.getIntValue();

		log.info(">>>> continuation value: {}", continuation);

		List<IRODSQueryResultRow> result = translateResponseIntoResultSet(
				response, translatedIRODSQuery, continuation);

		IRODSQueryResultSet resultSet = IRODSQueryResultSet.instance(
				translatedIRODSQuery, result, continuation);

		return resultSet;

	}

	/**
	 * Given the raw response from iRODS, translate into a list of result rows
	 * for easier processing.
	 * 
	 * @param queryResponse
	 *            <code>Tag</code> set with the raw GenQuery response from
	 *            iRODS.
	 * @param translatedIRODSQuery
	 *            {@link org.irods.jargon.core.query.TranslatedIRODSQuery} that
	 *            has information about the query used to obtain the given
	 *            response
	 * @param continuation
	 *            <code>int</code>
	 * @return <code>List</code> of
	 *         {@link org.irods.jargon.core.query.IRODSQueryResultRow} for each
	 *         row in the GenQuery result
	 * @throws JargonException
	 */
	private List<IRODSQueryResultRow> translateResponseIntoResultSet(
			final Tag queryResponse,
			final TranslatedIRODSQuery translatedIRODSQuery,
			final int continuation) throws JargonException {

		List<IRODSQueryResultRow> resultSet = new ArrayList<IRODSQueryResultRow>();
		List<String> row = new ArrayList<String>();
		int recordCount = 1;
		boolean lastRecord = (continuation == 0);
		log.debug("do I have more? {}", lastRecord);

		if (queryResponse == null) {
			// no response, create an empty result set, and never return null
			log.info("empty result set from query, returning as an empty result set ( no rows found)");
			return resultSet;
		}

		int rows = queryResponse.getTag(GenQueryOut.ROW_CNT).getIntValue();
		log.info("rows returned from iRODS for GenQuery: {}", rows);

		int attributes = queryResponse.getTag(GenQueryOut.ATTRIB_CNT)
				.getIntValue();

		for (int i = 0; i < rows; i++) {
			// new row
			row = new ArrayList<String>();
			for (int j = 0; j < attributes; j++) {

				row.add(queryResponse.getTags()[4 + j].getTags()[2 + i]
						.getStringValue());
			}

			resultSet.add(IRODSQueryResultRow.instance(row,
					translatedIRODSQuery, recordCount++, lastRecord));

		}

		return resultSet;

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
				irodsQueryResultSet.getTranslatedIRODSQuery(), 1, 0);
	}

}
