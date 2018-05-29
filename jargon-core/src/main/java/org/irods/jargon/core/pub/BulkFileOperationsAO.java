package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.StructFileExtAndRegInp;
import org.irods.jargon.core.packinstr.StructFileExtAndRegInp.BundleType;

public interface BulkFileOperationsAO extends IRODSAccessObject {

	/**
	 * Creates a bundle (tar) file to be stored in iRODS using the contents of the
	 * specified collection. An optional (blank if not used) resource can be
	 * specified to store the new bundle. This version is no-force.
	 *
	 * @param absolutePathToBundleFileToBeCreatedOnIrods
	 *            {@code String} with the absolute path to a file on iRODS that will
	 *            store the created bundle.
	 * @param absolutePathToIrodsCollectionToBeBundled
	 *            {@code String} with the absolute path to a collection on iRODS
	 *            that will be the source of the bundle.
	 * @param resourceNameWhereBundleWillBeStored
	 *            {@code String} with the optional (leave blank if not used, not
	 *            null) iRODS resource where the bundle will be stored.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void createABundleFromIrodsFilesAndStoreInIrods(final String absolutePathToBundleFileToBeCreatedOnIrods,
			final String absolutePathToIrodsCollectionToBeBundled, final String resourceNameWhereBundleWillBeStored)
			throws JargonException;

	/**
	 * Creates a bundle (tar) file to be stored in iRODS using the contents of the
	 * specified collection. An optional (blank if not used) resource can be
	 * specified to store the new bundle. This version uses a force option to
	 * overwrite the tar file if it already exists.
	 *
	 * @param absolutePathToBundleFileToBeCreatedOnIrods
	 *            {@code String} with the absolute path to a file on iRODS that will
	 *            store the created bundle.
	 * @param absolutePathToIrodsCollectionToBeBundled
	 *            {@code String} with the absolute path to a collection on iRODS
	 *            that will be the source of the bundle.
	 * @param resourceNameWhereBundleWillBeStored
	 *            {@code String} with the optional (leave blank if not used, not
	 *            null) iRODS resource where the bundle will be stored.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void createABundleFromIrodsFilesAndStoreInIrodsWithForceOption(String absolutePathToBundleFileToBeCreatedOnIrods,
			String absolutePathToIrodsCollectionToBeBundled, String resourceNameWhereBundleWillBeStored)
			throws JargonException;

	/**
	 * Given a tar file that exists in iRODS, extract the contents to the given
	 * target directory. This is a no-force, no-bulk operation.
	 *
	 * @param absolutePathToBundleFileInIrodsToBeExtracted
	 *            {@code String} with the absolute path to the tar file in iRODS to
	 *            be extracted.
	 * @param absolutePathToIrodsCollectionToHoldExtractedFiles
	 *            {@code String} with the absolute path to the collection that will
	 *            be the target of the extraction. The collection does not have to
	 *            exist.
	 * @param resourceNameWhereBundleWillBeExtracted
	 *            {@code String} with the target resource for the extraction. This
	 *            is optional and should be set to blank if not used (not null).
	 * @throws JargonException
	 *             for iRODS error
	 */
	void extractABundleIntoAnIrodsCollection(String absolutePathToBundleFileInIrodsToBeExtracted,
			String absolutePathToIrodsCollectionToHoldExtractedFiles, String resourceNameWhereBundleWillBeExtracted)
			throws JargonException;

	/**
	 * Given a tar file that exists in iRODS, extract the contents to the given
	 * target directory. This is a no-bulk operation that will overwrite any
	 * previously extracted files
	 *
	 * @param absolutePathToBundleFileInIrodsToBeExtracted
	 *            {@code String} with the absolute path to the tar file in iRODS to
	 *            be extracted.
	 * @param absolutePathToIrodsCollectionToHoldExtractedFiles
	 *            {@code String} with the absolute path to the collection that will
	 *            be the target of the extraction. The collection does not have to
	 *            exist.
	 * @param resourceNameWhereBundleWillBeExtracted
	 *            {@code String} with the target resource for the extraction. This
	 *            is optional and should be set to blank if not used (not null).
	 * @throws JargonException
	 *             for iRODS error
	 */
	void extractABundleIntoAnIrodsCollectionWithForceOption(String absolutePathToBundleFileInIrodsToBeExtracted,
			String absolutePathToIrodsCollectionToHoldExtractedFiles, String resourceNameWhereBundleWillBeExtracted)
			throws JargonException;

