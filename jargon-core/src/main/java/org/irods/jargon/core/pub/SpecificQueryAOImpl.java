package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpecificQueryAOImpl extends IRODSGenericAO implements
		SpecificQueryAO {

	private static final String EXECUTING_SQUERY_PI = "executing specific query PI";
	public static final Logger log = LoggerFactory
			.getLogger(SpecificQueryAOImpl.class);
	private final EnvironmentalInfoAO environmentalInfoAO;

	protected SpecificQueryAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		this.environmentalInfoAO = this.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(getIRODSAccount());
		if (!environmentalInfoAO.isAbleToRunSpecificQuery()) {
			log.error("cannot run specific query on this iRODS version");
			throw new JargonException(
					"specific query not supported on this iRODS server");
		}
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

		if (specificQueryAlias == null || specificQueryAlias.isEmpty()) {
			throw new IllegalArgumentException("null specificQueryAlias");
		}

		log.info("alias:{}", specificQueryAlias);

		List<String> arguments = new ArrayList<String>();
		arguments.add(specificQueryAlias.trim());

		SpecificQuery specificQuery = SpecificQuery.instanceArguments(
				"listQueryByAliasLike", arguments, 0);
		SpecificQueryResultSet resultSet;
		try {
			resultSet = this.executeSpecificQueryUsingAliasWithoutAliasLookup(
					specificQuery, this.getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
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

		log.info("findSpecificQueryByAlias()");

		if (specificQueryAlias == null || specificQueryAlias.isEmpty()) {
			throw new IllegalArgumentException("null specificQueryAlias");
		}

		log.info("alias:{}", specificQueryAlias);

		List<String> arguments = new ArrayList<String>();
		arguments.add(specificQueryAlias.trim());

		SpecificQuery specificQuery = SpecificQuery.instanceArguments(
				"findQueryByAlias", arguments, 0);
		SpecificQueryResultSet resultSet;
		try {
			resultSet = this.executeSpecificQueryUsingAliasWithoutAliasLookup(
					specificQuery, this.getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
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
		posSelect = colNames.indexOf("distinct");

		if (posSelect > -1) {
			// trim off distinct
			colNames = colNames.substring(posSelect + 8);
		}

		String[] colList = colNames.split(",");

		List<String> listToReturn = new ArrayList<String>();
		for (String name : colList) {
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
		GeneralAdminInpForSQ queryPI;

		if (specificQuery == null) {
			throw new IllegalArgumentException(
					"cannot create specific query with null SpecificQueryDefinition object");
		}

		log.info("creating specific query: {}", specificQuery);

		queryPI = GeneralAdminInpForSQ
				.instanceForAddSpecificQuery(specificQuery);

		log.info(EXECUTING_SQUERY_PI);

		getIRODSProtocol().irodsFunction(queryPI);

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
	 * those aliases
	 * 
	 * @param specificQuery
	 * @param maxRows
	 * @return
	 * @throws DataNotFoundException
	 * @throws JargonException
	 * @throws JargonQueryException
	 */
	private SpecificQueryResultSet executeSpecificQueryUsingAliasWithoutAliasLookup(
			final SpecificQuery specificQuery, final int maxRows)
			throws DataNotFoundException, JargonException, JargonQueryException {
		log.info("executeSpecificQueryUsingAlias()");
		if (specificQuery == null) {
			throw new IllegalArgumentException("null specific query");
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
				specificQueryDefinition);
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

	@Override
	public SpecificQueryResultSet executeSpecificQueryUsingAlias(
			final SpecificQuery specificQuery, final int maxRows)
			throws DataNotFoundException, JargonException, JargonQueryException {

		log.info("executeSpecificQueryUsingAlias()");
		if (specificQuery == null) {
			throw new IllegalArgumentException("null specific query");
		}

		/*
		 * look up the alias and get the column names and number of arguments
		 * expected
		 */

		SpecificQueryDefinition specificQueryDefinition = this
				.findSpecificQueryByAlias(specificQuery.getQueryString());

		log.info("found specific query definition by alias");

		if (specificQuery.getArguments().size() != specificQueryDefinition
				.getArgumentCount()) {
			log.error("number of parameters in query does not match number of parameters provided");
			throw new JargonQueryException(
					"mismatch between query parameters and number of argumetns provided");
		}

		return queryOnAliasGivenDefinition(specificQuery, maxRows,
				specificQueryDefinition);
	}

	/**
	 * @param specificQuery
	 * @param maxRows
	 * @param specificQueryDefinition
	 * @return
	 * @throws JargonException
	 */
	private SpecificQueryResultSet queryOnAliasGivenDefinition(
			final SpecificQuery specificQuery, final int maxRows,
			final SpecificQueryDefinition specificQueryDefinition)
			throws JargonException {
		SpecificQueryInp specificQueryInp = SpecificQueryInp.instance(
				specificQuery.getArguments(), specificQuery.getQueryString(),
				maxRows, 0);

		Tag response = null;

		/*
		 * iRODS will throw an -808000 exception if no results (note the alias has already been looked up in iRODS, so
		 * I won't co-mingle this with an actual query missing error).  Treat this as an empty result set
		 */
		try {
			response = this.getIRODSProtocol().irodsFunction(specificQueryInp);
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

		// FIXME: clean up has more results by pulling into abstract superclass
		List<IRODSQueryResultRow> resultRows = QueryResultProcessingUtils
				.translateResponseIntoResultSet(response,
						specificQueryDefinition.getColumnNames(), continuation,
						0);

		return new SpecificQueryResultSet(specificQuery, resultRows,
				specificQueryDefinition.getColumnNames(), hasMoreRecords,
				continuation);
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

		log.info("executeSpecificQueryUsingSql()");
		if (specificQuery == null) {
			throw new IllegalArgumentException("null specific query");
		}

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
					"mismatch between query parameters and number of argumetns provided");
		}

		SpecificQueryInp specificQueryInp = SpecificQueryInp.instance(
				specificQuery.getArguments(), specificQuery.getQueryString(),
				maxRows, 0);

		Tag response = null;

		response = this.getIRODSProtocol().irodsFunction(specificQueryInp);

		int continuation = QueryResultProcessingUtils
				.getContinuationValue(response);

		boolean hasMoreRecords = false;

		if (continuation != 0) {
			hasMoreRecords = true;
		}

		List<IRODSQueryResultRow> resultRows = QueryResultProcessingUtils
				.translateResponseIntoResultSet(response, columnNames,
						continuation, 0);

		return new SpecificQueryResultSet(specificQuery, resultRows,
				columnNames, hasMoreRecords, continuation);
	}

}
