package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.irods.jargon.transfer.dao.SynchronizationDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author Mike Conway
 * 
 */
public class SynchronizationDAOImpl extends HibernateDaoSupport implements
		SynchronizationDAO {

	private static final Logger log = LoggerFactory
			.getLogger(SynchronizationDAOImpl.class);

	public SynchronizationDAOImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.SynchronizationDAO#purgeSynchronizations()
	 */
	@Override
	public void purgeSynchronizations() throws TransferDAOException {
		try {

			StringBuilder sb = new StringBuilder();
			sb.append("delete from Synchronization");

			log.debug("delete synchronization sql:{}", sb.toString());

			HibernateTemplate hibernateTemplate = super.getHibernateTemplate();

			int rows = hibernateTemplate.bulkUpdate(sb.toString());
			log.debug("deleted synchs count of: {}", rows);

		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in purgeSynchronizations()", e);
			throw new TransferDAOException("Failed purgeSynchronizations()", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.AccountDAO#save(org.irods.jargon.transfer
	 * .dao.domain.Account)
	 */
	@Override
	public void save(final Synchronization synchronization)
			throws TransferDAOException {
		logger.debug("entering save(Synchronization)");
		try {
			if (synchronization.getId() == null) {
				log.info("not persisted yet, saving");
				this.getSessionFactory().getCurrentSession()
						.saveOrUpdate(synchronization);
			} else {
				log.info("already persisted, merging");
				this.getSessionFactory().getCurrentSession()
						.merge(synchronization);
			}
		} catch (Exception e) {

			log.error("error in save(synchronization)", e);
			throw new TransferDAOException("Failed save(synchronization)", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.AccountDAO#findByName(java.lang.String)
	 */
	@Override
	public Synchronization findByName(final String name)
			throws TransferDAOException {
		logger.debug("entering findByName(String)");
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("null or empty name");
		}
		log.info("findByName name:{}", name);
		try {
			Criteria criteria = this.getSessionFactory().getCurrentSession()
					.createCriteria(Synchronization.class);
			criteria.add(Restrictions.eq("name", name));
			return (Synchronization) criteria.uniqueResult();

		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error", e);
			throw new TransferDAOException("exception in findByName", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.AccountDAO#findAll()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Synchronization> findAll() throws TransferDAOException {
		logger.debug("entering findAll()");
		List<Synchronization> ret = null;
		Session session = this.getSessionFactory().getCurrentSession();
		try {
			Criteria criteria = session.createCriteria(Synchronization.class);
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
	 * @see org.irods.jargon.transfer.dao.AccountDAO#findById(java.lang.Long)
	 */
	@Override
	public Synchronization findById(final Long id) throws TransferDAOException {
		logger.debug("entering findById(Long)");
		Synchronization ret = null;
		Session session = this.getSessionFactory().getCurrentSession();
		try {
			Criteria criteria = session.createCriteria(Synchronization.class);
			criteria.add(Restrictions.eq("id", id));
			ret = (Synchronization) criteria.uniqueResult();
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
	 * org.irods.jargon.transfer.dao.AccountDAO#delete(org.irods.jargon.transfer
	 * .dao.domain.Account)
	 */
	@Override
	public void delete(final Synchronization synchronization)
			throws TransferDAOException {
		logger.debug("entering delete(Synchronization)");

		try {

			this.getSessionFactory().getCurrentSession()
					.delete(synchronization);

		} catch (Exception e) {

			log.error("error in delete(synchronization)", e);
			throw new TransferDAOException("Failed delete(synchronization)", e);
		}
	}

}
