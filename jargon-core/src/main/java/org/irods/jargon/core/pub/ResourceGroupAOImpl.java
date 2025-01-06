/**
 *
 */
package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.AbstractIRODSQueryResultSet;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryOrderByField.OrderByType;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Access object that represents resource groups and related operations in
 * iRODS.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ResourceGroupAOImpl extends IRODSGenericAO implements ResourceGroupAO {

	private static Logger log = LogManager.getLogger(ResourceGroupAOImpl.class);

	/**
	 * @param irodsSession
	 *            {@link IRODSSession}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @throws JargonException
	 *             for iRODS error
	 */
	protected ResourceGroupAOImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.ResourceGroupAO#listResourceGroupNames()
	 */
	@Override
	public List<String> listResourceGroupNames() throws JargonException {

		List<String> resourceNames = new ArrayList<String>();

		if (getIRODSServerProperties().isAtLeastIrods410()) {
			log.info("resource groups are not supported post iRODS4, simply list parent resources");
			ResourceAO resourceAO = getIRODSAccessObjectFactory().getResourceAO(getIRODSAccount());
			return resourceAO.listResourceNames();
		}

		AbstractIRODSQueryResultSet resultSet = null;
		try {
			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_RESC_GROUP_NAME)
					.addOrderByGenQueryField(RodsGenQueryEnum.COL_RESC_GROUP_NAME, OrderByType.ASC);

			IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
					.getIRODSGenQueryExecutor(getIRODSAccount());

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(builder.exportIRODSQueryFromBuilder(
					getIRODSAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax()), 0);
		} catch (JargonQueryException e) {
			log.error("jargon query exception getting results", e);
			throw new JargonException(e);
		} catch (GenQueryBuilderException e) {
			log.error("jargon query exception getting results", e);
			throw new JargonException(e);
		}

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			resourceNames.add(row.getColumn(0));
		}

		return resourceNames;

	}

}
