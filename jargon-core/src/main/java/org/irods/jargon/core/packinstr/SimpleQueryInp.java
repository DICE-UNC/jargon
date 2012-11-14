package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.AbstractAliasedQuery;
import org.irods.jargon.core.query.SimpleQuery;

/**
 * Packing instruction to execute a simple query on iRODS. Simple query is a
 * dedicated facility to run certain authorized sql statements. This is
 * configured on the iRODS server itself.
 * <p/>
 * This object is immutable and thread-safe.
 * <p/>
 * This is used in cases where it is easier to do a straight-forward SQL query
 * rather than go thru the generalQuery interface. This is used in the iadmin.c
 * interface as it was easier to work in SQL for admin type ops.
 * <p/>
 * For improved security, this is available only to admin users and the code
 * checks that the input sql is one of the allowed forms.
 * <p/>
 * input: sql, up to for optional arguments (bind variables), and requested
 * format, max text to return (maxOutBuf) output: text (outBuf) or error return
 * input/output: control: on input if 0 request is starting, returned non-zero
 * if more rows are available (and then it is the statement number); on input if
 * positive it is the statement number (+1) that is being continued. format 1:
 * column-name : column value, and with CR after each column format 2: column
 * headings CR, rows and col values with CR.
 * <p/>
 * This implementation will default to format 2, as this is used to formulate a
 * result set.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SimpleQueryInp extends AbstractIRODSPackingInstruction {

	private final AbstractAliasedQuery simpleQuery;
	private static final int maxBuffSize = 1024;

	public static final String PI_TAG = "simpleQueryInp_PI";
	public static final int SIMPLE_QUERY_API_NBR = 614;

	public static final String SQL = "sql";
	public static final String ARG1 = "arg1";
	public static final String ARG2 = "arg2";
	public static final String ARG3 = "arg3";
	public static final String ARG4 = "arg4";
	public static final String CONTROL = "control";
	public static final String FORM = "form";
	public static final String MAX_BUF_SIZE = "maxBufSize";

	/**
	 * Static instance method to create the packing instruction.
	 * 
	 * @param simpleQuery
	 *            {@link SimpleQuery} that contains the query to send to iRODS.
	 * @return <code>SimpleQueryInp</code> packing instruction.
	 */
	public static SimpleQueryInp instance(final AbstractAliasedQuery simpleQuery) {
		return new SimpleQueryInp(SIMPLE_QUERY_API_NBR, simpleQuery);
	}

	public AbstractAliasedQuery getSimpleQuery() {
		return simpleQuery;
	}

	public int getMaxBuffSize() {
		return maxBuffSize;
	}

	private SimpleQueryInp(final int apiNbr,
			final AbstractAliasedQuery simpleQuery) {
		super();

		if (apiNbr <= 0) {
			throw new IllegalArgumentException("negative or zero apiNbr");
		}

		if (simpleQuery == null) {
			throw new IllegalArgumentException("simpleQuery is null");
		}

		this.simpleQuery = simpleQuery;
		this.setApiNumber(apiNbr);

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

		int sz = simpleQuery.getArguments().size();

		if (sz > 0 && simpleQuery.getArguments().get(0) != null) {
			arg1 = simpleQuery.getArguments().get(0);
		}

		if (sz > 1 && simpleQuery.getArguments().get(1) != null) {
			arg2 = simpleQuery.getArguments().get(1);
		}

		if (sz > 2 && simpleQuery.getArguments().get(2) != null) {
			arg3 = simpleQuery.getArguments().get(2);
		}

		if (sz > 3 && simpleQuery.getArguments().get(3) != null) {
			arg4 = simpleQuery.getArguments().get(3);
		}

		Tag message = new Tag(PI_TAG, new Tag[] {
				new Tag(SQL, simpleQuery.getQueryString()),
				new Tag(ARG1, arg1), new Tag(ARG2, arg2), new Tag(ARG3, arg3),
				new Tag(ARG4, arg4),
				new Tag(CONTROL, simpleQuery.getContinuationValue()),
				new Tag(FORM, 2), new Tag(MAX_BUF_SIZE, maxBuffSize), });

		return message;

	}

}
