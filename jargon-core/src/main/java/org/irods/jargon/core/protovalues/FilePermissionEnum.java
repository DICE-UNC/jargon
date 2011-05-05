package org.irods.jargon.core.protovalues;

import java.util.ArrayList;
import java.util.List;

/**
 * Values for file permission
 * @author Mike Conway - DICE (www.irods.org)
 */
public enum FilePermissionEnum {

	NONE(-1), OWN(1200), WRITE(1120), READ(1050), ;

	FilePermissionEnum(final int i) {
	}

	public static FilePermissionEnum valueOf(final int i) {
		switch (i) {
		case -1:
			return NONE;
		case 1050:
			return READ;
		case 1120:
			return WRITE;
		case 1200:
			return OWN;
		default:
			throw new IllegalArgumentException("No permission type for value: " + i);
		}
	}
	
	/**
	 * Handy method to get all enum values as a list, good for building select boxes and the like
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
