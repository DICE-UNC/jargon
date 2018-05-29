/**
 * #define DataObjInp_PI "str objPath[MAX_NAME_LEN]; int createMode; int openFlags; double offset; double dataSize; int numThreads; int oprType; struct *SpecColl_PI; struct KeyValPair_PI;"
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
public class DataObjInpForChecksum extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = DataObjInp.PI_TAG;

	public static final String MY_STR = DataObjInp.MY_STR;
	public static final String FORCE_CHECKSUM_KW = "forceChecksum";
	public static final String VERIFY_CHECKSUM_KW = "verifyChecksum";
	public static final String CHECKSUM_ALL_KW = "chksumAll";
	public static final String REPL_NUM_KW = "replNum";
	public static final String RESC_NAME_KW = "rescName";
	public static final String TRANSLATED_PATH_KW = "translatedPath";
	public static final int CHECKSUM_API_NBR = 629;

	private String fileAbsolutePath = "";

	/**
	 * Optional checksum value used for operations where a checksum validation is
	 * requested. This will be the computed checksum of the file in question.
	 * <p>
	 * Can be set to {@code null} if no checksum is specified
	 */
	private final ChecksumOptions checksumOptions;
	private final String resourceName;
	private final int replicaNumber;

	// public static DataObjInpForChecksum instanceForChecksumDataObject(final
	// String irodsAbsolutePath )

	private DataObjInpForChecksum(final String fileAbsolutePath, final String resourceName, final int replicaNumber,
			final ChecksumOptions checksumOptions) {

		super();
		if (fileAbsolutePath == null || fileAbsolutePath.length() == 0) {
			throw new IllegalArgumentException("file absolute path is null or empty");
		}

		if (resourceName == null) {
			throw new IllegalArgumentException("resourceName is null, may be set to blank if not required");
		}

		if (checksumOptions == null) {
			throw new IllegalArgumentException("checksumOptions is null");
		}

		this.fileAbsolutePath = fileAbsolutePath;
		this.checksumOptions = checksumOptions;
		this.replicaNumber = replicaNumber;
		this.resourceName = resourceName;
	}

	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(PI_TAG,
				new Tag[] { new Tag(DataObjInp.OBJ_PATH, getFileAbsolutePath()), new Tag(DataObjInp.CREATE_MODE, 0),
						new Tag(DataObjInp.OPEN_FLAGS, 0), new Tag(DataObjInp.OFFSET, 0),
						new Tag(DataObjInp.DATA_SIZE, 0), new Tag(DataObjInp.NUM_THREADS, 0),
						new Tag(DataObjInp.OPR_TYPE, 0) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		if (checksumOptions.isForce()) {
			kvps.add(KeyValuePair.instance(FORCE_CHECKSUM_KW, ""));
		}

		if (checksumOptions.isChecksumAllReplicas()) {
			kvps.add(KeyValuePair.instance(CHECKSUM_ALL_KW, ""));
		}

		if (checksumOptions.isVerifyChecksumInIcat()) {
			kvps.add(KeyValuePair.instance(VERIFY_CHECKSUM_KW, ""));
		}

		if (checksumOptions.isRecursive()) {
			kvps.add(KeyValuePair.instance(TRANSLATED_PATH_KW, ""));
		}

		/*
		 * If a resource name is provided, use that, otherwise look and see if a replica
		 * number is added
		 */

		if (!resourceName.isEmpty()) {
			kvps.add(KeyValuePair.instance(RESC_NAME_KW, resourceName));
		} else if (replicaNumber > -1) {
			kvps.add(KeyValuePair.instance(REPL_NUM_KW, resourceName));
		}

		/*
		 * if (getOperationType() == REPLICATE_OPERATION_TYPE && isReplicationToAll()) {
		 * kvps.add(KeyValuePair.instance(ALL, "")); }
		 */

		message.addTag(createKeyValueTag(kvps));
		return message;
	}

	public String getFileAbsolutePath() {
		return fileAbsolutePath;
	}

}
