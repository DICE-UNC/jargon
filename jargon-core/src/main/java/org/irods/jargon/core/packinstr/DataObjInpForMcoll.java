package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Translation of a DataObjInp operation for management of special collections
 * (imcoll operations)
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObjInpForMcoll extends AbstractIRODSPackingInstruction {

	public static final int MCOLL_AN = 630;
	public static final String COLL_TYPE_LINK = "linkPoint";
	public static final String COLL_TYPE_MSSO = "mssoStructFile";

	private final String sourceFileAbsolutePath;
	private final String targetFileAbsolutePath;
	private final String collectionType;
	private final String destResourceName;
	private int operationType = 0;

	/**
	 * Create a packing instruction to mount a MSSO (microservice object)
	 * 
	 * @param microServiceSourceFile
	 *            <code>String</code> with the microservice source file
	 * @param targetFileAbsolutePath
	 *            <code>String</code> target path for the mounted collection
	 * @param destRescName
	 *            <code>String</code>, blank if unused, that describes the
	 *            destination resource name
	 * @return
	 */
	public static DataObjInpForMcoll instanceForMSSOMount(
			final String microServiceSourceFile,
			final String targetFileAbsolutePath, final String destRescName) {

		return new DataObjInpForMcoll(microServiceSourceFile,
				targetFileAbsolutePath, COLL_TYPE_MSSO, destRescName);
	}

	/**
	 * Create a packing instruction to mount a soft link
	 * 
	 * @param sourceFileAbsolutePath
	 *            <code>String</code> with the source path for the mounted
	 *            collection
	 * @param targetFileAbsolutePath
	 *            <code>String</code> target path for the mounted collection
	 * @param destRescName
	 *            <code>String</code>, blank if unused, that describes the
	 *            destination resource name
	 * @return
	 */
	public static DataObjInpForMcoll instanceForSoftLinkMount(
			final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String destRescName) {

		return new DataObjInpForMcoll(sourceFileAbsolutePath,
				targetFileAbsolutePath, COLL_TYPE_LINK, destRescName);
	}

	/**
	 * Private constructor, use the instance methods to create the proper
	 * instance.
	 * 
	 * @param sourceFileAbsolutePath
	 *            <code>String</code> with the source path for the mounted
	 *            collection
	 * @param targetFileAbsolutePath
	 *            <code>String</code> target path for the mounted collection
	 * @param collectionType
	 *            <code>String</code> with a collection type, as understood by
	 *            the iRODS imcoll protocol
	 * @param destRescName
	 *            <code>String</code>, blank if unused, that describes the
	 *            destination resource name
	 */
	private DataObjInpForMcoll(final String sourceFileAbsolutePath,
			final String targetFileAbsolutePath, final String collectionType,
			final String destRescName) {

		super();
		if (sourceFileAbsolutePath == null || sourceFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"sourceFileAbsolutePath is null or empty");
		}

		if (targetFileAbsolutePath == null || targetFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"targetFileAbsolutePath is null or empty");
		}

		if (collectionType == null || collectionType.isEmpty()) {
			throw new IllegalArgumentException(
					"collectionType is null or empty");
		}

		if (destRescName == null) {
			throw new IllegalArgumentException(
					"destRescName is null, set to blank if unused");
		}

		this.sourceFileAbsolutePath = sourceFileAbsolutePath;
		this.targetFileAbsolutePath = targetFileAbsolutePath;
		this.collectionType = collectionType;
		this.destResourceName = destRescName;
		this.setApiNumber(MCOLL_AN);

	}

	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(DataObjInp.PI_TAG, new Tag[] {
				new Tag(DataObjInp.OBJ_PATH, targetFileAbsolutePath),
				new Tag(DataObjInp.CREATE_MODE, 0),
				new Tag(DataObjInp.OPEN_FLAGS, 0),
				new Tag(DataObjInp.OFFSET, 0),
				new Tag(DataObjInp.DATA_SIZE, 0),
				new Tag(DataObjInp.NUM_THREADS, 0),
				new Tag(DataObjInp.OPR_TYPE, operationType) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();
		kvps.add(KeyValuePair.instance("collectionType", collectionType));

		if (this.collectionType.equals(COLL_TYPE_MSSO)) {
			kvps.add(KeyValuePair.instance("dataType", "msso file"));
		}

		kvps.add(KeyValuePair.instance("destRescName", destResourceName));
		kvps.add(KeyValuePair.instance("filePath", sourceFileAbsolutePath));

		message.addTag(createKeyValueTag(kvps));
		return message;
	}

	/*
	 * 
	 * 
	 * sending msg: <DataObjInp_PI>
	 * <objPath>/test1/home/test1/jargon-scratch/MountedCollectionAOImplForMSSOTest
	 * /testMountMSSOWorkflow/mounted</objPath> <createMode>0</createMode>
	 * <openFlags>0</openFlags> <offset>0</offset> <dataSize>0</dataSize>
	 * <numThreads>0</numThreads> <oprType>0</oprType> <KeyValPair_PI>
	 * <ssLen>4</ssLen> <keyWord>collectionType</keyWord>
	 * <keyWord>dataType</keyWord> <keyWord>destRescName</keyWord>
	 * <keyWord>filePath</keyWord> <svalue>mssoStructFile</svalue> <svalue>msso
	 * file</svalue> <svalue>test1-resc</svalue>
	 * <svalue>/test1/home/test1/jargon
	 * -scratch/MountedCollectionAOImplForMSSOTest
	 * /testMountMSSOWorkflow/eCWkflow.mss</svalue> </KeyValPair_PI>
	 * </DataObjInp_PI>
	 */

}
