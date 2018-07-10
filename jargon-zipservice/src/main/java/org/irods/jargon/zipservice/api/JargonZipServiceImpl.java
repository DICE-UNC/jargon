/**
 *
 */
package org.irods.jargon.zipservice.api;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.BulkFileOperationsAO;
import org.irods.jargon.core.pub.BulkFileOperationsAOImpl;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.FileIOOperations;
import org.irods.jargon.core.pub.io.FileIOOperationsAOImpl;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.transfer.TransferControlBlock;
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
public class JargonZipServiceImpl extends AbstractJargonService implements JargonZipService {

	private ZipServiceConfiguration zipServiceConfiguration = null;
	private final Random random;

	public static final Logger log = LoggerFactory.getLogger(JargonZipServiceImpl.class);

	public JargonZipServiceImpl() {
		super();
		random = new Random();
	}

	/**
	 * @param zipServiceConfiguration
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public JargonZipServiceImpl(final ZipServiceConfiguration zipServiceConfiguration,
			final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
		if (zipServiceConfiguration == null) {
			throw new IllegalArgumentException("null zipServiceConfiguration");
		}
		this.zipServiceConfiguration = zipServiceConfiguration;
		random = new Random();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.zipservice.api.JargonZipService#getZipServiceConfiguration
	 * ()
	 */
	@Override
	public ZipServiceConfiguration getZipServiceConfiguration() {
		return zipServiceConfiguration;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.zipservice.api.JargonZipService#setZipServiceConfiguration
	 * (org.irods.jargon.zipservice.api.ZipServiceConfiguration)
	 */
	@Override
	public void setZipServiceConfiguration(final ZipServiceConfiguration zipServiceConfiguration) {
		this.zipServiceConfiguration = zipServiceConfiguration;
	}

	/**
	 * Handy method to verify configuration of this service before any opeerations
	 *
	 * @throws ZipServiceConfigurationException
	 */
	protected void validateConfiguration() throws ZipServiceConfigurationException {
		if (zipServiceConfiguration == null) {
			throw new ZipServiceConfigurationException(
					"null zipServiceConfiguration, needs to be set before operations");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.zipservice.api.JargonZipService#
	 * obtainBundleAsInputStreamWithAdditionalMetadataGivenPaths(java.util.List)
	 */
	@SuppressWarnings("resource")
	@Override
	public BundleStreamWrapper obtainBundleAsInputStreamWithAdditionalMetadataGivenPaths(
			final List<String> irodsAbsolutePaths) throws ZipServiceException {
		log.info("obtainBundleAsInputStreamWithAdditionalMetadataGivenPaths()");
		FileIOOperations fileIOOperations;
		try {
			fileIOOperations = new FileIOOperationsAOImpl(getIrodsAccessObjectFactory().getIrodsSession(),
					getIrodsAccount());
			IRODSFile bundleFile = obtainBundleAsIrodsFileGivenPaths(irodsAbsolutePaths);
			BundleClosingInputStream inputStream = new BundleClosingInputStream(bundleFile, fileIOOperations);
			return new BundleStreamWrapper(inputStream, bundleFile.length(), bundleFile.getName());
		} catch (JargonException | FileNotFoundException e) {
			log.error("JargonException getting input stream", e);
			throw new ZipServiceException("Jargon exception getting input stream", e);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.zipservice.api.JargonZipService#
	 * obtainBundleAsInputStreamGivenPaths(java.util.List)
	 */
	@Override
	public InputStream obtainBundleAsInputStreamGivenPaths(final List<String> irodsAbsolutePaths)
			throws ZipServiceException {
		log.info("obtainInputStreamForBundleGivenPaths()");
		FileIOOperations fileIOOperations;
		try {
			fileIOOperations = new FileIOOperationsAOImpl(getIrodsAccessObjectFactory().getIrodsSession(),
					getIrodsAccount());
			IRODSFile bundleFile = obtainBundleAsIrodsFileGivenPaths(irodsAbsolutePaths);
			return new BundleClosingInputStream(bundleFile, fileIOOperations);
		} catch (JargonException | FileNotFoundException e) {
			log.error("JargonException getting input stream", e);
			throw new ZipServiceException("Jargon exception getting input stream", e);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.zipservice.api.JargonZipService#
	 * obtainBundleAsIrodsFileGivenPaths(java.util.List)
	 */
	@Override
	public IRODSFile obtainBundleAsIrodsFileGivenPaths(final List<String> irodsAbsolutePaths)
			throws ZipServiceException {

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

		if (totalLength > getZipServiceConfiguration().getMaxTotalBytesForZip()) {
			log.error("bundle total size is:{} and is larger than the configured maximum", totalLength);
			throw new ZipRequestTooLargeException("total size is larger than configured bundle max");
		}

		log.info("copy phase, building a local subdir");

		log.info("building in user home");
		String parentPath = createOrReturnZipParentPath();
		log.info("parent path:{}", parentPath);
		long currentTime = System.currentTimeMillis();
		IRODSFile bundleParent = createOrReturnBundlePath(currentTime, parentPath);

		log.info("begin the copy phase...");
		copyFilesToBundleDir(irodsAbsolutePaths, bundleParent);
		log.info("create the bundle...");
		BulkFileOperationsAO bulkFileOperationsAO;
		try {
			bulkFileOperationsAO = getIrodsAccessObjectFactory().getBulkFileOperationsAO(getIrodsAccount());
		} catch (JargonException e) {
			log.error("JargonException getting bulkFileOperationsAO", e);
			throw new ZipServiceException("Jargon exception getting bulkFileOperationsAO", e);
		}

		StringBuilder sb = new StringBuilder(parentPath);
		sb.append("/");
		sb.append(bundleParent.getName());
		sb.append(
				BulkFileOperationsAOImpl.fileExtensionForBundleType(zipServiceConfiguration.getPreferredBundleType()));
		String zipFileName = sb.toString();
		IRODSFile zipFile;

		try {
			bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrods(zipFileName, bundleParent.getAbsolutePath(),
					"", getZipServiceConfiguration().getPreferredBundleType());
			zipFile = getIrodsAccessObjectFactory().getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(zipFileName);
			log.info("delete temp files before returning the bundle file");
			bundleParent.deleteWithForceOption();

		} catch (JargonException e) {
			log.error("JargonException creating bundle", e);
			throw new ZipServiceException("Jargon exception creating bundle", e);
		}

		log.info("bundle is in file:{}", bundleParent);
		return zipFile;
	}

	/**
	 * Copy files from the disparate paths to the bundle dir
	 *
	 * @param irodsAbsolutePaths
	 *            <code>List<String></code> with the paths that will be copied to
	 *            the bundle directory
	 * @param bundleParent
	 *            <code>String</code> with the absolute path of the bundle directory
	 * @throws ZipServiceException
	 *             for any exception that occurs, there are subclasses of this
	 *             top-level exception that can be discerened
	 */
	private void copyFilesToBundleDir(final List<String> irodsAbsolutePaths, final IRODSFile bundleParent)
			throws ZipServiceException {
		// now bundle parent exists and is blank, start the copies
		log.info("starting copies, may take a while...");
		DataTransferOperations dataTransferOperations;

		try {

			dataTransferOperations = getIrodsAccessObjectFactory().getDataTransferOperations(getIrodsAccount());
		} catch (JargonException e) {
			log.error("JargonException getting data transfer operations:{}", e);
			throw new ZipServiceException("Jargon exception getting data transfer operations", e);
		}

		for (String path : irodsAbsolutePaths) {
			log.info("copy {}", path);
			try {
				TransferControlBlock tcb = irodsAccessObjectFactory
						.buildDefaultTransferControlBlockBasedOnJargonProperties();
				tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
				dataTransferOperations.copy(path, "", bundleParent.getAbsolutePath(), null, tcb);
			} catch (JargonException e) {
				if (zipServiceConfiguration.isFailFast()) {
					log.error("JargonException copying to bundle dir for:{}, fail fast is set", path, e);
					throw new ZipServiceException("Unable to copy a file into the bundle dir - fail fast is set", e);
				} else {
					log.warn("JargonException copying to bundle dir for:{}, fail fast is not set, so we will proceed",
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
	 *            <code>String</code> with the absolute path to the main parent of
	 *            bundle files in general
	 * @return <code>String</code> with the absolute path to the parent for this
	 *         particular bundle, which will be cleared and created
	 * @throws ZipServiceException
	 */
	private IRODSFile createOrReturnBundlePath(final long currentTime, final String parentPath)
			throws ZipServiceException {
		log.info("createOrReturnBundlePath()");
		if (parentPath == null || parentPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty parentPath");
		}

		StringBuilder sb = new StringBuilder(getZipServiceConfiguration().getBundlePrefix());
		sb.append('-');
		sb.append(currentTime);
		sb.append('-');
		sb.append(random.nextInt());

		IRODSFile subdirFile;
		try {
			subdirFile = getIrodsAccessObjectFactory().getIRODSFileFactory(getIrodsAccount())
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
	 * Given the configuration, return a path to the subdir under which bundle dirs
	 * will be created. The subdirs are guaranteed to exist at least when this
	 * method returns
	 *
	 * @return <code>String</code> with the iRODS absloute path to the bundle parent
	 * @throws ZipServiceException
	 */
	private String createOrReturnZipParentPath() throws ZipServiceException {
		log.info("createOrReturnZipParentPath()");
		if (!zipServiceConfiguration.isGenerateTempDirInUserHome()) {
			log.error("currently needs a subdir under the user home");
			throw new ZipServiceConfigurationException("unsupported zip location");
		}

		StringBuilder sb = new StringBuilder();
		sb.append(MiscIRODSUtils.buildIRODSUserHomeForAccountUsingDefaultScheme(getIrodsAccount()));
		sb.append('/');
		sb.append(zipServiceConfiguration.getBundleSubDirPath());
		IRODSFile subdirFile;
		try {
			subdirFile = getIrodsAccessObjectFactory().getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(sb.toString());
			subdirFile.mkdirs();
		} catch (JargonException e) {
			log.error("JargonException getting the file:{}", sb.toString(), e);
			throw new ZipServiceException("Jargon exception getting subdir", e);

		}

		return subdirFile.getAbsolutePath();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.zipservice.api.JargonZipService#computeBundleSizeInBytes
	 * (java.util.List)
	 */
	@Override
	public long computeBundleSizeInBytes(final List<String> irodsAbsolutePaths) throws ZipServiceException {

		log.info("computeBundleSizeInBytes()");
		if (irodsAbsolutePaths == null) {
			throw new IllegalArgumentException("null irodsAbsolutePaths");
		}
		if (irodsAbsolutePaths.isEmpty()) {
			return 0;
		}
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectAO;
		try {
			collectionAndDataObjectAO = getIrodsAccessObjectFactory()
					.getCollectionAndDataObjectListAndSearchAO(getIrodsAccount());
		} catch (JargonException e) {
			log.error("JargonException getting CollectionAndDataObjectListAndSearchAO", e);
			throw new ZipServiceException("Jargon exception getting CollectionAndDataObjectListAndSearchAO", e);
		}

		long totalCount = 0;

		for (String path : irodsAbsolutePaths) {
			log.info("getting count for path:{}", path);
			try {
				totalCount += collectionAndDataObjectAO.totalDataObjectSizesUnderPath(path);
			} catch (JargonException e) {
				log.error("JargonException counting under paths", e);
				throw new ZipServiceException("Jargon exception getting CollectionAndDataObjectListAndSearchAO", e);
			}
		}

		log.info("total count:{}", totalCount);
		return totalCount;

	}

}
