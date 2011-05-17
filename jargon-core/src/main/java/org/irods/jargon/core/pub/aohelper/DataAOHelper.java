/**
 * 
 */
package org.irods.jargon.core.pub.aohelper;

import static edu.sdsc.grid.io.irods.IRODSConstants.RODS_API_REQ;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSCommands;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.query.RodsGenQueryEnum;
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
	public String buildSelects() {
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
	public DataObject buildDomainFromResultSetRow(final IRODSQueryResultRow row)
			throws JargonException {
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
	public String buildMetadataSelects() {
		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS.getName());
		return sb.toString();
	}

	public List<DataObject> buildListFromResultSet(
			final IRODSQueryResultSetInterface resultSet) throws JargonException {

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
	public StringBuilder buildConditionPart(final AVUQueryElement queryElement) {
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
	public List<MetaDataAndDomainData> buildMetaDataAndDomainDataListFromResultSet(
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
	 * @throws JargonException
	 */
	public void processNormalGetTransfer(final File localFileToHoldData,
			final long length, final IRODSCommands irodsProtocol, final TransferOptions transferOptions) throws JargonException {

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
		irodsProtocol.read(localFileOutputStream, length);
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
	 * @param localFile
	 * @param overwrite
	 * @param transferOptions
	 * @param targetFile
	 * @throws JargonException
	 * @throws FileNotFoundException
	 */
	public void processNormalPutTransfer(final File localFile,
			final boolean overwrite, final TransferOptions transferOptions,
			IRODSFile targetFile, final IRODSCommands irodsProtocol) throws JargonException, FileNotFoundException {
		
		if (localFile == null) {
			throw new IllegalArgumentException("null localFile");
		}
		
		if (transferOptions == null) {
			throw new IllegalArgumentException("null transferOptions");
		}
		
		log.info("processing as a normal put strategy");

		DataObjInp dataObjInp = DataObjInp.instanceForNormalPutStrategy(
				targetFile.getAbsolutePath(), localFile.length(),
				targetFile.getResource(), overwrite, transferOptions);

		irodsProtocol.irodsFunction(RODS_API_REQ,
				dataObjInp.getParsedTags(), 0, null,
				localFile.length(), new FileInputStream(localFile),
				dataObjInp.getApiNumber());
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
	public IRODSFile checkTargetFileForPutOperation(final File localFile,
			final IRODSFile irodsFileDestination, final boolean ignoreChecks, final IRODSFileFactory irodsFileFactory)
			throws JargonException {
		
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
	public static String buildACLQueryForCollectionPathAndDataName(
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

	
	

}
