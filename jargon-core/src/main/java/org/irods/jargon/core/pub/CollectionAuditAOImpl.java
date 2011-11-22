package org.irods.jargon.core.pub;

import static org.irods.jargon.core.pub.aohelper.AOHelper.EQUALS_AND_QUOTE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.QUOTE;
import static org.irods.jargon.core.pub.aohelper.AOHelper.WHERE;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.AuditActionEnum;
import org.irods.jargon.core.pub.domain.AuditedAction;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents audit trail capabilities for collections
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class CollectionAuditAOImpl extends IRODSGenericAO implements
		CollectionAuditAO {

	public static final Logger log = LoggerFactory
			.getLogger(CollectionAuditAOImpl.class);
	public static final char COMMA = ',';

	private transient final IRODSGenQueryExecutor irodsGenQueryExecutor;

	/**
	 * Default constructor as invoked by {@link IRODSAccessObjectFactory}
	 * 
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected CollectionAuditAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
		this.irodsGenQueryExecutor = this.getIRODSAccessObjectFactory()
				.getIRODSGenQueryExecutor(irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.CollectionAuditAO#findAllAuditRecordsForCollection
	 * (org.irods.jargon.core.pub.io.IRODSFile, int)
	 */
	@Override //FIXME: add junit test
	public List<AuditedAction> findAllAuditRecordsForCollection(
			final IRODSFile irodsFile, final int partialStart)
			throws JargonException {

		log.info("findAllAuditRecordsForCollection()");

		if (irodsFile == null) {
			throw new IllegalArgumentException("null or empty irodsFile");
		}

		if (partialStart < 0) {
			throw new IllegalArgumentException("partial start must be >= 0");
		}

		log.info("irodsFile:{}", irodsFile);
		log.info("partialStart:{}", partialStart);

		List<AuditedAction> auditedActions = new ArrayList<AuditedAction>();

		final StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(RodsGenQueryEnum.COL_AUDIT_OBJ_ID.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_AUDIT_USER_ID.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_AUDIT_ACTION_ID.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_AUDIT_COMMENT.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_AUDIT_CREATE_TIME.getName());
		sb.append(COMMA);
		sb.append(RodsGenQueryEnum.COL_AUDIT_MODIFY_TIME.getName());
		sb.append(WHERE);
		sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
		sb.append(EQUALS_AND_QUOTE);
		sb.append(IRODSDataConversionUtil.escapeSingleQuotes(irodsFile
				.getAbsolutePath().trim()));
		sb.append(QUOTE);

		final String query = sb.toString();
		log.debug("query for audit collection:{}", query);

		final IRODSGenQuery irodsQuery = IRODSGenQuery.instance(query,
				getIRODSSession().getJargonProperties()
						.getMaxFilesAndDirsQueryMax());

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, partialStart);
		} catch (JargonQueryException e) {
			log.error("query exception for query: {}", query, e);
			throw new JargonException("error in query for data object", e);
		}

		AuditedAction auditedAction;
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			auditedAction = new AuditedAction();
			auditedAction.setObjectId(Integer.parseInt(row.getColumn(0)));
			auditedAction
					.setDomainObjectUniqueName(irodsFile.getAbsolutePath());
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
			auditedActions.add(auditedAction);
			log.info("added audited action:{}", auditedAction);
			// add info to track position in records for possible requery
		}

		return auditedActions;

	}

}
