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
//  Host.h  -  edu.sdsc.grid.io.Host
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-->Host
//
//  PRINCIPAL AUTHOR
//  David R. Nadeau, SDSC/UCSD
//
package org.irods.jargon.core.utils;

/**
 * <DIV ALIGN=JUSTIFY> The Host class describes important attributes of the
 * current host processor, compiler, and operating system. Attributes include:
 * <P>
 * <UL>
 * <LI>Byte order
 * <LI>Data type storage sizes
 * <LI>Data type significant bits
 * <LI>Data type storage alignment (imposed by compiler)
 * </UL>
 * <P>
 * All methods and fields of this class are static. Object instances of this
 * class cannot be created.
 * <P>
 * <B>Attributes</B><BR>
 * <BLOCKQUOTE> <B>Byte order</B><BR>
 * For all data types requiring more than one byte (i.e., sizeof(type)>1), the
 * processor stores the value as a sequence of bytes. That sequence may place
 * the highest-order bytes first or last:
 * <P>
 * <UL>
 * <LI>Big endian = Most-significant Byte First = MBF
 * <LI>Little endian = Least-significant Byte First = LBF
 * </UL>
 * <P>
 * Intel processors are LBF, while most other processors are MBF. Java is always
 * MBF, regardless of processor.
 * <P>
 * <B>Data type storage sizes</B><BR>
 * Java defines fixed values, independent of processor, for the storage size of
 * each primitive type.
 * <P>
 * <B>Data type significant bits</B><BR>
 * The significant bits of a data type are the bits that may be set when using a
 * value of that type. In Java this is the same number of bits as used to store
 * that type.
 * <P>
 * <B>Data type storage alignment</B><BR>
 * Storage alignment forces data types to start on half-word, word, or
 * double-word address boundaries (depending on the type). This data cannot be
 * determined, or used, in Java applications. </BLOCKQUOTE>
 * <P>
 * <B>methods</B><BR>
 * <BLOCKQUOTE> <B>Cast methods</B><BR>
 * Methods named "castToType" where "Type" is "Short," "Int", etc., provide a
 * smart method of casting bytes from a raw byte array into a data type's value.
 * For instance, if an array "bytes" contains a 4-byte float, then use:
 * <BLOCKQUOTE> <TT>float value = Host.castToFloat( bytes );</TT> </BLOCKQUOTE>
 * <P>
 * <B>Copy methods</B><BR>
 * Methods named "copyType" where "Type" is "Short," "Int," etc., provide a
 * smart method of copying a data typed value into a raw byte array. For
 * instance, if a 4-byte float value needs to be copied into the next 4 bytes in
 * a "bytes" array, then use: <BLOCKQUOTE>
 * <TT>Host.copyFloat( value, bytes );</TT> </BLOCKQUOTE> </BLOCKQUOTE>
 * <DL>
 * <DT>Sub-classing</DT>
 * <DD>Final</DD>
 * <DT>Thread safe</DT>
 * <DD>Yes</DD>
 * </DL>
 * </DIV>
 * <P>
 *
 * @author David R. Nadeau, San Diego Supercomputer Center
 */
public final class Host extends Object {

	/**
	 * A language primitive signed or unsigned char.
	 * <P>
	 */
	public static final int CHAR = 0;

	/**
	 * A language primitive signed or unsigned short integer.
	 * <P>
	 */
	public static final int SHORT = 1;

	/**
	 * A language primitive signed or unsigned integer.
	 * <P>
	 */
	public static final int INT = 2;

	/**
	 * A language primitive signed or unsigned long integer.
	 * <P>
	 */
	public static final int LONG = 3;

	/**
	 * A language primitive signed or unsigned long long integer.
	 * <P>
	 */
	public static final int LONGLONG = 4;

	/**
	 * A language primitive float.
	 * <P>
	 */
	public static final int FLOAT = 5;

	/**
	 * A language primitive double.
	 * <P>
	 */
	public static final int DOUBLE = 6;

	/**
	 * A language primitive long double.
	 * <P>
	 */
	public static final int LONGDOUBLE = 7;

	/**
	 * The number of language primitive types.
	 * <P>
	 */
	public static final int NUMBER_TYPES = 8;

	/**
	 * The guaranteed maximum number of bytes ever needed to hold the largest
	 * data type. This value may used to pre-allocate byte arrays used for read,
	 * writing, and byte-flipping values of any primitive data type.
	 * <P>
	 * The maximum value is intentionally set large in order to accomodate
	 * current and future architectures.
	 * <P>
	 */
	public static final int MAX_TYPE_SIZE = 64;

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------
	/**
	 * Holds true if the host byte order is MBF, and false if LBF.
	 * <P>
	 * Note: Java is always MBF, regardless of host.
	 * <P>
	 */
	private static boolean isMBF = true;

	/**
	 * Holds a name for each data type.
	 * <P>
	 */
	private static final String typeName[] = { "char", "short", "int", "long",
		"long long", "float", "double", "long double" };

	/**
	 * Holds the size, in bytes, of each of the language primitive types.
	 * <P>
	 * Note: Java type sizes are fixed, regardless of host.
	 * <P>
	 */
	private static final byte typeSize[] = { 1, // char
		2, // short
		4, // int
		8, // long
		8, // long long (doesn't exist in Java)
		4, // float
		8, // double
		8, // long double (doesn't exist in Java)
	};

	/**
	 * Holds the number of significant bits for each of the language primitive
	 * types.
	 * <P>
	 * Note: Java type significant bits are fixed, regardless of host.
	 * <P>
	 */
	private static final short typeBits[] = { 8, // char
		16, // short
		32, // int
		64, // long
		64, // long long (doesn't exist in Java)
		32, // float
		64, // double
		64, // long double (doesn't exist in Java)
	};

	/**
	 * Holds the alignment boundary, in bytes, imposed by the compiler when
	 * storing the language primitive types.
	 * <P>
	 * Note: This data in the C++ version of this code is used to enable pointer
	 * casting for access to the raw bytes of stored multi-byte values. In Java,
	 * there are no pointers, and so this feature is not available.
	 * Additionally, without pointers it is not possible to clearly determine
	 * alignment characteristics. We therefore default to the type size for all
	 * values.
	 * <P>
	 */
	private static final byte typeCompilerAlignment[] = { 1, // char
		2, // short
		4, // int
		8, // long
		8, // long long (doesn't exist in Java)
		4, // float
		8, // double
		8, // long double (doesn't exist in Java)
	};

	// ----------------------------------------------------------------------
	// Attributes Methods
	// ----------------------------------------------------------------------
	/**
	 * Returns true if the host's byte order is "big endian" or
	 * Most-significant-Byte-First (MBF); otherwise false.
	 * <P>
	 *
	 * @return true if MBF; false if LBF
	 */
	public final static boolean isMBFByteOrder() {
		return isMBF;
	}

	/**
	 * Returns true if the host's byte order is "little endian" or
	 * Least-significant-Byte-First (LBF); otherwise false.
	 * <P>
	 *
	 * @return true if LBF; false if MBF
	 */
	public final static boolean isLBFByteOrder() {
		return !isMBF;
	}

