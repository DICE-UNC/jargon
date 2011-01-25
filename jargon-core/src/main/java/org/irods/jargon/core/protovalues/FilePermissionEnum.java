package org.irods.jargon.core.protovalues;

/**
 * Values for file permission
 * @author Mike Conway - DICE (www.irods.org)
 */
public enum FilePermissionEnum {

	OWN(1200), WRITE(1120), READ(1050), NONE(-1);

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
}
