/**
 * 
 */
package org.irods.jargon.zipservice.api;

import java.io.InputStream;
import java.util.List;
import java.util.Random;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.zipservice.api.exception.ZipServiceConfigurationException;
import org.irods.jargon.zipservice.api.exception.ZipServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract service to handle zipping and transferring a set of iRODS paths as
 * one bundle
 * 
 * @author Mike Conway - DICE
 *
 */
public class JargonZipService extends AbstractJargonService {

	private ZipServiceConfiguration zipServiceConfiguration = null;
	private final Random random;

	public static final Logger log = LoggerFactory
			.getLogger(JargonZipService.class);

	public JargonZipService() {
		super();
		this.random = new Random();
	}

	public JargonZipService(
			final ZipServiceConfiguration zipServiceConfiguration,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
		if (zipServiceConfiguration == null) {
			throw new IllegalArgumentException("null zipServiceConfiguration");
		}
		this.zipServiceConfiguration = zipServiceConfiguration;
		this.random = new Random();

	}

	/**
	 * @return the zipServiceConfiguration
	 */
	public ZipServiceConfiguration getZipServiceConfiguration() {
		return zipServiceConfiguration;
	}

	/**
	 * @param zipServiceConfiguration
	 *            the zipServiceConfiguration to set
	 */
	public void setZipServiceConfiguration(
			ZipServiceConfiguration zipServiceConfiguration) {
		this.zipServiceConfiguration = zipServiceConfiguration;
	}

	/**
	 * Handy method to verify configuration of this service before any
	 * opeerations
	 * 
	 * @throws ZipServiceConfigurationException
	 */
	protected void validateConfiguration()
			throws ZipServiceConfigurationException {
		if (zipServiceConfiguration == null) {
			throw new ZipServiceConfigurationException(
					"null zipServiceConfiguration, needs to be set before operations");
		}
	}

	public InputStream obtainInputStreamForBundleGivenPaths(
			final List<String> irodsAbsolutePaths) throws ZipServiceException {

		log.info("obtainInputStreamForBundleGivenPaths()");
		if (irodsAbsolutePaths == null) {
			throw new IllegalArgumentException("null irodsAbsolutePaths");
		}
		if (irodsAbsolutePaths.isEmpty()) {
			throw new ZipServiceException("empty zip request");
		}
		validateConfiguration();

		long totalLength = computeBundleSizeInBytes(irodsAbsolutePaths);

		log.info("computed total size:{}", totalLength);

		if (totalLength > this.getZipServiceConfiguration()
				.getMaxTotalBytesForZip()) {
			log.error(
					"bundle total size is:{} and is larger than the configured maximum",
					totalLength);
			throw new ZipRequestTooLargeException(
					"total size is larger than configured bundle max");
		}

		log.info("copy phase, building a local subdir");

		log.info("building in user home");
		String parentPath = createOrReturnZipParentPath();
		log.info("parent path:{}", parentPath);
		long currentTime = System.currentTimeMillis();
		IRODSFile bundleParent = createOrReturnBundlePath(currentTime,
				parentPath);

		log.info("begin the copy phase...");
		copyFilesToBundleDir(irodsAbsolutePaths, bundleParent);
		log.info("create the bundle...");

	}

