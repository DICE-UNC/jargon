package org.irods.jargon.core.pub.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;
import org.irods.jargon.core.pub.io.FileIOOperations.SeekWhenceType;
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
	 * This is the default open mode see {@link DataObjInp.OpenFlags} for
	 * details. New signatures allow other open options.
	 */
	private OpenFlags openFlags = OpenFlags.WRITE;

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
			final FileIOOperations fileIOOperations, final OpenFlags openFlags)
			throws NoResourceDefinedException, FileNotFoundException,
			JargonException {

		super();
		checkFileParameter(irodsFile);
		if (fileIOOperations == null) {
			throw new IllegalArgumentException("fileIOOperations is null");
		}

		if (openFlags == null) {
			throw new IllegalArgumentException("openFlags is null");
		}

		/*
		 * Exists and other checks done by object calling this constructor
		 */

		this.irodsFile = irodsFile;
		this.openFlags = openFlags;
		openIRODSFile(fileIOOperations);
		this.fileIOOperations = fileIOOperations;
	}

	private int openIRODSFile(final FileIOOperations fileIOOperations)
			throws NoResourceDefinedException, JargonException {

		log.info("openIRODSFile()");
		int fileDescriptor = -1;

		boolean exists = irodsFile.exists();
		log.info("exists? {}", exists);

		/*
		 * Check exists with open flags and throw error or create as needed
		 */

		irodsFile.setOpenFlags(openFlags);

		if (exists) {
			if (openFlags == OpenFlags.WRITE_FAIL_IF_EXISTS
					|| openFlags == OpenFlags.READ_WRITE_FAIL_IF_EXISTS) {
				log.error("file exists, open flags indicate failure intended");
				throw new JargonException(
						"Attempt to open a file that exists is an error based on the desired openFlags");
			} else {
				log.info("open file with given flags");
				irodsFile.open(openFlags);
			}

		} else {
			log.info("file does not exist, create it");
			irodsFile.createNewFileCheckNoResourceFound(openFlags);

		}

		fileDescriptor = irodsFile.getFileDescriptor();

		/**
		 * Am I seeking to the end of the file?
		 */
		if (openFlags == OpenFlags.READ_WRITE
				|| openFlags == OpenFlags.READ_WRITE_CREATE_IF_NOT_EXISTS) {
			log.info("seeking to end of file based on open flags...");
			fileIOOperations.seek(fileDescriptor, 0L, SeekWhenceType.SEEK_END);
		}

		if (fileDescriptor == -1) {
			String msg = "no file descriptor returned from file creation";
			log.error(msg);
			throw new JargonException(msg);
		}
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
