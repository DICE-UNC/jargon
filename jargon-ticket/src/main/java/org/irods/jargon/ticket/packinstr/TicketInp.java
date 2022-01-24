package org.irods.jargon.ticket.packinstr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction;
import org.irods.jargon.core.packinstr.KeyValuePair;
import org.irods.jargon.core.packinstr.Tag;

/**
 * Base packing instruction for interacting with the ticket subsystem in iRODS.
 * Note that this contains the necessary support for the get and put operations.
 * The jargon-ticket project has extended support for ticket administration.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class TicketInp extends AbstractIRODSPackingInstruction {

	protected static final String PI_TAG = "ticketAdminInp_PI";

	/**
	 * Protocol API identifier for gen admin operations
	 */
	public static final int TICKET_ADMIN_INP_API_NBR = 723;

	protected static final String ARG1 = "arg1";
	protected static final String ARG2 = "arg2";
	protected static final String ARG3 = "arg3";
	protected static final String ARG4 = "arg4";
	protected static final String ARG5 = "arg5";
	protected static final String ARG6 = "arg6";
	protected static final String BLANK = "";
	protected static final Pattern MODIFY_DATE_FORMAT = Pattern.compile(
			"^(20|21|22)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])[\\. /.]([01][0-9]||2[0123])[: /.]([0-5][0-9])[: /.]([0-5][0-9])$");

	protected String arg1 = "";
	protected String arg2 = "";
	protected String arg3 = "";
	protected String arg4 = "";
	protected String arg5 = "";
	protected String arg6 = "";

	/**
	 * Create an instance of the packing instruction suitable for setting the
	 * session with the given ticket. This session initialization is done before get
	 * and put operations in iRODS, and will typically be done on behalf of a caller
	 * accomplishing a transfer when a ticket is provided.
	 *
	 * @param ticketString {@code String} (required) with a valid ticket
	 * @return {@code TicketInp} instance suitable for initializing a ticket session
	 */
	public static TicketInp instanceForSetSessionWithTicket(final String ticketString) {
		if (ticketString == null || ticketString.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticketString");
		}
		return new TicketInp(TICKET_ADMIN_INP_API_NBR, "session", ticketString, BLANK, BLANK, BLANK, BLANK);
	}

	/**
	 * Private constructor for TicketAdminInp, use the instance() methods to create
	 * per command
	 *
	 *
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @param arg6
	 */
	protected TicketInp(final int apiNbr, final String arg1, final String arg2, final String arg3, final String arg4,
			final String arg5, final String arg6) {

		this.arg1 = arg1;
		this.arg2 = arg2;
		this.arg3 = arg3;
		this.arg4 = arg4;
		this.arg5 = arg5;
		this.arg6 = arg6;
		setApiNumber(apiNbr);
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
		// FIXME: refactor for kvps
		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();
		Tag message = new Tag(PI_TAG, new Tag[] { new Tag(ARG1, arg1), new Tag(ARG2, arg2), new Tag(ARG3, arg3),
				new Tag(ARG4, arg4), new Tag(ARG5, arg5), new Tag(ARG6, arg6) });
		message.addTag(createKeyValueTag(kvps));

		return message;
	}
}
