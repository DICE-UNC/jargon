/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.irods.jargon.core.protovalues;

/**
 * Values for serverType in MiscSvrInfo_PI
 *
 * #define RCAT_NOT_ENABLED 0 #define RCAT_ENABLED 1
 *
 * @author toaster
 */
public enum ServerTypeEnum {

	RCAT_NOT_ENABLED(0), RCAT_ENABLED(1);

	ServerTypeEnum(final int i) {
	}

	public static ServerTypeEnum valueOf(final int i) {
		switch (i) {
		case 0:
			return RCAT_NOT_ENABLED;
		case 1:
			return RCAT_ENABLED;
		default:
			throw new IllegalArgumentException("No server type for value: " + i);
		}
	}
}
