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

	public void setIrodsAccountIdentifier(String irodsAccountIdentifier) {
		this.irodsAccountIdentifier = irodsAccountIdentifier;
	}

	public RestartType getRestartType() {
		return restartType;
	}

	public void setRestartType(RestartType restartType) {
		this.restartType = restartType;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof FileRestartInfoIdentifier)) {
			return false;
		}

		FileRestartInfoIdentifier other = (FileRestartInfoIdentifier) obj;
		return (this.absolutePath.equals(other.getAbsolutePath())
				&& this.irodsAccountIdentifier.equals(other
						.getIrodsAccountIdentifier()) && this.getRestartType() == other
				.getRestartType());

	}

	@Override
	public int hashCode() {
		return this.absolutePath.hashCode()
				+ this.irodsAccountIdentifier.hashCode()
				+ this.restartType.hashCode();
	}
}
