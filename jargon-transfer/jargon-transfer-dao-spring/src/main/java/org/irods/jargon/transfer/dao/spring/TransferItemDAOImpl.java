package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.irods.jargon.transfer.dao.TransferItemDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
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
public class TransferItemDAOImpl extends HibernateDaoSupport
		implements TransferItemDAO {

	private static final Logger log = LoggerFactory
			.getLogger(TransferItemDAOImpl.class);

	public TransferItemDAOImpl() {
		super();
	}

	@Override
	public void save(final TransferItem localIRODSTransferItem)
			throws TransferDAOException {

		try {
			this.getSessionFactory().getCurrentSession()
					.saveOrUpdate(localIRODSTransferItem);
		} catch (Exception e) {

			log.error("error in save(LocalIRODSTransferItem)", e);
			throw new TransferDAOException(
					"Failed save(LocalIRODSTransferItem)", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TransferItem> findErrorItemsByTransferId(final Long id)
			throws TransferDAOException {
		log.debug("entering findErrorItemsByTransferId(Long)");

		try {
			Criteria criteria = this.getSessionFactory().getCurrentSession()
					.createCriteria(TransferItem.class);
			criteria.add(Restrictions.eq("error", true));
			criteria.createCriteria("localIRODSTransfer").add(
					Restrictions.eq("id", id));
			criteria.addOrder(Order.asc("transferredAt"));
			return criteria.list();
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findErrorItemsByTransferId(Long)", e);
			throw new TransferDAOException(
					"Failed findErrorItemsByTransferId(Long)", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TransferItem> findAllItemsForTransferByTransferId(
			final Long id) throws TransferDAOException {
		log.debug("entering findAllItemsForTransferByTransferId(Long)");

		try {
			Criteria criteria = this.getSessionFactory().getCurrentSession()
					.createCriteria(TransferItem.class);
			criteria.createCriteria("localIRODSTransfer").add(
					Restrictions.eq("id", id));
			criteria.addOrder(Order.asc("transferredAt"));
			return criteria.list();
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findAllItemsForTransferByTransferId(Long)", e);
			throw new TransferDAOException(
					"Failed findAllItemsForTransferByTransferId(Long)", e);
		}
	}

	@Override
	public TransferItem findById(final Long id)
			throws TransferDAOException {
		logger.debug("entering findById(Long)");

		try {
			Criteria criteria = this.getSessionFactory().getCurrentSession()
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
	public void delete(final TransferItem localIrodsTransferItem)
			throws TransferDAOException {
		logger.debug("entering delete(LocalIRODSTransferItem)");

		try {

			this.getSessionFactory().getCurrentSession()
					.delete(localIrodsTransferItem);

		} catch (Exception e) {

			log.error("error in delete(LocalIRODSTransferItem)", e);
			throw new TransferDAOException(
					"Failed delete(LocalIRODSTransferItem)", e);
		}
	}

}
