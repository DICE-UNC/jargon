package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.irods.jargon.transfer.dao.SynchConfigurationDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.SynchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SynchConfigurationDAOImpl extends HibernateDaoSupport implements
		SynchConfigurationDAO {

	private static final Logger log = LoggerFactory
			.getLogger(SynchConfigurationDAOImpl.class);
	
	/*
	 * (non-Javadoc) 
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.SynchConfigurationDAO#save(org.irods.jargon
	 * .transfer.dao.domain.SynchConfiguration)
	 */
	@Override
	public void save(final SynchConfiguration synchConfiguration)
			throws TransferDAOException {
		log.info("entering save(SynchConfiguration)");

		this.getSessionFactory().getCurrentSession()
				.saveOrUpdate(synchConfiguration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.SynchConfigurationDAO#findById(java.lang
	 * .Long)
	 */
	@Override
	public SynchConfiguration findById(final Long id)
			throws TransferDAOException {
		return (SynchConfiguration) this.getSessionFactory()
				.getCurrentSession().get(SynchConfiguration.class, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.SynchConfigurationDAO#findInitializedById
	 * (java.lang.Long)
	 */
	@Override
	public SynchConfiguration findInitializedById(final Long id)
			throws TransferDAOException {
		SynchConfiguration synchConfiguration = (SynchConfiguration) this
				.getSessionFactory().getCurrentSession()
				.get(SynchConfiguration.class, id);
		Hibernate.initialize(synchConfiguration);
		return synchConfiguration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.SynchConfigurationDAO#findAll()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<SynchConfiguration> findAll() throws TransferDAOException {
		List<SynchConfiguration> retList = null;
		Criteria criteria = this.getSessionFactory().getCurrentSession()
				.createCriteria(SynchConfiguration.class);

		retList = criteria.list();
		return retList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.SynchConfigurationDAO#delete(org.irods.
	 * jargon.transfer.dao.domain.SynchConfiguration)
	 */
	@Override
	public void delete(final SynchConfiguration synchConfiguration)
			throws TransferDAOException {
		this.getSessionFactory().getCurrentSession().delete(synchConfiguration);
	}

}
