/**
 * 
 */
package org.irods.jargon.core.packinstr;

/**
 * Options to control checksum processing
 * 
 * @author Mike Conway - DICE
 * 
 */
public class ChecksumOptions {

	/**
	 * Force the checksum operation, even if a previous checksum was registered
	 * in iCAT
	 */
	private boolean force = false;
	/**
	 * Do a checksum on every replica
	 */
	private boolean checksumAllReplicas = true;

	/**
	 * If the checksum does not exist in the iCAT, compute and store it
	 */
	private boolean verifyChecksumInIcat = true;

	/**
	 * @return the force
	 */
	public boolean isForce() {
		return force;
	}

	/**
	 * @param force
	 *            the force to set
	 */
	public void setForce(boolean force) {
		this.force = force;
	}

	/**
	 * @return the checksumAllReplicas
	 */
	public boolean isChecksumAllReplicas() {
		return checksumAllReplicas;
	}

	/**
	 * @param checksumAllReplicas
	 *            the checksumAllReplicas to set
	 */
	public void setChecksumAllReplicas(boolean checksumAllReplicas) {
		this.checksumAllReplicas = checksumAllReplicas;
	}

	/**
	 * @return the verifyChecksumInIcat
	 */
	public boolean isVerifyChecksumInIcat() {
		return verifyChecksumInIcat;
	}

	/**
	 * @param verifyChecksumInIcat
	 *            the verifyChecksumInIcat to set
	 */
	public void setVerifyChecksumInIcat(boolean verifyChecksumInIcat) {
		this.verifyChecksumInIcat = verifyChecksumInIcat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChecksumOptions [force=");
		builder.append(force);
		builder.append(", checksumAllReplicas=");
		builder.append(checksumAllReplicas);
		builder.append(", verifyChecksumInIcat=");
		builder.append(verifyChecksumInIcat);
		builder.append("]");
		return builder.toString();
	}

}
