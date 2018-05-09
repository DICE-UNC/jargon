/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.OutputStream;

import org.irods.jargon.core.checksum.ChecksumValue;
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

	int write(int fd, byte buffer[], int offset, int length) throws JargonException;

	int fileRead(int fd, OutputStream destination, long length) throws JargonException;

	/**
	 * Reads up to {@code len} bytes of data from the input stream into an array of
	 * bytes. An attempt is made to read as many as {@code len} bytes, but a smaller
	 * number may be read, possibly zero. The number of bytes actually read is
	 * returned as an integer.
	 *
	 * <p>
	 * This method blocks until input data is available, end of file is detected, or
	 * an exception is thrown.
	 *
	 * <p>
	 * If {@code b} is {@code null}, a {@code JargonRuntimeException} is thrown.
	 *
	 * <p>
	 * If {@code off} is negative, or {@code len} is negative, or {@code off+len} is
	 * greater than the length of the array {@code b}, then an
	 * {@code IndexOutOfBoundsException} is thrown.
	 *
	 * <p>
	 * If {@code len} is zero, then no bytes are read and {@code 0} is returned;
	 * otherwise, there is an attempt to read at least one byte. If no byte is
	 * available because the stream is at end of file, the value {@code -1} is
	 * returned; otherwise, at least one byte is read and stored into {@code b}.
	 *
	 * <p>
	 * The first byte read is stored into element {@code b[off]}, the next one into
	 * {@code b[off+1]}, and so on. The number of bytes read is, at most, equal to
	 * {@code len}. Let <i>k</i> be the number of bytes actually read; these bytes
	 * will be stored in elements {@code b[off]} through
	 * {@code b[off+}<i>k</i>{@code -1]}, leaving elements
	 * {@code b[off+}<i>k</i>{@code ]} through {@code b[off+len-1]} unaffected.
	 *
	 * <p>
	 * In every case, elements {@code b[0]} through {@code b[off]} and elements
	 * {@code b[off+len]} through {@code b[b.length-1]} are unaffected.
	 *
	 * <p>
	 * If the first byte cannot be read for any reason other than end of file, then
	 * an {@code IOException} is thrown. In particular, an {@code IOException} is
	 * thrown if the input stream has been closed.
	 *
	 * <p>
	 * The {@code read(b,} {@code off,} {@code len)} method for class
	 * {@code InputStream} simply calls the method {@code read()} repeatedly. If the
	 * first such call results in an {@code IOException}, that exception is returned
	 * from the call to the {@code read(b,} {@code off,} {@code len)} method. If any
	 * subsequent call to {@code read()} results in a {@code IOException}, the
	 * exception is caught and treated as if it were end of file; the bytes read up
	 * to that point are stored into {@code b} and the number of bytes read before
	 * the exception occurred is returned. Subclasses are encouraged to provide a
	 * more efficient implementation of this method.
	 * 
	 * @param fd
	 * @param buffer
	 * @param offset
	 * @param length
	 * @return {@code int} with the amount of data read
	 * @throws JargonException
	 */
	int fileRead(int fd, byte buffer[], int offset, int length) throws JargonException;

	/**
	 * Set the file position for the IRODS file to the specified position
	 *
	 * @param fd
	 *            {@code int} containg the file desriptor for an open IRODS file.
	 * @param seek
	 *            {@code long} that is the offset value
	 * @param whence
	 *            {@code int} that specifies the postion to compute the offset from
	 * @return {@code long} with the new offset.
	 * @throws JargonException
	 */
	public long seek(int fd, long seek, SeekWhenceType whence) throws JargonException;

	/**
	 * Convenience method to compute a checksum on a given iRODS file
	 * 
	 * @param irodsFileAbsolutePath
	 *            {@code String} with an iRODS absolute path to a file
	 * @return {@link ChecksumValue} with the resulting checksum
	 * @throws JargonException
	 */
	public ChecksumValue computeChecksumOnIrodsFile(final String irodsFileAbsolutePath) throws JargonException;
}
