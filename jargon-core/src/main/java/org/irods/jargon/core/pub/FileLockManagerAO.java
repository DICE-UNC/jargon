package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInpForFileLock;
import org.irods.jargon.core.pub.domain.FileLock;

/**
 * Interface for a manager that can manipulate file locks in iRODS
 * 
 * @author Mike Conway - DICE
 *
 */
public interface FileLockManagerAO {

	/**
	 * Obtain a lock on the given file without a wait
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS file
	 * @param lockType
	 *            {@link DataObjInpForFileLock.LockType} for read/write locs,
	 *            etc
	 * @return {@link FileLock} with info about the lock obtained
	 * @throws JargonException
	 */
	public abstract FileLock obtainFileLockWithoutWait(
			String irodsAbsolutePath, DataObjInpForFileLock.LockType lockType)
			throws JargonException;

}