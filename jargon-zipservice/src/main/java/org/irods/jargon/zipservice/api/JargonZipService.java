package org.irods.jargon.zipservice.api;

import java.io.InputStream;
import java.util.List;

import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.zipservice.api.exception.ZipServiceException;

/**
 * Interface to a service to bundle up groups of files or collections and obtain
 * handles of various sources. This is a useful service for interfaces.
 * 
 * @author Mike Conway - DICE
 *
 */
public interface JargonZipService {

	/**
	 * @return the zipServiceConfiguration
	 */
	public abstract ZipServiceConfiguration getZipServiceConfiguration();

	/**
	 * @param zipServiceConfiguration
	 *            the zipServiceConfiguration to set
	 */
	public abstract void setZipServiceConfiguration(
			ZipServiceConfiguration zipServiceConfiguration);

	/**
	 * Given a list of iRODS paths, obtain a bundle as an iRODS file that
	 * represents the files at those paths.
	 * <p/>
	 * Note that bundle type, failure modes, etc are all configured in the
	 * {@link ZipServiceConfiguration}. Any temporary directories will be
	 * cleaned up, and it is up to the caller to delete the bundle when done.
	 * 
	 * @param irodsAbsolutePaths
	 *            <code>List<String></code> of iRODS paths
	 * @return {@link IRODSFile} with the reference to the bundle
	 * @throws ZipServiceException
	 */
	public abstract IRODSFile obtainBundleAsIrodsFileGivenPaths(
			List<String> irodsAbsolutePaths) throws ZipServiceException;

	/**
	 * Get the estimated size of the bundle given the provided paths
	 * 
	 * @param irodsAbsolutePaths
	 *            <code>List<String></code> of iRODS paths
	 * @return <code>long</code> with the length of the
	 * @throws ZipServiceException
	 */
	public abstract long computeBundleSizeInBytes(
			List<String> irodsAbsolutePaths) throws ZipServiceException;

	/**
	 * Given a list of iRODS paths, obtain a bundle as an iRODS file that
	 * represents the files at those paths, represented by an input stream
	 * <p/>
	 * Note that bundle type, failure modes, etc are all configured in the
	 * {@link ZipServiceConfiguration}. Note that closing the stream will cause
	 * the bundle and any temporary files to be cleaned up.
	 * 
	 * @param irodsAbsolutePaths
	 *            <code>List<String></code> of iRODS paths
	 * @return {@link InputStream} with the reference to the bundle
	 * @throws ZipServiceException
	 */
	InputStream obtainBundleAsInputStreamGivenPaths(
			List<String> irodsAbsolutePaths) throws ZipServiceException;

	/**
	 * Given a list of iRODS paths, obtain a bundle as an iRODS file that
	 * represents the files at those paths, represented by an input stream,
	 * wrapped in an object that contains data about the length of the file and
	 * its actual name.
	 * <p/>
	 * Note that bundle type, failure modes, etc are all configured in the
	 * {@link ZipServiceConfiguration}. Note that closing the stream will cause
	 * the bundle and any temporary files to be cleaned up.
	 * 
	 * @param irodsAbsolutePaths
	 *            <code>List<String></code> of iRODS paths
	 * @return {@link InputStream} with the reference to the bundle
	 * @throws ZipServiceException
	 */
	BundleStreamWrapper obtainBundleAsInputStreamWithAdditionalMetadataGivenPaths(
			List<String> irodsAbsolutePaths) throws ZipServiceException;

}