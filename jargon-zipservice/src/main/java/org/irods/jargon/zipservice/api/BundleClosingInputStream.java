/**
 *
 */
package org.irods.jargon.zipservice.api;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.irods.jargon.core.pub.io.FileIOOperations;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;

/**
 * @author Mike Conway - DICE Input stream subclass that will do cleanup and
 *         closing
 *
 */
public class BundleClosingInputStream extends IRODSFileInputStream {

	public BundleClosingInputStream(final IRODSFile irodsFile, final FileIOOperations fileIOOperations)
			throws FileNotFoundException {
		super(irodsFile, fileIOOperations);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFileInputStream#close()
	 */
	@Override
	public void close() throws IOException {
		super.close();
		// delete the bundle, the temp files are deleted when the bundle was
		// created
		super.getIrodsFile().deleteWithForceOption();
	}
}
