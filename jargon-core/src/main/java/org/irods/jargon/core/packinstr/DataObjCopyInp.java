/**
 *
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Translation of a DataObjInp operation into XML protocol format.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DataObjCopyInp extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "DataObjCopyInp_PI";
	public static final int RENAME_API_NBR = 601;
	public static final int RENAME_FILE_API_NBR = 627;
	public static final int COPY_API_NBR = 613;
	public static final int COPY_API_NBR_410 = 696;

	private final String fromFileAbsolutePath;
	private final String toFileAbsolutePath;
	private final int operationType;
	private final String resourceName;
	private final long sourceFileLength;
	private final boolean force;

	/**
	 * Create an instance of the packing instruction for a move of a data object
	 * (not a collection, there is a different initializer for that).
	 *
	 * @param sourceFileAbsolutePath {@code String} with the absolute path to the
	 *                               source file.
	 * @param targetFileAbsolutePath {@code String} with the absolute path to the
	 *                               target file.
	 * @return {@code DataObjCopyInp}
	 * @throws JargonException for iRODS error
	 */
	public static final DataObjCopyInp instanceForRenameFile(final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath) throws JargonException {

		return DataObjCopyInp.instanceForRenameFile(sourceFileAbsolutePath, targetFileAbsolutePath, false);
	}

	/**
	 * Create an instance of the packing instruction for a move of a data object
	 * (not a collection, there is a different initializer for that).
	 *
	 * @param sourceFileAbsolutePath {@code String} with the absolute path to the
	 *                               source file.
	 * @param targetFileAbsolutePath {@code String} with the absolute path to the
	 *                               target file.
	 * @param force                  {@code boolean} indicating force operation
	 * @return {@code DataObjCopyInp}
	 * @throws JargonException for iRODS error
	 */
	public static final DataObjCopyInp instanceForRenameFile(final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final boolean force) throws JargonException {

		DataObjCopyInp dataObjCopyInp = new DataObjCopyInp(RENAME_FILE_API_NBR, sourceFileAbsolutePath,
				targetFileAbsolutePath, DataObjInp.RENAME_FILE_OPERATION_TYPE, "", 0, force);
		return dataObjCopyInp;
	}

	/**
	 * Create an instance that will do a file copy between two iRODS directories
	 * using 4.1+ iRODS protocol
	 *
	 * @param sourceFileAbsolutePath {@code String} with the absolute path to the
	 *                               source file
	 * @param targetFileAbsolutePath {@code String} with the absolute path to the
	 *                               target file
	 * @param copyToResource         {@code String} with an optional resource for
	 *                               the target file
	 * @param sourceFileLength       {@code long} with the length of the source file
	 * @param force                  {@code boolean} that indicates whether force
	 *                               option should be set
	 * @return {@link DataObjCopyInp}
	 * @throws JargonException for iRODS error
	 */
	public static final DataObjCopyInp instanceForCopy410(final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String copyToResource, final long sourceFileLength,
			final boolean force) throws JargonException {
		DataObjCopyInp dataObjCopyInp = new DataObjCopyInp(COPY_API_NBR_410, sourceFileAbsolutePath,
				targetFileAbsolutePath, DataObjInp.COPY_FILE_SRC_OPERATION_TYPE, copyToResource, sourceFileLength,
				force);
		return dataObjCopyInp;
	}

	/**
	 * Create an instance that will do a file copy between two iRODS directories
	 *
	 * @param sourceFileAbsolutePath {@code String} with the absolute path to the
	 *                               source file
	 * @param targetFileAbsolutePath {@code String} with the absolute path to the
	 *                               target file
	 * @param copyToResource         {@code String} with an optional resource for
	 *                               the target file
	 * @param sourceFileLength       {@code long} with the length of the source file
	 * @param force                  {@code boolean} that indicates whether force
	 *                               option should be set
	 * @return {@link DataObjCopyInp}
	 * @throws JargonException for iRODS error
	 */
	public static final DataObjCopyInp instanceForCopy(final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String copyToResource, final long sourceFileLength,
			final boolean force) throws JargonException {
		DataObjCopyInp dataObjCopyInp = new DataObjCopyInp(COPY_API_NBR, sourceFileAbsolutePath, targetFileAbsolutePath,
				DataObjInp.COPY_FILE_SRC_OPERATION_TYPE, copyToResource, sourceFileLength, force);
		return dataObjCopyInp;
	}

	/**
	 * Create an instance of the packing instruction for a move of a collection (not
	 * a data object, there is a different initializer for that).
	 *
	 * @param sourceFileAbsolutePath {@code String} with the absolute path to the
	 *                               source file
	 * @param targetFileAbsolutePath {@code String} with the absolute path to the
	 *                               target file
	 * @return {@code DataObjCopyInp}
	 * @throws JargonException for iRODS error
	 */
	public static final DataObjCopyInp instanceForRenameCollection(final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath) throws JargonException {

		DataObjCopyInp dataObjCopyInp = new DataObjCopyInp(RENAME_FILE_API_NBR, sourceFileAbsolutePath,
				targetFileAbsolutePath, DataObjInp.RENAME_DIRECTORY_OPERATION_TYPE, "", 0, false);
		return dataObjCopyInp;
	}

	/*
	 * Create an instance of the packing instruction for a move of a collection (not
	 * a data object, there is a different initializer for that).
	 *
	 * @param sourceFileAbsolutePath {@code String} with the absolute path to the
	 * source file
	 * 
	 * @param targetFileAbsolutePath {@code String} with the absolute path to the
	 * target file
	 * 
	 * @param targetFileAbsolutePath {@code boolean} indicating whether force is in
	 * effect (true)
	 * 
	 * @return {@code DataObjCopyInp}
	 * 
	 * @throws JargonException for iRODS error
	 */
	public static final DataObjCopyInp instanceForRenameCollection(final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final boolean force) throws JargonException {

		DataObjCopyInp dataObjCopyInp = new DataObjCopyInp(RENAME_FILE_API_NBR, sourceFileAbsolutePath,
				targetFileAbsolutePath, DataObjInp.RENAME_DIRECTORY_OPERATION_TYPE, "", 0, force);
		return dataObjCopyInp;
	}

	/**
	 * Create an instance of the packing instruction for a copy of a collection.
	 *
	 * @param sourceFileAbsolutePath {@code String} with the absolute path to the
	 *                               source file.
	 * @param targetFileAbsolutePath {@code String} with the absolute path to the
	 *                               target file.
	 * @param targetResourceName     {@code String} with optional resource name for
	 *                               target. Blank if not used
	 * @param sourceFileLength       {@code long} with a length of the source file
	 * @param force                  {@code boolean} if this is a force operation
	 * @return {@code DataObjCopyInp}
	 * @throws JargonException for iRODS error
	 */
	public static final DataObjCopyInp instanceForCopyCollection(final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String targetResourceName, final long sourceFileLength,
			final boolean force) throws JargonException {

		DataObjCopyInp dataObjCopyInp = new DataObjCopyInp(COPY_API_NBR, sourceFileAbsolutePath, targetFileAbsolutePath,
				DataObjInp.COPY_FILE_SRC_OPERATION_TYPE, targetResourceName, sourceFileLength, force);
		return dataObjCopyInp;
	}

	private DataObjCopyInp(final int apiNumber, final String fromFileAbsolutePath, final String toFileAbsolutePath,
			final int operationType, final String resourceName, final long sourceFileLength, final boolean force)
			throws JargonException {
		super();

		if (fromFileAbsolutePath == null || fromFileAbsolutePath.length() == 0) {
			throw new IllegalArgumentException("from file absolute path is null or empty");
		}

		if (toFileAbsolutePath == null || toFileAbsolutePath.length() == 0) {
			throw new IllegalArgumentException("to file absolute path is null or empty");
		}

		if (operationType == DataObjInp.RENAME_DIRECTORY_OPERATION_TYPE
				|| operationType == DataObjInp.RENAME_FILE_OPERATION_TYPE
				|| operationType == DataObjInp.COPY_FILE_SRC_OPERATION_TYPE) {
			// ok
		} else {
			throw new IllegalArgumentException("unknown operation type:" + operationType);
		}

		if (resourceName == null) {
			throw new IllegalArgumentException("null resourceName");
		}

		if (sourceFileLength < 0) {
			throw new IllegalArgumentException("negative sourceFileLength");
		}

		setApiNumber(apiNumber);
		this.fromFileAbsolutePath = fromFileAbsolutePath;
		this.toFileAbsolutePath = toFileAbsolutePath;
		this.operationType = operationType;
		this.resourceName = resourceName;
		this.sourceFileLength = sourceFileLength;
		this.force = force;
	}

	/**
	 * @return
	 */
	private Tag buildDataObjInpTag(final String filePath, final int operationType) throws JargonException {
		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		Tag fileTag = new Tag(DataObjInp.PI_TAG,
				new Tag[] { new Tag(DataObjInp.OBJ_PATH, filePath), new Tag(DataObjInp.CREATE_MODE, 0),
						new Tag(DataObjInp.OPEN_FLAGS, 0), new Tag(DataObjInp.OFFSET, 0),
						new Tag(DataObjInp.DATA_SIZE, 0), new Tag(DataObjInp.NUM_THREADS, 0),
						new Tag(DataObjInp.OPR_TYPE, operationType), createKeyValueTag(kvps) });
		return fileTag;
	}

	@Override
	public Tag getTagValue() throws JargonException {

		if (getApiNumber() == COPY_API_NBR || getApiNumber() == COPY_API_NBR_410) {
			return getTagValueForCopy();
		} else if (getApiNumber() == RENAME_API_NBR || getApiNumber() == RENAME_FILE_API_NBR) {
			return getTagValueForRename();
		} else {
			return getTagValueForReplicate();
		}
	}

	private Tag getTagValueForRename() throws JargonException {
		// get the DataObjInp tag for the from file
		Tag fromFileTag = buildDataObjInpTagForCopySource(fromFileAbsolutePath, force);
		Tag toFileTag = buildDataObjInpTagForCopyDest(toFileAbsolutePath, resourceName, false);

		// now build the whole tag
		Tag message = new Tag(PI_TAG, new Tag[] { fromFileTag, toFileTag });

		return message;
	}

	private Tag getTagValueForReplicate() throws JargonException {
		// get the DataObjInp tag for the from file
		Tag fromFileTag = buildDataObjInpTag(fromFileAbsolutePath, operationType);
		Tag toFileTag = buildDataObjInpTag(toFileAbsolutePath, operationType);

		// now build the whole tag
		Tag message = new Tag(PI_TAG, new Tag[] { fromFileTag, toFileTag });

		return message;

	}

	private Tag getTagValueForCopy() throws JargonException {
		// get the DataObjInp tag for the from file
		Tag fromFileTag = buildDataObjInpTagForCopySource(fromFileAbsolutePath, false);
		Tag toFileTag = buildDataObjInpTagForCopyDest(toFileAbsolutePath, resourceName, force);

		// now build the whole tag
		Tag message = new Tag(PI_TAG, new Tag[] { fromFileTag, toFileTag });

		return message;

	}

	private Tag buildDataObjInpTagForCopySource(final String fromFileAbsolutePath, final boolean force)
			throws JargonException {
		DataObjInp dataObjInp = DataObjInp.instanceForCopySource(fromFileAbsolutePath, force);
		return dataObjInp.getTagValue();
	}

	private Tag buildDataObjInpTagForCopyDest(final String destFileAbsolutePath, final String destResource,
			final boolean force) throws JargonException {
		DataObjInp dataObjInp = DataObjInp.instanceForCopyDest(destFileAbsolutePath, destResource, force);
		return dataObjInp.getTagValue();
	}

	public String getFromFileAbsolutePath() {
		return fromFileAbsolutePath;
	}

	public String getToFileAbsolutePath() {
		return toFileAbsolutePath;
	}

	public int getOperationType() {
		return operationType;
	}

	public String getResourceName() {
		return resourceName;
	}

	public long getSourceFileLength() {
		return sourceFileLength;
	}

}
