/**
 * 
 */
package org.irods.jargon.core.connection;

/**
 * Defines an interface for a listener that will receive low-level callbacks of
 * status from the connection. Examples of callbacks include instantaneous bytes
 * sent.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface ConnectionProgressStatusListener {

	/**
	 * Method to receive a notify of the status of connection progress.
	 * 
	 * @param connectionProgressStatus
	 *            {@link ConnectionProgressStatus} with callback information
	 */
	void connectionProgressStatusCallback(
			final ConnectionProgressStatus connectionProgressStatus);

}
