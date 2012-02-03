package org.irods.jargon.ticket.packinstr;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction;
import org.irods.jargon.core.packinstr.Tag;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
	private static final Pattern MODE = Pattern.compile("read|write");
	private static final Pattern MODIFY_ACTION = Pattern.compile("uses|expire|write-file|write-byte");
	private static final Pattern MODIFY_ADD_REM_ACTION = Pattern.compile("add|remove");
	private static final Pattern MODIFY_OBJECT_TYPE = Pattern.compile("user|group|host");
	private static final Pattern MODIFY_DATE_FORMAT = Pattern.compile(
			"^(20|21|22)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])[\\. /.]([01][0-9]||2[0123])[: /.]([0-5][0-9])[: /.]([0-5][0-9])$");

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
	
	public static TicketAdminInp instanceForCreate(final String mode, String fullPath, String ticketId) {
		String id = BLANK;
		
		if (mode == null || mode.isEmpty()) {
			throw new IllegalArgumentException("null or empty permission mode");
		}
		Matcher matcher = MODE.matcher(mode);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("illegal permission mode");
		}
		if (fullPath == null || (fullPath.isEmpty())) {
			throw new IllegalArgumentException("null or empty full path name");
		}
		// ticketId is optional?
		if ((ticketId != null) && (!ticketId.isEmpty())) {
			id = ticketId;
		}
		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "create", mode,
				fullPath, id, BLANK, BLANK);
	}
	// TODO: create another method for create with no ticketId param? public static TicketAdminInp instanceForCreate(final String mode, String fullPath)
	
	public static TicketAdminInp instanceForList(final String ticketId) {
		String id = BLANK;
		//ticketId is optional??
		if ((ticketId != null) && (!ticketId.isEmpty())) {
			id = ticketId;
		}
		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "list", id,
					BLANK, BLANK, BLANK, BLANK);
	}
	// TODO: create another method for list with no param? public static TicketAdminInp instanceForList()
	
	public static TicketAdminInp instanceForListAll() {

		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "list-all", BLANK,
				BLANK, BLANK, BLANK, BLANK);
	}
	
	public static TicketAdminInp instanceForModify(final String ticketId, String action,
			String objectTypeOrInt, String modObject) {
		
		String obj = BLANK;
		Matcher matcher = null;
		Integer theInt = 0;
		
		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		
		// check and see if action is set
		if (action == null || action.isEmpty()) {
			throw new IllegalArgumentException("null or empty modify action");
		}
		
		// see if this is an add or remove
		if (MODIFY_ADD_REM_ACTION.matcher(action).matches()) {
			
			if (objectTypeOrInt == null || objectTypeOrInt.isEmpty()) {
				throw new IllegalArgumentException("null or empty user, group, or host");
			}
			
			if (!MODIFY_OBJECT_TYPE.matcher(objectTypeOrInt).matches()){
				throw new IllegalArgumentException("must choose user, group, or host for ticket mod add/remove");
			}
			
			if (modObject == null || modObject.isEmpty()) {
				throw new IllegalArgumentException("null or empty user, group, or host");
			}
			obj = modObject;
		}
		
		// else check for other type of actions
		else
		if (MODIFY_ACTION.matcher(action).matches()) {
			
			if (action.equals("expire")) {
				// check to make sure objectTypeOrInt is set
				if (objectTypeOrInt == null || objectTypeOrInt.isEmpty()) {
					throw new IllegalArgumentException("null or empty expire date");
				}
				// check date format
				if (!MODIFY_DATE_FORMAT.matcher(objectTypeOrInt).matches()) {
					throw new IllegalArgumentException("illegal expire date");
				}
			}
			// else this action is uses, write-file or write-byte
			else {
				try {
					theInt = Integer.parseInt(objectTypeOrInt);
				}
				catch(NumberFormatException ex) {
					throw new IllegalArgumentException("illegal integer for uses, write-file, or write-byte");
				}
				if (theInt < 0) {
					throw new IllegalArgumentException("illegal integer for uses, write-file, or write-byte");
				}
			}
		}
		
		// else this is an illegal action
		else {
			throw new IllegalArgumentException(
					"illegal modify action - use add, remove, uses, expire, write-file, or write-byte");
		}
		
		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", action,
				objectTypeOrInt, obj, BLANK, BLANK);
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
				new Tag(ARG5, arg5), new Tag(ARG6, arg6) });

		return message;
	}

}
