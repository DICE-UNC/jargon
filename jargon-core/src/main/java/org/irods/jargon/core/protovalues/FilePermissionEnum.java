package org.irods.jargon.core.protovalues;

import java.util.ArrayList;
import java.util.List;

/**
 * Values for file permission
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public enum FilePermissionEnum {

	NONE(-1), NULL(1000), EXECUTE(1010), READ_ANNOTATION(1020), READ_SYSTEM_METADATA(1030), READ_METADATA(1040), READ(
			1050), WRITE_ANNOTATION(1060), CREATE_METADATA(1070), MODIFY_METADATA(1080), DELETE_METADATA(
					1090), ADMINISTER_OBJECT(1100), CREATE_OBJECT(1110), WRITE(1120), DELETE_OBJECT(
							1130), CREATE_TOKEN(1140), DELETE_TOKEN(1150), CURATE(1160), OWN(1200), MODIFY_OBJECT(1120), READ_OBJECT(1050);

	private int permissionNumericValue;

	FilePermissionEnum(final int i) {
		permissionNumericValue = i;
	}

	public static FilePermissionEnum valueOf(final int i) {
		switch (i) {
		case -1:
			return NONE;
		case 1000:
			return NULL;
		case 1010:
			return EXECUTE;
		case 1020:
			return READ_ANNOTATION;
		case 1030:
			return READ_SYSTEM_METADATA;
		case 1040:
			return READ_METADATA;
		case 1060:
			return WRITE_ANNOTATION;
		case 1050:
			return READ;
		case 1070:
			return CREATE_METADATA;
		case 1080:
			return MODIFY_METADATA;
		case 1090:
			return DELETE_METADATA;
		case 1100:
			return ADMINISTER_OBJECT;
		case 1110:
			return CREATE_OBJECT;
		case 1120:
			return WRITE;
		case 1130:
			return DELETE_OBJECT;
		case 1140:
			return CREATE_TOKEN;
		case 1150:
			return DELETE_TOKEN;
		case 1160:
			return CURATE;
		case 1200:
			return OWN;
		default:
			throw new IllegalArgumentException("No permission type for value: " + i);
		}
	}

	/**
	 * Translate a lower case text representation of a permission (as found in
	 * r_token_main)
	 * 
	 * @param textPermission
	 *            {@code String} with the permission token
	 * @return {@link FilePermissionEnum} value
	 */
	public static FilePermissionEnum enumValueFromSpecificQueryTextPermission(final String textPermission) {
		if (textPermission == null || textPermission.isEmpty()) {
			throw new IllegalArgumentException("null or empty textPermission");
		}

		if (textPermission.equals("own")) {
			return OWN;
		}

		if (textPermission.equals("read") || textPermission.equals("read object")
				|| textPermission.equals("read_object")) {
			return READ;
		}

		if (textPermission.equals("write") || textPermission.equals("modify object")
				|| textPermission.equals("modify_object")) {
			return WRITE;
		}

		if (textPermission.equals("create_metadata")) {
			return CREATE_METADATA;
		}

		if (textPermission.equals("modify_metadata")) {
			return MODIFY_METADATA;
		}

		if (textPermission.equals("delete_metadata")) {
			return DELETE_METADATA;
		}
		
		if (textPermission.equals("read_metadata")) {
			return READ_METADATA;
		}

		if (textPermission.equals("create_object")) {
			return CREATE_OBJECT;
		}

		if (textPermission.equals("delete_object")) {
			return DELETE_OBJECT;
		}

		if (textPermission.equals("null")) {
			return NULL;
		}

		if (textPermission.equals("none")) {
			return NONE;
		}

		return NONE;

	}

	/**
	 * Handy method to get all enum values as a list, good for building select boxes
	 * and the like
	 *
	 * @return {@code List<FilePermissionEnum>}
	 */
	public static List<FilePermissionEnum> listAllValues() {
		List<FilePermissionEnum> allValues = new ArrayList<FilePermissionEnum>();
		FilePermissionEnum[] enumList = FilePermissionEnum.values();
		for (FilePermissionEnum enumVal : enumList) {
			allValues.add(enumVal);
		}
		return allValues;

	}

	/**
	 * @return the permissionNumericValue {@code int} with the iRODS protocol value
	 *         for the permission
	 */
	public int getPermissionNumericValue() {
		return permissionNumericValue;
	}

	/**
	 * @param permissionNumericValue
	 *            {@code int} with the permissionNumericValue that is the iRODS
	 *            protocol representation
	 */
	public void setPermissionNumericValue(final int permissionNumericValue) {
		this.permissionNumericValue = permissionNumericValue;
	}
}
