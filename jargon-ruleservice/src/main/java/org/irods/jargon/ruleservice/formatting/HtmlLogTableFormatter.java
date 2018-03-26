/**
 * 
 */
package org.irods.jargon.ruleservice.formatting;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Class to format stdout or stderror as an HTML table
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class HtmlLogTableFormatter {

	/**
	 * Given an exception, print the stack trace as a formatted HTML table
	 * usable in bootsrap2
	 * 
	 * @param exception
	 * @return {@code String} representing an HTML table for bootstrap2
	 */
	public static String formatStackTraceAsBootstrap2Table(
			final Exception exception) {

		if (exception == null) {
			throw new IllegalArgumentException("null exception");
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		return formatAsBootstrap2Table(sw.toString(), "Stack Trace");

	}

	/**
	 * Given a set of log data (which is delimited by the carriage returns, and
	 * an optional caption, return a table in bootstrap2 format, which is a
	 * basic striped table representing the log data
	 * 
	 * @param logData
	 *            {@code String} with the log data
	 * @param caption
	 *            {@code String} that is blank if not needed, that givs an
	 *            optional table caption
	 * @return {@code String} representing an HTML table for bootstrap2
	 */
	public static String formatAsBootstrap2Table(final String logData,
			final String caption) {

		if (logData == null) {
			throw new IllegalArgumentException("null logData");
		}

		if (caption == null) {
			throw new IllegalArgumentException("null caption");
		}

		// build table header cruft

		StringBuilder sb = new StringBuilder();
		sb.append("<table class=\"table table-striped\">");
		sb.append("<caption>");
		sb.append(caption);
		sb.append("</caption>");
		sb.append("<tbody>");

		// convert crs to table rows

		String[] result = logData.split("\\n");
		for (String element : result) {
			sb.append("<tr><td>");
			sb.append(element);
			sb.append("</td></tr>");
		}

		sb.append("</tbody>");
		sb.append("</table>");

		return sb.toString();

	}

}
