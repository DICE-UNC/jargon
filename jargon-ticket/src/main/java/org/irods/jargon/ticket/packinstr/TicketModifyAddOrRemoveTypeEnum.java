package org.irods.jargon.ticket.packinstr;

import java.util.ArrayList;
import java.util.List;

public enum TicketModifyAddOrRemoveTypeEnum {

	TICKET_MODIFY_USER("user"), TICKET_MODIFY_GROUP("group"), TICKET_MODIFY_HOST("host"), TICKET_MODIFY_UNKNOWN(
			"unknown");

	private String textValue;

	TicketModifyAddOrRemoveTypeEnum(final String textValue) {
		this.textValue = textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public static List<String> getTicketModifyAddOrRemoveTypeList() {

		List<String> types = new ArrayList<String>();
		for (TicketModifyAddOrRemoveTypeEnum ticketModifyAddOrRemoveTypeEnum : TicketModifyAddOrRemoveTypeEnum
				.values()) {
			types.add(ticketModifyAddOrRemoveTypeEnum.textValue);
		}
		return types;
	}

	public static TicketModifyAddOrRemoveTypeEnum findTypeByString(final String type) {
		TicketModifyAddOrRemoveTypeEnum ticketModifyAddOrRemoveTypeEnum = null;
		for (TicketModifyAddOrRemoveTypeEnum modifyType : TicketModifyAddOrRemoveTypeEnum.values()) {
			if (modifyType.getTextValue().equals(type)) {
				ticketModifyAddOrRemoveTypeEnum = modifyType;
				break;
			}
		}
		if (ticketModifyAddOrRemoveTypeEnum == null) {
			ticketModifyAddOrRemoveTypeEnum = TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_UNKNOWN;
		}
		return ticketModifyAddOrRemoveTypeEnum;

	}
}