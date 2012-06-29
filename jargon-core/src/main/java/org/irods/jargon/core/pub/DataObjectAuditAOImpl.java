package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.AuditActionEnum;
import org.irods.jargon.core.pub.domain.AuditedAction;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents audit trail capabilities for data objects.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObjectAuditAOImpl extends IRODSGenericAO implements
		DataObjectAuditAO {

	public static final Logger log = LoggerFactory
			.getLogger(DataObjectAuditAOImpl.class);
	public static final char COMMA = ',';

	private transient final IRODSGenQueryExecutor irodsGenQueryExecutor;

	/**
	 * Default constructor as invoked by {@link IRODSAccessObjectFactory}
	 * 
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected DataObjectAuditAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		this.irodsGenQueryExecutor = this.getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAuditAO#getAuditedActionForDataObject
	 * (org.irods.jargon.core.pub.io.IRODSFile, int)
	 */
	@Override
	public AuditedAction getAuditedActionForDataObject(final IRODSFile irodsFile, final int id)
			throws DataNotFoundException, JargonException {

		log.info("getAuditedActionForDataObject()");
		
		if (irodsFile == null) {
			throw new IllegalArgumentException("null or empty irodsFile");
		}

		if (id < 1) {
			throw new IllegalArgumentException("id must be > 0");
		}
		
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
							QueryConditionOperators.EQUAL, String.valueOf(id));
				
			// .addOrderByGenQueryField(RodsGenQueryEnum.COL_DATA_NAME,
			// GenQueryOrderByField.OrderByType.ASC);
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(1);
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
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
				resultSet
				.getFirstResult());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.DataObjectAuditAO#findAllAuditRecords(org.irods
	 * .jargon.core.pub.io.IRODSFile, int)
	 */
	@Override
	public List<AuditedAction> findAllAuditRecordsForDataObject(
			final IRODSFile irodsFile, final int partialStart,
			final int numberOfResultsDesired) throws JargonException {

		log.info("findAllAuditRecords()");

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

		log.info("irodsFile:{}", irodsFile);
		log.info("partialStart:{}", partialStart);

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
							RodsGenQueryEnum.COL_COLL_NAME,
							QueryConditionOperators.EQUAL,
							IRODSDataConversionUtil
									.escapeSingleQuotes(irodsFile.getParent()
											.trim()))
					.addConditionAsGenQueryField(
							RodsGenQueryEnum.COL_DATA_NAME,
							QueryConditionOperators.EQUAL,
							IRODSDataConversionUtil
									.escapeSingleQuotes(irodsFile.getName()
											.trim()));
			// .addOrderByGenQueryField(RodsGenQueryEnum.COL_DATA_NAME,
			// GenQueryOrderByField.OrderByType.ASC);
			IRODSGenQueryFromBuilder irodsQuery = builder
					.exportIRODSQueryFromBuilder(numberOfResultsDesired);
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, partialStart);
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

	/**
	 * @param irodsFile
	 * @param row
	 * @return
	 * @throws NumberFormatException
	 * @throws JargonException
	 */
	private AuditedAction buildAuditedActionForResultRow(
			final IRODSFile irodsFile, IRODSQueryResultRow row)
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
		auditedAction.setUpdatedAt(IRODSDataConversionUtil
				.getDateFromIRODSValue(row.getColumn(6)));
		auditedAction.setLastResult(row.isLastResult());
		auditedAction.setCount(row.getRecordCount());
		auditedAction.setCount(row.getRecordCount());
		auditedAction.setLastResult(row.isLastResult());
		return auditedAction;
	}

}
