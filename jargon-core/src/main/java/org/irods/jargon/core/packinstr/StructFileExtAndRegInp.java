package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.IRODSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translation of a StructFileExtAndRegInp operation into XML protocol format.
 * This packing instruction is used to create, extract and manipulate bundled
 * files.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class StructFileExtAndRegInp extends
		AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "StructFileExtAndRegInp_PI";
	public static final String OBJ_PATH = "objPath";
	public static final String COLLECTION = "collection";
	public static final String OPR_TYPE = "oprType";
	public static final String FLAGS = "flags";
	public static final String FORCE_FLAG_KW = "forceFlag";
	public static final String BULK_OPERATION_KW = "bulkOpr";

	public static final String RESOURCE_NAME_KW = "rescName";
	public static final String DEST_RESOURCE_NAME_KW = "destRescName";
	public static final String DATA_TYPE = "dataType";
	public static final String TAR_DATA_TYPE_KW_VALUE = "tar file";
	public static final String ZIP_DATA_TYPE_KW_VALUE = "zipFile";
	public static final String GZIP_DATA_TYPE_KW_VALUE = "gzip";
	public static final String BZIP_DATA_TYPE_KW_VALUE = "bzip";

	public static final int STRUCT_FILE_EXTRACT_AND_REG_API_NBR = 665;
	public static final int STRUCT_FILE_BUNDLE_API_NBR = 666;

	public static final int DEFAULT_OPERATION_TYPE = 0;

	private ForceOptions forceOption = ForceOptions.NO_FORCE;

	private static final Logger LOG = LoggerFactory
			.getLogger(StructFileExtAndRegInp.class);

	public enum ForceOptions {
		FORCE, NO_FORCE
	}

	public enum BundleType {
		DEFAULT, ZIP, TAR, GZIP, BZIP
	}

	private final String tarFileAbsolutePath;
	private final String tarCollectionAbsolutePath;
	private final int operationType;
	private final String resourceName;
	private final boolean extractAsBulkOperation;
	private BundleType bundleType = BundleType.DEFAULT;

	/**
	 * Create a packing instruction to cause the specified tar file that exists
	 * in iRODS to be extracted to the given collection in iRODS.
	 *
	 * @param tarFileAbsolutePath
	 *            {@code String} that is the absolute path to a tar file in
	 *            iRODS to be extracted.
	 * @param tarCollectionAbsolutePath
	 *            {@code String} that is the absolute path to an iRODS
	 *            collection that is the target of the extracted files from the
	 *            given tar, note that this option does not specify a force
	 *            flag, so any subfiles in the target collection will cause the
	 *            operation to fail.
	 * @param resourceNameToStoreTo
	 *            {@code String} with an optional resource name to store
	 *            the extracted files to. Leave blank if not specified, not
	 *            null.
	 * @return {@code StructFileExtAndRegInp} packing instruction
	 */
	public static final StructFileExtAndRegInp instanceForExtractBundleNoForce(
			final String tarFileAbsolutePath,
			final String tarCollectionAbsolutePath,
			final String resourceNameToStoreTo) {
		return new StructFileExtAndRegInp(STRUCT_FILE_EXTRACT_AND_REG_API_NBR,
				tarFileAbsolutePath, tarCollectionAbsolutePath,
				ForceOptions.NO_FORCE, resourceNameToStoreTo, false);
	}

	/**
	 * Create a packing instruction to cause the specified tar file that exists
	 * in iRODS to be extracted to the given collection in iRODS. This version
	 * of the packing instruction will use the bulk facility in iRODS to
	 * register the files, resulting in reduced overhead.
	 *
	 * @param tarFileAbsolutePath
	 *            {@code String} that is the absolute path to a tar file in
	 *            iRODS to be extracted.
	 * @param tarCollectionAbsolutePath
	 *            {@code String} that is the absolute path to an iRODS
	 *            collection that is the target of the extracted files from the
	 *            given tar, note that this option does not specify a force
	 *            flag, so any subfiles in the target collection will cause the
	 *            operation to fail.
	 * @param resourceNameToStoreTo
	 *            {@code String} with an optional resource name to store
	 *            the extracted files to. Leave blank if not specified, not
	 *            null.
	 * @return {@code StructFileExtAndRegInp} packing instruction
	 */
	public static final StructFileExtAndRegInp instanceForExtractBundleNoForceWithBulkOperation(
			final String tarFileAbsolutePath,
			final String tarCollectionAbsolutePath,
			final String resourceNameToStoreTo) {
		return new StructFileExtAndRegInp(STRUCT_FILE_EXTRACT_AND_REG_API_NBR,
				tarFileAbsolutePath, tarCollectionAbsolutePath,
				ForceOptions.NO_FORCE, resourceNameToStoreTo, true);
	}

	/**
	 * Create a packing instruction to cause the specified tar file that exists
	 * in iRODS to be extracted to the given collection in iRODS. A force option
	 * is specified.
	 *
	 * @param tarFileAbsolutePath
	 *            {@code String} that is the absolute path to a tar file in
	 *            iRODS to be extracted.
	 * @param tarCollectionAbsolutePath
	 *            {@code String} that is the absolute path to an iRODS
	 *            collection that is the target of the extracted files from the
	 *            given tar, note that this option specifies a force flag, so
	 *            any subfiles in the target collection will not cause the
	 *            operation to fail.
	 * @param resourceNameToStoreTo
	 *            {@code String} with an optional resource name to store
	 *            the extracted files to. Leave blank if not specified, not
	 *            null.
	 * @return {@code StructFileExtAndRegInp} packing instruction
	 */
	public static final StructFileExtAndRegInp instanceForExtractBundleWithForceOption(
			final String tarFileAbsolutePath,
			final String tarCollectionAbsolutePath,
			final String resourceNameToStoreTo) {
		return new StructFileExtAndRegInp(STRUCT_FILE_EXTRACT_AND_REG_API_NBR,
				tarFileAbsolutePath, tarCollectionAbsolutePath,
				ForceOptions.FORCE, resourceNameToStoreTo, false);
	}

	/**
	 * Create a packing instruction to cause the specified tar file that exists
	 * in iRODS to be extracted to the given collection in iRODS. A force option
	 * is specified. This version of the packing instruction will use the bulk
	 * facility in iRODS to register the files, resulting in reduced overhead.
	 *
	 * @param tarFileAbsolutePath
	 *            {@code String} that is the absolute path to a tar file in
	 *            iRODS to be extracted.
	 * @param tarCollectionAbsolutePath
	 *            {@code String} that is the absolute path to an iRODS
	 *            collection that is the target of the extracted files from the
	 *            given tar, note that this option specifies a force flag, so
	 *            any subfiles in the target collection will not cause the
	 *            operation to fail.
	 * @param resourceNameToStoreTo
	 *            {@code String} with an optional resource name to store
	 *            the extracted files to. Leave blank if not specified, not
	 *            null.
	 * @return {@code StructFileExtAndRegInp} packing instruction
	 */
	public static final StructFileExtAndRegInp instanceForExtractBundleWithForceOptionAndBulkOperation(
			final String tarFileAbsolutePath,
			final String tarCollectionAbsolutePath,
			final String resourceNameToStoreTo) {
		return new StructFileExtAndRegInp(STRUCT_FILE_EXTRACT_AND_REG_API_NBR,
				tarFileAbsolutePath, tarCollectionAbsolutePath,
				ForceOptions.FORCE, resourceNameToStoreTo, true);
	}

	/**
	 * Packing instruction to create a tar file using the given iRODS collection
	 * as the source.
	 *
	 * @param tarFileToCreateAbsolutePath
	 *            {@code String} with the absolute path to the tar file
	 *            that will be created.
	 * @param irodsSourceCollectionForTarAbsolutePath
	 *            {@code String} with the absolute path to collection that
	 *            will be bundled into the tar file.
	 * @param resourceNameThatIsSourceForTarFile
	 *            {@code String} that is the resource that will be the
	 *            source, set to blank of not used (not null)
	 * @return {@code StructFileExtAndRegInp} packing instruction
	 */
	public static final StructFileExtAndRegInp instanceForCreateBundle(
			final String tarFileToCreateAbsolutePath,
			final String irodsSourceCollectionForTarAbsolutePath,
			final String resourceNameThatIsSourceForTarFile) {

		return new StructFileExtAndRegInp(STRUCT_FILE_BUNDLE_API_NBR,
				tarFileToCreateAbsolutePath,
				irodsSourceCollectionForTarAbsolutePath, ForceOptions.NO_FORCE,
				resourceNameThatIsSourceForTarFile, false);
	}

	/**
	 * Packing instruction to create a tar file using the given iRODS collection
	 * as the source. This version of the packing instruction uses the force
	 * option which will overwrite the bun file if already existing.
	 *
	 * @param tarFileToCreateAbsolutePath
	 *            {@code String} with the absolute path to the tar file
	 *            that will be created.
	 * @param irodsSourceCollectionForTarAbsolutePath
	 *            {@code String} with the absolute path to collection that
	 *            will be bundled into the tar file.
	 * @param resourceNameThatIsSourceForTarFile
	 *            {@code String} that is the resource that will be the
	 *            source, set to blank of not used (not null)
	 * @return {@code StructFileExtAndRegInp} packing instruction
	 */
	public static final StructFileExtAndRegInp instanceForCreateBundleWithForceOption(
			final String tarFileToCreateAbsolutePath,
			final String irodsSourceCollectionForTarAbsolutePath,
			final String resourceNameThatIsSourceForTarFile) {

		return new StructFileExtAndRegInp(STRUCT_FILE_BUNDLE_API_NBR,
				tarFileToCreateAbsolutePath,
				irodsSourceCollectionForTarAbsolutePath, ForceOptions.FORCE,
				resourceNameThatIsSourceForTarFile, false);
	}

	/**
	 * Packing instruction to create a tar file using the given iRODS collection
	 * as the source. This version of the packing instruction uses the force
	 * option which will overwrite the bun file if already existing.
	 * 
	 * @param tarFileToCreateAbsolutePath
	 *            <code>String</code> with the absolute path to the tar file
	 *            that will be created.
	 * @param irodsSourceCollectionForTarAbsolutePath
	 *            <code>String</code> with the absolute path to collection that
	 *            will be bundled into the tar file.
	 * @param resourceNameThatIsSourceForTarFile
	 *            <code>String</code> that is the resource that will be the
	 *            source, set to blank of not used (not null)
	 * @param bundleType
	 *            {@link BundleType} enum value for the resulting bundle type
	 * @return
	 */
	public static final StructFileExtAndRegInp instanceForCreateBundleWithForceOption(
			final String tarFileToCreateAbsolutePath,
			final String irodsSourceCollectionForTarAbsolutePath,
			final String resourceNameThatIsSourceForTarFile,
			final BundleType bundleType) {

		if (bundleType == null) {
			throw new IllegalArgumentException("null bundle type");
		}

		StructFileExtAndRegInp pi = new StructFileExtAndRegInp(
				STRUCT_FILE_BUNDLE_API_NBR, tarFileToCreateAbsolutePath,
				irodsSourceCollectionForTarAbsolutePath, ForceOptions.FORCE,
				resourceNameThatIsSourceForTarFile, false);
		pi.bundleType = bundleType;
		return pi;
	}

	/**
	 * Packing instruction to create a tar file using the given iRODS collection
	 * as the source.
	 * 
	 * @param tarFileToCreateAbsolutePath
	 *            <code>String</code> with the absolute path to the tar file
	 *            that will be created.
	 * @param irodsSourceCollectionForTarAbsolutePath
	 *            <code>String</code> with the absolute path to collection that
	 *            will be bundled into the tar file.
	 * @param resourceNameThatIsSourceForTarFile
	 *            <code>String</code> that is the resource that will be the
	 *            source, set to blank of not used (not null)
	 * @return
	 */
	public static final StructFileExtAndRegInp instanceForCreateBundle(
			final String tarFileToCreateAbsolutePath,
			final String irodsSourceCollectionForTarAbsolutePath,
			final String resourceNameThatIsSourceForTarFile,
			final BundleType bundleType) {

		if (bundleType == null) {
			throw new IllegalArgumentException("null bundle type");
		}

		StructFileExtAndRegInp pi = new StructFileExtAndRegInp(
				STRUCT_FILE_BUNDLE_API_NBR, tarFileToCreateAbsolutePath,
				irodsSourceCollectionForTarAbsolutePath, ForceOptions.NO_FORCE,
				resourceNameThatIsSourceForTarFile, false);
		pi.bundleType = bundleType;
		return pi;
	}

	private StructFileExtAndRegInp(final int apiNumber,
			final String tarFileAbsolutePath,
			final String tarCollectionAbsolutePath,
			final ForceOptions forceOption, final String resourceName,
			final boolean extractAsBulkOperation) {

		super();

		if (tarFileAbsolutePath == null || tarFileAbsolutePath.length() == 0) {
			throw new IllegalArgumentException(
					"tarFileAbsolutePath is null or empty");
		}

		if (tarCollectionAbsolutePath == null
				|| tarCollectionAbsolutePath.length() == 0) {
			throw new IllegalArgumentException(
					"tarCollectionAbsolutePath is null or empty");
		}

		if (resourceName == null) {
			throw new IllegalArgumentException("resourceName is null");
		}

		this.tarFileAbsolutePath = tarFileAbsolutePath;
		this.tarCollectionAbsolutePath = tarCollectionAbsolutePath;
		setApiNumber(apiNumber);
		this.forceOption = forceOption;
		operationType = DEFAULT_OPERATION_TYPE;
		this.resourceName = resourceName;
		this.extractAsBulkOperation = extractAsBulkOperation;

	}

	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(PI_TAG, new Tag[] {
				new Tag(OBJ_PATH, tarFileAbsolutePath),
				new Tag(COLLECTION, tarCollectionAbsolutePath),
				new Tag(OPR_TYPE, 0), new Tag(IRODSConstants.flags, 0) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		if (this.bundleType == BundleType.TAR
				|| bundleType == BundleType.DEFAULT) {
			kvps.add(KeyValuePair.instance(DATA_TYPE, TAR_DATA_TYPE_KW_VALUE));
		} else if (this.bundleType == BundleType.GZIP) {
			kvps.add(KeyValuePair.instance(DATA_TYPE, GZIP_DATA_TYPE_KW_VALUE));
		} else if (this.bundleType == BundleType.BZIP) {
			kvps.add(KeyValuePair.instance(DATA_TYPE, BZIP_DATA_TYPE_KW_VALUE));
		} else if (this.bundleType == BundleType.ZIP) {
			kvps.add(KeyValuePair.instance(DATA_TYPE, ZIP_DATA_TYPE_KW_VALUE));
		}

		// formatted resource keyword
		if (resourceName.length() > 0) {
			LOG.debug("resourceName is specified, add resource keywords");
			kvps.add(KeyValuePair.instance(DEST_RESOURCE_NAME_KW, resourceName));
			kvps.add(KeyValuePair.instance(RESOURCE_NAME_KW, resourceName));
		}

		if (forceOption == ForceOptions.FORCE) {
			LOG.debug("adding force flag");
			kvps.add(KeyValuePair.instance(FORCE_FLAG_KW, ""));
		}

		if (isExtractAsBulkOperation()) {
			LOG.debug("adding bulk operation flag");
			kvps.add(KeyValuePair.instance(BULK_OPERATION_KW, ""));
		}

		message.addTag(createKeyValueTag(kvps));
		return message;
	}

	public ForceOptions getForceOption() {
		return forceOption;
	}

	public void setForceOption(final ForceOptions forceOption) {
		this.forceOption = forceOption;
	}

	public String getTarFileAbsolutePath() {
		return tarFileAbsolutePath;
	}

	public String getTarCollectionAbsolutePath() {
		return tarCollectionAbsolutePath;
	}

	public int getOperationType() {
		return operationType;
	}

	/**
	 * @return the resourceName used to describe the source resource when
	 *         creating a tar bundle.
	 */
	public String getResourceName() {
		return resourceName;
	}

	protected boolean isExtractAsBulkOperation() {
		return extractAsBulkOperation;
	}

}
