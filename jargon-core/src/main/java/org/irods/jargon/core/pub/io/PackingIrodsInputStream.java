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
	private int ptr = 0;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public PackingIrodsInputStream(IRODSFileInputStream irodsFileInputStream) {
		super();
		if (this.irodsFileInputStream == null) {
			throw new IllegalArgumentException("null irodsFileInputStream");
		}
		this.irodsFileInputStream = irodsFileInputStream;
		this.bufferSizeForIrods = irodsFileInputStream.getFileIOOperations()
				.getJargonProperties().getGetBufferSize();
		log.info("buffer size for gets from iRODS:{}", bufferSizeForIrods);
		if (bufferSizeForIrods <= 0) {
			throw new JargonRuntimeException(
					"misconfiguration in jargon.properties, getBufferSize is <= 0");
		}
	}

	private void checkAndInitializeNextByteInputStream() throws IOException {
		log.debug("checkAndInitializeNextByteInputStream()");
		if (byteArrayInputStream == null) {
			log.debug("Getting next buffer from iRODS...");
			fillByteBufferFromIrods();
		}
	}

	private void fillByteBufferFromIrods() throws IOException {
		byte[] b = new byte[bufferSizeForIrods];
		irodsFileInputStream.read(b);
		byteArrayInputStream = new ByteArrayInputStream(b);
	}

	@Override
	public int read() throws IOException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(byte[] b) throws IOException {
		return super.read(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		log.debug("read()");
		checkAndInitializeNextByteInputStream();
		// how much are they trying to read? Is it in my buffer?
		int currentBuff = bufferSizeForIrods - ptr;
		log.debug("currently have {} in the buffer", currentBuff);
		if (currentBuff >= len) {
			// what I want to read is available in the current buffer
		} else {
			// see what is in the buff, and copy over, and then see if I need to
			// re-acquire the remainder
		}

		byteArrayInputStream.read(b, off, currentBuff);

		return super.read(b, off, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {
		// TODO Auto-generated method stub
		return super.skip(n);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		// TODO Auto-generated method stub
		return super.available();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public synchronized void reset() throws IOException {
		// TODO Auto-generated method stub
		super.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {
		// TODO Auto-generated method stub
		return super.markSupported();
	}

}
