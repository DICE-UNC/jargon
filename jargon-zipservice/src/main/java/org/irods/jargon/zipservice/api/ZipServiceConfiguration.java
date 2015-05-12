/**
 * 
 */
package org.irods.jargon.zipservice.api;

/**
 * Basic configuration for the zip service.
 * <p/>
 * <b>Current assumptions for first round of service</b>
 * 
 * <ul>
 * <li>Bundles will be subdirectories under the bundleSubdirPath</li>
 * *
 * <li>The bundleSubdirPath will be under the logged-in user home</li>
 * <li>A bundle is created as a directory under that bundle path with the
 * bundlePrefix and a time stamp</li>
 * <li>The resulting bundle is underneath the bundleSubDirPath at the same level
 * as the child dir that was bundled up</li>
 * 
 * </ul>
 * 
 * 
 * 
 * @author Mike Conway - DICE
 *
 */
public class ZipServiceConfiguration {
	/**
	 * Maximum total bytes that can be processed by this service
	 */
	private long maxTotalBytesForZip = 1 * 1024 * 1024 * 1024; // default to 1GB
	/**
	 * Treat the temp bundle creation dir as a relative path in user home
	 * currently defaults to true
	 */
	private final boolean generateTempDirInUserHome = true;
	/**
	 * The subdir that will be used under the bundle directory for creating
	 * child bundle folders
	 */
	private String bundleSubDirPath = ".jargonZipService";
	/**
	 * The prefix for bundle service temporary directories and resulting bundles
	 */
	private String bundlePrefix = "zipServiceBundle";

	/**
	 * On an error with an individual file, fail fast, or, if <code>false</code>
	 */
	private boolean failFast = true;

	/**
	 * @return the maxTotalBytesForZip
	 */
	public long getMaxTotalBytesForZip() {
		return maxTotalBytesForZip;
	}

	/**
	 * @param maxTotalBytesForZip
	 *            the maxTotalBytesForZip to set
	 */
	public void setMaxTotalBytesForZip(long maxTotalBytesForZip) {
		this.maxTotalBytesForZip = maxTotalBytesForZip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ZipServiceConfiguration [maxTotalBytesForZip=")
				.append(maxTotalBytesForZip)
				.append(", generateTempDirInUserHome=")
				.append(generateTempDirInUserHome).append(", ");
		if (bundleSubDirPath != null) {
			builder.append("bundleSubDirPath=").append(bundleSubDirPath)
					.append(", ");
		}
		if (bundlePrefix != null) {
			builder.append("bundlePrefix=").append(bundlePrefix).append(", ");
		}
		builder.append("failFast=").append(failFast).append("]");
		return builder.toString();
	}

	public boolean isGenerateTempDirInUserHome() {
		return generateTempDirInUserHome;
	}

	/*
	 * temporarily this is not settable public void
	 * setGenerateTempDirInUserHome(boolean generateTempDirInUserHome) {
	 * this.generateTempDirInUserHome = generateTempDirInUserHome; }
	 */

	/**
	 * @return the bundleSubDirPath
	 */
	public String getBundleSubDirPath() {
		return bundleSubDirPath;
	}

	/**
	 * @param bundleSubDirPath
	 *            the bundleSubDirPath to set
	 */
	public void setBundleSubDirPath(String bundleSubDirPath) {
		this.bundleSubDirPath = bundleSubDirPath;
	}

	/**
	 * @return the bundlePrefix
	 */
	public String getBundlePrefix() {
		return bundlePrefix;
	}

	/**
	 * @param bundlePrefix
	 *            the bundlePrefix to set
	 */
	public void setBundlePrefix(String bundlePrefix) {
		this.bundlePrefix = bundlePrefix;
	}

	/**
	 * @return the failFast
	 */
	public boolean isFailFast() {
		return failFast;
	}

	/**
	 * @param failFast
	 *            the failFast to set
	 */
	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}

}
