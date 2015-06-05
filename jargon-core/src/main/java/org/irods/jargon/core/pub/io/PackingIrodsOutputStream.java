/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A stream that presents the normal api, but accumulates bytes written until at
 * an optimal buffer size before sending to iRODS
 * 
 * @author Mike Conway - DICE
 * 
 */
public class PackingIrodsOutputStream extends OutputStream {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public final int BUFFER_SIZE = 4 * 1024 * 1024; // FIXME: make this a jargon
													// props later
	private int byteBufferSizeMax = 32 * 1024;
	private int ptr = 0;
	private ByteArrayOutputStream byteArrayOutputStream = null;
	private final IRODSFileOutputStream irodsFileOutputStream;

	/**
	 * @param irodsFile
	 * @param fileIOOperations
	 * @param openFlags
	 * @throws NoResourceDefinedException
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	public PackingIrodsOutputStream(IRODSFileOutputStream irodsFileOutputStream)
			throws NoResourceDefinedException, FileNotFoundException,
			JargonException {
		if (irodsFileOutputStream == null) {
			throw new IllegalArgumentException("null irodsFileOutputStream");
		}

		byteBufferSizeMax = irodsFileOutputStream.getFileIOOperations()
				.getJargonProperties().getPutBufferSize();
		if (byteBufferSizeMax <= 0) {
			throw new IllegalStateException(
					"cannot have a zero or negative buffer size");
		}
		byteArrayOutputStream = new ByteArrayOutputStream();
		this.irodsFileOutputStream = irodsFileOutputStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFileOutputStream#write(byte[],
	 * int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		log.debug("write()");
		int projectedLen = this.ptr + len;
		log.info("projectedLen:{}", projectedLen);
		int lenToHoldOver = len;

		if (projectedLen > byteBufferSizeMax) {
			log.info("greater than buffer max so write cache to iRODS");
			// would overflow the buff, so write partial to iRODS and start a
			// new buffer
			lenToHoldOver = projectedLen - byteBufferSizeMax;
			log.info("holdingOver:{}", lenToHoldOver);
			int lenToAddToBuff = len - lenToHoldOver;
			if (lenToAddToBuff > 0) {
				log.info("adding to buffer:{}", lenToAddToBuff);
				byteArrayOutputStream.write(b, off, lenToAddToBuff);
			}
			flushAndResetBufferStream();
		}

		byteArrayOutputStream.write(b, off, lenToHoldOver);
		ptr += lenToHoldOver;
		log.info("ptr after write is:{}", ptr);
	}

	private void flushAndResetBufferStream() throws IOException {
		irodsFileOutputStream.write(byteArrayOutputStream.toByteArray());
		byteArrayOutputStream.reset();
		ptr = 0;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFileOutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] b) throws IOException {
		this.write(b, 0, b.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFileOutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
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
		this.flush();
		log.info("closing underlying stream");
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
		if (ptr > 0) {
			log.debug("flushing buffered bytes and resetting");
			flushAndResetBufferStream();
			log.debug("now flushing the underlying iRODS stream");
			irodsFileOutputStream.flush();
		}
	}
}
