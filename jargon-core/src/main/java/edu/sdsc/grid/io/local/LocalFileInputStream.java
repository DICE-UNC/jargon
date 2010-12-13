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
//  LocalFileInputStream.java  -  edu.sdsc.grid.io.local.LocalFileInputStream
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-java.io.InputStream
//          |
//          +-edu.sdsc.grid.io.GeneralFileInputStream
//              |
//              +-edu.sdsc.grid.io.LocalFileInputStream
//
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io.local;

import java.io.FileInputStream;
import java.io.IOException;

import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.GeneralFileInputStream;

/**
 * A LocalFileInputStream obtains input bytes from a file in a file system. What
 * files are available depends on the host environment.
 * <P>
 * LocalFileInputStream is meant for reading streams of raw bytes such as image
 * data.
 * <P>
 * The original intention for this class was to subclass
 * java.io.FileInputStream. But that is not currently the case.
 * <P>
 * 
 * @author Lucas Gilbert
 * @since JARGON1.4
 */
public class LocalFileInputStream extends GeneralFileInputStream {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------
	/**
   *
   */
	private FileInputStream in;

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	/**
	 * Creates a <code>LocalFileInputStream</code> by opening a connection to an
	 * actual file, the file named by the path name <code>name</code> in the
	 * file system.
	 * <p>
	 * First, the security is checked to verify the file can be read.
	 * <p>
	 * If the named file does not exist, is a directory rather than a regular
	 * file, or for some other reason cannot be opened for reading then a
	 * <code>IOException</code> is thrown.
	 * 
	 * @param name
	 *            the system-dependent file name.
	 * @exception IOException
	 *                if the file does not exist, is a directory rather than a
	 *                regular file, or for some other reason cannot be opened
	 *                for reading.
	 */
	public LocalFileInputStream(final String name) throws IOException {
		super(new LocalFileSystem(), name);
	}

	/**
	 * Creates a <code>LocalFileInputStream</code> by opening a connection to an
	 * actual file, the file named by the <code>LocalFile</code> object
	 * <code>file</code> in the file system. A new <code>FileDescriptor</code>
	 * object is created to represent this file connection.
	 * <p>
	 * First, the security is checked to verify the file can be read.
	 * <p>
	 * If the named file does not exist, is a directory rather than a regular
	 * file, or for some other reason cannot be opened for reading then a
	 * <code>IOException</code> is thrown.
	 * 
	 * @param file
	 *            the file to be opened for reading.
	 * @exception IOException
	 *                if the file does not exist, is a directory rather than a
	 *                regular file, or for some other reason cannot be opened
	 *                for reading.
	 * @see java.io.File#getPath()
	 */
	public LocalFileInputStream(final LocalFile file) throws IOException {
		super(file);
	}

	/**
	 * Ensures that the <code>close</code> method of this file input stream is
	 * called when there are no more references to it.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @see edu.sdsc.grid.io.LocalFileInputStream#close()
	 */
	@Override
	protected void finalize() throws IOException {
		close();
	}

	// ----------------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------------
	/**
	 * Opens the given file for use by this stream.
	 * 
	 * @param file
	 *            the file to be opened.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	protected void open(final GeneralFile file) throws IOException {
		in = new FileInputStream(((LocalFile) file).getFile());
	}

	/**
	 * Reads a byte of data from this input stream. This method blocks if no
	 * input is yet available.
	 * 
	 * @return the next byte of data, or <code>-1</code> if the end of the file
	 *         is reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public int read() throws IOException {
		return in.read();
	}

	/**
	 * Reads up to <code>b.length</code> bytes of data from this input stream
	 * into an array of bytes. This method blocks until some input is available.
	 * 
	 * @param b
	 *            the buffer into which the data is read.
	 * @return the total number of bytes read into the buffer, or
	 *         <code>-1</code> if there is no more data because the end of the
	 *         file has been reached.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public int read(final byte b[]) throws IOException {
		return in.read(b);
	}

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
	 * <code>NullPointerException</code> is thrown.
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
	@Override
	public int read(final byte b[], final int off, final int len)
			throws IOException {
		return in.read(b, off, len);
	}

	/**
	 * Skips over and discards <code>n</code> bytes of data from the input
	 * stream. The <code>skip</code> method may, for a variety of reasons, end
	 * up skipping over some smaller number of bytes, possibly <code>0</code>.
	 * The actual number of bytes skipped is returned.
	 * 
	 * @param n
	 *            the number of bytes to be skipped.
	 * @return the actual number of bytes skipped.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public long skip(final long n) throws IOException {
		return in.skip(n);
	}

	/**
	 * Returns the number of bytes that can be read from this file input stream
	 * without blocking.
	 * 
	 * @return the number of bytes that can be read from this file input stream
	 *         without blocking.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public int available() throws IOException {
		return in.available();
	}

	/**
	 * Closes this file input stream and releases any system resources
	 * associated with the stream.
	 * <p>
	 * If this stream has an associated channel then the channel is closed as
	 * well.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override
	public void close() throws IOException {
		in.close();
	}

	/**
	 * Returns the <code>FileDescriptor</code> object that represents the
	 * connection to the actual file in the file system being used by this
	 * <code>FileInputStream</code>.
	 * 
	 * @return the file descriptor object associated with this stream.
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @see java.io.FileDescriptor
	 */
	/*
	 * public final FileDescriptor getFD() throws IOException {
	 * 
	 * }
	 */

	/**
	 * Returns the unique {@link java.nio.channels.FileChannel FileChannel}
	 * object associated with this file input stream.
	 * 
	 * <p>
	 * The initial {@link java.nio.channels.FileChannel#position()
	 * </code>position<code>} of the returned channel will be equal to the
	 * number of bytes read from the file so far. Reading bytes from this stream
	 * will increment the channel's position. Changing the channel's position,
	 * either explicitly or by reading, will change this stream's file position.
	 * 
	 * @return the file channel associated with this file input stream
	 */
	/*
	 * public FileChannel getChannel() {
	 * 
	 * }
	 */
}
