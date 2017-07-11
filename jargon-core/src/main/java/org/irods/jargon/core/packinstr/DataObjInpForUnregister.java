package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Translation of a DataObjInp operation into XML protocol format for file
 * unregistration
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DataObjInpForUnregister extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "DataObjInp_PI";
	public static final String OBJ_PATH = "objPath";
	public static final String CREATE_MODE = "createMode";
	public static final String OPEN_FLAGS = "openFlags";
	public static final String DATA_SIZE = "dataSize";
	public static final String OPR_TYPE = "oprType";
	public static final String OFFSET = "offset";
	public static final String NUM_THREADS = "numThreads";
	public static final String DATA_TYPE = "dataType";
	public static final String FORCE_FLAG_KW = "forceFlag";
	public static final String RESC_NAME = "rescName";
	public static final String MY_STR = "myStr";

	public static final int DELETE_FILE_API_NBR = 615;
	public static final String DATA_TYPE_GENERIC = "generic";

	public static final String BS_LEN = "bsLen";

	private String fileAbsolutePath = "";
	private boolean force = false;

	/**
	 * Specify a delete with the force option enabled
	 *
	 * @param fileAbsolutePath
	 *            {@code String} with the absolute path to the file/data
	 *            object to be deleted.
	 * @param force
	 *            {@code boolean} that indicates whether a force option
	 *            should be used
	 * @return {@code DataObjInp} containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInpForUnregister instanceForDelete(
			final String fileAbsolutePath, final boolean force)
					throws JargonException {
		return new DataObjInpForUnregister(fileAbsolutePath, force);

	}

	private DataObjInpForUnregister(final String fileAbsolutePath,
			final boolean force) {

		super();
		if (fileAbsolutePath == null || fileAbsolutePath.length() == 0) {
			throw new IllegalArgumentException(
					"file absolute path is null or empty");
		}
		this.fileAbsolutePath = fileAbsolutePath;
		this.force = force;
		setApiNumber(DELETE_FILE_API_NBR);

	}

	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(PI_TAG, new Tag[] {
				new Tag(OBJ_PATH, fileAbsolutePath), new Tag(CREATE_MODE, 0),
				new Tag(OPEN_FLAGS, 0), new Tag(OFFSET, 0),
				new Tag(DATA_SIZE, 0), new Tag(NUM_THREADS, 0),
				new Tag(OPR_TYPE, 26) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		if (force) {
			kvps.add(KeyValuePair.instance(FORCE_FLAG_KW, ""));
		}
		message.addTag(createKeyValueTag(kvps));
		return message;
	}

}
