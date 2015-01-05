/**
 * 
 */
package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.packinstr.DataObjInpForFileLock;
import org.irods.jargon.core.packinstr.DataObjInpForFileLock.LockCommandType;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.domain.FileLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for file locking semantics in iRODS. Provides methods to obtain and
 * release various locks on iRODS files.
 * 
 * @author Mike Conway - DICE
 *
 */
public class FileLockManagerAOImpl extends IRODSGenericAO implements
		FileLockManagerAO {

	private static Logger log = LoggerFactory
			.getLogger(FileLockManagerAOImpl.class);

	protected FileLockManagerAOImpl(IRODSSession irodsSession,
			IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	// TODO: how about wait? test exception for no file found

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.FileLockManagerAO#obtainFileLockWithoutWait
	 * (java.lang.String,
	 * org.irods.jargon.core.packinstr.DataObjInpForFileLock.LockType)
	 */
	@Override
	public FileLock obtainFileLockWithoutWait(final String irodsAbsolutePath,
			final DataObjInpForFileLock.LockType lockType)
			throws JargonException {
		log.info("obtainFileLock");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}
		if (lockType == null) {
			throw new IllegalArgumentException("null lockType");
		}
		log.info("obtain lock of type:{}", lockType);
		log.info("on file:{}", irodsAbsolutePath);

		DataObjInpForFileLock dataObjInp = DataObjInpForFileLock.instance(
				irodsAbsolutePath, lockType, LockCommandType.SET_LOCK);

		long currentTime = System.currentTimeMillis();

		Tag response = getIRODSProtocol().irodsFunction(dataObjInp);
		log.info("response:{}", response);
		Tag intVal = response.getTag("MsgHeader_PI").getTag("intInfo");
		if (intVal == null) {
			throw new JargonRuntimeException("no fd returned from lock call");
		}

		FileLock fileLock = new FileLock();
		fileLock.setApproximateSystemTimeWhenLockObtained(currentTime);
		fileLock.setFd(intVal.getIntValue());
		fileLock.setIrodsAbsolutePath(irodsAbsolutePath);
		fileLock.setLockType(lockType);
		log.info("lock obtained:{}", fileLock);

		return fileLock; // change to the proper fd
	}

}
