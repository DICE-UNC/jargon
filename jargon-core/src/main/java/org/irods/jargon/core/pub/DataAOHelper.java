package org.irods.jargon.core.pub;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.checksum.AbstractChecksumComputeStrategy;
import org.irods.jargon.core.checksum.ChecksumManager;
import org.irods.jargon.core.checksum.ChecksumManagerImpl;
import org.irods.jargon.core.checksum.ChecksumValue;
import org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol;
import org.irods.jargon.core.connection.ConnectionProgressStatusListener;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.OpenedDataObjInp;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.protovalues.ChecksumEncodingEnum;
import org.irods.jargon.core.pub.aohelper.AOHelper;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.ByteCountingCallbackInputStreamWrapper;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This helper class encapsulates lower-level helper code for the
 * {@link org.irods.jargon.core.pub.DataObjectAO} This class is meant to be used
 * internally, but does expose some useful methods when building new services
 * that are manipulating iRODS Data Objects.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class DataAOHelper extends AOHelper {
	public static final Logger log = LoggerFactory
			.getLogger(DataAOHelper.class);

	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	private final IRODSAccount irodsAccount;
	private final ChecksumManager checksumManager;

	private int putBufferSize = 0;

	DataAOHelper(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) throws JargonException {
		super();
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;

		this.irodsAccessObjectFactory.getJargonProperties()
				.getSendInputStreamBufferSize();
		putBufferSize = this.irodsAccessObjectFactory.getJargonProperties()
				.getPutBufferSize();
		checksumManager = new ChecksumManagerImpl(irodsAccount,
				irodsAccessObjectFactory);

	}

	/**
	 * Create a set of selects for a data object, used in general query.
	 *
	 * @param builder
	 *            {@link IRODSGenQueryBuilder} that will be appended with the
	 *            selects
	 *
	 * @return <code>String</code> with select statements for the domain object.
	 */
	void buildSelects(final IRODSGenQueryBuilder builder)
			throws JargonException {

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_DATA_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_COLL_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_DATA_REPL_NUM)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_VERSION)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_DATA_TYPE_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_RESC_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_DATA_PATH)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_OWNER_NAME)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_OWNER_ZONE)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_D_REPL_STATUS)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_D_DATA_STATUS)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_D_DATA_CHECKSUM)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_EXPIRY)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_MAP_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_COMMENTS)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_D_CREATE_TIME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_D_MODIFY_TIME);
		} catch (GenQueryBuilderException e) {
			throw new JargonException(e);
		}

	}

	/**
	 * Return a <code>DataObject</code> domain object given a result row from a
	 * query
	 *
	 * @param row
	 *            {@link org.irods.jargon.core.query.IRODSQueryResultRow}
	 *            containing the result of a query
	 * @return {@link org.irods.jargon.pub.domain.DataObject} that represents
	 *         the data in the row.
	 * @throws JargonException
	 */
	public static DataObject buildDomainFromResultSetRow(
			final IRODSQueryResultRow row) throws JargonException {
		DataObject dataObject = new DataObject();
		dataObject.setId(Integer.parseInt(row.getColumn(0)));
		dataObject.setCollectionId(Integer.parseInt(row.getColumn(1)));
		dataObject.setCollectionName(row.getColumn(2));
		dataObject.setDataName(row.getColumn(3));
		dataObject.setDataReplicationNumber(Integer.parseInt(row.getColumn(4)));
		dataObject.setDataVersion(IRODSDataConversionUtil
				.getIntOrZeroFromIRODSValue(row.getColumn(5)));
		dataObject.setDataTypeName(row.getColumn(6));
		dataObject.setDataSize(Long.parseLong(row.getColumn(7)));
		dataObject.setResourceName(row.getColumn(8));
		dataObject.setDataPath(row.getColumn(9));
		dataObject.setDataOwnerName(row.getColumn(10));
		dataObject.setDataOwnerZone(row.getColumn(11));
		dataObject.setReplicationStatus(row.getColumn(12));
		dataObject.setDataStatus(row.getColumn(13));
		dataObject.setChecksum(row.getColumn(14));
		dataObject.setExpiry(row.getColumn(15));
		dataObject.setDataMapId(Integer.parseInt(row.getColumn(16)));
		dataObject.setComments(row.getColumn(17));
		dataObject.setCreatedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(18)));
		dataObject.setUpdatedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(19)));

		// add info to track position in records for possible requery
		dataObject.setLastResult(row.isLastResult());
		dataObject.setCount(row.getRecordCount());

		if (log.isInfoEnabled()) {
			log.info("data object built \n");
			log.info(dataObject.toString());
		}

		return dataObject;
	}

	public static List<DataObject> buildListFromResultSet(
			final IRODSQueryResultSetInterface resultSet)
			throws JargonException {

		final List<DataObject> data = new ArrayList<DataObject>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			data.add(buildDomainFromResultSetRow(row));
		}

		return data;
	}

	/**
	 * @param metaDataDomain
	 * @param irodsQueryResultSet
	 * @return
	 * @throws JargonException
	 */
	static List<MetaDataAndDomainData> buildMetaDataAndDomainDataListFromResultSet(
			final IRODSQueryResultSetInterface irodsQueryResultSet)
			throws JargonException {

		if (irodsQueryResultSet == null) {
			throw new JargonException("null irodsQueryResultSet");
		}

		List<MetaDataAndDomainData> metaDataResults = new ArrayList<MetaDataAndDomainData>();

		for (IRODSQueryResultRow row : irodsQueryResultSet.getResults()) {
			metaDataResults
					.add(buildMetaDataAndDomainDataFromResultSetRowForDataObject(
							row, irodsQueryResultSet.getTotalRecords()));
		}

		return metaDataResults;
	}

	/**
	 * @param metadataDomain
	 * @param row
	 * @param totalRecordCount
	 * @return
	 * @throws JargonException
	 */
	static MetaDataAndDomainData buildMetaDataAndDomainDataFromResultSetRowForDataObject(
			final IRODSQueryResultRow row, final int totalRecordCount)
			throws JargonException {

		String domainId = row.getColumn(0);
		StringBuilder sb = new StringBuilder();
		sb.append(row.getColumn(1));
		sb.append('/');
		sb.append(row.getColumn(2));
		String domainUniqueName = sb.toString();
		// long dataSize = row.getColumnAsLongOrZero(3);
		// Date createdAt = row.getColumnAsDateOrNull(4);
		// Date modifiedAt = row.getColumnAsDateOrNull(5);

		int attributeId = Integer.parseInt(row.getColumn(3));
		String attributeName = row.getColumn(4);
		String attributeValue = row.getColumn(5);
		String attributeUnits = row.getColumn(6);

		MetaDataAndDomainData data = MetaDataAndDomainData.instance(
				MetadataDomain.DATA, domainId, domainUniqueName, 0, null, null,
				attributeId, attributeName, attributeValue, attributeUnits);

		data.setCount(row.getRecordCount());
		data.setLastResult(row.isLastResult());
		data.setTotalRecords(totalRecordCount);

		log.debug("metadataAndDomainData: {}", data);
		return data;
	}

	/**
	 * Overwrites have already been checked
	 *
	 * @param localFileToHoldData
	 * @param length
	 * @param transferOptions
	 * @param transferStatusCallbackListener
	 * @param transferControlBlock
	 * @throws JargonException
	 */
	void processNormalGetTransfer(final File localFileToHoldData,
			final long length,
			final AbstractIRODSMidLevelProtocol irodsProtocol,
			final TransferOptions transferOptions,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws JargonException {

		log.info("normal file transfer started, get output stream for local destination file");

		if (irodsProtocol == null) {
			throw new IllegalArgumentException("null irodsProtocol");
		}

		if (transferOptions == null) {
			throw new IllegalArgumentException("null transferOptions");
		}

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		// get an input stream from the irodsFile
		BufferedOutputStream localFileOutputStream;

		try {

			if (irodsProtocol.getPipelineConfiguration()
					.getLocalFileOutputStreamBufferSize() <= 0) {

				localFileOutputStream = new BufferedOutputStream(
						new FileOutputStream(localFileToHoldData));
			} else {
				localFileOutputStream = new BufferedOutputStream(
						new FileOutputStream(localFileToHoldData),
						irodsProtocol.getPipelineConfiguration()
								.getLocalFileOutputStreamBufferSize());
			}
		} catch (FileNotFoundException e) {
			log.error(
					"FileNotFoundException when trying to create a new file for the local output stream for {}",
					localFileToHoldData.getAbsolutePath(), e);
			throw new JargonException(
					"FileNotFoundException for local file when trying to get to: "
							+ localFileToHoldData.getAbsolutePath(), e);
		}

		ConnectionProgressStatusListener intraFileStatusListener = null;

		/*
		 * If specified by options, and with a call-back listener registered,
		 * create an object to aggregate and channel within-file progress
		 * reports to the caller.
		 */
		if (transferStatusCallbackListener != null
				&& transferControlBlock.getTransferOptions()
						.isIntraFileStatusCallbacks()) {
			intraFileStatusListener = DefaultIntraFileProgressCallbackListener
					.instance(TransferType.GET, length, transferControlBlock,
							transferStatusCallbackListener);
		}

		// read the message byte stream into the local file
		irodsProtocol.read(localFileOutputStream, length,
				intraFileStatusListener);
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

	/**
	 * Process a put transfer (uplaod a file to iRODS from the local file
	 * system). This method is meant to be used within the API, and as such, is
	 * not useful to api users. To do normal data transfers, please consult the
	 * {@link DataTransferOperations} access object.
	 *
	 * @param localFile
	 *            <code>File</code> which points to the local data object or
	 *            collection to uplaod
	 * @param overwrite
	 *            <code>boolean</code> that indicates whether the data can be
	 *            overwritten // FIXME: migrate to the transfer control block
	 * @param targetFile
	 *            {@link IRODSFile} that will be the target of the put
	 * @param irodsProtocol
	 *            {@link IRODSProtocol} that represents the active connection to
	 *            iRODS
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} that contains information that
	 *            controls the transfer operation, this is required
	 * @param transferStatusCallbackListener
	 *            {@link StatusCallbackListener} implementation to receive
	 *            status callbacks, this can be set to <code>null</code> if
	 *            desired
	 * @throws JargonException
	 * @throws FileNotFoundException
	 */
	void processNormalPutTransfer(final File localFile,
			final boolean overwrite, final IRODSFile targetFile,
			final AbstractIRODSMidLevelProtocol irodsProtocol,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws JargonException, FileNotFoundException {

		if (localFile == null) {
			throw new IllegalArgumentException("null localFile");
		}

		if (targetFile == null) {
			throw new IllegalArgumentException("null targetFile");
		}

		if (irodsProtocol == null) {
			throw new IllegalArgumentException("null irodsProtocol");
		}

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		log.info("processNormalPutTransfer");

		// make a defensive copy
		TransferOptions myTransferOptions = new TransferOptions(
				transferControlBlock.getTransferOptions());
		myTransferOptions.setMaxThreads(0);

		boolean execFlag = false;
		if (localFile.canExecute()) {
			log.info("file is executable");
			execFlag = true;
		}

		DataObjInp dataObjInp = DataObjInp.instanceForNormalPutStrategy(
				targetFile.getAbsolutePath(), localFile.length(),
				targetFile.getResource(), overwrite, myTransferOptions,
				execFlag);

		// see if checksum is required

		if (myTransferOptions != null) {
			if (myTransferOptions.isComputeAndVerifyChecksumAfterTransfer()
					|| myTransferOptions.isComputeChecksumAfterTransfer()) {
				log.info("computing a checksum on the file at:{}",
						localFile.getAbsolutePath());

				ChecksumValue localFileChecksum = computeLocalFileChecksum(
						localFile, null);

				log.info("local file checksum is:{}", localFileChecksum);
				dataObjInp.setFileChecksumValue(localFileChecksum);
			}
		}

		ConnectionProgressStatusListener intraFileStatusListener = null;

		/*
		 * If specified by options, and with a call-back listener registered,
		 * create an object to aggregate and channel within-file progress
		 * reports to the caller.
		 */
		if (transferStatusCallbackListener != null
				&& myTransferOptions.isIntraFileStatusCallbacks()) {
			intraFileStatusListener = DefaultIntraFileProgressCallbackListener
					.instance(TransferType.PUT, localFile.length(),
							transferControlBlock,
							transferStatusCallbackListener);
		}

		InputStream fileInputStream = new FileInputStream(localFile);
		int inputStreamBuffSize = irodsAccessObjectFactory
				.getJargonProperties().getLocalFileInputStreamBufferSize();
		if (inputStreamBuffSize == 0) {
			log.debug("local file input stream will use default buffering");
			fileInputStream = new BufferedInputStream(fileInputStream);
		} else if (inputStreamBuffSize > 0) {
			log.debug(
					"local file input stream will use specified buffering:{}",
					inputStreamBuffSize);
			fileInputStream = new BufferedInputStream(fileInputStream,
					inputStreamBuffSize);
		}

		irodsProtocol.irodsFunctionIncludingAllDataInStream(dataObjInp,
				localFile.length(), fileInputStream, intraFileStatusListener);

	}

	/**
	 * Given local file data, compute the appropriate checksum
	 *
	 * @param localFile
	 * @param overrideChecksumEncoding
	 *            {@link ChecksumEncodingEnum} to use explicitly, otherwise will
	 *            use a default and <code>null</code> can be passed here
	 * @return
	 * @throws JargonException
	 */
	ChecksumValue computeLocalFileChecksum(final File localFile,
			final ChecksumEncodingEnum overrideChecksumEncoding)
			throws JargonException {

		log.info("computeLocalFileChecksum()");

		if (localFile == null) {
			throw new IllegalArgumentException("null localFile");
		}

		log.info("localFile:{}", localFile);

		if (!localFile.exists()) {
			throw new JargonException("file does not exist");
		}

		if (!localFile.isFile()) {
			throw new JargonException("path is not a file");
		}

		ChecksumEncodingEnum checksumEncoding;
		if (overrideChecksumEncoding == null) {
			checksumEncoding = checksumManager
					.determineChecksumEncodingForTargetServer();
		} else {
			checksumEncoding = overrideChecksumEncoding;
		}

		log.info("using checksum algorithm:{}", checksumEncoding);

		AbstractChecksumComputeStrategy strategy = irodsAccessObjectFactory
				.getIrodsSession().getLocalChecksumComputerFactory()
				.instance(checksumEncoding);
		try {
			return strategy.computeChecksumValueForLocalFile(localFile
					.getAbsolutePath());
		} catch (FileNotFoundException e) {
			log.error("cannot find file for computing local checksum", e);
			throw new JargonException(
					"cannot find local file to do the checksum", e);
		}

	}

	/**
	 * Given a checksum value coming back from iRODS, compute the checksum value
	 *
	 * @param irodsValue
	 * @return
	 * @throws JargonException
	 * @deprecated see {@link DataObjectChecksumUtilitiesAO} in future
	 */
	@Deprecated
	ChecksumValue computeChecksumValueFromIrodsData(final String irodsValue)
			throws JargonException {
		// param checks in delegated method
		return checksumManager
				.determineChecksumEncodingFromIrodsData(irodsValue.trim());
	}

	void putReadWriteLoop(final File localFile, final boolean overwrite,
			final IRODSFile targetFile, final int fd,
			final AbstractIRODSMidLevelProtocol irodsProtocol,
			final TransferControlBlock transferControlBlock,
			final ConnectionProgressStatusListener intraFileStatusListener)
			throws JargonException, FileNotFoundException {

		log.info("put read/write loop");

		if (localFile == null) {
			throw new IllegalArgumentException("null localFile");
		}

		if (targetFile == null) {
			throw new IllegalArgumentException("null targetFile");
		}

		if (irodsProtocol == null) {
			throw new IllegalArgumentException("null irodsProtocol");
		}

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}

		/*
		 * Process the iRODS put operation by successively sending chunks of the
		 * file in a loop
		 */

		long lengthLeftToSend = localFile.length();
		InputStream fileInputStream = new FileInputStream(localFile);
		int inputStreamBuffSize = irodsAccessObjectFactory
				.getJargonProperties().getLocalFileInputStreamBufferSize();
		if (inputStreamBuffSize == 0) {
			log.debug("local file input stream will use default buffering");
			fileInputStream = new BufferedInputStream(fileInputStream);
		} else if (inputStreamBuffSize > 0) {
			log.debug(
					"local file input stream will use specified buffering:{}",
					inputStreamBuffSize);
			fileInputStream = new BufferedInputStream(fileInputStream,
					inputStreamBuffSize);
		}

		try {

			log.info("starting read/write loop to send data to iRODS");
			OpenedDataObjInp openedDataObjInp = null;
			long lengthThisSend = 0;

			while (lengthLeftToSend > 0) {

				if (Thread.interrupted()) {
					log.info("cancellation detected, set cancelled in tcb");
					transferControlBlock.setCancelled(true);
				}

				if (transferControlBlock.isCancelled()
						|| transferControlBlock.isPaused()) {
					log.info("cancelling");
					break;
				}

				lengthThisSend = Math.min(putBufferSize, lengthLeftToSend);
				openedDataObjInp = OpenedDataObjInp.instanceForFilePut(fd,
						lengthThisSend);
				lengthLeftToSend -= irodsProtocol
						.irodsFunctionForStreamingToIRODSInFrames(
								openedDataObjInp, (int) lengthThisSend,
								fileInputStream, intraFileStatusListener);

				log.debug("length left:{}", lengthLeftToSend);

			}

			if (lengthLeftToSend != 0) {
				log.error("did not send all data");
				irodsAccessObjectFactory.getIrodsSession()
						.discardSessionForErrors(irodsAccount);
				throw new JargonException("did not send all data");
			}

			log.info("send operation done, send opr complete");
			irodsProtocol.operationComplete(fd);

		} catch (Exception e) {
			log.error("error encountered in read/write loop, will rethrow");
			throw new JargonException(e);
		} finally {
			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}

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
	IRODSFile checkTargetFileForPutOperation(final File localFile,
			final IRODSFile irodsFileDestination, final boolean ignoreChecks,
			final IRODSFileFactory irodsFileFactory) throws JargonException {

		if (localFile == null) {
			throw new IllegalArgumentException("null localFile");
		}

		if (irodsFileDestination == null) {
			throw new IllegalArgumentException("null irodsFileDestination");
		}

		if (irodsFileFactory == null) {
			throw new IllegalArgumentException("null irodsFileFactory");
		}

		IRODSFile targetFile;

		if (ignoreChecks) {
			log.debug("ignoring iRODS checks, assume this is a data object");
			targetFile = irodsFileDestination;
		} else {

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

	/**
	 * @param irodsCollectionAbsolutePath
	 * @param dataName
	 * @param builder
	 * @return
	 */
	static void buildACLQueryForCollectionPathAndDataName(
			final String irodsCollectionAbsolutePath, final String dataName,
			final IRODSGenQueryBuilder builder) throws JargonException {

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_DATA_ACCESS_USER_ID)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_DATA_ACCESS_TYPE)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_TYPE)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_ZONE)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_COLL_NAME,
							QueryConditionOperators.EQUAL,
							irodsCollectionAbsolutePath)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_DATA_NAME,
							QueryConditionOperators.EQUAL, dataName);
		} catch (GenQueryBuilderException e) {
			throw new JargonException(e);
		}
	}

	/**
	 * Special transfer operation when the file is to be read and streamed to
	 * the given output.
	 *
	 * @param irodsFile
	 * @param localFileToHoldData
	 * @param irodsFileLength
	 * @param transferOptions
	 * @param fd
	 * @param transferStatusCallbackListener
	 * @param transferControlBlock
	 * @throws JargonException
	 */
	void processGetTransferViaRead(final IRODSFile irodsFile,
			final File localFileToHoldData, final long irodsFileLength,
			final TransferOptions transferOptions, final int fd,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener)
			throws JargonException {
		log.info("processGetTransferViaRead()");

		if (localFileToHoldData == null) {
			throw new IllegalArgumentException("null localFileToHoldData");
		}

		if (irodsFileLength < 0) {
			throw new IllegalArgumentException("irodsFileLength < 0");
		}

		if (fd <= 0) {
			throw new IllegalArgumentException("fd is <= 0");
		}

		log.info("streaming file transfer started, get output stream for local destination file");

		try {
			IRODSFileInputStream ifis = irodsAccessObjectFactory
					.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFileInputStreamGivingFD(irodsFile, fd);

			Stream2StreamAO stream2StreamAO = irodsAccessObjectFactory
					.getStream2StreamAO(irodsAccount);

			if (transferControlBlock.getTransferOptions()
					.isIntraFileStatusCallbacks()
					&& transferStatusCallbackListener != null) {
				log.info("wrapping stream with callback stream wrapper");
				ConnectionProgressStatusListener connectionProgressStatusListener = DefaultIntraFileProgressCallbackListener
						.instanceSettingTransferOptions(TransferType.GET,
								irodsFileLength, transferControlBlock,
								transferStatusCallbackListener,
								transferControlBlock.getTransferOptions());
				InputStream wrapper = new ByteCountingCallbackInputStreamWrapper(
						connectionProgressStatusListener, ifis);

				stream2StreamAO.transferStreamToFileUsingIOStreams(wrapper,
						localFileToHoldData, irodsFileLength,
						irodsAccessObjectFactory.getJargonProperties()
								.getGetBufferSize());

			} else {

				stream2StreamAO.transferStreamToFileUsingIOStreams(ifis,
						localFileToHoldData, irodsFileLength, 4194304);

			}

		} catch (JargonException e) {
			log.error("Exception streaming data to local file from iRODS: {}",
					localFileToHoldData.getAbsolutePath(), e);
			throw e;
		} finally {

			// here you know it's a 0 threads so do a file close with the right
			// index

			irodsFile.closeGivenDescriptor(fd);
		}

	}

	/**
	 * Append the appropriately formed query condition to the provided builder
	 * for a collection metadata query
	 *
	 * @param queryElement
	 *            {@link AVUQueryElement} to be added as a condition
	 * @param builder
	 *            {@link IRODSGenQueryBuilder} that will have the derived
	 *            condition appended
	 * @throws JargonQueryException
	 *             if the query cannot be built
	 */
	public static void appendConditionPartToBuilderQuery(
			final AVUQueryElement queryElement,
			final IRODSGenQueryBuilder builder) throws JargonQueryException {

		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.ATTRIBUTE) {
			builder.addConditionAsGenQueryField(
					RodsGenQueryEnum.COL_META_DATA_ATTR_NAME,
					queryElement.getOperator(), queryElement.getValue().trim());
		} else if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.VALUE) {
			builder.addConditionAsGenQueryField(
					RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE,
					queryElement.getOperator(), queryElement.getValue().trim());
		} else if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.UNITS) {
			builder.addConditionAsGenQueryField(
					RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS,
					queryElement.getOperator(), queryElement.getValue().trim());
		} else {
			throw new JargonQueryException("unable to resolve AVU Query part");
		}

	}

	public static void translateAVUQueryElementOperatorToBuilderQueryCondition(
			final AVUQueryElement avuQueryElement) {

	}

	/**
	 * Given the provide builder, add the selects for the iCAT data object.
	 * These will be appended to the provided <code>IRODSGenQueryBuilder</code>
	 * object
	 *
	 * @param builder
	 *            {@link IRODSGenQueryBuilder} that will have the selects
	 *            appended to it
	 * @throws GenQueryBuilderException
	 */
	public static void addDataObjectSelectsToBuilder(
			final IRODSGenQueryBuilder builder) throws GenQueryBuilderException {

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_DATA_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_COLL_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_REPL_NUM)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_VERSION)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_TYPE_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE)
				.addSelectAsGenQueryValue(
						RodsGenQueryEnum.COL_D_RESC_GROUP_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_RESC_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_DATA_PATH)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_OWNER_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_OWNER_ZONE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_REPL_STATUS)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_DATA_STATUS)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_DATA_CHECKSUM)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_EXPIRY)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_MAP_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_COMMENTS)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_CREATE_TIME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_MODIFY_TIME);

	}

	/**
	 * Build the necessary GenQuery selects to query data objects for
	 * information. Used in many common queries for listing data objects, as in
	 * an ils-like command. This version does not ask for any replication
	 * information.
	 *
	 * @param builder
	 *            {@link IRODSGenQueryBuilder} that will be augmented with the
	 *            necessary selects
	 *
	 */
	public static void buildDataObjectQuerySelectsNoReplicationInfo(
			final IRODSGenQueryBuilder builder) throws GenQueryBuilderException {

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_DATA_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_OWNER_NAME);

	}

	/**
	 * Given a result set from the
	 * <code>buildDataObjectQuerySelectsNoReplicationInfo()</code> method,
	 * return a result set of <code>CollectionAndDataObjectListingEntry</code>
	 *
	 * @param row
	 *            {@link IRODSQueryResultRow} entry from query
	 * @param totalRecords
	 *            <code>int</code> with total records in the result set
	 * @return <code>List</code> of {@link CollectionAndDataObjectListingEntry}
	 * @throws JargonException
	 */
	public static CollectionAndDataObjectListingEntry buildCollectionListEntryFromResultSetRowForDataObjectQueryNoReplicationInfo(
			final IRODSQueryResultRow row, final int totalRecords)
			throws JargonException {

		CollectionAndDataObjectListingEntry entry = new CollectionAndDataObjectListingEntry();
		entry.setParentPath(row.getColumn(0));
		entry.setObjectType(ObjectType.DATA_OBJECT);
		entry.setPathOrName(row.getColumn(1));
		entry.setId(IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(row
				.getColumn(2)));
		entry.setDataSize(IRODSDataConversionUtil
				.getLongOrZeroFromIRODSValue(row.getColumn(3)));
		entry.setOwnerName(row.getColumn(4));
		entry.setCount(row.getRecordCount());
		entry.setLastResult(row.isLastResult());
		entry.setTotalRecords(totalRecords);

		log.debug("listing entry built {}", entry.toString());

		return entry;

	}

}
