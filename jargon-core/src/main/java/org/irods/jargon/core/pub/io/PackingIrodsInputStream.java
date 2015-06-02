/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.io.FileNotFoundException;

/**
 * Wrap an iRODS input stream in an accumulating buffer that will emulate reads
 * from a continuous stream while fetching chunks from iRODS in a more optimal
 * size
 * 
 * @author Mike Conway - DICE
 *
 */
public class PackingIrodsInputStream extends IRODSFileInputStream {

	/**
	 * @param irodsFile
	 * @param fileIOOperations
	 * @throws FileNotFoundException
	 */
	public PackingIrodsInputStream(IRODSFile irodsFile,
			FileIOOperations fileIOOperations) throws FileNotFoundException {
		super(irodsFile, fileIOOperations);
	}

	/**
	 * @param irodsFile
	 * @param fileIOOperations
	 * @param fd
	 * @throws FileNotFoundException
	 */
	public PackingIrodsInputStream(IRODSFile irodsFile,
			FileIOOperations fileIOOperations, int fd)
			throws FileNotFoundException {
		super(irodsFile, fileIOOperations, fd);
	}

}