	/**
	 * Gets the name of the type.
	 * <P>
	 *
	 * @param type
	 *            the type code for the data type
	 * @return the type name
	 */
	public final static String getName(final int type) {
		return typeName[type];
	}

	/**
	 * Gets the number of bytes occupied by the type.
	 * <P>
	 *
	 * @param type
	 *            the type code for the data type
	 * @return the number of bytes
	 */
	public final static byte getStorageSize(final int type) {
		return typeSize[type];
	}

	/**
	 * Gets the number of significant bits of the type.
	 * <P>
	 *
	 * @param type
	 *            the type code for the data type
	 * @return the number of bytes
	 */
	public final static int getSignificantBits(final int type) {
		return typeBits[type];
	}

	/**
	 * Gets the alignment boundary, in bytes, imposed by the compiler for the
	 * type.
	 * <P>
	 *
	 * @param type
	 *            the type code for the data type
	 * @return the alignment in bytes
	 */
	public final static int getCompilerAlignment(final int type) {
		return typeCompilerAlignment[type];
	}

	// ----------------------------------------------------------------------
	// Swapping Methods
	// ----------------------------------------------------------------------
	/**
	 * Reverses the byte order of the first nBytes of the byte array. This
	 * method can be used to convert a byte array from Most- to
	 * Least-significant-Byte-First, or vice versa.
	 * <P>
	 * The swap is done in-place, reversing the order of bytes in the given
	 * array.
	 * <P>
	 *
	 * @param bytes
	 *            an array of bytes to swap the order of
	 * @param nBytes
	 *            the number of bytes to swap
	 */
	public final static void swap(final byte[] bytes, final int nBytes) {
		byte tmp;

		// Use an accelerated path for common swap sizes
		switch (nBytes) {
		case 1:
			return;
		case 2:
			tmp = bytes[0];
			bytes[0] = bytes[1];
			bytes[1] = tmp;
			return;

		case 4:
			tmp = bytes[0];
			bytes[0] = bytes[3];
			bytes[3] = tmp;

			tmp = bytes[1];
			bytes[1] = bytes[2];
			bytes[2] = tmp;
			return;

		case 8:
			tmp = bytes[0];
			bytes[0] = bytes[7];
			bytes[7] = tmp;

			tmp = bytes[1];
			bytes[1] = bytes[6];
			bytes[6] = tmp;

			tmp = bytes[2];
			bytes[2] = bytes[5];
			bytes[5] = tmp;

			tmp = bytes[3];
			bytes[3] = bytes[4];
			bytes[4] = tmp;
			return;
		}

		// Otherwise handle it generically
		int nb2 = nBytes >>> 1; // Divide by two & truncate
		for (int i = 0, j = nBytes - 1; i < nb2; i++, j--) {
			tmp = bytes[i];
			bytes[i] = bytes[j];
			bytes[j] = tmp;
		}
	}

	/**
	 * Reverses the byte order of nBytes of the byte array, starting at the
	 * given offset. This method can be used to convert a byte array from Most-
	 * to Least-significant-Byte-First, or vice versa.
	 * <P>
	 * The swap is done in-place, reversing the order of bytes in the given
	 * array.
	 * <P>
	 *
	 * @param bytes
	 *            an array of bytes to swap the order of
	 * @param offset
	 *            the starting point in the bytes array
	 * @param nBytes
	 *            the number of bytes to swap
	 */
	public final static void swap(final byte[] bytes, final int offset,
			final int nBytes) {
		byte tmp;

		// Use an accelerated path for common swap sizes
		switch (nBytes) {
		case 1:
			return;
		case 2:
			tmp = bytes[offset + 0];
			bytes[offset + 0] = bytes[offset + 1];
			bytes[offset + 1] = tmp;
			return;

		case 4:
			tmp = bytes[offset + 0];
			bytes[offset + 0] = bytes[offset + 3];
			bytes[offset + 3] = tmp;

			tmp = bytes[offset + 1];
			bytes[offset + 1] = bytes[offset + 2];
			bytes[offset + 2] = tmp;
			return;

		case 8:
			tmp = bytes[offset + 0];
			bytes[offset + 0] = bytes[offset + 7];
			bytes[offset + 7] = tmp;

			tmp = bytes[offset + 1];
			bytes[offset + 1] = bytes[offset + 6];
			bytes[offset + 6] = tmp;

			tmp = bytes[offset + 2];
			bytes[offset + 2] = bytes[offset + 5];
			bytes[offset + 5] = tmp;

			tmp = bytes[offset + 3];
			bytes[offset + 3] = bytes[offset + 4];
			bytes[offset + 4] = tmp;
			return;
		}

		// Otherwise handle it generically
		int nb2 = nBytes >>> 1; // Divide by two & truncate
		for (int i = 0, j = nBytes - 1; i < nb2; i++, j--) {
			tmp = bytes[offset + i];
			bytes[offset + i] = bytes[offset + j];
			bytes[offset + j] = tmp;
		}
	}

	/**
	 * Reverses the byte order of nBytes of the byte array, doing it nTimes in a
	 * row for consecutive runs of nBytes. This method can be used to convert a
	 * byte array containing multiple values, swaping each one from Most- to
	 * Least-significant-Byte-First, or vice versa.
	 * <P>
	 * The swap is done in-place, reversing the order of bytes in the given
	 * array.
	 * <P>
	 *
	 * @param bytes
	 *            an array of bytes to swap the order of
	 * @param nBytes
	 *            the number of bytes to swap
	 * @param nTimes
	 *            the number of times to do consecutive swaps
	 */
	public final static void swapMultiple(final byte[] bytes, final int nBytes,
			final int nTimes) {
		int offset = 0;
		for (int i = 0; i < nTimes; i++) {
			swap(bytes, offset, nBytes);
			offset += nBytes;
		}
	}

	/**
	 * Reverses the byte order of nBytes of the byte array, starting at the
	 * given offset, and doing it nTimes in a row for consecutive runs of
	 * nBytes. This method can be used to convert a byte array containing
	 * multiple values, swaping each one from Most- to
	 * Least-significant-Byte-First, or vice versa.
	 * <P>
	 * The swap is done in-place, reversing the order of bytes in the given
	 * array.
	 * <P>
	 *
	 * @param bytes
	 *            an array of bytes to swap the order of
	 * @param offset
	 *            starting point in the array
	 * @param nBytes
	 *            the number of bytes to swap
	 * @param nTimes
	 *            the number of times to do consecutive swaps
	 */
	public final static void swapMultiple(final byte[] bytes, int offset,
			final int nBytes, final int nTimes) {
		for (int i = 0; i < nTimes; i++) {
			swap(bytes, offset, nBytes);
			offset += nBytes;
		}
	}

	// ----------------------------------------------------------------------
	// Value Cast Methods
	// ----------------------------------------------------------------------

	//
	// Without array offset
	//
	// Unsigned Byte
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 * Because Java doesn't support unsigned primitive types, this method
	 * returns the value in the next larger data type, with the high bits set to
	 * zeroes.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @return the unsigned byte value (as an short)
	 */
	public final static short castToByte(final byte[] bytes) {
		return (short) (bytes[0] & 0x00FF);
	}

