package org.irods.jargon.core.pub.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specific query - advanced query for storage in iRODS and use
 * with iquest or SpecificQueryExecutorAO.
 *
 * @author Lisa Stillwell (RENCI)
 *
 */

public class SpecificQueryDefinition extends IRODSDomainObject {

	private String alias = "";
	private String sql = "";
	private int argumentCount = 0;
	private List<String> columnNames = new ArrayList<String>();

	/**
	 * handy constructor that takes an alias and sql string, this is useful for
	 * user-created instances for operations like adding. When this object is
	 * retrieved from iRODS, it is addtionally provide with information such as
	 * parameter counts and column names.
	 *
	 * @param alias
	 * @param sql
	 */
	public SpecificQueryDefinition(final String alias, final String sql) {

		if (alias == null || alias.isEmpty()) {
			throw new IllegalArgumentException("null or empty alias");
		}

		if (sql == null || sql.isEmpty()) {
			throw new IllegalArgumentException("null or empty sql");
		}

		this.sql = sql;
		this.alias = alias;
	}

	/**
	 * Default (no-values) constructor
	 */
	public SpecificQueryDefinition() {

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SpecificQuerySQLDetails:");
		sb.append("\n   alias:");
		sb.append(alias);
		sb.append("\n    sql:");
		sb.append(sql);
		sb.append("\n    argumentCount:");
		sb.append(argumentCount);
		sb.append("\n    columnNames:");
		sb.append(columnNames);
		return sb.toString();
	}

	/**
	 * Gets the count of arguments, this is set when this data is retrieved from
	 * iRODS by query
	 *
	 * @return the argumentCount <code>int</code> with the count of arguments in
	 *         the sql query
	 */
	public int getArgumentCount() {
		return argumentCount;
	}

	/**
	 * @param argumentCount
	 *            the argumentCount to set
	 */
	public void setArgumentCount(final int argumentCount) {
		this.argumentCount = argumentCount;
	}

	/**
	 * Gets the column names by parsing the SQL query. Note that this is only
	 * available when querying the object from iRODS.
	 *
	 * @return the columnNames <code>List<String></code> with the parsed column
	 *         names from the query
	 */
	public List<String> getColumnNames() {
		return columnNames;
	}

	/**
	 * @param columnNames
	 *            the columnNames to set
	 */
	public void setColumnNames(final List<String> columnNames) {
		this.columnNames = columnNames;
	}

	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias
	 *            the alias to set
	 */
	public void setAlias(final String alias) {
		this.alias = alias;
	}

	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param sql
	 *            the sql to set
	 */
	public void setSql(final String sql) {
		this.sql = sql;
	}

}
