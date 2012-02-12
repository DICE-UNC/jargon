package org.irods.jargon.ticket;

import org.irods.jargon.core.exception.JargonException;

public enum TicketGenQueryEnum {
	
	COL_TICKET_ID("TICKET_ID", 2200),
	COL_TICKET_STRING("TICKET_STRING", 2201),
	COL_TICKET_TYPE("TICKET_TYPE", 2202),
	COL_TICKET_USER_ID("TICKET_USER_ID", 2203),
	COL_TICKET_OBJECT_ID("TICKET_OBJECT_ID", 2204),
	COL_TICKET_OBJECT_TYPE("TICKET_OBJECT_TYPE", 2205),
	COL_TICKET_USES_LIMIT("TICKET_USES_LIMIT", 2206),
	COL_TICKET_USES_COUNT("TICKET_USES_COUNT", 2207),
	COL_TICKET_EXPIRY_TS("TICKET_EXPIRY_TS", 2208),
	COL_TICKET_CREATE_TIME("TICKET_CREATE_TIME", 2209),
	COL_TICKET_MODIFY_TIME("TICKET_MODIFY_TIME", 2210),
	COL_TICKET_WRITE_FILE_COUNT("TICKET_WRITE_FILE_COUNT", 2211),
	COL_TICKET_WRITE_FILE_LIMIT("TICKET_WRITE_FILE_LIMIT", 2212),
	COL_TICKET_WRITE_BYTE_COUNT("TICKET_WRITE_BYTE_COUNT", 2213),
	COL_TICKET_WRITE_BYTE_LIMIT("TICKET_WRITE_BYTE_LIMIT", 2214),
	COL_TICKET_ALLOWED_HOST_TICKET_ID("TICKET_ALLOWED_HOST_TICKET_ID", 2220),
	COL_TICKET_ALLOWED_HOST("TICKET_ALLOWED_HOST", 2221),
	COL_TICKET_ALLOWED_USER_TICKET_ID("TICKET_ALLOWED_USER_TICKET_ID", 2222),
	COL_TICKET_ALLOWED_USER_NAME("TICKET_ALLOWED_USER_NAME", 2223),
	COL_TICKET_ALLOWED_GROUP_TICKET_ID("TICKET_ALLOWED_GROUP_TICKET_ID", 2224),
	COL_TICKET_ALLOWED_GROUP_NAME("TICKET_ALLOWED_GROUP_NAME", 2225),
	COL_TICKET_DATA_NAME("TICKET_DATA_NAME", 2226),
	COL_TICKET_DATA_COLL_NAME("TICKET_DATA_COLL_NAME", 2227),
	COL_TICKET_COLL_NAME("TICKET_COLL_NAME", 2228),
	COL_TICKET_OWNER_NAME("TICKET_OWNER_NAME", 2229),
	COL_TICKET_OWNER_ZONE("TICKET_OWNER_ZONE", 2230);
	
	private String name;
	private int numericValue;

	TicketGenQueryEnum(final String name, final int numericValue) {
		this.name = name;
		this.numericValue = numericValue;
	}
	
	static int getNumericFromStringValue(final String stringValue) {
		int queryVal = -1;
		for (TicketGenQueryEnum ticketGenQueryEnum : TicketGenQueryEnum.values()) {
			if (ticketGenQueryEnum.getName().equals(stringValue)) {
				queryVal = ticketGenQueryEnum.getNumericValue();
				break;
			}
		}
		return queryVal;
	}

	static TicketGenQueryEnum getAttributeBasedOnId(
			final String attribNumericValue) throws JargonException {
		if (attribNumericValue == null) {
			throw new JargonException("null attribute value");
		}

		int attribAsInt = 0;
		TicketGenQueryEnum returnTicketGenQueryEnum = null;

		try {
			attribAsInt = Integer.parseInt(attribNumericValue);
		} catch (NumberFormatException e) {
			throw new JargonException(
					"unable to translate attrib value to an int.  Given value ="
							+ attribNumericValue);
		}

		for (TicketGenQueryEnum ticketGenQueryEnum : TicketGenQueryEnum.values()) {
			if (ticketGenQueryEnum.getNumericValue() == attribAsInt) {
				returnTicketGenQueryEnum = ticketGenQueryEnum;
				break;
			}
		}

		// this cannot happen, but still check
		if (returnTicketGenQueryEnum == null) {
			throw new JargonException(
					"logic error - derived a null value for the TicketGenQuery");
		}

		return returnTicketGenQueryEnum;

	}

	/**
	 * Given a String column name, translate to the specific pre-defined field
	 * value. If there is no match, null will be returned.
	 * 
	 * @param attribName
	 *            <code>String</code> that contains the attibuteName to look up
	 * @return {@link org.irods.jargon.ticket.TicketGenQueryEnum
	 *         TicketGenQueryEnum} that gives information about the query field
	 * @throws JargonException
	 */
	static TicketGenQueryEnum getAttributeBasedOnName(final String attribName)
			throws JargonException {
		if (attribName == null || attribName.length() == 0) {
			throw new JargonException("missing attribute name");
		}

		TicketGenQueryEnum returnTicketGenQueryEnum = null;

		for (TicketGenQueryEnum ticketGenQueryEnum : TicketGenQueryEnum.values()) {
			if (ticketGenQueryEnum.getName().equals(attribName)) {
				returnTicketGenQueryEnum = ticketGenQueryEnum;
				break;
			}
		}
		return returnTicketGenQueryEnum;

	}

	public String getName() {
		return name;
	}

	public int getNumericValue() {
		return numericValue;
	}

}
