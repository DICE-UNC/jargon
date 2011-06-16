package org.irods.jargon.core.pub;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
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

	public static final Logger log = LoggerFactory
			.getLogger(Stream2StreamAOImpl.class);

	public Stream2StreamAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

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
			ChannelTools.fastChannelCopy(inputChannel, outputChannel);
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

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.pub.Stream2StreamAO#streamFileToByte(org.irods.jargon.core.pub.io.IRODSFile)
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
			ChannelTools.fastChannelCopy(inputChannel, outputChannel);
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

}