	// Short
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @return the short value
	 */
	public final static short castToShort(final byte[] bytes) {
		return (short) (((bytes[0] & 0x00FF) << 8) | (bytes[1] & 0x00FF));
	}

	// Unsigned Short
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 * Because Java doesn't support unsigned primitive types, this method
	 * returns the value in the next larger data type, with the high bits set to
	 * zeroes.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @return the unsigned short value (as an int)
	 */
	public final static int castToUnsignedShort(final byte[] bytes) {
		return ((bytes[0] & 0x00FF) << 8) | (bytes[1] & 0x00FF);
	}

	// Int
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @return the int value
	 */
	public final static int castToInt(final byte[] bytes) {
		return ((bytes[0] & 0x00FF) << 24) | ((bytes[1] & 0x00FF) << 16)
				| ((bytes[2] & 0x00FF) << 8) | (bytes[3] & 0x00FF);
	}

	// Unsigned Int
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 * Because Java doesn't support unsigned primitive types, this method
	 * returns the value in the next larger data type, with the high bits set to
	 * zeroes.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @return the unsigned int value (as a long)
	 */
	public final static long castToUnsignedInt(final byte[] bytes) {
		return (((long) bytes[0] & 0x00FF) << 24)
				| (((long) bytes[1] & 0x00FF) << 16)
				| (((long) bytes[2] & 0x00FF) << 8)
				| ((long) bytes[3] & 0x00FF);
	}

	// Long
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @return the long value
	 */
	public final static long castToLong(final byte[] bytes) {
		return (((long) bytes[0] & 0x00FF) << 56)
				| (((long) bytes[1] & 0x00FF) << 48)
				| (((long) bytes[2] & 0x00FF) << 40)
				| (((long) bytes[3] & 0x00FF) << 32)
				| (((long) bytes[4] & 0x00FF) << 24)
				| (((long) bytes[5] & 0x00FF) << 16)
				| (((long) bytes[6] & 0x00FF) << 8)
				| ((long) bytes[7] & 0x00FF);
	}

	// Unsigned Long
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @return the unsigned long value
	 */
	public final static long castToUnsignedLong(final byte[] bytes) {
		// Since there is no larger type we can return this
		// unsigned value in, we must return as a long.
		// This makes the code here identical to castToLong().
		return (((long) bytes[0] & 0x00FF) << 56)
				| (((long) bytes[1] & 0x00FF) << 48)
				| (((long) bytes[2] & 0x00FF) << 40)
				| (((long) bytes[3] & 0x00FF) << 32)
				| (((long) bytes[4] & 0x00FF) << 24)
				| (((long) bytes[5] & 0x00FF) << 16)
				| (((long) bytes[6] & 0x00FF) << 8)
				| ((long) bytes[7] & 0x00FF);
	}

	// Long Long
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @return the long long value
	 */
	public final static long castToLongLong(final byte[] bytes) {
		return (((long) bytes[0] & 0x00FF) << 56)
				| (((long) bytes[1] & 0x00FF) << 48)
				| (((long) bytes[2] & 0x00FF) << 40)
				| (((long) bytes[3] & 0x00FF) << 32)
				| (((long) bytes[4] & 0x00FF) << 24)
				| (((long) bytes[5] & 0x00FF) << 16)
				| (((long) bytes[6] & 0x00FF) << 8)
				| ((long) bytes[7] & 0x00FF);
	}

	// Unsigned Long Long
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @return the unsigned long long value
	 */
	public final static long castToUnsignedLongLong(final byte[] bytes) {
		// Since there is no larger type we can return this
		// unsigned value in, we must return as a long.
		// This makes the code here identical to castToLong().
		return (((long) bytes[0] & 0x00FF) << 56)
				| (((long) bytes[1] & 0x00FF) << 48)
				| (((long) bytes[2] & 0x00FF) << 40)
				| (((long) bytes[3] & 0x00FF) << 32)
				| (((long) bytes[4] & 0x00FF) << 24)
				| (((long) bytes[5] & 0x00FF) << 16)
				| (((long) bytes[6] & 0x00FF) << 8)
				| ((long) bytes[7] & 0x00FF);
	}

	// Float
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @return the float value
	 */
	public final static float castToFloat(final byte[] bytes) {
		int i = ((bytes[0] & 0x00FF) << 24) | ((bytes[1] & 0x00FF) << 16)
				| ((bytes[2] & 0x00FF) << 8) | (bytes[3] & 0x00FF);
		return Float.intBitsToFloat(i);
	}

	// Double
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @return the double value
	 */
	public final static double castToDouble(final byte[] bytes) {
		long l = (((long) bytes[0] & 0x00FF) << 56)
				| (((long) bytes[1] & 0x00FF) << 48)
				| (((long) bytes[2] & 0x00FF) << 40)
				| (((long) bytes[3] & 0x00FF) << 32)
				| (((long) bytes[4] & 0x00FF) << 24)
				| (((long) bytes[5] & 0x00FF) << 16)
				| (((long) bytes[6] & 0x00FF) << 8)
				| ((long) bytes[7] & 0x00FF);
		return Double.longBitsToDouble(l);
	}

	// Long Double
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 * Note: Since Java does not support long double types, this method is
	 * identical to the double version.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @return the long double value
	 */
	public final static double castToLongDouble(final byte[] bytes) {
		long l = (((long) bytes[0] & 0x00FF) << 56)
				| (((long) bytes[1] & 0x00FF) << 48)
				| (((long) bytes[2] & 0x00FF) << 40)
				| (((long) bytes[3] & 0x00FF) << 32)
				| (((long) bytes[4] & 0x00FF) << 24)
				| (((long) bytes[5] & 0x00FF) << 16)
				| (((long) bytes[6] & 0x00FF) << 8)
				| ((long) bytes[7] & 0x00FF);
		return Double.longBitsToDouble(l);
	}

	//
	// With array offset
	//
	// Byte
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 * Because Java doesn't support unsigned primitive types, this method
	 * returns the value in the next larger data type, with the high bits set to
	 * zeroes.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            starting point in byte array
	 * @return the unsigned byte value (as a short)
	 */
	public final static short castToByte(final byte[] bytes, final int offset) {
		return (short) (bytes[offset] & 0x00FF);
	}

	// Short
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            starting point in byte array
	 * @return the short value
	 */
	public final static short castToShort(final byte[] bytes, final int offset) {
		return (short) (((bytes[offset + 0] & 0x00FF) << 8) | (bytes[offset + 1] & 0x00FF));
	}

	// Unsigned Short
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 * Because Java doesn't support unsigned primitive types, this method
	 * returns the value in the next larger data type, with the high bits set to
	 * zeroes.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            starting point in byte array
	 * @return the unsigned short value (as an int)
	 */
	public final static int castToUnsignedShort(final byte[] bytes,
			final int offset) {
		return ((bytes[offset + 0] & 0x00FF) << 8)
				| (bytes[offset + 1] & 0x00FF);
	}

