/**
 * 
 */
package org.irods.jargon.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

	/**
	 * private constructor, this is not meant to be an instantiated class.
	 */
	private LocalFileUtils() {

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
	 * Compute a checksum for a local file given an absolute path
	 * 
	 * @param absolutePathToLocalFile
	 *            <code>String</code> with absolute local file path under
	 *            scratch (no leading '/')
	 * @return <code>long</code> with the file's checksum value
	 * @throws JargonException
	 */
	public static long computeFileCheckSumViaAbsolutePath(
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

}
