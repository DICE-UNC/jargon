/**
 * 
 */
package org.irods.jargon.core.query;

/**
 * Represents an sql statement as a 'simple query', which is an iRODS facility that allows certain SQL statements to be executed. These
 * statements are configured within iRODS.  
 * <p/>
 * This class is immutable and thread-safe.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class SimpleQuery {
	
	private final String queryString;
	private final String arguments;
	
	/**
	 * Creates a new instance of a <code>SimpleQuery</code> object.
	 * @param queryString <code>String</code> with the sql query to execute.
	 * @param arguments <code>String</code> with arguments to the query.  Blank if unused, nulls are not valid.
	 * @return <code>SimpleQuery</code>
	 */
	public static SimpleQuery instance(final String queryString, final String arguments) {
		return new SimpleQuery(queryString, arguments);
	}
	
	private SimpleQuery(final String queryString, final String arguments) {
		
		if (queryString == null || queryString.isEmpty()) {
			throw new IllegalArgumentException("empty or null queryString");
		}
		
		if (arguments == null) {
			throw new IllegalArgumentException("null arguments, set to blank if not used");
		}
		
		this.queryString = queryString;
		this.arguments = arguments;
	}
	
	public String getQueryString() {
		return queryString;
	}

	public String getArguments() {
		return arguments;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("simpleQuery:");
		sb.append("\n   queryString:");
		sb.append(queryString);
		sb.append("\n   arguments:");
		sb.append(arguments);
		return sb.toString();
	}

}
