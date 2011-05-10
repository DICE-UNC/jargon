package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author jdr0887
 * 
 */
public class LocalIRODSTransferDAOImpl extends HibernateDaoSupport implements LocalIRODSTransferDAO {

    private static final Logger log = LoggerFactory.getLogger(LocalIRODSTransferDAOImpl.class);

    public LocalIRODSTransferDAOImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#save(org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer)
     */
    @Override
    public void save(LocalIRODSTransfer ea) throws TransferDAOException {
        logger.debug("entering save(LocalIRODSTransfer)");
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
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            log.error("error in save(LocalIRODSTransfer)", e);
            throw new TransferDAOException("Failed save(LocalIRODSTransfer)", e);
        } finally {
            session.flush();
            session.close();
        }
    }

    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findById(java.lang.Long)
     */
    @Override
    public LocalIRODSTransfer findById(Long id) throws TransferDAOException {
        logger.debug("entering findById(Long)");
        LocalIRODSTransfer ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(LocalIRODSTransfer.class);
            criteria.add(Restrictions.eq("id", id));
            ret = (LocalIRODSTransfer) criteria.uniqueResult();
        } catch (Exception e) {
            log.error("error in findById(Long)", e);
            throw new TransferDAOException("Failed findById(Long)", e);
        } finally {
            session.close();
        }
        return ret;
    }

    
    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findInitializedById(java.lang.Long)
     */
    @Override
    public LocalIRODSTransfer findInitializedById(Long id) throws TransferDAOException {
        logger.debug("entering findInitializedById(Long)");
        LocalIRODSTransfer ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(LocalIRODSTransfer.class);
            criteria.add(Restrictions.eq("id", id));
            ret = (LocalIRODSTransfer) criteria.uniqueResult();
            if (ret != null) {
                Hibernate.initialize(ret.getLocalIRODSTransferItems());
            }
        } catch (Exception e) {
            log.error("error in findById(Long)", e);
            throw new TransferDAOException("Failed findById(Long)", e);
        } finally {
            session.close();
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findById(java.lang.Long, boolean)
     */
    @Override
    public LocalIRODSTransfer findById(Long id, boolean error) throws TransferDAOException {
        logger.debug("entering findById(Long, boolean)");
        LocalIRODSTransfer ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(LocalIRODSTransfer.class);
            criteria.add(Restrictions.eq("id", id));
            criteria.createCriteria("localIRODSTransferItems").add(Restrictions.eq("error", true));
            criteria.addOrder(Order.asc("createdAt"));
            ret = (LocalIRODSTransfer) criteria.uniqueResult();
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            log.error("error in findById(Long id)", e);
            throw new TransferDAOException("Failed findById(Long id)", e);
        } finally {
            session.close();
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findInitializedById(java.lang.Long, boolean)
     */
    @Override
    public LocalIRODSTransfer findInitializedById(Long id, boolean error) throws TransferDAOException {
        logger.debug("entering findById(Long, boolean)");
        LocalIRODSTransfer ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(LocalIRODSTransfer.class);
            criteria.add(Restrictions.eq("id", id));
            criteria.createCriteria("localIRODSTransferItems").add(Restrictions.eq("error", true));
            criteria.addOrder(Order.asc("createdAt"));
            ret = (LocalIRODSTransfer) criteria.uniqueResult();
            if (ret != null) {
            Hibernate.initialize(ret.getLocalIRODSTransferItems());
            }
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            log.error("error in findById(Long id)", e);
            throw new TransferDAOException("Failed findById(Long id)", e);
        } finally {
            session.close();
        }
        return ret;
    }
    
    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findByTransferState(org.irods.jargon.transfer.dao.domain.TransferState[])
     */
    @Override
    public List<LocalIRODSTransfer> findByTransferState(TransferState... transferState) throws TransferDAOException {
        log.debug("entering findByTransferState(TransferState...)");
        List<LocalIRODSTransfer> ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(LocalIRODSTransfer.class);
            criteria.add(Restrictions.in("transferState", transferState));
            criteria.addOrder(Order.desc("transferStart"));
            ret = criteria.list();
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            log.error("error in findByTransferState(TransferState...)", e);
            throw new TransferDAOException("Failed findByTransferState(TransferState...)", e);
        } finally {
            session.close();
        }
        if (ret != null) {
            log.debug("entities found: {}", ret.size());
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findByTransferState(int, org.irods.jargon.transfer.dao.domain.TransferState[])
     */
    @Override
    public List<LocalIRODSTransfer> findByTransferState(int maxResults, TransferState... transferState)
            throws TransferDAOException {
        log.debug("entering findByTransferState(int, TransferState...)");
        List<LocalIRODSTransfer> ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(LocalIRODSTransfer.class);
            criteria.add(Restrictions.in("transferState", transferState));
            criteria.setMaxResults(maxResults);
            criteria.addOrder(Order.desc("transferStart"));
            ret = criteria.list();
            for (LocalIRODSTransfer xfer : ret) {
                Hibernate.initialize(xfer.getLocalIRODSTransferItems());
            }
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            log.error("error in findByTransferState(int, TransferState...)", e);
            throw new TransferDAOException("Failed findByTransferState(int, TransferState...)", e);
        } finally {
            session.close();
        }
        if (ret != null) {
            log.debug("entities found: {}", ret.size());
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findByTransferStatus(int, org.irods.jargon.transfer.dao.domain.TransferStatus[])
     */
    @Override
    public List<LocalIRODSTransfer> findByTransferStatus(int maxResults, TransferStatus... transferStatus)
            throws TransferDAOException {
        log.debug("entering findByTransferState(int, TransferStatus...)");
        List<LocalIRODSTransfer> ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(LocalIRODSTransfer.class);
            criteria.add(Restrictions.in("transferStatus", transferStatus));
            criteria.setMaxResults(maxResults);
            criteria.addOrder(Order.desc("transferStart"));
            ret = criteria.list();
            for (LocalIRODSTransfer xfer : ret) {
                Hibernate.initialize(xfer.getLocalIRODSTransferItems());
            }
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            log.error("error in findByTransferState(int, TransferStatus...)", e);
            throw new TransferDAOException("Failed findByTransferState(int, TransferStatus...)", e);
        } finally {
            session.close();
        }
        if (ret != null) {
            log.debug("entities found: {}", ret.size());
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findAllSortedDesc(int)
     */
    @Override
    public List<LocalIRODSTransfer> findAllSortedDesc(int maxResults) throws TransferDAOException {
        log.debug("entering findAllSortedDesc(int)");
        List<LocalIRODSTransfer> ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(LocalIRODSTransfer.class);
            criteria.setMaxResults(maxResults);
            criteria.addOrder(Order.desc("transferStart"));
            ret = criteria.list();
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            log.error("error in findAllSortedDesc(int)", e);
            throw new TransferDAOException("Failed findAllSortedDesc(int)", e);
        } finally {
            session.close();
        }
        if (ret != null) {
            log.debug("entities found: {}", ret.size());
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#findAll()
     */
    @Override
    public List<LocalIRODSTransfer> findAll() throws TransferDAOException {
        log.debug("entering findAll()");
        List<LocalIRODSTransfer> ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(LocalIRODSTransfer.class);
            criteria.addOrder(Order.desc("transferStart"));
            ret = criteria.list();
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            log.error("error in findAll()", e);
            throw new TransferDAOException("Failed findAll()", e);
        } finally {
            session.close();
        }
        if (ret != null) {
            log.debug("entities found: {}", ret.size());
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#purgeQueue()
     */
    @Override
    public void purgeQueue() throws TransferDAOException {
        log.debug("entering purgeQueue()");
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            int rows = super.getHibernateTemplate().bulkUpdate(
                    "delete from LocalIRODSTransfer where transferState <> ?", TransferState.PROCESSING);
            log.debug("updated rows: {}", rows);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            log.error("error in purgeQueue()", e);
            throw new TransferDAOException("Failed purgeQueue()", e);
        } finally {
            session.close();
        }
    }

    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#purgeSuccessful()
     */
    @Override
    public void purgeSuccessful() throws TransferDAOException {
        log.debug("entering purgeSuccessful()");
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            int rows = super
                    .getHibernateTemplate()
                    .bulkUpdate(
                            "delete from LocalIRODSTransfer where transferState = ? or transferState = ? and transferErrorStatus = ?",
                            new Object[] { TransferState.COMPLETE, TransferState.CANCELLED, TransferStatus.OK });
            log.debug("updated rows: {}", rows);
            tx.commit();
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (DataAccessException e) {
            if (tx != null) {
                tx.rollback();
            }
            log.error("error in purgeSuccessful()", e);
            throw new TransferDAOException("Failed purgeSuccessful()", e);
        } finally {
            session.close();
        }
    }

    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#purgeQueueByDate(int)
     */
    @Override
    public void purgeQueueByDate(int retentionDays) throws TransferDAOException {
        log.debug("entering purgeQueueByDate(int)");
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            int rows = super.getHibernateTemplate().bulkUpdate(
                    "delete from LocalIRODSTransfer as transfer where transfer.transferState = ? or transferState = ?",
                    new Object[] { TransferState.COMPLETE, TransferState.CANCELLED });
            log.debug("updated rows: {}", rows);
            tx.commit();
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (DataAccessException e) {
            if (tx != null) {
                tx.rollback();
            }
            log.error("error in purgeQueueByDate(int)", e);
            throw new TransferDAOException("Failed purgeQueueByDate(int)", e);
        } finally {
            session.close();
        }
    }

    /* (non-Javadoc)
     * @see org.irods.jargon.transfer.dao.LocalIRODSTransferDAO#delete(org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer)
     */
    @Override
    public void delete(LocalIRODSTransfer ea) throws TransferDAOException {
        logger.debug("entering delete()");
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(ea);
            tx.commit();
        } catch (HibernateException e) {
            log.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            log.error("error in delete(LocalIRODSTransfer entity)", e);
            throw new TransferDAOException("Failed delete(LocalIRODSTransfer entity)", e);
        } finally {
            session.close();
        }
    }

}
