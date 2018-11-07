/**
 *
 */
package org.irods.jargon.datautils.avuautocomplete;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryOrderByField.OrderByType;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.service.AbstractJargonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - NIEHS
 *
 */
public class AvuAutocompleteServiceImpl extends AbstractJargonService implements AvuAutocompleteService {

	public static final Logger log = LoggerFactory.getLogger(AvuAutocompleteServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 */
	public AvuAutocompleteServiceImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	public AvuAutocompleteServiceImpl() {
	}

	@Override
	public AvuSearchResult gatherAvailableAttributes(final String prefix, final int offset,
			final AvuTypeEnum avuTypeEnum) throws JargonException {

		log.info("gatherAvailableAttributes()");

		if (prefix == null) {
			throw new IllegalArgumentException("null prefix");
		}

		if (offset < 0) {
			throw new IllegalArgumentException("offset must be >= 0");
		}

		if (avuTypeEnum == null) {
			throw new IllegalArgumentException("null avuTypeEnum");
		}
		log.info("prefix:{}", prefix);
		log.info("offset:{}", offset);
		log.info("avuTypeEnum:{}", avuTypeEnum);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, true, null); // distinct, case insensitive
		IRODSQueryResultSetInterface resultSet;
		AvuSearchResult result = new AvuSearchResult();

		try {
			if (avuTypeEnum == AvuTypeEnum.COLLECTION) {

				builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME).addConditionAsGenQueryField(
						RodsGenQueryEnum.COL_META_COLL_ATTR_NAME, QueryConditionOperators.LIKE, prefix);

			} else if (avuTypeEnum == AvuTypeEnum.DATA_OBJECT) {

				builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME).addConditionAsGenQueryField(
						RodsGenQueryEnum.COL_META_DATA_ATTR_NAME, QueryConditionOperators.LIKE, prefix);
			} else if (avuTypeEnum == AvuTypeEnum.BOTH) {
				// to do add query for both
				throw new UnsupportedOperationException();
			}

			IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(
					getIrodsAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax());

			IRODSGenQueryExecutor irodsGenQueryExecutor = getIrodsAccessObjectFactory()
					.getIRODSGenQueryExecutor(irodsAccount);

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, offset);

			for (IRODSQueryResultRow row : resultSet.getResults()) {
				result.getElements().add(row.getColumn(0));
			}

			result.setMore(resultSet.isHasMoreRecords());
			result.setOffset(offset);
			if (resultSet.isHasMoreRecords()) {
				result.setNextOffset(resultSet.getResults().get(resultSet.getResults().size() - 1).getRecordCount());
			}

		} catch (GenQueryBuilderException | JargonQueryException e) {
			log.error("error building query", e);
			throw new JargonException(e);
		}

		return result;
	}

	@Override
	public AvuSearchResult gatherAvailableValues(final String forAttribute, final String prefix, final int offset,
			final AvuTypeEnum avuTypeEnum) throws JargonException {
		log.info("gatherAvailableValues()");

		if (forAttribute == null) {
			throw new IllegalArgumentException("null forAttribute");
		}

		if (prefix == null) {
			throw new IllegalArgumentException("null prefix");
		}

		if (offset < 0) {
			throw new IllegalArgumentException("offset must be >= 0");
		}

		if (avuTypeEnum == null) {
			throw new IllegalArgumentException("null avuTypeEnum");
		}
		log.info("forAttribute:{}", forAttribute);
		log.info("prefix:{}", prefix);
		log.info("offset:{}", offset);
		log.info("avuTypeEnum:{}", avuTypeEnum);

		IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, true, null); // distinct, case insensitive
		IRODSQueryResultSetInterface resultSet;
		AvuSearchResult result = new AvuSearchResult();

		try {
			if (avuTypeEnum == AvuTypeEnum.COLLECTION) {

				builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE)
						.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_COLL_ATTR_NAME,
								QueryConditionOperators.LIKE, forAttribute)
						.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE,
								QueryConditionOperators.LIKE, prefix)
						.addOrderByGenQueryField(RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE, OrderByType.ASC);

			} else if (avuTypeEnum == AvuTypeEnum.DATA_OBJECT) {

				builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE)
						.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME,
								QueryConditionOperators.LIKE, forAttribute)
						.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE,
								QueryConditionOperators.LIKE, prefix)
						.addOrderByGenQueryField(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE, OrderByType.ASC);

			} else if (avuTypeEnum == AvuTypeEnum.BOTH) {

				// to do add query for both
				return null;
			}

			IRODSGenQueryFromBuilder irodsQuery = builder.exportIRODSQueryFromBuilder(
					getIrodsAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax());

			IRODSGenQueryExecutor irodsGenQueryExecutor = getIrodsAccessObjectFactory()
					.getIRODSGenQueryExecutor(irodsAccount);

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, offset);

			for (IRODSQueryResultRow row : resultSet.getResults()) {
				result.getElements().add(row.getColumn(0));
			}

			result.setMore(resultSet.isHasMoreRecords());
			result.setOffset(offset);
			if (resultSet.isHasMoreRecords()) {
				result.setNextOffset(resultSet.getResults().get(resultSet.getResults().size() - 1).getRecordCount());
			}

		} catch (GenQueryBuilderException | JargonQueryException e) {
			log.error("error building query", e);
			throw new JargonException(e);
		}

		return result;

	}

}
