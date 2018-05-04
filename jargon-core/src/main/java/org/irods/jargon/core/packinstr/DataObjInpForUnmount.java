package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Translation of a DataObjInp operation for management of special collections
 * (imcoll operations). This specifically unmounts a special collection
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DataObjInpForUnmount extends AbstractIRODSPackingInstruction {

	public static final int MCOLL_AN = 630;

	private final String collectionToUnmountAbsolutePath;
	private final String destResourceName;

	private int operationType = 0;

	/**
	 * Create a packing instruction to unmount a special collection
	 *
	 * @param destResourceName
	 *            {@code String} with the absolute path for the mounted collection
	 *            to be unmounted
	 * @return {@link DataObjInpForUnmount}
	 */
	public static DataObjInpForUnmount instanceForUnmount(final String collectionToUnmountAbsolutePath,
			final String destResourceName) {

		return new DataObjInpForUnmount(collectionToUnmountAbsolutePath, destResourceName);
	}

	/**
	 * Private constructor, use the instance methods to create the proper instance.
	 *
	 * @param collectionToUnmountAbsolutePath
	 *            {@code String} with the absolute path to the collection to be
	 *            unmounted
	 */
	private DataObjInpForUnmount(final String collectionToUnmountAbsolutePath, final String destResourceName) {

		super();
		if (collectionToUnmountAbsolutePath == null || collectionToUnmountAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("collectionToUnmountAbsolutePath is null or empty");
		}

		if (destResourceName == null) {
			throw new IllegalArgumentException("destResourceName is null set to blank if unused");
		}

		this.collectionToUnmountAbsolutePath = collectionToUnmountAbsolutePath;
		this.destResourceName = destResourceName;
		setApiNumber(MCOLL_AN);

	}

	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(DataObjInp.PI_TAG,
				new Tag[] { new Tag(DataObjInp.OBJ_PATH, collectionToUnmountAbsolutePath),
						new Tag(DataObjInp.CREATE_MODE, 0), new Tag(DataObjInp.OPEN_FLAGS, 0),
						new Tag(DataObjInp.OFFSET, 0), new Tag(DataObjInp.DATA_SIZE, 0),
						new Tag(DataObjInp.NUM_THREADS, 0), new Tag(DataObjInp.OPR_TYPE, operationType) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();
		kvps.add(KeyValuePair.instance("collectionType", "unmount"));
		kvps.add(KeyValuePair.instance("destRescName", destResourceName));

		message.addTag(createKeyValueTag(kvps));
		return message;
	}

	/*
	 * 
	 * <DataObjInp_PI> <objPath>/test1/home/test1/linked</objPath>
	 * <createMode>0</createMode> <openFlags>0</openFlags> <offset>0</offset>
	 * <dataSize>0</dataSize> <numThreads>0</numThreads> <oprType>0</oprType>
	 * <KeyValPair_PI> <ssLen>2</ssLen> <keyWord>collectionType</keyWord>
	 * <keyWord>destRescName</keyWord> <svalue>unmount</svalue>
	 * <svalue>test1-resc</svalue> </KeyValPair_PI> </DataObjInp_PI>
	 */

}
