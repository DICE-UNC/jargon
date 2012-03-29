package org.irods.jargon.ticket.packinstr;

import java.util.ArrayList;
import java.util.List;

public enum TicketCreateModeEnum {

	TICKET_CREATE_READ("read"), TICKET_CREATE_WRITE("write"), TICKET_CREATE_UNKNOWN(
			"unknown");

	private String textValue;

	TicketCreateModeEnum(final String textValue) {
		this.textValue = textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public static List<String> getTicketModifyAddOrRemoveTypeList() {

		List<String> types = new ArrayList<String>();
		for (TicketCreateModeEnum ticketCreateMode : TicketCreateModeEnum
				.values()) {
			types.add(ticketCreateMode.textValue);
		}
		return types;
	}

	public static TicketCreateModeEnum findTypeByString(final String type) {
		TicketCreateModeEnum ticketCreateMode = null;
		for (TicketCreateModeEnum createType : TicketCreateModeEnum.values()) {
			if (createType.getTextValue().equals(type)) {
				ticketCreateMode = createType;
				break;
			}
		}
		if (ticketCreateMode == null) {
			ticketCreateMode = TicketCreateModeEnum.TICKET_CREATE_UNKNOWN;
		}
		return ticketCreateMode;

	}
}