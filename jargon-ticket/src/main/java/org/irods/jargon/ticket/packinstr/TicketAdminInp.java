package org.irods.jargon.ticket.packinstr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Packing instruction for admin functions for the ticket subsystem in iRODS.
 * These functions mirror the packing instructions in the iticket icommand.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TicketAdminInp extends TicketInp {

	public static TicketAdminInp instanceForDelete(final String ticketId) {
		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "delete", ticketId,
				BLANK, BLANK, BLANK, BLANK);
	}

	public static TicketAdminInp instanceForCreate(
			final TicketCreateModeEnum mode, final String fullPath,
			final String ticketId) {

		if (mode == null) {
			throw new IllegalArgumentException("null permission mode");
		}
		if (fullPath == null || (fullPath.isEmpty())) {
			throw new IllegalArgumentException("null or empty full path name");
		}
		// ticketId is not optional
		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("null or empty full path name");
		}

		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "create", ticketId,
				mode.getTextValue(), fullPath, BLANK, BLANK);
	}

	// TODO: create another method for create with no ticketId param? public
	// static TicketAdminInp instanceForCreate(final String mode, String
	// fullPath)

	public static TicketAdminInp instanceForList(final String ticketId) {
		String id = BLANK;

		// ticketId is optional??
		if ((ticketId != null) && (!ticketId.isEmpty())) {
			id = ticketId;
		}

		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "list", id, BLANK,
				BLANK, BLANK, BLANK);
	}

	// TODO: create another method for list with no param? public static
	// TicketAdminInp instanceForList()

	public static TicketAdminInp instanceForListAll() {

		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "list-all", BLANK,
				BLANK, BLANK, BLANK, BLANK);
	}

	public static TicketAdminInp instanceForModifyAddAccess(
			final String ticketId,
			final TicketModifyAddOrRemoveTypeEnum addTypeEnum,
			final String modObject) {

		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		// check and see if add type is set
		if (addTypeEnum == null) {
			throw new IllegalArgumentException(
					"null modify add permission type not set");
		}
		// check and see if action is set
		if (modObject == null || modObject.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty modify add - user, group, or host");
		}

		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId,
				"add", addTypeEnum.getTextValue(), modObject, BLANK);
	}

	public static TicketAdminInp instanceForModifyRemoveAccess(
			final String ticketId,
			final TicketModifyAddOrRemoveTypeEnum addTypeEnum,
			final String modObject) {

		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		// check and see if add type is set
		if (addTypeEnum == null) {
			throw new IllegalArgumentException(
					"null modify remove permission type not set");
		}
		// check and see if action is set
		if (modObject == null || modObject.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty modify remove - user, group, or host");
		}

		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId,
				"remove", addTypeEnum.getTextValue(), modObject, BLANK);
	}

	public static TicketAdminInp instanceForModifyNumberOfUses(
			final String ticketId, final Integer numberOfUses) {

		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}

		if (numberOfUses < 0) {
			throw new IllegalArgumentException(
					"illegal integer for uses - must be 0 or greater");
		}

		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId,
				"uses", numberOfUses.toString(), BLANK, BLANK);

	}

	public static TicketAdminInp instanceForModifyFileWriteNumber(
			final String ticketId, final Integer numberOfFileWrites) {

		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}

		if (numberOfFileWrites < 0) {
			throw new IllegalArgumentException(
					"illegal integer for write-file - must be 0 or greater");
		}

		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId,
				"write-file", numberOfFileWrites.toString(), BLANK, BLANK);

	}

	public static TicketAdminInp instanceForModifyByteWriteNumber(
			final String ticketId, final long byteWriteLimit) {

		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}

		if (byteWriteLimit < 0) {
			throw new IllegalArgumentException(
					"illegal integer for write-byte - must be 0 or greater");
		}

		return new TicketAdminInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId,
				"write-byte", String.valueOf(byteWriteLimit), BLANK, BLANK);

	}

	public static TicketAdminInp instanceForModifyExpiration(
			final String ticketId, final String expirationDate) {

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

	/**
	 * Create a packing instruction to modify the expiration date. Setting the
	 * date to <code>null</code> removes the expiration
	 * 
	 * @param ticketId
	 *            <code>String</code> with the unique ticket string
	 * @param expirationDate
	 *            <code>Date</code> or <code>null</code> to remove
	 * @return
	 */
	public static TicketAdminInp instanceForModifyExpiration(
			final String ticketId, final Date expirationDate) {

		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}

		String formattedDate = "";

		if (expirationDate != null) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
			formattedDate = df.format(expirationDate);
		}

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

		super(apiNbr, arg1, arg2, arg3, arg4, arg5, arg6);
	}

}
