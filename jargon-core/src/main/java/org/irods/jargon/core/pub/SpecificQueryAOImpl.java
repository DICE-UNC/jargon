package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.GeneralAdminInpForSQ;
import org.irods.jargon.core.pub.domain.SpecificQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SpecificQueryAOImpl extends IRODSGenericAO implements SpecificQueryAO {
	
	private static final String EXECUTING_SQUERY_PI = "executing specific query PI";
	public static final Logger log = LoggerFactory
	.getLogger(SpecificQueryAOImpl.class);

	protected SpecificQueryAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.accessobject.SpecificQueryAO#addSpecificQuery(org.irods.jargon.core
	 * .domain.SpecificQuery)
	 */
	@Override
	public void addSpecificQuery(SpecificQuery specificQuery)
			throws JargonException, DuplicateDataException {
		GeneralAdminInpForSQ queryPI;

		if (specificQuery == null) {
			throw new IllegalArgumentException(
					"cannot create specific query with null SpecificQuery object");
		}

		log.info("creating specific query: {}", specificQuery);

		try {
			queryPI = GeneralAdminInpForSQ.instanceForAddSpecificQuery(specificQuery);
		} catch (JargonException je) {
			throw je;
		}
		log.info(EXECUTING_SQUERY_PI);

		try {
			getIRODSProtocol().irodsFunction(queryPI);
		} catch (DuplicateDataException dde) {
			throw dde;
		} catch (JargonException je) {
			throw je;
		}

		log.info("added specific query");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.accessobject.SpecificQueryAO#removeSpecificQuery(org.irods.jargon.core
	 * .domain.SpecificQuery)
	 */
	@Override
	public void removeSpecificQuery(SpecificQuery specificQuery)
			throws JargonException {
		GeneralAdminInpForSQ queryPI;

		if (specificQuery == null) {
			throw new IllegalArgumentException(
					"cannot remove specific query with null SpecificQuery object");
		}

		log.info("removing specific query: {}", specificQuery);

		try {
			queryPI = GeneralAdminInpForSQ.instanceForRemoveSpecificQuery(specificQuery);
		} catch (JargonException je) {
			throw je;
		}
		log.info(EXECUTING_SQUERY_PI);

		try {
			getIRODSProtocol().irodsFunction(queryPI);
		} catch (JargonException je) {
			throw je;
		}

		log.info("removed specific query");
		
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.accessobject.SpecificQueryAO#removeSpecificQueryByAlias(String)
	 */
	@Override
	public void removeSpecificQueryByAlias(String alias)
			throws JargonException, DuplicateDataException {
		GeneralAdminInpForSQ queryPI;

		if (alias == null) {
			throw new IllegalArgumentException(
					"cannot remove specific query with null alias");
		}

		log.info("removing specific query by alias: {}", alias);

		try {
			queryPI = GeneralAdminInpForSQ.instanceForRemoveSpecificQueryByAlias(alias);
		} catch (JargonException je) {
			throw je;
		}
		log.info(EXECUTING_SQUERY_PI);

		try {
			getIRODSProtocol().irodsFunction(queryPI);
		} catch (JargonException je) {
			throw je;
		}

		log.info("removed specific query");
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.accessobject.SpecificQueryAO#removeAllSpecificQueryBySQL(String)
	 */
	@Override
	public void removeAllSpecificQueryBySQL(String sqlQuery)
			throws JargonException, DuplicateDataException {
		GeneralAdminInpForSQ queryPI;

		if (sqlQuery == null) {
			throw new IllegalArgumentException(
					"cannot remove specific query with null SQL query");
		}

		log.info("removing all specific queries by sql query: {}", sqlQuery);

		try {
			queryPI = GeneralAdminInpForSQ.instanceForRemoveAllSpecificQueryBySQL(sqlQuery);
		} catch (JargonException je) {
			throw je;
		}
		log.info(EXECUTING_SQUERY_PI);

		try {
			getIRODSProtocol().irodsFunction(queryPI);
		} catch (JargonException je) {
			throw je;
		}

		log.info("removed specific query");
		
	}

}