	// Int
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            starting point in byte array
	 * @return the int value
	 */
	public final static int castToInt(final byte[] bytes, final int offset) {
		return ((bytes[offset + 0] & 0x00FF) << 24)
				| ((bytes[offset + 1] & 0x00FF) << 16)
				| ((bytes[offset + 2] & 0x00FF) << 8)
				| (bytes[offset + 3] & 0x00FF);
	}

	// Unsigned Int
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 * Because Java doesn't support unsigned primitive types, this method
	 * returns the value in the next larger data type, with the high bits set to
	 * zeroes.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            starting point in byte array
	 * @return the unsigned int value (as a long)
	 */
	public final static long castToUnsignedInt(final byte[] bytes,
			final int offset) {
		return (((long) bytes[offset + 0] & 0x00FF) << 24)
				| (((long) bytes[offset + 1] & 0x00FF) << 16)
				| (((long) bytes[offset + 2] & 0x00FF) << 8)
				| ((long) bytes[offset + 3] & 0x00FF);
	}

	// Long
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            starting point in byte array
	 * @return the long value
	 */
	public final static long castToLong(final byte[] bytes, final int offset) {
		return (((long) bytes[offset + 0] & 0x00FF) << 56)
				| (((long) bytes[offset + 1] & 0x00FF) << 48)
				| (((long) bytes[offset + 2] & 0x00FF) << 40)
				| (((long) bytes[offset + 3] & 0x00FF) << 32)
				| (((long) bytes[offset + 4] & 0x00FF) << 24)
				| (((long) bytes[offset + 5] & 0x00FF) << 16)
				| (((long) bytes[offset + 6] & 0x00FF) << 8)
				| ((long) bytes[offset + 7] & 0x00FF);
	}

	// Unsigned Long
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            starting point in byte array
	 * @return the unsigned long value
	 */
	public final static long castToUnsignedLong(final byte[] bytes,
			final int offset) {
		// Since there is no larger type we can return this
		// unsigned value in, we must return as a long.
		// This makes the code here identical to castToLong().
		return (((long) bytes[offset + 0] & 0x00FF) << 56)
				| (((long) bytes[offset + 1] & 0x00FF) << 48)
				| (((long) bytes[offset + 2] & 0x00FF) << 40)
				| (((long) bytes[offset + 3] & 0x00FF) << 32)
				| (((long) bytes[offset + 4] & 0x00FF) << 24)
				| (((long) bytes[offset + 5] & 0x00FF) << 16)
				| (((long) bytes[offset + 6] & 0x00FF) << 8)
				| ((long) bytes[offset + 7] & 0x00FF);
	}

	// Long Long
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            starting point in byte array
	 * @return the long long value
	 */
	public final static long castToLongLong(final byte[] bytes, final int offset) {
		return (((long) bytes[offset + 0] & 0x00FF) << 56)
				| (((long) bytes[offset + 1] & 0x00FF) << 48)
				| (((long) bytes[offset + 2] & 0x00FF) << 40)
				| (((long) bytes[offset + 3] & 0x00FF) << 32)
				| (((long) bytes[offset + 4] & 0x00FF) << 24)
				| (((long) bytes[offset + 5] & 0x00FF) << 16)
				| (((long) bytes[offset + 6] & 0x00FF) << 8)
				| ((long) bytes[offset + 7] & 0x00FF);
	}

	// Unsigned Long Long
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            starting point in byte array
	 * @return the unsigned long long value
	 */
	public final static long castToUnsignedLongLong(final byte[] bytes,
			final int offset) {
		// Since there is no larger type we can return this
		// unsigned value in, we must return as a long.
		// This makes the code here identical to castToLong().
		return (((long) bytes[offset + 0] & 0x00FF) << 56)
				| (((long) bytes[offset + 1] & 0x00FF) << 48)
				| (((long) bytes[offset + 2] & 0x00FF) << 40)
				| (((long) bytes[offset + 3] & 0x00FF) << 32)
				| (((long) bytes[offset + 4] & 0x00FF) << 24)
				| (((long) bytes[offset + 5] & 0x00FF) << 16)
				| (((long) bytes[offset + 6] & 0x00FF) << 8)
				| ((long) bytes[offset + 7] & 0x00FF);
	}

	// Float
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            starting point in byte array
	 * @return the float value
	 */
	public final static float castToFloat(final byte[] bytes, final int offset) {
		int i = ((bytes[offset + 0] & 0x00FF) << 24)
				| ((bytes[offset + 1] & 0x00FF) << 16)
				| ((bytes[offset + 2] & 0x00FF) << 8)
				| (bytes[offset + 3] & 0x00FF);
		return Float.intBitsToFloat(i);
	}

	// Double
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            starting point in byte array
	 * @return the double value
	 */
	public final static double castToDouble(final byte[] bytes, final int offset) {
		long l = (((long) bytes[offset + 0] & 0x00FF) << 56)
				| (((long) bytes[offset + 1] & 0x00FF) << 48)
				| (((long) bytes[offset + 2] & 0x00FF) << 40)
				| (((long) bytes[offset + 3] & 0x00FF) << 32)
				| (((long) bytes[offset + 4] & 0x00FF) << 24)
				| (((long) bytes[offset + 5] & 0x00FF) << 16)
				| (((long) bytes[offset + 6] & 0x00FF) << 8)
				| ((long) bytes[offset + 7] & 0x00FF);
		return Double.longBitsToDouble(l);
	}

	// Long Double
	/**
	 * Casts bytes from the given byte array into a value of the type and
	 * returns it. The bytes in the array need not be aligned appropriately for
	 * the type. The array is assumed to contain a legal value for the type with
	 * sizeof(type) bytes in the Java byte order.
	 * <P>
	 * Note: Since Java does not support long double types, this method is
	 * identical to the double version.
	 * <P>
	 *
	 * @param bytes
	 *            byte array
	 * @param offset
	 *            starting point in byte array
	 * @return the long double value
	 */
	public final static double castToLongDouble(final byte[] bytes,
			final int offset) {
		long l = (((long) bytes[offset + 0] & 0x00FF) << 56)
				| (((long) bytes[offset + 1] & 0x00FF) << 48)
				| (((long) bytes[offset + 2] & 0x00FF) << 40)
				| (((long) bytes[offset + 3] & 0x00FF) << 32)
				| (((long) bytes[offset + 4] & 0x00FF) << 24)
				| (((long) bytes[offset + 5] & 0x00FF) << 16)
				| (((long) bytes[offset + 6] & 0x00FF) << 8)
				| ((long) bytes[offset + 7] & 0x00FF);
		return Double.longBitsToDouble(l);
	}

	// ----------------------------------------------------------------------
	// Value Copy Methods
	// ----------------------------------------------------------------------

	//
	// Without array offset
	//

