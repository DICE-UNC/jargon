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
//  BinaryDataFormat.java  -  edu.sdsc.grid.io.BinaryDataFormat
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-->BinaryDataFormat
//
//  PRINCIPAL AUTHOR
//  David R. Nadeau, SDSC/UCSD
//
package org.irods.jargon.core.utils;

/**
 * <DIV ALIGN=JUSTIFY> The BinaryDataFormat class describes the byte order and
 * primitive data type sizes for binary numeric data. For instance, a format may
 * describe that data is stored with the most-significant byte first and with a
 * "short" taking 2 bytes, an "int" 4 bytes, a "long" 8 bytes, and so on.
 * <P>
 * Methods on the class set or get a format's attributes. Methods also support
 * using those attributes to control conversion of numeric values into and out
 * of raw byte arrays. These methods are designed for use by binary file readers
 * and writers to enable them to easily map between the binary format of data in
 * a file and that of the current host.
 * <P>
 * <B>Example</B><BR>
 * Perhaps a file contains binary data that uses a Least-significant-Byte-First
 * (LBF) byte order (such as Intel processors) with short, int, long, and long
 * long data types that are 2, 4, 4, and 8 bytes in size, respectively. To
 * convert this data to the host's format, first create a BinaryDataFormat
 * object to describe the file's format, and then call that object's
 * shortValue(bytes*b), intValue(bytes*b), longValue(bytes*b), and
 * longLongValue(bytes*b) methods to convert to the host's format:
 * <P>
 * 
 * <PRE>
 * // Define the file's binary data attributes
 * BinaryDataFormat fileFormat = new BinaryDataFormat();
 * fileFormat.setLBFByteOrder();
 * fileFormat.setShortSize(2);
 * fileFormat.setIntSize(4);
 * fileFormat.setLongSize(4);
 * fileFormat.setLongLongSize(8);
 * 
 * // Read a 4-byte long from the file
 * byte b = new byte[4];
 * read(fd, b, 4);
 * 
 * // Convert it from the file's format to the host's format
 * long lng = fileFormat.longValue(b);
 * </PRE>
 * <P>
 * Conversion methods automatically handle changes in byte order and data type
 * size. For instance, if the host's "long" data type in the code above is
 * actually 8 bytes long and stored Most-significant-Byte-First (MBF), the
 * longValue(bytes*b) method will swap the byte order of the incoming 4-byte
 * file long, then sign-extend or zero-pad the value to create an 8-byte host
 * long.
 * <P>
 * Similar operations apply for floating-point values of differing byte order
 * and size. All floating point operations assume, however, that the data is in
 * IEEE 754 format. All current processors use this format. Older processors do
 * not, including the IBM 370, VAX, and Cray XMP, YMP, 2, C90, and first
 * generation T90. On older hosts, floating point value handling will not work
 * properly.
 * <P>
 * <B>Initial values</B><BR>
 * By default, initial values for all attributes match those of the host
 * executing the application. For instance, initially the number of bytes
 * occupied by a float, as returned by getFloatSize(), equals sizeof(float).
 * Initial values may be changed by calling the set*Size() methods, such as
 * setFloatSize().
 * <P>
 * <B>Integer values</B><BR>
 * Methods on this class support integer data types of arbitrary size. Integer
 * data converted from a large to small size may be truncated. Integer data
 * converted from a small to large size will be sign-extended or zero-padded
 * depending upon if the data type is signed or unsigned, respectively.
 * <P>
 * While this class supports conversions for long long integers, the Java
 * language does not support this data type. As a result, conversion from a long
 * long int into a Java long or int may truncate.
 * <P>
 * <B>Floating point values</B><BR>
 * Methods on this class <I>assume</I> that floating point data types conform to
 * the IEEE 754 standard. That standard constrains floating point types to these
 * sizes:
 * <P>
 * <BLOCKQUOTE>
 * <TABLE>
 * <TR>
 * <TD><B>Precision</B></TD>
 * <TD><B>Size</B></TD>
 * </TR>
 * <TR>
 * <TD>single</TD>
 * <TD>4 bytes</TD>
 * </TR>
 * <TR>
 * <TD>double</TD>
 * <TD>8 bytes</TD>
 * </TR>
 * <TR>
 * <TD>quadruple</TD>
 * <TD>16 bytes</TD>
 * </TR>
 * </TABLE>
 * </BLOCKQUOTE>
 * <P>
 * The mapping from these precision sizes to the floating point data types
 * "float", "double", and "long double" supports any combination. For instance,
 * a "double" may be defined as 4 bytes, 8 bytes, or 16 bytes in size. A
 * "double" may even be made smaller than a "float", though this kind of odd
 * configuration probably should not be done.
 * <P>
 * While this class supports conversions for long doubles, the Java language
 * does not support this data type. As a result, conversion from a long double
 * into a Java double or float may truncate.
 * <P>
 * <DL>
 * <DT>Sub-classing</DT>
 * <DD>Final</DD>
 * </DL>
 * </DIV>
 * <P>
 * 
 * @author David R. Nadeau, San Diego Supercomputer Center
 */
public final class BinaryDataFormat extends Object {
	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------
	//
	// For speed, we use individual variables, not an array of values.
	// Array access would require a multiply, add, and two pointer
	// dereferences (object and array). Individual variables instead
	// require only one pointer dereference (object).
	//

	/**
	 * Holds the size, in bytes, of the short language primitive supported for
	 * binary I/O.
	 * <P>
	 */
	private byte shortSize = 0;

	/**
	 * Holds the size, in bytes, of the int language primitive supported for
	 * binary I/O.
	 * <P>
	 */
	private byte intSize = 0;

	/**
	 * Holds the size, in bytes, of the long language primitive supported for
	 * binary I/O.
	 * <P>
	 */
	private byte longSize = 0;

	/**
	 * Holds the size, in bytes, of the long long language primitive supported
	 * for binary I/O.
	 * <P>
	 */
	private byte longLongSize = 0;

	/**
	 * Holds the size, in bytes, of the float language primitive supported for
	 * binary I/O.
	 * <P>
	 */
	private byte floatSize = 0;

	/**
	 * Holds the size, in bytes, of the double language primitive supported for
	 * binary I/O.
	 * <P>
	 */
	private byte doubleSize = 0;

	/**
	 * Holds the size, in bytes, of the long double language primitive supported
	 * for binary I/O.
	 * <P>
	 */
	private byte longDoubleSize = 0;

	/**
	 * Holds true if the byte order is MBF, and false if LBF.
	 * <P>
	 */
	private boolean isMBF = true;

	//
	// Accelerators for value conversion. These are used to avoid
	// having to do a comparison between this format and the host's
	// attributes for every conversion.
	//

	/**
	 * Holds true if the host and this binary format have the same byte order.
	 * <P>
	 */
	private boolean sameByteOrder = true;

	/**
	 * Holds true if the host and this binary format have the same size for
	 * short values.
	 * <P>
	 */
	private boolean sameShortSize = true;

	/**
	 * Holds true if the host and this binary format have the same size for int
	 * values.
	 * <P>
	 */
	private boolean sameIntSize = true;

	/**
	 * Holds true if the host and this binary format have the same size for long
	 * values.
	 * <P>
	 */
	private boolean sameLongSize = true;

	/**
	 * Holds true if the host and this binary format have the same size for long
	 * long values.
	 * <P>
	 */
	private boolean sameLongLongSize = true;

	/**
	 * Holds true if the host and this binary format have the same size for
	 * float values.
	 * <P>
	 */
	private boolean sameFloatSize = true;

	/**
	 * Holds true if the host and this binary format have the same size for
	 * double values.
	 * <P>
	 */
	private boolean sameDoubleSize = true;

	/**
	 * Holds true if the host and this binary format have the same size for long
	 * double values.
	 * <P>
	 */
	private boolean sameLongDoubleSize = true;

	// ----------------------------------------------------------------------
	// Constructors / Destructors
	// ----------------------------------------------------------------------
	/**
	 * Constructs a binary data format description with initial values set to
	 * match the attributes of the host.
	 * <P>
	 */
	public BinaryDataFormat() {
		// Copy the host's attributes to be the current values
		isMBF = Host.isMBFByteOrder();
		sameByteOrder = true;

		shortSize = Host.getStorageSize(Host.SHORT);
		intSize = Host.getStorageSize(Host.INT);
		longSize = Host.getStorageSize(Host.LONG);
		longLongSize = Host.getStorageSize(Host.LONGLONG);
		floatSize = Host.getStorageSize(Host.FLOAT);
		doubleSize = Host.getStorageSize(Host.DOUBLE);
		longDoubleSize = Host.getStorageSize(Host.LONGDOUBLE);

		sameShortSize = true;
		sameIntSize = true;
		sameLongSize = true;
		sameLongLongSize = true;
		sameFloatSize = true;
		sameDoubleSize = true;
		sameLongDoubleSize = true;
	}

	/**
	 * Constructs a binary data format description with initial values copied
	 * from the given binary data format. If the given format is null, a
	 * NullPointerException is thrown.
	 * <P>
	 * 
	 * @param format
	 *            a BinaryDataFormat to copy
	 * @throws NullPointerException
	 *             if the given format is a null
	 */
	public BinaryDataFormat(final BinaryDataFormat format)
			throws NullPointerException {
		if (format == null) {
			throw new NullPointerException();
		}

		// Copy the given attributes to be the current values
		isMBF = format.isMBF;

		shortSize = format.shortSize;
		intSize = format.intSize;
		longSize = format.longSize;
		longLongSize = format.longLongSize;
		floatSize = format.floatSize;
		doubleSize = format.doubleSize;
		longDoubleSize = format.longDoubleSize;

		sameByteOrder = (Host.isMBFByteOrder() == isMBF);
		sameShortSize = (Host.getStorageSize(Host.SHORT) == shortSize);
		sameIntSize = (Host.getStorageSize(Host.INT) == intSize);
		sameLongSize = (Host.getStorageSize(Host.LONG) == longSize);
		sameLongLongSize = (Host.getStorageSize(Host.LONGLONG) == longLongSize);
		sameFloatSize = (Host.getStorageSize(Host.FLOAT) == floatSize);
		sameDoubleSize = (Host.getStorageSize(Host.DOUBLE) == doubleSize);
		sameLongDoubleSize = (Host.getStorageSize(Host.LONGDOUBLE) == longDoubleSize);
	}

	/**
	 * Destroys a binary data format description.
	 * <P>
	 */
	@Override
	public final void finalize() {
	}

	// ----------------------------------------------------------------------
	// Object Methods
	// ----------------------------------------------------------------------
	/**
	 * Indicates whether another binary data format object is equal to this. For
	 * such an object to be equal, it must describe the same byte order and data
	 * type sizes as this object.
	 * <P>
	 * 
	 * @return true if the objects are equal; false otherwise
	 */
	public final boolean equals(final BinaryDataFormat bdf) {
		if (isMBF != bdf.isMBF) {
			return false;
		}
		if (shortSize != bdf.shortSize) {
			return false;
		}
		if (intSize != bdf.intSize) {
			return false;
		}
		if (longSize != bdf.longSize) {
			return false;
		}
		if (longLongSize != bdf.longLongSize) {
			return false;
		}
		if (floatSize != bdf.floatSize) {
			return false;
		}
		if (doubleSize != bdf.doubleSize) {
			return false;
		}
		if (longDoubleSize != bdf.longDoubleSize) {
			return false;
		}
		return true;
	}

	/**
	 * Indicates whether another binary data format object is equal to this. For
	 * such an object to be equal, it must describe the same byte order and data
	 * type sizes as this object.
	 * <P>
	 * 
	 * @return true if the objects are equal; false otherwise
	 */
	@Override
	public final boolean equals(final Object obj) {
		if (super.equals(obj) == false) {
			return false;
		}
		BinaryDataFormat bdf = (BinaryDataFormat) obj;
		return equals(bdf);
	}

