package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.irods.jargon.transfer.dao.SynchProcessDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.SynchProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class SynchProcessDAOImpl extends HibernateDaoSupport implements
		SynchProcessDAO {

	private static final Logger log = LoggerFactory
			.getLogger(SynchProcessDAOImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.SynchProcessDAO#save(org.irods.jargon.transfer
	 * .dao.domain.SynchProcess)
	 */
	@Override
	public void save(final SynchProcess synchProcess)
			throws TransferDAOException {
		log.info("save(SynchProcess)");

		this.getSessionFactory().getCurrentSession().saveOrUpdate(synchProcess);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.SynchProcessDAO#findById(java.lang.Long)
	 */
	@Override
	public SynchProcess findById(final Long id) throws TransferDAOException {
		return (SynchProcess) this.getSessionFactory().getCurrentSession()
				.get(SynchProcess.class, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.SynchProcessDAO#findAll()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<SynchProcess> findAll() throws TransferDAOException {
		List<SynchProcess> retList = null;
		Criteria criteria = this.getSessionFactory().getCurrentSession()
				.createCriteria(SynchProcess.class);

		retList = criteria.list();
		return retList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.SynchProcessDAO#delete(org.irods.jargon
	 * .transfer.dao.domain.SynchProcess)
	 */
	@Override
	public void delete(final SynchProcess synchProcess)
			throws TransferDAOException {
		this.getSessionFactory().getCurrentSession().delete(synchProcess);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.SynchProcessDAO#findInitializedById(java
	 * .lang.Long)
	 */
	@Override
	public SynchProcess findInitializedById(final Long id)
			throws TransferDAOException {
		SynchProcess synchProcess = (SynchProcess) this.getSessionFactory()
				.getCurrentSession().get(SynchProcess.class, id);
		Hibernate.initialize(synchProcess);
		return synchProcess;
	}

}
