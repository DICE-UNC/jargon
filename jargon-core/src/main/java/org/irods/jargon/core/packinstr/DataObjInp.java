/**
 * #define DataObjInp_PI "str objPath[MAX_NAME_LEN]; int createMode; int openFlags; double offset; double dataSize; int numThreads; int oprType; struct *SpecColl_PI; struct KeyValPair_PI;"
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.checksum.ChecksumValue;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions.PutOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public static final String LOCAL_PATH = "localPath";
	public static final String ALL = "all";

	public static final int TRUNCATE = 512;

	public static final int CREATE_FILE_API_NBR = 601;
	public static final int DELETE_FILE_API_NBR = 615;
	public static final int PHYMOVE_FILE_API_NBR = 631;
	public static final int PHYMOVE_FILE_API_NBR_41 = 697;
	public static final int OPEN_FILE_API_NBR = 602;
	public static final int PUT_FILE_API_NBR = 606;
	public static final int GET_FILE_API_NBR = 608;
	public static final int REPLICATE_API_NBR = 610;
	public static final int CHECKSUM_API_NBR = 629;
	public static final int GET_HOST_FOR_GET_API_NBR = 694;
	public static final int GET_HOST_FOR_PUT_API_NBR = 686;

	public static final String DATA_TYPE_GENERIC = "generic";
	public static final String DATA_TYPE_MSSO = "msso file";

	public static final int DEFAULT_OPERATION_TYPE = 0;

	public static final int COPY_FILE_SRC_OPERATION_TYPE = 10;
	public static final int COPY_FILE_DEST_OPERATION_TYPE = 9;
	public static final int RENAME_FILE_OPERATION_TYPE = 11;
	public static final int RENAME_DIRECTORY_OPERATION_TYPE = 12;
	public static final int PHYMOVE_OPERATION_TYPE = 15;
	public static final int PUT_OPERATION_TYPE = 1;
	public static final int GET_OPERATION_TYPE = 2;
	public static final int REPLICATE_OPERATION_TYPE = 6;

	private boolean initialPutGetCall = true;

	private static Logger log = LoggerFactory.getLogger(DataObjInp.class);

	public static final int DEFAULT_CREATE_MODE = 33188;
	public static final int EXEC_CREATE_MODE = 33261;
	public static final int ZERO_CREATE_MODE = 0;

	public static final String BS_LEN = "bsLen";
	private static final int REPLICATE_API_NBR_410 = 695;

	/*
	 * (Supported modes are:
	 * 
	 * READ 'r' Open for reading only; place the file pointer at the beginning
	 * of the file.
	 * 
	 * READ_TRUNCATE 'r+' Open for reading and writing; place the file pointer
	 * at the beginning of the file.
	 * 
	 * WRITE 'w' Open for writing only; place the file pointer at the beginning
	 * of the file and truncate the file to zero length. If the file does not
	 * exist, attempt to create it.
	 * 
	 * WRITE_TRUNCATE 'w+' Open for reading and writing; place the file pointer
	 * at the beginning of the file and truncate the file to zero length. If the
	 * file does not exist, attempt to create it.
	 * 
	 * READ_WRITE 'a' Open for writing only; place the file pointer at the end
	 * of the file. If the file does not exist, attempt to create it.
	 * 
	 * READ_WRITE_CREATE_IF_NOT_EXISTS 'a+' Open for reading and writing; place
	 * the file pointer at the end of the file. If the file does not exist,
	 * attempt to create it.
	 * 
	 * WRITE_FAIL_IF_EXISTS 'x' Create and open for writing only; place the file
	 * pointer at the beginning of the file. If the file already exists, the
	 * fopen() call will fail by returning FALSE and generating an error of
	 * level E_WARNING. If the file does not exist, attempt to create it. This
	 * is equivalent to specifying O_EXCL|O_CREAT flags for the underlying
	 * open(2) system call.
	 * 
	 * READ_WRITE_FAIL_IF_EXISTS 'x+' Create and open for reading and writing;
	 * place the file pointer at the beginning of the file. If the file already
	 * exists, the fopen() call will fail by returning FALSE and generating an
	 * error of level E_WARNING. If the file does not exist, attempt to create
	 * it. This is equivalent to specifying O_EXCL|O_CREAT flags for the
	 * underlying open(2) system call.
	 */

	public enum OpenFlags {
		READ, WRITE, READ_WRITE, READ_TRUNCATE, WRITE_TRUNCATE, READ_WRITE_CREATE_IF_NOT_EXISTS, WRITE_FAIL_IF_EXISTS, READ_WRITE_FAIL_IF_EXISTS
	}

	public enum ForceOptions {
		FORCE, NO_FORCE
	}

	private String fileAbsolutePath = "";
	private String localPath = "";
	private int createMode = DEFAULT_CREATE_MODE;
	private OpenFlags openFlags = null;
	private long offset = 0L;
	private long dataSize = 0L;
	private String resource = "";
	private ForceOptions forceOption = ForceOptions.NO_FORCE;
	private int operationType = 0;
	private boolean replicationToAll = false;
	private TransferOptions transferOptions;
	/**
	 * Optional checksum value used for operations where a checksum validation
	 * is requested. This will be the computed checksum of the file in question.
	 * <p/>
	 * Can be set to <code>null</code> if no checksum is specified
	 */
	private ChecksumValue fileChecksumValue = null;

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
	 * @param resource
	 *            <code>String</code> with the given resource.
	 * @param transferOptions
	 *            {@link TransferOptions} that configures details about the
	 *            underlying technique used in the transfer. Can be set to null
	 *            if not desired.
	 *
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instance(final String fileAbsolutePath,
			final int createMode, final OpenFlags openFlags, final long offset,
			final long dataSize, final String resource,
			final TransferOptions transferOptions) throws JargonException {
		return new DataObjInp(fileAbsolutePath, createMode, openFlags, offset,
				dataSize, resource, transferOptions);
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
				ZERO_CREATE_MODE, OpenFlags.READ, 0L, 0L, "", null);
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
				DEFAULT_CREATE_MODE, OpenFlags.READ, 0L, 0L, "", null);
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
				DEFAULT_CREATE_MODE, OpenFlags.READ, 0L, 0L, "", null);
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
				OpenFlags.READ, 0L, 0L, resource, null);
		dataObjInp.operationType = REPLICATE_OPERATION_TYPE;
		dataObjInp.setApiNumber(REPLICATE_API_NBR);
		return dataObjInp;

	}

	/**
	 * Replicate packing instruction for iRODS 4.1
	 *
	 * @param irodsFileAbsolutePath
	 * @param targetResource
	 * @return
	 * @throws JargonException
	 */
	public static DataObjInp instanceForReplicate410(
			String irodsFileAbsolutePath, String targetResource)
			throws JargonException {
		if (targetResource == null || targetResource.length() == 0) {
			throw new JargonException("null or missing destination resource");
		}

		DataObjInp dataObjInp = DataObjInp.instance(irodsFileAbsolutePath, 0,
				OpenFlags.READ, 0L, 0L, targetResource, null);
		dataObjInp.operationType = REPLICATE_OPERATION_TYPE;
		dataObjInp.setApiNumber(REPLICATE_API_NBR_410);
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
				OpenFlags.READ, 0L, 0L, resourceGroup, null);
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
				ZERO_CREATE_MODE, OpenFlags.READ, 0L, 0L, "", null);
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
		DataObjInp dataObjInp = new DataObjInp(fileAbsolutePath,
				DEFAULT_CREATE_MODE, openFlags, 0L, 0L, "", null);
		if (openFlags == OpenFlags.WRITE
				|| openFlags == OpenFlags.WRITE_FAIL_IF_EXISTS
				|| openFlags == OpenFlags.WRITE_TRUNCATE) {
			dataObjInp.setOperationType(PUT_OPERATION_TYPE);
		}
		return dataObjInp;
	}

	/**
	 * Create the proper packing instruction for the initial call that starts a
	 * put operation. The iRODS response will indicate the mode for the actual
	 * transmission (include the data in the byte stream, use parallel transfer,
	 * etc). Jargon will interpret the guidance given by iRODS to effect the
	 * actual data transmission.
	 *
	 * @param destinationAbsolutePath
	 *            <code>String</code> with the absolute path to the file
	 * @param length
	 *            <code>long</code> with the length of the file
	 * @param destinationResource
	 *            <code>String</code> with the IRODS Resource where the file
	 *            will be placed.
	 * @param overwrite
	 *            <code>boolean</code> that indicates that a force option will
	 *            be used.
	 * @param transferOptions
	 *            {@link TransferOptions} that configures details about the
	 *            underlying technique used in the transfer. Can be set to null
	 *            if not desired.
	 * @param execFlag
	 *            <code>boolean</code> that indicates that the execBit should be
	 *            preserved
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 *
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForInitialCallToPut(
			final String destinationAbsolutePath, final long length,
			final String destinationResource, final boolean overwrite,
			final TransferOptions transferOptions, final boolean execFlag)
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

		int createMode = DEFAULT_CREATE_MODE;
		if (execFlag) {
			createMode = EXEC_CREATE_MODE;
		}

		DataObjInp dataObjInp = new DataObjInp(destinationAbsolutePath,
				createMode, OpenFlags.READ_WRITE, 0L, length,
				destinationResource, transferOptions);
		dataObjInp.operationType = PUT_OPERATION_TYPE;
		dataObjInp.setApiNumber(PUT_FILE_API_NBR);

		if (overwrite) {
			dataObjInp.setForceOption(ForceOptions.FORCE);
		}

		dataObjInp.setInitialPutGetCall(true);

		return dataObjInp;
	}

	/**
	 * Create an instance of the packing instruction for a parallel put transfer
	 *
	 * @param destinationAbsolutePath
	 *            <code>String</code> with the absolute path to the file
	 * @param length
	 *            <code>long</code> with the length of the file
	 * @param destinationResource
	 *            <code>String</code> with the IRODS Resource where the file
	 *            will be placed.
	 * @param overwrite
	 *            <code>boolean</code> that indicates that a force option will
	 *            be used.
	 * @param transferOptions
	 *            {@link TransferOptions} that configures details about the
	 *            underlying technique used in the transfer. Can be set to null
	 *            if not desired.
	 * @param execFlag
	 *            <code>boolean</code> that indicates that the exec bit should
	 *            be preserved
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForParallelPut(
			final String destinationAbsolutePath, final long length,
			final String destinationResource, final boolean overwrite,
			final TransferOptions transferOptions, final boolean execFlag)
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

		int createMode = DEFAULT_CREATE_MODE;
		if (execFlag) {
			createMode = EXEC_CREATE_MODE;
		}

		DataObjInp dataObjInp = new DataObjInp(destinationAbsolutePath,
				createMode, OpenFlags.READ_WRITE, 0L, length,
				destinationResource, transferOptions);
		dataObjInp.operationType = PUT_OPERATION_TYPE;
		dataObjInp.setApiNumber(PUT_FILE_API_NBR);

		if (overwrite) {
			dataObjInp.setForceOption(ForceOptions.FORCE);
		}

		dataObjInp.setInitialPutGetCall(true);

		return dataObjInp;
	}

	/**
	 * Create the proper packing instruction for a put operation. This method is
	 * used for a response after calling
	 * <code>instanceForInitialCallToPut()</code>, and is used when the data is
	 * to be included in the binary response (e.g. no parallel file transfer or
	 * other strategy required).
	 *
	 * @param destinationAbsolutePath
	 *            <code>String</code> with the absolute path to the file
	 * @param length
	 *            <code>long</code> with the length of the file
	 * @param destinationResource
	 *            <code>String</code> with the IRODS Resource where the file
	 *            will be placed.
	 * @param overwrite
	 *            <code>boolean</code> that indicates that a force option will
	 *            be used.
	 * @param transferOptions
	 *            {@link TransferOptions} that configures details about the
	 *            underlying technique used in the transfer. Can be set to null
	 *            if not desired.
	 * @param execFlag
	 *            <code>boolean</code> that indicates that the exec bit should
	 *            be preserved
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForNormalPutStrategy(
			final String destinationAbsolutePath, final long length,
			final String destinationResource, final boolean overwrite,
			final TransferOptions transferOptions, final boolean execFlag)
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

		int createMode = DEFAULT_CREATE_MODE;
		if (execFlag) {
			createMode = EXEC_CREATE_MODE;
		}

		DataObjInp dataObjInp = new DataObjInp(destinationAbsolutePath,
				createMode, OpenFlags.READ_WRITE, 0L, length,
				destinationResource, transferOptions);
		dataObjInp.operationType = PUT_OPERATION_TYPE;
		dataObjInp.setApiNumber(PUT_FILE_API_NBR);
		if (overwrite) {
			dataObjInp.setForceOption(ForceOptions.FORCE);
		}

		dataObjInp.setInitialPutGetCall(false);

		return dataObjInp;
	}

	public static final DataObjInp instanceForCopyDest(
			final String destinationAbsolutePath,
			final String destinationResource, final boolean overwrite)
			throws JargonException {
		DataObjInp dataObjInp = new DataObjInp(destinationAbsolutePath,
				ZERO_CREATE_MODE, OpenFlags.READ, 0L, 0L, destinationResource,
				null);
		dataObjInp.operationType = DataObjInp.COPY_FILE_DEST_OPERATION_TYPE;
		if (overwrite) {
			dataObjInp.setForceOption(ForceOptions.FORCE);
		}
		return dataObjInp;
	}

	/**
	 * Create the proper packing instruction for a get operation
	 *
	 * @param sourceAbsolutePath
	 *            <code>String</code> with the absolute path to the file to get
	 * @param dataObjectSize
	 *            <code>long</code> with the size of the data object to retrieve
	 * @param transferOptions
	 *            {@link TransferOptions} that configures details about the
	 *            underlying technique used in the transfer. Can be set to null
	 *            if not desired.
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForGet(
			final String sourceAbsolutePath, final long dataObjectSize,
			final TransferOptions transferOptions) throws JargonException {
		if (sourceAbsolutePath == null || sourceAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty sourceAbsolutePath");
		}

		DataObjInp dataObjInp = new DataObjInp(sourceAbsolutePath, 0,
				OpenFlags.READ, 0L, dataObjectSize, "", transferOptions);
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
	 * @param localPath
	 *            <code>String</code> with the absolute path to the local file
	 * @param transferOptions
	 *            {@link TransferOptions} that configures details about the
	 *            underlying technique used in the transfer. Can be set to null
	 *            if not desired.
	 * @return <code>DataObjInp</code> containing the necessary packing
	 *         instruction
	 * @throws JargonException
	 */
	public static final DataObjInp instanceForGetSpecifyingResource(
			final String sourceAbsolutePath, final String resource,
			final String localPath, final TransferOptions transferOptions)
			throws JargonException {

		if (sourceAbsolutePath == null || sourceAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty sourceAbsolutePath");
		}

		if (resource == null) {
			throw new JargonException("null resource");
		}

		if (localPath == null) {
			throw new IllegalArgumentException(
					"localPath is null, set to spaces if not used");
		}

		DataObjInp dataObjInp = new DataObjInp(sourceAbsolutePath, 0,
				OpenFlags.READ, 0L, 0L, resource, transferOptions);
		dataObjInp.operationType = GET_OPERATION_TYPE;
		dataObjInp.setApiNumber(GET_FILE_API_NBR);
		dataObjInp.setLocalPath(localPath);

		return dataObjInp;
	}

	/**
	 * Create a packing instruction to inquire about the correct host to use for
	 * a get. This supports re-routing a connection when data resides on a
	 * different resource server.
	 *
	 * @param sourceAbsolutePath
	 *            <code>String</code> with the absolute path to the file to get
	 * @param resource
	 *            <code>String</code> with the resource that contains the file
	 *            that should be retrieved
	 * @return
	 * @throws JargonException
	 */
	public static DataObjInp instanceForGetHostForGet(
			final String sourceAbsolutePath, final String resource)
			throws JargonException {

		if (sourceAbsolutePath == null || sourceAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty sourceAbsolutePath");
		}

		if (resource == null) {
			throw new JargonException("null resource");
		}

		DataObjInp dataObjInp = new DataObjInp(sourceAbsolutePath, 0,
				OpenFlags.READ, 0L, 0L, resource, null);
		dataObjInp.operationType = GET_OPERATION_TYPE;
		dataObjInp.setApiNumber(GET_HOST_FOR_GET_API_NBR);

		return dataObjInp;
	}

	/**
	 * Create a packing instruction to inquire about the correct host to use for
	 * a put. This supports re-routing a connection when data resides on a
	 * different resource server.
	 *
	 * @param sourceAbsolutePath
	 *            <code>String</code> with the absolute path to the file to put
	 * @param resource
	 *            <code>String</code> with the resource that contains the file
	 *            that should be retrieved
	 * @return
	 * @throws JargonException
	 */
	public static DataObjInp instanceForGetHostForPut(
			final String sourceAbsolutePath, final String resource)
			throws JargonException {

		if (sourceAbsolutePath == null || sourceAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty sourceAbsolutePath");
		}

		if (resource == null) {
			throw new JargonException("null resource");
		}

		DataObjInp dataObjInp = new DataObjInp(sourceAbsolutePath, 0,
				OpenFlags.READ, 0L, 0L, resource, null);
		dataObjInp.operationType = PUT_OPERATION_TYPE;
		dataObjInp.setApiNumber(GET_HOST_FOR_PUT_API_NBR);

		return dataObjInp;
	}

	private DataObjInp(final String fileAbsolutePath, final int createMode,
			final OpenFlags openFlags, final long offset, final long dataSize,
			final String resource, final TransferOptions transferOptions)
			throws JargonException {

		super();
		if (fileAbsolutePath == null || fileAbsolutePath.length() == 0) {
			throw new JargonException("file absolute path is null or empty");
		}

		if (dataSize < 0) {
			throw new JargonException("negative data size");
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
		this.resource = resource;
		forceOption = DataObjInp.ForceOptions.NO_FORCE;
		this.transferOptions = transferOptions;

	}

	@Override
	public Tag getTagValue() throws JargonException {
		int tagOpenFlags = translateOpenFlagsValue();
		int transferOptionsNumThreads = 0;

		if (transferOptions != null) {
			if (getApiNumber() == DataObjInp.PUT_FILE_API_NBR
					|| getApiNumber() == DataObjInp.GET_FILE_API_NBR) {
				transferOptionsNumThreads = transferOptions.getMaxThreads();
			}
		}

		Tag message = new Tag(PI_TAG, new Tag[] {
				new Tag(OBJ_PATH, getFileAbsolutePath()),
				new Tag(CREATE_MODE, getCreateMode()),
				new Tag(OPEN_FLAGS, tagOpenFlags),
				new Tag(OFFSET, getOffset()),
				new Tag(DATA_SIZE, getDataSize()),
				new Tag(NUM_THREADS, transferOptionsNumThreads),
				new Tag(OPR_TYPE, getOperationType()) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		if (getApiNumber() == DataObjInp.PUT_FILE_API_NBR) {
			processPutOperationKvps(transferOptionsNumThreads, kvps);
		}

		if (forceOption == ForceOptions.FORCE) {
			kvps.add(KeyValuePair.instance(FORCE_FLAG_KW, ""));
		}

		if (getOperationType() == REPLICATE_OPERATION_TYPE
				&& isReplicationToAll()) {
			kvps.add(KeyValuePair.instance(ALL, ""));
		}

		if (!getLocalPath().isEmpty()) {
			kvps.add(KeyValuePair.instance(LOCAL_PATH, getLocalPath()));
		}

		// add a keyword tag for resource if a resource was given to the packing
		// instruction.
		if (getResource().length() > 0) {
			if (getApiNumber() == DataObjInp.GET_FILE_API_NBR
					|| getApiNumber() == DataObjInp.GET_HOST_FOR_GET_API_NBR
					|| getApiNumber() == DataObjInp.GET_HOST_FOR_PUT_API_NBR) {
				kvps.add(KeyValuePair.instance(RESC_NAME, getResource()));
			} else {
				kvps.add(KeyValuePair.instance(DEST_RESC_NAME, getResource()));
			}
		}

		message.addTag(createKeyValueTag(kvps));
		return message;
	}

	/**
	 * @param transferOptionsNumThreads
	 * @param kvps
	 * @throws JargonException
	 */
	private void processPutOperationKvps(final int transferOptionsNumThreads,
			final List<KeyValuePair> kvps) throws JargonException {

		if (transferOptions == null) {
			kvps.add(KeyValuePair.instance(DATA_TYPE, DATA_TYPE_GENERIC));
		} else if (transferOptions.getPutOption() == PutOptions.NORMAL) {
			kvps.add(KeyValuePair.instance(DATA_TYPE, DATA_TYPE_GENERIC));
		} else if (transferOptions.getPutOption() == PutOptions.MSSO_FILE) {
			kvps.add(KeyValuePair.instance(DATA_TYPE, DATA_TYPE_MSSO));
		}

		if (!isInitialPutGetCall()) {
			kvps.add(KeyValuePair.instance(DATA_INCLUDED_KW, ""));
		}

		if (transferOptions == null) {
			return;
		}
		// transfer options passed, in, use in put operation kvps

		if (transferOptions.isComputeAndVerifyChecksumAfterTransfer()
				|| transferOptions.isComputeChecksumAfterTransfer()) {
			if (fileChecksumValue == null) {
				throw new JargonException(
						"no fileChecksumValue set, call the setter with the encoded checksum value");
			}
			log.info("local file checksum is:{}", getFileChecksumValue());

			// verify (if true) overrides a plain compute

			if (transferOptions.isComputeAndVerifyChecksumAfterTransfer()) {
				log.info("adding kvps to compute and verify checksum");
				kvps.add(KeyValuePair.instance("verifyChksum",
						fileChecksumValue.getChecksumTransmissionFormat()));
			} else if (transferOptions.isComputeChecksumAfterTransfer()) {
				log.info("adding dvp to compute checksum");
				kvps.add(KeyValuePair.instance("regChksum",
						fileChecksumValue.getChecksumTransmissionFormat()));
			}
		}
	}

	/**
	 * @return
	 * @throws JargonException
	 */
	private int translateOpenFlagsValue() throws JargonException {
		int tagOpenFlags = 0;

		switch (getOpenFlags()) {
		case READ:
			tagOpenFlags = 0;
			break;
		case WRITE:
			tagOpenFlags = 1;
			break;
		case READ_TRUNCATE:
			tagOpenFlags = 0 | TRUNCATE;
			break;
		case WRITE_TRUNCATE:
			tagOpenFlags = 1 | TRUNCATE;
			break;
		case READ_WRITE:
			tagOpenFlags = 2;
			break;
		case READ_WRITE_CREATE_IF_NOT_EXISTS:
			tagOpenFlags = 2;
			break;
		case READ_WRITE_FAIL_IF_EXISTS:
			tagOpenFlags = 2;
			break;
		case WRITE_FAIL_IF_EXISTS:
			tagOpenFlags = 1;
			break;
		default:
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

	public TransferOptions getTransferOptions() {
		return transferOptions;
	}

	public void setTransferOptions(final TransferOptions transferOptions) {
		this.transferOptions = transferOptions;
	}

	/**
	 * @param fileChecksumValue
	 *            the fileChecksumValue to set
	 */
	public void setFileChecksumValue(final ChecksumValue fileChecksumValue) {
		this.fileChecksumValue = fileChecksumValue;
	}

	/**
	 * @return the fileChecksumValue
	 */
	public ChecksumValue getFileChecksumValue() {
		return fileChecksumValue;
	}

	/**
	 * @return the localPath
	 */
	public String getLocalPath() {
		return localPath;
	}

	/**
	 * @param localPath
	 *            the localPath to set
	 */
	public void setLocalPath(final String localPath) {
		this.localPath = localPath;
	}

	/**
	 * @param operationType
	 *            the operationType to set
	 */
	public void setOperationType(final int operationType) {
		this.operationType = operationType;
	}

}
