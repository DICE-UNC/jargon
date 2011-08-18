/**
 * 
 */
package org.irods.jargon.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for working with the local file system.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class LocalFileUtils {

	public static final Logger log = LoggerFactory
			.getLogger(LocalFileUtils.class);
	
	public static final DateFormat dateFormat = DateFormat.getDateTimeInstance();

	/**
	 * private constructor, this is not meant to be an instantiated class.
	 */
	private LocalFileUtils() {

	}

	/**
	 * Parse a file name to get the stuff after the last '.' character to treat
	 * as the file extension
	 * @param fileName <code>String</code> with the file name to parse out.
	 * @return <code>String</code> with the file extension
	 */
	public static String getFileExtension(final String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null fileName");
		}
	
		int lastDot = fileName.lastIndexOf('.');
		if (lastDot == -1) {
			return "";
		} else {
			return(fileName.substring(lastDot));
		}
	
	}
	
	/**
	 * Parse a file name to get the stuff before last '.' character to treat
	 * as the file name
	 * @param fileName <code>String</code> with the file name to parse out.
	 * @return <code>String</code> with the file name before the extension, without the '.'
	 */
	public static String getFileNameUpToExtension(final String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null fileName");
		}
	
		int lastDot = fileName.lastIndexOf('.');
		if (lastDot == -1) {
			return "";
		} else {
			return(fileName.substring(0, lastDot));
		}
	
	}
	
	/**
	 * Interpose a time stamp between the file name and extension
	 * @param fileName <code>String</code> with the file name to parse out
	 * @return <code>String</code> with the updated file name containing a time stamp
	 */
	public static String getFileNameWithTimeStampInterposed(final String fileName) {
		String namePart = getFileNameUpToExtension(fileName);
		String extension = getFileExtension(fileName);
		StringBuilder newName = new StringBuilder(namePart);
		newName.append(".[backup from - ");
		newName.append(dateFormat.format(new Date()));
		newName.append("]");
		newName.append(extension);
		return newName.toString();
	}
	
	
	/**
	 * Count files in a directory (including files in all subdirectories)
	 * 
	 * @param directory
	 *            <code>File</code> with the directory to start in
	 * @return the total number of files as <code>int</code>
	 */
	public static int countFilesInDirectory(final File directory) {
		int count = 0;

		if (directory.isFile()) {
			count = 1;
		} else {

			for (File file : directory.listFiles()) {
				if (file.isFile()) {
					count++;
				}
				if (file.isDirectory()) {
					count += countFilesInDirectory(file);
				}
			}
		}
		return count;
	}

	/**
	 * @param localFileToHoldData
	 * @throws JargonException
	 */
	public static void createLocalFileIfNotExists(final File localFileToHoldData)
			throws JargonException {
		if (localFileToHoldData.exists()) {
			log.info(
					"local file exists, will not create the local file for {}",
					localFileToHoldData.getAbsolutePath());
		} else {
			log.info(
					"local file does not exist, will attempt to create local file: {}",
					localFileToHoldData.getAbsolutePath());
			try {
				localFileToHoldData.createNewFile();
			} catch (IOException e) {
				log.error(
						"IOException when trying to create a new file for the local output stream for {}",
						localFileToHoldData.getAbsolutePath(), e);
				throw new JargonException(
						"IOException trying to create new file: "
								+ localFileToHoldData.getAbsolutePath(), e);
			}
		}
	}

	/**
	 * Compute a CRC32 checksum for a local file given an absolute path
	 * 
	 * @param absolutePathToLocalFile
	 *            <code>String</code> with absolute local file path under
	 *            scratch (no leading '/')
	 * @return <code>long</code> with the file's checksum value
	 * @throws JargonException
	 */
	public static long computeCRC32FileCheckSumViaAbsolutePath(
			final String absolutePathToLocalFile) throws JargonException {

		FileInputStream file;
		try {
			file = new FileInputStream(absolutePathToLocalFile);
		} catch (FileNotFoundException e1) {
			throw new JargonException(
					"error computing checksum, file not found:"
							+ absolutePathToLocalFile, e1);

		}
		CheckedInputStream check = new CheckedInputStream(file, new CRC32());
		BufferedInputStream in = new BufferedInputStream(check);
		try {
			while (in.read() != -1) {
			}
		} catch (IOException e) {
			throw new JargonException("error computing checksum for file:"
					+ absolutePathToLocalFile, e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// ignore
			}
		}

		return check.getChecksum().getValue();

	}

	/**
	 * Compute an MD5 checksum for a local file given an absolute path
	 * 
	 * @param absolutePathToLocalFile
	 *            <code>String</code> with absolute local file path under
	 *            scratch (no leading '/')
	 * @return <code>byte[]</code> with the file's checksum value
	 * @throws JargonException
	 */
	public static byte[] computeMD5FileCheckSumViaAbsolutePath(
			final String absolutePathToLocalFile) throws JargonException {

		FileInputStream file;
		try {
			file = new FileInputStream(absolutePathToLocalFile);
		} catch (FileNotFoundException e1) {
			throw new JargonException(
					"error computing checksum, file not found:"
							+ absolutePathToLocalFile, e1);

		}

		BufferedInputStream in = new BufferedInputStream(file);
		byte[] buffer = new byte[4096];
		MessageDigest complete;

		try {
			complete = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new JargonException("no such algorithm exception for MD5");
		}

		int numRead;

		try {

			do {
				numRead = in.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);

			return complete.digest();

		} catch (Exception e) {
			throw new JargonException("Error computing MD5 checksum", e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * Given a <code>String</code> representing hex characters (e.g. b1f0a2),
	 * return the actual bytes represented by the hex value
	 * 
	 * @param s
	 *            <code>String</code> with the representation of the hex bytes
	 * @return <code>byte[]</code> with the actual translation
	 */
	public static byte[] hexStringToByteArray(final String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Given an md5 checksum, return a
	 * <code>String</value> as used in iRODS packing instructions
	 * 
	 * @param md5
	 *            <code>byte[]</code> which is an MD5 checksum value
	 * @return <code>String</code> in hex that represents this checkSum
	 */
	public static String md5ByteArrayToString(final byte[] md5) {

		if (md5 == null || md5.length != 16) {
			throw new IllegalArgumentException(
					"unknown format, not recognized as an MD5 checksum in a byte array");
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < 16; i++) {

			sb.append(String.format("%02x", md5[i]));

		}

		return sb.toString();
	}

}
