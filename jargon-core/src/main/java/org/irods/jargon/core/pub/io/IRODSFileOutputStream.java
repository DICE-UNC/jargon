/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
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

	private Logger log = LoggerFactory.getLogger(this.getClass());

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
	 * @exception IOException
	 *                if the file does not exist, is a directory rather than a
	 *                regular file, or for some other reason cannot be opened
	 *                for reading.
	 */
	protected IRODSFileOutputStream(final IRODSFile irodsFile,
			final FileIOOperations fileIOOperations)
			throws FileNotFoundException {
		super();
		checkFileParameter(irodsFile);
		if (fileIOOperations == null) {
			throw new JargonRuntimeException("fileIOOperations is null");
		}

		if (!irodsFile.exists()) {
			String msg = "file does not exist:" + irodsFile.getAbsolutePath();
			log.error(msg);
			throw new FileNotFoundException(msg);
		}

		if (irodsFile.exists() && !irodsFile.isFile()) {
			String msg = "this is not a file, it is a directory:"
					+ irodsFile.getAbsolutePath();
			log.error(msg);
			throw new FileNotFoundException(msg);
		}

		if (!irodsFile.canWrite()) {
			String msg = "cannot write the file:" + irodsFile.getAbsolutePath();
			log.error(msg);
			throw new FileNotFoundException(msg);
		}

		this.irodsFile = irodsFile;

		try {
			openIRODSFile();
		} catch (JargonException e) {
			String msg = "JargonException caught in constructor, rethrow as JargonRuntimeException";
			log.error(msg, e);
			throw new JargonRuntimeException(msg, e);
		}

		this.fileIOOperations = fileIOOperations;

	}

	private int openIRODSFile() throws JargonException {
		int fileDescriptor = -1;
		if (!irodsFile.exists()) {
			try {
				irodsFile.createNewFile();

				if (irodsFile.getFileDescriptor() == -1) {
					String msg = "no file descriptor returned from file creation";
					log.error(msg);
					throw new JargonException(msg);
				}
				fileDescriptor = irodsFile.getFileDescriptor();
			} catch (IOException e) {
				log.error("error creating file:" + this, e);
				throw new JargonException(
						"IOException rethrown as Jargon exception when creating file:"
								+ irodsFile, e);
			}
		} else {
			log.info("opening the file");
			// open the file
			fileDescriptor = irodsFile.open();
		}

		if (log.isDebugEnabled()) {
			log.debug("file descriptor from open/create operation ="
					+ fileDescriptor);
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
		checkIfOpen();
		try {
			this.fileIOOperations.write(getFileDescriptor(), b, off, len);
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
