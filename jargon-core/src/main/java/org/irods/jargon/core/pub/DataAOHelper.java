/**
 * 
 */
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

import org.irods.jargon.core.connection.ConnectionProgressStatusListener;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.OpenedDataObjInp;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.pub.aohelper.AOHelper;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.ByteCountingCallbackInputStreamWrapper;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.LocalFileUtils;
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
final class DataAOHelper extends AOHelper {
	public static final Logger log = LoggerFactory
			.getLogger(DataAOHelper.class);

	private final IRODSAccessObjectFactory irodsAccessObjectFactory;
	private final IRODSAccount irodsAccount;
	private int streamBufferSize = 0;
	private int putBufferSize = 0;

	protected DataAOHelper(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super();
		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;

		streamBufferSize = this.irodsAccessObjectFactory.getJargonProperties()
				.getSendInputStreamBufferSize();
		putBufferSize = this.irodsAccessObjectFactory.getJargonProperties()
				.getPutBufferSize();

	}

	/**
	 * Create a set of selects for a data object, used in general query. Note
	 * that the 'SELECT' token is appended as the first token in the query.
	 * 
	 * FIXME: alternative queries for 1 result per object versus 1 result per
	 * replica? Otherwise, perhaps the replica info could be in a list within
	 * the data object?
	 * 
	 * @return <code>String</code> with select statements for the domain object.
	 */
	protected String buildSelects() {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_D_DATA_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_COLL_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_REPL_NUM.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_VERSION.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_TYPE_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_DATA_SIZE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_RESC_GROUP_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_RESC_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_DATA_PATH.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_OWNER_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_OWNER_ZONE.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_REPL_STATUS.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_DATA_STATUS.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_DATA_CHECKSUM.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_EXPIRY.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_MAP_ID.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_COMMENTS.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_CREATE_TIME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_D_MODIFY_TIME.getName());
		return query.toString();
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
	protected DataObject buildDomainFromResultSetRow(
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
		dataObject.setResourceGroupName(row.getColumn(8));
		dataObject.setResourceName(row.getColumn(9));
		dataObject.setDataPath(row.getColumn(10));
		dataObject.setDataOwnerName(row.getColumn(11));
		dataObject.setDataOwnerZone(row.getColumn(12));
		dataObject.setReplicationStatus(row.getColumn(13));
		dataObject.setDataStatus(row.getColumn(14));
		dataObject.setChecksum(row.getColumn(15));
		dataObject.setExpiry(row.getColumn(16));
		dataObject.setDataMapId(Integer.parseInt(row.getColumn(17)));
		dataObject.setComments(row.getColumn(18));
		dataObject.setCreatedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(19)));
		dataObject.setUpdatedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(20)));

		// add info to track position in records for possible requery
		dataObject.setLastResult(row.isLastResult());
		dataObject.setCount(row.getRecordCount());

		if (log.isInfoEnabled()) {
			log.info("data object built \n");
			log.info(dataObject.toString());
		}

		return dataObject;
	}

	/**
	 * Convenience method to add a series of selects for the AVU metadata for
	 * this domain. Note that the 'select' token is not present in the returned
	 * data.
	 * 
	 * @return <code>String</code> with an iquest-like set of select values for
	 *         the metadata AVU elements.
	 */
	protected String buildMetadataSelects() {
		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS.getName());
		return sb.toString();
	}

	protected List<DataObject> buildListFromResultSet(
			final IRODSQueryResultSetInterface resultSet)
			throws JargonException {

		final List<DataObject> data = new ArrayList<DataObject>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			data.add(buildDomainFromResultSetRow(row));
		}

		return data;
	}

	/**
	 * Given a set of AVU Query parameters, build the appropriate condition to
	 * add to a query
	 * 
	 * @param queryCondition
	 *            <code>List</code> of
	 *            {@link org.irods.jargon.core.query.AVUQueryElement} that
	 *            describes a metadata query
	 * @param queryElement
	 *            <codeStringBuilder</code> with the given AVU query in iquest
	 *            query form.
	 */
	protected StringBuilder buildConditionPart(
			final AVUQueryElement queryElement) {
		StringBuilder queryCondition = new StringBuilder();
		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.ATTRIBUTE) {
			queryCondition.append(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME
					.getName());
			queryCondition.append(SPACE);
			queryCondition
					.append(queryElement.getOperator().getOperatorValue());
			queryCondition.append(SPACE);
			queryCondition.append(QUOTE);
			queryCondition.append(queryElement.getValue());
			queryCondition.append(QUOTE);
		}

		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.VALUE) {
			queryCondition.append(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE
					.getName());
			queryCondition.append(SPACE);
			queryCondition
					.append(queryElement.getOperator().getOperatorValue());
			queryCondition.append(SPACE);
			queryCondition.append(QUOTE);
			queryCondition.append(queryElement.getValue());
			queryCondition.append(QUOTE);
		}

		if (queryElement.getAvuQueryPart() == AVUQueryElement.AVUQueryPart.UNITS) {
			queryCondition.append(RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS
					.getName());
			queryCondition.append(SPACE);
			queryCondition
					.append(queryElement.getOperator().getOperatorValue());
			queryCondition.append(SPACE);
			queryCondition.append(QUOTE);
			queryCondition.append(queryElement.getValue());
			queryCondition.append(QUOTE);
		}

		return queryCondition;
	}

	/**
	 * @param metaDataDomain
	 * @param irodsQueryResultSet
	 * @return
	 * @throws JargonException
	 */
	protected List<MetaDataAndDomainData> buildMetaDataAndDomainDataListFromResultSet(
			final IRODSQueryResultSetInterface irodsQueryResultSet)
			throws JargonException {

		if (irodsQueryResultSet == null) {
			throw new JargonException("null irodsQueryResultSet");
		}

		List<MetaDataAndDomainData> metaDataResults = new ArrayList<MetaDataAndDomainData>();

		for (IRODSQueryResultRow row : irodsQueryResultSet.getResults()) {
			metaDataResults
					.add(buildMetaDataAndDomainDataFromResultSetRowForDataObject(row));
		}

		return metaDataResults;
	}

	/**
	 * @param metadataDomain
	 * @param row
	 * @return
	 * @throws JargonException
	 */
	private static MetaDataAndDomainData buildMetaDataAndDomainDataFromResultSetRowForDataObject(
			final IRODSQueryResultRow row) throws JargonException {

		String domainId = row.getColumn(0);
		StringBuilder sb = new StringBuilder();
		sb.append(row.getColumn(1));
		sb.append('/');
		sb.append(row.getColumn(2));
		String domainUniqueName = sb.toString();
		String attributeName = row.getColumn(3);
		String attributeValue = row.getColumn(4);
		String attributeUnits = row.getColumn(5);

		MetaDataAndDomainData data = MetaDataAndDomainData.instance(
				MetadataDomain.DATA, domainId, domainUniqueName, attributeName,
				attributeValue, attributeUnits);

		data.setCount(row.getRecordCount());
		data.setLastResult(row.isLastResult());

		log.debug("metadataAndDomainData: {}", data);
		return data;
	}

	/**
	 * @param localFileToHoldData
	 * @param length
	 * @param transferOptions
	 * @param transferStatusCallbackListener
	 * @param transferControlBlock
	 * @throws JargonException
	 */
	protected void processNormalGetTransfer(final File localFileToHoldData,
			final long length, final IRODSCommands irodsProtocol,
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
			localFileOutputStream = new BufferedOutputStream(
					new FileOutputStream(localFileToHoldData)); // FIXME: set
																// buffer size
																// for file
																// output stream
																// jargon.io.local.output.stream.buffer.size
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
				|| transferControlBlock.getTransferOptions()
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
	protected void processNormalPutTransfer(final File localFile,
			final boolean overwrite, final IRODSFile targetFile,
			final IRODSCommands irodsProtocol,
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
				targetFile.getResource(), overwrite, myTransferOptions, execFlag);

		// see if checksum is required

		if (myTransferOptions != null) {
			if (myTransferOptions.isComputeAndVerifyChecksumAfterTransfer()
					|| myTransferOptions.isComputeChecksumAfterTransfer()) {
				log.info("computing a checksum on the file at:{}",
						localFile.getAbsolutePath());
				String localFileChecksum = LocalFileUtils
						.md5ByteArrayToString(LocalFileUtils
								.computeMD5FileCheckSumViaAbsolutePath(localFile
										.getAbsolutePath()));
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
				|| myTransferOptions.isIntraFileStatusCallbacks()) {
			intraFileStatusListener = DefaultIntraFileProgressCallbackListener
					.instanceSettingInterval(TransferType.PUT,
							localFile.length(), transferControlBlock,
							transferStatusCallbackListener, 100);
		}

		irodsProtocol.irodsFunctionIncludingAllDataInStream(dataObjInp,
				localFile.length(), new FileInputStream(localFile),
				intraFileStatusListener);

	}

	protected void putReadWriteLoop(final File localFile,
			final boolean overwrite, final IRODSFile targetFile, final int fd,
			final IRODSCommands irodsProtocol,
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
		InputStream fis = null;
		try {
			fis = new FileInputStream(localFile);

			log.debug(
					"stream buffer size for file input stream used to read local file:{}",
					streamBufferSize);

			if (streamBufferSize == 0) {
				fis = new BufferedInputStream(fis);
			} else if (streamBufferSize > 0) {
				fis = new BufferedInputStream(fis, streamBufferSize);
			}

			log.info("starting read/write loop to send data to iRODS");
			OpenedDataObjInp openedDataObjInp = null;
			long lengthThisSend = 0;

			while (lengthLeftToSend > 0) {
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
								openedDataObjInp, (int) lengthThisSend, fis,
								intraFileStatusListener);

				log.debug("length left:{}", lengthLeftToSend);

			}

			if (lengthLeftToSend != 0) {
				log.error("did not send all data");
				irodsProtocol.disconnectWithIOException();
				throw new JargonException("did not send all data");
			}

			log.info("send operation done, send opr complete");
			irodsProtocol.operationComplete(fd);

		} catch (Exception e) {
			log.error("error encountered in read/write loop, will rethrow");
			throw new JargonException(e);
		} finally {
			try {
				fis.close();
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
	protected IRODSFile checkTargetFileForPutOperation(final File localFile,
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

	/**
	 * @param irodsCollectionAbsolutePath
	 * @param dataName
	 * @return
	 */
	protected static String buildACLQueryForCollectionPathAndDataName(
			final String irodsCollectionAbsolutePath, final String dataName) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		query.append(",");
		query.append(RodsGenQueryEnum.COL_DATA_ACCESS_USER_ID.getName());
		query.append(",");
		query.append(RodsGenQueryEnum.COL_DATA_ACCESS_TYPE.getName());
		query.append(" WHERE ");
		query.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		query.append(EQUALS_AND_QUOTE);
		query.append(IRODSDataConversionUtil
				.escapeSingleQuotes(irodsCollectionAbsolutePath));
		query.append(QUOTE);
		query.append(AND);
		query.append(RodsGenQueryEnum.COL_DATA_NAME.getName());
		query.append(EQUALS_AND_QUOTE);
		query.append(dataName);
		query.append(QUOTE);
		return query.toString();
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
	protected void processGetTransferViaRead(final IRODSFile irodsFile,
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
						.instanceSettingInterval(TransferType.GET,
								irodsFileLength, transferControlBlock,
								transferStatusCallbackListener, 5);
				InputStream wrapper = new ByteCountingCallbackInputStreamWrapper(
						connectionProgressStatusListener, ifis);
				/*
				 * stream2StreamAO.transferStreamToFile(wrapper,
				 * localFileToHoldData, irodsFileLength, 32768L); // FIXME:
				 * parameterize buffer length
				 */
				stream2StreamAO.transferStreamToFileUsingIOStreams(wrapper,
						localFileToHoldData, irodsFileLength, 4194304);

			} else {
				/*
				 * stream2StreamAO.transferStreamToFile(ifis,
				 * localFileToHoldData, irodsFileLength, 32768L);
				 */
				stream2StreamAO.transferStreamToFileUsingIOStreams(ifis,
						localFileToHoldData, irodsFileLength, 4194304);

			}

		} catch (JargonException e) {
			log.error("Exception streaming data to local file from iRODS: {}",
					localFileToHoldData.getAbsolutePath(), e);
			throw e;
		} finally {
			irodsFile.closeGivenDescriptor(fd);
		}

	}

}
