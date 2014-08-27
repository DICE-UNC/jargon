/**
 * 
 */
package org.irods.jargon.core.checksum;

import java.io.FileNotFoundException;

import org.irods.jargon.core.exception.JargonException;

/**
 * Abstract base class for classes that compute and return a checksum based on
 * some algorithm, based on as Strategy pattern
 * 
 * @author Mike Conway - DICE
 * 
 */
public abstract class AbstractChecksumComputeStrategy {

	/**
	 * 
	 * @param localFileAbsolutePath
	 *            <code>String</code> with the absolute path to a local file
	 * @return <code>String</code> with a checksum in digest (string) form.
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	public abstract String instanceChecksumForPackingInstruction(
			final String localFileAbsolutePath) throws FileNotFoundException,
			JargonException;

}
