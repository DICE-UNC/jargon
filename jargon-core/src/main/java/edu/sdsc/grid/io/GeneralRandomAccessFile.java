//  Copyright (c) 2005, Regents of the University of California
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//    * Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//    * Neither the name of the University of California, San Diego (UCSD) nor
//  the names of its contributors may be used to endorse or promote products
//  derived from this software without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
//  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
//  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//
//  FILE
//  GeneralRandomAccessFile.java  -  edu.sdsc.grid.io.GeneralRandomAccessFile
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.GeneralRandomAccessFile
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UTFDataFormatException;

/**
 * <DIV ALIGN=JUSTIFY> Instances of this class support I/O on random-access
 * binary files. Methods on the class support conversion between host and file
 * byte orders and word sizes.
 * <P>
 * Two sets of read methods are provided:
 * <P>
 * <UL>
 * <LI>Single-value read methods such as int readInt().
 * <LI>Multi-value read methods such as void readInts(int* values,int nValues).
 * </UL>
 * <P>
 * Single-value read methods read a single short, int, long, etc., and return
 * the value.
 * <P>
 * Multi-value read methods read multiple consecutive shorts, ints, longs, etc.,
 * and return them in a given array.
 * <P>
 * Two sets of write methods are provided:
 * <P>
 * <UL>
 * <LI>Single-value write methods such as int writeInt(int value).
 * <LI>Multi-value write methods such as void writeInts(int* values,int
 * nValues).
 * </UL>
 * <P>
 * Single-value write methods write a single short, int, long, etc.
 * <P>
 * Multi-value write methods write multiple shorts, ints, longs, etc., from an
 * array of values.
 * <P>
 * <B>Note:</B> This class offers features that extend those found in
 * java.io.RandomAccessFile. However, it <I>is not</I> a subclass, due to the
 * unfortunate use of final methods in java.io.RandomAccessFile. </DIV>
 * <P>
 * 
 * @author Lucas Gilbert
 * @author David R. Nadeau, San Diego Supercomputer Center
 * @since JARGON1.0
 */

