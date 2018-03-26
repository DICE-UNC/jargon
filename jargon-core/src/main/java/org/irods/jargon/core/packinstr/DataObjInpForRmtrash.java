/**
 * 
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.CollInpForEmptyTrash.TrashOperationMode;
import org.irods.jargon.core.pub.TrashOptions;

/**
 * DataObjInp for removing trash in irmtrash operations
 * 
 * @author conwaymc
 *
 */
public class DataObjInpForRmtrash extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = DataObjInp.PI_TAG;

	/**
	 * Path to delete
	 */
	private final String fileAbsolutePath;
	/**
	 * Detailed operation flags
	 */
	private final TrashOptions trashOptions;

	/**
	 * If a remote zone is involved, it must be set here, otherwise it can be left
	 * blank
	 */
	private final String zone;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue()
	 */
	@Override
	public Tag getTagValue() throws JargonException {
		Tag message = new Tag(PI_TAG,
				new Tag[] { new Tag(DataObjInp.OBJ_PATH, fileAbsolutePath), new Tag(DataObjInp.CREATE_MODE, 0),
						new Tag(DataObjInp.OPEN_FLAGS, 0), new Tag(DataObjInp.OFFSET, 0),
						new Tag(DataObjInp.DATA_SIZE, 0), new Tag(DataObjInp.NUM_THREADS, 0),
						new Tag(DataObjInp.OPR_TYPE, 26) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		kvps.add(KeyValuePair.instance(CollInp.FORCE_FLAG, ""));

		if (trashOptions.isRecursive()) {
			kvps.add(KeyValuePair.instance(CollInp.RECURSIVE_OPR, ""));
		}

		if (trashOptions.getTrashOperationMode() == TrashOperationMode.ADMIN) {
			kvps.add(KeyValuePair.instance(CollInpForEmptyTrash.ADMIN_RMTRASH_KW, ""));
		} else {
			kvps.add(KeyValuePair.instance(CollInpForEmptyTrash.RMTRASH_KW, ""));
		}

		/*
		 * If in a remote zone a zone kw needs to be supplied
		 * 
		 */

		if (!zone.isEmpty()) {
			kvps.add(KeyValuePair.instance(CollInpForEmptyTrash.ZONE_KW, zone));
		}

		/*
		 * Age is added if > 0
		 */

		if (trashOptions.getAgeInMinutes() > 0) {
			kvps.add(
					KeyValuePair.instance(CollInpForEmptyTrash.AGE_KW, String.valueOf(trashOptions.getAgeInMinutes())));
		}

		message.addTag(createKeyValueTag(kvps));
		return message;
	}

	/**
	 * Constructor with required values
	 * 
	 * @param fileAbsolutePath
	 *            {@link String} with path to be deleted
	 * @param trashOptions
	 *            {@link TrashOptions} with detailed parameters to control behavior
	 * @param zone
	 *            {@link String} with the target zone
	 */
	public DataObjInpForRmtrash(String fileAbsolutePath, TrashOptions trashOptions, String zone) {
		super();

		if (fileAbsolutePath == null || fileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileAbsolutePath");
		}

		if (trashOptions == null) {
			throw new IllegalArgumentException("null trashOptions");
		}

		this.fileAbsolutePath = fileAbsolutePath;
		this.trashOptions = trashOptions;
		this.zone = zone;
	}

}
