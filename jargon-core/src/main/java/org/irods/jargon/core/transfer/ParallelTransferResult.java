package org.irods.jargon.core.transfer;

/**
 * represents a return value from a parallel transfer operation. Currently, this
 * is a place holder, but could grow to include performance stats, etc.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ParallelTransferResult {
	Exception transferException = null;

	/**
	 * @return the transferException
	 */
	public Exception getTransferException() {
		return transferException;
	}

}