	/**
	 * Copy files from the disparate paths to the bundle dir
	 * 
	 * @param irodsAbsolutePaths
	 *            <code>List<String></code> with the paths that will be copied
	 *            to the bundle directory
	 * @param bundleParent
	 *            <code>String</code> with the absolute path of the bundle
	 *            directory
	 * @throws ZipServiceException
	 *             for any exception that occurs, there are subclasses of this
	 *             top-level exception that can be discerened
	 */
	private void copyFilesToBundleDir(final List<String> irodsAbsolutePaths,
			IRODSFile bundleParent) throws ZipServiceException {
		// now bundle parent exists and is blank, start the copies
		log.info("starting copies, may take a while...");
		DataTransferOperations dataTransferOperations;
		try {
			dataTransferOperations = this.getIrodsAccessObjectFactory()
					.getDataTransferOperations(getIrodsAccount());
		} catch (JargonException e) {
			log.error("JargonException getting data transfer operations:{}", e);
			throw new ZipServiceException(
					"Jargon exception getting data transfer operations", e);
		}

		for (String path : irodsAbsolutePaths) {
			log.info("copy {}", path);
			try {
				dataTransferOperations.copy(path, "",
						bundleParent.getAbsolutePath(), null, null);
			} catch (JargonException e) {
				if (this.zipServiceConfiguration.isFailFast()) {
					log.error(
							"JargonException copying to bundle dir for:{}, fail fast is set",
							path, e);
					throw new ZipServiceException(
							"Unable to copy a file into the bundle dir - fail fast is set",
							e);
				} else {
					log.warn(
							"JargonException copying to bundle dir for:{}, fail fast is not set, so we will proceed",
							path, e);
				}
			}
		}
	}

	/**
	 * Given a time stamp and parent path, make sure an initialized (created but
	 * empty) bundle path exists
	 * <p/>
	 * Default algo is configured prefix + time stamp + a random int
	 * 
	 * @param currentTime
	 *            <code>long</code> with the timestamp of the request, used to
	 *            differentiate the files
	 * @param parentPath
	 *            <code>String</code> with the absolute path to the main parent
	 *            of bundle files in general
	 * @return <code>String</code> with the absolute path to the parent for this
	 *         particular bundle, which will be cleared and created
	 * @throws ZipServiceException
	 */
	private IRODSFile createOrReturnBundlePath(long currentTime,
			String parentPath) throws ZipServiceException {
		log.info("createOrReturnBundlePath()");
		if (parentPath == null || parentPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty parentPath");
		}

		StringBuilder sb = new StringBuilder(this.getZipServiceConfiguration()
				.getBundlePrefix());
		sb.append('-');
		sb.append(currentTime);
		sb.append('-');
		sb.append(random.nextInt());

		IRODSFile subdirFile;
		try {
			subdirFile = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(parentPath, sb.toString());
			subdirFile.deleteWithForceOption();
			subdirFile.mkdirs();
		} catch (JargonException e) {
			log.error("JargonException getting the file:{}", sb.toString(), e);
			throw new ZipServiceException("Jargon exception getting subdir", e);
		}

		return subdirFile;

	}

	/**
	 * Given the configuration, return a path to the subdir under which bundle
	 * dirs will be created. The subdirs are guaranteed to exist at least when
	 * this method returns
	 * 
	 * @return <code>String</code> with the iRODS absloute path to the bundle
	 *         parent
	 * @throws ZipServiceException
	 */
	private String createOrReturnZipParentPath() throws ZipServiceException {
		log.info("createOrReturnZipParentPath()");
		if (!this.zipServiceConfiguration.isGenerateTempDirInUserHome()) {
			log.error("currently needs a subdir under the user home");
			throw new ZipServiceConfigurationException(
					"unsupported zip location");
		}

		StringBuilder sb = new StringBuilder();
		sb.append(MiscIRODSUtils
				.buildIRODSUserHomeForAccountUsingDefaultScheme(getIrodsAccount()));
		sb.append('/');
		sb.append(zipServiceConfiguration.getBundleSubDirPath());
		IRODSFile subdirFile;
		try {
			subdirFile = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(sb.toString());
			subdirFile.mkdirs();
		} catch (JargonException e) {
			log.error("JargonException getting the file:{}", sb.toString(), e);
			throw new ZipServiceException("Jargon exception getting subdir", e);

		}

		return subdirFile.getAbsolutePath();

	}

	/**
	 * Get the estimated size of the bundle given the provided paths
	 * 
	 * @param irodsAbsolutePaths
	 *            <code>List<String></code> of iRODS paths
	 * @return <code>long</code> with the length of the
	 * @throws ZipServiceException
	 */
	public long computeBundleSizeInBytes(List<String> irodsAbsolutePaths)
			throws ZipServiceException {

		return 0;
	}

}
