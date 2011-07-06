package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
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
        logger.debug("entering save(Account)");
        this.getSessionFactory().getCurrentSession().saveOrUpdate(ea);
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
        // Criteria criteria = this.getSessionFactory().getCurrentSession().createCriteria(Synchronization.class);
        // criteria.add(Restrictions.eq("name", name));
        // ret = (Synchronization) criteria.uniqueResult();
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
        Criteria criteria = this.getSessionFactory().getCurrentSession().createCriteria(Synchronization.class);
        ret = criteria.list();
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
        return (Synchronization) this.getSessionFactory().getCurrentSession().get(Synchronization.class, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.irods.jargon.transfer.dao.AccountDAO#delete(org.irods.jargon.transfer.dao.domain.Account)
     */
    @Override
    public void delete(Synchronization ea) throws TransferDAOException {
        logger.debug("entering delete()");
        this.getSessionFactory().getCurrentSession().delete(ea);
    }

}
