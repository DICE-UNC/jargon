/**
 * 
 */
package org.irods.jargon.datautils.connectiontester;

/**
 * 
 * configuration for the connection tester specifying details for the test
 * 
 * @author Mike Conway - DICE
 * 
 * 
 */
public class ConnectionTesterConfiguration {

	/**
	 * Local source path for directory under which test files are created, put,
	 * and gotten
	 */
	private String localSourceParentDirectory = "";

	/**
	 * Complementary iRODS directory that is the target of the test
	 */
	private String irodsParentDirectory = "";

	/**
	 * Delete the contents of the source and target after the test
	 */
	private boolean cleanupOnCompletion = true;

	/**
	 * 
	 */
	public ConnectionTesterConfiguration() {
	}

	/**
	 * @return the localSourceParentDirectory
	 */
	public String getLocalSourceParentDirectory() {
		return localSourceParentDirectory;
	}

	/**
	 * @param localSourceParentDirectory
	 *            the localSourceParentDirectory to set
	 */
	public void setLocalSourceParentDirectory(String localSourceParentDirectory) {
		this.localSourceParentDirectory = localSourceParentDirectory;
	}

	/**
	 * @return the irodsParentDirectory
	 */
	public String getIrodsParentDirectory() {
		return irodsParentDirectory;
	}

	/**
	 * @param irodsParentDirectory
	 *            the irodsParentDirectory to set
	 */
	public void setIrodsParentDirectory(String irodsParentDirectory) {
		this.irodsParentDirectory = irodsParentDirectory;
	}

	/**
	 * @return the cleanupOnCompletion
	 */
	public boolean isCleanupOnCompletion() {
		return cleanupOnCompletion;
	}

	/**
	 * @param cleanupOnCompletion
	 *            the cleanupOnCompletion to set
	 */
	public void setCleanupOnCompletion(boolean cleanupOnCompletion) {
		this.cleanupOnCompletion = cleanupOnCompletion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConnectionTesterConfiguration [");
		if (localSourceParentDirectory != null) {
			builder.append("localSourceParentDirectory=");
			builder.append(localSourceParentDirectory);
			builder.append(", ");
		}
		if (irodsParentDirectory != null) {
			builder.append("irodsParentDirectory=");
			builder.append(irodsParentDirectory);
			builder.append(", ");
		}
		builder.append("cleanupOnCompletion=");
		builder.append(cleanupOnCompletion);
		builder.append("]");
		return builder.toString();
	}

}
