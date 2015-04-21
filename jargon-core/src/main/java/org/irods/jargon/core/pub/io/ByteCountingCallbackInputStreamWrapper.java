/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.IOException;
import java.io.InputStream;

import org.irods.jargon.core.connection.ConnectionProgressStatus;
import org.irods.jargon.core.connection.ConnectionProgressStatusListener;

/**
 * Wrapper for an input stream that counts the number of bytes read and
 * processes status callbacks as required
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ByteCountingCallbackInputStreamWrapper extends InputStream {

	private final ConnectionProgressStatusListener connectionProgressStatusListener;
	private final InputStream inputStream;

	/**
	 * Default constructor gives a required listener for status callbacks on
	 * stream operations
	 * 
	 * @param connectionProgressStatusListner
	 *            {@link ConnectionProgressStatusListener} that can handle
	 *            callbacks as the stream is read. This is required.
	 * @param inputStream
	 *            <code>InputStream</code> to be wrapped
	 */
	public ByteCountingCallbackInputStreamWrapper(
			final ConnectionProgressStatusListener connectionProgressStatusListener,
			final InputStream inputStream) {

		if (connectionProgressStatusListener == null) {
			throw new IllegalArgumentException(
					"null connectionProgressStatusListener");
		}

		if (inputStream == null) {
			throw new IllegalArgumentException("null inputStream");
		}

		this.connectionProgressStatusListener = connectionProgressStatusListener;
		this.inputStream = inputStream;

	}

	@Override
	public int read() throws IOException {
		return inputStream.read();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] b, final int off, final int len)
			throws IOException {

		int read = inputStream.read(b, off, len);

		callbackOnRead(read);

		return read;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(final byte[] b) throws IOException {

		int read = inputStream.read(b);

		callbackOnRead(read);

		return read;
	}

	/**
	 * @param read
	 */
	private void callbackOnRead(final int read) {
		if (read > 0) {
			ConnectionProgressStatus status = ConnectionProgressStatus
					.instanceForReceive(read);
			connectionProgressStatusListener
					.connectionProgressStatusCallback(status);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return inputStream.available();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		inputStream.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public synchronized void mark(final int readlimit) {
		inputStream.mark(readlimit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {

		return inputStream.markSupported();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public synchronized void reset() throws IOException {
		inputStream.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(final long n) throws IOException {

		return inputStream.skip(n);
	}

}
