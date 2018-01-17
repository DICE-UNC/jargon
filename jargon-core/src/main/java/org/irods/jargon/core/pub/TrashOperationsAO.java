package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * Handle trash operations for both users and admins
 * 
 * @author conwaymc
 *
 */
public interface TrashOperationsAO {

	/**
	 * Empty the trash can for the logged in user, with an optional (blank or null)
	 * zone. This defaults to a recursive operation to remove all trash
	 * 
	 * @param irodsZone
	 *            optional (<code>null</code> or blank) <code>String</code> with a
	 *            zone for which the trash will be emptied. defaults to the current
	 *            logged in zone
	 * @param trashOptions
	 *            {@link TrashOptions} that control details of the processing
	 * @throws JargonException
	 */
	void emptyTrashForLoggedInUser(String irodsZone, int age) throws JargonException;

	/**
	 * Get a handle to the top level of a user's trash
	 * 
	 * @return {@link IRODSFile} that is the top of the logged in user's trash
	 * @throws JargonException
	 */
	IRODSFile getTrashHomeForLoggedInUser() throws JargonException;

}