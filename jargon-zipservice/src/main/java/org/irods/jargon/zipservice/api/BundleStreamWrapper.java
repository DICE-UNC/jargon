/**
 *
 */
package org.irods.jargon.zipservice.api;

import java.io.InputStream;

/**
 * Wraps a stream from a bundle with extra info (such as length)
 *
 * @author Mike Conway - DICE
 *
 */
public class BundleStreamWrapper {

	/**
	 * @param inputStream
	 *            {@link InputStream} from the bundle
	 * @param length
	 *            {@code long} with the length of the bundle
	 * @param bundleFileName
	 *            {@code String} with the name of the bundle file
	 */
	public BundleStreamWrapper(final InputStream inputStream, final long length, final String bundleFileName) {
		super();
		this.inputStream = inputStream;
		this.length = length;
		this.bundleFileName = bundleFileName;
	}

	/**
	 * Underlying input stream to the bundle. Will clean up on close()
	 */
	private final InputStream inputStream;
	/**
	 * Length of the bundle file
	 */
	private final long length;
	/**
	 * Name of the bundle file (last path component, not abs path, useful for
	 * returning to client, and for getting extension/type)
	 */
	private final String bundleFileName;

	/**
	 * @return {@link InputStream} from the bundle
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * @return the length as {@code long}
	 */
	public long getLength() {
		return length;
	}

	/**
	 * @return the bundleFileName as {@code String}
	 */
	public String getBundleFileName() {
		return bundleFileName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BundleStreamWrapper [");
		if (inputStream != null) {
			builder.append("inputStream=").append(inputStream).append(", ");
		}
		builder.append("length=").append(length).append(", ");
		if (bundleFileName != null) {
			builder.append("bundleFileName=").append(bundleFileName);
		}
		builder.append("]");
		return builder.toString();
	}

}
