package org.irods.jargon.core.pub.domain;

/**
 * Represents a specific query - advanced query for storage in iRODS and use with
 * iquest or SpecificQueryExecutorAO.
 * 
 * @author Lisa Stillwell (RENCI)
 * 
 */

public class SpecificQuery extends IRODSDomainObject {
	
	private String sqlQuery;
	private String alias;
	
	/*
	 * Default (no values) constructor
	 */
	public SpecificQuery() {
	}
	
	/*
	 *  constructor
	 *  @param sqlQuery
	 *  @param alias
	 */
	public SpecificQuery(String sqlQuery, String alias) {
		this.sqlQuery = sqlQuery;
		this.alias = alias;
	}
	
	/**
	 * @return the sqlQuery
	 */
	public String getSqlQuery() {
		return sqlQuery;
	}
	
	/**
	 * @param sqlQuery
	 *            the sqlQuery to set
	 */
	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}
	
	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}
	
	/**
	 * @param alias
	 *            the alias name to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("Specific Query:");
		sb.append("\n   sqlQuery:");
		sb.append(sqlQuery);
		sb.append("\n   alias:");
		sb.append(alias);

		return sb.toString();
	}
}
