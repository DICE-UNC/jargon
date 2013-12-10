package org.irods.jargon.transfer.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.TransferItemDAO;
import org.irods.jargon.transfer.dao.domain.TransferItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author jdr0887
 * 
 */
public class TransferItemDAOImpl extends HibernateDaoSupport implements
		TransferItemDAO {

	private static final Logger log = LoggerFactory
			.getLogger(TransferItemDAOImpl.class);

	public TransferItemDAOImpl() {
		super();
	}

	@Override
	public void save(final TransferItem transferItem)
			throws TransferDAOException {

		try {
			getSessionFactory().getCurrentSession().saveOrUpdate(transferItem);
		} catch (Exception e) {

			log.error("error in save(TransferItem)", e);
			throw new TransferDAOException("Failed save(TransferItem)", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TransferItem> findErrorItemsByTransferAttemptId(final Long id)
			throws TransferDAOException {
		log.debug("entering findErrorItemsByTransferAttemptId(Long)");

		try {
			Criteria criteria = getSessionFactory().getCurrentSession()
					.createCriteria(TransferItem.class);
			criteria.add(Restrictions.eq("error", true));
			criteria.createCriteria("transferAttempt").add(
					Restrictions.eq("id", id));
			criteria.addOrder(Order.asc("transferredAt"));
			return criteria.list();
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findErrorItemsByTransferAttemptId(Long)", e);
			throw new TransferDAOException(
					"Failed findErrorItemsByTransferAttemptId(Long)", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TransferItem> findAllItemsForTransferByTransferAttemptId(
			final Long id) throws TransferDAOException {
		log.debug("entering findAllItemsForTransferByTransferAttemptId(Long)");

		try {
			Criteria criteria = getSessionFactory().getCurrentSession()
					.createCriteria(TransferItem.class);
			criteria.createCriteria("transferAttempt").add(
					Restrictions.eq("id", id));
			criteria.addOrder(Order.asc("transferredAt"));
			return criteria.list();
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findAllItemsForTransferByTransferId(Long)", e);
			throw new TransferDAOException(
					"Failed findAllItemsForTransferByTransferAttemptId(Long)",
					e);
		}
	}

	@Override
	public TransferItem findById(final Long id) throws TransferDAOException {
		logger.debug("entering findById(Long)");

		try {
			Criteria criteria = getSessionFactory().getCurrentSession()
					.createCriteria(TransferItem.class);
			return (TransferItem) criteria.uniqueResult();
		} catch (DataAccessResourceFailureException e) {
			throw new TransferDAOException(e);
		} catch (HibernateException e) {
			throw new TransferDAOException(e);
		} catch (IllegalStateException e) {
			throw new TransferDAOException(e);
		}
	}

	@Override
	public void delete(final TransferItem transferItem)
			throws TransferDAOException {
		logger.debug("entering delete(TransferItem)");

		try {

			getSessionFactory().getCurrentSession().delete(transferItem);

		} catch (Exception e) {

			log.error("error in delete(TransferItem)", e);
			throw new TransferDAOException("Failed delete(TransferItem)", e);
		}
	}

}
