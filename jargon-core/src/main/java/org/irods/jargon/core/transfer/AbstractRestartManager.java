/**
 * 
 */
package org.irods.jargon.core.transfer;


/**
 * Abstract superclass for a manager of file restarts. This allows in-memory,
 * file based, and other variants to maintain long file restart information.
 * <p/>
 * This manager serves as a repository of file restart info and a place to
 * update that information, maintaining a representation of
 * {@link FileRestartInfo} for a given path
 * 
 * @author Mike Conway - DICE
 *
 */
public abstract class AbstractRestartManager {

	/**
	 * Store the restart information
	 * 
	 * @param fileRestartInfo
	 *            {@link FileRestartInfo} to be stored
	 * @return {@link FileRestartInfoIdentifier} that is the derived key from
	 *         the restart info
	 * @throws FileRestartManagementException
	 */
	public abstract FileRestartInfoIdentifier storeRestart(
			final FileRestartInfo fileRestartInfo)
			throws FileRestartManagementException;

	/**
	 * Delete the file restart information
	 * 
	 * @param fileRestartInfoIdentifier
	 * @return {@link FileRestartInfoIdentifier} that is the key (oper type,
	 *         account info, path)
	 * @throws FileRestartManagementException
	 */
	public abstract void deleteRestart(
			final FileRestartInfoIdentifier fileRestartInfoIdentifier)
			throws FileRestartManagementException;

	/**
	 * Retrieve the file restart information given the identifying information
	 * 
	 * @param fileRestartInfoIdentifier
	 *            {@link FileRestartInfoIdentifier} that is the key (oper type,
	 *            account info, path)
	 * @return {@link FileRestartInfo} that matches the key or <code>null</code>
	 *         if no match
	 * @throws FileRestartManagementException
	 * 
	 */
	public abstract FileRestartInfo retrieveRestart(
			final FileRestartInfoIdentifier fileRestartInfoIdentifier)
			throws FileRestartManagementException;

}