	// ----------------------------------------------------------------------
	// Byte Order Methods
	// ----------------------------------------------------------------------
	/**
	 * Selects the byte order for binary data to be Most-significant-Byte-First
	 * (MBF).
	 * <P>
	 * 
	 * @see #setLBFByteOrder()
	 * @see #isMBFByteOrder()
	 * @see #isLBFByteOrder()
	 */
	public final void setMBFByteOrder() {
		isMBF = true;
		sameByteOrder = (isMBF == Host.isMBFByteOrder());
	}

	/**
	 * Selects the byte order for binary data to be Least-significant-Byte-First
	 * (LBF).
	 * <P>
	 * 
	 * @see #setMBFByteOrder()
	 * @see #isMBFByteOrder()
	 * @see #isLBFByteOrder()
	 */
	public final void setLBFByteOrder() {
		isMBF = false;
		sameByteOrder = (isMBF == Host.isMBFByteOrder());
	}

	/**
	 * Returns true if the binary data byte order is Most-significant-Byte-First
	 * (MBF); otherwise false when the byte order is instead
	 * Least-significant-Byte-First (LBF).
	 * <P>
	 * 
	 * @return true if MBF; false if LBF
	 * @see #setMBFByteOrder()
	 * @see #setLBFByteOrder()
	 */
	public final boolean isMBFByteOrder() {
		return isMBF;
	}

	/**
	 * Returns true if the binary data byte order is
	 * Least-significant-Byte-First (LBF); otherwise false when the byte order
	 * is instead Most-significant-Byte-First (MBF).
	 * <P>
	 * 
	 * @return true if LBF; false if MBF
	 * @see #setMBFByteOrder()
	 * @see #setLBFByteOrder()
	 */
	public final boolean isLBFByteOrder() {
		return !isMBF;
	}

	// ----------------------------------------------------------------------
	// Data Type Size Methods
	// ----------------------------------------------------------------------
	/**
	 * Sets the number of bytes occupied by a short integer.
	 * <P>
	 * 
	 * @param nBytes
	 *            the number of file bytes that make up a short
	 * @throws IllegalArgumentException
	 *             if nBytes is <= 0
	 */
	public final void setShortSize(final int nBytes)
			throws IllegalArgumentException {
		if (nBytes <= 0) {
			throw new IllegalArgumentException();
		}
		shortSize = (byte) nBytes;
		sameShortSize = (nBytes == Host.getStorageSize(Host.SHORT));
	}

	/**
	 * Sets the number of bytes occupied by an integer.
	 * <P>
	 * 
	 * @param nBytes
	 *            the number of file bytes that make up an int
	 * @throws IllegalArgumentException
	 *             if nBytes is <= 0
	 */
	public final void setIntSize(final int nBytes)
			throws IllegalArgumentException {
		if (nBytes <= 0) {
			throw new IllegalArgumentException();
		}
		intSize = (byte) nBytes;
		sameIntSize = (nBytes == Host.getStorageSize(Host.INT));
	}

	/**
	 * Sets the number of bytes occupied by a long integer.
	 * <P>
	 * 
	 * @param nBytes
	 *            the number of file bytes that make up a long
	 * @throws IllegalArgumentException
	 *             if nBytes is <= 0
	 */
	public final void setLongSize(final int nBytes)
			throws IllegalArgumentException {
		if (nBytes <= 0) {
			throw new IllegalArgumentException();
		}
		longSize = (byte) nBytes;
		sameLongSize = (nBytes == Host.getStorageSize(Host.LONG));
	}

	/**
	 * Sets the number of bytes occupied by a long long integer.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param nBytes
	 *            the number of file bytes that make up a long long
	 * @throws IllegalArgumentException
	 *             if nBytes is <= 0
	 */
	public final void setLongLongSize(final int nBytes)
			throws IllegalArgumentException {
		if (nBytes <= 0) {
			throw new IllegalArgumentException();
		}
		longLongSize = (byte) nBytes;
		sameLongLongSize = (nBytes == Host.getStorageSize(Host.LONGLONG));
	}

	/**
	 * Sets the number of bytes occupied by a single-precision float.
	 * <P>
	 * The IEEE 754 specification recognizes floating point values that are 4,
	 * 8, or 16 bytes in size. An IllegalArgumentException is thrown if the
	 * given size is not one of these.
	 * <P>
	 * 
	 * @param nBytes
	 *            the number of file bytes that make up a float
	 * @throws IllegalArgumentException
	 *             if nBytes is <= 0 or not 4, 8, or 16
	 */
	public final void setFloatSize(final int nBytes)
			throws IllegalArgumentException {
		if (nBytes <= 0) {
			throw new IllegalArgumentException();
		}
		if (nBytes != 4 && nBytes != 8 && nBytes != 16) {
			throw new IllegalArgumentException();
		}
		floatSize = (byte) nBytes;
		sameFloatSize = (nBytes == Host.getStorageSize(Host.FLOAT));
	}

	/**
	 * Sets the number of bytes occupied by a double-precision float.
	 * <P>
	 * The IEEE 754 specification recognizes floating point values that are 4,
	 * 8, or 16 bytes in size. An IllegalArgumentException is thrown if the
	 * given size is not one of these.
	 * <P>
	 * 
	 * @param nBytes
	 *            the number of file bytes that make up a double
	 * @throws IllegalArgumentException
	 *             if nBytes is <= 0 or not 4, 8, or 16
	 */
	public final void setDoubleSize(final int nBytes)
			throws IllegalArgumentException {
		if (nBytes <= 0) {
			throw new IllegalArgumentException();
		}
		if (nBytes != 4 && nBytes != 8 && nBytes != 16) {
			throw new IllegalArgumentException();
		}
		doubleSize = (byte) nBytes;
		sameDoubleSize = (nBytes == Host.getStorageSize(Host.DOUBLE));
	}

	/**
	 * Sets the number of bytes occupied by a long double-precision float.
	 * <P>
	 * The IEEE 754 specification recognizes floating point values that are 4,
	 * 8, or 16 bytes in size. An IllegalArgumentException is thrown if the
	 * given size is not one of these.
	 * <P>
	 * Java doesn't support long double types directly. While long double values
	 * may be manipulated using this class, if decoded, the largest returnable
	 * portion of the long double is a Java double. Similarly, the largest
	 * encodable value into a long double is one from a Java double.
	 * <P>
	 * 
	 * @param nBytes
	 *            the number of file bytes that make up a long double
	 * @throws IllegalArgumentException
	 *             if nBytes is <= 0 or not 4, 8, or 16
	 */
	public final void setLongDoubleSize(final int nBytes)
			throws IllegalArgumentException {
		if (nBytes <= 0) {
			throw new IllegalArgumentException();
		}
		if (nBytes != 4 && nBytes != 8 && nBytes != 16) {
			throw new IllegalArgumentException();
		}
		longDoubleSize = (byte) nBytes;
		sameLongDoubleSize = (nBytes == Host.getStorageSize(Host.LONGDOUBLE));
	}

	/**
	 * Gets the number of bytes occupied by a short integer.
	 * <P>
	 * 
	 * @return the number of file bytes that make up a short
	 */
	public final int getShortSize() {
		return shortSize;
	}

	/**
	 * Gets the number of bytes occupied by an integer.
	 * <P>
	 * 
	 * @return the number of file bytes that make up an int
	 */
	public final int getIntSize() {
		return intSize;
	}

	/**
	 * Gets the number of bytes occupied by a long integer.
	 * <P>
	 * 
	 * @return the number of file bytes that make up a long
	 */
	public final int getLongSize() {
		return longSize;
	}

	/**
	 * Gets the number of bytes occupied by a long long integer.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @return the number of file bytes that make up a long long
	 */
	public final int getLongLongSize() {
		return longLongSize;
	}

	/**
	 * Gets the number of bytes occupied by a single-precision float.
	 * <P>
	 * 
	 * @return the number of file bytes that make up a float
	 */
	public final int getFloatSize() {
		return floatSize;
	}

	/**
	 * Gets the number of bytes occupied by a double-precision float.
	 * <P>
	 * 
	 * @return the number of file bytes that make up a double
	 */
	public final int getDoubleSize() {
		return doubleSize;
	}

	/**
	 * Gets the number of bytes occupied by a long double-precision float.
	 * <P>
	 * Java doesn't support long double types directly. While long double values
	 * may be manipulated using this class, if decoded, the largest returnable
	 * portion of the long double is a Java double. Similarly, the largest
	 * encodable value into a long double is one from a Java double.
	 * <P>
	 * 
	 * @return the number of file bytes that make up a long double
	 */
	public final int getLongDoubleSize() {
		return longDoubleSize;
	}

	// ----------------------------------------------------------------------
	// Conversion to Host Format Methods
	// ----------------------------------------------------------------------

