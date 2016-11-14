package org.irods.jargon.core.pub.io;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;

/**
 * This is a special version of a <code>IRODSFileOutputStream</code> that adds
 * the capability to close the underlying <code>IRODSSession</code> when the
 * stream is closed. This is used in situations where a stream is created and
 * returned from a method, and the caller is not aware of the need to close the
 * iRODS connection.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SessionClosingIRODSFileOutputStream extends IRODSFileOutputStream {

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
	 *             if file cannot be found
	 * @throws JargonException
	 *             for other iRODS errors
	 */
	protected SessionClosingIRODSFileOutputStream(final IRODSFile irodsFile,
			final FileIOOperations fileIOOperations)
			throws FileNotFoundException, JargonException {
		super(irodsFile, fileIOOperations, DataObjInp.OpenFlags.WRITE);
	}

	/**
	 * Constructor is called from the appropriate method in
	 * <code>org.irods.jargon.core.pub.io.IRODSFileFactory}.
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} that underlies the stream
	 * @param fileIOOperations
	 *            {@link FileIOOperations} object that handles the actual iRODS
	 *            communication.
	 * @param openFlags
	 *            {@link DataObjInp.OpenFlags} enum value that dictates file
	 *            open, create, positioning for the stream
	 * @throws FileNotFoundException
	 *             if file cannot be found
	 * @throws JargonException
	 *             for other iRODS errors
	 */
	protected SessionClosingIRODSFileOutputStream(final IRODSFile irodsFile,
			final FileIOOperations fileIOOperations, final OpenFlags openFlags)
			throws FileNotFoundException, JargonException {
		super(irodsFile, fileIOOperations, openFlags);
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
			getFileIOOperations().getIRODSSession().closeSession(
					getFileIOOperations().getIRODSAccount());
		} catch (JargonException e) {
			throw new IOException(
					"error in close session returned as IOException for method contracts");
		}
	}
}
