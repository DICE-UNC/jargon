/**
 * 
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sdsc.grid.io.irods.Tag;

/**
 * Translation of a DataObjInp operation into XML protocol format.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObjInp extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "DataObjInp_PI";
	public static final String OBJ_PATH = "objPath";
	public static final String CREATE_MODE = "createMode";
	public static final String OPEN_FLAGS = "openFlags";
	public static final String DATA_SIZE = "dataSize";
	public static final String OPR_TYPE = "oprType";
	public static final String OFFSET = "offset";
	public static final String NUM_THREADS = "numThreads";
	public static final String DATA_TYPE = "dataType";
	public static final String DEST_RESC_NAME = "destRescName";
	public static final String FORCE_FLAG_KW = "forceFlag";
	public static final String DATA_INCLUDED_KW = "dataIncluded";
	public static final String RESC_NAME = "rescName";
	public static final String MY_STR = "myStr";
	public static final String ALL = "all";

	public static final int CREATE_FILE_API_NBR = 601;
	public static final int DELETE_FILE_API_NBR = 615;
	public static final int PHYMOVE_FILE_API_NBR = 631;
	public static final int OPEN_FILE_API_NBR = 602;
	public static final int PUT_FILE_API_NBR = 606;
	public static final int GET_FILE_API_NBR = 608;
	public static final int REPLICATE_API_NBR = 610;
	public static final int CHECKSUM_API_NBR = 629;

	public static final String DATA_TYPE_GENERIC = "generic";

	public static final int DEFAULT_OPERATION_TYPE = 0;

	public static final int RENAME_FILE_OPERATION_TYPE = 11;
	public static final int RENAME_DIRECTORY_OPERATION_TYPE = 12;
	public static final int PHYMOVE_OPERATION_TYPE = 15;
	public static final int PUT_OPERATION_TYPE = 1;
	public static final int GET_OPERATION_TYPE = 2;
	public static final int REPLICATE_OPERATION_TYPE = 6;

	private boolean initialPutGetCall = true;

	private static Logger log = LoggerFactory.getLogger(DataObjInp.class);

	public static final int DEFAULT_CREATE_MODE = 448;
	public static final int ZERO_CREATE_MODE = 0;

	public static final String BS_LEN = "bsLen";

	public enum OpenFlags {
		READ, WRITE, READ_WRITE
	}

	public enum ForceOptions {
		FORCE, NO_FORCE
	}

	private String fileAbsolutePath = "";
	private int createMode = DEFAULT_CREATE_MODE;
	private OpenFlags openFlags = null;
	private long offset = 0L;
	private long dataSize = 0L;
	private int numThreads = 0;
	private String resource = "";
	private ForceOptions forceOption = ForceOptions.NO_FORCE;
	private int operationType = 0;
	private boolean replicationToAll = false;

	/**
	 * Generic instance creation method with all constructor parameters. In this
	 * class are <code>instance()</code> methods that are specific to the
	 * desired operation, and are recommended. Some of these values are not used
	 * in certain protocol operations.
	 * 
	 * @param fileAbsolutePath
	 *            <code>String</code> with the file absolute path.
	 * @param createMode
	 *            <code>int</code> with the create mode.
	 * @param openFlags
	 *            <code>OpenFlags</code> enum value.
	 * @param offset
	 *            <code>long</code> with the offset into the data.
	 * @param dataSize
	 *            <code>long</code> with the data size.
	 * @param numThreads
	 *            <code> int</code> with the number of threads.
	 * @param resource
	 *            <code>String</code> with the given resource.
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instance(final String fileAbsolutePath,
			final int createMode, final OpenFlags openFlags, final long offset,
			final long dataSize, final int numThreads, final String resource)
			throws JargonException {
		return new DataObjInp(fileAbsolutePath, createMode, openFlags, offset,
				dataSize, numThreads, resource);
	}

	/**
	 * Specify a delete with the force option enabled
	 * 
	 * @param fileAbsolutePath
	 *            <code>String</code> with the absolute path to the file/data
	 *            object to be deleted.
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForDeleteWithNoForce(
			final String fileAbsolutePath) throws JargonException {
		DataObjInp dataObjInp = new DataObjInp(fileAbsolutePath,
				ZERO_CREATE_MODE, OpenFlags.READ, 0L, 0L, 0, "");
		dataObjInp.operationType = DEFAULT_OPERATION_TYPE;
		dataObjInp.setApiNumber(DELETE_FILE_API_NBR);
		return dataObjInp;
	}

	/**
	 * Specify a delete with the force option enabled
	 * 
	 * @param fileAbsolutePath
	 *            <code>String</code> with the absolute path to the file/data
	 *            object to be deleted.
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForDeleteWithForce(
			final String fileAbsolutePath) throws JargonException {
		DataObjInp dataObjInp = new DataObjInp(fileAbsolutePath,
				DEFAULT_CREATE_MODE, OpenFlags.READ, 0L, 0L, 0, "");
		dataObjInp.forceOption = DataObjInp.ForceOptions.FORCE;
		dataObjInp.operationType = DEFAULT_OPERATION_TYPE;
		return dataObjInp;
	}

	/**
	 * Specify a physical move where the resource is not supplied.
	 * 
	 * @param fileAbsolutePath
	 *            <code>String</code> with the absolute file path to the iRODS
	 *            file/collection to be moved
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForPhymove(
			final String fileAbsolutePath) throws JargonException {
		DataObjInp dataObjInp = new DataObjInp(fileAbsolutePath,
				DEFAULT_CREATE_MODE, OpenFlags.READ, 0L, 0L, 0, "");
		dataObjInp.forceOption = DataObjInp.ForceOptions.FORCE;
		dataObjInp.operationType = DEFAULT_OPERATION_TYPE;
		return dataObjInp;
	}

	/**
	 * Create an instance for replication of a file to a given resource.
	 * 
	 * @param fileAbsolutePath
	 *            <code>String</code> with the absolute path of the irodsFile to
	 *            replicate
	 * @param resource
	 *            <code>String</code> of the resource the file should be
	 *            replicated to.
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForReplicate(
			final String fileAbsolutePath, final String resource)
			throws JargonException {

		if (resource == null || resource.length() == 0) {
			throw new JargonException("null or missing destination resource");
		}

		DataObjInp dataObjInp = DataObjInp.instance(fileAbsolutePath, 0,
				OpenFlags.READ, 0L, 0L, 0, resource);
		dataObjInp.operationType = REPLICATE_OPERATION_TYPE;
		dataObjInp.setApiNumber(REPLICATE_API_NBR);
		return dataObjInp;

	}

	/**
	 * Create an instance for replication of a file to a given resource group.
	 * This will replicate to all resources in the resource group.
	 * 
	 * @param fileAbsolutePath
	 *            <code>String</code> with the absolute path of the irodsFile to
	 *            replicate
	 * @param resourceGroup
	 *            <code>String</code> of the resource group to which the file
	 *            will be replicated. (Replicates to all members of the group).
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForReplicateToResourceGroup(
			final String fileAbsolutePath, final String resourceGroup)
			throws JargonException {

		if (resourceGroup == null || resourceGroup.length() == 0) {
			throw new JargonException("null or missing resourceGroup");
		}

		if (fileAbsolutePath == null || fileAbsolutePath.length() == 0) {
			throw new JargonException("null or missing fileAbsolutePath");
		}

		DataObjInp dataObjInp = DataObjInp.instance(fileAbsolutePath, 0,
				OpenFlags.READ, 0L, 0L, 0, resourceGroup);
		dataObjInp.operationType = REPLICATE_OPERATION_TYPE;
		dataObjInp.setApiNumber(REPLICATE_API_NBR);
		dataObjInp.setReplicationToAll(true);

		return dataObjInp;

	}

	/**
	 * Create an instance of the protocol for a physical move operation.
	 * 
	 * @param fileAbsolutePath
	 *            <code>String</code> with the absolute path of the irodsFile to
	 *            move
	 * @param resource
	 *            <code>String</code> of the resource the file should be moved
	 *            to.
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForPhysicalMoveSpecifyingResource(
			final String fileAbsolutePath, final String resource)
			throws JargonException {

		if (resource == null || resource.length() == 0) {
			throw new JargonException("null or missing destination resource");
		}

		DataObjInp dataObjInp = DataObjInp.instanceForPhymove(fileAbsolutePath);
		dataObjInp.resource = resource;
		dataObjInp.operationType = PHYMOVE_OPERATION_TYPE;
		return dataObjInp;

	}

	/**
	 * Create an instance of the protocol to compute a checksum
	 * 
	 * @param dataObjectAbsolutePath
	 *            <code>String</code> with the iRODS absolute file path for the
	 *            data object upon which the checksum will be calculated.
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForDataObjectChecksum(
			final String dataObjectAbsolutePath) throws JargonException {

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new JargonException("dataObjectAbsolutePath is empty");
		}

		log.info("creating dataObjInp for a checksum operation on file:{}",
				dataObjectAbsolutePath);
		DataObjInp dataObjInp = new DataObjInp(dataObjectAbsolutePath,
				ZERO_CREATE_MODE, OpenFlags.READ, 0L, 0L, 0, "");
		dataObjInp.setApiNumber(CHECKSUM_API_NBR);
		return dataObjInp;
	}

	/**
	 * Create an instance of the protocol for a file open operation.
	 * 
	 * @param fileAbsolutePath
	 *            <code>String</code> with the physical path of the file to
	 *            open.
	 * @param openFlags
	 *            <code>OpenFlags</code> enum value.
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForOpen(
			final String fileAbsolutePath, final OpenFlags openFlags)
			throws JargonException {
		return new DataObjInp(fileAbsolutePath, DEFAULT_CREATE_MODE, openFlags,
				0L, 0L, 0, "");
	}

	/**
	 * Create the proper packing instruction for a put operatoin
	 * 
	 * @param destinationAbsolutePath
	 *            <code>String</code> with the absolute path to the file
	 * @param length
	 *            <code>long</code> with the length of the file
	 * @param destinationResource
	 *            <code>String</code> with the IRODS Resource where the file
	 *            will be placed.
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForInitialCallToPut(
			final String destinationAbsolutePath, final long length,
			final String destinationResource, final boolean overwrite)
			throws JargonException {
		if (destinationAbsolutePath == null
				|| destinationAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty destinationAbsolutePath");
		}

		if (destinationResource == null) {
			throw new JargonException("null destinationResource");
		}

		if (length < 0) {
			throw new JargonException("length is less than zero");
		}

		DataObjInp dataObjInp = new DataObjInp(destinationAbsolutePath,
				DEFAULT_CREATE_MODE, OpenFlags.WRITE, 0L, length, 0,
				destinationResource);
		dataObjInp.operationType = PUT_OPERATION_TYPE;
		dataObjInp.setApiNumber(PUT_FILE_API_NBR);

		if (overwrite) {
			dataObjInp.setForceOption(ForceOptions.FORCE);
		}

		dataObjInp.setInitialPutGetCall(true);

		return dataObjInp;
	}

	/**
	 * Create the proper packing instruction for a put operatoin FIXME: correct
	 * impl
	 * 
	 * @param destinationAbsolutePath
	 *            <code>String</code> with the absolute path to the file
	 * @param length
	 *            <code>long</code> with the length of the file
	 * @param destinationResource
	 *            <code>String</code> with the IRODS Resource where the file
	 *            will be placed.
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForNormalPutStrategy(
			final String destinationAbsolutePath, final long length,
			final String destinationResource, final boolean overwrite)
			throws JargonException {
		if (destinationAbsolutePath == null
				|| destinationAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty destinationAbsolutePath");
		}

		if (destinationResource == null) {
			throw new JargonException("null destinationResource");
		}

		if (length < 0) {
			throw new JargonException("length is less than zero");
		}

		DataObjInp dataObjInp = new DataObjInp(destinationAbsolutePath,
				DEFAULT_CREATE_MODE, OpenFlags.WRITE, 0L, length, 0,
				destinationResource);
		dataObjInp.operationType = PUT_OPERATION_TYPE;
		dataObjInp.setApiNumber(PUT_FILE_API_NBR);
		if (overwrite) {
			dataObjInp.setForceOption(ForceOptions.FORCE);
		}

		dataObjInp.setInitialPutGetCall(false);

		return dataObjInp;
	}

	/**
	 * Create the proper packing instruction for a get operation
	 * 
	 * @param sourceAbsolutePath
	 *            <code>String</code> with the absolute path to the file to get
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForGet(
			final String sourceAbsolutePath) throws JargonException {
		if (sourceAbsolutePath == null || sourceAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty sourceAbsolutePath");
		}

		DataObjInp dataObjInp = new DataObjInp(sourceAbsolutePath, 0,
				OpenFlags.READ, 0L, 0L, 0, "");
		dataObjInp.operationType = GET_OPERATION_TYPE;
		dataObjInp.setApiNumber(GET_FILE_API_NBR);

		return dataObjInp;
	}

	/**
	 * Create the proper packing instruction for a get operation specifying a
	 * resource
	 * 
	 * @param sourceAbsolutePath
	 *            <code>String</code> with the absolute path to the file to get
	 * @param resource
	 *            <code>String</code> with the resource that contains the file
	 *            that should be retrieved
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForGetSpecifyingResource(
			final String sourceAbsolutePath, final String resource)
			throws JargonException {

		if (sourceAbsolutePath == null || sourceAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty sourceAbsolutePath");
		}

		if (resource == null) {
			throw new JargonException("null resource");
		}

		DataObjInp dataObjInp = new DataObjInp(sourceAbsolutePath, 0,
				OpenFlags.READ, 0L, 0L, 0, resource);
		dataObjInp.operationType = GET_OPERATION_TYPE;
		dataObjInp.setApiNumber(GET_FILE_API_NBR);

		return dataObjInp;
	}

	private DataObjInp(final String fileAbsolutePath, final int createMode,
			final OpenFlags openFlags, final long offset, final long dataSize,
			final int numThreads, final String resource) throws JargonException {
		super();
		if (fileAbsolutePath == null || fileAbsolutePath.length() == 0) {
			throw new JargonException("file absolute path is null or empty");
		}

		if (dataSize < 0) {
			throw new JargonException("negative data size");
		}

		if (numThreads < 0) {
			throw new JargonException("num threads is negative");
		}

		if (offset > dataSize) {
			throw new JargonException("offset is greater than data size");
		}

		if (createMode < 0) {
			throw new JargonException("invalid create mode:" + createMode);
		}

		if (resource == null) {
			throw new JargonException(
					"resource is null, may be set to blank if not required");
		}

		if (openFlags == null) {
			throw new JargonException("null open flags");
		}

		this.fileAbsolutePath = fileAbsolutePath;
		this.createMode = createMode;
		this.openFlags = openFlags;
		this.offset = offset;
		this.dataSize = dataSize;
		this.numThreads = numThreads;
		this.resource = resource;
		this.forceOption = DataObjInp.ForceOptions.NO_FORCE;

	}

	@Override
	public Tag getTagValue() throws JargonException {
		int tagOpenFlags = translateOpenFlagsValue();

		Tag message = new Tag(PI_TAG, new Tag[] {
				new Tag(OBJ_PATH, getFileAbsolutePath()),
				new Tag(CREATE_MODE, getCreateMode()),
				new Tag(OPEN_FLAGS, tagOpenFlags),
				new Tag(OFFSET, getOffset()),
				new Tag(DATA_SIZE, getDataSize()),
				new Tag(NUM_THREADS, getNumThreads()),
				new Tag(OPR_TYPE, getOperationType()) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		if (this.getApiNumber() == DataObjInp.PUT_FILE_API_NBR) {
			// for puts, data included not put in the initial
			// call

			kvps.add(KeyValuePair.instance(DATA_TYPE, DATA_TYPE_GENERIC));
			if (!isInitialPutGetCall()) {
				kvps.add(KeyValuePair.instance(DATA_INCLUDED_KW, ""));
			}
		}

		if (forceOption == ForceOptions.FORCE) {
			kvps.add(KeyValuePair.instance(FORCE_FLAG_KW, ""));
		}

		if (this.getOperationType() == REPLICATE_OPERATION_TYPE
				&& isReplicationToAll()) {
			kvps.add(KeyValuePair.instance(ALL, ""));
		}

		// add a keyword tag for resource if a resource was given to the packing
		// instruction.
		if (getResource().length() > 0) {
			if (this.getApiNumber() == DataObjInp.GET_FILE_API_NBR) {
				kvps.add(KeyValuePair.instance(RESC_NAME, getResource()));
			} else {
				kvps.add(KeyValuePair.instance(DEST_RESC_NAME, getResource()));
			}
		}

		message.addTag(createKeyValueTag(kvps));
		return message;
	}

	/**
	 * @return
	 * @throws JargonException
	 */
	private int translateOpenFlagsValue() throws JargonException {
		int tagOpenFlags = 0;

		if (getOpenFlags() == OpenFlags.READ) {
			tagOpenFlags = 0;
		} else if (getOpenFlags() == OpenFlags.WRITE) {
			tagOpenFlags = 1;
		} else if (getOpenFlags() == OpenFlags.READ_WRITE) {
			tagOpenFlags = 2;
		} else {
			throw new JargonException("invalid open flags:" + getOpenFlags());
		}
		return tagOpenFlags;
	}

	public String getFileAbsolutePath() {
		return fileAbsolutePath;
	}

	public int getCreateMode() {
		return createMode;
	}

	public OpenFlags getOpenFlags() {
		return openFlags;
	}

	public long getOffset() {
		return offset;
	}

	public long getDataSize() {
		return dataSize;
	}

	public int getNumThreads() {
		return numThreads;
	}

	protected String getResource() {
		return resource;
	}

	public ForceOptions getForceOptions() {
		return forceOption;
	}

	public int getOperationType() {
		return operationType;
	}

	public ForceOptions getForceOption() {
		return forceOption;
	}

	protected void setForceOption(final ForceOptions forceOption) {
		this.forceOption = forceOption;
	}

	public boolean isReplicationToAll() {
		return replicationToAll;
	}

	public void setReplicationToAll(final boolean replicationToAll) {
		this.replicationToAll = replicationToAll;
	}

	public boolean isInitialPutGetCall() {
		return initialPutGetCall;
	}

	public void setInitialPutGetCall(final boolean initialPutGetCall) {
		this.initialPutGetCall = initialPutGetCall;
	}

}
