package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.irods.jargon.transfer.dao.SynchronizationDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author jdr0887
 * 
 */
public class SynchronizationDAOImpl extends HibernateDaoSupport implements SynchronizationDAO {

    private static final Logger logger = LoggerFactory.getLogger(SynchronizationDAOImpl.class);

    public SynchronizationDAOImpl() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.irods.jargon.transfer.dao.AccountDAO#save(org.irods.jargon.transfer.dao.domain.Account)
     */
    @Override
    public void save(Synchronization ea) throws TransferDAOException {
        logger.debug("entering save(Synchronization)");
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
            logger.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error("error in save(Account)", e);
            throw new TransferDAOException("Failed save(Account)", e);
        } finally {
            session.flush();
            session.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.irods.jargon.transfer.dao.AccountDAO#findByName(java.lang.String)
     */
    @Override
    public Synchronization findByName(String name) throws TransferDAOException {
        logger.debug("entering findByName(String)");
        Synchronization ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(Synchronization.class);
            criteria.add(Restrictions.eq("name", name));
            ret = (Synchronization) criteria.uniqueResult();
        } catch (Exception e) {
            logger.error("error in findByName(String)", e);
            throw new TransferDAOException("Failed findByName(String)", e);
        } finally {
            session.close();
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.irods.jargon.transfer.dao.AccountDAO#findAll()
     */
    @Override
    public List<Synchronization> findAll() throws TransferDAOException {
        logger.debug("entering findAll()");
        List<Synchronization> ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(Synchronization.class);
            ret = criteria.list();
        } catch (Exception e) {
            logger.error("error in findAll()", e);
            throw new TransferDAOException("Failed findAll()", e);
        } finally {
            session.close();
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.irods.jargon.transfer.dao.AccountDAO#findById(java.lang.Long)
     */
    @Override
    public Synchronization findById(Long id) throws TransferDAOException {
        logger.debug("entering findById(Long)");
        Synchronization ret = null;
        Session session = getSession();
        try {
            Criteria criteria = session.createCriteria(Synchronization.class);
            criteria.add(Restrictions.eq("id", id));
            ret = (Synchronization) criteria.uniqueResult();
        } catch (Exception e) {
            logger.error("error in findById(Long)", e);
            throw new TransferDAOException("Failed findById(Long)", e);
        } finally {
            session.close();
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.irods.jargon.transfer.dao.AccountDAO#delete(org.irods.jargon.transfer.dao.domain.Account)
     */
    @Override
    public void delete(Synchronization ea) throws TransferDAOException {
        logger.debug("entering delete()");
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(ea);
            tx.commit();
        } catch (HibernateException e) {
            logger.error("HibernateException", e);
            throw new TransferDAOException(e);
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error("error in delete(LocalIRODSTransfer entity)", e);
            throw new TransferDAOException("Failed delete(LocalIRODSTransfer entity)", e);
        } finally {
            session.close();
        }
    }

}
