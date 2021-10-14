package org.irods.jargon.core.pub.io;

import java.io.File;
import java.net.URI;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;

/**
 * Defines the interface to a factory that can produce connected versions of
 * various {@code java.io.*} objects specific to iRODS.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface IRODSFileFactory {

	/**
	 * Create an instance of an {@code IRODSFile} by absolute path.
	 *
	 * @param path {@code String} with the absolute path to the iRODS file or
	 *             collection
	 * @return {@link IRODSFile}
	 * @throws JargonException for iRODS error
	 */
	IRODSFile instanceIRODSFile(String path) throws JargonException;

	/**
	 * Creates an instance of an iRODS file by using the standard irods URL format
	 *
	 * @param uri {@code URI} in iRODS specific format.
	 * @return {@link IRODSFile}
	 * @throws JargonException for iRODS error
	 */
	IRODSFile instanceIRODSFile(URI uri) throws JargonException;

	/**
	 * Creates an instance of an iRODS file by defining a parent and child path
	 *
	 * @param parent {@code String} with the absolute path to the parent
	 * @param child  {@code String} with the relative path below the parent
	 * @return {@link IRODSFile}
	 * @throws JargonException for iRODS error
	 */
	IRODSFile instanceIRODSFile(final String parent, final String child) throws JargonException;

	/**
	 * Creates an iRODS output stream such that data can be written to the given
	 * iRODS file. This will use the default OpenFlags.WRITE setting
	 *
	 * @param irodsFile {@link IRODSFile} that will be written to via the given
	 *                  stream.
	 * @return {@link IRODSFileOutputStream} implementation of a
	 *         {@code java.io.OutputStream}
	 * @throws NoResourceDefinedException if no storage resource is defined and no
	 *                                    default rule is set up on iRODS
	 * @throws JargonException            for iRODS error
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStream(IRODSFile irodsFile)
			throws NoResourceDefinedException, JargonException;

	/**
	 * Creates an iRODS output stream such that data can be written to the given
	 * iRODS file. This method takes an {@code OpenFlags} parameter that dicates the
	 * open mode and file create mode. See {@link OpenFlags} for details
	 *
	 * @param file      {@link IRODSFile} that will be written to via the given
	 *                  stream.
	 * @param openFlags {@link OpenFlags} parameter that dicates open mode and
	 *                  automatic create behavior, as well as overwrite/truncation
	 *                  behavior
	 *
	 * @return {@link IRODSFileOutputStream} with an opened and positioned stream
	 * @throws NoResourceDefinedException when resource missing
	 * @throws JargonException            for iRODS error
	 *
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStream(IRODSFile file, OpenFlags openFlags)
			throws NoResourceDefinedException, JargonException;

	/**
	 * Creates an iRODS output stream such that data can be written to the given
	 * iRODS file. This method takes an {@code OpenFlags} parameter that dicates the
	 * open mode and file create mode. See {@link OpenFlags} for details
	 *
	 * @param file        {@link IRODSFile} that will be written to via the given
	 *                    stream.
	 * @param openFlags   {@link OpenFlags} parameter that dicates open mode and
	 *                    automatic create behavior, as well as overwrite/truncation
	 *                    behavior
	 * @param coordinated {@code boolean} which is set to {@code true} when you want
	 *                    Jargon to manage replica tokens and multiple threads with
	 *                    the same token
	 *
	 * @return {@link IRODSFileOutputStream} with an opened and positioned stream
	 * @throws NoResourceDefinedException when resource missing
	 * @throws JargonException            for iRODS error
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStream(IRODSFile file, OpenFlags openFlags, boolean coordinated)
			throws NoResourceDefinedException, JargonException;

	/**
	 * Creates an iRODS output stream such that data can be written to the given
	 * iRODS file. This will default the open behavior to {@code OpenFlags.WRITE}
	 *
	 * @param name {@code String} with and absolute path to the file that will be
	 *             written to via the given stream.
	 * @return {@link IRODSFileOutputStream} implementation of a
	 *         {@code java.io.OutputStream}
	 * @throws NoResourceDefinedException if no storage resource is defined and no
	 *                                    default rule is set up on iRODS
	 * @throws JargonException            for iRODS error
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStream(String name) throws NoResourceDefinedException, JargonException;

	/**
	 *
	 * @param name      {@code String} with and absolute path to the file that will
	 *                  be written to via the given stream.
	 * @param openFlags {@link OpenFlags} parameter that dicates open mode and
	 *                  automatic create behavior, as well as overwrite/truncation
	 *                  behavior
	 * @return {@link IRODSFileOutputStream} implementation of a
	 *         {@code java.io.OutputStream}
	 * @throws NoResourceDefinedException when resource is missing
	 * @throws JargonException            for iRODS error
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStream(String name, OpenFlags openFlags)
			throws NoResourceDefinedException, JargonException;

	/**
	 * Creates an iRODS output stream such that data can be written to the given
	 * iRODS file.
	 * <p>
	 * This particular method will inspect the target resource, as set in the
	 * {@code irodsFile}, and potentially re-route the connection to that resource.
	 *
	 * @param irodsFile {@link IRODSFile} that is the target of the stream.
	 * @return {@link IRODSFileOutputStream} that will write to the target
	 *         {@code irodsFile}
	 * @throws NoResourceDefinedException if no storage resource is defined and no
	 *                                    default rule is set up on iRODS
	 * @throws JargonException            for iRODS error
	 */
	IRODSFileOutputStream instanceIRODSFileOutputStreamWithRerouting(IRODSFile irodsFile)
			throws NoResourceDefinedException, JargonException;

	/**
	 * Creates an iRODS version of an input stream such that data can be read from
	 * the source iRODS file.
	 *
	 * @param irodsFile {@link IRODSFile} that will be the source of the stream
	 * @return {@link IRODSFileInputStream} that allows reading of the contents of
	 *         the iRODS file
	 * @throws JargonException for iRODS error
	 */
	IRODSFileInputStream instanceIRODSFileInputStream(IRODSFile irodsFile) throws JargonException;

	/**
	 * Creates an iRODS input stream such that data can be read to the given iRODS
	 * file.
	 *
	 * @param name {@code String} with and absolute path to the file that will be
	 *             read to via the given stream.
	 * @return {@link IRODSFileInputStream} implementation of a
	 *         {@code java.io.InputStream}
	 * @throws JargonException for iRODS error
	 */
	IRODSFileInputStream instanceIRODSFileInputStream(String name) throws JargonException;

	/**
	 * Creates an iRODS input stream such that data can be read to the given iRODS
	 * file.
	 * <p>
	 * Note that this method signature will do any necessary connection re-routing
	 * based to a resource actually containing the file. If such rerouting is done,
	 * the {@code InputStream} will be wrapped with a
	 * {@link SessionClosingIRODSFileInputStream} that will close the re-routed
	 * connection when the stream is closed.
	 *
	 * @param irodsAbsolutePath {@code String} with and absolute path to the file
	 *                          that will be read to via the given stream.
	 * @return {@link IRODSFileInputStream} implementation of a
	 *         {@code java.io.InputStream}
	 * @throws JargonException for iRODS error
	 */
	IRODSFileInputStream instanceIRODSFileInputStreamWithRerouting(String irodsAbsolutePath) throws JargonException;

	/**
	 * Create an IRODSRandomAccessFile given the {@code IRODSFileImpl}. Note that
	 * this method will check if the file exists, and the file will be created if it
	 * does not.
	 *
	 * @param irodsFile {@link org.irods.jargon.core.pub.io.IRODSFileImpl} that
	 *                  encapsulates the underlying IRODS File
	 * @return {@link IRODSRandomAccessFile}
	 * @throws NoResourceDefinedException if no storage resource is defined and no
	 *                                    default rule is set up on iRODS
	 * @throws JargonException            for iRODS error
	 */
	IRODSRandomAccessFile instanceIRODSRandomAccessFile(IRODSFile irodsFile)
			throws NoResourceDefinedException, JargonException;

	/**
	 * Create an IRODSRandomAccessFile given the absolutePath. Note that this method
	 * will check if the file exists, and the file will be created if it does not.
	 *
	 * @param name {@code String} with the absolute path to the file.
	 * @return {@link IRODSRandomAccessFile}
	 * @throws NoResourceDefinedException if no storage resource is defined and no
	 *                                    default rule is set up on iRODS
	 * @throws JargonException            for iRODS error
	 */
	IRODSRandomAccessFile instanceIRODSRandomAccessFile(String name) throws NoResourceDefinedException, JargonException;

	/**
	 * Create a writer that will write to the iRODS file with the given absolute
	 * path
	 *
	 * @param name {@code String} with the absolute path to the iRODS file that will
	 *             be witten to.
	 * @return {@link IRODSFileWriter} that is an iRODS specific implmementation of
	 *         a {@code FileWriter}
	 * @throws NoResourceDefinedException if no storage resource is defined and no
	 *                                    default rule is set up on iRODS
	 * @throws JargonException            for iRODS error
	 */
	IRODSFileWriter instanceIRODSFileWriter(String name) throws NoResourceDefinedException, JargonException;

	/**
	 * Return an instance of an {@link org.irods.jargon.core.pub.io.IRODSFileReader}
	 *
	 * @param irodsFileAbsolutePath {@code String} containing the absolute path to
	 *                              the iRODS file to be read
	 * @return {@link IRODSFileReader}
	 * @throws JargonException for iRODS error
	 */
	IRODSFileReader instanceIRODSFileReader(String irodsFileAbsolutePath) throws JargonException;

	/**
	 * Returns an iRODS file given a {@code File} as a parent, and a string which is
	 * the relative path underneath the parent file.
	 *
	 * @param parent {@code File} that describes the parent of the iRODS file. Note
	 *               that the parent will actually be an instance of
	 *               {@code IRODSFile}
	 * @param child  {@code String} with the relative file path underneat the given
	 *               parent file.
	 * @return {@link IRODSFile}
	 * @throws JargonException for iRODS error
	 */
	IRODSFile instanceIRODSFile(File parent, String child) throws JargonException;

	/**
	 * Create an instance of a {@link SessionClosingIRODSFileInputStream}. This
	 * special input stream will close the underlying iRODS connection when the
	 * stream is closed.
	 *
	 * @param name {@code String} with the absolute path to the iRODS file that will
	 *             be read and streamed.
	 * @return {@link SessionClosingIRODSFileInputStream}
	 * @throws JargonException for iRODS error
	 */
	SessionClosingIRODSFileInputStream instanceSessionClosingIRODSFileInputStream(String name) throws JargonException;

	/**
	 * Create an instance of a
	 * {@link org.irods.jargon.core.pub.io.SessionClosingIRODSFileInputStream}. This
	 * special input stream will close the underlying iRODS connection when the
	 * stream is closed.
	 *
	 * @param file {@link IRODSFile} with the iRODS file that will be opened and
	 *             streamed.
	 * @return {@link SessionClosingIRODSFileInputStream}
	 * @throws JargonException for iRODS error
	 */
	SessionClosingIRODSFileInputStream instanceSessionClosingIRODSFileInputStream(IRODSFile file)
			throws JargonException;

	/**
	 * Create an instance of a {@link SessionClosingIRODSFileInputStream}. This
	 * special input stream will close the underlying iRODS connection when the
	 * stream is closed. This method provides the ability to add the {@code fd} when
	 * the file is already opened.
	 *
	 * @param file {@code File} with the absolute path to the iRODS file that will
	 *             be read and streamed.
	 * @param fd   {@code int} with the file descriptor
	 * @return {@link SessionClosingIRODSFileInputStream}
	 * @throws JargonException for iRODS error
	 */
	IRODSFileInputStream instanceIRODSFileInputStreamGivingFD(IRODSFile file, int fd) throws JargonException;

	/**
	 * Create an instance of a {@link SessionClosingIRODSFileOutputStream}. This
	 * special output stream will close the underlying iRODS connection when the
	 * stream is closed.
	 *
	 * @param file {@link IRODSFile} with the iRODS file that will be opened and
	 *             streamed.
	 * @return {@link SessionClosingIRODSFileInputStream}
	 * @throws NoResourceDefinedException if no storage resource is defined and no
	 *                                    default rule is set up on iRODS
	 * @throws JargonException            for iRODS error
	 */
	SessionClosingIRODSFileOutputStream instanceSessionClosingIRODSFileOutputStream(IRODSFile file)
			throws NoResourceDefinedException, JargonException;

	/**
	 * Create a reference to an iRODS file that is the computed user's home
	 * directory. This uses the normal scheme of /zone/home/username, so it may not
	 * apply in highly customized iRODS instances.
	 *
	 * @param userName {@code String} with the userName that will be used for the
	 *                 home directory
	 * @return {@link IRODSFile} that is the user home directory
	 * @throws JargonException for iRODS error
	 */
	IRODSFile instanceIRODSFileUserHomeDir(String userName) throws JargonException;

	/**
	 * Create an IRODSRandomAccessFile given the absolutePath. Note that this method
	 * will check if the file exists. This variant observes the open flags, so if
	 * the file needs to be created, it must have a flags setting for 'create if not
	 * exists'
	 *
	 * @param name      {@code String} with the absolute path to the file.
	 * @param openFlags {@link OpenFlags} that defines how the file is to be opened
	 *                  (e.g. Read only versus Read/write)
	 * @return {@link IRODSRandomAccessFile}
	 * @throws NoResourceDefinedException if no storage resource is defined and no
	 *                                    default rule is set up on iRODS
	 * @throws JargonException            for iRODS error
	 */
	IRODSRandomAccessFile instanceIRODSRandomAccessFile(String name, OpenFlags openFlags)
			throws NoResourceDefinedException, JargonException;

	/**
	 * Create an IRODSRandomAccessFile given the absolutePath. Note that this method
	 * will check if the file exists. This variant observes the open flags, so if
	 * the file needs to be created, it must have a flags setting for 'create if not
	 * exists'
	 *
	 * @param irodsFile   {@link IRODSFile} with the file to use .
	 * @param openFlags   {@link OpenFlags} that defines how the file is to be
	 *                    opened (e.g. Read only versus Read/write)
	 * @param coordinated {@code boolean} indicating whether to cache replica tokens
	 * @return {@link IRODSRandomAccessFile}
	 * @throws NoResourceDefinedException if no storage resource is defined and no
	 *                                    default rule is set up on iRODS
	 * @throws JargonException            for iRODS error
	 */
	IRODSRandomAccessFile instanceIRODSRandomAccessFile(IRODSFile irodsFile, OpenFlags openFlags, boolean coordinated)
			throws NoResourceDefinedException, JargonException;

}
