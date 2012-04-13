package org.irods.jargon.core.protovalues;

import java.util.ArrayList;
import java.util.List;

/**
 * Values for file permission
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public enum FilePermissionEnum {

	NONE(-1), NULL(1000), EXECUTE(1010), READ_ANNOTATION(1020), READ_SYSTEM_METADATA(
			1030), READ_METADATA(1040), READ(1050), WRITE_ANNOTATION(1060), CREATE_METADATA(
			1070), MODIFY_METADATA(1080), DELETE_METADATA(1090), ADMINISTER_OBJECT(
			1100), CREATE_OBJECT(1110), WRITE(1120), DELETE_OBJECT(1130), CREATE_TOKEN(
			1140), DELETE_TOKEN(1150), CURATE(1160), OWN(1200), ;

	FilePermissionEnum(final int i) {
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
			throw new IllegalArgumentException("No permission type for value: "
					+ i);
		}
	}

	/**
	 * Handy method to get all enum values as a list, good for building select
	 * boxes and the like
	 * 
	 * @return
	 */
	public static List<FilePermissionEnum> listAllValues() {
		List<FilePermissionEnum> allValues = new ArrayList<FilePermissionEnum>();
		FilePermissionEnum[] enumList = FilePermissionEnum.values();
		for (FilePermissionEnum enumVal : enumList) {
			allValues.add(enumVal);
		}
		return allValues;

	}
}
