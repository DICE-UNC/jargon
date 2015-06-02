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

/**
 * A stream that presents the normal api, but accumulates bytes written until at
 * an optimal buffer size before sending to iRODS
 * 
 * @author Mike Conway - DICE
 *
 */
public class PackingIrodsOutputStream extends OutputStream {

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
		int projectedLen = this.ptr + len;

		if (projectedLen > byteBufferSizeMax) {
			// would overflow the buff, so write partial to iRODS and start a
			// new buffer
			int lenToHoldOver = projectedLen - byteBufferSizeMax;
			int lenToAddToBuff = len - lenToHoldOver;
			byteArrayOutputStream.write(b, off, lenToAddToBuff);
			irodsFileOutputStream
		}

		super.write(b, off, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFileOutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		super.write(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFileOutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub
		super.write(b);
	}

	/**
	 * @return the byteBufferSize
	 */
	public int getByteBufferSize() {
		return byteBufferSize;
	}

	/**
	 * @param byteBufferSize
	 *            the byteBufferSize to set
	 */
	public void setByteBufferSize(int byteBufferSize) {
		this.byteBufferSize = byteBufferSize;
	}

}