	// Short
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyShort(final byte value, final byte[] bytes) {
		short s = value; // Sign extend
		bytes[0] = (byte) (s >> 8);
		bytes[1] = (byte) (s);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyShort(final short value, final byte[] bytes) {
		bytes[0] = (byte) (value >> 8);
		bytes[1] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyShort(final int value, final byte[] bytes) {
		// Identical to copyShort(short,byte[])
		bytes[0] = (byte) (value >> 8);
		bytes[1] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyShort(final long value, final byte[] bytes) {
		// Identical to copyShort(short,byte[])
		bytes[0] = (byte) (value >> 8);
		bytes[1] = (byte) (value);
	}

	// Unsigned Short
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedShort(final byte value,
			final byte[] bytes) {
		bytes[0] = 0; // zero extend
		bytes[1] = value;
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedShort(final short value,
			final byte[] bytes) {
		bytes[0] = (byte) (value >>> 8); // zero extend
		bytes[1] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedShort(final int value,
			final byte[] bytes) {
		// Identical to copyUnsignedShort(short,byte[])
		bytes[0] = (byte) (value >>> 8); // zero extend
		bytes[1] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedShort(final long value,
			final byte[] bytes) {
		// Identical to copyUnsignedShort(short,byte[])
		bytes[0] = (byte) (value >>> 8); // zero extend
		bytes[1] = (byte) (value);
	}

	// Int
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyInt(final byte value, final byte[] bytes) {
		int i = value; // sign extend
		bytes[0] = (byte) (i >> 24);
		bytes[1] = (byte) (i >> 16);
		bytes[2] = (byte) (i >> 8);
		bytes[3] = (byte) (i);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyInt(final short value, final byte[] bytes) {
		int i = value; // sign extend
		bytes[0] = (byte) (i >> 24);
		bytes[1] = (byte) (i >> 16);
		bytes[2] = (byte) (i >> 8);
		bytes[3] = (byte) (i);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyInt(final int value, final byte[] bytes) {
		bytes[0] = (byte) (value >> 24);
		bytes[1] = (byte) (value >> 16);
		bytes[2] = (byte) (value >> 8);
		bytes[3] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyInt(final long value, final byte[] bytes) {
		// Identical to copyInt(int,byte[])
		bytes[0] = (byte) (value >> 24);
		bytes[1] = (byte) (value >> 16);
		bytes[2] = (byte) (value >> 8);
		bytes[3] = (byte) (value);
	}

	// Unsigned Int
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedInt(final byte value,
			final byte[] bytes) {
		bytes[0] = 0; // zero extend
		bytes[1] = 0; // zero extend
		bytes[2] = 0; // zero extend
		bytes[3] = value;
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedInt(final short value,
			final byte[] bytes) {
		bytes[0] = 0; // zero extend
		bytes[1] = 0; // zero extend
		bytes[2] = (byte) (value >>> 8); // zero extend
		bytes[3] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedInt(final int value, final byte[] bytes) {
		bytes[0] = (byte) (value >>> 24); // zero extend
		bytes[1] = (byte) (value >>> 16); // zero extend
		bytes[2] = (byte) (value >>> 8); // zero extend
		bytes[3] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedInt(final long value,
			final byte[] bytes) {
		// Identical to copyUnsignedInt(int,byte[])
		bytes[0] = (byte) (value >>> 24); // zero extend
		bytes[1] = (byte) (value >>> 16); // zero extend
		bytes[2] = (byte) (value >>> 8); // zero extend
		bytes[3] = (byte) (value);
	}

	// Long
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyLong(final byte value, final byte[] bytes) {
		long lng = value; // sign extend
		bytes[0] = (byte) (lng >> 56);
		bytes[1] = (byte) (lng >> 48);
		bytes[2] = (byte) (lng >> 40);
		bytes[3] = (byte) (lng >> 32);
		bytes[4] = (byte) (lng >> 24);
		bytes[5] = (byte) (lng >> 16);
		bytes[6] = (byte) (lng >> 8);
		bytes[7] = (byte) (lng);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyLong(final short value, final byte[] bytes) {
		long lng = value; // sign extend
		bytes[0] = (byte) (lng >> 56);
		bytes[1] = (byte) (lng >> 48);
		bytes[2] = (byte) (lng >> 40);
		bytes[3] = (byte) (lng >> 32);
		bytes[4] = (byte) (lng >> 24);
		bytes[5] = (byte) (lng >> 16);
		bytes[6] = (byte) (lng >> 8);
		bytes[7] = (byte) (lng);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyLong(final int value, final byte[] bytes) {
		long lng = value; // sign extend
		bytes[0] = (byte) (lng >> 56);
		bytes[1] = (byte) (lng >> 48);
		bytes[2] = (byte) (lng >> 40);
		bytes[3] = (byte) (lng >> 32);
		bytes[4] = (byte) (lng >> 24);
		bytes[5] = (byte) (lng >> 16);
		bytes[6] = (byte) (lng >> 8);
		bytes[7] = (byte) (lng);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyLong(final long value, final byte[] bytes) {
		bytes[0] = (byte) (value >> 56);
		bytes[1] = (byte) (value >> 48);
		bytes[2] = (byte) (value >> 40);
		bytes[3] = (byte) (value >> 32);
		bytes[4] = (byte) (value >> 24);
		bytes[5] = (byte) (value >> 16);
		bytes[6] = (byte) (value >> 8);
		bytes[7] = (byte) (value);
	}

	// Unsigned Long
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedLong(final byte value,
			final byte[] bytes) {
		bytes[0] = 0; // zero extend
		bytes[1] = 0; // zero extend
		bytes[2] = 0; // zero extend
		bytes[3] = 0; // zero extend
		bytes[4] = 0; // zero extend
		bytes[5] = 0; // zero extend
		bytes[6] = 0; // zero extend
		bytes[7] = value;
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedLong(final short value,
			final byte[] bytes) {
		bytes[0] = 0; // zero extend
		bytes[1] = 0; // zero extend
		bytes[2] = 0; // zero extend
		bytes[3] = 0; // zero extend
		bytes[4] = 0; // zero extend
		bytes[5] = 0; // zero extend
		bytes[6] = (byte) (value >>> 8); // zero extend
		bytes[7] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedLong(final int value,
			final byte[] bytes) {
		bytes[0] = 0; // zero extend
		bytes[1] = 0; // zero extend
		bytes[2] = 0; // zero extend
		bytes[3] = 0; // zero extend
		bytes[4] = (byte) (value >>> 24); // zero extend
		bytes[5] = (byte) (value >>> 16); // zero extend
		bytes[6] = (byte) (value >>> 8); // zero extend
		bytes[7] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedLong(final long value,
			final byte[] bytes) {
		bytes[0] = (byte) (value >>> 56); // zero extend
		bytes[1] = (byte) (value >>> 48); // zero extend
		bytes[2] = (byte) (value >>> 40); // zero extend
		bytes[3] = (byte) (value >>> 32); // zero extend
		bytes[4] = (byte) (value >>> 24); // zero extend
		bytes[5] = (byte) (value >>> 16); // zero extend
		bytes[6] = (byte) (value >>> 8); // zero extend
		bytes[7] = (byte) (value);
	}

	// Long Long
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyLongLong(final byte value, final byte[] bytes) {
		long lng = value; // sign extend
		bytes[0] = (byte) (lng >> 56);
		bytes[1] = (byte) (lng >> 48);
		bytes[2] = (byte) (lng >> 40);
		bytes[3] = (byte) (lng >> 32);
		bytes[4] = (byte) (lng >> 24);
		bytes[5] = (byte) (lng >> 16);
		bytes[6] = (byte) (lng >> 8);
		bytes[7] = (byte) (lng);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyLongLong(final short value, final byte[] bytes) {
		long lng = value; // sign extend
		bytes[0] = (byte) (lng >> 56);
		bytes[1] = (byte) (lng >> 48);
		bytes[2] = (byte) (lng >> 40);
		bytes[3] = (byte) (lng >> 32);
		bytes[4] = (byte) (lng >> 24);
		bytes[5] = (byte) (lng >> 16);
		bytes[6] = (byte) (lng >> 8);
		bytes[7] = (byte) (lng);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyLongLong(final int value, final byte[] bytes) {
		long lng = value; // sign extend
		bytes[0] = (byte) (lng >> 56);
		bytes[1] = (byte) (lng >> 48);
		bytes[2] = (byte) (lng >> 40);
		bytes[3] = (byte) (lng >> 32);
		bytes[4] = (byte) (lng >> 24);
		bytes[5] = (byte) (lng >> 16);
		bytes[6] = (byte) (lng >> 8);
		bytes[7] = (byte) (lng);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyLongLong(final long value, final byte[] bytes) {
		bytes[0] = (byte) (value >> 56);
		bytes[1] = (byte) (value >> 48);
		bytes[2] = (byte) (value >> 40);
		bytes[3] = (byte) (value >> 32);
		bytes[4] = (byte) (value >> 24);
		bytes[5] = (byte) (value >> 16);
		bytes[6] = (byte) (value >> 8);
		bytes[7] = (byte) (value);
	}

	// Unsigned Long
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedLongLong(final byte value,
			final byte[] bytes) {
		bytes[0] = 0; // zero extend
		bytes[1] = 0; // zero extend
		bytes[2] = 0; // zero extend
		bytes[3] = 0; // zero extend
		bytes[4] = 0; // zero extend
		bytes[5] = 0; // zero extend
		bytes[6] = 0; // zero extend
		bytes[7] = value;
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedLongLong(final short value,
			final byte[] bytes) {
		bytes[0] = 0; // zero extend
		bytes[1] = 0; // zero extend
		bytes[2] = 0; // zero extend
		bytes[3] = 0; // zero extend
		bytes[4] = 0; // zero extend
		bytes[5] = 0; // zero extend
		bytes[6] = (byte) (value >>> 8); // zero extend
		bytes[7] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedLongLong(final int value,
			final byte[] bytes) {
		bytes[0] = 0; // zero extend
		bytes[1] = 0; // zero extend
		bytes[2] = 0; // zero extend
		bytes[3] = 0; // zero extend
		bytes[4] = (byte) (value >>> 24); // zero extend
		bytes[5] = (byte) (value >>> 16); // zero extend
		bytes[6] = (byte) (value >>> 8); // zero extend
		bytes[7] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyUnsignedLongLong(final long value,
			final byte[] bytes) {
		bytes[0] = (byte) (value >>> 56); // zero extend
		bytes[1] = (byte) (value >>> 48); // zero extend
		bytes[2] = (byte) (value >>> 40); // zero extend
		bytes[3] = (byte) (value >>> 32); // zero extend
		bytes[4] = (byte) (value >>> 24); // zero extend
		bytes[5] = (byte) (value >>> 16); // zero extend
		bytes[6] = (byte) (value >>> 8); // zero extend
		bytes[7] = (byte) (value);
	}

	// Float
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyFloat(final float value, final byte[] bytes) {
		int v = Float.floatToIntBits(value);
		bytes[0] = (byte) (v >> 24);
		bytes[1] = (byte) (v >> 16);
		bytes[2] = (byte) (v >> 8);
		bytes[3] = (byte) (v);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyFloat(final double value, final byte[] bytes) {
		int v = Float.floatToIntBits((float) value);
		bytes[0] = (byte) (v >> 24);
		bytes[1] = (byte) (v >> 16);
		bytes[2] = (byte) (v >> 8);
		bytes[3] = (byte) (v);
	}

	// Double
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyDouble(final double value, final byte[] bytes) {
		long v = Double.doubleToLongBits(value);
		bytes[0] = (byte) (v >> 56);
		bytes[1] = (byte) (v >> 48);
		bytes[2] = (byte) (v >> 40);
		bytes[3] = (byte) (v >> 32);
		bytes[4] = (byte) (v >> 24);
		bytes[5] = (byte) (v >> 16);
		bytes[6] = (byte) (v >> 8);
		bytes[7] = (byte) (v);
	}

	// Long Double
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long double types, this method is
	 * identical to the double version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 */
	public final static void copyLongDouble(final double value,
			final byte[] bytes) {
		long v = Double.doubleToLongBits(value);
		bytes[0] = (byte) (v >> 56);
		bytes[1] = (byte) (v >> 48);
		bytes[2] = (byte) (v >> 40);
		bytes[3] = (byte) (v >> 32);
		bytes[4] = (byte) (v >> 24);
		bytes[5] = (byte) (v >> 16);
		bytes[6] = (byte) (v >> 8);
		bytes[7] = (byte) (v);
	}

	//
	// With array offset
	//
	// Short
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyShort(final byte value, final byte[] bytes,
			final int offset) {
		short s = value; // sign extend
		bytes[offset + 0] = (byte) (s >> 8);
		bytes[offset + 1] = (byte) (s);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyShort(final short value, final byte[] bytes,
			final int offset) {
		bytes[offset + 0] = (byte) (value >> 8);
		bytes[offset + 1] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyShort(final int value, final byte[] bytes,
			final int offset) {
		// Identical to copyShort(short,byte[])
		bytes[offset + 0] = (byte) (value >> 8);
		bytes[offset + 1] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyShort(final long value, final byte[] bytes,
			final int offset) {
		// Identical to copyShort(short,byte[])
		bytes[offset + 0] = (byte) (value >> 8);
		bytes[offset + 1] = (byte) (value);
	}

	// Unsigned Short
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedShort(final byte value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = 0; // zero extend
		bytes[offset + 1] = value;
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedShort(final short value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = (byte) (value >>> 8); // zero extend
		bytes[offset + 1] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedShort(final int value,
			final byte[] bytes, final int offset) {
		// Identical to copyUnsignedShort(short,byte[])
		bytes[offset + 0] = (byte) (value >>> 8); // zero extend
		bytes[offset + 1] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedShort(final long value,
			final byte[] bytes, final int offset) {
		// Identical to copyUnsignedShort(short,byte[])
		bytes[offset + 0] = (byte) (value >>> 8); // zero extend
		bytes[offset + 1] = (byte) (value);
	}

	// Int
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyInt(final byte value, final byte[] bytes,
			final int offset) {
		int i = value; // sign extend
		bytes[offset + 0] = (byte) (i >> 24);
		bytes[offset + 1] = (byte) (i >> 16);
		bytes[offset + 2] = (byte) (i >> 8);
		bytes[offset + 3] = (byte) (i);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyInt(final short value, final byte[] bytes,
			final int offset) {
		int i = value; // sign extend
		bytes[offset + 0] = (byte) (i >> 24);
		bytes[offset + 1] = (byte) (i >> 16);
		bytes[offset + 2] = (byte) (i >> 8);
		bytes[offset + 3] = (byte) (i);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyInt(final int value, final byte[] bytes,
			final int offset) {
		bytes[offset + 0] = (byte) (value >> 24);
		bytes[offset + 1] = (byte) (value >> 16);
		bytes[offset + 2] = (byte) (value >> 8);
		bytes[offset + 3] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyInt(final long value, final byte[] bytes,
			final int offset) {
		// Identical to copyInt(int,byte[])
		bytes[offset + 0] = (byte) (value >> 24);
		bytes[offset + 1] = (byte) (value >> 16);
		bytes[offset + 2] = (byte) (value >> 8);
		bytes[offset + 3] = (byte) (value);
	}

	// Unsigned Int
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedInt(final byte value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = 0; // zero extend
		bytes[offset + 1] = 0; // zero extend
		bytes[offset + 2] = 0; // zero extend
		bytes[offset + 3] = value;
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedInt(final short value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = 0; // zero extend
		bytes[offset + 1] = 0; // zero extend
		bytes[offset + 2] = (byte) (value >>> 8); // zero extend
		bytes[offset + 3] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedInt(final int value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = (byte) (value >>> 24); // zero extend
		bytes[offset + 1] = (byte) (value >>> 16); // zero extend
		bytes[offset + 2] = (byte) (value >>> 8); // zero extend
		bytes[offset + 3] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedInt(final long value,
			final byte[] bytes, final int offset) {
		// Identical to copyUnsignedInt(int,byte[])
		bytes[offset + 0] = (byte) (value >>> 24); // zero extend
		bytes[offset + 1] = (byte) (value >>> 16); // zero extend
		bytes[offset + 2] = (byte) (value >>> 8); // zero extend
		bytes[offset + 3] = (byte) (value);
	}

	// Long
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyLong(final byte value, final byte[] bytes,
			final int offset) {
		long lng = value; // sign extend
		bytes[offset + 0] = (byte) (lng >> 56);
		bytes[offset + 1] = (byte) (lng >> 48);
		bytes[offset + 2] = (byte) (lng >> 40);
		bytes[offset + 3] = (byte) (lng >> 32);
		bytes[offset + 4] = (byte) (lng >> 24);
		bytes[offset + 5] = (byte) (lng >> 16);
		bytes[offset + 6] = (byte) (lng >> 8);
		bytes[offset + 7] = (byte) (lng);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyLong(final short value, final byte[] bytes,
			final int offset) {
		long lng = value; // sign extend
		bytes[offset + 0] = (byte) (lng >> 56);
		bytes[offset + 1] = (byte) (lng >> 48);
		bytes[offset + 2] = (byte) (lng >> 40);
		bytes[offset + 3] = (byte) (lng >> 32);
		bytes[offset + 4] = (byte) (lng >> 24);
		bytes[offset + 5] = (byte) (lng >> 16);
		bytes[offset + 6] = (byte) (lng >> 8);
		bytes[offset + 7] = (byte) (lng);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyLong(final int value, final byte[] bytes,
			final int offset) {
		long lng = value; // sign extend
		bytes[offset + 0] = (byte) (lng >> 56);
		bytes[offset + 1] = (byte) (lng >> 48);
		bytes[offset + 2] = (byte) (lng >> 40);
		bytes[offset + 3] = (byte) (lng >> 32);
		bytes[offset + 4] = (byte) (lng >> 24);
		bytes[offset + 5] = (byte) (lng >> 16);
		bytes[offset + 6] = (byte) (lng >> 8);
		bytes[offset + 7] = (byte) (lng);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyLong(final long value, final byte[] bytes,
			final int offset) {
		bytes[offset + 0] = (byte) (value >> 56);
		bytes[offset + 1] = (byte) (value >> 48);
		bytes[offset + 2] = (byte) (value >> 40);
		bytes[offset + 3] = (byte) (value >> 32);
		bytes[offset + 4] = (byte) (value >> 24);
		bytes[offset + 5] = (byte) (value >> 16);
		bytes[offset + 6] = (byte) (value >> 8);
		bytes[offset + 7] = (byte) (value);
	}

	// Unsigned Long
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedLong(final byte value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = 0; // zero extend
		bytes[offset + 1] = 0; // zero extend
		bytes[offset + 2] = 0; // zero extend
		bytes[offset + 3] = 0; // zero extend
		bytes[offset + 4] = 0; // zero extend
		bytes[offset + 5] = 0; // zero extend
		bytes[offset + 6] = 0; // zero extend
		bytes[offset + 7] = value;
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedLong(final short value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = 0; // zero extend
		bytes[offset + 1] = 0; // zero extend
		bytes[offset + 2] = 0; // zero extend
		bytes[offset + 3] = 0; // zero extend
		bytes[offset + 4] = 0; // zero extend
		bytes[offset + 5] = 0; // zero extend
		bytes[offset + 6] = (byte) (value >>> 8); // zero extend
		bytes[offset + 7] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedLong(final int value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = 0; // zero extend
		bytes[offset + 1] = 0; // zero extend
		bytes[offset + 2] = 0; // zero extend
		bytes[offset + 3] = 0; // zero extend
		bytes[offset + 4] = (byte) (value >>> 24); // zero extend
		bytes[offset + 5] = (byte) (value >>> 16); // zero extend
		bytes[offset + 6] = (byte) (value >>> 8); // zero extend
		bytes[offset + 7] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedLong(final long value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = (byte) (value >>> 56); // zero extend
		bytes[offset + 1] = (byte) (value >>> 48); // zero extend
		bytes[offset + 2] = (byte) (value >>> 40); // zero extend
		bytes[offset + 3] = (byte) (value >>> 32); // zero extend
		bytes[offset + 4] = (byte) (value >>> 24); // zero extend
		bytes[offset + 5] = (byte) (value >>> 16); // zero extend
		bytes[offset + 6] = (byte) (value >>> 8); // zero extend
		bytes[offset + 7] = (byte) (value);
	}

	// Long Long
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyLongLong(final byte value, final byte[] bytes,
			final int offset) {
		long lng = value; // sign extend
		bytes[offset + 0] = (byte) (lng >> 56);
		bytes[offset + 1] = (byte) (lng >> 48);
		bytes[offset + 2] = (byte) (lng >> 40);
		bytes[offset + 3] = (byte) (lng >> 32);
		bytes[offset + 4] = (byte) (lng >> 24);
		bytes[offset + 5] = (byte) (lng >> 16);
		bytes[offset + 6] = (byte) (lng >> 8);
		bytes[offset + 7] = (byte) (lng);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyLongLong(final short value,
			final byte[] bytes, final int offset) {
		long lng = value; // sign extend
		bytes[offset + 0] = (byte) (lng >> 56);
		bytes[offset + 1] = (byte) (lng >> 48);
		bytes[offset + 2] = (byte) (lng >> 40);
		bytes[offset + 3] = (byte) (lng >> 32);
		bytes[offset + 4] = (byte) (lng >> 24);
		bytes[offset + 5] = (byte) (lng >> 16);
		bytes[offset + 6] = (byte) (lng >> 8);
		bytes[offset + 7] = (byte) (lng);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyLongLong(final int value, final byte[] bytes,
			final int offset) {
		long lng = value; // sign extend
		bytes[offset + 0] = (byte) (lng >> 56);
		bytes[offset + 1] = (byte) (lng >> 48);
		bytes[offset + 2] = (byte) (lng >> 40);
		bytes[offset + 3] = (byte) (lng >> 32);
		bytes[offset + 4] = (byte) (lng >> 24);
		bytes[offset + 5] = (byte) (lng >> 16);
		bytes[offset + 6] = (byte) (lng >> 8);
		bytes[offset + 7] = (byte) (lng);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyLongLong(final long value, final byte[] bytes,
			final int offset) {
		bytes[offset + 0] = (byte) (value >> 56);
		bytes[offset + 1] = (byte) (value >> 48);
		bytes[offset + 2] = (byte) (value >> 40);
		bytes[offset + 3] = (byte) (value >> 32);
		bytes[offset + 4] = (byte) (value >> 24);
		bytes[offset + 5] = (byte) (value >> 16);
		bytes[offset + 6] = (byte) (value >> 8);
		bytes[offset + 7] = (byte) (value);
	}

	// Unsigned Long
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedLongLong(final byte value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = 0; // zero extend
		bytes[offset + 1] = 0; // zero extend
		bytes[offset + 2] = 0; // zero extend
		bytes[offset + 3] = 0; // zero extend
		bytes[offset + 4] = 0; // zero extend
		bytes[offset + 5] = 0; // zero extend
		bytes[offset + 6] = 0; // zero extend
		bytes[offset + 7] = value;
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedLongLong(final short value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = 0; // zero extend
		bytes[offset + 1] = 0; // zero extend
		bytes[offset + 2] = 0; // zero extend
		bytes[offset + 3] = 0; // zero extend
		bytes[offset + 4] = 0; // zero extend
		bytes[offset + 5] = 0; // zero extend
		bytes[offset + 6] = (byte) (value >>> 8); // zero extend
		bytes[offset + 7] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedLongLong(final int value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = 0; // zero extend
		bytes[offset + 1] = 0; // zero extend
		bytes[offset + 2] = 0; // zero extend
		bytes[offset + 3] = 0; // zero extend
		bytes[offset + 4] = (byte) (value >>> 24); // zero extend
		bytes[offset + 5] = (byte) (value >>> 16); // zero extend
		bytes[offset + 6] = (byte) (value >>> 8); // zero extend
		bytes[offset + 7] = (byte) (value);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long long types, this method is
	 * identical to the long version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyUnsignedLongLong(final long value,
			final byte[] bytes, final int offset) {
		bytes[offset + 0] = (byte) (value >>> 56); // zero extend
		bytes[offset + 1] = (byte) (value >>> 48); // zero extend
		bytes[offset + 2] = (byte) (value >>> 40); // zero extend
		bytes[offset + 3] = (byte) (value >>> 32); // zero extend
		bytes[offset + 4] = (byte) (value >>> 24); // zero extend
		bytes[offset + 5] = (byte) (value >>> 16); // zero extend
		bytes[offset + 6] = (byte) (value >>> 8); // zero extend
		bytes[offset + 7] = (byte) (value);
	}

	// Float
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyFloat(final float value, final byte[] bytes,
			final int offset) {
		int v = Float.floatToIntBits(value);
		bytes[offset + 0] = (byte) (v >> 24);
		bytes[offset + 1] = (byte) (v >> 16);
		bytes[offset + 2] = (byte) (v >> 8);
		bytes[offset + 3] = (byte) (v);
	}

	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyFloat(final double value, final byte[] bytes,
			final int offset) {
		int v = Float.floatToIntBits((float) value);
		bytes[offset + 0] = (byte) (v >> 24);
		bytes[offset + 1] = (byte) (v >> 16);
		bytes[offset + 2] = (byte) (v >> 8);
		bytes[offset + 3] = (byte) (v);
	}

	// Double
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyDouble(final double value, final byte[] bytes,
			final int offset) {
		long v = Double.doubleToLongBits(value);
		bytes[offset + 0] = (byte) (v >> 56);
		bytes[offset + 1] = (byte) (v >> 48);
		bytes[offset + 2] = (byte) (v >> 40);
		bytes[offset + 3] = (byte) (v >> 32);
		bytes[offset + 4] = (byte) (v >> 24);
		bytes[offset + 5] = (byte) (v >> 16);
		bytes[offset + 6] = (byte) (v >> 8);
		bytes[offset + 7] = (byte) (v);
	}

	// Long Double
	/**
	 * Copies the given value of the type into the given byte array. The array
	 * is assumed to be large enough to hold a value of sizeof(type) bytes.
	 * <P>
	 * Note: Since Java does not support long double types, this method is
	 * identical to the double version.
	 * <P>
	 *
	 * @param value
	 *            the value to copy
	 * @param bytes
	 *            byte array to receive value
	 * @param offset
	 *            starting point in array
	 */
	public final static void copyLongDouble(final double value,
			final byte[] bytes, final int offset) {
		long v = Double.doubleToLongBits(value);
		bytes[offset + 0] = (byte) (v >> 56);
		bytes[offset + 1] = (byte) (v >> 48);
		bytes[offset + 2] = (byte) (v >> 40);
		bytes[offset + 3] = (byte) (v >> 32);
		bytes[offset + 4] = (byte) (v >> 24);
		bytes[offset + 5] = (byte) (v >> 16);
		bytes[offset + 6] = (byte) (v >> 8);
		bytes[offset + 7] = (byte) (v);
	}

	// ----------------------------------------------------------------------
	// Debugging Methods
	// ----------------------------------------------------------------------
	/**
	 * Prints to System.out the attributes of the current host.
	 * <P>
	 */
	public final static void print() {
		System.out.println("Host attributes:");
		System.out.println("  " + "Type            " + // 15 char field
				"Size " + // 4 char field
				"Bits " + // 4 char field
				"CC align"); // 10 char field
		for (int i = 0; i < NUMBER_TYPES; i++) {
			StringBuffer sb = new StringBuffer("  ");
			int len;
			String s;

			sb.append(typeName[i]);
			len = 15 - typeName[i].length();
			for (int j = 0; j < len; j++) {
				sb.append(' ');
			}
			sb.append(' ');

			s = String.valueOf(typeSize[i]);
			sb.append(s);
			len = 4 - s.length();
			for (int j = 0; j < len; j++) {
				sb.append(' ');
			}
			sb.append(' ');

			s = String.valueOf(typeBits[i]);
			sb.append(s);
			len = 4 - s.length();
			for (int j = 0; j < len; j++) {
				sb.append(' ');
			}
			sb.append(' ');

			s = String.valueOf(typeCompilerAlignment[i]);
			sb.append(s);
			len = 4 - s.length();
			for (int j = 0; j < len; j++) {
				sb.append(' ');
			}

			System.out.println(sb);
		}
	}
}
