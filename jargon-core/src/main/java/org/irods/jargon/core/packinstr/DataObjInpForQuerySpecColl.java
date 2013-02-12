/**
 * #define DataObjInp_PI "str objPath[MAX_NAME_LEN]; int createMode; int openFlags; double offset; double dataSize; int numThreads; int oprType; struct *SpecColl_PI; struct KeyValPair_PI;"
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Translation of a DataObjInp operation to query a special collection
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObjInpForQuerySpecColl extends AbstractIRODSPackingInstruction {

	public static final int QUERY_SPEC_COLL_API_NBR = 645;

	private String fileAbsolutePath = "";
	private long offset = 0L;
	private int operationType = 0;
	private String selObjType = null;
	private SpecColInfo specColInfo;

	/**
	 * Create the DataObjInp packing instruction to query data objects in
	 * special collections
	 * 
	 * @param fileAbsolutePath
	 *            <code>String</code> with the file absolute path.
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInpForQuerySpecColl instanceQueryDataObj(
			final String fileAbsolutePath) throws JargonException {
		return new DataObjInpForQuerySpecColl(fileAbsolutePath, "dataObj", null);
	}

	/**
	 * Create the DataObjInp packing instruction to query collections in special
	 * collections
	 * 
	 * @param fileAbsolutePath
	 *            <code>String</code> with the file absolute path.
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInpForQuerySpecColl instanceQueryCollections(
			final String fileAbsolutePath, final SpecColInfo specColInfo)
			throws JargonException {
		if (specColInfo == null) {
			throw new IllegalArgumentException("null specColInfo");
		}

		return new DataObjInpForQuerySpecColl(fileAbsolutePath, "collection",
				specColInfo);
	}

	private DataObjInpForQuerySpecColl(final String fileAbsolutePath,
			final String selObjType, final SpecColInfo specColInfo)
			throws JargonException {

		super();
		if (fileAbsolutePath == null || fileAbsolutePath.length() == 0) {
			throw new IllegalArgumentException(
					"file absolute path is null or empty");
		}

		if (selObjType == null || selObjType.length() == 0) {
			throw new IllegalArgumentException("selObjType is null or empty");
		}

		this.fileAbsolutePath = fileAbsolutePath;
		this.selObjType = selObjType;
		this.specColInfo = specColInfo;
		this.setApiNumber(QUERY_SPEC_COLL_API_NBR);
	}

	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(DataObjInp.PI_TAG, new Tag[] {
				new Tag(DataObjInp.OBJ_PATH, getFileAbsolutePath()),
				new Tag(DataObjInp.CREATE_MODE, 0),
				new Tag(DataObjInp.OPEN_FLAGS, 0),
				new Tag(DataObjInp.OFFSET, offset),
				new Tag(DataObjInp.DATA_SIZE, 0),
				new Tag(DataObjInp.NUM_THREADS, 0),
				new Tag(DataObjInp.OPR_TYPE, operationType) });

		if (specColInfo != null) {
			message.addTag(this.createSpecCollTag(specColInfo));
		}

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();
		kvps.add(KeyValuePair.instance("selObjType", selObjType));
		message.addTag(createKeyValueTag(kvps));
		return message;
	}

	public String getFileAbsolutePath() {
		return fileAbsolutePath;
	}

}
