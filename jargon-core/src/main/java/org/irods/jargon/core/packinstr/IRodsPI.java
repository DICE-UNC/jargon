//FIXME: attribution
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 *
 * @author toaster
 */
public interface IRodsPI {

	public static final String MESSAGE_TAG = "msg";
	public static final String MSG_HEADER_PI_TAG = "MsgHeader_PI";

	int getApiNumber();

	String getParsedTags() throws JargonException;

}
