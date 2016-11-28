/**
 *
 */
package org.irods.jargon.core.transfer;

import java.util.ArrayList;
import java.util.List;

/**
 * iRODS file restart info, analagous to file_restart_info_t
 *
 * This is a representation of the file restart info used for long file restart
 * as in rcPortalOpr.c
 *
 * @author Mike Conway - DICE
 *
 */
public class FileRestartInfo {

	public enum RestartStatus {
		ON, OFF
	}

	public enum RestartType {
		PUT, GET
	}

	/**
	 * String representation of <code>IRODAccount</code>, which is equivalent to
	 * <code>IRODSAccount.toString()</code>. This URI format is easy to
	 * serialize and avoids saving any actual iRODS credentials inadvertantly.
	 */
	private String irodsAccountIdentifier = "";
	private String localAbsolutePath = "";
	private String irodsAbsolutePath = "";
	private RestartStatus restartStatus = RestartStatus.OFF;
	private RestartType restartType = RestartType.PUT;
	/**
	 * Cached count of the number of restart attempts
	 */
	private int numberRestarts = 0;
	private List<FileRestartDataSegment> fileRestartDataSegments = new ArrayList<FileRestartDataSegment>();

	public String getLocalAbsolutePath() {
		return localAbsolutePath;
	}

	public void setLocalAbsolutePath(final String localAbsolutePath) {
		this.localAbsolutePath = localAbsolutePath;
	}

	public String getIrodsAbsolutePath() {
		return irodsAbsolutePath;
	}

	public void setIrodsAbsolutePath(final String irodsAbsolutePath) {
		this.irodsAbsolutePath = irodsAbsolutePath;
	}

	public RestartStatus getRestartStatus() {
		return restartStatus;
	}

	public void setRestartStatus(final RestartStatus restartStatus) {
		this.restartStatus = restartStatus;
	}

	public List<FileRestartDataSegment> getFileRestartDataSegments() {
		return fileRestartDataSegments;
	}

	public void setFileRestartDataSegments(
			final List<FileRestartDataSegment> fileRestartDataSegments) {
		this.fileRestartDataSegments = fileRestartDataSegments;
	}

	/**
	 * Get the identifier associated with this info
	 *
	 * @return {@link FileRestartInfoIdentifier} that points to this info. The
	 *         restart info is keyed by various attributes in hashes, or for
	 *         generating file names
	 */
	public FileRestartInfoIdentifier identifierFromThisInfo() {
		FileRestartInfoIdentifier identifier = new FileRestartInfoIdentifier();
		identifier.setAbsolutePath(irodsAbsolutePath);
		identifier.setIrodsAccountIdentifier(irodsAccountIdentifier);
		identifier.setRestartType(restartType);
		return identifier;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("FileRestartInfo [");
		if (irodsAccountIdentifier != null) {
			builder.append("irodsAccountIdentifier=");
			builder.append(irodsAccountIdentifier);
			builder.append(", ");
		}
		if (localAbsolutePath != null) {
			builder.append("localAbsolutePath=");
			builder.append(localAbsolutePath);
			builder.append(", ");
		}
		if (irodsAbsolutePath != null) {
			builder.append("irodsAbsolutePath=");
			builder.append(irodsAbsolutePath);
			builder.append(", ");
		}
		if (restartStatus != null) {
			builder.append("restartStatus=");
			builder.append(restartStatus);
			builder.append(", ");
		}
		if (restartType != null) {
			builder.append("restartType=");
			builder.append(restartType);
			builder.append(", ");
		}

		builder.append(", numberRestarts=");
		builder.append(numberRestarts);
		builder.append(", ");
		if (fileRestartDataSegments != null) {
			builder.append("fileRestartDataSegments=");
			builder.append(fileRestartDataSegments.subList(0,
					Math.min(fileRestartDataSegments.size(), maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}

	public String getIrodsAccountIdentifier() {
		return irodsAccountIdentifier;
	}

	public void setIrodsAccountIdentifier(final String irodsAccountIdentifier) {
		this.irodsAccountIdentifier = irodsAccountIdentifier;
	}

	public RestartType getRestartType() {
		return restartType;
	}

	public void setRestartType(final RestartType restartType) {
		this.restartType = restartType;
	}

	/**
	 * @return the numberRestarts
	 */
	public int getNumberRestarts() {
		return numberRestarts;
	}

	/**
	 * @param numberRestarts
	 *            the numberRestarts to set
	 */
	public void setNumberRestarts(final int numberRestarts) {
		this.numberRestarts = numberRestarts;
	}

	/**
	 * Get an estimate of the length returned so far, helpful for progress
	 * indicators
	 *
	 * @return <code>long</code> with an estimate of the total transferred so
	 *         far.
	 */
	public long estimateLengthSoFar() {
		long total = 0;
		for (FileRestartDataSegment fileRestartDataSegment : fileRestartDataSegments) {
			total += fileRestartDataSegment.getLength();
		}
		return total;
	}

}
