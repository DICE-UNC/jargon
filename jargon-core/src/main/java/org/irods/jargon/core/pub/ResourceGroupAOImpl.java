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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access object that represents resource groups and related operations in
 * iRODS.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ResourceGroupAOImpl extends IRODSGenericAO implements
		ResourceGroupAO {

	private static Logger log = LoggerFactory
			.getLogger(ResourceGroupAOImpl.class);

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected ResourceGroupAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
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

		AbstractIRODSQueryResultSet resultSet = null;
		try {
			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
			builder.addSelectAsGenQueryValue(
					RodsGenQueryEnum.COL_RESC_GROUP_NAME)
					.addOrderByGenQueryField(
							RodsGenQueryEnum.COL_RESC_GROUP_NAME,
							OrderByType.ASC);

			IRODSGenQueryExecutor irodsGenQueryExecutor = getIRODSAccessObjectFactory()
					.getIRODSGenQueryExecutor(getIRODSAccount());

			resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResult(
							builder.exportIRODSQueryFromBuilder(getIRODSAccessObjectFactory()
									.getJargonProperties()
									.getMaxFilesAndDirsQueryMax()), 0);
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
