/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.io.OutputStream;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObject;

/**
 * Interface for an object that wraps file IO operations. Note that this
 * interface is not meant for direct access, rather, the various IRODS output
 * and input stream implementations act as a facade and delegate to this access
 * object.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public interface FileIOOperations extends IRODSAccessObject {

	public enum SeekWhenceType {
		SEEK_START, SEEK_CURRENT, SEEK_END
	}

	int write(int fd, byte buffer[], int offset, int length)
			throws JargonException;

	int fileRead(int fd, OutputStream destination, long length)
			throws JargonException;

	/**
	 * Reads up to <code>len</code> bytes of data from the input stream into an
	 * array of bytes. An attempt is made to read as many as <code>len</code>
	 * bytes, but a smaller number may be read, possibly zero. The number of
	 * bytes actually read is returned as an integer.
	 * 
	 * <p>
	 * This method blocks until input data is available, end of file is
	 * detected, or an exception is thrown.
	 * 
	 * <p>
	 * If <code>b</code> is <code>null</code>, a
	 * <code>JargonRuntimeException</code> is thrown.
	 * 
	 * <p>
	 * If <code>off</code> is negative, or <code>len</code> is negative, or
	 * <code>off+len</code> is greater than the length of the array
	 * <code>b</code>, then an <code>IndexOutOfBoundsException</code> is thrown.
	 * 
	 * <p>
	 * If <code>len</code> is zero, then no bytes are read and <code>0</code> is
	 * returned; otherwise, there is an attempt to read at least one byte. If no
	 * byte is available because the stream is at end of file, the value
	 * <code>-1</code> is returned; otherwise, at least one byte is read and
	 * stored into <code>b</code>.
	 * 
	 * <p>
	 * The first byte read is stored into element <code>b[off]</code>, the next
	 * one into <code>b[off+1]</code>, and so on. The number of bytes read is,
	 * at most, equal to <code>len</code>. Let <i>k</i> be the number of bytes
	 * actually read; these bytes will be stored in elements <code>b[off]</code>
	 * through <code>b[off+</code><i>k</i><code>-1]</code>, leaving elements
	 * <code>b[off+</code><i>k</i><code>]</code> through
	 * <code>b[off+len-1]</code> unaffected.
	 * 
	 * <p>
	 * In every case, elements <code>b[0]</code> through <code>b[off]</code> and
	 * elements <code>b[off+len]</code> through <code>b[b.length-1]</code> are
	 * unaffected.
	 * 
	 * <p>
	 * If the first byte cannot be read for any reason other than end of file,
	 * then an <code>IOException</code> is thrown. In particular, an
	 * <code>IOException</code> is thrown if the input stream has been closed.
	 * 
	 * <p>
	 * The <code>read(b,</code> <code>off,</code> <code>len)</code> method for
	 * class <code>InputStream</code> simply calls the method
	 * <code>read()</code> repeatedly. If the first such call results in an
	 * <code>IOException</code>, that exception is returned from the call to the
	 * <code>read(b,</code> <code>off,</code> <code>len)</code> method. If any
	 * subsequent call to <code>read()</code> results in a
	 * <code>IOException</code>, the exception is caught and treated as if it
	 * were end of file; the bytes read up to that point are stored into
	 * <code>b</code> and the number of bytes read before the exception occurred
	 * is returned. Subclasses are encouraged to provide a more efficient
	 * implementation of this method.
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @param off
	 *            the start offset in array <code>b</code> at which the data is
	 *            written.
	 * @param len
	 *            the maximum number of bytes to read.
	 * @return the total number of bytes read into the buffer, or
	 *         <code>-1</code> if there is no more data because the end of the
	 *         stream has been reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @exception NullPointerException
	 *                if <code>b</code> is <code>null</code>.
	 * @see java.io.InputStream#read()
	 */
	int fileRead(int fd, byte buffer[], int offset, int length)
			throws JargonException;

	/**
	 * Set the file position for the IRODS file to the specified position
	 * 
	 * @param fd
	 *            <code>int</code> containg the file desriptor for an open IRODS
	 *            file.
	 * @param seek
	 *            <code>long</code> that is the offset value
	 * @param whence
	 *            <code>int</code> that specifies the postion to compute the
	 *            offset from
	 * @return <code>long</code with the new offset.
	 * @throws IOException
	 */
	public long seek(int fd, long seek, SeekWhenceType whence)
			throws JargonException;
}
