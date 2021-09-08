package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.GeneralAdminInp;
import org.irods.jargon.core.pub.domain.Quota;
import org.irods.jargon.core.query.AbstractAliasedQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.SimpleQuery;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access object to manage quota information for users and groups.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class QuotaAOImpl extends IRODSGenericAO implements QuotaAO {

	public static final Logger log = LoggerFactory.getLogger(QuotaAOImpl.class);
	private SimpleQueryExecutorAO simpleQueryExcecutor = null;

	public static final String ALL_QUOTA_GLOBAL_QUERY = "select user_name, R_USER_MAIN.zone_name, quota_limit, quota_over, R_QUOTA_MAIN.modify_ts from R_QUOTA_MAIN, R_USER_MAIN where R_USER_MAIN.user_id = R_QUOTA_MAIN.user_id and R_QUOTA_MAIN.resc_id = 0";
	public static final String QUOTA_GLOBAL_FOR_USER_AND_ZONE_QUERY = "select user_name, R_USER_MAIN.zone_name, quota_limit, quota_over, R_QUOTA_MAIN.modify_ts from R_QUOTA_MAIN, R_USER_MAIN where R_USER_MAIN.user_id = R_QUOTA_MAIN.user_id and R_QUOTA_MAIN.resc_id = 0 and user_name=? and R_USER_MAIN.zone_name=?";
	public static final String ALL_QUOTA_QUERY = "select user_name, R_USER_MAIN.zone_name, resc_name, quota_limit, quota_over, R_QUOTA_MAIN.modify_ts from R_QUOTA_MAIN, R_USER_MAIN, R_RESC_MAIN where R_USER_MAIN.user_id = R_QUOTA_MAIN.user_id and R_RESC_MAIN.resc_id = R_QUOTA_MAIN.resc_id";
	public static final String QUOTA_FOR_USER_AND_ZONE_QUERY = "select user_name, R_USER_MAIN.zone_name, resc_name, quota_limit, quota_over, R_QUOTA_MAIN.modify_ts from R_QUOTA_MAIN, R_USER_MAIN, R_RESC_MAIN where R_USER_MAIN.user_id = R_QUOTA_MAIN.user_id and R_RESC_MAIN.resc_id = R_QUOTA_MAIN.resc_id and user_name=? and R_USER_MAIN.zone_name=?";

	/**
	 * Default constructor (protected). Object is created via factory.
	 *
	 * @param irodsSession
	 *            {@link IRODSSession}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @throws JargonException
	 *             for iRODS error
	 */
	protected QuotaAOImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.QuotaAO#listAllQuota()
	 */
	@Override
	public List<Quota> listAllQuota() throws JargonException {
		log.info("listAllQuota()");
		List<Quota> quota = new ArrayList<Quota>();
		AbstractAliasedQuery simpleQuery = SimpleQuery.instanceWithNoArguments(ALL_QUOTA_QUERY, 0);
		log.info("exec simple query to get quota values");
		IRODSQueryResultSetInterface resultSet = getSimpleQueryExecutorAO().executeSimpleQuery(simpleQuery);

		Quota quotaValue = null;
		for (IRODSQueryResultRow row : resultSet.getResults()) {
			quota.add(buildQuotaFromPerResourceResultRow(row));
			log.info("added quota value:{}", quotaValue);
		}

		return quota;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.QuotaAO#listAllGlobalQuota()
	 */
	@Override
	public List<Quota> listAllGlobalQuota() throws JargonException {
		log.info("listAllGlobalQuota()");
		List<Quota> quota = new ArrayList<Quota>();
		AbstractAliasedQuery simpleQuery = SimpleQuery.instanceWithNoArguments(ALL_QUOTA_GLOBAL_QUERY, 0);
		log.info("exec simple query to get quota values");
		IRODSQueryResultSetInterface resultSet = getSimpleQueryExecutorAO().executeSimpleQuery(simpleQuery);

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			quota.add(buildQuotaFromGlobalResultRow(row));
		}

		return quota;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.QuotaAO#setUserQuotaTotal(java.lang.String,
	 * long)
	 */
	@Override
	public void setUserQuotaTotal(final String userName, final long quotaValue) throws JargonException {

		log.info("setUserQuotaTotal()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (quotaValue < 0) {
			throw new IllegalArgumentException("quotaValue is null or empty");
		}

		log.info("userName:{}", userName);
		log.info("quotaValue:{}", quotaValue);

		GeneralAdminInp adminPI = GeneralAdminInp.instanceForSetUserQuotaTotal(userName, quotaValue);
		log.debug("executing admin PI");
		getIRODSProtocol().irodsFunction(adminPI);
		log.info("quota set");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.QuotaAO#setUserGroupQuotaTotal(java.lang.String ,
	 * long)
	 */
	@Override
	public void setUserGroupQuotaTotal(final String userGroupName, final long quotaValue) throws JargonException {

		log.info("setUserGroupQuotaTotal()");

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userGroupName");
		}

		if (quotaValue <= 0) {
			throw new IllegalArgumentException("quotaValue is null or empty");
		}

		log.info("userGroupName:{}", userGroupName);
		log.info("quotaValue:{}", quotaValue);

		GeneralAdminInp adminPI = GeneralAdminInp.instanceForSetUserGroupQuotaTotal(userGroupName, quotaValue);
		log.debug("executing admin PI");
		getIRODSProtocol().irodsFunction(adminPI);
		log.info("quota set");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.QuotaAO#setUserQuotaForResource(java.lang.String ,
	 * java.lang.String, long)
	 */
	@Override
	public void setUserQuotaForResource(final String userName, final String resourceName, final long quotaValue)
			throws JargonException {

		log.info("setUserQuotaForResource()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}

		if (quotaValue <= 0) {
			throw new IllegalArgumentException("quotaValue is null or empty");
		}

		log.info("userName:{}", userName);
		log.info("resourceName:{}", resourceName);
		log.info("quotaValue:{}", quotaValue);

		GeneralAdminInp adminPI = GeneralAdminInp.instanceForSetUserQuotaForResource(userName, resourceName,
				quotaValue);
		log.debug("executing admin PI");
		getIRODSProtocol().irodsFunction(adminPI);
		log.info("quota set");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.QuotaAO#setUserGroupQuotaForResource(java.lang
	 * .String, java.lang.String, long)
	 */
	@Override
	public void setUserGroupQuotaForResource(final String userGroupName, final String resourceName,
			final long quotaValue) throws JargonException {

		// TODO: add tests when user group crud in place per [#471] add user
		// group CRUD

		log.info("setUserGroupQuotaForResource()");

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userGroupName");
		}

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}

		if (quotaValue <= 0) {
			throw new IllegalArgumentException("quotaValue is null or empty");
		}

		log.info("userGroupName:{}", userGroupName);
		log.info("resourceName:{}", resourceName);
		log.info("quotaValue:{}", quotaValue);

		GeneralAdminInp adminPI = GeneralAdminInp.instanceForSetUserGroupQuotaForResource(userGroupName, resourceName,
				quotaValue);
		log.debug("executing admin PI");
		getIRODSProtocol().irodsFunction(adminPI);
		log.info("quota set");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.QuotaAO#listQuotaForAUser(java.lang.String)
	 */
	@Override
	public List<Quota> listQuotaForAUser(final String userName) throws JargonException {
		log.info("listQuotaForAUser()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("userName:{}", userName);

		List<Quota> quota = new ArrayList<Quota>();

		AbstractAliasedQuery simpleQuery = SimpleQuery.instanceWithTwoArguments(QUOTA_FOR_USER_AND_ZONE_QUERY, userName,
				getIRODSAccount().getZone(), 0);
		IRODSQueryResultSetInterface resultSet = getSimpleQueryExecutorAO().executeSimpleQuery(simpleQuery);

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			quota.add(buildQuotaFromPerResourceResultRow(row));
		}

		return quota;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.QuotaAO#getGlobalQuotaForAUser(java.lang.String )
	 */
	@Override
	public Quota getGlobalQuotaForAUser(final String userName) throws JargonException {

		log.info("getGlobalQuotaForAUser()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("userName:{}", userName);

		Quota quota = null;

		AbstractAliasedQuery simpleQuery = SimpleQuery.instanceWithTwoArguments(QUOTA_GLOBAL_FOR_USER_AND_ZONE_QUERY,
				userName, getIRODSAccount().getZone(), 0);
		IRODSQueryResultSetInterface resultSet = getSimpleQueryExecutorAO().executeSimpleQuery(simpleQuery);

		try {
			IRODSQueryResultRow row = resultSet.getFirstResult();
			quota = buildQuotaFromGlobalResultRow(row);
			log.info("found quota:{}", quota);
		} catch (DataNotFoundException dnf) {
			log.info("no quota found, will return null");
		}

		return quota;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.QuotaAO#calculateQuotaUsage()
	 */
	@Override
	public void calculateQuotaUsage() throws JargonException {

		log.info("calculateQuotaUsage()");
		GeneralAdminInp adminPI = GeneralAdminInp.instanceForCalculateQuotaUsage();
		log.debug("executing admin PI");
		getIRODSProtocol().irodsFunction(adminPI);
		log.info("quota usage calculated");

	}

	private SimpleQueryExecutorAO getSimpleQueryExecutorAO() throws JargonException {
		if (simpleQueryExcecutor == null) {
			simpleQueryExcecutor = getIRODSAccessObjectFactory().getSimpleQueryExecutorAO(getIRODSAccount());
		}
		return simpleQueryExcecutor;
	}

	private Quota buildQuotaFromPerResourceResultRow(final IRODSQueryResultRow row) throws JargonException {
		Quota quotaValue;
		quotaValue = new Quota();
		quotaValue.setUserName(row.getColumn(0));
		quotaValue.setZoneName(row.getColumn(1));
		quotaValue.setResourceName(row.getColumn(2));
		quotaValue.setQuotaLimit(IRODSDataConversionUtil.getLongOrZeroFromIRODSValue(row.getColumn(3)));
		quotaValue.setQuotaOver(IRODSDataConversionUtil.getLongOrZeroFromIRODSValue(row.getColumn(4)));
		quotaValue.setUpdatedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(5)));
		return quotaValue;
	}

	private Quota buildQuotaFromGlobalResultRow(final IRODSQueryResultRow row) throws JargonException {
		Quota quotaValue;
		quotaValue = new Quota();
		quotaValue.setUserName(row.getColumn(0));
		quotaValue.setZoneName(row.getColumn(1));
		quotaValue.setResourceName("total");
		quotaValue.setQuotaLimit(IRODSDataConversionUtil.getLongOrZeroFromIRODSValue(row.getColumn(2)));
		quotaValue.setQuotaOver(IRODSDataConversionUtil.getLongOrZeroFromIRODSValue(row.getColumn(3)));
		quotaValue.setUpdatedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(4)));
		return quotaValue;
	}

}
