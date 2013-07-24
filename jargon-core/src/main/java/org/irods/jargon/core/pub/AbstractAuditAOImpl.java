package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.AuditActionEnum;
import org.irods.jargon.core.pub.domain.AuditedAction;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryOrderByField.OrderByType;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class handles common collection and data object functionality.
 * There are data object and collection audit objects that will essentially
 * delegate a lot of processing to this parent object, however, it is
 * anticipated that this API will evolve to more data object and file specific
 * operations, thus there is a semantic split between data objects and
 * collections to match the different ICAT representation of data objects and
 * collections in general system metadata.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractAuditAOImpl extends IRODSGenericAO {

	protected final transient IRODSGenQueryExecutor irodsGenQueryExecutor;
	public static final Logger log = LoggerFactory
			.getLogger(AbstractAuditAOImpl.class);

	/**
	 * Default constructor as invoked by {@link IRODSAccessObjectFactory}
	 * 
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	public AbstractAuditAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		irodsGenQueryExecutor = getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(irodsAccount);
	}

	/**
	 * Get an individual audit action for afile, given that you know enough
	 * fields to find the unique entry. This is sort of difficult (can can be
	 * expensive) as there is not a unique index or generated id to an audit
	 * event, so use sparingly.
	 * 
	 * @param irodsFile
	 *            {@link IRODSFile} that will be the target of the query
	 * @param auditActionCode
	 *            <code>String</code> with the audited action code (the event
	 *            type)
	 * @param timeStampInIRODSFormat
	 *            <code>String</code> with the time stamp (in irods format) that
	 *            is associated with this event. Conveniently, the
	 *            <code>AuditedAction</code> object returned from a query has
	 *            this data in the correct format.
	 * @return {@link AuditedAction} with available details about the audit
	 *         event
	 * @throws DataNotFoundException
	 *             if the data object cannot be found
	 * @throws JargonException
	 */
	protected AuditedAction getAuditedActionForFile(final IRODSFile irodsFile,
			final String auditActionCode, final String timeStampInIRODSFormat)
			throws DataNotFoundException, JargonException {

		log.info("getAuditedActionForDataObject()");

		if (irodsFile == null) {
			throw new IllegalArgumentException("null or empty irodsFile");
		}

		if (auditActionCode == null || auditActionCode.isEmpty()) {
			throw new IllegalArgumentException("null or empty auditActionCode");
		}

		if (timeStampInIRODSFormat == null || timeStampInIRODSFormat.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty timeStampInIRODSFormat");
		}

		log.info("looking up data object id via objStat");
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());
		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsFile.getAbsolutePath());

		// make sure this special coll type has support
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		// get the canonical path name as a collection parent and data name

		// get absolute path to use for querying iCAT (could be a soft link)
		String absPath = MiscIRODSUtils
				.determineAbsolutePathBasedOnCollTypeInObjectStat(objStat);

		log.info("absPath for querying iCAT:{}", absPath);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_AUDIT_OBJ_ID)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_AUDIT_USER_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_AUDIT_ACTION_ID)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_AUDIT_COMMENT)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_AUDIT_CREATE_TIME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_AUDIT_MODIFY_TIME)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_AUDIT_OBJ_ID,
							QueryConditionOperators.EQUAL, objStat.getDataId())
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_AUDIT_ACTION_ID,
							QueryConditionOperators.EQUAL,
							String.valueOf(auditActionCode))
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
							QueryConditionOperators.EQUAL,
							String.valueOf(timeStampInIRODSFormat))
					.addOrderByGenQueryField(
							RodsGenQueryEnum.COL_AUDIT_CREATE_TIME,
							OrderByType.ASC);

			// .addOrderByGenQueryField(RodsGenQueryEnum.COL_DATA_NAME,
			// GenQueryOrderByField.OrderByType.ASC);
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(1);
			String zone = MiscIRODSUtils.getZoneInPath(objStat
					.getAbsolutePath());
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, zone);
		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("error building query", e);
		} catch (JargonQueryException jqe) {
			log.error("error executing query", jqe);
			throw new JargonException("error executing query", jqe);
		}

		/*
		 * DataNotFoundException will be thrown if there was no result row
		 */
		return buildAuditedActionForResultRow(irodsFile,
				resultSet.getFirstResult());

	}

	/**
	 * List all audit records for a given file. This has a partial start index
	 * for paging through very large data sets. The <code>AuditedAction</code>
	 * objects contain information about 'more results' as well as sequence
	 * numbers to aid in paging.
	 * 
	 * @param irodsFile
	 *            {@link IRODSFile} that will be the target of the query
	 * @param partialStart
	 *            <code>int</code> that is 0 or an offset into the result set,
	 *            for paging
	 * @param numberOfResultsDesired
	 *            <code>int</code> that indicates the number of results to
	 *            return in one query
	 * 
	 * @return <code>List</code> of {@link AuditedAction} with information about
	 *         the audit history of the data object
	 * @throws FileNotFoundException
	 *             if file is missing
	 * @throws JargonException
	 */
	protected List<AuditedAction> findAllAuditRecordsForFile(
			final IRODSFile irodsFile, final int partialStart,
			final int numberOfResultsDesired) throws FileNotFoundException,
			JargonException {

		log.info("findAllAuditRecordsForFile()");

		if (irodsFile == null) {
			throw new IllegalArgumentException("null or empty irodsFile");
		}

		if (partialStart < 0) {
			throw new IllegalArgumentException("partial start must be >= 0");
		}

		if (numberOfResultsDesired < 1) {
			throw new IllegalArgumentException(
					"numberOfResultsDesired must be >= 1");
		}

		log.info("looking up data object id via objStat");
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIRODSAccount());
		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsFile.getAbsolutePath());

		log.info("irodsFile:{}", irodsFile);
		log.info("partialStart:{}", partialStart);

		// make sure this special coll type has support
		MiscIRODSUtils.evaluateSpecCollSupport(objStat);

		List<AuditedAction> auditedActions = new ArrayList<AuditedAction>();

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
		IRODSQueryResultSetInterface resultSet;

		try {
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_AUDIT_OBJ_ID)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_AUDIT_USER_ID)
					.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_NAME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_AUDIT_ACTION_ID)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_AUDIT_COMMENT)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_AUDIT_CREATE_TIME)
					.addSelectAsGenQueryValue(
							RodsGenQueryEnum.COL_AUDIT_MODIFY_TIME)
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_AUDIT_OBJ_ID,
							QueryConditionOperators.EQUAL, objStat.getDataId());
			// .addOrderByGenQueryField(RodsGenQueryEnum.COL_DATA_NAME,
			// GenQueryOrderByField.OrderByType.ASC);
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(numberOfResultsDesired);
			String zone = MiscIRODSUtils.getZoneInPath(objStat
					.getAbsolutePath());
			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResultInZone(irodsQuery,
							partialStart, zone);
		} catch (GenQueryBuilderException e) {
			log.error("error building query", e);
			throw new JargonException("error building query", e);
		} catch (JargonQueryException jqe) {
			log.error("error executing query", jqe);
			throw new JargonException("error executing query", jqe);
		}

		AuditedAction auditedAction;
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			auditedAction = buildAuditedActionForResultRow(irodsFile, row);
			auditedActions.add(auditedAction);
			log.info("added audited action:{}", auditedAction);
		}

		return auditedActions;

	}

	protected AuditedAction buildAuditedActionForResultRow(
			final IRODSFile irodsFile, final IRODSQueryResultRow row)
			throws NumberFormatException, JargonException {
		AuditedAction auditedAction;
		auditedAction = new AuditedAction();
		auditedAction.setObjectId(Integer.parseInt(row.getColumn(0)));
		auditedAction.setDomainObjectUniqueName(irodsFile.getAbsolutePath());
		auditedAction.setUserId(Integer.parseInt(row.getColumn(1)));
		auditedAction.setUserName(row.getColumn(2));
		auditedAction.setAuditActionEnum(AuditActionEnum.valueOf(Integer
				.parseInt(row.getColumn(3))));
		auditedAction.setComment(row.getColumn(4));
		auditedAction.setCreatedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(5)));
		auditedAction.setTimeStampInIRODSFormat(row.getColumn(5));
		auditedAction.setUpdatedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(6)));
		auditedAction.setLastResult(row.isLastResult());
		auditedAction.setCount(row.getRecordCount());
		auditedAction.setCount(row.getRecordCount());
		auditedAction.setLastResult(row.isLastResult());
		return auditedAction;
	}

}