public abstract class GeneralRandomAccessFile implements DataOutput, DataInput// ,
// Cloneable
{
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------
	/**
	 * Used to set the offset for seek calls to the beginning of the file.
	 */
	// public static final int SEEK_SET = 0;
	/**
	 * Used to set the offset for seek calls to the beginning of the file. Same
	 * as SEEK_SET (as in C/C++, but that name is less intuitive)
	 */
	public static final int SEEK_START = 0;

	/**
	 * Used to set the offset for seek calls to the current offset of the file.
	 */
	// public static final int SEEK_CUR = 1;
	/**
	 * Used to set the offset for seek calls to the current offset of the file.
	 * Same as SEEK_CUR (as in C/C++, but that name is less intuitive)
	 */
	public static final int SEEK_CURRENT = 1;

	/**
	 * Used to set the offset for seek calls to the end of the file.
	 */
	public static final int SEEK_END = 2;

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------
	/**
	 * Holds the server information used by this file.
	 */
	protected int fd = -1;

	/**
	 * 0, if read only 1, if read/write 2, if sync r/w 3, dsync r/w
	 */
	protected int rw;

	/**
	 * Holds the file's binary data format.
	 */
	protected BinaryDataFormat fileFormat;

	/**
	 * Holds a true if the host and file formats have reversed byte orders,
	 * forcing bytes to have their order swapped on reads and writes.
	 */
	protected boolean swapNeeded;

	/**
	 * Has this random access file been closed. A closed random access file
	 * cannot perform input or output operations and cannot be reopened.
	 */
	protected boolean isClosed;

	/**
	 * Has this random access file been closed. A closed random access file
	 * cannot perform input or output operations and cannot be reopened.
	 */
	protected GeneralFile file;

	protected String mode;

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	/**
	 * Creates a random access file stream to read from, and optionally to write
	 * to, the file specified by the {@link GeneralFile} argument. A new file
	 * descriptor is obtained which represents the connection to the file.
	 * 
	 * <a name="mode">
	 * <p>
	 * The <tt>mode</tt> argument specifies the access mode in which the file is
	 * to be opened. The permitted values and their meanings are:
	 * 
	 * <blockquote>
	 * <table>
	 * <tr>
	 * <td valign="top"><tt>"r"</tt></td>
	 * <td>Open for reading only. Invoking any of the <tt>write</tt> methods of
	 * the resulting object will cause an {@link java.io.IOException} to be
	 * thrown.</td>
	 * </tr>
	 * <tr>
	 * <td valign="top"><tt>"rw"</tt></td>
	 * <td>Open for reading and writing. If the file does not already exist then
	 * an attempt will be made to create it.</td>
	 * </tr>
	 * <tr>
	 * <td valign="top"><tt>"rws"</tt></td>
	 * <td>Open for reading and writing, as with <tt>"rw"</tt>, and also require
	 * that every update to the file's content or metadata be written
	 * synchronously to the underlying storage device.</td>
	 * </tr>
	 * <tr>
	 * <td valign="top"><tt>"rwd"&nbsp;&nbsp;</tt></td>
	 * <td>Open for reading and writing, as with <tt>"rw"</tt>, and also require
	 * that every update to the file's content be written synchronously to the
	 * underlying storage device.</td>
	 * </tr>
	 * </table>
	 * </blockquote>
	 * 
	 * On construction a check is made to see if read access to the file is
	 * allowed. If the mode allows writing, write access to the file is also
	 * checked.
	 * 
	 * @param file
	 *            the file object
	 * @param mode
	 *            the access mode, as described <a href="#mode">above</a>
	 * @throws IOException
	 *             If an I/O error occurs
	 * @throws IllegalArgumentException
	 *             if the mode argument is not equal to one of <tt>"r"</tt>,
	 *             <tt>"rw"</tt>, <tt>"rws"</tt>, or <tt>"rwd"</tt>
	 * @throws FileNotFoundException
	 *             If the file exists but is a directory rather than a regular
	 *             file, or cannot be opened or created for any other reason
	 * @throws SecurityException
	 *             If denied read access to the file or the mode is "rw" and
	 *             denied write access to the file.
	 */
	public GeneralRandomAccessFile(final GeneralFile file, final String mode)
			throws IOException, SecurityException {
		this.file = file;
		this.mode = mode;

		open(file);
		fileFormat = new BinaryDataFormat();
	}

	/**
	 * Finalize the object by closing the file.
	 * <P>
	 * 
	 * @throws throws Throwable If the file cannot be closed.
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
	}

	/**
	 * Sets the boolean rw value according to the mode and checks that such
	 * permissions are available.
	 * <P>
	 * "r" would allow for read-only access. "rw" would allow for read-write
	 * access. Case-insensitive.
	 * 
	 * @throws IllegalArgumentException
	 *             the mode is invalid.
	 * @throws SecurityException
	 *             if the permissions are wrong.
	 */
	protected void rwCheck(final GeneralFile file, String mode)
			throws IllegalArgumentException, SecurityException {
		mode = mode.toLowerCase();

		if (mode.equals("r")) {
			rw = 0;
		} else if (mode.equals("rw")) {
			rw = 1;
		} else if (mode.equals("rws")) {
			rw = 2;
		} else if (mode.equals("rwd")) {
			rw = 3;
		} else {
			throw new IllegalArgumentException("Illegal mode \"" + mode
					+ "\" must be one of \"r\", \"rw\", \"rws\", or \"rwd\"");
		}

		if (!file.canRead()) {
			throw new SecurityException(
					"Wrong permissions to access this file.");
		}

		if (rw > 0) {
			if (!file.canWrite()) {
				throw new SecurityException(
						"Wrong permissions to access this file.");
			}
		}
	}

	/**
	 * Returns the file descriptor associated with this stream. </p>
	 * 
	 * @return the file descriptor object associated with this stream.
	 * @throws IllegalArgumentException
	 *             if an error occurs.
	 * @see java.io.FileDescriptor
	 */
	public int getFD() {
		if (fd != -1) {
			return fd;
		}

		throw new IllegalArgumentException("No file descriptor available.");
	}

	/**
	 * Returns the abstract filepath associated with this stream. </p>
	 * 
	 * @return the abstract filepath object associated with this stream.
	 */
	public GeneralFile getFile() {
		return file;
	}

	/**
	 * Returns the file system object for this file.
	 * 
	 * @throws NullPointerException
	 *             if fileSystem is null.
	 * @return GeneralFileSystem
	 */
	public GeneralFileSystem getFileSystem() {
		return file.getFileSystem();
	}

	/**
	 * Opens a file and returns the file descriptor. The file is opened in
	 * read-write mode if writeable is true, else the file is opened as
	 * read-only. If the <code>name</code> refers to a directory, an IOException
	 * is thrown.
	 * 
	 * @param name
	 *            the name of the file
	 * @param mode
	 *            the mode flags, a combination of the O_ constants defined
	 *            above
	 */
	protected abstract void open(GeneralFile file) throws IOException;

	@Override
	public String toString() {
		switch (rw) {
		case 0:
			return file.getParent() + "/" + file.getName() + " : r";
		case 1:
			return file.getParent() + "/" + file.getName() + " : rw";
		case 2:
			return file.getParent() + "/" + file.getName() + " : rws";
		case 3:
			return file.getParent() + "/" + file.getName() + " : rwd";
		}

		return file.getParent() + "/" + file.getName();
	}

	/**
	 * Reads a byte of data from this file. The byte is returned as an integer
	 * in the range 0 to 255 (<code>0x00-0x0ff</code>). This method blocks if no
	 * input is yet available.
	 * <p>
	 * Although <code>GeneralRandomAccessFile</code> is not a subclass of
	 * <code>InputStream</code>, this method behaves in exactly the same way as
	 * java.io.InputStream.read().
	 * 
	 * @return the next byte of data, or <code>-1</code> if the end of the file
	 *         has been reached.
	 * @throws IOException
	 *             if an I/O error occurs. Not thrown if end-of-file has been
	 *             reached.
	 */
	public int read() throws IOException {
		// Subclasses should probably rewrite this.
		byte buffer[] = new byte[1];
		int read = readBytes(buffer, 0, 1);
		if (read >= 0) {
			return buffer[0];
		}
		return -1;
	}

	/**
	 * Reads a sub array as a sequence of bytes.
	 * 
	 * @param b
	 *            the data to be written
	 * @param off
	 *            the start offset in the data
	 * @param len
	 *            the number of bytes that are written
	 * @throws IOException
	 *             If an I/O error has occurred.
	 */
	protected abstract int readBytes(byte b[], int offset, int len)
			throws IOException;

	/**
	 * Reads up to <code>len</code> bytes of data from this file into an array
	 * of bytes. This method blocks until at least one byte of input is
	 * available.
	 * <p>
	 * Although <code>GeneralRandomAccessFile</code> is not a subclass of
	 * <code>InputStream</code>, this method behaves in the exactly the same way
	 * as java.io.InputStream.read(byte[], int, int).
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @param off
	 *            the start offset of the data.
	 * @param len
	 *            the maximum number of bytes read.
	 * @return the total number of bytes read into the buffer, or
	 *         <code>-1</code> if there is no more data because the end of the
	 *         file has been reached.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public int read(final byte b[], final int offset, final int len)
			throws IOException {
		return readBytes(b, offset, len);
	}

	/**
	 * Reads up to <code>b.length</code> bytes of data from this file into an
	 * array of bytes. This method blocks until at least one byte of input is
	 * available.
	 * <p>
	 * Although <code>GeneralRandomAccessFile</code> is not a subclass of
	 * <code>InputStream</code>, this method behaves in the exactly the same way
	 * as java.io.InputStream.read(byte[]).
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @return the total number of bytes read into the buffer, or
	 *         <code>-1</code> if there is no more data because the end of this
	 *         file has been reached.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public int read(final byte b[]) throws IOException {
		return readBytes(b, 0, b.length);
	}

	/**
	 * Reads <code>b.length</code> bytes from this file into the byte array,
	 * starting at the current file pointer. This method reads repeatedly from
	 * the file until the requested number of bytes are read. This method blocks
	 * until the requested number of bytes are read, the end of the stream is
	 * detected, or an exception is thrown.
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @throws EOFException
	 *             if this file reaches the end before reading all the bytes.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public void readFully(final byte b[]) throws IOException {
		readFully(b, 0, b.length);
	}

	/**
	 * Reads exactly <code>len</code> bytes from this file into the byte array,
	 * starting at the current file pointer. This method reads repeatedly from
	 * the file until the requested number of bytes are read. This method blocks
	 * until the requested number of bytes are read, the end of the stream is
	 * detected, or an exception is thrown.
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @param off
	 *            the start offset of the data.
	 * @param len
	 *            the number of bytes to read.
	 * @throws EOFException
	 *             if this file reaches the end before reading all the bytes.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public void readFully(final byte b[], final int offset, final int len)
			throws IOException {
		int inc = 0;
		do {
			int count = this.read(b, offset + inc, len - inc);

			if (count < 0) {
				throw new EOFException();
			}

			inc += count;
		} while (inc < len);
	}

	/**
	 * Attempts to skip over <code>n</code> bytes of input discarding the
	 * skipped bytes.
	 * <p>
	 * 
	 * This method may skip over some smaller number of bytes, possibly zero.
	 * This may result from any of a number of conditions; reaching end of file
	 * before <code>n</code> bytes have been skipped is only one possibility.
	 * This method never throws an <code>EOFException</code>. The actual number
	 * of bytes skipped is returned. If <code>n</code> is negative, no bytes are
	 * skipped.
	 * 
	 * @param n
	 *            the number of bytes to be skipped.
	 * @return the actual number of bytes skipped.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public int skipBytes(final int n) throws IOException {
		if (n <= 0) {
			return 0;
		}

		long position = getFilePointer();
		long length = length();
		long newPosition = position + n;
		if (newPosition > length) {
			newPosition = length;
		}
		seek(newPosition);

		return (int) (newPosition - position);
	}

	/**
	 * Writes the specified byte to this file. The write starts at the current
	 * file pointer.
	 * 
	 * @param b
	 *            the <code>byte</code> to be written.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public void write(final int b) throws IOException {
		byte buffer[] = { (byte) b };

		writeBytes(buffer, 0, buffer.length);
	}

	/**
	 * Writes a sub array as a sequence of bytes.
	 * 
	 * @param b
	 *            the data to be written
	 * 
	 * @param off
	 *            the start offset in the data
	 * @param len
	 *            the number of bytes that are written
	 * @throws IOException
	 *             If an I/O error has occurred.
	 */
	protected abstract void writeBytes(byte b[], int offset, int len)
			throws IOException;

	/**
	 * Writes <code>b.length</code> bytes from the specified byte array to this
	 * file, starting at the current file pointer.
	 * 
	 * @param b
	 *            the data.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public void write(final byte b[]) throws IOException {
		writeBytes(b, 0, b.length);
	}

	/**
	 * Writes <code>len</code> bytes from the specified byte array starting at
	 * offset <code>off</code> to this file.
	 * 
	 * @param b
	 *            the data.
	 * @param off
	 *            the start offset in the data.
	 * @param len
	 *            the number of bytes to write.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public void write(final byte b[], final int offset, final int len)
			throws IOException {
		writeBytes(b, offset, len);
	}

	/**
	 * Writes a String to the file, by first converting the string to a byte
	 * array, using the <code>String.getBytes()<code> method.
	 * 
	 * @param text
	 *            the data.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public void write(final String text) throws IOException {
		writeBytes(text.getBytes(), 0, text.length());
	}

	/**
	 * Returns the current offset in this file.
	 * 
	 * @return the offset from the beginning of the file, in bytes, at which the
	 *         next read or write occurs.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public abstract long getFilePointer() throws IOException;

	/**
	 * Sets the file-pointer offset, measured from the beginning of this file,
	 * at which the next read or write occurs. The offset may be set beyond the
	 * end of the file. Setting the offset beyond the end of the file does not
	 * change the file length. The file length will change only by writing after
	 * the offset has been set beyond the end of the file.
	 * 
	 * @param pos
	 *            the offset position, measured in bytes from the beginning of
	 *            the file, at which to set the file pointer.
	 * @throws IOException
	 *             if <code>pos</code> is less than <code>0</code> or if an I/O
	 *             error occurs.
	 */
	public void seek(final long position) throws IOException {
		seek(position, SEEK_START);
	}

	/**
	 * Sets the file-pointer offset at which the next read or write occurs. The
	 * offset may be set beyond the end of the file. Setting the offset beyond
	 * the end of the file does not change the file length. The file length will
	 * change only by writing after the offset has been set beyond the end of
	 * the file.
	 * 
	 * @param pos
	 *            the offset position, measured in bytes from the at which to
	 *            set the file pointer.
	 * @param origin
	 *            a Sets offset for the beginning of the seek.<br>
	 *            SEEK_START - sets the offset from the beginning of the file.
	 *            SEEK_CURRENT - sets the offset from the current position of
	 *            the filePointer.<br>
	 *            SEEK_END - sets the offset from the end of the file.<br>
	 * 
	 * @throws IOException
	 *             if <code>pos</code> is less than <code>0</code> or if an I/O
	 *             error occurs.
	 */
	public abstract void seek(long position, int origin) throws IOException;

	/**
	 * Returns the length of this file.
	 * 
	 * @return the length of this file, measured in bytes.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public abstract long length() throws IOException;

	/**
	 * Sets the length of this file.
	 * <P>
	 * If the present length of the file as returned by the <code>length</code>
	 * method is greater than the <code>newLength</code> argument then the file
	 * will be truncated. In this case, if the file offset as returned by the
	 * <code>getFilePointer</code> method is greater then <code>newLength</code>
	 * then after this method returns the offset will be equal to
	 * <code>newLength</code>.
	 * <P>
	 * If the present length of the file as returned by the <code>length</code>
	 * method is smaller than the <code>newLength</code> argument then the file
	 * will be extended. In this case, the contents of the extended portion of
	 * the file are not defined.
	 * 
	 * @param newLength
	 *            The desired length of the file
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public abstract void setLength(long newLength) throws IOException;

	/**
	 * Closes this random access file stream and releases any system resources
	 * associated with the stream. A closed random access file cannot perform
	 * input or output operations and cannot be reopened.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public abstract void close() throws IOException;

	/**
	 * Returns true if this random access file is closed. A closed random access
	 * file cannot perform input or output operations and cannot be reopened.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public boolean isClosed() {
		return isClosed;
	}

	/**
	 * Reads a <code>boolean</code> from this file. This method reads a single
	 * byte from the file, starting at the current file pointer. A value of
	 * <code>0</code> represents <code>false</code>. Any other value represents
	 * <code>true</code>. This method blocks until the byte is read, the end of
	 * the stream is detected, or an exception is thrown.
	 * 
	 * @return the <code>boolean</code> value read.
	 * @throws EOFException
	 *             if this file has reached the end.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public boolean readBoolean() throws IOException {
		int value = this.read();
		if (value < 0) {
			throw new EOFException();
		}

		return (value != 0);
	}

	/**
	 * Reads a signed eight-bit value from this file. This method reads a byte
	 * from the file, starting from the current file pointer. If the byte read
	 * is <code>b</code>, where
	 * <code>0&nbsp;&lt;=&nbsp;b&nbsp;&lt;=&nbsp;255</code>, then the result is:
	 * <blockquote>
	 * 
	 * <pre>
	 * (byte) (b)
	 * </pre>
	 * 
	 * </blockquote>
	 * <P>
	 * This method blocks until the byte is read, the end of the stream is
	 * detected, or an exception is thrown.
	 * 
	 * @return the next byte of this file as a signed eight-bit
	 *         <code>byte</code>.
	 * @throws EOFException
	 *             if this file has reached the end.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public byte readByte() throws IOException {
		int value = this.read();
		if (value < 0) {
			throw new EOFException();
		}

		return (byte) (value);
	}

	/**
	 * Reads an unsigned eight-bit number from this file. This method reads a
	 * byte from this file, starting at the current file pointer, and returns
	 * that byte.
	 * <P>
	 * This method blocks until the byte is read, the end of the stream is
	 * detected, or an exception is thrown.
	 * 
	 * @return the next byte of this file, interpreted as an unsigned eight-bit
	 *         number.
	 * @throws EOFException
	 *             if this file has reached the end.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public int readUnsignedByte() throws IOException {
		int value = this.read();
		if (value < 0) {
			throw new EOFException();
		}

		return value;
	}

	/**
	 * Reads input bytes and returns a short value. The number of bytes read is
	 * equal to the size of a short using the current binary data format.
	 * <P>
	 * The data read by this method is converted from the file's binary data
	 * format to that of host running this application. This may involve a
	 * change in byte order and data type size.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeShort
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @return the value read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public short readShort() throws EOFException, IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		readFully(bytes, 0, fileFormat.getShortSize());
		return fileFormat.shortValue(bytes);
	}

	/**
	 * Reads input bytes and returns an unsigned value. The number of bytes read
	 * is equal to the size of an unsigned short using the current binary data
	 * format.
	 * <P>
	 * The data read by this method is converted from the file's binary data
	 * format to that of host running this application. This may involve a
	 * change in byte order and data type size.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeInt
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @return the unsigned value
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public int readUnsignedShort() throws IOException, EOFException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		readFully(bytes, 0, fileFormat.getShortSize());
		return fileFormat.unsignedShortValue(bytes);
	}

	/**
	 * Reads a Unicode character from this file. This method reads two bytes
	 * from the file, starting at the current file pointer. If the bytes read,
	 * in order, are <code>b1</code> and <code>b2</code>, where
	 * <code>0&nbsp;&lt;=&nbsp;b1,&nbsp;b2&nbsp;&lt;=&nbsp;255</code>, then the
	 * result is equal to: <blockquote>
	 * 
	 * <pre>
	 * (char) ((b1 &lt;&lt; 8) | b2)
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * This method blocks until the two bytes are read, the end of the stream is
	 * detected, or an exception is thrown.
	 * 
	 * @return the next two bytes of this file as a Unicode character.
	 * @throws EOFException
	 *             if this file reaches the end before reading two bytes.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public char readChar() throws IOException {
		int s = this.read();
		int t = this.read();
		if ((s | t) < 0) {
			throw new EOFException();
		}

		return (char) ((s << 8) + (t << 0));
	}

	/**
	 * Reads input bytes and returns an int value. The number of bytes read is
	 * equal to the size of an int using the current binary data format.
	 * <P>
	 * The data read by this method is converted from the file's binary data
	 * format to that of host running this application. This may involve a
	 * change in byte order and data type size.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeInt
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @return the value read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public int readInt() throws EOFException, IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		readFully(bytes, 0, fileFormat.getIntSize());
		return fileFormat.intValue(bytes);
	}

	/**
	 * Reads input bytes and returns an unsigned value. The number of bytes read
	 * is equal to the size of an unsigned int using the current binary data
	 * format.
	 * <P>
	 * The data read by this method is converted from the file's binary data
	 * format to that of host running this application. This may involve a
	 * change in byte order and data type size.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeInt
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @return the unsigned value
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public long readUnsignedInt() throws IOException, EOFException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		readFully(bytes, 0, fileFormat.getIntSize());
		return fileFormat.unsignedIntValue(bytes);
	}

	/**
	 * Reads input bytes and returns a long value. The number of bytes read is
	 * equal to the size of a long using the current binary data format.
	 * <P>
	 * The data read by this method is converted from the file's binary data
	 * format to that of host running this application. This may involve a
	 * change in byte order and data type size.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeLong
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @return the value read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public long readLong() throws EOFException, IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		readFully(bytes, 0, fileFormat.getLongSize());
		return fileFormat.longValue(bytes);
	}

	/**
	 * Reads input bytes and returns an unsigned value. The number of bytes read
	 * is equal to the size of an unsigned long using the current binary data
	 * format.
	 * <P>
	 * The data read by this method is converted from the file's binary data
	 * format to that of host running this application. This may involve a
	 * change in byte order and data type size.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeLong
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @return the unsigned value
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public long readUnsignedLong() throws IOException, EOFException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		readFully(bytes, 0, fileFormat.getLongSize());
		return fileFormat.unsignedLongValue(bytes);
	}

	/**
	 * Reads input bytes and returns an unsigned value. The number of bytes read
	 * is equal to the size of an unsigned long long using the current binary
	 * data format.
	 * <P>
	 * The data read by this method is converted from the file's binary data
	 * format to that of host running this application. This may involve a
	 * change in byte order and data type size.
	 * <P>
	 * This method is suitable for reading the bytes written by the
	 * writeLongLong method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @return the unsigned value
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public long readUnsignedLongLong() throws IOException, EOFException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		readFully(bytes, 0, fileFormat.getLongLongSize());
		return fileFormat.unsignedLongLongValue(bytes);
	}

	/**
	 * Reads input bytes and returns a signed value. The number of bytes read is
	 * equal to the size of a long long using the current binary data format.
	 * <P>
	 * The data read by this method is converted from the file's binary data
	 * format to that of host running this application. This may involve a
	 * change in byte order and data type size.
	 * <P>
	 * This method is suitable for reading the bytes written by the
	 * writeLongLong method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @return the value
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public long readLongLong() throws IOException, EOFException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		readFully(bytes, 0, fileFormat.getLongLongSize());
		return fileFormat.longLongValue(bytes);
	}

	/**
	 * Reads input bytes and returns a signed value. The number of bytes read is
	 * equal to the size of a long double using the current binary data format.
	 * <P>
	 * The data read by this method is converted from the file's binary data
	 * format to that of host running this application. This may involve a
	 * change in byte order and data type size.
	 * <P>
	 * This method is suitable for reading the bytes written by the
	 * writeLongDouble method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @return the value
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public double readLongDouble() throws IOException, EOFException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		readFully(bytes, 0, fileFormat.getLongDoubleSize());
		return fileFormat.longDoubleValue(bytes);
	}

	/**
	 * Reads input bytes and returns a float value. The number of bytes read is
	 * equal to the size of a float using the current binary data format.
	 * <P>
	 * The data read by this method is converted from the file's binary data
	 * format to that of host running this application. This may involve a
	 * change in byte order and data type size.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeFloat
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @return the value read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public float readFloat() throws EOFException, IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		readFully(bytes, 0, fileFormat.getFloatSize());
		return fileFormat.floatValue(bytes);
	}

	/**
	 * Reads input bytes and returns a double value. The number of bytes read is
	 * equal to the size of a double using the current binary data format.
	 * <P>
	 * The data read by this method is converted from the file's binary data
	 * format to that of host running this application. This may involve a
	 * change in byte order and data type size.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeDouble
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @return the value read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public double readDouble() throws EOFException, IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		readFully(bytes, 0, fileFormat.getDoubleSize());
		return fileFormat.doubleValue(bytes);
	}

	/**
	 * Reads the next line of text from this file. This method successively
	 * reads bytes from the file, starting at the current file pointer, until it
	 * reaches a line terminator or the end of the file. Each byte is converted
	 * into a character by taking the byte's value for the lower eight bits of
	 * the character and setting the high eight bits of the character to zero.
	 * This method does not, therefore, support the full Unicode character set.
	 * 
	 * <p>
	 * A line of text is terminated by a carriage-return character (
	 * <code>'&#92;r'</code>), a newline character (<code>'&#92;n'</code>), a
	 * carriage-return character immediately followed by a newline character, or
	 * the end of the file. Line-terminating characters are discarded and are
	 * not included as part of the string returned.
	 * 
	 * <p>
	 * This method blocks until a newline character is read, a carriage return
	 * and the byte following it are read (to see if it is a newline), the end
	 * of the file is reached, or an exception is thrown.
	 * 
	 * @return the next line of text from this file, or null if end of file is
	 *         encountered before even one byte is read.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public String readLine() throws IOException {
		char ch = (char) read();
		StringBuffer line = new StringBuffer();

		while ((ch != '\n') && (ch != '\r') && (ch != (char) -1)) {
			line.append(ch);
			ch = (char) read();
		}

		if ((ch == '\r') && ((read()) != '\n')) {
			seek(getFilePointer());
		} else if ((ch == -1) && (line.length() == 0)) {
			return null;
		}

		return line.toString();
	}

	/*
	 * Reads in a string from this file. The string has been encoded using a
	 * modified UTF-8 format. <p> The first two bytes are read, starting from
	 * the current file pointer, as if by <code>readUnsignedShort</code>. This
	 * value gives the number of following bytes that are in the encoded string,
	 * not the length of the resulting string. The following bytes are then
	 * interpreted as bytes encoding characters in the UTF-8 format and are
	 * converted into characters. <p> This method blocks until all the bytes are
	 * read, the end of the stream is detected, or an exception is thrown.
	 * 
	 * @return a Unicode string.
	 * 
	 * @throws EOFException if this file reaches the end before reading all the
	 * bytes.
	 * 
	 * @throws IOException if an I/O error occurs.
	 * 
	 * @throws UTFDataFormatException if the bytes do not represent valid UTF-8
	 * encoding of a Unicode string.
	 * 
	 * @see edu.sdsc.grid.io.GeneralRandomAccessFile#readUnsignedShort()
	 */
	/**
	 * @throws UnsupportedOperationException
	 *             not yet supported.
	 */
	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Writes a <code>boolean</code> to the file as a one-byte value. The value
	 * <code>true</code> is written out as the value <code>(byte)1</code>; the
	 * value <code>false</code> is written out as the value <code>(byte)0</code>
	 * . The write starts at the current position of the file pointer.
	 * 
	 * @param v
	 *            a <code>boolean</code> value to be written.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public void writeBoolean(final boolean v) throws IOException {
		write(v ? 1 : 0);
	}

	/**
	 * Writes a <code>byte</code> to the file as a one-byte value. The write
	 * starts at the current position of the file pointer.
	 * 
	 * @param v
	 *            a <code>byte</code> value to be written.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public void writeByte(final int v) throws IOException {
		write(v);
	}

	/**
	 * Writes bytes to the output stream to represent the short value of the
	 * argument. The number of bytes written is equal to the size of a short
	 * using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readShort method of
	 * interface BinaryDataInput, which will then return a short equal to
	 * (short)v.
	 * <P>
	 * 
	 * @param v
	 *            the short value to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public void writeShort(final int v) throws IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		int nBytes = fileFormat.encodeShort(v, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the char value of the
	 * argument. The number of bytes written is equal to the size of a char
	 * using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readChar method of
	 * interface BinaryDataInput, which will then return a char equal to
	 * (char)v.
	 * <P>
	 * 
	 * @param v
	 *            the short value to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public void writeChar(final int v) throws IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		int nBytes = fileFormat.encodeShort(v, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the int value of the
	 * argument. The number of bytes written is equal to the size of an int
	 * using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readInt method of
	 * interface BinaryDataInput, which will then return a int equal to (int)v.
	 * <P>
	 * 
	 * @param v
	 *            the int value to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public void writeInt(final int v) throws IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		int nBytes = fileFormat.encodeInt(v, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the long value of the
	 * argument. The number of bytes written is equal to the size of a long
	 * using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readLong method of
	 * interface BinaryDataInput, which will then return a long equal to
	 * (long)v.
	 * <P>
	 * 
	 * @param v
	 *            the long value to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public void writeLong(final long v) throws IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		int nBytes = fileFormat.encodeLong(v, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the long long value of the
	 * argument. The number of bytes written is equal to the size of a long long
	 * using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readLongLong method
	 * of interface BinaryDataInput, which will then return a long long equal to
	 * (long long)v.
	 * <P>
	 * 
	 * @param v
	 *            the long long value to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void writeLongLong(final long v) throws IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		int nBytes = fileFormat.encodeLongLong(v, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the long double value of
	 * the argument. The number of bytes written is equal to the size of a long
	 * double using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readLongDouble method
	 * of interface BinaryDataInput, which will then return a long double equal
	 * to (long double)v.
	 * <P>
	 * 
	 * @param v
	 *            the long double value to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void writeLongDouble(final double v) throws IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		int nBytes = fileFormat.encodeLongDouble(v, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the float value of the
	 * argument. The number of bytes written is equal to the size of a float
	 * using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readFloat method of
	 * interface BinaryDataInput, which will then return a float equal to
	 * (float)v.
	 * <P>
	 * 
	 * @param v
	 *            the float value to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public void writeFloat(final float v) throws IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		int nBytes = fileFormat.encodeFloat(v, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the double value of the
	 * argument. The number of bytes written is equal to the size of a double
	 * using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readFloat method of
	 * interface BinaryDataInput, which will then return a double equal to
	 * (double)v.
	 * <P>
	 * 
	 * @param v
	 *            the double value to be written
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	public void writeDouble(final double v) throws IOException {
		byte bytes[] = new byte[Host.MAX_TYPE_SIZE];
		int nBytes = fileFormat.encodeDouble(v, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes the string to the file as a sequence of bytes. Each character in
	 * the string is written out, in sequence, by discarding its high eight
	 * bits. The write starts at the current position of the file pointer.
	 * 
	 * @param s
	 *            a string of bytes to be written.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public void writeBytes(final String s) throws IOException {
		writeBytes(s.getBytes(), 0, s.length());
	}

	/**
	 * Writes a string to the file as a sequence of characters. Each character
	 * is written to the data output stream as if by the <code>writeChar</code>
	 * method. The write starts at the current position of the file pointer.
	 * 
	 * @param s
	 *            a <code>String</code> value to be written.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @see edu.sdsc.grid.io.GeneralRandomAccessFile#writeChar(int)
	 */
	@Override
	public void writeChars(final String s) throws IOException {
		char[] chars = s.toCharArray();
		for (int i = 0; i < s.length(); i++) {
			writeChar(chars[i]);
		}
	}

	/**
	 * Writes a string to the file using UTF-8 encoding in a machine-independent
	 * manner.
	 * <p>
	 * First, two bytes are written to the file, starting at the current file
	 * pointer, as if by the <code>writeShort</code> method giving the number of
	 * bytes to follow. This value is the number of bytes actually written out,
	 * not the length of the string. Following the length, each character of the
	 * string is output, in sequence, using the UTF-8 encoding for each
	 * character.
	 * 
	 * @param str
	 *            a string to be written.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public void writeUTF(final String str) throws IOException {

		byte buffer[] = null;
		int i = 0, n = 0, m = 0;
		int length = str.length();
		int utflength = 0;

		for (i = 0; i < length; i++) {
			n = str.charAt(i);

			if ((n >= 0x0001) && (n <= 0x007F)) {
				utflength++;
			} else if (n > 0x07FF) {
				utflength += 3;
			} else {
				utflength += 2;
			}
		}

		if (utflength > 65535) {
			throw new UTFDataFormatException("UTF string is " + utflength
					+ " bytes; max 65535.");
		}

		buffer = new byte[utflength + 2];
		buffer[m++] = (byte) ((utflength >>> 8) & 0xFF);
		buffer[m++] = (byte) ((utflength >>> 0) & 0xFF);

		for (; i < length; i++) {
			n = str.charAt(i);

			if ((n >= 0x0001) && (n <= 0x007F)) {
				buffer[m++] = (byte) n;
			} else if (n > 0x07FF) {
				buffer[m++] = (byte) (0xE0 | ((n >> 12) & 0x0F));
				buffer[m++] = (byte) (0x80 | ((n >> 6) & 0x3F));
				buffer[m++] = (byte) (0x80 | ((n >> 0) & 0x3F));
			} else {
				buffer[m++] = (byte) (0xC0 | ((n >> 6) & 0x1F));
				buffer[m++] = (byte) (0x80 | ((n >> 0) & 0x3F));
			}
		}

		write(buffer, 0, utflength + 2);
	}

	// ----------------------------------------------------------------------
	// Data Format Methods
	// ----------------------------------------------------------------------
	/**
	 * Sets the binary data format for data read from and written to the random
	 * access file. If the given format object is a null, no change is made to
	 * the current data format.
	 * <P>
	 * 
	 * @param bdf
	 *            the new file BinaryDataFormat object
	 */
	public void setBinaryDataFormat(final BinaryDataFormat bdf) {
		if (bdf == null) {
			return; // No change
		}
		fileFormat = bdf;

		if (fileFormat.isMBFByteOrder() == Host.isMBFByteOrder()) {
			swapNeeded = false;
		} else {
			swapNeeded = true;
		}
	}

	/**
	 * Gets the current binary data format object describing data read from and
	 * written to the random access file.
	 * <P>
	 * 
	 * @return the current BinaryDataFormat object
	 */
	public BinaryDataFormat getBinaryDataFormat() {
		return fileFormat;
	}

	// ----------------------------------------------------------------------
	// BinaryDataInput Multi-value Methods
	// ----------------------------------------------------------------------
	/**
	 * Reads nValues input bytes, each one representing a boolean value, and set
	 * each value in a boolean array to true if the corresponding byte is
	 * nonzero, false if that byte is zero. This method is suitable for reading
	 * the byte written by the writeBooleans method of interface
	 * BinaryDataOutput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to set
	 * @param nValues
	 *            the number of values to read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void readBooleans(final boolean[] values, final int nValues)
			throws EOFException, IOException {
		byte bytes[] = new byte[nValues];
		readFully(bytes, 0, nValues);
		for (int i = 0; i < nValues; i++) {
			values[i] = (bytes[i] != 0);
		}
	}

	/**
	 * Reads input bytes and sets nValues values in an array of doubles. The
	 * number of bytes read for each double is equal to the size of a double in
	 * the current binary data format.
	 * <P>
	 * The data read by this method is assumed to be in the host's byte order,
	 * data type size, and floating-point format. If the host uses
	 * Most-significant-Byte-First (MBF) byte ordering, then the first byte read
	 * will contain the highest-order bits of the value, and so on. Otherwise if
	 * the host uses Least-significant-Byte-First (LBF) byte ordering, then the
	 * first byte read will contain the lowest-order bits of the value, and so
	 * on.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeDoubles
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to set
	 * @param nValues
	 *            the number of values to read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void readDoubles(final double[] values, final int nValues)
			throws EOFException, IOException {
		int nBytes = nValues * fileFormat.getDoubleSize();
		byte bytes[] = new byte[nBytes];
		readFully(bytes, 0, nBytes);
		fileFormat.doubleValues(bytes, values, nValues);
	}

	/**
	 * Reads input bytes and sets nValues values in an array of floats. The
	 * number of bytes read for each float is equal to the size of a float in
	 * the current binary data format.
	 * <P>
	 * The data read by this method is assumed to be in the host's byte order,
	 * data type size, and floating-point format. If the host uses
	 * Most-significant-Byte-First (MBF) byte ordering, then the first byte read
	 * will contain the highest-order bits of the value, and so on. Otherwise if
	 * the host uses Least-significant-Byte-First (LBF) byte ordering, then the
	 * first byte read will contain the lowest-order bits of the value, and so
	 * on.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeFloats
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to set
	 * @param nValues
	 *            the number of values to read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void readFloats(final float[] values, final int nValues)
			throws EOFException, IOException {
		int nBytes = nValues * fileFormat.getFloatSize();
		byte bytes[] = new byte[nBytes];
		readFully(bytes, 0, nBytes);
		fileFormat.floatValues(bytes, values, nValues);
	}

	/**
	 * Reads input bytes and sets nValues values in an array of ints. The number
	 * of bytes read for each int is equal to the size of a int in the current
	 * binary data format.
	 * <P>
	 * The data read by this method is assumed to be in the host's byte order
	 * and data type size. If the host uses Most-significant-Byte-First (MBF)
	 * byte ordering, then the first byte read will contain the highest-order
	 * bits of the value, and so on. Otherwise if the host uses
	 * Least-significant-Byte-First (LBF) byte ordering, then the first byte
	 * read will contain the lowest-order bits of the value, and so on.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeInts
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to set
	 * @param nValues
	 *            the number of values to read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void readInts(final int[] values, final int nValues)
			throws EOFException, IOException {
		int nBytes = nValues * fileFormat.getIntSize();
		byte bytes[] = new byte[nBytes];
		readFully(bytes, 0, nBytes);
		fileFormat.intValues(bytes, values, nValues);
	}

	/**
	 * Reads input bytes and sets nValues values in an array of longs. The
	 * number of bytes read for each long is equal to the size of a long in the
	 * current binary data format.
	 * <P>
	 * The data read by this method is assumed to be in the host's byte order
	 * and data type size. If the host uses Most-significant-Byte-First (MBF)
	 * byte ordering, then the first byte read will contain the highest-order
	 * bits of the value, and so on. Otherwise if the host uses
	 * Least-significant-Byte-First (LBF) byte ordering, then the first byte
	 * read will contain the lowest-order bits of the value, and so on.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeLongs
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to set
	 * @param nValues
	 *            the number of values to read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void readLongs(final long[] values, final int nValues)
			throws EOFException, IOException {
		int nBytes = nValues * fileFormat.getLongSize();
		byte bytes[] = new byte[nBytes];
		readFully(bytes, 0, nBytes);
		fileFormat.longValues(bytes, values, nValues);
	}

	/**
	 * Reads input bytes and sets nValues values in an array of long doubles.
	 * The number of bytes read for each long double is equal to the size of a
	 * long double in the current binary data format.
	 * <P>
	 * The data read by this method is assumed to be in the host's byte order,
	 * data type size, and floating-point format. If the host uses
	 * Most-significant-Byte-First (MBF) byte ordering, then the first byte read
	 * will contain the highest-order bits of the value, and so on. Otherwise if
	 * the host uses Least-significant-Byte-First (LBF) byte ordering, then the
	 * first byte read will contain the lowest-order bits of the value, and so
	 * on.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeDoubles
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to set
	 * @param nValues
	 *            the number of values to read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void readLongDoubles(final double[] values, final int nValues)
			throws EOFException, IOException {
		int nBytes = nValues * fileFormat.getLongDoubleSize();
		byte bytes[] = new byte[nBytes];
		readFully(bytes, 0, nBytes);
		fileFormat.longDoubleValues(bytes, values, nValues);
	}

	/**
	 * Reads input bytes and sets nValues values in an array of long longs. The
	 * number of bytes read for each long long is equal to the size of a long
	 * long in the current binary data format.
	 * <P>
	 * The data read by this method is assumed to be in the host's byte order
	 * and data type size. If the host uses Most-significant-Byte-First (MBF)
	 * byte ordering, then the first byte read will contain the highest-order
	 * bits of the value, and so on. Otherwise if the host uses
	 * Least-significant-Byte-First (LBF) byte ordering, then the first byte
	 * read will contain the lowest-order bits of the value, and so on.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeLongs
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to set
	 * @param nValues
	 *            the number of values to read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void readLongLongs(final long[] values, final int nValues)
			throws EOFException, IOException {
		int nBytes = nValues * fileFormat.getLongLongSize();
		byte bytes[] = new byte[nBytes];
		readFully(bytes, 0, nBytes);
		fileFormat.longLongValues(bytes, values, nValues);
	}

	/**
	 * Reads input bytes and sets nValues values in an array of shorts. The
	 * number of bytes read for each short is equal to the size of a short in
	 * the current binary data format.
	 * <P>
	 * The data read by this method is assumed to be in the host's byte order
	 * and data type size. If the host uses Most-significant-Byte-First (MBF)
	 * byte ordering, then the first byte read will contain the highest-order
	 * bits of the value, and so on. Otherwise if the host uses
	 * Least-significant-Byte-First (LBF) byte ordering, then the first byte
	 * read will contain the lowest-order bits of the value, and so on.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeShorts
	 * method of interface BinaryDataOutput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to set
	 * @param nValues
	 *            the number of values to read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void readShorts(final short[] values, final int nValues)
			throws EOFException, IOException {
		int nBytes = nValues * fileFormat.getShortSize();
		byte bytes[] = new byte[nBytes];
		readFully(bytes, 0, nBytes);
		fileFormat.shortValues(bytes, values, nValues);
	}

	/**
	 * Reads input bytes and sets nValues values in an array of unsigned shorts.
	 * The number of bytes read for each unsigned short is equal to the size of
	 * a unsigned short in the current binary data format.
	 * <P>
	 * The data read by this method is assumed to be in the host's byte order
	 * and data type size. If the host uses Most-significant-Byte-First (MBF)
	 * byte ordering, then the first byte read will contain the highest-order
	 * bits of the value, and so on. Otherwise if the host uses
	 * Least-significant-Byte-First (LBF) byte ordering, then the first byte
	 * read will contain the lowest-order bits of the value, and so on.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeShorts
	 * method of interface BinaryDataOutput if the argument to writeShort was
	 * intended to be an unsigned value.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to set
	 * @param nValues
	 *            the number of values to read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void readUnsignedShorts(final short[] values, final int nValues)
			throws EOFException, IOException {
		int nBytes = nValues * fileFormat.getShortSize();
		byte bytes[] = new byte[nBytes];
		readFully(bytes, 0, nBytes);
		fileFormat.unsignedShortValues(bytes, values, nValues);
	}

	/**
	 * Reads input bytes and sets nValues values in an array of unsigned ints.
	 * The number of bytes read for each unsigned int is equal to the size of a
	 * unsigned int in the current binary data format.
	 * <P>
	 * The data read by this method is assumed to be in the host's byte order
	 * and data type size. If the host uses Most-significant-Byte-First (MBF)
	 * byte ordering, then the first byte read will contain the highest-order
	 * bits of the value, and so on. Otherwise if the host uses
	 * Least-significant-Byte-First (LBF) byte ordering, then the first byte
	 * read will contain the lowest-order bits of the value, and so on.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeShorts
	 * method of interface BinaryDataOutput if the argument to writeShort was
	 * intended to be an unsigned value.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to set
	 * @param nValues
	 *            the number of values to read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void readUnsignedInts(final int[] values, final int nValues)
			throws EOFException, IOException {
		int nBytes = nValues * fileFormat.getIntSize();
		byte bytes[] = new byte[nBytes];
		readFully(bytes, 0, nBytes);
		fileFormat.unsignedIntValues(bytes, values, nValues);
	}

	/**
	 * Reads input bytes and sets nValues values in an array of unsigned longs.
	 * The number of bytes read for each unsigned long is equal to the size of a
	 * unsigned long in the current binary data format.
	 * <P>
	 * The data read by this method is assumed to be in the host's byte order
	 * and data type size. If the host uses Most-significant-Byte-First (MBF)
	 * byte ordering, then the first byte read will contain the highest-order
	 * bits of the value, and so on. Otherwise if the host uses
	 * Least-significant-Byte-First (LBF) byte ordering, then the first byte
	 * read will contain the lowest-order bits of the value, and so on.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeShorts
	 * method of interface BinaryDataOutput if the argument to writeShort was
	 * intended to be an unsigned value.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to set
	 * @param nValues
	 *            the number of values to read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void readUnsignedLongs(final long[] values, final int nValues)
			throws EOFException, IOException {
		int nBytes = nValues * fileFormat.getLongSize();
		byte bytes[] = new byte[nBytes];
		readFully(bytes, 0, nBytes);
		fileFormat.unsignedLongValues(bytes, values, nValues);
	}

	/**
	 * Reads input bytes and sets nValues values in an array of unsigned long
	 * longs. The number of bytes read for each unsigned long long is equal to
	 * the size of a unsigned long long in the current binary data format.
	 * <P>
	 * The data read by this method is assumed to be in the host's byte order
	 * and data type size. If the host uses Most-significant-Byte-First (MBF)
	 * byte ordering, then the first byte read will contain the highest-order
	 * bits of the value, and so on. Otherwise if the host uses
	 * Least-significant-Byte-First (LBF) byte ordering, then the first byte
	 * read will contain the lowest-order bits of the value, and so on.
	 * <P>
	 * This method is suitable for reading the bytes written by the writeShorts
	 * method of interface BinaryDataOutput if the argument to writeShort was
	 * intended to be an unsigned value.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to set
	 * @param nValues
	 *            the number of values to read
	 * @throws EOFException
	 *             if this stream reaches the end before reading all the bytes
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void readUnsignedLongLongs(final long[] values, final int nValues)
			throws EOFException, IOException {
		int nBytes = nValues * fileFormat.getLongLongSize();
		byte bytes[] = new byte[nBytes];
		readFully(bytes, 0, nBytes);
		fileFormat.unsignedLongLongValues(bytes, values, nValues);
	}

	// ----------------------------------------------------------------------
	// BinaryDataOutput Multiple-value Methods
	// ----------------------------------------------------------------------
	/**
	 * Writes an array of boolean values to this output stream. For each true
	 * value, a (byte)1 is written; otherwise for a false value, a (byte)0 is
	 * written. The bytes written by this method may be read by the readBooleans
	 * method of interface BinaryDataInput, which will then return booleans
	 * equal to those in the values array.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to be written
	 * @param nValues
	 *            the number of values to write
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void writeBooleans(final boolean[] values, final int nValues)
			throws IOException {
		byte bytes[] = new byte[nValues];
		for (int i = 0; i < nValues; i++) {
			bytes[i] = (values[i] == false) ? (byte) 0 : (byte) 1;
		}
		write(bytes, 0, nValues);
	}

	/**
	 * Writes bytes to the output stream to represent the double values of the
	 * argument array. The number of bytes written for each value is equal to
	 * the size of a double using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readFloats method of
	 * interface BinaryDataInput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to be written
	 * @param nValues
	 *            the number of values to write
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void writeDoubles(final double[] values, final int nValues)
			throws IOException {
		int nBytes = nValues * fileFormat.getDoubleSize();
		byte[] bytes = new byte[nBytes];
		fileFormat.encodeDoubles(values, nValues, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the float values of the
	 * argument array. The number of bytes written for each value is equal to
	 * the size of a float using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readFloats method of
	 * interface BinaryDataInput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to be written
	 * @param nValues
	 *            the number of values to write
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void writeFloats(final float[] values, final int nValues)
			throws IOException {
		int nBytes = nValues * fileFormat.getFloatSize();
		byte[] bytes = new byte[nBytes];
		fileFormat.encodeFloats(values, nValues, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the int values of the
	 * argument array. The number of bytes written for each value is equal to
	 * the size of a int using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readInts method of
	 * interface BinaryDataInput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to be written
	 * @param nValues
	 *            the number of values to write
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void writeInts(final int[] values, final int nValues)
			throws IOException {
		int nBytes = nValues * fileFormat.getIntSize();
		byte[] bytes = new byte[nBytes];
		fileFormat.encodeInts(values, nValues, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the long values of the
	 * argument array. The number of bytes written for each value is equal to
	 * the size of a long using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readLongs method of
	 * interface BinaryDataInput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to be written
	 * @param nValues
	 *            the number of values to write
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void writeLongs(final long[] values, final int nValues)
			throws IOException {
		int nBytes = nValues * fileFormat.getLongSize();
		byte[] bytes = new byte[nBytes];
		fileFormat.encodeLongs(values, nValues, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the short values of the
	 * argument array. The number of bytes written for each value is equal to
	 * the size of a short using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readShorts method of
	 * interface BinaryDataInput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to be written
	 * @param nValues
	 *            the number of values to write
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void writeShorts(final short[] values, final int nValues)
			throws IOException {
		int nBytes = nValues * fileFormat.getShortSize();
		byte[] bytes = new byte[nBytes];
		fileFormat.encodeShorts(values, nValues, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the long long values of
	 * the argument array. The number of bytes written for each value is equal
	 * to the size of a long long using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readShorts method of
	 * interface BinaryDataInput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to be written
	 * @param nValues
	 *            the number of values to write
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void writeLongLongs(final long[] values, final int nValues)
			throws IOException {
		int nBytes = nValues * fileFormat.getLongLongSize();
		byte[] bytes = new byte[nBytes];
		fileFormat.encodeLongLongs(values, nValues, bytes);
		write(bytes, 0, nBytes);
	}

	/**
	 * Writes bytes to the output stream to represent the long double values of
	 * the argument array. The number of bytes written for each value is equal
	 * to the size of a long double using the current binary data format.
	 * <P>
	 * The data written by this method is converted from the host's binary data
	 * format to that for the file. This may involve a change in byte order and
	 * data type size.
	 * <P>
	 * The bytes written by this method may be read by the readShorts method of
	 * interface BinaryDataInput.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to be written
	 * @param nValues
	 *            the number of values to write
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void writeLongDoubles(final double[] values, final int nValues)
			throws IOException {
		int nBytes = nValues * fileFormat.getLongDoubleSize();
		byte[] bytes = new byte[nBytes];
		fileFormat.encodeLongDoubles(values, nValues, bytes);
		write(bytes, 0, nBytes);
	}
}
