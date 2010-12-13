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
//  LocalRandomAccessFile.java  -  edu.sdsc.grid.io.local.LocalRandomAccessFile
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.GeneralRandomAccessFile
//          |
//          +-.LocalRandomAccessFile
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.GeneralRandomAccessFile;

/**
 * A wrapper class for java.io.RandomAccessFile.
 * 
 * @see java.io.RandomAccessFile
 * @see edu.sdsc.grid.io.GeneralRandomAccessFile
 * @author Lucas Gilbert
 * @since Jargon1.0
 */
public class LocalRandomAccessFile extends GeneralRandomAccessFile {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------
	private RandomAccessFile wrapper;

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	/**
	 * Creates a random access file stream to read from, and optionally to write
	 * to, the file specified by the {@link String} argument. A new file
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
	public LocalRandomAccessFile(final String name, final String mode)
			throws IOException {
		this(new LocalFile(name), mode);
	}

	/**
	 * Creates a random access file stream to read from, and optionally to write
	 * to, the file specified by the {@link File} argument. A new file
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
	public LocalRandomAccessFile(final File file, final String mode)
			throws IOException {
		this(new LocalFile(file), mode);
	}

	/**
	 * Creates a random access file stream to read from, and optionally to write
	 * to, the file specified by the {@link LocalFile} argument. A new file
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
	public LocalRandomAccessFile(final LocalFile file, final String mode)
			throws IOException {
		super(file, mode);
	}

	// ----------------------------------------------------------------------
	// Setters and Getters
	// ----------------------------------------------------------------------
	/**
	 * Returns this object as a standard java.io.RandomAccessFile.
	 * 
	 * @see java.io.RandomAccessFile
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public RandomAccessFile getRandomAccessFile() throws IOException {
		return wrapper;
	}

	@Override
	protected void open(final GeneralFile file) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		} else if (file.isDirectory()) {
			throw new IllegalArgumentException("Wrong permissions or " + file
					+ " is a directory");
		}
		wrapper = new RandomAccessFile(file.getAbsolutePath(), mode);
	}

	// ----------------------------------------------------------------------
	// Read methods
	// ----------------------------------------------------------------------
	@Override
	public int read() throws IOException {
		return wrapper.read();
	}

	// Private method in wrapper, so call public.
	@Override
	protected int readBytes(final byte b[], final int offset, final int len)
			throws IOException {
		return wrapper.read(b, offset, len);
	}

	@Override
	public int skipBytes(final int n) throws IOException {
		return wrapper.skipBytes(n);
	}

	// ----------------------------------------------------------------------
	// Write methods
	// ----------------------------------------------------------------------
	@Override
	public void write(final int b) throws IOException {
		wrapper.write(b);
	}

	// Private method in wrapper, so call public.
	@Override
	protected void writeBytes(final byte b[], final int offset, final int len)
			throws IOException {
		wrapper.write(b, offset, len);
	}

	// ----------------------------------------------------------------------
	// Random access methods
	// ----------------------------------------------------------------------
	/**
	 * Returns the current offset in this file.
	 * 
	 * @return the offset from the beginning of the file, in bytes, at which the
	 *         next read or write occurs.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public long getFilePointer() throws IOException {
		return wrapper.getFilePointer();
	}

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
	@Override
	public void seek(final long position) throws IOException {
		wrapper.seek(position);
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
	@Override
	public void seek(final long position, final int origin) throws IOException {
		switch (origin) {
		case 1:
			seek(position + getFilePointer());
			break;
		case 2:
			seek(position + length());
			break;
		case 0:
		default:
			seek(position);
			break;
		}
	}

	/**
	 * Returns the length of this file.
	 * 
	 * @return the length of this file, measured in bytes.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public long length() throws IOException {
		return wrapper.length();
	}

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
	@Override
	public void setLength(final long newLength) throws IOException {
		wrapper.setLength(newLength);
	}

	/**
	 * Closes this random access file stream and releases any system resources
	 * associated with the stream. A closed random access file cannot perform
	 * input or output operations and cannot be reopened.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	@Override
	public void close() throws IOException {
		wrapper.close();
	}
}
