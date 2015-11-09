package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbstractAliasedQuery {

	protected final String queryString;
	protected final List<String> arguments;
	protected final int continuationValue;

	/**
	 * Base constructor
	 *
	 * @param queryString
	 *            <code>String</code> query string or alias, required
	 * @param arguments
	 *            <code>List<String></code> of arguments, may be set to
	 *            <code>null</code> if no args. If arguments are specified, they
	 *            are wrapped in an unmodifiable list
	 * @param continuationValue
	 *            <code>int</code> with a continuation value as returned from a
	 *            previous query result. Used for paging, set to zero if no
	 *            paging is done, or is initial query
	 */
	protected AbstractAliasedQuery(final String queryString,
			final List<String> arguments, final int continuationValue) {

		if (queryString == null || queryString.isEmpty()) {
			throw new IllegalArgumentException("null or empty queryString");
		}

		if (continuationValue < 0) {
			throw new IllegalArgumentException(
					"continuation value is less than zero");
		}

		if (arguments == null) {
			this.arguments = Collections
					.unmodifiableList(new ArrayList<String>());
		} else {
			if (arguments.size() > 4) {
				throw new IllegalArgumentException("limit of 4 arguments");
			}
			this.arguments = Collections.unmodifiableList(arguments);
		}

		this.queryString = queryString;
		this.continuationValue = continuationValue;
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