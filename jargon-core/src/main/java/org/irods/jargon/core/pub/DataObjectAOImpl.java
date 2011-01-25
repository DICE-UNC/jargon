package org.irods.jargon.core.pub;

import static edu.sdsc.grid.io.irods.IRODSConstants.MsgHeader_PI;
import static edu.sdsc.grid.io.irods.IRODSConstants.PortList_PI;
import static edu.sdsc.grid.io.irods.IRODSConstants.RODS_API_REQ;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.ModAccessControlInp;
import org.irods.jargon.core.packinstr.ModAvuMetadataInp;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.packinstr.TransferOptions.TransferType;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.aohelper.DataAOHelper;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.transfer.KeepAliveProcess;
import org.irods.jargon.core.transfer.ParallelGetFileTransferStrategy;
import org.irods.jargon.core.transfer.ParallelPutFileTransferStrategy;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
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
public final class DataObjectAOImpl extends IRODSGenericAO implements
		DataObjectAO {

	public static final Logger log = LoggerFactory
			.getLogger(DataObjectAOImpl.class);
	private transient final DataAOHelper dataAOHelper = new DataAOHelper();
	private transient final IRODSGenQueryExecutor irodsGenQueryExecutor = new IRODSGenQueryExecutorImpl(
			getIRODSSession(), getIRODSAccount());
	private transient final IRODSFileFactory irodsFileFactory = new IRODSFileFactoryImpl(
			getIRODSSession(), getIRODSAccount());

	public static final int DEFAULT_REC_COUNT = 1000;

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
			throws JargonException {

		DataObject dataObject = null;

		if (collectionPath == null || collectionPath.isEmpty()) {
			throw new JargonException("collection path is null or empty");
		}

		if (dataName == null || dataName.isEmpty()) {
			throw new JargonException("dataName is null or empty");
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
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);

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
			throws JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new JargonException("null or empty absolutePath");
		}

		log.info("findByAbsolutePath() with path:{}", absolutePath);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(absolutePath);
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
			throw new JargonException(
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
				DEFAULT_REC_COUNT);

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

		TransferOptions transferOptions = buildTransferOptionsBasedOnJargonProperties();
		
		log.info("testing file length to set parallel transfer options");
		if (localFile.length() > getIRODSSession().getJargonProperties().getParallelThreadsLengthThreshold()) {
			transferOptions.setMaxThreads(getIRODSSession().getJargonProperties().getMaxParallelThreads());
			log.info("length above threshold, send max threads cap");
		} else {
			transferOptions.setMaxThreads(0);
		}

		putLocalDataObjectToIRODS(localFile, irodsFileDestination, overwrite,
				false, transferOptions);

	}

	private TransferOptions buildTransferOptionsBasedOnJargonProperties()
			throws JargonException {
		JargonProperties jargonProperties = getIRODSSession()
				.getJargonProperties();
		TransferOptions transferOptions = new TransferOptions();
		transferOptions.setMaxThreads(jargonProperties.getMaxParallelThreads());

		if (transferOptions.getMaxThreads() > 20) { // FIXME: test code for
													// debugging strange error
			throw new JargonRuntimeException("corrupted max threads value");
		}

		if (jargonProperties.isUseParallelTransfer()) {
			transferOptions.setTransferType(TransferType.STANDARD);
		} else {
			transferOptions.setTransferType(TransferType.NO_PARALLEL);
		}
		return transferOptions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.pub.DataObjectAO#
	 * putLocalDataObjectToIRODSForClientSideRuleOperation(java.io.File,
	 * org.irods.jargon.core.pub.io.IRODSFile, boolean)
	 */
	@Override
	public void putLocalDataObjectToIRODSForClientSideRuleOperation(
			final File localFile, final IRODSFile irodsFileDestination,
			final boolean overwrite) throws JargonException {

		TransferOptions transferOptions = buildTransferOptionsBasedOnJargonProperties();

		putLocalDataObjectToIRODS(localFile, irodsFileDestination, overwrite,
				true, transferOptions);

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
			throw new JargonException("null local file");
		}

		if (irodsFileDestination == null) {
			throw new JargonException("null destination file");
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

		IRODSFile targetFile = checkTargetFileForPutOperation(localFile,
				irodsFileDestination, ignoreChecks);

		DataObjInp dataObjInp = DataObjInp.instanceForInitialCallToPut(
				targetFile.getAbsolutePath(), localFile.length(),
				targetFile.getResource(), overwrite, transferOptions);

		try {
			Tag responseToInitialCallForPut = getIRODSProtocol().irodsFunction(
					dataObjInp);

			int numberOfThreads = responseToInitialCallForPut
					.getTag(numThreads).getIntValue();

			if (numberOfThreads > 0) {

				log.info("tranfer will be done using {} threads",
						numberOfThreads);
				final String host = responseToInitialCallForPut
						.getTag(PortList_PI).getTag(hostAddr).getStringValue();
				final int port = responseToInitialCallForPut
						.getTag(PortList_PI).getTag(portNum).getIntValue();
				final int pass = responseToInitialCallForPut
						.getTag(PortList_PI).getTag(cookie).getIntValue();

				final ParallelPutFileTransferStrategy parallelPutFileStrategy = ParallelPutFileTransferStrategy
						.instance(host, port, numberOfThreads, pass, localFile);

				log.info(
						"getting ready to initiate parallel file transfer strategy:{}",
						parallelPutFileStrategy);

				// start a keep-alive that will ping the irods server every 30
				// seconds to keep the control channel alive
				KeepAliveProcess keepAliveProcess = new KeepAliveProcess(
						new EnvironmentalInfoAOImpl(this.getIRODSSession(),
								this.getIRODSAccount()));
				Thread keepAliveThread = new Thread(keepAliveProcess);
				keepAliveThread.start();

				try {
					parallelPutFileStrategy.transfer();
					keepAliveProcess.setTerminate(true);
					keepAliveThread.join(2000);
				} catch (Exception e) {
					log.error("error in parallel transfer", e);
					throw new JargonException("error in parallel transfer", e);
				}

				log.info("transfer is complete");
				int statusForComplete = responseToInitialCallForPut.getTag(
						l1descInx).getIntValue();
				log.debug("status for complete:{}", statusForComplete);

				this.getIRODSProtocol().operationComplete(statusForComplete);

			} else {
				log.info("processing as a normal put strategy");

				dataObjInp = DataObjInp.instanceForNormalPutStrategy(
						targetFile.getAbsolutePath(), localFile.length(),
						targetFile.getResource(), overwrite, transferOptions);

				Tag response = getIRODSProtocol().irodsFunction(RODS_API_REQ,
						dataObjInp.getParsedTags(), 0, null,
						localFile.length(), new FileInputStream(localFile),
						dataObjInp.getApiNumber());

				if (response == null) {
					log.warn("send of put returned null, currently is ignored and null is returned from put operation");
					return;
				}
			}

		} catch (DataNotFoundException dnf) {
			log.warn("send of put returned no data found from irods, currently is ignored and null is returned from put operation");
			return;
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
		}

		log.info("transfer complete");

	}

	/**
	 * Check if the target of a put is an iRODS collection or data object name.
	 * This method is smart enough to know that if you put a data object to an
	 * iRODS collection, it can carry over the fileName in the specified iRODS
	 * collection.
	 * 
	 * @param localFile
	 * @param irodsFileDestination
	 * @param ignoreChecks
	 * @return
	 * @throws JargonException
	 */
	private IRODSFile checkTargetFileForPutOperation(final File localFile,
			final IRODSFile irodsFileDestination, final boolean ignoreChecks)
			throws JargonException {
		IRODSFile targetFile;

		if (ignoreChecks) {
			log.debug("ignoring iRODS checks, assume this is a data object");
			targetFile = irodsFileDestination;
		} else {

			log.debug(">>>>>checking if destination file is a collection");
			if (irodsFileDestination.isDirectory()) {
				log.info(
						"put specifying an irods collection, will use the local file name as the iRODS file name:{}",
						localFile.getName());
				targetFile = irodsFileFactory.instanceIRODSFile(
						irodsFileDestination.getAbsolutePath(),
						localFile.getName());
				targetFile.setResource(irodsFileDestination.getResource());
			} else {
				targetFile = irodsFileDestination;
			}
		}
		return targetFile;
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

		if (localFileToHoldData == null) {
			throw new JargonException("null local file");
		}

		if (irodsFileToGet == null) {
			throw new JargonException("nulll destination file");
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

		TransferOptions transferOptions = buildTransferOptionsBasedOnJargonProperties();
		
		log.info("testing file length to set parallel transfer options");
		if (irodsFileToGet.length() > getIRODSSession().getJargonProperties().getParallelThreadsLengthThreshold()) {
			transferOptions.setMaxThreads(getIRODSSession().getJargonProperties().getMaxParallelThreads());
			log.info("length above threshold, send max threads cap");
		} else {
			transferOptions.setMaxThreads(0);
		}
		
		log.info("target local file: {}", localFile.getAbsolutePath());
		log.info("from source file: {}", irodsFileToGet.getAbsolutePath());

		final DataObjInp dataObjInp = DataObjInp.instanceForGet(
				irodsFileToGet.getAbsolutePath(), transferOptions);

		processGetAfterResourceDetermined(irodsFileToGet, localFile, dataObjInp);
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

		if (localFileToHoldData == null) {
			throw new JargonException("null local file");
		}

		if (irodsFileToGet == null) {
			throw new JargonException("nulll destination file");
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

		TransferOptions transferOptions = buildTransferOptionsBasedOnJargonProperties();
		
		log.info("testing file length to set parallel transfer options");
		if (irodsFileToGet.length() > getIRODSSession().getJargonProperties().getParallelThreadsLengthThreshold()) {
			transferOptions.setMaxThreads(getIRODSSession().getJargonProperties().getMaxParallelThreads());
			log.info("length above threshold, send max threads cap");
		} else {
			transferOptions.setMaxThreads(0);
		}

		final DataObjInp dataObjInp = DataObjInp
				.instanceForGetSpecifyingResource(
						irodsFileToGet.getAbsolutePath(),
						irodsFileToGet.getResource(), transferOptions);

		processGetAfterResourceDetermined(irodsFileToGet, localFile, dataObjInp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.pub.DataObjectAO#
	 * irodsDataObjectGetOperationForClientSideAction
	 * (org.irods.jargon.core.pub.io.IRODSFile, java.io.File)
	 */
	@Override
	public void irodsDataObjectGetOperationForClientSideAction(
			final IRODSFile irodsFileToGet, final File localFileToHoldData)
			throws DataNotFoundException, JargonException {

		if (localFileToHoldData == null) {
			throw new JargonException("null local file");
		}

		if (irodsFileToGet == null) {
			throw new JargonException("nulll destination file");
		}

		log.info("target local file: {}", localFileToHoldData.getAbsolutePath());
		log.info("from source file: {}", irodsFileToGet.getAbsolutePath());
		TransferOptions transferOptions = buildTransferOptionsBasedOnJargonProperties();

		final DataObjInp dataObjInp = DataObjInp
				.instanceForGetSpecifyingResource(
						irodsFileToGet.getAbsolutePath(), "", transferOptions);

		processGetAfterResourceDetermined(irodsFileToGet, localFileToHoldData,
				dataObjInp);
	}

	/**
	 * @param irodsFileToGet
	 * @param localFileToHoldData
	 * @param dataObjInp
	 * @throws JargonException
	 * @throws DataNotFoundException
	 * @throws UnsupportedOperationException
	 */
	private void processGetAfterResourceDetermined(
			final IRODSFile irodsFileToGet, final File localFileToHoldData,
			final DataObjInp dataObjInp) throws JargonException,
			DataNotFoundException, UnsupportedOperationException {
		createLocalFileIfNotExists(localFileToHoldData);

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
		final long length = temp.getIntValue();

		log.info("transfer length is:", length);

		// if length == zero, check for multiple thread copy
		if (length == 0) {
			final String host = message.getTag(PortList_PI).getTag(hostAddr)
					.getStringValue();
			int port = message.getTag(PortList_PI).getTag(portNum)
					.getIntValue();
			int password = message.getTag(PortList_PI).getTag(cookie)
					.getIntValue();
			int numberOfThreads = message.getTag(numThreads).getIntValue();
			log.info("number of threads for this transfer = {} ",
					numberOfThreads);
			ParallelGetFileTransferStrategy parallelGetTransferStrategy = ParallelGetFileTransferStrategy
					.instance(host, port, numberOfThreads, password,
							localFileToHoldData);

			// start a keep-alive that will ping the irods server every 30
			// seconds to keep the control channel alive
			KeepAliveProcess keepAliveProcess = new KeepAliveProcess(
					new EnvironmentalInfoAOImpl(this.getIRODSSession(),
							this.getIRODSAccount()));
			Thread keepAliveThread = new Thread(keepAliveProcess);
			keepAliveThread.start();

			try {
				parallelGetTransferStrategy.transfer();
				keepAliveProcess.setTerminate(true);
				keepAliveThread.join(2000);
			} catch (Exception e) {
				log.error("error in parallel transfer", e);
				throw new JargonException("error in parallel transfer", e);
			}

		} else {
			processNormalGetTransfer(localFileToHoldData, length);
		}
	}

	/**
	 * @param localFileToHoldData
	 * @param length
	 * @throws JargonException
	 */
	private void processNormalGetTransfer(final File localFileToHoldData,
			final long length) throws JargonException {

		log.info("normal file transfer started, get output stream for local destination file");
		// get an input stream from the irodsFile
		BufferedOutputStream localFileOutputStream;
		try {
			localFileOutputStream = new BufferedOutputStream(
					new FileOutputStream(localFileToHoldData));
		} catch (FileNotFoundException e) {
			log.error(
					"FileNotFoundException when trying to create a new file for the local output stream for {}",
					localFileToHoldData.getAbsolutePath(), e);
			throw new JargonException(
					"FileNotFoundException for local file when trying to get to: "
							+ localFileToHoldData.getAbsolutePath(), e);
		}

		// read the message byte stream into the local file
		getIRODSProtocol().read(localFileOutputStream, length);
		log.info("transfer is complete");
		try {
			localFileOutputStream.flush();
			localFileOutputStream.close();
		} catch (IOException e) {
			log.error(
					"IOException when trying to create a new file for the local output stream for {}",
					localFileToHoldData.getAbsolutePath(), e);
			throw new JargonException(
					"IOException for local file when trying to get to: "
							+ localFileToHoldData.getAbsolutePath(), e);
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
			throw new JargonException("null query");
		}

		if (dataObjectCollectionAbsPath == null
				|| dataObjectCollectionAbsPath.isEmpty()) {
			throw new JargonException(
					"null or empty absolutePath for dataObjectCollectionAbsPath");
		}

		if (dataObjectFileName == null || dataObjectFileName.isEmpty()) {
			throw new JargonException("null or empty dataObjectFileName");
		}

		final IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(
				this.getIRODSSession(), this.getIRODSAccount());

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
				DEFAULT_REC_COUNT);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutorImpl.executeIRODSQueryAndCloseResult(irodsQuery,
					0);

		} catch (JargonQueryException e) {
			log.error("query exception for query:" + queryString, e);
			throw new JargonException("error in query for a data object");
		}

		return dataAOHelper
				.buildMetaDataAndDomainDataListFromResultSet(resultSet);
	}

	/**
	 * @param localFileToHoldData
	 * @throws JargonException
	 */
	private void createLocalFileIfNotExists(final File localFileToHoldData)
			throws JargonException {
		if (localFileToHoldData.exists()) {
			log.info(
					"local file exists, will not create the local file for {}",
					localFileToHoldData.getAbsolutePath());
		} else {
			log.info(
					"local file does not exist, will attempt to create local file: {}",
					localFileToHoldData.getAbsolutePath());
			try {
				localFileToHoldData.createNewFile();
			} catch (IOException e) {
				log.error(
						"IOException when trying to create a new file for the local output stream for {}",
						localFileToHoldData.getAbsolutePath(), e);
				throw new JargonException(
						"IOException trying to create new file: "
								+ localFileToHoldData.getAbsolutePath(), e);
			}
		}
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
		final IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(fileAbsolutePath);

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
			throws DataNotFoundException, JargonException {
		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new JargonException("null or empty absolutePath");
		}

		if (avuData == null) {
			throw new JargonException("null AVU data");
		}

		log.info("adding avu metadata to data object: {}", avuData);
		log.info("absolute path: {}", absolutePath);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForAddDataObjectMetadata(absolutePath, avuData);

		log.debug("sending avu request");

		try {

			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);

		} catch (JargonException je) {

			if (je.getMessage().indexOf("-814000") > -1) {
				throw new DataNotFoundException(
						"Target collection was not found, could not add AVU");
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
	 * org.irods.jargon.core.pub.DataObjectAO#deleteAVUMetadata(java.lang.String
	 * , org.irods.jargon.core.pub.domain.AvuData)
	 */
	@Override
	public void deleteAVUMetadata(final String absolutePath,
			final AvuData avuData) throws DataNotFoundException,
			JargonException {

		if (absolutePath == null || absolutePath.isEmpty()) {
			throw new JargonException("null or empty absolutePath");
		}

		if (avuData == null) {
			throw new JargonException("null AVU data");
		}

		log.info("deleting avu metadata on dataObject: {}", avuData);
		log.info("absolute path: {}", absolutePath);

		final ModAvuMetadataInp modifyAvuMetadataInp = ModAvuMetadataInp
				.instanceForDeleteDataObjectMetadata(absolutePath, avuData);

		log.debug("sending avu request");

		try {
			getIRODSProtocol().irodsFunction(modifyAvuMetadataInp);
		} catch (JargonException je) {

			if (je.getMessage().indexOf("-814000") > -1) {
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
			throw new JargonException("null or empty query");
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
				DEFAULT_REC_COUNT);

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
		StringBuilder queryCondition = null;

		for (AVUQueryElement queryElement : avuQueryElements) {

			queryCondition = new StringBuilder();
			if (previousElement) {
				queryCondition.append(AND);
			}
			previousElement = true;
			query.append(dataAOHelper.buildConditionPart(queryElement));
		}

		final String queryString = query.toString();
		log.debug("query string for AVU query: {}", queryString);

		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString,
				DEFAULT_REC_COUNT);

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
			throw new JargonException("null or empty irodsFileAbsolutePath");
		}

		if (targetResource == null || targetResource.isEmpty()) {
			throw new JargonException("null or empty targetResource");
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
	 * @seeorg.irods.jargon.core.pub.DataObjectAO#
	 * replicateIrodsDataObjectToAllResourcesInResourceGroup(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void replicateIrodsDataObjectToAllResourcesInResourceGroup(
			final String irodsFileAbsolutePath,
			final String irodsResourceGroupName) throws JargonException {
		if (irodsFileAbsolutePath == null || irodsFileAbsolutePath.isEmpty()) {
			throw new JargonException("null or empty irodsFileAbsolutePath");
		}

		if (irodsResourceGroupName == null || irodsResourceGroupName.isEmpty()) {
			throw new JargonException("null or empty targetResource");
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

		ResourceAO resourceAO = new ResourceAOImpl(this.getIRODSSession(),
				this.getIRODSAccount());
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
			final String dataObjectFileName) throws JargonQueryException,
			JargonException {
		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		return this.findMetadataValuesForDataObjectUsingAVUQuery(queryElements,
				dataObjectCollectionAbsPath, dataObjectFileName);
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
			throw new JargonException("irodsFile is null");
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

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.pub.DataObjectAO#updateComment(java.lang.String, java.lang.String)
	 */
	@Override
	public void updateComment(final String comment,
			final String dataObjectAbsolutePath) throws JargonException {

		if (comment == null) {
			throw new JargonException(
					"comment is null, set to blank to clear comment in iRODS");
		}

		if (dataObjectAbsolutePath == null || dataObjectAbsolutePath.isEmpty()) {
			throw new JargonException("dataObjectAbsolutePath is null or empty");
		}

		throw new UnsupportedOperationException("not yet implemented");
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.core.pub.DataObjectAO#setAccessPermissionRead(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setAccessPermissionRead(final String zone, final String absolutePath, final String userName) throws JargonException {
		// pi tests parameters
		ModAccessControlInp modAccessControlInp = ModAccessControlInp.instanceForSetPermission(false, zone, absolutePath, userName, ModAccessControlInp.READ_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.core.pub.DataObjectAO#setAccessPermissionWrite(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setAccessPermissionWrite(final String zone, final String absolutePath, final String userName) throws JargonException {
		// pi tests parameters
		ModAccessControlInp modAccessControlInp = ModAccessControlInp.instanceForSetPermission(false, zone, absolutePath, userName, ModAccessControlInp.WRITE_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.core.pub.DataObjectAO#setAccessPermissionOwn(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setAccessPermissionOwn(final String zone, final String absolutePath, final String userName) throws JargonException {
		// pi tests parameters
		ModAccessControlInp modAccessControlInp = ModAccessControlInp.instanceForSetPermission(false, zone, absolutePath, userName, ModAccessControlInp.OWN_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.core.pub.DataObjectAO#removeAccessPermissionsForUser(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void removeAccessPermissionsForUser(final String zone, final String absolutePath, final String userName) throws JargonException {
		// pi tests parameters
		ModAccessControlInp modAccessControlInp = ModAccessControlInp.instanceForSetPermission(false, zone, absolutePath, userName, ModAccessControlInp.NULL_PERMISSION);
		getIRODSProtocol().irodsFunction(modAccessControlInp);
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.core.pub.DataObjectAO#getPermissionForDataObject(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public FilePermissionEnum getPermissionForDataObject(final String absolutePath, final String userName, final String zone) throws JargonException {
		
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
		
		IRODSFileSystemAO irodsFileSystemAO = new IRODSFileSystemAOImpl(this.getIRODSSession(), this.getIRODSAccount());
		IRODSFileFactory irodsFileFactory = new IRODSFileFactoryImpl(this.getIRODSSession(), this.getIRODSAccount());
		int permissionVal = irodsFileSystemAO.getFilePermissions(irodsFileFactory.instanceIRODSFile(absolutePath));
		FilePermissionEnum filePermissionEnum = FilePermissionEnum.valueOf(permissionVal);
		return filePermissionEnum;
		
	}

}
