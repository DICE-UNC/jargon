package org.irods.jargon.core.query;

import java.util.List;

/**
 * Represents a 'specific query' to run against iRODS. Specific Query is a a new
 * facility since iRODS 2.5 to run explicitly defined select queries against
 * iCAT in cases where GenQuery is not specific enough.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SpecificQuery extends AbstractAliasedQuery {
	
	/**
	 * Create an instance of a specific (SQL) query with no arguments
	 * 
	 * @param queryString
	 *            <code>String</code> with either the exact SQL as registered by
	 *            an admin, or an alias as registered by an admin
	 * @param continuationValue
	 *            <code>int</code> with the continuation value from a previous
	 *            page of results, or the value 0 if this is an initial query
	 * @return <code>SpecificQuery</code> instance that can be run against the
	 *         iRODS catalog
	 */
	public static SpecificQuery instanceWithNoArguments(
			final String queryString, final int continuationValue) {
		return new SpecificQuery(queryString, null, continuationValue);
	}

	/**
	 * Create an instance of a specific (SQL) query including arguments
	 * 
	 * @param queryString
	 *            <code>String</code> with either the exact SQL as registered by
	 *            an admin, or an alias as registered by an admin
	 * @param arguments
	 *            <code>List<String></code> with a maximum of 10 arguments to be
	 *            bound to variables defined in the specific query registered by
	 *            an administrator. This may be set to <code>null</code> if not
	 *            used
	 * @param continuationValue
	 *            <code>int</code> with the continuation value from a previous
	 *            page of results, or the value 0 if this is an initial query
	 * @return <code>SpecificQuery</code> instance that can be run against the
	 *         iRODS catalog
	 */
	public static SpecificQuery instanceArguments(final String queryString,
			List<String> arguments, final int continuationValue) {
		return new SpecificQuery(queryString, arguments, continuationValue);
	}

	/**
	 * Private constructors, use instance methods to create query instances
	 * 
	 * @param queryString
	 * @param arguments
	 * @param continuationValue
	 */
	private SpecificQuery(String queryString, List<String> arguments,
			int continuationValue) {
		super(queryString, arguments, continuationValue);
	}

}
