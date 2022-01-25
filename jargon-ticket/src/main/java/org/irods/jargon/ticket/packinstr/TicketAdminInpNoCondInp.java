package org.irods.jargon.ticket.packinstr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.Tag;

/**
 * Packing instruction for admin functions for the ticket subsystem in iRODS.
 * These functions mirror the packing instructions in the iticket icommand.This
 * packing instruction is good for iRODS prior to 4.2.11, after which a CondInp
 * was added for adding an admin flag.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class TicketAdminInpNoCondInp extends TicketInp {

	public static TicketAdminInpNoCondInp instanceForDelete(final String ticketId) {
		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}
		return new TicketAdminInpNoCondInp(TICKET_ADMIN_INP_API_NBR, "delete", ticketId, BLANK, BLANK, BLANK, BLANK);
	}

	public static TicketAdminInpNoCondInp instanceForCreate(final TicketCreateModeEnum mode, final String fullPath,
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

		return new TicketAdminInpNoCondInp(TICKET_ADMIN_INP_API_NBR, "create", ticketId, mode.getTextValue(), fullPath,
				BLANK, BLANK);
	}

	// TODO: create another method for create with no ticketId param? public
	// static TicketAdminInp instanceForCreate(final String mode, String
	// fullPath)

	public static TicketAdminInpNoCondInp instanceForList(final String ticketId) {
		String id = BLANK;

		// ticketId is optional??
		if ((ticketId != null) && (!ticketId.isEmpty())) {
			id = ticketId;
		}

		return new TicketAdminInpNoCondInp(TICKET_ADMIN_INP_API_NBR, "list", id, BLANK, BLANK, BLANK, BLANK);
	}

	// TODO: create another method for list with no param? public static
	// TicketAdminInp instanceForList()

	public static TicketAdminInpNoCondInp instanceForListAll() {

		return new TicketAdminInpNoCondInp(TICKET_ADMIN_INP_API_NBR, "list-all", BLANK, BLANK, BLANK, BLANK, BLANK);
	}

	public static TicketAdminInpNoCondInp instanceForModifyAddAccess(final String ticketId,
			final TicketModifyAddOrRemoveTypeEnum addTypeEnum, final String modObject) {

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

		return new TicketAdminInpNoCondInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId, "add", addTypeEnum.getTextValue(),
				modObject, BLANK);
	}

	public static TicketAdminInpNoCondInp instanceForModifyRemoveAccess(final String ticketId,
			final TicketModifyAddOrRemoveTypeEnum addTypeEnum, final String modObject) {

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

		return new TicketAdminInpNoCondInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId, "remove",
				addTypeEnum.getTextValue(), modObject, BLANK);
	}

	public static TicketAdminInpNoCondInp instanceForModifyNumberOfUses(final String ticketId,
			final Integer numberOfUses) {

		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}

		if (numberOfUses < 0) {
			throw new IllegalArgumentException("illegal integer for uses - must be 0 or greater");
		}

		return new TicketAdminInpNoCondInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId, "uses", numberOfUses.toString(),
				BLANK, BLANK);

	}

	public static TicketAdminInpNoCondInp instanceForModifyFileWriteNumber(final String ticketId,
			final Integer numberOfFileWrites) {

		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}

		if (numberOfFileWrites < 0) {
			throw new IllegalArgumentException("illegal integer for write-file - must be 0 or greater");
		}

		return new TicketAdminInpNoCondInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId, "write-file",
				numberOfFileWrites.toString(), BLANK, BLANK);

	}

	public static TicketAdminInpNoCondInp instanceForModifyByteWriteNumber(final String ticketId,
			final long byteWriteLimit) {

		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}

		if (byteWriteLimit < 0) {
			throw new IllegalArgumentException("illegal integer for write-byte - must be 0 or greater");
		}

		return new TicketAdminInpNoCondInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId, "write-bytes",
				String.valueOf(byteWriteLimit), BLANK, BLANK);

	}

	public static TicketAdminInpNoCondInp instanceForModifyExpiration(final String ticketId,
			final String expirationDate) {

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

		return new TicketAdminInpNoCondInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId, "expire", expirationDate, BLANK,
				BLANK);

	}

	/**
	 * Create a packing instruction to modify the expiration date. Setting the date
	 * to {@code null} removes the expiration
	 *
	 * @param ticketId       {@code String} with the unique ticket string
	 * @param expirationDate {@code Date} or {@code null} to remove
	 * @return {@link TicketAdminInpNoCondInp}
	 */
	public static TicketAdminInpNoCondInp instanceForModifyExpiration(final String ticketId,
			final Date expirationDate) {

		if (ticketId == null || ticketId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticket id");
		}

		String formattedDate = "";

		if (expirationDate != null) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
			formattedDate = df.format(expirationDate);
		}

		return new TicketAdminInpNoCondInp(TICKET_ADMIN_INP_API_NBR, "mod", ticketId, "expire", formattedDate, BLANK,
				BLANK);
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
	private TicketAdminInpNoCondInp(final int apiNbr, final String arg1, final String arg2, final String arg3,
			final String arg4, final String arg5, final String arg6) {

		super(apiNbr, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	@Override
	public Tag getTagValue() throws JargonException {
		// FIXME: refactor for kvps
		Tag message = new Tag(PI_TAG, new Tag[] { new Tag(ARG1, arg1), new Tag(ARG2, arg2), new Tag(ARG3, arg3),
				new Tag(ARG4, arg4), new Tag(ARG5, arg5), new Tag(ARG6, arg6) });
		return message;
	}

}
