/**
 *
 */
package org.irods.jargon.core.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.RuleProcessingAOImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for working with the local file system.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class LocalFileUtils {

	public static final Logger log = LoggerFactory.getLogger(LocalFileUtils.class);

	/**
	 * private constructor, this is not meant to be an instantiated class.
	 */
	private LocalFileUtils() {

	}

	/**
	 * Return the contents of a file as a string
	 * 
	 * @param file
	 *            {@link File} to read
	 * @return <code>String</code> with the file contents
	 * @throws IOException
	 */
	public static String fileContentsAsString(final File file) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("null file");
		}
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		final StringBuilder stringBuilder = new StringBuilder();
		final String ls = System.getProperty("line.separator");

		try {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}

			return stringBuilder.toString();
		} finally {
			reader.close();
		}
	}

	/**
	 * Parse a file name to get the stuff after the last '.' character to treat
	 * as the file extension
	 *
	 * @param fileName
	 *            <code>String</code> with the file name to parse out.
	 * @return <code>String</code> with the file extension
	 */
	public static String getFileExtension(final String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null fileName");
		}

		final int lastDot = fileName.lastIndexOf('.');
		if (lastDot == -1) {
			return "";
		} else {
			return (fileName.substring(lastDot));
		}

	}

	/**
	 * Normalize any Windows paths from \ separators to / separators
	 *
	 * @param inPath
	 * @return
	 */
	public static String normalizePath(final String inPath) {

		if (inPath == null) {
			throw new IllegalArgumentException("null inPath");
		}

		// if not windows path dont bother
		if (File.separatorChar != '\\') {
			return inPath;
		}

		return inPath.replaceAll("\\\\", "/");
	}

	/**
	 * Parse a file name to get the stuff before last '.' character to treat as
	 * the file name
	 *
	 * @param fileName
	 *            <code>String</code> with the file name to parse out.
	 * @return <code>String</code> with the file name before the extension,
	 *         without the '.'
	 */
	public static String getFileNameUpToExtension(final String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null fileName");
		}

		final int lastDot = fileName.lastIndexOf('.');
		if (lastDot == -1) {
			return "";
		} else {
			return (fileName.substring(0, lastDot));
		}

	}

	/**
	 * Interpose a time stamp between the file name and extension
	 *
	 * @param fileName
	 *            <code>String</code> with the file name to parse out
	 * @return <code>String</code> with the updated file name containing a time
	 *         stamp
	 */
	public static String getFileNameWithTimeStampInterposed(final String fileName) {
		final String namePart = getFileNameUpToExtension(fileName);
		final String extension = getFileExtension(fileName);
		final StringBuilder newName = new StringBuilder(namePart);
		newName.append(".[backup from - ");
		final DateFormat dateFormat = DateFormat.getDateTimeInstance();
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

		if (directory == null) {
			throw new IllegalArgumentException("null directory");
		}

		int count = 0;

		if (directory.isFile()) {
			count = 1;
		} else {
			final File[] files = directory.listFiles();
			if (files != null) {
				for (final File file : files) {
					if (file.isFile()) {
						count++;
					}
					if (file.isDirectory()) {
						count += countFilesInDirectory(file);
					}
				}
			}
		}
		return count;
	}

	/**
	 * @param localFileToHoldData
	 * @throws JargonException
	 */
	public static void createLocalFileIfNotExists(final File localFileToHoldData) throws JargonException {
		if (localFileToHoldData.exists()) {
			log.info("local file exists, will not create the local file for {}", localFileToHoldData.getAbsolutePath());
		} else {
			log.info("local file does not exist, will attempt to create local file: {}",
					localFileToHoldData.getAbsolutePath());
			try {
				localFileToHoldData.createNewFile();
			} catch (final IOException e) {
				log.error("IOException when trying to create a new file for the local output stream for {}",
						localFileToHoldData.getAbsolutePath(), e);
				throw new JargonException(
						"IOException trying to create new file: " + localFileToHoldData.getAbsolutePath(), e);
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
	public static long computeCRC32FileCheckSumViaAbsolutePath(final String absolutePathToLocalFile)
			throws JargonException {

		FileInputStream file;
		try {
			file = new FileInputStream(absolutePathToLocalFile);
		} catch (final FileNotFoundException e1) {
			throw new JargonException("error computing checksum, file not found:" + absolutePathToLocalFile, e1);

		}
		final CheckedInputStream check = new CheckedInputStream(file, new CRC32());
		final BufferedInputStream in = new BufferedInputStream(check);
		try {
			while (in.read() != -1) {
			}
		} catch (final IOException e) {
			throw new JargonException("error computing checksum for file:" + absolutePathToLocalFile, e);
		} finally {
			try {
				in.close();
			} catch (final IOException e) {
				// ignore
			}
		}

		return check.getChecksum().getValue();

	}

	/**
	 * Compute an SHA-256 checksum for a local file given an absolute path
	 *
	 * @param absolutePathToLocalFile
	 *            <code>String</code> with absolute local file path under
	 *            scratch (no leading '/')
	 * @return <code>byte[]</code> with the file's checksum value
	 * @throws JargonException
	 */
	public static byte[] computeSHA256FileCheckSumViaAbsolutePath(final String absolutePathToLocalFile)
			throws JargonException {

		log.info("computeSHA256FileCheckSumViaAbsolutePath()");

		if (absolutePathToLocalFile == null || absolutePathToLocalFile.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePathToLocalFile");
		}

		FileInputStream file;
		try {
			file = new FileInputStream(absolutePathToLocalFile);
		} catch (final FileNotFoundException e1) {
			throw new JargonException("error computing checksum, file not found:" + absolutePathToLocalFile, e1);

		}

		MessageDigest complete;
		int numRead;
		final BufferedInputStream in = new BufferedInputStream(file);
		final byte[] buffer = new byte[4096];

		try {
			complete = MessageDigest.getInstance("SHA-256");
			do {
				numRead = in.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);

			return complete.digest();
		} catch (final NoSuchAlgorithmException e) {
			throw new JargonException("no such algorithm exception for MD5");
		} catch (final Exception e) {
			throw new JargonException("Error computing MD5 checksum", e);
		} finally {
			try {
				in.close();
			} catch (final IOException e) {
				// ignore
			}
			try {
				file.close();
			} catch (final IOException e) {
				// ignore
			}
		}
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
	public static byte[] computeMD5FileCheckSumViaAbsolutePath(final String absolutePathToLocalFile)
			throws JargonException {

		FileInputStream file;
		try {
			file = new FileInputStream(absolutePathToLocalFile);
		} catch (final FileNotFoundException e1) {
			throw new JargonException("error computing checksum, file not found:" + absolutePathToLocalFile, e1);

		}

		MessageDigest complete;
		int numRead;
		final BufferedInputStream in = new BufferedInputStream(file);
		final byte[] buffer = new byte[4096];

		try {
			complete = MessageDigest.getInstance("MD5");
			do {
				numRead = in.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);

			return complete.digest();
		} catch (final NoSuchAlgorithmException e) {
			throw new JargonException("no such algorithm exception for MD5");
		} catch (final Exception e) {
			throw new JargonException("Error computing MD5 checksum", e);
		} finally {
			try {
				in.close();
			} catch (final IOException e) {
				// ignore
			}
			try {
				file.close();
			} catch (final IOException e) {
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
		final int len = s.length();
		final byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Given a checksum digest as a <code>byte[]</code>, return a
	 * <code>String</value> as used in iRODS packing instructions
	 *
	 * &#64;param digestAsByteArray
	 *            <code>byte[]</code> which is a checksum value
	 * 
	 * @return <code>String</code> in hex that represents this checkSum
	 */
	public static String digestByteArrayToString(final byte[] digestAsByteArray) {

		if (digestAsByteArray == null) {
			throw new IllegalArgumentException("unknown format, not recognized as an MD5 checksum in a byte array");
		}

		final StringBuilder sb = new StringBuilder();

		for (final byte element : digestAsByteArray) {
			sb.append(String.format("%02x", element));
		}

		return sb.toString();
	}

	/**
	 * Given a path to a classpath resoruce, return that resource as a
	 * <code>File</code>
	 *
	 * @param resourcePath
	 *            <code>String</code> with an absolute path to a resource in the
	 *            classpath
	 * @return <code>File</code> representing the resource in the classpath
	 * @throws JargonException
	 */
	public static File getClasspathResourceAsFile(final String resourcePath) throws JargonException {

		if (resourcePath == null || resourcePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourcePath");
		}
		// Load the directory as a resource
		final URL resourceUrl = LocalFileUtils.class.getResource(resourcePath);

		if (resourceUrl == null) {
			throw new JargonException("null resource, cannot find file");
		}

		// Turn the resource into a File object
		try {
			final File resourceFile = new File(resourceUrl.toURI());
			if (!resourceFile.exists()) {
				throw new JargonException("resource file does not exist");
			}
			return resourceFile;
		} catch (final URISyntaxException e) {
			throw new JargonException("unable to create uri from file path");
		}
	}

	/**
	 * Given a path to a classpath resource, return that resource data as a
	 * string
	 *
	 * @param resourcePath
	 *            <code>String</code> for a classpath resource
	 * @return <code>String</code> with the String value of that resource data
	 * @throws JargonException
	 */
	public static String getClasspathResourceFileAsString(final String resourcePath) throws JargonException {

		if (resourcePath == null || resourcePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourcePath");
		}

		final InputStreamReader resourceReader = new InputStreamReader(
				new BufferedInputStream(RuleProcessingAOImpl.class.getResourceAsStream(resourcePath)));

		StringWriter writer = null;
		String ruleString = null;

		try {
			writer = new StringWriter();
			final char[] buff = new char[1024];
			int i = 0;
			while ((i = resourceReader.read(buff)) > -1) {
				writer.write(buff, 0, i);
			}

			ruleString = writer.toString();

		} catch (final IOException ioe) {
			log.error("io exception reading rule data from resource", ioe);
			throw new JargonException("error reading rule from resource", ioe);
		} finally {
			try {
				resourceReader.close();
				if (writer != null) {
					writer.close();
				}
			} catch (final IOException e) {
				// ignore
			}

		}
		return ruleString;
	}

	/**
	 * Read the contents of a file into a byte array (be carefule not to read
	 * big files!)
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] getBytesFromFile(final File file) throws IOException {
		final InputStream is = new FileInputStream(file);
		byte[] bytes;

		try {
			// Get the size of the file
			final long length = file.length();

			// You cannot create an array using a long type.
			// It needs to be an int type.
			// Before converting to an int type, check
			// to ensure that file is not larger than Integer.MAX_VALUE.
			if (length > Integer.MAX_VALUE) {
				// File is too large (>2GB)
			}

			// Create the byte array to hold the data
			bytes = new byte[(int) length];

			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file " + file.getName());
			}
		} finally {
			// Close the input stream and return bytes
			is.close();
		}
		return bytes;
	}

}
