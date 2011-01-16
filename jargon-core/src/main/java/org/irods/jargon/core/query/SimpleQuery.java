/**
 * 
 */
package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an sql statement as a 'simple query', which is an iRODS facility
 * that allows certain SQL statements to be executed. These statements are
 * configured within iRODS.
 * <p/>
 * This class is immutable and thread-safe.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public  class SimpleQuery {

	private final String queryString;
	private final List<String> arguments;
	private final int continuationValue;

	/**
	 * Creates a new instance of a <code>SimpleQuery</code> object.
	 * 
	 * @param queryString
	 *            <code>String</code> with the sql query to execute.
	 * @param arguments
	 *            <code>List<String></code> with arguments to the query. This
	 *            may be set to null if no arguments desired.
	 * @param continuationValue <code>int</code> with the offset into the results.,  0 if no offset.
	 * @return <code>SimpleQuery</code>
	 */
	public static SimpleQuery instance(final String queryString,
			final List<String> arguments, final int continuationValue) {
		return new SimpleQuery(queryString, arguments, continuationValue);
	}
	
	/**
	 * Creates an instance that has no arguments.
	 * @param queryString
	 *            <code>String</code> with the sql query to execute.
	 * @param continuationValue <code>int</code> with the offset into the results.,  0 if no offset.
	 * @return
	 */
	public static SimpleQuery instanceWithNoArguments(final String queryString, final int continuationValue) {
		List<String> args = new ArrayList<String>();
		return new SimpleQuery(queryString, args, continuationValue);
	}

	/**
	 * Creates an instance of a <code>SimpleQuery</code> object giving one
	 * argument.
	 * 
	 * @param queryString
	 *            <code>String</code> with the sql query to execute.
	 * @param arg
	 *            <code>String</code> with the arugment for the query. Set to
	 *            blank if unused.
	 * @param continuationValue <code>int</code> with the offset into the results.,  0 if no offset.
	 * @return
	 */
	public static SimpleQuery instanceWithOneArgument(final String queryString,
			final String arg, final int continuationValue) {

		if (arg == null) {
			throw new IllegalArgumentException("arg is null");
		}

		List<String> args = new ArrayList<String>();
		if (!arg.isEmpty()) {
			args.add(arg);
		}
		return new SimpleQuery(queryString, args, continuationValue);

	}

	/**
	 * Creates an instance of a <code>SimpleQuery</code> object giving two
	 * arguments.
	 * 
	 * @param queryString
	 *            <code>String</code> with the sql query to execute.
	 * @param arg1
	 *            <code>String</code> with the argument for the query.
	 * @param arg2
	 *            <code>String</code> with the second argument for the query.
	 * @param continuationValue <code>int</code> with the offset into the results.,  0 if no offset.
	 * @return
	 */
	public static SimpleQuery instanceWithTwoArguments(final String queryString,
			final String arg1, final String arg2, final int continuationValue) {

		if (arg1 == null || arg1.isEmpty()) {
			throw new IllegalArgumentException("arg1 is null or empty");
		}

		if (arg2 == null || arg2.isEmpty()) {
			throw new IllegalArgumentException("arg2 is null or empty");
		}

		List<String> args = new ArrayList<String>();

		args.add(arg1);
		args.add(arg2);

		return new SimpleQuery(queryString, args, continuationValue);

	}

	private SimpleQuery(final String queryString, final List<String> arguments, final int continuationValue) {

		if (queryString == null || queryString.isEmpty()) {
			throw new IllegalArgumentException("empty or null queryString");
		}

		if (continuationValue < 0) {
			throw new IllegalArgumentException("continuation value is less than zero");
		}
		
		
		this.queryString = queryString;
		this.continuationValue = continuationValue;

		if (arguments == null) {
			this.arguments = Collections
					.unmodifiableList(new ArrayList<String>());
		} else {
			if (arguments.size() > 4) {
				throw new IllegalArgumentException("limit of 4 arguments");
			}
			this.arguments = Collections.unmodifiableList(arguments);
		}
	}

	public String getQueryString() {
		return queryString;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("simpleQuery:");
		sb.append("\n   queryString:");
		sb.append(queryString);
		sb.append("\n   arguments:");
		sb.append(arguments);
		sb.append("\n  continuationValue:");
		sb.append(continuationValue);
		return sb.toString();
	}
	
	public int getContinuationValue() {
		return continuationValue;
	}

	public List<String> getArguments() {
		return arguments;
	}

}
