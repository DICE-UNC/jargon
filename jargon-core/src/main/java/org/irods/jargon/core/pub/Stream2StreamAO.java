package org.irods.jargon.core.pub;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * Interface for an access object that helps with copying streams or byte arrays
 * to and from iRODS files.
 * <p/>
 * This is a new service, so methods will fill in as they are identified.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public interface Stream2StreamAO extends IRODSAccessObject {

	/**
	 * Given the set of bytes, copy them to the given target file in iRODS.
	 * 
	 * @param bytesToStream
	 *            <code>byte[]</code> with the bytes to copy
	 * @param irodsTargetFile
	 *            {@link IRODSFile} that will be written to
	 * @throws JargonException
	 */
	void streamBytesToIRODSFile(final byte[] bytesToStream,
			final IRODSFile irodsTargetFile) throws JargonException;

	/**
	 * Stream the file contents to a byte array. Note that this method is
	 * suitable for small data sizes, but since it uses memory, large files may
	 * cause memory problems.
	 * <p/>
	 * This particular method is used internally for object de-serialization
	 * when stored as iRODS files.
	 * 
	 * @param irodsFile
	 *            {@link IRODSFile} that will be the source of the byte data
	 * @return <code>byte[]</code> array representing the file contents
	 * @throws JargonException
	 */
	byte[] streamFileToByte(IRODSFile irodsFile) throws JargonException;

	/**
	 * Stream the <code>InputStream</code> to the <code>OutputStream</code>
	 * 
	 * @param inputStream
	 *            <code>InputStream</code> to stream from
	 * @param outputStream
	 *            <code>OutputStream</code> to stream to
	 * @throws JargonException
	 */
	void streamToStreamCopy(InputStream inputStream, OutputStream outputStream)
			throws JargonException;


	void transferStreamToFile(InputStream inputStream, File targetFile,
			long length, long readBuffSize) throws JargonException;

	/**
	 * Transfer the contents of a stream to a given file. Note that this method
	 * will detect if the target file is an <code>IRODSFile</code>, and in that
	 * case, it will properly handle the transfer via iRODS.
	 * <p/>
	 * Transfers are done in this method by doing a normal copy between java.io
	 * streams.
	 * <p/>
	 * This method will close the input and output streams as part of normal
	 * operations, and will attempt to do connection re-routing for iRODS files
	 * if so specified in the jargon properties.
	 * 
	 * @param inputStream
	 *            {@link InputStream} for the transfer, note that this method
	 *            does not wrap the stream with any buffering, so a properly
	 *            buffered stream should be provided based on needs.
	 * @param targetFile
	 *            {@link File} that will be the target for the stream transfer.
	 *            If the <code>targetFile</code> is an instance of
	 *            {@link IRODSFile}, the transfer will be done using iRODS
	 *            protocols to iRODS.
	 * @param length
	 *            <code>long</code> with the length of the source stream
	 * @param readBuffSize
	 *            <code>int</code> with the buffer size used for the transfer.
	 *            Setting to 0 will cause the default buffer size to be used.
	 * @throws JargonException
	 */
	void transferStreamToFileUsingIOStreams(InputStream inputStream,
			File targetFile, long length, int readBuffSize)
			throws JargonException;

	/**
	 * Stream a class-path resource to a target iRODS file
	 * 
	 * @param resourcePath
	 *            <code>String</code> with a path to a resource that can be
	 *            loaded by the class loader.
	 * @param irodsFileAbsolutePath
	 *            <code>String</code> with an iRODS file absolute path to which
	 *            the resource will be loaded.
	 * @throws JargonException
	 */
	void streamClasspathResourceToIRODSFile(String resourcePath,
			String irodsFileAbsolutePath) throws JargonException;

}