package org.irods.jargon.ticket.packinstr;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction;
import org.irods.jargon.core.packinstr.Tag;

/**
 * Packing instruction for admin functions for the ticket subsystem in iRODS.
 * These functions mirror the packing instructions in the iticket icommand.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TicketAdminInp extends AbstractIRODSPackingInstruction {

	private static final String PI_TAG = "ticketAdminInp_PI";

	/**
	 * Protocol API identifier for gen admin operations
	 */
	public static final int TICKET_ADMIN_INP_API_NBR = 723;

	private static final String ARG1 = "arg1";
	private static final String ARG2 = "arg2";
	private static final String ARG3 = "arg3";
	private static final String ARG4 = "arg4";
	private static final String ARG5 = "arg5";
	private static final String ARG6 = "arg6";
	private static final String BLANK = "";

	private String arg1 = "";
	private String arg2 = "";
	private String arg3 = "";
	private String arg4 = "";
	private String arg5 = "";
	private String arg6 = "";

	public static TicketAdminInp instanceForDelete(final String ticketId) {
		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "delete", ticketId,
				BLANK, BLANK, BLANK, BLANK);
	}

	/**
	 * Private constructor for TicketAdminInp, use the instance() methods to
	 * create per command
	 * 
	 * 
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @param arg6
	 */
	private TicketAdminInp(final int apiNbr, final String arg1,
			final String arg2, final String arg3, final String arg4,
			final String arg5, final String arg6) {

		this.arg1 = arg1;
		this.arg2 = arg2;
		this.arg3 = arg3;
		this.arg4 = arg4;
		this.arg5 = arg5;
		this.arg6 = arg6;
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
		Tag message = new Tag(PI_TAG, new Tag[] { new Tag(ARG1, arg1),
				new Tag(ARG2, arg2), new Tag(ARG3, arg3), new Tag(ARG4, arg4),
				new Tag(ARG4, arg4), new Tag(ARG5, arg5), new Tag(ARG6, arg6) });

		return message;
	}

}
