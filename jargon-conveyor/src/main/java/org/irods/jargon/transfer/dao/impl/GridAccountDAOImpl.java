package org.irods.jargon.transfer.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.irods.jargon.transfer.dao.GridAccountDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
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
public class GridAccountDAOImpl extends HibernateDaoSupport implements
		GridAccountDAO {

	private static final Logger log = LoggerFactory
			.getLogger(GridAccountDAOImpl.class);

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

		getSessionFactory().getCurrentSession().saveOrUpdate(gridAccount);
		logger.info("update successful");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.GridAccountDAO#findAll()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<GridAccount> findAll() throws TransferDAOException {
		logger.debug("entering findAll()");
		List<GridAccount> ret = null;
		Session session = getSessionFactory().getCurrentSession();
		try {
			Criteria criteria = session.createCriteria(GridAccount.class);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			ret = criteria.list();
		} catch (Exception e) {
			logger.error("error in findAll()", e);
			throw new TransferDAOException("Failed findAll()", e);
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.GridAccountDAO#findById(java.lang.Long)
	 */
	@Override
	public GridAccount findById(final Long id) throws TransferDAOException {
		logger.debug("entering findById(Long)");

		if (id == null) {
			throw new IllegalArgumentException("null id");
		}

		GridAccount ret = null;
		Session session = getSessionFactory().getCurrentSession();
		try {
			Criteria criteria = session.createCriteria(GridAccount.class);
			criteria.add(Restrictions.eq("id", id));
			ret = (GridAccount) criteria.uniqueResult();
		} catch (Exception e) {
			logger.error("error in findById(Long)", e);
			throw new TransferDAOException("Failed findById(Long)", e);
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.GridAccountDAO#findByHostZoneAndUserName
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public GridAccount findByHostZoneAndUserName(final String host,
			final String zone, final String userName)
			throws TransferDAOException {
		logger.info("findByHostZoneAndUserName()");

		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("host is null or empty");
		}

		if (zone == null || zone.isEmpty()) {
			throw new IllegalArgumentException("zone is null or empty");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("userName is null or empty");
		}

		GridAccount ret = null;
		Session session = getSessionFactory().getCurrentSession();
		try {
			Criteria criteria = session.createCriteria(GridAccount.class);
			criteria.add(Restrictions.eq("host", host))
					.add(Restrictions.eq("zone", zone))
					.add(Restrictions.eq("userName", userName));
			ret = (GridAccount) criteria.uniqueResult();
		} catch (Exception e) {
			logger.error("error in query", e);
			throw new TransferDAOException("error in find query", e);
		}

		return ret;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.GridAccountDAO#deleteGridAccount(org.irods
	 * .jargon.transfer.dao.domain.GridAccount)
	 */
	@Override
	public void deleteGridAccount(final GridAccount gridAccount)
			throws TransferDAOException {
		log.debug("entering deleteGridAccount()");

		if (gridAccount == null) {
			throw new IllegalArgumentException("null gridAccount");
		}

		log.info("gridAccount:{}", gridAccount);

		try {
			Session session = getSessionFactory().getCurrentSession();
			GridAccount toDelete = (GridAccount) session.merge(gridAccount);
			session.delete(toDelete);
			log.info("deleted");
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in purgeQueue()", e);
			throw new TransferDAOException("Failed purgeQueue()", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.GridAccountDAO#deleteAll()
	 */
	@Override
	public void deleteAll() throws TransferDAOException {
		try {

			StringBuilder sb = new StringBuilder();
			sb.append("delete from GridAccount");

			log.debug("delete grid account sql:{}", sb.toString());

			HibernateTemplate hibernateTemplate = super.getHibernateTemplate();

			int rows = hibernateTemplate.bulkUpdate(sb.toString());
			log.debug("deleted grid accounts count of: {}", rows);

		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in deleteAll()", e);
			throw new TransferDAOException("Failed deleteAll()", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.GridAccountDAO#delete(org.irods.jargon.
	 * transfer.dao.domain.GridAccount)
	 */
	@Override
	public void delete(final GridAccount gridAccount)
			throws TransferDAOException {

		logger.debug("delete()");

		try {
			getSessionFactory().getCurrentSession().delete(gridAccount);
		} catch (Exception e) {
			logger.error("error in delete()", e);
			throw new TransferDAOException("Failed delete()", e);
		}
	}

}
