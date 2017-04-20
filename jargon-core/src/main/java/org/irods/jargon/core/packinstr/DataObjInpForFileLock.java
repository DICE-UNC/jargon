/**
 * #define DataObjInp_PI "str objPath[MAX_NAME_LEN]; int createMode; int openFlags; double offset; double dataSize; int numThreads; int oprType; struct *SpecColl_PI; struct KeyValPair_PI;"
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Translation of a DataObjInp operation to obtain or release a file lock
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 *         getutil read lock replutil
 *
 *         pututil write lock
 *
 *
 *
 *         rsDataObjLock used by
 *
 *         rsDataObjCreate rsDataObjOpen rsDataObjRepl
 *
 */
public class DataObjInpForFileLock extends AbstractIRODSPackingInstruction {

	public static final int LOCK_API_NBR = 699;

	public enum LockType {
		READ_LOCK, WRITE_LOCK, UNLOCK
	}

	public enum LockCommandType {
		SET_LOCK, SET_LOCK_WAIT, GET_LOCK
	}

	public static final String READ_LOCK_TYPE = "readLockType";
	public static final String WRITE_LOCK_TYPE = "writeLockType";
	public static final String UNLOCK_TYPE = "unlockType";

	public static final String SET_LOCK_CMD = "setLockCmd";
	public static final String SET_LOCK_WAIT_CMD = "setLockWaitCmd";
	public static final String GET_LOCK_CMD = "getLockCmd";

	public static final String LOCK_TYPE_KW = "lockType";
	public static final String LOCK_CMD_KW = "lockCmd";
	public static final String LOCK_FD_KW = "lockFd";

	private String fileAbsolutePath = "";
	private LockType lockType = LockType.READ_LOCK;
	private LockCommandType lockCommandType = LockCommandType.SET_LOCK;
	private int fd = -1;

	/**
	 * Create the DataObjInp packing instruction to get an object stat.
	 *
	 * @param fileAbsolutePath
	 *            <code>String</code> with the file absolute path.
	 * @param lockType
	 *            {@link LockCommandType}
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInpForFileLock instance(
			final String fileAbsolutePath, final LockType lockType,
			final LockCommandType lockCommandType) throws JargonException {
		return new DataObjInpForFileLock(fileAbsolutePath, lockType,
				lockCommandType, -1);
	}

	private DataObjInpForFileLock(final String fileAbsolutePath,
			final LockType lockType, final LockCommandType lockCommandType,
			final int fd) throws JargonException {

		super();

		if (fileAbsolutePath == null || fileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileAbsolutePath");
		}

		if (lockType == null) {
			throw new IllegalArgumentException("null lockType");
		}

		if (lockCommandType == null) {
			throw new IllegalArgumentException("null lockCommandType");
		}

		this.fileAbsolutePath = fileAbsolutePath;
		this.lockType = lockType;
		this.lockCommandType = lockCommandType;
		this.fd = fd;
		setApiNumber(LOCK_API_NBR);
	}

	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(DataObjInp.PI_TAG, new Tag[] {
				new Tag(DataObjInp.OBJ_PATH, getFileAbsolutePath()),
				new Tag(DataObjInp.CREATE_MODE, 0),
				new Tag(DataObjInp.OPEN_FLAGS, 0),
				new Tag(DataObjInp.OFFSET, 0),
				new Tag(DataObjInp.DATA_SIZE, 0),
				new Tag(DataObjInp.NUM_THREADS, 0),
				new Tag(DataObjInp.OPR_TYPE, 0) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();
		String lockTypeVal;
		switch (lockType) {
		case WRITE_LOCK:
			lockTypeVal = WRITE_LOCK_TYPE;
			break;
		case READ_LOCK:
			lockTypeVal = READ_LOCK_TYPE;
			break;
		case UNLOCK:
			lockTypeVal = UNLOCK_TYPE;
			break;
		default:
			throw new IllegalArgumentException("unkown lock type");
		}

		kvps.add(KeyValuePair.instance(LOCK_TYPE_KW, lockTypeVal));

		String lockCommandVal;
		switch (lockCommandType) {
		case SET_LOCK:
			lockCommandVal = SET_LOCK_CMD;
			break;
		case SET_LOCK_WAIT:
			lockCommandVal = SET_LOCK_WAIT_CMD;
			break;
		case GET_LOCK:
			lockCommandVal = GET_LOCK_CMD;
			break;
		default:
			throw new IllegalArgumentException("invalid lockCommandType");
		}

		kvps.add(KeyValuePair.instance(LOCK_CMD_KW, lockCommandVal));

		// lockFD for unlock only

		message.addTag(createKeyValueTag(kvps));
		return message;
	}

	public String getFileAbsolutePath() {
		return fileAbsolutePath;
	}

	/**
	 * @return the lockType
	 */
	public LockType getLockType() {
		return lockType;
	}

	/**
	 * @return the lockCommandType
	 */
	public LockCommandType getLockCommandType() {
		return lockCommandType;
	}

	/**
	 * @return the fd
	 */
	public int getFd() {
		return fd;
	}

}
