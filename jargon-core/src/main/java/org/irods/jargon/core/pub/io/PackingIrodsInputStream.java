/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.irods.jargon.core.exception.JargonRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrap an iRODS input stream in an accumulating buffer that will emulate reads
 * from a continuous stream while fetching chunks from iRODS in a more optimal
 * size
 *
 * @author Mike Conway - DICE
 *
 */
public class PackingIrodsInputStream extends InputStream {
	private final IRODSFileInputStream irodsFileInputStream;
	private ByteArrayInputStream byteArrayInputStream = null;
	private final int bufferSizeForIrods;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private boolean done = false;

	public PackingIrodsInputStream(
			final IRODSFileInputStream irodsFileInputStream) {
		super();
		if (irodsFileInputStream == null) {
			throw new IllegalArgumentException("null irodsFileInputStream");
		}
		this.irodsFileInputStream = irodsFileInputStream;
		bufferSizeForIrods = irodsFileInputStream.getFileIOOperations()
				.getJargonProperties().getGetBufferSize();
		log.info("buffer size for gets from iRODS:{}", bufferSizeForIrods);
		if (bufferSizeForIrods <= 0) {
			throw new JargonRuntimeException(
					"misconfiguration in jargon.properties, getBufferSize is <= 0");
		}
	}

	private void checkAndInitializeNextByteInputStream() throws IOException {
		log.debug("checkAndInitializeNextByteInputStream()");
		if (done) {
			return;
		}
		if (byteArrayInputStream == null) {
			log.debug("Getting next buffer from iRODS...");
			fillByteBufferFromIrods();
		}
	}

	/**
	 * Fill up a new byte array input stream from iRODS using the requested
	 * buffer size, tries to fill that buffer
	 *
	 * @throws IOException
	 */
	private void fillByteBufferFromIrods() throws IOException {

		byte[] b = new byte[bufferSizeForIrods];

		int length = irodsFileInputStream.read(b);

		if (length == -1) {
			byteArrayInputStream = null;
			done = true;
		} else {
			byteArrayInputStream = new ByteArrayInputStream(b, 0, length);
		}
	}

	@Override
	public int read() throws IOException {
		byte buffer[] = new byte[1];

		int temp = this.read(buffer, 0, 1);

		if (temp < 0) {
			return -1;
		}
		return (buffer[0] & 0xFF);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(final byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] b, final int off, final int len)
			throws IOException {
		log.debug("read()");
		checkAndInitializeNextByteInputStream();
		/*
		 * I either have a byte buffer representing a chunk from iRODS, or it's
		 * null as I hit end of file and no data was read at all.
		 */
		if (byteArrayInputStream == null) {
			log.info("at end of stream");
			return -1;
		}

		int myOffset = off; // offset into current buffer
		int myLen = len; // length requested
		int readFromCurrent = 0;
		int lenToRead = 0;
		int totalRead = 0;
		/*
		 * loop while more to read for current request
		 */
		while (myLen > 0) { // fill successive buffers until all requested read
			// or end of data
			log.debug("looping to fill buffer while length remaining is:{}",
					myLen);

			if (byteArrayInputStream.available() > 0) { // get what's already
				// buffered
				log.debug("have available, copy into output array");
				lenToRead = Math.min(myLen, byteArrayInputStream.available());
				readFromCurrent = byteArrayInputStream.read(b, myOffset,
						lenToRead);
				myLen -= lenToRead;
				totalRead += readFromCurrent;
				myOffset += readFromCurrent;
				log.debug("read a total of:{} from current buffer",
						readFromCurrent);
			} else {
				log.debug("read all of current stream, get next buffer from iRODS...");
				fillByteBufferFromIrods();
				if (byteArrayInputStream == null) {
					log.debug("end of iRODS data");
					break;
				}
				/*
				 * I refilled the buffer, consult the available again by
				 * looping.
				 */
			}

		}

		log.debug("len for this read:{}", totalRead);
		/*
		 * If I've read some data, return that, otherwise, return a -1 to show
		 * end of data.
		 */
		return totalRead > 0 ? totalRead : -1;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(final long n) throws IOException {

		long mySkip = n;
		long skipped = 0;
		checkAndInitializeNextByteInputStream(); // if not read anything yet

		if (byteArrayInputStream.available() > 0) {
			long toSkip = Math.min(n, byteArrayInputStream.available());
			log.debug("skipping in byte buffer:{}", toSkip);
			mySkip -= toSkip;
			byteArrayInputStream.skip(toSkip);
			skipped += toSkip;
		}

		if (byteArrayInputStream.available() == 0) {
			byteArrayInputStream = null;// clear the stream so it's cached at
			// the
			// next pos
		}

		/*
		 * I got everything I could out of the stream, so skip further if need
		 * be in the actual underlying stream
		 */
		if (mySkip > 0) {
			skipped += irodsFileInputStream.skip(mySkip);
		}

		return skipped;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return irodsFileInputStream.available();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		irodsFileInputStream.close();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public synchronized void reset() throws IOException {
		irodsFileInputStream.reset();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {
		return irodsFileInputStream.markSupported();
	}

}
