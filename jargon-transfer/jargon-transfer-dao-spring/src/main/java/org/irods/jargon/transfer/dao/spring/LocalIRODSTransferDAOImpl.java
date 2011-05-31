package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.irods.jargon.transfer.dao.LocalIRODSTransferDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author jdr0887
 * 
 */
public class LocalIRODSTransferDAOImpl extends HibernateDaoSupport implements
		LocalIRODSTransferDAO {

	private static final Logger log = LoggerFactory
			.getLogger(LocalIRODSTransferDAOImpl.class);

	public LocalIRODSTransferDAOImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#save(org.irods.jargon
	 * .transfer.dao.domain.LocalIRODSTransfer)
	 */
	@Override
	public void save(final LocalIRODSTransfer localIRODSTransfer)
			throws TransferDAOException {
		logger.info("entering save(LocalIRODSTransfer)");
		this.getSessionFactory().getCurrentSession()
				.saveOrUpdate(localIRODSTransfer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findById(java.lang
	 * .Long)
	 */
	@Override
	public LocalIRODSTransfer findById(final Long id)
			throws TransferDAOException {
		logger.debug("entering findById(Long)");
		return (LocalIRODSTransfer) this.getSessionFactory()
				.getCurrentSession().get(LocalIRODSTransfer.class, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findInitializedById
	 * (java.lang.Long)
	 */
	@Override
	public LocalIRODSTransfer findInitializedById(final Long id)
			throws TransferDAOException {
		logger.debug("entering findInitializedById(Long)");
		LocalIRODSTransfer localIrodsTransfer = (LocalIRODSTransfer) this
				.getSessionFactory().getCurrentSession()
				.get(LocalIRODSTransfer.class, id);
		Hibernate.initialize(localIrodsTransfer);
		return localIrodsTransfer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findByTransferState
	 * (org.irods.jargon.transfer.dao.domain.TransferState[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<LocalIRODSTransfer> findByTransferState(
			final TransferState... transferState) throws TransferDAOException {
		log.debug("entering findByTransferState(TransferState...)");

		try {
			Criteria criteria = this.getSessionFactory().getCurrentSession()
					.createCriteria(LocalIRODSTransfer.class);
			criteria.add(Restrictions.in("transferState", transferState));
			criteria.addOrder(Order.desc("transferStart"));
			return criteria.list();
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findByTransferState(TransferState...)", e);
			throw new TransferDAOException(
					"Failed findByTransferState(TransferState...)", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findByTransferState
	 * (int, org.irods.jargon.transfer.dao.domain.TransferState[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<LocalIRODSTransfer> findByTransferState(final int maxResults,
			final TransferState... transferState) throws TransferDAOException {
		log.debug("entering findByTransferState(int, TransferState...)");
		try {
			Criteria criteria = this.getSessionFactory().getCurrentSession()
					.createCriteria(LocalIRODSTransfer.class);
			criteria.add(Restrictions.in("transferState", transferState));
			criteria.setMaxResults(maxResults);
			criteria.addOrder(Order.desc("transferStart"));
			return criteria.list();

		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findByTransferState(int, TransferState...)", e);
			throw new TransferDAOException(
					"Failed findByTransferState(int, TransferState...)", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findByTransferStatus
	 * (int, org.irods.jargon.transfer.dao.domain.TransferStatus[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<LocalIRODSTransfer> findByTransferStatus(final int maxResults,
			final TransferStatus... transferStatus) throws TransferDAOException {
		log.debug("entering findByTransferState(int, TransferStatus...)");

		try {
			Criteria criteria = this.getSessionFactory().getCurrentSession()
					.createCriteria(LocalIRODSTransfer.class);
			criteria.add(Restrictions.in("transferStatus", transferStatus));
			criteria.setMaxResults(maxResults);
			criteria.addOrder(Order.desc("transferStart"));
			return criteria.list();
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findByTransferState(int, TransferStatus...)", e);
			throw new TransferDAOException(
					"Failed findByTransferState(int, TransferStatus...)", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findAllSortedDesc
	 * (int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<LocalIRODSTransfer> findAllSortedDesc(final int maxResults)
			throws TransferDAOException {

		try {
			Criteria criteria = this.getSessionFactory().getCurrentSession()
					.createCriteria(LocalIRODSTransfer.class);
			criteria.setMaxResults(maxResults);
			criteria.addOrder(Order.desc("transferStart"));
			return criteria.list();
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findAllSortedDesc(int)", e);
			throw new TransferDAOException("Failed findAllSortedDesc(int)", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findAll()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<LocalIRODSTransfer> findAll() throws TransferDAOException {
		log.debug("entering findAll()");
		try {
			Criteria criteria = this.getSessionFactory().getCurrentSession()
					.createCriteria(LocalIRODSTransfer.class);
			criteria.addOrder(Order.desc("transferStart"));
			return criteria.list();
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findAll()", e);
			throw new TransferDAOException("Failed findAll()", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#purgeQueue()
	 */
	@Override
	public void purgeQueue() throws TransferDAOException {
		log.debug("entering purgeQueue()");

		try {

			StringBuilder sb = new StringBuilder();
			sb.append("delete from LocalIRODSTransferItem as item where ");
			sb.append("item.localIRODSTransfer.id in (");
			sb.append("select id from LocalIRODSTransfer as transfer where transfer.transferState <> ?)");

			log.debug("delete items sql:{}", sb.toString());

			HibernateTemplate hibernateTemplate = super.getHibernateTemplate();

			int rows = hibernateTemplate.bulkUpdate(sb.toString(),
					TransferState.PROCESSING);
			log.debug("deleted items count of: {}", rows);

			sb = new StringBuilder();
			sb.append("delete from LocalIRODSTransfer  where transferState <> ?");

			log.debug("delete items sql:{}", sb.toString());

			rows = super.getHibernateTemplate().bulkUpdate(sb.toString(),
					TransferState.PROCESSING);
			log.debug("deleted transfers count of: {}", rows);

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
	 * @see
	 * org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#purgeSuccessful()
	 */
	@Override
	public void purgeSuccessful() throws TransferDAOException {
		log.debug("entering purgeSuccessful()");

		try {

			StringBuilder sb = new StringBuilder();
			sb.append("delete from LocalIRODSTransferItem as item where ");
			sb.append("item.localIRODSTransfer.id in (");
			sb.append("select id from LocalIRODSTransfer as transfer where transferState = ? or transferState = ? and transferStatus = ?)");

			log.debug("delete transfer items sql:{}", sb.toString());

			int rows = super.getHibernateTemplate().bulkUpdate(
					sb.toString(),
					new Object[] { TransferState.COMPLETE,
							TransferState.CANCELLED, TransferStatus.OK });
			log.debug("deleted items count= {}", rows);

			sb = new StringBuilder();
			sb.append("delete from LocalIRODSTransfer  where transferState = ? or transferState = ? and transferStatus = ?");

			log.debug("delete transfers sql:{}", sb.toString());

			rows = super.getHibernateTemplate().bulkUpdate(
					sb.toString(),
					new Object[] { TransferState.COMPLETE,
							TransferState.CANCELLED, TransferStatus.OK });

			log.debug("deleted transfers count= {}", rows);

		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (DataAccessException e) {

			log.error("error in purgeSuccessful()", e);
			throw new TransferDAOException("Failed purgeSuccessful()", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#delete(org.irods.
	 * jargon.transfer.dao.domain.LocalIRODSTransfer)
	 */
	@Override
	public void delete(final LocalIRODSTransfer localIRODSTransfer)
			throws TransferDAOException {
		logger.debug("entering delete()");

		try {

			this.getSessionFactory().getCurrentSession()
					.delete(localIRODSTransfer);
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {

			log.error("error in delete(LocalIRODSTransfer entity)", e);
			throw new TransferDAOException(
					"Failed delete(LocalIRODSTransfer entity)", e);
		}
	}

}
