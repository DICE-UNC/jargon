/**
 * 
 */
package org.irods.jargon.core.utils;

import java.io.File;

/**
 * Utilities for working with the local file system.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class LocalFileUtils {

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

}
