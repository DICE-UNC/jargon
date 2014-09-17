package org.irods.jargon.core.pub.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IRODS-specific implementation of <code>java.io.FileOutputStream</code>
 * 
 * An IRODSFileOutputStream writes bytes to a file in a file system. What files
 * are available depends on the host environment.
 * 
 * IRODSFileOutputStream is meant for writing streams of raw bytes such as image
 * data.
 * 
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSFileOutputStream extends OutputStream {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final IRODSFile irodsFile;
	private final FileIOOperations fileIOOperations;

	/**
	 * @return the fileIOOperations
	 */
	protected FileIOOperations getFileIOOperations() {
		return fileIOOperations;

	}

	/**
	 * Creates a <code>FileOuputStream</code> by opening a connection to an
	 * actual file, the file named by the path name <code>name</code> in the
	 * file system.
	 * <p>
	 * First, the security is checked to verify the file can be written.
	 * <p>
	 * If the named file does not exist, is a directory rather than a regular
	 * file, or for some other reason cannot be opened for reading then a
	 * <code>FileNotFoundException</code> is thrown.
	 * 
	 * @param name
	 *            the system-dependent file name.
	 * @exception NoResourceDefinedException
	 *                if no storage resource is defined, and iRODS has not
	 *                default resource rule
	 * @exception FileNotFoundException
	 *                when file is not found in iRODS
	 * @exception JargonException
	 *                when other iRODS errors occur
	 */
	protected IRODSFileOutputStream(final IRODSFile irodsFile,
			final FileIOOperations fileIOOperations)
			throws NoResourceDefinedException, FileNotFoundException,
			JargonException {
		super();
		checkFileParameter(irodsFile);
		if (fileIOOperations == null) {
			throw new JargonRuntimeException("fileIOOperations is null");
		}

		/*
		 * Exists and other checks done by object calling this constructor
		 */

		this.irodsFile = irodsFile;
		openIRODSFile();
		this.fileIOOperations = fileIOOperations;

	}

	private int openIRODSFile() throws NoResourceDefinedException,
			JargonException {
		int fileDescriptor = -1;

		if (irodsFile.exists()) {
			log.info("deleting file, as this stream operation is overwriting");
			irodsFile.deleteWithForceOption();
		}

		irodsFile.createNewFileCheckNoResourceFound();

		if (irodsFile.getFileDescriptor() == -1) {
			String msg = "no file descriptor returned from file creation";
			log.error(msg);
			throw new JargonException(msg);
		}
		fileDescriptor = irodsFile.getFileDescriptor();
		return fileDescriptor;

	}

	/**
	 * @param file
	 * @throws JargonRuntimeException
	 */
	private void checkFileParameter(final IRODSFile file)
			throws JargonRuntimeException {
		if (file == null) {
			String msg = "file is null";
			log.error(msg);
			throw new JargonRuntimeException(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileOutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			irodsFile.close();
		} catch (JargonException e) {
			String msg = "JargonException caught in constructor, rethrow as IOException";
			log.error(msg, e);
			throw new IOException(msg, e);
		}
	}

	private void checkIfOpen() throws IOException {
		if (irodsFile.getFileDescriptor() == -1) {
			log.debug("this file is not open, will throw an IOException");
			throw new IOException("operation attempted on unopened file:"
					+ irodsFile.getAbsolutePath());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileOutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(final byte[] b, final int off, final int len)
			throws IOException {

		if (b == null || b.length == 0) {
			log.warn("nothing to write, ignore");
			return;
		}

		checkIfOpen();
		try {
			fileIOOperations.write(getFileDescriptor(), b, off, len);
		} catch (JargonException e) {
			log.error(
					"rethrowing JargonException as IO exception for write operation",
					e);
			throw new IOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileOutputStream#write(byte[])
	 */
	@Override
	public void write(final byte[] b) throws IOException {
		checkIfOpen();
		write(b, 0, b.length);
	}

	@Override
	public void write(final int b) throws IOException {
		checkIfOpen();
		byte buffer[] = { (byte) b };

		write(buffer, 0, buffer.length);
	}

	public int getFileDescriptor() {
		return irodsFile.getFileDescriptor();
	}
}
