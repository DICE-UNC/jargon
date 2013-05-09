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
	 * Given an exception, return the stack trace information in string format
	 * 
	 * @return <code>String</code> representation of a stack trace
	 */
	public static String stackTraceToString(final Exception e) {

		if (e == null) {
			throw new IllegalArgumentException("null exception");
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

}
