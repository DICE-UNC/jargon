package org.irods.jargon.core.pub.io;

import java.io.File;
import java.net.URI;

import org.irods.jargon.core.exception.JargonException;

public interface IRODSFileFactory {
	IRODSFile instanceIRODSFile(String path) throws JargonException;

	IRODSFile instanceIRODSFile(URI uri) throws JargonException;

	IRODSFile instanceIRODSFile(final String parent, final String child)
			throws JargonException;

	IRODSFileOutputStream instanceIRODSFileOutputStream(IRODSFile irodsFile)
			throws JargonException;

	IRODSFileOutputStream instanceIRODSFileOutputStream(String name)
			throws JargonException;

	public IRODSFileInputStream instanceIRODSFileInputStream(IRODSFile irodsFile)
			throws JargonException;

	public IRODSFileInputStream instanceIRODSFileInputStream(String name)
			throws JargonException;

	/**
	 * Create an IRODSRandomAccessFile given the <code>IRODSFileImpl</code>.
	 * Note that this method will check if the file exists, and the file will be
	 * created if it does not.
	 * 
	 * @param irodsFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFileImpl} that
	 *            encapsulates the underlying IRODS File
	 * @return
	 * @throws JargonException
	 */
	public IRODSRandomAccessFile instanceIRODSRandomAccessFile(
			IRODSFile irodsFile) throws JargonException;

	/**
	 * Create an IRODSRandomAccessFile given the absolutePath. Note that this
	 * method will check if the file exists, and the file will be created if it
	 * does not.
	 * 
	 * @param name
	 *            <code>String</code> with the absolute path to the file.
	 * @return
	 * @throws JargonException
	 */
	public IRODSRandomAccessFile instanceIRODSRandomAccessFile(String name)
			throws JargonException;

	public IRODSFileWriter instanceIRODSFileWriter(String name)
			throws JargonException;

	/**
	 * Return an instance of an
	 * {@link org.irods.jargon.core.pub.io.IRODSFileReader}
	 * 
	 * @param irodsFileAbsolutePath
	 *            <code>String</code> containing the absolute path to the iRODS
	 *            file to be read
	 * @return
	 * @throws JargonException
	 */
	public IRODSFileReader instanceIRODSFileReader(String irodsFileAbsolutePath)
			throws JargonException;

	IRODSFile instanceIRODSFile(File parent, String child)
			throws JargonException;

	/**
	 * Create an instance of a
	 * {@link org.irods.jargon.core.pub.io.SessionClosingIRODSFileInputStream}.
	 * This special input stream will close the underlying iRODS connection when
	 * the stream is closed.
	 * 
	 * @param name
	 *            <code>String</code> with the absolute path to the iRODS file
	 *            that will be read and streamed.
	 * @return{@link 
	 *               org.irods.jargon.core.pub.io.SessionClosingIRODSFileInputStream
	 *               }
	 * @throws JargonException
	 */
	SessionClosingIRODSFileInputStream instanceSessionClosingIRODSFileInputStream(
			String name) throws JargonException;

	/**
	 * Create an instance of a
	 * {@link org.irods.jargon.core.pub.io.SessionClosingIRODSFileInputStream}.
	 * This special input stream will close the underlying iRODS connection when
	 * the stream is closed.
	 * 
	 * @param name
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} with the iRODS
	 *            file that will be opened and streamed.
	 * @return{@link 
	 *               org.irods.jargon.core.pub.io.SessionClosingIRODSFileInputStream
	 *               }
	 * @throws JargonException
	 */
	SessionClosingIRODSFileInputStream instanceSessionClosingIRODSFileInputStream(
			IRODSFile file) throws JargonException;
}