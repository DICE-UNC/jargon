/**
 * 
 */
package org.irods.jargon.core.utils;

import java.io.File;
import java.io.IOException;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataObjectAOImpl;
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

}
