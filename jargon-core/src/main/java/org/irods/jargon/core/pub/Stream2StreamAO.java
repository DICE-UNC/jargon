package org.irods.jargon.core.pub;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * Interface for an access object that helps with copying streams or byte arrays
 * to and from iRODS files.
 * <p>
 * This is a new service, so methods will fill in as they are identified.
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public interface Stream2StreamAO extends IRODSAccessObject {

	/**
	 * Given the set of bytes, copy them to the given target file in iRODS.
	 *
	 * @param bytesToStream
	 *            {@code byte[]} with the bytes to copy
	 * @param irodsTargetFile
	 *            {@link IRODSFile} that will be written to
	 * @throws JargonException
	 */
	void streamBytesToIRODSFile(final byte[] bytesToStream, final IRODSFile irodsTargetFile) throws JargonException;

	/**
	 * Stream the file contents to a byte array. Note that this method is
	 * suitable for small data sizes, but since it uses memory, large files may
	 * cause memory problems.
	 * <p>
	 * This particular method is used internally for object de-serialization
	 * when stored as iRODS files.
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} that will be the source of the byte data
	 * @return {@code byte[]} array representing the file contents
	 * @throws JargonException
	 */
	byte[] streamFileToByte(IRODSFile irodsFile) throws JargonException;

	/**
	 * Stream the {@code InputStream} to the {@code OutputStream}
	 *
	 * @param inputStream
	 *            {@code InputStream} to stream from
	 * @param outputStream
	 *            {@code OutputStream} to stream to
	 * @throws NoResourceDefinedException
	 *             if no storage resource is defined, and iRODS does not have a
	 *             default resource selection rule
	 * @throws JargonException
	 */
	void streamToStreamCopy(InputStream inputStream, OutputStream outputStream)
			throws NoResourceDefinedException, JargonException;

	/**
	 * Transfer the contents of a stream to a given file. Note that this method
	 * will detect if the target file is an {@code IRODSFile}, and in that case,
	 * it will properly handle the transfer via iRODS.
	 * <p>
	 * Transfers are done in this method by doing a normal copy between java.io
	 * streams.
	 * <p>
	 * This method will close the input and output streams as part of normal
	 * operations, and will attempt to do connection re-routing for iRODS files
	 * if so specified in the jargon properties.
	 * <p>
	 * Note that the {@code inputStream} will be buffered if it is not passed in
	 * as a buffered stream, using the characteristics described in
	 * jargon.properties.
	 *
	 * @param inputStream
	 *            {@link InputStream} for the transfer, note that this method
	 *            does not wrap the stream with any buffering, so a properly
	 *            buffered stream should be provided based on needs.
	 * @param targetFile
	 *            {@link File} that will be the target for the stream transfer.
	 *            If the {@code targetFile} is an instance of {@link IRODSFile},
	 *            the transfer will be done using iRODS protocols to iRODS.
	 * @param length
	 *            {@code long} with the length of the source stream
	 * @param readBuffSize
	 *            {@code int} with the buffer size used for the transfer.
	 *            Setting to 0 will cause the default buffer size to be used.
	 * @return {@link TransferStatistics} that give information about the
	 *         transfer size and rate
	 * @throws JargonException
	 */
	TransferStatistics transferStreamToFileUsingIOStreams(InputStream inputStream, File targetFile, long length,
			int readBuffSize) throws JargonException;

	/**
	 * Stream a class-path resource to a target iRODS file
	 *
	 * @param resourcePath
	 *            {@code String} with a path to a resource that can be loaded by
	 *            the class loader.
	 * @param irodsFileAbsolutePath
	 *            {@code String} with an iRODS file absolute path to which the
	 *            resource will be loaded.
	 * @throws JargonException
	 */
	void streamClasspathResourceToIRODSFile(String resourcePath, String irodsFileAbsolutePath) throws JargonException;

	/**
	 * Copy an input stream to an output stream as guided by the
	 * jargon.properties settings.
	 * <p>
	 * Note that this method will buffer the streams provided in the call if
	 * they are not buffered, controlled by the jargon.properties.
	 * <p>
	 * This method will close the streams and do a final flush of the output
	 * stream, so no further processing is necessary.
	 *
	 * @param inputStream
	 *            {@link InputStream}. If not buffered, it will be buffered
	 * @param outputStream
	 *            {@link OutputStream}. If not buffered, it will be buffered
	 * @return {@link TransferStatistics} that give information about the
	 *         transfer size and rate
	 * @throws JargonException
	 */
	TransferStatistics streamToStreamCopyUsingStandardIO(InputStream inputStream, OutputStream outputStream)
			throws JargonException;

}
