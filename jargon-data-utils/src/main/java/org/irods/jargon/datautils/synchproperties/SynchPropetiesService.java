package org.irods.jargon.datautils.synchproperties;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;

public interface SynchPropetiesService {

	public static final String USER_SYNCH_DIR_TAG = "iRODSSynch:userSynchDir";
	public static final String BAR = "|";

	/**
	 * Get the information regarding the state of synchronization between iRODS and a local file system for a given user and device.
	 * @param userName <code>String</code> with the iRODS user name for the synch
	 * @param deviceName <code>String</code> with the given name for the device (laptop, desktop, etc)
	 * @param irodsAbsolutePath <code>String</code> with the absolute path to the iRODS collection that is the root of the synchronization
	 * @return {@link UserSynchTarget} describing the synchronization status for the user/target name/iRODS absolute path
	 * @throws DataNotFoundException
	 * @throws JargonException
	 */
	UserSynchTarget getUserSynchTargetForUserAndAbsolutePath(
			final String userName, final String deviceName,
			final String irodsAbsolutePath) throws DataNotFoundException,
			JargonException;

}