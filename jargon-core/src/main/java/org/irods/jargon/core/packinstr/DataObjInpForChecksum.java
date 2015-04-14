/**
 * #define DataObjInp_PI "str objPath[MAX_NAME_LEN]; int createMode; int openFlags; double offset; double dataSize; int numThreads; int oprType; struct *SpecColl_PI; struct KeyValPair_PI;"
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.checksum.ChecksumValue;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translation of a DataObjInp operation into XML protocol format.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObjInpForChecksum extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "DataObjInp_PI";

	public static final String MY_STR = "myStr";
	public static final String LOCAL_PATH = "localPath";
	public static final String FORCE_CHECKSUM_KW = "forceChecksum";
	public static final String VERIFY_CHECKSUM_KW = "verifyChecksum";
	public static final String REPL_NUM_KW = "replNum";
	public static final String RESC_NAME_KW = "rescName";
	public static final int CHECKSUM_API_NBR = 629;

	private static Logger log = LoggerFactory
			.getLogger(DataObjInpForChecksum.class);

	private String fileAbsolutePath = "";

	/**
	 * Optional checksum value used for operations where a checksum validation
	 * is requested. This will be the computed checksum of the file in question.
	 * <p/>
	 * Can be set to <code>null</code> if no checksum is specified
	 */
	private final ChecksumValue checksumValue;
	private final ChecksumOptions checksumOptions;
	private final String resourceName;
	private final int replicaNumber;

	private DataObjInpForChecksum(final String fileAbsolutePath,
			final String resourceName, final int replicaNumber,
			final ChecksumOptions checksumOptions,
			final ChecksumValue checksumValue) {

		super();
		if (fileAbsolutePath == null || fileAbsolutePath.length() == 0) {
			throw new IllegalArgumentException(
					"file absolute path is null or empty");
		}

		if (resourceName == null) {
			throw new IllegalArgumentException(
					"resourceName is null, may be set to blank if not required");
		}

		if (checksumOptions == null) {
			throw new IllegalArgumentException("checksumOptions is null");
		}

		this.fileAbsolutePath = fileAbsolutePath;
		this.checksumOptions = checksumOptions;
		this.checksumValue = checksumValue;
		this.replicaNumber = replicaNumber;
		this.resourceName = resourceName;
	}

	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(PI_TAG, new Tag[] {
				new Tag(DataObjInp.OBJ_PATH, getFileAbsolutePath()),
				new Tag(DataObjInp.CREATE_MODE, 0),
				new Tag(DataObjInp.OPEN_FLAGS, 0),
				new Tag(DataObjInp.OFFSET, 0),
				new Tag(DataObjInp.DATA_SIZE, 0),
				new Tag(DataObjInp.NUM_THREADS, 0),
				new Tag(DataObjInp.OPR_TYPE, 0) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		/*
		 * if (getOperationType() == REPLICATE_OPERATION_TYPE &&
		 * isReplicationToAll()) { kvps.add(KeyValuePair.instance(ALL, "")); }
		 */

		message.addTag(createKeyValueTag(kvps));
		return message;
	}

	public String getFileAbsolutePath() {
		return fileAbsolutePath;
	}

}
