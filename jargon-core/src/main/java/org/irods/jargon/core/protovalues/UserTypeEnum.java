package org.irods.jargon.core.protovalues;

import java.util.ArrayList;
import java.util.List;

/**
 * Possible header values for irods user type
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public enum UserTypeEnum {

	RODS_USER("rodsuser"), GROUP_ADMIN("groupadmin"), RODS_ADMIN("rodsadmin"), RODS_GROUP("rodsgroup"), RODS_UNKNOWN(
			"unknown");

	private String textValue;

	UserTypeEnum(final String textValue) {
		this.textValue = textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public static List<String> getUserTypeList() {

		List<String> userTypes = new ArrayList<String>();
		for (UserTypeEnum userTypeEnum : UserTypeEnum.values()) {
			userTypes.add(userTypeEnum.textValue);
		}
		return userTypes;
	}

	public static UserTypeEnum findTypeByString(final String userType) {
		UserTypeEnum userTypeEnumValue = null;
		for (UserTypeEnum userTypeEnum : UserTypeEnum.values()) {
			if (userTypeEnum.getTextValue().equals(userType)) {
				userTypeEnumValue = userTypeEnum;
				break;
			}
		}
		if (userTypeEnumValue == null) {
			userTypeEnumValue = UserTypeEnum.RODS_UNKNOWN;
		}
		return userTypeEnumValue;

	}
}
