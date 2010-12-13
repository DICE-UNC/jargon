/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.irods.jargon.core.exception.JargonException;

/**
 * This is a special version of a <code>IRODSFileInputStream</code> that add the
 * capability to close the underlying <code>IRODSSession</code> when the stream
 * is closed. This is used in situations where a stream is created and returned
 * from a method, and the caller is not aware of the need to close the iRODS
 * connection. *
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SessionClosingIRODSFileInputStream extends IRODSFileInputStream {

	/**
	 * Constructor is called from the appropriate method in
	 * <code>org.irods.jargon.core.pub.io.IRODSFileFactory}.
	 * 
	 * @param irodsFile
	 *            {@link IRODSFile} that underlies the stream
	 * @param fileIOOperations
	 *            {@link FileIOOperations} object that handles the actual iRODS
	 *            communication.
	 * @throws FileNotFoundException
	 */
	protected SessionClosingIRODSFileInputStream(final IRODSFile irodsFile,
			final FileIOOperations fileIOOperations)
			throws FileNotFoundException {
		super(irodsFile, fileIOOperations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFileInputStream#close()
	 */
	@Override
	public void close() throws IOException {
		super.close();
		try {
			// close the session in addition to the stream.
			this.getFileIOOperations().getIRODSSession().closeSession();
		} catch (JargonException e) {
			throw new IOException(
					"error in close session returned as IOException for method contracts");
		}
	}

}
