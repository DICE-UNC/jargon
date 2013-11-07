/**
 * 
 */
package org.irods.jargon.conveyor.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utils for managing exceptions that might occur in conveyor processing
 * 
 * @author Mike Conway - DICE (www.irods.org) see
 *         https://code.renci.org/gf/project/jargon/
 * 
 */
public class ExceptionUtils {

	/**
	 * Given an exception, return the stack trace information in string format.
	 * If the given exception is null, just return null. Avoids NPEs in code
	 * when processing exceptions not always available
	 * 
	 * @return <code>String</code> representation of a stack trace or
	 *         <code>null</code> if there is no exception
	 */
	public static String stackTraceToString(final Exception e) {

		if (e == null) {
			return null;
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	/**
	 * If an exception is present, return the message, otherwise, return null.
	 * Avoids NPEs in code when processing exceptions not always available
	 * 
	 * @param e
	 * @return <code>String</code> with the exception message, or
	 *         <code>null</code> if no exception is provided
	 */
	public static String messageOrNullFromException(final Exception e) {
		if (e == null) {
			return null;
		}

		return e.getMessage();

	}

}
