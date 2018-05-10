package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.StructFileExtAndRegInp;
import org.irods.jargon.core.packinstr.StructFileExtAndRegInp.BundleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object to handle bundled file operations. This object contains functionality
 * similar to the iRODS <code>ibun</code> to transmit and register, or bundle
 * and receive files.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class BulkFileOperationsAOImpl extends IRODSGenericAO implements BulkFileOperationsAO {

	public static final Logger log = LoggerFactory.getLogger(BulkFileOperationsAOImpl.class);

	/**
	 * Get the extension typically associated with a bundle type
	 * 
	 * @param bundleType
	 *            {@link BundleType} for an iRODS bundle
	 * @return
	 */
	public static String fileExtensionForBundleType(final BundleType bundleType) {
		log.info("fileExtensionForBundleType()");
		if (bundleType == null) {
			throw new IllegalArgumentException("null bundleType");
		}
		String extension = "";
		if (bundleType == BundleType.DEFAULT || bundleType == BundleType.TAR) {
			extension = ".tar";
		} else if (bundleType == BundleType.ZIP) {
			extension = ".zip";
		} else if (bundleType == BundleType.GZIP) {
			extension = ".gzip";
		} else if (bundleType == BundleType.BZIP) {
			extension = ".bzip";
		}

		return extension;

	}

	/**
	 * Constructor as called by the {@code IRODSAccessObjectFactory}, which is
	 * properly used to construct this access object.
	 *
	 * @param irodsSession
	 *            {@link IRODSSession}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @throws JargonException
	 *             for iRODS error
	 */
	BulkFileOperationsAOImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.BulkFileOperationsAO#
	 * createABundleFromIrodsFilesAndStoreInIrods(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void createABundleFromIrodsFilesAndStoreInIrods(final String absolutePathToBundleFileToBeCreatedOnIrods,
			final String absolutePathToIrodsCollectionToBeBundled, final String resourceNameWhereBundleWillBeStored)
			throws JargonException {

		if (absolutePathToBundleFileToBeCreatedOnIrods == null
				|| absolutePathToBundleFileToBeCreatedOnIrods.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePathToBundleFileToBeCreatedOnIrods");
		}

		if (absolutePathToIrodsCollectionToBeBundled == null || absolutePathToIrodsCollectionToBeBundled.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePathToIrodsCollectionToBeBundled");
		}

		if (resourceNameWhereBundleWillBeStored == null) {
			throw new IllegalArgumentException("null resourceNameWhereBundleWillBeStored. set to blank if not used");
		}

		log.info("createABundleFromIrodsFilesAndStoreInIrods, tar file:{}", absolutePathToBundleFileToBeCreatedOnIrods);
		log.info("source collection for tar:{}", absolutePathToIrodsCollectionToBeBundled);
		log.info("resource:{}", resourceNameWhereBundleWillBeStored);
		StructFileExtAndRegInp structFileExtAndRegInp = StructFileExtAndRegInp.instanceForCreateBundle(
				absolutePathToBundleFileToBeCreatedOnIrods, absolutePathToIrodsCollectionToBeBundled,
				resourceNameWhereBundleWillBeStored);

		getIRODSProtocol().irodsFunction(structFileExtAndRegInp);

	}

	@Override
	public void createABundleFromIrodsFilesAndStoreInIrods(final String absolutePathToBundleFileToBeCreatedOnIrods,
			final String absolutePathToIrodsCollectionToBeBundled, final String resourceNameWhereBundleWillBeStored,
			final StructFileExtAndRegInp.BundleType bundleType) throws JargonException {

		if (absolutePathToBundleFileToBeCreatedOnIrods == null
				|| absolutePathToBundleFileToBeCreatedOnIrods.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePathToBundleFileToBeCreatedOnIrods");
		}

		if (absolutePathToIrodsCollectionToBeBundled == null || absolutePathToIrodsCollectionToBeBundled.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePathToIrodsCollectionToBeBundled");
		}

		if (resourceNameWhereBundleWillBeStored == null) {
			throw new IllegalArgumentException("null resourceNameWhereBundleWillBeStored. set to blank if not used");
		}

		if (bundleType == null) {
			throw new IllegalArgumentException("null bundle type");
		}

		log.info("createABundleFromIrodsFilesAndStoreInIrods, tar file:{}", absolutePathToBundleFileToBeCreatedOnIrods);
		log.info("source collection for tar:{}", absolutePathToIrodsCollectionToBeBundled);
		log.info("resource:{}", resourceNameWhereBundleWillBeStored);
		log.info("bundle type:{}", bundleType);
		StructFileExtAndRegInp structFileExtAndRegInp = StructFileExtAndRegInp.instanceForCreateBundle(
				absolutePathToBundleFileToBeCreatedOnIrods, absolutePathToIrodsCollectionToBeBundled,
				resourceNameWhereBundleWillBeStored, bundleType);

		getIRODSProtocol().irodsFunction(structFileExtAndRegInp);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.BulkFileOperationsAO#
	 * createABundleFromIrodsFilesAndStoreInIrodsWithForceOption (java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void createABundleFromIrodsFilesAndStoreInIrodsWithForceOption(
			final String absolutePathToBundleFileToBeCreatedOnIrods,
			final String absolutePathToIrodsCollectionToBeBundled, final String resourceNameWhereBundleWillBeStored)
			throws JargonException {

		if (absolutePathToBundleFileToBeCreatedOnIrods == null
				|| absolutePathToBundleFileToBeCreatedOnIrods.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePathToBundleFileToBeCreatedOnIrods");
		}

		if (absolutePathToIrodsCollectionToBeBundled == null || absolutePathToIrodsCollectionToBeBundled.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePathToIrodsCollectionToBeBundled");
		}

		if (resourceNameWhereBundleWillBeStored == null) {
			throw new IllegalArgumentException("null resourceNameWhereBundleWillBeStored. set to blank if not used");
		}

		log.info("createABundleFromIrodsFilesAndStoreInIrods, tar file:{}", absolutePathToBundleFileToBeCreatedOnIrods);
		log.info("source collection for tar:{}", absolutePathToIrodsCollectionToBeBundled);
		log.info("resource:{}", resourceNameWhereBundleWillBeStored);
		StructFileExtAndRegInp structFileExtAndRegInp = StructFileExtAndRegInp.instanceForCreateBundleWithForceOption(
				absolutePathToBundleFileToBeCreatedOnIrods, absolutePathToIrodsCollectionToBeBundled,
				resourceNameWhereBundleWillBeStored);

		getIRODSProtocol().irodsFunction(structFileExtAndRegInp);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.BulkFileOperationsAO#
	 * createABundleFromIrodsFilesAndStoreInIrodsWithForceOption (java.lang.String,
	 * java.lang.String, java.lang.String,
	 * org.irods.jargon.core.packinstr.StructFileExtAndRegInp.BundleType)
	 */
	@Override
	public void createABundleFromIrodsFilesAndStoreInIrodsWithForceOption(
			final String absolutePathToBundleFileToBeCreatedOnIrods,
			final String absolutePathToIrodsCollectionToBeBundled, final String resourceNameWhereBundleWillBeStored,
			final StructFileExtAndRegInp.BundleType bundleType) throws JargonException {

		if (absolutePathToBundleFileToBeCreatedOnIrods == null
				|| absolutePathToBundleFileToBeCreatedOnIrods.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePathToBundleFileToBeCreatedOnIrods");
		}

		if (absolutePathToIrodsCollectionToBeBundled == null || absolutePathToIrodsCollectionToBeBundled.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePathToIrodsCollectionToBeBundled");
		}

		if (resourceNameWhereBundleWillBeStored == null) {
			throw new IllegalArgumentException("null resourceNameWhereBundleWillBeStored. set to blank if not used");
		}

		if (bundleType == null) {
			throw new IllegalArgumentException("null bundle type");
		}

		log.info("createABundleFromIrodsFilesAndStoreInIrods, tar file:{}", absolutePathToBundleFileToBeCreatedOnIrods);
		log.info("source collection for tar:{}", absolutePathToIrodsCollectionToBeBundled);
		log.info("resource:{}", resourceNameWhereBundleWillBeStored);
		log.info("bundle type:{}", bundleType);

		StructFileExtAndRegInp structFileExtAndRegInp = StructFileExtAndRegInp.instanceForCreateBundleWithForceOption(
				absolutePathToBundleFileToBeCreatedOnIrods, absolutePathToIrodsCollectionToBeBundled,
				resourceNameWhereBundleWillBeStored, bundleType);

		getIRODSProtocol().irodsFunction(structFileExtAndRegInp);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.BulkFileOperationsAO#
	 * extractABundleIntoAnIrodsCollection(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void extractABundleIntoAnIrodsCollection(final String absolutePathToBundleFileInIrodsToBeExtracted,
			final String absolutePathToIrodsCollectionToHoldExtractedFiles,
			final String resourceNameWhereBundleWillBeExtracted) throws JargonException {

		extractABundleIntoAnIrodsCollection(absolutePathToBundleFileInIrodsToBeExtracted,
				absolutePathToIrodsCollectionToHoldExtractedFiles, resourceNameWhereBundleWillBeExtracted, false,
				false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.BulkFileOperationsAO#
	 * extractABundleIntoAnIrodsCollectionWithBulkOperationOptimization
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void extractABundleIntoAnIrodsCollectionWithBulkOperationOptimization(
			final String absolutePathToBundleFileInIrodsToBeExtracted,
			final String absolutePathToIrodsCollectionToHoldExtractedFiles,
			final String resourceNameWhereBundleWillBeExtracted) throws JargonException {

		extractABundleIntoAnIrodsCollection(absolutePathToBundleFileInIrodsToBeExtracted,
				absolutePathToIrodsCollectionToHoldExtractedFiles, resourceNameWhereBundleWillBeExtracted, false, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.BulkFileOperationsAO#
	 * extractABundleIntoAnIrodsCollectionWithForceOption(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void extractABundleIntoAnIrodsCollectionWithForceOption(
			final String absolutePathToBundleFileInIrodsToBeExtracted,
			final String absolutePathToIrodsCollectionToHoldExtractedFiles,
			final String resourceNameWhereBundleWillBeExtracted) throws JargonException {

		extractABundleIntoAnIrodsCollection(absolutePathToBundleFileInIrodsToBeExtracted,
				absolutePathToIrodsCollectionToHoldExtractedFiles, resourceNameWhereBundleWillBeExtracted, true, false);
	}

	/**
	 * Internal method with params for various options to be delegated to by
	 * specific extract methods in api
	 *
	 * @param absolutePathToBundleFileInIrodsToBeExtracted
	 * @param absolutePathToIrodsCollectionToHoldExtractedFiles
	 * @param resourceNameWhereBundleWillBeExtracted
	 * @param force
	 * @param bulkOperation
	 * @throws JargonException
	 */
	protected void extractABundleIntoAnIrodsCollection(final String absolutePathToBundleFileInIrodsToBeExtracted,
			final String absolutePathToIrodsCollectionToHoldExtractedFiles,
			final String resourceNameWhereBundleWillBeExtracted, final boolean force, final boolean bulkOperation)
			throws JargonException {

		if (absolutePathToBundleFileInIrodsToBeExtracted == null
				|| absolutePathToBundleFileInIrodsToBeExtracted.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePathToBundleFileInIrodsToBeExtracted");
		}

		if (absolutePathToIrodsCollectionToHoldExtractedFiles == null
				|| absolutePathToIrodsCollectionToHoldExtractedFiles.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePathToIrodsCollectionToHoldExtractedFiles");
		}

		if (resourceNameWhereBundleWillBeExtracted == null) {
			throw new IllegalArgumentException(
					"null or empty resourceNameWhereBundleWillBeExtracted, set to blank if not used");
		}

		log.info("extractABundleIntoAnIrodsCollection, tar file:{}", absolutePathToBundleFileInIrodsToBeExtracted);
		log.info("target collection for tar expansio:{}", absolutePathToIrodsCollectionToHoldExtractedFiles);
		log.info("resource:{}", resourceNameWhereBundleWillBeExtracted);

		StructFileExtAndRegInp structFileExtAndRegInp;

		if (force) {
			if (bulkOperation) {
				log.info("force, bulk optimization");
				structFileExtAndRegInp = StructFileExtAndRegInp.instanceForExtractBundleWithForceOptionAndBulkOperation(
						absolutePathToBundleFileInIrodsToBeExtracted, absolutePathToIrodsCollectionToHoldExtractedFiles,
						resourceNameWhereBundleWillBeExtracted);
			} else {
				log.info("force, no bulk optimization");
				structFileExtAndRegInp = StructFileExtAndRegInp.instanceForExtractBundleWithForceOption(
						absolutePathToBundleFileInIrodsToBeExtracted, absolutePathToIrodsCollectionToHoldExtractedFiles,
						resourceNameWhereBundleWillBeExtracted);
			}
		} else {
			if (bulkOperation) {
				log.info("no force, bulk optimization");
				structFileExtAndRegInp = StructFileExtAndRegInp.instanceForExtractBundleNoForceWithBulkOperation(
						absolutePathToBundleFileInIrodsToBeExtracted, absolutePathToIrodsCollectionToHoldExtractedFiles,
						resourceNameWhereBundleWillBeExtracted);
			} else {
				log.info("no force, no bulk optimization");
				structFileExtAndRegInp = StructFileExtAndRegInp.instanceForExtractBundleNoForce(
						absolutePathToBundleFileInIrodsToBeExtracted, absolutePathToIrodsCollectionToHoldExtractedFiles,
						resourceNameWhereBundleWillBeExtracted);
			}
		}

		getIRODSProtocol().irodsFunction(structFileExtAndRegInp);

	}

}
