package org.irods.jargon.core.query;

import java.util.ArrayList;
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

	private final String zoneHint;

	/**
	 * Get the (optional) hint that points to the correct zone to query
	 * 
	 * @return
	 */
	public String getZoneHint() {
		return zoneHint;
	}

	/**
	 * Create an instance of a specific (SQL) query with no arguments
	 * 
	 * @param queryString
	 *            <code>String</code> with either the exact SQL as registered by
	 *            an admin, or an alias as registered by an admin
	 * @param continuationValue
	 *            <code>int</code> with the continuation value from a previous
	 *            page of results, or the value 0 if this is an initial query
	 * @param zoneHint
	 *            <code>String</code> (optional, blank if not needed) zone hint
	 *            for cross-zone invocation
	 * @return <code>SpecificQuery</code> instance that can be run against the
	 *         iRODS catalog
	 */
	public static SpecificQuery instanceWithNoArguments(
			final String queryString, final int continuationValue,
			final String zoneHint) {
		return new SpecificQuery(queryString, null, continuationValue, zoneHint);

	}

	/**
	 * Create an instance of a specific (SQL) query including one argument
	 * 
	 * @param queryString
	 *            <code>String</code> with either the exact SQL as registered by
	 *            an admin, or an alias as registered by an admin
	 * @param argument
	 *            <code>String<String></code> with a single argument
	 * @param continuationValue
	 *            <code>int</code> with the continuation value from a previous
	 *            page of results, or the value 0 if this is an initial query
	 * @param zoneHint
	 *            <code>String</code> (optional, blank if not needed) zone hint
	 *            for cross-zone invocation
	 * @return <code>SpecificQuery</code> instance that can be run against the
	 *         iRODS catalog
	 */
	public static SpecificQuery instanceWithOneArgument(
			final String queryString, final String argument,
			final int continuationValue, final String zoneHint) {

		List<String> args = new ArrayList<String>(1);
		args.add(argument);
		return new SpecificQuery(queryString, args, continuationValue, zoneHint);
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
	 * @param zoneHint
	 *            <code>String</code> (optional, blank if not needed) zone hint
	 *            for cross-zone invocation
	 * @return <code>SpecificQuery</code> instance that can be run against the
	 *         iRODS catalog
	 */
	public static SpecificQuery instanceArguments(final String queryString,
			final List<String> arguments, final int continuationValue,
			final String zoneHint) {
		return new SpecificQuery(queryString, arguments, continuationValue,
				zoneHint);
	}

	/**
	 * Private constructors, use instance methods to create query instances
	 * 
	 * @param queryString
	 * @param arguments
	 * @param continuationValue
	 * @param zoneHint
	 *            <code>String</code> (optional, blank if not needed) zone hint
	 *            for cross-zone invocation
	 */
	private SpecificQuery(final String queryString,
			final List<String> arguments, final int continuationValue,
			final String zoneHint) {
		super(queryString, arguments, continuationValue);
		if (zoneHint == null) {
			throw new IllegalArgumentException("null zoneHint");
		}
		this.zoneHint = zoneHint;
	}

}
