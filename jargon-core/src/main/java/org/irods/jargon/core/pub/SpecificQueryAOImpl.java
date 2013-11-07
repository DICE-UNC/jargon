package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidArgumentException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.SpecificQueryException;
import org.irods.jargon.core.packinstr.GeneralAdminInpForSQ;
import org.irods.jargon.core.packinstr.SpecificQueryInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.domain.SpecificQueryDefinition;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryResultProcessingUtils;
import org.irods.jargon.core.query.SpecificQuery;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.core.utils.Overheaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpecificQueryAOImpl extends IRODSGenericAO implements
		SpecificQueryAO {

	private static final String EXECUTING_SQUERY_PI = "executing specific query PI";
	public static final Logger log = LoggerFactory
			.getLogger(SpecificQueryAOImpl.class);

	protected SpecificQueryAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws SpecificQueryException,
			JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.SpecificQueryAO#listSpecificQueryByAliasLike
	 * (java.lang.String)
	 */
	@Override
	public List<SpecificQueryDefinition> listSpecificQueryByAliasLike(
			final String specificQueryAlias) throws DataNotFoundException,
			JargonException {

		log.info("findSpecificQueryByAliasLike()");

		return listSpecificQueryByAliasLike(specificQueryAlias, "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.SpecificQueryAO#listSpecificQueryByAliasLike
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public List<SpecificQueryDefinition> listSpecificQueryByAliasLike(
			final String specificQueryAlias, final String zoneHint)
			throws DataNotFoundException, JargonException {

		log.info("findSpecificQueryByAliasLike()");

		checkSupportForSpecificQuery();

		if (specificQueryAlias == null || specificQueryAlias.isEmpty()) {
			throw new IllegalArgumentException("null specificQueryAlias");
		}

		if (zoneHint == null) {
			throw new IllegalArgumentException("null zoneHint");
		}

		log.info("alias:{}", specificQueryAlias);
		log.info("zoneHint:{}", zoneHint);

		List<String> arguments = new ArrayList<String>();
		arguments.add(specificQueryAlias);

		SpecificQuery specificQuery = SpecificQuery.instanceArguments(
				"listQueryByAliasLike", arguments, 0, zoneHint);
		SpecificQueryResultSet resultSet;
		try {

			resultSet = this.executeSpecificQueryUsingAliasWithoutAliasLookup(
					specificQuery, this.getJargonProperties()
							.getMaxFilesAndDirsQueryMax(), false);
		} catch (JargonQueryException e) {
			log.error("query exception for specific query:{}", specificQuery, e);
			throw new JargonException(
					"query exception processing specific query", e);
		}

		SpecificQueryDefinition specificQueryDefinition;
		List<SpecificQueryDefinition> specificQueryDefinitions = new ArrayList<SpecificQueryDefinition>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			specificQueryDefinition = new SpecificQueryDefinition();
			specificQueryDefinition.setAlias(row.getColumn(0));
			specificQueryDefinition.setSql(row.getColumn(1));
			specificQueryDefinition.setCount(row.getRecordCount());
			specificQueryDefinition.setLastResult(row.isLastResult());
			specificQueryDefinition
					.setArgumentCount(countArgumentsInQuery(specificQueryDefinition
							.getSql()));
			specificQueryDefinition
					.setColumnNames(parseColumnNamesFromQuery(specificQueryDefinition
							.getSql()));
			specificQueryDefinitions.add(specificQueryDefinition);

		}

		log.info("query definitions:{}", specificQueryDefinitions);
		return specificQueryDefinitions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.SpecificQueryAO#findSpecificQueryByAlias(java
	 * .lang.String)
	 */
	@Override
	public SpecificQueryDefinition findSpecificQueryByAlias(
			final String specificQueryAlias) throws DataNotFoundException,
			JargonException {

		return findSpecificQueryByAlias(specificQueryAlias, "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.SpecificQueryAO#findSpecificQueryByAlias(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public SpecificQueryDefinition findSpecificQueryByAlias(
			final String specificQueryAlias, final String zoneHint)
			throws DataNotFoundException, JargonException {

		log.info("findSpecificQueryByAlias()");

		checkSupportForSpecificQuery();

		if (specificQueryAlias == null || specificQueryAlias.isEmpty()) {
			throw new IllegalArgumentException("null specificQueryAlias");
		}

		if (zoneHint == null) {
			throw new IllegalArgumentException("null zoneHint");
		}

		log.info("alias:{}", specificQueryAlias);
		log.info("zoneHint:{}", zoneHint);

		List<String> arguments = new ArrayList<String>();
		arguments.add(specificQueryAlias);

		SpecificQuery specificQuery = SpecificQuery.instanceArguments(
				"findQueryByAlias", arguments, 0, zoneHint);
		SpecificQueryResultSet resultSet;
		try {

			resultSet = this.executeSpecificQueryUsingAliasWithoutAliasLookup(
					specificQuery, this.getJargonProperties()
							.getMaxFilesAndDirsQueryMax(), false);
		} catch (JargonQueryException e) {
			log.error("query exception for specific query:{}", specificQuery, e);
			throw new JargonException(
					"query exception processing specific query", e);
		}

		IRODSQueryResultRow row = resultSet.getFirstResult();
		SpecificQueryDefinition specificQueryDefinition = new SpecificQueryDefinition();
		specificQueryDefinition.setAlias(row.getColumn(0));
		specificQueryDefinition.setSql(row.getColumn(1));
		specificQueryDefinition.setCount(row.getRecordCount());
		specificQueryDefinition.setLastResult(row.isLastResult());
		specificQueryDefinition
				.setArgumentCount(countArgumentsInQuery(specificQueryDefinition
						.getSql()));
		specificQueryDefinition
				.setColumnNames(parseColumnNamesFromQuery(specificQueryDefinition
						.getSql()));

		log.info("query definition:{}", specificQueryDefinition);
		return specificQueryDefinition;
	}

	/**
	 * Given an sql query, parse out the column names and return as a list
	 * 
	 * @param sql
	 *            <code>String</code> with the sql
	 * @return <code>List<String></code> of column names. These will appear as
	 *         lower case
	 */
	public static List<String> parseColumnNamesFromQuery(final String sql) {
		if (sql == null || sql.isEmpty()) {
			throw new IllegalArgumentException("null or empty sql");
		}

		String lcSql = sql.toLowerCase();
		int posSelect = lcSql.indexOf("select");
		if (posSelect == -1) {
			throw new IllegalArgumentException("no select statement found");
		}

		int posFrom = lcSql.indexOf("from");
		if (posFrom == -1) {
			throw new IllegalArgumentException("no from statement found");
		}

		String colNames = sql.substring(posSelect + 6, posFrom);

		// check for distinct
		posSelect = colNames.indexOf("distinct");
		if (posSelect > -1) {
			// trim off distinct
			colNames = colNames.substring(posSelect + 8);
		}

		String[] colList = colNames.split(",");

		List<String> listToReturn = new ArrayList<String>();
		for (String name : colList) {
			// check for aggregates
			// look for closed paren first because open paren may have been
			// removed
			// if distinct keyword was previously trimmed
			int posCloseParen = name.indexOf(")");
			if (posCloseParen > -1) {
				// trim off parens
				int posOpenParen = colNames.indexOf("(");
				if (posOpenParen > -1) {
					name = name.substring(posOpenParen + 1, posCloseParen);
				} else {
					name = name.substring(0, posCloseParen);
				}

			}

			listToReturn.add(name.trim());
		}

		return listToReturn;
	}

	/**
	 * Given a query string, count the number of arguments expected
	 * 
	 * @param sql
	 *            <code>String</code> with the actual sql used in the specific
	 *            query
	 * @return <code>int</code> with the expected number of arguments
	 */
	public static int countArgumentsInQuery(final String sql) {
		if (sql == null || sql.isEmpty()) {
			throw new IllegalArgumentException("null or empty sql");
		}

		return MiscIRODSUtils.countCharsInString(sql, '?');
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.accessobject.SpecificQueryAO#addSpecificQuery(org
	 * .irods.jargon.core .domain.SpecificQuery)
	 */
	@Override
	public void addSpecificQuery(final SpecificQueryDefinition specificQuery)
			throws JargonException, DuplicateDataException {

		checkSupportForSpecificQuery();

		GeneralAdminInpForSQ queryPI;

		if (specificQuery == null) {
			throw new IllegalArgumentException(
					"cannot create specific query with null SpecificQueryDefinition object");
		}

		log.info("creating specific query: {}", specificQuery);

		queryPI = GeneralAdminInpForSQ
				.instanceForAddSpecificQuery(specificQuery);

		log.info(EXECUTING_SQUERY_PI);

		try {
			getIRODSProtocol().irodsFunction(queryPI);
		} catch (InvalidArgumentException e) {
			log.error("invalid argument exception adding a specific query, see if caused by alias not unique");
			if (e.getMessage().indexOf("Alias is not unique") != -1) {
				throw new DuplicateDataException(e.getMessage());
			} else {
				throw e;
			}
		}

		log.info("added specific query");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.accessobject.SpecificQueryAO#removeSpecificQuery
	 * (org.irods.jargon.core .domain.SpecificQuery)
	 */
	@Override
	public void removeSpecificQuery(final SpecificQueryDefinition specificQuery)
			throws JargonException {

		checkSupportForSpecificQuery();

		GeneralAdminInpForSQ queryPI;

		if (specificQuery == null) {
			throw new IllegalArgumentException(
					"cannot remove specific query with null SpecificQueryDefinition object");
		}

		log.info("removing specific query: {}", specificQuery);
		queryPI = GeneralAdminInpForSQ
				.instanceForRemoveSpecificQuery(specificQuery);
		log.info(EXECUTING_SQUERY_PI);
		getIRODSProtocol().irodsFunction(queryPI);
		log.info("removed specific query");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.accessobject.SpecificQueryAO#removeSpecificQueryByAlias
	 * (String)
	 */
	@Override
	public void removeSpecificQueryByAlias(final String alias)
			throws JargonException, DuplicateDataException {

		checkSupportForSpecificQuery();

		GeneralAdminInpForSQ queryPI;

		if (alias == null) {
			throw new IllegalArgumentException(
					"cannot remove specific query with null alias");
		}

		log.info("removing specific query by alias: {}", alias);
		queryPI = GeneralAdminInpForSQ
				.instanceForRemoveSpecificQueryByAlias(alias);
		log.info(EXECUTING_SQUERY_PI);
		getIRODSProtocol().irodsFunction(queryPI);
		log.info("removed specific query");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.accessobject.SpecificQueryAO#
	 * removeAllSpecificQueryBySQL(String)
	 */
	@Override
	public void removeAllSpecificQueryBySQL(final String sqlQuery)
			throws JargonException, DuplicateDataException {

		checkSupportForSpecificQuery();

		GeneralAdminInpForSQ queryPI;

		if (sqlQuery == null) {
			throw new IllegalArgumentException(
					"cannot remove specific query with null SQL query");
		}

		log.info("removing all specific queries by sql query: {}", sqlQuery);
		queryPI = GeneralAdminInpForSQ
				.instanceForRemoveAllSpecificQueryBySQL(sqlQuery);
		log.info(EXECUTING_SQUERY_PI);
		getIRODSProtocol().irodsFunction(queryPI);
		log.info("removed specific query");

	}

	/**
	 * Used internally when querying on alias to avoid recursively looking up
	 * those aliases. Note that the <code>specificQuery</code> parameter can
	 * contain a zone hint, and this is used to properly route the request.
	 * 
	 * @param specificQuery
	 * @param maxRows
	 * @param justTryWithoutSupportCheck
	 *            <code>boolean</code> that indicates that checks for support
	 *            for specific query should be bypassed. This is used to test
	 *            support by trying, which is an overhead because eIRODS version
	 *            numbers are off
	 * @return
	 * @throws DataNotFoundException
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	@Overheaded
	// BUG [#1663] iRODS environment shows 'rods3.0' as version
	private SpecificQueryResultSet executeSpecificQueryUsingAliasWithoutAliasLookup(
			final SpecificQuery specificQuery, final int maxRows,
			final boolean justTryWithoutSupportCheck)
			throws DataNotFoundException, JargonException, JargonQueryException {
		log.info("executeSpecificQueryUsingAlias()");

		if (specificQuery == null) {
			throw new IllegalArgumentException("null specific query");
		}

		if (!justTryWithoutSupportCheck) {
			checkSupportForSpecificQuery();
		}

		/*
		 * look up the alias and get the column names and number of arguments
		 * expected
		 */

		SpecificQueryDefinition specificQueryDefinition = new SpecificQueryDefinition();
		List<String> columnNames = new ArrayList<String>();
		columnNames.add("alias");
		columnNames.add("sql");
		specificQueryDefinition.setColumnNames(columnNames);

		return queryOnAliasGivenDefinition(specificQuery, maxRows,
				specificQueryDefinition, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.SpecificQueryAO#closeSpecificQuery()
	 */
	/*
	 * @Override public void closeSpecificQuery() throws JargonException {
	 * log.info("closeSpecificQuery()"); SpecificQueryInp specificQueryInp =
	 * SpecificQueryInp.instanceForClose();
	 * this.getIRODSProtocol().irodsFunction(specificQueryInp); }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.SpecificQueryAO#executeSpecificQueryUsingAlias
	 * (org.irods.jargon.core.query.SpecificQuery, int)
	 */
	@Override
	public SpecificQueryResultSet executeSpecificQueryUsingAlias(
			final SpecificQuery specificQuery, final int maxRows)
			throws DataNotFoundException, JargonException, JargonQueryException {

		return executeSpecificQueryUsingAlias(specificQuery, maxRows, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.SpecificQueryAO#executeSpecificQueryUsingAlias
	 * (org.irods.jargon.core.query.SpecificQuery, int, int)
	 */
	@Override
	public SpecificQueryResultSet executeSpecificQueryUsingAlias(
			final SpecificQuery specificQuery, final int maxRows,
			final int userDefinedOffset) throws DataNotFoundException,
			JargonException, JargonQueryException {

		log.info("executeSpecificQueryUsingAlias()");
		if (specificQuery == null) {
			throw new IllegalArgumentException("null specific query");
		}

		checkSupportForSpecificQuery();

		/*
		 * look up the alias and get the column names and number of arguments
		 * expected
		 */

		SpecificQueryDefinition specificQueryDefinition = findSpecificQueryByAlias(specificQuery
				.getQueryString());

		log.info("found specific query definition by alias");

		if (specificQuery.getArguments().size() != specificQueryDefinition
				.getArgumentCount()) {
			log.error("number of parameters in query does not match number of parameters provided");
			throw new JargonQueryException(
					"mismatch between query parameters and number of arguments provided");
		}

		return queryOnAliasGivenDefinition(specificQuery, maxRows,
				specificQueryDefinition, userDefinedOffset);
	}

	/**
	 * @param specificQuery
	 * @param maxRows
	 * @param specificQueryDefinition
	 * @param userDefinedOffset
	 *            <code>int</code> that represents an offset to use in the
	 *            returned record counts that is enforced within the sql itself.
	 *            This is used because users often use LIMIT and OFFSET
	 *            statements inside the actual SQL to accomplish custom paging.
	 *            This allows the result set to reflect any user supplied
	 *            offsets
	 * @return
	 * @throws JargonException
	 */
	private SpecificQueryResultSet queryOnAliasGivenDefinition(
			final SpecificQuery specificQuery, final int maxRows,
			final SpecificQueryDefinition specificQueryDefinition,
			final int userDefinedOffset) throws JargonException {

		SpecificQueryInp specificQueryInp = SpecificQueryInp.instance(
				specificQuery.getArguments(), specificQuery.getQueryString(),
				maxRows, specificQuery.getContinuationValue(),
				specificQuery.getZoneHint());

		Tag response = null;

		/*
		 * iRODS will throw an -808000 exception if no results (note the alias
		 * has already been looked up in iRODS, so I won't co-mingle this with
		 * an actual query missing error). Treat this as an empty result set
		 */
		try {
			response = getIRODSProtocol().irodsFunction(specificQueryInp);
		} catch (DataNotFoundException e) {
			log.info("no reults from iRODS, return as an empty result set");
			return new SpecificQueryResultSet(specificQuery,
					specificQueryDefinition.getColumnNames());
		}

		// result set is not empty

		int continuation = QueryResultProcessingUtils
				.getContinuationValue(response);

		boolean hasMoreRecords = false;

		if (continuation != 0) {
			hasMoreRecords = true;
		}

		List<IRODSQueryResultRow> resultRows = QueryResultProcessingUtils
				.translateResponseIntoResultSet(response,
						specificQueryDefinition.getColumnNames(), continuation,
						userDefinedOffset);

		SpecificQueryResultSet results = new SpecificQueryResultSet(
				specificQuery, resultRows,
				specificQueryDefinition.getColumnNames(), hasMoreRecords,
				continuation);

		log.info("doing a close for this page...");
		this.closeResultSet(results);
		return results;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.SpecificQueryAO#executeSpecificQueryUsingSql
	 * (org.irods.jargon.core.query.SpecificQuery, int)
	 */
	@Override
	public SpecificQueryResultSet executeSpecificQueryUsingSql(
			final SpecificQuery specificQuery, final int maxRows)
			throws DataNotFoundException, JargonException, JargonQueryException {

		return executeSpecificQueryUsingSql(specificQuery, maxRows, 0);
	}

	@Override
	public SpecificQueryResultSet executeSpecificQueryUsingSql(
			final SpecificQuery specificQuery, final int maxRows,
			final int userDefinedOffset) throws DataNotFoundException,
			JargonException, JargonQueryException {

		log.info("executeSpecificQueryUsingSql()");
		if (specificQuery == null) {
			throw new IllegalArgumentException("null specific query");
		}

		checkSupportForSpecificQuery();

		/*
		 * I assume the sql is there, and process it for number of parameters
		 * and column names
		 */

		List<String> columnNames = SpecificQueryAOImpl
				.parseColumnNamesFromQuery(specificQuery.getQueryString());
		int numberOfParameters = SpecificQueryAOImpl
				.countArgumentsInQuery(specificQuery.getQueryString());

		if (specificQuery.getArguments().size() != numberOfParameters) {
			log.error("number of parameters in query does not match number of parameters provided");
			throw new JargonQueryException(
					"mismatch between query parameters and number of arguments provided");
		}

		SpecificQueryInp specificQueryInp = SpecificQueryInp.instance(
				specificQuery.getArguments(), specificQuery.getQueryString(),
				maxRows, specificQuery.getContinuationValue(),
				specificQuery.getZoneHint());

		Tag response = null;

		response = getIRODSProtocol().irodsFunction(specificQueryInp);

		int continuation = QueryResultProcessingUtils
				.getContinuationValue(response);

		boolean hasMoreRecords = false;

		if (continuation != 0) {
			hasMoreRecords = true;
		}

		List<IRODSQueryResultRow> resultRows = QueryResultProcessingUtils
				.translateResponseIntoResultSet(response, columnNames,
						continuation, userDefinedOffset);

		SpecificQueryResultSet results = new SpecificQueryResultSet(
				specificQuery, resultRows, columnNames, hasMoreRecords,
				continuation);

		log.info("doing a close for this page...");
		this.closeResultSet(results);
		return results;

	}

	/**
	 * Close the result set associated with the given specific query. This will
	 * ignore calls if no continuation was in the result set.
	 * <p/>
	 * Note that this is currently private, and invoked for each request. This
	 * is to match the predominant usage pattern in clients where a page is
	 * viewed for a good deal of user think time, and we want to avoid leaving
	 * query handles open in iRODS. This might later change if we add
	 * continuations to the Jargon specific query support.
	 * 
	 * @param specificQueryResultSet
	 * @throws JargonException
	 */
	private void closeResultSet(
			final SpecificQueryResultSet specificQueryResultSet)
			throws JargonException {
		log.info("closeResultSet()");
		if (specificQueryResultSet == null) {
			throw new IllegalArgumentException("null specificQueryResultSet");
		}

		if (specificQueryResultSet.getContinuationIndex() == 0) {
			log.info("continuation is zero, no need to close...silently ignored");
			return;
		}

		SpecificQueryInp specificQueryInp = SpecificQueryInp
				.instanceForClose(specificQueryResultSet);

		this.getIRODSProtocol().irodsFunction(specificQueryInp);
		log.info("specific query closed");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.SpecificQueryAO#isSupportsSpecificQuery()
	 */
	@Override
	public boolean isSupportsSpecificQuery() throws JargonException {
		return !isSpecificQueryToBeBypassed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.SpecificQueryAO#isSpecificQueryToBeBypassed()
	 */
	@Overheaded
	// BUG [#1663] iRODS environment shows 'rods3.0' as version
	@Override
	public boolean isSpecificQueryToBeBypassed() throws JargonException {

		/*
		 * Per the overhead comment, the eIRODS 3.0 server advertises as
		 * "rods3.0". This is an issue because it makes the server appear to be
		 * an older version of iRODS. In order to assess specific query support,
		 * I'll look for at least rods3.0, then check by attempting to do a
		 * specific query. The results of this attempt are cached so I only do
		 * that once per server
		 */

		if (this.getIRODSServerProperties().isSupportsSpecificQuery()) {
			log.info("by version number I know I support specific query");
			return false;

		} else {
			return true;
		}

	}

	/**
	 * @throws JargonException
	 */
	private void checkSupportForSpecificQuery() throws JargonException {
		if (isSpecificQueryToBeBypassed()) {
			throw new JargonException("no support for specific query");
		}
	}
}
