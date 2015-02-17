package org.irods.jargon.core.pub;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.checksum.ChecksumValue;
import org.irods.jargon.core.connection.ConnectionConstants;
import org.irods.jargon.core.connection.ConnectionProgressStatusListener;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.CatNoAccessException;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileIntegrityException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.InvalidInputParameterException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.exception.OperationNotSupportedForCollectionTypeException;
import org.irods.jargon.core.exception.OverwriteException;
import org.irods.jargon.core.packinstr.DataObjCopyInp;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.ModAccessControlInp;
import org.irods.jargon.core.packinstr.ModAvuMetadataInp;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.BulkAVUOperationResponse.ResultStatus;
import org.irods.jargon.core.pub.RuleProcessingAO.RuleProcessingType;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.ObjStat.SpecColType;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryOrderByField.OrderByType;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.query.SpecificQuery;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.IRODSRuleParameter;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.ParallelGetFileTransferStrategy;
import org.irods.jargon.core.transfer.ParallelPutFileTransferStrategy;
import org.irods.jargon.core.transfer.ParallelPutFileViaNIOTransferStrategy;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.IRODSConstants;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.core.utils.Overheaded;
import org.irods.jargon.core.utils.RuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access Object provides 'DAO' operations on IRODS Data Objects (files).
 * <p/>
 * Note that traditional file io per the java.io.* interfaces is handled through
 * the objects in the <code>org.irods.jargon.core.pub.io</code> package. This
 * class represents operations that are outside of the contracts one would
 * expect from an <code>java.io.File</code> object or the various streams.
 * <p/>
 * Note that the operations are tuned using parameters set in the
 * <code>JargonProperties</code> object kept in <code>IRODSession</code>. Unless
 * specifically indicated in the method signatures or comments, the defaults
 * control such aspects as whether parallel file transfers are done.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public final class DataObjectAOImpl extends FileCatalogObjectAOImpl implements
		DataObjectAO {

	private static final String NULL_OR_EMPTY_IRODS_COLLECTION_ABSOLUTE_PATH = "null or empty irodsCollectionAbsolutePath";
	private static final String ERROR_IN_PARALLEL_TRANSFER = "error in parallel transfer";
	private static final String NULL_LOCAL_FILE = "null local file";
	private static final String NULL_OR_EMPTY_ABSOLUTE_PATH = "null or empty absolutePath";
	public static final Logger log = LoggerFactory
			.getLogger(DataObjectAOImpl.class);
	private transient final DataAOHelper dataAOHelper = new DataAOHelper(
			getIRODSAccessObjectFactory(), getIRODSAccount());
	private transient final IRODSGenQueryExecutor irodsGenQueryExecutor;

	private enum OverwriteResponse {
		SKIP, PROCEED_WITH_NO_FORCE, PROCEED_WITH_FORCE
	}

	/**
	 * Default constructor
	 * 
	 * @param irodsSession
	 *            {@link org.irods.jargon.core.connection.IRODSSession} that
	 *            will manage connecting to iRODS.
	 * @param irodsAccount
	 *            (@link org.irods.jargon.core.connection.IRODSAccount} that
	 *            contains the connection information used to get a connection
	 *            from the <code>irodsSession</code>
	 * @throws JargonException
	 */
	protected DataObjectAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		irodsGenQueryExecutor = getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(irodsAccount);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findByCollectionNameAndDataName
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public DataObject findByCollectionNameAndDataName(
			final String collectionPath, final String dataName)
			throws FileNotFoundException, JargonException {

		if (collectionPath == null) {
			throw new IllegalArgumentException("null collectionPath");
		}

		if (dataName == null || dataName.isEmpty()) {
			throw new IllegalArgumentException("dataName is null or empty");
		}

		MiscIRODSUtils.checkPathSizeForMax(collectionPath, dataName);

		log.info("find by collection path: {}", collectionPath);
		log.info(" data obj name: {}", dataName);

		String absPath = MiscIRODSUtils
				.buildAbsolutePathFromCollectionParentAndFileName(
						collectionPath, dataName);
		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(absPath);

		return findGivenObjStat(objStat);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findByAbsolutePath(java.lang.String
	 * )
	 */
	@Override
	public DataObject findByAbsolutePath(final String absolutePath)
			throws FileNotFoundException, JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException(NULL_OR_EMPTY_ABSOLUTE_PATH);
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		log.info("findByAbsolutePath() with path:{}", absolutePath);
		CollectionAndPath collectionAndPath = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(absolutePath);
		return findByCollectionNameAndDataName(
				collectionAndPath.getCollectionParent(),
				collectionAndPath.getChildName());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#findById(int)
	 */
	@Override
	public DataObject findById(final int id) throws FileNotFoundException,
			JargonException {

		log.info("findById() with id:{}", id);
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

		dataAOHelper.buildSelects(builder);

		builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_D_DATA_ID,
				QueryConditionOperators.EQUAL, String.valueOf(id));

		IRODSQueryResultSet resultSet = null;
		try {
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for data object", e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for data object", e);
		}

		if (resultSet.getFirstResult() == null) {
			log.error("no data object data found for id:{}", id);
			throw new FileNotFoundException(
					"no data object data found in iCAT for id");
		}

		return DataAOHelper.buildDomainFromResultSetRow(resultSet
				.getFirstResult());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findGivenObjStat(org.irods.jargon
	 * .core.pub.domain.ObjStat)
	 */
	@Override
	public DataObject findGivenObjStat(final ObjStat objStat)
			throws DataNotFoundException, JargonException {
		log.info("findGivenObjStat()");

		if (objStat == null) {
			throw new IllegalArgumentException("null objStat");
		}

		log.info("objStat:{}", objStat);

		if (objStat.isSomeTypeOfCollection()) {
			log.error(
					"objStat is not for a data object, wrong method called:{}",
					objStat);
			throw new JargonException(
					"object is not a data object, it's a collection");
		}

		// make sure this special coll type has support
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		// get absolute path to use for querying iCAT (could be a soft link)
		String absPath = objStat
				.determineAbsolutePathBasedOnCollTypeInObjectStat();

		log.info("absPath for querying iCAT:{}", absPath);

		// split this as queries are by collection parent and data name
		CollectionAndPath collectionAndPath = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(absPath);
		log.info("collection and path for data object:{}", collectionAndPath);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

		dataAOHelper.buildSelects(builder);

		if (collectionAndPath.getCollectionParent() == null
				|| collectionAndPath.getCollectionParent().isEmpty()) {
			log.info("ignoring collection path in query");
		} else {
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
					QueryConditionOperators.EQUAL,
					collectionAndPath.getCollectionParent());
		}

		builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_DATA_NAME,
				QueryConditionOperators.EQUAL, collectionAndPath.getChildName());

		IRODSQueryResultSet resultSet = null;
		try {
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							MiscIRODSUtils.getZoneInPath(absPath));

		} catch (JargonQueryException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for data object", e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for query", e);
			throw new JargonException("error in query for data object", e);
		}

		DataObject dataObject;

		if (resultSet.getFirstResult() == null) {
			log.error("no data object data found for objStat:{}", objStat);
			throw new DataNotFoundException(
					"no data object data found in iCAT for objStat");
		}

		dataObject = DataAOHelper.buildDomainFromResultSetRow(resultSet
				.getFirstResult());

		// use the ObjStat to twizzle the data object to reflect any special
		// collection info
		dataObject.setSpecColType(objStat.getSpecColType());
		dataObject.setObjectPath(objStat.getObjectPath());
		// if in a linked coll, still keep the same path and name as
		// requested, objPath will have canonical parent

		if (objStat.getSpecColType() == SpecColType.LINKED_COLL) {
			CollectionAndPath requestedCollectionAndPath = MiscIRODSUtils
					.separateCollectionAndPathFromGivenAbsolutePath(objStat
							.getAbsolutePath());
			dataObject.setCollectionName(requestedCollectionAndPath
					.getCollectionParent());
			dataObject.setDataName(requestedCollectionAndPath.getChildName());
		}
		log.info("returning: {}", dataObject.toString());
		return dataObject;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#findWhere(java.lang.String,
	 * int)
	 */

	/**
	 * Transfer a file or directory from the local file system to iRODS.
	 * <p/>
	 * Note that re-routing of connections to resources is not done from methods
	 * in this class, but can be handled by using the methods in
	 * {@link DataTransferOperations}.
	 * <p/>
	 * Note that this operation is for a single data object, not for recursive
	 * transfers of collections. See {@link DataTransferOperations} for
	 * recursive data transfers.
	 * <p/>
	 * If the <code>TransferOptions</code> specified in the
	 * <code>TransferControlBlock</code> indicates no force, then an attempted
	 * overwrite will throw the <code>OverwriteException</code>. If the tranfer
	 * option is set to ask the callback listener, then the
	 * <code>TransferStatusCallbackListener</code> will receive a message asking
	 * for the overwrite option for this transfer operation. This is the
	 * appropriate mode when the client is interactive.
	 * 
	 * @param localFile
	 *            <code>File</code> with a source file or directory in the local
	 *            file system
	 * @param irodsFileDestination
	 *            {@link IRODSFile} that is the target of the data transfer
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that will control aspects of the
	 *            data transfer. Note that the {@link TransferOptions} that are
	 *            a member of the <code>TransferControlBlock</code> may be
	 *            specified here to pass to the running transfer. If this is set
	 *            to <code>null</code> a default block will be created, and the
	 *            <code>TransferOptions</code> will be set to the defined
	 *            default parameters
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener}, or <code>null</code>
	 *            if not specified, that can receive callbacks on the status of
	 *            the transfer operation
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set and no callback listener can be consulted, or set to
	 *             no overwrite,
	 * @throws DataNotFoundException
	 *             if the source local file does not exist or the target iRODS
	 *             collection does not exist
	 * @throws JargonException
	 */
	void putLocalDataObjectToIRODS(final File localFile,
			final IRODSFile irodsFileDestination,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws JargonException {

		TransferControlBlock effectiveTransferControlBlock = checkTransferControlBlockForOptionsAndSetDefaultsIfNotSpecified(transferControlBlock);

		putLocalDataObjectToIRODSCommonProcessing(localFile,
				irodsFileDestination, false, effectiveTransferControlBlock,
				transferStatusCallbackListener);

	}

	/**
	 * Method to put local data to iRODS taking default options, and not
	 * specifying a call-back listener. Note that re-routing of connections to
	 * resources is not done from methods in this class, but can be handled by
	 * using the methods in {@link DataTransferOperations}. Note that this
	 * operation is for a single data object, not for recursive transfers of
	 * collections. See {@link DataTransferOperations} for recursive data
	 * transfers.
	 * <p/>
	 * If the <code>TransferOptions</code> specified in the
	 * <code>TransferControlBlock</code> indicates no force, then an attempted
	 * overwrite will throw the <code>OverwriteException</code>. If the tranfer
	 * option is set to ask the callback listener, then the
	 * <code>TransferStatusCallbackListener</code> will receive a message asking
	 * for the overwrite option for this transfer operation. This is the
	 * appropriate mode when the client is interactive.
	 * 
	 * @param localFile
	 *            <code>File</code> with a source file or directory in the local
	 *            file system
	 * @param irodsFileDestination
	 *            {@link IRODSFile} that is the target of the data transfer
	 * @param overwrite
	 *            <code>boolean</code> that indicates whether data should be
	 *            overwritten at the target
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set and no callback listener can be consulted, or set to
	 *             no overwrite,
	 * @throws DataNotFoundException
	 *             if the source local file does not exist or the target iRODS
	 *             collection does not exist
	 * @throws JargonException
	 */
	void putLocalDataObjectToIRODS(final File localFile,
			final IRODSFile irodsFileDestination, final boolean overwrite)
			throws DataNotFoundException, OverwriteException, JargonException {

		// call with no control block will create defaults
		TransferControlBlock effectiveTransferControlBlock = checkTransferControlBlockForOptionsAndSetDefaultsIfNotSpecified(null);
		if (overwrite) {
			effectiveTransferControlBlock.getTransferOptions().setForceOption(
					ForceOption.USE_FORCE);
		}
		putLocalDataObjectToIRODSCommonProcessing(localFile,
				irodsFileDestination, false, effectiveTransferControlBlock,
				null);

	}

	/**
	 * Transfer a file or directory from the local file system to iRODS as
	 * invoked by a client-side rule operation. This is used only for special
	 * cases during rule invocation.
	 * <p/>
	 * Note that re-routing of connections to resources is not done from methods
	 * in this class, but can be handled by using the methods in
	 * {@link DataTransferOperations}.
	 * <p/>
	 * Note that this operation is for a single data object, not for recursive
	 * transfers of collections. See {@link DataTransferOperations} for
	 * recursive data transfers.
	 * <p/>
	 * If the <code>TransferOptions</code> specified in the
	 * <code>TransferControlBlock</code> indicates no force, then an attempted
	 * overwrite will throw the <code>OverwriteException</code>. If the transfer
	 * option is set to ask the callback listener, then the
	 * <code>TransferStatusCallbackListener</code> will receive a message asking
	 * for the overwrite option for this transfer operation. This is the
	 * appropriate mode when the client is interactive.
	 * 
	 * @param localFile
	 *            <code>File</code> with a source file or directory in the local
	 *            file system
	 * @param irodsFileDestination
	 *            {@link IRODSFile} that is the target of the data transfer
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that will control aspects of the
	 *            data transfer. Note that the {@link TransferOptions} that are
	 *            a member of the <code>TransferControlBlock</code> may be
	 *            specified here to pass to the running transfer. If this is set
	 *            to <code>null</code> a default block will be created, and the
	 *            <code>TransferOptions</code> will be set to the defined
	 *            default parameters
	 * @throws JargonException
	 */
	void putLocalDataObjectToIRODSForClientSideRuleOperation(
			final File localFile, final IRODSFile irodsFileDestination,
			final TransferControlBlock transferControlBlock)
			throws DataNotFoundException, OverwriteException, JargonException {

		TransferControlBlock effectiveTransferControlBlock = checkTransferControlBlockForOptionsAndSetDefaultsIfNotSpecified(transferControlBlock);

		// no callback listener for client side operations, may add later
		putLocalDataObjectToIRODSCommonProcessing(localFile,
				irodsFileDestination, true, effectiveTransferControlBlock, null);

	}

	/**
	 * Internal common method to execute puts.
	 * 
	 * @param localFile
	 *            <code>File</code> with the local data.
	 * @param irodsFileDestination
	 *            <code>IRODSFile</code> that describe the target of the put.
	 * @param ignoreChecks
	 *            <code>boolean</code> that bypasses any checks of the iRODS
	 *            data before attempting the put.
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that contains information that
	 *            controls the transfer operation, this is required
	 * @param transferStatusCallbackListener
	 *            {@link StatusCallbackListener} implementation to receive
	 *            status callbacks, this can be set to <code>null</code> if
	 *            desired
	 * @throws JargonException
	 */
	protected void putLocalDataObjectToIRODSCommonProcessing(
			final File localFile, final IRODSFile irodsFileDestination,
			final boolean ignoreChecks,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws DataNotFoundException, OverwriteException, JargonException {

		if (localFile == null) {
			throw new IllegalArgumentException(NULL_LOCAL_FILE);
		}

		if (irodsFileDestination == null) {
			throw new IllegalArgumentException("null destination file");
		}

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		log.info("put operation, localFile: {}", localFile.getAbsolutePath());
		log.info("to irodsFile: {}", irodsFileDestination.getAbsolutePath());

		/*
		 * Restart of connections may or may not be on, it's set in
		 * jargon.properties, this wrapping of the put operation signals that,
		 * if restarting is on, it should be done for this operation.
		 */

		putCommonProcessing(localFile, irodsFileDestination, ignoreChecks,
				transferControlBlock, transferStatusCallbackListener);

	}

	/**
	 * @param localFile
	 * @param irodsFileDestination
	 * @param ignoreChecks
	 * @param transferControlBlock
	 * @param transferStatusCallbackListener
	 * @throws DataNotFoundException
	 * @throws JargonException
	 * @throws JargonRuntimeException
	 * @throws OverwriteException
	 */
	private void putCommonProcessing(final File localFile,
			final IRODSFile irodsFileDestination, final boolean ignoreChecks,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws DataNotFoundException, JargonException,
			JargonRuntimeException, OverwriteException {

		log.info("putCommonProcessing()");

		if (!localFile.exists()) {
			log.error("put error, local file does not exist: {}",
					localFile.getAbsolutePath());
			throw new DataNotFoundException(
					"put attempt where local file does not exist:"
							+ localFile.getAbsolutePath());
		}

		/*
		 * Typically, checks are done regarding the target file, so that Jargon
		 * may gracefully handle a put of a local file that is a data object
		 * when the iRODS file is specified as a collection. An optional
		 * 'ignoreChecks' parameter is possible, and is used when processing
		 * rules with client-side put actions, as any intervening GenQuery calls
		 * cause the iRODS agent to become confused and possibly send GenQuery
		 * results back when other instructions are intended.
		 */

		IRODSFile targetFile = dataAOHelper.checkTargetFileForPutOperation(
				localFile, irodsFileDestination, ignoreChecks,
				getIRODSFileFactory());

		boolean force = false;

		/*
		 * The check above has set target file to be the data object name, so
		 * see if this results in an over-write
		 */
		if (!ignoreChecks && targetFile.exists()) {

			if (transferControlBlock.getTransferOptions() == null) {
				throw new JargonRuntimeException(
						"transfer control block does not have a transfer options set");
			}
			/*
			 * Handle potential overwrites, will consult the client if so
			 * configured
			 */
			OverwriteResponse overwriteResponse = evaluateOverwrite(localFile,
					transferControlBlock, transferStatusCallbackListener,
					transferControlBlock.getTransferOptions(),
					(File) targetFile);

			if (overwriteResponse == OverwriteResponse.SKIP) {
				log.info("skipping due to overwrite status");
				return;
			} else if (overwriteResponse == OverwriteResponse.PROCEED_WITH_FORCE) {
				force = true;
			}
		} else {
			// ignore options set, so set force to true if use force is set in
			// transfer options
			if (transferControlBlock.getTransferOptions().getForceOption() == ForceOption.USE_FORCE) {
				force = true;
			}
		}

		long localFileLength = localFile.length();
		log.debug("localFileLength:{}", localFileLength);
		long startTime = System.currentTimeMillis();

		if (localFileLength < ConnectionConstants.MAX_SZ_FOR_SINGLE_BUF) {

			log.info("processing transfer as normal, length below max");
			try {
				dataAOHelper.processNormalPutTransfer(localFile, force,
						targetFile, getIRODSProtocol(), transferControlBlock,
						transferStatusCallbackListener);
			} catch (FileNotFoundException e) {
				log.error(
						"File not found for local file I was trying to put:{}",
						localFile.getAbsolutePath());
				throw new DataNotFoundException(
						"localFile not found to put to irods", e);
			} catch (java.io.FileNotFoundException e) {
				throw new DataNotFoundException(
						"localFile not found to put to irods", e);
			}
		} else {

			log.info("processing as a parallel transfer, length above max");

			processAsAParallelPutOperationIfMoreThanZeroThreads(localFile,
					targetFile, force, transferControlBlock,
					transferStatusCallbackListener);
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		log.info(">>>>>>>>>>>>>>transfer complete in:{} millis", duration);
	}

	/**
	 * This put will be handled as a parallel transfer. iRODS may have a no
	 * parallel transfers policy, in which case, the transfer will fall back to
	 * direct streaming of data
	 * 
	 * @param localFile
	 *            <code>File</code> with source of the transfer in the local
	 *            file system
	 * @param targetFile
	 *            {@link IRODSFile} that is the target of the transfer
	 * @param overwrite
	 *            <code>boolean</code> that indicates whether to over-write the
	 *            current file. This is a per file value that is derived from
	 *            the transfer options contained in the
	 *            <code>TransferControlBlock</code>, but accounts for any
	 *            real-time decisions on whether to proceed with an over-write
	 *            by including responses of clients.
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that controls the transfer, and
	 *            any options involved, this is required
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} or <code>null</code>
	 *            for an optional listener to get status call-backs, this may be
	 *            <code>null</code>
	 */
	private void processAsAParallelPutOperationIfMoreThanZeroThreads(
			final File localFile, final IRODSFile targetFile,
			final boolean overwrite,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws DataNotFoundException, OverwriteException, JargonException {

		if (localFile == null) {
			throw new IllegalArgumentException("null localFile");
		}

		if (targetFile == null) {
			throw new IllegalArgumentException("null target file");
		}

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		TransferOptions myTransferOptions = new TransferOptions(
				transferControlBlock.getTransferOptions());

		if (myTransferOptions.isUseParallelTransfer()) {
			myTransferOptions.setMaxThreads(getJargonProperties()
					.getMaxParallelThreads());
			log.info("setting max threads cap to:{}",
					myTransferOptions.getMaxThreads());
		} else {
			log.info("no parallel transfer set in transferOptions");
			myTransferOptions.setMaxThreads(-1);
		}

		ConnectionProgressStatusListener intraFileStatusListener = null;

		boolean execFlag = false;
		if (localFile.canExecute()) {
			log.info("file is executable");
			execFlag = true;
		}

		/*
		 * If specified by options, and with a call-back listener registered,
		 * create an object to aggregate and channel within-file progress
		 * reports to the caller.
		 */

		DataObjInp dataObjInp = DataObjInp.instanceForParallelPut(
				targetFile.getAbsolutePath(), localFile.length(),
				targetFile.getResource(), overwrite, myTransferOptions,
				execFlag);

		try {

			if (myTransferOptions.isComputeAndVerifyChecksumAfterTransfer()
					|| myTransferOptions.isComputeChecksumAfterTransfer()) {
				log.info(
						"before generating parallel transfer threads, computing a checksum on the file at:{}",
						localFile.getAbsolutePath());

				ChecksumValue localFileChecksum = dataAOHelper
						.computeLocalFileChecksum(localFile, null);

				log.info("local file checksum is:{}", localFileChecksum);
				dataObjInp.setFileChecksumValue(localFileChecksum);

			}

			Tag responseToInitialCallForPut = getIRODSProtocol().irodsFunction(
					dataObjInp);

			int numberOfThreads = responseToInitialCallForPut.getTag(
					IRODSConstants.numThreads).getIntValue();

			int fd = responseToInitialCallForPut.getTag(
					IRODSConstants.L1_DESC_INX).getIntValue();

			log.debug("fd for file:{}", fd);

			if (numberOfThreads < 0) {
				throw new JargonException(
						"numberOfThreads returned from iRODS is < 0, some error occurred");
			} else if (numberOfThreads > 0) {
				parallelPutTransfer(localFile, responseToInitialCallForPut,
						numberOfThreads, localFile.length(),
						transferControlBlock, transferStatusCallbackListener);
			} else {
				log.info("parallel operation deferred by server sending 0 threads back in PortalOperOut, revert to single thread transfer");
				if (transferStatusCallbackListener != null
						|| myTransferOptions.isIntraFileStatusCallbacks()) {
					intraFileStatusListener = DefaultIntraFileProgressCallbackListener
							.instanceSettingInterval(TransferType.PUT,
									localFile.length(), transferControlBlock,
									transferStatusCallbackListener, 100);
				}
				dataAOHelper.putReadWriteLoop(localFile, overwrite, targetFile,
						fd, getIRODSProtocol(), transferControlBlock,
						intraFileStatusListener);
			}

		} catch (DataNotFoundException dnf) {
			log.warn("send of put returned no data found from irods, currently is ignored and null is returned from put operation");
		} catch (JargonException je) {
			if (je.getMessage().indexOf("-312000") > -1) {
				log.error("attempted put of file that exists in irods without overwrite");
				throw new JargonException(
						"attempted put of a file that already exists in IRODS, overwrite was not set to true",
						je);
			} else {
				throw je;
			}
		} catch (Exception e) {
			log.error(ERROR_IN_PARALLEL_TRANSFER, e);
			throw new JargonException(ERROR_IN_PARALLEL_TRANSFER, e);
		}
	}

	/**
	 * 
	 * @param localFile
	 * @param responseToInitialCallForPut
	 * @param numberOfThreads
	 * @param transferLength
	 * @param transferControlBlock
	 * @param transferStatusCallbackListener
	 */
	private void parallelPutTransfer(final File localFile,
			final Tag responseToInitialCallForPut, final int numberOfThreads,
			final long transferLength,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws DataNotFoundException, OverwriteException, JargonException {

		log.info("transfer will be done using {} threads", numberOfThreads);
		final String host = responseToInitialCallForPut
				.getTag(IRODSConstants.PortList_PI)
				.getTag(IRODSConstants.hostAddr).getStringValue();
		final int port = responseToInitialCallForPut
				.getTag(IRODSConstants.PortList_PI)
				.getTag(IRODSConstants.portNum).getIntValue();
		final int pass = responseToInitialCallForPut
				.getTag(IRODSConstants.PortList_PI)
				.getTag(IRODSConstants.cookie).getIntValue();

		if (getJargonProperties().isUseNIOForParallelTransfers()) {
			log.info(">>>>>>using NIO for parallel put");
			ParallelPutFileViaNIOTransferStrategy parallelPutFileStrategy = ParallelPutFileViaNIOTransferStrategy
					.instance(host, port, numberOfThreads, pass, localFile,
							getIRODSAccessObjectFactory(), transferLength,
							transferControlBlock,
							transferStatusCallbackListener);
			log.info(
					"getting ready to initiate parallel file transfer strategy:{}",
					parallelPutFileStrategy);
			parallelPutFileStrategy.transfer();
		} else {
			log.info(">>>>>>using standard i/o for parallel put");
			ParallelPutFileTransferStrategy parallelPutFileStrategy = ParallelPutFileTransferStrategy
					.instance(host, port, numberOfThreads, pass, localFile,
							getIRODSAccessObjectFactory(), transferLength,
							transferControlBlock,
							transferStatusCallbackListener);
			log.info(
					"getting ready to initiate parallel file transfer strategy:{}",
					parallelPutFileStrategy);

			parallelPutFileStrategy.transfer();
		}

		log.info("transfer process is complete");
		int statusForComplete = responseToInitialCallForPut.getTag(
				IRODSConstants.L1_DESC_INX).getIntValue();
		log.debug("status for complete:{}", statusForComplete);

		log.info("sending operation complete at termination of parallel transfer");
		getIRODSProtocol().operationComplete(statusForComplete);
	}

	/**
	 * Retrieve a file from iRODS and store it locally.
	 * <p/>
	 * Note that this operation is for a single data object, not for recursive
	 * transfers of collections. See {@link DataTransferOperations} for
	 * recursive data transfers. This get operation will use the default
	 * settings for <code>TransferOptions</code>.
	 * <p/>
	 * A note about overwrites: This method call does not allow for
	 * specification of transfer options or registration for a callback
	 * listener, instead, it will look at the configured
	 * <code>JargonProperties</code> for any global settings on overwrites.
	 * Other method signatures for get operations allow specification of force
	 * options, and also allow interaction between the caller and the
	 * transferring process when an overwrite is detected.
	 * 
	 * @param irodsFileToGet
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            source of the transfer
	 * @param localFileToHoldData
	 *            <code>File</code> which is the target of the transfer. If the
	 *            given target is a collection, the file name of the iRODS file
	 *            is used as the file name of the local file.
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set
	 * @throws DataNotFoundException
	 *             if the source iRODS file does not exist
	 * @throws JargonException
	 */
	void getDataObjectFromIrods(final IRODSFile irodsFileToGet,
			final File localFileToHoldData) throws OverwriteException,
			DataNotFoundException, JargonException {

		getDataObjectFromIrods(irodsFileToGet, localFileToHoldData, null, null);
	}

	/**
	 * Get operation for a single data object. This method allows the the
	 * definition of a <code>TransferControlBlock</code> object as well as a
	 * <code>TransferStatusCallbackListener</code>.
	 * <p/>
	 * Note that this operation is for a single data object, not for recursive
	 * transfers of collections. See {@link DataTransferOperations} for
	 * recursive data transfers.
	 * <p/>
	 * If the <code>TransferOptions</code> specified in the
	 * <code>TransferControlBlock</code> indicates no force, then an attempted
	 * overwrite will throw the <code>OverwriteException</code>. If the tranfer
	 * option is set to ask the callback listener, then the
	 * <code>TransferStatusCallbackListener</code> will receive a message asking
	 * for the overwrite option for this transfer operation. This is the
	 * appropriate mode when the client is interactive.
	 * 
	 * @param irodsFileToGet
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            source of the transfer. Setting the resource name in the
	 *            <code>irodsFileToGet</code> will specify that the file is
	 *            retrieved from that particular resource.
	 * 
	 * @param localFileToHoldData
	 *            <code>File</code> which is the target of the transfer. If the
	 *            given target is a collection, the file name of the iRODS file
	 *            is used as the file name of the local file.
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that will control aspects of the
	 *            data transfer. Note that the {@link TransferOptions} that are
	 *            a member of the <code>TransferControlBlock</code> may be
	 *            specified here to pass to the running transfer. If this is set
	 *            to <code>null</code> a default block will be created, and the
	 *            <code>TransferOptions</code> will be set to the defined
	 *            default parameters
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener}, or <code>null</code>
	 *            if not specified, that can receive call-backs on the status of
	 *            the transfer operation
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set and no callback listener can be consulted, or set to
	 *             no overwrite,
	 * @throws DataNotFoundException
	 *             if the source iRODS file does not exist
	 * @throws JargonException
	 */
	void getDataObjectFromIrods(final IRODSFile irodsFileToGet,
			final File localFileToHoldData,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws OverwriteException, DataNotFoundException, JargonException {

		log.info("getDataObjectFromIrods()");

		if (localFileToHoldData == null) {
			throw new IllegalArgumentException(NULL_LOCAL_FILE);
		}

		if (irodsFileToGet == null) {
			throw new IllegalArgumentException("nulll destination file");
		}

		log.info("irodsFileToGet:{}", irodsFileToGet.getAbsolutePath());
		log.info("localFileToHoldData:{}",
				localFileToHoldData.getAbsolutePath());

		TransferControlBlock operativeTransferControlBlock = checkTransferControlBlockForOptionsAndSetDefaultsIfNotSpecified(transferControlBlock);
		TransferOptions thisFileTransferOptions = new TransferOptions(
				operativeTransferControlBlock.getTransferOptions());

		File localFile;
		if (localFileToHoldData.isDirectory()) {
			log.info("a get to a directory, just use the source file name and accept the directory as a target");
			StringBuilder sb = new StringBuilder();
			sb.append(localFileToHoldData.getAbsolutePath());
			sb.append("/");
			sb.append(irodsFileToGet.getName());
			log.info("target file name will be:{}", sb.toString());
			localFile = new File(sb.toString());
		} else {
			localFile = localFileToHoldData;
		}

		/*
		 * Handle potential overwrites, will consult the client if so configured
		 */
		OverwriteResponse overwriteResponse = evaluateOverwrite(
				(File) irodsFileToGet, transferControlBlock,
				transferStatusCallbackListener, thisFileTransferOptions,
				localFile);

		if (overwriteResponse == OverwriteResponse.SKIP) {
			log.info("skipping due to overwrite status");
			return;
		}

		long irodsFileLength = irodsFileToGet.length();
		log.info("testing file length to set parallel transfer options");
		if (irodsFileLength > ConnectionConstants.MAX_SZ_FOR_SINGLE_BUF) {
			if (thisFileTransferOptions.isUseParallelTransfer()) {
				thisFileTransferOptions.setMaxThreads(getJargonProperties()
						.getMaxParallelThreads());
				log.info("setting max threads cap to:{}",
						thisFileTransferOptions.getMaxThreads());
			} else {
				log.info("no parallel transfer set in transferOptions");
				thisFileTransferOptions.setMaxThreads(-1);
			}
		} else {
			thisFileTransferOptions.setMaxThreads(0);
		}

		log.info("target local file: {}", localFile.getAbsolutePath());
		log.info("from source file: {}", irodsFileToGet.getAbsolutePath());

		final DataObjInp dataObjInp;
		if (irodsFileToGet.getResource().isEmpty()) {
			dataObjInp = DataObjInp.instanceForGet(
					irodsFileToGet.getAbsolutePath(), irodsFileLength,
					thisFileTransferOptions);
		} else {
			dataObjInp = DataObjInp.instanceForGetSpecifyingResource(
					irodsFileToGet.getAbsolutePath(),
					irodsFileToGet.getResource(), "", thisFileTransferOptions);
		}

		processGetAfterResourceDetermined(irodsFileToGet, localFile,
				dataObjInp, thisFileTransferOptions, irodsFileLength,
				operativeTransferControlBlock, transferStatusCallbackListener,
				false);
	}

	/**
	 * Incorporate user responses in the case of a potential overwrite of data
	 * 
	 * @param sourceFile
	 * @param transferControlBlock
	 * @param transferStatusCallbackListener
	 * @param thisFileTransferOptions
	 * @param targetFile
	 * @return
	 * @throws OverwriteException
	 */
	@Overheaded
	// [#1606] inconsistent objstat semantics for mounted collections
	private OverwriteResponse evaluateOverwrite(
			final File sourceFile,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferOptions thisFileTransferOptions, final File targetFile)
			throws OverwriteException {
		OverwriteResponse overwriteResponse = OverwriteResponse.PROCEED_WITH_NO_FORCE;
		if (targetFile.exists()) {

			/*
			 * objstat does not work for mounted collections (see Bug [#1606]
			 * inconsistent objstat semantics for mounted collections) this
			 * overhead will always force for puts to a mounted collection
			 */

			if (targetFile instanceof IRODSFile) {
				try {
					if (getObjectStatForAbsolutePath(
							targetFile.getAbsolutePath()).getSpecColType() == SpecColType.MOUNTED_COLL) {
						log.info("always use force for mounted collections, see comments for Bug 1606");
						overwriteResponse = OverwriteResponse.PROCEED_WITH_FORCE;
						return overwriteResponse;

					}
				} catch (org.irods.jargon.core.exception.FileNotFoundException e) {
					// ignore, should not happen
				} catch (JargonException e) {
					log.error("jargon error checking overwrite", e);
					throw new JargonRuntimeException(
							"runtime exception in overwrite handling process",
							e);
				}
			}

			log.info(
					"target file exists, check if overwrite allowed, file is:{}",
					targetFile.getAbsolutePath());
			if (thisFileTransferOptions.getForceOption() == ForceOption.NO_FORCE) {
				throw new OverwriteException(
						"attempt to overwrite file, target file already exists and no force option specified");
			} else if (thisFileTransferOptions.getForceOption() == ForceOption.USE_FORCE) {
				log.info("force specified, do the overwrite");
				overwriteResponse = OverwriteResponse.PROCEED_WITH_FORCE;
			} else if (thisFileTransferOptions.getForceOption() == ForceOption.ASK_CALLBACK_LISTENER) {
				if (transferStatusCallbackListener == null) {
					throw new OverwriteException(
							"attempt to overwrite file, target file already exists and no callback listener provided to ask");
				} else {
					TransferStatusCallbackListener.CallbackResponse callbackResponse = transferStatusCallbackListener
							.transferAsksWhetherToForceOperation(
									sourceFile.getAbsolutePath(), false);
					switch (callbackResponse) {
					case CANCEL:
						log.info("transfer cancelleld");
						overwriteResponse = OverwriteResponse.SKIP;
						break;
					case YES_THIS_FILE:
						overwriteResponse = OverwriteResponse.PROCEED_WITH_FORCE;
						break;
					case NO_THIS_FILE:
						overwriteResponse = OverwriteResponse.SKIP;
						break;
					case YES_FOR_ALL:
						if (transferControlBlock == null) {
							log.warn("attempting to process a 'yes for all' response, but no transfer control block to maintain this, it will be ignored for subsequent transfers");
						} else {
							transferControlBlock.getTransferOptions()
									.setForceOption(ForceOption.USE_FORCE);
						}
						overwriteResponse = OverwriteResponse.PROCEED_WITH_FORCE;
						break;
					case NO_FOR_ALL:
						if (transferControlBlock == null) {
							log.warn("attempting to process a 'no for all' response, but no transfer control block to maintain this, it will be ignored for subsequent transfers");
							overwriteResponse = OverwriteResponse.SKIP;
						} else {
							transferControlBlock.getTransferOptions()
									.setForceOption(ForceOption.NO_FORCE);
							overwriteResponse = OverwriteResponse.SKIP;
						}
						break;
					default:
						log.error("unknown callback response:{}",
								callbackResponse);
					}
				}
			}
		}
		return overwriteResponse;
	}

	/**
	 * Retrieve a file from iRODS and store it locally. This method will assume
	 * that the resource is not specified, which is useful for processing
	 * client-side rule actions, or other occasions where the get operation
	 * needs to be directly processed and there can be no other intervening XML
	 * protocol operations.
	 * 
	 * @param irodsFileToGet
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            source of the transfer. The resource of the
	 *            <code>IRODSFile</code> is controlling.
	 * @param localFileToHoldData
	 *            <code>File</code> which is the target of the transfer
	 * @param {@link TransferOptions} to control the transfer, or null if not
	 *        specified. Note that the <code>TransferOptions</code> object will
	 *        be cloned, and as such the passed-in parameter will not be
	 *        altered.
	 * @return <code>int</code> that represents the handle (l1descInx) for the
	 *         opened file, to be used for sending operation complete messages
	 * @throws JargonException
	 */
	int irodsDataObjectGetOperationForClientSideAction(
			final IRODSFile irodsFileToGet, final File localFileToHoldData,
			final TransferOptions transferOptions) throws OverwriteException,
			DataNotFoundException, JargonException {

		if (localFileToHoldData == null) {
			throw new IllegalArgumentException(NULL_LOCAL_FILE);
		}

		if (irodsFileToGet == null) {
			throw new IllegalArgumentException("nulll destination file");
		}

		evaluateOverwrite((File) irodsFileToGet, null, null,
				buildDefaultTransferOptionsIfNotSpecified(transferOptions),
				localFileToHoldData);

		log.info("target local file: {}", localFileToHoldData.getAbsolutePath());
		log.info("from source file: {}", irodsFileToGet.getAbsolutePath());
		TransferOptions myTransferOptions = buildDefaultTransferOptionsIfNotSpecified(transferOptions);

		final DataObjInp dataObjInp = DataObjInp
				.instanceForGetSpecifyingResource(
						irodsFileToGet.getAbsolutePath(),
						irodsFileToGet.getResource(),
						localFileToHoldData.getAbsolutePath(),
						myTransferOptions);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();
		transferControlBlock.setTransferOptions(myTransferOptions);

		return processGetAfterResourceDetermined(irodsFileToGet,
				localFileToHoldData, dataObjInp, myTransferOptions, 0,
				transferControlBlock, null, true);

	}

	/**
	 * The resource to use for the get operation has been determined. Note that
	 * the state of the resource determination is important due to the fact that
	 * finding such state information requires a GenQuery. There are certain
	 * occasions where get operations are called, where doing an intervening
	 * query to the ICAT can cause a protocol issue. Issuing such a GenQuery can
	 * confuse what can be a multi-step protocol. For this reason, callers of
	 * this method can be assured that no queries will be issued to iRODS while
	 * processing the get. An occasion where this has been problematic has been
	 * the processing of client-side get actions as the result of rule
	 * execution.
	 * <p/>
	 * Note that an iRODS file length is passed here. This avoids any query from
	 * an <code>IRODSFile.length()</code> operation. The length is passed in, as
	 * there are some occasions where the multi-step protocol can show a zero
	 * file length, such as when iRODS is preparing to treat a get operation as
	 * a parallel file transfer. There are cases where iRODS responds with a
	 * zero length, indicating a parallel transfer, but a policy rule in place
	 * in iRODS may 'turn off' such parallel transfers. In that case, the length
	 * usually referred to returns as a zero, and the number of threads will be
	 * zero. This must be handled.
	 * 
	 * 
	 * @param irodsFileToGet
	 * @param localFileToHoldData
	 * @param dataObjInp
	 * @param thisFileTransferOptions
	 * @param irodsFileLength
	 *            actual length of file from iRODS. This is passed in as there
	 *            are occasions where the protocol exchange results in a zero
	 *            file length. See the note above.
	 * @param transferStatusCallbackListener
	 * @param transferControlBlock
	 * @param clientSideAction
	 *            <code>boolean</code> that is <code>true</code> if this is a
	 *            client-side action in rule processing
	 * @return <code>int</code> that is the file handle (l1descInx) that iRODS
	 *         uses for this file
	 * @throws JargonException
	 * @throws DataNotFoundException
	 * @throws UnsupportedOperationException
	 */
	private int processGetAfterResourceDetermined(
			final IRODSFile irodsFileToGet,
			final File localFileToHoldData,
			final DataObjInp dataObjInp,
			final TransferOptions thisFileTransferOptions,
			final long irodsFileLength,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final boolean clientSideAction) throws OverwriteException,
			DataNotFoundException, JargonException {

		log.info("process get after resource determined");

		if (transferStatusCallbackListener == null) {
			log.info("no transfer status callback listener provided");
		}

		if (thisFileTransferOptions == null) {
			throw new IllegalArgumentException("null transfer options");
		}

		LocalFileUtils.createLocalFileIfNotExists(localFileToHoldData);
		Tag message;
		try {
			message = getIRODSProtocol().irodsFunction(dataObjInp);
		} catch (CatNoAccessException e) {
			log.error(
					"no access exception wrapped as DataNotFoundException for consistency with API",
					e);
			throw new FileNotFoundException(e);

		}

		// irods file doesn't exist
		if (message == null) {
			log.warn(
					"irods file does not exist, null was returned from the get, return DataNotFoundException for iRODS file: {}",
					irodsFileToGet.getAbsolutePath());
			throw new FileNotFoundException(
					"irods file not found during get operation:"
							+ irodsFileToGet.getAbsolutePath());
		}

		// Need the total dataSize
		Tag temp = message.getTag(IRODSConstants.MsgHeader_PI);

		if (temp == null) {
			// length is zero
			log.info("create a new file, length is zero");
			return 0;
		}

		temp = temp.getTag(DataObjInp.BS_LEN);
		if (temp == null) {
			log.info("no size returned, return from get with no update done");
			return 0;
		}

		final long lengthFromIrodsResponse = temp.getLongValue();

		log.info("transfer length is:", lengthFromIrodsResponse);

		// get the L1_DESC_INX for the return value
		temp = message.getTag(IRODSConstants.L1_DESC_INX);

		int l1descInx = 0;
		if (temp != null) {
			l1descInx = temp.getIntValue();
		}

		log.debug("l1descInx value is:{}", l1descInx);

		// if length == zero, check for multiple thread copy, may still process
		// as a standard txfr if 0 threads specified
		try {
			if (lengthFromIrodsResponse == 0) {
				checkNbrThreadsAndProcessAsParallelIfMoreThanZeroThreads(
						irodsFileToGet, localFileToHoldData,
						thisFileTransferOptions, message,
						lengthFromIrodsResponse, irodsFileLength,
						transferControlBlock, transferStatusCallbackListener,
						clientSideAction);
				getIRODSProtocol().operationComplete(l1descInx);

			} else {
				dataAOHelper.processNormalGetTransfer(localFileToHoldData,
						lengthFromIrodsResponse, getIRODSProtocol(),
						thisFileTransferOptions, transferControlBlock,
						transferStatusCallbackListener);
			}

			if (thisFileTransferOptions != null
					&& thisFileTransferOptions
							.isComputeAndVerifyChecksumAfterTransfer()) {

				// compute iRODS first, use algorithm from iRODS to compute the
				// local checksum that should match

				ChecksumValue irodsChecksum = computeChecksumOnDataObject(irodsFileToGet);

				log.info("computing a checksum on the file at:{}",
						localFileToHoldData.getAbsolutePath());

				ChecksumValue localFileChecksum = dataAOHelper
						.computeLocalFileChecksum(localFileToHoldData,
								irodsChecksum.getChecksumEncoding());

				log.info("local file checksum is:{}", localFileChecksum);
				log.info("irods checksum:{}", irodsChecksum);
				if (!(irodsChecksum.getChecksumStringValue()
						.equals(localFileChecksum.getChecksumStringValue()))) {
					throw new FileIntegrityException(
							"checksum verification after get fails");
				}
			}

			if (!clientSideAction) {
				log.info("looking for executable to set flag on local file");

				if (irodsFileToGet.canExecute()) {
					log.info("execute set on local file");
					localFileToHoldData.setExecutable(true);
				}
			}

		} catch (Exception e) {
			log.error(ERROR_IN_PARALLEL_TRANSFER, e);
			throw new JargonException(ERROR_IN_PARALLEL_TRANSFER, e);
		}

		return l1descInx;
	}

	/**
	 * This is expected to be a parallel transfer, due to the size of the file.
	 * An initial request to get the file has been sent. iRODS may come back and
	 * decide (based on a rule) to not do a parallel transfer, in which case,
	 * the file will be streamed normally.
	 * 
	 * @param irodsSourceFile
	 * @param localFileToHoldData
	 * @param transferOptions
	 * @param message
	 * @param length
	 * @param irodsFileLength
	 * @param transferControlBlock
	 * @param transferStatusCallbackListener
	 * @throws JargonException
	 */
	private void checkNbrThreadsAndProcessAsParallelIfMoreThanZeroThreads(
			final IRODSFile irodsSourceFile,
			final File localFileToHoldData,
			final TransferOptions transferOptions,
			final Tag message,
			final long length,
			final long irodsFileLength,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final boolean clientSideAction) throws JargonException {

		final String host = message.getTag(IRODSConstants.PortList_PI)
				.getTag(IRODSConstants.hostAddr).getStringValue();
		int port = message.getTag(IRODSConstants.PortList_PI)
				.getTag(IRODSConstants.portNum).getIntValue();
		int password = message.getTag(IRODSConstants.PortList_PI)
				.getTag(IRODSConstants.cookie).getIntValue();
		int numberOfThreads = message.getTag(IRODSConstants.numThreads)
				.getIntValue();

		log.info("number of threads for this transfer = {} ", numberOfThreads);

		if (numberOfThreads == 0) {
			log.info("number of threads is zero, possibly parallel transfers were turned off via rule, process as normal");
			int fd = message.getTag(IRODSConstants.L1_DESC_INX).getIntValue();
			dataAOHelper.processGetTransferViaRead(irodsSourceFile,
					localFileToHoldData, irodsFileLength, transferOptions, fd,
					transferControlBlock, transferStatusCallbackListener);
		} else {

			log.info("process as a parallel transfer");
			if (transferStatusCallbackListener == null) {
				log.info("no callback listener specified");
			} else {
				log.info("callback listener was provided");
			}

			ParallelGetFileTransferStrategy parallelGetTransferStrategy = ParallelGetFileTransferStrategy
					.instance(host, port, numberOfThreads, password,
							localFileToHoldData, getIRODSAccessObjectFactory(),
							irodsFileLength, transferControlBlock,
							transferStatusCallbackListener);

			parallelGetTransferStrategy.transfer();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#listMetadataValuesForDataObject
	 * (java.util.List, java.lang.String, java.lang.String)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesForDataObjectUsingAVUQuery(
			final List<AVUQueryElement> avuQuery,
			final String dataObjectCollectionAbsPath,
			final String dataObjectFileName) throws JargonQueryException,
			JargonException {

		return findMetadataValuesForDataObjectUsingAVUQuery(avuQuery,
				dataObjectCollectionAbsPath, dataObjectFileName, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#
	 * findMetadataValuesForDataObjectUsingAVUQuery(java.util.List,
	 * java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesForDataObjectUsingAVUQuery(
			final List<AVUQueryElement> avuQuery,
			final String dataObjectCollectionAbsPath,
			final String dataObjectFileName, final boolean caseInsensitive)
			throws JargonQueryException, JargonException {

		if (avuQuery == null) {
			throw new IllegalArgumentException("null query");
		}

		if (dataObjectCollectionAbsPath == null
				|| dataObjectCollectionAbsPath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absolutePath for dataObjectCollectionAbsPath");
		}

		if (dataObjectFileName == null || dataObjectFileName.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty dataObjectFileName");
		}

		MiscIRODSUtils.checkPathSizeForMax(dataObjectCollectionAbsPath,
				dataObjectFileName);

		if (caseInsensitive) {
			if (!getIRODSServerProperties().isSupportsCaseInsensitiveQueries()) {
				throw new JargonException(
						"case insensitive queries not supported on this iRODS version");
			}
		}

		ObjStat objStat = this.retrieveObjStat(dataObjectCollectionAbsPath,
				dataObjectFileName);
		String absPath = resolveAbsolutePathGivenObjStat(objStat);

		// need to break up the path for the query
		IRODSFile dataObjectFile = getIRODSFileFactory().instanceIRODSFile(
				absPath);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true,
				caseInsensitive, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			addMetadataAndDomainDataSelectsToBuilder(builder);

			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
					QueryConditionOperators.EQUAL, dataObjectFile.getParent())
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_DATA_NAME,
							QueryConditionOperators.EQUAL,
							dataObjectFile.getName());

			for (AVUQueryElement queryElement : avuQuery) {
				DataAOHelper.appendConditionPartToBuilderQuery(queryElement,
						builder);
			}

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());

			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							MiscIRODSUtils.getZoneInPath(absPath));

		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("error building query", e);
		} catch (JargonQueryException jqe) {
			log.error("error executing query", jqe);
			throw new JargonException("error executing query", jqe);
		}

		return DataAOHelper
				.buildMetaDataAndDomainDataListFromResultSet(resultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#
	 * findMetadataValuesForDataObjectUsingAVUQuery(java.util.List,
	 * java.lang.String)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesForDataObjectUsingAVUQuery(
			final List<AVUQueryElement> avuQuery,
			final String dataObjectAbsolutePath) throws JargonQueryException,
			JargonException {

		if (avuQuery == null) {
			throw new IllegalArgumentException("null query");
		}

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty absolutePath for dataObjectAbsolutePath");
		}

		IRODSFile irodsFile = getIRODSFileFactory().instanceIRODSFile(
				dataObjectAbsolutePath);
		return findMetadataValuesForDataObjectUsingAVUQuery(avuQuery,
				irodsFile.getParent(), irodsFile.getName());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#instanceIRODSFileForPath(java.
	 * lang.String)
	 */
	@Override
	public IRODSFile instanceIRODSFileForPath(final String fileAbsolutePath)
			throws JargonException {
		log.info("returning a file for path: {}", fileAbsolutePath);
		final IRODSFile irodsFile = getIRODSFileFactory().instanceIRODSFile(
				fileAbsolutePath);
		return irodsFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#addBulkAVUMetadataToDataObject
	 * (java.lang.String, java.util.List)
	 */
	@Override
	public List<BulkAVUOperationResponse> addBulkAVUMetadataToDataObject(
			final String absolutePath, final List<AvuData> avuData)
			throws JargonException {

		log.info("addBulkAVUMetadataToDataObject()");

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolute path");
		}

		if (avuData == null || avuData.isEmpty()) {
			throw new IllegalArgumentException("null or empty avuData");
		}

		List<BulkAVUOperationResponse> responses = new ArrayList<BulkAVUOperationResponse>();

		for (AvuData value : avuData) {
			try {
				addAVUMetadata(absolutePath, value);
			} catch (DataNotFoundException dnf) {
				log.error(
						"dataNotFoundException when adding an AVU, catch and add to response data",
						dnf);
				responses.add(BulkAVUOperationResponse.instance(
						ResultStatus.MISSING_METADATA_TARGET, value,
						dnf.getMessage()));
				continue;
			} catch (DuplicateDataException dde) {
				log.error(
						"DuplicateDataException when adding an AVU, catch and add to response data",
						dde);
				responses.add(BulkAVUOperationResponse.instance(
						ResultStatus.DUPLICATE_AVU, value, dde.getMessage()));
				continue;

			}

			log.info("treat as success...", value);
			responses.add(BulkAVUOperationResponse.instance(ResultStatus.OK,
					value, ""));
		}

		log.info("...complete");
		return responses;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#deleteBulkAVUMetadataFromDataObject
	 * (java.lang.String, java.util.List)
	 */
	@Override
	public List<BulkAVUOperationResponse> deleteBulkAVUMetadataFromDataObject(
			final String absolutePath, final List<AvuData> avuData)
			throws JargonException {

		log.info("deleteBulkAVUMetadataFromDataObject()");

		if (avuData == null || avuData.isEmpty()) {
			throw new IllegalArgumentException("null or empty avuData");
		}

		List<BulkAVUOperationResponse> responses = new ArrayList<BulkAVUOperationResponse>();

		for (AvuData value : avuData) {
			try {
				deleteAVUMetadata(absolutePath, value);
			} catch (DataNotFoundException dnf) {
				log.error(
						"dataNotFoundException when deleti an AVU, catch and add to response data",
						dnf);
				responses.add(BulkAVUOperationResponse.instance(
						ResultStatus.MISSING_METADATA_TARGET, value,
						dnf.getMessage()));
				continue;

			}

			log.info("treat as success...", value);
			responses.add(BulkAVUOperationResponse.instance(ResultStatus.OK,
					value, ""));
		}

		log.info("...complete");
		return responses;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#addAVUMetadata(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void addAVUMetadata(final String absolutePath, final AvuData avuData)
			throws OperationNotSupportedForCollectionTypeException,
			DataNotFoundException, DuplicateDataException, JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException(NULL_OR_EMPTY_ABSOLUTE_PATH);
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		log.info("adding avu metadata to data object: {}", avuData);
		log.info("absolute path: {}", absolutePath);

		/*
		 * Handle soft links by munging the path
		 */

		ObjStat objStat;
		try {
			objStat = this.retrieveObjStat(absolutePath);
		} catch (Exception e) {
			throw new DataNotFoundException(e);
		}

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForAddDataObjectMetadata(absPath, avuData);

		log.debug("sending avu request");

		try {

			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);

		} catch (JargonException je) {

			if (je.getMessage().indexOf("-817000") > -1) {
				throw new DataNotFoundException(
						"Target dataObject was not found, could not add AVU");
			} else if (je.getMessage().indexOf("-809000") > -1) {
				throw new DuplicateDataException(
						"Duplicate AVU exists, cannot add");
			}

			log.error("jargon exception adding AVU metadata", je);
			throw je;
		}

		log.debug("metadata added");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#addAVUMetadata(java.lang.String,
	 * java.lang.String, org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void addAVUMetadata(final String irodsCollectionAbsolutePath,
			final String fileName, final AvuData avuData)
			throws DataNotFoundException, JargonException {

		if (irodsCollectionAbsolutePath == null
				|| irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					NULL_OR_EMPTY_IRODS_COLLECTION_ABSOLUTE_PATH);
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		MiscIRODSUtils.checkPathSizeForMax(irodsCollectionAbsolutePath,
				fileName);

		log.info("adding avu metadata to data object: {}", avuData);
		log.info("parent collection absolute path: {}",
				irodsCollectionAbsolutePath);
		log.info("file name: {}", fileName);

		/*
		 * Handle soft links by munging the path
		 */

		ObjStat objStat = this.retrieveObjStat(irodsCollectionAbsolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);

		StringBuilder sb = new StringBuilder(absPath);
		sb.append("/");
		sb.append(fileName);

		addAVUMetadata(sb.toString(), avuData);

	}

	@Override
	public void deleteAllAVUForDataObject(final String absolutePath)
			throws DataNotFoundException, JargonException {

		/*
		 * There are multiple objsStats being called, need to consolidate this
		 * and have methods that can take in a passed in object stat
		 */

		log.info("deleteAllAVForDataObject()");

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException(NULL_OR_EMPTY_ABSOLUTE_PATH);
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		log.info("absolute path: {}", absolutePath);

		ObjStat objStat;
		try {
			objStat = this.retrieveObjStat(absolutePath);
		} catch (FileNotFoundException e) {
			throw new DataNotFoundException(e);
		}

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			return;
		}

		List<MetaDataAndDomainData> metadatas = this
				.findMetadataValuesForDataObject(objStat);

		List<AvuData> avusToDelete = new ArrayList<AvuData>();

		for (MetaDataAndDomainData metadata : metadatas) {
			avusToDelete.add(AvuData.instance(metadata.getAvuAttribute(),
					metadata.getAvuValue(), metadata.getAvuUnit()));
		}

		deleteBulkAVUMetadataFromDataObject(absolutePath, avusToDelete);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#deleteAVUMetadata(java.lang.String
	 * , org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void deleteAVUMetadata(final String absolutePath,
			final AvuData avuData) throws DataNotFoundException,
			JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException(NULL_OR_EMPTY_ABSOLUTE_PATH);
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		log.info("deleting avu metadata on dataObject: {}", avuData);
		log.info("absolute path: {}", absolutePath);

		ObjStat objStat;
		try {
			objStat = this.retrieveObjStat(absolutePath);
		} catch (FileNotFoundException e) {
			throw new DataNotFoundException(e);
		}

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForDeleteDataObjectMetadata(absPath, avuData);

		log.debug("sending avu request");

		try {
			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);
		} catch (JargonException je) {

			if (je.getMessage().indexOf("-817000") > -1) {
				throw new DataNotFoundException(
						"Target data object was not found, could not remove AVU");
			}

			log.error("jargon exception removing AVU metadata", je);
			throw je;
		}

		log.debug("metadata removed");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findMetadataValuesByMetadataQuery
	 * (java.util.List)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			final List<AVUQueryElement> avuQuery) throws JargonQueryException,
			JargonException {

		return findMetadataValuesByMetadataQuery(avuQuery, 0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findMetadataValuesByMetadataQuery
	 * (java.util.List, int)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			final List<AVUQueryElement> avuQuery, final int partialStartIndex)
			throws JargonQueryException, JargonException {

		return findMetadataValuesByMetadataQuery(avuQuery, partialStartIndex,
				false);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findMetadataValuesByMetadataQuery
	 * (java.util.List, int, boolean)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesByMetadataQuery(
			final List<AVUQueryElement> avuQuery, final int partialStartIndex,
			final boolean caseInsensitive) throws JargonQueryException,
			JargonException {

		if (avuQuery == null || avuQuery.isEmpty()) {
			throw new IllegalArgumentException("null or empty query");
		}

		if (caseInsensitive) {
			if (!getIRODSServerProperties().isSupportsCaseInsensitiveQueries()) {
				throw new JargonException(
						"case insensitive queries not supported on this iRODS version");
			}
		}

		log.info("building a metadata query for: {}", avuQuery);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true,
				caseInsensitive, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			addMetadataAndDomainDataSelectsToBuilder(builder);

			for (AVUQueryElement queryElement : avuQuery) {
				DataAOHelper.appendConditionPartToBuilderQuery(queryElement,
						builder);
			}

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, partialStartIndex);

		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("error building query", e);
		} catch (JargonQueryException jqe) {
			log.error("error executing query", jqe);
			throw new JargonException("error executing query", jqe);
		}

		return DataAOHelper
				.buildMetaDataAndDomainDataListFromResultSet(resultSet);

	}

	private void addMetadataAndDomainDataSelectsToBuilder(
			IRODSGenQueryBuilder builder) throws GenQueryBuilderException {
		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_DATA_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_CREATE_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_MODIFY_TIME)
				.addSelectAsGenQueryValue(
						RodsGenQueryEnum.COL_META_DATA_ATTR_ID)
				.addSelectAsGenQueryValue(
						RodsGenQueryEnum.COL_META_DATA_ATTR_NAME)
				.addSelectAsGenQueryValue(
						RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE)
				.addSelectAsGenQueryValue(
						RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findDomainByMetadataQuery(java
	 * .util.List)
	 */
	@Override
	public List<DataObject> findDomainByMetadataQuery(
			final List<AVUQueryElement> avuQueryElements)
			throws JargonQueryException, JargonException {

		return findDomainByMetadataQuery(avuQueryElements, 0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findDomainByMetadataQuery(java
	 * .util.List, int)
	 */
	@Override
	public List<DataObject> findDomainByMetadataQuery(
			final List<AVUQueryElement> avuQueryElements,
			final int partialStartIndex) throws JargonQueryException,
			JargonException {

		return findDomainByMetadataQuery(avuQueryElements, partialStartIndex,
				false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findDomainByMetadataQuery(java
	 * .util.List, int, boolean)
	 */
	@Override
	public List<DataObject> findDomainByMetadataQuery(
			final List<AVUQueryElement> avuQueryElements,
			final int partialStartIndex, final boolean caseInsensitive)
			throws JargonQueryException, JargonException {

		if (avuQueryElements == null || avuQueryElements.isEmpty()) {
			throw new IllegalArgumentException("null or empty avuQueryElements");
		}

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException(
					"partial start index must be 0 or greater");
		}

		if (caseInsensitive) {
			if (!getIRODSServerProperties().isSupportsCaseInsensitiveQueries()) {
				throw new JargonException(
						"case insensitive queries not supported on this iRODS version");
			}
		}

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true,
				caseInsensitive, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			DataAOHelper.addDataObjectSelectsToBuilder(builder);
			builder.addSelectAsGenQueryValue(
					RodsGenQueryEnum.COL_META_DATA_ATTR_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS);

			for (AVUQueryElement queryElement : avuQueryElements) {
				DataAOHelper.appendConditionPartToBuilderQuery(queryElement,
						builder);
			}

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, partialStartIndex);

		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("error building query", e);
		} catch (JargonQueryException jqe) {
			log.error("error executing query", jqe);
			throw new JargonException("error executing query", jqe);
		}

		return DataAOHelper.buildListFromResultSet(resultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#replicateIrodsDataObject(java.
	 * lang.String, java.lang.String)
	 */
	@Override
	public void replicateIrodsDataObject(final String irodsFileAbsolutePath,
			final String targetResource) throws JargonException {

		if (irodsFileAbsolutePath == null || irodsFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsFileAbsolutePath");
		}

		if (targetResource == null || targetResource.isEmpty()) {
			throw new IllegalArgumentException("null or empty targetResource");
		}

		MiscIRODSUtils.checkPathSizeForMax(irodsFileAbsolutePath);

		log.info("replicate operation, irodsFileAbsolutePath: {}",
				irodsFileAbsolutePath);
		log.info("to resource: {}", targetResource);

		final DataObjInp dataObjInp = DataObjInp.instanceForReplicate(
				irodsFileAbsolutePath, targetResource);

		try {
			getIRODSProtocol().irodsFunction(dataObjInp);
		} catch (JargonException je) {
			log.error("error replicating irods file", je);
			throw je;
		}
		log.info("replication complete");
	}

	@Override
	public void replicateIrodsDataObjectAsynchronously(
			final String irodsCollectionAbsolutePath, final String fileName,
			final String resourceName, final int delayInMinutes)
			throws JargonException {

		if (irodsCollectionAbsolutePath == null
				|| irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsCollectionAbsolutePath");
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}

		if (delayInMinutes <= 0) {
			throw new IllegalArgumentException("delay in minutes must be > 0");
		}

		log.info("irodsCollectionAbsolutePath:{}", irodsCollectionAbsolutePath);
		log.info("fileName:{}", fileName);
		log.info("resourceName:{}", resourceName);
		log.info("delayInMinutes:{}", delayInMinutes);

		if (!getIRODSServerProperties()
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			throw new JargonException(
					"service not available on servers prior to rods3.0");
		}

		RuleProcessingAO ruleProcessingAO = getIRODSAccessObjectFactory()
				.getRuleProcessingAO(getIRODSAccount());

		List<IRODSRuleParameter> irodsRuleParameters = new ArrayList<IRODSRuleParameter>();

		StringBuilder sb = new StringBuilder();
		sb.append(irodsCollectionAbsolutePath);
		sb.append("/");
		sb.append(fileName);

		irodsRuleParameters.add(new IRODSRuleParameter("*SourceFile",
				MiscIRODSUtils.wrapStringInQuotes(sb.toString())));

		irodsRuleParameters.add(new IRODSRuleParameter("*Resource",
				MiscIRODSUtils.wrapStringInQuotes(resourceName)));

		irodsRuleParameters.add(new IRODSRuleParameter("*DelayInfo", RuleUtils
				.buildDelayParamForMinutes(delayInMinutes)));

		IRODSRuleExecResult result = ruleProcessingAO.executeRuleFromResource(
				"/rules/rulemsiDataObjReplAsync.r", irodsRuleParameters,
				RuleProcessingType.EXTERNAL);
		log.info("result of action:{}", result.getRuleExecOut().trim());

	}

	/**
	 * Copy a file from one iRODS location to another. This is the preferred
	 * method signature for copy operations, with other forms now deprecated.
	 * Note that the <code>transferControlBlock</code> and
	 * <code>TransferStatusCallbackListener</code> objects are optional and may
	 * be set to <code>null</code> if not required.
	 * <p/>
	 * Note that this operation is for a single data object, not for recursive
	 * transfers of collections. See {@link DataTransferOperations} for
	 * recursive data transfers.
	 * <p/>
	 * 
	 * 
	 * @param irodsSourceFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            source of the transfer
	 * @param irodsTargetFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} that is the
	 *            collection or explicitly named target for the transfer
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set
	 * @throws DataNotFoundException
	 *             if the source iRODS file does not exist
	 * @throws JargonException
	 */
	void copyIRODSDataObject(final IRODSFile irodsSourceFile,
			final IRODSFile irodsTargetFile,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws OverwriteException, DataNotFoundException, JargonException {

		log.info("copyIRODSDataObject()");
		if (irodsSourceFile == null) {
			throw new IllegalArgumentException("null irodsSourceFile");
		}

		if (irodsTargetFile == null) {
			throw new IllegalArgumentException("null irodsTargetFile");
		}

		log.info("sourceFile:{}", irodsSourceFile.getAbsolutePath());
		log.info("targetFile:{}", irodsTargetFile.getAbsolutePath());
		log.info("at resource: {}", irodsTargetFile.getResource());

		if (!irodsSourceFile.exists()) {
			throw new DataNotFoundException(
					"the source file for the copy does not exist");
		}

		if (!irodsSourceFile.isFile()) {
			throw new JargonException("the source file is not a data object");
		}

		IRODSFile myTargetFile = irodsTargetFile;

		// checking for overwrite
		if (myTargetFile.exists()) {
			if (myTargetFile.isDirectory()) {
				log.info("target is a directory, check if the file already exists");
				myTargetFile = getIRODSFileFactory().instanceIRODSFile(
						irodsTargetFile.getAbsolutePath(),
						irodsSourceFile.getName());
				log.info("target file normalized as a data object:{}",
						irodsTargetFile.getAbsolutePath());
			}
		}

		TransferControlBlock operativeTransferControlBlock = checkTransferControlBlockForOptionsAndSetDefaultsIfNotSpecified(transferControlBlock);

		/*
		 * Handle potential overwrites, will consult the client if so configured
		 */
		OverwriteResponse overwriteResponse = evaluateOverwrite(
				(File) irodsSourceFile, transferControlBlock,
				transferStatusCallbackListener,
				operativeTransferControlBlock.getTransferOptions(),
				(File) myTargetFile);

		boolean force = false;

		if (overwriteResponse == OverwriteResponse.SKIP) {
			log.info("skipping due to overwrite status");
			return;
		} else if (overwriteResponse == OverwriteResponse.PROCEED_WITH_FORCE) {
			force = true;
		}

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp.instanceForCopy(
				irodsSourceFile.getAbsolutePath(),
				myTargetFile.getAbsolutePath(), irodsTargetFile.getResource(),
				irodsSourceFile.length(), force);

		try {
			getIRODSProtocol().irodsFunction(dataObjCopyInp);
		} catch (JargonException je) {
			log.error("error copying irods file", je);
			throw je;
		}
		log.info("copy complete");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.pub.DataObjectAO#
	 * replicateIrodsDataObjectToAllResourcesInResourceGroup(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void replicateIrodsDataObjectToAllResourcesInResourceGroup(
			final String irodsFileAbsolutePath,
			final String irodsResourceGroupName) throws JargonException {

		if (irodsFileAbsolutePath == null || irodsFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsFileAbsolutePath");
		}

		if (irodsResourceGroupName == null || irodsResourceGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty targetResource");
		}

		MiscIRODSUtils.checkPathSizeForMax(irodsFileAbsolutePath);

		log.info("replicate operation, irodsFileAbsolutePath: {}",
				irodsFileAbsolutePath);
		log.info("to resource group: {}", irodsResourceGroupName);

		final DataObjInp dataObjInp = DataObjInp
				.instanceForReplicateToResourceGroup(irodsFileAbsolutePath,
						irodsResourceGroupName);

		try {
			getIRODSProtocol().irodsFunction(dataObjInp);
		} catch (JargonException je) {
			log.error("error replicating irods file to resource group", je);
			throw je;
		}
		log.info("replication complete");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#getResourcesForDataObject(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public List<Resource> getResourcesForDataObject(
			final String dataObjectPath, final String dataObjectName)
			throws JargonException {

		if (dataObjectPath == null || dataObjectPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty dataObjectPath");
		}

		if (dataObjectName == null || dataObjectName.isEmpty()) {
			throw new IllegalArgumentException("null or empty dataObjectName");
		}

		IRODSFile irodsFile = getIRODSFileFactory().instanceIRODSFile(
				dataObjectPath, dataObjectName);
		return listFileResources(irodsFile.getAbsolutePath());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findMetadataValuesForDataObject
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesForDataObject(
			final String dataObjectCollectionAbsPath,
			final String dataObjectFileName) throws JargonException {

		if (dataObjectCollectionAbsPath == null
				|| dataObjectCollectionAbsPath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty dataObjectCollectionAbsPath");
		}

		if (dataObjectFileName == null || dataObjectFileName.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty dataObjectFileName");
		}

		MiscIRODSUtils.checkPathSizeForMax(dataObjectCollectionAbsPath,
				dataObjectFileName);

		ObjStat objStat = this.retrieveObjStat(dataObjectCollectionAbsPath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		// contract checks in delegated method

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		try {
			return this.findMetadataValuesForDataObjectUsingAVUQuery(
					queryElements, dataObjectCollectionAbsPath,
					dataObjectFileName);
		} catch (JargonQueryException e) {
			log.error("query exception looking up data object:{}",
					dataObjectCollectionAbsPath + "/" + dataObjectFileName, e);
			log.error("fileName: {}", dataObjectFileName);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findMetadataValueForDataObjectById
	 * (java.lang.String, int)
	 */
	@Override
	public MetaDataAndDomainData findMetadataValueForDataObjectById(
			final String dataObjectAbsolutePath, final int id)
			throws FileNotFoundException, DataNotFoundException,
			JargonException {

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty dataObjectAbsolutePath");
		}

		MiscIRODSUtils.checkPathSizeForMax(dataObjectAbsolutePath);

		log.info("findMetadataValueForDataObjectById: {}",
				dataObjectAbsolutePath);
		log.info("id:{}", id);

		ObjStat objStat = this.retrieveObjStat(dataObjectAbsolutePath);

		return findMetadataValueForDataObjectById(objStat, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findMetadataValueForDataObjectById
	 * (org.irods.jargon.core.pub.domain.ObjStat, int)
	 */
	@Override
	public MetaDataAndDomainData findMetadataValueForDataObjectById(
			final ObjStat objStat, final int id) throws DataNotFoundException,
			JargonException {

		if (objStat == null) {
			throw new IllegalArgumentException("null or empty objStat");
		}

		log.info("findMetadataValueForDataObjectById: {}", objStat);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new JargonException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);

		// need to break up the path for the query
		IRODSFile dataObjectFile = getIRODSFileFactory().instanceIRODSFile(
				absPath);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, false,
				null);
		IRODSQueryResultSetInterface resultSet;

		try {
			addMetadataAndDomainDataSelectsToBuilder(builder);

			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
					QueryConditionOperators.EQUAL, dataObjectFile.getParent())
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_DATA_NAME,
							QueryConditionOperators.EQUAL,
							dataObjectFile.getName())
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_META_DATA_ATTR_ID,
							QueryConditionOperators.EQUAL, id);

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());

			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							MiscIRODSUtils.getZoneInPath(absPath));

			return DataAOHelper
					.buildMetaDataAndDomainDataFromResultSetRowForDataObject(
							resultSet.getFirstResult(), 1);

		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("error building query", e);
		} catch (JargonQueryException jqe) {
			log.error("error executing query", jqe);
			throw new JargonException("error executing query", jqe);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findMetadataValuesForDataObject
	 * (java.lang.String)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesForDataObject(
			final String dataObjectAbsolutePath) throws FileNotFoundException,
			JargonException {

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty dataObjectAbsolutePath");
		}

		MiscIRODSUtils.checkPathSizeForMax(dataObjectAbsolutePath);

		log.info("findMetadataValuesForDataObject: {}", dataObjectAbsolutePath);

		ObjStat objStat = this.retrieveObjStat(dataObjectAbsolutePath);

		return findMetadataValuesForDataObject(objStat);
	}

	private List<MetaDataAndDomainData> findMetadataValuesForDataObject(
			final ObjStat objStat) throws FileNotFoundException,
			JargonException {

		if (objStat == null) {
			throw new IllegalArgumentException("null or empty objStat");
		}

		log.info("findMetadataValuesForDataObject: {}", objStat);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		CollectionAndPath collectionAndName = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(objStat
						.getAbsolutePath());

		try {
			return this.findMetadataValuesForDataObjectUsingAVUQuery(
					queryElements, collectionAndName.getCollectionParent(),
					collectionAndName.getChildName());
		} catch (JargonQueryException e) {
			log.error("query exception looking up data object:{}",
					objStat.getAbsolutePath(), e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#findMetadataValuesForDataObject
	 * (org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public List<MetaDataAndDomainData> findMetadataValuesForDataObject(
			final IRODSFile irodsFile) throws JargonException {

		if (irodsFile == null) {
			throw new IllegalArgumentException("null irodsFile");
		}

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		try {
			return this.findMetadataValuesForDataObjectUsingAVUQuery(
					queryElements, irodsFile.getParent(), irodsFile.getName());
		} catch (JargonQueryException e) {
			log.error("query exception rethrown as Jargon Exception", e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#computeMD5ChecksumOnFile(org.irods
	 * .jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public String computeMD5ChecksumOnDataObject(final IRODSFile irodsFile)
			throws JargonException {

		if (irodsFile == null) {
			throw new IllegalArgumentException("irodsFile is null");
		}

		log.info("computing checksum on irodsFile: {}",
				irodsFile.getAbsolutePath());

		DataObjInp dataObjInp = DataObjInp
				.instanceForDataObjectChecksum(irodsFile.getAbsolutePath());
		Tag response = getIRODSProtocol().irodsFunction(dataObjInp);

		if (response == null) {
			log.error("invalid response to checksum call, response was null, expected checksum value");
			throw new JargonException(
					"invalid response to checksum call, received null response when doing checksum on file:"
							+ irodsFile);
		}

		String returnedChecksum = response.getTag(DataObjInp.MY_STR)
				.getStringValue();
		log.info("checksum is: {}", returnedChecksum);
		return returnedChecksum;
	}

	/**
	 * @param irodsFile
	 * @param checksumEncoding
	 * @return
	 * @throws JargonException
	 */
	@Override
	public ChecksumValue computeChecksumOnDataObject(final IRODSFile irodsFile)
			throws JargonException {

		log.info("computeChecksumOnDataObject()");

		if (irodsFile == null) {
			throw new IllegalArgumentException("irodsFile is null");
		}

		log.info("computing checksum on irodsFile: {}",
				irodsFile.getAbsolutePath());

		DataObjInp dataObjInp = DataObjInp
				.instanceForDataObjectChecksum(irodsFile.getAbsolutePath());
		Tag response = getIRODSProtocol().irodsFunction(dataObjInp);

		if (response == null) {
			log.error("invalid response to checksum call, response was null, expected checksum value");
			throw new JargonException(
					"invalid response to checksum call, received null response when doing checksum on file:"
							+ irodsFile);
		}

		String returnedChecksum = response.getTag(DataObjInp.MY_STR)
				.getStringValue();
		log.info("checksum is: {}", returnedChecksum);

		return dataAOHelper.computeChecksumValueFromIrodsData(returnedChecksum);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#setAccessPermissionRead(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setAccessPermissionRead(final String zone,
			final String absolutePath, final String userName)
			throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		/*
		 * Handle soft links by munging the path
		 */
		ObjStat objStat = this.retrieveObjStat(absolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermission(false, zone, absPath, userName,
						ModAccessControlInp.READ_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#setAccessPermissionReadInAdminMode
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setAccessPermissionReadInAdminMode(final String zone,
			final String absolutePath, final String userName)
			throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		/*
		 * Handle soft links by munging the path
		 */

		ObjStat objStat = this.retrieveObjStat(absolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermissionInAdminMode(false, zone, absPath,
						userName, ModAccessControlInp.READ_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#setAccessPermissionWrite(java.
	 * lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setAccessPermissionWrite(final String zone,
			final String absolutePath, final String userName)
			throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		ObjStat objStat = this.retrieveObjStat(absolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermission(false, zone, absPath, userName,
						ModAccessControlInp.WRITE_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#setAccessPermission(java.lang.
	 * String, java.lang.String, java.lang.String,
	 * org.irods.jargon.core.protovalues.FilePermissionEnum)
	 */
	@Override
	public void setAccessPermission(final String zone,
			final String absolutePath, final String userName,
			final FilePermissionEnum filePermission) throws JargonException {

		log.info("setAccessPermission()");

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (filePermission == null) {
			throw new IllegalArgumentException("null filePermission");
		}

		// right now, own, read, write are only permission I can set

		if (filePermission == FilePermissionEnum.OWN) {
			setAccessPermissionOwn(zone, absolutePath, userName);
		} else if (filePermission == FilePermissionEnum.READ) {
			setAccessPermissionRead(zone, absolutePath, userName);
		} else if (filePermission == FilePermissionEnum.WRITE) {
			setAccessPermissionWrite(zone, absolutePath, userName);
		} else if (filePermission == FilePermissionEnum.NONE) {
			removeAccessPermissionsForUser(zone, absolutePath, userName);
		} else {
			throw new JargonException(
					"Cannot update permission, currently only READ, WRITE, and OWN, and NONE are supported");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#setAccessPermissionWriteInAdminMode
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setAccessPermissionWriteInAdminMode(final String zone,
			final String absolutePath, final String userName)
			throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		ObjStat objStat = this.retrieveObjStat(absolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);
		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermissionInAdminMode(false, zone, absPath,
						userName, ModAccessControlInp.WRITE_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#setAccessPermissionOwn(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setAccessPermissionOwn(final String zone,
			final String absolutePath, final String userName)
			throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		ObjStat objStat = this.retrieveObjStat(absolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);
		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermission(false, zone, absPath, userName,
						ModAccessControlInp.OWN_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#setAccessPermissionOwnInAdminMode
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setAccessPermissionOwnInAdminMode(final String zone,
			final String absolutePath, final String userName)
			throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		/*
		 * Handle soft links by munging the path
		 */

		ObjStat objStat = this.retrieveObjStat(absolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);
		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermissionInAdminMode(false, zone, absPath,
						userName, ModAccessControlInp.OWN_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#removeAccessPermissionsForUser
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void removeAccessPermissionsForUser(final String zone,
			final String absolutePath, final String userName)
			throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		/*
		 * Handle soft links by munging the path
		 */
		ObjStat objStat = this.retrieveObjStat(absolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);
		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermission(false, zone, absPath, userName,
						ModAccessControlInp.NULL_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#
	 * removeAccessPermissionsForUserInAdminMode(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void removeAccessPermissionsForUserInAdminMode(final String zone,
			final String absolutePath, final String userName)
			throws JargonException {

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		/*
		 * Handle soft links by munging the path
		 */
		CollectionAndPath collName = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(absolutePath);

		ObjStat objStat = this.retrieveObjStat(absolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);

		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermissionInAdminMode(false, zone, absPath,
						userName, ModAccessControlInp.NULL_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#getPermissionForDataObject(java
	 * .lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public FilePermissionEnum getPermissionForDataObject(
			final String absolutePath, final String userName, final String zone)
			throws JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException(NULL_OR_EMPTY_ABSOLUTE_PATH);
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		UserFilePermission userFilePermission = this
				.getPermissionForDataObjectForUserName(absolutePath, userName);

		if (userFilePermission == null) {
			return null;
		} else {
			return userFilePermission.getFilePermissionEnum();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#listPermissionsForDataObject(java
	 * .lang.String, java.lang.String)
	 */
	// FIXME: add group access

	@Override
	public List<UserFilePermission> listPermissionsForDataObject(
			final String irodsCollectionAbsolutePath, final String dataName)
			throws JargonException {

		if (irodsCollectionAbsolutePath == null
				|| irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					NULL_OR_EMPTY_IRODS_COLLECTION_ABSOLUTE_PATH);
		}

		if (dataName == null || dataName.isEmpty()) {
			throw new IllegalArgumentException("null or empty dataName");
		}

		MiscIRODSUtils.checkPathSizeForMax(irodsCollectionAbsolutePath,
				dataName);

		log.info("listPermissionsForDataObject path: {}",
				irodsCollectionAbsolutePath);
		log.info("dataName: {}", irodsCollectionAbsolutePath);

		ObjStat objStat = getObjectStatForAbsolutePath(irodsCollectionAbsolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		String absPath = resolveAbsolutePathGivenObjStat(objStat);

		List<UserFilePermission> userFilePermissions = new ArrayList<UserFilePermission>();
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

		DataAOHelper.buildACLQueryForCollectionPathAndDataName(absPath,
				dataName, builder);

		IRODSQueryResultSetInterface resultSet;

		try {
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);

			for (IRODSQueryResultRow row : resultSet.getResults()) {
				userFilePermissions
						.add(buildUserFilePermissionFromResultRow(row));
			}

		} catch (JargonQueryException e) {
			log.error("query exception for  query", e);
			throw new JargonException(
					"error in query loading user file permissions for data object",
					e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for  query", e);
			throw new JargonException(
					"error in query loading user file permissions for data object",
					e);
		}

		return userFilePermissions;

	}

	/**
	 * @param row
	 * @return
	 * @throws JargonException
	 */
	private UserFilePermission buildUserFilePermissionFromResultRow(
			final IRODSQueryResultRow row) throws JargonException {

		UserFilePermission userFilePermission;
		userFilePermission = new UserFilePermission(row.getColumn(0),
				row.getColumn(1),
				FilePermissionEnum.valueOf(IRODSDataConversionUtil
						.getIntOrZeroFromIRODSValue(row.getColumn(2))),
				UserTypeEnum.findTypeByString(row.getColumn(3)),
				row.getColumn(4));
		return userFilePermission;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#listPermissionsForDataObject(java
	 * .lang.String)
	 */
	// FIXME: add group access
	@Override
	public List<UserFilePermission> listPermissionsForDataObject(
			final String irodsDataObjectAbsolutePath) throws JargonException {

		if (irodsDataObjectAbsolutePath == null
				|| irodsDataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsDataObjectAbsolutePath");
		}

		MiscIRODSUtils.checkPathSizeForMax(irodsDataObjectAbsolutePath);

		log.info("listPermissionsForDataObject: {}",
				irodsDataObjectAbsolutePath);
		CollectionAndPath collName = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(irodsDataObjectAbsolutePath);
		return listPermissionsForDataObject(collName.getCollectionParent(),
				collName.getChildName());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#
	 * modifyAvuValueBasedOnGivenAttributeAndUnit(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void modifyAvuValueBasedOnGivenAttributeAndUnit(
			final String absolutePath, final AvuData avuData)
			throws DataNotFoundException, JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException(NULL_OR_EMPTY_ABSOLUTE_PATH);
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null avuData");
		}

		MiscIRODSUtils.checkPathSizeForMax(absolutePath);

		log.info("setting avu metadata value for dataObject");
		log.info("with avu metadata:{}", avuData);
		log.info("absolute path: {}", absolutePath);

		ObjStat objStat = this.retrieveObjStat(absolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		// avu is distinct based on attrib and value, so do an attrib/unit
		// query, can only be one result
		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> result;

		try {
			queryElements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryElement.AVUQueryPart.ATTRIBUTE,
					AVUQueryOperatorEnum.EQUAL, avuData.getAttribute()));
			queryElements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryElement.AVUQueryPart.UNITS,
					AVUQueryOperatorEnum.EQUAL, avuData.getUnit()));
			result = this.findMetadataValuesForDataObjectUsingAVUQuery(
					queryElements, absolutePath);
		} catch (JargonQueryException e) {
			log.error("error querying data for avu", e);
			throw new JargonException("error querying data for AVU");
		}

		if (result.isEmpty()) {
			throw new DataNotFoundException("no avu data found");
		} else if (result.size() > 1) {
			throw new JargonException(
					"more than one AVU found with given attribute and unit, cannot modify non-unique AVU's in this way");
		}

		AvuData currentAvuData = new AvuData(result.get(0).getAvuAttribute(),
				result.get(0).getAvuValue(), result.get(0).getAvuUnit());

		AvuData modAvuData = new AvuData(result.get(0).getAvuAttribute(),
				avuData.getValue(), result.get(0).getAvuUnit());
		modifyAVUMetadata(absolutePath, currentAvuData, modAvuData);
		log.info("metadata modified to:{}", modAvuData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#modifyAVUMetadata(java.lang.String
	 * , org.irods.jargon.core.pub.domain.AvuData,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void modifyAVUMetadata(final String dataObjectAbsolutePath,
			final AvuData currentAvuData, final AvuData newAvuData)
			throws DataNotFoundException, JargonException {

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty dataObjectAbsolutePath");
		}

		if (currentAvuData == null) {
			throw new IllegalArgumentException("null currentAvuData");
		}

		if (newAvuData == null) {
			throw new IllegalArgumentException("null newAvuData");
		}

		MiscIRODSUtils.checkPathSizeForMax(dataObjectAbsolutePath);

		log.info("overwrite avu metadata for data object: {}", currentAvuData);
		log.info("with new avu metadata:{}", newAvuData);
		log.info("absolute path: {}", dataObjectAbsolutePath);

		ObjStat objStat = this.retrieveObjStat(dataObjectAbsolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForModifyDataObjectMetadata(dataObjectAbsolutePath,
						currentAvuData, newAvuData);

		log.debug("sending avu request");

		try {

			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);

		} catch (JargonException je) {

			if (je.getMessage().indexOf("-817000") > -1) {
				throw new DataNotFoundException(
						"Target data object was not found, could not modify AVU");
			}

			log.error("jargon exception modifying AVU metadata", je);
			throw je;
		}

		log.debug("metadata rewritten");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#modifyAVUMetadata(java.lang.String
	 * , java.lang.String, org.irods.jargon.core.pub.domain.AvuData,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void modifyAVUMetadata(final String irodsCollectionAbsolutePath,
			final String dataObjectName, final AvuData currentAvuData,
			final AvuData newAvuData) throws DataNotFoundException,
			JargonException {

		if (irodsCollectionAbsolutePath == null
				|| irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					NULL_OR_EMPTY_IRODS_COLLECTION_ABSOLUTE_PATH);
		}

		if (dataObjectName == null || dataObjectName.isEmpty()) {
			throw new IllegalArgumentException("null or empty dataObjectName");
		}

		if (currentAvuData == null) {
			throw new IllegalArgumentException("null currentAvuData");
		}

		if (newAvuData == null) {
			throw new IllegalArgumentException("null newAvuData");
		}

		MiscIRODSUtils.checkPathSizeForMax(irodsCollectionAbsolutePath);

		ObjStat objStat = this.retrieveObjStat(irodsCollectionAbsolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		log.info("overwrite avu metadata for collection: {}", currentAvuData);
		log.info("with new avu metadata:{}", newAvuData);
		log.info("absolute path: {}", irodsCollectionAbsolutePath);
		log.info(" data object name: {}", dataObjectName);

		StringBuilder sb = new StringBuilder();
		sb.append(irodsCollectionAbsolutePath);
		sb.append("/");
		sb.append(dataObjectName);

		modifyAVUMetadata(sb.toString(), currentAvuData, newAvuData);

	}

	/**
	 * Get permission value via specific query for qroup based permissions
	 * 
	 * @param dataName
	 * @param userName
	 * @param objStat
	 * @param absPath
	 * @return
	 * @throws JargonException
	 */
	private UserFilePermission getPermissionViaSpecQueryAsGroupMember(
			final String dataName, final String userName,
			final ObjStat objStat, final String absPath) throws JargonException {
		log.info("see if there is a permission based on group membership...");
		UserFilePermission permissionViaGroup = null;

		if (getJargonProperties()
				.isUsingSpecQueryForDataObjPermissionsForUserInGroup()) {
			log.info("is set to use specific query for group permissions via isUsingSpecQueryForDataObjPermissionsForUserInGroup()");
			permissionViaGroup = findPermissionForUserGrantedThroughUserGroup(
					userName, MiscIRODSUtils.getZoneInPath(absPath),
					objStat.determineAbsolutePathBasedOnCollTypeInObjectStat());
			return permissionViaGroup;
		} else {
			log.info("no group membership data found, not using specific query");
			return null;
		}
	}

	private UserFilePermission getPermissionViaGenQuery(final String dataName,
			final String userName, final String absPath) throws JargonException {
		UserFilePermission userFilePermission = null;
		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

		DataAOHelper.buildACLQueryForCollectionPathAndDataName(absPath,
				dataName, builder);

		IRODSQueryResultSetInterface resultSet;

		try {
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_NAME,
					QueryConditionOperators.EQUAL, userName)
					.addOrderByGenQueryField(
							RodsGenQueryEnum.COL_DATA_ACCESS_TYPE,
							OrderByType.DESC);
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							MiscIRODSUtils.getZoneInPath(absPath));

			// FIXME: this may be incorrect, should I return the highest? What
			// if there are multiples? should I sort on highest, add order by

			IRODSQueryResultRow row = resultSet.getFirstResult();
			userFilePermission = buildUserFilePermissionFromResultRow(row);
			log.debug("loaded filePermission:{}", userFilePermission);

		} catch (JargonQueryException e) {
			log.error("query exception for  query", e);
			throw new JargonException(
					"error in query loading user file permissions for data object",
					e);
		} catch (DataNotFoundException dnf) {
			log.info("no data found for user ACL");
		} catch (GenQueryBuilderException e) {
			log.error("query exception for  query", e);
			throw new JargonException(
					"error in query loading user file permissions for data object",
					e);
		}
		return userFilePermission;
	}

	private UserFilePermission findPermissionForUserGrantedThroughUserGroup(
			final String userName, final String zone, final String absPath)
			throws JargonException {

		log.info("findPermissionForUserGrantedThroughUserGroup()");

		IRODSFile collFile = getIRODSFileFactory().instanceIRODSFile(absPath);

		SpecificQueryAO specificQueryAO = getIRODSAccessObjectFactory()
				.getSpecificQueryAO(getIRODSAccount());

		if (!specificQueryAO.isSupportsSpecificQuery()) {
			log.info("no specific query support, so just return null");
			return null;
		}

		// I support spec query, give it a try

		List<String> arguments = new ArrayList<String>(3);
		arguments.add(collFile.getParent());
		arguments.add(collFile.getName());
		arguments.add(userName);

		SpecificQuery specificQuery = SpecificQuery.instanceArguments(
				"listUserACLForDataObjViaGroup", arguments, 0, zone);

		SpecificQueryResultSet specificQueryResultSet;
		UserFilePermission userFilePermission = null;
		try {
			specificQueryResultSet = specificQueryAO
					.executeSpecificQueryUsingAlias(specificQuery,
							getJargonProperties().getMaxFilesAndDirsQueryMax(),
							0);

			IRODSQueryResultRow row = null;

			try {
				row = specificQueryResultSet.getFirstResult();
				userFilePermission = buildUserFilePermissionFromResultRow(row);

			} catch (DataNotFoundException dnf) {
				log.info("no result, return null");
				return null;
			}

		} catch (JargonQueryException e) {
			log.error(
					"jargon query exception looking up permission via specific query",
					e);
			throw new JargonException(e);
		}

		return userFilePermission;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#listFileResources(java.lang.String
	 * )
	 */
	@Override
	public List<Resource> listFileResources(final String irodsAbsolutePath)
			throws JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		MiscIRODSUtils.checkPathSizeForMax(irodsAbsolutePath);

		log.info("listFileResources() for path:{}", irodsAbsolutePath);

		ResourceAOHelper resourceAOHelper = new ResourceAOHelper(
				getIRODSAccount(), getIRODSAccessObjectFactory());
		IRODSFile irodsFile = getIRODSFileFactory().instanceIRODSFile(
				irodsAbsolutePath);

		ObjStat objStat = getObjectStatForAbsolutePath(irodsAbsolutePath);
		String absPath = resolveAbsolutePathGivenObjStat(objStat);
		CollectionAndPath collName = MiscIRODSUtils
				.separateCollectionAndPathFromGivenAbsolutePath(absPath);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

		resourceAOHelper.buildResourceSelects(builder);

		if (irodsFile.exists() && irodsFile.isFile()) {

			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
					QueryConditionOperators.EQUAL,
					collName.getCollectionParent())
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_DATA_NAME,
							QueryConditionOperators.EQUAL, irodsFile.getName());

		} else {
			log.error(
					"file for query does not exist, or is not a file at path:{}",
					irodsAbsolutePath);
			throw new JargonException("file does not exist, or is not a file");
		}

		IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				getIRODSSession(), getIRODSAccount());

		IRODSQueryResultSetInterface resultSet;
		try {
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(getJargonProperties()
							.getMaxFilesAndDirsQueryMax());
			resultSet = irodsGenQueryExecutorImpl
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0,
							MiscIRODSUtils.getZoneInPath(absPath));
		} catch (JargonQueryException e) {
			log.error("query exception", e);
			throw new JargonException("error in query");
		} catch (GenQueryBuilderException e) {
			log.error("query exception", e);
			throw new JargonException("error in query");
		}

		List<Resource> resources = resourceAOHelper
				.buildResourceListFromResultSetClassic(resultSet);

		if (resources.isEmpty()) {
			log.warn("no data found");
			throw new DataNotFoundException("no resources found for file:"
					+ irodsFile.getAbsolutePath());
		}

		return resources;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#listReplicationsForFileInResGroup
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<DataObject> listReplicationsForFileInResGroup(
			String collectionAbsPath, final String fileName,
			final String resourceGroupName) throws JargonException {

		log.info("listReplicationsForFileInResGroup");

		if (collectionAbsPath == null || collectionAbsPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection");
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty filename");
		}

		if (resourceGroupName == null || resourceGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resgroup");
		}

		if (collectionAbsPath.endsWith("/")) {
			collectionAbsPath = collectionAbsPath.substring(0,
					collectionAbsPath.length() - 1);
		}

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet;
		IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(getIRODSAccount());

		try {
			DataAOHelper.addDataObjectSelectsToBuilder(builder);
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
					QueryConditionOperators.EQUAL, collectionAbsPath)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_DATA_NAME,
							QueryConditionOperators.EQUAL, fileName)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_D_RESC_GROUP_NAME,
							QueryConditionOperators.EQUAL, resourceGroupName);

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(100);

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
			return DataAOHelper.buildListFromResultSet(resultSet);
		} catch (Exception e) {
			log.error("Error executing Query getReplicationsForFile()", e);
			throw new JargonException(
					"error querying for files in resource group", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#getPermissionForDataObjectForUserName
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public UserFilePermission getPermissionForDataObjectForUserName(
			final String irodsAbsolutePath, final String userName)
			throws JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		MiscIRODSUtils.checkPathSizeForMax(irodsAbsolutePath);

		ObjStat objStat = this.retrieveObjStat(irodsAbsolutePath);

		if (objStat.getSpecColType() == SpecColType.MOUNTED_COLL) {
			log.info(
					"objStat indicates collection type that does not support this operation:{}",
					objStat);
			throw new OperationNotSupportedForCollectionTypeException(
					"The special collection type does not support this operation");
		}

		log.info("listPermissionsForDataObjectForUserName path: {}",
				irodsAbsolutePath);
		log.info("userName:{}", userName);

		IRODSFile irodsFile = getIRODSFileFactory().instanceIRODSFile(
				resolveAbsolutePathGivenObjStat(objStat));

		/*
		 * User may have permission via a direct user permission, or may have a
		 * group level permission, check both and get the highest value
		 */

		UserFilePermission userFilePermission = getPermissionViaGenQuery(
				irodsFile.getName(), userName, irodsFile.getParent());

		// be tolerant if the specific query facility is not available. FIXME:
		// add to cache.this is a patch
		UserFilePermission groupFilePermission;
		try {
			groupFilePermission = getPermissionViaSpecQueryAsGroupMember(
					irodsFile.getName(), userName, objStat,
					irodsFile.getParent());
		} catch (Exception e) {
			log.error("error in getting group permission, see bug 1655");
			return userFilePermission;
		}

		return scoreAndReturnHighestPermission(userFilePermission,
				groupFilePermission);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#
	 * listPermissionsForDataObjectForUserName(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public UserFilePermission getPermissionForDataObjectForUserName(
			final String irodsCollectionAbsolutePath, final String dataName,
			final String userName) throws JargonException {

		if (irodsCollectionAbsolutePath == null
				|| irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					NULL_OR_EMPTY_IRODS_COLLECTION_ABSOLUTE_PATH);
		}

		if (dataName == null || dataName.isEmpty()) {
			throw new IllegalArgumentException("null or empty dataName");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		StringBuilder sb = new StringBuilder();
		sb.append(irodsCollectionAbsolutePath);
		sb.append('/');
		sb.append(dataName);

		return this.getPermissionForDataObjectForUserName(sb.toString(),
				userName);
	}

	/**
	 * @param transferOptions
	 * @return
	 * @throws JargonException
	 */

	private TransferOptions buildDefaultTransferOptionsIfNotSpecified(
			final TransferOptions transferOptions) throws JargonException {
		TransferOptions myTransferOptions = transferOptions;

		if (transferOptions == null) {
			myTransferOptions = getIRODSSession()
					.buildTransferOptionsBasedOnJargonProperties();
		} else {
			myTransferOptions = new TransferOptions(transferOptions);
		}
		return myTransferOptions;
	}

	/**
	 * Check the provided <code>TransferControlBlock</code> to make sure the
	 * <code>TransferOptions</code> are specified. If they are not specified,
	 * then put in defaults.
	 * 
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} to check for
	 *            <code>TransferOptions</code>, can be <code>null</code>
	 * @throws JargonException
	 */
	private TransferControlBlock checkTransferControlBlockForOptionsAndSetDefaultsIfNotSpecified(
			final TransferControlBlock transferControlBlock)
			throws JargonException {
		TransferControlBlock effectiveTransferControlBlock = transferControlBlock;
		if (effectiveTransferControlBlock == null) {
			log.info("no transferControlBlock provided, building a default version");
			effectiveTransferControlBlock = DefaultTransferControlBlock
					.instance();
		}

		if (effectiveTransferControlBlock.getTransferOptions() == null) {
			log.info("creating a default transferOptions, as none specified");
			effectiveTransferControlBlock.setTransferOptions(getIRODSSession()
					.buildTransferOptionsBasedOnJargonProperties());
		}

		return effectiveTransferControlBlock;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.FileCatalogObjectAOImpl#isUserHasAccess(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public boolean isUserHasAccess(final String irodsAbsolutePath,
			final String userName) throws JargonException {

		log.info("isUserHasAccess()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		MiscIRODSUtils.checkPathSizeForMax(irodsAbsolutePath);

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("userName:{}", userName);

		UserFilePermission derivedPermission = this
				.getPermissionForDataObjectForUserName(irodsAbsolutePath,
						userName);
		boolean hasPermission = false;
		if (derivedPermission != null) {
			hasPermission = true;
		}

		log.info("has permision? {}", hasPermission);
		return hasPermission;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#getTotalNumberOfReplsForDataObject
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public int getTotalNumberOfReplsForDataObject(
			final String irodsAbsolutePath, final String fileName)
			throws JargonException {

		log.info("getTotalNumberOfReplsForDataObject()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		List<Resource> totalres;
		totalres = getResourcesForDataObject(irodsAbsolutePath, fileName);

		return totalres.size();

	}

	@Override
	public int getTotalNumberOfReplsInResourceGroupForDataObject(
			final String irodsAbsolutePath, final String fileName,
			final String resourceGroupName) throws JargonException {

		log.info("getTotalNumberOfReplsInResourceGroupForDataObject()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		if (resourceGroupName == null || resourceGroupName.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty resourceGroupName");
		}

		List<DataObject> totalres;
		totalres = listReplicationsForFileInResGroup(irodsAbsolutePath,
				fileName, resourceGroupName);

		return totalres.size();

	}

	/**
	 * General method to trim replicas for a resource or resource group. Check
	 * the parameter notes carefully.
	 * 
	 * @param irodsCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the iRODS parent
	 *            collection
	 * @param fileName
	 *            <code>String</code> with the file name of the data object to
	 *            be trimmed
	 * @param resourceName
	 *            <code>String</code> with the optional (blank if not specified)
	 *            replica resource to trim
	 * @param numberOfCopiesToKeep
	 *            <code>int</code> with the optional (leave -1 if not specified)
	 *            number of copies to retain
	 * @param replicaNumberToDelete
	 *            <code>int</code> with a specific replica number to trim (leave
	 *            as -1 if not specified)
	 * @param asIRODSAdmin
	 *            <code>boolean</code> to process the given action as the
	 *            rodsAdmin
	 * @throws JargonException
	 */
	@Override
	public void trimDataObjectReplicas(
			final String irodsCollectionAbsolutePath, final String fileName,
			final String resourceName, final int numberOfCopiesToKeep,
			final int replicaNumberToDelete, final boolean asIRODSAdmin)
			throws DataNotFoundException, JargonException {
		log.info("trimDataObjectReplicasInResourceGroup");

		if (irodsCollectionAbsolutePath == null
				|| irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsCollectionAbsolutePath");
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		if (resourceName == null) {
			throw new IllegalArgumentException("null resourceName");
		}

		log.info("irodsCollectionAbsolutePath:{}", irodsCollectionAbsolutePath);
		log.info("fileName:{}", fileName);
		log.info("resourceName:{}", resourceName);

		log.info("numberOfCopiesToKeep:{}", numberOfCopiesToKeep);
		log.info("replicaNumberToDelete:{}", replicaNumberToDelete);
		log.info("asIRODSAdmin:{}", asIRODSAdmin);

		if (!getIRODSServerProperties()
				.isTheIrodsServerAtLeastAtTheGivenReleaseVersion("rods3.0")) {
			throw new JargonException(
					"service not available on servers prior to rods3.0");
		}

		RuleProcessingAO ruleProcessingAO = getIRODSAccessObjectFactory()
				.getRuleProcessingAO(getIRODSAccount());

		List<IRODSRuleParameter> irodsRuleParameters = new ArrayList<IRODSRuleParameter>();

		StringBuilder sb = new StringBuilder();
		sb.append(irodsCollectionAbsolutePath);
		sb.append("/");
		sb.append(fileName);

		irodsRuleParameters.add(new IRODSRuleParameter("*SourceFile",
				MiscIRODSUtils.wrapStringInQuotes(sb.toString())));

		if (resourceName.isEmpty()) {
			irodsRuleParameters.add(new IRODSRuleParameter("*StorageResource",
					MiscIRODSUtils.wrapStringInQuotes("null")));
		} else {
			irodsRuleParameters.add(new IRODSRuleParameter("*StorageResource",
					MiscIRODSUtils.wrapStringInQuotes(resourceName)));
		}

		if (replicaNumberToDelete > -1) {
			irodsRuleParameters.add(new IRODSRuleParameter("*ReplicaNumber",
					MiscIRODSUtils.wrapStringInQuotes(String
							.valueOf(replicaNumberToDelete))));

		} else {
			irodsRuleParameters.add(new IRODSRuleParameter("*ReplicaNumber",
					MiscIRODSUtils.wrapStringInQuotes("null")));
		}

		if (numberOfCopiesToKeep > -1) {
			irodsRuleParameters.add(new IRODSRuleParameter("*KeepReplicas",
					MiscIRODSUtils.wrapStringInQuotes(String
							.valueOf(numberOfCopiesToKeep))));
		} else {
			irodsRuleParameters.add(new IRODSRuleParameter("*KeepReplicas",
					MiscIRODSUtils.wrapStringInQuotes("null")));
		}

		if (asIRODSAdmin) {
			irodsRuleParameters.add(new IRODSRuleParameter("*IRODSAdminFlag",
					MiscIRODSUtils.wrapStringInQuotes(String
							.valueOf(asIRODSAdmin))));
		} else {
			irodsRuleParameters.add(new IRODSRuleParameter("*IRODSAdminFlag",
					MiscIRODSUtils.wrapStringInQuotes("null")));
		}

		try {
			IRODSRuleExecResult result = ruleProcessingAO
					.executeRuleFromResource("/rules/trimDataObject.r",
							irodsRuleParameters, RuleProcessingType.EXTERNAL);
			log.info("result of action:{}", result.getRuleExecOut().trim());

		} catch (InvalidInputParameterException e) {
			log.warn(
					"invalid input parameter, for iRODS 4.0 plus treat this like it should be an ignore to preserve previous behavior",
					e);
			if (getIRODSServerProperties().isEirods()) {
				log.warn("ignored....is eirods");
			} else {
				throw e;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#listReplicationsForFile(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	public List<DataObject> listReplicationsForFile(String collectionAbsPath,
			final String fileName) throws JargonException {

		log.info("listReplicationsForFile");

		if (collectionAbsPath == null || collectionAbsPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection");
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty filename");
		}

		if (collectionAbsPath.endsWith("/")) {
			collectionAbsPath = collectionAbsPath.substring(0,
					collectionAbsPath.length() - 1);
		}

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet;
		IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(getIRODSAccount());

		try {
			DataAOHelper.addDataObjectSelectsToBuilder(builder);
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_COLL_NAME,
					QueryConditionOperators.EQUAL, collectionAbsPath)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_DATA_NAME,
							QueryConditionOperators.EQUAL, fileName);

			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(100);

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);

			return DataAOHelper.buildListFromResultSet(resultSet);

		} catch (Exception e) {
			log.error("error querying for replicas", e);
			throw new JargonException("error querying for file replicas", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#
	 * computeSHA1ChecksumOfIrodsFileByReadingDataFromStream(java.lang.String)
	 */
	@Override
	public byte[] computeSHA1ChecksumOfIrodsFileByReadingDataFromStream(
			final String irodsAbsolutePath) throws DataNotFoundException,
			JargonException {

		log.info("computeSHA1ChecksumOfIrodsFileByReadingDataFromStream()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		log.info("get input stream and read to compute sha1");
		InputStream is = new BufferedInputStream(getIRODSFileFactory()
				.instanceIRODSFileInputStream(irodsAbsolutePath));
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			log.error("unable to get SHA1 message digest", e);
			throw new JargonException("cannot commute SHA1 checksum", e);
		}

		byte[] dataBytes = new byte[1024];

		int nread = 0;

		try {
			while ((nread = is.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}
		} catch (IOException e) {
			log.error("IO exception computing sha1 checksum", e);
			throw new JargonException(e.getMessage());
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// ignore
			}
		}

		byte[] mdbytes = md.digest();
		log.info("computed");
		return mdbytes;

	}

}
