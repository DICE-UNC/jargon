package org.irods.jargon.transfer.dao.spring;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.irods.jargon.transfer.dao.KeyStoreDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.KeyStore;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * DAO for <code>KeyStore</code> managing the stored 'pass phrase' for the
 * transfer database.
 * <p/>
 * The <code>GridAccount</code> preserves account information for transfers and
 * synchs, and also allows preserving and automatically logging in to remembered
 * grids. Note that this uses a scheme of encrypted passwords based on a global
 * 'pass phrase' which must be provided for the various operations. In this way,
 * passwords are always encrypted for all operations.
 * <p/>
 * This <code>KeyStore</code> holds a hash of the pass phrase used by the
 * transfer manager user, and can verify the correct pass phrase. Note the
 * actual pass phrase, and any unencrypted password information, is not found in
 * the transfer database.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class KeyStoreDAOImpl extends HibernateDaoSupport implements KeyStoreDAO {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.KeyStoreDAO#save(org.irods.jargon.transfer
	 * .dao.domain.KeyStore)
	 */
	@Override
	public void save(final KeyStore keyStore) throws TransferDAOException {
		logger.info("save()");

		if (keyStore == null) {
			throw new IllegalArgumentException("null keyStore");
		}

		try {
			this.getSessionFactory().getCurrentSession().saveOrUpdate(keyStore);
		} catch (Exception e) {
			logger.error("error in save()", e);
			throw new TransferDAOException("Failed saveOrUpdate()", e);
		}

		logger.info("update successful");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.KeyStoreDAO#findById(java.lang.String)
	 */
	@Override
	public KeyStore findById(final String id) throws TransferDAOException {
		logger.debug("entering findById()");

		if (id == null) {
			throw new IllegalArgumentException("null id");
		}

		KeyStore ret = null;
		Session session = this.getSessionFactory().getCurrentSession();
		try {
			Criteria criteria = session.createCriteria(KeyStore.class);
			criteria.add(Restrictions.eq("id", id));
			ret = (KeyStore) criteria.uniqueResult();
		} catch (Exception e) {
			logger.error("error in findById()", e);
			throw new TransferDAOException("Failed findById()", e);
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.KeyStoreDAO#delete(org.irods.jargon.transfer
	 * .dao.domain.KeyStore)
	 */
	@Override
	public void delete(final KeyStore keyStore) throws TransferDAOException {

		logger.debug("delete()");

		if (keyStore == null) {
			throw new IllegalArgumentException("null keyStore");
		}

		try {
			this.getSessionFactory().getCurrentSession().delete(keyStore);
		} catch (Exception e) {
			logger.error("error in delete()", e);
			throw new TransferDAOException("Failed delete()", e);
		}
	}

}
