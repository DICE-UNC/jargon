/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.File;
import java.io.FileFilter;

/**
 * Default IRODS file filter implementation that simply accepts all. Useful
 * mostly for testing and to provide an example.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSAcceptAllFileFilter implements FileFilter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(final File pathname) {
		return true;
	}

}
