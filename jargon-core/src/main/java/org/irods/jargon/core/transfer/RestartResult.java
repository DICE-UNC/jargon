/**
 *
 */
package org.irods.jargon.core.transfer;

/**
 * Result of restart processing for a file.
 *
 * @author Mike Conway - DICE
 *
 */
public class RestartResult {

	private boolean restarted = false;

	/**
	 * @return the restarted
	 */
	public boolean isRestarted() {
		return restarted;
	}

	/**
	 * @param restarted
	 *            the restarted to set
	 */
	public void setRestarted(final boolean restarted) {
		this.restarted = restarted;
	}

}
