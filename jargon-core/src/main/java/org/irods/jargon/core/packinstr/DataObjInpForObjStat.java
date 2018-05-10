/**
 * #define DataObjInp_PI "str objPath[MAX_NAME_LEN]; int createMode; int openFlags; double offset; double dataSize; int numThreads; int oprType; struct *SpecColl_PI; struct KeyValPair_PI;"
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Translation of a DataObjInp operation to obtain a stat on an object into XML
 * protocol format.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DataObjInpForObjStat extends AbstractIRODSPackingInstruction {

	public static final int OBJ_STAT_API_NBR = 633;

	private String fileAbsolutePath = "";
	private long offset = 0L;
	private int operationType = 0;

	/**
	 * Create the DataObjInp packing instruction to get an object stat.
	 *
	 * @param fileAbsolutePath
	 *            {@code String} with the file absolute path.
	 * @return {@code DataObjInp} containing the necessary packing instruction
	 * @throws JargonException
	 *             for iRODS error
	 */
	public static final DataObjInpForObjStat instance(final String fileAbsolutePath) throws JargonException {
		return new DataObjInpForObjStat(fileAbsolutePath);
	}

	private DataObjInpForObjStat(final String fileAbsolutePath) throws JargonException {

		super();
		if (fileAbsolutePath == null || fileAbsolutePath.length() == 0) {
			throw new IllegalArgumentException("file absolute path is null or empty");
		}

		this.fileAbsolutePath = fileAbsolutePath;
		setApiNumber(OBJ_STAT_API_NBR);
	}

	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(DataObjInp.PI_TAG,
				new Tag[] { new Tag(DataObjInp.OBJ_PATH, getFileAbsolutePath()), new Tag(DataObjInp.CREATE_MODE, 0),
						new Tag(DataObjInp.OPEN_FLAGS, 0), new Tag(DataObjInp.OFFSET, offset),
						new Tag(DataObjInp.DATA_SIZE, 0), new Tag(DataObjInp.NUM_THREADS, 0),
						new Tag(DataObjInp.OPR_TYPE, operationType) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();
		message.addTag(createKeyValueTag(kvps));
		return message;
	}

	public String getFileAbsolutePath() {
		return fileAbsolutePath;
	}

}
