package org.irods.jargon.core.pub;

import static edu.sdsc.grid.io.irods.IRODSConstants.MsgHeader_PI;
import static edu.sdsc.grid.io.irods.IRODSConstants.PortList_PI;
import static edu.sdsc.grid.io.irods.IRODSConstants.cookie;
import static edu.sdsc.grid.io.irods.IRODSConstants.hostAddr;
import static edu.sdsc.grid.io.irods.IRODSConstants.l1descInx;
import static edu.sdsc.grid.io.irods.IRODSConstants.numThreads;
import static edu.sdsc.grid.io.irods.IRODSConstants.portNum;
import static org.irods.jargon.core.pub.aohelper.AOHelper.AND;
import static org.irods.jargon.core.pub.aohelper.AOHelper.COMMA;
import static org.irods.jargon.core.pub.aohelper.AOHelper.EQUALS_AND_QUOTE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.QUOTE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.SPACE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.WHERE;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.ConnectionConstants;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileIntegrityException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjCopyInp;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.ModAccessControlInp;
import org.irods.jargon.core.packinstr.ModAvuMetadataInp;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.aohelper.DataAOHelper;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.transfer.ParallelGetFileTransferStrategy;
import org.irods.jargon.core.transfer.ParallelPutFileTransferStrategy;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sdsc.grid.io.irods.Tag;

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
 * control such aspects as whether paralllel file transfers are done.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public final class DataObjectAOImpl extends FileCatalogObjectAOImpl implements
		DataObjectAO {

	public static final Logger log = LoggerFactory
			.getLogger(DataObjectAOImpl.class);
	private transient final DataAOHelper dataAOHelper = new DataAOHelper(this.getIRODSAccessObjectFactory(), this.getIRODSAccount());
	private transient final IRODSGenQueryExecutor irodsGenQueryExecutor;

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
		this.irodsGenQueryExecutor = this.getIRODSAccessObjectFactory()
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
			throws DataNotFoundException, JargonException {

		DataObject dataObject = null;

		if (collectionPath == null || collectionPath.isEmpty()) {
			throw new IllegalArgumentException(
					"collection path is null or empty");
		}

		if (dataName == null || dataName.isEmpty()) {
			throw new IllegalArgumentException("dataName is null or empty");
		}

		log.info("find by collection path: {}", collectionPath);
		log.info(" data obj name: {}", dataName);

		final StringBuilder sb = new StringBuilder();
		sb.append(dataAOHelper.buildSelects());
		sb.append(WHERE);
		sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		sb.append(EQUALS_AND_QUOTE);
		sb.append(IRODSDataConversionUtil.escapeSingleQuotes(collectionPath
				.trim()));
		sb.append(QUOTE);
		sb.append(AND);
		sb.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		sb.append(EQUALS_AND_QUOTE);
		sb.append(IRODSDataConversionUtil.escapeSingleQuotes(dataName.trim()));
		sb.append(QUOTE);

		final String query = sb.toString();
		log.debug("query for data object:{}", query);

		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query,
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query: {}", query, e);
			throw new JargonException("error in query for data object", e);
		}

		if (resultSet.getFirstResult() != null) {
			dataObject = dataAOHelper.buildDomainFromResultSetRow(resultSet
					.getFirstResult());
			log.debug("returning: {}", dataObject.toString());
		}

		return dataObject;

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
			throws DataNotFoundException, JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		log.info("findByAbsolutePath() with path:{}", absolutePath);
		IRODSFile irodsFile = this.getIRODSFileFactory().instanceIRODSFile(
				absolutePath);
		return findByCollectionNameAndDataName(irodsFile.getParent(),
				irodsFile.getName());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#findWhere(java.lang.String)
	 */
	@Override
	public List<DataObject> findWhere(final String where)
			throws JargonException {
		return findWhere(where, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#findWhere(java.lang.String,
	 * int)
	 */
	@Override
	public List<DataObject> findWhere(final String where, final int partialStart)
			throws JargonException {

		if (where == null || where.isEmpty()) {
			throw new IllegalArgumentException(
					"where clause is empty, this is not advisable for data object queries");
		}

		log.info("find by where: {}", where);

		final StringBuilder sb = new StringBuilder();
		sb.append(dataAOHelper.buildSelects());
		sb.append(WHERE);
		sb.append(where);

		final String query = sb.toString();
		log.debug("query for data object:{}", query);

		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query,
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryWithPaging(
					irodsQuery, partialStart);

		} catch (JargonQueryException e) {
			log.error("query exception for query: {}", query, e);
			throw new JargonException("error in query for data object", e);
		}
		return dataAOHelper.buildListFromResultSet(resultSet);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#putLocalDataObjectToIRODS(java
	 * .io.File, org.irods.jargon.core.pub.io.IRODSFileImpl, boolean)
	 */
	@Override
	public void putLocalDataObjectToIRODS(final File localFile,
			final IRODSFile irodsFileDestination, final boolean overwrite)
			throws JargonException {

		putLocalDataObjectToIRODSGivingTransferOptions(localFile,
				irodsFileDestination, overwrite, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#
	 * putLocalDataObjectToIRODSGivingTransferOptions(java.io.File,
	 * org.irods.jargon.core.pub.io.IRODSFile, boolean,
	 * org.irods.jargon.core.packinstr.TransferOptions)
	 */
	@Override
	public void putLocalDataObjectToIRODSGivingTransferOptions(
			final File localFile, final IRODSFile irodsFileDestination,
			final boolean overwrite, final TransferOptions transferOptions)
			throws JargonException {

		TransferOptions myTransferOptions = buildDefaultTransferOptionsIfNotSpecified(transferOptions);

		log.info("testing file length to set parallel transfer options");
		if (localFile.length() > ConnectionConstants.MAX_SZ_FOR_SINGLE_BUF) { // FIXME:
																				// remove
			// from
			// props
			myTransferOptions.setMaxThreads(getIRODSSession()
					.getJargonProperties().getMaxParallelThreads());
			log.info("length above threshold, send max threads cap");
		} else {
			myTransferOptions.setMaxThreads(0);
		}

		putLocalDataObjectToIRODS(localFile, irodsFileDestination, overwrite,
				false, myTransferOptions);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#
	 * putLocalDataObjectToIRODSForClientSideRuleOperation(java.io.File,
	 * org.irods.jargon.core.pub.io.IRODSFile, boolean,
	 * org.irods.jargon.core.packinstr.TransferOptions)
	 */
	@Override
	public void putLocalDataObjectToIRODSForClientSideRuleOperation(
			final File localFile, final IRODSFile irodsFileDestination,
			final boolean overwrite, final TransferOptions transferOptions)
			throws JargonException {

		TransferOptions myTransferOptions = buildDefaultTransferOptionsIfNotSpecified(transferOptions);
		putLocalDataObjectToIRODS(localFile, irodsFileDestination, overwrite,
				true, myTransferOptions);
	}

	/**
	 * Internal common method to execute puts.
	 * 
	 * @param localFile
	 *            <code>File</code> with the local data.
	 * @param irodsFileDestination
	 *            <code>IRODSFile</code> that describe the target of the put.
	 * @param overwrite
	 *            <code>boolean</code> that adds a force option to overwrite any
	 *            file already on iRODS.
	 * @param ignoreChecks
	 *            <code>boolean</code> that bypasses any checks of the iRODS
	 *            data before attempting the put.
	 * @param transferOptions
	 *            {@link TransferOptions} that optionally give directions on
	 *            details of the transfer. Will be <code>null</code> if not set.
	 * @throws JargonException
	 */
	protected void putLocalDataObjectToIRODS(final File localFile,
			final IRODSFile irodsFileDestination, final boolean overwrite,
			final boolean ignoreChecks, final TransferOptions transferOptions)
			throws JargonException {

		if (localFile == null) {
			throw new IllegalArgumentException("null local file");
		}

		if (irodsFileDestination == null) {
			throw new IllegalArgumentException("null destination file");
		}

		if (transferOptions == null) {
			throw new IllegalArgumentException("null transferOptions");
		}

		log.info("put operation, localFile: {}", localFile.getAbsolutePath());
		log.info("to irodsFile: {}", irodsFileDestination.getAbsolutePath());

		if (!localFile.exists()) {
			log.error("put error, local file does not exist: {}",
					localFile.getAbsolutePath());
			throw new JargonException(
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

		long localFileLength = localFile.length();

		log.debug("localFileLength:{}", localFileLength);

		if (localFileLength < ConnectionConstants.MAX_SZ_FOR_SINGLE_BUF) {

			log.info("processing transfer as normal, length below max");

			try {
				dataAOHelper.processNormalPutTransfer(localFile, overwrite,
						transferOptions, targetFile, this.getIRODSProtocol());
				return;
			} catch (FileNotFoundException e) {
				log.error(
						"File not found for local file I was trying to put:{}",
						localFile.getAbsolutePath());
				throw new JargonException(
						"localFile not found to put to irods", e);
			}
		} else {

			log.info("processing as a parallel transfer, length above max");

			processAsAParallelPutOperationIfMoreThanZeroThreads(localFile,
					overwrite, transferOptions, targetFile);
		}
		log.info("transfer complete");

	}

	/**
	 * @param localFile
	 * @param overwrite
	 * @param transferOptions
	 * @param targetFile
	 * @throws JargonException
	 */
	private void processAsAParallelPutOperationIfMoreThanZeroThreads(
			final File localFile, final boolean overwrite,
			final TransferOptions transferOptions, final IRODSFile targetFile)
			throws JargonException {
		// if this was below the max_sz_for_single_buf, the data was included in
		// the put above and will have returned

		DataObjInp dataObjInp = DataObjInp.instanceForParallelPut(
				targetFile.getAbsolutePath(), localFile.length(),
				targetFile.getResource(), overwrite, transferOptions);

		try {
			
			if (transferOptions != null) {
				if (transferOptions.isComputeAndVerifyChecksumAfterTransfer() || transferOptions.isComputeChecksumAfterTransfer()) {
					log.info("computing a checksum on the file at:{}", localFile.getAbsolutePath());
					String localFileChecksum = LocalFileUtils.md5ByteArrayToString(LocalFileUtils.computeMD5FileCheckSumViaAbsolutePath(localFile.getAbsolutePath()));
					log.info("local file checksum is:{}", localFileChecksum);
					dataObjInp.setFileChecksumValue(localFileChecksum);
				}
			}
			
			Tag responseToInitialCallForPut = getIRODSProtocol().irodsFunction(
					dataObjInp);

			int numberOfThreads = responseToInitialCallForPut
					.getTag(numThreads).getIntValue();

			if (numberOfThreads < 0) {
				throw new JargonException("numberOfThreads returned from iRODS is < 0, some error occurred");
			} else if (numberOfThreads > 0) {
				parallelPutTransfer(localFile, responseToInitialCallForPut,
						numberOfThreads);
			} else {
				log.info("parallel operation deferred by server sending 0 threads back in PortalOperOut, revert to single thread transfer");
				dataAOHelper.processNormalPutTransfer(localFile, overwrite,
						transferOptions, targetFile, this.getIRODSProtocol());
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
		} catch (FileNotFoundException e) {
			log.error("File not found for local file I was trying to put:{}",
					localFile.getAbsolutePath());
			throw new JargonException("localFile not found to put to irods", e);
		} catch (Exception e) {
			log.error("error in parallel transfer", e);
			throw new JargonException("error in parallel transfer", e);
		}
	}

	/**
	 * @param localFile
	 * @param responseToInitialCallForPut
	 * @param numberOfThreads
	 * @throws JargonException
	 */
	private void parallelPutTransfer(final File localFile,
			final Tag responseToInitialCallForPut, final int numberOfThreads)
			throws JargonException {
		log.info("tranfer will be done using {} threads", numberOfThreads);
		final String host = responseToInitialCallForPut.getTag(PortList_PI)
				.getTag(hostAddr).getStringValue();
		final int port = responseToInitialCallForPut.getTag(PortList_PI)
				.getTag(portNum).getIntValue();
		final int pass = responseToInitialCallForPut.getTag(PortList_PI)
				.getTag(cookie).getIntValue();

		final ParallelPutFileTransferStrategy parallelPutFileStrategy = ParallelPutFileTransferStrategy
				.instance(host, port, numberOfThreads, pass, localFile,
						this.getIRODSAccessObjectFactory());

		log.info(
				"getting ready to initiate parallel file transfer strategy:{}",
				parallelPutFileStrategy);

		parallelPutFileStrategy.transfer();
		log.info("transfer is done, now terminate the keep alive process");

		log.info("transfer process is complete");
		int statusForComplete = responseToInitialCallForPut.getTag(l1descInx)
				.getIntValue();
		log.debug("status for complete:{}", statusForComplete);

		log.info("sending operation complete at termination of parallel transfer");
		this.getIRODSProtocol().operationComplete(statusForComplete);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#irodsDataObjectGetOperation(org
	 * .irods.jargon.core.pub.io.IRODSFile, java.io.File)
	 */
	@Override
	public void getDataObjectFromIrods(final IRODSFile irodsFileToGet,
			final File localFileToHoldData) throws DataNotFoundException,
			JargonException {

		getDataObjectFromIrodsGivingTransferOptions(irodsFileToGet,
				localFileToHoldData, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#
	 * getDataObjectFromIrodsGivingTransferOptions
	 * (org.irods.jargon.core.pub.io.IRODSFile, java.io.File,
	 * org.irods.jargon.core.packinstr.TransferOptions)
	 */
	@Override
	public void getDataObjectFromIrodsGivingTransferOptions(
			final IRODSFile irodsFileToGet, final File localFileToHoldData,
			final TransferOptions transferOptions)
			throws DataNotFoundException, JargonException {

		if (localFileToHoldData == null) {
			throw new IllegalArgumentException("null local file");
		}

		if (irodsFileToGet == null) {
			throw new IllegalArgumentException("nulll destination file");
		}

		TransferOptions myTransferOptions = buildDefaultTransferOptionsIfNotSpecified(transferOptions);

		File localFile;
		if (localFileToHoldData.isDirectory()) {
			log.info("a put to a directory, just use the source file name and accept the directory as a target");
			StringBuilder sb = new StringBuilder();
			sb.append(localFileToHoldData.getAbsolutePath());
			sb.append("/");
			sb.append(irodsFileToGet.getName());
			log.info("target file name will be:{}", sb.toString());
			localFile = new File(sb.toString());
		} else {
			localFile = localFileToHoldData;
		}

		long irodsFileLength = irodsFileToGet.length();
		log.info("testing file length to set parallel transfer options");
		if (irodsFileLength > ConnectionConstants.MAX_SZ_FOR_SINGLE_BUF) {
			myTransferOptions.setMaxThreads(getIRODSSession()
					.getJargonProperties().getMaxParallelThreads());
			log.info("length above threshold, send max threads cap");
		} else {
			myTransferOptions.setMaxThreads(0);
		}

		log.info("target local file: {}", localFile.getAbsolutePath());
		log.info("from source file: {}", irodsFileToGet.getAbsolutePath());

		final DataObjInp dataObjInp = DataObjInp.instanceForGet(
				irodsFileToGet.getAbsolutePath(), irodsFileLength,
				myTransferOptions);

		processGetAfterResourceDetermined(irodsFileToGet, localFile,
				dataObjInp, myTransferOptions, irodsFileLength);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.pub.DataObjectAO#
	 * irodsDataObjectGetOperationUsingTheSpecificResourceSetInIrodsFile
	 * (org.irods.jargon.core.pub.io.IRODSFile, java.io.File)
	 */
	@Override
	public void getDataObjectFromIrodsUsingTheSpecificResourceSetInIrodsFile(
			final IRODSFile irodsFileToGet, final File localFileToHoldData)
			throws DataNotFoundException, JargonException {

		getDataObjectFromIrodsUsingTheSpecificResourceSetInIrodsFileSpecifyingTransferOptions(
				irodsFileToGet, localFileToHoldData, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#
	 * getDataObjectFromIrodsUsingTheSpecificResourceSetInIrodsFileSpecifyingTransferOptions
	 * (org.irods.jargon.core.pub.io.IRODSFile, java.io.File,
	 * org.irods.jargon.core.packinstr.TransferOptions)
	 */
	@Override
	public void getDataObjectFromIrodsUsingTheSpecificResourceSetInIrodsFileSpecifyingTransferOptions(
			final IRODSFile irodsFileToGet, final File localFileToHoldData,
			final TransferOptions transferOptions)
			throws DataNotFoundException, JargonException {

		if (localFileToHoldData == null) {
			throw new IllegalArgumentException("null local file");
		}

		if (irodsFileToGet == null) {
			throw new IllegalArgumentException("nulll destination file");
		}

		File localFile;
		if (localFileToHoldData.isDirectory()) {
			log.info("a put to a directory, just use the source file name and accept the directory as a target");
			StringBuilder sb = new StringBuilder();
			sb.append(localFileToHoldData.getAbsolutePath());
			sb.append("/");
			sb.append(irodsFileToGet.getName());
			log.info("target file name will be:{}", sb.toString());
			localFile = new File(sb.toString());
		} else {
			localFile = localFileToHoldData;
		}

		log.info("target local file: {}", localFile.getAbsolutePath());
		log.info("from source file: {}", irodsFileToGet.getAbsolutePath());

		TransferOptions myTransferOptions = buildDefaultTransferOptionsIfNotSpecified(transferOptions);

		log.info("testing file length to set parallel transfer options");
		if (irodsFileToGet.length() > getIRODSSession().getJargonProperties()
				.getParallelThreadsLengthThreshold()) {
			myTransferOptions.setMaxThreads(getIRODSSession()
					.getJargonProperties().getMaxParallelThreads());
			log.info("length above threshold, send max threads cap");
		} else {
			myTransferOptions.setMaxThreads(0);
		}

		final DataObjInp dataObjInp = DataObjInp
				.instanceForGetSpecifyingResource(
						irodsFileToGet.getAbsolutePath(),
						irodsFileToGet.getResource(), myTransferOptions);

		processGetAfterResourceDetermined(irodsFileToGet, localFile,
				dataObjInp, myTransferOptions, irodsFileToGet.length());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.DataObjectAO#
	 * irodsDataObjectGetOperationForClientSideAction
	 * (org.irods.jargon.core.pub.io.IRODSFile, java.io.File,
	 * org.irods.jargon.core.packinstr.TransferOptions)
	 */
	@Override
	public void irodsDataObjectGetOperationForClientSideAction(
			final IRODSFile irodsFileToGet, final File localFileToHoldData,
			final TransferOptions transferOptions)
			throws DataNotFoundException, JargonException {

		if (localFileToHoldData == null) {
			throw new IllegalArgumentException("null local file");
		}

		if (irodsFileToGet == null) {
			throw new IllegalArgumentException("nulll destination file");
		}

		log.info("target local file: {}", localFileToHoldData.getAbsolutePath());
		log.info("from source file: {}", irodsFileToGet.getAbsolutePath());
		TransferOptions myTransferOptions = this
				.buildDefaultTransferOptionsIfNotSpecified(transferOptions);

		final DataObjInp dataObjInp = DataObjInp
				.instanceForGetSpecifyingResource(
						irodsFileToGet.getAbsolutePath(), "", transferOptions);

		processGetAfterResourceDetermined(irodsFileToGet, localFileToHoldData,
				dataObjInp, myTransferOptions, 0);//irodsFileToGet.length());
	}

	/**
	 * The resource to use for the get operation has been determined.  Note that the state of the resource determination is important due to the fact that finding such state
	 * information requires a GenQuery.  There are certain occasions where get operations are called, where doing an intervening query to the ICAT can cause a protocol issue.
	 * Issuing such a GenQuery can confuse what can be a multi-step protocol.  For this reason, callers of this method can be assured that no queries will be issued to 
	 * iRODS while processing the get.  An occasion where this has been problematic has been the processing of client-side get actions as the result of rule execution.
	 * <p/>
	 * Note that an iRODS file length is passed here.  This avoids any query from an <code>IRODSFile.length()</code> operation.  The length is passed in, as there are some occasions where
	 * the multi-step protocol can show a zero file length, such as when iRODS is preparing to treat a get operation as a parallel file transfer.  There are cases where iRODS responds with a zero length,
	 * indicating a parallel transfer, but a policy rule in place in iRODS may 'turn off' such parallel transfers.  In that case, the length usually referred to returns as a zero, and the number of
	 * threads will be zero.  This must be handled.
	 * 
	 * 
	 * @param irodsFileToGet
	 * @param localFileToHoldData
	 * @param dataObjInp
	 * @param transferOptions
	 * @param irodsFileLength - actual length of file from irods.  This is passed in as there are occasions where the protocol exchange results in a zero file length.  See
	 * the note above.
	 * @throws JargonException
	 * @throws DataNotFoundException
	 * @throws UnsupportedOperationException
	 */
	private void processGetAfterResourceDetermined(
			final IRODSFile irodsFileToGet, final File localFileToHoldData,
			final DataObjInp dataObjInp, final TransferOptions transferOptions, long irodsFileLength)
			throws JargonException, DataNotFoundException {

		if (transferOptions == null) {
			throw new IllegalArgumentException("null transfer options");
		}

		LocalFileUtils.createLocalFileIfNotExists(localFileToHoldData);

		final Tag message = getIRODSProtocol().irodsFunction(dataObjInp);

		// irods file doesn't exist
		if (message == null) {
			log.warn(
					"irods file does not exist, null was returned from the get, return DataNotFoundException for iRODS file: {}",
					irodsFileToGet.getAbsolutePath());
			throw new DataNotFoundException(
					"irods file not found during get operation:"
							+ irodsFileToGet.getAbsolutePath());
		}

		// Need the total dataSize
		Tag temp = message.getTag(MsgHeader_PI);

		if (temp == null) {
			// length is zero
			log.info("create a new file, length is zero");
			return;
		}

		temp = temp.getTag(DataObjInp.BS_LEN);
		if (temp == null) {
			log.info("no size returned, return from get with no update done");
			return;
		}

		final long lengthFromIrodsResponse = temp.getIntValue();

		log.info("transfer length is:", lengthFromIrodsResponse);

		// if length == zero, check for multiple thread copy, may still process
		// as a standard txfr if 0 threads specified
		try {
			if (lengthFromIrodsResponse == 0) {
				checkNbrThreadsAndProcessAsParallelIfMoreThanZeroThreads(
						irodsFileToGet, localFileToHoldData, transferOptions, message, lengthFromIrodsResponse, irodsFileLength);

			} else {
				dataAOHelper.processNormalGetTransfer(localFileToHoldData,
						lengthFromIrodsResponse, this.getIRODSProtocol(), transferOptions);
			}
			
			if (transferOptions != null) {
				if (transferOptions.isComputeAndVerifyChecksumAfterTransfer()) {
					log.info("computing a checksum on the file at:{}", localFileToHoldData.getAbsolutePath());
					String localFileChecksum = LocalFileUtils.md5ByteArrayToString(LocalFileUtils.computeMD5FileCheckSumViaAbsolutePath(localFileToHoldData.getAbsolutePath()));
					log.info("local file checksum is:{}", localFileChecksum);
					String irodsChecksum = computeMD5ChecksumOnDataObject(irodsFileToGet);
					log.info("irods checksum:{}", irodsChecksum);
					if (!(irodsChecksum.equals(localFileChecksum))) {
						throw new FileIntegrityException("checksum verification after get fails");
					}
				}
			}
			
			
		} catch (Exception e) {
			log.error("error in parallel transfer", e);
			throw new JargonException("error in parallel transfer", e);
		}
	}

	/**
	 * @param localFileToHoldData
	 * @param transferOptions
	 * @param message
	 * @param length length returned from the initial DataObjInp call to iRODS
	 * @param irodsFileLength actual length of irodsFile
	 * @throws JargonException
	 */
	private void checkNbrThreadsAndProcessAsParallelIfMoreThanZeroThreads(final IRODSFile irodsSourceFile,
			final File localFileToHoldData,
			final TransferOptions transferOptions, final Tag message,
			final long length, long irodsFileLength) throws JargonException {
		final String host = message.getTag(PortList_PI).getTag(hostAddr)
				.getStringValue();
		int port = message.getTag(PortList_PI).getTag(portNum).getIntValue();
		int password = message.getTag(PortList_PI).getTag(cookie).getIntValue();
		int numberOfThreads = message.getTag(numThreads).getIntValue();
		
		log.info("number of threads for this transfer = {} ", numberOfThreads);

		if (numberOfThreads == 0) {
			log.info("number of threads is zero, possibly parallel transfers were turned off via rule, process as normal");
			int fd = message.getTag(l1descInx).getIntValue();
			dataAOHelper.processGetTransferViaRead(irodsSourceFile, localFileToHoldData, irodsFileLength,
					 transferOptions, fd);
		} else {

			log.info("process as a parallel transfer");
			ParallelGetFileTransferStrategy parallelGetTransferStrategy = ParallelGetFileTransferStrategy
					.instance(host, port, numberOfThreads, password,
							localFileToHoldData,
							this.getIRODSAccessObjectFactory());

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

		log.info("building a metadata query for: {}", avuQuery);

		final StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_D_DATA_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS.getName());
		query.append(WHERE);
		boolean previousElement = false;

		for (AVUQueryElement queryElement : avuQuery) {

			if (previousElement) {
				query.append(AND);
			}
			previousElement = true;
			query.append(dataAOHelper.buildConditionPart(queryElement));
		}

		if (previousElement) {
			query.append(AND);
		} else {
			query.append(SPACE);
		}

		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(EQUALS_AND_QUOTE);
		query.append(IRODSDataConversionUtil
				.escapeSingleQuotes(dataObjectCollectionAbsPath));
		query.append(QUOTE);
		query.append(AND);
		query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		query.append(EQUALS_AND_QUOTE);
		query.append(IRODSDataConversionUtil
				.escapeSingleQuotes(dataObjectFileName));
		query.append(QUOTE);

		final String queryString = query.toString();
		log.debug("query string for AVU query: {}", queryString);

		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);

		} catch (JargonQueryException e) {
			log.error("query exception for query:" + queryString, e);
			throw new JargonException("error in query for a data object");
		}

		return dataAOHelper
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
	 * org.irods.jargon.core.pub.DataObjectAO#addAVUMetadata(java.lang.String,
	 * org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void addAVUMetadata(final String absolutePath, final AvuData avuData)
			throws DataNotFoundException, DuplicateDataException,
			JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("adding avu metadata to data object: {}", avuData);
		log.info("absolute path: {}", absolutePath);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForAddDataObjectMetadata(absolutePath, avuData);

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
					"null or empty irodsCollectionAbsolutePath");
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("adding avu metadata to data object: {}", avuData);
		log.info("parent collection absolute path: {}",
				irodsCollectionAbsolutePath);
		log.info("file name: {}", fileName);

		StringBuilder sb = new StringBuilder(irodsCollectionAbsolutePath);
		sb.append("/");
		sb.append(fileName);

		addAVUMetadata(sb.toString(), avuData);

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
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null AVU data");
		}

		log.info("deleting avu metadata on dataObject: {}", avuData);
		log.info("absolute path: {}", absolutePath);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForDeleteDataObjectMetadata(absolutePath, avuData);

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

		if (avuQuery == null || avuQuery.isEmpty()) {
			throw new IllegalArgumentException("null or empty query");
		}

		log.info("building a metadata query for: {}", avuQuery);

		final StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_D_DATA_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS.getName());

		query.append(WHERE);
		boolean previousElement = false;
		@SuppressWarnings("unused")
		StringBuilder queryCondition;

		for (AVUQueryElement queryElement : avuQuery) {

			if (previousElement) {
				query.append(AND);
			}
			previousElement = true;
			query.append(dataAOHelper.buildConditionPart(queryElement));
		}

		final String queryString = query.toString();
		log.debug("query string for AVU query: {}", queryString);

		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryWithPaging(
					irodsQuery, partialStartIndex);
		} catch (JargonQueryException e) {
			log.error("query exception for query:" + queryString, e);
			throw new JargonException("error in data object AVU Query", e);
		}

		return dataAOHelper
				.buildMetaDataAndDomainDataListFromResultSet(resultSet);
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

		log.info("building a metadata query for: {}", avuQueryElements);

		final StringBuilder query = new StringBuilder();
		query.append(dataAOHelper.buildSelects());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS.getName());

		query.append(WHERE);
		boolean previousElement = false;

		for (AVUQueryElement queryElement : avuQueryElements) {

			if (previousElement) {
				query.append(AND);
			}
			previousElement = true;
			query.append(dataAOHelper.buildConditionPart(queryElement));
		}

		final String queryString = query.toString();
		log.debug("query string for AVU query: {}", queryString);

		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryWithPaging(
					irodsQuery, partialStartIndex);

		} catch (JargonQueryException e) {
			log.error("query exception for query:" + queryString, e);
			throw new JargonException(
					"error in query for data objects query by metadata");
		}

		return dataAOHelper.buildListFromResultSet(resultSet);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#copyIrodsDataObject(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
	@Override
	public void copyIrodsDataObject(final String irodsSourceFileAbsolutePath,
			final String irodsTargetFileAbsolutePath,
			final String targetResourceName) throws JargonException {

		if (irodsSourceFileAbsolutePath == null
				|| irodsSourceFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsSourceFileAbsolutePath");
		}

		if (irodsTargetFileAbsolutePath == null
				|| irodsTargetFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsTargetFileAbsolutePath");
		}

		if (targetResourceName == null) {
			throw new IllegalArgumentException("null  targetResourceName");
		}

		log.info("copyIrodsDataObject, irodsSourceFileAbsolutePath: {}",
				irodsSourceFileAbsolutePath);
		log.info("irodsTargetFileAbsolutePath:{}", irodsTargetFileAbsolutePath);
		log.info("at resource: {}", targetResourceName);

		// I need the length of the file
		IRODSFile sourceFile = getIRODSFileFactory().instanceIRODSFile(
				irodsSourceFileAbsolutePath);

		if (!sourceFile.exists()) {
			throw new JargonException(
					"the source file for the copy does not exist");
		}

		if (!sourceFile.isFile()) {
			throw new JargonException("the source file is not a data object");
		}

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp.instanceForCopy(
				irodsSourceFileAbsolutePath, irodsTargetFileAbsolutePath,
				targetResourceName, sourceFile.length(), false);

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
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#copyIrodsDataObjectWithForce(java
	 * .lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void copyIrodsDataObjectWithForce(
			final String irodsSourceFileAbsolutePath,
			final String irodsTargetFileAbsolutePath,
			final String targetResourceName) throws JargonException {

		if (irodsSourceFileAbsolutePath == null
				|| irodsSourceFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsSourceFileAbsolutePath");
		}

		if (irodsTargetFileAbsolutePath == null
				|| irodsTargetFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsTargetFileAbsolutePath");
		}

		if (targetResourceName == null) {
			throw new IllegalArgumentException(
					"null or empty targetResourceName");
		}

		log.info(
				"copyIrodsDataObjectWithForce, irodsSourceFileAbsolutePath: {}",
				irodsSourceFileAbsolutePath);
		log.info("irodsTargetFileAbsolutePath:{}", irodsTargetFileAbsolutePath);
		log.info("at resource: {}", targetResourceName);

		// I need the length of the file
		IRODSFile sourceFile = getIRODSFileFactory().instanceIRODSFile(
				irodsSourceFileAbsolutePath);

		if (!sourceFile.exists()) {
			throw new JargonException(
					"the source file for the copy does not exist");
		}

		if (!sourceFile.isFile()) {
			throw new JargonException("the source file is not a data object");
		}

		DataObjCopyInp dataObjCopyInp = DataObjCopyInp.instanceForCopy(
				irodsSourceFileAbsolutePath, irodsTargetFileAbsolutePath,
				targetResourceName, sourceFile.length(), true);

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

		log.info("getting resources for path:{}", dataObjectPath);

		ResourceAO resourceAO = this.getIRODSAccessObjectFactory()
				.getResourceAO(getIRODSAccount());
		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		sb.append(EQUALS_AND_QUOTE);
		sb.append(IRODSDataConversionUtil.escapeSingleQuotes(dataObjectPath));
		sb.append(QUOTE);
		sb.append(AND);
		sb.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		sb.append(EQUALS_AND_QUOTE);
		sb.append(IRODSDataConversionUtil.escapeSingleQuotes(dataObjectName));
		sb.append(QUOTE);

		return resourceAO.findWhere(sb.toString());

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

		// contract checks in delegated method

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		try {
			return this.findMetadataValuesForDataObjectUsingAVUQuery(
					queryElements, dataObjectCollectionAbsPath,
					dataObjectFileName);
		} catch (JargonQueryException e) {
			log.error("query exception looking up data object:{}",
					dataObjectCollectionAbsPath, e);
			log.error("fileName: {}", dataObjectFileName);
			throw new JargonException(e);
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
			final String dataObjectAbsolutePath) throws JargonException {

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty dataObjectAbsolutePath");
		}

		log.info("findMetadataValuesForDataObject: {}", dataObjectAbsolutePath);

		IRODSFile irodsFile = getIRODSFileFactory().instanceIRODSFile(
				dataObjectAbsolutePath);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		try {
			return this.findMetadataValuesForDataObjectUsingAVUQuery(
					queryElements, irodsFile.getParent(), irodsFile.getName());
		} catch (JargonQueryException e) {
			log.error("query exception looking up data object:{}",
					dataObjectAbsolutePath, e);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#updateComment(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void updateComment(final String comment,
			final String dataObjectAbsolutePath) throws JargonException {

		if (comment == null) {
			throw new IllegalArgumentException(
					"comment is null, set to blank to clear comment in iRODS");
		}

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"dataObjectAbsolutePath is null or empty");
		}

		throw new UnsupportedOperationException("not yet implemented");
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
		// pi tests parameters
		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermission(false, zone, absolutePath, userName,
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
		// pi tests parameters
		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermissionInAdminMode(false, zone, absolutePath,
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
		// pi tests parameters
		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermission(false, zone, absolutePath, userName,
						ModAccessControlInp.WRITE_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
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
		// pi tests parameters
		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermissionInAdminMode(false, zone, absolutePath,
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
		// pi tests parameters
		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermission(false, zone, absolutePath, userName,
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
		// pi tests parameters
		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermissionInAdminMode(false, zone, absolutePath,
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
		// pi tests parameters
		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermission(false, zone, absolutePath, userName,
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
		// pi tests parameters
		ModAccessControlInp modAccessControlInp = ModAccessControlInp
				.instanceForSetPermissionInAdminMode(false, zone, absolutePath,
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
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		log.info("getPermissionForDataObject for absPath:{}", absolutePath);
		log.info("userName:{}", userName);

		IRODSFileSystemAO irodsFileSystemAO = getIRODSAccessObjectFactory()
				.getIRODSFileSystemAO(getIRODSAccount());
		int permissionVal = irodsFileSystemAO
				.getFilePermissionsForGivenUser(getIRODSFileFactory()
						.instanceIRODSFile(absolutePath), userName);
		FilePermissionEnum filePermissionEnum = FilePermissionEnum
				.valueOf(permissionVal);
		return filePermissionEnum;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#listPermissionsForDataObject(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public List<UserFilePermission> listPermissionsForDataObject(
			final String irodsCollectionAbsolutePath, final String dataName)
			throws JargonException {

		if (irodsCollectionAbsolutePath == null
				|| irodsCollectionAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsCollectionAbsolutePath");
		}

		if (dataName == null || dataName.isEmpty()) {
			throw new IllegalArgumentException("null or empty dataName");
		}

		log.info("listPermissionsForDataObject path: {}",
				irodsCollectionAbsolutePath);
		log.info("dataName: {}", irodsCollectionAbsolutePath);

		List<UserFilePermission> userFilePermissions = new ArrayList<UserFilePermission>();

		StringBuilder query = new StringBuilder(
				DataAOHelper.buildACLQueryForCollectionPathAndDataName(
						irodsCollectionAbsolutePath, dataName));

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(),
				this.getJargonProperties().getMaxFilesAndDirsQueryMax());
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);

			UserFilePermission userFilePermission = null;
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				userFilePermission = new UserFilePermission(row.getColumn(0),
						row.getColumn(1),
						FilePermissionEnum.valueOf(IRODSDataConversionUtil
								.getIntOrZeroFromIRODSValue(row.getColumn(2))));
				log.debug("loaded filePermission:{}", userFilePermission);
				userFilePermissions.add(userFilePermission);
			}

		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}", query.toString(), e);
			throw new JargonException(
					"error in query loading user file permissions for data object",
					e);
		}

		return userFilePermissions;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAO#listPermissionsForDataObject(java
	 * .lang.String)
	 */
	@Override
	public List<UserFilePermission> listPermissionsForDataObject(
			final String irodsDataObjectAbsolutePath) throws JargonException {

		if (irodsDataObjectAbsolutePath == null
				|| irodsDataObjectAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsDataObjectAbsolutePath");
		}

		log.info("listPermissionsForDataObject: {}",
				irodsDataObjectAbsolutePath);
		IRODSFile irodsFile = getIRODSFileFactory().instanceIRODSFile(
				irodsDataObjectAbsolutePath);

		return listPermissionsForDataObject(irodsFile.getParent(),
				irodsFile.getName());

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
			throw new IllegalArgumentException("null or empty absolutePath");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null avuData");
		}

		log.info("setting avu metadata value for dataObject");
		log.info("with  avu metadata:{}", avuData);
		log.info("absolute path: {}", absolutePath);

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

		log.info("overwrite avu metadata for data object: {}", currentAvuData);
		log.info("with new avu metadata:{}", newAvuData);
		log.info("absolute path: {}", dataObjectAbsolutePath);

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
					"null or empty irodsCollectionAbsolutePath");
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
					"null or empty irodsCollectionAbsolutePath");
		}

		if (dataName == null || dataName.isEmpty()) {
			throw new IllegalArgumentException("null or empty dataName");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("listPermissionsForDataObjectForUserName path: {}",
				irodsCollectionAbsolutePath);
		log.info("dataName: {}", irodsCollectionAbsolutePath);
		log.info("userName:{}", userName);

		UserFilePermission userFilePermission = null;

		StringBuilder query = new StringBuilder(
				DataAOHelper.buildACLQueryForCollectionPathAndDataName(
						irodsCollectionAbsolutePath, dataName));
		query.append(" AND ");
		query.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		query.append(" = '");
		query.append(userName);
		query.append("'");

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query.toString(),
				this.getJargonProperties().getMaxFilesAndDirsQueryMax());
		IRODSQueryResultSetInterface resultSet;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
			IRODSQueryResultRow row = resultSet.getFirstResult();

			userFilePermission = new UserFilePermission(row.getColumn(0),
					row.getColumn(1),
					FilePermissionEnum.valueOf(IRODSDataConversionUtil
							.getIntOrZeroFromIRODSValue(row.getColumn(2))));
			log.debug("loaded filePermission:{}", userFilePermission);

		} catch (JargonQueryException e) {
			log.error("query exception for  query:{}", query.toString(), e);
			throw new JargonException(
					"error in query loading user file permissions for data object",
					e);
		} catch (DataNotFoundException dnf) {
			log.info("no data found for user ACL");
		}

		return userFilePermission;

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

		log.info("listPermissionsForDataObjectForUserName path: {}",
				irodsAbsolutePath);
		log.info("userName:{}", userName);

		IRODSFile irodsFile = this.getIRODSFileFactory().instanceIRODSFile(
				irodsAbsolutePath);

		return getPermissionForDataObjectForUserName(irodsFile.getParent(),
				irodsFile.getName(), userName);

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

}
