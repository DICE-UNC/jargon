package org.irods.jargon.ticket.packinstr;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction;
import org.irods.jargon.core.packinstr.Tag;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	
	
	public static TicketAdminInp instanceForCreate(final TicketCreateModeEnum mode, String fullPath, String ticketId) {
		String id = BLANK;
		
		if (mode == null) {
			throw new IllegalArgumentException("null permission mode");
		}
		if (fullPath == null || (fullPath.isEmpty())) {
			throw new IllegalArgumentException("null or empty full path name");
		}	
		// ticketId is optional?
		if ((ticketId != null) && (!ticketId.isEmpty())) {
			id = ticketId;
		}
		
		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "create", mode.getTextValue(),
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
	
	
	public static TicketAdminInp instanceForModifyAddAccess(final String ticketId, 
			TicketModifyAddOrRemoveTypeEnum addTypeEnum, String modObject) {
		
		
		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		// check and see if add type is set
		if (addTypeEnum == null) {
			throw new IllegalArgumentException("null modify add permission type not set");
		}
		// check and see if action is set
		if (modObject == null || modObject.isEmpty()) {
			throw new IllegalArgumentException("null or empty modify add - user, group, or host");
		}
		
		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId,
				"add", addTypeEnum.getTextValue(), modObject, BLANK);
	}
	
	public static TicketAdminInp instanceForModifyRemoveAccess(final String ticketId, 
			TicketModifyAddOrRemoveTypeEnum addTypeEnum, String modObject) {
		
		
		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		// check and see if add type is set
		if (addTypeEnum == null) {
			throw new IllegalArgumentException("null modify remove permission type not set");
		}
		// check and see if action is set
		if (modObject == null || modObject.isEmpty()) {
			throw new IllegalArgumentException("null or empty modify remove - user, group, or host");
		}
		
		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId,
				"remove", addTypeEnum.getTextValue(), modObject, BLANK);
	}
	
	public static TicketAdminInp instanceForModifyNumberOfUses(final String ticketId, Integer numberOfUses) {
		
		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		
		if (numberOfUses < 0) {
			throw new IllegalArgumentException("illegal integer for uses - must be 0 or greater");
		}


		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId,
				"uses", numberOfUses.toString(), BLANK, BLANK);
		
	}
	
	public static TicketAdminInp instanceForModifyFileWriteNumber(final String ticketId, Integer numberOfFileWrites) {
		
		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		
		if (numberOfFileWrites < 0) {
			throw new IllegalArgumentException("illegal integer for write-file - must be 0 or greater");
		}


		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId,
				"write-file", numberOfFileWrites.toString(), BLANK, BLANK);
		
	}
	
	public static TicketAdminInp instanceForModifyByteWriteNumber(final String ticketId, Integer numberOfByteWrites) {
		
		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		
		if (numberOfByteWrites < 0) {
			throw new IllegalArgumentException("illegal integer for write-byte - must be 0 or greater");
		}


		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId,
				"write-byte", numberOfByteWrites.toString(), BLANK, BLANK);
		
	}
	
	public static TicketAdminInp instanceForModifyExpiration(final String ticketId, String expirationDate) {
		
		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		
		if (expirationDate == null || expirationDate.isEmpty()) {
			throw new IllegalArgumentException("null or empty expiration date");
		}

		// check date format
		if (!MODIFY_DATE_FORMAT.matcher(expirationDate).matches()) {
			throw new IllegalArgumentException("illegal expiration date");
		}

		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId,
				"expire", expirationDate, BLANK, BLANK);
		
	}
	
	public static TicketAdminInp instanceForModifyExpiration(final String ticketId, Date expirationDate) {
		
		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		
		if (expirationDate == null) {
			throw new IllegalArgumentException("null expiration date");
		}
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
		String formattedDate = df.format(expirationDate);
		
		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId,
				"expire", formattedDate, BLANK, BLANK);
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
