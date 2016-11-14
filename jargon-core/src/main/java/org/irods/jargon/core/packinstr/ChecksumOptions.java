/**
 *
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;

/**
 * Options to control checksum processing
 *
 * @author Mike Conway - DICE
 *
 */
public class ChecksumOptions {

	private ChecksumEncodingEnum checksumEncodingEnum = ChecksumEncodingEnum.DEFAULT;
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
	 * Checksum all files in a collection, including subtrees
	 */
	private boolean recursive = false;

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
	public void setForce(final boolean force) {
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
	public void setChecksumAllReplicas(final boolean checksumAllReplicas) {
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
	public void setVerifyChecksumInIcat(final boolean verifyChecksumInIcat) {
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
		builder.append("ChecksumOptions [");
		if (checksumEncodingEnum != null) {
			builder.append("checksumEncodingEnum=");
			builder.append(checksumEncodingEnum);
			builder.append(", ");
		}
		builder.append("force=");
		builder.append(force);
		builder.append(", checksumAllReplicas=");
		builder.append(checksumAllReplicas);
		builder.append(", verifyChecksumInIcat=");
		builder.append(verifyChecksumInIcat);
		builder.append(", recursive=");
		builder.append(recursive);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the checksumEncodingEnum
	 */
	protected ChecksumEncodingEnum getChecksumEncodingEnum() {
		return checksumEncodingEnum;
	}

	/**
	 * @param checksumEncodingEnum
	 *            the checksumEncodingEnum to set
	 */
	protected void setChecksumEncodingEnum(
			final ChecksumEncodingEnum checksumEncodingEnum) {
		this.checksumEncodingEnum = checksumEncodingEnum;
	}

	/**
	 * @return the recursive
	 */
	protected boolean isRecursive() {
		return recursive;
	}

	/**
	 * @param recursive
	 *            the recursive to set
	 */
	protected void setRecursive(final boolean recursive) {
		this.recursive = recursive;
	}

}
