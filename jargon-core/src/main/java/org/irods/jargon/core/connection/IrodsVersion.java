/**
 * 
 */
package org.irods.jargon.core.connection;

/**
 * Represents an iRODS version in a structured and comparable way
 * 
 * @author Mike Conway - DFC
 *
 */
public class IrodsVersion implements Comparable<IrodsVersion> {

	public static final String RODS_PREFIX = "rods";
	private final String origVersion;
	private final String majorAsString;
	private final String minorAsString;
	private final String patchAsString;
	private final int major;
	private final int minor;
	private final int patch;

	public IrodsVersion(String reportedVersion) {
		if (reportedVersion == null || reportedVersion.isEmpty()) {
			throw new IllegalArgumentException("null or empty reportedVersion");
		}

		int i = reportedVersion.indexOf(RODS_PREFIX);
		if (i == -1) {
			throw new IllegalArgumentException(
					"reported version is not a valid version string");
		}

		origVersion = reportedVersion.substring(RODS_PREFIX.length());

		String[] tokens = origVersion.split("\\.");
		if (tokens.length == 3 || tokens.length == 2) {
		} else {
			throw new IllegalArgumentException(
					"version is not major.minor.patch");
		}

		majorAsString = tokens[0];
		minorAsString = tokens[1];

		if (tokens.length == 2) {
			patchAsString = "0";
		} else {
			patchAsString = tokens[2];
		}

		major = Integer.parseInt(majorAsString);
		minor = Integer.parseInt(minorAsString);
		patch = Integer.parseInt(patchAsString);

	}

	public String getPatchAsString() {
		return patchAsString;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IrodsVersion [");
		if (origVersion != null) {
			builder.append("origVersion=").append(origVersion).append(", ");
		}
		if (majorAsString != null) {
			builder.append("majorAsString=").append(majorAsString).append(", ");
		}
		if (minorAsString != null) {
			builder.append("minorAsString=").append(minorAsString).append(", ");
		}
		if (patchAsString != null) {
			builder.append("patchAsString=").append(patchAsString).append(", ");
		}
		builder.append("major=").append(major).append(", minor=").append(minor)
				.append(", patch=").append(patch).append("]");
		return builder.toString();
	}

	public String getOrigVersion() {
		return origVersion;
	}

	public String getMajorAsString() {
		return majorAsString;
	}

	public String getMinorAsString() {
		return minorAsString;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getPatch() {
		return patch;
	}

	/**
	 * Convenience method to see if the current version in this instance is at
	 * least the given version in the parameter
	 * 
	 * @param versionString
	 *            <code>String</code> with the rodsx.x.x version number
	 * @return <code>boolean</code>
	 */
	public boolean hasVersionOfAtLeast(final String versionString) {
		return compareTo(new IrodsVersion(versionString)) >= 0;
	}

	/**
	 * Returns a negative integer, zero, or a positive integer as this object is
	 * less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(IrodsVersion otherVersion) {
		if (this.major < otherVersion.major) {
			return -1;
		} else if (this.major > otherVersion.major) {
			return 1;
		}
		// major equal do minor
		if (this.minor < otherVersion.minor) {
			return -1;
		} else if (this.minor > otherVersion.minor) {
			return 1;
		}
		// minor equal do patch
		if (this.patch < otherVersion.patch) {
			return -1;
		} else if (this.patch > otherVersion.patch) {
			return 1;
		}

		return 0;

	}

}
