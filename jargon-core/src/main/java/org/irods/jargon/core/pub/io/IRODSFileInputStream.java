package org.irods.jargon.core.pub.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IRODS specific implementation of the <code>java.io.FileInputStream</code>.
 * This object is created by the {@link IRODSFileFactory}, and once created can
 * be treated as usual. *
 * <p/>
 * This code handles soft linked files and collections as expected. You may
 * operate on canoncial paths or soft-linked paths.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSFileInputStream extends InputStream {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private transient final IRODSFile irodsFile;
	private transient final FileIOOperations fileIOOperations;
	private transient int fd = -1;
	private transient long filePointer = 0;

	/**
	 * Creates a <code>FileInputStream</code> by opening a connection to an
	 * actual file, the file named by the path name <code>name</code> in the
	 * file system.
	 * <p/>
	 * First, the security is checked to verify the file can be written.
	 * <p/>
	 * If the named file does not exist, is a directory rather than a regular
	 * file, or for some other reason cannot be opened for reading then a
	 * <code>FileNotFoundException</code> is thrown.
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} with the file that will be the basis of the
	 *            input stream
	 * 
	 * @exception IOException
	 *                if the file does not exist, is a directory rather than a
	 *                regular file, or for some other reason cannot be opened
	 *                for reading.
	 */
	protected IRODSFileInputStream(final IRODSFile irodsFile,
			final FileIOOperations fileIOOperations)
			throws FileNotFoundException {

		super();
		checkFileParameter(irodsFile);
		if (fileIOOperations == null) {
			throw new JargonRuntimeException("fileIOOperations is null");
		}

		if (!irodsFile.exists()) {
			final String msg = "file does not exist:"
					+ irodsFile.getAbsolutePath();
			log.error(msg);
			throw new FileNotFoundException(msg);
		}

		if (!irodsFile.isFile()) {
			final String msg = "this is not a file, it is a directory:"
					+ irodsFile.getAbsolutePath();
			log.error(msg);
			throw new FileNotFoundException(msg);
		}

		/*
		 * 
		 * TODO: replace(?) when bug is resolved: [#621] error reading file
		 * w/group permissions
		 * 
		 * if (!irodsFile.canRead()) { final String msg =
		 * "cannot read the file:" + irodsFile.getAbsolutePath();
		 * log.error(msg); throw new FileNotFoundException(msg); }
		 */
		/*
		 * File is not opened until first read. This avoids situations, such as
		 * in Fedora repository, where the stream would otherwise be opened by
		 * one thread and read by another, causing an invalid fd (irods -345000
		 * error).
		 */

		this.irodsFile = irodsFile;
		this.fileIOOperations = fileIOOperations;
		openFile();

	}

	/**
	 *
	 */
	private void openFile() {

		if (fd == -1) {
			log.debug("file will be opened on this operation");
		} else {
			return;
		}

		try {
			openIRODSFile();
			fd = irodsFile.getFileDescriptor();
		} catch (JargonException e) {
			final String msg = "JargonException caught in constructor, rethrow as JargonRuntimeException";
			log.error(msg, e);
			throw new JargonRuntimeException(msg, e);
		}
	}

	/**
	 * Create an <code>IRODSFileInputStream</code> providing an already-opened
	 * file handle.
	 *
	 * @param irodsFile
	 * @param fileIOOperations
	 * @param fd
	 * @throws FileNotFoundException
	 */
	protected IRODSFileInputStream(final IRODSFile irodsFile,
			final FileIOOperations fileIOOperations, final int fd)
			throws FileNotFoundException {

		super();

		if (irodsFile == null) {
			throw new IllegalArgumentException("null irodsFile");
		}

		if (fd <= 0) {
			throw new IllegalArgumentException("fd <= 0");
		}

		this.fileIOOperations = fileIOOperations;
		this.irodsFile = irodsFile;

		this.fd = fd;

	}

	/**
	 * @param file
	 * @throws JargonRuntimeException
	 */
	private void checkFileParameter(final IRODSFile file)
			throws JargonRuntimeException {
		if (file == null) {
			final String msg = "file is null";
			log.error(msg);
			throw new JargonRuntimeException(msg);
		}

	}

	private int openIRODSFile() throws JargonException {
		log.info("openIRODSFile()");
		if (!irodsFile.exists()) {
			log.warn("opening non-existant file for read: {}",
					irodsFile.getAbsolutePath());
			throw new JargonException("file does not exist, cannot read");
		}

		log.info("opening the file");
		// open the file (read-only since its an input stream)
		fd = irodsFile.open(OpenFlags.READ);
		log.info("file descriptor from open operation:{}", fd);

		return fd;

	}

	/**
	 * Note: Use of this method is inadvisable due to the long delays that can
	 * occur with network communcations. Reading even a few bytes in this manner
	 * could cause noticeable slowdowns.
	 * <p/>
	 * Reads the next byte of data from the input stream. The value byte is
	 * returned as an <code>int</code> in the range <code>0</code> to
	 * <code>255</code>. If no byte is available because the end of the stream
	 * has been reached, the value <code>-1</code> is returned. This method
	 * blocks until input data is available, the end of the stream is detected,
	 * or an exception is thrown.
	 *
	 * @return the next byte of data, or <code>-1</code> if the end of the
	 *         stream is reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public int read() throws IOException {
		try {
			byte buffer[] = new byte[1];

			int temp = fileIOOperations.fileRead(fd, buffer, 0, 1);

			if (temp < 0) {
				return -1;
			}
			filePointer += temp; // 0 or 1
			return (buffer[0] & 0xFF);

		} catch (JargonException e) {
			log.error(
					"JargonException in read is converted to IOException for method contract",
					e);
			throw new IOException(e);
		}
	}

	/**
	 * Reads up to <code>len</code> bytes of data from the input stream into an
	 * array of bytes. An attempt is made to read as many as <code>len</code>
	 * bytes, but a smaller number may be read, possibly zero. The number of
	 * bytes actually read is returned as an integer.
	 *
	 * <p/>
	 * This method blocks until input data is available, end of file is
	 * detected, or an exception is thrown.
	 *
	 * <p/>
	 * If <code>b</code> is <code>null</code>, a
	 * <code>JargonRuntimeException</code> is thrown.
	 *
	 * <p/>
	 * If <code>off</code> is negative, or <code>len</code> is negative, or
	 * <code>off+len</code> is greater than the length of the array
	 * <code>b</code>, then an <code>IndexOutOfBoundsException</code> is thrown.
	 *
	 * <p/>
	 * If <code>len</code> is zero, then no bytes are read and <code>0</code> is
	 * returned; otherwise, there is an attempt to read at least one byte. If no
	 * byte is available because the stream is at end of file, the value
	 * <code>-1</code> is returned; otherwise, at least one byte is read and
	 * stored into <code>b</code>.
	 *
	 * <p/>
	 * The first byte read is stored into element <code>b[off]</code>, the next
	 * one into <code>b[off+1]</code>, and so on. The number of bytes read is,
	 * at most, equal to <code>len</code>. Let <i>k</i> be the number of bytes
	 * actually read; these bytes will be stored in elements <code>b[off]</code>
	 * through <code>b[off+</code><i>k</i><code>-1]</code>, leaving elements
	 * <code>b[off+</code><i>k</i><code>]</code> through
	 * <code>b[off+len-1]</code> unaffected.
	 *
	 * <p/>
	 * In every case, elements <code>b[0]</code> through <code>b[off]</code> and
	 * elements <code>b[off+len]</code> through <code>b[b.length-1]</code> are
	 * unaffected.
	 *
	 * <p/>
	 * If the first byte cannot be read for any reason other than end of file,
	 * then an <code>IOException</code> is thrown. In particular, an
	 * <code>IOException</code> is thrown if the input stream has been closed.
	 *
	 * <p/>
	 * The <code>read(b,</code> <code>off,</code> <code>len)</code> method for
	 * class <code>InputStream</code> simply calls the method
	 * <code>read()</code> repeatedly. If the first such call results in an
	 * <code>IOException</code>, that exception is returned from the call to the
	 * <code>read(b,</code> <code>off,</code> <code>len)</code> method. If any
	 * subsequent call to <code>read()</code> results in a
	 * <code>IOException</code>, the exception is caught and treated as if it
	 * were end of file; the bytes read up to that point are stored into
	 * <code>b</code> and the number of bytes read before the exception occurred
	 * is returned. Subclasses are encouraged to provide a more efficient
	 * implementation of this method.
	 *
	 * @param b
	 *            the buffer into which the data is read.
	 * @param off
	 *            the start offset in array <code>b</code> at which the data is
	 *            written.
	 * @param len
	 *            the maximum number of bytes to read.
	 * @return the total number of bytes read into the buffer, or
	 *         <code>-1</code> if there is no more data because the end of the
	 *         stream has been reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @exception NullPointerException
	 *                if <code>b</code> is <code>null</code>.
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read(final byte b[], final int off, final int len)
			throws IOException {

		int temp;
		try {
			temp = fileIOOperations.fileRead(fd, b, off, len);
		} catch (JargonException e) {
			log.error(
					"JargonException in read is converted to IOException for method contract",
					e);
			throw new IOException(e);
		}
		if (temp > 0) {
			filePointer += temp;
		}
		return temp;
	}

	/**
	 * Reads some number of bytes from the input stream and stores them into the
	 * buffer array <code>b</code>. The number of bytes actually read is
	 * returned as an integer. This method blocks until input data is available,
	 * end of file is detected, or an exception is thrown.
	 *
	 * <p/>
	 * If <code>b</code> is <code>null</code>, a
	 * <code>JargonRuntimeException</code> is thrown. If the length of
	 * <code>b</code> is zero, then no bytes are read and <code>0</code> is
	 * returned; otherwise, there is an attempt to read at least one byte. If no
	 * byte is available because the stream is at end of file, the value
	 * <code>-1</code> is returned; otherwise, at least one byte is read and
	 * stored into <code>b</code>.
	 *
	 * <p/>
	 * The first byte read is stored into element <code>b[0]</code>, the next
	 * one into <code>b[1]</code>, and so on. The number of bytes read is, at
	 * most, equal to the length of <code>b</code>. Let <i>k</i> be the number
	 * of bytes actually read; these bytes will be stored in elements
	 * <code>b[0]</code> through <code>b[</code><i>k</i><code>-1]</code>,
	 * leaving elements <code>b[</code><i>k</i><code>]</code> through
	 * <code>b[b.length-1]</code> unaffected.
	 *
	 * <p/>
	 * If the first byte cannot be read for any reason other than end of file,
	 * then an <code>IOException</code> is thrown. In particular, an
	 * <code>IOException</code> is thrown if the input stream has been closed.
	 *
	 * <p/>
	 * The <code>read(b)</code> method for class <code>InputStream</code> has
	 * the same effect as:
	 *
	 * <pre>
	 * <code> read(b, 0, b.length) </code>
	 * </pre>
	 *
	 * @param b
	 *            the buffer into which the data is read.
	 * @return the total number of bytes read into the buffer, or
	 *         <code>-1</code> is there is no more data because the end of the
	 *         stream has been reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @exception JargonRuntimeException
	 *                if <code>b</code> is <code>null</code>.
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(final byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * Skips over and discards <code>n</code> bytes of data from the input
	 * stream. The <code>skip</code> method may, for a variety of reasons, end
	 * up skipping over some smaller number of bytes, possibly <code>0</code>.
	 * The actual number of bytes skipped is returned.
	 *
	 * @param numberOfBytesToSkip
	 *            the number of bytes to be skipped.
	 * @return the actual number of bytes skipped.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public long skip(final long numberOfBytesToSkip) throws IOException {

		long length = availableAsLong();
		if (length <= 0) {
			return 0;
		}

		try {
			openFile();
			if ((filePointer + numberOfBytesToSkip) < length) {

				fileIOOperations.seek(fd, numberOfBytesToSkip,
						FileIOOperations.SeekWhenceType.SEEK_CURRENT);

				filePointer += numberOfBytesToSkip;
				return numberOfBytesToSkip;
			} else {
				fileIOOperations.seek(fd, length,
						FileIOOperations.SeekWhenceType.SEEK_CURRENT);
				filePointer += length;
				return length;
			}
		} catch (JargonException e) {
			log.error(
					"JargonException in operation, rethrown as IOException for contract",
					e);
			throw new IOException(e);
		}
	}

	/**
	 * Returns the number of bytes that can be read from this file input stream
	 * without blocking. Due to the nature of how files are transferred (a byte
	 * array is returned from a socket call for each read operation), Jargon can
	 * never read without blocking. Because of these reasons this method always
	 * returns the length of the file as iRODS knows it.
	 *
	 * @return the number of bytes that can be read from this file input stream
	 *         without blocking.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */

	@Override
	public int available() throws IOException {
		return (int) irodsFile.length();
	}

	/**
	 * Returns the number of bytes that can be read from this file input stream
	 * without blocking. Due to the nature of how files are transferred (a byte
	 * array is returned from a socket call for each read operation), Jargon can
	 * never read without blocking. Because of these reasons this method always
	 * returns the length of the file as iRODS knows it.
	 *
	 * @return the number of bytes that can be read from this file input stream
	 *         without blocking.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */

	public long availableAsLong() throws IOException {
		return irodsFile.length();
	}

	protected FileIOOperations getFileIOOperations() {
		return fileIOOperations;
	}

	/**
	 * Closes this file input stream and releases any system resources
	 * associated with the stream.
	 * <p/>
	 * If this stream has an associated channel then the channel is closed as
	 * well.
	 *
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public void close() throws IOException {
		log.info("closing file stream and file");
		try {
			irodsFile.close();
		} catch (JargonException e) {
			log.error(
					"JargonException in operation, rethrown as IOException for contract",
					e);
			throw new IOException(e);
		}
		filePointer = 0L;
	}

}
