/**
 *
 */
package org.irods.jargon.zipservice.api;

import java.io.InputStream;

/**
 * Wraps an imput stream from a bundle with extra info (such as length), useful
 * in
 *
 * @author Mike Conway - DICE
 *
 */
public class BundleStreamWrapper {

	/**
	 * @param inputStream
	 * @param length
	 * @param bundleFileName
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
	 * @return the inputStream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * @return the length
	 */
	public long getLength() {
		return length;
	}

	/**
	 * @return the bundleFileName
	 */
	public String getBundleFileName() {
		return bundleFileName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
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