	/**
	 * Given a tar file that exists in iRODS, extract the contents to the given
	 * target directory. This is a no-force operation that will use the bulk
	 * registration optimization.
	 *
	 * @param absolutePathToBundleFileInIrodsToBeExtracted
	 *            {@code String} with the absolute path to the tar file in iRODS to
	 *            be extracted.
	 * @param absolutePathToIrodsCollectionToHoldExtractedFiles
	 *            {@code String} with the absolute path to the collection that will
	 *            be the target of the extraction. The collection does not have to
	 *            exist.
	 * @param resourceNameWhereBundleWillBeExtracted
	 *            {@code String} with the target resource for the extraction. This
	 *            is optional and should be set to blank if not used (not null).
	 * @throws JargonException
	 *             for iRODS error
	 */
	void extractABundleIntoAnIrodsCollectionWithBulkOperationOptimization(
			String absolutePathToBundleFileInIrodsToBeExtracted,
			String absolutePathToIrodsCollectionToHoldExtractedFiles, String resourceNameWhereBundleWillBeExtracted)
			throws JargonException;

	/**
	 * Given a tar file that exists in iRODS, extract the contents to the given
	 * target directory. This is a no-bulk operation that will overwrite any
	 * previously extracted files
	 * 
	 * @param absolutePathToBundleFileInIrodsToBeExtracted
	 *            <code>String</code> with the absolute path to the tar file in
	 *            iRODS to be extracted.
	 * @param absolutePathToIrodsCollectionToHoldExtractedFiles
	 *            <code>String</code> with the absolute path to the collection
	 *            that will be the target of the extraction. The collection does
	 *            not have to exist.
	 * @param resourceNameWhereBundleWillBeExtracted
	 *            <code>String</code> with the target resource for the
	 *            extraction. This is optional and should be set to blank if not
	 *            used (not null).
	 * @param bundleType
	 *            {@link StructFileExtAndRegInp.BundleType} that describes the
	 *            desired bundle to be produced
	 * @throws JargonException
	 */
	void createABundleFromIrodsFilesAndStoreInIrodsWithForceOption(
			String absolutePathToBundleFileToBeCreatedOnIrods,
			String absolutePathToIrodsCollectionToBeBundled,
			String resourceNameWhereBundleWillBeStored, BundleType bundleType)
			throws JargonException;

	/**
	 * Creates a bundle (tar) file to be stored in iRODS using the contents of
	 * the specified collection. An optional (blank if not used) resource can be
	 * specified to store the new bundle. This version is no-force.
	 * 
	 * @param absolutePathToBundleFileToBeCreatedOnIrods
	 *            <code>String</code> with the absolute path to a file on iRODS
	 *            that will store the created bundle.
	 * @param absolutePathToIrodsCollectionToBeBundled
	 *            <code>String</code> with the absolute path to a collection on
	 *            iRODS that will be the source of the bundle.
	 * @param resourceNameWhereBundleWillBeStored
	 *            <code>String</code> with the optional (leave blank if not
	 *            used, not null) iRODS resource where the bundle will be
	 *            stored. * @param bundleType
	 *            {@link StructFileExtAndRegInp.BundleType} that describes the
	 *            desired bundle to be produced
	 * @throws JargonException
	 */
	void createABundleFromIrodsFilesAndStoreInIrods(
			String absolutePathToBundleFileToBeCreatedOnIrods,
			String absolutePathToIrodsCollectionToBeBundled,
			String resourceNameWhereBundleWillBeStored, BundleType bundleType)
			throws JargonException;

}