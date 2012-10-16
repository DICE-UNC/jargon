
package org.irods.jargon.transfer.dao.spring;

import org.irods.jargon.transfer.dao.GridAccountDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * DAO for <code>GridAccount</code> managing a cache of iRODS accounts and
 * related configuration.
 * <p/>
 * The <code>GridAccount</code> preserves account information for transfers and
 * synchs, and also allows preserving and automatically logging in to remembered
 * grids. Note that this uses a scheme of encrypted passwords based on a global
 * 'pass phrase' which must be provided for the various operations. In this way,
 * passwords are always encrypted for all operations.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class GridAccountDAOImpl extends HibernateDaoSupport implements GridAccountDAO {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.GridAccountDAO#save(org.irods.jargon.transfer
	 * .dao.domain.GridAccount)
	 */
	@Override
	public void save(final GridAccount gridAccount) throws TransferDAOException {
		logger.info("save()");

		if (gridAccount == null) {
			throw new IllegalArgumentException("null gridAccount");
		}

		this.getSessionFactory().getCurrentSession().saveOrUpdate(gridAccount);
		logger.info("update successful");

	}


}
