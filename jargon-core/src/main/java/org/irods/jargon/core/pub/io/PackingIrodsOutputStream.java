/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A stream that presents the normal api, but accumulates bytes written until at
 * an optimal buffer size before sending to iRODS
 * <p>
 * Specifically, this wrapper around the iRODS output stream presents the normal
 * write methods, but will cache them up the the size of the putBufferSize
 * specified in jargon properties.
 * <p>
 * Flush and close are used and behave as expected, and will properly handle the
 * close of the underlying iRODS File and stream.
 *
 *
 * @author Mike Conway - DICE
 *
 */
public class PackingIrodsOutputStream extends OutputStream {

	private final Logger log = LogManager.getLogger(this.getClass());

	private int byteBufferSizeMax;
	private ByteArrayOutputStream byteArrayOutputStream = null;
	private final IRODSFileOutputStream irodsFileOutputStream;
	private long controlByteCount = 0;
	private long controlBytesIn = 0;

	/**
	 * Constructor
	 *
	 * @param irodsFileOutputStream
	 *            {@link IRODSFileOutputStream} that underlies this stream
	 */
	public PackingIrodsOutputStream(final IRODSFileOutputStream irodsFileOutputStream) {
		if (irodsFileOutputStream == null) {
			throw new IllegalArgumentException("null irodsFileOutputStream");
		}

		byteBufferSizeMax = irodsFileOutputStream.getFileIOOperations().getJargonProperties().getPutBufferSize();
		if (byteBufferSizeMax <= 0) {
			throw new IllegalStateException("cannot have a zero or negative buffer size");
		}
		byteArrayOutputStream = new ByteArrayOutputStream();
		this.irodsFileOutputStream = irodsFileOutputStream;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFileOutputStream#write(byte[], int,
	 * int)
	 */
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		log.debug("write()");
		controlBytesIn += len;
		log.debug("controlBytesIn:{}", controlBytesIn);
		int projectedLen = byteArrayOutputStream.size() + (len - off);
		log.info("projectedLen:{}", projectedLen);
		if (projectedLen < byteBufferSizeMax) {
			log.debug("less than buffer max so cache until full");
			byteArrayOutputStream.write(b, off, len);
		} else {
			log.debug("buffer is full, write to irods and reset");
			byteArrayOutputStream.write(b, off, len);
			flushAndResetBufferStream();
		}
	}

	private void flushAndResetBufferStream() throws IOException {
		if (byteArrayOutputStream.size() > 0) {
			irodsFileOutputStream.write(byteArrayOutputStream.toByteArray());
			controlByteCount += byteArrayOutputStream.size();
			log.debug("controlByteCount:{}", controlByteCount);
			byteArrayOutputStream.reset();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFileOutputStream#write(byte[])
	 */
	@Override
	public void write(final byte[] b) throws IOException {
		this.write(b, 0, b.length);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFileOutputStream#write(int)
	 */
	@Override
	public void write(final int b) throws IOException {
		byte buffer[] = { (byte) b };

		this.write(buffer, 0, buffer.length);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		flush();
		log.info("closing underlying stream");
		if (controlByteCount != controlBytesIn) {
			throw new IOException("control balance error in stream");
		}
		irodsFileOutputStream.close();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {
		log.debug("flush()...see if any bytes are buffered");
		if (byteArrayOutputStream.size() > 0) {
			log.debug("flushing buffered bytes and resetting");
			flushAndResetBufferStream();
			log.debug("now flushing the underlying iRODS stream");
			irodsFileOutputStream.flush();
		}
	}
}
