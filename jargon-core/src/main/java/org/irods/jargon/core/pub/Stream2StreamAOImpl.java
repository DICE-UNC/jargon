package org.irods.jargon.core.pub;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.irods.jargon.core.utils.ChannelTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helpful object for stream to stream copies, also handles byte arrays (as
 * contracts fill out). Allows streaming from one source into or out of iRODS.
 * 
 * (methods to be filled out as needed, this is a new service)
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class Stream2StreamAOImpl extends IRODSGenericAO implements
		Stream2StreamAO {

	// private final int bufferSize = this.getJargonProperties().get
	private static final int bufferSize = 32 * 1024; // FIXME: temp code

	public static final Logger log = LoggerFactory
			.getLogger(Stream2StreamAOImpl.class);

	public Stream2StreamAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * TODO: might depracate Potentially deprecated....experimental
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.Stream2StreamAO#streamBytesToIRODSFile(byte[],
	 * org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public void streamBytesToIRODSFile(final byte[] bytesToStream,
			final IRODSFile irodsTargetFile) throws JargonException {

		if (bytesToStream == null || bytesToStream.length == 0) {
			throw new IllegalArgumentException("null or empty bytesToStream");
		}

		if (irodsTargetFile == null) {
			throw new IllegalArgumentException("null irodsTargetFile");
		}

		log.info("streamBytesToIRODSFile(), irodsFile:{}", irodsTargetFile);
		log.info("bytesToStream length:{}", bytesToStream.length);

		OutputStream ifOs = this.getIRODSFileFactory()
				.instanceIRODSFileOutputStream(irodsTargetFile);
		InputStream bis = new ByteArrayInputStream(bytesToStream);

		final ReadableByteChannel inputChannel = Channels.newChannel(bis);
		final WritableByteChannel outputChannel = Channels.newChannel(ifOs);
		// copy the channels
		try {
			ChannelTools.fastChannelCopy(inputChannel, outputChannel,
					bufferSize);
		} catch (IOException e) {
			log.error("IO Exception copying buffers", e);
			throw new JargonException("io exception copying buffers", e);
		} finally {
			try {
				inputChannel.close();
				outputChannel.close();
			} catch (Exception e) {

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.Stream2StreamAO#transferStreamToFileUsingIOStreams
	 * (java.io.InputStream, java.io.File, long, int)
	 */
	@Override
	public void transferStreamToFileUsingIOStreams(
			final InputStream inputStream, final File targetFile,
			final long length, final int readBuffSize)
			throws NoResourceDefinedException, JargonException {

		if (inputStream == null) {
			throw new IllegalArgumentException("null or empty inputStream");
		}

		if (targetFile == null) {
			throw new IllegalArgumentException("null targetFile");
		}

		log.info("transferStreamToFile(), inputStream:{}", inputStream);
		log.info("targetFile:{}", targetFile);

		OutputStream fileOutputStream = null;

		try {

			int outputBufferSize = this.getJargonProperties()
					.getLocalFileOutputStreamBufferSize();

			if (targetFile instanceof IRODSFile) {
				log.info("target file is an iRODS file");

				if (this.getJargonProperties().isAllowPutGetResourceRedirects()) {
					log.info("using transfer redirects, so check for stream re-routing");
					fileOutputStream = this.getIRODSFileFactory()
							.instanceIRODSFileOutputStreamWithRerouting(
									(IRODSFile) targetFile);
				} else {
					log.info("not using transfer redirects, so do not do any stream re-routing");
					fileOutputStream = this.getIRODSFileFactory()
							.instanceIRODSFileOutputStream(
									(IRODSFile) targetFile);
				}

			} else {
				log.info("target file is a normal file");
				fileOutputStream = new FileOutputStream(targetFile);
			}

			log.debug("output buffer size for file output stream in copy:{}",
					outputBufferSize);

			if (outputBufferSize == -1) {
				log.info("no buffer on file output stream to local file");

			} else if (outputBufferSize == 0) {
				log.info("default buffered io to file output stream to local file");
				fileOutputStream = new BufferedOutputStream(fileOutputStream);
			} else {
				log.info(
						"buffer io to file output stream to local file with size of: {}",
						outputBufferSize);
				fileOutputStream = new BufferedOutputStream(fileOutputStream,
						outputBufferSize);
			}

			int myBuffSize = readBuffSize;
			if (myBuffSize <= 0) {
				myBuffSize = this.getJargonProperties()
						.getInputToOutputCopyBufferByteSize();
			}

			if (myBuffSize <= 0) {
				throw new JargonException(
						"invalid stream to stream copy buffer size of {}",
						myBuffSize);
			}

			log.debug("using {} as copy buffer size", myBuffSize);

			int doneCnt = -1;

			byte buf[] = new byte[myBuffSize];

			while ((doneCnt = inputStream.read(buf, 0, myBuffSize)) >= 0) {

				if (doneCnt == 0) {
					Thread.yield();
				} else {
					fileOutputStream.write(buf, 0, doneCnt);
				}
			}

			fileOutputStream.flush();

		} catch (FileNotFoundException e) {
			log.error("File not found exception copying buffers", e);
			throw new JargonException(
					"file not found exception copying buffers", e);
		} catch (IOException e) {
			log.error("io exception copying buffers", e);
			throw new JargonException("io exception copying buffers", e);
		} finally {

			try {
				inputStream.close();
			} catch (Exception e) {
			}

			try {
				fileOutputStream.close();
			} catch (Exception e) {
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.Stream2StreamAO#streamToStreamCopy(java.io.
	 * InputStream, java.io.OutputStream)
	 */
	@Override
	public void streamToStreamCopy(final InputStream inputStream,
			final OutputStream outputStream) throws JargonException {

		if (inputStream == null) {
			throw new IllegalArgumentException("null inputStream");
		}

		if (outputStream == null) {
			throw new IllegalArgumentException("null outputStream");
		}

		log.info("streamToStreamCopy()");

		final ReadableByteChannel inputChannel = Channels
				.newChannel(inputStream);
		final WritableByteChannel outputChannel = Channels
				.newChannel(outputStream);
		// copy the channels
		try {
			ChannelTools.fastChannelCopy(inputChannel, outputChannel,
					bufferSize);
		} catch (IOException e) {
			log.error("IO Exception copying buffers", e);
			throw new JargonException("io exception copying buffers", e);
		} finally {
			try {
				inputChannel.close();
				outputChannel.close();
			} catch (Exception e) {

			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.Stream2StreamAO#streamFileToByte(org.irods.
	 * jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public byte[] streamFileToByte(final IRODSFile irodsFile)
			throws JargonException {

		if (irodsFile == null) {
			throw new IllegalArgumentException("null irodsTargetFile");
		}

		log.info("streamFileToByte() file:{}", irodsFile);

		if (irodsFile.exists() && irodsFile.isFile()) {
			log.info("verified as an existing data object");
		} else {
			throw new JargonException(
					"cannot stream, does not exist or is not a file");
		}

		InputStream is = this.getIRODSFileFactory()
				.instanceIRODSFileInputStream(irodsFile);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ReadableByteChannel inputChannel = Channels.newChannel(is);
		final WritableByteChannel outputChannel = Channels.newChannel(bos);
		// copy the channels
		try {
			ChannelTools.fastChannelCopy(inputChannel, outputChannel,
					bufferSize);
		} catch (IOException e) {
			log.error("IO Exception copying buffers", e);
			throw new JargonException("io exception copying buffers", e);
		} finally {
			try {
				inputChannel.close();
				outputChannel.close();
			} catch (Exception e) {

			}
		}

		return bos.toByteArray();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.Stream2StreamAO#streamClasspathResourceToIRODSFile
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public void streamClasspathResourceToIRODSFile(final String resourcePath,
			final String irodsFileAbsolutePath) throws JargonException {

		if (resourcePath == null || resourcePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourcePath");
		}

		if (irodsFileAbsolutePath == null || irodsFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsFileAbsolutePath");
		}

		IRODSFile irodsTarget = this.getIRODSFileFactory().instanceIRODSFile(
				irodsFileAbsolutePath);
		irodsTarget.getParentFile().mkdirs();
		irodsTarget.delete();

		InputStream inputStream = new BufferedInputStream(this.getClass()
				.getResourceAsStream(resourcePath));

		IRODSFileOutputStream irodsFileOutputStream = this
				.getIRODSFileFactory().instanceIRODSFileOutputStream(
						irodsFileAbsolutePath);

		byte[] buff = new byte[4096];

		try {

			int i = 0;
			while ((i = inputStream.read(buff)) > -1) {
				irodsFileOutputStream.write(buff, 0, i);
			}

		} catch (IOException ioe) {
			log.error("io exception reading rule data from resource", ioe);
			throw new JargonException("error reading rule from resource", ioe);
		} finally {
			try {
				irodsFileOutputStream.close();
			} catch (IOException e) {
				// ignore
			}
			try {
				inputStream.close();
			} catch (IOException e) {
				// ignore
			}

		}

	}

}
