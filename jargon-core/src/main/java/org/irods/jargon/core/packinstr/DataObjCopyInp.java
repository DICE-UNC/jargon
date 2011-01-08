/**
 * 
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

import edu.sdsc.grid.io.irods.Tag;

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

	private final String fromFileAbsolutePath;
	private final String toFileAbsolutePath;
	private final int operationType;

	/**
	 * Create an instance of the packing instruction for a move of a data object
	 * (not a collection, there is a different initializer for that).
	 * 
	 * @param sourceFileAbsolutePath
	 *            <code>String</code> with the absolute path to the source file.
	 * @param targetFileAbsolutePath
	 *            <code>String</code> with the absolute path to the target file.
	 * @return <code>DataObjCopyInp</code>
	 * @throws JargonException
	 */
	public static final DataObjCopyInp instanceForRenameFile(
			final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath) throws JargonException {

		DataObjCopyInp dataObjCopyInp = new DataObjCopyInp(
				sourceFileAbsolutePath, targetFileAbsolutePath,
				DataObjInp.RENAME_FILE_OPERATION_TYPE);
		dataObjCopyInp.setApiNumber(RENAME_FILE_API_NBR);
		return dataObjCopyInp;
	}

	/**
	 * Create an instance of the packing instruction for a move of a collection
	 * (not a data object, there is a different initializer for that).
	 * 
	 * @param sourceFileAbsolutePath
	 *            <code>String</code> with the absolute path to the source file.
	 * @param targetFileAbsolutePath
	 *            <code>String</code> with the absolute path to the target file.
	 * @return <code>DataObjCopyInp</code>
	 * @throws JargonException
	 */
	public static final DataObjCopyInp instanceForRenameCollection(
			final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath) throws JargonException {

		DataObjCopyInp dataObjCopyInp = new DataObjCopyInp(
				sourceFileAbsolutePath, targetFileAbsolutePath,
				DataObjInp.RENAME_DIRECTORY_OPERATION_TYPE);
		dataObjCopyInp.setApiNumber(RENAME_FILE_API_NBR);
		return dataObjCopyInp;
	}

	public static final DataObjCopyInp instance(
			final String fromFileAbsolutePath, final String toFileAbsolutePath,
			final int operationType) throws JargonException {
		return new DataObjCopyInp(fromFileAbsolutePath, toFileAbsolutePath,
				operationType);
	}

	private DataObjCopyInp(final String fromFileAbsolutePath,
			final String toFileAbsolutePath, final int operationType)
			throws JargonException {
		super();

		if (fromFileAbsolutePath == null || fromFileAbsolutePath.length() == 0) {
			throw new JargonException(
					"from file absolute path is null or empty");
		}

		if (toFileAbsolutePath == null || toFileAbsolutePath.length() == 0) {
			throw new JargonException("to file absolute path is null or empty");
		}

		if (operationType == DataObjInp.RENAME_DIRECTORY_OPERATION_TYPE
				|| operationType == DataObjInp.RENAME_FILE_OPERATION_TYPE) {
			// ok
		} else {
			throw new JargonException("unknown operation type:" + operationType);
		}

		this.fromFileAbsolutePath = fromFileAbsolutePath;
		this.toFileAbsolutePath = toFileAbsolutePath;
		this.operationType = operationType;
	}

	/**
	 * @return
	 */
	private Tag buildDataObjInpTag(final String filePath,
			final int operationType) throws JargonException {
		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		Tag fileTag = new Tag(DataObjInp.PI_TAG, new Tag[] {
				new Tag(DataObjInp.OBJ_PATH, filePath),
				new Tag(DataObjInp.CREATE_MODE, 0),
				new Tag(DataObjInp.OPEN_FLAGS, 0),
				new Tag(DataObjInp.OFFSET, 0),
				new Tag(DataObjInp.DATA_SIZE, 0),
				new Tag(DataObjInp.NUM_THREADS, 0),
				new Tag(DataObjInp.OPR_TYPE, operationType),
				this.createKeyValueTag(kvps) });
		return fileTag;
	}

	@Override
	public Tag getTagValue() throws JargonException {
		// get the DataObjInp tag for the from file
		Tag fromFileTag = buildDataObjInpTag(fromFileAbsolutePath,
				operationType);
		Tag toFileTag = buildDataObjInpTag(toFileAbsolutePath, operationType);

		// now build the whole tag
		Tag message = new Tag(PI_TAG, new Tag[] { fromFileTag, toFileTag });

		return message;

	}

}
