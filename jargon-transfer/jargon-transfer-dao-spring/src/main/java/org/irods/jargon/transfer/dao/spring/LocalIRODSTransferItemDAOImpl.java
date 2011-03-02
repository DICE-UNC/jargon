package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.irods.jargon.transfer.dao.LocalIRODSTransferItemDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransferItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author jdr0887
 * 
 */
public class LocalIRODSTransferItemDAOImpl extends HibernateDaoSupport implements LocalIRODSTransferItemDAO {

    private static final Logger log = LoggerFactory.getLogger(LocalIRODSTransferItemDAOImpl.class);

    public LocalIRODSTransferItemDAOImpl() {
        super();
    }

    @Override
    public void save(LocalIRODSTransferItem ea) throws TransferDAOException {
        logger.debug("entering save(LocalIRODSTransferItem)");
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            if (ea.getId() == null) {
                session.save(ea);
            } else {
                session.update(ea);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            log.error("error in save(LocalIRODSTransferItem)", e);
            throw new TransferDAOException("Failed save(LocalIRODSTransferItem)", e);
        } finally {
            session.flush();
            session.close();
        }
    }

    @Override
    public List<LocalIRODSTransferItem> findErrorItemsByTransferId(Long id) throws TransferDAOException {
        log.debug("entering findErrorItemsByTransferId(Long)");
        List<LocalIRODSTransferItem> ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(LocalIRODSTransferItem.class);
            criteria.add(Restrictions.eq("error", true));
            criteria.createCriteria("localIRODSTransfer").add(Restrictions.eq("id", id));
            criteria.addOrder(Order.asc("transferredAt"));
            ret = criteria.list();
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            log.error("error in findErrorItemsByTransferId(Long)", e);
            throw new TransferDAOException("Failed findErrorItemsByTransferId(Long)", e);
        } finally {
            session.close();
        }
        if (ret != null) {
            log.debug("entities found: {}", ret.size());
        }
        return ret;
    }

    @Override
    public List<LocalIRODSTransferItem> findAllItemsForTransferByTransferId(Long id) throws TransferDAOException {
        log.debug("entering findAllItemsForTransferByTransferId(Long)");
        List<LocalIRODSTransferItem> ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(LocalIRODSTransferItem.class);
            criteria.createCriteria("localIRODSTransfer").add(Restrictions.eq("id", id));
            criteria.addOrder(Order.asc("transferredAt"));
            ret = criteria.list();
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            log.error("error in findAllItemsForTransferByTransferId(Long)", e);
            throw new TransferDAOException("Failed findAllItemsForTransferByTransferId(Long)", e);
        } finally {
            session.close();
        }
        if (ret != null) {
            log.debug("entities found: {}", ret.size());
        }
        return ret;
    }

    @Override
    public LocalIRODSTransferItem findById(Long id) throws TransferDAOException {
        logger.debug("entering findById(Long)");
        LocalIRODSTransferItem ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(LocalIRODSTransferItem.class);
            criteria.add(Restrictions.eq("id", id));
            ret = (LocalIRODSTransferItem) criteria.uniqueResult();
        } catch (DataAccessResourceFailureException e) {
            e.printStackTrace();
        } catch (HibernateException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return ret;
    }

    @Override
    public void delete(LocalIRODSTransferItem ea) throws TransferDAOException {
        logger.debug("entering delete(LocalIRODSTransferItem)");
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(ea);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            log.error("error in delete(LocalIRODSTransferItem)", e);
            throw new TransferDAOException("Failed delete(LocalIRODSTransferItem)", e);
        } finally {
            session.close();
        }
    }

}
