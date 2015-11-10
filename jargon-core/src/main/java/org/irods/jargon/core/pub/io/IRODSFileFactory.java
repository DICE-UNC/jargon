package org.irods.jargon.core.pub.io;

import java.io.File;
import java.net.URI;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;

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
	 * iRODS file. This will use the default OpenFlags.WRITE setting
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} that will be written to via the given
	 *            stream.
	 * @return {@link IRODSFileOutputStream} implementation of a
	 *         <code>java.io.OutputStream</code>
	 * @throws NoResourceDefinedException
	 *             if no storage resource is defined and no default rule is set
	 *             up on iRODS
	 * @throws JargonException
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStream(IRODSFile irodsFile)
			throws NoResourceDefinedException, JargonException;

	/**
	 * Creates an iRODS output stream such that data can be written to the given
	 * iRODS file. This method takes an <code>OpenFlags</code> parameter that
	 * dicates the open mode and file create mode. See
	 * {@link DataObjInp.OpenFlags} for details
	 *
	 * @param file
	 *            {@link IRODSFile} that will be written to via the given
	 *            stream.
	 * @param openFlags
	 *            {@link DataObjInp.OpenFlags} parameter that dicates open mode
	 *            and automatic create behavior, as well as overwrite/truncation
	 *            behavior
	 *
	 * @return {@link IRODSFileOutputStream} with an opened and positioned
	 *         stream
	 * @throws NoResourceDefinedException
	 * @throws JargonException
	 *             +
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStream(IRODSFile file,
			OpenFlags openFlags) throws NoResourceDefinedException,
			JargonException;

	/**
	 * Creates an iRODS output stream such that data can be written to the given
	 * iRODS file. This will default the open behavior to
	 * <code>OpenFlags.WRITE</code>
	 *
	 * @param name
	 *            <code>String</code> with and absolute path to the file that
	 *            will be written to via the given stream.
	 * @return {@link IRODSFileOutputStream} implementation of a
	 *         <code>java.io.OutputStream</code>
	 * @throws NoResourceDefinedException
	 *             if no storage resource is defined and no default rule is set
	 *             up on iRODS
	 * @throws JargonException
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStream(String name)
			throws NoResourceDefinedException, JargonException;

	/**
	 *
	 * @param name
	 *            <code>String</code> with and absolute path to the file that
	 *            will be written to via the given stream.
	 * @param openFlags
	 *            {@link DataObjInp.OpenFlags} parameter that dicates open mode
	 *            and automatic create behavior, as well as overwrite/truncation
	 *            behavior
	 * @return {@link IRODSFileOutputStream} implementation of a
	 *         <code>java.io.OutputStream</code>
	 * @throws NoResourceDefinedException
	 * @throws JargonException
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStream(String name,
			OpenFlags openFlags) throws NoResourceDefinedException,
			JargonException;

	/**
	 * Creates an iRODS output stream such that data can be written to the given
	 * iRODS file.
	 * <p/>
	 * This particular method will inspect the target resource, as set in the
	 * <code>irodsFile</code>, and potentially re-route the connection to that
	 * resource.
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} that is the target of the stream.
	 * @return {@link IRODSFileOutputStream} that will write to the target
	 *         <code>irodsFile</code>
	 * @throws NoResourceDefinedException
	 *             if no storage resource is defined and no default rule is set
	 *             up on iRODS
	 * @throws JargonException
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStreamWithRerouting(
			IRODSFile irodsFile) throws NoResourceDefinedException,
			JargonException;

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
	 * Creates an iRODS input stream such that data can be read to the given
	 * iRODS file.
	 * <p/>
	 * Note that this method signature will do any necessary connection
	 * re-routing based to a resource actually containing the file. If such
	 * rerouting is done, the <code>InputStream</code> will be wrapped with a
	 * {@link SessionClosingIRODSFileInputStream} that will close the re-routed
	 * connection when the stream is closed.
	 *
	 * @param name
	 *            <code>String</code> with and absolute path to the file that
	 *            will be read to via the given stream.
	 * @return {@link IRODSFileInputStream} implementation of a
	 *         <code>java.io.InputStream</code>
	 * @throws JargonException
	 */
	IRODSFileInputStream instanceIRODSFileInputStreamWithRerouting(
			String irodsAbsolutePath) throws JargonException;

	/**
	 * Create an IRODSRandomAccessFile given the <code>IRODSFileImpl</code>.
	 * Note that this method will check if the file exists, and the file will be
	 * created if it does not.
	 *
	 * @param irodsFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFileImpl} that
	 *            encapsulates the underlying IRODS File
	 * @return
	 * @throws NoResourceDefinedException
	 *             if no storage resource is defined and no default rule is set
	 *             up on iRODS
	 * @throws JargonException
	 */
	IRODSRandomAccessFile instanceIRODSRandomAccessFile(IRODSFile irodsFile)
			throws NoResourceDefinedException, JargonException;

	/**
	 * Create an IRODSRandomAccessFile given the absolutePath. Note that this
	 * method will check if the file exists, and the file will be created if it
	 * does not.
	 *
	 * @param name
	 *            <code>String</code> with the absolute path to the file.
	 * @return
	 * @throws NoResourceDefinedException
	 *             if no storage resource is defined and no default rule is set
	 *             up on iRODS
	 * @throws JargonException
	 */
	IRODSRandomAccessFile instanceIRODSRandomAccessFile(String name)
			throws NoResourceDefinedException, JargonException;

	/**
	 * Create a writer that will write to the iRODS file with the given absolute
	 * path
	 *
	 * @param name
	 *            <code>String</code> with the absolute path to the iRODS file
	 *            that will be witten to.
	 * @return {@link IRODSFileWriter} that is an iRODS specific implmementation
	 *         of a <code>FileWriter</code>
	 * @throws NoResourceDefinedException
	 *             if no storage resource is defined and no default rule is set
	 *             up on iRODS
	 * @throws JargonException
	 */
	IRODSFileWriter instanceIRODSFileWriter(String name)
			throws NoResourceDefinedException, JargonException;

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
	 * Create an instance of a
	 * {@link org.irods.jargon.core.pub.io.SessionClosingIRODSFileInputStream}.
	 * This special input stream will close the underlying iRODS connection when
	 * the stream is closed. This method provides the ability to add the
	 * <code>fd</code> when the file is already opened.
	 *
	 * @param name
	 *            <code>String</code> with the absolute path to the iRODS file
	 *            that will be read and streamed.
	 * @param fd
	 *            <code>int</code> with the file descriptor
	 * @return{@link
	 *               org.irods.jargon.core.pub.io.SessionClosingIRODSFileInputStream
	 *               }
	 * @throws JargonException
	 */
	IRODSFileInputStream instanceIRODSFileInputStreamGivingFD(IRODSFile file,
			int fd) throws JargonException;

	/**
	 * Create an instance of a
	 * {@link org.irods.jargon.core.pub.io.SessionClosingIRODSFileOutputStream}.
	 * This special output stream will close the underlying iRODS connection
	 * when the stream is closed.
	 *
	 * @param name
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} with the iRODS
	 *            file that will be opened and streamed.
	 * @return{@link
	 *               org.irods.jargon.core.pub.io.SessionClosingIRODSFileInputStream
	 *               }
	 * @throws NoResourceDefinedException
	 *             if no storage resource is defined and no default rule is set
	 *             up on iRODS
	 * @throws JargonException
	 */
	SessionClosingIRODSFileOutputStream instanceSessionClosingIRODSFileOutputStream(
			IRODSFile file) throws NoResourceDefinedException, JargonException;

	/**
	 * Create a reference to an iRODS file that is the computed user's home
	 * directory. This uses the normal scheme of /zone/home/username, so it may
	 * not apply in highly customized iRODS instances.
	 *
	 * @param userName
	 *            <code>String</code> with the userName that will be used for
	 *            the home directory
	 * @return {@link IRODSFile} that is the user home directory
	 * @throws JargonException
	 */
	IRODSFile instanceIRODSFileUserHomeDir(String userName)
			throws JargonException;

	/**
	 * Create an IRODSRandomAccessFile given the absolutePath. Note that this
	 * method will check if the file exists. This variant observes the open
	 * flags, so if the file needs to be created, it must have a flags setting
	 * for 'create if not exists'
	 *
	 * @param name
	 *            <code>String</code> with the absolute path to the file.
	 * @param openFlags
	 *            {@link OpenFlags} that defines how the file is to be opened
	 *            (e.g. Read only versus Read/write)
	 * @return
	 * @throws NoResourceDefinedException
	 *             if no storage resource is defined and no default rule is set
	 *             up on iRODS
	 * @throws JargonException
	 */
	IRODSRandomAccessFile instanceIRODSRandomAccessFile(String name,
			OpenFlags openFlags) throws NoResourceDefinedException,
			JargonException;

}