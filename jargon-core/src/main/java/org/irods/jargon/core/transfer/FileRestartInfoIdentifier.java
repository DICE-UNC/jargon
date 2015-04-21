/**
 * 
 */
package org.irods.jargon.core.transfer;

import org.irods.jargon.core.transfer.FileRestartInfo.RestartType;

/**
 * Represents a hashable key for file restart information. This is the key that
 * will be used to store the restart file
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FileRestartInfoIdentifier {

	public static FileRestartInfoIdentifier instanceFromFileRestartInfo(
			final FileRestartInfo fileRestartInfo) {
		if (fileRestartInfo == null) {
			throw new IllegalArgumentException("null fileRestartInfo");
		}

		FileRestartInfoIdentifier identifier = new FileRestartInfoIdentifier();
		identifier.setAbsolutePath(fileRestartInfo.getIrodsAbsolutePath());
		identifier.setIrodsAccountIdentifier(fileRestartInfo
				.getIrodsAccountIdentifier());
		identifier.setRestartType(fileRestartInfo.getRestartType());
		return identifier;
	}

	/**
	 * Account used for the transfer
	 */
	private String irodsAccountIdentifier;
	/**
	 * Type of operation (get versus put)
	 */
	private RestartType restartType = RestartType.PUT;

	/**
	 * Absolute path of the source of the file (put = local file name, get =
	 * irodsFilename)
	 */
	private String absolutePath = "";

	public FileRestartInfoIdentifier() {
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

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(final String absolutePath) {
		this.absolutePath = absolutePath;
	}

	@Override
	public boolean equals(final Object obj) {

		if (!(obj instanceof FileRestartInfoIdentifier)) {
			return false;
		}

		FileRestartInfoIdentifier other = (FileRestartInfoIdentifier) obj;
		return (absolutePath.equals(other.getAbsolutePath())
				&& irodsAccountIdentifier.equals(other
						.getIrodsAccountIdentifier()) && getRestartType() == other
					.getRestartType());

	}

	@Override
	public int hashCode() {
		return absolutePath.hashCode() + irodsAccountIdentifier.hashCode()
				+ restartType.hashCode();
	}
}
