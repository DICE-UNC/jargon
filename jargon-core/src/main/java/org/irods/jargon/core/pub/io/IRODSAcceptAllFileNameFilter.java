/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.File;
import java.io.FilenameFilter;

/**
 * IRODS specific implementation of standard <code>java.io.FilenameFilter</code>
 * . This default filter simply accepts all files.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSAcceptAllFileNameFilter implements FilenameFilter {

	public IRODSAcceptAllFileNameFilter() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	@Override
	public boolean accept(final File dir, final String name) {
		return true;
	}

}
