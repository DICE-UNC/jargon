package org.irods.jargon.core.pub.io;

import java.io.File;
import java.net.URI;

import org.irods.jargon.core.exception.JargonException;

/**
 * Defines the interface to a factory that can produce connected versions of
 * various <code>java.io.*</code> objects specific to iRODS.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface IRODSFileFactory {

	/**
	 * Create an instance of an <code>IRODSFile</code> by absolute path.
	 * 
	 * @param path
	 *            <code>String</code> with the absolute path to the iRODS file
	 *            or collection
	 * @return {@link IRODSFile}
	 * @throws JargonException
	 */
	IRODSFile instanceIRODSFile(String path) throws JargonException;

	/**
	 * Creates an instance of an iRODS file by using the standard irods URL
	 * format
	 * 
	 * @parm URI <code>URI</code> in iRODS specific format.
	 * @return {@link IRODSFile}
	 * @throws JargonException
	 */
	IRODSFile instanceIRODSFile(URI uri) throws JargonException;

	/**
	 * Creates an instance of an iRODS file by defining a parent and child path
	 * 
	 * @param parent
	 *            <code>String</code> with the absolute path to the parent
	 * @param child
	 *            <code>String</code> with the relative path below the parent
	 * @return {@link IRODSFile}
	 * @throws JargonException
	 */
	IRODSFile instanceIRODSFile(final String parent, final String child)
			throws JargonException;

	/**
	 * Creates an iRODS output stream such that data can be written to the given
	 * iRODS file.
	 * 
	 * @param irodsFile
	 *            {@link IRODSFile} that will be written to via the given
	 *            stream.
	 * @return {@link IRODSFileOutputStream} implementation of a
	 *         <code>java.io.OutputStream</code>
	 * @throws JargonException
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStream(IRODSFile irodsFile)
			throws JargonException;

	/**
	 * Creates an iRODS output stream such that data can be written to the given
	 * iRODS file.
	 * 
	 * @param name
	 *            <code>String</code> with and absolute path to the file that
	 *            will be written to via the given stream.
	 * @return {@link IRODSFileOutputStream} implementation of a
	 *         <code>java.io.OutputStream</code>
	 * @throws JargonException
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStream(String name)
			throws JargonException;

	/**
	 * Creates an iRODS version of an input stream such that data can be read
	 * from the source iRODS file.
	 * 
	 * @param irodsFile
	 *            {@link IRODSFile} that will be the source of the stream
	 * @return {@link IRODSFileInputStream} that allows reading of the contents
	 *         of the iRODS file
	 * @throws JargonException
	 */
	IRODSFileInputStream instanceIRODSFileInputStream(IRODSFile irodsFile)
			throws JargonException;

	/**
	 * Creates an iRODS input stream such that data can be read to the given
	 * iRODS file.
	 * 
	 * @param name
	 *            <code>String</code> with and absolute path to the file that
	 *            will be read to via the given stream.
	 * @return {@link IRODSFileInputStream} implementation of a
	 *         <code>java.io.InputStream</code>
	 * @throws JargonException
	 */
	IRODSFileInputStream instanceIRODSFileInputStream(String name)
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
	IRODSRandomAccessFile instanceIRODSRandomAccessFile(IRODSFile irodsFile)
			throws JargonException;

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
	IRODSRandomAccessFile instanceIRODSRandomAccessFile(String name)
			throws JargonException;

	/**
	 * Create a writer that will write to the iRODS file with the given absolute
	 * path
	 * 
	 * @param name
	 *            <code>String</code> with the absolute path to the iRODS file
	 *            that will be witten to.
	 * @return {@link IRODSFileWriter} that is an iRODS specific implmementation
	 *         of a <code>FileWriter</code>
	 * @throws JargonException
	 */
	IRODSFileWriter instanceIRODSFileWriter(String name) throws JargonException;

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
	IRODSFileReader instanceIRODSFileReader(String irodsFileAbsolutePath)
			throws JargonException;

	/**
	 * Returns an iRODS file given a <code>File</code> as a parent, and a string
	 * which is the relative path underneath the parent file.
	 * 
	 * @param parent
	 *            <code>File</code> that describes the parent of the iRODS file.
	 *            Note that the parent will actually be an instance of
	 *            <code>IRODSFile</code>
	 * @param child
	 *            <code>String</code> with the relative file path underneat the
	 *            given parent file.
	 * @return {@link IRODSFile}
	 * @throws JargonException
	 */
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

	/**
	 * Create an instance of an <code>IRODSFile</code> by absolute path.  This call allows an optimization
	 * to preset whether this is a file or collection, avoiding some subsequent queries if <code>isFile()</code> is called.
	 * Internally, when that information is available in some methods, this will be pre-set and cached.
	 * 
	 * @param path
	 *            <code>String</code> with the absolute path to the iRODS file
	 *            or collection
	 *  @param isFile <code>boolean</code> that indicates whether this is a file (versus a collection)
	 * @return {@link IRODSFile}
	 * @throws JargonException
	 */
	IRODSFile instanceIRODSFileIndicatingType(String path, boolean isFile)
			throws JargonException;
}