/**
 *
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.SpecificQueryResultSet;

/**
 * Packing instruction for exectution of a specific (SQL) query.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SpecificQueryInp extends AbstractIRODSPackingInstruction {

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("SpecificQueryInp [");
		if (args != null) {
			builder.append("args=");
			builder.append(args.subList(0, Math.min(args.size(), maxLen)));
			builder.append(", ");
		}
		if (queryOrAlias != null) {
			builder.append("queryOrAlias=");
			builder.append(queryOrAlias);
			builder.append(", ");
		}
		builder.append("maxRows=");
		builder.append(maxRows);
		builder.append(", continueIndex=");
		builder.append(continueIndex);
		builder.append(", ");
		if (zoneHint != null) {
			builder.append("zoneHint=");
			builder.append(zoneHint);
		}
		builder.append("]");
		return builder.toString();
	}

	public static final String PI_TAG = "specificQueryInp_PI";
	public static final int SPECIFIC_QUERY_API_NBR = 722;
	private static final int MAX_ARGS = 10;

	public static final String ARG1 = "arg1";
	public static final String ARG2 = "arg2";
	public static final String ARG3 = "arg3";
	public static final String ARG4 = "arg4";
	public static final String ARG5 = "arg5";
	public static final String ARG6 = "arg6";
	public static final String ARG7 = "arg7";
	public static final String ARG8 = "arg8";
	public static final String ARG9 = "arg9";
	public static final String ARG10 = "arg10";
	public static final String SQL = "sql";
	public static final String MAX_ROWS = "maxRows";
	public static final String CONTINUE = "continueInx";
	public static final String ROW_OFFSET = "rowOffset";
	public static final String OPTIONS = "options";

	private final List<String> args;
	private final String queryOrAlias;
	private final int maxRows;
	private final int continueIndex;
	private final String zoneHint;

	/**
	 * Create an instance of the packing instruction to execute a specific query
	 * statement
	 *
	 * @param args
	 *            <code>List<String></code> of arguments that match query
	 *            parameters. Note that there may be a max of 10 parameters
	 *            provided, or an error will result. This may be set to
	 *            <code>null</code> if no parameters are passed in
	 * @param queryOrAlias
	 *            <cod>String</code> with either the query alias, or a query
	 *            that matches exactly to a query stored on the grid via the
	 *            rods administrator.
	 * @param maxRows
	 *            <code>int</code> with the maximum number of rows to be
	 *            returned.
	 * @param continueIndex
	 *            <code>int</code> with the index passed back from a preceeding
	 *            query
	 * @param zoneHint
	 *            <code>String</code> (optional, blank if not needed) zone hint
	 *            for cross-zone invocation
	 */
	public static final SpecificQueryInp instance(final List<String> args,
			final String queryOrAlias, final int maxRows,
			final int continueIndex, final String zoneHint) {
		SpecificQueryInp specificQueryInp = new SpecificQueryInp(args,
				queryOrAlias, maxRows, continueIndex, zoneHint);
		specificQueryInp.setApiNumber(SPECIFIC_QUERY_API_NBR);
		return specificQueryInp;
	}

	public static final SpecificQueryInp instanceForClose(
			final SpecificQueryResultSet specificQueryResultSet) {

		if (specificQueryResultSet == null) {
			throw new IllegalArgumentException("null specificQueryResultSet");
		}

		SpecificQueryInp specificQueryInp = new SpecificQueryInp(null, "close",
				0, specificQueryResultSet.getContinuationIndex(), "");
		specificQueryInp.setApiNumber(SPECIFIC_QUERY_API_NBR);
		return specificQueryInp;
	}

	/**
	 *
	 * @param args
	 *            <code>List<String></code> of arguments that match query
	 *            parameters. Note that there may be a max of 10 parameters
	 *            provided, or an error will result. This may be set to
	 *            <code>null</code> if no parameters are passed in
	 * @param queryOrAlias
	 *            <cod>String</code> with either the query alias, or a query
	 *            that matches exactly to a query stored on the grid via the
	 *            rods administrator.
	 * @param maxRows
	 *            <code>int</code> with the maximum number of rows to be
	 *            returned. Note that this will be ignored if the
	 *            <code>autoClose</code> parameter is set to <code>true</code>
	 * @param continueIndexj
	 *            <code>int</code> with the index passed back from a preceeding
	 *            query
	 * @param zoneHint
	 *            <code>String</code> (optional, blank if not needed) zone hint
	 *            for cross-zone invocation
	 */
	private SpecificQueryInp(final List<String> args,
			final String queryOrAlias, final int maxRows,
			final int continueIndex, final String zoneHint) {

		if (args == null) {
			this.args = new ArrayList<String>();
		} else if (args.size() > MAX_ARGS) {
			throw new IllegalArgumentException("Too many arguments supplied");
		} else {
			this.args = args;
		}

		if (queryOrAlias == null || queryOrAlias.isEmpty()) {
			throw new IllegalArgumentException("null or empty queryOrAlias");
		}

		if (zoneHint == null) {
			throw new IllegalArgumentException("null zoneHint");
		}

		this.queryOrAlias = queryOrAlias;
		this.maxRows = maxRows;
		this.continueIndex = continueIndex;
		this.zoneHint = zoneHint;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue
	 * ()
	 */
	@Override
	public Tag getTagValue() throws JargonException {

		String arg1 = "";
		String arg2 = "";
		String arg3 = "";
		String arg4 = "";
		String arg5 = "";
		String arg6 = "";
		String arg7 = "";
		String arg8 = "";
		String arg9 = "";
		String arg10 = "";

		int sz = args.size();

		if (sz > 0 && args.get(0) != null) {
			arg1 = args.get(0);
		}

		if (sz > 1 && args.get(1) != null) {
			arg2 = args.get(1);
		}

		if (sz > 2 && args.get(2) != null) {
			arg3 = args.get(2);
		}

		if (sz > 3 && args.get(3) != null) {
			arg4 = args.get(3);
		}

		if (sz > 4 && args.get(4) != null) {
			arg5 = args.get(4);
		}

		if (sz > 5 && args.get(5) != null) {
			arg6 = args.get(5);
		}

		if (sz > 6 && args.get(6) != null) {
			arg7 = args.get(6);
		}

		if (sz > 7 && args.get(7) != null) {
			arg8 = args.get(7);
		}

		if (sz > 8 && args.get(8) != null) {
			arg9 = args.get(8);
		}

		if (sz > 9 && args.get(9) != null) {
			arg10 = args.get(9);
		}

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		/*
		 * If there is a zone hint, add it to the query
		 */
		if (!zoneHint.isEmpty()) {
			kvps.add(KeyValuePair.instance("zone", zoneHint));
		}

		Tag message = new Tag(PI_TAG);

		message.addTag(new Tag(SQL, queryOrAlias));

		if (!arg1.isEmpty()) {
			message.addTag(new Tag(ARG1, arg1));
		}

		if (!arg2.isEmpty()) {
			message.addTag(new Tag(ARG2, arg2));
		}

		if (!arg3.isEmpty()) {
			message.addTag(new Tag(ARG3, arg3));
		}

		if (!arg4.isEmpty()) {
			message.addTag(new Tag(ARG4, arg4));
		}

		if (!arg5.isEmpty()) {
			message.addTag(new Tag(ARG5, arg5));
		}

		if (!arg6.isEmpty()) {
			message.addTag(new Tag(ARG6, arg6));
		}

		if (!arg7.isEmpty()) {
			message.addTag(new Tag(ARG7, arg7));
		}

		if (!arg8.isEmpty()) {
			message.addTag(new Tag(ARG8, arg8));
		}

		if (!arg9.isEmpty()) {
			message.addTag(new Tag(ARG9, arg9));
		}

		if (!arg10.isEmpty()) {
			message.addTag(new Tag(ARG10, arg10));
		}

		message.addTag(new Tag(MAX_ROWS, maxRows));
		message.addTag(new Tag(CONTINUE, continueIndex));
		message.addTag(new Tag(ROW_OFFSET, 0));
		message.addTag(new Tag(OPTIONS, 0));

		message.addTag(createKeyValueTag(kvps));

		return message;
	}

}