	//
	// Without array offset
	//
	/**
	 * Decodes the binary short value contained in the byte array, and described
	 * by this binary data format, into a short in the host's native binary data
	 * format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and short size is used to extract
	 * the appropriate number of bytes from the start of the byte array. Those
	 * bytes may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format short.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's short is larger than that
	 * of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's short is smaller than that
	 * of the host, the upper bytes of the returned value are padded with zeroes
	 * or ones (sign-extension) depending upon if the value is positive or
	 * negative, respectively.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the host-format value
	 */
	public final short shortValue(final byte[] bytes) {
		if (sameShortSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, shortSize);
			}
			return Host.castToShort(bytes);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = shortSize;
		short value = 0;
		if (isMBF) {
			if ((bytes[0] & (byte) 0x80) > 0) {
				value = ~0; // Pad with 1's for negative
			}
			for (int i = 0; i < nBytes; i++) {
				value = (short) ((value << 8) | ((bytes[i]) & 0x000000FF));
			}
			return value;
		}
		if ((bytes[nBytes - 1] & (byte) 0x80) > 0) {
			value = ~0; // Pad with 1's for negative
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = (short) ((value << 8) | ((bytes[i]) & 0x000000FF));
		}
		return value;
	}

	/**
	 * Decodes the binary unsigned short value contained in the byte array, and
	 * described by this binary data format, into an unsigned short in the
	 * host's native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and short size is used to extract
	 * the appropriate number of bytes from the start of the byte array. Those
	 * bytes may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format unsigned short.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's unsigned short is larger
	 * than that of the host, truncation may occur.
	 * <P>
	 * Java doesn't support unsigned types directly. The unsigned short value is
	 * therefore returned as an int, but with the upper bytes padded with
	 * zeroes.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the host-format value (as an int)
	 */
	public final int unsignedShortValue(final byte[] bytes) {
		if (sameShortSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, shortSize);
			}
			return Host.castToShort(bytes);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = shortSize;
		int value = 0;
		if (isMBF) {
			for (int i = 0; i < nBytes; i++) {
				value = (value << 8) | ((bytes[i]) & 0x000000FF);
			}
			value &= ~(~0 << Host.getSignificantBits(Host.SHORT));
			return value;
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = (value << 8) | ((bytes[i]) & 0x000000FF);
		}
		value &= ~(~0 << Host.getSignificantBits(Host.SHORT));
		return value;
	}

	/**
	 * Decodes the binary integer value contained in the byte array, and
	 * described by this binary data format, into an integer in the host's
	 * native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and integer size is used to extract
	 * the appropriate number of bytes from the start of the byte array. Those
	 * bytes may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format integer.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's integer is larger than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's integer is smaller than
	 * that of the host, the upper bytes of the returned value are padded with
	 * zeroes or ones (sign-extension) depending upon if the value is positive
	 * or negative, respectively.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the host-format value
	 */
	public final int intValue(final byte[] bytes) {
		if (sameIntSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, intSize);
			}
			return Host.castToInt(bytes);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = intSize;
		int value = 0;
		if (isMBF) {
			if ((bytes[0] & (byte) 0x80) > 0) {
				value = ~0; // Pad with 1's for negative
			}
			for (int i = 0; i < nBytes; i++) {
				value = ((value << 8) | ((bytes[i]) & 0x000000FF));
			}
			return value;
		}
		if ((bytes[nBytes - 1] & (byte) 0x80) > 0) {
			value = ~0; // Pad with 1's for negative
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = ((value << 8) | ((bytes[i]) & 0x000000FF));
		}
		return value;
	}

	/**
	 * Decodes the binary unsigned integer value contained in the byte array,
	 * and described by this binary data format, into an unsigned integer in the
	 * host's native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and unsigned integer size is used to
	 * extract the appropriate number of bytes from the start of the byte array.
	 * Those bytes may be swapped to match the host's byte order. The bytes are
	 * then assembled to form a host-format unsigned integer.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's unsigned integer is larger
	 * than that of the host, truncation may occur.
	 * <P>
	 * Java doesn't support unsigned types directly. The unsigned int value is
	 * therefore returned as a long, but with the upper bytes padded with
	 * zeroes.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the host-format value (as a long)
	 */
	public final long unsignedIntValue(final byte[] bytes) {
		if (sameIntSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, intSize);
			}
			return Host.castToInt(bytes);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = intSize;
		long value = 0L;
		if (isMBF) {
			for (int i = 0; i < nBytes; i++) {
				value = (value << 8) | (((long) bytes[i]) & (long) 0x000000FF);
			}
			value &= ~(~(long) 0 << Host.getSignificantBits(Host.INT));
			return value;
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = (value << 8) | (((long) bytes[i]) & (long) 0x000000FF);
		}
		value &= ~(~(long) 0 << Host.getSignificantBits(Host.INT));
		return value;
	}

	/**
	 * Decodes the binary long value contained in the byte array, and described
	 * by this binary data format, into a long in the host's native binary data
	 * format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and long size is used to extract the
	 * appropriate number of bytes from the start of the byte array. Those bytes
	 * may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format integer.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's long is larger than that
	 * of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's long is smaller than that
	 * of the host, the upper bytes of the returned value are padded with zeroes
	 * or ones (sign-extension) depending upon if the value is positive or
	 * negative, respectively.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the host-format value
	 */
	public final long longValue(final byte[] bytes) {
		if (sameLongSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, longSize);
			}
			return Host.castToLong(bytes);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longSize;
		long value = 0L;
		if (isMBF) {
			if ((bytes[0] & (byte) 0x80) > 0) {
				value = ~0; // Pad with 1's for negative
			}
			for (int i = 0; i < nBytes; i++) {
				value = ((value << 8) | (((long) bytes[i]) & 0x000000FF));
			}
			return value;
		}
		if ((bytes[nBytes - 1] & (byte) 0x80) > 0) {
			value = ~0; // Pad with 1's for negative
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = ((value << 8) | (((long) bytes[i]) & 0x000000FF));
		}
		return value;
	}

	/**
	 * Decodes the binary unsigned long value contained in the byte array, and
	 * described by this binary data format, into an unsigned long in the host's
	 * native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and unsigned long size is used to
	 * extract the appropriate number of bytes from the start of the byte array.
	 * Those bytes may be swapped to match the host's byte order. The bytes are
	 * then assembled to form a host-format unsigned long.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's unsigned long is larger
	 * than that of the host, truncation may occur.
	 * <P>
	 * Java doesn't support unsigned types directly. The unsigned long value is
	 * therefore returned as a long.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the host-format value
	 */
	public final long unsignedLongValue(final byte[] bytes) {
		if (sameLongSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, longSize);
			}
			return Host.castToLong(bytes);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longSize;
		long value = 0L;
		if (isMBF) {
			for (int i = 0; i < nBytes; i++) {
				value = (value << 8) | (((long) bytes[i]) & (long) 0x000000FF);
			}
			return value;
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = (value << 8) | (((long) bytes[i]) & (long) 0x000000FF);
		}
		return value;
	}

	/**
	 * Decodes the binary long long value contained in the byte array, and
	 * described by this binary data format, into a long long in the host's
	 * native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and long long size is used to
	 * extract the appropriate number of bytes from the start of the byte array.
	 * Those bytes may be swapped to match the host's byte order. The bytes are
	 * then assembled to form a host-format integer.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's long long is larger than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's long long is smaller than
	 * that of the host, the upper bytes of the returned value are padded with
	 * zeroes or ones (sign-extension) depending upon if the value is positive
	 * or negative, respectively.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the host-format value
	 */
	public final long longLongValue(final byte[] bytes) {
		if (sameLongLongSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, longLongSize);
			}
			return Host.castToLongLong(bytes);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longLongSize;
		long value = 0L;
		if (isMBF) {
			if ((bytes[0] & (byte) 0x80) > 0) {
				value = ~0; // Pad with 1's for negative
			}
			for (int i = 0; i < nBytes; i++) {
				value = ((value << 8) | (((long) bytes[i]) & 0x000000FF));
			}
			return value;
		}
		if ((bytes[nBytes - 1] & (byte) 0x80) > 0) {
			value = ~0; // Pad with 1's for negative
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = ((value << 8) | (((long) bytes[i]) & 0x000000FF));
		}
		return value;
	}

	/**
	 * Decodes the binary unsigned long long value contained in the byte array,
	 * and described by this binary data format, into an unsigned long long in
	 * the host's native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and unsigned long long size is used
	 * to extract the appropriate number of bytes from the start of the byte
	 * array. Those bytes may be swapped to match the host's byte order. The
	 * bytes are then assembled to form a host-format unsigned long long.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's unsigned long long is
	 * larger than that of the host, truncation may occur.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the host-format value
	 */
	public final long unsignedLongLongValue(final byte[] bytes) {
		if (sameLongLongSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, longLongSize);
			}
			return Host.castToLongLong(bytes);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longLongSize;
		long value = 0L;
		if (isMBF) {
			for (int i = 0; i < nBytes; i++) {
				value = (value << 8) | (((long) bytes[i]) & (long) 0x000000FF);
			}
			return value;
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = (value << 8) | (((long) bytes[i]) & (long) 0x000000FF);
		}
		return value;
	}

	/**
	 * Decodes the binary float value contained in the byte array, and described
	 * by this binary data format, into a float in the host's native binary data
	 * format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and float size is used to extract
	 * the appropriate number of bytes from the start of the byte array. Those
	 * bytes may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format float.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's float is larger than that
	 * of the host, truncation may occur.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the host-format value
	 */
	public final float floatValue(final byte[] bytes) {
		if (sameFloatSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, floatSize);
			}
			return Host.castToFloat(bytes);
		}

		// Different number of bytes - any similar host floats?
		int nBytes = floatSize;
		if (Host.getStorageSize(Host.DOUBLE) == nBytes) {
			// Swap and cast to double, then to float
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return (float) Host.castToDouble(bytes);
		}
		if (Host.getStorageSize(Host.LONGDOUBLE) == nBytes) {
			// Swap and cast to long double, then to float
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return (float) Host.castToLongDouble(bytes);
		}

		// Incompatible - must build float
		// IMPLEMENT!
		return (float) 0.0;
	}

	/**
	 * Decodes the binary double value contained in the byte array, and
	 * described by this binary data format, into a double in the host's native
	 * binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and double size is used to extract
	 * the appropriate number of bytes from the start of the byte array. Those
	 * bytes may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format double.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's double is larger than that
	 * of the host, truncation may occur.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the host-format value
	 */
	public final double doubleValue(final byte[] bytes) {
		if (sameDoubleSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, doubleSize);
			}
			return Host.castToDouble(bytes);
		}

		// Different number of bytes - any similar host floats?
		int nBytes = doubleSize;
		if (Host.getStorageSize(Host.FLOAT) == nBytes) {
			// Swap and cast to float, then to double
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return Host.castToFloat(bytes);
		}
		if (Host.getStorageSize(Host.LONGDOUBLE) == nBytes) {
			// Swap and cast to long double, then to double
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return Host.castToLongDouble(bytes);
		}

		// Incompatible - must build double
		// IMPLEMENT!
		return 0.0;
	}

	/**
	 * Decodes the binary long double value contained in the byte array, and
	 * described by this binary data format, into a long double in the host's
	 * native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and long double size is used to
	 * extract the appropriate number of bytes from the start of the byte array.
	 * Those bytes may be swapped to match the host's byte order. The bytes are
	 * then assembled to form a host-format long double.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's long double is larger than
	 * that of the host, truncation may occur.
	 * <P>
	 * Java doesn't support long double types directly. While long double values
	 * may be manipulated using this class, if decoded, the largest returnable
	 * portion of the long double is a Java double. Similarly, the largest
	 * encodable value into a long double is one from a Java double.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the host-format value
	 */
	public final double longDoubleValue(final byte[] bytes) {
		if (sameLongDoubleSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, longDoubleSize);
			}
			return Host.castToLongDouble(bytes);
		}

		// Different number of bytes - any similar host floats?
		int nBytes = longDoubleSize;
		if (Host.getStorageSize(Host.FLOAT) == nBytes) {
			// Swap and cast to float, then to long double
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return Host.castToFloat(bytes);
		}
		if (Host.getStorageSize(Host.DOUBLE) == nBytes) {
			// Swap and cast to double, then to long double
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return Host.castToDouble(bytes);
		}

		// Incompatible - must build long double
		// IMPLEMENT!
		return 0.0;
	}

	//
	// With array offset
	//
	/**
	 * Decodes the binary short value contained in the byte array, and described
	 * by this binary data format, into a short in the host's native binary data
	 * format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and short size is used to extract
	 * the appropriate number of bytes from the start of the byte array. Those
	 * bytes may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format short.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's short is larger than that
	 * of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's short is smaller than that
	 * of the host, the upper bytes of the returned value are padded with zeroes
	 * or ones (sign-extension) depending upon if the value is positive or
	 * negative, respectively.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the host-format value
	 */
	public final short shortValue(final byte[] bytes, final int offset) {
		if (sameShortSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, offset, shortSize);
			}
			return Host.castToShort(bytes, offset);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = shortSize;
		short value = 0;
		if (isMBF) {
			if ((bytes[offset] & (byte) 0x80) > 0) {
				value = ~0; // Pad with 1's for negative
			}
			for (int i = 0; i < nBytes; i++) {
				value = (short) ((value << 8) | ((bytes[offset + i]) & 0x000000FF));
			}
			return value;
		}
		if ((bytes[offset + nBytes - 1] & (byte) 0x80) > 0) {
			value = ~0; // Pad with 1's for negative
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = (short) ((value << 8) | ((bytes[offset + i]) & 0x000000FF));
		}
		return value;
	}

	/**
	 * Decodes the binary unsigned short value contained in the byte array, and
	 * described by this binary data format, into an unsigned short in the
	 * host's native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and short size is used to extract
	 * the appropriate number of bytes from the start of the byte array. Those
	 * bytes may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format unsigned short.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's unsigned short is larger
	 * than that of the host, truncation may occur.
	 * <P>
	 * Java doesn't support unsigned types directly. The unsigned short value is
	 * therefore returned as an int, but with the upper bytes padded with
	 * zeroes.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the host-format value (as an int)
	 */
	public final int unsignedShortValue(final byte[] bytes, final int offset) {
		if (sameShortSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, offset, shortSize);
			}
			return Host.castToShort(bytes, offset);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = shortSize;
		int value = 0;
		if (isMBF) {
			for (int i = 0; i < nBytes; i++) {
				value = (value << 8) | ((bytes[offset + i]) & 0x000000FF);
			}
			value &= ~(~0 << Host.getSignificantBits(Host.SHORT));
			return value;
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = (value << 8) | ((bytes[offset + i]) & 0x000000FF);
		}
		value &= ~(~0 << Host.getSignificantBits(Host.SHORT));
		return value;
	}

	/**
	 * Decodes the binary integer value contained in the byte array, and
	 * described by this binary data format, into an integer in the host's
	 * native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and integer size is used to extract
	 * the appropriate number of bytes from the start of the byte array. Those
	 * bytes may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format integer.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's integer is larger than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's integer is smaller than
	 * that of the host, the upper bytes of the returned value are padded with
	 * zeroes or ones (sign-extension) depending upon if the value is positive
	 * or negative, respectively.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the host-format value
	 */
	public final int intValue(final byte[] bytes, final int offset) {
		if (sameIntSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, offset, intSize);
			}
			return Host.castToInt(bytes, offset);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = intSize;
		int value = 0;
		if (isMBF) {
			if ((bytes[offset] & (byte) 0x80) > 0) {
				value = ~0; // Pad with 1's for negative
			}
			for (int i = 0; i < nBytes; i++) {
				value = ((value << 8) | ((bytes[offset + i]) & 0x000000FF));
			}
			return value;
		}
		if ((bytes[offset + nBytes - 1] & (byte) 0x80) > 0) {
			value = ~0; // Pad with 1's for negative
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = ((value << 8) | ((bytes[offset + i]) & 0x000000FF));
		}
		return value;
	}

	/**
	 * Decodes the binary unsigned integer value contained in the byte array,
	 * and described by this binary data format, into an unsigned integer in the
	 * host's native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and unsigned integer size is used to
	 * extract the appropriate number of bytes from the start of the byte array.
	 * Those bytes may be swapped to match the host's byte order. The bytes are
	 * then assembled to form a host-format unsigned integer.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's unsigned integer is larger
	 * than that of the host, truncation may occur.
	 * <P>
	 * Java doesn't support unsigned types directly. The unsigned int value is
	 * therefore returned as a long, but with the upper bytes padded with
	 * zeroes.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the host-format value (as a long)
	 */
	public final long unsignedIntValue(final byte[] bytes, final int offset) {
		if (sameIntSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, offset, intSize);
			}
			return Host.castToInt(bytes, offset);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = intSize;
		long value = 0L;
		if (isMBF) {
			for (int i = 0; i < nBytes; i++) {
				value = (value << 8)
						| (((long) bytes[offset + i]) & (long) 0x000000FF);
			}
			value &= ~(~(long) 0 << Host.getSignificantBits(Host.INT));
			return value;
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = (value << 8)
					| (((long) bytes[offset + i]) & (long) 0x000000FF);
		}
		value &= ~(~(long) 0 << Host.getSignificantBits(Host.INT));
		return value;
	}

	/**
	 * Decodes the binary long value contained in the byte array, and described
	 * by this binary data format, into a long in the host's native binary data
	 * format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and long size is used to extract the
	 * appropriate number of bytes from the start of the byte array. Those bytes
	 * may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format integer.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's long is larger than that
	 * of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's long is smaller than that
	 * of the host, the upper bytes of the returned value are padded with zeroes
	 * or ones (sign-extension) depending upon if the value is positive or
	 * negative, respectively.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the host-format value
	 */
	public final long longValue(final byte[] bytes, final int offset) {
		if (sameLongSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, offset, longSize);
			}
			return Host.castToLong(bytes, offset);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longSize;
		long value = 0L;
		if (isMBF) {
			if ((bytes[offset] & (byte) 0x80) > 0) {
				value = ~0; // Pad with 1's for negative
			}
			for (int i = 0; i < nBytes; i++) {
				value = ((value << 8) | (((long) bytes[offset + i]) & 0x000000FF));
			}
			return value;
		}
		if ((bytes[offset + nBytes - 1] & (byte) 0x80) > 0) {
			value = ~0; // Pad with 1's for negative
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = ((value << 8) | (((long) bytes[offset + i]) & 0x000000FF));
		}
		return value;
	}

	/**
	 * Decodes the binary unsigned long value contained in the byte array, and
	 * described by this binary data format, into an unsigned long in the host's
	 * native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and unsigned long size is used to
	 * extract the appropriate number of bytes from the start of the byte array.
	 * Those bytes may be swapped to match the host's byte order. The bytes are
	 * then assembled to form a host-format unsigned long.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's unsigned long is larger
	 * than that of the host, truncation may occur.
	 * <P>
	 * Java doesn't support unsigned types directly. The unsigned long value is
	 * therefore returned as a long.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the host-format value
	 */
	public final long unsignedLongValue(final byte[] bytes, final int offset) {
		if (sameLongSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, offset, longSize);
			}
			return Host.castToLong(bytes, offset);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longSize;
		long value = 0L;
		if (isMBF) {
			for (int i = 0; i < nBytes; i++) {
				value = (value << 8)
						| (((long) bytes[offset + i]) & (long) 0x000000FF);
			}
			return value;
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = (value << 8)
					| (((long) bytes[offset + i]) & (long) 0x000000FF);
		}
		return value;
	}

	/**
	 * Decodes the binary long long value contained in the byte array, and
	 * described by this binary data format, into a long long in the host's
	 * native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and long long size is used to
	 * extract the appropriate number of bytes from the start of the byte array.
	 * Those bytes may be swapped to match the host's byte order. The bytes are
	 * then assembled to form a host-format integer.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's long long is larger than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's long long is smaller than
	 * that of the host, the upper bytes of the returned value are padded with
	 * zeroes or ones (sign-extension) depending upon if the value is positive
	 * or negative, respectively.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the host-format value
	 */
	public final long longLongValue(final byte[] bytes, final int offset) {
		if (sameLongLongSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, offset, longLongSize);
			}
			return Host.castToLongLong(bytes, offset);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longLongSize;
		long value = 0L;
		if (isMBF) {
			if ((bytes[offset] & (byte) 0x80) > 0) {
				value = ~0; // Pad with 1's for negative
			}
			for (int i = 0; i < nBytes; i++) {
				value = ((value << 8) | (((long) bytes[offset + i]) & 0x000000FF));
			}
			return value;
		}
		if ((bytes[offset + nBytes - 1] & (byte) 0x80) > 0) {
			value = ~0; // Pad with 1's for negative
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = ((value << 8) | (((long) bytes[offset + i]) & 0x000000FF));
		}
		return value;
	}

	/**
	 * Decodes the binary unsigned long long value contained in the byte array,
	 * and described by this binary data format, into an unsigned long long in
	 * the host's native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and unsigned long long size is used
	 * to extract the appropriate number of bytes from the start of the byte
	 * array. Those bytes may be swapped to match the host's byte order. The
	 * bytes are then assembled to form a host-format unsigned long long.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's unsigned long long is
	 * larger than that of the host, truncation may occur.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the host-format value
	 */
	public final long unsignedLongLongValue(final byte[] bytes, final int offset) {
		if (sameLongLongSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, offset, longLongSize);
			}
			return Host.castToLongLong(bytes, offset);
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longLongSize;
		long value = 0L;
		if (isMBF) {
			for (int i = 0; i < nBytes; i++) {
				value = (value << 8)
						| (((long) bytes[offset + i]) & (long) 0x000000FF);
			}
			return value;
		}
		for (int i = nBytes - 1; i >= 0; i--) {
			value = (value << 8)
					| (((long) bytes[offset + i]) & (long) 0x000000FF);
		}
		return value;
	}

	/**
	 * Decodes the binary float value contained in the byte array, and described
	 * by this binary data format, into a float in the host's native binary data
	 * format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and float size is used to extract
	 * the appropriate number of bytes from the start of the byte array. Those
	 * bytes may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format float.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's float is larger than that
	 * of the host, truncation may occur.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the host-format value
	 */
	public final float floatValue(final byte[] bytes, final int offset) {
		if (sameFloatSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, offset, floatSize);
			}
			return Host.castToFloat(bytes, offset);
		}

		// Different number of bytes - any similar host floats?
		int nBytes = floatSize;
		if (Host.getStorageSize(Host.DOUBLE) == nBytes) {
			// Swap and cast to double, then to float
			if (!sameByteOrder) {
				Host.swap(bytes, offset, nBytes);
			}
			return (float) Host.castToDouble(bytes, offset);
		}
		if (Host.getStorageSize(Host.LONGDOUBLE) == nBytes) {
			// Swap and cast to long double, then to float
			if (!sameByteOrder) {
				Host.swap(bytes, offset, nBytes);
			}
			return (float) Host.castToLongDouble(bytes, offset);
		}

		// Incompatible - must build float
		// IMPLEMENT!
		return (float) 0.0;
	}

	/**
	 * Decodes the binary double value contained in the byte array, and
	 * described by this binary data format, into a double in the host's native
	 * binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and double size is used to extract
	 * the appropriate number of bytes from the start of the byte array. Those
	 * bytes may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format double.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's double is larger than that
	 * of the host, truncation may occur.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the host-format value
	 */
	public final double doubleValue(final byte[] bytes, final int offset) {
		if (sameDoubleSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, offset, doubleSize);
			}
			return Host.castToDouble(bytes, offset);
		}

		// Different number of bytes - any similar host floats?
		int nBytes = doubleSize;
		if (Host.getStorageSize(Host.FLOAT) == nBytes) {
			// Swap and cast to float, then to double
			if (!sameByteOrder) {
				Host.swap(bytes, offset, nBytes);
			}
			return Host.castToFloat(bytes, offset);
		}
		if (Host.getStorageSize(Host.LONGDOUBLE) == nBytes) {
			// Swap and cast to long double, then to double
			if (!sameByteOrder) {
				Host.swap(bytes, offset, nBytes);
			}
			return Host.castToLongDouble(bytes, offset);
		}

		// Incompatible - must build double
		// IMPLEMENT!
		return 0.0;
	}

	/**
	 * Decodes the binary long double value contained in the byte array, and
	 * described by this binary data format, into a long double in the host's
	 * native binary data format. The host-format value is returned.
	 * <P>
	 * This binary data format's byte order and long double size is used to
	 * extract the appropriate number of bytes from the start of the byte array.
	 * Those bytes may be swapped to match the host's byte order. The bytes are
	 * then assembled to form a host-format long double.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's long double is larger than
	 * that of the host, truncation may occur.
	 * <P>
	 * Java doesn't support long double types directly. While long double values
	 * may be manipulated using this class, if decoded, the largest returnable
	 * portion of the long double is a Java double. Similarly, the largest
	 * encodable value into a long double is one from a Java double.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the host-format value
	 */
	public final double longDoubleValue(final byte[] bytes, final int offset) {
		if (sameLongDoubleSize) {
			// Same number of bytes as this host - swap and cast
			if (!sameByteOrder) {
				Host.swap(bytes, offset, longDoubleSize);
			}
			return Host.castToLongDouble(bytes, offset);
		}

		// Different number of bytes - any similar host floats?
		int nBytes = longDoubleSize;
		if (Host.getStorageSize(Host.FLOAT) == nBytes) {
			// Swap and cast to float, then to long double
			if (!sameByteOrder) {
				Host.swap(bytes, offset, nBytes);
			}
			return Host.castToFloat(bytes, offset);
		}
		if (Host.getStorageSize(Host.DOUBLE) == nBytes) {
			// Swap and cast to double, then to long double
			if (!sameByteOrder) {
				Host.swap(bytes, offset, nBytes);
			}
			return Host.castToDouble(bytes, offset);
		}

		// Incompatible - must build long double
		// IMPLEMENT!
		return 0.0;
	}

	// Array methods
	/**
	 * Decodes the nValues binary short values contained in the byte array, and
	 * described by this binary data format, into an array of shorts in the
	 * host's native binary data format. The host-format values are returned in
	 * the values array.
	 * <P>
	 * The given values array is presumed to be large enough to receive nValues
	 * values. The bytes array is presumed to be large enough to supply data for
	 * these values.
	 * <P>
	 * This binary data format's byte order and short size is used to extract
	 * the appropriate number of bytes for the byte array. Those bytes may be
	 * swapped to match the host's byte order. The bytes are then assembled to
	 * form a host-format short.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's short is larger than that
	 * of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's short is smaller than that
	 * of the host, the upper bytes of the returned value are padded with zeroes
	 * or ones (sign-extension) depending upon if the value is positive or
	 * negative, respectively.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param values
	 *            the returned list of host-format values
	 * @param nValues
	 *            the number of values to return
	 */
	public final void shortValues(final byte[] bytes, final short[] values,
			final int nValues) {
		int nBytes = shortSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			values[i] = shortValue(bytes, offset);
			offset += nBytes;
		}
	}

	/**
	 * Decodes the nValues binary unsigned short values contained in the byte
	 * array, and described by this binary data format, into an array of
	 * unsigned shorts in the host's native binary data format. The host-format
	 * values are returned in the values array.
	 * <P>
	 * The given values array is presumed to be large enough to receive nValues
	 * values. The bytes array is presumed to be large enough to supply data for
	 * these values.
	 * <P>
	 * This binary data format's byte order and unsigned short size is used to
	 * extract the appropriate number of bytes for the byte array. Those bytes
	 * may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format unsigned short.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's unsigned short is larger
	 * than that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's unsigned short is smaller
	 * than that of the host, the upper bytes of the returned value are padded
	 * with zeroes or ones (sign-extension) depending upon if the value is
	 * positive or negative, respectively.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param values
	 *            the returned list of host-format values
	 * @param nValues
	 *            the number of values to return
	 */
	public final void unsignedShortValues(final byte[] bytes,
			final short[] values, final int nValues) {
		int nBytes = shortSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			values[i] = (short) unsignedShortValue(bytes, offset);
			offset += nBytes;
		}
	}

	/**
	 * Decodes the nValues binary int values contained in the byte array, and
	 * described by this binary data format, into an array of ints in the host's
	 * native binary data format. The host-format values are returned in the
	 * values array.
	 * <P>
	 * The given values array is presumed to be large enough to receive nValues
	 * values. The bytes array is presumed to be large enough to supply data for
	 * these values.
	 * <P>
	 * This binary data format's byte order and int size is used to extract the
	 * appropriate number of bytes for the byte array. Those bytes may be
	 * swapped to match the host's byte order. The bytes are then assembled to
	 * form a host-format int.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's int is larger than that of
	 * the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's int is smaller than that
	 * of the host, the upper bytes of the returned value are padded with zeroes
	 * or ones (sign-extension) depending upon if the value is positive or
	 * negative, respectively.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param values
	 *            the returned list of host-format values
	 * @param nValues
	 *            the number of values to return
	 */
	public final void intValues(final byte[] bytes, final int[] values,
			final int nValues) {
		int nBytes = intSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			values[i] = intValue(bytes, offset);
			offset += nBytes;
		}
	}

	/**
	 * Decodes the nValues binary unsigned int values contained in the byte
	 * array, and described by this binary data format, into an array of
	 * unsigned ints in the host's native binary data format. The host-format
	 * values are returned in the values array.
	 * <P>
	 * The given values array is presumed to be large enough to receive nValues
	 * values. The bytes array is presumed to be large enough to supply data for
	 * these values.
	 * <P>
	 * This binary data format's byte order and unsigned int size is used to
	 * extract the appropriate number of bytes for the byte array. Those bytes
	 * may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format unsigned int.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's unsigned int is larger
	 * than that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's unsigned int is smaller
	 * than that of the host, the upper bytes of the returned value are padded
	 * with zeroes or ones (sign-extension) depending upon if the value is
	 * positive or negative, respectively.
	 * <P>
	 * Java doesn't support unsigned types directly. This method is identical to
	 * the signed version.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param values
	 *            the returned list of host-format values
	 * @param nValues
	 *            the number of values to return
	 */
	public final void unsignedIntValues(final byte[] bytes, final int[] values,
			final int nValues) {
		int nBytes = intSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			values[i] = (int) unsignedIntValue(bytes, offset);
			offset += nBytes;
		}
	}

	/**
	 * Decodes the nValues binary long values contained in the byte array, and
	 * described by this binary data format, into an array of longs in the
	 * host's native binary data format. The host-format values are returned in
	 * the values array.
	 * <P>
	 * The given values array is presumed to be large enough to receive nValues
	 * values. The bytes array is presumed to be large enough to supply data for
	 * these values.
	 * <P>
	 * This binary data format's byte order and long size is used to extract the
	 * appropriate number of bytes for the byte array. Those bytes may be
	 * swapped to match the host's byte order. The bytes are then assembled to
	 * form a host-format long.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's long is larger than that
	 * of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's long is smaller than that
	 * of the host, the upper bytes of the returned value are padded with zeroes
	 * or ones (sign-extension) depending upon if the value is positive or
	 * negative, respectively.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param values
	 *            the returned list of host-format values
	 * @param nValues
	 *            the number of values to return
	 */
	public final void longValues(final byte[] bytes, final long[] values,
			final int nValues) {
		int nBytes = longSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			values[i] = longValue(bytes, offset);
			offset += nBytes;
		}
	}

	/**
	 * Decodes the nValues binary unsigned long values contained in the byte
	 * array, and described by this binary data format, into an array of
	 * unsigned longs in the host's native binary data format. The host-format
	 * values are returned in the values array.
	 * <P>
	 * The given values array is presumed to be large enough to receive nValues
	 * values. The bytes array is presumed to be large enough to supply data for
	 * these values.
	 * <P>
	 * This binary data format's byte order and unsigned long size is used to
	 * extract the appropriate number of bytes for the byte array. Those bytes
	 * may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format unsigned long.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's unsigned long is larger
	 * than that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's unsigned long is smaller
	 * than that of the host, the upper bytes of the returned value are padded
	 * with zeroes or ones (sign-extension) depending upon if the value is
	 * positive or negative, respectively.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param values
	 *            the returned list of host-format values
	 * @param nValues
	 *            the number of values to return
	 */
	public final void unsignedLongValues(final byte[] bytes,
			final long[] values, final int nValues) {
		int nBytes = longSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			values[i] = unsignedLongValue(bytes, offset);
			offset += nBytes;
		}
	}

	/**
	 * Decodes the nValues binary long long values contained in the byte array,
	 * and described by this binary data format, into an array of long longs in
	 * the host's native binary data format. The host-format values are returned
	 * in the values array.
	 * <P>
	 * The given values array is presumed to be large enough to receive nValues
	 * values. The bytes array is presumed to be large enough to supply data for
	 * these values.
	 * <P>
	 * This binary data format's byte order and long long size is used to
	 * extract the appropriate number of bytes for the byte array. Those bytes
	 * may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format long long.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's long long is larger than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's long long is smaller than
	 * that of the host, the upper bytes of the returned value are padded with
	 * zeroes or ones (sign-extension) depending upon if the value is positive
	 * or negative, respectively.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param values
	 *            the returned list of host-format values
	 * @param nValues
	 *            the number of values to return
	 */
	public final void longLongValues(final byte[] bytes, final long[] values,
			final int nValues) {
		int nBytes = longLongSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			values[i] = longLongValue(bytes, offset);
			offset += nBytes;
		}
	}

	/**
	 * Decodes the nValues binary unsigned long long values contained in the
	 * byte array, and described by this binary data format, into an array of
	 * unsigned long longs in the host's native binary data format. The
	 * host-format values are returned in the values array.
	 * <P>
	 * The given values array is presumed to be large enough to receive nValues
	 * values. The bytes array is presumed to be large enough to supply data for
	 * these values.
	 * <P>
	 * This binary data format's byte order and unsigned long long size is used
	 * to extract the appropriate number of bytes for the byte array. Those
	 * bytes may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format unsigned long long.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's unsigned long long is
	 * larger than that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this binary format's unsigned long long is
	 * smaller than that of the host, the upper bytes of the returned value are
	 * padded with zeroes or ones (sign-extension) depending upon if the value
	 * is positive or negative, respectively.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param values
	 *            the returned list of host-format values
	 * @param nValues
	 *            the number of values to return
	 */
	public final void unsignedLongLongValues(final byte[] bytes,
			final long[] values, final int nValues) {
		int nBytes = longLongSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			values[i] = unsignedLongLongValue(bytes, offset);
			offset += nBytes;
		}
	}

	/**
	 * Decodes the nValues binary float values contained in the byte array, and
	 * described by this binary data format, into an array of float in the
	 * host's native binary data format. The host-format values are returned in
	 * the values array.
	 * <P>
	 * The given values array is presumed to be large enough to receive nValues
	 * values. The bytes array is presumed to be large enough to supply data for
	 * these values.
	 * <P>
	 * This binary data format's byte order and float size is used to extract
	 * the appropriate number of bytes for the byte array. Those bytes may be
	 * swapped to match the host's byte order. The bytes are then assembled to
	 * form a host-format float.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's float is larger than that
	 * of the host, truncation may occur.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param values
	 *            the returned list of host-format values
	 * @param nValues
	 *            the number of values to return
	 */
	public final void floatValues(final byte[] bytes, final float[] values,
			final int nValues) {
		int nBytes = floatSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			values[i] = floatValue(bytes, offset);
			offset += nBytes;
		}
	}

	/**
	 * Decodes the nValues binary double values contained in the byte array, and
	 * described by this binary data format, into an array of double in the
	 * host's native binary data format. The host-format values are returned in
	 * the values array.
	 * <P>
	 * The given values array is presumed to be large enough to receive nValues
	 * values. The bytes array is presumed to be large enough to supply data for
	 * these values.
	 * <P>
	 * This binary data format's byte order and double size is used to extract
	 * the appropriate number of bytes for the byte array. Those bytes may be
	 * swapped to match the host's byte order. The bytes are then assembled to
	 * form a host-format double.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's double is larger than that
	 * of the host, truncation may occur.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param values
	 *            the returned list of host-format values
	 * @param nValues
	 *            the number of values to return
	 */
	public final void doubleValues(final byte[] bytes, final double[] values,
			final int nValues) {
		int nBytes = doubleSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			values[i] = doubleValue(bytes, offset);
			offset += nBytes;
		}
	}

	/**
	 * Decodes the nValues binary long double values contained in the byte
	 * array, and described by this binary data format, into an array of long
	 * double in the host's native binary data format. The host-format values
	 * are returned in the values array.
	 * <P>
	 * The given values array is presumed to be large enough to receive nValues
	 * values. The bytes array is presumed to be large enough to supply data for
	 * these values.
	 * <P>
	 * This binary data format's byte order and long double size is used to
	 * extract the appropriate number of bytes for the byte array. Those bytes
	 * may be swapped to match the host's byte order. The bytes are then
	 * assembled to form a host-format long double.
	 * <P>
	 * If byte swapping is needed, the order of bytes in the given byte array
	 * will be reversed, in-place.
	 * <P>
	 * If the number of bytes in this binary format's long double is larger than
	 * that of the host, truncation may occur.
	 * <P>
	 * Java doesn't support long doubles types directly. This method is
	 * identical to the double version.
	 * <P>
	 * 
	 * @param bytes
	 *            the byte array supplying the data
	 * @param values
	 *            the returned list of host-format values
	 * @param nValues
	 *            the number of values to return
	 */
	public final void longDoubleValues(final byte[] bytes,
			final double[] values, final int nValues) {
		int nBytes = longDoubleSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			values[i] = longDoubleValue(bytes, offset);
			offset += nBytes;
		}
	}

	// ----------------------------------------------------------------------
	// Conversion from Host Format Methods
	// ----------------------------------------------------------------------

	//
	// Without array offsets
	//
	/**
	 * Encodes the short value, in the host's native binary data format, into a
	 * byte array containing bytes that make up an short described by this
	 * binary data format. Differences in byte order and data type size between
	 * the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and short size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for a short value.
	 * <P>
	 * If the number of bytes in this format's short is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's short is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes or ones (sign-extensions) depending upon if the given value
	 * is positive or negative, respectively.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeShort(short value, final byte[] bytes) {
		if (sameShortSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyShort(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, shortSize);
			}
			return shortSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = shortSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[i] = (byte) value;
				value >>= 8; // Sign extends
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[i] = (byte) value;
			value >>= 8; // Sign extends
		}
		return nBytes;
	}

	/**
	 * Encodes the short value, in the host's native binary data format, into a
	 * byte array containing bytes that make up an short described by this
	 * binary data format. Differences in byte order and data type size between
	 * the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and short size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for a short value.
	 * <P>
	 * If the number of bytes in this format's short is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's short is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes or ones (sign-extensions) depending upon if the given value
	 * is positive or negative, respectively.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeShort(final int value, final byte[] bytes) {
		if (sameShortSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyShort(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, shortSize);
			}
			return shortSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = shortSize;
		short s = (short) value;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[i] = (byte) s;
				s >>= 8; // Sign extends
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[i] = (byte) s;
			s >>= 8; // Sign extends
		}
		return nBytes;
	}

	/**
	 * Encodes the unsigned short value, in the host's native binary data
	 * format, into a byte array containing bytes that make up an unsigned short
	 * described by this binary data format. Differences in byte order and data
	 * type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and unsigned short size are used to
	 * control how to fill the byte array with the given value. The byte array
	 * is assumed to be large enough for an unsigned short value.
	 * <P>
	 * If the number of bytes in this format's unsigned short is smaller than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned short is larger than
	 * that of the host, then the most significant bytes of the byte array will
	 * be padded with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedShort(short value, final byte[] bytes) {
		if (sameShortSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyUnsignedShort(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, shortSize);
			}
			return shortSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = shortSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[i] = (byte) value;
				value >>>= 8; // Zero pads
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[i] = (byte) value;
			value >>>= 8; // Zero pads
		}
		return nBytes;
	}

	/**
	 * Encodes the unsigned short value, in the host's native binary data
	 * format, into a byte array containing bytes that make up an unsigned short
	 * described by this binary data format. Differences in byte order and data
	 * type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and unsigned short size are used to
	 * control how to fill the byte array with the given value. The byte array
	 * is assumed to be large enough for an unsigned short value.
	 * <P>
	 * If the number of bytes in this format's unsigned short is smaller than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned short is larger than
	 * that of the host, then the most significant bytes of the byte array will
	 * be padded with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedShort(final int value, final byte[] bytes) {
		if (sameShortSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyUnsignedShort(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, shortSize);
			}
			return shortSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = shortSize;
		short us = (short) value;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[i] = (byte) us;
				us >>>= 8; // Zero pads
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[i] = (byte) us;
			us >>>= 8; // Zero pads
		}
		return nBytes;
	}

	/**
	 * Encodes the integer value, in the host's native binary data format, into
	 * a byte array containing bytes that make up an integer described by this
	 * binary data format. Differences in byte order and data type size between
	 * the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and integer size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for an integer value.
	 * <P>
	 * If the number of bytes in this format's integer is smaller than that of
	 * the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's integer is larger than that of
	 * the host, then the most significant bytes of the byte array will be
	 * padded with zeroes or ones (sign-extensions) depending upon if the given
	 * value is positive or negative, respectively.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeInt(int value, final byte[] bytes) {
		if (sameIntSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyInt(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, intSize);
			}
			return intSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = intSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[i] = (byte) value;
				value >>= 8; // Sign extends
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[i] = (byte) value;
			value >>= 8; // Sign extends
		}
		return nBytes;
	}

	/**
	 * Encodes the unsigned integer value, in the host's native binary data
	 * format, into a byte array containing bytes that make up an unsigned
	 * integer described by this binary data format. Differences in byte order
	 * and data type size between the host and this binary data format are
	 * handled.
	 * <P>
	 * This binary data format's byte order and unsigned integer size are used
	 * to control how to fill the byte array with the given value. The byte
	 * array is assumed to be large enough for an unsigned integer value.
	 * <P>
	 * If the number of bytes in this format's unsigned integer is smaller than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned integer is larger than
	 * that of the host, then the most significant bytes of the byte array will
	 * be padded with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedInt(int value, final byte[] bytes) {
		if (sameIntSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyUnsignedInt(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, intSize);
			}
			return intSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = intSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[i] = (byte) value;
				value >>>= 8; // Zero pads
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[i] = (byte) value;
			value >>>= 8; // Zero pads
		}
		return nBytes;
	}

	/**
	 * Encodes the unsigned integer value, in the host's native binary data
	 * format, into a byte array containing bytes that make up an unsigned
	 * integer described by this binary data format. Differences in byte order
	 * and data type size between the host and this binary data format are
	 * handled.
	 * <P>
	 * This binary data format's byte order and unsigned integer size are used
	 * to control how to fill the byte array with the given value. The byte
	 * array is assumed to be large enough for an unsigned integer value.
	 * <P>
	 * If the number of bytes in this format's unsigned integer is smaller than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned integer is larger than
	 * that of the host, then the most significant bytes of the byte array will
	 * be padded with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedInt(long value, final byte[] bytes) {
		if (sameIntSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyUnsignedInt((int) value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, intSize);
			}
			return intSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = intSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[i] = (byte) value;
				value >>>= 8; // Zero pads
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[i] = (byte) value;
			value >>>= 8; // Zero pads
		}
		return nBytes;
	}

	/**
	 * Encodes the long value, in the host's native binary data format, into a
	 * byte array containing bytes that make up an long described by this binary
	 * data format. Differences in byte order and data type size between the
	 * host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and long size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for a long value.
	 * <P>
	 * If the number of bytes in this format's long is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's long is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes or ones (sign-extensions) depending upon if the given value
	 * is positive or negative, respectively.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeLong(long value, final byte[] bytes) {
		if (sameLongSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyLong(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, longSize);
			}
			return longSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[i] = (byte) value;
				value >>= 8; // Sign extends
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[i] = (byte) value;
			value >>= 8; // Sign extends
		}
		return nBytes;
	}

	/**
	 * Encodes the unsigned long value, in the host's native binary data format,
	 * into a byte array containing bytes that make up an unsigned long
	 * described by this binary data format. Differences in byte order and data
	 * type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and unsigned long size are used to
	 * control how to fill the byte array with the given value. The byte array
	 * is assumed to be large enough for an unsigned long value.
	 * <P>
	 * If the number of bytes in this format's unsigned long is smaller than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned long is larger than that
	 * of the host, then the most significant bytes of the byte array will be
	 * padded with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedLong(long value, final byte[] bytes) {
		if (sameLongSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyUnsignedLong(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, longSize);
			}
			return longSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[i] = (byte) value;
				value >>>= 8; // Zero pads
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[i] = (byte) value;
			value >>>= 8; // Zero pads
		}
		return nBytes;
	}

	/**
	 * Encodes the long long value, in the host's native binary data format,
	 * into a byte array containing bytes that make up an long long described by
	 * this binary data format. Differences in byte order and data type size
	 * between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and long long size are used to
	 * control how to fill the byte array with the given value. The byte array
	 * is assumed to be large enough for a long long value.
	 * <P>
	 * If the number of bytes in this format's long long is smaller than that of
	 * the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's long long is larger than that of
	 * the host, then the most significant bytes of the byte array will be
	 * padded with zeroes or ones (sign-extensions) depending upon if the given
	 * value is positive or negative, respectively.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeLongLong(long value, final byte[] bytes) {
		if (sameLongLongSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyLongLong(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, longLongSize);
			}
			return longLongSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longLongSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[i] = (byte) value;
				value >>= 8; // Sign extends
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[i] = (byte) value;
			value >>= 8; // Sign extends
		}
		return nBytes;
	}

	/**
	 * Encodes the unsigned long long value, in the host's native binary data
	 * format, into a byte array containing bytes that make up an unsigned long
	 * long described by this binary data format. Differences in byte order and
	 * data type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and unsigned long long size are used
	 * to control how to fill the byte array with the given value. The byte
	 * array is assumed to be large enough for an unsigned long long value.
	 * <P>
	 * If the number of bytes in this format's unsigned long long is smaller
	 * than that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned long long is larger than
	 * that of the host, then the most significant bytes of the byte array will
	 * be padded with zeroes.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedLongLong(long value, final byte[] bytes) {
		if (sameLongLongSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyUnsignedLongLong(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, longLongSize);
			}
			return longLongSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longLongSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[i] = (byte) value;
				value >>>= 8; // Zero pads
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[i] = (byte) value;
			value >>>= 8; // Zero pads
		}
		return nBytes;
	}

	/**
	 * Encodes the float value, in the host's native binary data format, into a
	 * byte array containing bytes that make up an float described by this
	 * binary data format. Differences in byte order and data type size between
	 * the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and float size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for an float value.
	 * <P>
	 * If the number of bytes in this format's float is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's float is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeFloat(final float value, final byte[] bytes) {
		if (sameFloatSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyFloat(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, floatSize);
			}
			return floatSize;
		}

		// Different number of bytes - any similar host floats?
		int nBytes = floatSize;
		if (Host.getStorageSize(Host.DOUBLE) == nBytes) {
			// Cast to double, then copy and swap
			Host.copyDouble(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return nBytes;
		}
		if (Host.getStorageSize(Host.LONGDOUBLE) == nBytes) {
			// Cast to long double, then copy and swap
			Host.copyLongDouble(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return nBytes;
		}

		// Incompatible - must build float
		// IMPLEMENT!
		return nBytes;
	}

	/**
	 * Encodes the float value, in the host's native binary data format, into a
	 * byte array containing bytes that make up an float described by this
	 * binary data format. Differences in byte order and data type size between
	 * the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and float size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for an float value.
	 * <P>
	 * If the number of bytes in this format's float is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's float is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeFloat(final double value, final byte[] bytes) {
		if (sameFloatSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyFloat((float) value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, floatSize);
			}
			return floatSize;
		}

		// Different number of bytes - any similar host floats?
		int nBytes = floatSize;
		if (Host.getStorageSize(Host.DOUBLE) == nBytes) {
			// Cast to double, then copy and swap
			Host.copyDouble(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return nBytes;
		}
		if (Host.getStorageSize(Host.LONGDOUBLE) == nBytes) {
			// Cast to long double, then copy and swap
			Host.copyLongDouble(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return nBytes;
		}

		// Incompatible - must build float
		// IMPLEMENT!
		return nBytes;
	}

	/**
	 * Encodes the double value, in the host's native binary data format, into a
	 * byte array containing bytes that make up an double described by this
	 * binary data format. Differences in byte order and data type size between
	 * the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and double size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for an double value.
	 * <P>
	 * If the number of bytes in this format's double is smaller than that of
	 * the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's double is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeDouble(final double value, final byte[] bytes) {
		if (sameDoubleSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyDouble(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, doubleSize);
			}
			return doubleSize;
		}

		// Different number of bytes - any similar host floats?
		int nBytes = doubleSize;
		if (Host.getStorageSize(Host.FLOAT) == nBytes) {
			// Cast to float, then copy and swap
			Host.copyFloat((float) value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return nBytes;
		}
		if (Host.getStorageSize(Host.LONGDOUBLE) == nBytes) {
			// Cast to long double, then copy and swap
			Host.copyLongDouble(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return nBytes;
		}

		// Incompatible - must build float
		// IMPLEMENT!
		return nBytes;
	}

	/**
	 * Encodes the long double value, in the host's native binary data format,
	 * into a byte array containing bytes that make up an long double described
	 * by this binary data format. Differences in byte order and data type size
	 * between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and long double size are used to
	 * control how to fill the byte array with the given value. The byte array
	 * is assumed to be large enough for an long double value.
	 * <P>
	 * If the number of bytes in this format's long double is smaller than that
	 * of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's long double is larger than that
	 * of the host, then the most significant bytes of the byte array will be
	 * padded with zeroes.
	 * <P>
	 * Java doesn't support long double types directly. While long double values
	 * may be manipulated using this class, if decoded, the largest returnable
	 * portion of the long double is a Java double. Similarly, the largest
	 * encodable value into a long double is one from a Java double.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeLongDouble(final double value, final byte[] bytes) {
		if (sameLongDoubleSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyLongDouble(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, longDoubleSize);
			}
			return longDoubleSize;
		}

		// Different number of bytes - any similar host floats?
		int nBytes = longDoubleSize;
		if (Host.getStorageSize(Host.FLOAT) == nBytes) {
			// Cast to float, then copy and swap
			Host.copyFloat((float) value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return nBytes;
		}
		if (Host.getStorageSize(Host.DOUBLE) == nBytes) {
			// Cast to double, then copy and swap
			Host.copyDouble(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return nBytes;
		}

		// Incompatible - must build float
		// IMPLEMENT!
		return nBytes;
	}

	//
	// With array offsets
	//
	/**
	 * Encodes the short value, in the host's native binary data format, into a
	 * byte array containing bytes that make up an short described by this
	 * binary data format. Differences in byte order and data type size between
	 * the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and short size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for a short value.
	 * <P>
	 * If the number of bytes in this format's short is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's short is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes or ones (sign-extensions) depending upon if the given value
	 * is positive or negative, respectively.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeShort(short value, final byte[] bytes,
			final int offset) {
		if (sameShortSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyShort(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, shortSize);
			}
			return shortSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = shortSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[offset + i] = (byte) value;
				value >>= 8; // Sign extends
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[offset + i] = (byte) value;
			value >>= 8; // Sign extends
		}
		return nBytes;
	}

	/**
	 * Encodes the short value, in the host's native binary data format, into a
	 * byte array containing bytes that make up an short described by this
	 * binary data format. Differences in byte order and data type size between
	 * the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and short size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for a short value.
	 * <P>
	 * If the number of bytes in this format's short is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's short is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes or ones (sign-extensions) depending upon if the given value
	 * is positive or negative, respectively.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeShort(final int value, final byte[] bytes,
			final int offset) {
		if (sameShortSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyShort((short) value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, shortSize);
			}
			return shortSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = shortSize;
		short s = (short) value;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[offset + i] = (byte) s;
				s >>= 8; // Sign extends
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[offset + i] = (byte) s;
			s >>= 8; // Sign extends
		}
		return nBytes;
	}

	/**
	 * Encodes the unsigned short value, in the host's native binary data
	 * format, into a byte array containing bytes that make up an unsigned short
	 * described by this binary data format. Differences in byte order and data
	 * type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and unsigned short size are used to
	 * control how to fill the byte array with the given value. The byte array
	 * is assumed to be large enough for an unsigned short value.
	 * <P>
	 * If the number of bytes in this format's unsigned short is smaller than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned short is larger than
	 * that of the host, then the most significant bytes of the byte array will
	 * be padded with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedShort(short value, final byte[] bytes,
			final int offset) {
		if (sameShortSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyUnsignedShort(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, shortSize);
			}
			return shortSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = shortSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[offset + i] = (byte) value;
				value >>>= 8; // Zero pads
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[offset + i] = (byte) value;
			value >>>= 8; // Zero pads
		}
		return nBytes;
	}

	/**
	 * Encodes the unsigned short value, in the host's native binary data
	 * format, into a byte array containing bytes that make up an unsigned short
	 * described by this binary data format. Differences in byte order and data
	 * type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and unsigned short size are used to
	 * control how to fill the byte array with the given value. The byte array
	 * is assumed to be large enough for an unsigned short value.
	 * <P>
	 * If the number of bytes in this format's unsigned short is smaller than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned short is larger than
	 * that of the host, then the most significant bytes of the byte array will
	 * be padded with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedShort(final int value, final byte[] bytes,
			final int offset) {
		if (sameShortSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyUnsignedShort((short) value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, shortSize);
			}
			return shortSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = shortSize;
		short us = (short) value;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[offset + i] = (byte) us;
				us >>>= 8; // Zero pads
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[offset + i] = (byte) us;
			us >>>= 8; // Zero pads
		}
		return nBytes;
	}

	/**
	 * Encodes the integer value, in the host's native binary data format, into
	 * a byte array containing bytes that make up an integer described by this
	 * binary data format. Differences in byte order and data type size between
	 * the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and integer size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for an integer value.
	 * <P>
	 * If the number of bytes in this format's integer is smaller than that of
	 * the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's integer is larger than that of
	 * the host, then the most significant bytes of the byte array will be
	 * padded with zeroes or ones (sign-extensions) depending upon if the given
	 * value is positive or negative, respectively.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeInt(int value, final byte[] bytes, final int offset) {
		if (sameIntSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyInt(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, intSize);
			}
			return intSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = intSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[offset + i] = (byte) value;
				value >>= 8; // Sign extends
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[offset + i] = (byte) value;
			value >>= 8; // Sign extends
		}
		return nBytes;
	}

	/**
	 * Encodes the unsigned integer value, in the host's native binary data
	 * format, into a byte array containing bytes that make up an unsigned
	 * integer described by this binary data format. Differences in byte order
	 * and data type size between the host and this binary data format are
	 * handled.
	 * <P>
	 * This binary data format's byte order and unsigned integer size are used
	 * to control how to fill the byte array with the given value. The byte
	 * array is assumed to be large enough for an unsigned integer value.
	 * <P>
	 * If the number of bytes in this format's unsigned integer is smaller than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned integer is larger than
	 * that of the host, then the most significant bytes of the byte array will
	 * be padded with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedInt(int value, final byte[] bytes,
			final int offset) {
		if (sameIntSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyUnsignedInt(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, intSize);
			}
			return intSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = intSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[offset + i] = (byte) value;
				value >>>= 8; // Zero pads
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[offset + i] = (byte) value;
			value >>>= 8; // Zero pads
		}
		return nBytes;
	}

	/**
	 * Encodes the unsigned integer value, in the host's native binary data
	 * format, into a byte array containing bytes that make up an unsigned
	 * integer described by this binary data format. Differences in byte order
	 * and data type size between the host and this binary data format are
	 * handled.
	 * <P>
	 * This binary data format's byte order and unsigned integer size are used
	 * to control how to fill the byte array with the given value. The byte
	 * array is assumed to be large enough for an unsigned integer value.
	 * <P>
	 * If the number of bytes in this format's unsigned integer is smaller than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned integer is larger than
	 * that of the host, then the most significant bytes of the byte array will
	 * be padded with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedInt(final long value, final byte[] bytes,
			final int offset) {
		if (sameIntSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyUnsignedInt(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, intSize);
			}
			return intSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = intSize;
		int v = (int) value;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[offset + i] = (byte) v;
				v >>>= 8; // Zero pads
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[offset + i] = (byte) v;
			v >>>= 8; // Zero pads
		}
		return nBytes;
	}

	/**
	 * Encodes the long value, in the host's native binary data format, into a
	 * byte array containing bytes that make up an long described by this binary
	 * data format. Differences in byte order and data type size between the
	 * host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and long size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for a long value.
	 * <P>
	 * If the number of bytes in this format's long is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's long is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes or ones (sign-extensions) depending upon if the given value
	 * is positive or negative, respectively.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeLong(long value, final byte[] bytes, final int offset) {
		if (sameLongSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyLong(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, longSize);
			}
			return longSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[offset + i] = (byte) value;
				value >>= 8; // Sign extends
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[offset + i] = (byte) value;
			value >>= 8; // Sign extends
		}
		return nBytes;
	}

	/**
	 * Encodes the unsigned long value, in the host's native binary data format,
	 * into a byte array containing bytes that make up an unsigned long
	 * described by this binary data format. Differences in byte order and data
	 * type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and unsigned long size are used to
	 * control how to fill the byte array with the given value. The byte array
	 * is assumed to be large enough for an unsigned long value.
	 * <P>
	 * If the number of bytes in this format's unsigned long is smaller than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned long is larger than that
	 * of the host, then the most significant bytes of the byte array will be
	 * padded with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedLong(long value, final byte[] bytes,
			final int offset) {
		if (sameLongSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyUnsignedLong(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, longSize);
			}
			return longSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[offset + i] = (byte) value;
				value >>>= 8; // Zero pads
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[offset + i] = (byte) value;
			value >>>= 8; // Zero pads
		}
		return nBytes;
	}

	/**
	 * Encodes the long long value, in the host's native binary data format,
	 * into a byte array containing bytes that make up an long long described by
	 * this binary data format. Differences in byte order and data type size
	 * between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and long long size are used to
	 * control how to fill the byte array with the given value. The byte array
	 * is assumed to be large enough for a long long value.
	 * <P>
	 * If the number of bytes in this format's long long is smaller than that of
	 * the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's long long is larger than that of
	 * the host, then the most significant bytes of the byte array will be
	 * padded with zeroes or ones (sign-extensions) depending upon if the given
	 * value is positive or negative, respectively.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeLongLong(long value, final byte[] bytes,
			final int offset) {
		if (sameLongLongSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyLongLong(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, longLongSize);
			}
			return longLongSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longLongSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[offset + i] = (byte) value;
				value >>= 8; // Sign extends
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[offset + i] = (byte) value;
			value >>= 8; // Sign extends
		}
		return nBytes;
	}

	/**
	 * Encodes the unsigned long long value, in the host's native binary data
	 * format, into a byte array containing bytes that make up an unsigned long
	 * long described by this binary data format. Differences in byte order and
	 * data type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and unsigned long long size are used
	 * to control how to fill the byte array with the given value. The byte
	 * array is assumed to be large enough for an unsigned long long value.
	 * <P>
	 * If the number of bytes in this format's unsigned long long is smaller
	 * than that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned long long is larger than
	 * that of the host, then the most significant bytes of the byte array will
	 * be padded with zeroes.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedLongLong(long value, final byte[] bytes,
			final int offset) {
		if (sameLongLongSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyUnsignedLongLong(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, longLongSize);
			}
			return longLongSize;
		}

		// Different number of bytes - assemble with shifts
		int nBytes = longLongSize;
		if (isMBF) {
			for (int i = nBytes - 1; i >= 0; i--) {
				bytes[offset + i] = (byte) value;
				value >>>= 8; // Zero pads
			}
			return nBytes;
		}
		for (int i = 0; i < nBytes; i++) {
			bytes[offset + i] = (byte) value;
			value >>>= 8; // Zero pads
		}
		return nBytes;
	}

	/**
	 * Encodes the float value, in the host's native binary data format, into a
	 * byte array containing bytes that make up an float described by this
	 * binary data format. Differences in byte order and data type size between
	 * the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and float size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for an float value.
	 * <P>
	 * If the number of bytes in this format's float is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's float is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeFloat(final float value, final byte[] bytes,
			final int offset) {
		if (sameFloatSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyFloat(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, floatSize);
			}
			return floatSize;
		}

		// Different number of bytes - any similar host floats?
		int nBytes = floatSize;
		if (Host.getStorageSize(Host.DOUBLE) == nBytes) {
			// Cast to double, then copy and swap
			Host.copyDouble(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return nBytes;
		}
		if (Host.getStorageSize(Host.LONGDOUBLE) == nBytes) {
			// Cast to long double, then copy and swap
			Host.copyLongDouble(value, bytes);
			if (!sameByteOrder) {
				Host.swap(bytes, nBytes);
			}
			return nBytes;
		}

		// Incompatible - must build float
		// IMPLEMENT!
		return nBytes;
	}

	/**
	 * Encodes the float value, in the host's native binary data format, into a
	 * byte array containing bytes that make up an float described by this
	 * binary data format. Differences in byte order and data type size between
	 * the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and float size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for an float value.
	 * <P>
	 * If the number of bytes in this format's float is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's float is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeFloat(final double value, final byte[] bytes,
			final int offset) {
		if (sameFloatSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyFloat((float) value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, floatSize);
			}
			return floatSize;
		}

		// Different number of bytes - any similar host floats?
		int nBytes = floatSize;
		if (Host.getStorageSize(Host.DOUBLE) == nBytes) {
			// Cast to double, then copy and swap
			Host.copyDouble(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, nBytes);
			}
			return nBytes;
		}
		if (Host.getStorageSize(Host.LONGDOUBLE) == nBytes) {
			// Cast to long double, then copy and swap
			Host.copyLongDouble(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, nBytes);
			}
			return nBytes;
		}

		// Incompatible - must build float
		// IMPLEMENT!
		return nBytes;
	}

	/**
	 * Encodes the double value, in the host's native binary data format, into a
	 * byte array containing bytes that make up an double described by this
	 * binary data format. Differences in byte order and data type size between
	 * the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and double size are used to control
	 * how to fill the byte array with the given value. The byte array is
	 * assumed to be large enough for an double value.
	 * <P>
	 * If the number of bytes in this format's double is smaller than that of
	 * the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's double is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeDouble(final double value, final byte[] bytes,
			final int offset) {
		if (sameDoubleSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyDouble(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, doubleSize);
			}
			return doubleSize;
		}

		// Different number of bytes - any similar host floats?
		int nBytes = doubleSize;
		if (Host.getStorageSize(Host.FLOAT) == nBytes) {
			// Cast to float, then copy and swap
			Host.copyFloat((float) value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, nBytes);
			}
			return nBytes;
		}
		if (Host.getStorageSize(Host.LONGDOUBLE) == nBytes) {
			// Cast to long double, then copy and swap
			Host.copyLongDouble(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, nBytes);
			}
			return nBytes;
		}

		// Incompatible - must build float
		// IMPLEMENT!
		return nBytes;
	}

	/**
	 * Encodes the long double value, in the host's native binary data format,
	 * into a byte array containing bytes that make up an long double described
	 * by this binary data format. Differences in byte order and data type size
	 * between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and long double size are used to
	 * control how to fill the byte array with the given value. The byte array
	 * is assumed to be large enough for an long double value.
	 * <P>
	 * If the number of bytes in this format's long double is smaller than that
	 * of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's long double is larger than that
	 * of the host, then the most significant bytes of the byte array will be
	 * padded with zeroes.
	 * <P>
	 * Java doesn't support long double types directly. While long double values
	 * may be manipulated using this class, if decoded, the largest returnable
	 * portion of the long double is a Java double. Similarly, the largest
	 * encodable value into a long double is one from a Java double.
	 * <P>
	 * 
	 * @param value
	 *            the value to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @param offset
	 *            the starting offset into the byte array
	 * @return the number of bytes set during encoding
	 */
	public final int encodeLongDouble(final double value, final byte[] bytes,
			final int offset) {
		if (sameLongDoubleSize) {
			// Same number of bytes as this host - cast and swap
			Host.copyLongDouble(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, longDoubleSize);
			}
			return longDoubleSize;
		}

		// Different number of bytes - any similar host floats?
		int nBytes = longDoubleSize;
		if (Host.getStorageSize(Host.FLOAT) == nBytes) {
			// Cast to float, then copy and swap
			Host.copyFloat((float) value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, nBytes);
			}
			return nBytes;
		}
		if (Host.getStorageSize(Host.DOUBLE) == nBytes) {
			// Cast to double, then copy and swap
			Host.copyDouble(value, bytes, offset);
			if (!sameByteOrder) {
				Host.swap(bytes, offset, nBytes);
			}
			return nBytes;
		}

		// Incompatible - must build float
		// IMPLEMENT!
		return nBytes;
	}

	// Array methods
	/**
	 * Encodes an array of short values, in the host's native binary data
	 * format, into a byte array containing bytes that make up each short
	 * described by this binary data format. Differences in byte order and data
	 * type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and short size are used to control
	 * how to fill the byte array with the given values. The byte array is
	 * assumed to be large enough for the given number of short values.
	 * <P>
	 * If the number of bytes in this format's short is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's short is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes or ones (sign-extensions) depending upon if the given value
	 * is positive or negative, respectively.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to convert
	 * @param nValues
	 *            the number of values to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeShorts(final short[] values, final int nValues,
			final byte[] bytes) {
		int nBytes = shortSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			encodeShort(values[i], bytes, offset);
			offset += nBytes;
		}
		return offset;
	}

	/**
	 * Encodes an array of unsigned short values, in the host's native binary
	 * data format, into a byte array containing bytes that make up each
	 * unsigned short described by this binary data format. Differences in byte
	 * order and data type size between the host and this binary data format are
	 * handled.
	 * <P>
	 * This binary data format's byte order and unsigned short size are used to
	 * control how to fill the byte array with the given values. The byte array
	 * is assumed to be large enough for the given number of unsigned short
	 * values.
	 * <P>
	 * If the number of bytes in this format's unsigned short is smaller than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned short is larger than
	 * that of the host, then the most significant bytes of the byte array will
	 * be padded with zeroes or ones (sign-extensions) depending upon if the
	 * given value is positive or negative, respectively.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to convert
	 * @param nValues
	 *            the number of values to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedShorts(final short[] values,
			final int nValues, final byte[] bytes) {
		int nBytes = shortSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			encodeUnsignedShort(values[i], bytes, offset);
			offset += nBytes;
		}
		return offset;
	}

	/**
	 * Encodes an array of int values, in the host's native binary data format,
	 * into a byte array containing bytes that make up each int described by
	 * this binary data format. Differences in byte order and data type size
	 * between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and int size are used to control how
	 * to fill the byte array with the given values. The byte array is assumed
	 * to be large enough for the given number of int values.
	 * <P>
	 * If the number of bytes in this format's int is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's int is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes or ones (sign-extensions) depending upon if the given value
	 * is positive or negative, respectively.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to convert
	 * @param nValues
	 *            the number of values to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeInts(final int[] values, final int nValues,
			final byte[] bytes) {
		int nBytes = intSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			encodeInt(values[i], bytes, offset);
			offset += nBytes;
		}
		return offset;
	}

	/**
	 * Encodes an array of unsigned int values, in the host's native binary data
	 * format, into a byte array containing bytes that make up each unsigned int
	 * described by this binary data format. Differences in byte order and data
	 * type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and unsigned int size are used to
	 * control how to fill the byte array with the given values. The byte array
	 * is assumed to be large enough for the given number of unsigned int
	 * values.
	 * <P>
	 * If the number of bytes in this format's unsigned int is smaller than that
	 * of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned int is larger than that
	 * of the host, then the most significant bytes of the byte array will be
	 * padded with zeroes or ones (sign-extensions) depending upon if the given
	 * value is positive or negative, respectively.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to convert
	 * @param nValues
	 *            the number of values to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedInts(final int[] values, final int nValues,
			final byte[] bytes) {
		int nBytes = intSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			encodeUnsignedInt(values[i], bytes, offset);
			offset += nBytes;
		}
		return offset;
	}

	/**
	 * Encodes an array of long values, in the host's native binary data format,
	 * into a byte array containing bytes that make up each long described by
	 * this binary data format. Differences in byte order and data type size
	 * between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and long size are used to control
	 * how to fill the byte array with the given values. The byte array is
	 * assumed to be large enough for the given number of long values.
	 * <P>
	 * If the number of bytes in this format's long is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's long is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes or ones (sign-extensions) depending upon if the given value
	 * is positive or negative, respectively.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to convert
	 * @param nValues
	 *            the number of values to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeLongs(final long[] values, final int nValues,
			final byte[] bytes) {
		int nBytes = longSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			encodeLong(values[i], bytes, offset);
			offset += nBytes;
		}
		return offset;
	}

	/**
	 * Encodes an array of unsigned long values, in the host's native binary
	 * data format, into a byte array containing bytes that make up each
	 * unsigned long described by this binary data format. Differences in byte
	 * order and data type size between the host and this binary data format are
	 * handled.
	 * <P>
	 * This binary data format's byte order and unsigned long size are used to
	 * control how to fill the byte array with the given values. The byte array
	 * is assumed to be large enough for the given number of unsigned long
	 * values.
	 * <P>
	 * If the number of bytes in this format's unsigned long is smaller than
	 * that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned long is larger than that
	 * of the host, then the most significant bytes of the byte array will be
	 * padded with zeroes or ones (sign-extensions) depending upon if the given
	 * value is positive or negative, respectively.
	 * <P>
	 * Java doesn't support unsigned types directly. This method is identical to
	 * the signed version.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to convert
	 * @param nValues
	 *            the number of values to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedLongs(final long[] values,
			final int nValues, final byte[] bytes) {
		int nBytes = longSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			encodeUnsignedLong(values[i], bytes, offset);
			offset += nBytes;
		}
		return offset;
	}

	/**
	 * Encodes an array of long long values, in the host's native binary data
	 * format, into a byte array containing bytes that make up each long long
	 * described by this binary data format. Differences in byte order and data
	 * type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and long long size are used to
	 * control how to fill the byte array with the given values. The byte array
	 * is assumed to be large enough for the given number of long long values.
	 * <P>
	 * If the number of bytes in this format's long long is smaller than that of
	 * the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's long long is larger than that of
	 * the host, then the most significant bytes of the byte array will be
	 * padded with zeroes or ones (sign-extensions) depending upon if the given
	 * value is positive or negative, respectively.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to convert
	 * @param nValues
	 *            the number of values to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeLongLongs(final long[] values, final int nValues,
			final byte[] bytes) {
		int nBytes = longLongSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			encodeLongLong(values[i], bytes, offset);
			offset += nBytes;
		}
		return offset;
	}

	/**
	 * Encodes an array of unsigned long long values, in the host's native
	 * binary data format, into a byte array containing bytes that make up each
	 * unsigned long long described by this binary data format. Differences in
	 * byte order and data type size between the host and this binary data
	 * format are handled.
	 * <P>
	 * This binary data format's byte order and unsigned long long size are used
	 * to control how to fill the byte array with the given values. The byte
	 * array is assumed to be large enough for the given number of unsigned long
	 * long values.
	 * <P>
	 * If the number of bytes in this format's unsigned long long is smaller
	 * than that of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's unsigned long long is larger than
	 * that of the host, then the most significant bytes of the byte array will
	 * be padded with zeroes or ones (sign-extensions) depending upon if the
	 * given value is positive or negative, respectively.
	 * <P>
	 * Java doesn't support long long types directly. While long long values may
	 * be manipulated using this class, if decoded, the largest returnable
	 * portion of the long long is a Java long. Similarly, the largest encodable
	 * value into a long long is one from a Java long.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to convert
	 * @param nValues
	 *            the number of values to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeUnsignedLongLongs(final long[] values,
			final int nValues, final byte[] bytes) {
		int nBytes = longLongSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			encodeUnsignedLongLong(values[i], bytes, offset);
			offset += nBytes;
		}
		return offset;
	}

	/**
	 * Encodes an array of float values, in the host's native binary data
	 * format, into a byte array containing bytes that make up each float
	 * described by this binary data format. Differences in byte order and data
	 * type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and float size are used to control
	 * how to fill the byte array with the given values. The byte array is
	 * assumed to be large enough for the given number of float values.
	 * <P>
	 * If the number of bytes in this format's float is smaller than that of the
	 * host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's float is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes or ones (sign-extensions) depending upon if the given value
	 * is positive or negative, respectively.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to convert
	 * @param nValues
	 *            the number of values to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeFloats(final float[] values, final int nValues,
			final byte[] bytes) {
		int nBytes = floatSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			encodeFloat(values[i], bytes, offset);
			offset += nBytes;
		}
		return offset;
	}

	/**
	 * Encodes an array of double values, in the host's native binary data
	 * format, into a byte array containing bytes that make up each double
	 * described by this binary data format. Differences in byte order and data
	 * type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and double size are used to control
	 * how to fill the byte array with the given values. The byte array is
	 * assumed to be large enough for the given number of double values.
	 * <P>
	 * If the number of bytes in this format's double is smaller than that of
	 * the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's double is larger than that of the
	 * host, then the most significant bytes of the byte array will be padded
	 * with zeroes or ones (sign-extensions) depending upon if the given value
	 * is positive or negative, respectively.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to convert
	 * @param nValues
	 *            the number of values to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeDoubles(final double[] values, final int nValues,
			final byte[] bytes) {
		int nBytes = doubleSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			encodeDouble(values[i], bytes, offset);
			offset += nBytes;
		}
		return offset;
	}

	/**
	 * Encodes an array of long double values, in the host's native binary data
	 * format, into a byte array containing bytes that make up each long double
	 * described by this binary data format. Differences in byte order and data
	 * type size between the host and this binary data format are handled.
	 * <P>
	 * This binary data format's byte order and long double size are used to
	 * control how to fill the byte array with the given values. The byte array
	 * is assumed to be large enough for the given number of long double values.
	 * <P>
	 * If the number of bytes in this format's long double is smaller than that
	 * of the host, truncation may occur.
	 * <P>
	 * If the number of bytes in this format's long double is larger than that
	 * of the host, then the most significant bytes of the byte array will be
	 * padded with zeroes or ones (sign-extensions) depending upon if the given
	 * value is positive or negative, respectively.
	 * <P>
	 * Java doesn't support long double types directly. While long double values
	 * may be manipulated using this class, if decoded, the largest returnable
	 * portion of the long double is a Java double. Similarly, the largest
	 * encodable value into a long double is one from a Java double.
	 * <P>
	 * 
	 * @param values
	 *            the array of values to convert
	 * @param nValues
	 *            the number of values to convert
	 * @param bytes
	 *            the byte array supplying the data
	 * @return the number of bytes set during encoding
	 */
	public final int encodeLongDoubles(final double[] values,
			final int nValues, final byte[] bytes) {
		int nBytes = longDoubleSize;
		int offset = 0;
		for (int i = nValues - 1; i >= 0; i--) {
			encodeDouble(values[i], bytes, offset);
			offset += nBytes;
		}
		return offset;
	}
};
