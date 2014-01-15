/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.io.OutputStream;

import org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.OpenedDataObjInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.pub.IRODSGenericAO;
import org.irods.jargon.core.utils.IRODSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 *         Access object that encapsulates file IO operations. This is a
 *         delegate class for the <code>IRODSFileInputStream</code> and
 *         <code>IRODSFileOutputStream</code> classes. This class is not
 *         publicly visible. Instead, the various IRODS-specific steam classes
 *         should be used.
 */
final class FileIOOperationsAOImpl extends IRODSGenericAO implements
		FileIOOperations {

	static Logger log = LoggerFactory.getLogger(FileIOOperationsAOImpl.class);

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected FileIOOperationsAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.FileIOOperations#write(int, byte[],
	 * int, int)
	 */
	@Override
	public int write(final int fd, final byte buffer[], final int offset,
			final int length) throws JargonException {

		if (fd <= 0) {
			throw new IllegalArgumentException(
					"file is not open, file descriptor was less than zero:"
							+ fd);
		}

		if (buffer == null || buffer.length == 0) {
			throw new IllegalArgumentException("null or empty buffer");
		}

		if (offset < 0) {
			throw new IllegalArgumentException("offset less than zero");
		}

		if (length <= 0) {
			throw new IllegalArgumentException("zero or negative length");
		}

		if (offset > length) {
			throw new IllegalArgumentException("offset of:" + offset
					+ " is > length:" + length);
		}

		if (offset > buffer.length) {
			throw new IllegalArgumentException("offset of:" + offset
					+ " is greater than the buffer length of:" + buffer.length);
		}

		if (log.isDebugEnabled()) {
			log.debug("attempting to write to fd:" + fd
					+ " a buffer with a size of " + buffer.length
					+ " using a length of " + length + " and an offset of "
					+ offset);
		}

		OpenedDataObjInp openedDataObjInp = OpenedDataObjInp
				.instanceForFileWrite(fd, offset, length);
		// DataObjWriteInp dataObjWriteInp = DataObjWriteInp.instance(fd,
		// length);

		Tag message = getIRODSProtocol().irodsFunction(
				IRODSConstants.RODS_API_REQ, openedDataObjInp.getParsedTags(),
				null, 0, 0, buffer, offset, length,
				openedDataObjInp.getApiNumber());

		return message.getTag(IRODSConstants.MsgHeader_PI)
				.getTag(IRODSConstants.intInfo).getIntValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.FileIOOperations#fileRead(int,
	 * java.io.OutputStream, long)
	 */
	@Override
	public int fileRead(final int fd, final OutputStream destination,
			long length) throws JargonException {

		log.info("file read for fd: {}", fd);

		if (fd <= 0) {
			throw new IllegalArgumentException("invalid file descriptor");
		}

		// DataObjRead dataObjReadPI = DataObjRead.instance(fd, length);
		OpenedDataObjInp fileReadInp = OpenedDataObjInp.instanceForFileRead(fd,
				length);
		AbstractIRODSMidLevelProtocol irodsProtocol = getIRODSProtocol();

		Tag message = irodsProtocol.irodsFunction(fileReadInp);

		// Need the total dataSize
		if (message == null) {
			return -1;
		}

		length = message.getTag(IRODSConstants.MsgHeader_PI)
				.getTag(IRODSConstants.bsLen).getIntValue();

		// read the message byte stream into the local file
		irodsProtocol.read(destination, length);
		return message.getTag(IRODSConstants.MsgHeader_PI)
				.getTag(IRODSConstants.intInfo).getIntValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.FileIOOperations#fileRead(int, byte[],
	 * int, int)
	 */
	@Override
	public int fileRead(final int fd, final byte buffer[], final int offset,
			int length) throws JargonException {

		log.info("file read for fd: {}", fd);

		if (fd <= 0) {
			throw new IllegalArgumentException("invalid file descriptor");
		}

		OpenedDataObjInp fileReadInp = OpenedDataObjInp.instanceForFileRead(fd,
				length);
		AbstractIRODSMidLevelProtocol irodsProtocol = getIRODSProtocol();
		Tag message = irodsProtocol.irodsFunction(fileReadInp);

		// Need the total dataSize
		if (message == null) {
			return -1;
		}

		length = message.getTag(IRODSConstants.MsgHeader_PI)
				.getTag(IRODSConstants.bsLen).getIntValue();

		// read the message byte stream into the local file

		int read = irodsProtocol.read(buffer, offset, length);

		if (read == message.getTag(IRODSConstants.MsgHeader_PI)
				.getTag(IRODSConstants.intInfo).getIntValue()) {
			return read;
		} else {
			log.error("did not read length equal to response length, expected"
					+ length + " bytes actually read:" + read);
			throw new JargonException("Bytes read mismatch");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.FileIOOperations#seek(int, long,
	 * org.irods.jargon.core.pub.io.FileIOOperations.SeekWhenceType)
	 */
	@Override
	public long seek(final int fd, final long seek, final SeekWhenceType whence)
			throws JargonException {

		if (whence == SeekWhenceType.SEEK_START
				|| whence == SeekWhenceType.SEEK_CURRENT
				|| whence == SeekWhenceType.SEEK_END) {
		} else {
			log.error("Illegal Argument exception, whence value in seek must be SEEK_START, SEEK_CURRENT, or SEEK_END");
			throw new IllegalArgumentException(
					"whence value in seek must be SEEK_START, SEEK_CURRENT, or SEEK_END");
		}

		if (fd <= 0) {
			log.error("no valid file handle provided");
			throw new IllegalArgumentException("no valid file handle provided");
		}

		Tag message;

		OpenedDataObjInp openedDataObjInp = OpenedDataObjInp
				.instanceForFileSeek(seek, fd, whence.ordinal());
		message = getIRODSProtocol().irodsFunction(openedDataObjInp);

		return message.getTag(IRODSConstants.offset).getLongValue();
	}
}
