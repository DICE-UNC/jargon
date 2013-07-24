/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.UTFDataFormatException;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.FileIOOperations.SeekWhenceType;
import org.irods.jargon.core.utils.BinaryDataFormat;
import org.irods.jargon.core.utils.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of this class support I/O on random-access binary files. Methods on
 * the class support conversion between host and file byte orders and word
 * sizes.
 * 
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
 * @author Mike Conway - DICE (www.irods.org) adopted from original Jargon
 *         implementation by Lucas Gilbert
 * 
 */
public class IRODSRandomAccessFile implements DataInput, DataOutput {

	private static Logger log = LoggerFactory.getLogger(IRODSFileImpl.class);
	private final FileIOOperations fileIOOperations;
	private final IRODSFile irodsFile;
	private long filePointer = 0;
	private BinaryDataFormat fileFormat = new BinaryDataFormat();

	/**
	 * Create an instance of the IRODS implementation of a random access file
	 * 
	 * @param irodsFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFileImpl} that
	 *            describes the file.
	 * @param fileIOOperations
	 *            <code>FileIOOperations</code> that will encapsulate the actual
	 *            methods in IRODS
	 * @return <code>IRODSRandomAccessFile</code>
	 * @throws JargonException
	 */
	protected IRODSRandomAccessFile(final IRODSFile irodsFile,
			final FileIOOperations fileIOOperations) throws JargonException {

		log.info("constructor IRODSRandomAccessFile");

		if (irodsFile == null) {
			throw new JargonException("irodsFile is null");
		}

		if (!irodsFile.exists()) {
			throw new JargonException("the file does not exist:"
					+ irodsFile.getAbsolutePath());
		}

		if (!irodsFile.isFile()) {
			throw new JargonException("this is not a file:"
					+ irodsFile.getAbsolutePath());
		}

		if (fileIOOperations == null) {
			throw new JargonException("fileIOOperations is null");
		}

		this.irodsFile = irodsFile;
		this.fileIOOperations = fileIOOperations;

	}

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
			seek(getFilePointer(), null);
		} else if ((line.length() == 0)) {
			return null;
		}

		return line.toString();
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
	 * Reads in a string from this file. The string has been encoded using a
	 * modified UTF-8 format.
	 * <p>
	 * The first two bytes are read, starting from the current file pointer, as
	 * if by <code>readUnsignedShort</code>. This value gives the number of
	 * following bytes that are in the encoded string, not the length of the
	 * resulting string. The following bytes are then interpreted as bytes
	 * encoding characters in the UTF-8 format and are converted into
	 * characters.
	 * <p>
	 * This method blocks until all the bytes are read, the end of the stream is
	 * detected, or an exception is thrown.
	 * 
	 * @return a Unicode string.
	 * 
	 * @throws EOFException
	 *             if this file reaches the end before reading all the bytes.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs.
	 * 
	 * @throws UTFDataFormatException
	 *             if the bytes do not represent valid UTF-8 encoding of a
	 *             Unicode string.
	 * 
	 * @see edu.sdsc.grid.io.GeneralRandomAccessFile#readUnsignedShort()
	 */
	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException();
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

		seek(newPosition, SeekWhenceType.SEEK_CURRENT);

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

	/**
	 * Reads a byte of data from this file. The byte is returned as an integer
	 * in the range 0 to 255 (<code>0x00-0x0ff</code>). This method blocks if no
	 * input is yet available.
	 * <p>
	 * Although <code>IRODSRandomAccessFile</code> is not a subclass of
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
		byte buffer[] = new byte[1];
		int offset = 0;
		int read;
		try {
			read = fileIOOperations.fileRead(irodsFile.getFileDescriptor(),
					buffer, offset, 1);
		} catch (JargonException e) {
			log.error("JargonException reading file", e);
			throw new IOException(e);
		}
		if (read == 1 && buffer != null) {
			filePointer += 1;

			return buffer[0];
		}
		return -1;

	}

	/**
	 * Reads a sub array as a sequence of bytes.
	 * 
	 * @param buffer
	 *            the buffer into which the data is read.
	 * @param offset
	 *            the start offset in the data
	 * @param len
	 *            the maximum number of bytes read.
	 * @throws IOException
	 *             If an I/O error has occurred.
	 */
	protected int readBytes(final byte buffer[], final int offset, final int len)
			throws IOException {
		int read;
		try {
			read = fileIOOperations.fileRead(irodsFile.getFileDescriptor(),
					buffer, offset, len);
		} catch (JargonException e) {
			log.error("JargonException reading file", e);
			throw new IOException(e);
		}
		filePointer += read;

		return read;
	}

	/**
	 * Writes a sub array as a sequence of bytes.
	 * 
	 * @param buffer
	 *            the data to be written
	 * @param offset
	 *            the start offset in the data
	 * @param len
	 *            the number of bytes that are written
	 * @throws IOException
	 *             If an I/O error has occurred.
	 */
	protected void writeBytes(final byte buffer[], final int offset,
			final int len) throws IOException {
		try {
			filePointer += fileIOOperations.write(
					irodsFile.getFileDescriptor(), buffer, offset, len);
		} catch (JargonException e) {
			log.error("JargonException reading file", e);
			throw new IOException(e);
		}
	}

	/**
	 * Returns the current offset in this file.
	 * 
	 * @return the offset from the beginning of the file, in bytes, at which the
	 *         next read or write occurs.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public long getFilePointer() throws IOException {
		return filePointer;
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
	public void seek(final long position, final SeekWhenceType origin)
			throws IOException {
		if (position < 0) {
			throw new IllegalArgumentException();
		}

		try {
			fileIOOperations.seek(irodsFile.getFileDescriptor(), position,
					origin);
		} catch (JargonException e) {
			log.error("JargonException reading file", e);
			throw new IOException(e);
		}
		filePointer = position;
	}

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
	 * Returns the length of this file.
	 * 
	 * @return the length of this file, measured in bytes.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public long length() throws IOException {
		return irodsFile.length();
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
	 * This method closes the underlying IRODSFileImpl
	 */
	public void close() throws IOException {
		try {
			log.debug("closing: {}", irodsFile.getAbsolutePath());
			irodsFile.close();
		} catch (JargonException e) {
			log.error("JargonException reading file", e);
			throw new IOException(e);
		}

	}

}
